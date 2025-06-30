# OCL library ocl.js


static function sqr(x)  
{ return x*x; }

static function sqrt(x)  
{ return Math.sqrt(x); }

static function cbrt(x)  
{ return Math.cbrt(x); }

static function abs(x)  
{ return Math.abs(x); } 

static function gcd(xx,yy) 
{ var x = Math.abs(xx);
  var y = Math.abs(yy); 
  while (x != 0 && y != 0) 
  { z = y; 
    y = x % y; 
    x = z;
  }

  if (y == 0) 
  { return x; }
  
  if (x == 0) 
  { return y; }
 
  return 0;
}

static function roundTo(x,n) 
{ if (n == 0) 
  { return Math.round(x); }
  var y = x*Math.pow(10,n); 
  return Math.round(y)/Math.pow(10,n);
} 

static function truncateTo(x,n) 
{ if (n <= 0) 
  { return Math.trunc(x); }
  var y = x*Math.pow(10,n); 
  return Math.trunc(y)/Math.pow(10,n); 
}


static function isInteger(sx)  
{ var ss = "" + sx; 
  if (("" + parseInt(ss)) == ss) 
  { return true; } 
  return false; 
}

static function isLong(sx)  
{ return isInteger(sx); } 

static function isReal(sx)  
{ if (isInteger(sx)) 
  { return true; } 
  var ss = "" + sx; 
  if (("" + parseFloat(ss)) == ss) 
  { return true; } 
  return false; 
} 

function toInteger(sx) 
{ return parseInt("" + sx); }

static function toLong(sx) 
{ return parseInt("" + sx); }

static function toReal(sx) 
{ return parseFloat("" + sx); }

static function toBoolean(xx)  
{ if (xx == null || xx == 0 || xx == "") 
  { return false; } 
  if (xx == undefined) 
  { return false; } 
  if (Number.isNaN(xx)) 
  { return false; }
  if (("" + xx) == "false") 
  { return false; } 
  if (("" + xx) == "true") 
  { return true; } 
  return null; 
} 


static function char2byte(ch)  
{ if (ch == "") 
  { return -1; } 
  return ch.charCodeAt(0); 
} 

static function byte2char(bb)  
{ if (bb == -1) 
  { return ""; } 
  return String.fromCharCode(bb);  
} 

static function ord(lit)  
{ if ((typeof lit) == "string")  
  { return char2byte(lit); } 

  if ((typeof lit) == "number")
  { return lit; } 
  return null; 
}  

static function succ(lit)  
{ if ((typeof lit) == "string") 
  { var litord = char2byte(lit); 
    return byte2char(litord + 1); 
  } 

  if ((typeof lit) == "number")
  { return lit + 1; } 

  return null; 
} 

static function pred(lit)  
{ if ((typeof lit) == "string") 
  { var litord = char2byte(lit); 
    return byte2char(litord - 1); 
  } 

  if ((typeof lit) == "number")
  { return lit - 1; }
 
  return null; 
} 

static function asBag(sq)  
{ return sortSequence(sq); } 
// a bag is a sorted sequence

def mapAsBag(mp) :
  # mp of type Map(T,int)
  res = [] 
  for k in mp : 
    n = mp[k]
    for i in range(0,n) : 
      res.append(k)
  return res
    
def asOrderedSet(sq) :
  elems = set([])
  res = []
  for x in sq :
    if x in elems :
      pass 
    else :
      res.append(x)
      elems.add(x)
  return res


def isUnique(s) : 
  return len(set(s)) == len(s)

def any(s) : 
  lst = list(s)
  if 1 <= len(lst) : 
    return lst[0]
  return None

def anyMap(s) : 
  for k in s : 
    return s[k]
  return None

def includesAll(supset,subset) :
  for x in subset :
    if x in supset :
      pass
    else :
      return False
  return True


def excludesAll(supset,subset) :
  for x in subset :
    if x in supset :
      return False
  return True

def includesValue(mp, x) : 
  for k in mp : 
    if mp[k] == x : 
      return True
  return False

def excludesValue(mp, x) : 
  for k in mp : 
    if mp[k] == x : 
      return False
  return True

def iterate(sq,init,f) : 
  acc = init
  for x in sq : 
    acc = f(x,acc)
  return acc

def exists(col,f) :
  for x in col :
    if f(x) :
      return True
  return False


def forAll(col,f) :
  for x in col :
    if f(x) :
      pass
    else :
      return False
  return True


def exists1(col,f) :
  found = False
  for x in col :
    if f(x) :
      if found :
        return False
      else :
        found = True
  return found


def toLowerCase(str) :
  s = '' + str
  return s.lower()


def toUpperCase(str) :
  s = '' + str
  return s.upper()


def before(s,sep) : 
  if len(sep) <= 0 : 
    return s
  i = s.find(sep)
  if i < 0 : 
    return s
  else : 
    return s[0:i]

def after(s,sep) : 
  j = len(sep)
  if j <= 0 : 
    return ""
  i = s.find(sep)
  if i < 0 : 
    return ""
  else : 
    return s[i+j:]


def characters(str) :
  res = []
  for x in str :
    res.append('' + x)
  return res


def insertAtString(x,i,s) :
  # i must be > 0
  if i <= 0 : 
    return x
  x1 = x[0:i-1]
  x2 = x[i-1:]
  return x1 + s + x2

def setAtString(x,i,s) :
  # i must be > 0, s has length 1
  if i <= 0 : 
    return x
  x1 = x[0:i-1]
  x2 = x[i:]
  return x1 + s + x2

def reverseString(st) :
  if len(st) == 0 : 
    return st
  else :
    return reverseString(st[1:]) + st[0:1]
  

def subtractString(s1,s2) :
  res = ''
  for x in s1 :
    if x in s2 :
      pass
    else :
      res = res + x
  return res


def equalsIgnoreCase(s1,s2) :
  result = False
  if toLowerCase(s1) == toLowerCase(s2) :
    result = True
  return result

def compareTo(s1,s2) : 
  result = 0
  if s1 < s2 : 
    return -1
  if s1 > s2 : 
    return 1
  return result

def indexOfSequence(sq,x) : 
  if x in sq : 
    return sq.index(x) + 1
  else : 
    return 0

def lastIndexOfSequence(sq,x) : 
  if x in sq : 
    sqr = reverseSequence(sq)
    i = indexOfSequence(sqr,x)
    return len(sq) - i + 1
  else : 
    return 0

def lastIndexOf(s,d) :
  result = 0
  dlen = len(d)
  if dlen == 0 : 
    return 0
  i = (reverseString(s).find(reverseString(d)) + 1)
  if i <= 0 :
    result = 0
  else :
    if i > 0 :
      result = len(s) - i - dlen + 2
  return result

def lastIndexOfString(s, d) : 
  return lastIndexOf(s,d)

def hasPrefixSequence(sq,sq1,j) : 
  m = len(sq1)
  n = len(sq)
  if m == 0 or n == 0 or n < j+m : 
    return False
  i = 0
  while i < m and i+j < n : 
    if sq[i+j] != sq1[i] : 
      return False
    i = i+1
  return True

def indexOfString(ss,sub) : 
  n = ss.find(sub)
  return n + 1

def indexOfSubSequence(sq,sq1) : 
  m = len(sq1)
  n = len(sq)
  if m == 0 or n == 0 or n < m : 
    return 0
  i = 0
  while i+m <= n : 
    if hasPrefixSequence(sq,sq1,i) : 
      return i+1
    i = i + 1
  return 0


def lastIndexOfSubSequence(sq,sq1) :
  m = len(sq1)
  n = len(sq)
  if m == 0 or n == 0 or n < m : 
    return 0
  rsq = reverseSequence(sq)
  rsq1 = reverseSequence(sq1)
  i = indexOfSubSequence(rsq,rsq1)
  if i <= 0 :
    return 0
  return n - i - m + 2
 
def at(sq,i) : 
  if i <= 0 : 
    return None
  if len(sq) > i : 
    return None
  return sq[i-1]

def first(sq) : 
  if len(sq) == 0 : 
    return None
  return sq[0]

def last(sq) : 
  if len(sq) == 0 : 
    return None
  return sq[len(sq)-1]

def front(sq) :
  if len(sq) == 0 : 
    return sq 
  return sq[:-1]

def tail(sq) :
  if len(sq) == 0 : 
    return sq 
  return sq[1:]

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


def trim(str) : 
  res = '' + str
  return res.strip()


def replace(str,sub,rep) : 
  res = '' + str
  return res.replace(sub,rep)


def splitByAll(str, delimiters) :
  if 0 < len(delimiters) :  
    delim = delimiters[0] 
    taildelims = (delimiters)[1:]
    splits = str.split(delim)
    res = [] 
    for st in splits : 
      if len(st) > 0 : 
        res.extend(splitByAll(st, taildelims))
    return res
  else : 
    result = [] 
    result.append(str) 
    return result


def split(str, pattern) : 
  splits = re.split(pattern,str)
  res = [] 
  for st in splits : 
    if len(st) > 0 : 
      res.append(st)
  return res


def isMatch(str, pattern) : 
  if re.fullmatch(pattern,str) == None : 
    return False
  else : 
    return True

def hasMatch(str, pattern) : 
  if re.search(pattern,str) == None : 
    return False
  else : 
    return True


def firstMatch(str, pattern) : 
  m = re.search(pattern,str)
  if m == None :  
    return None
  else : 
    return m.group()


def allMatches(str, pattern) : 
  res = []
  res.extend(re.findall(pattern,str)) 
  return res
  

def replaceAll(str, pattern, repl) : 
  res = '' + str
  res = re.sub(pattern, repl, res)
  return res


def replaceAllMatches(str, pattern, repl) : 
  res = '' + str
  res = re.sub(pattern, repl, res)
  return res


def replaceFirstMatch(str, pattern, repl) : 
  res = '' + str
  res = re.sub(pattern, repl, res, 1)
  return res


def insertInto(x,i,s) :
  # i must be > 0
  if i <= 0 : 
    return x
  x1 = x[0:i-1]
  x2 = x[i-1:]
  x1.extend(s)
  x1.extend(x2)
  return x1

def excludingSubrange(x,i,j) :
  # i must be > 0, i <= j
  if i <= 0 : 
    return x
  if i > j : 
    return x
  x1 = x[0:i-1]
  x2 = x[j:]
  x1.extend(x2)
  return x1

def setSubrange(x,i,j,v) :
  # Only for strings. i must be > 0, i <= j
  if i <= 0 : 
    return x
  if i > j : 
    return x
  x1 = x[0:i-1] + str(v) + x[j:]
  return x1

def sequenceSubrange(l, i, j) :
  # For lists. OCL indexing used for i, j
  result = []
  if j < 0 : 
    j = len(l) + j
  # eg: -1 is the last element of l
  if i == 0 : 
    i = 1
  if i < 0:
    i = len(l) + i
  
  for k in range(i-1, j):
    result.append(l[k])
  return result


def insertAt(x,i,s) :
  # i must be > 0
  if i <= 0 : 
    return x
  sq = []
  sq.extend(x)
  sq.insert(i-1,s)
  return sq


def setAt(sq,i,val) : 
  res = []
  res.extend(sq)
  if i >= 1 and i <= len(sq) : 
    res[i-1] = val
  return res

def removeAt(sq,i) : 
  res = []
  res.extend(sq)
  if i >= 1 and i <= len(sq) :
    del res[i-1]
  return res

def setAtMap(sq,k,val) : 
  res = sq.copy()
  res[k] = val
  return res

def removeAtMap(sq,k) : 
  res = sq.copy()
  del res[k]
  return res

def removeAtString(ss,i) : 
  res = "" + ss
  if i == 1 and i <= len(ss) : 
    return res[1:]
  if i >= 2 and i <= len(ss) :
    return ss[0:(i-1)] + ss[i:]
  return res

def excludingAtString(ss,i) : 
  return removeAtString(ss,i)


def excludingFirst(sq,x) : 
  res = []
  res.extend(sq)
  try : 
    res.remove(x)
  except : 
    pass
  return res



def includingSequence(s,x) :
  res = []
  res.extend(s)
  res.append(x)
  return res

def includingSortedSequence(s,x) :
  res = s.copy()
  res.add(x)
  return res

def includingSet(s,x) :
  res = s.copy()
  res.add(x)
  return res

def includingSortedSet(s,x) :
  res = s.copy()
  res.add(x)
  return res


def excludeAllSequence(s1,s2) :
  res = []
  for x in s1 :
    if x in s2 :
      pass
    else :
      res.append(x)
  return res


def excludeAllSet(s1,s2) :
  res = s1.copy()
  return res.difference(s2)

def excludeAllSortedSet(s1,s2) :
  res = s1.copy()
  return res.difference(s2)


def excludingSequence(s,y) :
  res = []
  for x in s :
    if x == y :
      pass
    else :
      res.append(x)
  return res


def excludingSet(s,x) :
  res = s.copy()
  subtr = {x}
  return res.difference(subtr)

def excludingSortedSet(s,x) :
  res = s.copy()
  res.discard(x)
  return res

def prepend(s,x) :
  res = [x]
  res.extend(s)
  return res


def append(s,x) :
  res = []
  res.extend(s)
  res.append(x)
  return res


def union(s,t) :
  res = []
  res.extend(s)
  for x in t : 
    if x in s : 
      pass
    else : 
      res.append(x)
  return res


def unionSequence(s,t) :
  res = []
  res.extend(s)
  res.extend(t)
  return res


def unionSet(s,t) :
  res = s.copy()
  return res.union(t)

def unionSortedSet(s,t) :
  res = s.copy()
  return res.union(t)
 

def concatenate(s,t) :
  res = []
  res.extend(s)
  res.extend(t)
  return res
 

def intersectionSequence(s,t) :
  res = [x for x in s if x in t]
  return res


def intersectionSet(s,t) :
  res = s.copy()
  return res.intersection(t) 

def intersectionSortedSet(s,t) :
  res = s.copy()
  return res.intersection(t) 


def concatenateAll(sq) :
  res = []
  for s in sq : 
    res.extend(s)
  return res


def unionAll(sq) :
  res = any(sq)
  for s in sq : 
    res = res.union(s)
  return res



def intersectAllSet(sq) :
  res = any(sq)
  for s in sq : 
    res = res.intersection(s)
  return res


def intersectAllSequence(sq) :
  res = any(sq)
  for s in sq : 
    res = intersectionSequence(res,s)
  return res


def reverseSequence(s) :
  res = []
  res.extend(s)
  res.reverse()
  return res


def reverseSet(s) :
  return s.copy()


static function sortSequence(s) 
{ if (s.length == 0) 
  { return s; } 
  
  var s0 = s[0]; 

  if ((typeof s0) == number)
  { return s.toSorted((a, b) => a - b); } 
  
  return s.toSorted(); 
} 


def sortSet(s) :
  return s.copy()

def sortMap(s) :
  ks = sorted(s)
  m = dict({})
  for k in ks : 
    m[k] = s[k]
  return m


def sortedBy(s, fields) : 
  n = len(fields)
  p = s.copy()
  for i in range(1,n+1) : 
    p = sorted(p, key = fields[n-i])
  return p


def selectSet(s,f) : 
  return set({x for x in s if f(x)})

def selectSortedSet(s,f) : 
  return SortedSet({x for x in s if f(x)})

def selectSequence(s,f) : 
  return [x for x in s if f(x)]

def rejectSet(s,f) : 
  return set({x for x in s if not f(x)})

def rejectSortedSet(s,f) : 
  return SortedSet({x for x in s if not f(x)})

def rejectSequence(s,f) : 
  return [x for x in s if not f(x)]

def collectSet(s,f) : 
  return set({f(x) for x in s})

def collectSequence(s,f) : 
  return [f(x) for x in s]

def any1(s,f) : 
  for x in s : 
    if f(x) : 
      return x
  return None



def selectMaximalsSequence(col,f) :
  result = []
  if len(col) == 0 :
    return result
  maximal = f(col[0])
  result = [col[0]]
  for x in col[1:] :
    value = f(x)
    if value > maximal :
      result = [x]
      maximal = value
    else : 
      if value == maximal :
        result.append(x)
  return result
 

def selectMaximalsSet(col,f) :
  result = {}
  elems = col.copy()
  if len(col) == 0 :
    return result
  x = elems.pop()
  maximal = f(x)
  result = {x}
  for y in elems :
    value = f(y)
    if value > maximal :
      result = {y}
      maximal = value
    else : 
      if value == maximal :
        result.add(y)
  return result


def selectMinimalsSequence(col,f) :
  result = []
  if len(col) == 0 :
    return result
  minimal = f(col[0])
  result = [col[0]]
  for x in col[1:] :
    value = f(x)
    if value < minimal :
      result = [x]
      minimal = value
    else : 
      if value == minimal :
        result.append(x)
  return result
 

def selectMinimalsSet(col,f) :
  result = {}
  elems = col.copy()
  if len(col) == 0 :
    return result
  x = elems.pop()
  minimal = f(x)
  result = {x}
  for y in elems :
    value = f(y)
    if value < minimal :
      result = {y}
      minimal = value
    else : 
      if value == minimal :
        result.add(y)
  return result
 

def minSequence(sq) :
  result = sq[0]
  for x in sq :
    if x < result :
      result = x
  return result


def minSet(col) :
  elems = col.copy()
  result = elems.pop()
  for x in elems :
    if x < result :
      result = x
  return result


def maxSequence(sq) :
  result = sq[0]
  for x in sq :
    if x > result :
      result = x
  return result


def maxSet(col) :
  elems = col.copy()
  result = elems.pop()
  for x in elems :
    if x > result :
      result = x
  return result


def sum(col) : 
  result = 0
  for x in col : 
    result = result + x
  return result

def sumint(col) : 
  return sum(col)

def sumlong(col) : 
  return sum(col)

def sumdouble(col) : 
  result = 0.0
  for x in col : 
    result = result + x
  return result

def sumString(col) : 
  result = ""
  for x in col : 
    result = result + x
  return result


def prd(col) : 
  result = 1
  for x in col : 
    result *= x
  return result


def includesAllMap(supset,subset) :
  for x in subset :
    if x in supset :
      if subset[x] != supset[x] :
        return False       
    else :
      return False
  return True



def excludesAllMap(supset,subset) :
  for x in subset :
    if x in supset :
      if subset[x] == supset[x] :
        return False
  return True



def existsMap(m,f) :
  for x in m :
    if f(m[x]) :
      return True
  return False


def forAllMap(m,f) :
  for x in m :
    if f(m[x]) :
      pass
    else :
      return False
  return True


def exists1Map(m,f) :
  found = False
  for x in m :
    if f(m[x]) :
      if found :
        return False
      else :
        found = True
  return found


def includingMap(m,x,y) :
  res = m.copy()
  res[x] = y
  return res


def excludeAllMap(s1,s2) :
  res = s1.copy()
  for x in s1 :
    if x in s2 :
      del res[x]
  return res


def excludingMapKey(m,y) :
  res = m.copy()
  if y in m :  
    del res[y]
  return res


def excludingMapValue(s,y) :
  res = s.copy()
  for x in s :
    if s[x] == y :
      del res[x]
  return res


def unionMap(s,t) :
  res = s.copy()
  for x in t :
    res[x] = t[x]
  return res

def unionAllMap(sq) :
  res = dict({})
  for s in sq : 
    res = unionMap(res,s)
  return res


def intersectionMap(s,t) :
  res = s.copy()
  for x in s :
    if x in t and s[x] == t[x] :
      pass
    else : 
      del res[x]
  return res

def intersectAllMap(sq) :
  res = dict({})
  if len(sq) == 0 : 
    return res
  res = sq[0]

  for s in sq : 
    res = intersectionMap(res,s)
  return res


def selectMap(m,p) :
  res = m.copy()
  for x in m :
    if p(m[x]) :
      pass
    else : 
      del res[x]
  return res


def rejectMap(m,p) :
  res = m.copy()
  for x in m :
    if p(m[x]) :
      del res[x]
  return res


def collectMap(m,e) :
  res = dict({})
  for x in m :
    res[x] = e(m[x])
  return res



def restrict(m,ks) :
  res = m.copy()
  for x in m :
    if x in ks :
      pass
    else :
      del res[x]
  return res


def antirestrict(m,ks) :
  res = m.copy()
  for x in m :
    if x in ks :
      del res[x]
  return res

def excludingAtMap(m,k) : 
  res = m.copy()
  if x in m : 
    del res[k]
  return res


def keys(m) : 
  res = set({})
  for x in m : 
    res.add(x)
  return res


def values(m) : 
  res = set({})
  for x in m : 
    res.add(m[x])
  return res


# sortedBy is used like this: 
# data = ["1111234", "1000234", "1000421", "1111012"]
# sdata = sortedBy(data, [lambda s : s[0:5], lambda s : s[5:8]])
# print(sdata)

# ss = "a long string"
# pp = setSubrange(ss, 3, 5, "and")
# print(pp)

# ss = [1, 4, 6, 7, 2]
# print(listSubrange(ss, 2, -1))

# mp = dict({"a": 1, "b": 2})

# print(excludesValue(mp,2))
# print(excludesValue(mp,4))

# mp = dict({"c" : 1, "b": 4, "a": 2})
# ms = sortMap(mp)
# print(ms)

# bg = mapAsBag(mp)
# print(bg)



