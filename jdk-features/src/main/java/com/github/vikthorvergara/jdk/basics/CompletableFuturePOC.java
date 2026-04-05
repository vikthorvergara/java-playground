package com.github.vikthorvergara.jdk.basics;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class CompletableFuturePOC {

    static int sleepAndReturn(int value, long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
        return value;
    }

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        var single = CompletableFuture
                .supplyAsync(() -> sleepAndReturn(10, 50))
                .thenApply(v -> v * 2)
                .thenApply(v -> "result=" + v);
        System.out.println(single.get());

        var a = CompletableFuture.supplyAsync(() -> sleepAndReturn(3, 100));
        var b = CompletableFuture.supplyAsync(() -> sleepAndReturn(4, 100));
        var combined = a.thenCombine(b, Integer::sum);
        System.out.println("combined=" + combined.get());

        var failing = CompletableFuture
                .supplyAsync(() -> {
                    throw new IllegalStateException("boom");
                })
                .exceptionally(ex -> "recovered: " + ex.getCause().getMessage());
        System.out.println(failing.get());

        var any = CompletableFuture.anyOf(
                CompletableFuture.supplyAsync(() -> sleepAndReturn(1, 200)),
                CompletableFuture.supplyAsync(() -> sleepAndReturn(2, 50)),
                CompletableFuture.supplyAsync(() -> sleepAndReturn(3, 300))
        );
        System.out.println("anyOf first=" + any.get());

        var all = CompletableFuture.allOf(
                CompletableFuture.runAsync(() -> sleepAndReturn(0, 30)),
                CompletableFuture.runAsync(() -> sleepAndReturn(0, 60)),
                CompletableFuture.runAsync(() -> sleepAndReturn(0, 90))
        );
        all.get();
        System.out.println("allOf done");
    }
}
