function [imgTime, counter] = TuringSimulation( systemSize, parameters, plotit)



% load('inputA.mat');
% %load('inputAbad2.mat');
% A = inputA;
% load('inputB.mat');
% B = inputB;

    A = rand(systemSize)*16;   
    B = rand(systemSize)*16;
    
%     % testing:
%     M = load('/Users/ebru/Desktop/TuringDataSet/JavaOptimization/test1/THREAD10_2_initialState.txt');
%     M = reshape(M, 2,1024);
%     A = reshape(M(1,:), systemSize, systemSize);
%     B = reshape(M(2,:), systemSize, systemSize);
    
    
    Ap = zeros(systemSize);
    Bp = zeros(systemSize);
    
    
    dt = parameters.dt;
    T = parameters.T;
       
    kernel = zeros(3);
    kernel(1,2) = 1;
    kernel(2,1) = 1;
    kernel(2,3) = 1;
    kernel(3,2) = 1;
    kernel = kernel/4;
    kernel(2,2) = -1;
    
    
    dx = 0.00001;
    if(plotit)
        figure;
        hold on;
    end
    counter = 0;
    maxA = 0;
    img = zeros(systemSize, systemSize, 3);
    imgTime = zeros(T/dt,systemSize, systemSize,2);
    
    fa = 0;
    fb = 0;
    sz = systemSize*systemSize;
    
    bfSize = 50;
    faBuffer = ones(10,1);
    bfIndex = 1;
    for t=0:dt:T
        counter = counter + 1;
        
        % dA = parameters.CA*(conv2( [0 A(end, :) 0 ; A(:,end) A A(:,1) ; 0 A(1,:) 0], kernel, 'same'));
        dA = parameters.CA*(conv2( [0 A(1, :) 0 ; A(:,1) A A(:,end) ; 0 A(end,:) 0], kernel, 'same'));
        AB = A.*B;
        
        Ap = A + dt*(AB - A - 12 + dA(2:end-1,2:end-1));
        
        Ap( Ap < 0) = 0;
        
        % dB = parameters.CB*(conv2([0 B(end, :) 0 ; B(:,end) B B(:,1) ; 0 B(1,:) 0], kernel, 'same'));
        dB = parameters.CB*(conv2([0 B(1, :) 0 ; B(:,1) B B(:,end) ; 0 B(end,:) 0], kernel, 'same'));

        Bp = B + dt*(16 - AB + dB(2:end-1, 2:end-1));
        Bp(Bp < 0) = 0;
        
        
        ma = max(max(max(A)), max(max(Ap)));
      %  mb = max(max(max(B)), max(max(Bp)));
        
        fa = max(max(abs(A - Ap)))/ma;
        faBuffer(bfIndex) = fa;
        
        if(bfIndex == bfSize)
            bfIndex = 1;
        else
            bfIndex = bfIndex + 1;
        end
        
      %  fb = max(max(abs(B - Bp)))/mb; % check the maximum
        
        A = Ap;
        B = Bp;
        
        if(plotit && rem(counter, 50) == 0)
            % fa
            img(:,:,1) = A/max(max(A));
            image(img);
            maxA = max( maxA, max(max(A)));
            drawnow;
            
        end
        
        
        if(max(faBuffer) < dx ) 
            % steady state 
            break;
        end
        
        if(~isempty(find(isnan(A),1) ) || ~isempty(find(isinf(A),1) ))
            A = ones(systemSize);
            break;
            
        end
        if(~isempty(find(isnan(B),1)))
            display('nanb');
        end
        

    imgTime(counter,:,:,1)= A;
    imgTime(counter,:,:,2)= B;
    end
    
        
    
    
    mx = max(max(A));
    if(~isnan(mx) && mx > 0 )
        A = A/max(max(A));
    end
end


