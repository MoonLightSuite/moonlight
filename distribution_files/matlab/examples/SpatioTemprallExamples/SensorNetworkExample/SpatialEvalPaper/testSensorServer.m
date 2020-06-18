%% Initializing the script
clear;       %clear all the memory
close all;   %close all the open windows

elapseTimeSeq = [];
num_exp = 50;
num_nodes_seq = [10, 100, 1000];


% % increasing the number of nodes
moonlight_sat_time_spec1 = [];
moonlight_sat_time_spec2 = [];
moonlight_sat_time_spec3 = [];
moonlight_sat_time_spec4 = [];
moonlight_sat_time_spec5 = [];
moonlight_rob_time_spec1 = [];
moonlight_rob_time_spec2 = [];
moonlight_rob_time_spec3 = [];
moonlight_rob_time_spec4 = [];
moonlight_rob_time_spec5 = [];
numSteps =1;
currDate = strrep(datestr(datetime), ' ', '_');
status = mkdir('testServer',currDate);
for num_nodes = num_nodes_seq
          
    %% Generating input signals
    fprintf('Generating input signals with %f \n', num_nodes);
    [spatialModel,time,signalInput]= sensorModel(num_nodes,numSteps, false);

  
    [b_time_results, rob_time_results] = monSpTempMon (spatialModel,time,signalInput, num_exp);
    moonlight_sat_time_spec1 = [moonlight_sat_time_spec1, b_time_results(1)];
    moonlight_sat_time_spec2 = [moonlight_sat_time_spec2, b_time_results(2)];
    moonlight_sat_time_spec3 = [moonlight_sat_time_spec3, b_time_results(3)];
    moonlight_sat_time_spec4 = [moonlight_sat_time_spec4, b_time_results(4)];
    moonlight_sat_time_spec5 = [moonlight_sat_time_spec5, b_time_results(5)];
    moonlight_rob_time_spec1 = [moonlight_rob_time_spec1, rob_time_results(1)];
    moonlight_rob_time_spec2 = [moonlight_rob_time_spec2, rob_time_results(2)];
    moonlight_rob_time_spec3 = [moonlight_rob_time_spec3, rob_time_results(3)];
    moonlight_rob_time_spec4 = [moonlight_rob_time_spec4, rob_time_results(4)];
    moonlight_rob_time_spec5 = [moonlight_rob_time_spec5, rob_time_results(5)];
    
    fprintf('Specification (Satisfaction)  - Moonlight Times (sec): p1=%f, p2=%f, p3=%f, p4=%f, p5=%f  \n', b_time_results(1), b_time_results(2), b_time_results(3), b_time_results(4), b_time_results(5));
    fprintf('Specification (Robustness)  - Moonlight Times (sec): p1=%f, p2=%f, p3=%f, p4=%f, p5=%f  \n', rob_time_results(1), rob_time_results(2), rob_time_results(3), rob_time_results(4), rob_time_results(5));

end

% moonlight_rob_max_time_spec1    = max(moonlight_robust_time_spec1(:));
% moonlight_rob_min_time_spec1    = min(moonlight_robust_time_spec1(:));
% moonlight_rob_median_time_spec1 = median(moonlight_robust_time_spec1(:));
% moonlight_rob_var_time_spec1    = var(moonlight_robust_time_spec1(:));
% moonlight_rob_mean_time_spec1   = mean(moonlight_robust_time_spec1(:))

%fprintf('Specification 1 (Satisfaction)  - Moonlight Times (sec): min=%f, max=%f, mean=%f, median=%f, var=%f\n', moonlight_sat_min_time_spec1, moonlight_sat_max_time_spec1, moonlight_sat_mean_time_spec1, moonlight_sat_median_time_spec1, moonlight_sat_var_time_spec1);
%fprintf('Specification 3 (Robustness)  - Moonlight Times (sec): min=%f, max=%f, mean=%f, median=%f, var=%f\n', moonlight_rob_min_time_spec3, moonlight_rob_max_time_spec3, moonlight_rob_mean_time_spec3, moonlight_rob_median_time_spec3, moonlight_rob_var_time_spec3);

% save (strcat('./test/',currDate,'/moonlight_sat_times_stat.mat'), 'moonlight_sat_max_time_spec1', 'moonlight_sat_min_time_spec1', 'moonlight_sat_median_time_spec1', 'moonlight_sat_var_time_spec1', 'moonlight_sat_mean_time_spec1', 'moonlight_sat_max_time_spec2', 'moonlight_sat_min_time_spec2', 'moonlight_sat_median_time_spec2', 'moonlight_sat_var_time_spec2', 'moonlight_sat_mean_time_spec2', 'moonlight_sat_max_time_spec3', 'moonlight_sat_min_time_spec3', 'moonlight_sat_median_time_spec3', 'moonlight_sat_var_time_spec3', 'moonlight_sat_mean_time_spec3', 'moonlight_sat_max_time_spec4', 'moonlight_sat_min_time_spec4', 'moonlight_sat_median_time_spec4', 'moonlight_sat_var_time_spec4', 'moonlight_sat_mean_time_spec4');
% save (strcat('./test/',currDate,'/moonlight_rob_times_stat.mat'), 'moonlight_rob_max_time_spec1', 'moonlight_rob_min_time_spec1', 'moonlight_rob_median_time_spec1', 'moonlight_rob_var_time_spec1', 'moonlight_rob_mean_time_spec1', 'moonlight_rob_max_time_spec2', 'moonlight_rob_min_time_spec2', 'moonlight_rob_median_time_spec2', 'moonlight_rob_var_time_spec2', 'moonlight_rob_mean_time_spec2', 'moonlight_rob_max_time_spec3', 'moonlight_rob_min_time_spec3', 'moonlight_rob_median_time_spec3', 'moonlight_rob_var_time_spec3', 'moonlight_rob_mean_time_spec3', 'moonlight_rob_max_time_spec4', 'moonlight_rob_min_time_spec4', 'moonlight_rob_median_time_spec4', 'moonlight_rob_var_time_spec4', 'moonlight_rob_mean_time_spec4');
% save (strcat('./test/',currDate,'/monitoring_moonlight.mat'), 'moonlight_robust_spec1', 'moonlight_robust_spec2', 'moonlight_robust_spec3', 'moonlight_robust_spec4','moonlight_robust_time_spec1', 'moonlight_robust_time_spec2', 'moonlight_robust_time_spec3', 'moonlight_robust_time_spec4');
 %save (strcat('./test/',currDate,'/simulation.mat'), 'time', 'spatialModel,', 'num_exp', 'signalInput');


save (strcat('./testServer/',currDate,'/dataInput.mat'), 'time', 'spatialModel','signalInput','num_nodes_seq', 'num_exp', 'numSteps');
save (strcat('./testServer/',currDate,'/moonlight_sat_times_stat.mat'), 'moonlight_sat_time_spec1', 'moonlight_sat_time_spec2', 'moonlight_sat_time_spec3', 'moonlight_sat_time_spec4', 'moonlight_sat_time_spec5');
save (strcat('./testServer/',currDate,'/moonlight_rob_times_stat.mat'), 'moonlight_rob_time_spec1', 'moonlight_rob_time_spec2', 'moonlight_rob_time_spec3', 'moonlight_rob_time_spec4', 'moonlight_sat_time_spec5');
%save (strcat('./test/',currDate,'/monitoring_moonlight.mat'), 'moonlight_robust_spec1', 'moonlight_robust_spec2', 'moonlight_robust_spec3', 'moonlight_robust_spec4','moonlight_robust_time_spec1', 'moonlight_robust_time_spec2', 'moonlight_robust_time_spec3', 'moonlight_robust_time_spec4');




    
