function [Output] = run_bench(Input)

simopt = simget('autotrans_mod04');
simopt = simset(simopt,'SaveFormat','Array');

[T, XT, YTtmp] = sim('autotrans_mod04',[Input(1,1,1) Input(end,1,1)],simopt, Input);

Output = zeros(size(Input,1), 6);
Output(:,1:3) = Input;
size(YTtmp)
Output(:,4:6) = YTtmp;

fileID = fopen('output.txt','w');



for i=1:size(Input,1)
    
    fprintf(fileID,'%f\t%f\t%f\t%f\t%f\t%d\n', Output(i,1), Output(i,2), Output(i,3), Output(i,4), Output(i,5), Output(i,6));
end

fclose(fileID);

end