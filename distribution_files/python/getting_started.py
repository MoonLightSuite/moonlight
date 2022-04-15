from moonlight import *
import numpy as np
import matplotlib.pyplot as plt


## STEP 1: generate the  signal
time =list(np.arange(0,10,0.05))
f1,f2 = np.sin(time),np.cos(time)

plt.rcParams["figure.figsize"] = (15,10)
plt.plot(time,f1)
plt.plot(time,f2)

## STEP 2: describe and load the monitor

script = """
signal { real x; real y;}
domain boolean; 
formula future = globally [0, 0.2]  (x > y);
formula past = historically [0, 0.2]  (x > y);
"""
moonlightScript = ScriptLoader.loadFromText(script)

## STEP 3 (optional): change the domain on the fly
#moonlightScript.setMinMaxDomain();
#moonlightScript.setBooleanDomain();

## STEP 4: getting the monitor associated with a target formula
futureMonitor = moonlightScript.getMonitor("future");

## STEP 5: monitor the signal
signals = list(zip(f1,f2))
result = futureMonitor.monitor(time,signals)
arrayresults = np.array(futureMonitor.monitor(time,signals))

plt.rcParams["figure.figsize"] = (15,10)
plt.plot(time,f1)
plt.plot(time,f2)
plt.step(arrayresults[:,0],arrayresults[:,1])