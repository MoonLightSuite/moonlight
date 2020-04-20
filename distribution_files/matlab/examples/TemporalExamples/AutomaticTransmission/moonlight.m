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

%Generate a monitor object from the script fine multiple_spec.mls 
%this object is an implementation of MoonlightEngine class, please 
%refer to the doc of this class for more details (ex. write in console 
%"doc MoonlightEngine" ) please, open multiple_spec.mls 
monitor = MoonlightEngine.load("multiple_spec");

%[bMonitorResult1, t]  = monitor.temporalMonitor("BooleanMonitorSpec1",     time,output,4000); 
%[qMonitorResult1, t]  = monitor.temporalMonitor("QuantitativeMonitorSpec1",time,output,4000); 


%% Monitoring property with Moonlight

fprintf('Monitoring\n');

tStart                = tic;
bMonitorResult1  = monitor.temporalMonitor("BooleanMonitorSpec1",     time, output, 4000); 
tElapsedSpec1MoonlightBoolean   = toc(tStart)

tStart                = tic;
qMonitorResult1  = monitor.temporalMonitor("QuantitativeMonitorSpec1",time, output, 4000); 
tElapsedSpec1MoonlightRobust   = toc(tStart)

tStart                = tic;
bMonitorResult2  = monitor.temporalMonitor("BooleanMonitorSpec2",     time, output,[3000, 120]); 
tElapsedSpec2MoonlightBoolean   = toc(tStart)

tStart                = tic;
qMonitorResult2  = monitor.temporalMonitor("QuantitativeMonitorSpec2",time, output,[3000, 120]); 
tElapsedSpec2MoonlightRobust   = toc(tStart)

tStart                = tic;
bMonitorResult6  = monitor.temporalMonitor("BooleanMonitorSpec6",     time, output,[3000, 120, 10]);
tElapsedSpec6MoonlightBoolean   = toc(tStart)

tStart                = tic;
qMonitorResult6  = monitor.temporalMonitor("QuantitativeMonitorSpec6",time, output,[3000, 120, 10]); 
tElapsedSpec6MoonlightRobust   = toc(tStart)

tStart                = tic;
bMonitorResult7  = monitor.temporalMonitor("BooleanMonitorSpec7",     time, output,[3000, 120, 10]); 
tElapsedSpec7MoonlightBoolean   = toc(tStart)

tStart                = tic;
qMonitorResult7  = monitor.temporalMonitor("QuantitativeMonitorSpec7",time, output,[3000, 120, 10]); 
tElapsedSpec7MoonlightRobust   = toc(tStart)

%% Monitoring property with Breach

trace = [time'; output']; % trace is in column format, first column is time
BrTrace = BreachTraceSystem({'v_speed','e_speed','gear'}, trace');
figure; BrTrace.PlotSignals();

figure; BrTrace.PlotRobustSat('alw (e_speed[t] < 4000)',1);

tStart                = tic;
spec1                 = STL_Formula('Spec1', 'alw (e_speed[t] < 4000)');
spec1_rob             = BrTrace.CheckSpec(spec1)
tElapsedSpec1Breach   = toc(tStart)

tStart                = tic;
spec2                 = STL_Formula('Spec2', 'alw (( e_speed[t] < 3000 ) and (v_speed[t] < 120))');
spec2_rob             = BrTrace.CheckSpec(spec2)
tElapsedSpec2Breach   = toc(tStart)


tStart                = tic;
spec6                 = STL_Formula('Spec6', 'not ((alw_[0,10] (v_speed[t] > 120))  and  alw (e_speed[t] < 3000 ))');
spec6_rob             = BrTrace.CheckSpec(spec6)
tElapsedSpec6Breach   = toc(tStart)

tStart                = tic;
spec7                 = STL_Formula('Spec7', '(ev_[0,10] (v_speed[t] >= 120)) and  (alw (e_speed[t] < 3000))');
spec7_rob             = BrTrace.CheckSpec(spec7)
tElapsedSpec7Breach   = toc(tStart)

%% Monitoring property with S-Taliro


% alw (e_speed[t] < 4000)

st_spec1 = '[] (a1)';

%a1: e_speed < omega

st_spec1_Pred(1).str = 'a1';
st_spec1_Pred(1).A = [0 1 0];
st_spec1_Pred(1).b = [4000];

tStart                = tic;
rob1 = fw_taliro(st_spec1,st_spec1_Pred,output,time)
tElapsedSpec1Staliro  = toc(tStart)

% alw (( e_speed[t] < 3000 ) and (v_speed[t] < 120))

st_spec2 = '[] (a1 /\ a2)';

%a1: e_speed[t] < 3000

st_spec2_Pred(1).str = 'a1';
st_spec2_Pred(1).A = [0 1 0];
st_spec2_Pred(1).b = [3000];

%a2: (v_speed[t] < 120)

st_spec2_Pred(2).str = 'a2';
st_spec2_Pred(2).A = [1 0 0];
st_spec2_Pred(2).b = [120];

tStart                = tic;
rob2 = fw_taliro(st_spec2,st_spec2_Pred,output,time)
tElapsedSpec2Staliro  = toc(tStart)

% not ((alw_[0,10] (v_speed[t] > 120))  and  alw (e_speed[t] < 3000 ))

st_spec6 = '!([]_[0,10] (a1) /\ [] (a2))';

%a1: v_speed[t] > 120

st_spec6_Pred(1).str = 'a1';
st_spec6_Pred(1).A = [-1 0 0];
st_spec6_Pred(1).b = [-120];

%a2: e_speed[t] < 3000

st_spec6_Pred(2).str = 'a2';
st_spec6_Pred(2).A = [0 1 0];
st_spec6_Pred(2).b = [3000];

tStart                = tic;
rob6 = fw_taliro(st_spec6,st_spec6_Pred,output,time)
tElapsedSpec6Staliro  = toc(tStart)

% (ev_[0,10] (v_speed[t] >= 120)) and  (alw (e_speed[t] < 3000))

st_spec7 = '(<>_[0,10] (a1)) /\ ([] (a2))';

%a1: v_speed[t] > 120

st_spec7_Pred(1).str = 'a1';
st_spec7_Pred(1).A = [-1 0 0];
st_spec7_Pred(1).b = [-120];

%a2: e_speed[t] < 3000

st_spec7_Pred(2).str = 'a2';
st_spec7_Pred(2).A = [0 1 0];
st_spec7_Pred(2).b = [3000];

tStart                = tic;
rob7 = fw_taliro(st_spec7,st_spec7_Pred,output,time)
tElapsedSpec7Staliro  = toc(tStart)




