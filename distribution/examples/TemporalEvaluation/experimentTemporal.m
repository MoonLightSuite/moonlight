clear;

% Trajectory
trajFunction = @(t)t.^2';
numStep = 101;
endTime = 100;
tStep = endTime/(numStep-1);
time = 0:tStep:endTime;

%values = trajFunction(time);
values  = 1000*rand(numStep,1) - 20 ;

%%%%% MoonLight  %%%%%%%%
monitor = MoonlightEngine.load("TempFormScript");
[resultMoonlight, timeMoonLight] = monitor.temporalMonitor(...
"nesting",time,values);
resultMoonlight1 = resultMoonlight(1,2);

%%%%%  TALIRO prop %%%%%%%%
% x >= 0
psi_Pred(1).str = 'a';
psi_Pred(1).A   =  -1;
psi_Pred(1).b   =  0;

%psi = '[]_[73,98] a';
psi = '<>_[0,80]([]_[1,10] a)';

%%%%  BREACH prop %%%%%%%%
%phiBreach = 'alw_[73,98](X[t]>0)';
phiBreach = 'ev_[0,80](alw_[1,10](X[t]>0))';

[rTal,timeTal, rBreach1, timeBreach] = tempEval(values,time',psi,psi_Pred,phiBreach);
