package ca.retrylife.filterlib;

import java.util.HashMap;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

import javax.annotation.Nullable;

public class Filter<T> {

    private HashMap<T, @Nullable Double> scores = new HashMap<>();

    public Filter(List<T> items) {

        // Handle mapping all items
        items.forEach((T item) -> {
            this.scores.put(item, 0.0);
        });
    }

    public Filter(T[] items) {

        // Handle mapping all items
        for (T item : items) {
            this.scores.put(item, 0.0);
        }
    }

    public void score(Function<T, Double> fun) {
        this.score((T item, Double score) -> fun.apply(item));
    }

    public void score(BiFunction<T, Double, Double> fun) {

        // Handle every item that is not removed
        this.forEach((T item, Double score) -> {

            // Fetch the new score
            Double newScore = fun.apply(item, score);

            // Set the score
            this.scores.put(item, newScore);
        });
    }

    public void remove(Function<T, Boolean> fun) {
        this.remove((T item, Double score) -> fun.apply(item));
    }

    public void remove(BiFunction<T, Double, Boolean> fun) {

        // Setting a score to NULL will remove it. This just uses that logic
        this.score((T item, Double score) -> fun.apply(item, score) ? null : score);
    }

    public void remove(T singleItem) {
        this.scores.put(singleItem, null);
    }

    public void keepOnly(Function<T, Boolean> fun) {
        this.keepOnly((T item, Double score) -> fun.apply(item));
    }

    public void keepOnly(BiFunction<T, Double, Boolean> fun) {

        // This function is the inverse of remove(). Just use that logic
        this.remove((T item, Double score) -> !fun.apply(item, score));
    }

    public void reset() {

        // Set all scores to 0
        this.scores.replaceAll((T item, Double score) -> 0.0);
    }

    public int getCount() {

        int count = 0;

        for (Double score : this.scores.values()) {
            if (score != null) {
                count++;
            }
        }

        return count;
    }

    public boolean isEmpty() {
        return getCount() == 0;
    }

    public void forEach(Consumer<T> consumer) {
        this.forEach((T item, Double score) -> consumer.accept(item));
    }

    public void forEach(BiConsumer<T, Double> consumer) {
        this.scores.forEach((item, score) -> {
            if (item != null) {
                consumer.accept(item, score);
            }
        });
    }

    public @Nullable T getBest() {
        return null;
    }

    public void withBest(Consumer<T> consumer) {

        // Get the best item
        T best = getBest();

        // Accept if not null
        if (best != null) {
            consumer.accept(best);
        }
    }

    public @Nullable T getWorst() {
        return null;
    }

    public void withWorst(Consumer<T> consumer) {

        // Get the worst item
        T worst = getWorst();

        // Accept if not null
        if (worst != null) {
            consumer.accept(worst);
        }
    }

    public T[] getAboveThreshold(double threshold) {
        return null;
    }

    public void forEachAboveThreshold(double threshold, Consumer<T> consumer) {
        this.forEachAboveThreshold(threshold, (T item, Double score) -> consumer.accept(item));
    }

    public void forEachAboveThreshold(double threshold, BiConsumer<T, Double> consumer) {
        this.forEach((T item, Double score) -> {
            if (score > threshold) {
                consumer.accept(item, score);
            }
        });
    }

    public T[] getBelowThreshold(double threshold) {
        return null;
    }

    public void forEachBelowThreshold(double threshold, Consumer<T> consumer) {
        this.forEachBelowThreshold(threshold, (T item, Double score) -> consumer.accept(item));
    }

    public void forEachBelowThreshold(double threshold, BiConsumer<T, Double> consumer) {
        this.forEach((T item, Double score) -> {
            if (score < threshold) {
                consumer.accept(item, score);
            }
        });
    }

    public T[] getRemaining() {
        return null;
    }

    public T[] getRemoved() {
        return null;
    }

}