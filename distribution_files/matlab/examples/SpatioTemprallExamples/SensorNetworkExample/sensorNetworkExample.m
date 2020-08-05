% sensor network example
clear
close all
%% generation of the data (spatial model, time, values)
numSteps        = 5;   % number of frames
num_nodes       = 50;    % number of nodes
framePlot = false; % to enable or disable the plot of the graph
% see the sensorModel function for the description of the output
[spatialModelv,spatialModelc, time, values]= sensorModel(num_nodes,numSteps, framePlot);
inputModel =spatialModelv;
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
% P1 = (nodeType==3) reach (hop)[0, 1] (nodeType==2 | nodeType==1 ) ;
% as default it is taken the semantics of the .mls script, in this case a boolean semantics 
P1Monitor = moonlightScript.getMonitor("P1");

% we can set the semantics using
moonlightScript.setBooleanDomain(); %for the boolean semantics
%formula P1pam (int k) = atom reach (hop)[0, k] ( nodeType== 1) ;
PparMonitor = moonlightScript.getMonitor("Ppar");

moonlightScript.setMinMaxDomain(); % for the quantitative semantics
% formula P2 = escape(hop)[5, inf] (battery > 0.5) ;
P2monitor = moonlightScript.getMonitor("P2");
% P3 = somewhere(hop)[0, 3] (battery > 0.5) ;
P3Monitor = moonlightScript.getMonitor("P3");

inputModel =spatialModelc;
%% Results
%evaluate ReachFormula
resultReachMonitor = P1Monitor.monitor(inputModel,time,values);
%evaluate the parametric ParametricReachFormula with k=1, type=2
resultParamMonitor = PparMonitor.monitor(inputModel,time,values, 1);
%evaluate escapeFormula
resultEscapeMonitor = P2monitor.monitor(inputModel,time,values);

%evaluate SomeWhereFormula
resultSomeMonitor = P3Monitor.monitor(inputModel,time,values,inf);

%% plots
plotResults(inputModel, resultReachMonitor, true);
plotResults(inputModel, resultEscapeMonitor, false);
plotResults(inputModel, resultSomeMonitor, false);
