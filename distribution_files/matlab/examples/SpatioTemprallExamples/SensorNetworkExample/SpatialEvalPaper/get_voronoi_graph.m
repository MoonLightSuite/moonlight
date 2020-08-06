function [graph] = get_voronoi_graph(v,c)

graph = zeros(length(c));

for i=1:length(c)
    for j=1:length(c)
        if (i~=j)
            edge_set = intersect(c{i},c{j});
            if (~isempty(edge_set))
                if (size(edge_set) == 1)  
                    if (edge_set(1) == 1)
                        graph(i,j) = 2;
                    end
                else 
                    graph(i,j) = 1;
                end
            else
                graph(i,j) = Inf;
            end
        end
    end
end



end