package ca.retrylife.filterlib;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class FilterTest {

    @Test
    public void testCounting() {

        // Set up a filter
        Filter<Integer> filter = new Filter<Integer>(new Integer[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 });

        // Check count
        assertEquals(10, filter.getCount());
        assertFalse("Filter empty", filter.isEmpty());

    }

    @Test
    public void testScoring() {

        // Set up a filter
        Filter<Integer> filter = new Filter<Integer>(new Integer[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 });

        // Give everything above 5 a positive score
        filter.score((value) -> (value > 5) ? 1.0 : 0.0);

        // Check scores
        for (int value : filter.getAboveThreshold(0)) {
            assertTrue(String.format("%d is greater than 5", value), value > 5);
        }
    }

    @Test
    public void testRemoval() {

        // Set up a filter
        Filter<Integer> filter = new Filter<Integer>(new Integer[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 });

        // Remove everything less than 3
        filter.remove((val) -> val < 3);

        // Check remaining values
        for (int value : filter.getRemaining()) {
            assertTrue(String.format("%d is GTE 3", value), value >= 3);
        }

        // Check removed values
        for (int value : filter.getRemoved()) {
            assertTrue(String.format("%d is less than 3", value), value < 3);
        }

    }

    @Test
    public void testOrdering() {

        // Set up a filter
        Filter<Integer> filter = new Filter<Integer>(new Integer[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 });

        // Set every value's score to itself
        filter.score((val) -> (double) val);

        // Ensure the ordered list is correct
        assertArrayEquals(new Integer[] { 10, 9, 8, 7, 6, 5, 4, 3, 2, 1 }, filter.getOrdered().toArray());

    }

}