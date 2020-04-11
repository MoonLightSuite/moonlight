clear;


%Generate a monitor object from the script fine multiple_spec.mls 
%this object is an implementation of MoonlightEngine class, please 
%refer to the doc of this class for more details (ex. write in console 
%"doc MoonlightEngine" ) please, open multiple_spec.mls 
monitor = MoonlightEngine.load("multiple_spec");


%% Configuration file for simulating the Simulink model "autotrans_mod04"
% Variables:
%    - stime - simulation_time (unit sec)
%    - dt - integration step (unit sec)
%    - piecewise_throttle - define the input signal of the throttle
%    - piecewise_break - define the input signal of the break

dt     =  0.02;
stime  =  80;
solver = 'ode5';

picewise_throttle = [  0,  5,   5, 10, 10, 15, 15, 20, 20, 25, 25, stime;   %time
                      52, 52,  95, 95, 60, 60, 85, 85, 75, 75, 80,   80     %value
                       ];  %time
                   
picewise_brake    = [  0, stime;   %time
                       0,    0];   %value

                   
%% Generating input signals
%

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
[time, output] = simSimulinkModel ('autotrans_mod04', input, solver, dt);


%% Plotting the simulation
plotting (input, output);


%% Monitoring

bMonitorResult1  = monitor.temporalMonitor("BooleanMonitorSpec1",     time,output,4000); 
qMonitorResult1  = monitor.temporalMonitor("QuantitativeMonitorSpec1",time,output,4000); 

bMonitorResult2  = monitor.temporalMonitor("BooleanMonitorSpec2",     time,output,[3000, 120]); 
qMonitorResult2  = monitor.temporalMonitor("QuantitativeMonitorSpec2",time,output,[3000, 120]); 

bMonitorResult6  = monitor.temporalMonitor("BooleanMonitorSpec6",     time,output,[3000, 120, 10]); 
qMonitorResult6  = monitor.temporalMonitor("QuantitativeMonitorSpec6",time,output,[3000, 120, 10]); 

bMonitorResult7  = monitor.temporalMonitor("BooleanMonitorSpec7",     time,output,[3000, 120, 10]); 
qMonitorResult7  = monitor.temporalMonitor("QuantitativeMonitorSpec7",time,output,[3000, 120, 10]); 



