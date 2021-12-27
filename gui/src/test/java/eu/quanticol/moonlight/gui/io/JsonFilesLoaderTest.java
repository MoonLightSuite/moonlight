package eu.quanticol.moonlight.gui.io;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.io.*;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Objects;

import static eu.quanticol.moonlight.gui.io.Serializer.interfaceSerializer;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SuppressWarnings("ResultOfMethodCallIgnored")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class JsonFilesLoaderTest {

    FilesLoader jsonFileLoader = new JsonFilesLoader();
    String path = System.getProperty("user.home") + File.separator + "MoonLightConfig" + File.separator + "files.json";
    File file = new File(path);
    Gson gson = new GsonBuilder()
            .registerTypeAdapter(RecentFile.class, interfaceSerializer(SimpleRecentFile.class))
            .create();

    @Test
    void saveToJsonTest() throws IOException {
        file.getParentFile().mkdirs();
        if(!file.exists())
            file.createNewFile();
        Reader reader = new FileReader(file);
        Type filterListType = new TypeToken<ArrayList<RecentFile>>() {}.getType();
        ArrayList<RecentFile> fileContent = gson.fromJson(reader,filterListType);
        reader.close();
        PrintWriter writer = new PrintWriter(file);
        writer.print("");
        writer.close();
        ArrayList<RecentFile> recentFiles = new ArrayList<>();
        RecentFile fileD = new SimpleRecentFile(FileType.TRA, Objects.requireNonNull(ClassLoader.getSystemClassLoader().getResource("dynamic.tra")).toString());
        RecentFile fileS = new SimpleRecentFile(FileType.TRA, Objects.requireNonNull(ClassLoader.getSystemClassLoader().getResource("static.tra")).toString());
        recentFiles.add(fileD);
        recentFiles.add(fileS);
        jsonFileLoader.saveToJson(fileD.getPathFile(),FileType.TRA);
        jsonFileLoader.saveToJson(fileS.getPathFile(),FileType.TRA);
        Reader reader1 = new FileReader(file);
        ArrayList<RecentFile> fromJson = gson.fromJson(reader1,filterListType);
        assertEquals(fromJson,recentFiles);
        reader1.close();
        PrintWriter writer1 = new PrintWriter(file);
        writer1.print("");
        writer1.close();
        FileWriter fileWriter = new FileWriter(file);
        gson.toJson(fileContent, fileWriter);
        fileWriter.close();
    }

    @Test
    void getFilesFromJsonTest() throws IOException {
        file.getParentFile().mkdirs();
        if(!file.exists())
            file.createNewFile();
        Reader reader = new FileReader(file);
        Type filterListType = new TypeToken<ArrayList<RecentFile>>() {}.getType();
        ArrayList<RecentFile> fileContent = gson.fromJson(reader,filterListType);
        reader.close();
        assertEquals(jsonFileLoader.getFilesFromJson(), fileContent);
    }
}