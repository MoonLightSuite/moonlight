package eu.quanticol.moonlight.api;

import java.util.Arrays;

public class ProvaMatlab {

    public double somma(double a, double b) {
        return a + b;
    }

    public double somma(double[] a) {
        return Arrays.stream(a).sum();
    }

    public double[] identity(double[] a) {
        return a;
    }
}