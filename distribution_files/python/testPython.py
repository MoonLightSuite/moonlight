import numpy as np
import matplotlib.pyplot as plt
time =list(np.arange(0,10,0.05))
f1,f2 = np.sin(time),np.cos(time)
space = list(zip(f1,f2))
plt.plot(time,f1)
plt.plot(time,f2)
