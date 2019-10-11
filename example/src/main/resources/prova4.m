time = 0:0.1:1000;
X = time;
T = time;
trace = @(X,T)[T' X'];
stringTrace = {'X'};
stringFormulaName = 'phi';
stringFormula = 'ev_[926,934]((X[t]>=-30) and (X[t]<=30))';
robBreach = @(X,T) robEval(stringTrace, trace(X,T),stringFormulaName,stringFormula);