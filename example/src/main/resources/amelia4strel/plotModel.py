from networkx import *
import matplotlib.pyplot as plt
import random
from amelia4strel.repl import  *


inputmodel = "model-simple.gml"
# inputmodel ="model-trajects10357searchrange30.gml"
model = load_model_gml(inputmodel)

r1(model)