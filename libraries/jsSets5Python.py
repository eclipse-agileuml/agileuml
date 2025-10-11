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


class FromJavaScript : 
  fromjavascript_instances = []
  fromjavascript_index = dict({})

  def __init__(self):
    self.mySet = set({})
    self.set1 = set({})
    FromJavaScript.fromjavascript_instances.append(self)



  def func_0(self, point) :
    if point.x > 10 :
      self.set1 = ocl.excludingSet(self.set1, point)
    else :
      pass

  def newFromJavaScript() :
    result = None
    result = createFromJavaScript()
    result.mySet = set({})
    result.set1 = set({})
    result.initialise()
    return result

  def initialise(self) :
    self.mySet = set({})
    self.mySet = ocl.includingSet(self.mySet, 1)
    self.mySet = ocl.includingSet(ocl.includingSet(self.mySet, 5), "some text")
    print(self.mySet)
    self.set1 = set({})
    self.set1 = ocl.includingSet(ocl.includingSet(self.set1, dict({"x":10,"y":20})), dict({"x":20,"y":30}))
    for point in self.set1 :
      self.func_0(point)
    print((len(self.set1)))

  def killFromJavaScript(fromjavascript_x) :
    fromjavascript_instances = ocl.excludingSet(fromjavascript_instances, fromjavascript_x)
    free(fromjavascript_x)

def createFromJavaScript():
  fromjavascript = FromJavaScript()
  return fromjavascript

def allInstances_FromJavaScript():
  return FromJavaScript.fromjavascript_instances


fromjavascript_OclType = createByPKOclType("FromJavaScript")
fromjavascript_OclType.instance = createFromJavaScript()
fromjavascript_OclType.actualMetatype = type(fromjavascript_OclType.instance)


fj = FromJavaScript.newFromJavaScript()
