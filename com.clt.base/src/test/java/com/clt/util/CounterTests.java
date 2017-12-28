package com.clt.util;

import junit.framework.TestCase;
import org.junit.Test;

public class CounterTests extends TestCase {

    Counter counter;

    @Override
    public void setUp() {
        counter = new Counter();
    }

    @Test
    public void testNewCounterHasCountZero() {
        assertEquals(counter.get(), 0);
    }

    @Test
    public void testSetCounterValue() {
        counter.set(42);
        assertEquals(counter.get(), 42);
    }

    @Test
    public void testIncreaseAndDecreaseCounter() {
        counter.increase();
        counter.increase();
        assertEquals(counter.get(), 2);
        counter.decrease();
        assertEquals(counter.get(), 1);
    }

    @Test
    public void testCounterToString() {
        counter.set(42);
        assertEquals(counter.toString(), "42");
    }
}