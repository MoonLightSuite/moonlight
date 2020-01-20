clear
fileLocation = ".."+filesep+"output"+filesep;
myFiles = dir(fullfile(fileLocation,"*.jar"));
for k = 1:length(myFiles)
    javarmpath(fileLocation+ myFiles(k).name)
end