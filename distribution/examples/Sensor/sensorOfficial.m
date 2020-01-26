clear
numSteps        = 20;
num_nodes       = 50;
[spatialModel,time,signal]= sensorSystem(numSteps,num_nodes);
monitor = MoonlightEngine.load("sensor");
result = monitor.spatioTemporalMonitor(spatialModel,time,signal);