function [resultTaliro,tElapsedTaliro, resultBreach1, tElapsedBreach] = tempEval(values,time,psi,psi_Pred,phiBreach)

tStart =tic;
resultTaliro = fw_taliro(psi,psi_Pred,values,time);
tElapsedTaliro = toc(tStart);

InitBreach
%trace
%trace = [time X]; % trace is in column format, first column is time
BrTrace = BreachTraceSystem({'X'}, [time values]);
%figure; BrTrace.PlotSignals();
BreachProp= STL_Formula('A',phiBreach);
tStart =tic;
[resultBreach, tau] =  STL_Eval(BrTrace.Sys, BreachProp, BrTrace.P, BrTrace.P.traj,'thom');
resultBreach1 = resultBreach(1);
tElapsedBreach = toc(tStart);
end

