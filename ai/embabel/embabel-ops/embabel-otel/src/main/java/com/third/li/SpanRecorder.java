package com.third.li;

import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 把每个 Observation 记录成一条可查询的 span。
 *
 * <p>这是"可观测性后端"的最小替代品：真实项目里换成
 * {@code micrometer-tracing-bridge-otel} + OTLP exporter，
 * 这些 span 就会被导出到 Jaeger/Tempo 等后端——**代码不用改**，
 * 因为框架只依赖 {@code Observation} 这个抽象。
 *
 * <p>框架调用 {@code AgentInstrumentation.observe} 时传进来的
 * {@link Observation.Context} 已经带好了语义信息（操作类型、进程 ID 等），
 * 所以这里只负责计时与收集，不需要自己埋点。
 */
@Component
public class SpanRecorder implements ObservationHandler<Observation.Context> {

    private static final Logger log = LoggerFactory.getLogger(SpanRecorder.class);

    private final List<SpanReport.Span> spans = Collections.synchronizedList(new ArrayList<>());
    private final Map<Observation.Context, Long> startNanos = new ConcurrentHashMap<>();

    @Override
    public boolean supportsContext(Observation.Context context) {
        return true;
    }

    @Override
    public void onStart(Observation.Context context) {
        startNanos.put(context, System.nanoTime());
    }

    @Override
    public void onStop(Observation.Context context) {
        Long start = startNanos.remove(context);
        double durationMillis = start == null ? 0.0 : (System.nanoTime() - start) / 1_000_000.0;
        Map<String, String> tags = new LinkedHashMap<>();
        context.getLowCardinalityKeyValues().forEach(keyValue ->
                tags.put(keyValue.getKey(), keyValue.getValue()));

        SpanReport.Span span = new SpanReport.Span(
                context.getName(),
                Math.round(durationMillis * 100) / 100.0,
                context.getError() != null,
                tags);
        spans.add(span);
        log.info("span: name={}, {}ms, error={}, tags={}",
                span.name(), span.durationMillis(), span.error(), span.tags());
    }

    public List<SpanReport.Span> spans() {
        return List.copyOf(spans);
    }

    public void clear() {
        spans.clear();
        startNanos.clear();
    }
}
