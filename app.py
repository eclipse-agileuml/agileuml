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


class Person : 
  person_instances = []
  person_index = dict({})

  def __init__(self):
    self.name = ""
    self.age = 0
    Person.person_instances.append(self)



  def killPerson(person_x) :
    person_instances = ocl.excludingSet(person_instances, person_x)
    free(person_x)

class CC : 
  cc_instances = []
  cc_index = dict({})

  def __init__(self):
    self.sq = []
    CC.cc_instances.append(self)



  def op(self) :
    result = []
    result = sorted(self.sq, key = lambda self : self.name)
    return result

  def killCC(cc_x) :
    cc_instances = ocl.excludingSet(cc_instances, cc_x)
    free(cc_x)

def createPerson():
  person = Person()
  return person

def allInstances_Person():
  return Person.person_instances


person_OclType = createByPKOclType("Person")
person_OclType.instance = createPerson()
person_OclType.actualMetatype = type(person_OclType.instance)


def createCC():
  cc = CC()
  return cc

def allInstances_CC():
  return CC.cc_instances


cc_OclType = createByPKOclType("CC")
cc_OclType.instance = createCC()
cc_OclType.actualMetatype = type(cc_OclType.instance)


