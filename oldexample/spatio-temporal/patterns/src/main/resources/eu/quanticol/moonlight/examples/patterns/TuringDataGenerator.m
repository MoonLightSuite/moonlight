close all;

% patternSize
patternSize = 32;

% parameters :
PatternParameters.CA = 5.6;  % diffusion
PatternParameters.CB = 24.6; % diffusion
PatternParameters.dt = 0.01;
PatternParameters.T = 50;

[trajectory, counter] = TuringSimulation(patternSize, PatternParameters, false);

traj=trajectory(1:50:length(trajectory),:,:,:);
pippo = 2;


% %%%%% animation pattern formation
% for t=1:1:length(traj(:,1,1,1))
%     A(:,:)=traj(t,:,:,1);
%     surf(A)
%     view(2);
%     %contourf(X)
%     xlabel('X','Fontsize', 18);
%     ylabel('Y','Fontsize', 18);
%     zlabel('A','Fontsize', 18);
%     set(gca,'FontSize',18);
%     colormap jet
%     colorbar('FontSize',18);
%     axis([1 patternSize 1 patternSize]);
%     pause(0.01);
% end
  
Atraj=traj(:,:,:,1);
% Btraj=traj(:,:,:,2);
% 
% tend=length(Atraj);
% X=zeros(patternSize,patternSize);
% X(:,:)=Atraj(length(Atraj),:,:);
% surf(X)
% view(2);
% %contourf(X)
% xlabel('X','Fontsize', 18);
% ylabel('Y','Fontsize', 18);
% zlabel('A','Fontsize', 18)
% set(gca,'FontSize',18)
% colormap jet
% colorbar('FontSize',18);
% axis([1 patternSize 1 patternSize]);
% drawnow
% 
% %mkdir('allDataPattern')
% cd ../data
% save('traj.mat', 'traj');
% 
% cd allDataPattern
% for y=1:1:32     
%     for x=1:1:32
%         fname=sprintf('values_%d_%d_xA.dat',x,y);         
%         dlmwrite(fname,traj(:,x,y,1));
%         fname=sprintf('values_%d_%d_xB.dat',x,y);
%         dlmwrite(fname,traj(:,x,y,2));
%     end
% end
% time = 0:1:length(traj)-1;
% 
% dlmwrite('time.dat',time');
% cd ..
% cd ..
% cd matlab
% % 
