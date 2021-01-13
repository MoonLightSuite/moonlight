package eu.quanticol.moonlight.examples.subway.io;

import eu.quanticol.moonlight.examples.subway.parsing.ParsingStrategy;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/**
 * Generic Class to read line-wise from a text file.
 * After a parsing process executed from a parsing strategy,
 * it returns an object of the given parameter
 *
 * @param <T> type to which the data read is converted
 *
 * @see ParsingStrategy
 */
public class DataReader<T> {
    private final FileType type;
    private final String path;
    private final ParsingStrategy<T> strategy;

    /**
     * Reader initialization
     * @param path the path to the data source file
     * @param type the file type of source
     * @param strategy the parsing strategy to execute
     */
    public DataReader(String path, FileType type, ParsingStrategy<T> strategy) {
        this.path = path;
        this.type = type;
        this.strategy = strategy;
    }

    /**
     * Reads the data from the source file
     *
     * @return the parsed data after being transformed to T
     */
    public T read() {
        try {
            BufferedReader fileReader = new BufferedReader(new FileReader(path));

            // Read the first line to initialize the parser
            fileReader = readHeader(fileReader);

            // Process the data
            readLines(fileReader);

            fileReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return strategy.result();
    }


    /**
     * Header parsing method
     *
     * @param fileReader buffer to the file to read
     * @throws IOException when a new line can't be read
     */
    private BufferedReader readHeader(BufferedReader fileReader)
            throws IOException
    {
        String header = fileReader.readLine();

        strategy.initialize(splitLine(header));

        // Text files have header that coincides with the first line of data
        // so we reset
        if (FileType.TEXT == type) {
            fileReader.close();
            fileReader = new BufferedReader(new FileReader(path));
        }

        return fileReader;
    }

    /**
     * Line parsing method
     *
     * @param fileReader buffer to the file to read
     * @throws IOException when the line can't be read
     */
    private void readLines(BufferedReader fileReader) throws IOException {
        String row;
        while (null != (row = fileReader.readLine())) {
            strategy.process(splitLine(row));
        }
    }

    /**
     *
     * @param line
     * @return
     */
    private String[] splitLine(String line) {
        String[] data;
        if (FileType.CSV == type)
            data = line.split(",");
        else if (FileType.TEXT == type)
            data = line.split(" ");
        else
            throw new UnsupportedFileTypeException(
                    "The file format doesn't comply with the allowed ones.");

        return data;
    }

}


