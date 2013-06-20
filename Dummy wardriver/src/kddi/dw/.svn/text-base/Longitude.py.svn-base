'''
Created on 2010/01/20

@author: jgall
'''

from __future__ import division

from Position import Position

'''
        note:
        from google, a crude solution that is good enough for us (spherical earth)
        
        Wikipedia says "Historically, the metre was defined by the French Academy
        of Sciences as the length between two marks on a platinum-iridium bar,
        which was designed to represent 1/10000000 (10M) of the distance from the 
        equator to the north pole through Paris"
        If there are 90 degrees from the equator to the North Pole then
        10000000 / 90 = 111111,11111111111111111111111111
        . . .the number of meters in a degree of latitude.
        
        Decimal degrees (dd.ff) to (dd + mm/60 +ss/3600)
        For the reverse conversion, we want to convert dd.ff to dd mm ss. Here ff = the fractional part of a decimal degree.
        mm = 60*ff
        ss = 60*(fractional part of mm)
        Use only the whole number part of mm in the final result.
        30.2561 degrees = 30 degrees
        .2561*60 = 15.366 minutes
        .366 minutes = 22 seconds, so the final result is 30 degrees 15 minutes 22 seconds
        
         Local calculation :p
         Equatorial radius R 6378,137 km
         we will use that with a stupid spherical model
         1deg lat = R*pi/180 km = 111,3195 km
         so we'll go for 
         1deg lon = 111,32 km
         1deg lat = 111,11 km
         (bonus, flattened earth effect \o/!!)
         and we'll use the previous calculation to go from deg to ddmmss
'''

class dLongitude(Position):
    
    # 4 on syncmask means meters are used
    
    def __init__(self, *args, **kwargs):
        if 'meter' in kwargs:
            kwargs = kwargs.copy()
            kwargs['deg'] = kwargs['meter'] / 111320
            self.meter = kwargs['meter']
            self.syncmask = self.syncmask | 4
        super(dLongitude, self).__init__(*args, **kwargs)
        self.set_deg = None
        self.set_ddmmss = None
        if not self.syncmask & 4:
            self.meter = self.deg * 111320

class Longitude(Position):
    '''
    classdocs
    '''


    def __init__(self, *args, **kwargs):
        '''
        Constructor
        '''
        super(Longitude, self).__init__(*args, **kwargs)
    
    def __sub__(self, b):
        return dLongitude(deg=abs(self.deg - b.deg))
    
    def __add__(self, b):
        '''
        You cannot add a Latitude to a Latitude, but you can add a distance to
        one, that's what we will do
        By convention distances are in meters
        '''
        return Longitude(deg=self.deg + b / 111320)
