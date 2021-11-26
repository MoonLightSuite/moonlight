package eu.quanticol.moonlight.gui.filter;

import java.util.ArrayList;

/**
 * Interface that defines a group of filters
 *
 * @author Albanese Clarissa, Sorritelli Greta
 */
public interface FilterGroup {

    ArrayList<Filter> getFilters();

    String getName();
}