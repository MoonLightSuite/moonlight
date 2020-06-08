classdef ScriptLoader
    methods(Static)
        function  moonlightScript = load(filename)
            ScriptLoader.loadInner(filename);
            warning('off','all');
            moonlightScript=MoonlightScript(eval("moonlight.script.Script"+filename));
            warning('on','all');
        end
        function loadInner(filename)
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
    end
end