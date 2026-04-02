package com.github.vikthorvergara.jdk.basics;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Gatherer;
import java.util.stream.Gatherers;
import java.util.stream.Stream;

public class SimpleGathererPOC {

    static <T> Gatherer<T, ?, List<T>> chunkOf(int size) {
        return Gatherer.<T, ArrayList<T>, List<T>>ofSequential(
                ArrayList::new,
                (buffer, element, downstream) -> {
                    buffer.add(element);
                    if (buffer.size() == size) {
                        var chunk = List.copyOf(buffer);
                        buffer.clear();
                        return downstream.push(chunk);
                    }
                    return true;
                },
                (buffer, downstream) -> {
                    if (!buffer.isEmpty()) {
                        downstream.push(List.copyOf(buffer));
                    }
                }
        );
    }

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

        var chunks = Stream.of("a", "b", "c", "d", "e")
                .gather(chunkOf(2))
                .toList();
        System.out.println("custom chunkOf(2): " + chunks);
    }
}
