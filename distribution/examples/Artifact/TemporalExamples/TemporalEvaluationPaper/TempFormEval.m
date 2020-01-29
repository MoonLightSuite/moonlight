clear

monitor = MoonlightEngine.load("TempFormScript");
%trajFunction = @(t)[t.^2;cos(t);sin(t)]';
trajFunction = @(t)t.^2';
timeStep = 0.01;
endTime = 100;
numstep = (endTime)/0.01+1;
time = 0:timeStep:endTime;

values  = 100*rand(numstep,1) - 20 ;

%%%%% MoonLight  %%%%%%%%
%tStart =tic;
[resultMoonlight, tElapsedMoonLight] = monitor.temporalMonitor("nesting",time,values);
%tElapsedMoonLight = toc(tStart);
resultMoonlight1 = resultMoonlight(1,2);

%%%%%  TALIRO  %%%%%%%%
% x >= 0
psi_Pred(1).str = 'a';
psi_Pred(1).A   =  -1;
psi_Pred(1).b   =  0;
psi = '<>_[0,1] a';

tStart =tic;
resultTaliro = fw_taliro(psi,psi_Pred,values,time');
tElapsedTaliro = toc(tStart);

%%%%  BREACH  %%%%%%%%
InitBreach
BrTrace = BreachTraceSystem({'X'}, [time' values]);
% prop
phiBreach = 'ev_[0,1](X[t]>=0)';
BreachProp= STL_Formula('phi',phiBreach);

tStart =tic;
resultBreach= BrTrace.CheckSpec('phi');
tElapsedBreach = toc(tStart);
resultBreach = resultBreach(1);


