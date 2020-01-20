clear
% Simulation of MANETs with ZigBee protocols
%
%
% Parameters
size            = 2000;
nodes_positions = [size 2];
numSteps        = 20;
num_nodes       = 10;

%The expected percentage of the nodes that are the routers
routers_ratio   = 0.3;

node_radius_type   = [500 400 250 ];
coordinator_radius = 1;
router_radius      = 2;
enddevice_radus    = 3;

num_routers   = 1;
routers{1}    = 1;
num_enddevice = 0;

const_random_waypoint  =  0;
const_random_walk      =  1;
const_gauss_markov     =  2;

mobility_model         =  const_random_waypoint;

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% PARAMETERS FOR RANDOM WAYPOINT AND RANDOM WALK     %     
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

T_p   = 10;
V_max = 20;


%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% PARAMETERS FOR RANDOM WAYPOINT AND RANDOM WALK     %     
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

alpha_speed = 0.5;
alpha_dir   = 0.1;

mean_speed  = 1;
sigma_speed = 10;


  

% Make figure with axes.
figure; 
subplot(1,3,1);
axis square; hold on;
set(gca,'XLim',[0 size], 'YLim', [0 size]);
title('Voronoi Diagram (Proximity)');

subplot(1,3,2);
axis square; hold on;
set(gca,'XLim',[0 size], 'YLim', [0 size]);
title('General Connectivity Graph');

subplot(1,3,3);
axis square; hold on;
set(gca,'XLim',[0 size], 'YLim', [0 size]);
title('Zigbee Role-based Connectivity Graph');


% MANETs nodes initialisation 
% 
% Type 1 -- Coordinator, Type 2 -- Routers, Type 3 -- EndDevice
%
% 
types_distribution = rand(num_nodes,1);

for i=1:num_nodes
    
    
    nodes{i}         = random('Uniform',500,size-500,1,2);
    
    if (i == 1)
        nodes_type{i}    = 1; %There is always only one coordinator
    else 
        if (types_distribution(i) <= routers_ratio)
            nodes_type{i}          = 2;
            num_routers             = num_routers + 1;
            routers{num_routers}    = i;
        else 
            nodes_type{i}           = 3;
            num_enddevice           = num_enddevice + 1;
            enddevice{num_enddevice}  = i;
        end
    end
    nodes_speed{i}        = normrnd(mean_speed,sigma_speed);
    nodes_dir_avg{i}      = random('Uniform',0,2*pi);
    nodes_dir{i}          = random('Uniform',0,2*pi);
    
    nodes_destination {i} = [rand * size * 2/3  rand * size * 2/3 ];
    nodes_wdest_times {i} = T_p * rand;
    
end


 
    G = generateG(nodes, nodes_type);     
    node = nodes{i};
    subplot(1,3,1);
    plot( G,'XData',G.Nodes.x,'YData',G.Nodes.y );
    subplot(1,3,2);
    plot( G,'XData',G.Nodes.x,'YData',G.Nodes.y );
    subplot(1,3,3);
    plot( G,'XData',G.Nodes.x,'YData',G.Nodes.y);
 
vorSpTemModel = cell(numSteps,1);
conSpTemModel = cell(numSteps,1);
conZigSpTemModel = cell(numSteps,1);
for frameNr = 1 : numSteps
    
    %Clear
    subplot(1,3,1);
	cla;
    subplot(1,3,2);
    cla;
    subplot(1,3,3);
    cla;
    
    for i=1:num_nodes
        
        if (mobility_model == const_random_waypoint)
            node  = nodes{i};
            dest  = nodes_destination{i};
            
            dist  = sqrt ((dest(1) - node(1))^2 + (dest(2) - node(2))^2);
            speed = V_max * rand;
            if (dist < speed)
                nodes_wdest_times {i} = nodes_wdest_times {i} - 1;
                if (nodes_wdest_times {i} < 0)
                   nodes_destination {i} = [rand * size * 2/3 rand * size * 2/3 ];
                   nodes_wdest_times {i} = T_p * rand;
                end
                x = node(1);
                y = node(2);
                nodes_positions(i,:) = [x y];
                
            else
                
                x = node(1) + speed * ((dest(1) - node(1))/dist);
                y = node(2) + speed * ((dest(2) - node(2))/dist);
                
                nodes{i} = [x, y];
                nodes_positions(i,:) = [x y];
            end
        
        elseif (mobility_model == const_random_walk)
                speed = V_max * rand;
                node  = nodes{i};
                dir   = rand * 2 * pi;
                x = node(1) + speed * cos(dir);
                y = node(2) + speed * sin(dir);
                nodes{i} = [x, y];
                nodes_positions(i,:) = [x  y];
                
        elseif (mobility_model == const_gauss_markov)
                nodes_speed{i} = alpha_speed * nodes_speed{i} + (1 - alpha_speed) * mean_speed       + sqrt(1 - alpha_speed^2) * sigma_speed * normrnd(0,1);
                nodes_dir{i}   = alpha_speed * nodes_dir{i}   + (1 - alpha_speed) * nodes_dir_avg{i} + sqrt(1 - alpha_speed^2) * normrnd(0,1);
                node = nodes{i};
                x = node(1) + nodes_speed{i} * cos(nodes_dir{i});
                y = node(2) + nodes_speed{i} * sin(nodes_dir{i});
                nodes{i} = [x, y];
                nodes_positions(i,:) = [x  y];
        end
%         
%         
%          G1 = generateG(nodes, nodes_type);     
%          node = nodes{i};
%          subplot(1,3,1);
%          plot( G1, 'o','XData',G.Nodes.x,'YData',G.Nodes.y );
%          subplot(1,3,2);
%          plot( G1,'XData',G.Nodes.x,'YData',G.Nodes.y );
%          subplot(1,3,3);
%          plot( G1,'XData',G.Nodes.x,'YData',G.Nodes.y);
        
        if (nodes_type{i}    == 1)
                subplot(1,3,1);
                circle(x,y, node_radius_type(coordinator_radius), 'g');
                subplot(1,3,2);
                circle(x,y, node_radius_type(coordinator_radius), 'g');
                subplot(1,3,3);
                circle(x,y, node_radius_type(coordinator_radius), 'g');
                
        else
            if (nodes_type{i} == 2)
                subplot(1,3,1);
                circle(x,y, node_radius_type(router_radius), 'r');
                subplot(1,3,2);
                circle(x,y, node_radius_type(router_radius), 'r');
                subplot(1,3,3);
                circle(x,y, node_radius_type(router_radius), 'r');
            else 
                subplot(1,3,1);
                circle(x,y, node_radius_type(enddevice_radus), 'k');
                subplot(1,3,2);
                circle(x,y, node_radius_type(enddevice_radus), 'k');
                subplot(1,3,3);
                circle(x,y, node_radius_type(enddevice_radus), 'k');
            end
        end
    end
    G = generateG(nodes, nodes_type);  
    subplot(1,3,1);
    voronoi(nodes_positions(:,1),nodes_positions(:,2));
    [v,c] = voronoin(nodes_positions);   
    vgraph = get_voronoi_graph(v,c);
    [row,col] = find(vgraph==1);
    Gvor = addedge(G,row,col,1);
    plot(Gvor,'r','XData',G.Nodes.x,'YData',G.Nodes.y)
    
    subplot(1,3,2);
    cgraph1 = get_connectivity_graph(nodes, nodes_type, node_radius_type);
    Gcon = generateG(nodes, nodes_type,cgraph1);
    plot(Gcon,'r','XData',G.Nodes.x,'YData',G.Nodes.y)
    
    
    subplot(1,3,3);
    cgraph2 = get_zigbee_connectivity_graph(nodes, nodes_type, node_radius_type);
    GconZig = generateG(nodes, nodes_type,cgraph2);
    plot(GconZig,'r','XData',G.Nodes.x,'YData',G.Nodes.y)
    
%  p = plot(G,'XData',G.Nodes.x,'YData',G.Nodes.y);
%  p.Marker = 's';
%  p.NodeColor = 'r';
%  p.MarkerSize = 7;
%     
   file_name = sprintf('trace/frame%d.mat',frameNr);
   save(file_name,'Gvor','Gcon','GconZig');

    
    % Get the frame for the animation.
	frames(frameNr) = getframe;
    
    vorSpTemModel{frameNr} = Gvor;
    conSpTemModel{frameNr} =  Gcon;
    conZigSpTemModel {frameNr} = GconZig;
end
    
save('spTempModels','vorSpTemModel','conSpTemModel','conZigSpTemModel');



