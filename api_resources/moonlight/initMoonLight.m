myFiles = dir(fullfile("..\output\","*.jar"));
for k = 1:length(myFiles)
    javaaddpath("..\output\"+ myFiles(k).name)
end