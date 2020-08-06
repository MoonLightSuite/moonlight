function [robust_results] = monBreach (time, output, num_exp, omega, speed_threshold, T)


fprintf('Monitoring with Breach, omega=%f, speed_threshold=%f, T=%f\n', omega, speed_threshold, T);

robust_results  = zeros(4,2);


trace = [time'; output']; % trace is in column format, first column is time
BrTrace = BreachTraceSystem({'v_speed','e_speed','gear'}, trace');
%figure; BrTrace.PlotSignals();

%figure; BrTrace.PlotRobustSat(strcat('alw (e_speed[t] < ',num2str(omega),')'),1);

tElapsedSpec1Breach = 0;
for i=1:num_exp
    tStart              = tic;
    spec1               = STL_Formula('Spec1', strcat('alw (e_speed[t] < ',num2str(omega), ')'));
    spec1_rob           = BrTrace.CheckSpec(spec1);
    tElapsedSpec1Breach = tElapsedSpec1Breach + toc(tStart);
end

robust_results(1,:) = [spec1_rob, tElapsedSpec1Breach/num_exp];

tElapsedSpec2Breach = 0;
for i=1:num_exp
    tStart              = tic;
    spec2               = STL_Formula('Spec2', strcat('alw (( e_speed[t] < ', num2str(omega), ') and (v_speed[t] < ', num2str(speed_threshold), '))'));
    spec2_rob           = BrTrace.CheckSpec(spec2);
    tElapsedSpec2Breach = tElapsedSpec2Breach + toc(tStart);
end 

robust_results(2,:) = [spec2_rob, tElapsedSpec2Breach/num_exp];

tElapsedSpec6Breach = 0;
for i=1:num_exp
    tStart              = tic;
    spec6               = STL_Formula('Spec6', strcat('not ((alw_[0,', num2str(T),'] (v_speed[t] >', num2str(speed_threshold), '))  and  alw (e_speed[t] < ', num2str(omega), ') )'));
    spec6_rob           = BrTrace.CheckSpec(spec6);
    tElapsedSpec6Breach = tElapsedSpec6Breach + toc(tStart);
end 

robust_results(3,:) = [spec6_rob, tElapsedSpec6Breach/num_exp];

tElapsedSpec7Breach = 0;
for i=1:num_exp
    
    tStart              = tic;
    %disp(strcat('(ev_[0,', num2str(T), '] (v_speed[t] >= ',num2str(speed_threshold),')) and  (alw (e_speed[t] < ',num2str(omega),'))'));
    spec7               = STL_Formula('Spec7', strcat('ev_[0,', num2str(T), '] ((v_speed[t] >= ',num2str(speed_threshold),')) and  (alw (e_speed[t] < ',num2str(omega),'))'));
    spec7_rob           = BrTrace.CheckSpec(spec7);
    tElapsedSpec7Breach = tElapsedSpec7Breach + toc(tStart);
end

robust_results(4,:) = [spec7_rob, tElapsedSpec7Breach/num_exp];


end