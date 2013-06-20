'''
Created on 2009/11/05

@author: jgall
'''

from Queue import Empty as EmptyQ
from Queue import Full as FullQ

from time import time, sleep

from messages import *

def transitions(**dict):
    '''Decorator'''
    
    def inner_decorator(f):
        f.transitions = dict
        return f
    
    return inner_decorator

class FSM(object):
    
    def __init__(self, input, output):
        self.input = input
        self.output = output
        
        # transform the function dictionary
        for f in type(self).__dict__.values():
            if callable(f):
                try:
                    # ugly...
                    # v.transitions = dict(( (k, self.__getattribute__(v))
                    # for (k, v) in v.transitions.items() ))
                    for k, v in f.transitions.items():
                        f.transitions[k] = self.__getattribute__(v)
                except AttributeError:
                    pass
    
    def run(self, startfunc):
        f = startfunc
        order = None
        f(None)
        
        period_execution_start, period, period_execution_end, delta = 0,0,0,0
        while(True):
            # Get a new order in the queue, CONTINUE if no order
            try:
                order = self.input.get(False)
            except EmptyQ:
                order = CONTINUE()
            
            # We get the next state (func)
            typename = type(order).__name__
            
            f = f.transitions[typename]
            
            # previous loopexec attribued time end here
            period_execution_end = time()
            delta = period_execution_end - period_execution_start
            # we check if we must sleep a bit
            if period_execution_end - period_execution_start < period:
                sleep(period - delta)
            # prev. loop period completed, we take the current one
            period = order.period
            CONTINUE.period = order.period
            # We execute the func
            period_execution_start = period_execution_end
            f(order)
            
    def out(self, val):
        try:
            self.output.put(val, False)
        except FullQ:
            pass
