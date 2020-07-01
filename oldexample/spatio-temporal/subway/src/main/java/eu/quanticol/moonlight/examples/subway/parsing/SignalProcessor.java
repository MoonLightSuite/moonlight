package eu.quanticol.moonlight.examples.subway.parsing;

import eu.quanticol.moonlight.util.data.MultiValuedTrace;

/**
 * Generic interface that characterizes a signal factory
 * that return a Signal from parse input data.
 *
 * The goal is that implementors collect the preprocessing strategies
 * required to generate the signals of their interest.
 *
 * Implementors are supposed to have minimal to no side-effects,
 * and only limited to time/space management of the signal.
 *
 * @param <T> the data structure that will be passed by the caller.
 *
 * @see MultiValuedTrace for info about the output format
 */
public interface SignalProcessor<T> {

    /**
     * Optional method to initialize the signal generation process.
     * @param space the size of signal's space dimension
     * @param time the length of the signal time horizon
     */
    void initializeSpaceTime(int space, int time);

    /**
     * Factory method that generates signals.
     * @param data the data used to generate the signal
     * @return a signal generated from the input data
     */
    MultiValuedTrace generateSignal(T[][] data);

}
