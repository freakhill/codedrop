'''
Created on 2010/01/15

@author: jgall
'''

from __future__ import division

from PathFinder import PathFinder
#from Latitude import Latitude
#from Longitude import Longitude

import random

def bounds_constraint(a, b, c):
    return min(max(a, b), c)

class Walker(object):
    '''
    Simulates a walker in a city that will alternate phases of *going to 
    a special point* *waiting* and *strolling*
    '''

    GOTO = 0
    WAIT = 1
    STROLL = 2
    
    city = None
    pf = None # PathFinder
    
    i_max = 0 #max index of the vertice layers of the current city 

    def __init__(self, city=None, *kargs, **kwargs):
        '''
        Constructor
        '''
        #super(Walker, self).__init__(*kargs, **kwargs)
        
        self.city = city
        if city == None:
            raise Exception('given (or default) parameter city set to None!')
        
        self.pf = PathFinder(city)
        
        self.i_max = int(city.side_size / city.block_size)
        
    # ugly bunch of lines because i want to finish this program!
    # refactor this later
    # ... some time pass
    # MOOOONSTER FUUUNK
    def path_generator_generator(self, resolution=250, waypoints=5):
        '''
        Returns a generator that generates the lat, lon values
        at parametrized time resolution according a path randomly generated
        Said path is composed of GOTO_point WAIT_at_point and STROLL
        sequences.
        (resolution in ms)
        '''
        # generates a path
        # we will start with very simple paths
        # :posA goto posB wait goto posC stroll goto posA loops
        max_waypoint_id = waypoints - 1
        waypoints = [ (int(random.randint(0, self.i_max)), int(random.randint(0, self.i_max))) for _ in range(waypoints) ]
        lowerlayer = self.city.vertices_layers[-1]
        waypoints = [lowerlayer[i, j] for i, j in waypoints]
        
        GOTO = 0
        WAIT = 1
        STROLL = 2
        
        pf = self.pf
        
        def path():
            # func that walks through this path
            lat = waypoints[0].lat # they are Objects so they are modified through closure
            lon = waypoints[0].lon
            current_waypoint_id = 0
            current_action = 0
            time_eaten_by_last_action = 0
            path = None
            
            class FinishedWalking(Exception):
                pass
            
            def from_vertice_walk_on_edge(vertice, edge, time_eaten_by_last_action):
                a, b = edge.vertices
                otherside = a if b == vertice else a
                been_crawling_for = resolution - time_eaten_by_last_action # ms
                while been_crawling_for < edge.traversal_cost:
                    lat = vertice.lat + ((otherside.lat - vertice.lat).meter * (been_crawling_for / edge.traversal_cost))
                    lon = vertice.lon + ((otherside.lon - vertice.lon).meter * (been_crawling_for / edge.traversal_cost))
                    yield lat, lon
                    been_crawling_for = been_crawling_for + resolution
                finished = FinishedWalking()
                finished.time_eaten_by_last_action = been_crawling_for - edge.traversal_cost
                lat = otherside.lat
                lon = otherside.lon
                raise finished
                
            while True:
                if current_action == GOTO:
                    if path == None:
                        path = pf.astar(waypoints[current_waypoint_id], waypoints[(current_waypoint_id + 1) % (max_waypoint_id + 1)])
                    for vertice, edge in path:
                        edgewalker = from_vertice_walk_on_edge(vertice, edge, time_eaten_by_last_action)
                        while True:
                            try:
                                yield edgewalker.next()
                            except FinishedWalking as e:
                                time_eaten_by_last_action = e.time_eaten_by_last_action
                                break
                    # we have finished crawling this path
                    current_waypoint_id = (current_waypoint_id + 1) % (max_waypoint_id + 1)
                    current_action = random.choice([WAIT, STROLL])
                    path = None
                if current_action == WAIT:
                    # wait during a random time at a fixed position
                    # for the time we'll do something like 30mn/3h gaussian
                    # with mean 1h stddev 30mn
                    will_wait_for = bounds_constraint(.5 * 3600.100, random.gauss(1 * 3600 * 100, .5 * 3600.100), 3 * 3600.100)
                    # time eaten by last_action < 250ms and will be *eaten* by will_wait_for
                    while will_wait_for > 0:
                        yield lat, lon
                        will_wait_for = will_wait_for - resolution
                    current_action = GOTO
                    time_eaten_by_last_action = 0
                if current_action == STROLL:
                    # we will stroll randomly around our position, then construct a path to our next waypoint
                    # we will stroll during a duration like 30mn/3h gaussian
                    # with mean 1h stddev 30mn
                    will_stroll_for = bounds_constraint(.5 * 3600.100, random.gauss(1 * 3600 * 100, .5 * 3600.100), 3 * 3600.100)
                    # we're strolling, so we come from a GOTO, so we're precisely
                    # on a waypoint
                    vertice = waypoints[current_waypoint_id]
                    while will_stroll_for > 0:
                        # randomly choose an edge in the lower level
                        # follows a performance horror but it's the first thing that comes to mind
                        while True:
                            edge = random.choice(vertice.edges)
                            if not edge.inter_network:
                                break
                        # walk through it
                        edgewalker = from_vertice_walk_on_edge(vertice, edge, time_eaten_by_last_action)
                        while True:
                            try:
                                yield edgewalker.next()
                            except FinishedWalking as e:
                                time_eaten_by_last_action = e.time_eaten_by_last_action
                                break
                        # remove the edge traversal_cost
                        will_stroll_for = will_stroll_for - edge.traversal_cost
                    # construct a path to the next waypoint
                    path = pf.astar(vertice, waypoints[(current_waypoint_id + 1) % (max_waypoint_id + 1)])
                    current_action = GOTO
                
        return path
