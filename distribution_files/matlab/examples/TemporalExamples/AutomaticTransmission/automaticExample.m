clear;       %clear all the memory
close all;   %close all the open windows
%% Configuration file for simulating the Simulink model "autotrans_mod04"
% Variables:
%    - stime - simulation_time (unit sec)
%    - dt - integration step (unit sec)
%    - piecewise_throttle - define the input signal of the throttle
%    - piecewise_break - define the input signal of the break

dt            =  0.01;
endtime         =  32;
solver        = 'ode5';

model_name         = 'autotrans_mod04';
input_labels  = {'time', 'throttle', 'brake'};
output_labels = {'engine speed in rpm', 'vehicle speed in mph', 'gear'};

fprintf('Settings\n\n');
fprintf('\t dt     = %f \n',  dt    );
fprintf('\t endtime  = %f \n',  endtime );
fprintf('\t solver = %s \n\n',solver);


piecewise_throttle = [  0,  5,   5, 10, 10, 15, 15, 20, 20, 25, 25, endtime;   %time
                       52, 52,  95, 95, 60, 60, 85, 85, 75, 75, 80,   80];   %value
%                    
piecewise_brake    = [  0, endtime;   %time
                        0,    0];   %value
                   
% n_controlpoints = 12;
% [piecewise_throttle, piecewise_brake] = generate_inputs (endtime,n_controlpoints );                   
%% Generating input signals
%

fprintf('Generating input signals\n');

time = 0:dt:endtime;

size_t = size(time,2);
input_throttle = zeros(size_t,1);
input_brake    = zeros(size_t,1);

for s=1:size_t
    input_throttle(s) = piecewise(time(s), piecewise_throttle);
    input_brake(s)    = piecewise(time(s), piecewise_brake);
end

input = zeros(size_t,3);

input(:,1) = time';
input(:,2) = input_throttle';
input(:,3) = input_brake';


load('opt_input.mat')
%% Simulating Simulink Model 

fprintf('Simulation of Simulink Model \n');


simopt = simget(model_name);
%Setting the Simulink simulation option.
simopt = simset(simopt,'solver', solver, 'FixedStep', dt, 'SaveFormat','Array');
[time, xt, output] = sim(model_name,[input(1,1,1) input(end,1,1)], simopt, input);


%% Plotting the simulation
fprintf('Plotting simulation \n');
plotting (input, output, input_labels, output_labels);


%% Monitoring parameters 

 
engine_speed_thresholds  = [4500,5000,5200,5500];  % omega
vehicle_speed_thresholds = [ 120, 160, 170, 200];  % v
time_bounds              = [   4,   8,  10,  20];



%% Monitoring property with Moonlight


%Generate a monitor object from the script fine multiple_spec.mls 
%this object is an implementation of ScriptLoader class, please 
%refer to the doc of this class for more details (ex. write in console 
%"doc ScriptLoader" ) please, open multiple_spec.mls 

% script = [
% "signal { real v_speed; real e_speed; int gear;}",...
% "domain boolean;",... 
% "formula Spec1(real omega) = globally [0, 0.2] ( e_speed < omega );",...
% "formula Spec2(real omega, real v) = globally {( e_speed < omega ) & ( v_speed < v )} ;"
% ];
% moonlightScript = ScriptLoader.loadFromText(script);

moonlightScript = ScriptLoader.loadFromFile("multiple_spec.mls");
prop_name = "Spec2";

moonlightScript.setBooleanDomain();
% formula Spec2(real omega, real v) = globally {( e_speed < omega ) & ( v_speed < v )} 
booleanMonitor1 = moonlightScript.getMonitor(prop_name);
bMonitorResult1  = booleanMonitor1.monitor(time, output, [engine_speed_thresholds(1), vehicle_speed_thresholds(2)]); 

moonlightScript.setMinMaxDomain();
quantitativeMonitor1 = moonlightScript.getMonitor(prop_name);
qMonitorResult1  = quantitativeMonitor1.monitor(time, output, [engine_speed_thresholds(1), vehicle_speed_thresholds(2)]); 

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
