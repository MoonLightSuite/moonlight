clear;
close all;

%% STEP 1: define a spatial-temporal signal.
numSteps        = 7;   % number of frames
num_nodes       = 10;    % number of nodes
framePlot = false; % to enable or disable the plot of the graph
% see the sensorModel function for the description of the output
[~,spatialModel, time, values]= sensorModel(num_nodes,numSteps, framePlot);

script= [
"signal { int nodeType; real battery; real temperature; }",...
"space {edges { int hop; real dist; }}",...
"domain minmax;",... 
"formula P1 = ( nodeType==3 ) reach (hop)[0, 1] ( nodeType==1 | nodeType==2 ) ;",...
"formula P2 = escape(hop)[5,inf] (battery > 0.5) ;",...
"formula P3 = somewhere(dist)[0,300] (battery > 0.5))"
];

%% STEP 2: loading the Moonlight Script for a stringArray variable (i.e., script)
moonlightScript = ScriptLoader.loadFromText(script);

%% STEP 3 (optional): change the domain on the fly
% moonlightScript.setMinMaxDomain();

%% STEP 4: getting the monitor associated with a target formula
spTempMonitor = moonlightScript.getMonitor("P3");

%% STEP 5: monitor the signal 
result = spTempMonitor.monitor(spatialModel,time,values);
