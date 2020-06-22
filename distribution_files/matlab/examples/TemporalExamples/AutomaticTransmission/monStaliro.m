function [robust_results] = monStaliro (time, output, num_exp, omega, speed_threshold, T)


fprintf('Monitoring with S-Taliro, omega=%f, speed_threshold=%f, T=%f\n', omega, speed_threshold, T);

robust_results  = zeros(4,2);

% alw (e_speed[t] < 4000)

st_spec1 = '[] (a1)';

%a1: e_speed < omega

st_spec1_Pred(1).str = 'a1';
st_spec1_Pred(1).A = [0 1 0];
st_spec1_Pred(1).b = omega;

tElapsedSpec1Staliro  = 0;
for i=1:num_exp
    
    tStart                = tic;
    rob1                  = fw_taliro(st_spec1,st_spec1_Pred,output,time);
    tElapsedSpec1Staliro  = tElapsedSpec1Staliro + toc(tStart);
end

robust_results(1,:) = [rob1, tElapsedSpec1Staliro/num_exp];


% alw (( e_speed[t] < 3000 ) and (v_speed[t] < 120))

st_spec2 = '[] (a1 /\ a2)';

%a1: e_speed[t] < 3000

st_spec2_Pred(1).str = 'a1';
st_spec2_Pred(1).A = [0 1 0];
st_spec2_Pred(1).b = omega;

%a2: (v_speed[t] < 120)

st_spec2_Pred(2).str = 'a2';
st_spec2_Pred(2).A = [1 0 0];
st_spec2_Pred(2).b = speed_threshold;

tElapsedSpec2Staliro  = 0;
for i=1:num_exp
    tStart                = tic;
    rob2                  = fw_taliro(st_spec2,st_spec2_Pred,output,time);
    tElapsedSpec2Staliro  = tElapsedSpec2Staliro + toc(tStart);
end

robust_results(2,:) = [rob2, tElapsedSpec2Staliro/num_exp];

% not ((alw_[0,10] (v_speed[t] > 120))  and  alw (e_speed[t] < 3000 ))

st_spec6 = strcat('!([]_[0,',num2str(T),'] (a1) /\ [] (a2))');

%a1: v_speed[t] > speed_threshold

st_spec6_Pred(1).str = 'a1';
st_spec6_Pred(1).A = [-1 0 0];
st_spec6_Pred(1).b = -speed_threshold;

%a2: e_speed[t] < omega

st_spec6_Pred(2).str = 'a2';
st_spec6_Pred(2).A = [0 1 0];
st_spec6_Pred(2).b = omega;

tElapsedSpec6Staliro  = 0;
for i=1:num_exp
    tStart                = tic;
    rob6                  = fw_taliro(st_spec6,st_spec6_Pred,output,time);
    tElapsedSpec6Staliro  = tElapsedSpec6Staliro + toc(tStart);
end

robust_results(3,:) = [rob6, tElapsedSpec6Staliro/num_exp];

% ev_[0,10] ((v_speed[t] >= 120)) and  (alw (e_speed[t] < 3000))

st_spec7 = strcat('<>_[0,',num2str(T),'] ((a1)) /\ ([] (a2))');

%a1: v_speed[t] > speed_threshold

st_spec7_Pred(1).str = 'a1';
st_spec7_Pred(1).A = [-1 0 0];
st_spec7_Pred(1).b = -speed_threshold;

%a2: e_speed[t] < omega

st_spec7_Pred(2).str = 'a2';
st_spec7_Pred(2).A = [0 1 0];
st_spec7_Pred(2).b = omega;

tElapsedSpec7Staliro  = 0;
for i=1:num_exp
    tStart                = tic;
    rob7                  = fw_taliro(st_spec7,st_spec7_Pred,output,time);
    tElapsedSpec7Staliro  = tElapsedSpec7Staliro + toc(tStart);
end

robust_results(4,:) = [rob7, tElapsedSpec7Staliro/num_exp];


