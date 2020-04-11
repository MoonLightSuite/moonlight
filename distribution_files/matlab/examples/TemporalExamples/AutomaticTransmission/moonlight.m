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
stime         =  80;
solver        = 'ode5';

model         = 'autotrans_mod04'
input_labels  = {'time', 'throttle', 'brake'};
output_labels = {'engine speed in rpm', 'vehicle speed in mph', 'gear'};

fprintf('Settings\n\n');
fprintf('\t dt     = %f \n',dt    );
fprintf('\t stime  = %f \n',stime );
fprintf('\t solver = %s \n\n',solver);


picewise_throttle = [  0,  5,   5, 10, 10, 15, 15, 20, 20, 25, 25, stime;   %time
                      52, 52,  95, 95, 60, 60, 85, 85, 75, 75, 80,   80];   %value
                   
picewise_brake    = [  0, stime;   %time
                       0,    0];   %value

                   
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

fprintf('Plotting \n');

%% Plotting the simulation
plotting (input, output, input_labels, output_labels);


%% Monitoring initialization

fprintf('Monitoring initialization\n');

%Generate a monitor object from the script fine multiple_spec.mls 
%this object is an implementation of MoonlightEngine class, please 
%refer to the doc of this class for more details (ex. write in console 
%"doc MoonlightEngine" ) please, open multiple_spec.mls 
monitor = MoonlightEngine.load("multiple_spec");


%% Monitoring

fprintf('Monitoring\n');
tic
bMonitorResult1  = monitor.temporalMonitor("BooleanMonitorSpec1",     time, output, 4000); 
toc 
tic
qMonitorResult1  = monitor.temporalMonitor("QuantitativeMonitorSpec1",time, output, 4000); 
toc
tic
bMonitorResult2  = monitor.temporalMonitor("BooleanMonitorSpec2",     time, output,[3000, 120]); 
toc
tic
qMonitorResult2  = monitor.temporalMonitor("QuantitativeMonitorSpec2",time, output,[3000, 120]); 
toc
tic
bMonitorResult6  = monitor.temporalMonitor("BooleanMonitorSpec6",     time, output,[3000, 120, 10]);
toc
tic
qMonitorResult6  = monitor.temporalMonitor("QuantitativeMonitorSpec6",time, output,[3000, 120, 10]); 
toc

bMonitorResult7  = monitor.temporalMonitor("BooleanMonitorSpec7",     time, output,[3000, 120, 10]); 
qMonitorResult7  = monitor.temporalMonitor("QuantitativeMonitorSpec7",time, output,[3000, 120, 10]); 



