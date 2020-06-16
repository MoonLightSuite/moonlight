% sensor network example
clear

%% generation of the data (spatial model, time, values)
numSteps        = 1;   % number of frames
num_nodes       = 10;    % number of nodes
framePlot = false; % to enable or disable the plot of the graph
% see the sensorModel function for the description of the output
[spatialModel, time, values]= sensorModel(num_nodes,numSteps, framePlot);

% plot of the last frame
numframe = 1;
plotGraph(spatialModel, 1 , 'node');

%% monitor
% loading of the script
%generate a moonlightScript object from the script file multipleMonitors.mls (contained in this folder)
%this object is an implementation of ScriptLoader class, please refer to 
%the doc of this class for more details (ex. write in console "doc ScriptLoader" )
moonlightScript = ScriptLoader.loadFromFile("sensorNetworkMonitorScript");

% list of formulas in the monitor object
moonlightScript.getMonitors()

% generate the monitor for property
% ReachFormula = ( nodeType==3 ) reach (hop)[0, 2] ( nodeType==2 ) ;
% as default it is taken the semantics of the .mls script, in this case a boolean semantics 
reachMonitor = moonlightScript.getMonitor("ReachFormula");

% we can set the semantics using
moonlightScript.setBooleanDomain(); %for the boolean semantics
%formula ParametricReachFormula (int k, int type) = ( nodeType==3 ) reach (hop)[0, k] ( nodeType==type ) ;
parametricReachMonitor = moonlightScript.getMonitor("ParametricReachFormula");

moonlightScript.setMinMaxDomain(); % for the quantitative semantics
% formula SomeWhereFormula = somewhere(dist)[0,3] ( battery > 0.5);
someWhereFormula = moonlightScript.getMonitor("SomeWhereFormula");

%evaluate ReachFormula
resultReachMonitor = reachMonitor.monitor(spatialModel,time,values);
%evaluate the parametric ParametricReachFormula with k=1, type=2
resultParamMonitor = parametricReachMonitor.monitor(spatialModel,time,values, [1,2]);
%evaluate SomeWhereFormula
resultSomeMonitor = someWhereFormula.monitor(spatialModel,time,values);

%% plots
% plotResults(spatialModel, resultReachMonitor, true);
% plotResults(spatialModel, resultParamMonitor, true);
% plotResults(spatialModel, resultSomeMonitor, false);



