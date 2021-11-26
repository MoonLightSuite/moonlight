package eu.quanticol.moonlight.gui.filter;

import java.util.ArrayList;
import java.util.Objects;

/**
 * This class implements the {@link FilterGroup} interface and is responsible to define a group of filters.
 *
 * @author Albanese Clarissa, Sorritelli Greta
 */
public class SimpleFilterGroup implements FilterGroup {

    private final String name;
    private final ArrayList<Filter> filters;

    public SimpleFilterGroup(String name, ArrayList<Filter> filters){
        this.name = name;
        this.filters = filters;

    }

    public ArrayList<Filter> getFilters() {
        return filters;
    }

    public String getName(){
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SimpleFilterGroup that = (SimpleFilterGroup) o;
        return Objects.equals(name, that.name) && Objects.equals(filters, that.filters);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, filters);
    }

    @Override
    public String toString() {
        return "{" + name +
                "," + filters +
                '}';
    }
}
