function plotResult(num_nodes_seq,node_results)
figure; % open a new figure window
plot(num_nodes_seq,r1,'LineWidth',2);
hold on;
plot(num_nodes_seq,r2,'LineWidth',2);
plot(num_nodes_seq,r3,'LineWidth',2);
plot(num_nodes_seq,r4,'LineWidth',2);
xlabel('number of nodes');
ylabel('time');
legend('P1','P2','P3','P4')
set(gca,'FontSize',18); 



% ============================================================

end