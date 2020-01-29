t =num_nodes_seq;
plot(t,elapseTimeSeq,'b-+',t,elapseTimeSeq,'r--*',t,elapseTimeSeq,'g-.o',...
'LineWidth',2,'MarkerSize',10);
%plot(t,timeMoonRepSignal,'b-+',t,timeBreachRepSignal,'g-.o',...
%'LineWidth',2,'MarkerSize',10);
set(gca,'FontSize',20)
legend('phi1','phi2','phi3','FontSize',20)
