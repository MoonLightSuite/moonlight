clear
numSteps        = 20;
num_nodes       = 50;
[vorSpTemModel,time,signal]= sensorSystem(numSteps,num_nodes);
monitor = MoonlightEngine.load("sensor");
result = monitor.spatioTemporalMonitor(vorSpTemModel,time,signal);