import jnius_config
jnius_config.set_classpath('./jar/moonlight.jar')
from jnius import autoclass

class MoonlightScript:
    
    def __init__(self,script):
        self.script=script
    
    def getMonitors(self):
        return self.script.getMonitors();

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

    def setBooleanDomain(self):
        self.script.setBooleanDomain();
        
    def setMinMaxDomain(self):
        self.script.setMinMaxDomain();

class ScriptLoader:
    
    @staticmethod
    def loadFromText(script):
        moonlightScript = autoclass('eu.quanticol.moonlight.xtext.ScriptLoader')()
        return MoonlightScript(moonlightScript.compileScript(script))
    
    @staticmethod
    def loadFromFile(path):
        moonlightScript = autoclass('eu.quanticol.moonlight.xtext.ScriptLoader')()
        with open(path) as file:
            script = file.read()
        return MoonlightScript(moonlightScript.compileScript(script))
    
          
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