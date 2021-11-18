package eu.quanticol.moonlight.gui.io;

import eu.quanticol.moonlight.gui.filter.Filter;
import eu.quanticol.moonlight.gui.filter.FilterGroup;
import eu.quanticol.moonlight.gui.filter.SimpleFilter;
import eu.quanticol.moonlight.gui.filter.SimpleFilterGroup;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import eu.quanticol.moonlight.gui.util.DialogBuilder;
import javafx.scene.control.TableView;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Optional;
import static eu.quanticol.moonlight.gui.io.Serializer.interfaceSerializer;

/**
 * This class implements the {@link JsonLoader} interface and is responsible to save and import filters.
 */
public class JsonFiltersLoader implements JsonLoader {

    /**
     * Saves filters in a Json file.
     */
    public void saveToJson(ArrayList<Filter> filters, ArrayList<FilterGroup> filterGroups, String name, String theme) throws IOException {
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(Filter.class, interfaceSerializer(SimpleFilter.class))
                .registerTypeAdapter(FilterGroup.class, interfaceSerializer(SimpleFilterGroup.class))
                .create();
        File file = new File("src/main/resources/json/filters.json");
        if (file.length() != 0)
            readJsonFile(filterGroups,gson);
        writeJsonFile(gson,filters,filterGroups,name,theme);
    }

    /**
     * Reads an arrayList of filters in the Json File.
     *
     * @param gson gson instance
     */
    private void readJsonFile(ArrayList<FilterGroup> filterGroups, Gson gson) throws IOException {
        ArrayList<FilterGroup> fromJson = getListFromJson(gson);
        if(fromJson != null) {
            if (!filterGroups.toString().equals(fromJson.toString()))
                filterGroups.addAll(fromJson);
        }
    }

    /**
     * Takes filters from Json file
     *
     * @param gson gson instance
     * @return     arrayList of filters
     */
    private ArrayList<FilterGroup> getListFromJson(Gson gson) throws IOException {
        Reader reader = Files.newBufferedReader(Paths.get("src/main/resources/json/filters.json"));
        Type filterListType = new TypeToken<ArrayList<FilterGroup>>() {}.getType();
        ArrayList<FilterGroup> fromJson = gson.fromJson(reader,filterListType);
        reader.close();
        return fromJson;
    }

    /**
     * Writes filters in a Json file.
     *
     * @param gson  gson instance
     * @param filters filters to write
     */
    private void writeJsonFile(Gson gson, ArrayList<Filter> filters,ArrayList<FilterGroup> filterGroups, String name, String theme) throws IOException {
        DialogBuilder d = new DialogBuilder(theme);
        Writer writer = Files.newBufferedWriter(Paths.get("src/main/resources/json/filters.json"));
        FilterGroup filterGroup = new SimpleFilterGroup(name, filters);
        boolean filterGroupPresent = filterGroups.stream().anyMatch(f -> f.equals(filterGroup));
        boolean filtersPresent = filterGroups.stream().anyMatch(f -> f.getFilters().equals(filterGroup.getFilters()));
        boolean namePresent = filterGroups.stream().anyMatch(f -> f.getName().equals(name));
        if(!(filtersPresent || namePresent || filterGroupPresent)) {
            filterGroups.add(filterGroup);
            d.info(name + " filter saved with success!");
        } else
            checkFilterGroup(d,namePresent,filterGroupPresent);
        gson.toJson(filterGroups, writer);
        writer.close();
    }

    /**
     * Check the filterGroup when user save filters on file.
     *
     * @param d                    dialogBuilder
     * @param namePresent          boolean that say if the name of filters is already present
     * @param filterGroupPresent   boolean that say if the filterGroup is already present
     */
    private void checkFilterGroup(DialogBuilder d, boolean namePresent, boolean filterGroupPresent){
        if(filterGroupPresent)
            d.warning("Name and filters already present!");
        else if(namePresent)
            d.warning("Name already present!");
        else d.warning("Filters already present!");
    }

    /**
     * Loads filters take from Json file on table.
     *
     */
    public boolean loadFromJson(String name, TableView<Filter> tableView) throws IOException {
            Gson gson = new GsonBuilder()
                    .registerTypeAdapter(Filter.class, interfaceSerializer(SimpleFilter.class))
                    .registerTypeAdapter(FilterGroup.class, interfaceSerializer(SimpleFilterGroup.class))
                    .create();
            ArrayList<FilterGroup> fromJson = getListFromJson(gson);
            if(fromJson != null){
                Optional<FilterGroup> filterGroup = fromJson.stream().filter(f -> f.getName().equals(name)).findFirst();
                filterGroup.ifPresent(group -> tableView.getItems().addAll(group.getFilters()));
                return filterGroup.isPresent();
            } else
                throw new IOException("Filters not found");
    }
}