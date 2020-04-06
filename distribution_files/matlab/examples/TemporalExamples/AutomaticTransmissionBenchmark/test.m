clear;
load input.mat
[Output] =  run_bench(Input);

plot(Output);

figure
subplot(5,1,1);
plot(Output(:,1),Output(:,2))
xlabel('time')
ylabel('Input Throttle')

subplot(5,1,2);
plot(Output(:,1),Output(:,3))
xlabel('time')
ylabel('Input Break')

subplot(5,1,3);
plot(Output(:,1),Output(:,4))
xlabel('time')
ylabel('(Output) Speed (mph)')

subplot(5,1,4);
plot(Output(:,1),Output(:,5))
xlabel('time')
ylabel('(Output) Engine (RPM)')

subplot(5,1,5);
plot(Output(:,1),Output(:,6))
xlabel('time')
ylabel('(Output) Gear')