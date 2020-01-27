clear

monitor = MoonlightEngine.load("TempFormScript");
trajFunctionV = @(t)[t.^2;cos(t);sin(t)]';
trajFunction = @(t)t.^2';
timeStep = 1;
time = 0:timeStep:100;
values = trajFunction(time);
valuesV = trajFunctionV(time);

%%%%% MoonLight  %%%%%%%%
%tStart =tic;
[resultMoonlight, tElapsedMoonLight] = monitor.temporalMonitor("nesting",time,values);
%tElapsedMoonLight = toc(tStart);
result = resultMoonlight(1,2);

%%%%%  TALIRO  %%%%%%%%
% x >= 0
psi_Pred(1).str = 'a';
psi_Pred(1).A   =  -1;
psi_Pred(1).b   =  0;

psi = '[]_[73.007,98.272] a';
psinest = '<>_[0,80]([]_[1,10] a)';

tStart =tic;
resultTaliro = fw_taliro(psinest,psi_Pred,values,time');
tElapsedTaliro = toc(tStart);

%%%%  BREACH  %%%%%%%%
InitBreach
%trace
%trace = [time X]; % trace is in column format, first column is time
BrTrace = BreachTraceSystem({'X'}, [time' values]);
%figure; BrTrace.PlotSignals();

% prop
phiBreach = 'alw_[73.007,98.272](X[t]>0)';
phiBreachNest = 'ev_[0,80](alw_[1,10](X[t]>0))';
BreachProp= STL_Formula('A',phiBreach);
BreachPropNest= STL_Formula('A',phiBreachNest);
tStart =tic;
[resultBreach, tau] =  STL_Eval(BrTrace.Sys, BreachPropNest, BrTrace.P, BrTrace.P.traj,'thom');
tElapsedBreach = toc(tStart);
resultBreach1 = resultBreach(1);

