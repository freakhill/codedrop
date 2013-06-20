'''
Created on 2010/02/22

@author: jgall
'''

class AggMeasure(object):
    '''
    Statistic value for an aggregated measure
    '''
    known = False
    value = 0
    
    ref_quad = None
    query_manager = None

    def __init__(self, qm, rq, **kw):
        '''
        Constructor
        '''
        self.query_manager = qm
        self.ref_quad = rq
        
        self.foldfunc = kw['foldfunc'] if 'foldfunc' in kw else lambda x, y : x + y 
        self.aggfunc = kw['aggfunc'] if 'aggfunc' in kw else lambda x : x
        self.initkw = kw
        
        self.known = False
        self.value = 0
        
    foldedranks = 0
    foldedvalue = 0
    def fold(self, data, rank):
        #init
        if self.foldedranks == 0:
            self.known = False
            self.foldedvalue = type(self.value)()
        #body
        if self.foldedranks & 1 << rank:
            return
        self.foldedranks = self.foldedranks + 1 << rank
        self.foldedvalue = self.foldfunc(self.foldedvalue, data.value)
        #ender
        if self.foldedranks == 16:
            self.known = True
            self.foldedranks = 0
            self.value = self.aggfunc(self.foldedvalue)
    
    def unfold(self, rank):
        # we are forced to update the value...
        rq = self.ref_quad
        Qi = rq.Q0 if rank == 0 else rq.Q1 if rank == 1 else rq.Q2 if rank == 2 else rq.Q3
        res = type(self)(self.query_manager, Qi, **self.initkw)
        res.update()
        return res
    
    def __str__(self):
        return str(self.value)
