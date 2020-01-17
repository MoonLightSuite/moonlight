clear
monitor = MoonlightMatlab;
monitor.Script = eu.quanticol.moonlight.api.TestMoonLightScript;
time = 1:1:10;
trajFunction = @(t) [t;t]';
result = monitor.monitor(time,trajFunction(time));
