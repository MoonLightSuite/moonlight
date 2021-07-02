function stop = outfun(x,optimValues,state)
    stop = false;
    
     if (optimValues.fval < 0)
        stop = true;
    end
end