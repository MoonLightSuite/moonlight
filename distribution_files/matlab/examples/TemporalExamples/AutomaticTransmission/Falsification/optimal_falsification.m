%%%% to run this code you need the  Optimization Toolbox for %% fmincon and  Global Optimization Toolbox for %% PSO
tic;
clear;       % clear all the memory
close all;   % close all the open windows

disp('Setting up the simulation environment');

addpath('Model');

%% Setup model environment
model_path = 'Model/';
model_name_wo_ext = 'MLOpt_autotrans_mod04';
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

%% Monitoring parameters 
vehicle_speed_thresholds = 120;
engine_speed_thresholds  = 4500;
time_bounds              = 4;


model         = 'MLOpt_autotrans_mod04';
input_labels  = {'time', 'throttle', 'brake'};
output_labels = {'engine speed in rpm', 'vehicle speed in mph', 'gear'};

% fprintf('Settings\n\n');
% fprintf('\t dt     = %f \n',  dt    );
% fprintf('\t stime  = %f \n',  stime );
% fprintf('\t solver = %s \n\n',solver);
                
%% step signal

step_time0 = 0.8 + rand(1)*(5-0.8); %x=xmin+rand(1,n)*(xmax-xmin)
initial_value0 = 10 + rand(1)*(50-10);
final_value0 = 20 + rand(1)*(90-20);
lb = [0.8 10 20];  %lower bounds time and throttle values  
ub = [5 90 95];  %'upper bounds time and throttle values 

%% Optimize the function for robustness

%% fmincon
x0 = [step_time0 initial_value0 final_value0];
options = optimoptions('fmincon','Display','iter','PlotFcns',@optimplotfval);%'OutputFcn',@outfun);
fun = @(x)my_objective(x, model, solver,  dt);
nonlconst = @(x)my_constrfunc(x, model, solver, dt);
[x, fval] = fmincon(fun,x0,[], [], [], [], lb, ub, nonlconst, options);
 
% %% PSO
% options = optimoptions('particleswarm','Display','iter','PlotFcns','pswplotbestf','OutputFcn',@outfunps);
% fun = @(x)my_objective(x, model, solver,dt);
% nvars = 3; % number of decision variables
% %options.HybridFcn = @fmincon;
% [x,fval,exitflag,output] = particleswarm(fun,nvars,lb, ub, options);
% save('OptInput','x')
% step_time = x(1);
% initial_value = (x(2));
% final_value = (x(3));


%% Save and Close
save_system(system);
close_system(system);
toc;




