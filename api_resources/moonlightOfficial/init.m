setenv("MOONLIGHT_FOLDER",fullfile(pwd,"moonlight"))
javaaddpath(fullfile(getenv("MOONLIGHT_FOLDER"),"jar","console-1.0-SNAPSHOT.jar"));
addpath(fullfile(getenv("MOONLIGHT_FOLDER"),"matlab"));