package eu.quanticol.moonlight.gui.filter;

import java.util.ArrayList;

/**
 * This interface is responsible to define a group of filters.
 */
public interface FilterGroup {

    ArrayList<Filter> getFilters();

    String getName();
}