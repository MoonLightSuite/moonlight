%% Initializing the script
clear;       %clear all the memory
close all;   %close all the open windows

elapseTimeSeq = [];
num_exp = 50;
N =[10, 100,1000];

numSteps =1;
% % increasing the number of nodes
moonlight_sat_time_spec1 = [];
moonlight_sat_time_spec2 = [];
moonlight_sat_time_spec3 = [];
moonlight_rob_time_spec1 = [];
moonlight_rob_time_spec2 = [];
moonlight_rob_time_spec3 = [];

currDate = strrep(datestr(datetime), ' ', '_');
status = mkdir('testResults',currDate);
for num_nodes = N
          
    %% Generating input signals
    fprintf('Generating input signals with %f \n', num_nodes);
    [spatialModelv,spatialModelc,time,signalInput]= sensorModel(num_nodes,numSteps, false);
    
    [b_time_results, rob_time_results] = monSpTempMon (spatialModelc,time,signalInput, num_exp);
    moonlight_sat_time_spec1 = [moonlight_sat_time_spec1, b_time_results(1)];
    moonlight_sat_time_spec2 = [moonlight_sat_time_spec2, b_time_results(2)];
    moonlight_sat_time_spec3 = [moonlight_sat_time_spec3, b_time_results(3)];
    moonlight_rob_time_spec1 = [moonlight_rob_time_spec1, rob_time_results(1)];
    moonlight_rob_time_spec2 = [moonlight_rob_time_spec2, rob_time_results(2)];
    moonlight_rob_time_spec3 = [moonlight_rob_time_spec3, rob_time_results(3)];
    
    fprintf('Specification (Satisfaction)  - Moonlight Times (sec): p1=%f, p2=%f, p3=%f \n', b_time_results(1), b_time_results(2), b_time_results(3));
    fprintf('Specification (Robustness)  - Moonlight Times (sec): p1=%f, p2=%f, p3=%f  \n', rob_time_results(1), rob_time_results(2), rob_time_results(3));

end

save (strcat('./testResults/',currDate,'/dataInput.mat'), 'time','spatialModelv', 'spatialModelc','signalInput','N', 'num_exp', 'numSteps');
save (strcat('./testResults/',currDate,'/moonlight_sat_times_stat.mat'), 'moonlight_sat_time_spec1', 'moonlight_sat_time_spec2', 'moonlight_sat_time_spec3');
save (strcat('./testResults/',currDate,'/moonlight_rob_times_stat.mat'), 'moonlight_rob_time_spec1', 'moonlight_rob_time_spec2', 'moonlight_rob_time_spec3');

function [b_time_results,rob_time_results] = monSpTempMon (spatialModel,time,signalInput, num_exp)
%% Initializing the script

% loading of the script
moonlightScript = ScriptLoader.loadFromFile("sensorScript");

bMonitor1 = moonlightScript.getMonitor("P2");
bMonitor2 = moonlightScript.getMonitor("P3");
bMonitor3 = moonlightScript.getMonitor("P4");

moonlightScript.setMinMaxDomain();
qMonitor1 = moonlightScript.getMonitor("P2");
qMonitor2 = moonlightScript.getMonitor("P3");
qMonitor3 = moonlightScript.getMonitor("P4");

b_time_results = zeros(3,1);
rob_time_results  = zeros(3,1);

bMonitorResult1 = bMonitor1.monitor(spatialModel,time,signalInput);
bMonitorResult1 = bMonitor1.monitor(spatialModel,time,signalInput);
bMonitorResult1 = bMonitor1.monitor(spatialModel,time,signalInput);
tElapsedSpecMoonlightBoolean = 0;
for i=1:num_exp
    tStart                = tic;
    bMonitorResult1 = bMonitor1.monitor(spatialModel,time,signalInput);
    tElapsedSpecMoonlightBoolean   = tElapsedSpecMoonlightBoolean + toc(tStart);
end
b_time_results(1) = tElapsedSpecMoonlightBoolean/num_exp; 

tElapsedSpecMoonlightQuant = 0;
for i=1:num_exp
    tStart                = tic;
    qMonitorResult1 = qMonitor1.monitor(spatialModel,time,signalInput);
    tElapsedSpecMoonlightQuant   = tElapsedSpecMoonlightQuant + toc(tStart);
end
rob_time_results(1) = tElapsedSpecMoonlightQuant/num_exp;

tElapsedSpecMoonlightBoolean = 0;
for i=1:num_exp
    tStart                = tic;
    bMonitorResult2 = bMonitor2.monitor(spatialModel,time,signalInput);
    tElapsedSpecMoonlightBoolean   = tElapsedSpecMoonlightBoolean + toc(tStart);
end
b_time_results(2) = tElapsedSpecMoonlightBoolean/num_exp;

tElapsedSpecMoonlightQuant = 0;
for i=1:num_exp
    tStart                = tic;
    qMonitorResult2 = qMonitor2.monitor(spatialModel,time,signalInput);
    tElapsedSpecMoonlightQuant   = tElapsedSpecMoonlightQuant + toc(tStart);
end
rob_time_results(2) = tElapsedSpecMoonlightQuant/num_exp;

tElapsedSpecMoonlightBoolean = 0;
for i=1:num_exp
    tStart                = tic;
    bMonitorResult3 = bMonitor3.monitor(spatialModel,time,signalInput);
    tElapsedSpecMoonlightBoolean   = tElapsedSpecMoonlightBoolean + toc(tStart);
end
b_time_results(3) = tElapsedSpecMoonlightBoolean/num_exp;

tElapsedSpecMoonlightQuant = 0;
for i=1:num_exp
    tStart                = tic;
    qMonitorResult3 = qMonitor3.monitor(spatialModel,time,signalInput);
    tElapsedSpecMoonlightQuant   = tElapsedSpecMoonlightQuant + toc(tStart);
end
rob_time_results(3) =  tElapsedSpecMoonlightQuant/num_exp;

end
   
