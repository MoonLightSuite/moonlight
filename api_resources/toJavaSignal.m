function signal = toJavaSignal(time, valueFunction)
    import eu.quanticol.moonlight.signal.Signal
    
    import eu.quanticol.moonlight.signal.RecordHandler
import eu.quanticol.moonlight.signal.DataHandler
dataHandler = javaArray('eu.quanticol.moonlight.signal.DataHandler',3);
dataHandler(1)=DataHandler.REAL;
dataHandler(2)=DataHandler.REAL;
dataHandler(3)=DataHandler.REAL;
factory = RecordHandler(dataHandler);

    signal = Signal;
    for i = 1:length(time)
         value = valueFunction(time(i));
         element = javaArray('java.lang.Object',length(value));
         for j = 1:length(value)
             element(j)=java.lang.Double(value(j));
         end
        signal.add(time(i),factory.fromObject(element));
    end
end