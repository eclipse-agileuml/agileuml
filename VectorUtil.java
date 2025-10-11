import java.util.Vector; 
import java.io.*; 

/******************************
* Copyright (c) 2003--2025 Kevin Lano
* This program and the accompanying materials are made available under the
* terms of the Eclipse Public License 2.0 which is available at
* http://www.eclipse.org/legal/epl-2.0
*
* SPDX-License-Identifier: EPL-2.0
* *****************************/
/* Package: Utilities */ 


class VectorUtil
{ // static void display(final Vector a) 
  // { for (int i = 0; i < a.size(); i++) 
  //   { a.elementAt(i).display(); } 
  // } 

  static Vector toVector(Object[] arr)
  { Vector res = new Vector(); 
    for (int i = 0; i < arr.length; i++) 
    { res.add(arr[i]); } 
    return res; 
  } 

  static Vector toVector(java.util.Collection arr)
  { Vector res = new Vector(); 
    for (Object x : arr) 
    { res.add(x); } 
    return res; 
  } 

  static Vector asSet(java.util.Collection arr)
  { Vector res = new Vector(); 
    for (Object x : arr) 
    { if (res.contains(x)) { } 
      else 
      { res.add(x); }
    }  
    return res; 
  } 

  static Vector allToVector(java.util.Collection arr)
  { Vector res = new Vector(); 
    for (Object x : arr) 
    { if (x instanceof Object[])
      { res.addAll(VectorUtil.toVector((Object[]) x)); } 
      else 
      { res.add(x); } 
    } 
    return res; 
  } 

  static void printCommaList(final Vector a) 
  { for (int i = 0; i < a.size(); i++) 
    { System.out.print(a.get(i)); 
      if (i < a.size() - 1) 
      { System.out.print(", "); } 
    } 
  } 

  static void printCommaList(final Vector a, 
                             PrintWriter out) 
  { for (int i = 0; i < a.size(); i++) 
    { out.print(a.get(i)); 
      if (i < a.size() - 1) 
      { out.print(", "); } 
    } 
  } 

  public static void printLines(final Vector a)
  { for (int i = 0; i < a.size(); i++)
    { System.out.println(a.get(i)); }
  }

  public static void printLines(final Vector a,
                                PrintWriter out)
  { for (int i = 0; i < a.size(); i++)
    { out.println(a.get(i)); }
  }

  static Vector vector_append(final Vector a, final Vector b)
  { Vector res = (Vector) a.clone();
    for (int i = 0; i < b.size(); i++)
    { res.addElement(b.elementAt(i)); }
    return res; }

  /* Assume no duplicates in a */ 
  static Vector vector_merge(final Vector a, final Vector b)
  { Vector res = (Vector) a.clone();
    for (int i = 0; i < b.size(); i++)
    { if (!a.contains(b.elementAt(i)))
      { res.addElement(b.elementAt(i)); } 
    }
    return res; 
  }

  public static Vector sharedElements(Vector vects)
  { // returns list of elements
    // that occur in 2 or more of
    // Vectors in vects.
    Vector res = new Vector();
    Vector total = new Vector();
    int n = vects.size();

    for (int i = 0; i < n; i++)
    { Vector vv = (Vector) vects.get(i);
      for (int j = 0; j < vv.size(); j++)
      { Object elem = vv.get(j);
        if (total.contains(elem))
        { if (res.contains(elem)) { }
          else 
          { res.add(elem); }
        }
      }
      total.addAll(vv);
    }
    return res;
  } 


  static Vector getNames(final Vector nameds)
  { Vector res = new Vector(); 
    for (int i = 0; i < nameds.size(); i++) 
    { res.add(((Named) nameds.elementAt(i)).label); } 
    return res; 
  } 

  static Vector getStrings(final Vector nameds)
  { Vector res = new Vector(); 
    for (int i = 0; i < nameds.size(); i++) 
    { res.add("" + nameds.get(i)); } 
    return res; 
  } 

  static Vector vectorEqmerge(final Vector a, final Vector b)
  { Vector res = (Vector) a.clone();
    for (int i = 0; i < b.size(); i++)
    { Object belem = b.elementAt(i); 
      boolean found = false; 

      for (int j = 0; j < a.size(); j++) 
      { if (belem.equals(a.elementAt(j))) 
        { found = true;
          break; } 
      } 
      if (found) {} 
      else 
      { res.add(belem); } 
    } 
    return res; 
  } 

  static Vector flattenVectorArray(final Vector[] a)
  { Vector res = new Vector();
    for (int i = 0; i < a.length; i++)
    { Vector acoll = a[i]; 

      if (acoll == null) { continue; } 

      for (int j = 0; j < acoll.size(); j++) 
      { if (res.contains(acoll.get(j))) { } 
        else 
        { res.add(acoll.get(j)); } 
      } 
    } 

    return res; 
  } 

  static public Named lookup(final String key, final Vector data)
  { for (int i = 0; i < data.size(); i++)
    { if ( key.equals(((Named) data.elementAt(i)).label) )
      { return (Named) data.elementAt(i); }
    }
    return null;  
  }

  public static boolean containsEqual(final String s, final Vector vs)
  { boolean res = false;
    for (int i = 0; i < vs.size(); i++)
    { Object obj = vs.elementAt(i);
      if (obj instanceof String)
      { res = ((String) obj).equals(s);
        // System.out.println("Found " + s + " " + res); 
        if (res)
        { return res; } 
      } 
      // else 
      // { System.out.println("Not found " + s); } 
    }
    return res; 
  }

  public static boolean containsEqualString(final String s, final Vector vs)
  { boolean res = false;
    for (int i = 0; i < vs.size(); i++)
    { Object obj = vs.elementAt(i);
      if ((obj + "").equals(s))
      { return true; } 
    }
    return res; 
  }

  public static boolean containsEqualExpression(final String s, final Vector vs)
  { boolean res = false;

    for (int i = 0; i < vs.size(); i++)
    { Object obj = vs.elementAt(i);
      if (VectorUtil.test("=", (obj + ""), s))
      { return true; } 
    }

    return res; 
  } // 1 = 1.0, etc. 

  public static boolean containsEqualVector(final Vector v, final Vector vs)
  { boolean res = false; 
    for (int i = 0; i < vs.size(); i++) 
    { Vector vx = (Vector) vs.get(i); 
      if (vectorEqual(v,vx))
      { return true; } 
    } 
    return res; 
  } 

  public static boolean containsSupsetVector(final Vector v, final Vector vs)
  { boolean res = false; 
    for (int i = 0; i < vs.size(); i++) 
    { Vector vx = (Vector) vs.get(i); 
      if (vx.containsAll(v))
      { return true; } 
    } 
    return res; 
  } 

  public static boolean vectorEqual(Vector v1, Vector v2)
  { if (v1 == null && v2 == null) 
    { return true; } 
    if (v1 == null) 
    { return false; } 
    if (v2 == null) 
    { return false; } 
    return v1.containsAll(v2) && v2.containsAll(v1); 
  } 

  public static boolean allElementsEqual(Vector v)
  { if (v.size() == 0) { return true; } 

    Object v1 = v.get(0); 
    for (int i = 1; i < v.size(); i++) 
    { Object vx = v.get(i); 
      if (v1.equals(vx)) { } 
      else 
      { return false; } 
    } 
    return true; 
  } 

  public static Vector removeByName(final Vector vs, final String s)
  { Vector res = new Vector(); 
    for (int i = 0; i < vs.size(); i++)
    { Object obj = vs.elementAt(i);
      if ((obj + "").equals(s))
      { } 
      else 
      { res.add(obj); } 
    }
    return res; 
  }

  public static Vector removeAllEqualString(final String s, final Vector vs)
  { Vector res = new Vector(); 
    for (int i = 0; i < vs.size(); i++)
    { Object obj = vs.elementAt(i);
      if ((obj + "").equals(s))
      { } 
      else 
      { res.add(obj); } 
    }
    return res; 
  }

  public static Vector addAll(final Vector oldvs, final Vector newvs)
  { Vector res = new Vector(); 
    res.addAll(oldvs); 
    for (int i = 0; i < newvs.size(); i++) 
    { Vector vx = (Vector) newvs.get(i); 
      if (containsEqualVector(vx,res)) { } 
      else 
      { res.add(vx); } 
    } 
    return res; 
  } 

  public static Vector addAllSupset(final Vector oldvs, final Vector newvs)
  { Vector res = new Vector(); 
    res.addAll(oldvs); 
    for (int i = 0; i < newvs.size(); i++) 
    { Vector vx = (Vector) newvs.get(i); 
      if (containsSupsetVector(vx,res)) { } 
      else 
      { res.add(vx); } 
    } 
    return res; 
  } 

  public static int maxSize(Vector vs)
  { int res = 0; 
    for (int i = 0; i < vs.size(); i++) 
    { Vector vx = (Vector) vs.get(i); 
      int sz = vx.size(); 
      if (sz > res) 
      { res = sz; } 
    } 
    return res; 
  } 

  public static boolean subset(final Vector v1, final Vector v2)
  { boolean res = true;
    for (int i = 0; i < v1.size(); i++)
    { Object obj = v1.elementAt(i);
      if (v2.contains(obj)) {}
      else 
      { res = false; 
        return res; 
      } 
    }
    return res; 
  }

  public static boolean haveCommonElement(final Vector v1, final Vector v2) 
  { Vector res = new Vector(); 
    for (int i = 0; i < v1.size(); i++) 
    { Object obj = v1.elementAt(i); 
      if (v2.contains(obj))
      { return true; } 
    } 

    return false; 
  } 

  public static boolean haveCommonElementAsString(final Vector v1, final Vector v2) 
  { for (int i = 0; i < v1.size(); i++) 
    { Object obj = v1.get(i); 
      if (VectorUtil.containsEqualString(obj + "", v2))
      { return true; } 
    } 

    return false; 
  } 

  public static Vector commonElementsByString(final Vector v1, final Vector v2) 
  { Vector res = new Vector(); 
    for (int i = 0; i < v1.size(); i++) 
    { Object obj = v1.get(i); 
      if (VectorUtil.containsEqualString(obj + "", v2))
      { res.add(obj); } 
    } 

    return res; 
  } 

  public static boolean haveCommonElementName(final Vector v1, final Vector v2) 
  { for (int i = 0; i < v1.size(); i++) 
    { ModelElement obj = (ModelElement) v1.get(i);
      String nme = obj.getName();  
      if (VectorUtil.containsName(nme,v2))
      { return true; } 
    } 

    return false; 
  } 

  public static boolean haveCommonElementSuffixName(final Vector v1, final Vector v2) 
  { for (int i = 0; i < v1.size(); i++) 
    { ModelElement obj = (ModelElement) v1.get(i);
      String nme = obj.getName();  
      int dIndex = nme.lastIndexOf("$"); 
      if (dIndex > 0 && dIndex < nme.length())
      { String baseName = nme.substring(dIndex+1);
         
        if (VectorUtil.containsSuffixName(baseName,v2))
        { return true; } 
      } 
    } 

    return false; 
  } 

  public static Vector commonElementNames(final Vector v1, final Vector v2) 
  { Vector res = new Vector();
 
    for (int i = 0; i < v1.size(); i++) 
    { ModelElement obj = (ModelElement) v1.get(i);
      String nme = obj.getName();  
      if (VectorUtil.containsName(nme,v2))
      { res.add(nme); } 
    } 

    return res; 
  } 

  public static Vector commonElementSuffixNames(final Vector v1, final Vector v2) 
  { Vector res = new Vector();
 
    for (int i = 0; i < v1.size(); i++) 
    { ModelElement obj = (ModelElement) v1.get(i);
      String nme = obj.getName();  
      int dIndex = nme.lastIndexOf("$"); 
      if (dIndex > 0 && dIndex < nme.length())
      { String baseName = nme.substring(dIndex+1); 
        if (VectorUtil.containsSuffixName(baseName,v2))
        { res.add(nme); }
      }  
    } 

    return res; 
  } 

  public static boolean containsName(String nme, Vector v)
  { for (int i = 0; i < v.size(); i++) 
    { ModelElement obj = (ModelElement) v.get(i);
      if (nme.equals(obj.getName())) 
      { return true; } 
    } 
    return false; 
  }   

  public static boolean containsSuffixName(String nme, Vector v)
  { for (int i = 0; i < v.size(); i++) 
    { ModelElement obj = (ModelElement) v.get(i);
      if (obj.getName().endsWith("$" + nme)) 
      { return true; } 
    } 
    return false; 
  }   
    
  public static Vector intersection(final Vector v1, final Vector v2) 
  { Vector res = new Vector(); 
    for (int i = 0; i < v1.size(); i++) 
    { Object obj = v1.elementAt(i); 
      if (v2.contains(obj))
      { res.add(obj); } 
    } 
    return res; 
  } 

  public static Vector union(Vector a, Vector b)
  { if (a == null) 
    { return b; } 

    Vector res = new Vector();
    for (int i = 0; i < a.size(); i++)
    { Object aobj = a.elementAt(i);
      if (res.contains(aobj)) {}
      else 
      { res.add(aobj); } 
    }

    if (b == null) 
    { return res; } 

    for (int j = 0; j < b.size(); j++)
    { Object bobj = b.elementAt(j);
      if (res.contains(bobj)) {}
      else 
      { res.add(bobj); } 
    }

    return res; 
  }

  public static Vector filterVector(Vector names, Vector v)
  { Vector res = new Vector();
    for (int i = 0; i < v.size(); i++)
    { Named nn = (Named) v.elementAt(i);
      if (names.contains(nn.label))
      res.add(nn); 
    }
    return res; 
  }

  public static Vector allSubvectors(Vector v)
  { Vector res = new Vector();
    if (v.size() == 0)
    { res.add(new Vector());
      return res;
    }
    if (v.size() == 1)
    { res.add(new Vector());
      res.add(v);
      return res;
    }
    Vector s = new Vector();
    Object x = v.get(0);
    s.addAll(v);
    s.remove(0);
    Vector scs = allSubvectors(s);
    res.addAll(scs);
    for (int i = 0; i < scs.size(); i++)
    { Vector sc = (Vector) scs.get(i);
      Vector scc = new Vector();
      scc.add(x);
      scc.addAll(sc);
      res.add(scc);
    }
    return res;
  }

  public static Vector allSubvectors(Vector v, int n)
  { Vector scs = VectorUtil.allSubvectors(v); 

    Vector res = new Vector(); 
    for (int i = 0; i < scs.size(); i++)
    { Vector sc = (Vector) scs.get(i);
      if (sc.size() >= n) 
      { res.add(sc); } 
    }
    return res;
  }

  public static Vector allSubsequences(Vector v, int n)
  { Vector scs = VectorUtil.allSubvectors(v); 

    Vector res = new Vector(); 
    for (int i = 0; i < scs.size(); i++)
    { Vector sc = (Vector) scs.get(i);
      if (sc.size() >= n && VectorUtil.continuous(sc,v)) 
      { res.add(sc); } 
    }
    return res;
  }

  public static Vector subrange(Vector v, int st, int en)
  { // v.subrange(st,en) with 0-based indexes

    Vector res = new Vector(); 
    for (int i = st; i <= en; i++) 
    { res.add(v.get(i)); } 

    return res; 
  } 
 
  public static Vector allSubsegments(Vector v, int n)
  { Vector res = new Vector(); 
    
    int mx = v.size(); 

    for (int i = n; i < mx; i++) 
    { // get all segments of length i
      for (int j = 0; j+i-1 < mx; j++) 
      { res.add(subrange(v,j,j+i-1)); } // length i 
    } 
    return res; 
  } 

  public static Vector replaceSubsequence(Vector sq, Vector subs, Vector rep)
  { // replace each subsequence subs of sq by rep

    int sbsize = subs.size(); 

    for (int i = 0; i + sbsize - 1 < sq.size(); i++) 
    { int j = i + sbsize - 1; 
      Vector sqsubseq = VectorUtil.subsequence(sq,i,j); 

      if ((subs + "").equals(sqsubseq + ""))
      { Vector result = new Vector(); 
        Vector sq1 = VectorUtil.subsequence(sq,0,i-1); 
        Vector sq2 = VectorUtil.subsequence(sq,j+1); 
        result.addAll(sq1); 
        result.addAll(rep); 
        result.addAll(sq2); 
        return result; 
      } 
    }
    return sq; 
  }

  public static boolean continuous(Vector sc, Vector v)
  { // sc consists of sequential elements from v

    if (sc.size() <= 1) 
    { return true; } 

    for (int i = 0; i + 1 < sc.size(); i++) 
    { Object elem1 = sc.get(i); 
      Object elem2 = sc.get(i+1); 
  
      int j = v.indexOf(elem1); 
      int k = v.indexOf(elem2); 

      if (k == j+1) { } 
      else 
      { return false; } 
    }
    
    return true; 
  } 

  public static Object mapletValue(String src, 
                     Vector maplets)
  { for (int i = 0; i < maplets.size(); i++)
    { Object obj = maplets.get(i);
      if (obj instanceof Maplet)
      { Maplet mm = (Maplet) obj;
        if (src.equals((String) mm.source))
        { return mm.dest; }
      }
    }
    return null;
  }

  public static boolean hasConflictingMappings(Vector a)
  { // a is a Vector of Maplets.
    for (int i = 0; i < a.size(); i++)
    { Maplet x = (Maplet) a.get(i);
      Object x1 = x.source;
      Object x2 = x.dest;
      for (int j = i+1; j < a.size(); j++)
      { Maplet y = (Maplet) a.get(j);
        Object y1 = y.source;
        Object y2 = y.dest;
        if (x1.equals(y1) &&
            !(x2.equals(y2)))
        { return true; }
      }
    }
    return false;
  }

  public static Vector satisfies(String operator, Vector vals1, Vector vals2)
  { // returns pairs x |-> y from vals1*vals2 which
    // satisfy relation  x operator y.
    Vector res = new Vector();

    for (int i = 0; i < vals1.size(); i++)
    { String s1 = (String) vals1.get(i);
      int x = Integer.parseInt(s1);
      for (int j = 0; j < vals2.size(); j++)
      { String s2 = (String) vals2.get(j);
        int y = Integer.parseInt(s2);
        if (VectorUtil.test(operator,x,y))
        { res.add(new Maplet(s1,s2)); }
      }
    }
    return res;
  }

  public static boolean test(String op, String x, String y)
  { try
    { double x1 = Double.parseDouble(x); 
      double y1 = Double.parseDouble(y);
      return VectorUtil.test(op, x1, y1); 
    } 
    catch (Exception _ex) { } 

    // Treat as strings

    if (op.equals("="))
    { return ("" + x).equals(y); }

    if (op.equals("<"))
    { return ("" + x).compareTo(y) < 0; }

    if (op.equals("!=") || op.equals("/="))
    { return !("" + x).equals(y); }

    if (op.equals("<="))
    { return ("" + x).compareTo(y) <= 0; }

    if (op.equals(">"))
    { return ("" + x).compareTo(y) > 0; }

    if (op.equals(">="))
    { return ("" + x).compareTo(y) >= 0; }

    return false; 
  } 

  public static boolean test(String op, double x, double y)
  { if (op.equals("="))
    { return x == y; }  // already handled in Expression

    if (op.equals("<"))
    { return x < y; }

    if (op.equals("!=") || op.equals("/="))
    { return x != y; }

    if (op.equals("<="))
    { return x <= y; }

    if (op.equals(">"))
    { return x > y; }

    if (op.equals(">="))
    { return x >= y; }

    return false;
  }

  public static Expression mapletsToExpression(Vector map)
  { Expression res = new BasicExpression("true");
    for (int i = 0; i < map.size(); i++)
    { Maplet mm = (Maplet) map.get(i);
      String sen = (String) mm.source;
      String val = (String) mm.dest;
      Expression be = 
        new BinaryExpression("=",
          new BasicExpression(sen),
          new BasicExpression(val));
      res = Expression.simplify("&",res,be,null);
    }
    return res;
  }

  public static Expression mapletsToSmvExpression(Vector map)
  { Expression res = new BasicExpression("true");
    for (int i = 0; i < map.size(); i++)
    { Maplet mm = (Maplet) map.get(i);
      String sen = (String) mm.source;
      String val = (String) mm.dest;
      Expression be =
        new BinaryExpression("=",
          new BasicExpression("M" + sen + "." + sen),
          new BasicExpression(val));
      res = Expression.simplify("&",res,be,null);
    }
    return res;
  }

    public static Vector maximalElements(Vector s, Vector v)
    { Vector res = new Vector();
      Comparable largest;
      if (s.size() > 0)
      { largest = (Comparable) v.get(0); 
        res.add(s.get(0));
      }
      else 
      { return res; } 

      for (int i = 1; i < s.size(); i++)
      { Comparable next = (Comparable) v.get(i);
        if (largest.compareTo(next) < 0)
        { largest = next;
          res.clear();
          res.add(next);
        }
        else if (largest.compareTo(next) == 0)
        { res.add(next); }
    }
    return res;
  }

  public static boolean isInitialSegment(Vector v1, Vector v2)
  { if (v2.size() < v1.size())
    { return false; } 

    for (int i = 0; i < v1.size(); i++) 
    { Object x = v1.get(i); 
      if (("" + x).equals("" + v2.get(i))) { } 
      else 
      { return false; } 
    } 
    return true; 
  } 

  public static Vector largestInitialSegment(Vector path, Vector sqs) 
  { // largest initial segment of path that is in sqs

    Vector res = new Vector(); 
    for (int i = 0; i < sqs.size(); i++) 
    { Vector sq = (Vector) sqs.get(i); 
      if (VectorUtil.isInitialSegment(sq,path))
      { res.add(sq); } 
    } 

    if (res.size() == 0)
    { return null; } 

    Vector best = (Vector) res.get(0); 
    for (int i = 0; i < res.size(); i++) 
    { Vector sq = (Vector) res.get(i); 
      if (sq.size() > best.size())
      { best = sq; } 
    } 
    return best; 
  } 

  public static Vector vectorTail(Vector vect)
  { Vector res = new Vector(); 
    res.addAll(vect); 
    vect.remove(0); 
    return res; 
  } 

  public static Vector vectorTail(int i, Vector vect)
  { // tail from i onwards
    Vector res = new Vector(); 
    for (int x = i; x < vect.size(); x++) 
    { res.add(vect.get(x)); } 
    return res; 
  } 

  public static Vector subsequence(Vector sq, int i, int j)
  { Vector res = new Vector(); 
    for (int k = i; k <= j && k < sq.size(); k++) 
    { res.add(sq.get(k)); } 
    return res; 
  } 

  public static Vector subsequence(Vector sq, int i)
  { Vector res = new Vector(); 
    for (int k = i; k < sq.size(); k++) 
    { res.add(sq.get(k)); } 
    return res; 
  } 

  public static Vector vectorSummation(Vector v1, Vector v2)
  { // x + y for x in v1, y in v2

    Vector res = new Vector(); 
    for (int i = 0; i < v1.size(); i++) 
    { Object x = v1.get(i); 
      try { 
        Double d = Double.parseDouble("" + x); 
        for (int j = 0; j < v2.size(); j++) 
        { Object y = v2.get(j); 
          try { 
            Double e = Double.parseDouble("" + y); 
            res.add(d + e); 
          } catch (Exception _q) { } 
        } 
      } catch (Exception _p) { } 
    } 
    return res; 
  } 

  public static Vector vectorSubtraction(Vector v1, Vector v2)
  { // x + y for x in v1, y in v2

    Vector res = new Vector(); 
    for (int i = 0; i < v1.size(); i++) 
    { Object x = v1.get(i); 
      try { 
        Double d = Double.parseDouble("" + x); 
        for (int j = 0; j < v2.size(); j++) 
        { Object y = v2.get(j); 
          try { 
            Double e = Double.parseDouble("" + y); 
            res.add(d - e); 
          } catch (Exception _q) { } 
        } 
      } catch (Exception _p) { } 
    } 
    return res; 
  } 

  public static double vectorMinimum(Vector v1)
  { double res = 0; 
    if (v1.size() == 0) { return res; } 

    try
    { res = Double.parseDouble("" + v1.get(0)); } 
    catch (Exception _ex) { return 0; }  

    for (int i = 0; i < v1.size(); i++) 
    { Object x = v1.get(i); 
      try { 
        Double d = Double.parseDouble("" + x); 
        if (d < res) 
        { res = d; } 
      } catch (Exception _p) { } 
    } 
    return res; 
  } 

  public static double vectorMaximum(Vector v1)
  { double res = 0; 
    if (v1.size() == 0) 
    { return res; } 

    try
    { res = Double.parseDouble("" + v1.get(0)); } 
    catch (Exception _ex) { return 0; }  

    for (int i = 0; i < v1.size(); i++) 
    { Object x = v1.get(i); 
      try { 
        Double d = Double.parseDouble("" + x); 
        if (d > res) 
        { res = d; } 
      } catch (Exception _p) { } 
    } 

    return res; 
  } 

  public static Vector vectorDivide(Vector v1, double z)
  { Vector res = new Vector(); 
    if (v1.size() == 0) 
    { return res; } 

    for (int i = 0; i < v1.size(); i++) 
    { Object x = v1.get(i); 
      try { 
        Double d = Double.parseDouble("" + x); 
        res.add(d/z);  
      } catch (Exception _p) { } 
    } 
    return res; 
  } 

  public static Vector vectorMax(Vector v1, Vector v2)
  { // max(Set{x, y}) for x in v1, y in v2

    Vector res = new Vector(); 
    for (int i = 0; i < v1.size(); i++) 
    { Object x = v1.get(i); 
      try { 
        Double d = Double.parseDouble("" + x); 
        for (int j = 0; j < v2.size(); j++) 
        { Object y = v2.get(j); 
          try { 
            Double e = Double.parseDouble("" + y); 
            if (d > e)
            { res.add(d); } 
            else 
            { res.add(e); }  
          } catch (Exception _q) { } 
        } 
      } catch (Exception _p) { } 
    } 
    return res; 
  } 

  public static Vector vectorMultiplication(Vector v1, double d)
  { // x * d for x in v1

    Vector res = new Vector(); 
    for (int i = 0; i < v1.size(); i++) 
    { Object x = v1.get(i); 
      try { 
        Double dx = Double.parseDouble("" + x); 
        res.add(dx.doubleValue()*d);  
      } catch (Exception _p) { } 
    } 
    return res; 
  } 

  public static Vector removeDuplicates(Vector v1)
  { Vector res = new Vector(); 
    if (v1.size() == 0) 
    { return res; } 

    for (int i = 0; i < v1.size(); i++)
    { double x = 0; 
      try
      { x = Double.parseDouble("" + v1.get(i)); } 
      catch (Exception _ex) { return res; }  

      boolean found = false; 
      for (int j = 0; j < res.size(); j++) 
      { double y = 0;  
        try { 
           y = Double.parseDouble("" + res.get(j)); 
           if (x == y) 
           { found = true; } 
        } catch (Exception _p) { } 
      }

      if (!found) 
      { res.add(x); } 
    } 

    return res; 
  } 

  public static void main(String[] args) 
  { Vector coll = new Vector(); 
    Vector coll1 = new Vector(); 
   /* Attribute a1 = new Attribute("a1", new Type("int",null), ModelElement.INTERNAL); 
    Attribute a2 = new Attribute("a2", new Type("int",null), ModelElement.INTERNAL); 
    Attribute a3 = new Attribute("a3", new Type("int",null), ModelElement.INTERNAL); 
    coll.add(a1); coll.add(a2); 
    coll1.add(a1); 
    Vector coll2 = new Vector(); 
    coll2.add(a1); coll2.add(a2); coll2.add(a3);  
    Vector sqs = new Vector(); 
    sqs.add(coll1); sqs.add(coll); 
    System.out.println(VectorUtil.largestInitialSegment(coll2,sqs));  

    coll.add("a"); coll.add("b"); coll.add("c"); 
    coll.add("d"); coll.add("e"); 
    Vector vv = VectorUtil.allSubsequences(coll,2); 
    System.out.println(vv);

    Vector ww = VectorUtil.allSubsegments(coll,2); 
    System.out.println(ww);

    Vector v1 = new Vector(); 
    v1.add("c"); v1.add("d"); 
    Vector v2 = new Vector(); 
    v2.add("x");  
    Vector rr = VectorUtil.replaceSubsequence(coll,v1,v2); 
    System.out.println(rr); 
           
    Vector zz = new Vector(); 
    Vector yy = new Vector(); 
    zz.add(1); yy.add(0); yy.add(1); yy.add(1.0); 
    System.out.println(VectorUtil.vectorSummation(zz,yy));  
    System.out.println(VectorUtil.vectorSubtraction(zz,yy)); 
    System.out.println(VectorUtil.vectorMinimum(yy));  
    System.out.println(VectorUtil.vectorMaximum(yy));  

    System.out.println(VectorUtil.removeDuplicates(yy)); */ 

    System.out.println(VectorUtil.test("<=", "21x", "21z"));  
  } 
    
}

