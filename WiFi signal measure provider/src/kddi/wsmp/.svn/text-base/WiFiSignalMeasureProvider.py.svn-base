'''
Created on 2009/10/20

@author: jgall
'''

import os
import sys
import time

import db
import gps
import gui
import wifi

import measures

import ConfigParser as cp

class IncompatibleSystemException(Exception):
    def __init__(self, value):
        self.value = value
    def __str__(self):
        return repr(self.value)

class WiFiSignalMeasureProvider(object):
    '''
    Application class that manages... everything
    '''
    
    OS = ''
    db_manager = None
    gps_manager = None
    gui_manager = None
    wifi_manager = None
    
    measure_manager = None
    
    DbException = db.DbException
    
    qt_app = None

    measure_manager = None
    
    # Configuration file object
    config = None
    # Configuration file uri
    conf_file = ''

    def __init__(self, argv):
        '''
        Constructor
        
        Initialize the db, gps, gui and wifi managers
        Check the OS's compatibility with the current app
        Check the presence of the language file
        Load the config file
        '''
        # Should parse the config file url from argv...
        self.conf_file = os.path.join(os.path.dirname(__file__), 'wsmp.ini')
        self.config = cp.ConfigParser()
        if not self.config.read(self.conf_file):
            self.saveConfig()
        
        self.db_manager = db.DbManager(self, [])
        self.gps_manager = gps.GpsManager(self, [])
        self.wifi_manager = wifi.WifiManager(self, [])
        self.measure_manager = measures.MeasureManager(self, [])
        # Do it last!
        self.gui_manager = gui.GuiManager(self, [])
        
        try:
            self.check_os(self.db_manager,
                          self.gps_manager,
                          self.wifi_manager,
                          self.measure_manager,
                          self.gui_manager)
        except IncompatibleSystemException as e:
            print "Incompatible System Exception: " + e.value
        
    def shutdown(self):
        self.measure_manager.shutdown()
        self.db_manager.shutdown()
        
    def startMeasures(self):
        '''
        Begin to take measures for the current conf.
        
        Check if we are already taking measure, if it is not for the same conf
        stop them. Start threads to take the measures and feed them back to
        the main thread
        '''
        self.measure_manager.start()
        
    def sendMeasure(self):
        #CHANGE THIS CODE ASAP
        db = self.db_manager
        
        t = time.time() # format epoch.miliseconds
        timestamp = time.strftime('%Y-%m-%d %H:%M:%S', time.gmtime(t))
        ms = int((t - int(t)) * 1000)
        
        ap = db.insertAccessPoint(model='', sn='', ssid='')
        db.commit()
        ap_pos = db.insertPosition(latitude=0, longitude=0, height=0)
        db.commit()
        cl = db.insertClientInterface(model='', mac_address='')
        db.commit()
        cl_pos = db.insertPosition(latitude=1, longitude=1, height=1)
        db.commit()
        sv_pos = db.insertPosition(latitude=2, longitude=2, height=2)
        db.commit()
        
        print ap.id, ap_pos.id, cl.id, cl_pos.id, timestamp, ms
        l = lambda x : self.loadConfig('last_values', x)
        measure_call = dict(ap_id=ap.id, ap_pos=ap_pos.id,
                            client_id=cl.id, client_pos=cl_pos.id,
                            server_pos=sv_pos.id, date=timestamp,
                            date_ms=ms, exp_tag='', remarks='')
        for k in self.measure_manager.managedSignals:
            measure_call[k] = l(k)
        
        db.insertMeasures(**measure_call)
        db.commit()
    
    def stopMeasures(self):
        self.measure_manager.stop()
    
    def guiStart(self):
        '''
        launch the gui
        '''
        self.gui_manager.launch()

    def loadConfig(self, section, option, default=''):
        '''
        Load an option/section in the config file and create it with default
        value '' if it doesn't exist. Return the loaded value.
        '''
        try:
            return self.config.get(section, option, raw=True)
        except cp.NoSectionError:
            self.config.add_section(section)
            self.config.set(section, option, default)
            with open(self.conf_file, 'wb') as conf_file:
                self.config.write(conf_file)
        except cp.NoOptionError:
            self.config.set(section, option, default)
            with open(self.conf_file, 'wb') as conf_file:
                self.config.write(conf_file)
        return default

    def saveConfig(self):
        '''
        Save the current config object into the config file.
        '''
        with open(self.conf_file, 'wb') as conf_file:
                self.config.write(conf_file)
                
    def loadAndSetConfig(self, section, option, value):
        if self.loadConfig(section, option) != value:
            self.config.set(section, option, value)

    def check_os(self, *managers):
        '''
        Check if the Application is compatible with the running OS.
        
        First get informations about the running OS
        then call each module's appropriate compatibility check
        function until failing. If it fails raise an exception
        If no one fails, returns True.
        '''
        if os.name.startswith('nt'):
            windows = sys.getwindowsversion()
            major, minor, build, platform, text = windows
            if platform == 0:
                self.OS = 'Win32s on Windows 3.1'
            elif platform == 1:
                self.OS = 'Windows 95/98/ME'
            elif platform == 2:
                self.OS = 'Windows NT/2000/XP/x64'
            elif platform == 3:
                self.OS = 'Windows CE'
            else:
                self.OS = 'unknown Windows'
            for manager in managers:
                if not manager.checkWindowsCompatibility(*windows):
                    raise IncompatibleSystemException(
                                    "Current OS not managed by the module: "
                                     + type(manager).__name__)
        elif os.name.startswith('posix'):
            self.OS = 'posix'
            raise IncompatibleSystemException(
                                "POSIX systems are not managed yet.")
        else:
            self.OS = os.name
            raise IncompatibleSystemException(
            "Unknown system (os.name {0} is not managed yet)".format(os.name))


if __name__ == '__main__':
    '''
    launch wsmp - WiFi Signal Measure Provider
    
    get the OS
    check the config file (create an empty one if it doesn't exist)
    launch the GUI
    '''
    #import pydevd
    #pydevd.settrace()
    
    app = WiFiSignalMeasureProvider(sys.argv)
    app.guiStart()
