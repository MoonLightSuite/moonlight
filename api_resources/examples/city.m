clear
%GRAPH
from = [1,2,1,5,2];
to = [1,1,5,1,3];
weights = [2.0,2.0,2.0,2.0,1.0];
graph{1}= digraph(from,to,weights);
graph{2} = digraph(from,to,weights);
graph{3} = digraph(from,to,weights);
%SIGNAL
time = [0,1,2];
signal{1}=[[true,123];[true,123];[true,123]];
signal{2}=[[true,123];[true,123];[true,123]];
signal{3}=[[true,123];[true,123];[true,123]];
signal{4}=[[true,123];[true,123];[true,123]];
signal{5}=[[true,123];[true,123];[true,123]];
%MONITOR
monitor = MoonlightEngine;
monitor.Script = eu.quanticol.moonlight.api.CityMonitor;
result = monitor.spatioTemporalMonitor(graph,time,signal);



