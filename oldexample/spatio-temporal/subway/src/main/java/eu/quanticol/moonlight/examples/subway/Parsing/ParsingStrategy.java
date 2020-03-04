package eu.quanticol.moonlight.examples.subway.Parsing;

/**
 * Generic interface that characterizes a parsing strategy
 * for gathering data from a String array (e.g. a text file).
 *
 * @param <T> the data structure that results from the parsing
 */
public interface ParsingStrategy<T> {

    /**
     * Hook to execute the initialization of the parser, when needed.
     * @param header an array of strings containing insights for the parser
     */
    void initialize(String[] header);

    /**
     * Parses the input data. Might be reiterated
     * (e.g. lines of a text file)
     * @param data an array of strings to be processed
     */
    void process(String[] data);

    /**
     * Method to fetch the result of the parsing procedure.
     * @return the data structure resulting from parsing
     */
    T result();
}
