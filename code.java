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

class javaRecursion2Source {
  
  public static double op(int n)
  { if (n <= 1) 
    { return 1; }  
    
    return n * javaRecursion2Source.op(n-1);   
  }
}  