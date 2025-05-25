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
from oclrandom import *
from stringlib import *
from enum import Enum

import app

class MutationTest :

  def tailrec_mutation_tests_0(_self, _counts, _totals) :
    pass
    x = 0

    try :
      tailrec_result = _self.tailrec(x)
      print("Test 0 of tailrec on " + str(_self) + " result = " + str(tailrec_result))
    except:
      print("Unable to execute tailrec")
      return



  def tailrec_mutation_tests_1(_self, _counts, _totals) :
    pass
    x = -1

    try :
      tailrec_result = _self.tailrec(x)
      print("Test 1 of tailrec on " + str(_self) + " result = " + str(tailrec_result))
    except:
      print("Unable to execute tailrec")
      return



  def tailrec_mutation_tests_2(_self, _counts, _totals) :
    pass
    x = 1

    try :
      tailrec_result = _self.tailrec(x)
      print("Test 2 of tailrec on " + str(_self) + " result = " + str(tailrec_result))
    except:
      print("Unable to execute tailrec")
      return



  def tailrec_mutation_tests_3(_self, _counts, _totals) :
    pass
    x = 1024

    try :
      tailrec_result = _self.tailrec(x)
      print("Test 3 of tailrec on " + str(_self) + " result = " + str(tailrec_result))
    except:
      print("Unable to execute tailrec")
      return



  def tailrec_mutation_tests_4(_self, _counts, _totals) :
    pass
    x = -1025

    try :
      tailrec_result = _self.tailrec(x)
      print("Test 4 of tailrec on " + str(_self) + " result = " + str(tailrec_result))
    except:
      print("Unable to execute tailrec")
      return



  def tailrec_mutation_tests(_self, _counts, _totals) :
    pass
    MutationTest.tailrec_mutation_tests_0(_self,_counts,_totals)

    MutationTest.tailrec_mutation_tests_1(_self,_counts,_totals)

    MutationTest.tailrec_mutation_tests_2(_self,_counts,_totals)

    MutationTest.tailrec_mutation_tests_3(_self,_counts,_totals)

    MutationTest.tailrec_mutation_tests_4(_self,_counts,_totals)




