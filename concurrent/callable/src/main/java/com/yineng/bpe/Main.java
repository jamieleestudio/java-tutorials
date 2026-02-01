package com.yineng.bpe;

public class Main {
    public static void main(String[] args) {
        System.out.println("Callable/Future demos start");
        BasicCallableExample.run();
        InvokeAllExample.run();
        TimeoutCancelExample.run();
        ExceptionPropagationExample.run();
        CompletionServiceExample.run();
        FutureTaskExample.run();
        WebFetchExample.run();
        System.out.println("Callable/Future demos end");
    }
}
