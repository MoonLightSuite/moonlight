function [y] = piecewise (x, piecewise)
 

     xs = piecewise(1,:);
     ys = piecewise(2,:);
     
     for xi=2:size(xs,2)
             if x < xs(xi)
                break; 
             end
     end
     x1 = xs(xi-1);
     x2 = xs(xi);
     y1 = ys(xi-1);
     y2 = ys(xi);
     if (y1 == y2)
         y = y1;
     else
         y = y1 + ((y2 - y1) / (x2 - x1)) * (x -x1);
     end
end