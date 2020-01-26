clear
monitor = MoonlightEngine.load("randomFormulae");
trajFunction = @(t)[t;t;t]';
time = 0:1:100;
values = trajFunction(time);
result = monitor.temporalMonitor(time,values);