clear;
close all;
load('dataInput.mat');
script= [
"signal { int nodeType; real battery; real temperature; }",...
"space {edges { int hop; real dist; }}",...
"domain boolean;",... 
"formula MyFirstFormula = ( nodeType==3 ) reach (hop)[0, 1] ( nodeType==2 ) ;"
];
moonlightScript = ScriptLoader.loadFromText(script);

boolSpTempMonitor = moonlightScript.getMonitor("MyFirstFormula");
%%%%% phi 1 %%%%%%
result = boolSpTempMonitor.monitor(spatialModel,time,values);

