'''
Created on 2010/01/15

@author: jgall
'''
from __future__ import division

import Antenna

class PathFinder(object):
    '''
    PathFinder class that operates on City.py NetworkVertice
    (or compatible)
    '''

    def __init__(self, city): 
        '''
        Constructor
        '''
        self.city = city
    
    def astar(self, from_v, to_v):
        from_v.g_score = 0
        self.__astar(from_v, to_v)
        return self.moonwalk_this_path(from_v, to_v)
    
    def moonwalk_this_path(self, from_v, to_v):
        finalpath = []
        moonwalker = to_v
        while moonwalker != from_v:
            finalpath.append((moonwalker.current_astar_parent, moonwalker.current_astar_parent_edge))
            moonwalker = moonwalker.current_astar_parent
        finalpath.reverse()
        return finalpath
    
    def __astar(self, from_v, to_v): # from vertice, to vertice
        '''
        A* pathfinder
        Heuristic
        F = G + H
        G = cost (ms) to get to evaluated node
        H = time fo fly to the destination with the evaluated node speed
        
        more doc on http://www.gamedev.net/reference/articles/article2003.asp
        '''
        from_v.g_score = 0
        open_dict = {}
        closed_dict = {}
        open_dict[from_v.i, from_v.j, from_v.masternode.layer_depth] = from_v
        
        bp_value = 2
        
        while from_v != to_v:
            #print from_v.i, from_v.j
            del open_dict[from_v.i, from_v.j, from_v.masternode.layer_depth]
            closed_dict[from_v.i, from_v.j, from_v.masternode.layer_depth] = True
            #print open_list, closed_list

            for edge in from_v.edges:
                a, b = edge.vertices
                otherside = b if a == from_v else a
                
                if not (otherside.i, otherside.j, otherside.masternode.layer_depth) in closed_dict:
                    if not (otherside.i, otherside.j, otherside.masternode.layer_depth) in open_dict:
                        otherside.g_score = from_v.g_score + edge.traversal_cost
                        otherside.current_astar_parent = from_v
                        otherside.current_astar_parent_edge = edge
                        otherside.h_score = self.city.nodiagdistance_rulersumimag(otherside, to_v) / (otherside.speed * 1000 / 3600000) 
                        open_dict[otherside.i, otherside.j, otherside.masternode.layer_depth] = otherside
                        if otherside.masternode.layer_depth == bp_value:
                            pass
                    else :
                        if from_v.g_score + edge.traversal_cost < otherside.g_score:
                            otherside.g_score = from_v.g_score + edge.traversal_cost
                            otherside.current_astar_parent = from_v
                            otherside.current_astar_parent_edge = edge
                    otherside.f_score = otherside.g_score + otherside.h_score
                    otherside.f_score = (otherside.f_score.real, otherside.f_score.imag) # with this, the default < establish a partial order
                    # this is the quick shit design, a good design would put *score_func *partial_order_operable_func *partial_order_compare_func into a structure

            min_fscore_id = min([(open_dict[i, j, l].f_score, (i, j, l)) for i, j, l in open_dict])[1]
            min_fscore_vertice = open_dict[min_fscore_id]
            from_v = min_fscore_vertice

if __name__ == "__main__":
    #city = City.HierarchicalGridCity(latitude = 35.40, longitude = 139.45, block_size = 20)
    city = Antenna.AntennaCity(latitude=35.40, longitude=139.45, block_size=20, antenna_hdist=1000, shadowing_correlation_distance=20)
    city.generate_city('1', '40', '3', '30', '2', '15', '2', '5')
    pf = PathFinder(city)
    
    path = pf.astar(city.vertices_layers[-1][201, 147],
                    city.vertices_layers[-1][43, 5])
    i = 0
    for vertice, edge in path:
        i = i + 1
        print i, '- Vertice i,j,speed,g_score: ', vertice.i, vertice.j, vertice.speed, vertice.g_score, 'traversal cost:', edge.traversal_cost
        print max(city.get_rssi_list_at_pos(vertice.lat, vertice.lon))
