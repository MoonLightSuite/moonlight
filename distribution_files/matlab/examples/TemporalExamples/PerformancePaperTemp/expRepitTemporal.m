clear;
%%%% PROPERTY %%%
%%%%%  MOONLIGHT prop %%%%%%%%
monitor = MoonlightEngineFast.load("TempFormScript");
%phiString = "RandomFormulaeQ";
phiString = "nesting";
%%%%%  TALIRO prop %%%%%%%%
% x >= 0
psi_Pred(1).str = 'a';
psi_Pred(1).A   =  -1;
psi_Pred(1).b   =  0;

%psi = '[] a';
psi = '<>_[0,80]([]_[1,10] a)';

%%%%  BREACH prop %%%%%%%%
%phiBreach = 'alw(X[t]>0)';
phiBreach = 'ev_[0,80](alw_[1,10](X[t]>0))';
nRuns = 10;
%numStep = 1001;
timeMoonRepSignal = [];
timeTalRepSignal = [];
timeBreachRepSignal = [];
numStepInt= 101:1001:10001;
% for numStep= numStepInt
%     numStep
%     [meanTimeMoon ,meanTimeTal, meaTimeBreach]  =  evalRep(nRuns, numStep,...
%     monitor, phiString,psi,psi_Pred,phiBreach);
%     timeMoonRepSignal = [timeMoonRepSignal,meanTimeMoon];
%     timeTalRepSignal = [timeTalRepSignal,meanTimeTal];
%     timeBreachRepSignal = [timeBreachRepSignal,meaTimeBreach];
% end
nRuns = 10;
numStep = 10000;
[meanTimeMoon ,meanTimeTal, meaTimeBreach]  =  evalRep(nRuns, numStep,...
monitor, phiString,psi,psi_Pred,phiBreach);
timeMoonRepSignal = [timeMoonRepSignal,meanTimeMoon];
timeTalRepSignal = [timeTalRepSignal,meanTimeTal];
timeBreachRepSignal = [timeBreachRepSignal,meaTimeBreach];


t =numStepInt;
plot(t,timeMoonRepSignal,'b-+',t,timeTalRepSignal,'r--*',t,timeBreachRepSignal,'g-.o',...
'LineWidth',2,'MarkerSize',10);
set(gca,'FontSize',20)
legend('Moonlight','Taliro','Breach','FontSize',20)

function [meanTimeMoon ,meanTimeTal, meaTimeBreach] = evalRep(nRuns, numStep, monitor, phiString,psi,psi_Pred,phiBreach)
% Trajectory
endTime = 100;
tStep = endTime/(numStep-1);
time = 0:tStep:endTime;

timeMoonRep = [];
timeTalRep = [];
timeBreachRep = [];
for i = 1:nRuns
    i
    values  = 1000*rand(numStep,1) - 20 ;
    %%%%% MoonLight  %%%%%%%%
    start = tic;
    [resultMoonlight, timeMoonLight] = monitor.temporalMonitor(...
    phiString,time,values);
    timeMoonLight = toc(start);
    rMoon = resultMoonlight(1,2);
    timeMoonRep = [timeMoonRep,timeMoonLight];
    a = cell(1);
    [rTal,timeTal, rBreach1, timeBreach] = tempEval(values,time',psi,psi_Pred,phiBreach,time);
    timeTalRep = [timeTalRep,timeTal ];
    timeBreachRep = [timeBreachRep ,timeBreach];
    %resultMoonlight1 =resultMoonlight(1,2)
    %rTal
    %rBreach1
end
meanTimeMoon = mean(timeMoonRep);
meanTimeTal = mean(timeTalRep);
meaTimeBreach = mean(timeBreachRep);
end