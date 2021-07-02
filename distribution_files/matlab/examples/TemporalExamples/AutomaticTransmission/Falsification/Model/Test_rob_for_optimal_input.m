% clear;       % clear all the memory
% close all;   % close all the open windows

disp('Setting up the simulation environment');

addpath('Model');

%% Setup model environment
model_path = 'Model/';
model_name_wo_ext = 'Copy_of_MLOpt_autotrans_mod04';
model_ext = '.slx';

% Set up the input parameters:
input_name = strcat(model_name_wo_ext,'/Throttle');

% Load the model
system = load_system([model_path, model_name_wo_ext, model_ext]);

%% Set the simulation environment
set_param(system, 'Solver', 'ode5', 'StopTime', '30', 'ReturnWorkspaceOutputs', 'on');

%% Configuration file for simulating the Simulink model "autotrans_mod04"
% Variables:
%    - stime - simulation_time (unit sec)
%    - dt - integration step (unit sec)
%    - piecewise_throttle - define the input signal of the throttle
%    - piecewise_break - define the input signal of the break

dt            =  0.04;
stime         =  30;
solver        = 'ode5';
num_exp       =  1;

model         = 'Copy_of_MLOpt_autotrans_mod04';
input_labels  = {'time', 'throttle', 'brake'};
output_labels = {'engine speed in rpm', 'vehicle speed in mph', 'gear'};


%% Compute robustness

%% For PSO
x = [2.4769 36.6611 88.7864];
rob = my_objective(x, model, solver, dt);
rob

%% For HS
x = [1.5310 35.3534 86.7861];
rob = my_objective(x, model, solver, dt);
rob

%% For WCA
x = [2.4791 20.3948 76.0048];
rob = my_objective(x, model, solver, dt);
rob

%% Save and Close
save_system(system);
close_system(system);

