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

dt    = 0.02
stime = 30

picewise_throttle = [  0,  5,   5, 10, 10, 15, 15, 20, 20, 25, 25, 30;   %time
                      52, 52,  95, 95, 60, 60, 85, 85, 75, 75, 80, 80   %value
                       ];  %time
                   
picewise_break    = [  0, 30;   %time
                       0,  0];  %value

                   
%% Generating input signals
%

time = 0:dt:stime;

size_t = size(time,2)
input_throttle = zeros(size_t,1);
input_break    = zeros(size_t,1);

for s=1:size_t
    input_throttle(s) = piecewise(time(s), picewise_throttle);
    input_break(s)    = piecewise(time(s), picewise_break);
end

input = zeros(size_t,3)

input(:,1) = time';
input(:,2) = input_throttle';
input(:,3) = input_break';

%% Simulating Simulink Model 
%
%
simopt = simget('autotrans_mod04');

%Setting the Simulink simulation option.
simopt = simset(simopt,'solver', 'ode5', 'FixedStep', dt, 'SaveFormat','Array');

[T, XT, output] = sim('autotrans_mod04',[time(1) time(end)], simopt, input);

%% Plotting the simulation
%
figure
subplot(5,1,1);
plot(T,input(:,2))
xlabel('time')
ylabel('Input (throttle)')

subplot(5,1,2);
plot(T,input(:,3))
xlabel('time')
ylabel('Input (break)')

subplot(5,1,3);
plot(T,output(:,1))
xlabel('time')
ylabel('Output Speed (mph)')

subplot(5,1,4);
plot(T,output(:,2))
xlabel('time')
ylabel('Output Engine (rpm)')

subplot(5,1,5);
plot(T,output(:,3))
xlabel('time')
ylabel('Output Gear')


%% Monitoring

bMonitorResult1  = monitor.temporalMonitor("BooleanMonitorSpec1",     time,output,5000); 
qMonitorResult1  = monitor.temporalMonitor("QuantitativeMonitorSpec1",time,output,5000); 

bMonitorResult2  = monitor.temporalMonitor("BooleanMonitorSpec2",     time,output,[3000, 120]); 
qMonitorResult2  = monitor.temporalMonitor("QuantitativeMonitorSpec2",time,output,[3000, 120]); 

bMonitorResult6  = monitor.temporalMonitor("BooleanMonitorSpec6",     time,output,[3000, 120, 10]); 
qMonitorResult6  = monitor.temporalMonitor("QuantitativeMonitorSpec6",time,output,[3000, 120, 10]); 

bMonitorResult7  = monitor.temporalMonitor("BooleanMonitorSpec7",     time,output,[3000, 120, 10]); 
qMonitorResult7  = monitor.temporalMonitor("QuantitativeMonitorSpec7",time,output,[3000, 120, 10]); 



