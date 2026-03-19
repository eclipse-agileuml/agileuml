import math
import re
import copy
import psutil
import time
import threading
import platform
import cpuinfo


from enum import Enum

def badaverage(lst) : 
  return sum(lst)/len(lst)
  
def average(lst) : 
  if lst : 
    return sum(lst)/len(lst)
  return 0

# print(badaverage([2,3,4]))
# print(badaverage([]))

class EnergyMonitor : 
  def __init__(self): 
    self.utilisations = []
    self.startTime = 0.0
    self.endTime = 0.0
    self.steps = 10

  def sampling(self) : 
    for _ in range(self.steps):
      self.utilisations.append(psutil.cpu_percent(interval=0.1))

  def startMonitoring(self, bound) :
    self.steps = bound*10 # 0.1 seconds  
    self.startTime = time.time()
    self.samplingThread = threading.Thread(target = self.sampling)
    self.samplingThread.start()

  def stopMonitoring(self) : 
    self.endTime = time.time() # seconds
    self.samplingThread.join()

  def getEnergy(self) : 
    recorded = len(self.utilisations)
    if recorded == 0 : 
      return 0
    
    tsecs = (self.endTime - self.startTime)    # secs
    tsteps = tsecs*10                   # tenths-of-second

    subsequence = self.utilisations[:int(tsteps)+1]
    recorded = len(subsequence)

    averageCPU = sum(subsequence)/recorded # percent utilisation
    mem = psutil.virtual_memory().used  # bytes
    cores = psutil.cpu_count()

    cpu = averageCPU/100
    t = tsecs*1000
    # print(t)
    memGB = mem/1000000000.0
    # print(memGB)
    # energy = t*(cpu*cores*10.8 + memGB*0.3725)
    return (t/3600.0)*(cpu*cores*10.8 + memGB*0.3725)

# print(psutil.cpu_count())

# print(psutil.virtual_memory().used)

def op() : 
  for x in range(10) : 
    print(average(range(x)))


print(platform.machine())

print(platform.platform())


mon = EnergyMonitor()

mon.startMonitoring(1)

op()

mon.stopMonitoring()
rate = mon.getEnergy()
print(rate)

# background = threading.Thread(target = op)
# background.start()

# for i in range(10) : 
#   print("step " + str(i))

# background.join()
# print("Threads joined")
# exit(0)

cpu_info = cpuinfo.get_cpu_info()
print(cpu_info)
if cpu_info:
   cpu_model_detected = cpu_info.get("brand_raw", "")
print(cpu_model_detected)    