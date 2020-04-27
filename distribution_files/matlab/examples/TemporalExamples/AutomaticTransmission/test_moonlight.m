%% Initializing the script

clear;       %clear all the memory
close all;   %close all the open windows

%% Configuration file for simulating the Simulink model "autotrans_mod04"
% Variables:
%    - stime - simulation_time (unit sec)
%    - dt - integration step (unit sec)
%    - piecewise_throttle - define the input signal of the throttle
%    - piecewise_break - define the input signal of the break

dt            =  0.02;
stime         =  64;
solver        = 'ode5';

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
                   
[picewise_throttle, picewise_brake] = generate_inputs (stime, 12)

                   
%% Generating input signals
%

fprintf('Generating input signals\n');

time = 0:dt:stime;

size_t = size(time,2);
input_throttle = zeros(size_t,1);
input_brake    = zeros(size_t,1);

for s=1:size_t
    input_throttle(s) = piecewise(time(s), picewise_throttle);
    input_brake(s)    = piecewise(time(s), picewise_brake);
end

input = zeros(size_t,3);

input(:,1) = time';
input(:,2) = input_throttle';
input(:,3) = input_brake';

%% Simulating Simulink Model 

fprintf('Simulation of Simulink Model \n');

[time, output] = simSimulinkModel (model, input, solver, dt);

fprintf('Plotting simulation \n');

%% Plotting the simulation
plotting (input, output, input_labels, output_labels);


%% Monitoring property with Moonlight

[boolean_results, robust_results1] = monMoonlight (time, output, 200, 4000, 120, 10);

%% Monitoring property with Breach

[robust_results2]                  = monBreach   (time, output, 200, 4000, 120, 10);

%% Monitoring property with S-Taliro

[robust_results3]                  = monStaliro  (time, output, 200, 4000, 120, 10);


%% Checking the results between Moonlight and Breach

count = 0;
for i=1:size (robust_results2)
    
    if (robust_results1(i) == robust_results2(i))
        count = count + 1;
    elseif (abs(robust_results1(i) - robust_results2(i)) < 0.0001)
        fprintf("Comparison between Moonlight and Breach: Warning Negligeble Rounded Error=%f on Specification num (%d)\n", abs(robust_results1(i) - robust_results2(i)), i);
    else 
       fprintf("Comparison between Moonlight and Breach failed on Specification num (%d) Moonlight=%d Breach=%d \n", i, robust_results1(i), robust_results2(i));
    end
end

if (count == 4)
    fprintf("Comparison of results between Moonlight and Breach is successful !!\n");
end

count = 0;
for i=1:size (robust_results3)
    if (robust_results1(i) == robust_results3(i))
       count = count + 1;
    elseif (abs(robust_results1(i) - robust_results3(i)) < 0.0001)
       fprintf("Comparison between Moonlight and S-Taliro: Warning Negligeble Rounded Error=%f on Specification num (%d)\n", abs(robust_results1(i) - robust_results3(i)),i ); 
       count = count + 1;
    else 
       fprintf("Comparison between Moonlight and S-Taliro failed on Specification num (%d) Moonlight=%d S-Taliro=%d \n", i, robust_results1(i), robust_results3(i));
    end
end

if (count == 4)
    fprintf("Comparison of results between Moonlight and S-Taliro is successful!!\n");
end


