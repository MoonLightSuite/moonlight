% Script Monitor Example Example
clear

%generate a signal [time, x, y]  where x= sin(t) and y = cos(t)
trajFunction = @(t)[sin(t);cos(t)]';
time = 0:0.1:3.1;
values = trajFunction(time);

script1 = [
"signal { real x; real y}",...
"domain minmax;",... 
"formula future = globally [0, 0.2]  (x > y);"...
"formula past = historically [0, 0.2]  (x > y);"
];

script2 = [
"signal { real x; real y}",...
"domain minmax;",... 
"formula future = globally [0, 0.2]  (x > y);"...
"formula past = historically [0, 0.2]  (x > y);"
];
moonlightScript = ScriptLoader.loadFromText(script1);
quantitativeMonitor = moonlightScript.getMonitor("future");
moonlightScript = ScriptLoader.loadFromText(script2);
booleanMonitor = moonlightScript.getMonitor("future");


%evalauting monitors
quantiativeMonitorResult = quantitativeMonitor.monitor(time,values);
booleanMonitorResult = booleanMonitor.monitor(time,values);


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