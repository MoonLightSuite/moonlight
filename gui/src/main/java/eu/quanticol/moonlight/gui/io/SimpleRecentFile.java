package eu.quanticol.moonlight.gui.io;

import java.util.Objects;

/**
 * Class that implements the {@link RecentFile} interface and is responsible to define a recent file
 *
 * @author Albanese Clarissa, Sorritelli Greta
 */
public class SimpleRecentFile implements RecentFile{

    private final FileType type;

    private final String pathFile;

    public SimpleRecentFile(FileType type, String pathFile) {
        this.type = type;
        this.pathFile = pathFile;
    }

    public FileType getType() {
        return type;
    }

    public String getPathFile() {
        return pathFile;
    }

    @Override
    public String toString() {
        return "" + pathFile + '\'';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SimpleRecentFile that = (SimpleRecentFile) o;
        return type == that.type && Objects.equals(pathFile, that.pathFile);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, pathFile);
    }
}
