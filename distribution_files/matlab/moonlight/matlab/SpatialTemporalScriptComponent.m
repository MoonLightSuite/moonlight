classdef SpatialTemporalScriptComponent
    % This is a wrapper around the Java class eu.quanticol.moonlight.SpatialTemporalScriptComponent
    % 
    % This class contains a method (i.e., monitor(...)) that can be used to
    % monitor a specific spatial-temporal trajectory.
    properties
        ScriptComponent
    end
    methods
        function self = SpatialTemporalScriptComponent(script)
            self.ScriptComponent = script;
        end
        function result = getName(self)
            % getName get the name of this monitor
            result = self.ScriptComponent.getName();
        end
        function result = monitor_static(self, graph, time, values, parameters)
            % monitor get the result of monitoring a spatial-temporal trajectory
            %   monitor(graph, time, values, parameters)
            %       - time: an array containing the trajectory timesteps
            %       - values: a matrix containing the trajectory values
            %       - parameters: (optional) an array containing the values
            %                      of the formula paramters
            if ~exist('parameters','var')
                % third parameter does not exist. Default is the empty
                % array (i.e., [])
                parameters = [];
            end
            javaGraphModel = self.toJavaGraphModel([ graph ]);
            javaSignal = self.toJavaSignal(values);
            result=self.ScriptComponent.monitorToObjectArrayAdjacencyList([time(1)],javaGraphModel,time,javaSignal,parameters);
        end
        function result = monitor(self, graph, time, values, parameters)
            % monitor get the result of monitoring a spatial-temporal trajectory
            %   monitor(graph, time, values, parameters)
            %       - time: an array containing the trajectory timesteps
            %       - values: a matrix containing the trajectory values
            %       - parameters: (optional) an array containing the values
            %                      of the formula parameters
            if ~exist('parameters','var')
                % third parameter does not exist. Default is the empty
                % array (i.e., [])
                parameters = [];
            end
            javaGraphModel = self.toJavaGraphModel(graph);
            javaSignal = self.toJavaSignal(values);
            result=self.ScriptComponent.monitorToObjectArrayAdjacencyList(time,javaGraphModel,time,javaSignal,parameters);
        end
    end
    methods (Access = private)
         function graph = toJavaGraphModel(~,diagram)
            element = diagram(1);
            graphWidth =  length(element{1}.Edges.Weights(1,:));
            for i = 1:length(diagram)
                element = diagram(i);
                elementEdges = element{1}.Edges;
                for j = 1 : length(elementEdges.EndNodes)
                    endNodes = elementEdges.EndNodes(j,:);
                    weight = elementEdges.Weights(j,:);
                    graph(i,j,1) =endNodes(1)-1;
                    graph(i,j,2) =endNodes(2)-1;
                    for l = 1:graphWidth
                        graph(i,j,2+l)=weight(l);
                    end
                end
            end
        end
        function javaSignal = toJavaSignal(~,signal)
            javaLocationsLength = length(signal);
            s = size(signal{1});
            javaTimeLength= s(1);
            javaSignalWidth= s(2);
            for i = 1: javaLocationsLength
                for j = 1:javaTimeLength
                    for k = 1:javaSignalWidth
                        javaSignal(i,j,k)= signal{i}(j,k);
                    end
                end
            end
        end
    end
end