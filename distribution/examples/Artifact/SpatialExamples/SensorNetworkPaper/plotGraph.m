function plotGraph(spatialModel, numFrame, s)
Gvor = spatialModel{numFrame};
A = adjacency(Gvor);
G = graph(A);
p = plot(G,'r','XData',Gvor.Nodes.x,'YData',Gvor.Nodes.y);
p.EdgeColor = 'black';
p.MarkerSize = 10;
set(gca,'FontSize',18);    
colorbar('FontSize',18);
if s == 'node'
    p.NodeCData = cell2mat(Gvor.Nodes.nodeType);
elseif s == 'batt'
    p.NodeCData = Gvor.Nodes.battery;
elseif s == 'temp'
    p.NodeCData = Gvor.Nodes.temperature;
colorbar
end
end

