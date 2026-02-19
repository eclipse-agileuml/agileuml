import ocl
import math
import re
import copy
import time

from enum import Enum


class Matrix : 
  
  def __init__(self, sh):
    self.elements : list[float] = []
    self.size = 0
    self.shape = sh
    self.factors = []

    prd : int = 1
    for i in range(0,len(sh)) : 
      dim = sh[i]
      prd = prd*dim
    
    self.size = prd
    for i in range(0,prd) :
      self.elements.append(0.0) 

    self.factors.append(prd)
    for i in range(1,len(sh)+1) : 
      prd = prd//sh[i-1]
      self.factors.append(prd)


  def reshape(self, sh) : 
    self.shape = sh
    self.factors = []

    prd : int = 1
    for i in range(0, len(sh)) : 
      dim = sh[i]
      prd = prd*dim

    self.factors.append(prd)
    for i in range(1, len(sh)+1) : 
      prd = prd//sh[i-1]
      self.factors.append(prd)

    return self


  def singleValueMatrix(sh, value) : 
    res = Matrix(sh)
    for ind in range(0, res.size) : 
      res.elements[ind] = value

    return res


  def set(self, indexes, value) :
    position = 0; 
    for i in range(0, len(indexes)-1) :  
      position = position + indexes[i]*self.factors[i+1]

    position = position + indexes[len(indexes)-1]  
    self.elements[position] = value 
  
  def get(self, indexes) :
    position = 0 
    for i in range(0, len(indexes)-1) : 
      position = position + indexes[i]*self.factors[i+1]
    position = position + indexes[len(indexes)-1]  
    return self.elements[position]

  def multrowcolumn(self, i, j) :
    res = 0.0 
    dim = self.factors[1] 
    for k in range(0, dim) :  
      res = res + self.elements[i*dim + k]*self.elements[j + k*dim]
    return res 

  def square(self) :
    dim = self.factors[1] 
    sq = Matrix([dim, dim]) 
    for i in range(0, dim) : 
      for j in range(0, dim) :  
        sq.set([i,j], self.multrowcolumn(i,j))
    return sq



# sze = 80
# mm = Matrix([sze, sze])

# for i in range(0,sze) : 
#   for j in range(0,sze) : 
#     mm.set([i,j], i+j)

# t0 = time.time()

# sq = mm.square()

# t1 = time.time()

# print(str(1000*(t1-t0)))


sze = 80
t1 = time.time()

m1 = Matrix.singleValueMatrix([sze,sze,sze], 10.0)
m2 = m1.reshape([sze//2, 2, sze//2, 2, sze])

t2 = time.time() 
print(str(1000*(t2 - t1)))
