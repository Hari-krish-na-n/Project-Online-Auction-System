package com.auction.util;

import java.util.Collection;
import java.util.Objects;

/**
 * Generic Utility Demonstrations for Day 9:
 *
 * Concepts Demonstrated:
 * 1. Pair<K, V>: Generic container holding two differently typed elements.
 * 2. Box<T extends Number>: Generic class with an upper bound constraint.
 * 3. Generic max method: <T extends Comparable<T>> T max(T a, T b).
 * 4. PECS Rule (Producer Extends, Consumer Super):
 *    - ? extends Number: Producer (reading numbers from collection)
 *    - ? super Integer: Consumer (adding/writing integers to collection)
 * 5. Type Erasure: Java generics exist for compile-time safety and are erased to their bounds/Object at runtime.
 */
public final class GenericUtils {

    private GenericUtils() {
        // Utility class: prevent instantiation
    }

    /**
     * Generic Pair holding two values of potentially different types K and V.
     */
    public static final class Pair<K, V> {
        private final K first;
        private final V second;

        public Pair(K first, V second) {
            this.first = first;
            this.second = second;
        }

        public static <K, V> Pair<K, V> of(K first, V second) {
            return new Pair<>(first, second);
        }

        public K getFirst() {
            return first;
        }

        public V getSecond() {
            return second;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Pair<?, ?> pair = (Pair<?, ?>) o;
            return Objects.equals(first, pair.first) && Objects.equals(second, pair.second);
        }

        @Override
        public int hashCode() {
            return Objects.hash(first, second);
        }

        @Override
        public String toString() {
            return "Pair[" + first + ", " + second + "]";
        }
    }

    /**
     * Bounded Generic Box: Accepts only numeric types (e.g. Integer, Double, BigDecimal, Long).
     */
    public static final class Box<T extends Number> {
        private final T value;

        public Box(T value) {
            this.value = Objects.requireNonNull(value, "Box value cannot be null");
        }

        public T getValue() {
            return value;
        }

        public double doubleValue() {
            return value.doubleValue();
        }

        public boolean isGreaterThan(Box<? extends Number> other) {
            return this.doubleValue() > other.doubleValue();
        }

        @Override
        public String toString() {
            return "Box[" + value + "]";
        }
    }

    /**
     * Generic max method using an upper bound: <T extends Comparable<T>>.
     * Guarantees that elements passed can be compared to one another.
     */
    public static <T extends Comparable<T>> T max(T a, T b) {
        if (a == null && b == null) return null;
        if (a == null) return b;
        if (b == null) return a;
        return a.compareTo(b) >= 0 ? a : b;
    }

    /**
     * Demonstrates Producer Extends (? extends Number):
     * Reads numbers from a collection and computes their total sum.
     * The collection produces numbers, so we use ? extends Number.
     */
    public static double sumOfNumbers(Collection<? extends Number> numbers) {
        if (numbers == null) return 0.0;
        double sum = 0.0;
        for (Number n : numbers) {
            if (n != null) {
                sum += n.doubleValue();
            }
        }
        return sum;
    }

    /**
     * Demonstrates Consumer Super (? super Integer):
     * Adds integer values into a collection.
     * The collection consumes integers, so we use ? super Integer.
     */
    public static void populateIntegers(Collection<? super Integer> consumer, int count) {
        if (consumer == null) return;
        for (int i = 1; i <= count; i++) {
            consumer.add(i);
        }
    }
}
