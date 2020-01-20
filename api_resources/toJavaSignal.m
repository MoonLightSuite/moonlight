function javaSignal = toJavaSignal(signal)
javaSignalLength = length(signal);
locationsLength = length(signal{1});
javaSignalWidth =  length(signal{1,1});
javaSignal = javaArray('java.lang.Object',javaSignalLength,locationsLength,javaSignalWidth);
for i = 1: length(signal)
    for j = 1:length(locationsLength)
        for k = 1:javaSignalWidth
            javaSignal(i,j,k)= java.lang.Double(signal{i,j}(k));
        end
    end
end
end
