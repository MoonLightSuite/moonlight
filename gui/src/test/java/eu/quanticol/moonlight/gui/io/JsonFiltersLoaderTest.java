package eu.quanticol.moonlight.gui.io;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import eu.quanticol.moonlight.gui.filter.Filter;
import eu.quanticol.moonlight.gui.filter.FilterGroup;
import eu.quanticol.moonlight.gui.filter.SimpleFilter;
import eu.quanticol.moonlight.gui.filter.SimpleFilterGroup;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.lang.reflect.Type;
import java.util.ArrayList;

import static eu.quanticol.moonlight.gui.io.Serializer.interfaceSerializer;
import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings("ResultOfMethodCallIgnored")
class JsonFiltersLoaderTest {

    final FiltersLoader jsonFiltersLoader = new JsonFiltersLoader();
    static final String path = System.getProperty("user.home") + File.separator + "MoonLightConfig" + File.separator + "filters.json";
    static final File file = new File(path);
    static final ArrayList<FilterGroup> filterGroups = new ArrayList<>();
    static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(Filter.class, interfaceSerializer(SimpleFilter.class))
            .registerTypeAdapter(FilterGroup.class, interfaceSerializer(SimpleFilterGroup.class))
            .create();

    @Test
    void saveToJsonTest() throws IOException {
        file.getParentFile().mkdirs();
        if(!file.exists())
            file.createNewFile();
        ArrayList<Filter> filters = new ArrayList<>();
        ArrayList<FilterGroup> filterGroups = new ArrayList<>();
        Filter filter = new SimpleFilter("Value", "=",0.0);
        Filter filter1 = new SimpleFilter("Direction",">", 3.0);
        Filter filter2 = new SimpleFilter("Speed","<",5.0);
        filters.add(filter);
        filters.add(filter1);
        filters.add(filter2);
        jsonFiltersLoader.saveToJson(filters,filterGroups,"Filters1");
        assertEquals("Filters1", filterGroups.get(0).getName());
        Reader reader = new FileReader(file);
        Type filterListType = new TypeToken<ArrayList<FilterGroup>>() {}.getType();
        ArrayList<FilterGroup> fromJson = gson.fromJson(reader,filterListType);
        reader.close();
        assertEquals(fromJson,filterGroups);
    }

    @Test
    void getFromJsonTest() throws IOException {
        file.getParentFile().mkdirs();
        if(!file.exists())
            file.createNewFile();
        new FileWriter(file,false).close();
        ArrayList<Filter> filters = new ArrayList<>();
        ArrayList<FilterGroup> filterGroups = new ArrayList<>();
        Filter filter = new SimpleFilter("Value", "=",0.0);
        Filter filter1 = new SimpleFilter("Direction",">", 3.0);
        Filter filter2 = new SimpleFilter("Speed","<",5.0);
        filters.add(filter);
        filters.add(filter1);
        filters.add(filter2);
        assertThrows(IOException.class, () -> jsonFiltersLoader.getFromJson("Filters1",filters));
        jsonFiltersLoader.saveToJson(filters,filterGroups,"Filters1");
        assertTrue(jsonFiltersLoader.getFromJson("Filters1",filters));
        assertFalse(jsonFiltersLoader.getFromJson("Filters2",filters));
    }

    @Test
    @BeforeAll
    static void resetFile() throws IOException {
        file.getParentFile().mkdirs();
        if(!file.exists())
            file.createNewFile();
        Reader reader = new FileReader(file);
        Type filterListType = new TypeToken<ArrayList<FilterGroup>>() {}.getType();
        ArrayList<FilterGroup> filterGroups1 = gson.fromJson(reader,filterListType);
        if(filterGroups1 != null)
            filterGroups.addAll(filterGroups1);
        new FileWriter(file,false).close();
    }

    @Test
    @AfterAll
    static void reset() throws IOException {
        file.getParentFile().mkdirs();
        if(!file.exists())
            file.createNewFile();
        new FileWriter(file,false).close();
        Writer writer = new FileWriter(file);
        gson.toJson(filterGroups, writer);
        writer.close();
    }
}