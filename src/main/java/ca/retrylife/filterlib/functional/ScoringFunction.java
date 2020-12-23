package ca.retrylife.filterlib.functional;

@FunctionalInterface
public interface ScoringFunction<T> {

    public double score(T item, double score);

}