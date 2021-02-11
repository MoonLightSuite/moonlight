#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Created on Wed Dec 23 10:35:45 2020

@author: luca
"""

# !/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Created on Sat Dec  5 22:17:38 2020

@author: luca
"""

import networkx as nx
import numpy as np
import matplotlib.pyplot as plt
import numpy.random as rnd
from scipy.stats import lognorm, gamma, beta
from scipy.sparse import lil_matrix as sparse_matrix


class Distribution():

    def __init__(self):
        pass

    def sample(self):
        pass

    def sample_sequence(self, n):
        pass


class BetaDistribution(Distribution):

    def __init__(self, a, b):
        self.a = a
        self.b = b

    def mean(self):
        return beta.mean(self.a, self.b)

    def sample(self):
        return beta.rvs(self.a, self.b)

    def sample_sequence(self, n):
        return beta.rvs(self.a, self.b, size=n)


class GammaDistribution(Distribution):

    def __init__(self, shape, scale, loc=0, max_range=None, rounding=True):
        self.shape = shape
        self.loc = loc
        self.scale = scale
        self.max_range = max_range
        if rounding:
            self.post_process = np.round
        else:
            self.post_process = lambda x: x

    def mean(self):
        return gamma.mean(self.shape, self.loc, self.scale)

    def sample(self):
        if self.max_range == None:
            return self.post_process(gamma.rvs(self.shape, self.loc, self.scale))
        else:
            x = self.max_range + 1
            while x > self.max_range:
                x = self.post_process(gamma.rvs(self.shape, self.loc, self.scale))
            return x

    def sample_sequence(self, n):
        if self.max_range == None:
            return np.around(gamma.rvs(self.shape, self.loc, self.scale, size=n))
        else:
            s = np.zeros(n)
            for i in range(n):
                s[i] = self.sample()


class LognormalDistribution(Distribution):

    def __init__(self, shape, loc, scale, rounding=True):
        self.shape = shape
        self.loc = loc
        self.scale = scale
        if rounding:
            self.post_process = np.round
        else:
            self.post_process = lambda x: x

    def mean(self):
        return lognorm.mean(self.shape, self.loc, self.scale)

    def sample(self):
        return self.post_process(lognorm.rvs(self.shape, self.loc, self.scale))

    def sample_sequence(self, n):
        return np.around(lognorm.rvs(self.shape, self.loc, self.scale, size=n))


class DeterministicDistribution(Distribution):

    def __init__(self, value):
        self.value = value

    def mean(self):
        return self.value

    def sample(self):
        return self.value

    def sample_sequence(self, n):
        return self.value * np.ones(n)


class PoissonDistribution(Distribution):

    def __init__(self, mean):
        self.mean = mean

    def sample(self):
        return np.random.poisson(self.mean)

    def sample_sequence(self, n):
        return np.random.poisson(self.mean, n)


class CategoricalDistribution(Distribution):
    def __init__(self, categories, probabilities):
        self.categories = categories
        self.probabilities = np.array(probabilities)

    def sample(self):
        return np.random.choice(self.categories, p=self.probabilities)

    def sample_sequence(self, n):
        return np.random.choice(self.categories, n, p=self.probabilities)


class DeterministicCategoricalDistribution(Distribution):
    def __init__(self, category):
        self.category = category

    def sample(self):
        return self.category

    def sample_sequence(self, n):
        return [self.category for i in range(n)]


class DiscreteDistribution(Distribution):

    def __init__(self, counts, max_range, name):
        self.counts = counts
        self.max_range = max_range
        self.values = np.arange(max_range + 1)
        self.name = name
        self.total = np.sum(counts)
        self.probabilities = counts / self.total

    def mean(self):
        return np.dot(self.values, self.probabilities)

    def sample(self):
        return np.random.choice(self.values, p=self.probabilities)

    def sample_sequence(self, n):
        return np.random.choice(self.values, n, p=self.probabilities)

    def conditional(self, lower, upper):
        """
        Conditions the sampling to be included in [lower,upper]
        """
        if lower > self.max_range:
            raise Exception("Conditioning not allowed, lower bound exceeds distribution range")
        if lower == 0 and upper == np.inf:
            self.probabilities = self.counts / self.total
        else:
            mask = np.zeros(self.max_range + 1)
            for i in range(lower, upper + 1):
                mask[i] = 1
            self.probabilities = self.counts * mask / np.sum(self.counts * mask)


class State():
    """ the state of an agent, this class contains also the change dynamics and age information
    
    model: has to be a StateTransitionModel
    """

    def __init__(self, model):
        self.model = model
        self.reset_state()

    def reset_state(self):
        self.state = self.model.initial_state.sample()
        self.update_age()

    def sample_state(self, distribution):
        self.state = distribution.sample()
        self.update_age()

    def is_susceptible(self):
        return self.model.susceptible[self.state]

    def is_infective(self):
        return self.model.infective[self.state]

    def update_age_needed(self):
        return self.model.has_age[self.state]

    def update(self, infected=False):
        if infected and self.is_susceptible():
            self.change_state()
            return 1
        elif self.update_age_needed():
            self.age -= 1
            if self.age == 0:
                self.change_state()
        return 0

    def is_state(self, state):
        return self.state == state

    def get_state(self):
        return self.state

    def change_state(self):
        self.state = self.model.next_state[self.state].sample()
        self.update_age()

    def update_age(self):
        if self.update_age_needed():
            self.age = self.model.age_distribution[self.state].sample()
        else:
            self.age = 0


class StateTransitionModel():

    def __init__(self, states=["S", "I", "R"],
                 susceptible={"S": True, "I": False, "R": False},
                 infective={"S": False, "I": True, "R": False},
                 has_age={"S": False, "I": True, "R": False},
                 age_distribution={"I": DeterministicDistribution(2)},
                 next_state={"S": DeterministicCategoricalDistribution("I"),
                             "I": DeterministicCategoricalDistribution("R")},
                 initial_state=CategoricalDistribution(["S", "I", "R"], [0.95, 0.05, 0.0])):
        self.states = states
        self.susceptible = susceptible
        self.infective = infective
        self.has_age = has_age
        self.age_distribution = age_distribution
        self.next_state = next_state
        self.initial_state = initial_state


# definire edge con -log(1-infection_prob) -> più alto il valore più alta la probabilità di contagio
# definire una event probability per ogni nodo, la campiono da una discreta con 0, 1/30, 1/15, 1/7, 3/7, prob 0.2 each
# infection probability è Beta (2,38)        
# nella simulazione, per ogni nodk scelgo se resta, genero la nuova rete, calcolo per ogni susc la somma delle probability di infezione


class Network():

    def __init__(self, node_number, state_model, main_degree_distribution, event_degree_distribution, event_probability,
                 infection_probability_distribution):
        self.node_number = node_number
        self.model = state_model
        self.main_degree_distribution = main_degree_distribution
        self.event_degree_distribution = event_degree_distribution
        self.event_probability = event_probability
        self.infection_probability_distribution = infection_probability_distribution
        self.generate_random_network()
        self.nodes = self.network.nodes
        self.event_network = None

    def generate_random_network(self):
        """ Generates a random network with given degree distribution
            
        """
        degrees = self.main_degree_distribution.sample_sequence(self.node_number)
        self.network = nx.expected_degree_graph(degrees, seed=None, selfloops=False)
        for n in self.network.nodes:
            self.network.nodes[n]['state'] = State(self.model)
            self.network.nodes[n]['event_prob'] = self.event_probability.sample()
        for e in self.network.edges:
            self.network[e[0]][e[1]]['p'] = self.infection_probability_distribution.sample()
            self.network[e[0]][e[1]]['mlogp'] = -np.log(self.network[e[0]][e[1]]['p'])

    def sample_event_network(self):
        # sample subset of nodes, by sampling a degree sequence with several zeros
        degrees = np.zeros(self.node_number, dtype=np.int16)
        for n in self.network.nodes:
            if rnd.rand() <= self.network.nodes[n]['event_prob']:
                degrees[n] = self.event_degree_distribution.sample()
            else:
                degrees[n] = 0
        # sample network
        self.event_network = nx.expected_degree_graph(degrees, seed=None, selfloops=False)
        for e in self.event_network.edges:
            self.event_network[e[0]][e[1]]['p'] = self.infection_probability_distribution.sample()
            self.event_network[e[0]][e[1]]['mlogp'] = -np.log(self.event_network[e[0]][e[1]]['p'])

    def count_states(self):
        count = {}
        for s in self.model.states:
            count[s] = 0
        for n in self.network.nodes:
            s = self.network.nodes[n]['state'].state
            count[s] += 1
        inf = 0
        for s in self.model.states:
            if self.model.infective[s]:
                inf += count[s]
        return count, inf == 0

    def compute_infection_probability(self, prob_array):
        for n, nbrs in self.network.adj.items():
            if self.network.nodes[n]['state'].is_infective():
                for nbr, _ in nbrs.items():
                    if self.network.nodes[nbr]['state'].is_susceptible():
                        prob_array[nbr] += -np.log(1 - self.network[n][nbr]['p'])
        for n, nbrs in self.event_network.adj.items():
            if self.network.nodes[n]['state'].is_infective():
                for nbr, _ in nbrs.items():
                    if self.network.nodes[nbr]['state'].is_susceptible():
                        prob_array[nbr] += -np.log(1 - self.event_network[n][nbr]['p'])
        prob_array = 1 - np.exp(-prob_array)

    def print_network(self, time):
        s = "TIME {0:.5f}\n".format(time)
        adj_matrix = sparse_matrix((self.node_number, self.node_number))
        adj_matrix2 = sparse_matrix((self.node_number, self.node_number))
        for n in self.network.nodes:
            adj_list = self.network.adj[n]
            for nbr in adj_list:
                mlogp = adj_list[nbr]['mlogp']
                adj_matrix[n, nbr] += mlogp
                # if n < nbr:
                #     adj_matrix2[n, nbr] += mlogp
                # else:
                #     adj_matrix2[nbr, n] += mlogp
            # s += "{0:d}; {1:d}; {2:.5f}\n".format(n,nbr,mlogp)
            # print dynamic network
            if not self.event_network is None:
                adj_list = self.event_network.adj[n]
                for nbr in adj_list:
                    mlogp = adj_list[nbr]['mlogp']
                    if n < nbr:
                        adj_matrix[n, nbr] += mlogp
                    else:
                        adj_matrix[nbr, n] += mlogp
                # s += "{0:d}; {1:d}; {2:.5f}\n".format(n,nbr,mlogp)

        row, col = adj_matrix.nonzero()
        for i, j in zip(row, col):
            s += "{0:d}; {1:d}; {2:.5f}\n".format(i, j, adj_matrix[i, j])
        return s
        # return a string with the encoding of the network.

    def print_network_state(self, time):
        s = "{0:.5f}".format(time)
        for n in self.network.nodes:
            # print current node and its state
            local_state = self.network.nodes[n]['state'].state
            s += f"; {local_state}"
        s += "\n"
        return s
        # return a string with the encoding of the network.


class NetworkSimulator():

    def __init__(self, network, initial_distribution=None, save_full_network=False, out_filename="epidemic_simulation"):
        self.network = network
        self.initial_distribution = initial_distribution
        self.save_full_network = save_full_network
        self.out_filename = out_filename

    def simulation_step(self, time, out_file_network, out_file_traj):
        self.network.sample_event_network()
        infection_probability = np.zeros(self.network.node_number)
        self.network.compute_infection_probability(infection_probability)

        ###sample infection events
        infected = (rnd.rand(self.network.node_number) <= infection_probability)

        ###update network state
        new_infections = 0
        for n, inf in zip(self.network.nodes, infected):
            new_infections += self.network.nodes[n]['state'].update(inf)

        if self.save_full_network:
            s = self.network.print_network(time)
            out_file_network.write(s)
            s = self.network.print_network_state(time)
            out_file_traj.write(s)

        return new_infections

    def _init_simulation(self):
        if self.initial_distribution is None:
            for n in self.network.nodes:
                self.network.nodes[n]['state'].reset_state()
        else:
            for n in self.network.nodes:
                self.network.nodes[n]['state'].sample_state(self.initial_distribution)

    def simulate(self, runs=1, max_steps=100):
        states = self.network.model.states
        trajectories = {}
        for s in states:
            trajectories[s] = np.zeros((runs, max_steps + 1))
        trajectories["new_infections"] = np.zeros((runs, max_steps + 1))
        trajectories["days"] = np.arange(max_steps + 1)
        steps = np.zeros(runs)

        for run in range(runs):
            trajectory = {}
            for s in states:
                trajectory[s] = trajectories[s][run]
            trajectory["new_infections"] = trajectories["new_infections"][run]
            self._init_simulation()
            steps[run] = self._simulate_once(max_steps, trajectory, states, run)

        final_size = {}
        for s in states:
            final_size[s] = np.reshape(trajectories[s][:, -1], runs)

        out = SimulationOutput(self.network, trajectories, final_size, steps, runs, max_steps)
        return out

    def _simulate_once(self, max_steps, trajectory, states, run):
        network_state, _ = self.network.count_states()
        for s in states:
            trajectory[s][0] = network_state[s]
        if self.save_full_network:
            filename = self.out_filename + "_network_" + str(run) + ".txt"
            file_network = open(filename, "w")
            filename = self.out_filename + "_trajectory_" + str(run) + ".txt"
            file_traj = open(filename, "w")
            file_network.write(f"LOCATIONS {self.network.node_number}\nUNDIRECTED\n")
            file_traj.write(f"LOCATIONS {self.network.node_number}\n")
        else:
            file_network = None
            file_traj = None

        for i in range(max_steps):
            new_i = self.simulation_step(i, file_network, file_traj)
            network_state, terminate = self.network.count_states()
            for s in states:
                trajectory[s][i + 1] = network_state[s]
            trajectory["new_infections"][i] = new_i
            steps = i + 1
            if terminate:
                for s in states:
                    for j in range(i + 2, max_steps + 1):
                        trajectory[s][j] = network_state[s]
                break
        file_network.close()
        file_traj.close()
        return steps


class SimulationOutput():

    def __init__(self, network, trajectories, final_size, steps, runs, max_steps, new_infection_explosion_threshold=10):
        self.network = network
        self.states = network.model.states + ["new_infections"]
        self.trajectories = trajectories
        self.final_size = final_size
        self.steps = steps
        self.runs = runs
        self.max_steps = max_steps
        self.new_infection_explosion_threshold = new_infection_explosion_threshold
        self.mask_only_explosive = np.amax(self.trajectories["new_infections"],
                                           axis=1) > self.new_infection_explosion_threshold
        self.infected_trajectories = self._compute_infected_trajectory()

    def extinction_probability(self):
        return 1 - sum(self.mask_only_explosive) / self.runs

    def peak_distribution(self):
        return np.amax(self.infected_trajectories, axis=1)

    def peak_time_distribution(self):
        return np.argmax(self.infected_trajectories, axis=1)

    def _compute_infected_trajectory(self):
        model = self.network.model
        infected_traj = np.zeros((self.runs, self.max_steps + 1))
        for s in model.states:
            if model.infective[s]:
                infected_traj += self.trajectories[s]
        return infected_traj

    def plot_trajectory(self, simID, show=True, file=None, title=""):
        if simID < 0 or simID > self.runs:
            raise Exception("Wrong index {0:d} of trajectory, must be in [0, {1:d}]".format(simID, self.runs - 1))
        for s in self.states:
            plt.plot(self.trajectories["days"], self.trajectories[s][simID])
        plt.legend(self.states)
        plt.title("Trajectory {0:d}: {1}".format(simID, title))
        if show:
            plt.show()
        if not file is None:
            plt.savefig(file)

    def print_trajectory(self, simID):
        if simID < 0 or simID > self.runs:
            raise Exception("Wrong index {0:d} of trajectory, must be in [0, {1:d}]".format(simID, self.runs - 1))
        for s in self.states:
            print("state {0}: {1}\n".format(s, self.trajectories[s][simID]))

    def plot_trajectories(self, first=None, last=None, show=True, file=None):
        # plot all trajectories from first to last
        pass

    def plot_mean_trajectory(self, show=True, file=None, confidence_level=1.96, title="epidemic evolution",
                             only_explosive=True):
        for s in self.states:
            if only_explosive:
                tt = self.trajectories[s][self.mask_only_explosive, :]
                ar = sum(self.mask_only_explosive)
            else:
                tt = self.trajectories[s]
                ar = self.runs
            means = np.mean(tt, axis=0)
            stds = np.std(tt, axis=0)
            plt.plot(self.trajectories["days"], means)
            plt.fill_between(self.trajectories["days"], means - confidence_level * stds,
                             means + confidence_level * stds, alpha=0.2)
            # plt.errorbar(self.trajectories["days"],means,confidence_level*stds,marker='o')
        plt.legend(self.states)
        if only_explosive:
            title_extra = "conditional on exploding epidemics (p = {0:.5f})".format(ar / self.runs)
        else:
            title_extra = "all runs"
        plt.title("Mean of {0} runs: {1} {2}".format(ar, title, title_extra))
        if show:
            plt.show()
        if not file is None:
            plt.savefig(file)
