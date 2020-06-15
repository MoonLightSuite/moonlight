clear;
close all;
%generation of the data
numSteps        = 1;
num_nodes       = 10;
framePlot = false; % to enable or disable the plot of the graph
[spatialModel,time,signal]= sensorModel(num_nodes,numSteps, framePlot);

numframe = length(spatialModel);
plotGraph(spatialModel, numframe , 'node');

%%%%%% monitor  %%%%
% loading of the script
moonlightScript = ScriptLoader.loadFromFile("test");
%moonlightScript.setMinMaxDomain();
moonlightScript.setBooleanDomain();
%creating the Boolean monitor for formula 
% MyFirstFormula = ( nodeType==3 ) reach (hop)[0, 1] ( nodeType==1 );
% SecondFormula = ( nodeType==3 ) || {( nodeType==3 ) reach (hop)[0, 2] ( nodeType==2 )};
boolSpTempMonitor = moonlightScript.getMonitor("MyFirstFormula");
%%%%% phi 1 %%%%%%
result1 = boolSpTempMonitor.monitor(spatialModel,time,signal);


time=result1(1,:,1);
signalResult1= result1(:,:,2);



figure1 = figure('Colormap',...
    [1 0 0;1 0.03125 0.03125;1 0.0625 0.0625;1 0.09375 0.09375;1 0.125 0.125;1 0.15625 0.15625;1 0.1875 0.1875;1 0.21875 0.21875;1 0.25 0.25;1 0.28125 0.28125;1 0.3125 0.3125;1 0.34375 0.34375;1 0.375 0.375;1 0.40625 0.40625;1 0.4375 0.4375;1 0.46875 0.46875;1 0.5 0.5;1 0.53125 0.53125;1 0.5625 0.5625;1 0.59375 0.59375;1 0.625 0.625;1 0.65625 0.65625;1 0.6875 0.6875;1 0.71875 0.71875;1 0.75 0.75;1 0.78125 0.78125;1 0.8125 0.8125;1 0.84375 0.84375;1 0.875 0.875;1 0.90625 0.90625;1 0.9375 0.9375;1 0.96875 0.96875;1 1 1;0.967741906642914 0.982289671897888 0.991650879383087;0.935483872890472 0.964579403400421 0.98330169916153;0.903225779533386 0.946869075298309 0.974952578544617;0.870967745780945 0.929158747196198 0.966603398323059;0.838709652423859 0.91144847869873 0.958254277706146;0.806451618671417 0.893738150596619 0.949905097484589;0.774193525314331 0.876027822494507 0.941555976867676;0.74193549156189 0.858317494392395 0.933206856250763;0.709677398204803 0.840607225894928 0.924857676029205;0.677419364452362 0.822896897792816 0.916508555412292;0.645161271095276 0.805186569690704 0.908159375190735;0.612903237342834 0.787476301193237 0.899810254573822;0.580645143985748 0.769765973091125 0.891461133956909;0.548387110233307 0.752055644989014 0.883111953735352;0.516129016876221 0.734345376491547 0.874762833118439;0.483870953321457 0.716635048389435 0.866413652896881;0.451612889766693 0.698924720287323 0.858064532279968;0.419354826211929 0.681214451789856 0.849715352058411;0.387096762657166 0.663504123687744 0.841366231441498;0.354838699102402 0.645793795585632 0.833017110824585;0.322580635547638 0.628083467483521 0.824667930603027;0.290322571992874 0.610373198986053 0.816318809986115;0.25806450843811 0.592662870883942 0.807969629764557;0.225806444883347 0.57495254278183 0.799620509147644;0.193548381328583 0.557242274284363 0.791271388530731;0.161290317773819 0.539531946182251 0.782922208309174;0.129032254219055 0.521821618080139 0.774573087692261;0.0967741906642914 0.504111349582672 0.766223907470703;0.0645161271095276 0.48640102148056 0.75787478685379;0.0322580635547638 0.468690693378448 0.749525606632233;0 0.450980395078659 0.74117648601532]);
Gvor = spatialModel{length(spatialModel)};
A = adjacency(Gvor);
G = graph(A);
Gvor.Nodes.results = signalResult1(:,1);
p = plot(G,'r','XData',Gvor.Nodes.x,'YData',Gvor.Nodes.y);
p.NodeCData = Gvor.Nodes.results;
p.EdgeColor = 'black';
p.MarkerSize = 10;
set(gca,'FontSize',18);    
colorbar('FontSize',18);
caxis([0, 1])
box on;
