function [boolean_results, robust_results] = monMoonlight (time, output, num_exp, omega, speed_threshold, T)


%Generate a monitor object from the script fine multiple_spec.mls 
%this object is an implementation of ScriptLoader class, please 
%refer to the doc of this class for more details (ex. write in console 
%"doc ScriptLoader" ) please, open multiple_spec.mls 
moonlightScript = ScriptLoader.load("multiple_spec");

moonlightScript.setBooleanDomain();
booleanMonitor1 = moonlightScript.getMonitor("Spec1");
booleanMonitor2 = moonlightScript.getMonitor("Spec2");
booleanMonitor3 = moonlightScript.getMonitor("Spec6");
booleanMonitor4 = moonlightScript.getMonitor("Spec7");

moonlightScript.setMinMaxDomain();
quantitativeMonitor1 = moonlightScript.getMonitor("Spec1");
quantitativeMonitor2 = moonlightScript.getMonitor("Spec2");
quantitativeMonitor3 = moonlightScript.getMonitor("Spec6");
quantitativeMonitor4 = moonlightScript.getMonitor("Spec7");

fprintf('Monitoring with Moonlight, omega=%f, speed_threshold=%f, T=%f\n', omega, speed_threshold, T);

boolean_results = zeros(4,2);
robust_results  = zeros(4,2);

for i=1:num_exp
    tElapsedSpec1MoonlightBoolean = 0;
    tStart                = tic;
    bMonitorResult1  = booleanMonitor1.monitor(time, output, omega); 
    tElapsedSpec1MoonlightBoolean   = tElapsedSpec1MoonlightBoolean + toc(tStart);
end

boolean_results(1,:) = [bMonitorResult1(1,2), tElapsedSpec1MoonlightBoolean/num_exp];


for i=1:num_exp
    tElapsedSpec1MoonlightRobust = 0;
    tStart                = tic;
    qMonitorResult1  = quantitativeMonitor1.monitor(time, output, omega); 
    tElapsedSpec1MoonlightRobust   = tElapsedSpec1MoonlightRobust + toc(tStart);
end

robust_results(1,:) = [qMonitorResult1(1,2), tElapsedSpec1MoonlightRobust/num_exp];

for i=1:num_exp
    tElapsedSpec2MoonlightBoolean = 0;
    tStart                        = tic;
    bMonitorResult2               = booleanMonitor2.monitor(time, output,[omega, speed_threshold]); 
    tElapsedSpec2MoonlightBoolean = tElapsedSpec2MoonlightBoolean + toc(tStart);
end


boolean_results(2,:) = [bMonitorResult2(1,2), tElapsedSpec2MoonlightBoolean/num_exp];

for i=1:num_exp
    tElapsedSpec2MoonlightRobust  = 0;
    tStart                        = tic;
    qMonitorResult2               = quantitativeMonitor2.monitor(time, output,[omega, speed_threshold]); 
    tElapsedSpec2MoonlightRobust  = tElapsedSpec2MoonlightRobust + toc(tStart);
end

robust_results(2,:) = [qMonitorResult2(1,2), tElapsedSpec2MoonlightRobust/num_exp];

for i=1:num_exp
    tElapsedSpec6MoonlightBoolean = 0;    
    tStart                        = tic;
    bMonitorResult6               = booleanMonitor3.monitor(time, output,[omega, speed_threshold, T]);
    tElapsedSpec6MoonlightBoolean = tElapsedSpec6MoonlightBoolean + toc(tStart);
end

boolean_results(3,:) = [bMonitorResult6(1,2), tElapsedSpec6MoonlightBoolean/num_exp];

for i=1:num_exp
    tElapsedSpec6MoonlightRobust   = 0;
    tStart                         = tic;
    qMonitorResult6                = quantitativeMonitor3.monitor(time, output,[omega, speed_threshold, T]); 
    tElapsedSpec6MoonlightRobust   = tElapsedSpec6MoonlightRobust + toc(tStart);
end

robust_results(3,:) = [qMonitorResult6(1,2), tElapsedSpec6MoonlightRobust/num_exp];

for i=1:num_exp
    tElapsedSpec7MoonlightBoolean = 0;
    tStart                        = tic;
    bMonitorResult7               = booleanMonitor4.monitor( time, output,[omega, speed_threshold, T]); 
    tElapsedSpec7MoonlightBoolean = tElapsedSpec7MoonlightBoolean + toc(tStart);
end

boolean_results(4,:) = [bMonitorResult7(1,2), tElapsedSpec7MoonlightBoolean/num_exp];

for i=1:num_exp
    tElapsedSpec7MoonlightRobust = 0;
    tStart                       = tic;
    qMonitorResult7              = quantitativeMonitor4.monitor(time, output,[omega, speed_threshold, T]); 
    tElapsedSpec7MoonlightRobust = tElapsedSpec7MoonlightRobust + toc(tStart);
end

robust_results(4,:) = [qMonitorResult7(1,2), tElapsedSpec7MoonlightRobust/num_exp];

end