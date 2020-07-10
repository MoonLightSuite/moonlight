package eu.quanticol.moonlight;

import eu.quanticol.moonlight.domain.SignalDomain;

import java.lang.reflect.InvocationTargetException;

public abstract class MoonLightTemporalScript implements MoonLightScript {

    private final String[] formulas;
    private SignalDomain<?> domain;

    public MoonLightTemporalScript(String[] formulas) {
        this.formulas = formulas;
    }

    public abstract TemporalScriptComponent<?> selectTemporalComponent(String name);

    public abstract TemporalScriptComponent<?> selectDefaultTemporalComponent();

    public void setMonitoringDomain(SignalDomain<?> domain) {
        this.domain = domain;
    }

    public SignalDomain<?> getMonitoringDomain() {
        return this.domain;
    }

    @Override
    public String[] getMonitors() {
        return formulas;
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