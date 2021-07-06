run('cartpole_main.m')

open_system('cartpole')
load_system('cartpole')

in = Simulink.SimulationInput();

T_end = 20;
Tsim = 1e-3;
Ts =1e-2;

n_sim = 10;

data = zeros(T_end/Tsim+1,4,n_sim);

for i= 1:n_sim

    const = rand(4,1) * 2e-4;
    r1 = const(1);
    r2 = const(2);
    r3 = const(3);
    r4 = const(4);

    out = sim('cartpole');
    
    data(:,:,i) = out.simout.Data;

end