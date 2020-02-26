function [graph] = get_connectivity_graph(npos, ntypes, radius_type)

graph = zeros(length(npos));

for i=1:length(npos)
    for j=1:length(npos)
        if (i~=j)
            %Mi calcolo la distanza euclidea di due nodi
            dist = sqrt((npos{i}(1)-npos{j}(1))^2 + (npos{i}(2)-npos{j}(2))^2);
            %Non e' ottimale
            if ((radius_type(ntypes{i}) > dist) && (radius_type(ntypes{j}) > dist))
                graph(i,j) = dist;
            else 
                graph(i,j) = Inf;
            end
        end
    end
end



end