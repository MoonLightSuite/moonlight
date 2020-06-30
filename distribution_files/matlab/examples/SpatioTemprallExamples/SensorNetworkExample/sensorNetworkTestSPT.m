% sensor network example
clear
close all
%% generation of the data (spatial model, time, values)
numSteps        = 1;   % number of frames
num_nodes       = 5;    % number of nodes
framePlot = false; % to enable or disable the plot of the graph
% see the sensorModel function for the description of the output
[spatialModelv,spatialModelc, time, values]= sensorModel(num_nodes,numSteps, framePlot);
inputModel =spatialModelv;
% plot of the last frame
% numframe = 1;
plotGraph(inputModel, 1 , 'node');

%% monitor
% loading of the script
%generate a moonlightScript object from the script file multipleMonitors.mls (contained in this folder)
%this object is an implementation of ScriptLoader class, please refer to 
%the doc of this class for more details (ex. write in console "doc ScriptLoader" )
moonlightScript = ScriptLoader.loadFromFile("sensorNetMonitorScript");

%moonlightScript.setMinMaxDomain(); % for the quantitative semantics
moonlightScript.setBooleanDomain();
Monitor = moonlightScript.getMonitor("PE");
MonitorResult = Monitor.monitor(inputModel,time,values);
plotResults(inputModel, MonitorResult , true);
