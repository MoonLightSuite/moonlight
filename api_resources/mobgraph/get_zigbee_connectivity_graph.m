function [graph] = get_zigbee_connectivity_graph(npos, ntypes, radius_type)

graph = zeros(length(npos));

for i=1:length(npos)
    for j=1:length(npos)
        if (i~=j)
            %Mi calcolo la distanza euclidea di due nodi
            dist = sqrt((npos{i}(1)-npos{j}(1))^2 + (npos{i}(2)-npos{j}(2))^2);
            %Non e' ottimale dal punto di vista di performance
            %La connessione tra due nodi puo essere possibile se la
            %distanza euclidea tra due nodi ? maggiore della distanza
            %e almeno dei due nodi deve esser un coordinatore o un 
            %router.
            if ((radius_type(ntypes{i}) > dist) && (radius_type(ntypes{j}) > dist) && ((ntypes{i}<=2) || (ntypes{j}<=2))) 
                graph(i,j) = dist;
            else 
                graph(i,j) = 0;
            end
        end
    end
end



end