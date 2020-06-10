import numpy as np
from pyDOE import lhs
#from simulation1Day import simulation, hypoGlicemia, hyperGlicemia, pidC1
from scipy.integrate import odeint


import matplotlib.pyplot as plt

from moonlight import *

# importing the model and the simulation function
from pancreasModel import modelPID, simulation

# setting the k_i parameters
Kd = [0, -0.0602, -0.0573, -0.06002, -0.0624]
Ki = [0, -3.53e-07, -3e-07, -1.17e-07, -7.55e-07]
Kp = [0, -6.17e-04, -6.39e-04, -6.76e-04, -5.42e-04]

pidC1 = lambda x, t: modelPID(x, t, Kp[1], Ki[1], Kd[1])
pidC2 = lambda x, t: modelPID(x, t, Kp[2], Ki[2], Kd[2])
pidC3 = lambda x, t: modelPID(x, t, Kp[3], Ki[3], Kd[3])
pidC4 = lambda x, t: modelPID(x, t, Kp[4], Ki[4], Kd[4])

pidC1Noise = lambda x, t: modelPID(x, t, Kp[1], Ki[1], Kd[1])

# defining the control parameters 
t_meal1 = np.random.normal(300, 60)
t_meal2 = np.random.normal(300, 60)
t_meal3 = 1440 - t_meal1 - t_meal2
dg1 = np.random.normal(40, 10)
dg2 = np.random.normal(90, 10)
dg3 = np.random.normal(60, 10)

t, y = simulation([t_meal1, t_meal2, t_meal3], [dg1, dg2, dg3], pidC3)
y = y[:,0];



# plot results
thresholdUp = 180;
thresholdDown = 70;

y_signal = [yy for yy in y]
plt.fill_between([t[0],t[-1]], [4,4],[max(y_signal),max(y_signal)],alpha=0.5)
plt.plot(t,y_signal,'r-',linewidth=2)
plt.axhline(y=thresholdUp, color='k', linestyle='-')
plt.axhline(y=thresholdDown, color='k', linestyle='-')
plt.xlabel('time')
plt.ylabel('y(t)')
plt.legend()
plt.xlabel('Time (min)')
plt.ylabel('BG (mmol/L)')
plt.show()

# defining the properties
script = """
signal {real y;}
domain minmax; 
formula hypoGlicemia = globally [0.0, 1400]  (y > 70);
formula hyperGlicemia = globally [0.0, 1400]  (y < 180);
"""
moonlight = Moonlight()
moonlight.set_script(script)


monitor = moonlight.get_monitor("hypoGlicemia")
y_signal = [[yy] for yy in y]
result = monitor.monitor(list(t),y_signal)
print(result)



#y_signal = [yy+ np.random.normal(0, 5) for yy in y]

