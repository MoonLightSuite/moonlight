% Multiple Monitor Example
clear

%generate a monitor object from the script fine multipleMonitors.mls (contained in this folder)
%this object is an implementation of MoonlightEngine class, please refer to the doc of this class
%for more details (ex. write in console "doc MoonlightEngine" )
moonlightScript = ScriptLoader.load("multipleMonitorsBoolean");
moonlightScript.setBooleanDomain();
booleanMonitor = moonlightScript.getMonitor("future");
moonlightScript.setMinMaxDomain();
quantitativeMonitor = moonlightScript.getMonitor("future");


%generate a signal [time, x, y]  where x= sin(t) and y = cos(t)
trajFunction = @(t)[sin(t);cos(t)]';
time = 0:0.1:3.1;
values = trajFunction(time);

%evaluate the BooleanMonitorScript defined in multipleMonitors.mls
%Formula: globally [0, 0.2]  #[ x > y ]#
booleanMonitorResult = booleanMonitor.monitor(time,values);
%evalaute the QuantitativeMonitorScript defined in multipleMonitors.mls
%Formula: globally [0, 0.2]  #[ x > y ]#
quantiativeMonitorResult = quantitativeMonitor.monitor(time,values);

%Plotting result...
figure,
tiledlayout(2,1)
nexttile
plot(time, sin(time))
hold on
plot(time, cos(time))
title('Signals')
legend('x=sin(t)','y=cos(t)')
nexttile
stairs(quantiativeMonitorResult(:,1),quantiativeMonitorResult(:,2))
hold on
%We add a last point to the boolean monitor to plot it easily!
boolean = [booleanMonitorResult;time(end), booleanMonitorResult(2,end)];
stairs(boolean(:,1),boolean(:,2))
title('globally [0, 0.2]  (x > y)')
legend('Quantiative Monitor','Boolean Monitor')

% 
% %evaluate the BooleanPastMonitorScript defined in multipleMonitors.mls
% %Formula: historically [0, 0.2]  #[ x > y ]#
% [booleanPastMonitorResult,t] = booleanMonitor.temporalMonitor("past",time,values);
% disp(t)
% %evalaute the QuantitativePastMonitorScript defined in multipleMonitors.mls
% %Formula: historically [0, 0.2]  #[ x > y ]#
% [quantiativePastMonitorResult,t] = quantitativeMonitor.temporalMonitor("past",time,values);
% disp(t)
% 
% 
% %Plotting result...
% figure,
% tiledlayout(2,1)
% nexttile
% plot(time, sin(time))
% hold on
% plot(time, cos(time))
% title('Signals')
% legend('x=sin(t)','y=cos(t)')
% nexttile
% stairs(quantiativePastMonitorResult(:,1),quantiativePastMonitorResult(:,2))
% hold on
% %We add a last point to the boolean monitor to plot it easily!
% boolean = [booleanPastMonitorResult;time(end), booleanPastMonitorResult(2,end)];
% stairs(boolean(:,1),boolean(:,2))
% title('historically [0, 0.2]  (x > y)')
% legend('Quantiative Monitor','Boolean Monitor')