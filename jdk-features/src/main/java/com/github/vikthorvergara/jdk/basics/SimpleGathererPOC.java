package com.github.vikthorvergara.jdk.basics;

import java.util.stream.Gatherers;
import java.util.stream.Stream;

public class SimpleGathererPOC {

    public static void main(String[] args) {
        var fixed = Stream.of(1, 2, 3, 4, 5, 6, 7)
                .gather(Gatherers.windowFixed(3))
                .toList();
        System.out.println("windowFixed(3): " + fixed);

        var sliding = Stream.of(1, 2, 3, 4, 5)
                .gather(Gatherers.windowSliding(3))
                .toList();
        System.out.println("windowSliding(3): " + sliding);

        var runningSum = Stream.of(1, 2, 3, 4, 5)
                .gather(Gatherers.scan(() -> 0, Integer::sum))
                .toList();
        System.out.println("scan(sum): " + runningSum);

        var folded = Stream.of("a", "b", "c", "d")
                .gather(Gatherers.fold(() -> "", String::concat))
                .toList();
        System.out.println("fold(concat): " + folded);
    }
}
