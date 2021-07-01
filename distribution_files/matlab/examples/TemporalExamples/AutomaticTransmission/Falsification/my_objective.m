function rob = my_objective(x, model, solver,  dt)

addpath('Model');

% Setup model environment
model_path = 'Model/';
model_name_wo_ext = 'MLOpt_autotrans_mod04';
model_ext = '.slx';

% Set up the input parameters:
input_name = strcat(model_name_wo_ext,'/Throttle');

%% Generating input signals
stime = 30;
% dt = 0.04;
time = 0:dt:stime;

num_exp = 1;
step_time = x(1);
%step_time = floor(x(1));

initial_value = (x(2));
final_value = (x(3));
% signal_before_transition = initial_value * ones(1,step_time/0.04);                 % value before transition
% signal_after_transition = final_value * ones(1,total_time - step_time/0.04);       % value after transition
% 
% % Obtain the input signal (throttle)
% input_throttle = [signal_before_transition, signal_after_transition];    
% input_brake    = zeros(size_t,1);
% 
% input = zeros(size_t,3);
% 
% input(:,1) = time';
% input(:,2) = input_throttle';
% input(:,3) = input_brake';

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

moonlightScript = ScriptLoader.loadFromFile("multiple_spec");
prop_name = "Spec2";

moonlightScript.setMinMaxDomain();
quantitativeMonitor = moonlightScript.getMonitor(prop_name);
qMonitorResult1  = quantitativeMonitor.monitor(time, output, [engine_speed_thresholds, vehicle_speed_thresholds]); 

        
rob = qMonitorResult1(1,2);
 
end