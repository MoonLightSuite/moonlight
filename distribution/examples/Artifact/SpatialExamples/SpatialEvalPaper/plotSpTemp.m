t =num_nodes_seq;
plot(t,ReachBoolElapseTimeSeq,'b-+',t,phi3,'r--*',t,EscapeBool_elapseTime,'g-.o',...
'LineWidth',2,'MarkerSize',10);
%plot(t,timeMoonRepSignal,'b-+',t,timeBreachRepSignal,'g-.o',...
%'LineWidth',2,'MarkerSize',10);
set(gca,'FontSize',20)
legend('phi1','phi2','phi3','FontSize',20)
xlabel('number of nodes','Fontsize', 20);
ylabel('computational time (sec)','Fontsize',20);