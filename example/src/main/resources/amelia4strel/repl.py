from networkx import *
import random


def load_model_gml(filename):
    import networkx as nx
    """ read and init from GML. """
    print('Loading gml from', filename)
    a = nx.read_gml(filename)
    g = nx.DiGraph()
    for n in a.nodes():
        for p in a.node[n]['predicates']:
            g.add_node(n)
            try:
                g.node[n]['predicates'].append(p)
            except KeyError:
                g.node[n]['predicates'] = [p]  # first predicate
    g.add_edges_from(a.edges(data=True))
    return g


def add_peoplepredicate_node(model, node, peopleval):
    model.node[node]["people"] = peopleval
    return model


def add_taxipredicate_node(model, node):
    model.node[node]["predicates"].append("taxi")
    return model


def neighbor_predicates(model, node):
    for neigh in model.neighbors(node):
        return model.node[neigh]

def r1(model):
    """ R1:  All hospitals have a taxi less than 10 minutes away """
    print("R1")
    hospitals=[]
    for node in model.nodes():
        for predicate in model.node[node]['predicates']:
            if predicate == 'HEALTHHOSPITAL':
                # found node
                print('found HEALTHHOSPITAL on', node)
                hospitals.append(node)
    # for each hospital, put a taxi in the next node
    for hospital in hospitals:
        for neigh in model.neighbors(hospital):
            # if network[node][neigh]["times"]
            print('adding taxi near it, at', neigh)
            model = add_taxipredicate_node(model, neigh)

    write_gml(model, inputmodel.split(
        ".gm")[0] + "R1-satisfied" + ".gml")
    print


def r2(model):
    """ R2:Taxis should be at landmarks where one can reach the main square through bus or metro stops in less than 10 minutes''; """
    # start from main square
    # get neighbors
    print("R2")

    # lets say "W445439334" is the main square
    print('marking W445439334 as the main square')
    model.node["W445439334"]["predicates"].append("main_square")
    startnode = 'W445439334'

    for neigh in model.neighbors(startnode):
        if "TRANSPORTBUSSTOP" in model.node[neigh]['predicates'] or "TRANSPORTSUBWAY" in model.node[neigh]['predicates'] and network[node][neigh]["times"] < 10:
            # put a taxi
            print(neigh, 'is a TRANSPORTSUBWAY or TRANSPORTBUSSTOP and less than 10 away, adding taxi')
            model = add_taxipredicate_node(model, neigh)

    write_gml(model, inputmodel.split(".gm")[0] + "R2-satisfied" + ".gml")
    print


def r3(model):
    """ R3:if there is a landmark where more than 200 people are located, then a taxi will be there within 20 minutes """
    # add taxis
    # add people to nodes
    print("R3")

    target = random.choice(model.nodes())
    print('Adding 100 people on node', target)
    model = add_peoplepredicate_node(model, target, 100)
    target = random.choice(model.nodes())
    print('Adding 201 people on node', target)
    model = add_peoplepredicate_node(model, target, 201)

    for neigh in model.neighbors(target):
        if model[target][neigh]["times"] < 10:
            print('adding a taxi on node', neigh, 'which has 201 people')
            print(neigh, 'is away from', target, model[target][neigh]["times"])
            # put a taxi
            model = add_taxipredicate_node(model, neigh)
    write_gml(model, inputmodel.split(
        ".gm")[0] + "R3-satisfied" + ".gml")
    print




#inputmodel = "model-simple.gml"
inputmodel ="model-simpleR1-satisfied.gml"
# inputmodel ="model-trajects10357searchrange30.gml"
model = load_model_gml(inputmodel)

r1(model)
#r2(model)
#r3(model)
