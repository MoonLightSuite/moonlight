clear;
close all;

%% STEP 1: define a spatial-temporal signal.
numSteps        = 10;   % number of frames
num_nodes       = 10;    % number of nodes
framePlot = false; % to enable or disable the plot of the graph
% see the sensorModel function for the description of the output
[~,spatialModel, time, values]= sensorModel(num_nodes,numSteps, framePlot);

script= [
"signal { int nodeType; real battery; real temperature; }",...
"space {edges { int hop; real dist; }}",...
"domain boolean;",... 
"formula P1 = ( nodeType==3 ) reach (hop)[0, 5] ( nodeType==1 ) ;"
];

%% STEP 2: loading the Moonlight Script for a stringArray variable (i.e., script)
moonlightScript = ScriptLoader.loadFromText(script);

%% STEP 3 (optional): change the domain on the fly
% moonlightScript.setMinMaxDomain();

%% STEP 4: getting the monitor associated with a target formula
boolSpTempMonitor = moonlightScript.getMonitor("MyFirstFormula");

%% STEP 5: monitor the signal 
result = boolSpTempMonitor.monitor(spatialModel,time,values);
