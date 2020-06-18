clear;
close all;

%generation of the data
numSteps        = 1;
num_nodes       = 20;
framePlot = false; % to enable or disable the plot of the graph
[spatialModelv,spatialModelc,time,values]= sensorModel(num_nodes,numSteps, framePlot);
%load('dataInput.mat');
numframe = 1;
plotGraph(spatialModelc, numframe , 'node');

%% property  
moonlightScript = ScriptLoader.loadFromFile("test");
moonlightScript.setBooleanDomain();
% P1 = ( nodeType==2 ) reach (hop)[0, 5] ( nodeType==1 );
boolSpTempMonitor = moonlightScript.getMonitor("P1");

%% evaluation
inputModel = spatialModelc;
result = boolSpTempMonitor.monitor(inputModel,time,values);

plotResults(inputModel, result, true)