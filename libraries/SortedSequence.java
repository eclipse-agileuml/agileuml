import java.util.List;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.ListIterator;
import java.util.Iterator;
import java.util.Spliterator;


class SortedSequence<T extends Comparable<T>> implements List<T> 
{ ArrayList<T> elements = new ArrayList<T>(); 

  public SortedSequence() { }

  public SortedSequence(List<T> col)
  { elements = new ArrayList<T>(); 
    elements.addAll(col); 
    Collections.sort(elements); 
  } 

  public SortedSequence(Collection<T> col)
  { elements = new ArrayList<T>(); 
    elements.addAll(col); 
    Collections.sort(elements); 
  } 

  public SortedSequence<T> clone()
  { ArrayList<T> elems = (ArrayList<T>) elements.clone(); 
    SortedSequence<T> ss = new SortedSequence<T>();
    ss.elements = elems; 
    return ss;
  }

  public static <S extends Comparable<S>> SortedSequence<S> initialiseSortedSequence(S ... args)
  { SortedSequence<S> result = new SortedSequence<S>(); 
    for (int i = 0; i < args.length; i++) 
    { result.add(args[i]); } 
    return result;  
  } 	
  
  public T get(int i) 
  { return elements.get(i); } 

  public SortedSequence<T> subList(int i, int j)
  { ArrayList<T> subelems = new ArrayList<T>(); 
    SortedSequence<T> ss = new SortedSequence<T>();
    ss.elements.addAll(elements.subList(i,j)); 
    return ss; 
  } 

  public ListIterator listIterator(int x)
  { return elements.listIterator(x); } 

  public ListIterator listIterator()
  { return elements.listIterator(); } 

  public Iterator iterator()
  { return elements.iterator(); } 

  public int lastIndexOf(Object x)
  { int insertPoint = Collections.binarySearch(elements, (T) x); 
    
    if (insertPoint < 0)
    { return insertPoint; }
	
    for (int i = insertPoint+1; i < elements.size(); i++) 
    { if (x.equals(elements.get(i)))
      { insertPoint++; }
      else 
      { return insertPoint; }
    } 
	
    return insertPoint; 
  } // O(n) in worst case, however.  

  public int indexOf(Object x)
  { int insertPoint = Collections.binarySearch(elements, (T) x); 
    
    if (insertPoint < 0)
    { return insertPoint; }
	
    for (int i = insertPoint-1; i >= 0; i--) 
    { if (x.equals(elements.get(i)))
      { insertPoint--; }
      else 
      { return insertPoint; }
    } 
	
    return insertPoint;
  } // O(n) in worst case, however. 

  public boolean remove(Object x)
  { return elements.remove(x); } 

  public T remove(int x)
  { return elements.remove(x); }

  public void add(int i, T x)
  { // Only changes the list if i is the correct position

    int insertionPoint = 
           Collections.binarySearch(elements, x); 
               // log(elements.size()) time complexity

    if (insertionPoint < 0)
    { int ip = -(insertionPoint + 1); 
      if (i == ip)
      { elements.add(i,x); } 
    } 
    else 
    { // in list already 
      if (i == insertionPoint)
      { elements.add(i,x); } 
    } 
  } 

  public boolean add(T x) // self->including(x)
  { int insertionPoint = 
           Collections.binarySearch(elements, x); 
    
    if (insertionPoint < 0)
    { int ip = -(insertionPoint + 1); 
      elements.add(ip,x);  
    } 
    else 
    { elements.add(insertionPoint,x); } 
    return true;  
    /* throw new UnsupportedOperationException(); */  
  } 

  public boolean including(T x) // self->including(x)
  { int insertionPoint = 
           Collections.binarySearch(elements, x); 
               // log(elements.size()) time complexity

    if (insertionPoint < 0)
    { int ip = -(insertionPoint + 1); 
      elements.add(ip,x);  
    } 
    else 
    { elements.add(insertionPoint,x); } 
    return true; 
  } 

  public boolean add(T x, int nCopies)
  { int insertionPoint = 
           Collections.binarySearch(elements, x); 
               // log(elements.size()) time complexity

    if (insertionPoint < 0)
    { int ip = -(insertionPoint + 1); 
      for (int i = 0; i < nCopies; i++) 
      { elements.add(ip,x); }  
    } 
    else 
    { for (int i = 0; i < nCopies; i++) 
      { elements.add(insertionPoint,x); }
    }
	 
    return true; 
  } 

  public T set(int i, T x)
  { int insertionPoint = 
           Collections.binarySearch(elements, x); 
               // log(elements.size()) time complexity

    if (insertionPoint < 0)
    { int ip = -(insertionPoint + 1); 
      if (i == ip)
      { return elements.set(i,x); }
      return null;  
    } 
    else 
    { // in list already 
      return x;  
    } 
  } // another interpretation is to remove the element at 
    // position i, and insert x. 

  public void clear()
  { elements.clear(); }

  public boolean addAll(int index, Collection col)
  { boolean res = elements.addAll(col); 
    Collections.sort(elements);
    return res;  
  } // More efficient to sort col and then merge:  

  public SortedSequence<T> unionSortedSequence(
                              SortedSequence<T> sq)
  { int n = elements.size(); 
    int m = sq.elements.size(); 

    ArrayList<T> res = new ArrayList<T>(n+m);

    int reached = 0; 
    int i = 0;
    
    while (i < n && reached < m) 
    { Comparable obj = (Comparable) elements.get(i);
      Comparable sqelem = 
           (Comparable) sq.elements.get(reached);
  
      if (obj.compareTo(sqelem) < 0)
      { res.add((T) obj);
        i++;
      } 
      else 
      { res.add((T) sqelem); 
        reached++; 
      } 
    } 

    for (int j = i; j < n; j++) 
    { res.add((T) elements.get(j)); } 

    for (int k = reached; k < m; k++) 
    { res.add((T) sq.elements.get(k)); } 
 
    SortedSequence<T> newsq = new SortedSequence<T>(); 
    newsq.elements = res;   
    return newsq; 
  } 
    
  public java.util.Set<T> uniqueSet()
  { return asSet(); }

  public java.util.Set<T> asSet()
  { java.util.TreeSet<T> res = new java.util.TreeSet<T>();

    int n = elements.size(); 
 
    if (n == 0) 
    { return res; } 

    T elem0 = elements.get(0); 
    res.add(elem0); 

    T lastAdded = elem0;         

    for (int i = 1; i < n; i++) 
    { T elem = elements.get(i); 
      if (elem.equals(lastAdded)) { } 
      else 
      { res.add(elem); 
        lastAdded = elem; 
      } 
    } 

    return res; 
  }

  public SortedOrderedSet<T> asSortedOrderedSet()
  { java.util.Set<T> rset = new java.util.HashSet<T>(); 
    int n = elements.size(); 

    ArrayList<T> rseq = new ArrayList<T>(n); 
	
    if (n == 0) 
    { return new SortedOrderedSet<T>(); } 

    T elem0 = elements.get(0); 
    rset.add(elem0); 
    rseq.add(elem0); 

    T lastAdded = elem0;         

    for (int i = 1; i < n; i++) 
    { T elem = elements.get(i); 
      if (elem.equals(lastAdded)) { } 
      else 
      { rset.add(elem);
        rseq.add(elem);  
        lastAdded = elem; 
      } 
    } 

    SortedSequence<T> rsseq = new SortedSequence<T>(); 
    rsseq.elements = rseq; 
    SortedOrderedSet<T> res = new SortedOrderedSet<T>(); 
    res.setElementSet(rset); 
    res.setElementSeq(rsseq); 
    return res; 
  }

  public boolean contains(Object x)
  { int insertionPoint = 
           Collections.binarySearch(elements, (T) x); 
               // log(elements.size()) time complexity

    if (insertionPoint < 0)
    { return false; } 
    else 
    { return true; } 
  } 
  
  public boolean retainAll(Collection col)
  { return elements.retainAll(col); }   

  public SortedSequence<T> intersection(SortedSequence<T> sq)
  { int n = elements.size(); 
    int m = sq.elements.size(); 

    ArrayList<T> res = new ArrayList<T>(n);

    int reached = 0; 
    int i = 0;
    
    while (i < n && reached < m) 
    { Comparable obj = (Comparable) elements.get(i);
      Comparable sqelem = 
           (Comparable) sq.elements.get(reached);
  
      if (obj.compareTo(sqelem) == 0)
      { res.add((T) obj);
        i++;
        reached++; 
      } 
      else if (obj.compareTo(sqelem) < 0)
      { i++; } 
      else 
      { reached++; } 
    } 
 
    SortedSequence<T> newsq = new SortedSequence<T>(); 
    newsq.elements = res;   
    return newsq; 
  } 

  public int size()
  { return elements.size(); }   

  public boolean isEmpty()
  { return elements.isEmpty(); }   

  public String toString()
  { return elements.toString(); }   

  public boolean removeAll(Collection col)
  { return elements.removeAll(col); }   

  public boolean addAll(Collection col)
  { boolean res = elements.addAll(col); 
    Collections.sort(elements);
    return res;  
  } // No: merge the sorted sequence version of col
   
  public boolean containsAll(Collection col)
  { return elements.containsAll(col); }   
  // can be optimised using binarySearch repeatedly

  public SortedSequence<T> front()
  { int n = elements.size(); 

    ArrayList<T> res = new ArrayList<T>(n);
    for (int i = 0; i < n-1; i++) 
    { res.add(elements.get(i)); } 

    SortedSequence<T> newsq = new SortedSequence<T>(); 
    newsq.elements = res;   
    return newsq; 
  } 

  public SortedSequence<T> tail()
  { int n = elements.size(); 

    ArrayList<T> res = new ArrayList<T>(n);
    for (int i = 1; i < n; i++) 
    { res.add(elements.get(i)); } 

    SortedSequence<T> newsq = new SortedSequence<T>(); 
    newsq.elements = res;   
    return newsq; 
  } 
  
  public Object[] toArray()
  { return elements.toArray(); } 

  public <T> T[] toArray(T[] arr)
  { return elements.toArray(arr); }
  
  public T min()
  { return elements.get(0); }

  public T max()
  { return elements.get(elements.size()-1); }
  
  public int getCount(T x)
  { int insertionPoint = 
           this.lastIndexOf(x); 
               // log(elements.size()) time complexity

    // System.out.println(">>> Insertion point for " + x + " is " + insertionPoint); 
	
    if (insertionPoint < 0)
    { return 0; } 
    else 
    { int cnt = 0; 
      for (int i = insertionPoint; i >= 0; i--) 
      { if (x.equals(elements.get(i)))
        { cnt++; }
        else 
        { break; }
      } 
      return cnt; 
    }  
  } 
  
  public boolean equals(Object col)
  { if (col instanceof SortedSequence)
    { SortedSequence ss = (SortedSequence) col; 
      int n = elements.size(); 
      int m = ss.elements.size(); 
      if (n != m) 
      { return false; } 

      for (int i = 0; i < n; i++) 
      { Object x = elements.get(i); 
        Object y = ss.elements.get(i); 
        if (x == y || x.equals(y)) { } 
        else 
        { return false; } 
      } 

      return true; 
    } 

    return false; 
  }    

  public Spliterator spliterator()
  { Spliterator splt = elements.spliterator(); 
    // splt.SORTED = 1; 
    return splt; 
  } 

/* 
  public static void main(String[] args)
  { SortedSequence<String> ss = new SortedSequence<String>(); 
     
    ss.add("bb", 2); ss.add("aa", 3); 
    ss.add("cc", 1);  ss.add("bb", 6);
 
    System.out.println(ss); 
	// System.out.println(ss.indexOf("aa")); 
	// System.out.println(ss.indexOf("cc"));
	// System.out.println(ss.getCount("aa")); 
	// System.out.println(ss.getCount("bb"));

    SortedSequence<String> tt = new SortedSequence<String>(); 
    tt.add("aa", 2); tt.add("cc", 4); tt.add("ee", 2);  

    SortedSequence<String> pp = ss.unionSortedSequence(tt); 
    System.out.println(pp); 

    SortedSequence<String> qq = ss.intersection(tt); 
    System.out.println(qq); 
	
     SortedSequence<Person> pers = new SortedSequence<Person>(); 
	Person p1 = new Person(); 
	p1.name = "Tom"; p1.age = 55; 
	Person p2 = new Person(); 
	p2.name = "Tom"; p2.age = 35;
	pers.add(p1); pers.add(p2); 
	System.out.println(pers);  
	
	SortedSequence<Person> pers1 = new SortedSequence<Person>(); 
	pers1.add(p2); pers1.add(p1); 
	System.out.println(pers1);    
  } */   
}

/* class Person implements Comparable<Person>
{ String name; 
  double age; 
  
  public int compareTo(Person p) 
  { Person pp = (Person) p; 
	return name.compareTo(pp.name); 
  }
} */ 
