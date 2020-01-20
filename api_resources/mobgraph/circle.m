function h = circle(x,y,r,c)

d = r*2;

px = x-r;

py = y-r;

h = rectangle('Position',[px py d d],'Curvature',[1,1], 'LineStyle', '-.', 'EdgeColor',c);

daspect([1,1,1])
end
