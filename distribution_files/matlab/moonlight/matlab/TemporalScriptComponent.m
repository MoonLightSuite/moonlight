classdef TemporalScriptComponent
    % This is a wrapper around the Java class io.github.moonlightsuite.moonlight.TemporalScriptComponent
    %
    % This class contains a method (i.e., monitor(...)) that can be used to
    % monitor a specific temporal trajectory.
    properties
        ScriptComponent
    end
    methods
        function self = TemporalScriptComponent(script)
            self.ScriptComponent = script;
        end
        function result = getName(self)
            % getName get the name of this monitor
            result = self.ScriptComponent.getName();
        end
        function result = monitor(self,time, values, parameters)
            % monitor get the result of monitoring a temporal trajectory
            %   monitor(time,values,parameters)
            %       - time: an array containing the trajectory timesteps
            %       - values: a matrix containing the trajectory values
            %       - parameters: (optional) an array containing the values
            %                      of the formula paramters
            if ~exist('parameters','var')
                % third parameter does not exist. Default is the empty
                % array (i.e., [])
                parameters = [];
            end
            result=self.ScriptComponent.monitorToArray(time,values,parameters);
        end
    end
end
