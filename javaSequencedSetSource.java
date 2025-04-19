import java.util.*;

public class javaSequencedSetSource
{ SequencedSet st = new LinkedHashSet(); 

  public void op(int x)
  { st.add(x); st.addFirst(5); st.addLast(6); 
     
    System.out.println(st.reversed()); 
  }

  public static void main(String[] args)
  { javaSequencedSetSource cc = new javaSequencedSetSource(); 
    cc.op(3); 
  }
}

