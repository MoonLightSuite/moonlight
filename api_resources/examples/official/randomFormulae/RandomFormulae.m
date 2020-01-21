clear
monitor = MoonlightEngine;
monitor.Script = eu.quanticol.moonlight.api.example.RandomFormulae;
trajFunction = @(t)[t;t;t]';
time = 0:1:100;
values = trajFunction(time);
result = monitor.temporalMonitor(time,values);