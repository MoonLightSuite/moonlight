%% Initializing the script
clear;       %clear all the memory
close all;   %close all the open windows

num_exp = 50;
N=[10,100,1000];
K =[10, 100];
for numSteps = K
    moonlight_sat_time_spec1 = [];
    moonlight_sat_time_spec2 = [];
    moonlight_rob_time_spec1 = [];
    moonlight_rob_time_spec2 = [];
    currDate = strrep(datestr(datetime), ' ', '_');
    status = mkdir('testResults',currDate);
    for num_nodes = N

        %% Generating input signals
        fprintf('Generating input signals with %f \n', num_nodes);
        [spatialModelv,spatialModelc,time,signalInput]= sensorModel(num_nodes,numSteps, false);

        [b_time_results, rob_time_results] = monSpTempMon (spatialModelv,time,signalInput, num_exp);
        moonlight_sat_time_spec1 = [moonlight_sat_time_spec1, b_time_results(1)];
        moonlight_sat_time_spec2 = [moonlight_sat_time_spec2, b_time_results(2)];
        moonlight_rob_time_spec1 = [moonlight_rob_time_spec1, rob_time_results(1)];
        moonlight_rob_time_spec2 = [moonlight_rob_time_spec2, rob_time_results(2)];

        fprintf('Specification (Satisfaction)  - Moonlight Times (sec): p1=%f, p2=%f  \n', b_time_results(1), b_time_results(2));
        fprintf('Specification (Robustness)  - Moonlight Times (sec): p1=%f, p2=%f  \n', rob_time_results(1), rob_time_results(2));

    end
    save (strcat('./testResults/',currDate,'/dataInput.mat'), 'time', 'spatialModelv', 'spatialModelc','signalInput', 'num_exp','N', 'numSteps');
    save (strcat('./testResults/',currDate,'/moonlight_sat_times_stat.mat'), 'moonlight_sat_time_spec1', 'moonlight_sat_time_spec2');
    save (strcat('./testResults/',currDate,'/moonlight_rob_times_stat.mat'), 'moonlight_rob_time_spec1', 'moonlight_rob_time_spec2');
end


function [b_time_results,rob_time_results] = monSpTempMon (spatialModel,time,signalInput, num_exp)
%% Initializing the script

% loading of the script
moonlightScript = ScriptLoader.loadFromFile("sensorScript");

bMonitor1 = moonlightScript.getMonitor("PT1");
bMonitor2 = moonlightScript.getMonitor("PT2");

moonlightScript.setMinMaxDomain();
qMonitor1 = moonlightScript.getMonitor("PT1");
qMonitor2 = moonlightScript.getMonitor("PT2");
b_time_results = zeros(2,1);
rob_time_results  = zeros(2,1);

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

tElapsedSpec1MoonlightQuant = 0;
for i=1:num_exp
    tStart                = tic;
    qMonitorResult1 = qMonitor1.monitor(spatialModel,time,signalInput);
    tElapsedSpec1MoonlightQuant   = tElapsedSpec1MoonlightQuant + toc(tStart);
end
rob_time_results(1) = tElapsedSpec1MoonlightQuant/num_exp;

tElapsedSpecMoonlightBoolean = 0;
for i=1:num_exp
    tStart                = tic;
    bMonitorResult2 = bMonitor2.monitor(spatialModel,time,signalInput);
    tElapsedSpecMoonlightBoolean   = tElapsedSpecMoonlightBoolean + toc(tStart);
end
b_time_results(2) = tElapsedSpecMoonlightBoolean/num_exp;

tElapsedSpec1MoonlightQuant = 0;
for i=1:num_exp
    tStart                = tic;
    qMonitorResult2 = qMonitor2.monitor(spatialModel,time,signalInput);
    tElapsedSpec1MoonlightQuant   = tElapsedSpec1MoonlightQuant + toc(tStart);
end
rob_time_results(2) = tElapsedSpec1MoonlightQuant/num_exp;
end