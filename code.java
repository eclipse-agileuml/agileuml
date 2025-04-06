public class Test
{ public void op()
  { Set ss = new HashSet(); 
    Iterator ot = ss.iterator(); 
    Enumeration en = new StringTokenizer("a long string"); 
    Object ob = en.nextElement(); 
    Vector vv = new Vector(ss); 
    en = vv.elements(); 
    en = Collections.enumeration(ss); 
    List pp = new ArrayList(ss); 
    ListIterator lp = pp.listIterator(2); 
  }
}



