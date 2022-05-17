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
% plot of the last frame
% numframe = 1;
% plotGraph(spatialModelv, 1 , 'node');

% % % %  % create video writer object
% % % %  writerObj = VideoWriter('con50.avi');
% % % %  % set the frame rate to one frame per second
% % % %  set(writerObj,'FrameRate',10);
% % % %  % open the writer
% % % %  open(writerObj);
% % % %  % iterate over each image
% % % %  for k=1:length(frames)
% % % %      % write the frame to the video
% % % %      writeVideo(writerObj,frames(k));
% % % %  end
% % % %  % close the writer
% % % %  close(writerObj);


%% monitor

script= [
"signal { int nodeType; real battery; real temperature; }",...
"space {edges { int hop; real dist; }}",...
"domain boolean;",... 
"formula P1 = ( nodeType==3 ) reach (hop)[0, 1] (( nodeType==1) | (nodeType==2 ));",...
"formula Ppar (int k) = ( nodeType==3 ) reach (hop)[0, k] ( nodeType== 1);",...
"formula P2 = escape(hop)[5,inf] (battery > 0.5) ;",...
"formula P3 = somewhere(dist)[0,250] (battery > 0.5);"
];

% loading of the script
%generate a moonlightScript object from the script file multipleMonitors.mls (contained in this folder)
%this object is an implementation of ScriptLoader class, please refer to 
%the doc of this class for more details (ex. write in console "doc ScriptLoader" )

moonlightScript = ScriptLoader.loadFromFile("sensorNetMonitorScript.mls");
%moonlightScript = ScriptLoader.loadFromText(script);

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
% %evaluate the parametric ParametricReachFormula with k=1, type=2
% resultParamMonitor = PparMonitor.monitor(inputModel,time,values, 1);
% %evaluate escapeFormula
% resultEscapeMonitor = P2monitor.monitor(inputModel,time,values);
%
% %evaluate SomeWhereFormula
resultSomeMonitor = P3Monitor.monitor(inputModel,time,values);

%% plots
plotResults(inputModel, resultReachMonitor, true);
% plotResults(inputModel, resultEscapeMonitor, false);
plotResults(inputModel, resultSomeMonitor, false);

%  % create video writer object
%  writerObj = VideoWriter('sol.avi');
%  % set the frame rate to one frame per second
%  set(writerObj,'FrameRate',5);
%  % open the writer
%  open(writerObj);
%  % iterate over each image
%  for k=1:length(frameSet)
%      % write the frame to the video
%      writeVideo(writerObj,frameSet(k));
%  end
%  % close the writer
%  close(writerObj);
