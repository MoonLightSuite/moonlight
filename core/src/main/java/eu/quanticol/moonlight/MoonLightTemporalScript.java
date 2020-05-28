/**
 *
 */
package eu.quanticol.moonlight;

import java.lang.reflect.InvocationTargetException;

/**
 * @author loreti
 *
 */
public abstract class MoonLightTemporalScript implements MoonLightScript {

    private final String[] formulas;

    public MoonLightTemporalScript(String[] formulas) {
        this.formulas = formulas;
    }

    public abstract TemporalScriptComponent<?> selectTemporalComponent(String name);

    public abstract TemporalScriptComponent<?> selectDefaultTemporalComponent();

    public String[] getMonitors() {
        return formulas;
    }

    public String getInfoDefaultTemporalMonitor() {
        TemporalScriptComponent<?> c = selectDefaultTemporalComponent();
        if (c != null) {
            return c.getInfo();
        } else {
            return "There it not any default temporal monitor!";
        }
    }

    public String getInfoTemporalMonitor(String name) {
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
