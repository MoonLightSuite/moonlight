clear;
monitor = MoonlightEngine.load("prova");
trajFunction = @(t)t.^2';
timeStep = 0.1;
time = 0:timeStep:100;
values = trajFunction(time);

%%%%% MoonLight  %%%%%%%%
[resultMoonlight, timeMoonLight] = monitor.temporalMonitor("Prova3",time,values);
resultMoonlight1 = resultMoonlight(1,2);

% %%%%%  TALIRO prop %%%%%%%%
% % x >= 0
% psi_Pred(1).str = 'a';
% psi_Pred(1).A   =  -1;
% psi_Pred(1).b   =  0;
% 
% psi = '[]_[73.007,98.272] a';
% % psi = '<>_[0,80]([]_[1,10] a)';
% 
% %%%%  BREACH prop %%%%%%%%
% 
% phiBreach = 'alw_[73.007,98.272](X[t]>0)';
% % phiBreach = 'ev_[0,80](alw_[1,10](X[t]>0))';
% 
% [rTal,timeTal, rBreach1, timeBreach] = tempEval(values,time',psi,psi_Pred,phiBreach);
