import os
library_dir = os.path.dirname(os.path.abspath(__file__))
import jnius_config
jnius_config.add_classpath(f'{library_dir}/jar/engine.jar')
jnius_config.add_classpath(f'{library_dir}/jar/antlr4-runtime.jar')
jnius_config.add_classpath(f'{library_dir}/jar/script.jar')
from jnius import autoclass


class MoonlightScript:

    def __init__(self, script):
        self.script = script

    def getMonitors(self):
        '''gets the list of available monitors (i.e., formulas)'''
        return self.script.getMonitors()

    def isTemporal(self):
        '''returns if this is a temporal script'''
        return self.script.isTemporal()

    def isSpatialTemporal(self):
        '''returns if this is a spatial-temporal script'''
        return self.Script.isSpatialTemporal()

    def getMonitor(self, formulaName):
        '''gets the monitor associated to a target the fomula'''
        loader = autoclass('eu.quanticol.moonlight.MoonlightScriptFactory')()
        if(self.isTemporal()):
            return TemporalScriptComponent(loader.getTemporalScript(self.script).selectTemporalComponent(formulaName))
        else:
            return SpatialTemporalScriptComponent(loader.getSpatialTemporalScript(self.script).selectSpatialTemporalComponent(formulaName))

    def setBooleanDomain(self):
        '''sets the Boolean domain to this script'''
        self.script.setBooleanDomain()

    def setMinMaxDomain(self):
        '''sets the MinMax domain to this script'''
        self.script.setMinMaxDomain()


class ScriptLoader:

    @staticmethod
    def loadFromText(script):
        '''load the script from a string-variable'''
        # moonlightScript = autoclass('eu.quanticol.moonlight.xtext.ScriptLoader')()
        # return MoonlightScript(moonlightScript.compileScript(script))
        ScriptLoader = autoclass('eu.quanticol.moonlight.script.ScriptLoader')
        return MoonlightScript(ScriptLoader.loadFromCode(script))

    @staticmethod
    def loadFromFile(script):
        '''load the script from a file'''
        # moonlightScript = autoclass('eu.quanticol.moonlight.xtext.ScriptLoader')()
        # with open(path) as file:
        #     script = file.read()
        #return MoonlightScript(moonlightScript.compileScript(script))
        ScriptLoader = autoclass('eu.quanticol.moonlight.script.ScriptLoader')
        return MoonlightScript(ScriptLoader.loadFromFile(script))


class TemporalScriptComponent:
    def __init__(self, moonlight_monitor):
        self.scriptComponent = moonlight_monitor

    def monitor(self, time, values, parameters=None):
        """
            gets the result of monitoring a temporal trajectory
                   - time: an array containing the trajectory timesteps
                   - values: a matrix containing the trajectory values
                   - parameters: (optional) an array containing the values
                                  of the formula paramters
        """
        monitor = self.scriptComponent
        if not parameters:
            return monitor.monitorToArray(time, values)
        else:
            return monitor.monitorToArray(time, values, parameters)


class SpatialTemporalScriptComponent:

    def __init__(self, moonlight_monitor):
        self.scriptComponent = moonlight_monitor

    def monitor(self, locationTimeArray, graph, signalTimeArray, signalValues, parameters=None):
        monitor = self.scriptComponent
        if not parameters:
            return monitor.monitorToObjectArrayAdjacencyListWithPrint(locationTimeArray, graph, signalTimeArray, signalValues)
        else:
            return monitor.monitorToObjectArrayAdjacencyListWithPrint(locationTimeArray, graph, signalTimeArray, signalValues, parameters)
