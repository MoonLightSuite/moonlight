setenv("MOONLIGHT_FOLDER",fullfile(pwd,"moonlight"))
javaaddpath(fullfile(getenv("MOONLIGHT_FOLDER"),"jar","moonlight.jar"));
addpath(fullfile(getenv("MOONLIGHT_FOLDER"),"matlab"));