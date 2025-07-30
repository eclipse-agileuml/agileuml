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
  } // independent of col's type

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

  static indexOfSequence(sq,x)
  { var indx = sq.indexOf(x);
    return indx + 1;
  }

  static lastIndexOfSequence(sq,x) 
  { var indx = sq.lastIndexOf(x);
    return indx + 1;
  }

static lastIndexOf(s,d) 
{ var result = 0
  var dlen = d.length;
  if (dlen == 0) 
  { return 0; }
  var i = s.lastIndexOf(d);
  if (i < 0)
  { return 0; }
  return i+1;
}

static lastIndexOfString(s, d) 
{ return Ocl.lastIndexOf(s,d); }

}

class Person { 
  constructor() { this.name  = "Bob"; this.age = 44; } 
} 

class CC {
  constructor( ) {   this.col = [];
   }
   op1( ) {   result = Ocl.maxSequence(this.col) ; }
   op2( ) {   result = Ocl.minSequence(Ocl.selectSequence(this.col, function(x) { return x > 0; })) ; }
}

function testFunction() 
{ var col = new Map([[3,"a"],[5,"b"],[7,"c"]]);
  var sub = new Map([[7,"a"], [8, "b"], [9,"c"]]);  
  var sq = [1,4,5,7,5]; 
  // var chrs = Ocl.subtractString("a Long String", "gnt");
  return Ocl.indexOfSequence(sq, 9);    
} 
