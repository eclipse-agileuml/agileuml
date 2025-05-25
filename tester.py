import ocl
import math
import re
import copy

from mathlib import *
from oclfile import *
from ocltype import *
from ocldate import *
from oclprocess import *
from ocliterator import *
from ocldatasource import *
from enum import Enum

import app
import MutationTest




intTestValues = [0, -1, 1, -1025, 1024]
longTestValues = [0, -1, 1, -999999, 100000]
doubleTestValues = [0, -1, 1, 3125.0891, 0.0000000000000000000000001]
booleanTestValues = [False, True]
stringTestValues = ["", " abc_XZ ", "#�$* &~@'"]

MAXOBJECTS = 500



cc_count = len(app.CC.cc_instances)
cc_tailrec_counts = [0 for _x in range(0,100)]
cc_tailrec_totals = [0 for _y in range(0,100)]

for _ex in app.CC.cc_instances :
  MutationTest.MutationTest.tailrec_mutation_tests(_ex,cc_tailrec_counts, cc_tailrec_totals)
  print("")

for _idx in range(0,len(cc_tailrec_counts)) :
  if cc_tailrec_totals[_idx] > 0 :
    print("Test " + str(_idx) + " effectiveness: " + str(100.0*cc_tailrec_counts[_idx]/cc_tailrec_totals[_idx]) + "%")



