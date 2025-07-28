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

}

function testFunction() 
{ var pp = [1, 10, 1]; 
  var xx = Ocl.asOrderedSet(pp);
  return xx[2];   
} 
