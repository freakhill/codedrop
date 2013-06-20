'''
Created on 2009/10/29

@author: jgall
'''

import multiprocessing
from Queue import Empty as EmptyQ

import collections
import exceptions
import imp
import os

import measure_process
from measure_process.messages import START, STOP, SHUTDOWN

def MeasureProcessLauncher(startfunc, stopfunc, shutdownfunc):
    def inner_decorator(f):
        f.start = startfunc
        f.stop = stopfunc
        f.shutdown = shutdownfunc
        
        file = None
        try:
            file, filename, desc = imp.find_module(f.__name__,
                                                [os.path.join(
                                                    os.path.dirname(__file__),
                                                    'measure_process')])
        except exceptions.ImportError as e:
            raise e
        else:
            imp.acquire_lock()
            f.module = imp.load_module('measure_process.' + f.__name__,
                                       file, filename, desc)
            imp.release_lock()
        finally:
            if file:
                file.close()
        return f
    
    return inner_decorator
        

class MeasureManager(object):
    '''
    MeasureManager
    
    to access fresh measures (in a list), simply access the attribute
    to define a new measure foo, simply create a fooProcess global func
    '''
    
    processes = {}
    
    app = None

    def __init__(self, app, params):
        '''
        Constructor
        '''
        self.app = app
        
        # Checking all the class's attribs with a name finishing by Process
        # and create the framework around this measure taking method
        # That is, if it is a callable
        
        for k, v in globals().items():
            if k.endswith('Process') and callable(v):
                self.addMeasure(k[0:-7])
        self.stop()
    
    def __getattr__(self, name):
        '''
        if __getattribute__ couldn't catch it, that means it's measure data
        so we return measure data, we even create them if necessary :p
        '''
        if not self.processes.has_key(name):
            self.addMeasure(name)
            
        return self.extractQueue(self.processes[name].wsmp_output)
    
    def addMeasure(self, name):
        '''
        Enables the management of a new measure named name.
        Link the structure created to the func nameProcess if defined,
        if not link them do a dummy method feeding 0 values
        '''
        
        # If the measure doesn't have a measure taking process defined
        # redirect if to the dummy one
        ftarget = globals()['dummy']
        try:
            ftarget = globals()[name + 'Process']
        except KeyError:
            self.__setattr__(name + 'Process', dummy)
        
        input = multiprocessing.Queue(5000)
        output = multiprocessing.Queue(5000)
        self.processes[name] = multiprocessing.Process(target=ftarget,
                                                       args=(input, output))
        self.processes[name].wsmp_target = ftarget
        self.processes[name].wsmp_input = input
        self.processes[name].wsmp_output = output
        
        self.__setattr__(ftarget.__name__ + '_input', input)
        
        self.processes[name].start()
    
    def __getManagedSignalsList(self):
        return self.processes.keys()
    
    
    def extractQueue(self, queue):
        res = collections.deque()
        # The fast way would be list(q.queue)
        # We'll go at this if we get perf probs
        try:
            while True:
                res.append(queue.get(False))
        except EmptyQ:
            # We emptied that queue!
            pass
        return res
    
    def start(self):
        '''
        Start taking measures for every measure enabled (and the ones to come)
        '''
        for x in self.processes.values():
            x.wsmp_target.start(self)
    
    def stop(self):
        '''
        Stop taking measures for every measure enabled (and the ones to come)
        '''
        for x in self.processes.values():
            x.wsmp_target.stop(self)
            
    def shutdown(self):
        for x in self.processes.values():
            x.wsmp_target.shutdown(self)
        
        for x in self.processes.values():
            x.join()

    def checkWindowsCompatibility(self, major, minor, build, platform, text):
        '''
        Should check the windows compatibility
        but for now just returns True... 
        '''
        return True
    
    def rssiProcessStart(self):
        s = START()
        self.rssiProcess_input.put(s)
    
    def rssiProcessStop(self):
        s = STOP()
        self.rssiProcess_input.put(s)
        
    def rssiProcessShutdown(self):
        s = SHUTDOWN()
        self.rssiProcess_input.put(s)
    
    def throughputProcessStart(self):
        s = START()
        s.path = self.app.loadConfig('executables', 'iperf',
                        default='C:\Program Files\iperf-2.0.2\bin\iperf.exe')
        s.ip = self.app.loadConfig('apc', 'server_ip')
        s.port = self.app.loadConfig('apc', 'server_port')
        
        s.ip, s.port, s.path = str(s.ip), str(s.port), str(s.path)
        
        self.throughputProcess_input.put(s)
    
    def throughputProcessStop(self):
        s = STOP()
        self.throughputProcess_input.put(s)
        
    def throughputProcessShutdown(self):
        s = SHUTDOWN()
        self.throughputProcess_input.put(s)
    
    def dummyProcessStart(self):
        s = START()
        self.dummyProcess_input.put(s)
    
    def dummyProcessStop(self):
        s = STOP()
        self.dummyProcess_input.put(s)
        
    def dummyProcessShutdown(self):
        s = SHUTDOWN()
        self.dummyProcess_input.put(s)
    
    # PROPERTIES
    managedSignals = property(__getManagedSignalsList)
    
@MeasureProcessLauncher(MeasureManager.dummyProcessStart,
                        MeasureManager.dummyProcessStop,
                        MeasureManager.dummyProcessShutdown)
def dummy(input, output):
    obj = dummy.module.Process(input, output)
    obj.start()

@MeasureProcessLauncher(MeasureManager.throughputProcessStart,
                        MeasureManager.throughputProcessStop,
                        MeasureManager.throughputProcessShutdown)
def throughputProcess(input, output):
    obj = throughputProcess.module.Process(input, output)
    obj.start()
    
@MeasureProcessLauncher(MeasureManager.rssiProcessStart,
                        MeasureManager.rssiProcessStop,
                        MeasureManager.rssiProcessShutdown)
def rssiProcess(input, output):
    obj = rssiProcess.module.Process(input, output)
    obj.start() 
    
