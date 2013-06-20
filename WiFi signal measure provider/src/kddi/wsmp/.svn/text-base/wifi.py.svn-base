'''
Created on 2009/10/22

@author: jgall
'''

import wmi

class WifiManager(object):
    '''
    Manages everything WiFi related
    For windows it deals with the Native WiFi Api and COM objects
    via the pywin library
    '''

    app = None
    
    connected = False
    
    __wmi__ = None
    net_object = None

    def __init__(self, app, params):
        '''
        Constructor
        '''
        self.app = app
        self.connected = False
        
        self.__wmi__ = wmi.WMI(namespace='WMI')
    
    def checkWindowsCompatibility(self, major, minor, build, platform, text):
        """
        Should check the windows compatibility
        but for now just returns True... 
        """
        return True
    
    def getInterfaces(self):
        pass
    
    def scan(self, interface):
        pass
    
    def connect(self, interface, ssid):
        '''
        Connect to the appropriate SSID with the given interface
        
        Start a thread who will keep getting measures and updating values
        '''
        pass
    
    def isConnected(self):
        return self.connected
    
    
