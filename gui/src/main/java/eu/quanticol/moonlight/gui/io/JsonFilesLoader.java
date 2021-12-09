package eu.quanticol.moonlight.gui.io;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.lang.reflect.Type;
import java.util.ArrayList;

import static eu.quanticol.moonlight.gui.io.Serializer.interfaceSerializer;

/**
 * Class that implements the {@link FilesLoader} interface and is responsible to save and import recent files
 *
 * @author Albanese Clarissa, Sorritelli Greta
 */
@SuppressWarnings("ResultOfMethodCallIgnored")
public class JsonFilesLoader implements FilesLoader {

    /**
     * @return file json with recent files
     */
    private File getFile() throws IOException {
        String path = System.getProperty("user.home");
        path += File.separator + "MoonLightConfig" + File.separator + "files.json";
        File userFile = new File(path);
        userFile.getParentFile().mkdirs();
        if (!userFile.exists())
            userFile.createNewFile();
        return userFile;
    }

    /**
     * Save the file chosen in a json file
     *
     * @param pathFile path of file
     * @param type     type of file
     */
    public void saveToJson(String pathFile, FileType type) throws IOException {
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(RecentFile.class, interfaceSerializer(SimpleRecentFile.class))
                .create();
        ArrayList<RecentFile> filesList = new ArrayList<>();
        File userFile = getFile();
        if (userFile.length() != 0)
            filesList.addAll(getFilesFromJson());
        writeJsonFile(gson, pathFile, type, filesList, userFile);
    }

    /**
     * Writes a file in json
     *
     * @param gson       gson instance
     * @param pathFile   path of file
     * @param type       type of file
     * @param files      files already present in the json file
     * @param userFile   file json of user
     */
    private void writeJsonFile(Gson gson, String pathFile, FileType type, ArrayList<RecentFile> files, File userFile) throws IOException {
        Writer writer = new FileWriter(userFile);
        RecentFile recentFile = new SimpleRecentFile(type,pathFile);
        files.remove(recentFile);
        files.add(recentFile);
        gson.toJson(files, writer);
        writer.close();
    }

    /**
     * Takes files from json file
     */
    public ArrayList<RecentFile> getFilesFromJson() throws IOException {
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(RecentFile.class, interfaceSerializer(SimpleRecentFile.class))
                .create();
        File userFile = getFile();
        Reader reader = new FileReader(userFile);
        Type filterListType = new TypeToken<ArrayList<RecentFile>>() {
        }.getType();
        ArrayList<RecentFile> fromJson = gson.fromJson(reader, filterListType);
        reader.close();
        return fromJson;
    }
}