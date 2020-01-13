function signal = toJavaSignal(time, valueFunction,factory)
    import eu.quanticol.moonlight.signal.Signal
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