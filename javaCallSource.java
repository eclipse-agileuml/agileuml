import java.util.*;
import java.util.HashMap;
import java.util.Collection;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import java.util.TreeSet;
import java.util.Collections;
import java.util.function.Function;
import java.io.Serializable;

class javaCallSource {
  
  public static ArrayList<Integer> op(int n)
  { if (n <= 0) 
    { return new ArrayList<Integer>(); }  
    
    ArrayList<Integer> res = op(n-1);
    res.add(n); 
    return res;    
  }

  public static void main(String[] args)
  { System.out.println(op(10).get(0)); }  
}  