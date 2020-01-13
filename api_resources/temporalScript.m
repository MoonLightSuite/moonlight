import eu.quanticol.moonlight.api.TestMoonLightScript

%[MISSING PART] temporalSys = aa.load("aaaa"); %[MISSING PART]

a = TestMoonLightScript; %This is the result of the previous MISSING PART 
b = a.selectDefaultTemporalComponent();   %Select Temporal monitor
monitor = b.getMonitor(java.lang.Object()); %There are no paramters

time = 1:1:10;
trajFunction = @(t) [t;t;t];
import eu.quanticol.moonlight.signal.RecordHandler
import eu.quanticol.moonlight.signal.DataHandler
dataHandler = javaArray('eu.quanticol.moonlight.signal.DataHandler',3);
dataHandler(1)=DataHandler.REAL;
dataHandler(2)=DataHandler.REAL;
dataHandler(3)=DataHandler.REAL;
factory = RecordHandler(dataHandler);

signal = toJavaSignal(time,trajFunction,factory);

results = monitor.monitor(signal);
results


%eval = temporalSys.Monitor(time, traj);


