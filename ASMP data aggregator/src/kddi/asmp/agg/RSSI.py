'''
Created on 2010/02/19

@author: jgall
'''

from __future__ import division

from AggMeasure import AggMeasure

class RSSI(AggMeasure):
    '''
    Statistic value for a quadtree containing the rxl's mean
    '''
    def __init__(self, qm, rq, **kw):
        '''
        Constructor
        '''
        def foldfunc(x, y):
            if y is None:
                foldfunc.nonecount = foldfunc.nonecount + 1
                return x
            return x + y
        def aggfunc(x):
            res = (x / (4 - foldfunc.nonecount)) if foldfunc.nonecount != 4 else None
            foldfunc.nonecount = 0
            return res
        kw['foldfunc'] = foldfunc
        kw['aggfunc'] = aggfunc
        super(RSSI, self).__init__(qm, rq, **kw)
    
    def update(self):
        # should do a 2sigma measure but not necessary for now
        qm = self.query_manager
        rq = self.ref_quad
        #print rq.x1f(),rq.y1f(),rq.x2f(),rq.y2f()
        r = qm.cdma_data("avg(rxl)", is_neighbour=False,
                        x1=rq.x1, x2=rq.x2,
                        y1=rq.y1, y2=rq.y2)
        row = r.fetch_row()[0][0]
        self.value = None if row is None else float(row)
