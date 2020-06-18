% sensor network example
clear

%% generation of the data (spatial model, time, values)
numSteps        = 3;   % number of frames
num_nodes       = 10;    % number of nodes
framePlot = false; % to enable or disable the plot of the graph
% see the sensorModel function for the description of the output
[spatialModelv,spatialModelc, time, values]= sensorModel(num_nodes,numSteps, framePlot);

% plot of the last frame
% numframe = 1;
% plotGraph(spatialModelv, 1 , 'node');

%% monitor
% loading of the script
%generate a moonlightScript object from the script file multipleMonitors.mls (contained in this folder)
%this object is an implementation of ScriptLoader class, please refer to 
%the doc of this class for more details (ex. write in console "doc ScriptLoader" )
moonlightScript = ScriptLoader.loadFromFile("sensorNetMonitorScript");

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
% formula EscapeFormula (int k) = escape(hop)[3, k] ( nodeType==3) ;
escapeFormula = moonlightScript.getMonitor("EscapeFormula");

inputModel =spatialModelv;
%% Results
%evaluate ReachFormula
resultReachMonitor = reachMonitor.monitor(inputModel,time,values);
%evaluate the parametric ParametricReachFormula with k=1, type=2
resultParamMonitor = parametricReachMonitor.monitor(inputModel,time,values, [1,2]);
%evaluate SomeWhereFormula
resultSomeMonitor = someWhereFormula.monitor(inputModel,time,values);
%evaluate escapeFormula
resultEscapeMonitor = escapeFormula.monitor(inputModel,time,values,inf);

%% plots
% plotResults(spatialModel, resultReachMonitor, true);
% plotResults(spatialModel, resultParamMonitor, true);
% plotResults(spatialModel, resultSomeMonitor, false);



