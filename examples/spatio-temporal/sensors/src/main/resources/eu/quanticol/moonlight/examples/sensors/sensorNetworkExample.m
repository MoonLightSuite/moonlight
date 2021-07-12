% sensor network example
clear
close all
%% generation of the data (spatial model, time, values)
numSteps        = 50;   % number of frames
num_nodes       = 20;    % number of nodes
framePlot = true; % to enable or disable the plot of the graph
% see the sensorModel function for the description of the output
[spatialModelv,spatialModelc, time, values,frames]= sensorModel(num_nodes,numSteps, framePlot);
inputModel =spatialModelv;
inputValues = values
% plot of the last frame
% numframe = 1;
% plotGraph(spatialModelv, 1 , 'node');
input2 = values{:,1}