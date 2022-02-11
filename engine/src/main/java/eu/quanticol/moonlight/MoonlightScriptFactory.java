package eu.quanticol.moonlight;

public class MoonlightScriptFactory {

    public MoonLightTemporalScript getTemporalScript(MoonLightScript moonLightScript) {
        return moonLightScript.temporal();
    }

    public MoonLightSpatialTemporalScript getSpatialTemporalScript(MoonLightScript moonLightScript) {
        return moonLightScript.spatialTemporal();
    }
}