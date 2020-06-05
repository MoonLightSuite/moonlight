%% Initializing the script

clear;       %clear all the memory
close all;   %close all the open windows

%% Configuration file for simulating the Simulink model "autotrans_mod04"
% Variables:
%    - stime - simulation_time (unit sec)
%    - dt - integration step (unit sec)
%    - piecewise_throttle - define the input signal of the throttle
%    - piecewise_break - define the input signal of the break

dt            =  0.01;
stime         =  32;
solver        = 'ode5';
num_exp       = 50;

model         = 'autotrans_mod04'
input_labels  = {'time', 'throttle', 'brake'};
output_labels = {'engine speed in rpm', 'vehicle speed in mph', 'gear'};

fprintf('Settings\n\n');
fprintf('\t dt     = %f \n',  dt    );
fprintf('\t stime  = %f \n',  stime );
fprintf('\t solver = %s \n\n',solver);


%picewise_throttle = [  0,  5,   5, 10, 10, 15, 15, 20, 20, 25, 25, stime;   %time
%                      52, 52,  95, 95, 60, 60, 85, 85, 75, 75, 80,   80];   %value
                   
%picewise_brake    = [  0, stime;   %time
%                       0,    0];   %value
                   
[piecewise_throttle, piecewise_brake] = generate_inputs (stime, 12);


                   
%% Generating input signals
%

fprintf('Generating input signals\n');

time = 0:dt:stime;

size_t = size(time,2);
input_throttle = zeros(size_t,1);
input_brake    = zeros(size_t,1);

for s=1:size_t
    input_throttle(s) = piecewise(time(s), piecewise_throttle);
    input_brake(s)    = piecewise(time(s), piecewise_brake);
end

input = zeros(size_t,3);

input(:,1) = time';
input(:,2) = input_throttle';
input(:,3) = input_brake';



%% Simulating Simulink Model 

fprintf('Simulation of Simulink Model \n');

[time, output] = simSimulinkModel (model, input, solver, dt);

currDate = strrep(datestr(datetime), ' ', '_');
status = mkdir('test',currDate);

save (strcat('./test/',currDate,'/simulation.mat'), 'dt', 'stime', 'solver', 'num_exp', 'model', 'input', 'output');


fprintf('Plotting simulation \n');

%% Plotting the simulation
%plotting (input, output, input_labels, output_labels);
run('../../../init.m');
addpath(genpath('../../../externalsw/breach'));
addpath(genpath('../../../externalsw/s-taliro_public'));
InitBreach;


%% Monitoring parameters 

vehicle_speed_thresholds = [ 120, 160, 170, 200];
engine_speed_thresholds  = [4500,5000,5200,5500];
time_bounds              = [   4,   8,  10,  20];



%% Monitoring property with Moonlight

moonlight_satisfaction_spec1        = zeros(size(vehicle_speed_thresholds,2), size(engine_speed_thresholds,2), size(time_bounds,2));
moonlight_satisfaction_time_spec1   = zeros(size(vehicle_speed_thresholds,2), size(engine_speed_thresholds,2), size(time_bounds,2));

moonlight_satisfaction_spec2        = zeros(size(vehicle_speed_thresholds,2), size(engine_speed_thresholds,2), size(time_bounds,2));
moonlight_satisfaction_time_spec2   = zeros(size(vehicle_speed_thresholds,2), size(engine_speed_thresholds,2), size(time_bounds,2));

moonlight_satisfaction_spec3        = zeros(size(vehicle_speed_thresholds,2), size(engine_speed_thresholds,2), size(time_bounds,2));
moonlight_satisfaction_time_spec3   = zeros(size(vehicle_speed_thresholds,2), size(engine_speed_thresholds,2), size(time_bounds,2));

moonlight_satisfaction_spec4        = zeros(size(vehicle_speed_thresholds,2), size(engine_speed_thresholds,2), size(time_bounds,2));
moonlight_satisfaction_time_spec4   = zeros(size(vehicle_speed_thresholds,2), size(engine_speed_thresholds,2), size(time_bounds,2));



moonlight_robust_spec1        = zeros(size(vehicle_speed_thresholds,2), size(engine_speed_thresholds,2), size(time_bounds,2));
moonlight_robust_time_spec1   = zeros(size(vehicle_speed_thresholds,2), size(engine_speed_thresholds,2), size(time_bounds,2));

moonlight_robust_spec2        = zeros(size(vehicle_speed_thresholds,2), size(engine_speed_thresholds,2), size(time_bounds,2));
moonlight_robust_time_spec2   = zeros(size(vehicle_speed_thresholds,2), size(engine_speed_thresholds,2), size(time_bounds,2));

moonlight_robust_spec3        = zeros(size(vehicle_speed_thresholds,2), size(engine_speed_thresholds,2), size(time_bounds,2));
moonlight_robust_time_spec3   = zeros(size(vehicle_speed_thresholds,2), size(engine_speed_thresholds,2), size(time_bounds,2));

moonlight_robust_spec4        = zeros(size(vehicle_speed_thresholds,2), size(engine_speed_thresholds,2), size(time_bounds,2));
moonlight_robust_time_spec4   = zeros(size(vehicle_speed_thresholds,2), size(engine_speed_thresholds,2), size(time_bounds,2));

for est=1:size(engine_speed_thresholds,2)
    for vst=1:size(vehicle_speed_thresholds,2)
        for tbs=1:size(time_bounds,2)
            [boolean_results, robust_results] = monMoonlight (time, output, num_exp, engine_speed_thresholds(est), vehicle_speed_thresholds(vst), time_bounds(tbs));
            
            moonlight_satisfaction_spec1 (vst,est,tbs) = boolean_results(1,1);
            moonlight_satisfaction_spec2 (vst,est,tbs) = boolean_results(2,1);
            moonlight_satisfaction_spec3 (vst,est,tbs) = boolean_results(3,1);
            moonlight_satisfaction_spec4 (vst,est,tbs) = boolean_results(4,1);
            
            moonlight_satisfaction_time_spec1 (vst,est,tbs) = boolean_results(1,2);
            moonlight_satisfaction_time_spec2 (vst,est,tbs) = boolean_results(2,2);
            moonlight_satisfaction_time_spec3 (vst,est,tbs) = boolean_results(3,2);
            moonlight_satisfaction_time_spec4 (vst,est,tbs) = boolean_results(4,2);
            
            moonlight_robust_spec1 (vst,est,tbs) = robust_results(1,1);
            moonlight_robust_spec2 (vst,est,tbs) = robust_results(2,1);
            moonlight_robust_spec3 (vst,est,tbs) = robust_results(3,1);
            moonlight_robust_spec4 (vst,est,tbs) = robust_results(4,1);
            
            moonlight_robust_time_spec1 (vst,est,tbs) = robust_results(1,2);
            moonlight_robust_time_spec2 (vst,est,tbs) = robust_results(2,2);
            moonlight_robust_time_spec3 (vst,est,tbs) = robust_results(3,2);
            moonlight_robust_time_spec4 (vst,est,tbs) = robust_results(4,2);
        end
    end
end
moonlight_sat_max_time_spec1    = max(moonlight_satisfaction_time_spec1(:));
moonlight_sat_min_time_spec1    = min(moonlight_satisfaction_time_spec1(:));
moonlight_sat_median_time_spec1 = median(moonlight_satisfaction_time_spec1(:));
moonlight_sat_var_time_spec1    = var(moonlight_satisfaction_time_spec1(:));
moonlight_sat_mean_time_spec1   = mean(moonlight_satisfaction_time_spec1(:));

fprintf('Specification 1 (Satisfaction)  - Moonlight Times (sec): min=%f, max=%f, mean=%f, median=%f, var=%f\n', moonlight_sat_min_time_spec1, moonlight_sat_max_time_spec1, moonlight_sat_mean_time_spec1, moonlight_sat_median_time_spec1, moonlight_sat_var_time_spec1);

moonlight_sat_max_time_spec2    = max(moonlight_satisfaction_time_spec2(:));
moonlight_sat_min_time_spec2    = min(moonlight_satisfaction_time_spec2(:));
moonlight_sat_median_time_spec2 = median(moonlight_satisfaction_time_spec2(:));
moonlight_sat_var_time_spec2    = var(moonlight_satisfaction_time_spec2(:));
moonlight_sat_mean_time_spec2   = mean(moonlight_satisfaction_time_spec2(:));

fprintf('Specification 2 (Satisfaction)  - Moonlight Times (sec): min=%f, max=%f, mean=%f, median=%f, var=%f\n', moonlight_sat_min_time_spec2, moonlight_sat_max_time_spec2, moonlight_sat_mean_time_spec2, moonlight_sat_median_time_spec2, moonlight_sat_var_time_spec2);

moonlight_sat_max_time_spec3    = max(moonlight_satisfaction_time_spec3(:));
moonlight_sat_min_time_spec3    = min(moonlight_satisfaction_time_spec3(:));
moonlight_sat_median_time_spec3 = median(moonlight_satisfaction_time_spec3(:));
moonlight_sat_var_time_spec3    = var(moonlight_satisfaction_time_spec3(:));
moonlight_sat_mean_time_spec3   = mean(moonlight_satisfaction_time_spec3(:));

fprintf('Specification 3 (Satisfaction)  - Moonlight Times (sec): min=%f, max=%f, mean=%f, median=%f, var=%f\n', moonlight_sat_min_time_spec3, moonlight_sat_max_time_spec3, moonlight_sat_mean_time_spec3, moonlight_sat_median_time_spec3, moonlight_sat_var_time_spec3);


moonlight_sat_max_time_spec4    = max(moonlight_satisfaction_time_spec4(:));
moonlight_sat_min_time_spec4    = min(moonlight_satisfaction_time_spec4(:));
moonlight_sat_median_time_spec4 = median(moonlight_satisfaction_time_spec4(:));
moonlight_sat_var_time_spec4    = var(moonlight_satisfaction_time_spec4(:));
moonlight_sat_mean_time_spec4   = mean(moonlight_satisfaction_time_spec4(:));

fprintf('Specification 4 (Satisfaction) - Moonlight Times (sec): min=%f, max=%f, mean=%f, median=%f, var=%f\n', moonlight_sat_min_time_spec4, moonlight_sat_max_time_spec4, moonlight_sat_mean_time_spec4, moonlight_sat_median_time_spec4, moonlight_sat_var_time_spec4);

save (strcat('./test/',currDate,'/moonlight_sat_times_stat.mat'), 'moonlight_sat_max_time_spec1', 'moonlight_sat_min_time_spec1', 'moonlight_sat_median_time_spec1', 'moonlight_sat_var_time_spec1', 'moonlight_sat_mean_time_spec1', 'moonlight_sat_max_time_spec2', 'moonlight_sat_min_time_spec2', 'moonlight_sat_median_time_spec2', 'moonlight_sat_var_time_spec2', 'moonlight_sat_mean_time_spec2', 'moonlight_sat_max_time_spec3', 'moonlight_sat_min_time_spec3', 'moonlight_sat_median_time_spec3', 'moonlight_sat_var_time_spec3', 'moonlight_sat_mean_time_spec3', 'moonlight_sat_max_time_spec4', 'moonlight_sat_min_time_spec4', 'moonlight_sat_median_time_spec4', 'moonlight_sat_var_time_spec4', 'moonlight_sat_mean_time_spec4');


moonlight_rob_max_time_spec1    = max(moonlight_robust_time_spec1(:));
moonlight_rob_min_time_spec1    = min(moonlight_robust_time_spec1(:));
moonlight_rob_median_time_spec1 = median(moonlight_robust_time_spec1(:));
moonlight_rob_var_time_spec1    = var(moonlight_robust_time_spec1(:));
moonlight_rob_mean_time_spec1   = mean(moonlight_robust_time_spec1(:));

fprintf('Specification 1 (Robustness) - Moonlight Times (sec): min=%f, max=%f, mean=%f, median=%f, var=%f\n', moonlight_rob_min_time_spec1, moonlight_rob_max_time_spec1, moonlight_rob_mean_time_spec1, moonlight_rob_median_time_spec1, moonlight_rob_var_time_spec1);


moonlight_rob_max_time_spec2    = max(moonlight_robust_time_spec2(:));
moonlight_rob_min_time_spec2    = min(moonlight_robust_time_spec2(:));
moonlight_rob_median_time_spec2 = median(moonlight_robust_time_spec2(:));
moonlight_rob_var_time_spec2    = var(moonlight_robust_time_spec2(:));
moonlight_rob_mean_time_spec2   = mean(moonlight_robust_time_spec2(:));

fprintf('Specification 2 (Robustness)  - Moonlight Times (sec): min=%f, max=%f, mean=%f, median=%f, var=%f\n', moonlight_rob_min_time_spec2, moonlight_rob_max_time_spec2, moonlight_rob_mean_time_spec2, moonlight_rob_median_time_spec2, moonlight_rob_var_time_spec2);

moonlight_rob_max_time_spec3    = max(moonlight_robust_time_spec3(:));
moonlight_rob_min_time_spec3    = min(moonlight_robust_time_spec3(:));
moonlight_rob_median_time_spec3 = median(moonlight_robust_time_spec3(:));
moonlight_rob_var_time_spec3    = var(moonlight_robust_time_spec3(:));
moonlight_rob_mean_time_spec3   = mean(moonlight_robust_time_spec3(:));

fprintf('Specification 3 (Robustness)  - Moonlight Times (sec): min=%f, max=%f, mean=%f, median=%f, var=%f\n', moonlight_rob_min_time_spec3, moonlight_rob_max_time_spec3, moonlight_rob_mean_time_spec3, moonlight_rob_median_time_spec3, moonlight_rob_var_time_spec3);

moonlight_rob_max_time_spec4    = max(moonlight_robust_time_spec4(:));
moonlight_rob_min_time_spec4    = min(moonlight_robust_time_spec4(:));
moonlight_rob_median_time_spec4 = median(moonlight_robust_time_spec4(:));
moonlight_rob_var_time_spec4    = var(moonlight_robust_time_spec4(:));
moonlight_rob_mean_time_spec4   = mean(moonlight_robust_time_spec4(:));

fprintf('Specification 4 (Robustness)  - Moonlight Times (sec): min=%f, max=%f, mean=%f, median=%f, var=%f\n', moonlight_rob_min_time_spec4, moonlight_rob_max_time_spec4, moonlight_rob_mean_time_spec4, moonlight_rob_median_time_spec4, moonlight_rob_var_time_spec4);

save (strcat('./test/',currDate,'/moonlight_rob_times_stat.mat'), 'moonlight_rob_max_time_spec1', 'moonlight_rob_min_time_spec1', 'moonlight_rob_median_time_spec1', 'moonlight_rob_var_time_spec1', 'moonlight_rob_mean_time_spec1', 'moonlight_rob_max_time_spec2', 'moonlight_rob_min_time_spec2', 'moonlight_rob_median_time_spec2', 'moonlight_rob_var_time_spec2', 'moonlight_rob_mean_time_spec2', 'moonlight_rob_max_time_spec3', 'moonlight_rob_min_time_spec3', 'moonlight_rob_median_time_spec3', 'moonlight_rob_var_time_spec3', 'moonlight_rob_mean_time_spec3', 'moonlight_rob_max_time_spec4', 'moonlight_rob_min_time_spec4', 'moonlight_rob_median_time_spec4', 'moonlight_rob_var_time_spec4', 'moonlight_rob_mean_time_spec4');


save (strcat('./test/',currDate,'/monitoring_moonlight.mat'), 'moonlight_robust_spec1', 'moonlight_robust_spec2', 'moonlight_robust_spec3', 'moonlight_robust_spec4','moonlight_robust_time_spec1', 'moonlight_robust_time_spec2', 'moonlight_robust_time_spec3', 'moonlight_robust_time_spec4');


%% Monitoring property with Breach

breach_robust_spec1        = zeros(size(vehicle_speed_thresholds,2), size(engine_speed_thresholds,2), size(time_bounds,2));
breach_robust_time_spec1   = zeros(size(vehicle_speed_thresholds,2), size(engine_speed_thresholds,2), size(time_bounds,2));

breach_robust_spec2        = zeros(size(vehicle_speed_thresholds,2), size(engine_speed_thresholds,2), size(time_bounds,2));
breach_robust_time_spec2   = zeros(size(vehicle_speed_thresholds,2), size(engine_speed_thresholds,2), size(time_bounds,2));

breach_robust_spec3        = zeros(size(vehicle_speed_thresholds,2), size(engine_speed_thresholds,2), size(time_bounds,2));
breach_robust_time_spec3   = zeros(size(vehicle_speed_thresholds,2), size(engine_speed_thresholds,2), size(time_bounds,2));

breach_robust_spec4        = zeros(size(vehicle_speed_thresholds,2), size(engine_speed_thresholds,2), size(time_bounds,2));
breach_robust_time_spec4   = zeros(size(vehicle_speed_thresholds,2), size(engine_speed_thresholds,2), size(time_bounds,2));

for est=1:size(engine_speed_thresholds,2)
    for vst=1:size(vehicle_speed_thresholds,2)
        for tbs=1:size(time_bounds,2)            
            [robust_results]                  = monBreach   (time, output, num_exp, engine_speed_thresholds(est), vehicle_speed_thresholds(vst), time_bounds(tbs));
        
            breach_robust_spec1 (vst,est,tbs) = robust_results(1,1);
            breach_robust_spec2 (vst,est,tbs) = robust_results(2,1);
            breach_robust_spec3 (vst,est,tbs) = robust_results(3,1);
            breach_robust_spec4 (vst,est,tbs) = robust_results(4,1);
            
            breach_robust_time_spec1 (vst,est,tbs) = robust_results(1,2);
            breach_robust_time_spec2 (vst,est,tbs) = robust_results(2,2);
            breach_robust_time_spec3 (vst,est,tbs) = robust_results(3,2);
            breach_robust_time_spec4 (vst,est,tbs) = robust_results(4,2);
        end
    end
end


breach_rob_max_time_spec1    = max(breach_robust_time_spec1(:));
breach_rob_min_time_spec1    = min(breach_robust_time_spec1(:));
breach_rob_median_time_spec1 = median(breach_robust_time_spec1(:));
breach_rob_var_time_spec1    = var(breach_robust_time_spec1(:));
breach_rob_mean_time_spec1   = mean(breach_robust_time_spec1(:));

fprintf('Specification 1 (Robustness)  - Breach Times (sec): min=%f, max=%f, mean=%f, median=%f, var=%f\n', breach_rob_min_time_spec1, breach_rob_max_time_spec1, breach_rob_mean_time_spec1, breach_rob_median_time_spec1, breach_rob_var_time_spec1);


breach_rob_max_time_spec2    = max(breach_robust_time_spec2(:));
breach_rob_min_time_spec2    = min(breach_robust_time_spec2(:));
breach_rob_median_time_spec2 = median(breach_robust_time_spec2(:));
breach_rob_var_time_spec2    = var(breach_robust_time_spec2(:));
breach_rob_mean_time_spec2   = mean(breach_robust_time_spec2(:));

fprintf('Specification 2 (Robustness)  - Breach Times (sec): min=%f, max=%f, mean=%f, median=%f, var=%f\n', breach_rob_min_time_spec2, breach_rob_max_time_spec2, breach_rob_mean_time_spec2, breach_rob_median_time_spec2, breach_rob_var_time_spec2);

breach_rob_max_time_spec3    = max(breach_robust_time_spec3(:));
breach_rob_min_time_spec3    = min(breach_robust_time_spec3(:));
breach_rob_median_time_spec3 = median(breach_robust_time_spec3(:));
breach_rob_var_time_spec3    = var(breach_robust_time_spec3(:));
breach_rob_mean_time_spec3   = mean(breach_robust_time_spec3(:));

fprintf('Specification 3 (Robustness)  - Breach Times (sec): min=%f, max=%f, mean=%f, median=%f, var=%f\n', breach_rob_min_time_spec3, breach_rob_max_time_spec3, breach_rob_mean_time_spec3, breach_rob_median_time_spec3, breach_rob_var_time_spec3);

breach_rob_max_time_spec4    = max(breach_robust_time_spec4(:));
breach_rob_min_time_spec4    = min(breach_robust_time_spec4(:));
breach_rob_median_time_spec4 = median(breach_robust_time_spec4(:));
breach_rob_var_time_spec4    = var(breach_robust_time_spec4(:));
breach_rob_mean_time_spec4   = mean(breach_robust_time_spec4(:));

fprintf('Specification 4 (Robustness)  - Breach Times (sec): min=%f, max=%f, mean=%f, median=%f, var=%f\n', breach_rob_min_time_spec4, breach_rob_max_time_spec4, breach_rob_mean_time_spec4, breach_rob_median_time_spec4, breach_rob_var_time_spec4);


save (strcat('./test/',currDate,'/breach_rob_times_stat.mat'), 'breach_rob_max_time_spec1', 'breach_rob_min_time_spec1', 'breach_rob_median_time_spec1', 'breach_rob_var_time_spec1', 'breach_rob_mean_time_spec1', 'breach_rob_max_time_spec2', 'breach_rob_min_time_spec2', 'breach_rob_median_time_spec2', 'breach_rob_var_time_spec2', 'breach_rob_mean_time_spec2', 'breach_rob_max_time_spec3', 'breach_rob_min_time_spec3', 'breach_rob_median_time_spec3', 'breach_rob_var_time_spec3', 'breach_rob_mean_time_spec3', 'breach_rob_max_time_spec4', 'breach_rob_min_time_spec4', 'breach_rob_median_time_spec4', 'breach_rob_var_time_spec4', 'breach_rob_mean_time_spec4');



save (strcat('./test/',currDate,'/monitoring_breach.mat'), 'breach_robust_spec1', 'breach_robust_spec2', 'breach_robust_spec3', 'breach_robust_spec4','breach_robust_time_spec1', 'breach_robust_time_spec2', 'breach_robust_time_spec3', 'breach_robust_time_spec4');

%% Monitoring property with S-Taliro
staliro_robust_spec1        = zeros(size(vehicle_speed_thresholds,2), size(engine_speed_thresholds,2), size(time_bounds,2));
staliro_robust_time_spec1   = zeros(size(vehicle_speed_thresholds,2), size(engine_speed_thresholds,2), size(time_bounds,2));

staliro_robust_spec2        = zeros(size(vehicle_speed_thresholds,2), size(engine_speed_thresholds,2), size(time_bounds,2));
staliro_robust_time_spec2   = zeros(size(vehicle_speed_thresholds,2), size(engine_speed_thresholds,2), size(time_bounds,2));

staliro_robust_spec3        = zeros(size(vehicle_speed_thresholds,2), size(engine_speed_thresholds,2), size(time_bounds,2));
staliro_robust_time_spec3   = zeros(size(vehicle_speed_thresholds,2), size(engine_speed_thresholds,2), size(time_bounds,2));

staliro_robust_spec4        = zeros(size(vehicle_speed_thresholds,2), size(engine_speed_thresholds,2), size(time_bounds,2));
staliro_robust_time_spec4   = zeros(size(vehicle_speed_thresholds,2), size(engine_speed_thresholds,2), size(time_bounds,2));


for est=1:size(engine_speed_thresholds,2)
    for vst=1:size(vehicle_speed_thresholds,2)
        for tbs=1:size(time_bounds,2)  
           [robust_results]                  = monStaliro  (time, output, num_exp, engine_speed_thresholds(est), vehicle_speed_thresholds(vst), time_bounds(tbs));
           
           staliro_robust_spec1 (vst,est,tbs) = robust_results(1,1);
           staliro_robust_spec2 (vst,est,tbs) = robust_results(2,1);
           staliro_robust_spec3 (vst,est,tbs) = robust_results(3,1);
           staliro_robust_spec4 (vst,est,tbs) = robust_results(4,1);
            
           staliro_robust_time_spec1 (vst,est,tbs) = robust_results(1,2);
           staliro_robust_time_spec2 (vst,est,tbs) = robust_results(2,2);
           staliro_robust_time_spec3 (vst,est,tbs) = robust_results(3,2);
           staliro_robust_time_spec4 (vst,est,tbs) = robust_results(4,2);
        end
    end
end

staliro_rob_max_time_spec1    = max(staliro_robust_time_spec1(:));
staliro_rob_min_time_spec1    = min(staliro_robust_time_spec1(:));
staliro_rob_median_time_spec1 = median(staliro_robust_time_spec1(:));
staliro_rob_var_time_spec1    = var(staliro_robust_time_spec1(:));
staliro_rob_mean_time_spec1   = mean(staliro_robust_time_spec1(:));

fprintf('Specification 1 (Robustness)  - S-Taliro Times (sec): min=%f, max=%f, mean=%f, median=%f, var=%f\n', staliro_rob_min_time_spec1, staliro_rob_max_time_spec1, staliro_rob_mean_time_spec1, staliro_rob_median_time_spec1, staliro_rob_var_time_spec1);


staliro_rob_max_time_spec2    = max(staliro_robust_time_spec2(:));
staliro_rob_min_time_spec2    = min(staliro_robust_time_spec2(:));
staliro_rob_median_time_spec2 = median(staliro_robust_time_spec2(:));
staliro_rob_var_time_spec2    = var(staliro_robust_time_spec2(:));
staliro_rob_mean_time_spec2   = mean(staliro_robust_time_spec2(:));

fprintf('Specification 2 (Robustness)  - S-Taliro Times (sec): min=%f, max=%f, mean=%f, median=%f, var=%f\n',  staliro_rob_min_time_spec2,  staliro_rob_max_time_spec2, staliro_rob_mean_time_spec2,  staliro_rob_median_time_spec2,  staliro_rob_var_time_spec2);

staliro_rob_max_time_spec3    = max(staliro_robust_time_spec3(:));
staliro_rob_min_time_spec3    = min(staliro_robust_time_spec3(:));
staliro_rob_median_time_spec3 = median(staliro_robust_time_spec3(:));
staliro_rob_var_time_spec3    = var(staliro_robust_time_spec3(:));
staliro_rob_mean_time_spec3   = mean(staliro_robust_time_spec3(:));

fprintf('Specification 3 (Robustness)  - S-Taliro Times (sec): min=%f, max=%f, mean=%f, median=%f, var=%f\n', staliro_rob_min_time_spec3, staliro_rob_max_time_spec3, staliro_rob_mean_time_spec3, staliro_rob_median_time_spec3, staliro_rob_var_time_spec3);

staliro_rob_max_time_spec4    = max(staliro_robust_time_spec4(:));
staliro_rob_min_time_spec4    = min(staliro_robust_time_spec4(:));
staliro_rob_median_time_spec4 = median(staliro_robust_time_spec4(:));
staliro_rob_var_time_spec4    = var(staliro_robust_time_spec4(:));
staliro_rob_mean_time_spec4   = mean(staliro_robust_time_spec4(:));

fprintf('Specification 4 (Robustness)  -  S-Taliro Times (sec): min=%f, max=%f, mean=%f, median=%f, var=%f\n', staliro_rob_min_time_spec4, staliro_rob_max_time_spec4, staliro_rob_mean_time_spec4, staliro_rob_median_time_spec4, staliro_rob_var_time_spec4);


save (strcat('./test/',currDate,'/staliro_rob_times_stat.mat'), 'staliro_rob_max_time_spec1', 'staliro_rob_min_time_spec1', 'staliro_rob_median_time_spec1', 'staliro_rob_var_time_spec1', 'staliro_rob_mean_time_spec1', 'staliro_rob_max_time_spec2', 'staliro_rob_min_time_spec2', 'staliro_rob_median_time_spec2', 'staliro_rob_var_time_spec2', 'staliro_rob_mean_time_spec2', 'staliro_rob_max_time_spec3', 'staliro_rob_min_time_spec3', 'staliro_rob_median_time_spec3', 'staliro_rob_var_time_spec3', 'staliro_rob_mean_time_spec3', 'staliro_rob_max_time_spec4', 'staliro_rob_min_time_spec4', 'staliro_rob_median_time_spec4', 'staliro_rob_var_time_spec4', 'staliro_rob_mean_time_spec4');



save (strcat('./test/',currDate,'/monitoring_staliro.mat'), 'staliro_robust_spec1', 'staliro_robust_spec2', 'staliro_robust_spec3', 'staliro_robust_spec4','staliro_robust_time_spec1', 'staliro_robust_time_spec2', 'staliro_robust_time_spec3', 'staliro_robust_time_spec4');

%% Checking the results between Moonlight and Breach

fprintf('Checking consistency of the results between Moonlight and Breach\n');

count = 0;
for est=1:size(engine_speed_thresholds,2)
    for vst=1:size(vehicle_speed_thresholds,2)
        for tbs=1:size(time_bounds,2)
   
            if (moonlight_robust_spec1 (vst,est,tbs) == breach_robust_spec1 (vst,est,tbs))
                count = count + 1;
            elseif (abs(moonlight_robust_spec1 (vst,est,tbs) - breach_robust_spec1 (vst,est,tbs)) < 0.0001)
                fprintf("Comparison between Moonlight and Breach: Warning Negligeble Rounded Error=%f on Spec 1, omega=%f, speed_threshold=%f, T=%f\n", abs(moonlight_robust_spec1 (vst,est,tbs) - breach_robust_time_spec1 (vst,est,tbs)), engine_speed_thresholds(est), vehicle_speed_thresholds(vst), time_bounds(tbs));
            else
                fprintf("Comparison between Moonlight and Breach failed on Spec 1, omega=%f, speed_threshold=%f, T=%f\n, Moonlight=%f Breach=%f\n", engine_speed_thresholds(est), vehicle_speed_thresholds(vst), time_bounds(tbs), moonlight_robust_spec1 (vst,est,tbs), breach_robust_spec1 (vst,est,tbs));
            end
        end
    end
end

if (count == size(engine_speed_thresholds,2) * size(vehicle_speed_thresholds,2) * size(time_bounds,2))
     fprintf("Comparison of results between Moonlight and Breach for Specification 1 is successful!!\n");
end

count = 0;

for est=1:size(engine_speed_thresholds,2)
    for vst=1:size(vehicle_speed_thresholds,2)
        for tbs=1:size(time_bounds,2)
   
            if (moonlight_robust_spec2 (vst,est,tbs) == breach_robust_spec2 (vst,est,tbs))
                count = count + 1;
            elseif (abs(moonlight_robust_spec2 (vst,est,tbs) - breach_robust_spec2 (vst,est,tbs)) < 0.0001)
                fprintf("Comparison between Moonlight and Breach: Warning Negligeble Rounded Error=%f on Spec 2, omega=%f, speed_threshold=%f, T=%f\n", abs(moonlight_robust_spec2 (vst,est,tbs) - breach_robust_spec2 (vst,est,tbs)), engine_speed_thresholds(est), vehicle_speed_thresholds(vst), time_bounds(tbs));
            else
                fprintf("Comparison between Moonlight and Breach failed on Spec 2, omega=%f, speed_threshold=%f, T=%f\n, Moonlight=%f Breach=%f\n", engine_speed_thresholds(est), vehicle_speed_thresholds(vst), time_bounds(tbs), moonlight_robust_spec2 (vst,est,tbs), breach_robust_spec2 (vst,est,tbs));
            end
        end
    end
end

if (count == size(engine_speed_thresholds,2) * size(vehicle_speed_thresholds,2) * size(time_bounds,2))
     fprintf("Comparison of results between Moonlight and Breach for Specification 2 is successful!!\n");
end

count = 0;

for est=1:size(engine_speed_thresholds,2)
    for vst=1:size(vehicle_speed_thresholds,2)
        for tbs=1:size(time_bounds,2)
   
            if (moonlight_robust_spec3 (vst,est,tbs) == breach_robust_spec3 (vst,est,tbs))
                count = count + 1;
            elseif (abs(moonlight_robust_spec3 (vst,est,tbs) - breach_robust_spec3 (vst,est,tbs)) < 0.0001)
                fprintf("Comparison between Moonlight and Breach: Warning Negligeble Rounded Error=%f on Spec 3, omega=%f, speed_threshold=%f, T=%f\n", abs(moonlight_robust_spec3 (vst,est,tbs) - breach_robust_spec3 (vst,est,tbs)), engine_speed_thresholds(est), vehicle_speed_thresholds(vst), time_bounds(tbs));
            else
                fprintf("Comparison between Moonlight and Breach failed on Spec 3, omega=%f, speed_threshold=%f, T=%f\n, Moonlight=%f Breach=%f\n", engine_speed_thresholds(est), vehicle_speed_thresholds(vst), time_bounds(tbs), moonlight_robust_spec3 (vst,est,tbs), breach_robust_spec3 (vst,est,tbs));
            end
        end
    end
end

if (count == size(engine_speed_thresholds,2) * size(vehicle_speed_thresholds,2) * size(time_bounds,2))
     fprintf("Comparison of results between Moonlight and Breach for Specification 3 is successful!!\n");
end


count = 0;

for est=1:size(engine_speed_thresholds,2)
    for vst=1:size(vehicle_speed_thresholds,2)
        for tbs=1:size(time_bounds,2)
   
            if (moonlight_robust_spec4 (vst,est,tbs) == breach_robust_spec4 (vst,est,tbs))
                count = count + 1;
            elseif (abs(moonlight_robust_spec4 (vst,est,tbs) - breach_robust_spec4 (vst,est,tbs)) < 0.0001)
                fprintf("Comparison between Moonlight and Breach: Warning Negligeble Rounded Error=%f on Spec 4, omega=%f, speed_threshold=%f, T=%f\n", abs(moonlight_robust_spec4 (vst,est,tbs) - breach_robust_spec4 (vst,est,tbs)), engine_speed_thresholds(est), vehicle_speed_thresholds(vst), time_bounds(tbs));
            else
                fprintf("Comparison between Moonlight and Breach failed on Spec 4, omega=%f, speed_threshold=%f, T=%f\n, Moonlight=%f Breach=%f\n", engine_speed_thresholds(est), vehicle_speed_thresholds(vst), time_bounds(tbs), moonlight_robust_spec4 (vst,est,tbs), breach_robust_spec4 (vst,est,tbs));
            end
        end
    end
end

if (count == size(engine_speed_thresholds,2) * size(vehicle_speed_thresholds,2) * size(time_bounds,2))
     fprintf("Comparison of results between Moonlight and Breach for Specification 4 is successful!!\n");
end

% count = 0;
% for i=1:size (robust_results2)
%     
%     if (robust_results1(i) == robust_results2(i))
%         count = count + 1;
%     elseif (abs(robust_results1(i) - robust_results2(i)) < 0.0001)
%         fprintf("Comparison between Moonlight and Breach: Warning Negligeble Rounded Error=%f on Specification num (%d)\n", abs(robust_results1(i) - robust_results2(i)), i);
%     else 
%        fprintf("Comparison between Moonlight and Breach failed on Specification num (%d) Moonlight=%d Breach=%d \n", i, robust_results1(i), robust_results2(i));
%     end
% end
% 
% if (count == 4)
%     fprintf("Comparison of results between Moonlight and Breach is successful !!\n");
% end
% 
% count = 0;
% for i=1:size (robust_results3)
%     if (robust_results1(i) == robust_results3(i))
%        count = count + 1;
%     elseif (abs(robust_results1(i) - robust_results3(i)) < 0.0001)
%        fprintf("Comparison between Moonlight and S-Taliro: Warning Negligeble Rounded Error=%f on Specification num (%d)\n", abs(robust_results1(i) - robust_results3(i)),i ); 
%        count = count + 1;
%     else 
%        fprintf("Comparison between Moonlight and S-Taliro failed on Specification num (%d) Moonlight=%d S-Taliro=%d \n", i, robust_results1(i), robust_results3(i));
%     end
% end
% 
% if (count == 4)
%     fprintf("Comparison of results between Moonlight and S-Taliro is successful!!\n");
% end
  
