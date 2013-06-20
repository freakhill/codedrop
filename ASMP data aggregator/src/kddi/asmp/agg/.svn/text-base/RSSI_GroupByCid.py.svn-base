'''
Created on 2010/02/23

@author: jgall
'''
from AggMeasure import AggMeasure

class RSSI_GroupByCid(AggMeasure):
    '''
    Statistic value for a quadtree containing the rxl's mean for each cid
    '''
    def __init__(self, qm, rq, **kw):
        '''
        Constructor
        '''
        def foldfunc(x, y):
            # we must have in nonecount and foldedvalue the
            #
            res = {}
            #1st case the added value is None
            if y is None:
                for k in foldfunc.nonecount.keys():
                    foldfunc.nonecount[k] = foldfunc.nonecount[k] + 1
                foldfunc.maxnonecount = foldfunc.maxnonecount + 1
                return x
            #2nd case, the added value contains some points
            #we treat the points which keys are considered in the aggregate
            for k in x:
                if k in y:
                    res[k] = x[k] + y[k]
                else:
                    foldfunc.nonecount[k] = foldfunc.nonecount[k] + 1 if k in foldfunc.nonecount[k] else 1
            #we treat the points yet not put in the aggregate
            for k in y:
                if not k in x:
                    res[k] = y[k]
                    foldfunc.nonecount[k] = foldfunc.maxnonecount
            foldfunc.maxnonecount = foldfunc.maxnonecount + 1
            return res
        foldfunc.nonecount = {}
        self.maxnonecount = 0
        def aggfunc(x):
            #should not happen, let it in commentary because we want to grab the error
            #if x is None:
            #    return None
            #normal treatment
            res = {}
            for k in x:
                res[k] = ((x[k] / (4 - foldfunc.nonecount[k])) if foldfunc.nonecount != 4 else None) if k in foldfunc.nonecount else x[k] / 4
            foldfunc.maxnonecount = 0
            foldfunc.nonecount = {}
            return res
        kw['foldfunc'] = foldfunc
        kw['aggfunc'] = aggfunc
        super(RSSI_GroupByCid, self).__init__(qm, rq, **kw)
        self.value = {}
    
    def update(self):
        # should do a 2sigma measure but not necessary for now
        qm = self.query_manager
        rq = self.ref_quad
        #print rq.x1f(),rq.y1f(),rq.x2f(),rq.y2f()
        r = qm.cdma_data("cid,avg(rxl)",
                        x1=rq.x1, x2=rq.x2,
                        y1=rq.y1, y2=rq.y2,
                        group_by='cid')
        rows = r.fetch_row(maxrows=0)
        if rows is None:
            self.value = None
        else:
            for row in rows:
                self.value[row[0]] = float(row[1])
                
    def __str__(self):
        if self.value is None or self.value == {}:
            return 'None'
        maxk = max(self.value, key=lambda x:self.value[x])
        return "cid/rxl->{0}/{1}".format(maxk, self.value[maxk])
