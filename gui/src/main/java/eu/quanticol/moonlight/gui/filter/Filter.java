package eu.quanticol.moonlight.gui.filter;

/**
 * This interface is responsible to define a filter.
 *
 * @author Albanese Clarissa, Sorritelli Greta
 */
public interface Filter {

    String getAttribute();

    String getOperator();

    double getValue();
}

