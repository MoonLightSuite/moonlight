 function [c,ceq] = my_constrfunc(x, model, solver, dt)
        
        rob = my_objective(x, model, solver, dt);
        c = rob;
        ceq = [];
    end