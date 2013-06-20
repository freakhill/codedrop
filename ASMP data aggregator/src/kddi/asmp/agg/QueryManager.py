'''
Created on 2010/02/18

@author: jgall
'''

import _mysql as M

class QueryManager(object):
    '''
    classdocs
    '''
    db = None

    def __init__(self, host='localhost', login='pyscript', password='pyscript', database='field_exp_bcnf'):
        '''
        Constructor
        '''
        self.db = M.connect(host, login, password, database)
        
    def cdma_data(self, columns, **kw):
        '''
        1st param 'columns', string containing the name of the columns you
        want returned (sql style)
        
        returns a mysql result object from which you can fetch a tuple
        of tuples containing required values as strings
        '''
        pos_condition = "mbrcontains(square({0},{1},{2},{3}),lon_lat)".format(kw['x1'], kw['y1'], kw['x2'], kw['y2']) if 'x1' in kw else "true"
        neighbour_condition = "is_neighbour={0}".format(kw['is_neighbour']) if 'is_neighbour' in kw else "true"
        date_condition = "unixtime between {0} and {1}".format(kw['starttimestamp'], kw['stoptimestamp']) if 'starttimestamp' in kw else "true"
        index_tip = "use index (gpsindex)" if 'x1' in kw else ""
        group_by = " group by {0}".format(kw['group_by']) if 'group_by' in kw else ""
        query = "select {0} from cdma_data_bcnf_myisam {1} where {2} and {3} and {4} {5}".format(columns, index_tip, pos_condition, neighbour_condition, date_condition, group_by)
        self.db.query(query)
        #print query
        return self.db.store_result()
