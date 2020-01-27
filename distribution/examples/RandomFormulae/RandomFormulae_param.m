clear
monitor = MoonlightEngine.load("randomFormulae_param");
trajFunction = @(t)[t;t;t]';
T = 20;
time = 0:1:100;
values = trajFunction(time);
result = monitor.temporalMonitor("RandomFormulae",time,values,T);