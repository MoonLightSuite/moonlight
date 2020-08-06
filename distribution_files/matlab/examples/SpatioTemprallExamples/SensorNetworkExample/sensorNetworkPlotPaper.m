clear;
close all;
% 
% %generation of the data
% numSteps        = 1;
% num_nodes       = 10;
% framePlot = false; % to enable or disable the plot of the graph
% [spatialModelv,spatialModelc,time,values]= sensorModel(num_nodes,numSteps, framePlot);

load('dataInput.mat');
inputModel = spatialModelc;
numframe = 1;
plotGraph(inputModel, numframe , 'node');

%% property  
moonlightScript = ScriptLoader.loadFromFile("sensorNetMonitorScript");
moonlightScript.setBooleanDomain();
% P1 = ( nodeType==3 ) reach (hop)[0, 1] ( nodeType==2 );
boolSpTempMonitor = moonlightScript.getMonitor("P1");

%% evaluation

result1 = boolSpTempMonitor.monitor(inputModel,time,values);



% P1 = ( nodeType==3 ) reach (hop)[0, 1] ( nodeType==2 );
boolSpTempMonitor = moonlightScript.getMonitor("P4");

%% evaluation

result2 = boolSpTempMonitor.monitor(inputModel,time,values);

%tiledlayout(2,1) % Requires R2019b or later


plotResults(inputModel, result1, true)
 

plotResults(inputModel, result2, true)

