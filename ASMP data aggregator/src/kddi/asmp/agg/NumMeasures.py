'''
Created on 2010/02/18

@author: jgall
'''

from AggMeasure import AggMeasure

class NumMeasures(AggMeasure):
    '''
    Statistic value for a quadtree containing the number of measures in a quad
    '''    
    def __init__(self, qm, rq, **kw):
        '''
        Constructor
        '''
        super(NumMeasures, self).__init__(qm, rq, **kw)
    
    def update(self):
        qm = self.query_manager
        rq = self.ref_quad
        r = qm.cdma_data("count(*)", is_neighbour=False,
                        x1=rq.x1, x2=rq.x2,
                        y1=rq.y1, y2=rq.y2)
        self.value = int(r.fetch_row()[0][0])
    
if __name__ == "__main__":
    from QueryManager import QueryManager as QM
    from QuadTree import QuadTree as QT
    from RSSI_GroupByCid import RSSI_GroupByCid
    from CoordsAsStat import CoordsAsStat
    qm = QM()
    #small
    #qt = QT(x1=139.795,y1=35.655,x2=139.799,y2=35.66)
    #full
    #qt = QT(x1=139.76,y1=35.65,x2=139.799,y2=35.69)
    #medium
    qt = QT(x1=139.76, y1=35.68, x2=139.76975, y2=35.69)
    nm = NumMeasures(qm, qt)
    rxl = RSSI_GroupByCid(qm, qt)
    qas = CoordsAsStat(qm, qt)
    qt.data['numpoints'] = nm
    qt.data['rssi'] = rxl
    qt.data['coords'] = qas
    nm.update()
    rxl.update()
    class d(object):
        i = 0
    def weregoingdown(qt):
        if qt.isLeaf():
            if qt.data['numpoints'].value > 400:
                #print 'unfolding' + str(d.i) + ' - ' + str(qt.data['points'].num)
                if not qt.unfold(1):
                    return
                d.i = d.i + 1
                qt.Qx(weregoingdown, tail=True)
                d.i = d.i - 1
                #print 'backtrack' + str(d.i)
        else:
            d.i = d.i + 1
            #print 'weregoingdown' + str(d.i)
            qt.Qx(weregoingdown, tail=True)
            d.i = d.i - 1
    weregoingdown(qt)
    #print qt.as_svg()
    print qt.as_n3d()
    
