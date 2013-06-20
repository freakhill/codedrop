'''
Created on 2010/01/20

@author: jgall
'''

from __future__ import division

import math

def deg2ddmmss(deg):
    # dd, mm, ss = degrees, minutes, seconds
    mm, dd = math.modf(deg)
    dd = int(dd) + (1 if mm > .5 else 0)
    #dd = round(dd) # slower with round O_o
    ss, mm = math.modf(mm * 60)
    mm = int(mm) + (1 if ss > .5 else 0)
    #mm = round(mm)
    rest, ss = math.modf(ss * 60)
    ss = int(ss) + (1 if rest > .5 else 0)
    #ss = round(ss)
    return dd + mm / 100 + ss / 10000
    
def ddmmss2deg(ddmmss):
    mmss, dd = math.modf(ddmmss)
    dd = int(dd)
    mm = int(mmss * 100)
    ss, _ = math.modf(ddmmss * 100) # used to be ss, mmdd
    ss = int(ss * 100)
    return dd + mm / 60 + ss / 3600

ddmmss_epsilon = 0.0001

class IncoherentInitData(Exception):
    def __init__(self, msg, *args, **kwargs):
        super(IncoherentInitData, self).__init__('deg and ddmmss init values are not coherent', *args, **kwargs)
        
class CurrentlyHoldingNoData(Exception):
    def __init__(self, msg, *args, **kwargs):
        super(IncoherentInitData, self).__init__('does not currently hold a value', *args, **kwargs)


class Position(object):
    '''
    classdocs
    '''
    
    syncmask = 0
    # syncmask & 1 means ddmmss is synched
    # syncmask & 2 means deg is synched
    __ddmmss = 0
    __deg = 0

    def __init__(self, *args, **kwargs):
        #super(Position, self).__init__(*args, **kwargs)
        # why doesnt Py2.6 object.__init__ ignore all parameters
        # it seems to be very bad considering python's multiinheritance algorithm (MRO)
        
        if 'ddmmss' in kwargs:
            self.__ddmmss = kwargs['ddmmss']
            self.syncmask = self.syncmask | 1
        
        if 'deg' in kwargs:
            self.__deg = kwargs['deg']
            self.syncmask = self.syncmask | 2
        
        if self.syncmask == 3:
            if(abs(deg2ddmmss(self.__deg) - self.__ddmmss) > ddmmss_epsilon):
                raise IncoherentInitData()
    
    def get_ddmmss(self):
        if self.syncmask & 1:
            return self.__ddmmss
        if self.syncmask & 2:
            self.__ddmmss = deg2ddmmss(self.__deg)
            self.syncmask = 3
            return self.__ddmmss
        raise CurrentlyHoldingNoData()
    
    def get_deg(self):
        if self.syncmask & 2:
            return self.__deg
        if self.syncmask & 1:
            self.__deg = ddmmss2deg(self.__ddmmss)
            self.syncmask = 3
            return self.__deg
        raise CurrentlyHoldingNoData()
    
    def set_ddmmss(self, val):
        self.__ddmmss = val
        self.syncmask = 1
    
    def set_deg(self, val):
        self.__deg = val
        self.syncmask = 2
        
    ddmmss = property(get_ddmmss, set_ddmmss, None, 'Latitude value in ddmmss format')
    deg = property(get_deg, set_deg, None, 'Latitude value in deg format')

    def copy(self):
        return Position(deg=self.deg, ddmmss=self.ddmmss)
        
