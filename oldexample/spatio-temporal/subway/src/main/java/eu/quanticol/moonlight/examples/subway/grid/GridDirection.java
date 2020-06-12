package eu.quanticol.moonlight.examples.subway.grid;

public enum GridDirection {

    HH(0), // Holding still

    NN(1), // North
    SS(2), // South
    EE(3), // East
    WW(4), // West
    NE(5), // NorthEast
    NW(6), // NorthWest
    SE(7), // SouthEast
    SW(8); // SouthWest

    public final int value;

    GridDirection(int value) {
        this.value = value;
    }
}
