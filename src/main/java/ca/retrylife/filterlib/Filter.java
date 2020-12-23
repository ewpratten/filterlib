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

/**
 * Filter is a utility class for easily sorting and selecting a single object or
 * a group of objects based on simple rules.
 * 
 * @param <T> Common type of all objects being operated on
 */
public class Filter<T> {

    // Mapping of all objects to their scores
    private HashMap<T, @Nullable Double> objectScores = new HashMap<>();

    // Ordered list of objects by their score
    private List<T> orderedObjects = null;

    /**
     * Create a Filter from a list of items
     * 
     * @param items Items
     */
    public Filter(List<T> items) {

        // Handle mapping all items
        items.forEach((T item) -> {
            this.objectScores.put(item, 0.0);
        });

        // Reset the ordered list cache
        this.orderedObjects = null;
    }

    /**
     * Create a Filter from an array of items
     * 
     * @param items Items
     */
    public Filter(T[] items) {

        // Handle mapping all items
        for (T item : items) {
            this.objectScores.put(item, 0.0);
        }

        // Reset the ordered list cache
        this.orderedObjects = null;
    }

    /**
     * Assign a score for each item
     * 
     * @param fun Scoring function
     * 
     *            <pre>
     * (T item) -> double
     *            </pre>
     */
    public void score(Function<T, Double> fun) {
        this.score((T item, Double score) -> fun.apply(item));
    }

    /**
     * Assign a score for each item, based on it's previous score
     * 
     * @param fun Scoring function
     * 
     *            <pre>
     * (T item, double previousScore) -> double
     *            </pre>
     */
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

    /**
     * Remove any marked items
     * 
     * @param fun Function that returns true when an item should be removed
     * 
     *            <pre>
     * (T item) -> boolean
     *            </pre>
     */
    public void remove(Function<T, Boolean> fun) {
        this.remove((T item, Double score) -> fun.apply(item));
    }

    /**
     * Remove any marked items based on their score
     * 
     * @param fun Function that returns true when an item should be removed
     * 
     *            <pre>
     * (T item, double score) -> boolean
     *            </pre>
     */
    public void remove(BiFunction<T, Double, Boolean> fun) {

        // Setting a score to NULL will remove it. This just uses that logic
        this.score((T item, Double score) -> fun.apply(item, score) ? null : score);
    }

    /**
     * Remove a single item
     * 
     * @param singleItem Item
     */
    public void remove(T singleItem) {
        this.objectScores.put(singleItem, null);

        // Reset the ordered list cache
        this.orderedObjects = null;
    }

    /**
     * Keep only marked items, remove the rest
     * 
     * @param fun Function that returns true when an item should be kept
     * 
     *            <pre>
     * (T item) -> boolean
     *            </pre>
     */
    public void keepOnly(Function<T, Boolean> fun) {
        this.keepOnly((T item, Double score) -> fun.apply(item));
    }

    /**
     * Keep any marked items based on their score, remove the rest
     * 
     * @param fun Function that returns true when an item should be kept
     * 
     *            <pre>
     * (T item, double score) -> boolean
     *            </pre>
     */
    public void keepOnly(BiFunction<T, Double, Boolean> fun) {

        // This function is the inverse of remove(). Just use that logic
        this.remove((T item, Double score) -> !fun.apply(item, score));
    }

    /**
     * Resets all scores, and re-adds removed items
     */
    public void reset() {

        // Set all scores to 0
        this.objectScores.replaceAll((T item, Double score) -> 0.0);

        // Reset the ordered list cache
        this.orderedObjects = null;
    }

    /**
     * Get the number of items that have not been removed
     * 
     * @return Number of items
     */
    public int getCount() {

        int count = 0;

        for (Double score : this.objectScores.values()) {
            if (score != null) {
                count++;
            }
        }

        return count;
    }

    /**
     * Get if the Filter has no items left in it
     * 
     * @return Is empty?
     */
    public boolean isEmpty() {
        return getCount() == 0;
    }

    /**
     * Iterate over each item
     * 
     * @param consumer Callback for every item
     * 
     *                 <pre>
     * (T item) -> void
     *                 </pre>
     */
    public void forEach(Consumer<T> consumer) {
        this.forEach((T item, Double score) -> consumer.accept(item));
    }

    /**
     * Iterate over each item, along with it's score
     * 
     * @param consumer Callback for every item
     * 
     *                 <pre>
     * (T item, double score) -> void
     *                 </pre>
     */
    public void forEach(BiConsumer<T, Double> consumer) {
        this.objectScores.forEach((T item, Double score) -> {
            if (item != null) {
                consumer.accept(item, score);
            }
        });
    }

    /**
     * Iterate over each item that has been removed from the Filter
     * 
     * @param consumer Callback for every item
     * 
     *                 <pre>
     * (T item) -> void
     *                 </pre>
     */
    public void forEachRemoved(Consumer<T> consumer) {
        this.objectScores.forEach((T item, Double score) -> {
            if (item == null) {
                consumer.accept(item);
            }
        });
    }

    /**
     * Get the best item, or null if none matches that criteria
     * 
     * @return The item with the highest score
     */
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

    /**
     * Pass the best item (item with the highest score) to a function
     * 
     * @param consumer Function
     * 
     *                 <pre>
     * (T item) -> void
     *                 </pre>
     */
    public void withBest(Consumer<T> consumer) {

        // Get the best item
        T best = getBest();

        // Accept if not null
        if (best != null) {
            consumer.accept(best);
        }
    }

    /**
     * Get the worst item, or null if none matches that criteria
     * 
     * @return The item with the lowest score (not including removed items)
     */
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

    /**
     * Pass the worst item (item with the lowest score) to a function
     * 
     * @param consumer Function
     * 
     *                 <pre>
     * (T item) -> void
     *                 </pre>
     */
    public void withWorst(Consumer<T> consumer) {

        // Get the worst item
        T worst = getWorst();

        // Accept if not null
        if (worst != null) {
            consumer.accept(worst);
        }
    }

    /**
     * Get an {@link ImmutableList} of items that have a score greater than the
     * threshold
     * 
     * @param threshold Threshold value
     * @return List of items above threshold
     */
    public ImmutableList<T> getAboveThreshold(double threshold) {

        List<T> output = new ArrayList<T>();

        forEachAboveThreshold(threshold, (T object) -> output.add(object));

        return ImmutableList.copyOf(output);
    }

    /**
     * Iterate over each item with a score greater than the threshold
     * 
     * @param threshold Threshold value
     * @param consumer  Function
     * 
     *                  <pre>
     * (T item) -> void
     *                  </pre>
     */
    public void forEachAboveThreshold(double threshold, Consumer<T> consumer) {
        this.forEachAboveThreshold(threshold, (T item, Double score) -> consumer.accept(item));
    }

    /**
     * Iterate over each item with a score greater than the threshold
     * 
     * @param threshold Threshold value
     * @param consumer  Function
     * 
     *                  <pre>
     * (T item, double score) -> void
     *                  </pre>
     */
    public void forEachAboveThreshold(double threshold, BiConsumer<T, Double> consumer) {
        this.forEach((T item, Double score) -> {
            if (score > threshold) {
                consumer.accept(item, score);
            }
        });
    }

    /**
     * Get an {@link ImmutableList} of items that have a score less than the
     * threshold
     * 
     * @param threshold Threshold value
     * @return List of items below threshold
     */
    public ImmutableList<T> getBelowThreshold(double threshold) {

        List<T> output = new ArrayList<T>();

        forEachBelowThreshold(threshold, (T object) -> output.add(object));

        return ImmutableList.copyOf(output);

    }

    /**
     * Iterate over each item with a score less than the threshold
     * 
     * @param threshold Threshold value
     * @param consumer  Function
     * 
     *                  <pre>
     * (T item) -> void
     *                  </pre>
     */
    public void forEachBelowThreshold(double threshold, Consumer<T> consumer) {
        this.forEachBelowThreshold(threshold, (T item, Double score) -> consumer.accept(item));
    }

    /**
     * Iterate over each item with a score less than the threshold
     * 
     * @param threshold Threshold value
     * @param consumer  Function
     * 
     *                  <pre>
     * (T item, double score) -> void
     *                  </pre>
     */
    public void forEachBelowThreshold(double threshold, BiConsumer<T, Double> consumer) {
        this.forEach((T item, Double score) -> {
            if (score < threshold) {
                consumer.accept(item, score);
            }
        });
    }

    /**
     * Get an ordered {@link ImmutableList} of all items, sorted by score
     * 
     * @return Ordered list
     */
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

    /**
     * Get an {@link ImmutableList} of all remaining items
     * 
     * @return Remaining items
     */
    public ImmutableList<T> getRemaining() {
        List<T> output = new ArrayList<T>();

        forEach((T object) -> output.add(object));

        return ImmutableList.copyOf(output);
    }

    /**
     * Get an {@link ImmutableList} of all removed items
     * 
     * @return Removed items
     */
    public ImmutableList<T> getRemoved() {
        List<T> output = new ArrayList<T>();

        forEachRemoved((T object) -> output.add(object));

        return ImmutableList.copyOf(output);
    }

}