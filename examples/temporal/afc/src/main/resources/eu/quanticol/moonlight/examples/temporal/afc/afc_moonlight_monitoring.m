% this script executes the simulink model from
% https://github.com/decyphir/breach/tree/master/Online/examples/AFC_online
% without running the breach monitor,
% so that the execution times of Breach and Moonlight can be compared


% Original Script from
% https://github.com/decyphir/breach/tree/master/Online/examples/AFC_online

%% Online Monitoring
%

%% Initialization
% This demo uses a modified version of the AbstractFuelControl model to
% illustrate the use of online monitoring blocks from the Breach Library.
BrDemo.InitAFC_Online()

%%
% Breach Library is accessible via Simulink Library Browser, or by opening
% slstlib.slx. AFC_Online contains one 'STL Monitor' block and one 'STL
% stops when false' block. We configure the first one as follows.

%max_rob = +inf;            % estimate of maximum robust satisfaction of a formula (default to +inf)
sig_names = 'AF,AFref';   % declares signal names used in the specification

% defines a simple overshoot property - note that STL formula are given as
% string, STL_Formula object are not supported.
phi_st = 'alw_[10, 30] ((abs(AF[t]-AFref[t]) > 0.05) => (ev_[0, 1] (abs(AF[t]-AFref[t]) < 0.05)))';
%phi_st = '(abs(AF[t]-AFref[t]) < 0.05)';
%phi_st = '(ev_[0, 1] (abs(AF[t]-AFref[t]) < 0.05))';
%phi_st = '((abs(AF[t]-AFref[t]) > 0.05) => (ev_[0, 1] (abs(AF[t]-AFref[t]) < 0.05)))';

%%
% Set parameters for one simulation and run.
BrAFC_Online.SetParam({'max_rob','Pedal_Angle_pulse_period', 'Pedal_Angle_pulse_amp'}, [max_rob, 12, 50]);
BrAFC_Online.Sim(0:.1:40)

input = BrAFC_Online.GetExprValues('AF[t] - AFref[t]')
