function[SpTemModel, time, signal] = TuringDataGeneretor(patternSize,plotFrames)

    % patternSize
    %patternSize = 32;

    % parameters :
    PatternParameters.CA = 5.6;  % diffusion
    PatternParameters.CB = 24.6; % diffusion
    PatternParameters.dt = 0.01;
    PatternParameters.T = 50;

    [trajectory, counter] = TuringSimulation(patternSize, PatternParameters, false);

    traj=trajectory(1:50:length(trajectory),:,:,:);


    %%%%% animation pattern formation
    time=1:1:length(traj(:,1,1,1));
    if plotFrames      
        for t=time
            A(:,:)=traj(t,:,:,1);
            surf(A)
            view(2);
            %contourf(X)
            xlabel('X','Fontsize', 18);
            ylabel('Y','Fontsize', 18);
            zlabel('A','Fontsize', 18);
            set(gca,'FontSize',18);
            colormap jet
            colorbar('FontSize',18);
            axis([1 patternSize 1 patternSize]);
            pause(0.02);
        end
    end

    % Atraj=traj(:,:,:,1);
    % Btraj=traj(:,:,:,2);
    %  
    % tend=length(Atraj);
    % X=zeros(patternSize,patternSize);
    % X(:,:)=Atraj(length(Atraj),:,:);
    % surf(X)
    % view(2);
    % contourf(X)
    % xlabel('X','Fontsize', 18);
    % ylabel('Y','Fontsize', 18);
    % zlabel('A','Fontsize', 18)
    % set(gca,'FontSize',18)
    % colormap jet
    % colorbar('FontSize',18);
    % axis([1 patternSize 1 patternSize]);
    % drawnow

    num_nodes = patternSize *patternSize;
    signal = cell(num_nodes,1);
    SpTemModel = cell(length(time),1);
    for t=time
        A(:,:)=traj(t,:,:,1);
        B(:,:)=traj(t,:,:,2);
        Av = [];
        Bv = [];
        for i=1:1:32
            for j=1:1:32
                Av = [Av; A(i,j)];
                Bv = [Bv; B(i,j)];
             end
        end
        distFunc = 'cityblock';
        [X Y] = meshgrid(1:patternSize,1:patternSize);
        X = X(:); 
        Y = Y(:);
        adj = squareform( pdist([X Y], distFunc) == 1 );      
        G = digraph(adj);
        G.Nodes.A = Av;
        G.Nodes.B = Bv;
        numEdges = height(G.Edges);
        G.Edges.Weights= ones(numEdges,1);
        SpTemModel{t} = G;
        for  i=1:num_nodes
            signal{i}=[signal{i}; Av(i), Bv(i)];
        end
    end
    end
    