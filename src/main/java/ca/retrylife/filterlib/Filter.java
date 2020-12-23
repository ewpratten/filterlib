package ca.retrylife.filterlib;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.Map.Entry;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableList;

public class Filter<T> {

    // Mapping of all objects to their scores
    private HashMap<T, @Nullable Double> objectScores = new HashMap<>();

    // Ordered list of objects by their score
    private List<T> orderedObjects = null;

    public Filter(List<T> items) {

        // Handle mapping all items
        items.forEach((T item) -> {
            this.objectScores.put(item, 0.0);
        });

        // Reset the ordered list cache
        this.orderedObjects = null;
    }

    public Filter(T[] items) {

        // Handle mapping all items
        for (T item : items) {
            this.objectScores.put(item, 0.0);
        }

        // Reset the ordered list cache
        this.orderedObjects = null;
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
            this.objectScores.put(item, newScore);
        });

        // Reset the ordered list cache
        this.orderedObjects = null;
    }

    public void remove(Function<T, Boolean> fun) {
        this.remove((T item, Double score) -> fun.apply(item));
    }

    public void remove(BiFunction<T, Double, Boolean> fun) {

        // Setting a score to NULL will remove it. This just uses that logic
        this.score((T item, Double score) -> fun.apply(item, score) ? null : score);
    }

    public void remove(T singleItem) {
        this.objectScores.put(singleItem, null);

        // Reset the ordered list cache
        this.orderedObjects = null;
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
        this.objectScores.replaceAll((T item, Double score) -> 0.0);

        // Reset the ordered list cache
        this.orderedObjects = null;
    }

    public int getCount() {

        int count = 0;

        for (Double score : this.objectScores.values()) {
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
        this.objectScores.forEach((T item, Double score) -> {
            if (item != null) {
                consumer.accept(item, score);
            }
        });
    }

    public void forEachRemoved(Consumer<T> consumer) {
        this.objectScores.forEach((T item, Double score) -> {
            if (item == null) {
                consumer.accept(item);
            }
        });
    }

    public @Nullable T getBest() {
        // Get the ordered list
        List<T> ordered = this.getOrdered();

        // Ensure there is data
        if (ordered.size() > 0) {
            return ordered.get(0);
        } else {
            return null;
        }
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
        // Get the ordered list
        List<T> ordered = this.getOrdered();

        // Ensure there is data
        if (ordered.size() > 0) {
            return ordered.get(ordered.size() - 1);
        } else {
            return null;
        }
    }

    public void withWorst(Consumer<T> consumer) {

        // Get the worst item
        T worst = getWorst();

        // Accept if not null
        if (worst != null) {
            consumer.accept(worst);
        }
    }

    public ImmutableList<T> getAboveThreshold(double threshold) {

        List<T> output = new ArrayList<T>();

        forEachAboveThreshold(threshold, (T object) -> output.add(object));

        return ImmutableList.copyOf(output);
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

    public ImmutableList<T> getBelowThreshold(double threshold) {

        List<T> output = new ArrayList<T>();

        forEachBelowThreshold(threshold, (T object) -> output.add(object));

        return ImmutableList.copyOf(output);

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

    public ImmutableList<T> getOrdered() {

        // If there is not already a cached ordered list, create it
        if (this.orderedObjects == null) {

            // Get entry list
            Set<Entry<T, Double>> entrySet = this.objectScores.entrySet();
            List<Entry<T, Double>> entryList = new ArrayList<Entry<T, Double>>(entrySet);

            // Sort the list
            Collections.sort(entryList, new Comparator<Entry<T, Double>>() {
                @Override
                public int compare(Entry<T, Double> obj1, Entry<T, Double> obj2) {
                    return obj2.getValue().compareTo(obj2.getValue());
                }
            });

            // Allocate a new array
            this.orderedObjects = new LinkedList<T>();

            // Add all remaining objects in order
            for (Entry<T, Double> entry : entryList) {
                this.orderedObjects.add(entry.getKey());
            }

        }

        return ImmutableList.copyOf(this.orderedObjects);
    }

    public ImmutableList<T> getRemaining() {
        List<T> output = new ArrayList<T>();

        forEach((T object) -> output.add(object));

        return ImmutableList.copyOf(output);
    }

    public ImmutableList<T> getRemoved() {
        List<T> output = new ArrayList<T>();

        forEachRemoved((T object) -> output.add(object));

        return ImmutableList.copyOf(output);
    }

}