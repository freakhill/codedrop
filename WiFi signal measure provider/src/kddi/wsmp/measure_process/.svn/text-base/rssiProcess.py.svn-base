'''
Created on 2009/11/05

@author: jgall
'''
from fsm import FSM, transitions
import time
import exceptions

'''
    tsm = TStateMachine(input)
    while True:
        # Running our mini state machine
        state = tsm.next()[1]
            
        # Act according to state
        if state is RUNNING_STATE():
            #DEBUG val = self.app.wifi_manager.getRSSI()
            val = 0
            try:
                output.put((time.time(), val), False)
            except Queue.Full:
                pass 
'''

class Process(FSM):
    '''
    classdocs
    '''

    def __init__(self, *p):
        FSM.__init__(self, *p)

    def start(self):
        self.run(self.stopped)
    
    
    @transitions(CONTINUE='started',
                 START='error',
                 STOP='stopping',
                 SHUTDOWN='shutdown')
    def started(self, order):
        # Take a measure
        pass
    
    @transitions(CONTINUE='stopped',
                 START='starting',
                 STOP='stopped',
                 SHUTDOWN='shutdown')
    def stopped(self, order):
        # Huh... do nothing
        pass
    
    @transitions(CONTINUE='started',
                 START='error',
                 STOP='stopping',
                 SHUTDOWN='shutdown')
    def starting(self, order):
        # Launch
        pass
    
    @transitions(CONTINUE='stopped',
                 START='starting',
                 STOP='stopped',
                 SHUTDOWN='shutdown')
    def stopping(self, order):
        pass
    
    @transitions(CONTINUE='error',
                 START='error',
                 STOP='error',
                 SHUTDOWN='shutdown')
    def error(self, order):
        pass
    
    @transitions(CONTINUE='shutdown',
                 START='shutdown',
                 STOP='shutdown',
                 SHUTDOWN='shutdown')
    def shutdown(self, order):
        import sys
        sys.exit(0)
