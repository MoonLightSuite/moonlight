import networkx as nx
import numpy as np
import matplotlib.pyplot as plt
import numpy.random as rnd




def generate_random_ws_graph(nodes=100, nearest_neighbours=4, rewiring_prob=0.1, contact_prob_function = lambda: np.random.beta(1,10), states = ["S", "I", "R"], init_prob=(0.9,0.1,0.0)):
    """ Generates a random watts strogatz graph
        
        nodes: number of nodes
        nearest_neighbours: number of connected nearest neighbours in the ring topology
        rewiring_prob: probability of rewiring an edge in thr WS model
        contact_prob_function: a function taking zero arguments and returning a (random) number between zero and one
        states: the states a node can have
        init_prob: initial probability vector (same size as states)
    """
    graph = nx.watts_strogatz_graph(nodes, nearest_neighbours, rewiring_prob)
    graph = _generate_labels(graph,contact_prob_function, states, init_prob) 
    return graph
    
def _generate_labels(graph,contact_prob_function, states, init_prob):
    """ Generates labels for the graph
        Edge labels will be: 
        p: a contact probability
        mlogp: -log(contact probability)
        Node labels will be:
        state: state of the graph
    """
    for n in graph.nodes:
        graph.nodes[n]['state'] = rnd.choice(states,p=init_prob)
    for e in graph.edges:
        graph[e[0]][e[1]]['p'] = contact_prob_function()
        graph[e[0]][e[1]]['mlogp'] = -np.log(graph[e[0]][e[1]]['p'])
    return graph


def count(graph,states):
    """Counts how many nodes have a state as in state. Returns a dictionary
    """
    count = {}
    for s in states:
        count[s] = 0
    for n in graph.nodes:
        count[graph.nodes[n]['state']] += 1
    return count


def sir_simulation_step(graph,inf_prob,rec_prob):
    #compute logp of remaining susceptible for S nodes
    susc_logp = {}
    for n, nbrs in graph.adj.items():
        if graph.nodes[n]['state'] == "S":
            logp = 0
            for nbr, eattr in nbrs.items():
                if graph.nodes[nbr]['state'] == "I":
                    logp += np.log(1 - inf_prob*eattr['p'])
            susc_logp[n] = logp
    #does the update step
    for n in graph.nodes:
        if graph.nodes[n]['state'] == "S":
            if np.log(rnd.uniform()) > susc_logp[n]:
                graph.nodes[n]['state'] = "I"
        elif graph.nodes[n]['state'] == "I":
             if rnd.uniform() <= rec_prob:
                graph.nodes[n]['state'] = "R"
    return graph



def sir_simulate(graph,steps=100,inf_prob=0.5,rec_prob= 1.0/15):
    """Runs the simulation. Requires the model to be a SIR, with states named S, I, R"""
    states = ["S", "I", "R"]
    output = list()
    counts = {"S": list(), "I": list(), "R": list()}
    #init output
    output.append(graph.copy())
    c = count(graph,states)    
    for s in counts.keys():
        counts[s].append(c[s])
    
    #run simulation
    for i in range(steps):
        graph = sir_simulation_step(graph,inf_prob,rec_prob)
        output.append(graph.copy())
        c = count(graph,states)    
        for s in counts.keys():
            counts[s].append(c[s])
            
    return output, counts
    
    
    
def plot_graph(graph,type="shell",layout=None):
    cols = list()
    for n in graph.nodes:
        if graph.nodes[n]['state'] == "S":
            cols.append("blue")
        elif graph.nodes[n]['state'] == "I":
            cols.append("red")
        else:
            cols.append("green")
    options = {
        'node_color': cols,
        'node_size': 50,
        'width': 2,
    }
    if not layout is None:
        nx.draw(graph,pos = layout, **options)
    elif type == "shell":
        nx.draw_shell(graph,**options)
    elif type == "spectral":
        nx.draw_spectral(graph,**options)
    elif type == "spring":
        nx.draw_spring(graph,**options)
    else:
        nx.draw(graph,**options)
    return   


# constructor = [(5, 8, 0.8), (5, 8, 0.8), (5, 8, 0.8)]
# GG = nx.random_shell_graph(constructor)
# nx.draw_shell(GG)

# deg = [(2, 2), (1, 2), (1, 1), (2, 3), (1, 1), (2, 1), (1, 1), (0, 1)]
# G = nx.random_clustered_graph(deg)
# G = nx.Graph(G)
# G.remove_edges_from(nx.selfloop_edges(G))
# nx.draw_shell(G)
