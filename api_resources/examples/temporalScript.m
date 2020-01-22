clear
monitor = MoonlightEngine;
monitor.Script = eu.quanticol.moonlight.api.example.TestMoonLightScriptImpl;
trajFunction = @(t)[t;t]';
time = 1:1:10;
values = trajFunction(time);
result = monitor.temporalMonitor(time,values);