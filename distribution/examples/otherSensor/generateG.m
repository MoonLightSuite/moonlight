function G = generateG(nodes, nodes_type, A)
%
if nargin<3
    A = zeros(10);  % mat di adiacenza
end
coord = cell2mat(nodes);
x = coord(1:2:length(coord));
y = coord(2:2:length(coord));
NodeTable = table(nodes_type',x',y', 'VariableNames',{'Type','x','y'});
G = digraph(A,NodeTable);
end

