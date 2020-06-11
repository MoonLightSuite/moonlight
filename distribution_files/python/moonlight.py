import jnius_config
jnius_config.set_classpath('./jar/moonlight.jar')
from jnius import autoclass

class Moonlight:
    
    def set_script(self, script):
        script_loader = autoclass('eu.quanticol.moonlight.xtext.ScriptLoader')()
        self.compiled_script = script_loader.compileScript(script)

    def get_monitor(self,monitor_name):
        loader = autoclass('eu.quanticol.moonlight.MoonlightScriptFactory')()
        if(self.compiled_script.isTemporal()):
            return TemporalMonitor(loader.getTemporalScript(self.compiled_script).selectTemporalComponent(monitor_name))
        else:
            return SpatialTemporalMonitor(loader.getSpatialTemporalScript(compiled_script))
            
    
class TemporalMonitor():
     def __init__(self,moonlight_monitor):
            self.monitor_component = moonlight_monitor
     
     def monitor(self, time, space, parameters=None):
            monitor = self.monitor_component
            if(not parameters):
                return monitor.monitorToArray(time,space)
            else:
                return monitor.monitorToArray(time,space,parameters)

class SpatialTemporalMonitor():
     def __init__(self,moonlight_monitor):
            self.monitor_component = moonlight_monitor
     
     def monitor(self, time, space, parameters=None):
            monitor = self.monitor_component
            if(not parameters):
                return monitor.monitorToArray(time,space)
            else:
                return monitor.monitorToArray(time,space,parameters)