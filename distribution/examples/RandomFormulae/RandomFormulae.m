clear
monitor = MoonlightEngine.load("randomformulae");
trajFunction = @(t)[t;t;t]';
time = 0:1:100;
values = trajFunction(time);
result = monitor.temporalMonitor("RandomFormulae",time,values);