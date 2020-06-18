% file multipleMonitorSimple.m

clear
close all;

%% STEP 1: generating signal
trajFunction = @(t)[sin(t);cos(t)]';
time = 0:0.1:3.1;
values = trajFunction(time);

%% STEP 2: loading the Moonlight Script
moonlightScript = ScriptLoader.loadFromFile("multipleMonitors");

%% STEP 3 (optional): change the domain on the fly
moonlightScript.setMinMaxDomain();

%% STEP 4: getting the monitor associated with a target formula
boolFutureMonitor = moonlightScript.getMonitor("future");

%% STEP 5: monitor the signal 
boolFutureMonitorResult = boolFutureMonitor.monitor(time,values);