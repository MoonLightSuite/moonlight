clear
numSteps        = 10;
num_nodes       = 20;
[spatialModel,time,signal]= sensorSystem(num_nodes,numSteps);
monitor = MoonlightEngine.load("sensor");

tic
result = monitor.spatioTemporalMonitor("SensNetkQuant",spatialModel,time,signal);
toc

time=result{1}(:,1);
spatialResult= zeros(length(time), num_nodes);

for t=1:length(time)
    for i = 1:num_nodes
        spatialResult(t,i)=result{i}(t,2);    
    end
    Gvor = spatialModel{t};
    plot(Gvor,'r','XData',Gvor.Nodes.x,'YData',Gvor.Nodes.y)
    % Get the frame for the animation.
    frames(t) = getframe;
end





    
