%% Initializing the script
%clear;       %clear all the memory
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
num_exp       = 100;

model_name   = 'autotrans_mod04';
input_labels  = {'time', 'throttle', 'brake'};
output_labels = {'engine speed in rpm', 'vehicle speed in mph', 'gear'};
                   
                   
%% specifing property
moonlightScript = ScriptLoader.loadFromFile("multiple_spec.mls");
moonlightScript.setMinMaxDomain();
% formula Spec2(real omega, real v) = globally {( e_speed < omega ) & ( v_speed < v )} 
qMonitor = moonlightScript.getMonitor("Spec2");
omega = 140;
v  = 4700;
time_bound= 4;
    

time = 0:dt:stime;
size_t = size(time,2);
input_throttle = zeros(size_t,1);
input_brake    = zeros(size_t,1);

robMin = Inf;
for i=1:num_exp
    %% Generating input signals
    %

    fprintf('Generating random input signals\n');
    [piecewise_throttle, piecewise_brake] = generate_inputs (stime, 12);
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
    simopt = simget(model_name);
    %Setting the Simulink simulation option.
    simopt = simset(simopt,'solver', solver, 'FixedStep', dt, 'SaveFormat','Array');
    [ time, xt, output] = sim(model_name,[input(1,1,1) input(end,1,1)], simopt, input);

    %% Monitoring property with Moonlight
    fprintf('Evaluating the property \n');
    qMonitorResult  = qMonitor.monitor(time, output, [v, omega]);
    fprintf('iteration = %f \n', num_exp);
    if qMonitorResult(1,2) < robMin
       robMin = min (robMin, qMonitorResult(1,2));
       inputMin = input;
       fprintf('minRob = %f \n', robMin);
       if robMin < 0
           fprintf('The property is falsified \n');
           save('opt_input.mat', 'input')
           break;
       end
    else 
        fprintf('minRob = %f \n', robMin);
    end
end

% you can play varying the parameter of the formula