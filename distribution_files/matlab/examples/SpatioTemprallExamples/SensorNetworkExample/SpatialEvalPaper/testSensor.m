%% Initializing the script
clear;       %clear all the memory
close all;   %close all the open windows

elapseTimeSeq = [];
num_exp = 1;
num_nodes_seq = 10: 50: 1000;

boolean_node_results = zeros(4,2,length(num_nodes_seq));
robust_node_results  = zeros(4,2,length(num_nodes_seq));
% % increasing the number of nodes
i=1;
numSteps =1;
for num_nodes = num_nodes_seq
    [spatialModel,time,signalInput]= sensorModel(num_nodes,numSteps, false);
    [boolean_results, robust_results] = monSpTempMon (spatialModel,time,signalInput, num_exp);
    boolean_node_results(:,:,i)=boolean_results;
    robust_node_results(:,:,i)=robust_results;
    i = i + 1
end
 
plotResult(num_nodes_seq,boolean_node_results)




    
