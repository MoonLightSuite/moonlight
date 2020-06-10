% Parametric Monitor Example
clear

%generate a signal [time, x, y]  where x= sin(t) and y = cos(t)
trajFunction = @(t)[sin(t);cos(t)]';
time = 0:0.1:2*pi;
values = trajFunction(time);   % x,y

%generate a moonlightScript object from the script fine parametricMonitor.mls (contained in this folder)
%this object is an implementation of ScriptLoader class, please 
%refer to the doc of this class for more details (ex. write in console 
%"doc ScriptLoader" ) please, open parametricMonitor.mls 
moonlightScript = ScriptLoader.load("parametricMonitor");

% generate the monitor for each property, as default it is taken the
% semantincs of the .mls script, in this case a boolean semantics
%phi(real LB, real UB) = globally [LB, UB] (x >= y)
monitorPhi1 = moonlightScript.getMonitor("phi1");
%psi(real LB, real UB) = globally [0, 1] (x >= k)
monitorPhi2 = moonlightScript.getMonitor("phi2");

%evaluate boolMonitorPhi1 with LB=0 and UB=0.1, i.e. globally [0, 0.1] (x >= y) 
monitorResult1 = monitorPhi1.monitor(time,values,[0,0.1]); 
%evaluate boolMonitorPhi2 with k=3, i.e. globally [0, 1] (x >= 0.5) 
monitorResult2 = monitorPhi2.monitor(time,values,0.5);

% we can set the semantics using
moonlightScript.setBooleanDomain(); %for the boolean semantics
boolMonitorPhi1 = moonlightScript.getMonitor("phi1");
boolMonitorPhi2 = moonlightScript.getMonitor("phi2");
boolMonitorResult1 = boolMonitorPhi1.monitor(time,values,[0,0.1]);
boolMonitorResult2 = boolMonitorPhi2.monitor(time,values,0.5);

moonlightScript.setMinMaxDomain(); % for the quantitative semantics
quantMonitorPhi1 = moonlightScript.getMonitor("phi1");
quantMonitorPhi2 = moonlightScript.getMonitor("phi2");
quantMonitorResult1 = quantMonitorPhi1.monitor(time,values,[0,0.1]);
quantMonitorResult2 = quantMonitorPhi2.monitor(time,values,0.5);


%Plotting result...

tiledlayout(3,1)
nexttile
plot(time, sin(time))
hold on
plot(time, cos(time))
title('Signals')
legend('x=sin(t)','y=cos(t)')

nexttile
stairs(quantMonitorResult1(:,1),quantMonitorResult1(:,2))
hold on
%We add a last point to the boolean monitor to plot it easily!
boolean = [boolMonitorResult1;time(end), boolMonitorResult1(2,end)];
stairs(boolean(:,1),boolean(:,2))
title('globally [0, 0.1]  ( x > y )')
legend('Quantiative Monitor','Boolean Monitor')

nexttile
stairs(quantMonitorResult2(:,1),quantMonitorResult2(:,2))
hold on
%We add a last point to the boolean monitor to plot it easily!
ylim([-1.5,1])
boolean = [boolMonitorResult2;time(end), boolMonitorResult2(2)];
stairs(boolean(:,1),boolean(:,2))
title('globally [0, 1]  ( x > 0.5 )')
legend('Quantiative Monitor','Boolean Monitor')

