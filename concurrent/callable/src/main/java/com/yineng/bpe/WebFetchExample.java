package com.yineng.bpe;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class WebFetchExample {
    public static void run() {
        List<String> urls = Arrays.asList(
                "https://www.example.com/",
                "https://www.baidu.com/",
                "https://httpbin.org/status/404"
        );
        ExecutorService executor = Executors.newFixedThreadPool(3);
        HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(2)).build();
        try {
            List<Callable<FetchResult>> tasks = new ArrayList<>();
            for (String url : urls) {
                tasks.add(() -> fetch(client, url));
            }
            List<Future<FetchResult>> futures = executor.invokeAll(tasks, 5, TimeUnit.SECONDS);
            for (int i = 0; i < urls.size(); i++) {
                Future<FetchResult> f = futures.get(i);
                if (f.isCancelled()) {
                    System.out.println("WebFetchExample url=" + urls.get(i) + " cancelled");
                } else {
                    try {
                        FetchResult r = f.get();
                        System.out.println("WebFetchExample url=" + r.url + " status=" + r.status + " bytes=" + r.bytes);
                    } catch (Exception e) {
                        System.out.println("WebFetchExample url=" + urls.get(i) + " error=" + e.getClass().getSimpleName() + ":" + e.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("WebFetchExample error=" + e.getClass().getSimpleName() + ":" + e.getMessage());
        } finally {
            executor.shutdown();
        }
    }

    private static FetchResult fetch(HttpClient client, String url) throws Exception {
        HttpRequest req = HttpRequest.newBuilder(URI.create(url))
                .timeout(Duration.ofSeconds(3))
                .GET()
                .build();
        HttpResponse<byte[]> resp = client.send(req, HttpResponse.BodyHandlers.ofByteArray());
        return new FetchResult(url, resp.statusCode(), resp.body() == null ? 0 : resp.body().length);
    }

    private static class FetchResult {
        final String url;
        final int status;
        final int bytes;

        FetchResult(String url, int status, int bytes) {
            this.url = url;
            this.status = status;
            this.bytes = bytes;
        }
    }
}
