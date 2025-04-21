import java.util.*;

public class javaSequencedMapSource
{ SequencedMap<String,int> mp = new LinkedHashMap<String,int>(); 

  public void op(String s, int x)
  { mp.put(s,x); mp.putFirst("five", 5); mp.putLast("six", 6); 
     
    System.out.println(mp.reversed()); 

    System.out.println(mp.firstEntry()); 
	
    System.out.println(mp.lastEntry()); 
	
    mp.pollLastEntry();
	
    mp.pollFirstEntry(); 
	
    System.out.println(mp.sequencedKeySet());  
    System.out.println(mp.sequencedEntrySet());
    System.out.println(mp.sequencedValues());  
  }

}

