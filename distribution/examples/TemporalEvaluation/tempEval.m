function [resultTaliro,tElapsedTaliro, resultBreach1, tElapsedBreach] = tempEval(values,time,psi,psi_Pred,phiBreach)

tStart =tic;
resultTaliro = fw_taliro(psi,psi_Pred,values,time);
tElapsedTaliro = toc(tStart);

InitBreach
%trace
%trace = [time X]; % trace is in column format, first column is time
BrTrace = BreachTraceSystem({'X'}, [time values]);
%figure; BrTrace.PlotSignals();
BreachProp= STL_Formula('phi',phiBreach);
tStart =tic;
resultBreach= BrTrace.CheckSpec('phi');
tElapsedBreach = toc(tStart);
resultBreach1 = resultBreach(1);
end

