function graph = diagramToJavaObject(diagram,locations)
graphLength = length(diagram);
element = diagram(1);
graphWidth =  length(element{1}.Edges.Weight(1,:));
graph = javaArray('java.lang.Object',graphLength,locations,locations,graphWidth);
for i = 1:length(diagram)
    element = diagram(i);
    elementEdges = element{1}.Edges;
    for j = 1 : length(elementEdges.EndNodes)
        for l = 1:graphWidth
            endNodes = elementEdges.EndNodes(j,:);
            weight = elementEdges.Weight(j,:);
            graph(i,endNodes(1),endNodes(2),l) = java.lang.Double(weight(l));
        end
    end
end
end
