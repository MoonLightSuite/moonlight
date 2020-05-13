/**
 *
 */
package eu.quanticol.moonlight;

import java.lang.reflect.InvocationTargetException;

/**
 * @author loreti
 *
 */
public interface MoonLightScript {
	
	
	boolean isTemporal();
	
	boolean isSpatialTemporal();
	
	default MoonLightTemporalScript temporal() {
		throw new IllegalStateException();//TODO: Add error message!
	}
	
	default MoonLightSpatialTemporalScript spatialTemporal() {
		throw new IllegalStateException(); //TODO: Add error message!
	}

//    private final String[] temporalMonitors;
//    private final String[] spatialTemporalMonitors;
//
//    public MoonLightScript(String[] temporalMonitors, String[] spatialTemporalMonitors) {
//        this.temporalMonitors = temporalMonitors;
//        this.spatialTemporalMonitors = spatialTemporalMonitors;
//    }
//
//    public abstract TemporalScriptComponent<?> selectTemporalComponent(String name);
//
//    public abstract SpatialTemporalScriptComponent<?> selectSpatialTemporalComponent(String name);
//
//    public abstract TemporalScriptComponent<?> selectDefaultTemporalComponent();
//
//    public abstract SpatialTemporalScriptComponent<?> selectDefaultSpatialTemporalComponent();
//
//    public String[] getTemporalMonitors() {
//        return temporalMonitors;
//    }
//
//    public String[] getSpatialTemporalMonitors() {
//        return spatialTemporalMonitors;
//    }
//
//    public String getInfoDefaultTemporalMonitor() {
//        TemporalScriptComponent<?> c = selectDefaultTemporalComponent();
//        if (c != null) {
//            return c.getInfo();
//        } else {
//            return "There it not any default temporal monitor!";
//        }
//    }
//
//    public String getInfoTemporalMonitor(String name) {
//        TemporalScriptComponent<?> c = selectTemporalComponent(name);
//        if (c != null) {
//            return c.getInfo();
//        } else {
//            return "Temporal monitor " + name + " is unknown!";
//        }
//    }
//
//    public String getInfoSpatialTemporalMonitor(String name) {
//        SpatialTemporalScriptComponent<?> c = selectSpatialTemporalComponent(name);
//        if (c != null) {
//            return c.getInfo();
//        } else {
//            return "Spatial-temporal monitor " + name + " is unknown!";
//        }
//    }
//
//    public String getInfoDefaultSpatialTemporalMonitor() {
//        SpatialTemporalScriptComponent<?> c = selectDefaultSpatialTemporalComponent();
//        if (c != null) {
//            return c.getInfo();
//        } else {
//            return "There it not any default spatial temporal monitor!";
//        }
//    }
//
//    public static MoonLightScript forName(String className) throws ClassNotFoundException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
//        Class<?> scriptClass = MoonLightScript.class.getClassLoader().loadClass(className);
//        return (MoonLightScript) scriptClass.getDeclaredConstructor().newInstance();
//    }


}
