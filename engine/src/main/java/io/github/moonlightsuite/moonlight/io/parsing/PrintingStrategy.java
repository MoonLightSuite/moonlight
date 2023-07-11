package io.github.moonlightsuite.moonlight.io.parsing;

/**
 * Generic interface that characterizes a parsing strategy
 * for gathering data from a String array (e.g. a text file).
 *
 * This strategy is supposed to have side-effects on the
 * chosen output data structure.
 *
 * @param <T> the data structure that will be printed
 */
public interface PrintingStrategy<T> {

    /**
     * Hook to execute the initialization of the printer, when needed.
     * @param header an array of data containing the data to process
     */
    String initialize(T header, String wordBreak);

    /**
     * Parses the input data. Might be reiterated
     * (e.g. lines of a text file)
     * @param data an array of strings to be processed
     */
    String print(T data, String wordBreak);

    /**
     * @return true if there is nothing more to print
     */
    boolean isComplete();

}
