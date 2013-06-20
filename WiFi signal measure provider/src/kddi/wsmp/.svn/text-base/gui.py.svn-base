from __future__ import with_statement

import rawgui

import sys, collections

from PyQt4 import QtCore, QtGui


class SignalTabRefreshTimersStruct(object):
    
    # Dictionary key -> (timer, default period)
    timers = {}
    
    def __init__(self, **dict):
        for k, v in dict.items():
            self.timers[k] = (QtCore.QTimer(), v)
    
    def start(self):
        '''
        Start each timer with its default values
        '''
        for v in self.timers.itervalues():
            v[0].start(v[1])
        
    def stop(self):
        '''
        Stop every timer
        '''
        for v in self.timers.itervalues():
            v[0].stop()
            
    def __getattr__(self, name):
        return self.timers[name][0]

def SignalRefreshPeriod(period):
    def inner_decorator(f):
        f.period = period
        #r = SignalDisplayClass(f, period)
        return f
    
    return inner_decorator

class GuiManager(QtGui.QMainWindow):
    '''
    Manages everything GUI(PyQT) related
    
    To add the displaying of a new signal. Just add the method nameDisplay
    with the annotation @SignalRefreshPeriod(period)
    '''
    # The QT Application class
    qt_app = None
    # WSMP Main class
    app = None
    # Signal tab's display refresh timers
    timers = None
    
    managed_signals = []
    
    def __init__(self, app, argv):
        '''
        Constructor
        '''
        self.qt_app = QtGui.QApplication(argv)
        self.app = app
        
        QtGui.QWidget.__init__(self, None)
        self.ui = rawgui.Ui_MainWindow()
        self.ui.setupUi(self)
        
        signals = {}
        # Check the display functions, get back the periods
        # and build a dictionary 'signal_name' -> period
        for k, v in GuiManager.__dict__.items():
            if k.endswith('Display') and callable(v):
                fc = self.__getattribute__(k)
                signals[k[0:-7]] = fc.period
        
        # Create the Signal Tab GUI timers
        self.timers = SignalTabRefreshTimersStruct(**signals)
        self.managed_signals = list(signals.keys())
        
    def closeEvent(self, event):
        self.shutdown()
        event.accept()
    
    def shutdown(self):
        self.app.shutdown()
    
    def launch(self):
        '''
        Launch the GUI
        
        Show the GUI, load the conf file (last session) parameters,
        disable the signal tab, and connect the GUI elements (signal)
        to their appropriate reaction (slots, python callables).
        '''
        self.show()
        ui = self.ui
        
        self.loadSignalTabConf()
        self.connectSignalTabSlots()
        
        self.loadDbTabConf()
        self.connectDbTabSlots()
        
        self.loadApcTabConf()
        self.connectApcTabSlots()
        
        self.loadGpsTabConf()
        self.connectGpsTabSlots()
        
        self.loadLanguageTabConf()
        self.connectLanguageTabSlots()
        
        # Disabling the signal tab because we can't show nor send measures yet
        self.disableSignalTab()
        # We arbitrarily select the db tab as first widget to load
        ui.mainTab.setCurrentWidget(ui.dbTab)
        
        sys.exit(self.qt_app.exec_())

    def loadApcTabConf(self):
        '''
        Load the access point, client and server conf from the config file and
        use it them to initialize pertinent GUI elements.
        '''
        ui = self.ui
        
        server_ip = self.app.loadConfig('signal',
                                                   'server_ip',
                                                   default='127.0.0.1')
        server_port = self.app.loadConfig('signal',
                                                   'server_port',
                                                   default='5001')
        
        
        ip = ui.apcServerAddressLineEdit
        port = ui.apcServerPortLineEdit
        
        ip.setText(server_ip)
        port.setText(server_port)
        
        if server_ip != '' and server_port != '':
            ui.apcServerParamsButton.setChecked(True)
            ui.apcServerParamsButton.setText('CLICK TO CHANGE PARAMETERS')
            ip.setDisabled(True)
            port.setDisabled(True)
        
    def connectApcTabSlots(self):
        '''
        Connect the access point, client and server panel GUI elements
        to its appropriate behaviour.
        '''
        self.ui.apcServerParamsButton.toggled.connect(
                                            self.apcServerParamsButtonToggled)
    
    def loadGpsTabConf(self):
        '''
        Load the GPS (bluetooth) conf from the config file and use it to 
        initialize pertinent GUI elements
        '''
        pass
        
    def connectGpsTabSlots(self):
        '''
        Connect the GPS (bluetooth) panel GUI elements
        to its appropriate behaviour.
        '''
        pass

    def loadLanguageTabConf(self):
        '''
        Load the language conf from the config file and use it to initialize
        pertinent GUI elements
        '''
        ui = self.ui
        ui.langEnglishButton.setEnabled(False)
        ui.langJapaneseButton.setEnabled(False)
        
    def connectLanguageTabSlots(self):
        '''
        Connect the language panel GUI elements
        to its appropriate behaviour.
        '''
        pass
    
    def loadSignalTabConf(self):
        '''
        Load the signal measurement conf from the config file and it
        them to initialize pertinent GUI elements.
        '''
        throughputXWindow = int(self.app.loadConfig('signal',
                                                   'throughput_x_window',
                                                   default=32)) # 30 ticks
        throughputBgColor = self.app.loadConfig('signal',
                                                   'throughput_bg_color',
                                                   default='#000000')
        
        self.__throughputX = collections.deque(maxlen=throughputXWindow)
        self.__throughputY = collections.deque(maxlen=throughputXWindow)
        
        self.ui.throughputPlot.axes.set_axis_bgcolor(throughputBgColor)
        
        self.ui.throughputPlot.axes.set_xlabel('time (s)')
        self.ui.throughputPlot.axes.set_ylabel('throughput (Mbps)')
        self.ui.throughputPlot.axes.grid(True)
        
        # initialize the last measured values to '' if the section does not
        # exist in the config file
        for k in self.managed_signals:
            self.app.loadConfig('last_values', k, '')
        
    def connectSignalTabSlots(self):
        '''
        Connect the signal measurement panel GUI elements
        to its appropriate behaviour.
        '''
        # Connect the refresh timers to the appropriate methods
        for k in self.managed_signals:
            self.timers.__getattr__(k).timeout.connect(
                object.__getattribute__(self, k + 'Display'))
        self.ui.sendMeasureButton.clicked.connect(self.sendMeasureClicked)

    def disableSignalTab(self):
        self.ui.signalTab.setEnabled(False)
        self.stopSignalTabDisplayLoop()
        self.clearSignalTab()
        self.app.stopMeasures()
    
    def tryEnableSignalTab(self):
        #if not self.app.db_manager.isConnected():
        #    return False
        #if not self.app.wifi_manager.isConnected():
        #    return False
        #if not self.app.gps_manager.isConnected():
        #    return False
        self.ui.signalTab.setEnabled(True)
        self.app.startMeasures()
        self.startSignalTabDisplayLoop()
        return True

    def startSignalTabDisplayLoop(self):
        self.timers.start()
    
    def stopSignalTabDisplayLoop(self):
        self.timers.stop()
    
    def clearSignalTab(self):
        pass

    def loadDbTabConf(self):
        ui = self.ui
        address = ui.dbAddressLineEdit
        login = ui.dbLoginLineEdit
        
        l = lambda x : self.app.loadConfig('database', x)
        address.setText(l('address'))
        login.setText(l('login'))
                
    def writeDbTabConf(self):
        ui = self.ui
        # At this time we consider that the conf file exist
        # in a coherent state!
        # We'll manage the exceptions later...
        s = lambda x, y : self.app.config.set('database', x, y.text())
        s('login', ui.dbLoginLineEdit)
        s('address', ui.dbAddressLineEdit)
        self.app.saveConfig()

    def connectDbTabSlots(self):
        self.ui.dbConnectButton.toggled.connect(self.dbConnectToggled)

    def sendMeasureClicked(self):
        s = lambda x, y : self.app.config.set('last_values', x, y)
        for k in self.managed_signals:
            time, val = object.__getattribute__(self, k + 'Value')()
            s(k, val)
            
        self.app.sendMeasure()

    def apcServerParamsButtonToggled(self, is_checked):
        ui = self.ui
        ip = ui.apcServerAddressLineEdit
        port = ui.apcServerPortLineEdit
        usethese = ui.apcServerParamsButton
        
        if is_checked:
            # We are using these params now
            usethese.setChecked(True)
            usethese.setText('CLICK TO CHANGE PARAMETERS')
            ip.setDisabled(True)
            port.setDisabled(True)
            s = lambda x, y : self.app.config.set('apc', x, y.text())
            s('server_ip', ip)
            s('server_port', port)
            #self.tryEnableSignalTab()
        else:
            # Freeing the params
            self.app.db_manager.disconnect()
            usethese.setText('USE THESE PARAMS')
            ip.setDisabled(False)
            port.setDisabled(False)
            #self.disableSignalTab()

    def dbConnectToggled(self, is_checked):
        ui = self.ui
        address = ui.dbAddressLineEdit
        login = ui.dbLoginLineEdit
        password = ui.dbPasswordLineEdit
        connect = ui.dbConnectButton
        
        if is_checked:
            # We are connecting to a database
            try:
                self.app.db_manager.connect(
                                            login=login.text(),
                                            password=password.text(),
                                            address=address.text())
            except self.app.DbException as e:
                connect.setChecked(False)
                connect.setText('(connection failed) CONNECT ' + str(e))
            else:
                connect.setText('CONNECTED (Click to disconnect)')
                login.setDisabled(True)
                password.setDisabled(True)
                address.setDisabled(True)
                self.writeDbTabConf()
                self.tryEnableSignalTab()
        else:
            # Disconnection
            self.app.db_manager.disconnect()
            connect.setText('CONNECT')
            login.setDisabled(False)
            password.setDisabled(False)
            address.setDisabled(False)
            self.disableSignalTab()                

    def checkWindowsCompatibility(self, major, minor, build, platform, text):
        '''
        Should check the windows compatibility
        but for now just returns True... 
        '''
        return True
    
    def rssiValue(self):
        '''
        Return the last measured value and the time of measurement in a tuple
        (time, value).
        Also, internally update the table of values to periodically display
        '''
        return 0, 0
    
    @SignalRefreshPeriod(100)
    def rssiDisplay(self):
        pass
    
    def throughputValue(self):
        '''
        Return the last measured value and the time of measurement in a tuple
        (time, value).
        Also, internally update the table of values to periodically display
        '''
        measures = self.app.measure_manager.throughput
        self.__throughputX.extend([t[0] for t in measures])
        self.__throughputY.extend([t[1] for t in measures])
        
        try:
            return self.__throughputX[-1], self.__throughputY[-1]
        except IndexError:
            return 0, 0
    
    @SignalRefreshPeriod(1000)
    def throughputDisplay(self):
        self.throughputValue()
        
        x = list(self.__throughputX)
        y = list(self.__throughputY)
        self.ui.throughputPlot.axes.plot(x, y)
        
        try:
            xmin = int(x[0]) + 1
            xmax = int(x[-1]) - 1
            self.ui.throughputPlot.axes.set_xbound(xmin, xmax)
        except Exception:
            pass
        else:
            self.ui.throughputPlot.draw()
