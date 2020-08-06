figure; % open a new figure window
plot(N,moonlight_sat_time_spec1,'r','LineWidth',2);
hold on;
plot(N,moonlight_rob_time_spec1,'r--','LineWidth',2);
plot(N,moonlight_sat_time_spec2,'b','LineWidth',2);
plot(N,moonlight_rob_time_spec2,'b--','LineWidth',2);
plot(N,moonlight_sat_time_spec3,'g','LineWidth',2);
plot(N,moonlight_rob_time_spec3,'g--','LineWidth',2);
% plot(num_nodes_seq,moonlight_sat_time_spec4,'c','LineWidth',2);
% plot(num_nodes_seq,moonlight_rob_time_spec4,'c--','LineWidth',2);
% plot(num_nodes_seq,moonlight_sat_time_spec5,'m','LineWidth',2);
% plot(num_nodes_seq,moonlight_rob_time_spec5,'m--','LineWidth',2);
 xlabel('number of nodes');
ylabel('time');
legend('P1','P1q','P2','P2q','P3','P3q')
%legend('P1','P1q','P2','P2q','P3','P3q','P4','P4q','P5','P5q')
set(gca,'FontSize',18); 


