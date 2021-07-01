close all;

addpath('Model');
% Setup model environment
model_path = 'Model/';
model_name_wo_ext = 'MLOpt_autotrans_mod04';
model_ext = '.slx';

% Set up the input parameters:
input_name = strcat(model_name_wo_ext,'/Throttle');
% Load the model
system = load_system([model_path, model_name_wo_ext, model_ext]);
%% Generating input signals
stime = 30;
dt = 0.04;
time = 0:dt:stime;

num_exp = 1;
load('falsInput')
step_time = x(1);
%step_time = floor(x(1));

initial_value = (x(2));
final_value = (x(3));
get_param(input_name,'dialogparameters');
set_param(input_name, 'Time', num2str(step_time), 'Before', num2str(initial_value), 'After', num2str(final_value));

simOut = sim(model_name_wo_ext, 'SaveTime', 'on', 'TimeSaveName', 'tout',...
    'SaveState', 'on', 'StateSaveName', 'xoutNew', 'SaveOutput', 'on', ...
    'OutputSaveName', 'youtNew', 'SignalLogging', 'on', 'SignalLoggingName', 'logsout');

time = simOut.tout;
output1 = simOut.youtNew{1}.Values.Data;
output2 = simOut.youtNew{2}.Values.Data;
output3 = simOut.youtNew{3}.Values.Data;

output = [output1 output2 output3];


%% Monitoring parameters 

vehicle_speed_thresholds = 120;
engine_speed_thresholds  = 4500;
time_bounds              = 4;


%% Monitoring property with Moonlight

%Generate a monitor object from the script fine multiple_spec.mls 
%this object is an implementation of ScriptLoader class, please 
%refer to the doc of this class for more details (ex. write in console 
%"doc ScriptLoader" ) please, open multiple_spec.mls 
moonlightScript = ScriptLoader.loadFromFile("multiple_spec");
prop_name = "Spec2";

moonlightScript.setBooleanDomain();
% formula Spec2(real omega, real v) = globally {( e_speed < omega ) & ( v_speed < v )} 
booleanMonitor1 = moonlightScript.getMonitor(prop_name);
bMonitorResult1  = booleanMonitor1.monitor(time, output, [engine_speed_thresholds, vehicle_speed_thresholds]); 

moonlightScript.setMinMaxDomain();
quantitativeMonitor1 = moonlightScript.getMonitor(prop_name);
qMonitorResult1  = quantitativeMonitor1.monitor(time, output, [engine_speed_thresholds, vehicle_speed_thresholds]); 

%Plotting result...
figure,
fontsize = 16;
tiledlayout(3,1)
nexttile
plot(time, output(:,1), 'LineWidth',3)
title('e\_speed','FontSize',fontsize )
nexttile
plot(time, output(:,2), 'LineWidth',3)
title('v\_speed','FontSize',fontsize )
nexttile
stairs(qMonitorResult1(:,1),qMonitorResult1(:,2), 'LineWidth',3)
hold on
%We add a last point to the boolean monitor to plot it easily!
boolean = [bMonitorResult1;time(end), bMonitorResult1(2,end)];
stairs(boolean(:,1),boolean(:,2)*max(abs(qMonitorResult1(:,2))), 'LineWidth',3)
title('globally {( e\_speed < 4500 ) & ( v\_speed < 160 )} ','FontSize',fontsize )
%title('eventually [0 T] ( v\_speed >= v ) &  globally ( e\_speed < omega ) ','FontSize',fontsize )
legend('Quantiative Monitor','Boolean Monitor','FontSize',14 )
