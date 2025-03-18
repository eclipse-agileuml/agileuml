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



person_count = len(app.Person.person_instances)
cc_count = len(app.CC.cc_instances)


