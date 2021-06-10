import jnius_config
jnius_config.set_classpath('./jar/moonlight.jar')
from jnius import autoclass

class MoonlightScript:
    
    def __init__(self,script):
        self.script=script
    
    def getMonitors(self):
        return self.script.getMonitors();

    def getInfoDefaultMonitor(self):
        return self.script.getInfoDefaultMonitor();
    
    def getInfoMonitor(self, name):
        return self.script.getInfoMonitor(name);

    def isTemporal(self):
        return self.script.isTemporal();
        
    def isSpatialTemporal(self):
        return self.Script.isSpatialTemporal();
    
    def getMonitor(self,formulaName):
        loader = autoclass('eu.quanticol.moonlight.MoonlightScriptFactory')()
        if(self.isTemporal()):
            return TemporalScriptComponent(loader.getTemporalScript(self.script).selectTemporalComponent(formulaName));
        else:
            return SpatialTemporalScriptComponent(loader.getSpatialTemporalScript(self.script).selectSpatialTemporalComponent(formulaName));
        
    def temporal(self):
        return TemporalScriptComponent(self.script.temporal());
        
    def spatialTemporal(self):
        return SpatialTemporalScriptComponent(self.script.spatialTemporal());

    def setBooleanDomain(self):
        self.script.setBooleanDomain();
    def setMinMaxDomain(self):
        self.script.setMinMaxDomain();

class ScriptLoader:
    
    @staticmethod
    def loadFromText(script):
        moonlightScript = autoclass('eu.quanticol.moonlight.xtext.ScriptLoader')()
        return MoonlightScript(moonlightScript.compileScript(script))
        
        
class Moonlight:
    
    def set_script(self, script):
        script_loader = autoclass('eu.quanticol.moonlight.xtext.ScriptLoader')()
        self.compiled_script = script_loader.compileScript(script)

    def get_monitor(self,monitor_name):
        loader = autoclass('eu.quanticol.moonlight.MoonlightScriptFactory')()
        if(self.compiled_script.isTemporal()):
            return TemporalScriptComponent(loader.getTemporalScript(self.compiled_script).selectTemporalComponent(monitor_name))
        else:
            return SpatialTemporalScriptComponent(loader.getSpatialTemporalScript(self.compiled_script).selectSpatialTemporalComponent(monitor_name))
            
    
class TemporalScriptComponent():
     def __init__(self,moonlight_monitor):
            self.scriptComponent = moonlight_monitor
     
     def monitor(self, time, space, parameters=None):
            monitor = self.scriptComponent
            if(not parameters):
                return monitor.monitorToArray(time,space)
            else:
                return monitor.monitorToArray(time,space,parameters)

class SpatialTemporalScriptComponent():
     def __init__(self,moonlight_monitor):
            self.scriptComponent = moonlight_monitor
     
     def monitor(self, locationTimeArray, graph, signalTimeArray, signalValues, parameters=None):
            monitor = self.scriptComponent
            if(not parameters):
                return monitor.monitorToObjectArrayAdjacencyList(locationTimeArray,graph,signalTimeArray,signalValues)
            else:
                return monitor.monitorToObjectArrayAdjacencyList(locationTimeArray,graph,signalTimeArray,signalValues,parameters)