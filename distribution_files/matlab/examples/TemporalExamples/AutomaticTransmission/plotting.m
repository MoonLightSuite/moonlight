function [] = plotting (input, output, input_labels, output_labels)

    figure
    time = input(:,1);
    
    num_inputs  = size(input,  2);
    num_outputs = size(output, 2);
    
    num_plots   = (num_inputs - 1) + num_outputs;
    
    for i=2:num_inputs
        subplot(num_plots,1,i-1);
        plot(time,input(:,i), 'color','red', 'LineWidth',3);
        xlabel(input_labels(1));
        ylabel(strcat('Input (',input_labels(i) ,')'));
    end
    
    for i=1:num_outputs 
        subplot(num_plots,1,num_inputs+i-1);
        plot(time,output(:,i), 'color','blue', 'LineWidth',3);
        xlabel(input_labels(1));
        ylabel(strcat('Output (',output_labels(i) ,')'));
    end

end