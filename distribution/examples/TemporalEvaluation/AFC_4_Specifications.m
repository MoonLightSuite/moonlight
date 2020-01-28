%% Signal Temporal Logic (STL) Specifications 

%% Initialization
% The following script creates a default interface with the
% AbstractFuelControl model.
clear; close all;
BrDemo.InitAFC;
BrAFC

%% Writing a Simple STL Specification 
% First we define a predicate stating that AF is above 1% of AFref
AF_not_ok = STL_Formula('AF_ok', 'abs(AF[t]- AFref[t]) < 0.01*14.7')

%%
% The first argument 'AF_ok' is an id for the formula needed so that
% it can be referenced as a sub-formula. 

%%
% The second argument is a string describing the formula. The syntax AF[t] refers to the value of  
% signal AF at a time t. If we evaluate this formula, t will be
% instantiated with 0. We need temporal operators to examine other time
% instants. 

%%
% For example, we define next a formula stating "some time in the future, AF_ok is true". 
AF_ev_ok = STL_Formula('AF_ev_ok', 'ev (AF_ok)')

%%
% ev is a shorthand for 'eventually'. Other temporal operators are 'alw' or 'always' and 'until'.  

%% Checking a Simple Specification on a Simulation
% Temporal operators can specify a time range to operate on. For our
% system, AF does not need to be checked before 10s, and we simulate until
% 30s so we check the following formula:
AF_alw_ok = STL_Formula('AF_alw_ok', 'alw_[10,30] (AF_ok)') % alw shorthand for in 'always'

%%
% Then we check our formula on a simulation with nominal parameters:
AFC_w_Specs= BrAFC.copy();
Time = 0:.05:30;
AFC_w_Specs.Sim(Time);
AFC_w_Specs.PlotSignals({'AF','AFref','controller_mode'}); 

tStart =tic;
rob1  = AFC_w_Specs.CheckSpec(AF_ev_ok)
tElapsedBreach1 = toc(tStart);
tStart =tic;
rob2  = AFC_w_Specs.CheckSpec(AF_alw_ok)
tElapsedBreach2 = toc(tStart);

model = AFC_w_Specs.P.traj{1}.X;
AF = model(1,:);
AFref = model(2,:);
controller_mode = model(4,:);
AbsAF= abs(AF - AFref);
values = [AF; AFref;AbsAF; controller_mode]';
monitor = MoonlightEngine.load("TempFormScript");
[resultMoonlight, timeMoonLight1] = monitor.temporalMonitor("AF_ev_ok",Time,values);
resultMoonlight1 = resultMoonlight(1,2);
[resultMoonlight, timeMoonLight2] = monitor.temporalMonitor("AF_alw_ok",Time,values);
resultMoonlight2 = resultMoonlight(1,2);


%% Checking Another Formula
% The reason we are not interested in AF between 0 and 10s is because the 
% controller is not in a mode where it tries to regulate it at this time.
% We can implement this explicitly using the controller_mode signal:
AF_alw_ok2 = STL_Formula('AF_alw_ok2', 'alw (controller_mode[t]==0 => AF_ok)')
[resultMoonlight, timeMoonLight3] = monitor.temporalMonitor("AF_alw_ok2",Time,values);
resultMoonlight3 = resultMoonlight(1,2);
%%
% Then check the new formula on the simulation we performed already:
tStart =tic;
rob3 = AFC_w_Specs.CheckSpec(AF_alw_ok2)
tElapsedBreach3 = toc(tStart);


%% Monitoring a Formula on a Trace (1)
% To monitor STL formulas on existing traces, one can use the
% BreachTraceSystem class. 

% create a trace with signals x and y
time = 0:.05:10; x = cos(time); y = sin(time);
values =[x;y]';
trace = [time' x' y']; % trace is in column format, first column is time
BrTrace = BreachTraceSystem({'x','y'}, trace); 
figure; BrTrace.PlotSignals();

%% Monitoring a Formula on a Trace (2)
% Checks (plots) some formula on imported trace:
figure; BrTrace.PlotRobustSat('alw (x[t] > 0) or alw (y[t]>0)');
BreachProp=STL_Formula('phi','alw (x[t] > 0) or alw (y[t]>0)');
rob4=BrTrace.CheckSpec('phi');

[resultMoonlight, timeMoonLight4] = monitor.temporalMonitor("specBreach",time,values);
resultMoonlight4 = resultMoonlight(1,2);





