
clear
% Simulation of MANETs with ZigBee protocols
%
%
% Parameters
%
size            = 2000;
nodes_positions = [size 2];
numSteps        = 10;
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
% MANETs nodes initialisation
%
% Type 1 -- Coordinator, Type 2 -- Routers, Type 3 -- EndDevice
%
%
types_distribution = rand(num_nodes,1);

for i=1:num_nodes

    nodes{1,i}         = random('Uniform',500,size-500,1,2);

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

    node = nodes{i};
end

for frameNr = 1 : numSteps
    for i=1:num_nodes

        if (mobility_model == const_random_waypoint)
            node  = nodes{frameNr,i};
            dest  = nodes_destination{i};
            disp(node)
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

                nodes{frameNr+1,i} = [x, y];
                nodes_positions(i,:) = [x y];
            end

        elseif (mobility_model == const_random_walk)
                speed = V_max * rand;
                node  = nodes{frameNr,i};
                dir   = rand * 2 * pi;
                x = node(1) + speed * cos(dir);
                y = node(2) + speed * sin(dir);
                nodes{frameNr,i} = [x, y];
                nodes_positions(i,:) = [x  y];

        elseif (mobility_model == const_gauss_markov)
                nodes_speed{i} = alpha_speed * nodes_speed{i} + (1 - alpha_speed) * mean_speed       + sqrt(1 - alpha_speed^2) * sigma_speed * normrnd(0,1);
                nodes_dir{i}   = alpha_speed * nodes_dir{i}   + (1 - alpha_speed) * nodes_dir_avg{i} + sqrt(1 - alpha_speed^2) * normrnd(0,1);
                node = nodes{frameNr,i};
                x = node(1) + nodes_speed{i} * cos(nodes_dir{i});
                y = node(2) + nodes_speed{i} * sin(nodes_dir{i});
                nodes{frameNr+1,i} = [x, y];
                nodes_positions(i,:) = [x  y];


        end
    midNode{i}=nodes{frameNr,i};
    end
   cgraph1{frameNr} = get_connectivity_graph(midNode, nodes_type, node_radius_type);
   cgraph2{frameNr} = get_zigbee_connectivity_graph(midNode, nodes_type, node_radius_type);
    %voronoi(nodes_positions(:,1),nodes_positions(:,2));

    [v,c] = voronoin(nodes_positions);
    vgraph = get_voronoi_graph(v,c);
    [row,col] = find(vgraph==1);



   % file_name = sprintf('trace/frame%d.mat',frameNr);
   % save(file_name,'vgraph','cgraph1','cgraph2');
end





