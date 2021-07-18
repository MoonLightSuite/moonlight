% sensor network example
%numSteps        = double(50);   % number of frames
%num_nodes       = 20;    % number of nodes
%% generation of the data (spatial model, time, values)
framePlot = false; % to enable or disable the plot of the graph
% see the sensorModel function for the description of the output
[spatialModelv,spatialModelc, time, values,frames, nodes_type]= sensorModel(num_nodes,numSteps, framePlot);
% plot of the last frame
% numframe = 1;
% plotGraph(spatialModelv, 1 , 'node');