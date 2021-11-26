package eu.quanticol.moonlight.gui.filter;

import java.util.Objects;

/**
 * Class that implements the {@link Filter} interface and is responsible to define a filter
 *
 * @author Albanese Clarissa, Sorritelli Greta
 */
public class SimpleFilter implements Filter {

    private final String attribute;
    private final String operator;
    private final double value;

    public SimpleFilter(String attribute, String operator, double value) {
        this.attribute = attribute;
        this.operator = operator;
        this.value = value;
    }

    public String getAttribute() {
        return attribute;
    }

    public String getOperator() {
        return operator;
    }

    public double getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SimpleFilter filter = (SimpleFilter) o;
        return Double.compare(filter.value, value) == 0 && Objects.equals(attribute, filter.attribute) && Objects.equals(operator, filter.operator);
    }

    @Override
    public String toString() {
        return "{" + "attribute='" + attribute + '\'' +
                ", operator='" + operator + '\'' +
                ", value=" + value +
                '}';
    }

    @Override
    public int hashCode() {
        return Objects.hash(attribute, operator, value);
    }
}