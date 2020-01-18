clear
myFiles = dir(fullfile("..\output\","*.jar"));
for k = 1:length(myFiles)
    javarmpath("..\output\"+ myFiles(k).name)
end