%tot="40";

res = sim('AFC_Online3', tot2);

rho_up = res.yout.signals(1).values;
rho_low = res.yout.signals(2).values;
