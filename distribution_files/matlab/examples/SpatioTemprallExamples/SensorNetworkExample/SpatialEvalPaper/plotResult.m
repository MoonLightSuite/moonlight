function plotResult(num_nodes_seq,node_results)
figure; % open a new figure window
r1 = zeros(1,length(num_nodes_seq));
r2 = zeros(1,length(num_nodes_seq));
r3 = zeros(1,length(num_nodes_seq));
r4 = zeros(1,length(num_nodes_seq));
for i = 1:length(num_nodes_seq)
    r1(i)= node_results(1,2,i);
    r2(i)= node_results(2,2,i);
    r3(i)= node_results(3,2,i);
    r4(i)= node_results(4,2,i);
end
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