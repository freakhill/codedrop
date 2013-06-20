'''
Created on 2010/02/19

@author: jgall
'''

from __future__ import division

from AggMeasure import AggMeasure

class CoordsAsStat(AggMeasure):
    '''
    just to treat position as stats
    '''
    def __init__(self, qm, rq, **kw):
        '''
        Constructor
        '''
        kw['foldfunc'] = lambda x, y: 0
        kw['aggfunc'] = lambda x:0
        super(CoordsAsStat, self).__init__(qm, rq, **kw)
    
    def update(self):
        pass
    
    def __str__(self):
        rq = self.ref_quad
        return "P1({0},{1}),P2({2},{3})".format(rq.x1, rq.y1, rq.x2, rq.y2)
