classdef MoonlightScript
    % This is a wrapper around the Java interface io.github.moonlightsuite.moonlight.MoonLightScript
    %
    % This class contains all the useful methods to get monitor associated
    % to formulas defined in the script. There are also methods to change
    % the domain of the script on the fly.
    properties
        Script
    end
    methods
        function self = MoonlightScript(script)
            self.Script = script;
        end
        function result = getMonitors(self)
            % getMonitors get the list of available monitors (i.e., formulas)
            result = self.Script.getMonitors();
        end
        function result = isTemporal(self)
            %isTemporal return if this is a temporal script
            result = self.Script.isTemporal();
        end
        function result = isSpatialTemporal(self)
            %isSpatialTemporal return if this is a spatial-temporal script
            result = self.Script.isSpatialTemporal();
        end
        function monitor = getMonitor(self,formulaName)
            %getMonitor get the monitor associated to a target the fomula
            %   getMonitor(formulaName) get the monitor associated to the fomula with name formulaName
            if(self.isTemporal())
                monitor = TemporalScriptComponent(self.Script.temporal().selectTemporalComponent(formulaName));
            else
                monitor = SpatialTemporalScriptComponent(self.Script.spatialTemporal().selectSpatialTemporalComponent(formulaName));
            end
        end
        function self = setBooleanDomain(self)
            %setBooleanDomain set the Boolean domain to this script
            self.Script.setBooleanDomain();
        end
        function self = setMinMaxDomain(self)
            %setMinMaxDomain set the MinMax domain to this script
            self.Script.setMinMaxDomain();
        end
    end
    methods (Access = private)
        function monitor = temporal(self)
            monitor = TemporalScriptComponent(self.Script.temporal());
        end
        function monitor = spatialTemporal(self)
            monitor = SpatialTemporalScriptComponent(self.Script.spatialTemporal());
        end
    end
end
