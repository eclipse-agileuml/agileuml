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

class javaSumNumbers2Source {
  
  public static int op(int n)
  { int total = 0; 
    for (int i = 1; i <= n; i++) 
    { total += i*i; } 

    return total;  
  }

}  