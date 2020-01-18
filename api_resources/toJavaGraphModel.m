function graphModel = toJavaGraphModel(G)
    import eu.quanticol.moonlight.signal.GraphModel
    graphModel = GraphModel(size(G.Nodes,1));
    endNodes = G.Edges.EndNodes;
    weights = G.Edges.Weight;
    for i = 1:size(G.Edges,1)
        graphModel.add(endNodes(i,1)-1,weights(i)-1,endNodes(i,2)-1)
    end      
end

