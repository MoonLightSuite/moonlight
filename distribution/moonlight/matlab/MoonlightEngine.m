classdef MoonlightEngine
    properties
        Script
        monitorName
    end
    methods(Static)
        function  self = load(filename)
            self = MoonlightEngine;
            system("java -jar "+fullfile(getenv("MOONLIGHT_FOLDER"),"jar","moonlight.jar "+filename+".mls "+tempdir));
            system("jar -cvf "+fullfile(getenv("MOONLIGHT_FOLDER"), "script",filename+".jar")+" -C "+tempdir+" "+fullfile("moonlight","script","Script"+filename+".class"));
            javaaddpath(fullfile(getenv("MOONLIGHT_FOLDER"), "script",filename+".jar"));
            self.Script=eval("moonlight.script.Script"+filename);
        end
    end
    methods
        function [result,time] = temporalMonitor(self,temporalMonitorName, time, values, parameters)
            if ~exist('parameters','var')
                % third parameter does not exist, so default it to something
                parameters = javaArray('java.lang.String',0);
            else
                parameters = self.toJavaParameters(parameters);
            end
            %temporalMonitor = self.Script.selectDefaultTemporalComponent();
            temporalMonitor = self.Script.selectTemporalComponent(temporalMonitorName);
	        javaObjectMatrix = self.toJavaObjectMatrix(values);
            tic
            matrix=temporalMonitor.monitorToObjectArray(time,javaObjectMatrix,parameters);
            time = toc;
            result = self.temporalObjectToMatrix(matrix);          
        end
        function [result,time] = spatioTemporalMonitor(self,spatioTemporalMonitorName,graph, time, values,parameters)
            if ~exist('parameters','var')
                % third parameter does not exist, so default it to something
                parameters = javaArray('java.lang.String',0);
            else
                parameters = toJavaParameters(parameters);
            end
            %spatioTemporalMonitor = self.Script.selectDefaultSpatioTemporalComponent();
            spatioTemporalMonitor = self.Script.selectSpatioTemporalComponent(spatioTemporalMonitorName);
	    javaGraphModel = self.toJavaGraphModel(graph,length(values));
	    javaSignal = self.toJavaSignal(values);
            tic
            matrix=spatioTemporalMonitor.monitorToObjectArray(time,javaGraphModel,time,javaSignal,parameters);
            time = toc;
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
        function javaParameters = toJavaParameters(~,parameters)
            if(isvector(parameters))
                javaParameters=javaArray('java.lang.String',length(parameters));
                for i=1:length(parameters)
                    javaParameters(i)=java.lang.String(num2str(parameters(i)));
                end
            else
                javaParameters=javaArray('java.lang.String',1);
                javaParameters(1)=num2str(parameters,16);
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
