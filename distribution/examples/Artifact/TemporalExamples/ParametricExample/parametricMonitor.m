% Paramteric Monitor Example
clear

%generate a monitor object from the script fine multipleMonitors.mls (contained in this folder)
%this object is an implementation of MoonlightEngine class, please refer to the doc of this class
%for more details (ex. write in console "doc MoonlightEngine" )
% please, open parametricMonitor.mls and look at the two paramters LB and UB! 
monitor = MoonlightEngine.load("parametricMonitor");

%generate a signal [time, x, y]  where x= sin(t) and y = cos(t)
trajFunction = @(t)[sin(t);cos(t)]';
time = 0:0.1:2*pi;
values = trajFunction(time);

%evaluate the BooleanMonitorScript defined in multipleMonitors.mls
%Formula: globally [0, 0.1]  #[ x > y ]#
booleanMonitorResult1 = monitor.temporalMonitor("BooleanMonitorScript",time,values,[0,0.1]); % we add the paramter vecotor [0, 0.1]
                                                                                             % LB=0 and UB=0.1   
%evalaute the QuantitativeMonitorScript defined in multipleMonitors.mls
%Formula: globally [0, 0.1]  #[ x > y ]#
quantiativeMonitorResult1 = monitor.temporalMonitor("QuantitativeMonitorScript",time,values,[0,0.1]);

%evaluate the BooleanMonitorScript defined in multipleMonitors.mls
%Formula: globally [0, 4]  #[ x > y ]#
booleanMonitorResult2 = monitor.temporalMonitor("BooleanMonitorScript",time,values,[0,4]);

%evalaute the QuantitativeMonitorScript defined in multipleMonitors.mls
%Formula: globally [0, 4]  #[ x > y ]#
quantiativeMonitorResult2 = monitor.temporalMonitor("QuantitativeMonitorScript",time,values,[0,4]);



%Plotting result...

tiledlayout(3,1)
nexttile
plot(time, sin(time))
hold on
plot(time, cos(time))
title('Signals')
legend('x=sin(t)','y=cos(t)')

nexttile
stairs(quantiativeMonitorResult1(:,1),quantiativeMonitorResult1(:,2))
hold on
%We add a last point to the boolean monitor to plot it easily!
boolean = [booleanMonitorResult1;time(end), booleanMonitorResult1(2,end)];
stairs(boolean(:,1),boolean(:,2))
title('globally [0, 0.1]  #[ x > y ]#')
legend('Quantiative Monitor','Boolean Monitor')

nexttile
stairs(quantiativeMonitorResult2(:,1),quantiativeMonitorResult2(:,2))
hold on
%We add a last point to the boolean monitor to plot it easily!
ylim([-1.5,1])
boolean = [booleanMonitorResult2;time(end), booleanMonitorResult2(2)];
stairs(boolean(:,1),boolean(:,2))
title('globally [0, 4]  #[ x > y ]#')
legend('Quantiative Monitor','Boolean Monitor')

