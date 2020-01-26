classdef MoonlightEngine
    properties
        Script
        SignalType
    end
    methods(Static)
        function  self = load(filename)
            self = MoonlightEngine;
            system("java -jar "+fullfile(getenv("MOONLIGHT_FOLDER"),"jar","console-1.0-SNAPSHOT.jar "+filename+".mls "+tempdir));
            %system("jar -cvf "+fullfile(getenv("MOONLIGHT_FOLDER"), "script",filname+".jar")+" "+fullfile("moonlight","script","GeneratedScriptClass.class"));
            system("jar -cvf "+fullfile(getenv("MOONLIGHT_FOLDER"), "script",filename+".jar")+" -C "+tempdir+" "+fullfile("moonlight","script","Script"+filename+".class"));
            javaaddpath(fullfile(getenv("MOONLIGHT_FOLDER"), "script",filename+".jar"));
            self.Script=eval("moonlight.script.Script"+filename);
        end
    end
    methods
        function result = temporalMonitor(self, time, values, parameters)
            if ~exist('parameters','var')
                % third parameter does not exist, so default it to something
                parameters = java.lang.Object();
            end
            temporalMonitor = self.Script.selectDefaultTemporalComponent();
            matrix=temporalMonitor.monitorToObjectArray(time,self.toJavaObjectMatrix(values),parameters);
            result = self.temporalObjectToMatrix(matrix);
        end
        function result = spatioTemporalMonitor(self,graph, time, values,parameters)
            if ~exist('parameters','var')
                % third parameter does not exist, so default it to something
                parameters = java.lang.Object();
            end
            spatioTemporalMonitor = self.Script.selectDefaultSpatioTemporalComponent();
            matrix=spatioTemporalMonitor.monitorToObjectArray(time,self.toJavaGraphModel(graph,length(values)),time,self.toJavaSignal(values),parameters);
            result = self.spatialObjectToMatrix(matrix);
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
            objectVector = javaArray('java.lang.String',signalSize);
            for i = 1:signalSize(1)
                for j = 1:signalSize(2)
                    objectVector(i,j)=java.lang.String(num2str(signal(i,j)));
                end
            end
        end
        function graph = toJavaGraphModel(~,diagram,locations)
            graphLength = length(diagram);
            element = diagram(1);
            graphWidth =  length(element{1}.Edges.Weights(1,:));
            graph = javaArray('java.lang.String',graphLength,locations,locations,graphWidth);
            for i = 1:length(diagram)
                element = diagram(i);
                elementEdges = element{1}.Edges;
                for j = 1 : length(elementEdges.EndNodes)
                    for l = 1:graphWidth
                        endNodes = elementEdges.EndNodes(j,:);
                        weight = elementEdges.Weights(j,:);
                        graph(i,endNodes(1),endNodes(2),l) =java.lang.String(num2str((weight(l))));
                    end
                end
            end
        end
        function javaSignal = toJavaSignal(~,signal)
            javaLocationsLength = length(signal);
            s = size(signal{1});
            javaTimeLength= s(1);
            javaSignalWidth= s(2);
            javaSignal = javaArray('java.lang.String',javaLocationsLength,javaTimeLength,javaSignalWidth);
            for i = 1: javaLocationsLength
                for j = 1:javaTimeLength
                    for k = 1:javaSignalWidth
                        javaSignal(i,j,k)= java.lang.String(num2str(signal{i}(j,k)));
                    end
                end
            end
        end
        function result = temporalObjectToMatrix(~,matrix)
            result= [];
            for p =  1:length(matrix)
                a =matrix(p,:);
                result = [result;[a(1),a(2)]];
            end
        end
        function result = spatialObjectToMatrix(self,matrix)
            result= [];
            for i =  1:length(matrix)
                result{i} = self.temporalObjectToMatrix(matrix(i));
            end
            result = result';
        end
    end
end