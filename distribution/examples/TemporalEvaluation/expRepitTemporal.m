clear;
%%%% PROPERTY %%%
%%%%%  MOONLIGHT prop %%%%%%%%
monitor = MoonlightEngine.load("TempFormScript");
phiString = "RandomFormulaeQ";
%%%%%  TALIRO prop %%%%%%%%
% x >= 0
psi_Pred(1).str = 'a';
psi_Pred(1).A   =  -1;
psi_Pred(1).b   =  0;

psi = '[]_[73,98] a';
% psi = '<>_[0,80]([]_[1,10] a)';

%%%%  BREACH prop %%%%%%%%
phiBreach = 'alw_[73,98](X[t]>0)';
% phiBreach = 'ev_[0,80](alw_[1,10](X[t]>0))';

numStep = 101;
nRuns = 10;

[meanTimeMoon ,meanTimeTal, meaTimeBreach]  =  evalRep(nRuns, numStep,...
    monitor, phiString,psi,psi_Pred,phiBreach);

function [meanTimeMoon ,meanTimeTal, meaTimeBreach] = evalRep(nRuns, numStep, monitor, phiString,psi,psi_Pred,phiBreach)
% Trajectory
endTime = 100;
tStep = endTime/(numStep-1);
time = 0:tStep:endTime;

timeMoonRep = [];
timeTalRep = [];
timeBreachRep = [];
for i = 1:nRuns
    values  = 1000*rand(numStep,1) - 20 ;
    %%%%% MoonLight  %%%%%%%%
    [resultMoonlight, timeMoonLight] = monitor.temporalMonitor(...
    phiString,time,values);
    rMoon = resultMoonlight(1,2);
    timeMoonRep = [timeMoonRep,timeMoonLight];
    [rTal,timeTal, rBreach1, timeBreach] = tempEval(values,time',psi,psi_Pred,phiBreach);
    timeTalRep = [timeTalRep,timeTal ];
    timeBreachRep = [timeBreachRep ,timeBreach];
end
meanTimeMoon = mean(timeMoonRep);
meanTimeTal = mean(timeTalRep);
meaTimeBreach = mean(timeBreachRep);
end