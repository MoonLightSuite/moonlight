clear
load("data.mat")
monitor = MoonlightEngine.load("sensor");
result = monitor.spatioTemporalMonitor(vorSpTemModel,time,signal);
