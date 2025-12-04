
import ocl
import math
import re
import copy

from oclfile import *
from enum import Enum

# This is an optimised read-only iterator using tuples

class OclIterator :

  def __init__(self):
    self.position = 0
    self.markedPosition = 0
    self.elements = tuple()

  def getElements(self) : 
    return self.elements

  def getPosition(self) : 
    return self.position

  def hasNext(self) :
    result = False
    if self.position >= 0 and self.position < len(self.elements) :
      result = True
    else :
      result = False
    return result

  def isAfterLast(self) :
    result = False
    if self.position > len(self.elements) : 
      return True
    return result

  def isBeforeFirst(self) :
    result = False
    if self.position <= 0 : 
      return True
    return result

  def hasPrevious(self) :
    result = False
    if self.position > 1 and self.position <= len(self.elements) + 1 :
      result = True
    else :
      result = False
    return result

  def nextIndex(self) :
    result = 0
    result = self.position + 1
    return result

  def previousIndex(self) :
    result = 0
    result = self.position - 1
    return result

  def moveForward(self) :
    self.position = self.position + 1

  def moveBackward(self) :
    self.position = self.position - 1

  def moveTo(self, i) :
    self.position = i

  def setPosition(self, i) :
    self.position = i

  def markPosition(self) :
    self.markedPosition = self.position

  def movePosition(self, i) :
    self.position = i + self.position

  def moveToFirst(self) :
    self.position = 1

  def moveToLast(self) :
    self.position = len(self.elements)

  def moveToStart(self) :
    self.position = 0

  def moveToEnd(self) :
    self.position = len(self.elements) + 1

  def moveToMarkedPosition(self) :
    self.position = self.markedPosition

  def close(self) : 
    self.position = 0
    self.markedPosition = 0
    self.elements = tuple()

  def newOclIterator_Sequence(sq) :
    ot = OclIterator()
    ot.elements = tuple(sq)
    ot.position = 0
    return ot

  def getCurrent(self) :
    result = None
    if self.position <= len(self.elements) and self.position >= 1 : 
      result = (self.elements)[self.position - 1]
    return result

  def next(self) :
    result = None
    self.moveForward()
    return self.getCurrent()

  def previous(self) :
    result = None
    self.moveBackward()
    return self.getCurrent()

  def length(self) : 
    return len(self.elements)

  def at(self,i) : 
    return self.elements[i-1]


 




