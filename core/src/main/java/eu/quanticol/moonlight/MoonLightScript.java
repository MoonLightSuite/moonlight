/**
 * 
 */
package eu.quanticol.moonlight;

/**
 * @author loreti
 *
 */
public abstract class MoonLightScript {
	
	private final String[] temporalMonitors;
	private final String[] spatioTemporalMonitors;

	public MoonLightScript(String[] temporalMonitors, String[] spatioTemporalMonitors) {
		this.temporalMonitors = temporalMonitors;
		this.spatioTemporalMonitors = spatioTemporalMonitors;
	}
	
	protected abstract TemporalScriptComponent<?> selectTemporalComponent( String name );

	protected abstract SpatioTemporalScriptComponent<?> selectSpatioTemporalComponent( String name );

	protected abstract TemporalScriptComponent<?> selectDefaultTemporalComponent( );
	
	protected abstract SpatioTemporalScriptComponent<?> selectDefaultSpatioTemporalComponent( );
	
	public String[] getTemporalMonitors() {
		return temporalMonitors;
	}
	
	public String[] getSpatioTemporalMonitors() {
		return spatioTemporalMonitors;
	}
	
	public String getInfoDefaultTemporalMonitor( ) {
		TemporalScriptComponent<?> c = selectDefaultTemporalComponent();
		if (c!=null) {
			return c.getInfo();
		} else {
			return "There it not any default temporal monitor!";
		}
	}
	
	public String getInfoTemporalMonitor( String name ) {
		TemporalScriptComponent<?> c = selectTemporalComponent(name);
		if (c!=null) {
			return c.getInfo();
		} else {
			return "Temporal monitor "+name+" is unknown!";
		}
	}

	public String getInfoSpatioTemporalMonitor( String name ) {
		SpatioTemporalScriptComponent<?> c = selectSpatioTemporalComponent(name);
		if (c!=null) {
			return c.getInfo();
		} else {
			return "Spatio-temporal monitor "+name+" is unknown!";
		}
	}

	public String getInfoDefaultSpatioTemporalMonitor( ) {
		SpatioTemporalScriptComponent<?> c = selectDefaultSpatioTemporalComponent();
		if (c!=null) {
			return c.getInfo();
		} else {
			return "There it not any default spatio temporal monitor!";
		}
	}

}
