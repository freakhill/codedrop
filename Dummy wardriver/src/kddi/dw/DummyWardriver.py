'''
Created on 2010/01/20

@author: jgall
'''

from __future__ import division
from __future__ import with_statement

from Antenna import AntennaCity
from Walker import Walker

import sys
import random

from time import time

def bounds_constraint(a, b, c):
    return min(max(a, b), c)

if __name__ == "__main__":
    if len(sys.argv) < 10:
        print '''
        Wrong number of parameters!
        ex: DummyWardrive.py nWalkers meanRec stddevRec meanRes stddevRes stddevClock cdma.csv cdma_neighbours.csv max_id 
        where :
        nWalkers = number of people walking during the simulation []
        meanRec = mean recording time [hours]
        stddevRec = standard deviation of previous recording time (gaussvar) [hours]
        meanRes = mean of the mean resolution of measure taking  [ms]
        stddevRes = standard deviation of the previous value (gaussvar) [ms]
        stddevClock = standard deviation of the resolution (gaussvar) [ms]
        cdma.csv = name of the file in which we will print our rxl measures
        cdma_neighbours.csv = name of the file in which we will print our neighbours' rxl measures
        max_id = number of main measures to generate (if -1, then infinite)
        '''
        sys.exit()
    
    # somewhere in Tokyo
    city = AntennaCity(latitude=35.40, longitude=139.45, block_size=20, antenna_hdist=1000, shadowing_correlation_distance=20)
    city.generate_city('1', '40', '3', '30', '2', '7', '2', '4')
    
    num_paths = int(sys.argv[1])
    mean_record_time = int(sys.argv[2]) # hours
    stddev_record_time = int(sys.argv[3]) # hours
    mean_resolution = int(sys.argv[4]) # ms
    stddev_resolution = int(sys.argv[5]) # ms
    stddev_clock = int(sys.argv[6]) # ms
    main_out = sys.argv[7]
    neighbours_out = sys.argv[8]
    max_id = long(sys.argv[9])
    
    walker = Walker(city=city)
    
    resolutions = [int(bounds_constraint(0, random.gauss(mean_resolution, stddev_resolution), mean_resolution + 2 * stddev_resolution)) for i in range(num_paths)]
    pseudowalkers = [walker.path_generator_generator(resolutions[i]) for i in range(num_paths)]
    
    # OUTPUT DATA!
    id = 0L
    n_id = 0L # neighbour id
    reftime = long(time() * 100)
    with open(main_out, 'w') as main_out:
        main_out.write('id,date,rxl,cid,lac,rnc,lat,lon,phone_id\n')
        with open(neighbours_out, 'w') as neighbours_out:
            neighbours_out.write('id,ref_id,cid,lac,rxl\n')
            while id != max_id:
                for i in range(num_paths):
                    local_reftime = long(random.uniform(-2 * 3600 * 100, 2 * 3600 * 100))
                    local_reftime = local_reftime + reftime
                    # observation duration for walker i (in ms) (between 2 and 10 hours)
                    time_to_go = int(bounds_constraint(2, random.gauss(mean_record_time, stddev_record_time), 10)) * 3600 * 100
                    walker_i_res = resolutions[i]
                    walker_i = pseudowalkers[i]()
                    walker_i_time = local_reftime
                    while time_to_go > 0:
                        lat, lon = walker_i.next()
                        rssi_antenna_list = city.get_rssi_list_at_pos(lat, lon)
                        rssi_antenna_list = sorted(rssi_antenna_list)
                        rssi, antenna = rssi_antenna_list[-1]
                        cid, lac = antenna.cid, antenna.lac
                        del rssi_antenna_list[-1]
                        #rssi_antenna_list.remove((rssi, antenna))
                        # the gauss Xvar reflects 2 things
                        # the equipment doesn't take measures at sooo precise intervals
                        # and also people don't walk at a fixed speed but it's partly lost
                        # in the shadowing grid and we make the very broad hypothesis
                        # that in fine the measures are taken at fix distances!
                        # it would be perhaps bad for other scientific applications
                        # but to test a DB perf it should be good enough
                        # 2 gaussians involved in the measures (shadowing, time)
                        # instead of 3 (shadowing, time, walking speed) won't break things
                        # ... i hope
                        main_out.write(str(id) + ',' + str(walker_i_time \
                            + int(bounds_constraint(-100, random.gauss(0, stddev_clock), 100))) \
                            + ',' + str(rssi) + ',' + str(cid) + ',' + str(lac) \
                            + ',' + '0' + ',' + str(lat.deg) + ',' + str(lon.deg) \
                            + ',' + str(i) + '\n')
                        # fill the neighbour
                        num_neigh = len(rssi_antenna_list)
                        for j in range(bounds_constraint(0, int(random.gauss(4, 1)), num_neigh)):
                            rssi, antenna = rssi_antenna_list[-j - 1]
                            neighbours_out.write(str(n_id) + ',' + str(id) + ',' \
                                + str(antenna.cid) + ',' + str(antenna.lac) + ',' \
                                + str(rssi) + '\n')
                            n_id = n_id + 1
                        if id == max_id:
                            break
                        id = id + 1
                        time_to_go = time_to_go - walker_i_res
                        walker_i_time = walker_i_time + walker_i_res
                    if id == max_id:
                        break
                if id == max_id: # these 2 following lines have no
                    break # use, they're redundant, but help readability 
                reftime = reftime + 24 * 3600 * 100 # next day
            
