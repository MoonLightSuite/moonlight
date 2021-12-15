package eu.quanticol.moonlight;

import eu.quanticol.moonlight.formula.SignalDomain;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;

public class MoonLightSpatialTemporalScript implements MoonLightScript {

    private final Map<String,SpatialTemporalMonitorDefinition> monitors;
    private final String defaultMonitor;
    private SignalDomain<?> domain;

    public MoonLightSpatialTemporalScript(String defaultMonitor, SignalDomain<?> domain, SpatialTemporalMonitorDefinition[] monitors) {
        if (monitors.length == 0) {
            throw new IllegalArgumentException("At least a monitor should be provided!");
        }
        if (defaultMonitor == null) {
            throw new IllegalArgumentException("Default monitor should be a non null value!");
        }
        this.monitors = new TreeMap<>();
        Arrays.stream(monitors).forEach(d -> this.monitors.put(d.getName(), d));
        if (!this.monitors.containsKey(defaultMonitor)) {
            throw new IllegalArgumentException("No definition for the given default monitor is provided!");
        }
        this.defaultMonitor = defaultMonitor;
        this.domain = domain;
    }

    public SpatialTemporalScriptComponent<?> selectSpatialTemporalComponent(String name) {
        if (monitors.containsKey(name)) {
            return new SpatialTemporalScriptComponent<>(monitors.get(name),domain);
        }
        throw new IllegalArgumentException(String.format("Monitor %s is unknown.",name));
    }

    public SpatialTemporalScriptComponent<?> selectDefaultSpatialTemporalComponent() {
        return selectSpatialTemporalComponent(defaultMonitor);
    }

    public void setMonitoringDomain(SignalDomain<?> domain) {
        this.domain = domain;
    }

    public SignalDomain<?> getMonitoringDomain() {
        return this.domain;
    }

    @Override
    public String[] getMonitors() {
        return monitors.keySet().toArray(new String[0]);
    }

    @Override
    public String getInfoMonitor(String name) {
        SpatialTemporalMonitorDefinition d = monitors.get(name);
        if (d != null) {
            return d.getInfo();
        } else {
            return "Spatial-temporal monitor " + name + " is unknown!";
        }
    }

    @Override
    public String getInfoDefaultMonitor() {
        SpatialTemporalScriptComponent<?> c = selectDefaultSpatialTemporalComponent();
        if (c != null) {
            return c.getInfo();
        } else {
            return "There it not any default spatial temporal monitor!";
        }
    }

    public static MoonLightSpatialTemporalScript forName(String className) throws ClassNotFoundException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
        Class<?> scriptClass = MoonLightSpatialTemporalScript.class.getClassLoader().loadClass(className);
        return (MoonLightSpatialTemporalScript) scriptClass.getDeclaredConstructor().newInstance();
    }

    @Override
    public boolean isTemporal() {
        return false;
    }

    @Override
    public boolean isSpatialTemporal() {
        return true;
    }

    @Override
    public MoonLightSpatialTemporalScript spatialTemporal() {
        return this;
    }

}