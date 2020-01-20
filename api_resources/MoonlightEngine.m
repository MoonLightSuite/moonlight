classdef MoonlightEngine
    properties
        Script
    end
    methods
        function result = temporalMonitor(self, time, values, parameters)
            if ~exist('parameters','var')
                % third parameter does not exist, so default it to something
                parameters = java.lang.Object();
            end
            temporalMonitor = self.Script.selectDefaultTemporalComponent();
            result=temporalMonitor.monitorToObjectArray(time,self.toJavaObjectMatrix(values),parameters);
        end
        function result = spatioTemporalMonitor(self,graph, time, values,parameters)
            if ~exist('parameters','var')
                % third parameter does not exist, so default it to something
                parameters = java.lang.Object();
            end
            spatioTemporalMonitor = self.Script.selectDefaultSpatioTemporalComponent();
            s = size(values);
            locations =s(2);
            result=spatioTemporalMonitor.monitorToObjectArray(self.toJavaGraphModel(graph,locations),time,self.toJavaSignal(values),parameters);
        end
        function result = getTemporalMonitors(self)
            result = self.Script.getTemporalMonitors();
        end
        function result = getSpatioTemporalMonitors(self)
            result = self.Script.getSpatioTemporalMonitors();
        end
        function result = getInfoTemporalMonitor(self,name)
            result = self.Script.getInfoTemporalMonitor(name);
        end
        function result = getInfoSpatioTemporalMonitor(self,name)
            result = self.Script.getInfoSpatioTemporalMonitor(name);
        end
        function result = getInfoDefaultTemporalMonitor(self)
            result = self.Script.getInfoDefaultTemporalMonitor();
        end
        function result = getInfoDefaultSpatioTemporalMonitor(self)
            result = self.Script.getInfoDefaultSpatioTemporalMonitor();
        end
    end
    methods (Access = private)
        function objectVector = toJavaObjectMatrix(~,signal)
            signalSize=size(signal);
            objectVector = javaArray('java.lang.Object',signalSize);
            for i = 1:signalSize(1)
                for j = 1:signalSize(2)
                    objectVector(i,j)=java.lang.Double(signal(i,j));
                end
            end
        end
        function graph = toJavaGraphModel(~,diagram,locations)
            graphLength = length(diagram);
            element = diagram(1);
            graphWidth =  length(element{1}.Edges.Weight(1,:));
            graph = javaArray('java.lang.Object',graphLength,locations,locations,graphWidth);
            for i = 1:length(diagram)
                element = diagram(i);
                elementEdges = element{1}.Edges;
                for j = 1 : length(elementEdges.EndNodes)
                    for l = 1:graphWidth
                        endNodes = elementEdges.EndNodes(j,:);
                        weight = elementEdges.Weight(j,:);
                        graph(i,endNodes(1),endNodes(2),l) = java.lang.Double(weight(l));
                    end
                end
            end
        end
        function javaSignal = toJavaSignal(~,signal)
            javaSignalLength = length(signal);
            locationsLength = length(signal{1});
            javaSignalWidth =  length(signal{1,1});
            javaSignal = javaArray('java.lang.Object',javaSignalLength,locationsLength,javaSignalWidth);
            for i = 1: length(signal)
                for j = 1:length(locationsLength)
                    for k = 1:javaSignalWidth
                        javaSignal(i,j,k)= java.lang.Double(signal{i,j}(k));
                    end
                end
            end
        end
    end
end