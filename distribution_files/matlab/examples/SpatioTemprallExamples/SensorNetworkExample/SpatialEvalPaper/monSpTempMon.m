function [b_time_results,rob_time_results] = monSpTempMon (spatialModel,time,signalInput, num_exp)
%% Initializing the script

% loading of the script
%generate a moonlightScript object from the script file multipleMonitors.mls (contained in this folder)
%this object is an implementation of ScriptLoader class, please refer to 
%the doc of this class for more details (ex. write in console "doc ScriptLoader" )
moonlightScript = ScriptLoader.loadFromFile("sensorScript");

bMonitor1 = moonlightScript.getMonitor("P1");
bMonitor2 = moonlightScript.getMonitor("P2");
bMonitor3 = moonlightScript.getMonitor("P3");
bMonitor4 = moonlightScript.getMonitor("P4");

moonlightScript.setMinMaxDomain();
qMonitor1 = moonlightScript.getMonitor("P1");
qMonitor2 = moonlightScript.getMonitor("P2");
qMonitor3 = moonlightScript.getMonitor("P3");
qMonitor4 = moonlightScript.getMonitor("P4");

b_time_results = zeros(4,2);
rob_time_results  = zeros(4,2);


for i=1:num_exp
    tElapsedSpec1MoonlightBoolean = 0;
    tStart                = tic;
    bMonitorResult1 = bMonitor1.monitor(spatialModel,time,signalInput);
    tElapsedSpec1MoonlightBoolean   = tElapsedSpec1MoonlightBoolean + toc(tStart);
end
b_time_results(1) = tElapsedSpec1MoonlightBoolean/num_exp; 

for i=1:num_exp
    tElapsedSpec1MoonlightQuant = 0;
    tStart                = tic;
    qMonitorResult1 = qMonitor1.monitor(spatialModel,time,signalInput);
    tElapsedSpec1MoonlightQuant   = tElapsedSpec1MoonlightQuant + toc(tStart);
end
rob_time_results(1) = tElapsedSpec1MoonlightQuant/num_exp;

for i=1:num_exp
    tElapsedSpec1MoonlightBoolean = 0;
    tStart                = tic;
    bMonitorResult2 = bMonitor2.monitor(spatialModel,time,signalInput);
    tElapsedSpec1MoonlightBoolean   = tElapsedSpec1MoonlightBoolean + toc(tStart);
end
b_time_results(2) = tElapsedSpec1MoonlightBoolean/num_exp;

for i=1:num_exp
    tElapsedSpec1MoonlightQuant = 0;
    tStart                = tic;
    qMonitorResult2 = qMonitor2.monitor(spatialModel,time,signalInput);
    tElapsedSpec1MoonlightQuant   = tElapsedSpec1MoonlightQuant + toc(tStart);
end
rob_time_results(2) = tElapsedSpec1MoonlightQuant/num_exp;


for i=1:num_exp
    tElapsedSpec1MoonlightBoolean = 0;
    tStart                = tic;
    bMonitorResult3 = bMonitor3.monitor(spatialModel,time,signalInput);
    tElapsedSpec1MoonlightBoolean   = tElapsedSpec1MoonlightBoolean + toc(tStart);
end
b_time_results(3) = tElapsedSpec1MoonlightBoolean/num_exp;

for i=1:num_exp
    tElapsedSpec1MoonlightQuant = 0;
    tStart                = tic;
    qMonitorResult3 = qMonitor3.monitor(spatialModel,time,signalInput);
    tElapsedSpec1MoonlightQuant   = tElapsedSpec1MoonlightQuant + toc(tStart);
end
rob_time_results(3) =  tElapsedSpec1MoonlightQuant/num_exp;


for i=1:num_exp
    tElapsedSpec1MoonlightBoolean = 0;
    tStart                = tic;
    bMonitorResult4 = bMonitor4.monitor(spatialModel,time,signalInput);
    tElapsedSpec1MoonlightBoolean   = tElapsedSpec1MoonlightBoolean + toc(tStart);
end
b_time_results(4) =  tElapsedSpec1MoonlightBoolean/num_exp;

for i=1:num_exp
    tElapsedSpec1MoonlightQuant = 0;
    tStart                = tic;
    qMonitorResult4 = qMonitor4.monitor(spatialModel,time,signalInput);
    tElapsedSpec1MoonlightQuant   = tElapsedSpec1MoonlightQuant + toc(tStart);
end
rob_time_results(4) = tElapsedSpec1MoonlightQuant/num_exp;

end







