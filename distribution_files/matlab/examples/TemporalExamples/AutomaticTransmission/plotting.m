function [] = plotting (input, output)

    figure
    time = input(:,1);
    
    subplot(5,1,1);
    plot(time,input(:,2), 'color','red', 'LineWidth',3)
    xlabel('time')
    ylabel('Input (Throttle)')

    subplot(5,1,2);
    plot(time,input(:,3), 'color','red', 'LineWidth',3)
    xlabel('time')
    ylabel('Input (Brake)')

    subplot(5,1,3);
    plot(time,output(:,1), 'color','blue', 'LineWidth',3)
    xlabel('time')
    ylabel('Output (Vehicle Speed in mph)')

    subplot(5,1,4);
    plot(time,output(:,2), 'color','blue', 'LineWidth',3)
    xlabel('time')
    ylabel('Output (Engine Speed in rpm)')

    subplot(5,1,5);
    plot(time,output(:,3), 'color','blue', 'LineWidth',3)
    xlabel('time')
    ylabel('Output (Gear)')

end