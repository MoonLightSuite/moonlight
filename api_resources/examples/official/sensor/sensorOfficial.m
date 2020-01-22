clear
load("data.mat")
monitor = MoonlightEngine;
monitor.Script = eu.quanticol.moonlight.api.example.Sensor;
result = monitor.spatioTemporalMonitor(vorSpTemModel,time,signal);
