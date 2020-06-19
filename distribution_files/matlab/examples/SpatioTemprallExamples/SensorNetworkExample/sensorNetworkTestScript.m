clear;
close all;
load('dataInput.mat');
script= [
"signal { int nodeType; real battery; real temperature; }",...
"space {edges { int hop; real dist; }}",...
"domain boolean;",... 
"formula P1 = ( nodeType==3 ) reach (hop)[0, 5] ( nodeType==1 ) ;"
];
moonlightScript = ScriptLoader.loadFromText(script);

boolSpTempMonitor = moonlightScript.getMonitor("P1");
%%%%% phi 1 %%%%%%
result = boolSpTempMonitor.monitor(spatialModelc,time,values);

