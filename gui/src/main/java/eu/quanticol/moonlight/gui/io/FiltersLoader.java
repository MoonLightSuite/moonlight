package eu.quanticol.moonlight.gui.io;

import eu.quanticol.moonlight.gui.filter.Filter;
import eu.quanticol.moonlight.gui.filter.FilterGroup;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Interface that defines how to load filters on/from a .json file
 *
 * @author Albanese Clarissa, Sorritelli Greta
 */
public interface FiltersLoader {

    String saveToJson(ArrayList<Filter> filters, ArrayList<FilterGroup> filterGroups, String name) throws IOException;

    boolean getFromJson(String name, ArrayList<Filter> filters) throws IOException;
}