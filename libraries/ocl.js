class Ocl
{ static newList(...args)
  { var res = [];
    for (var x of args)
    { res.push(x); }
    return res;
  }

  static newSet(...args)
  { var res = new Set();
    for (var x of args)
    { res.add(x); }
    return res;
  }

  static newMap(...args)
  { var res = new Map();
    for (var x of args)
    { res.set(x.at(0), x.at(1)); }
    return res;
  }

  static size(col) 
  { var sq = Array.from(col);
    return sq.length;
  } // independent of col's type

  static at(col,i) 
  { var sq = Array.from(col);
    return sq[i-1];
  } // for strings and sequences

  static sqr(x)  
  { return x*x; }

  static sqrt(x)  
  { return Math.sqrt(x); }

  static cbrt(x)  
  { return Math.cbrt(x); }

  static abs(x)  
  { return Math.abs(x); } 

  static gcd(xx,yy) 
  { var x = Math.abs(xx);
    var y = Math.abs(yy); 
    while (x > 0 && y > 0) 
    { var z = y; 
      y = x % y; 
      x = z;
    }

    if (y == 0) 
    { return x; }
  
    if (x == 0) 
    { return y; }
 
    return 0;
  }

  static roundTo(x,n) 
  { if (n == 0) 
    { return Math.round(x); }
    var y = x*Math.pow(10,n); 
    return Math.round(y)/Math.pow(10,n);
  } 

  static truncateTo(x,n) 
  { if (n <= 0) 
    { return Math.trunc(x); }
    var y = x*Math.pow(10,n); 
    return Math.trunc(y)/Math.pow(10,n); 
  }

  static isInteger(sx)  
  { var ss = "" + sx; 
    if (("" + parseInt(ss)) == ss) 
    { return true; } 
    return false; 
  }

  static isLong(sx)  
  { return Ocl.isInteger(sx); } 

  static isReal(sx)  
  { if (Ocl.isInteger(sx)) 
    { return true; } 
    var ss = "" + sx; 
    if (("" + parseFloat(ss)) == ss) 
    { return true; } 
    return false; 
  } 

  static oclIsTypeOf(x,typ) 
  { if ("int" == typ || "long" == typ)
    { return Ocl.isInteger(x); }
    if ("double" == typ)
    { return Ocl.isReal(x); }

    var xtyp = (typeof x); 

    if ("boolean" == typ)
    { return xtyp == "boolean"; }

    if ("String" == typ)
    { return xtyp.startsWith("string"); }

    if (typ == "Sequence" || typ.startsWith("Sequence("))
    { return Array.isArray(x); }

    if (typ.startsWith("Function("))
    { return (xtyp == "function"); }
    if (typ.startsWith("Ref("))
    { return (xtyp == "symbol"); }
    if (typ == "Set" || typ.startsWith("Set("))
    { return x.toString().startsWith("[object Set"); }
    if (typ == "Map" || typ.startsWith("Map("))
    { return x.toString().startsWith("[object Map"); }
    if (x == null)
    { return typ == "void"; }
    if (xtyp == "object")
    { return (x instanceof typ); }
    return false;
  } 

  static oclType(x) 
  { if (Ocl.isInteger(x))
    { return "int"; }
    if (Ocl.isReal(x))
    { return "double"; }
    
    var typ = (typeof x); 

    if (typ == "string")
    { return "String"; }
    if (Array.isArray(x))
    { return "Sequence"; }
    if (typ == "function")
    { return "Function"; }
    if (x.toString().startsWith("[object Map"))
    { return "Map"; }
    if (x.toString().startsWith("[object Set"))
    { return "Set"; }
    if (typ == "symbol")
    { return "Ref"; }
    return (typeof x);
  }

  static copy(x) 
  { var typ = (typeof x); 
    
    if (typ == "number" || typ == "function" ||
        typ == "symbol")
    { return x; }    

    if (typ == "string")
    { return "" + x; }

    if (Array.isArray(x))
    { return [].concat(x); }

    if (x.toString().startsWith("[object Map"))
    { return new Map(x); }

    if (x.toString().startsWith("[object Set"))
    { return new Set(x); }

    var res = Object.assign({}, x);
    res.prototype = x.prototype; 
    return res;  
  }

  static toInteger(sx) 
  { return parseInt("" + sx); }

  static toLong(sx) 
  { return parseInt("" + sx); }

  static toReal(sx) 
  { return parseFloat("" + sx); }

  static toBoolean(xx)  
  { if (xx == null || xx == 0 || xx == "") 
    { return false; } 
    if (xx == undefined) 
    { return false; } 
    if (Number.isNaN(xx)) 
    { return false; }
    if (("" + xx) == "false") 
    { return false; } 
    if ((typeof xx) == "number")
    { return true; }
    if (("" + xx) == "true") 
    { return true; } 
    return null; 
  } 

  static char2byte(ch)  
  { if (ch == "") 
    { return -1; } 
    return ch.charCodeAt(0); 
  } 

  static byte2char(bb)  
  { if (bb == -1) 
    { return ""; } 
    return String.fromCharCode(bb);  
  } 

  static ord(lit)  
  { if ((typeof lit) == "string")  
    { return Ocl.char2byte(lit); } 

    if ((typeof lit) == "number")
    { return lit; } 

    return null; 
  }  // enum types are ints

  static succ(lit)  
  { if ((typeof lit) == "string") 
    { var litord = Ocl.char2byte(lit); 
      return Ocl.byte2char(litord + 1); 
    } 

    if ((typeof lit) == "number")
    { return lit + 1; } 

    return null; 
  } 

  static pred(lit)  
  { if ((typeof lit) == "string") 
    { var litord = Ocl.char2byte(lit); 
      return Ocl.byte2char(litord - 1); 
    } 

    if ((typeof lit) == "number")
    { return lit - 1; }
 
    return null; 
  } 

  static asBag(sq)  
  { return sq.toSorted(); } 
  // a bag is a sorted sequence

  static mapAsBag(mp) 
  { // mp of type Map(T,int)
    var res = []; 
    for (var [k, v] of mp) 
    { for (var _i = 0; _i < v; _i++) 
      { res.push(k); }
    }
    return res.toSorted();
  }
    
  static asOrderedSet(sq) 
  { var elems = new Set();
    var res = [];
    for (var x of sq)
    { if (elems.has(x)) {}
      else 
      { res.push(x);
        elems.add(x);
      }
    }
    return res;
  }

  static asSet(sq) 
  { var elems = new Set();
    for (var x of sq)
    { elems.add(x); }
    return elems;
  }

  static oclIsInvalid(x) 
  { return x === undefined || 
           Number.isNaN(x); 
  } 

  static oclIsUndefined(x) 
  { return x == null || x == undefined || 
           Number.isNaN(x); 
  } 

  static isUnique(s, f) 
  { var st = new Set();
    for (var x of s)
    { var y = f(x);
      if (st.has(y))
      { return false; }
      st.add(y);
    }
    return true;
  } // for any collection s

  static isUniqueMap(m, f) 
  { var st = new Set();
    for (var [k,x] of m)
    { var y = f(x);
      if (st.has(y))
      { return false; }
      st.add(y);
    }
    return true;
  }

  static any(s)  
  { var lst = Array.from(s);
    if (1 <= lst.length)  
    { return lst[0]; }
    return null;
  } // sets, sequences s

  static anyMap(s)  
  { for (var [k,v] of s) 
    { return v; }
    return null; 
  }

  static anyCollection(s, f)  
  { for (var v of s) 
    { if (f(v)) 
      { return v; }
    }
    return null;
  } // sets, sequences s

  static anyMapElement(s, f)  
  { for (var [k,v] of s) 
    { if (f(v)) 
      { return v; }
    }
    return null;
  }

  static includes(col, x)
  { for (var y of col)
    { if (x == y) 
      { return true; }
    }
    return false;
  }

  static excludes(col, x)
  { for (var y of col)
    { if (x == y) 
      { return false; }
    }
    return true;
  }

  static includesAllSet(supset,subset) 
  { for (var x of subset)
    { if (supset.has(x)) {}
      else 
      { return false; }
    }
    return true;
  }

  static includesAllSequence(supset,subset) 
  { for (var x of subset)
    { if (supset.includes(x)) {}
      else 
      { return false; }
    }
    return true;
  }

  static includesAll(supset,subset) 
  { for (var x of subset)
    { if (Ocl.includes(supset,x)) {}
      else 
      { return false; }
    }
    return true;
  }

  static excludesAllSet(supset,subset) 
  { for (var x of subset)
    { if (supset.has(x))
      { return false; }
    }
    return true;
  }

  static excludesAllSequence(supset,subset) 
  { for (var x of subset)
    { if (supset.includes(x))
      { return false; }
    }
    return true;
  }

  static excludesAll(supset,subset) 
  { for (var x of subset)
    { if (Ocl.includes(supset, x))
      { return false; }
    }
    return true;
  }


  static includesValue(mp, x) 
  { for (var [k,v] of mp) 
    { if (v == x) 
      { return true; }
    }
    return false;
  }

  static excludesValue(mp, x) 
  { for (var [k,v] of mp)
    { if (v == x)
      { return false; }
    }
    return true;
  }

  static iterate(sq,init,f)  
  { var acc = init;
    for (var x of sq) 
    { var g = f(x);
      acc = g(acc); 
    }
    return acc;
  }

  static exists(col,f) 
  { for (var x of col)
    { if (f(x))
      { return true; }
    }
    return false;
  }

  static forAll(col,f)   
  { for (var x of col)
    { if (f(x)) { }
      else 
      { return false; }
    }
    return true;
  }

  static exists1(col,f) 
  { var found = false;
    for (var x of col)
    { if (f(x))
      { if (found)
        { return false; }
        else 
        { found = true; }
      }
    }
    return found;
  }

  static toLowerCase(str) 
  { var s = "" + str;
    return s.toLowerCase();
  }

  static toUpperCase(str) 
  { var s = "" + str;
    return s.toUpperCase();
  }

  static before(s,sep) 
  { if (sep.length <= 0) 
    { return s; }
    var i = s.search(sep);
    if (i < 0)  
    { return s; }
    return s.substring(0,i);
  }

  static after(s,sep)  
  { var j = sep.length;
    if (j <= 0) 
    { return ""; }
    var i = s.search(sep);
    if (i < 0) 
    { return ""; }
    return s.substring(i+j, s.length);
  }

  static characters(str) 
  { var res = [];
    var n = str.length;
    for (var x = 0; x < n; x++)
    { res.push(str.at(x)); }
    return res;
  }

  static insertAtString(x,i,s) 
  { // 1-based index i
    if (i <= 0) 
    { return x; }
    var n = x.length;
    var x1 = x.substring(0,i-1);
    var x2 = x.substring(i-1, n);
    return x1 + s + x2;
  }

  static setAtString(x,i,s) 
  { // i must be > 0, s has length 1
    if (i <= 0) 
    { return x; }
    var n = x.length;
    var x1 = x.substring(0, i-1);
    var x2 = x.substring(i, n);
    return x1 + s + x2;
  }

  static reverseString(st) 
  { var res = "";
    var n = st.length;
    for (var x = n-1; x >= 0; x--)
    { res = res + st.at(x); }
    return res;
  }  

  static subtractString(s1,s2)
  { var res = ""; 
    var n = s1.length;
    for (var x = 0; x < n; x++)
    { var sx = s1.at(x); 
      var ind = s2.search(sx);
      if (ind >= 0) { }
      else 
      { res = res + sx; }
    }
    return res;
  }

  static equalsIgnoreCase(s1,s2) 
  { if (s1.toLowerCase() == s2.toLowerCase())
    { return true; }
    return false;
  }

  static compareTo(s1,s2) 
  { var result = 0;
    if (s1 < s2)  
    { return -1; }
    if (s1 > s2)
    { return 1; }
    return result;
  }

  static indexOf(sq,x)
  { var indx = sq.indexOf(x);
    return indx + 1;
  }

  static lastIndexOf(sq,x) 
  { var indx = sq.lastIndexOf(x);
    return indx + 1;
  } // strings or sequences


  static hasPrefixSubSequence(sq,sq1,j) 
  { // sq1 is a prefix of sq 
    // starting from position j

    var m = sq1.length;
    if (m == 0) 
    { return true; } 

    var n = sq.length;
    if (n == 0 ||
        n < j+m) 
    { return false; }

    var i = 0;
    while (i < m && i+j < n) 
    { if (sq[i+j] != sq1[i]) 
      { return false; } 
      i = i+1;
    }

    return true;
  }

  static hasPrefixSequence(sq,sq1)
  { if (sq1.length == 0) 
    { return true; } 
    return Ocl.hasPrefixSubSequence(sq,sq1,0); 
  }

  static hasSuffixSequence(sq,sq1)
  { var m = sq1.length;

    if (m == 0) 
    { return true; }
 
    var n = sq.length;

    if (n == 0 || n < m)
    { return false; }

    return Ocl.hasPrefixSubSequence(sq,sq1,n-m); 
  }

  static indexOfSubSequence(sq,sq1) 
  { var m = sq1.length;
    var n = sq.length;
    if (m == 0) 
    { return 1; } 

    if (n == 0 || 
        n < m) 
    { return 0; } // not found
    var i = 0;
    while (i+m <= n) 
    { if (Ocl.hasPrefixSubSequence(sq,sq1,i))
      { return i+1; }
      i = i + 1;
    }
    return 0;
  }


  static lastIndexOfSubSequence(sq,sq1) 
  { var m = sq1.length;
    var n = sq.length;

    if (m == 0) 
    { return n; } 

    if (n == 0 || 
        n < m) 
    { return 0; }

    var rsq = sq.toReversed();
    var rsq1 = sq1.toReversed();

    var i = Ocl.indexOfSubSequence(rsq,rsq1);
    if (i <= 0)
    { return 0; }

    return n - i - m + 2;
  }

  static first(sq)  
  { if (sq.length == 0) 
    { return null; }
    return sq.at(0);
  } // also for strings

  static last(sq) 
  { if (sq.length == 0) 
    { return null; }
    return sq.at(sq.length - 1);
  }

  static front(sq) 
  { if (sq.length == 0) 
    { return sq; } 
    return sq.slice(0, sq.length-1);
  }

  static tail(sq) 
  { if (sq.length == 0) 
    { return sq; } 
    return sq.slice(1,sq.length);
  }

  static firstMap(mm) 
  { if (mm.size == 0) 
    { return null; }
    for (var [i,v] of mm)
    { return v; }
  }

  static lastMap(mm) 
  { var res = null; 
    for (var [i,v] of mm)
    { res = v; }
    return res; 
  } // element, not a maplet


  static trim(str) 
  { var res = "" + str;
    return res.trim();
  }

  static replace(str,sub,rep) 
  { var res = "" + str;
    return res.replace(sub,rep);
  } // replaceFirstMatch

  static splitByAll(str, delimiters) 
  { if (0 < delimiters.length)   
    { var delim = delimiters[0]; 
      var taildelims = delimiters.slice(1);
      var splits = str.split(delim);
      var res = [] 
      for (var st of splits) 
      { if (st.length > 0) 
        { var spltall = Ocl.splitByAll(st, taildelims);
          res = res.concat(spltall);
        }
      }
      return res;
    }
    else 
    { var result = []; 
      result.push(str); 
      return result;
    }
  }

  static split(str, pattern) 
  { var splits = str.split(pattern);
    var res = []; 
    for (var st of splits) 
    { if (st.length > 0) 
      { res.push(st); }
    }
    return res;
  }

  static isMatch(str, pattern) 
  { var re = new RegExp(pattern);
    var mtchs = str.match(re);
    if (mtchs != null && mtchs.length > 0)
    { var mtch = mtchs.at(0);
      if (mtch == str)
      { return true; }
    }
    return false; 
  }

  static hasMatch(str, pattern) 
  { var re = new RegExp(pattern, 'g');
    var mtchs = str.match(re);
    if (mtchs != null && mtchs.length > 0)
    { return true; }
    return false;
  }

  static firstMatch(str, pattern) 
  { var re = new RegExp(pattern, 'g');
    var mtchs = str.match(re);
    if (mtchs != null && mtchs.length > 0)
    { return mtchs[0]; }
    return null;
  } 

  static allMatches(str, pattern) 
  { var re = new RegExp(pattern, 'g');
    var mtchs = str.match(re);
    if (mtchs != null && mtchs.length > 0)
    { return mtchs; }
    return [];
  }  

  static replaceAll(str, pattern, repl) 
  { var res = "" + str;
    res = res.replaceAll(pattern, repl);
    return res;
  }

  static replaceAllMatches(str, pattern, repl) 
  { return Ocl.replaceAll(str, pattern, repl); }

  static replaceFirstMatch(str, pattern, repl) 
  { var res = "" + str;
    res = str.replace(pattern, repl);
    return res;
  }

  static insertInto(x,i,s) 
  { // i must be > 0
    if (i <= 0) 
    { return x; }
    var x1 = x.slice(0,i-1);
    var x2 = x.slice(i-1);
    var res = x1.concat(s);
    return res.concat(x2);
  } // for sequences or strings

  static excludingSubrange(x,i,j)
  { // i must be > 0, i <= j
    if (i <= 0) 
    { return x; }
    if (i > j) 
    { return x; }
    var x1 = x.slice(0,i-1);
    var x2 = x.slice(j);
    return x1.concat(x2);
  } // ok for strings, sequences

  static setSubrange(x,i,j,v)
  { // sequences, strings. i must be > 0, i <= j
    if (i <= 0) 
    { return x; }
    if (i > j) 
    { return x; }
    var x1 = x.slice(0,i-1); 
    var x2 = x.slice(j); 
    return x1.concat(v).concat(x2);
  }

  static integerSubrange(i, j)
  { var result = [];
    for (var k = i; k <= j; k++)
    { result.push(k); }
    return result;
  }

  static insertAtSequence(x,i,elem) 
  { // i must be > 0
    if (i <= 0) 
    { return x; }
    var x1 = x.slice(0,i-1);
    x1.push(elem);
    var x2 = x.slice(i-1); 
    return x1.concat(x2);
  } // sequences

  static insertAtString(x,i,s) 
  { // i must be > 0
    if (i <= 0) 
    { return x; }
    var sq = x.slice(0,i-1);
    sq = sq + s;
    return sq.concat(x.slice(i-1));
  } // string

  static setAtSequence(sq,i,val)  
  { var res = sq;
    if (i >= 1 && i <= sq.length)  
    { res[i-1] = val; }
    return res;
  } // sequences

  static removeAt(sq,i)  
  { 
    if (i >= 1 && i <= sq.length)
    { return sq.slice(0,i-1).concat(sq.slice(i)); }
    return sq;
  } // ok for sequences, strings

  static setAtMap(sq,k,val) 
  { var res = new Map(sq);
    res.set(k, val);
    return res;
  }

  static excludingFirst(sq,x) 
  { var ind = sq.indexOf(x);
    if (ind < 0)
    { return sq; }
    var res = sq.slice(0,ind); 
    return res.concat(sq.slice(ind+1));
  } // ok for sequences, strings

  static includingSequence(s,x)
  { var res = [].concat(s);
    res.push(x);
    return res;
  }

  static includingSet(s,x) 
  { var res = new Set();
    res = res.union(s);
    res.add(x);
    return res;
  }

  static including(s, x)
  { if (Array.isArray(s)) 
    { return Ocl.includingSequence(s,x); }
    return Ocl.includingSet(s,x);
  }

  static excludeAllSequence(s1,s2)
  { var res = [];
    var st2 = Ocl.asSet(s2); 
    for (var x of s1)
    { if (st2.has(x))
      { }
      else  
      { res.push(x); }
    }
    return res;
  }

  static excludeAllSet(s1,s2) 
  { var res = new Set();
    var st2 = Ocl.asSet(s2); 
    res = res.union(s1);
    return res.difference(st2);
  }

  static excludeAll(s, x)
  { if (Array.isArray(s)) 
    { return Ocl.excludeAllSequence(s,x); }
    return Ocl.excludeAllSet(s,x);
  }

  static excludingSequence(s,y) 
  { var res = [];
    for (var x of s)
    { if (x == y) { }
      else
      { res.push(x); }
    }
    return res;
  }

  static excludingSet(s,x) 
  { var res = new Set();
    res = res.union(s);
    res.delete(x);
    return res;  
  }

  static excluding(s, x)
  { if (Array.isArray(s)) 
    { return Ocl.excludingSequence(s,x); }
    return Ocl.excludingSet(s,x);
  }

  static prepend(s,x) 
  { var res = [x];
    return res.concat(s);
  }

  static append(s,x) 
  { var res = [].concat(s)
    res.push(x);
    return res;
  }

  static unionSequence(s,t)
  { var res = [].concat(s);
    var tsq = Array.from(t); 
    return res.concat(tsq);
  }

  static unionSet(s,t) 
  { var res = new Set();
    var st = Ocl.asSet(t); 
    res = res.union(s);
    return res.union(st);
  }

  static union(s, x)
  { if (Array.isArray(s)) 
    { return Ocl.unionSequence(s,x); }
    return Ocl.unionSet(s,x);
  }

  static concatenate(s,t) 
  { return Ocl.unionSequence(s,t); }
 
  static intersectionSequence(s,t) 
  { var res = [];
    var st = Ocl.asSet(t); 
    for (var x of s)
    { if (st.has(x))
      { res.push(x); }
    }
    return res;
  }

  static intersectionSet(s,t) 
  { var st = Ocl.asSet(t); 
    return s.intersection(st); 
  } 

  static intersection(s, x)
  { if (Array.isArray(s)) 
    { return Ocl.intersectionSequence(s,x); }
    return Ocl.intersectionSet(s,x);
  }

  static concatenateAll(sq) 
  { var res = [];
    for (var s of sq) 
    { res = res.concat(s); }
    return res;
  }

  static unionAllSet(sq) 
  { var n = Ocl.size(sq); 
    if (n == 0) 
    { return new Set(); } 
    var res = Ocl.any(sq);
    for (var s of sq) 
    { res = Ocl.union(res,s); }
    return res;
  }

  static unionAll(s)
  { var n = Ocl.size(s); 
    if (n == 0) 
    { return new Set(); } 
    var x = Ocl.any(s); 
    if (Array.isArray(x)) 
    { return Ocl.concatenateAll(s); }
    return Ocl.unionAllSet(s);
  }

  static intersectAllSet(sq) 
  { var res = Ocl.any(sq); 
    if (res == null) 
    { return new Set(); }
    for (var s of sq)
    { res = res.intersection(s); }
    return res;
  }

  static intersectAllSequence(sq) 
  { var res = Ocl.any(sq); 
    if (res == null) 
    { return []; }
 
    for (var s of sq) 
    { res = Ocl.intersectionSequence(res,s); }
    return res; 
  }

  static intersectAll(s)
  { var x = Ocl.any(s); 
    if (x == null) 
    { return new Set(); }
    if (Array.isArray(x)) 
    { return Ocl.intersectAllSequence(s); }
    return Ocl.intersectAllSet(s);
  }

  static reverseSequence(s) 
  { return s.toReversed(); }

  static reverseSet(s) 
  { var res = new Set(); 
    var sq = Array.from(s);
    return Ocl.unionSet(res, sq.toReversed());
  } 

  static sortSequence(s) 
  { if (s.length == 0) 
    { return []; } 
  
    var s0 = s[0]; 

    if ((typeof s0) == "number")
    { return s.toSorted((a, b) => a - b); } 
  
    return s.toSorted(); 
  } 

  static sortSet(s) 
  { var res = Array.from(s); 
    res = Ocl.sortSequence(res);
    var result = new Set();
    for (var x of res)
    { result.add(x); }
    return result;
  }

  static sortMap(s) 
  { var ks = s.keys();
    var m = new Map();
    var res = Array.from(ks); 
    res = Ocl.sortSequence(res);
    for (var k of res) 
    { m.set(k, s.get(k)); }
    return m;
  }

  static sortedBy(s, f)
  { 
    var p = Array.from(s);
    if (p.length == 0) 
    { return p; } 

    var values = new Map(); 
    for (var x of p) 
    { values.set(x, f(x)); } 

    var y = f(p[0]);

    if ((typeof y) == "number")
    { return p.toSorted((a,b) => (values.get(a) - values.get(b))); }  

    return p.toSorted((a,b) => Ocl.compareTo(values.get(a), values.get(b)));
  } // for collections s

  static sortedByMultiple(s, fields)
  { var n = fields.length;
    var p = Array.from(s);
    for (var ind = n-1; ind >= 0; ind--)
    { var f = fields[ind];
      p = p.toSorted((a,b) => Ocl.compareTo(f(a), f(b)));
    }
    return p;
  } // for collections s

  static selectSet(s,f) 
  { var res = new Set();
    for (var x of s)
    { if (f(x))
      { res.add(x); }
    }
    return res;
  }

  static selectSequence(s,f) 
  { var res = []; 
    for (var x of s)
    { if (f(x))
      { res.push(x); }
    }
    return res;
  }

  static select(s,f)
  { if (Array.isArray(s))
    { return Ocl.selectSequence(s,f); }
    return Ocl.selectSet(s,f);
  }

  static rejectSet(s,f)  
  { var res = new Set();
    for (var x of s)
    { if (f(x)) { }
      else
      { res.add(x); }
    }
    return res;
  }  

  static rejectSequence(s,f) 
  { var res = []; 
    for (var x of s)
    { if (f(x)) { } 
      else 
      { res.push(x); }
    }
    return res;
  }

  static reject(s,f)
  { if (Array.isArray(s))
    { return Ocl.rejectSequence(s,f); }
    return Ocl.rejectSet(s,f);
  }

  static collect(s,f) 
  { var res = [];
    for (var x of s)
    { res.push(f(x)); }
    return res;
  } // ok for sets, sequences

  static selectMaximalsSequence(col,f)
  { var result = [];
    if (col.length == 0)
    { return result; }
    var maximal = f(col[0]);
    var result = [col[0]];
    for (var ind = 1; ind < col.length; ind++)
    { var x = col[ind];
      var value = f(x);
      if (value > maximal)
      { result = [x];
        maximal = value;
      }
      else  
      { if (value == maximal)
        { result.push(x); }
      }
    }
    return result;
  }
 
  static selectMaximalsSet(col,f) 
  { var result = new Set();
    if (col.size == 0)
    { return result; }
    var elem = Ocl.any(col);
    var maximal = f(elem);
    result.add(elem);

    for (var x of col)
    { var value = f(x);
      if (value > maximal)
      { result = new Set();
        result.add(x);
        maximal = value;
      }
      else 
      { if (value == maximal)
        { result.add(x); }
      }
    }
    return result;
  }

  static selectMaximals(s,f)
  { if (Array.isArray(s))
    { return Ocl.selectMaximalsSequence(s,f); }
    return Ocl.selectMaximalsSet(s,f);
  }

  static selectMinimalsSequence(col,f) 
  { var result = [];
    if (col.length == 0)
    { return result; }
    var minimal = f(col[0]);
    var result = [col[0]];
    for (var ind = 1; ind < col.length; ind++)
    { var x = col[ind];
      var value = f(x);
      if (value < minimal)
      { result = [x];
        minimal = value;
      }
      else  
      { if (value == minimal)
        { result.push(x); }
      }
    }
    return result;
  }

  static selectMinimalsSet(col,f)
  { var result = new Set();
    if (col.size == 0)
    { return result; }
    var elem = Ocl.any(col);
    var minimal = f(elem);
    result.add(elem);

    for (var x of col)
    { var value = f(x);
      if (value < minimal)
      { result = new Set();
        result.add(x);
        minimal = value;
      }
      else 
      { if (value == minimal)
        { result.add(x); }
      }
    }
    return result;
  }

  static selectMinimals(s,f)
  { if (Array.isArray(s))
    { return Ocl.selectMinimalsSequence(s,f); }
    return Ocl.selectMinimalsSet(s,f);
  }

  static selectMinimalsMap(m,f)
  { var result = new Map();
    if (m.size == 0)
    { return result; }
    var elem = Ocl.anyMap(m);
    var minimal = f(elem);

    for (var [k,x] of m)
    { var value = f(x);
      if (value < minimal)
      { result = new Map();
        result.set(k,x);
        minimal = value;
      }
      else 
      { if (value == minimal)
        { result.set(k,x); }
      }
    }
    return result;
  }

  static selectMaximalsMap(m,f)
  { var result = new Map();
    if (m.size == 0)
    { return result; }
    var elem = Ocl.anyMap(m);
    var maximal = f(elem);

    for (var [k,x] of m)
    { var value = f(x);
      if (value > maximal)
      { result = new Map();
        result.set(k,x);
        maximal = value;
      }
      else 
      { if (value == maximal)
        { result.set(k,x); }
      }
    }
    return result;
  }

  static minCollection(col) 
  { var result = Ocl.any(col); 
    for (var x of col)
    { if (x < result)
      { result = x; }
    }
    return result;
  } // ok for sets, sequences

  static maxCollection(col)
  { var result = Ocl.any(col);
    for (var x of col)
    { if (x > result)
      { result = x; }
    }
    return result;
  } // for sets, sequences
 
  static sumNumeric(col) 
  { var result = 0;
    for (var x of col) 
    { result = result + x; }
    return result;
  }

  static sumString(col)  
  { var result = "";
    for (var x of col) 
    { result = result + x; }
    return result;
  }

  static prd(col)  
  { var result = 1;
    for (var x of col) 
    { result *= x; }
    return result;
  }

  static includesAllMap(supset,subset) 
  { for (var [x,v] of subset)
    { if (supset.has(x) && 
          v == supset.get(x))
      { }  
      else
      { return false; }
    }
    return true;
  }

  static excludesAllMap(supset,subset) 
  { for (var [x,v] of subset)
    { if (supset.has(x))
      { if (v == supset.get(x))
        { return false; }
      }
    }
    return true;
  }

  static existsMap(m,f) 
  { for (var [x,v] of m)
    { if (f(v))
      { return true; }
    }
    return false;
  }

  static forAllMap(m,f) 
  { for (var [x,v] of m)
    { if (f(v)) { }
      else
      { return false; }
    }
    return true;
  }

  static exists1Map(m,f) 
  { var found = false;
    for (var [x,v] of m)
    { if (f(v))
      { if (found)
        { return false; }
        else 
        { found = true; }
      }
    }
    return found;
  }

  static includingMap(m,x,y) 
  { var res = new Map(m);
    res.set(x,y);
    return res;
  }

  static excludeAllMap(s1,s2)
  { var res = new Map();
    for (var [x,v] of s1)
    { if (s2.has(x))
      { } 
      else 
      { res.set(x,v); }
    }
    return res;
  } // either set or map s2

  static excludingMapKey(m,y) 
  { var res = new Map();
    for (var [x,v] of m)  
    { if (x == y) { }
      else 
      { res.set(x,v); }
    }
    return res;
  }

  static excludingMapValue(s,y) 
  { var res = new Map();
    for (var [x,v] of s)
    { if (v == y) { }
      else 
      { res.set(x,v); }
    }
    return res;
  }

  static maxMap(col)
  { if (col.size == 0)
    { return null; }
    var result = null;
    for (var [k,v] of col)
    { if (result == null)
      { result = v; }
      else if (v > result)
      { result = v; }
    }
    return result;
  }

  static minMap(col)
  { if (col.size == 0)
    { return null; }
    var result = null;
    for (var [k,v] of col)
    { if (result == null)
      { result = v; }
      else if (v < result)
      { result = v; }
    }
    return result;
  }

  static unionMap(s,t)
  { var res = new Map(s);
    for (var [x,v] of t)
    { res.set(x, v); }
    return res;
  }

  static unionAllMap(sq) 
  { var res = new Map();
    for (var s of sq) 
    { res = Ocl.unionMap(res,s); }
    return res;
  }

  static intersectionMap(s,t) 
  { var res = new Map();
    for (var [x,v] of s)
    { if (t.has(x) && v == t.get(x))
      { res.set(x,v); } 
    }
    return res;
  }

  static intersectAllMap(sq) 
  { var res = new Map();
    if (sq.length == 0) 
    { return res; }
    res = sq[0];

    for (var s of sq) 
    { res = Ocl.intersectionMap(res,s); }
    return res;
  }

  static selectMap(m,p) 
  { var res = new Map();
    for (var [x,v] of m)
    { if (p(v))
      { res.set(x,v); }
    }
    return res;
  }

  static rejectMap(m,p) 
  { var res = new Map();
    for (var [x,v] of m)
    { if (p(v)) { }
      else
      { res.set(x,v); }
    }
    return res;
  }

  static collectMap(m,e) 
  { var res = new Map();
    for (var [x,v] of m)
    { res.set(x, e(v)); }
    return res;
  }

  static restrict(m,ks) 
  { var res = new Map();
    for (var [x,v] of m)
    { if (ks.has(x))
      { res.set(x,v); }
    }
    return res;
  }

  static antirestrict(m,ks) 
  { var res = new Map();
    for (var [x,v] of m)
    { if (ks.has(x))
      { } 
      else 
      { res.set(x,v); }
    }
    return res;
  }

  static excludingAtMap(m,k) 
  { var res = new Map();
    for (var [x,v] of m) 
    { if (x == k) { } 
      else 
      { res.set(x,v); }
    }
    return res;
  }

  static keys(m) 
  { var res = new Set(m.keys());
    return res;
  }

  static values(m) 
  { var res = Array.from(m.values());
    return res;
  } // should be a sequence?

}
