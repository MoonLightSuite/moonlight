classdef SpatialTemporalScriptComponent
    properties
        ScriptComponent
    end
    methods
        function self = SpatialTemporalScriptComponent(script)
            self.ScriptComponent = script;
        end
        function result = monitor(self, graph, time, values, parameters)
            if ~exist('parameters','var')
                % third parameter does not exist. Default is the empty
                % array (i.e., [])
                parameters = [];
            end
            javaGraphModel = self.toJavaGraphModel(graph,length(values));
            javaSignal = self.toJavaSignal(values);
            result=self.ScriptComponent.monitorToObjectArrayAdjacencyMatrix(time,javaGraphModel,time,javaSignal,parameters);
        end
    end
    methods (Access = private)
        function graph = toJavaGraphModel(~,diagram,locations)
            element = diagram(1);
            graphWidth =  length(element{1}.Edges.Weights(1,:));
            for i = 1:length(diagram)
                element = diagram(i);
                elementEdges = element{1}.Edges;
                for j = 1 : length(elementEdges.EndNodes)
                    for l = 1:graphWidth
                        endNodes = elementEdges.EndNodes(j,:);
                        weight = elementEdges.Weights(j,:);
                        graph(i,endNodes(1),endNodes(2),l) =weight(l);
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