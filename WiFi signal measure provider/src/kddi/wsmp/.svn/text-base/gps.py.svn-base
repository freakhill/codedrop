'''
Created on 2009/10/22

@author: jgall
'''

import bluetooth as bt

class GpsManager(object):
    '''
    Manages everything GPS(Bluetooth) related
    '''

    app = None
    connected = False

    def __init__(self, app, params):
        '''
        Constructor
        '''
        self.app = app
        self.connected = False
    
    def isConnected(self):
        return self.connected
    
    def checkWindowsCompatibility(self, major, minor, build, platform, text):
        """
        Should check the windows compatibility
        but for now just returns True... 
        """
        return True
