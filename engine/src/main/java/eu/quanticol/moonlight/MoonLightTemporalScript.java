package eu.quanticol.moonlight;

import eu.quanticol.moonlight.domain.SignalDomain;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;

public class MoonLightTemporalScript implements MoonLightScript {

    private final Map<String,TemporalMonitorDefinition> monitors;
    private final String defaultMonitor;
    private SignalDomain<?> domain;

    public MoonLightTemporalScript(String defaultMonitor, SignalDomain<?> domain, TemporalMonitorDefinition[] monitors) {
        if (monitors.length == 0) {
            throw new IllegalArgumentException("At least a monitor should be provided!");
        }
        if (defaultMonitor == null) {
            throw new IllegalArgumentException("Default monitor should be a non null value!");
        }
        this.monitors = new TreeMap<>();
        Arrays.stream(monitors).forEach(d -> this.monitors.put(d.getName(),d));
        if (!this.monitors.containsKey(defaultMonitor)) {
            throw new IllegalArgumentException("No definition for the given default monitor is provided!");
        }
        this.defaultMonitor = defaultMonitor;
        this.domain = domain;
    }


    public TemporalScriptComponent<?> selectTemporalComponent(String name) {
        if (monitors.containsKey(name)) {
            return new TemporalScriptComponent<>(monitors.get(name),domain);
        }
        throw new IllegalArgumentException(String.format("Monitor %s is unknown.",name));
    }


    public TemporalScriptComponent<?> selectDefaultTemporalComponent() {
        return new TemporalScriptComponent<>(monitors.get(defaultMonitor),getMonitoringDomain());
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
    public String getInfoDefaultMonitor() {
        TemporalScriptComponent<?> c = selectDefaultTemporalComponent();
        if (c != null) {
            return c.getInfo();
        } else {
            return "There it not any default temporal monitor!";
        }
    }

    @Override
    public String getInfoMonitor(String name) {
        TemporalScriptComponent<?> c = selectTemporalComponent(name);
        if (c != null) {
            return c.getInfo();
        } else {
            return "Temporal monitor " + name + " is unknown!";
        }
    }

    public static MoonLightTemporalScript forName(String className) throws ClassNotFoundException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
        Class<?> scriptClass = MoonLightTemporalScript.class.getClassLoader().loadClass(className);
        return (MoonLightTemporalScript) scriptClass.getDeclaredConstructor().newInstance();
    }

    @Override
    public boolean isTemporal() {
        return true;
    }

    @Override
    public boolean isSpatialTemporal() {
        return false;
    }

    @Override
    public MoonLightTemporalScript temporal() {
        return this;
    }

}