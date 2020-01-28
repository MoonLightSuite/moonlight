% t =numStepInt;
% plot(t,timeMoonRepSignal,'b-+',t,timeTalRepSignal,'r--*',t,timeBreachRepSignal,'g-.o',...
% 'LineWidth',2,'MarkerSize',10);
% %plot(t,timeMoonRepSignal,'b-+',t,timeBreachRepSignal,'g-.o',...
% %'LineWidth',2,'MarkerSize',10);
% set(gca,'FontSize',20)
% legend('Moonlight','Taliro','Breach','FontSize',20)


    Gvor.Nodes.NodeColors = spatialResult(1,:)';
    p = plot(Gvor,'r','XData',Gvor.Nodes.x,'YData',Gvor.Nodes.y)
    p.MarkerSize = 8;
    p.NodeCData = Gvor.Nodes.NodeColors;
    colorbar