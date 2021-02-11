#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Created on Wed Dec 23 10:35:46 2020

@author: luca
"""
import numpy as np
import matplotlib.pyplot as plt
import numpy.random as rnd
from network4strel import BetaDistribution, LognormalDistribution, DiscreteDistribution, GammaDistribution, CategoricalDistribution, DeterministicCategoricalDistribution, StateTransitionModel, Network, NetworkSimulator, SimulationOutput 
import time
import pickle


#fitted parameters of a lognormal of node degree
#shape,loc,scale =  0.7985868513480973, -1.0172598183244927, 11.737271521475328
#lognorm = LognormalDistribution(shape,loc,scale)
#infection_duration_distribution  = DeterministicDiscreteDistribution(2)
            
            
with open("degree_dist_no_events.pickle","rb") as f:
    base_degree_distribution = pickle.load(f)
with open("degree_dist_events.pickle","rb") as f:
    event_degree_distribution = pickle.load(f)
   

event_probability_distribution = CategoricalDistribution([0.0,1.0/30,1.0/15,1.0/7,3.0/7],[0.2,0.2,0.2,0.2,0.2])

#rough
#80% asymptomatic, infective 1-2 days befor symptoms and 2-5 days after symptoms, equal probability.  
infectivity_duration  = DiscreteDistribution([0,100,100,125,250,250,250,125],7,"infection_duration")    
delay_to_infectivity = GammaDistribution(1.87,1/0.28,max_range=15) #from merler
#init_dist = CategoricalDistribution(["S","I","R"],[0.995,0.005,0.0])
init_dist = CategoricalDistribution(["1","2","3","4"],[0.99,0.0,0.01,0.0])



#Beta distribution with mean 0.05
infection_probability_distribution = BetaDistribution(2,38)

node_number = 500

state_model = StateTransitionModel(states = ["1","2","3","4"],
                                   susceptible = {"1":True,"2":False, "3":False, "4":False},
                                   infective = {"1":False, "2":False, "3":True, "4":False},
                                   has_age = {"1":False, "2":True, "3":True, "4":False},
                                   age_distribution = {"2":delay_to_infectivity, 
                                                       "3":infectivity_duration},
                                   next_state = {"1":DeterministicCategoricalDistribution("2"),
                                                 "2":DeterministicCategoricalDistribution("3"),
                                                 "3":DeterministicCategoricalDistribution("4")},
                                   initial_state = init_dist)
#state_model = StateTransitionModel(states = ["S","I","R"],
#                                   susceptible = {"S":True, "I":False, "R":False},
#                                   infective = {"S":False, "I":True, "R":False},
#                                   has_age = {"S":False, "I":True, "R":False},
#                                   age_distribution = {"I":DeterministicDistribution(2)},
#                                   next_state = {"S":DeterministicCategoricalDistribution("I"),
#                                                 "I":DeterministicCategoricalDistribution("R")},
#                                   initial_state = init_dist)



print("\n\n******************************")
t = time.process_time()  
network = Network(node_number,state_model,base_degree_distribution,event_degree_distribution,event_probability_distribution,infection_probability_distribution)
t = time.process_time() - t
print("Network of {0:d} nodes created in {1:.6f} seconds".format(node_number,t))



max_steps = 100
runs=5
outfile = "strelsim.png"

t = time.process_time()  
sim = NetworkSimulator(network,save_full_network = True)
out = sim.simulate(runs=runs,max_steps=max_steps)
t = time.process_time() - t
print("Simulation of {0:d} runs of {1:d} steps in {2:.6f} seconds".format(runs,max_steps,t))
#out.plot_trajectory(0,file=outfile)
x = out.peak_distribution()
x = x[out.mask_only_explosive]
print("mean size of infection peak is {0:.5f}".format(np.mean(x)/node_number))
y = out.peak_time_distribution()
y = y[out.mask_only_explosive]
print("mean peak time of infection in days: {0:.5f}".format(np.mean(y)))


