clear

monitor = MoonlightEngine.load("TempFormScript");
%trajFunction = @(t)t.^2';
trajFunction = @(t)cos(t)';
time = 0:1:100;
values = trajFunction(time);

%%%%% MoonLight  %%%%%%%%
tStart =tic;
resultMoonlight = monitor.temporalMonitor(time,values,"RandomFormulae2");
tElapsedMoonLight = toc(tStart);
result = resultMoonlight(1,2);

%%%%%  TALIRO  %%%%%%%%
% x >= 0
psi_Pred(1).str = 'a';
psi_Pred(1).A   =  -1;
psi_Pred(1).b   =  0;

psi = '<>_[0,99] a';

tStart =tic;
resultTaliro = fw_taliro(psi,psi_Pred,values,time');
tElapsedTaliro = toc(tStart);

%%%%  BREACH  %%%%%%%%
InitBreach
%trace
%trace = [time X]; % trace is in column format, first column is time
BrTrace = BreachTraceSystem({'X'}, [time' values]);
%figure; BrTrace.PlotSignals();

% prop
phiBreach = 'ev_[0,99](X[t]>=0)';
BreachProp= STL_Formula('A',phiBreach);
tStart =tic;
[resultBreach, tau] =  STL_Eval(BrTrace.Sys, BreachProp, BrTrace.P, BrTrace.P.traj,'thom');
tElapsedBreach = toc(tStart);

