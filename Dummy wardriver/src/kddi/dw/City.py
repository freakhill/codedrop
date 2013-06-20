'''
Created on 2010/01/15

@author: jgall
'''
from __future__ import division
# don't want to get hit during degrees meters and ddmmss conversions...

import random, math

from Latitude import Latitude
from Longitude import Longitude

# CHANGE THIS ALGO ASAP!
drau = {0 : 2 ** 16}
def rau(x):
    global drau
    if x in drau:
        return drau[x]
    rau = 0
    while not ((x >> rau) & 1):
            rau = rau + 1
    drau[x] = rau
    return rau

class CityNode(object):      
    def __init__(self, father=None, layer_depth=0):
        self.father = father
        self.layer_depth = layer_depth
        
        self.network_vertices = []
        self.i = 0
        self.j = 0
        
        self.sons = []
        self.associated_network_speed = 0
        
class NetworkVertice(object):
    def __init__(self, i, j, masternode, speed=0):
        self.i = i # coords on the lower level city grid
        self.j = j
        
        self.masternode = masternode
        self.edges = [] # list of edges connected to this vertice
        
        self.lat = None # lat and lon associated to this vertice
        self.lon = None
        
        self.speed = 0 # speed associated to the network associated to this vertice

class NetworkEdge(object):
    def __init__(self, vertices, speed=0, wait=0):
        self.vertices = vertices
        self.speed = speed #km/h
        self.wait = wait #ms
        
        self.inter_network = False
        self.traversal_cost = None #ms
            
class HierarchicalGridCity(object):
    '''
    Cities are hierarchically divided with a 4**k-tree
    Each level corresponds to a transportation network with associated speed
    and ramification lvl (this is the 'k')
    lvl 0 = city core
    for instance :
    lvl 1 -> k = 1, 4 sons, speed 40 Km/h (between the sons), express metro
    lvl 2 -> k = 2, 16 sons, speed 30 Km/h, metro
    lvl 3 -> k = 5, 1024 sons, speed 5 Km/h, walking
    As the city is also a grid, each point is also associated coordinations
    and refered into an arrayarray
    going from on level to another induce a random waiting time
    (transfer time)
    Globally this city is a non directed graph with a hierarchical treeview
    relative to the transportation networks
    '''
    
    topleft_lat = None
    topleft_lon = None
    block_size = 0 # m
    
    city = None
    vertices_layers = None
    
    side_size = 0 # m, size of a side of the city
    layer_struct = [] # list of couples (depth of network, speed on this network)
    # the depth of network determines the number of elements in the layer of
    # the city supported by this network with the relation
    # num_blocks = 2 ** depth_of_network
    

    def generate_city(self, *args):
        '''
        args are under the form
        [k_i, speed_i]*...
        we generate the associated city
        '''
        
        args = [int(i) for i in args]
        
        total_depth = sum(args[0::2])
        self.side_size = 2 ** total_depth * self.block_size
        self.layer_struct = zip(args[0::2], args[1::2])
        
        city, vertices_layers = self.gen_city(*args)
        # the city and its vertices have been generated

        # now we will connect vertices at each level
        for layer in vertices_layers:
            self.connect_vertices_in_layer(layer)
        self.connect_vertices_between_layers(vertices_layers)
                
        self.geolocate_city(vertices_layers)
        self.generate_traversal_costs(vertices_layers)
        
        # we now have a sexy hierarchical grid city!
        self.city, self.vertices_layers = city, vertices_layers
        
    def generate_traversal_costs(self, vertices_layers):
        for layer in vertices_layers:
            self.generate_traversal_costs_in_layer(layer)
        
    def generate_traversal_costs_in_layer(self, layer):
        i_max = int(max(layer.keys())[0])
        for i in range(i_max + 1):
            for j in range(i_max + 1):
                for edge in layer[i, j].edges:
                    if edge.traversal_cost is None:
                        # speed in km/h
                        # distance determines by vertices coords and
                        # minimum block size in m
                        # traversal_cost, integer in ms
                        d = self.adjacentverticesdistance(edge.vertices[0], edge.vertices[1]) #m
                        edge.traversal_cost = int(d / (edge.speed * 1000 / 3600000))
    
    def gen_city(self, *args):
        city = CityNode()
        vertices_layers = [{}]
        self.rec_gen_city(city, vertices_layers, 3, *args)
        # by construction the last layer is empty
        del vertices_layers[-1]
        return city, vertices_layers
        
    def rec_gen_city(self, node, vertices_layers, which_son, *args):
        # sons
        # -1,-1 1,-1
        # -1, 1 1, 1
        # so
        # 0, 2
        # 1, 3
        # because in binary x.y._bin
        # 00, 10 
        # 01, 11
        if  len(args) == 0:
            return
        if args[0] == 0:
            # we finished expanding that network level
            # so the current node is on the next layer depth
            node.layer_depth = node.layer_depth + 1
            # we add a vertice_layer if we didnt already do so
            if len(vertices_layers) < node.layer_depth + 1: # cause layer depth is 0indexed
                vertices_layers.append({})
            # and we then continue the generation with the current node on
            # the next layer
            return self.rec_gen_city(node, vertices_layers, which_son, *args[2:])
        
        layer_vertices = vertices_layers[node.layer_depth]
        
        depth_to_bottom = sum(args[0::2])
        off_i, off_j = (node.father.i, node.father.j) if not node.father is None else (0, 0)
        # sqrt(4**depth) number of blocks in a side = 2**depth
        # 2**depth/2 middle of the thing (2**depth trivially pair)
        offset = 2 ** (depth_to_bottom - 1)
        #off_i, off_j = (node.father.i, node.father.j) if not node.father is None else (offset,offset)
        node.i = off_i + (1 if which_son & 2 else - 1) * offset
        node.j = off_j + (1 if which_son & 1 else - 1) * offset
        node.associated_network_speed = args[1]
        node.sons = [CityNode(father=node, layer_depth=node.layer_depth) for i in range(4)]
        
        self.generate_citynode_vertices(node, layer_vertices, depth_to_bottom, args[0])

        newargs = [args[0] - 1] # its the parameter that determines the number of 'layers' yet to process
        # on our current network level
        newargs.extend(args[1:])
        for i in range(4):
            self.rec_gen_city(node.sons[i], vertices_layers, i, *newargs)
            
    def generate_citynode_vertices(self, node, layer_vertices, depth_to_bottom, depth_to_layer_end):
        '''
        we just unroll the 9 vertices associated to the citynode
         0 8  2
         4 12 6
         1 9  3
        binary
         offsetx.offsety.x.y._bin
         0000 1000 0010 
         0100 1100 0110 
         0001 1001 0011 
        '''
        offset_to_bottom = 2 ** (depth_to_bottom - 1)
        offset_to_layer_end = 2 ** (depth_to_layer_end - 1)
        # main pattern
        #for i in [8,4,12]:
        for i in [0, 1, 2, 3, 4, 6, 8, 9, 12]:
            layer_i = node.i + ((1 if i & 2 else - 1) + (1 if i & 8 else 0)) * offset_to_bottom
            layer_j = node.j + ((1 if i & 1 else - 1) + (1 if i & 4 else 0)) * offset_to_bottom
            key_i, key_j = layer_i / offset_to_bottom * offset_to_layer_end, layer_j / offset_to_bottom * offset_to_layer_end
            if not (key_i, key_j) in layer_vertices:
                layer_vertices[key_i, key_j] = NetworkVertice(layer_i, layer_j, node)
                layer_vertices[key_i, key_j].speed = node.associated_network_speed
            node.network_vertices.append(layer_vertices[key_i, key_j])
        
                   
    def connect_vertices_in_layer(self, layer):
        i_max = int(max(layer.keys())[0])
        for i in range(i_max + 1):
            for j in range(i_max + 1):
                # connect with right and down (in a wave way)
                #print '--'
                #print i,j,len(layer[i,j].edges), layer[i,j]
                if i + 1 <= i_max:
                    edge = NetworkEdge((layer[i, j], layer[i + 1, j]), speed=layer[i, j].speed)
                    layer[i, j].edges.append(edge)
                    layer[i + 1, j].edges.append(edge)
                #print i,j,len(layer[i,j].edges), layer[i,j]
                if j + 1 <= i_max:
                    edge = NetworkEdge((layer[i, j], layer[i, j + 1]), speed=layer[i, j].speed)
                    layer[i, j].edges.append(edge)
                    layer[i, j + 1].edges.append(edge)
                #print i,j,len(layer[i,j].edges), layer[i,j]
    
    def adjacentverticesdistance(self, verticeA, verticeB):
        ''' Measure the distance between 2 adjacent vertices (in meters)
        Adjacent means that there is an edge connecting those 2.'''
        # we're on a simple '''euclidian''' grid so the formula is ok
        # block_size is in meters
        return abs(verticeA.i - verticeB.i) * self.block_size + abs(verticeA.j - verticeB.j) * self.block_size
    
    nodiagdistance = adjacentverticesdistance
    
    def nodiagdistance_rulersumimag(self, verticeA, verticeB):
        # nodiagdistance - sum(number of trailing lsb 0s in A)
        # so we tend to favor big networks
        # the number of trailing 0s function is called the ruler function :p
        # CHANGE THE ALGO FOR RAU ASAP
        return complex(self.nodiagdistance(verticeA, verticeB), -rau(verticeA.i) - rau(verticeA.j))
    
    def distance(self, verticeA, verticeB):
        ''' Measure the distance between 2 vertices (in meters)'''
        # we're on a simple '''euclidian''' grid so the formula is ok
        # block_size is in meters
        return math.sqrt(((verticeA.i - verticeB.i) * self.block_size) ** 2 + ((verticeA.j - verticeB.j) * self.block_size) ** 2)                
                    
    def connect_vertices_between_layers(self, vertices_layers):
        '''
        layer i has got 4**a vertices max_key is (2**a,2**a) (- 1,1 cause of 0index)
        layer i+1 gas got 4**b vertices (2**a,2**a) (- 1,1 cause of 0index)
        since it is a grid we get
        2**b/2**a layer i+1 vertices between 2 layer i vertices (on a side)
        '''
        layer_indexes_without_last_layer = range(len(vertices_layers) - 1)
        layer_couples = [(vertices_layers[i], vertices_layers[i + 1]) for i in layer_indexes_without_last_layer]
        for layer_i, layer_j in layer_couples:
            max_li = int(max(layer_i.keys())[0])
            max_lj = int(max(layer_j.keys())[0])
            offset = int(max_lj / max_li)
            for i in range(max_li + 1):
                for j in range(max_li + 1):
                    edge = NetworkEdge((layer_i[i, j], layer_j[i * offset, j * offset]))
                    # the speed is infinity encoded as None
                    edge.speed = None
                    #edge.traversal_cost = 0
                    # the wait time is a random number between 0 and 5*60*1000 ms (5mn)
                    edge.wait = int(random.random()*5 * 60 * 1000)
                    # we include the time to walk to the other part of the internetwork station
                    # in the wait time and so it doesn't appear as a separate term
                    # in the traversal cost of such edges
                    edge.traversal_cost = edge.wait
                    # and we set it as an inter network edge
                    edge.inter_network = True
                    # and we append this to the pertinent vertices!
                    layer_i[i, j].edges.append(edge)
                    layer_j[i * offset, j * offset].edges.append(edge)
                    #print 'INTERNETWORK', layer_i[i, j].i, layer_i[i, j].j, 'WITH', layer_j[i * offset, j * offset].i, layer_j[i * offset, j * offset].j
                    
    def geolocate_city(self, vertices_layers):
        '''
        assign lat and long to every vertice
        we suppose that our zone is small enough so that the earth is flat :p
        we calculate lat/lon for extreme points and then linear interpolate
        the others (on the lowest layer).
        we then use inter_network edges to propagate coords to upper layers.
        
        we also assign durations to edges (duration of the edge traversal
        if we are to walk at the set speed, speed in km/h, duration in ms)
        '''
        lower_layer = vertices_layers[-1]

        topleft_lat = self.topleft_lat # in degrees
        topleft_lon = self.topleft_lon # in degrees
        for i, j in lower_layer: # i and j 0indexed coords of a vertice 
            lower_layer[i, j].lon = topleft_lon + i * self.block_size
            lower_layer[i, j].lat = topleft_lat + j * self.block_size
            # now we will upward propagate the coords through the inter_network edges
            self.propagate_coords(lower_layer[i, j])

    def propagate_coords(self, vertice):
        '''
        For each edge connected to the current vertice, we check if it's a
        inter network edge, and if it is and it has not yet had its lat/lon
        values modified to the current vertice one, we modify it and propagate
        these values (recursively) through this newly detected vertice
        '''
        for edge in vertice.edges:
            if not edge.inter_network:
                continue
            a, b = edge.vertices
            otherside = a if b == vertice else a
            if  otherside.lat == vertice.lat and otherside.lon == vertice.lon:
                continue
            otherside.lat = vertice.lat
            otherside.lon = vertice.lon
            #Without return, we can propagate through multiple vertices
            #(like up and backwards, or even on more directions through
            #inter network links)
            #With a return, we get a proper tail recursion but it doesn't
            #traduce to any optimization in python and we lose expressive power
            #Moreover, our networks are not deep enough to make the stack cry
            self.propagate_coords(otherside)
              
    def __init__(self, latitude=0, longitude=0, block_size=0, *args, **kwargs):
        '''
        Constructor
        '''
        super(HierarchicalGridCity, self).__init__(*args, **kwargs)
        
        self.topleft_lat = Latitude(ddmmss=latitude)
        self.topleft_lon = Longitude(ddmmss=longitude)
        self.block_size = block_size
        
if __name__ == "__main__":
    import sys
    #exemple call
    #City.py 1 40 2 30 5 5
    # for a city with a 1deep 0lvl (higher) 40km/h network
    # so 4**1 = 4 2deep 1lvl zone 30 km/h
    # so for each of this 1lvl zone 4**2 = 16 5deep 2lvl zone 5km/h
    # we finally have 4**5 = 1024 0deep 3lvl zones
    # in all 4*16*1024 base blocks so 65536 blocks (256*256 grid) 
    # with a 20m block we get a 4km*4km city!
    city = HierarchicalGridCity(latitude=35.40, longitude=139.45, block_size=20)
    city.generate_city(*sys.argv[1:])
    
