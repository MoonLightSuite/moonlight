function javaSignal = toJavaSignal(signal)
javaLocationsLength = length(signal);
s = size(signal{1});
javaTimeLength= s(1);
javaSignalWidth= s(2);
javaSignal = javaArray('java.lang.Object',javaLocationsLength,javaTimeLength,javaSignalWidth);
for i = 1: javaLocationsLength
    for j = 1:javaTimeLength
        for k = 1:javaSignalWidth
            javaSignal(i,j,k)= java.lang.Double(signal{i}(j,k));
        end
    end
end
end
