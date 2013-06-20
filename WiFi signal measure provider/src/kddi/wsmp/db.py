'''
Created on 2009/10/22

@author: jgall
'''
import sqlalchemy as sql
import sqlalchemy.orm as orm

import sqlalchemy.exceptions
from sqlalchemy.orm.exc import NoResultFound, MultipleResultsFound

import inspect

DbException = sqlalchemy.exceptions.SQLAlchemyError

class DBtable(object):
    def __init__(self, **dict):
        self.__dict__.update(dict)
        
    def isValidAsTable(self):
        return True

class AccessPoint(DBtable):
    pass

class ClientInterface(DBtable):
    pass

class Position(DBtable):
    pass

class Measures(DBtable):
    pass

class DbManager(object):
    '''
    Manages everything database related
    '''
    login = ''
    password = ''
    address = ''
    
    engine = None
    metadata = None
    conn = None
    sessionmaker = None
    session = None
    
    tables = {}
    
    connected = False
    mappers_initialized = False
    
    app = None
    
    def __init__(self, app, params):
        '''
        Constructor
        '''
        self.app = app
        
        self.metadata = sql.MetaData()
        self.sessionmaker = orm.sessionmaker()
        
        self.connected = False
        self.mappers_initialized = False
    
    def connect(self, login, password, address):
        '''
        connect to the wsmdb database with the given parameters
        disconnect from the currently used database (if there's one!)
        '''
        conn_string = "mysql://{0}:{1}@{2}/wsmdb".format(
                            login, password, address)
        
        if self.connected:
            self.disconnect()
        
        self.engine = sql.create_engine(conn_string)
        self.metadata.bind = self.engine
        self.metadata.create_all()
        
        # Check in the module for candidate classes for database mapping
        # it's simple their name is DBtablename
        # and then map them!
        for k, v in globals().items():
            if inspect.isclass(v) and (not (v is DBtable)):
                try:
                    v.isValidAsTable
                    self.tables[k] = sql.Table(k, self.metadata, autoload=True)
                except AttributeError:
                    pass
        
        self.sessionmaker.configure(bind=self.engine)
        self.session = self.sessionmaker()
        
        if not self.mappers_initialized:
            for k, v in self.tables.items():
                orm.mapper(globals()[k], v)
            self.mappers_initialized = True
        
        self.app.loadAndSetConfig('database', 'login', login)
        self.app.loadAndSetConfig('database', 'address', address)
        
        self.connected = True
        
    def insert(self, table_name, **dict):
        '''
        In fact it is a 'select or insert'
        '''
        theclass = globals()[table_name]
        query = self.session.query(theclass)
        
        # Create a query on the primary key
        pk = [ k.key for k in self.tables[table_name].primary_key ]
        for k, v in filter(lambda k : k[0] in pk, dict.items()):
            query = query.filter(theclass.__dict__[k] == v) 
        # huhu
        
        # Query
        try:
            res = query.one()
            print 'No Insert'
            print 'Res ->', res.id
            return res
        except NoResultFound:
            res = theclass(**dict)
            self.session.add(res)
            print 'Insert'
            return res
        except MultipleResultsFound:
            #??? we searched on a primary key!?
            pass
    
    def __getattr__(self, name):
        # if __getattribute__ couldn't find there isn't much choice possible
        if name.startswith('insert') and name[6:] in self.tables.keys():
            # this is an insert method that has yet to be lambdafied :P
            object.__setattr__(self, name,
                               lambda ** dict : self.insert(name[6:], **dict))
            return object.__getattribute__(self, name)
        else:
            raise AttributeError
        
    def commit(self):
        '''
        Commit the pending database operations
        '''
        if self.connected:
            self.session.commit()
    
    def flush(self):
        '''
        Flush the pending database operations (execute them!)
        '''
        if self.connected:
            self.session.flush()
    
    def disconnect(self):
        '''
        Disconnect us from any ongoing session/database
        '''
        
        if self.connected:
            self.commit()
            self.session.close()
        
            self.connected = False
        
    def isConnected(self):
        return self.connected
        
    def checkWindowsCompatibility(self, major, minor, build, platform, text):
        '''
        Should check the windows compatibility
        but for now just returns True... 
        '''
        return True
    
    shutdown = disconnect
