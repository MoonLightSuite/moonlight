time = 0:0.1:1000;
a = time;
b = cos(time);
trace = [time' a' b'];
stringTrace = {'a','b'};
stringFormulaName = 'phi';
stringFormula = 'alw_[0,500] (a[t]>=4)';
robEval(stringTrace, trace,stringFormulaName,stringFormula);