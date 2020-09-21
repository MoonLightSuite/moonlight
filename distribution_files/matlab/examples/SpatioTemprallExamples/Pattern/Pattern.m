clear;
close all;

%% STEP 1: define a spatial-temporal signal.
patternSize = 32;   % number of frames
num_nodes = patternSize*patternSize;    % number of nodes
framePlot = true; % to enable or disable the plot of the graph
% see the sensorModel function for the description of the output
[SpTempModel, time, values]  = TuringDataGenerator(patternSize, framePlot);


%% STEP 2: loading the Moonlight Script for a stringArray variable (i.e., script)
%% monitor
% loading of the script
%generate a moonlightScript object from the script file multipleMonitors.mls (contained in this folder)
%this object is an implementation of ScriptLoader class, please refer to 
%the doc of this class for more details (ex. write in console "doc ScriptLoader" )
moonlightScript = ScriptLoader.loadFromFile("patternMonitorScript");

%% STEP 3 (optional): change the domain on the fly
moonlightScript.setMinMaxDomain();

%% STEP 4: getting the monitor associated with a target formula
spTempMonitor = moonlightScript.getMonitor("reachability");

%% STEP 5: monitor the signal 
tic
result = spTempMonitor.monitor(SpTempModel,time,values);
toc
%% STEP 6: plot
%%%% animation pattern result
figure,
time = result(1,:,1);   
resultV = result(:,:,2); 
for t=1: length(result(1,:,1))
    i=1;
    for x = 1: 32
        for y = 1:32
            rob(x,y) = resultV(i,t);
            if resultV(i,t)>0
                bool(x,y) = 1;
            else
                bool(x,y) = 0;
            end
            i = i +1;
        end
    end
    surf(bool)
    view(2);
    %contourf(X)
    xlabel('X','Fontsize', 18);
    ylabel('Y','Fontsize', 18);
    zlabel('satisfaction','Fontsize', 18);
    set(gca,'FontSize',18);
    colormap jet
    colorbar('FontSize',18);
    axis([1 patternSize 1 patternSize]);
    pause(0.01);
end


