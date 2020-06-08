classdef MoonlightScript
    properties
        Script
    end
    methods
        function self = MoonlightScript(script)
            self.Script = script;
        end
        function result = isTemporal(self)
            result = self.Script.isTemporal();
        end
        function result = isSpatialTemporal(self)
            result = self.Script.isSpatialTemporal();
        end
        function monitor = getMonitor(self,formulaName)
            if(self.isTemporal())
                monitor = TemporalScriptComponent(self.Script.temporal().selectTemporalComponent(formulaName));
            else
                monitor = SpatialTemporalScriptComponent(self.Script.spatialTemporal().selectSpatialTemporalComponent(formulaName));
            end
        end      
        function monitor = temporal(self)
            monitor = TemporalScriptComponent(self.Script.temporal());
        end
        function monitor = spatialTemporal(self)
            monitor = SpatialTemporalScriptComponent(self.Script.spatialTemporal());
        end
        function self = setBooleanDomain(self)
            self.Script.setBooleanDomain();
        end
        function self = setMinMaxDomain(self)
            self.Script.setMinMaxDomain();
        end
    end
end  