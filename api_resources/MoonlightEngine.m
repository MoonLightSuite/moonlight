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
            result=spatioTemporalMonitor.monitorToObjectArray(graph,time,self.toJavaObjectMatrix(values),parameters);
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
    end
end