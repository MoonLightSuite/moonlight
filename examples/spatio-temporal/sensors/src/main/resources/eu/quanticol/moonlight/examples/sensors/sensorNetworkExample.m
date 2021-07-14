% sensor network example
clear
close all
%% generation of the data (spatial model, time, values)
numSteps        = 50;   % number of frames
num_nodes       = 20;    % number of nodes
framePlot = false; % to enable or disable the plot of the graph
% see the sensorModel function for the description of the output
[spatialModelv,spatialModelc, time, values,frames, nodes_type]= sensorModel(num_nodes,numSteps, framePlot);
inputModel =spatialModelv;
graph=spatialModelc{1,1}
graph
nodes = graph.Nodes
temperature  = nodes.temperature
% plot of the last frame
% numframe = 1;
% plotGraph(spatialModelv, 1 , 'node');