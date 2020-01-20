addpath(genpath("."));
fileLocation = ".."+filesep+"output"+filesep;
myFiles = dir(fullfile(fileLocation,"*.jar"));
for k = 1:length(myFiles)
    javaaddpath(fileLocation+ myFiles(k).name)
end