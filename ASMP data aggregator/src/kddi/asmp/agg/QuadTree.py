'''
Created on 2010/02/18

@author: jgall
'''
from __future__ import division

from LightweightSVG import Scene, Line, Text

class QuadTree(object):
    '''
    00 10 0 2 
    01 11 1 3
    '''
    Qf = None # father
    Q0, Q1, Q2, Q3 = None, None, None, None,
    
    # x1,y1 .
    #   .  x2,y2 (just to define a square)
    x1, y1, x2, y2 = 0, 0, 0, 0,
    # these position values could be treated as statistical data
    # but i wanted an example and I didn't want them to be overwritten
    # easily
    
    data = {} # dictionnary of statistic data
    # the objects stored must support the operations
    #  - fold(subdata, rank) - rank is 0,1,2 or 3
    #  - unfold(rank) - rank is 0,1,2,3
    
    unfold_forbidden = False

    def __init__(self, father=None, x1=0, y1=0, x2=0, y2=0, min_max_side=.000001):
        '''
        Constructor
        '''
        self.Qf = father
        self.x1, self.x2 = min(x1, x2), max(x1, x2)
        self.y1, self.y2 = min(y1, y2), max(y1, y2)
        
        max_side = max((self.x2 - self.x1), (self.y2 - self.y1))
        self.unfold_forbidden = True if max_side < min_max_side else False

        self.data = {}
    
    def isLeaf(self):
        return self.Q0 is None
    
    def as_n3d(self):
        return 'n3d.begin_graph()\n' + self._as_n3d() + 'n3d.end_graph()'
    
    def _as_n3d(self):
        nodename = 'qt' + str(id(self))
        nodedecl = "{0}='{1}/{2}'\n".format(nodename,
                                                 ' '.join([str(stat) for stat in self.data.values()]),
                                                 str(id(self))
                                                 )
        if self.Q0 is None:
            return nodedecl
        edges = ''.join(['n3d.add_edge({0},qt{1})\n'.format(nodename, str(id(q))) for q in [self.Q0, self.Q1, self.Q2, self.Q3]])
        return nodedecl + ''.join([q._as_n3d() for q in [self.Q0, self.Q1, self.Q2, self.Q3]]) + edges
    
    def as_svg(self):
        offset_x, offset_y = self.x1, self.y1
        #create the scene, draw the borders
        h = (self.y2 - self.y1) * 111310
        w = (self.x2 - self.x1) * 111320
        scene = Scene(height=h, width=w)
        scene.add(Line((0, 0), (0, h)))
        scene.add(Line((0, h), (w, h)))
        scene.add(Line((w, h), (w, 0)))
        scene.add(Line((w, 0), (0, 0)))
        # closuuuuuure
        def _as_svg(obj, scene, depth=0):
            side_x, side_y = obj.x2 - obj.x1, obj.y2 - obj.y1
            center_x, center_y = obj.x1 + side_x / 2, obj.y1 + side_y / 2
            # cross and text
            cxox = (center_x - offset_x) * 111320
            cyoy = (center_y - offset_y) * 111310
            scene.add(Line((cxox, (obj.y1 - offset_y) * 111310),
                           (cxox, (obj.y2 - offset_y) * 111310),
                           strokewidth=1 / (2 ** depth)))
            scene.add(Line(((obj.x1 - offset_x) * 111320, cyoy),
                           ((obj.x2 - offset_x) * 111320, cyoy),
                           strokewidth=1 / (2 ** depth)))
            scene.add(Text((cxox, cyoy),
                           ' '.join([str(stat) for stat in obj.data.values()]),
                           size=15 / (2 ** depth)))
            if not obj.Q0 is None:
                obj.Qx(lambda q: _as_svg(q, scene, depth + 1), tail=True)
        _as_svg(self, scene)
        return ''.join(scene.strarray())
        
    
    def Qx(self, func, tail=False):
        '''
		Apply function func to this node's sons Q0 Q1 Q2 and Q3 in this order
		If tail is defined to we ignore the returned value (to poorly simulate a tail call), else 
		Qi = func(Qi)
		'''
        self.Q0 = (func(self.Q0), self.Q0)[0 if not tail else 1]
        self.Q1 = (func(self.Q1), self.Q1)[0 if not tail else 1]
        self.Q2 = (func(self.Q2), self.Q2)[0 if not tail else 1]
        self.Q3 = (func(self.Q3), self.Q3)[0 if not tail else 1]
    
    def unfold(self, n):
        '''
        With a 1 km side square in 10 subdivisions we get a approx 1m resolution
        recursiveness won't make it explode on real world case
        '''
        if self.unfold_forbidden:
            return False
        if n == 0:
            return True
        Qx = self.Qx
        i = iter(range(4))
        # create sons
        # we will get cumulative fp approximations but it's simple
        # and good enough for low depths
        Qx(lambda Q : (lambda r : QuadTree(father=self,
                                           x1=self.x1 + (r >> 1) * (self.x2 - self.x1) / 2,
                                           y1=self.y1 + (r & 1) * (self.y2 - self.y1) / 2,
                                           x2=self.x1 + (1 + (r >> 1)) * (self.x2 - self.x1) / 2,
                                           y2=self.y1 + (1 + (r & 1)) * (self.y2 - self.y1) / 2))(i.next()))
        # put statistics in sons
        for key in self.data.keys():
            i = iter(range(4))
            Qx(lambda Q : Q.data.__setitem__(key, self.data[key].unfold(i.next())), tail=True)
        # unfold sons
        if n - 1 != 0:
            Qx(lambda Q : Q.unfold(n - 1), tail=True)
        return True
        
    def fold(self):
        Qx = self.Qx
        for key in self.data:
            i = iter(range(4))
            Qx(lambda Q : self.data[key].fold(Q.data[key], i.next()), tail=True)
        Qx(lambda x : None)
    
    
