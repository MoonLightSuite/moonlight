package io.github.moonlightsuite.moonlight.signal.online;

import io.github.moonlightsuite.moonlight.core.base.Box;
import io.github.moonlightsuite.moonlight.domain.DoubleDomain;
import io.github.moonlightsuite.moonlight.online.signal.OnlineSignal;
import io.github.moonlightsuite.moonlight.online.signal.Update;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OnlineSignalTest {

    @Test
    void refine1() {
        DoubleDomain domain = new DoubleDomain();
        Box<Double> data = new Box<>(10.0,30.0);
        Box<Double> any = new Box<>(
                Double.NEGATIVE_INFINITY,
                Double.POSITIVE_INFINITY);
        OnlineSignal<Double> signal  = new OnlineSignal<>(domain);

        signal.refine(new Update<>(0.0, 10.0, data));

        assertEquals(data, signal.getValueAt(0.0));
        assertEquals(data, signal.getValueAt(5.0));
        assertEquals(any, signal.getValueAt(10.0));
        assertEquals(any, signal.getValueAt(600.0));

        System.out.println(signal);
    }

    @Test
    void refine2() {
        DoubleDomain domain = new DoubleDomain();
        Box<Double> data = new Box<>(10.0,30.0);
        Box<Double> any = new Box<>(
                Double.NEGATIVE_INFINITY,
                Double.POSITIVE_INFINITY);
        OnlineSignal<Double> signal  = new OnlineSignal<>(domain);

        signal.refine(new Update<>(0.0, 10.0, data));

        Box<Double> data1 = new Box<>(10.0, 10.0);
        signal.refine(new Update<>(5.0, 10.0, data1));

        assertEquals(data, signal.getValueAt(0.0));
        assertEquals(data1, signal.getValueAt(5.0));
        assertEquals(data1, signal.getValueAt(9.0));
        assertEquals(any, signal.getValueAt(10.0));
        assertEquals(any, signal.getValueAt(600.0));

        System.out.println(signal);
    }

    @Test
    void refine3() {
        DoubleDomain domain = new DoubleDomain();
        Box<Double> data = new Box<>(10.0,30.0);
        Box<Double> any = new Box<>(
                Double.NEGATIVE_INFINITY,
                Double.POSITIVE_INFINITY);
        OnlineSignal<Double> signal  = new OnlineSignal<>(domain);

        signal.refine(new Update<>(0.0, 10.0, data));

        Box<Double> data1 = new Box<>(10.0, 20.0);
        signal.refine(new Update<>(5.0, 10.0, data1));

        Box<Double> data2 = new Box<>(10.0, 10.0);
        signal.refine(new Update<>(3.0, 8.0, data2));

        assertEquals(data, signal.getValueAt(0.0));
        assertEquals(data2, signal.getValueAt(5.0));
        assertEquals(data1, signal.getValueAt(9.0));
        assertEquals(any, signal.getValueAt(10.0));
        assertEquals(any, signal.getValueAt(600.0));

        System.out.println(signal);
    }
}
