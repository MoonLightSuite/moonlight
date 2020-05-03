function [time, output] = simSimulinkModel (model, input, solver, dt)

    simopt = simget(model);

    %Setting the Simulink simulation option.
    simopt = simset(simopt,'solver', solver, 'FixedStep', dt, 'SaveFormat','Array');

    [time, xt, output] = sim(model,[input(1,1,1) input(end,1,1)], simopt, input);

end


