%% Initializing the script
clear;       %clear all the memory
close all;   %close all the open windows

elapseTimeSeq = [];
num_exp = 1;
num_nodes_seq = 10: 50: 1000;


% % increasing the number of nodes
moonlight_sat_time_spec1 = [];
moonlight_sat_time_spec2 = [];
moonlight_sat_time_spec3 = [];
moonlight_sat_time_spec4 = [];
moonlight_rob_time_spec1 = [];
moonlight_rob_time_spec2 = [];
moonlight_rob_time_spec3 = [];
moonlight_rob_time_spec4 = [];
numSteps =1;
currDate = strrep(datestr(datetime), ' ', '_');
status = mkdir('test',currDate);
for num_nodes = num_nodes_seq
          
    %% Generating input signals
    fprintf('Generating input signals\n');
    [spatialModel,time,signalInput]= sensorModel(num_nodes,numSteps, false);

  
    [b_time_results, rob_time_results] = monSpTempMon (spatialModel,time,signalInput, num_exp);
    moonlight_sat_time_spec1 = [moonlight_sat_time_spec1, b_time_results(1)];
    moonlight_sat_time_spec2 = [moonlight_sat_time_spec2, b_time_results(2)];
    moonlight_sat_time_spec3 = [moonlight_sat_time_spec3, b_time_results(3)];
    moonlight_sat_time_spec4 = [moonlight_sat_time_spec4, b_time_results(4)];
    moonlight_rob_time_spec1 = [moonlight_rob_time_spec1, rob_time_results(1)];
    moonlight_rob_time_spec2 = [moonlight_rob_time_spec2, rob_time_results(2)];
    moonlight_rob_time_spec3 = [moonlight_rob_time_spec3, rob_time_results(3)];
    moonlight_rob_time_spec4 = [moonlight_rob_time_spec4, rob_time_results(4)];
    
    fprintf('Specification (Satisfaction)  - Moonlight Times (sec): p1=%f, p2=%f, p3=%f, p4=%f', b_time_results(1), b_time_results(2), b_time_results(3), b_time_results(4));
    fprintf('Specification (Robustness)  - Moonlight Times (sec): p1=%f, p2=%f, p3=%f, p4=%f', rob_time_results(1), rob_time_results(2), rob_time_results(3), rob_time_results(4));

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

save (strcat('./test/',currDate,'/dataInput.mat'), 'time', 'spatialModel','signalInput','num_nodes_seq', 'num_exp', 'numSteps');
save (strcat('./test/',currDate,'/moonlight_sat_times_stat.mat'), 'moonlight_sat_time_spec1', 'moonlight_sat_time_spec2', 'moonlight_sat_time_spec3', 'moonlight_sat_time_spec4');
save (strcat('./test/',currDate,'/moonlight_rob_times_stat.mat'), 'moonlight_rob_time_spec1', 'moonlight_rob_time_spec2', 'moonlight_rob_time_spec3', 'moonlight_rob_time_spec4');
%save (strcat('./test/',currDate,'/monitoring_moonlight.mat'), 'moonlight_robust_spec1', 'moonlight_robust_spec2', 'moonlight_robust_spec3', 'moonlight_robust_spec4','moonlight_robust_time_spec1', 'moonlight_robust_time_spec2', 'moonlight_robust_time_spec3', 'moonlight_robust_time_spec4');


% figure; % open a new figure window
% plot(num_nodes_seq,moonlight_sat_time_spec1,'b','LineWidth',2);
% hold on;
% plot(num_nodes_seq,moonlight_rob_time_spec1,'b--','LineWidth',2);
% plot(num_nodes_seq,moonlight_sat_time_spec2,'r','LineWidth',2);
% plot(num_nodes_seq,moonlight_rob_time_spec2,'r--','LineWidth',2);
% xlabel('number of nodes');
% ylabel('time');
% legend('P1','P1q','P2','P2q')
% set(gca,'FontSize',18); 
% 
% 
% figure; % open a new figure window
% plot(num_nodes_seq,moonlight_sat_time_spec3,'g','LineWidth',2);
% hold on;
% plot(num_nodes_seq,moonlight_rob_time_spec3,'g--','LineWidth',2);
% plot(num_nodes_seq,moonlight_sat_time_spec4,'y','LineWidth',2);
% plot(num_nodes_seq,moonlight_rob_time_spec4,'y--','LineWidth',2);
% xlabel('number of nodes');
% ylabel('time');
% legend('P3','P3q','P4','P4q')
% set(gca,'FontSize',18); 


    
