disp("### Moonlight Installation STARTED ###");
moonlightPath = fullfile(userpath,'moonlight.m');
moonlight = fopen(moonlightPath,'w');
if ispc()
    path = strrep(pwd,'\','\\');
else
    path = pwd;
end
fprintf(moonlight, strcat('setenv("MOONLIGHT_FOLDER","',path,'");\n'));
fprintf(moonlight, 'javaaddpath(fullfile(getenv("MOONLIGHT_FOLDER"),"moonlight","jar","moonlight.jar"));\n');
fprintf(moonlight, 'addpath(fullfile(getenv("MOONLIGHT_FOLDER"),"moonlight","matlab"));\n');
disp(strcat("Created startup file: ", moonlightPath));

startupPath = fullfile(userpath,'startup.m');
if(~isfile(fullfile(userpath,'startup.m')))
    startup = fopen(startupPath,'w');
    fprintf(startup,"moonlight;\n");
    disp(strcat("Created startup file: ", startupPath));
else
    content = fileread(fullfile(userpath,'startup.m'));
    if (~contains(content,"moonlight"))
        startup = fopen(fullfile(userpath,'startup.m'),'a');
        fprintf(startup,"moonlight\n");
        disp(strcat("Updated startup file: ", startupPath));
    end
end

setenv("MOONLIGHT_FOLDER",pwd)
javaaddpath(fullfile(getenv("MOONLIGHT_FOLDER"),"moonlight","jar","moonlight.jar"));
addpath(fullfile(getenv("MOONLIGHT_FOLDER"),"moonlight","matlab"));
disp("Environmental Variable Loaded");
disp("### Moonlight Installation ENDED ###");
