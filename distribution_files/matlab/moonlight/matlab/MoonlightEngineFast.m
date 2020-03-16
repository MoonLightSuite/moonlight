classdef MoonlightEngineFast
    % This is a wrapper class arounf the java core of Moonlight
    % Use the static constructor load(filename) to build it
  
    properties
        Script
        monitorName
    end
    methods(Static)
        function  self = load(filename)
            % class static constructor
            self = MoonlightEngineFast;
            [status, out] = system("java -jar "+fullfile(getenv("MOONLIGHT_FOLDER"),"moonlight","jar","moonlight.jar "+filename+".mls "+tempdir));
            if(status~=0)
                throw(MException("","PARSER OF THE SCRIPT FAILED "+out))
            end
            [status, out] = system("jar -cvf "+fullfile(getenv("MOONLIGHT_FOLDER"),"moonlight", "script",filename+".jar")+" -C "+tempdir+" "+fullfile("moonlight","script","Script"+filename+".class"));
            if(status~=0)
                throw(MException("","CREATION OF THE JAR FAILED \n"+out))
            end
            javaaddpath(fullfile(getenv("MOONLIGHT_FOLDER"),"moonlight", "script",filename+".jar"));
            self.Script=eval("moonlight.script.Script"+filename);
        end
    end
    methods
        function [result,time] = temporalMonitor(self,temporalMonitorName, time, values, parameters)
            %execute temporal monitor
            if ~exist('parameters','var')
                % third parameter does not exist, so default it to something
                parameters = javaArray('java.lang.String',0);
            else
                parameters = self.toJavaParameters(parameters);
            end
            %temporalMonitor = self.Script.selectDefaultTemporalComponent();
            temporalMonitor = self.Script.selectTemporalComponent(temporalMonitorName);
            %javaObjectMatrix = self.toJavaObjectMatrix(values);
            tic
            matrix=temporalMonitor.monitorToDoubleArray(time,values,parameters);
            time = toc;
            result = self.temporalObjectToMatrix(matrix);
        end
        function [result,time] = spatioTemporalMonitor(self,spatioTemporalMonitorName,graph, time, values,parameters)
            %execute spatio-temporal monitor
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
            matrix=spatioTemporalMonitor.monitorToDoubleArray(time,javaGraphModel,time,javaSignal,parameters);
            time = toc;
            result = self.spatialObjectToMatrix(matrix);
        end
        function result = getTemporalMonitors(self)
            %list all the temporal monitors
            result = self.Script.getTemporalMonitors();
        end
        function result = getSpatioTemporalMonitors(self)
            %list all the spatio-temporal monitors
            result = self.Script.getSpatioTemporalMonitors();
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
            for i = 1: javaLocationsLength
                for j = 1:javaTimeLength
                    for k = 1:javaSignalWidth
                        javaSignal(i,j,k)= signal{i}(j,k);
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
