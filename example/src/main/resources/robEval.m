function [robBreach, tau, compTime] =robEval(stringTrace, trace,stringFormulaName, stringFormula)
InitBreach
BrTrace = BreachTraceSystem(stringTrace, trace);
BreachProp=STL_Formula(stringFormulaName, stringFormula);
tic
[robBreach, tau] = STL_Eval(BrTrace.Sys, BreachProp, BrTrace.P, BrTrace.P.traj,'thom');
compTime=toc;
end
