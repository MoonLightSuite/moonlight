classdef TemporalScriptComponent
    properties
        ScriptComponent
    end
    methods
        function self = TemporalScriptComponent(script)
            self.ScriptComponent = script;
        end
        function result = monitor(self,time, values, parameters)
            if ~exist('parameters','var')
                % third parameter does not exist. Default is the empty
                % array (i.e., [])
                parameters = [];
            end
            result=self.ScriptComponent.monitorToArray(time,values,parameters);
        end
    end
end