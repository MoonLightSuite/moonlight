% Multiple Monitor Example
clear

%generate a signal [time, x, y]  where x= sin(t) and y = cos(t)
trajFunction = @(t)[sin(t);cos(t)]';
time = 0:0.1:3.1;
values = trajFunction(time);


%generate a monitor object from the script fine multipleMonitors.mls (contained in this folder)
%this object is an implementation of MoonlightEngine class, please refer to the doc of this class
%for more details (ex. write in console "doc MoonlightEngine" )
moonlightScript = ScriptLoader.loadFromFile("multipleMonitors");
moonlightScript.setBooleanDomain();
%creating the Boolean monitor for formula future = globally [0, 0.2]  (x > y)
boolFutureMonitor = moonlightScript.getMonitor("future");
%creating the Boolean monitor for formula  past = historically [0, 0.2]  (x > y);)
boolPastMonitor = moonlightScript.getMonitor("past");
moonlightScript.setMinMaxDomain();
%creating the Quantitative monitor for formula future = globally [0, 0.2]  (x > y) 
quantFutureMonitor = moonlightScript.getMonitor("future");
%creating the Quantitative monitor for formula past = historically [0, 0.2]  (x > y);)) 
quantPastMonitor = moonlightScript.getMonitor("past");

moonlightScript.getMonitors()
% moonlightScript.getInfoDefaultMonitor()
% moonlightScript.getInfoMonitor("future")

%evaluating the monitors
boolFutureMonitorResult = boolFutureMonitor.monitor(time,values);
quantFutureMonitorResult = quantFutureMonitor.monitor(time,values);
boolPastMonitorResult = boolPastMonitor.monitor(time,values);
quantPastMonitorResult = quantPastMonitor.monitor(time,values);


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
stairs(quantFutureMonitorResult(:,1),quantFutureMonitorResult(:,2))
hold on
%We add a last point to the boolean monitor to plot it easily!
boolean = [boolFutureMonitorResult;time(end), boolFutureMonitorResult(2,end)];
stairs(boolean(:,1),boolean(:,2))
title('globally [0, 0.2]  (x > y)')
legend('Quantiative Monitor','Boolean Monitor')




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
stairs(quantPastMonitorResult(:,1),quantPastMonitorResult(:,2))
hold on
%We add a last point to the boolean monitor to plot it easily!
boolean = [boolPastMonitorResult;time(end), boolPastMonitorResult(2,end)];
stairs(boolean(:,1),boolean(:,2))
title('historically [0, 0.2]  (x > y)')
legend('Quantiative Monitor','Boolean Monitor')