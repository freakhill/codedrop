'''
Created on 2009/12/08

@author: jgall
'''

# could use buffedred IO to drastically improve performance
# see http://neopythonic.blogspot.com/2008/10/sorting-million-32-bit-integers-in-2mb.html

from __future__ import with_statement
import sys, re, os

# CARE : INPUTS ARE NOT SANITIZED!

class Join(object):
    '''
    classdocs
    '''
    
    # table containing [key, FileAndIndice] , the key is the name of the
    # column used for merge, the FileAndIndice is associated to the key's
    # associated file
    keycol_fni_table = []
    # table containing booleans telling if we've reached the pertinent file's
    # EOF, to dertermine which file is pertinent indices match with
    # keycol_fni_table ones.
    terminated = [] 
    
    id = 0 # id of the current line in the merged output
    merge_id = 0 # reference id used for the merge decision
    
    clean_object = True
    
    class FileAndIndice(object):
        file = None
        # line read in file followed by nextline
        currentline = ''
        # last line read in file 
        nextline = ''
        # index of the column which will be used as reference for merge
        keycolumn_index = 0  
        # names of the fields in the file
        fields = []
        
        def __init__(self, file):
            self.file = file

    def __init__(self, *params):
        """
        Constructor
        *params must be in the form  - keycolumn name, csv filename...
        """
        keys = params[0::2]
        files = params[1::2]
        xlen = range(len(keys))
        # populate the keycol_fni_table table
        self.keycol_fni_table = [[keys[i], self.FileAndIndice(files[i])] for i in xlen]
        # populate the associated terminated table
        self.terminated = [ False ] * len(xlen)
        
    def _recursive_with(self, i, imax, inside_exec):
        """
        This function recursively opens the files passed in parameters
        using with_statement and consequently replace the filename param
        in FileAndIndice objects by builtin File objects.
        Then it executes the callable inside_exec passed in parameter
        This is made for fun and in order to automatically manage file
        close operation.
        BEWARE : it can utterly break the cstack with deep recursivity!
        """
        if i > imax:
            sys.stderr.write("Recursive depth index i = " + str(i) + " > max index = " + str(imax) + "\n")
            return False
        if i < imax:
            with open(self.keycol_fni_table[i][1].file) as self.keycol_fni_table[i][1].file:
                return self._recursive_with(i + 1, imax, inside_exec)
        else:
            return inside_exec()
            
    def inside_merge_init(self):
        """
        In this function we consider than the FileAndIndice objects have been
        *_recursive_with*ed.
        """
        # start by setting the keycolumn indexes and reading the first line
        for i, [key, f_and_i] in enumerate(self.keycol_fni_table):
            # getting the header line index
            #removing the \n
            f_and_i.currentline = f_and_i.file.readline()[:-1]
            #splitting in fields
            f_and_i.currentline = re.split(',', f_and_i.currentline)
            #getting the index of the supposed strictly unique field with match our key
            f = [rank_and_key[0] 
                 for rank_and_key in enumerate(f_and_i.currentline) 
                 if rank_and_key[1] == key]
            if len(f) == 0:
                # Wrong parameters somewhere
                sys.stderr.write("Can't find column " + key + " in file " + f_and_i.file.name + "\n")
                return False
            if len(f) > 1:
                sys.stderr.write("Multiple identical column " + key + " in file " + f_and_i.file.name + "\n")
                sys.stderr.write("(we didn't check for other column names)\n")
                return False
            f_and_i.keycolumn_index = f[0]
            
            # preparing for the merge body
            f_and_i.fields = f_and_i.currentline # because it is!
            
            # getting the next line
            line = f_and_i.file.readline()
            if line == '':
                # EOF reached, we initialize a dummy line with blank fields 
                # in the following procedure the fields from this file
                # will simply loop on these default value
                # (read the code you'll see!)
                f_and_i.nextline = [""] * len(f_and_i.currentline)
                f_and_i.nextline[f_and_i.keycolumn_index] = "0"
                self.terminated[i] = True
            else:
                f_and_i.nextline = line[:-1]
                f_and_i.nextline = re.split(',', f_and_i.nextline)
        self.print_result_header()
        if all(self.terminated):
            return True
        for i in range(len(self.keycol_fni_table)):
            self.advanceLine(i)
        return self.inside_merge_body()
    
    def print_result_header(self):
        """
        This function is called with *_recursive_with*ed FileAndIndice objects
        and with these structures currentline pointing on CSV file headerline.
        It prints in stdout the merged CSV file header line. 
        """
        # we remove the .csv of the filenames
        headers = [[os.path.basename(fni.file.name)[:-4] + "_" + non_keycolumn_header
                    for non_keycolumn_header in fni.currentline[:fni.keycolumn_index] + 
                                                fni.currentline[fni.keycolumn_index + 1:]]
                    for key, fni in self.keycol_fni_table]
        # btw python won't cry on all the strange out of bounds slice comb
        
        # now fuse in one big list
        headers = [",".join(one_file_headers) for one_file_headers in headers]
        
        # add id and merge_id fields in the output
        headers.insert(0, "mergeid")
        headers.insert(0, "id")
        
        print ",".join(headers)
        
    def inside_merge_body(self):
        """
        Process files until we reach everyone's EOF.
        """
        # The first merge_id will be the smaller one
        self.merge_id = min([int(fni.currentline[fni.keycolumn_index])
                             for key, fni in self.keycol_fni_table])
        # Merge the first line
        self.merge_current_lines()
        while not all(self.terminated):
            if not self.merge_body_advance():
                sys.stderr.write("Fail to merge files on merge_id " + str(self.merge_id) + "\n")
                return False
        return True
        
    def merge_body_advance(self):
        # check for file which has the smaller increase on merge_id in non
        # terminated files, move it on line
        # table of diffs
        next_diffs = [int(fni.nextline[fni.keycolumn_index]) - self.merge_id
                        for key, fni in self.keycol_fni_table]
        #adding an int to all terminated lines so that they get big diffs
        non_terminated_id = 0
        for i in range(len(next_diffs)):
            if self.terminated[i]:
                if i == 0:
                    # the first diff is from a terminated file
                    # so we need to find a non terminated diff id
                    while self.terminated[non_terminated_id]:
                        non_terminated_id = non_terminated_id + 1
                # set to a non terminated file diff + 1
                next_diffs[i] = next_diffs[non_terminated_id] + 1
            else:
                non_terminated_id = i
                
        # getting the index of the smaller diff
        smaller_diff_index = min(enumerate(next_diffs), key=lambda index_and_diff : index_and_diff[1])[0]
        # moving the line for that index
        self.advanceLine(smaller_diff_index)
        smaller_diff_fni = self.keycol_fni_table[smaller_diff_index][1]
        self.merge_id = int(smaller_diff_fni.currentline[smaller_diff_fni.keycolumn_index])        
        # check for the other files which index is closer to merge_id, the current line
        # or the next one? advance line or not according to that
        next_diffs = [int(fni.nextline[fni.keycolumn_index]) - self.merge_id
                        for key, fni in self.keycol_fni_table]
        curr_diffs = [int(fni.currentline[fni.keycolumn_index]) - self.merge_id
                        for key, fni in self.keycol_fni_table]
        # if the diff between merge_id and next line's id is smaller than for the current line, move ffs! 
        move_or_not = map(lambda x, y : x < y, next_diffs, curr_diffs)
        for i, move_decision in enumerate(move_or_not):
            if(move_decision):
                self.advanceLine(i)
        # ok now that we have advanced all the lines that should have advanced
        return self.merge_current_lines()
        
    
    def merge_current_lines(self):
        line_fields = [fni.currentline[:fni.keycolumn_index] + fni.currentline[fni.keycolumn_index + 1:]
                        for key, fni in self.keycol_fni_table]
        # fuse in one big list
        line_fields = [",".join(one_file_fields) for one_file_fields in line_fields]
        # add the merge_id
        line_fields.insert(0, str(self.merge_id))
        line_fields.insert(0, str(self.id))
        self.id = self.id + 1
        # Print :D !
        print ",".join(line_fields)
        return True
    
    def advanceLine(self, i):
        fni = self.keycol_fni_table[i][1]
        fni.currentline = fni.nextline
        
        newline = fni.file.readline()
        if newline == '': #EOF
            # go to final state and stay constant :p
            fni.nextline = fni.currentline
            self.terminated[i] = True
        else:
            fni.nextline = newline[:-1]
            fni.nextline = re.split(',', fni.nextline)
        
    
    def wavemerge(self):
        # usually getrecursionlimit() gives something like 1000
        # I don't want to lose time getting the exact value that would let
        # program work well so i use getrecursionlimit() - 100
        # but if it proves to be a limitation, you can fine tune that number
        # or increase the recursion limit (with setrecursionlimit)
        # or remove recursion altogether (quite easy in this program)
        # but I don't think you'll go crazy merging thousands of files :D
        if len(self.terminated) > max(15, sys.getrecursionlimit() - 100):
            sys.stderr.write("Can't merge more than " + 
                             max(15, sys.getrecursionlimit() - 100) + 
                             " files with this program!\n")
            return False
        if sys.getrecursionlimit() < 15:
            sys.stderr.write("The recursion limit on this computer is smaller" + 
                             " than 15. This program could probably work on " + 
                             "this conf but wasn't checked for it. For safety" + 
                             "execution is desactivated.\n")
            return False
        
        if self.clean_object == False:
            sys.stderr.write("Join object used multiple times!\n")
            return False
        self.clean_object = False
        
        self.id = 0
        self.merge_id = 0
        return self._recursive_with(0, len(self.keycol_fni_table), self.inside_merge_init)


if __name__ == "__main__":
    app = Join(*sys.argv[1:])
    if app.wavemerge():
        sys.stderr.write("Join success!")
    else:
        sys.stderr.write("Join fail!")
