clear

monitor = MoonlightEngine.load("TempFormScript");
trajFunctionV = @(t)[t.^2;cos(t);sin(t)]';
trajFunction = @(t)t.^2';
timeStep = 0.01;
time = 0:timeStep:100;
values = trajFunction(time);

%%%%% MoonLight  %%%%%%%%
%tStart =tic;
[resultMoonlight, tElapsedMoonLight] = monitor.temporalMonitor("RandomFormulaeQ",time,values);
%tElapsedMoonLight = toc(tStart);
resultMoonlight1 = resultMoonlight(1,2);

%%%%%  TALIRO  %%%%%%%%
% x >= 0
psi_Pred(1).str = 'a';
psi_Pred(1).A   =  -1;
psi_Pred(1).b   =  0;
psi = '[]_[73.006,98.272] a';

tStart =tic;
resultTaliro = fw_taliro(psi,psi_Pred,values,time');
tElapsedTaliro = toc(tStart);

%%%%  BREACH  %%%%%%%%
InitBreach
BrTrace = BreachTraceSystem({'X'}, [time' values]);
% prop
phiBreach = 'alw_[73.006,98.272](X[t]>=0)';
BreachProp= STL_Formula('A',phiBreach);
tStart =tic;
[resultBreach, tau] =  STL_Eval(BrTrace.Sys, BreachProp, BrTrace.P, BrTrace.P.traj,'thom');
tElapsedBreach = toc(tStart);
resultBreach1 = resultBreach(1);

