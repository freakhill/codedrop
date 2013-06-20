'''
Created on 2010/01/19

@author: jgall
'''

from __future__ import division

import City
import math
import random

#from Latitude import Latitude
#from Longitude import Longitude

def generate_nbitrand_pool_generator(range):
    def generate_nbitrand_pool(num):
        pool = [] # we use a list to keep the insertion order...
        poolset = set()
        rng = random.SystemRandom()
        while len(pool) < num:
            n = int(rng.getrandbits(range))
            if not n in poolset:
                pool.append(n)
                poolset.add(n)
        return pool
    return generate_nbitrand_pool

def methodizer(fun):
    def lolmethod(self, *args, **kwargs):
        return fun(*args, **kwargs)
    return lolmethod

class Antenna(object):
    def __init__(self, lac=0, cid=0, lat=0, lon=0, power=40, shadowing_correlation_distance=50, *args, **kwargs):
        super(Antenna, self).__init__(*args, **kwargs)
        
        self.lac = lac
        self.cid = cid
        self.lat = lat
        self.lon = lon
        self.power = power
        self.shadowing_correlation_distance = shadowing_correlation_distance
        
    def __ge__(self, other):
        return (self.cid, self.lac, self.power) >= (other.cid, other.lac, other.power)

class AntennaCity(City.HierarchicalGridCity):
    '''
    In order to add CDMA antennas on the city MAP and enable the
    calculus of RSSI values at any position in the city
    
    hypothesis (CHECK THIS LATER):
    shadow fading (slow one) changes according to the different antenna
    frequencies are negligible, thus we can share a reference shadowing grid
    for all the antennas.
    '''
    
    antenna_hdist = 0 #m
    antenna_vdist = 0 #m
    shadowing_correlation_distance = 20 #m
    
    antennas = []
    shadowing_grid = {}
    
    def __init__(self, antenna_hdist=0, shadowing_correlation_distance=20,
                 *args, **kwargs):
        '''
        City.HierarchicalGridCity arguments + approximate horizontal distance
         between antennas (in m)
        '''
        #parentkwargs = kwargs.copy()
        #del parentkwargs['antenna_hdist']
        #del parentkwargs['shadowing_correlation']
        super(AntennaCity, self).__init__(*args, **kwargs)
        
        self.antenna_hdist = antenna_hdist
        self.antenna_vdist = (math.sqrt(3) / 2) * self.antenna_hdist
        
        self.shadowing_correlation_distance = shadowing_correlation_distance
    
    def generate_city(self, *args, **kwargs):
        '''
        Generate the city as in City.HierarchicalGridCity.generate_city
        and then adds antennas
        '''
        super(AntennaCity, self).generate_city(*args, **kwargs)
        #add antennas
        #the pattern is
        # odd line : A A A A
        # even line:  A A A A
        #            A A . .
        #             A A . .
        #            ^
        #         odd column
        #             ^
        #            even column  
        # (0 indexed)
        
        self.antennas = self.generate_antennas()
        # now we generate the shadowing grid for the city
        self.shadowing_grid = self.generate_shadowing_grid(mu=0, sigma=5)

    def generate_antennas(self):
        num_antennas_on_odd_line = int(self.side_size / self.antenna_hdist)
        #num_antennas_on_even_line = int(self.side_size / self.antenna_hdist - .5)
        num_antennas_on_even_line = num_antennas_on_odd_line # we do that so we don't end up with a poor bordermap radio coverave
        
        num_antennas_on_odd_column = int(self.side_size / self.antenna_vdist)
        #num_antennas_on_even_column = int(self.side_size / (2*self.antenna_vdist) - .5)
        num_antennas_on_even_column = num_antennas_on_odd_column
        
        num_antennas_total = num_antennas_on_odd_line * num_antennas_on_odd_column \
                            + num_antennas_on_even_line * num_antennas_on_even_column
        
        cid_pool = self.generate_cid_pool(num_antennas_total)
        lac_pool = self.generate_lac_pool(num_antennas_total)
        
        antennas = []
        
        for i in range(num_antennas_on_odd_line):
            for j in range(num_antennas_on_odd_column):
                antenna = Antenna(lat=self.topleft_lat + self.antenna_vdist * j,
                                  lon=self.topleft_lon + self.antenna_hdist * i,
                                  lac=lac_pool.pop(), cid=cid_pool.pop(),
                                  power=40,
                                  shadowing_correlation_distance=20)
                antennas.append(antenna)
        
        for i in range(num_antennas_on_even_line):
            for j in range(num_antennas_on_even_column):
                antenna = Antenna(lon=self.topleft_lon + self.antenna_hdist * (i + .5),
                                  lat=self.topleft_lat + self.antenna_vdist * (j + 1),
                                  lac=lac_pool.pop(), cid=cid_pool.pop(),
                                  power=40,
                                  shadowing_correlation_distance=20)
                antennas.append(antenna)
        # yeah, as you could see, THE EARTH IS FLAT!!!
        return antennas
    
    def generate_shadowing_grid(self, mu=0, sigma=5):
        '''
        Generates a shadowing grid shared for all the antennas of the city.
        
        lognormal values are adapted to high density zone
        with this the city virtually becomes more complex than the pure grid
        it is!
        
        we generate a grid of resolution shadowcorrel_distance / 2
        then the shadowing_value for each point will be interpolated
        from the shadowing_grid points strictly inside the pertinent circle
        of diameter shadowing_distance
        thus 2 points separated by shadowcorrel_distance+ won't be correlated
        2 points separated by less than shadowcorrel_distance will!
        '''
        shadowing_grid = {}
        i_max = int((self.side_size / self.shadowing_correlation_distance) * 2)
        i_max = i_max + 1 # because we got one point at 0,0
        for i in range(i_max):
            for j in range(i_max):
                shadowing_grid[i, j] = min(random.lognormvariate(mu, sigma), 10)
        return shadowing_grid
    
    def get_rssi_list_at_pos(self, lat, lon):
        '''
        return a list of valid couples (cid, rssi)
        received (by a perfect omnidirection antenna) at the position
        with coords lat/lon
        '''
        rssis = []
        # rssi = emit_power - pathloss
        # pathloss = Pd(dist) + Pshadowing
        # Pd(dist) = 35.3 +37.6*log(dist in meters)
        # Pshadowing <-logvariate mean 0 sigma 5dB
        
        Pshadowing = self.get_interpolated_shadowing(lat, lon)
        
        for antenna in self.antennas:
            latdist = (antenna.lat - lat).meter #m
            londist = (antenna.lon - lon).meter #m
            d = math.sqrt(latdist ** 2 + londist ** 2) #m
            Pdistance = 35.3 + 37.6 * math.log10(max(20, d)) # model is not considered pertinent for < 20m thus the max(...
            rssis.append((antenna.power - Pdistance - Pshadowing, antenna))
            # yeah yeah the earth is flat
        return rssis
    
    def get_interpolated_shadowing(self, lat, lon):
        '''
        first calculate Pshadowing[dB] by interpolating from values
        of the shadowing grid inside a pertinent circle
        '''
        inside_points = []
        # find a point inside the circle
        i_max = int((self.side_size / self.shadowing_correlation_distance) * 2)
        
        a_floatant_shadowing_grid_longitude_index = ((lon - self.topleft_lon).meter / self.shadowing_correlation_distance) * 2
        inside_i = int(min(round(a_floatant_shadowing_grid_longitude_index), i_max))
        
        a_floatant_shadowing_grid_latitude_index = ((lat - self.topleft_lat).meter / self.shadowing_correlation_distance) * 2
        inside_j = int(min(round(a_floatant_shadowing_grid_latitude_index), i_max))
        # we can easily simplify the following calculus to gain performance
        # but for the better understanding of the algorithm (+ the low gain)
        # we won't
        di = (a_floatant_shadowing_grid_longitude_index - inside_i) * self.shadowing_correlation_distance / 2
        dj = (a_floatant_shadowing_grid_latitude_index - inside_j) * self.shadowing_correlation_distance / 2
        dist = math.sqrt(di ** 2 + dj ** 2)
        inside_points.append((dist, self.shadowing_grid[inside_i, inside_j]))
        # test if adjacent points are in the circle
        # note that furtherpoints are at min shadowcorreldist and cannot be
        # in the circle
        # 0000 0001 0010 masks 00.. 0 01.. 4 10.. 8 (horizontal or vertical is interchangeable :p)
        # 0100 0101 0110 masks ..00 0 ..01 1 ..10 2
        # 1000 1001 1010
        for id in [0, 1, 2, 6, 10, 9, 8, 4]:
            i = inside_i - 1 + (id >> 2) # DO NOT REMOVE PARENTHESIS!
            j = inside_j - 1 + (id & 3)
            if i > i_max or j > i_max: # borders
                continue
            # the complete calculus
            #di = (a_floatant_shadowing_grid_longitude_index - i) * self.shadowing_correlation_distance / 2
            #dj = (a_floatant_shadowing_grid_latitude_index - j) * self.shadowing_correlation_distance / 2
            #dist = math.sqrt(di ** 2 + dj ** 2)
            #if dist < self.shadowing_correlation_distance / 2:
            #    inside_points.append((dist, self.shadowing_grid[i,j]))
            
            # post simplification we get this (because the square func is strictly increasing on positive values)
            # it's the simplification we could have made above, but with a more significant gain
            # and the origin calculus presented
            di = (a_floatant_shadowing_grid_longitude_index - i) ** 2
            dj = (a_floatant_shadowing_grid_latitude_index - j) ** 2
            if di + dj < 1:
                inside_points.append((math.sqrt(di + dj) * self.shadowing_correlation_distance / 2, self.shadowing_grid[i, j]))
        # now that we have the points inside the circle INTERPOLATE!
        # barymetric interpol
        # the coeffs will be (sum_of_dist - this_dist) / ((num_of_points - 1) * sum_of_dist)
        # that is 1/(num_of_points-1)*(1-this_dist / sum_of_dist)
        # sum_of_coeffs = 1 so it's ok
        N = len(inside_points)
        if N == 1:
            shadowing = inside_points[0][1]
        else:
            sum_dist = sum([i[0] for i in inside_points])
            shadowing = 1 / (N - 1) * sum([(1 - dist / sum_dist) * shadowing for (dist, shadowing) in inside_points])
        
        return shadowing
            
    generate_cid_pool = methodizer(generate_nbitrand_pool_generator(16))
    generate_lac_pool = methodizer(generate_nbitrand_pool_generator(16))
