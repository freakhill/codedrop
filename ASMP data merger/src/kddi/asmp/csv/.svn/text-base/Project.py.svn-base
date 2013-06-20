'''
Created on 2009/12/08

@author: jgall
'''

# could use buffedred IO to drastically improve performance
# see http://neopythonic.blogspot.com/2008/10/sorting-million-32-bit-integers-in-2mb.html

from __future__ import with_statement
import sys, re

# CARE : INPUTS ARE NOT SANITIZED!

# incredibely unsure program for fun :p
# there is no fail management implemented, it will crash hard with
# wrong parameters!

class Project(object):
    '''
    classdocs
    '''
    
    file = None
    columns = []
    
    
    def add(self, *params):
        """
        select only columns defined in params and print them
        """
        file = self.file
        strline = file.readline()[:-1]
        line = re.split(',', strline)
        column_ids = [x[0] for x in enumerate(line) if x[1] in params]
        while strline:
            res = []
            for i in column_ids:
                res.append(line[i])
            print ",".join(res)
            strline = file.readline()[:-1]
            line = re.split(',', strline)
        return True
    
    def remove(self, *params):
        """
        select columns defined in params and print the other ones
        """
        file = self.file
        strline = file.readline()[:-1]
        line = re.split(',', strline)
        column_ids = [x[0] for x in enumerate(line) if x[1] in params]
        while strline:
            for i in sorted(column_ids, reverse=True):
                del line[i]
            print ",".join(line)
            strline = file.readline()[:-1]
            line = re.split(',', strline)
        return True
    
    def fuse(self, *params):
        """
        select columns defined in params and fuse their data with
        ' ' as the join token
        print the other columns and then the joined column 
        """
        file = self.file
        strline = file.readline()[:-1]
        line = re.split(',', strline)
        column_ids = [x[0] for x in enumerate(line) if x[1] in params]
        while strline:
            fused = [line[i] for i in column_ids]
            fused = " ".join(fused)
            for i in sorted(column_ids, reverse=True):
                del line[i]
            line.append(fused)
            print ",".join(line)
            strline = file.readline()[:-1]
            line = re.split(',', strline)
        return True
        
    mode_func_dict = {'add' : add, '+' : add,
                      'remove' : remove, '-' : remove, 'del' : remove, 'delete' : remove,
                      'fusion' : fuse, 'fuse' : fuse, 'fusefields' : fuse, 'f' : fuse }

    def __init__(self, *params):
        """
        Constructor
        *params must be in the form  filter_name filename.csv filter_param1 filter_param2 etc. 
        """
        self.func = self.mode_func_dict[params[0]]
        self.file = params[1]
        self.columns = params[2:]
        
    def output(self):
        with open(self.file) as self.file:
            return self.func(self, *self.columns)


if __name__ == "__main__":
    app = Project(*sys.argv[1:])
    if app.output():
        sys.stderr.write("Project success!")
    else:
        sys.stderr.write("Project fail!")
