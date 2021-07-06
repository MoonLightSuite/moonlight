function stop = outfunps(optimValues,state)
    stop = false;
    
     if (optimValues.bestfval < 0)
        stop = true;
    end
end