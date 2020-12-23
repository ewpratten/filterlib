package ca.retrylife.filterlib;

import java.util.HashMap;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import javax.annotation.Nullable;

import ca.retrylife.filterlib.functional.ScoringFunction;

public class Filter<T> {

    private HashMap<T, @Nullable Double> scores = new HashMap<>();

    public Filter(List<T> items) {

        // Handle mapping all items
        items.forEach((item) -> {
            this.scores.put(item, 0.0);
        });
    }

    public Filter(T[] items) {

        // Handle mapping all items
        for (T item : items) {
            this.scores.put(item, 0.0);
        }
    }

    public void score(ScoringFunction<T> fun) {

    }
    
    public void remove() {

    }
    
    public void remove(T singleItem) {
        this.scores.remove(singleItem);
    }

    public void keepOnly() {
        
    }

    public void reset() {
        
    }

    public int getCount(){
        return 0;
    }

    public boolean isEmpty() {
        return getCount() == 0;
    }

    public void forEach(Consumer<T> consumer) {
        this.forEach((item, score) -> {
                consumer.accept(item);
        });
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

    public void withBest(Consumer< T> consumer) {
        
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
        
    }

    public void forEachAboveThreshold(double threshold, BiConsumer<T, Double> consumer) {
        
    }

    public T[] getBelowThreshold(double threshold) {
        return null;
    }

    public void forEachBelowThreshold(double threshold, Consumer<T> consumer) {
        
    }

    public void forEachBelowThreshold(double threshold, BiConsumer<T, Double> consumer) {
        
    }

    public T[] getRemaining() {
        return null;
    }

    public T[] getRemoved(){
        return null;
    }

}