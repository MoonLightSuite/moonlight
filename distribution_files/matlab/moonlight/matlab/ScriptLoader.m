classdef ScriptLoader
    % This class contains methods to load the MoonLight scripts  
    methods(Static)
        function  moonlightScript = loadFromFile(filename)
            % loadFromFile  load a moonLightScript from file.
            %   loadFromFile(filename) load a script from a file named
            %   filename
            % ScriptLoader.loadInnerFromFile(filename);
            warning('off','all');
            % moonlightScript=MoonlightScript(eval("moonlight.script.Script"+filename));
            moonlightScript=MoonlightScript(eu.quanticol.moonlight.script.ScriptLoader.loadFromFile(which(filename)));
            warning('on','all');
        end
        function moonlightScript = loadFromText(stringArray)
            % loadFromText  load a moonLightScript from a stringArray.
            %   loadFromText(stringArray) load a script from a script saved
            %   as a stringArray as follows:
            %
            %   stringArray = [
            %   "signal { real x; real y}",...
            %   "domain minmax;",... 
            %   "formula future = globally [0, 0.2]  (x > y);"...
            %   "formula past = historically [0, 0.2]  (x > y);"
            %   ];
            moonlightScript=MoonlightScript(eu.quanticol.moonlight.script.ScriptLoader.loadFromCode(strjoin(stringArray,'\n')));

            % fileName = strcat("moonlight",extractBefore(char(java.util.UUID.randomUUID),"-"));
            % monlightScriptPath=fullfile(tempdir,"moonlight",fileName+".mls");
            % monlightScriptFile = fopen(monlightScriptPath,'w');
            %for line = stringArray
            %    fprintf(monlightScriptFile, strcat(line,"\n"));
            %end
            %ScriptLoader.loadInnerFromText(fileName,monlightScriptPath);
            %warning('off','all');
            %moonlightScript=MoonlightScript(eval("moonlight.script.Script"+fileName));
            %warning('on','all');
        end
    end
    methods(Static, Access = private)
        function loadInnerFromFile(filename)
            % class static constructor
            [status, out] = system("java -jar "+fullfile(getenv("MOONLIGHT_FOLDER"),"moonlight","jar","moonlight.jar "+filename+".mls "+tempdir));
            if(status~=0)
                throw(MException("","PARSER OF THE SCRIPT FAILED "+out))
            end
            [status, out] = system("jar -cvf "+fullfile(getenv("MOONLIGHT_FOLDER"),"moonlight", "script",filename+".jar")+" -C "+tempdir+" "+fullfile("moonlight","script","Script"+filename+".class"));
            if(status~=0)
                throw(MException("","CREATION OF THE JAR FAILED \n"+out))
            end
            warning('off','all');
            javaaddpath(fullfile(getenv("MOONLIGHT_FOLDER"),"moonlight", "script",filename+".jar"));
            warning('on','all');
        end
         function loadInnerFromText(filename,path)
            % class static constructor
            [status, out] = system("java -jar "+fullfile(getenv("MOONLIGHT_FOLDER"),"moonlight","jar","moonlight.jar "+path+" "+tempdir));
            if(status~=0)
                throw(MException("","PARSER OF THE SCRIPT FAILED "+out))
            end
            [status, out] = system("jar -cvf "+fullfile(getenv("MOONLIGHT_FOLDER"),"moonlight", "script",filename+".jar")+" -C "+tempdir+" "+fullfile("moonlight","script","Script"+filename+".class"));
            if(status~=0)
                throw(MException("","CREATION OF THE JAR FAILED \n"+out))
            end
            warning('off','all');
            javaaddpath(fullfile(getenv("MOONLIGHT_FOLDER"),"moonlight", "script",filename+".jar"));
            warning('on','all');
        end
    end
end