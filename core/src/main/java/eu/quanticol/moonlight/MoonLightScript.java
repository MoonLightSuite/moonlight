package eu.quanticol.moonlight;

import eu.quanticol.moonlight.structure.BooleanDomain;
import eu.quanticol.moonlight.structure.DoubleDomain;
import eu.quanticol.moonlight.structure.SignalDomain;

public interface MoonLightScript {


    String[] getMonitors();

    String getInfoDefaultMonitor();

    String getInfoMonitor(String name);

    boolean isTemporal();

    boolean isSpatialTemporal();

    default MoonLightTemporalScript temporal() {
        throw new IllegalStateException("Temporal Monitor is not compatible with this Script");
    }

    default MoonLightSpatialTemporalScript spatialTemporal() {
        throw new IllegalStateException("SpatialTemporal Monitor is not compatible with this Script");
    }

    void setMonitoringDomain(SignalDomain<?> domain);

    default void setBooleanDomain() {
        setMonitoringDomain(new BooleanDomain());
    }

    default void setMinMaxDomain() {
        setMonitoringDomain(new DoubleDomain());
    }

    SignalDomain<?> getMonitoringDomain();
}