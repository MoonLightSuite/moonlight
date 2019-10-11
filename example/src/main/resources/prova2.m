time = 0:1:200;
X = time';
T = time';
InitBreach
trace = [T' X'];
BrTrace = BreachTraceSystem({'a','b'}, trace);
BreachProp=STL_Formula('phi','alw_[0,500] (a[t]>=4)');
rob=BrTrace.CheckSpec('phi');
midBreach = @(X,T) STL_Eval(BrTrace.Sys, BreachProp, BrTrace.P, BrTrace.P.traj,'thom');
breach = @(X,T) midBreach(X,T)(0)