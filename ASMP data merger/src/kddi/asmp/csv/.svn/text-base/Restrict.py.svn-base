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

class Restrict(object):
    '''
    classdocs
    '''
    
    file = None
    columns = []
    
    
    def equal(self, *params):
        """
        print lines until the column param[0] holds the value param[1]
         [or param[2]]*
        You can simulate and conditions by chaining Restrict calls
        (for equal conditions!)
        """
        file = self.file
        strline = file.readline()[:-1]
        line = re.split(',', strline)
        column_ids = [x[0] for x in enumerate(line) if x[1] == params[0]]
        num_or_conditions = len(params) - 1
        if len(column_ids) == 0:
            sys.stderr.write("No column " + params[0] + " in the file!")
            return False
        column_id = column_ids[0]
        while strline:
            print strline
            line = re.split(',', strline)
            for i in range(num_or_conditions):
                if line[column_id] == params[1 + i]:
                    return True
            strline = file.readline()[:-1]
        return True
        
    mode_func_dict = {'=' : equal, 'equal' : equal,
                      }

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
    app = Restrict(*sys.argv[1:])
    if app.output():
        sys.stderr.write("Restrict success!")
    else:
        sys.stderr.write("Restrict fail!")
