clear;       %clear all the memory
close all;   %close all the open windows

NTest = 10;
NExp  = 50;
STime = 64;
Dt    = 0.01;
testdirs = {};

for i=1:NTest
    testdirs{i} = test_automatic (Dt, STime, NExp);
end

ntests = size (testdirs,2)

moonlight_rob_time_spec1 = []
moonlight_rob_time_spec2 = []
moonlight_rob_time_spec3 = []
moonlight_rob_time_spec4 = []

breach_rob_time_spec1 = []
breach_rob_time_spec2 = []
breach_rob_time_spec3 = []
breach_rob_time_spec4 = []

staliro_rob_time_spec1 = []
staliro_rob_time_spec2 = []
staliro_rob_time_spec3 = []
staliro_rob_time_spec4 = []

for i=1:ntests
   
    
    load (strcat('./test/',testdirs{i},'/monitoring_moonlight.mat'), 'moonlight_robust_spec1', 'moonlight_robust_spec2', 'moonlight_robust_spec3', 'moonlight_robust_spec4','moonlight_robust_time_spec1', 'moonlight_robust_time_spec2', 'moonlight_robust_time_spec3', 'moonlight_robust_time_spec4');
    moonlight_rob_time_spec1 = [moonlight_rob_time_spec1 moonlight_robust_time_spec1(:)] 
    moonlight_rob_time_spec2 = [moonlight_rob_time_spec2 moonlight_robust_time_spec2(:)] 
    moonlight_rob_time_spec3 = [moonlight_rob_time_spec3 moonlight_robust_time_spec3(:)] 
    moonlight_rob_time_spec4 = [moonlight_rob_time_spec4 moonlight_robust_time_spec4(:)]
    
    
    load (strcat('./test/',testdirs{i},'/monitoring_breach.mat'), 'breach_robust_spec1', 'breach_robust_spec2', 'breach_robust_spec3', 'breach_robust_spec4','breach_robust_time_spec1', 'breach_robust_time_spec2', 'breach_robust_time_spec3', 'breach_robust_time_spec4');

    breach_rob_time_spec1 = [breach_rob_time_spec1 breach_robust_time_spec1(:)] 
    breach_rob_time_spec2 = [breach_rob_time_spec2 breach_robust_time_spec2(:)] 
    breach_rob_time_spec3 = [breach_rob_time_spec3 breach_robust_time_spec3(:)] 
    breach_rob_time_spec4 = [breach_rob_time_spec4 breach_robust_time_spec4(:)]
    
    
    load (strcat('./test/',testdirs{i},'/monitoring_staliro.mat'), 'staliro_robust_spec1', 'staliro_robust_spec2', 'staliro_robust_spec3', 'staliro_robust_spec4','staliro_robust_time_spec1', 'staliro_robust_time_spec2', 'staliro_robust_time_spec3', 'staliro_robust_time_spec4');

    staliro_rob_time_spec1 = [staliro_rob_time_spec1 staliro_robust_time_spec1(:)] 
    staliro_rob_time_spec2 = [staliro_rob_time_spec2 staliro_robust_time_spec2(:)] 
    staliro_rob_time_spec3 = [staliro_rob_time_spec3 staliro_robust_time_spec3(:)] 
    staliro_rob_time_spec4 = [staliro_rob_time_spec4 staliro_robust_time_spec4(:)]
    
    
end

moonlight_rob_time_spec1 = moonlight_rob_time_spec1 (:);
moonlight_rob_time_spec2 = moonlight_rob_time_spec2 (:);
moonlight_rob_time_spec3 = moonlight_rob_time_spec3 (:);
moonlight_rob_time_spec4 = moonlight_rob_time_spec4 (:);

breach_rob_time_spec1 = breach_rob_time_spec1 (:);
breach_rob_time_spec2 = breach_rob_time_spec2 (:);
breach_rob_time_spec3 = breach_rob_time_spec3 (:);
breach_rob_time_spec4 = breach_rob_time_spec4 (:);

staliro_rob_time_spec1 = staliro_rob_time_spec1 (:);
staliro_rob_time_spec2 = staliro_rob_time_spec2 (:);
staliro_rob_time_spec3 = staliro_rob_time_spec3 (:);
staliro_rob_time_spec4 = staliro_rob_time_spec4 (:);

status = mkdir('test', 'meta');


currDate = strrep(datestr(datetime), ' ', '_');
status = mkdir('test/meta', currDate);

save (strcat('./test/meta/',currDate,'/meta.mat'), 'NTest', 'NExp', 'STime', 'Dt', 'testdirs', 'moonlight_rob_time_spec1', 'moonlight_rob_time_spec2', 'moonlight_rob_time_spec3', 'moonlight_rob_time_spec4', 'breach_rob_time_spec1', 'breach_rob_time_spec2' , 'breach_rob_time_spec3' , 'breach_rob_time_spec4' , 'staliro_rob_time_spec1','staliro_rob_time_spec2','staliro_rob_time_spec3','staliro_rob_time_spec4');

hold on 
legendEntries = {'Moonlight Spec1' 'Moonlight Spec2' 'Moonlight Spec3' 'Moonlight Spec4' 'Breach Spec1' 'Breach Spec2' 'Breach Spec3' 'Breach Spec4' 'STaliro Spec1' 'STaliro Spec2' 'STaliro Spec3' 'STaliro Spec4'};

boxplot(moonlight_rob_time_spec1, 'Color', [0.4 0.4 0.4],  'position', -0.7, 'boxstyle','filled', 'widths',0.05, 'MedianStyle', 'line', 'labels', 'Spec 1')
plot(NaN,1,'color',[0.4 0.4 0.4]); %// dummy plot for legend

boxplot(moonlight_rob_time_spec2, 'Color', [0.4 0.5 0.5], 'position', -0.6, 'boxstyle','filled', 'widths',0.05, 'MedianStyle', 'line', 'labels', 'Spec 1')
plot(NaN,1,'color',[0.4 0.5 0.5]); %// dummy plot for legend
boxplot(moonlight_rob_time_spec3, 'Color', [0.4 0.6 0.6], 'position', -0.5, 'boxstyle','filled', 'widths',0.05, 'MedianStyle', 'line')
plot(NaN,1,'color',[0.4 0.6 0.6]); %// dummy plot for legend
boxplot(moonlight_rob_time_spec4, 'Color', [0.4 0.7 0.7], 'position', -0.4, 'boxstyle','filled', 'widths',0.05, 'MedianStyle', 'line')
plot(NaN,1,'color',[0.4 0.7 0.7]); %// dummy plot for legend

boxplot(breach_rob_time_spec1, 'Color', [0.6 0.4 0.4], 'position', -0.3, 'boxstyle','filled', 'widths',0.05, 'MedianStyle', 'line')
plot(NaN,1,'color',[0.6 0.4 0.4]); %// dummy plot for legend

boxplot(breach_rob_time_spec2, 'Color', [0.6 0.5 0.5], 'position', -0.2, 'boxstyle','filled', 'widths',0.05, 'MedianStyle', 'line')
plot(NaN,1,'color',[0.6 0.5 0.5]); %// dummy plot for legend

boxplot(breach_rob_time_spec3, 'Color', [0.6 0.6 0.6], 'position', -0.1, 'boxstyle','filled', 'widths',0.05, 'MedianStyle', 'line')
plot(NaN,1,'color',[0.6 0.6 0.6]); %// dummy plot for legend

boxplot(breach_rob_time_spec4, 'Color', [0.6 0.7 0.7], 'position', 0.0, 'boxstyle','filled', 'widths',0.05, 'MedianStyle', 'line')
plot(NaN,1,'color',[0.6 0.7 0.7]); %// dummy plot for legend

boxplot(staliro_rob_time_spec1, 'Color', [0.8 0.4 0.4], 'position', 0.1, 'boxstyle','filled', 'widths',0.05, 'MedianStyle', 'line')
plot(NaN,1,'color',[0.8 0.4 0.4]); %// dummy plot for legend
boxplot(staliro_rob_time_spec2, 'Color', [0.8 0.5 0.5], 'position', 0.2, 'boxstyle','filled', 'widths',0.05, 'MedianStyle', 'line')
plot(NaN,1,'color',[0.8 0.5 0.5]); %// dummy plot for legend

boxplot(staliro_rob_time_spec3, 'Color', [0.8 0.6 0.6], 'position', 0.3, 'boxstyle','filled', 'widths',0.05, 'MedianStyle', 'line')
plot(NaN,1,'color',[0.8 0.6 0.6]); %// dummy plot for legend
boxplot(staliro_rob_time_spec4, 'Color', [0.8 0.7 0.7], 'position', 0.4, 'boxstyle','filled', 'widths',0.05, 'MedianStyle', 'line')
plot(NaN,1,'color',[0.8 0.7 0.7]); %// dummy plot for legend


ylim([0 0.12]) 
xlim([-0.9 0.5])

legend(legendEntries);
ylabel('Execution time (sec.)'); grid on;

status = mkdir('test', 'meta');
status = mkdir('test/meta/',currDate);

save (strcat('./test/meta/',currDate,'/meta.mat'), 'NTest', 'NExp', 'STime', 'Dt', 'testdirs', 'moonlight_rob_time_spec1', 'moonlight_rob_time_spec2', 'moonlight_rob_time_spec3', 'moonlight_rob_time_spec4', 'breach_rob_time_spec1', 'breach_rob_time_spec2' , 'breach_rob_time_spec3' , 'breach_rob_time_spec4' , 'staliro_rob_time_spec1','staliro_rob_time_spec2','staliro_rob_time_spec3','staliro_rob_time_spec4');

hold on 
legendEntries = {'Moonlight Spec1' 'Moonlight Spec2' 'Moonlight Spec3' 'Moonlight Spec4' 'Breach Spec1' 'Breach Spec2' 'Breach Spec3' 'Breach Spec4' 'STaliro Spec1' 'STaliro Spec2' 'STaliro Spec3' 'STaliro Spec4'};

boxplot(moonlight_rob_time_spec1, 'Color', [0.4 0.4 0.4],  'position', -0.7, 'boxstyle','filled', 'widths',0.05, 'MedianStyle', 'line', 'labels', 'Spec 1')
plot(NaN,1,'color',[0.4 0.4 0.4]); %// dummy plot for legend

boxplot(moonlight_rob_time_spec2, 'Color', [0.4 0.5 0.5], 'position', -0.6, 'boxstyle','filled', 'widths',0.05, 'MedianStyle', 'line', 'labels', 'Spec 1')
plot(NaN,1,'color',[0.4 0.5 0.5]); %// dummy plot for legend
boxplot(moonlight_rob_time_spec3, 'Color', [0.4 0.6 0.6], 'position', -0.5, 'boxstyle','filled', 'widths',0.05, 'MedianStyle', 'line')
plot(NaN,1,'color',[0.4 0.6 0.6]); %// dummy plot for legend
boxplot(moonlight_rob_time_spec4, 'Color', [0.4 0.7 0.7], 'position', -0.4, 'boxstyle','filled', 'widths',0.05, 'MedianStyle', 'line')
plot(NaN,1,'color',[0.4 0.7 0.7]); %// dummy plot for legend

boxplot(breach_rob_time_spec1, 'Color', [0.6 0.4 0.4], 'position', -0.3, 'boxstyle','filled', 'widths',0.05, 'MedianStyle', 'line')
plot(NaN,1,'color',[0.6 0.4 0.4]); %// dummy plot for legend

boxplot(breach_rob_time_spec2, 'Color', [0.6 0.5 0.5], 'position', -0.2, 'boxstyle','filled', 'widths',0.05, 'MedianStyle', 'line')
plot(NaN,1,'color',[0.6 0.5 0.5]); %// dummy plot for legend

boxplot(breach_rob_time_spec3, 'Color', [0.6 0.6 0.6], 'position', -0.1, 'boxstyle','filled', 'widths',0.05, 'MedianStyle', 'line')
plot(NaN,1,'color',[0.6 0.6 0.6]); %// dummy plot for legend

boxplot(breach_rob_time_spec4, 'Color', [0.6 0.7 0.7], 'position', 0.0, 'boxstyle','filled', 'widths',0.05, 'MedianStyle', 'line')
plot(NaN,1,'color',[0.6 0.7 0.7]); %// dummy plot for legend

boxplot(staliro_rob_time_spec1, 'Color', [0.8 0.4 0.4], 'position', 0.1, 'boxstyle','filled', 'widths',0.05, 'MedianStyle', 'line')
plot(NaN,1,'color',[0.8 0.4 0.4]); %// dummy plot for legend
boxplot(staliro_rob_time_spec2, 'Color', [0.8 0.5 0.5], 'position', 0.2, 'boxstyle','filled', 'widths',0.05, 'MedianStyle', 'line')
plot(NaN,1,'color',[0.8 0.5 0.5]); %// dummy plot for legend

boxplot(staliro_rob_time_spec3, 'Color', [0.8 0.6 0.6], 'position', 0.3, 'boxstyle','filled', 'widths',0.05, 'MedianStyle', 'line')
plot(NaN,1,'color',[0.8 0.6 0.6]); %// dummy plot for legend
boxplot(staliro_rob_time_spec4, 'Color', [0.8 0.7 0.7], 'position', 0.4, 'boxstyle','filled', 'widths',0.05, 'MedianStyle', 'line')
plot(NaN,1,'color',[0.8 0.7 0.7]); %// dummy plot for legend


ylim([0 0.12]) 
xlim([-0.9 0.5])

legend(legendEntries);
ylabel('Execution time (sec.)'); grid on;
