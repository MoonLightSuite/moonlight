classdef ScriptLoader
    methods(Static)
        function  moonlightScript = loadFromFile(filename)
            ScriptLoader.loadInnerFromFile(filename);
            warning('off','all');
            moonlightScript=MoonlightScript(eval("moonlight.script.Script"+filename));
            warning('on','all');
        end
        function moonlightScript = loadFromText(script)
            fileName = strcat("moonlight",extractBefore(char(java.util.UUID.randomUUID),"-"));
            monlightScriptPath=fullfile(tempdir,"moonlight",fileName+".mls");
            monlightScriptFile = fopen(monlightScriptPath,'w');
            for line = script
                fprintf(monlightScriptFile, strcat(line,"\n"));
            end
            ScriptLoader.loadInnerFromText(fileName,monlightScriptPath);
            warning('off','all');
            moonlightScript=MoonlightScript(eval("moonlight.script.Script"+fileName));
            warning('on','all');
        end
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