import ocl
import math
import re
import copy

from mathlib import *
from stringlib import *
from oclfile import *
from ocltype import *
from ocldate import *
from oclprocess import *
from ocliterator import *
from oclrandom import *
from ocldatasource import *
from sortedcontainers import *
from enum import Enum

def free(x):
  del x


def displayint(x):
  print(str(x))

def displaylong(x):
  print(str(x))

def displaydouble(x):
  print(str(x))

def displayboolean(x):
  print(str(x))

def displayString(x):
  print(x)

def displaySequence(x):
  print(x)

def displaySet(x):
  print(x)

def displayMap(x):
  print(x)

def firstSortedMap(mm) : 
  if len(mm) == 0 : 
    return None
  itm = mm.peekitem(0)
  lst = []
  lst.append(itm)
  res = SortedDict(lst)
  return res

def lastSortedMap(mm) : 
  if len(mm) == 0 : 
    return None
  itm = mm.peekitem(-1)
  lst = []
  lst.append(itm)
  res = SortedDict(lst)
  return res

def frontSortedMap(mm) :
  if len(mm) == 0 : 
    return mm
  res = mm.copy()
  res.popitem(-1) 
  return res

def tailSortedMap(mm) :
  if len(mm) == 0 : 
    return mm
  res = mm.copy()
  res.popitem(0) 
  return res


class CC : 
  cc_instances = []
  cc_index = dict({})

  def __init__(self):
    self.sq = SortedDict({"a":1,"b":2,"c":3})
    CC.cc_instances.append(self)



  def op(self) :
    dd = ocl.firstSortedMap(self.sq)
    print(dd)
    dd = ocl.lastSortedMap(self.sq)
    print(dd)
    # print(((self.sq)[0:-1]))
    # print(((self.sq)[0]))
    # print(((self.sq)[-1]))

  def killCC(cc_x) :
    cc_instances = ocl.excludingSet(cc_instances, cc_x)
    free(cc_x)

def createCC():
  cc = CC()
  return cc

def allInstances_CC():
  return CC.cc_instances


cc_OclType = createByPKOclType("CC")
cc_OclType.instance = createCC()
cc_OclType.actualMetatype = type(cc_OclType.instance)


xx = CC()
xx.op()

