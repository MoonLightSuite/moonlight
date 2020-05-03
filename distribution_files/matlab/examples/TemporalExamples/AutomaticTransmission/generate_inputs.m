function [picewise_throttle, piecewise_brake] = generate_inputs (sim, spoints)

       picewise_throttle = zeros(2,spoints+2);
       piecewise_brake    = zeros(2,spoints+2);
       
       time_throttle            = sort(random('unif', 0, sim, spoints, 1));
       value_throttle           = random(     'unif', 0,  95, spoints+1, 1);
       
       picewise_throttle(1,1)   = 0;
       picewise_throttle(1,end) = sim;
       
       picewise_throttle(2,1)   = 0;
        
       picewise_throttle(1,2:spoints+1) = time_throttle;
       picewise_throttle(2,2:spoints+2) = value_throttle; 
       
       
       time_brake               = sort(random('unif', 0, sim, spoints, 1))
       value_brake              = random(     'unif', 0,  40, spoints+1, 1);
       
       
       piecewise_brake(1,1)     = 0;
       piecewise_brake(1,end)   = sim;
       
       piecewise_brake(2,1)     = 0;
       
       
       piecewise_brake(1,2:spoints+1) = time_brake;
       piecewise_brake(2,2:spoints+2) = value_brake; 

end