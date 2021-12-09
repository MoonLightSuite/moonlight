package eu.quanticol.moonlight.gui.io;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Interface that defines how to load files on/from a .json file
 *
 * @author Albanese Clarissa, Sorritelli Greta
 */
public interface FilesLoader {

    void saveToJson(String pathFile, FileType type) throws IOException;

    ArrayList<RecentFile> getFilesFromJson() throws IOException;
}
