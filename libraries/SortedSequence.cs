using System;
using System.Collections.Generic;

class SortedSequence<T>: List<T> where T: IComparable<T> {
    List<T> elements = new List<T>();
    
    public SortedSequence() {}

    public SortedSequence(List<T> col) {
        elements = new List<T>();
        elements.AddRange(col);
        elements.Sort();
    }

    public SortedSequence(ICollection<T> col) {
        elements = new List<T>();
        elements.AddRange(col);
        elements.Sort();
    }

    public SortedSequence<T> clone() {
        List<T> elems = (List<T>) elements.MemberwiseClone()
        SortedSequence<T> ss = new SortedSequence<T>();
        ss.elements = elems;
        return ss;
    }

    public static SortedSequence<S> initialiseSortedSequence(params S args) {
        SortedSequence<S> result = new SortedSequence<S>();
        for (int i = 0; i < args.Count; i++) {
            result.Add(args[i]);
        }
        return result;
    }

    public T get(int i) {
        return elements[i];
    }

    public SortedSequence<T> subList(int i, int j) {
        List<T> subelems = new List<T>();
        SortedSequence<T> ss = new SortedSequence<T>();
        ss.elements.AddRange(elements.GetRange(i, j));
        return ss;
    }

    public List<T>.Enumerator listIterator(int x) {
        IEnumerator<T> iterator = this.listIterator();
        foreach (T element in iterator) {
            if (element == elements[x]) {
                break;
            }
        }
        return iterator;
    }

    public List<T>.Enumerator listIterator() {
        return elements.GetEnumerator();
    }

    public IEnumerator<T> iterator() {
        return elements.GetEnumerator();
    }

    public int lastIndexOf(Object x) {
        int insertPoint = elements.BinarySearch((T) x);
        
        if (insertPoint < 0) {
            return insertPoint;
        }

        for (int i = insertPoint + 1; i < elements.Count; i++) {
            if (x.Equals(elements[i])) {
                insertPoint++;
            } else {
                return insertPoint;
            }
        }

        return insertPoint;
    }

    public int indexOf(Object x) {
        int insertPoint = elements.BinarySearch((T) x);
        
        if (insertPoint < 0) {
            return insertPoint;
        }

        for (int i = insertPoint - 1; i >= 0; i--) {
            if (x.Equals(elements[i])) {
                insertPoint--;
            } else {
                return insertPoint;
            }
        }

        return insertPoint;
    }

    public bool remove(Object x) {
        return elements.Remove(x);
    }

    public T remove(int x) {
        T item = elements[x];
        elements.RemoveAt(x); // RemoveAt doesn't return anything
        return item;
    }

    public void add(int i, T x) {
        // Only changes the list if i is the correct position

        int insertionPoint = elements.BinarySearch(x);
        // Math.Log((double) elements.Count) time complexity

        if (insertionPoint < 0) {
            int ip = -(insertionPoint + 1);
            if (i == ip) {
                elements.InsertAt(i, x);
            } else {
                // in list already
                if (i == insertionPoint) {
                    elements.InsertAt(i, x);
                }
            }
        }
    }

    public bool add(T x) {
        int insertionPoint = elements.BinarySearch(x);
        // Math.Log((double) elements.Count) time complexity
        
        if (insertionPoint < 0) {
            int ip = -(insertionPoint + 1);
            elements.InsertAt(ip, x);
        } else {
            elements.InsertAt(insertionPoint, x);
        }

        return true;
    }

    public bool add(T x, int nCopies) {
        int insertionPoint = elements.BinarySearch(x);
        // Math.Log((double) elements.Count) time complexity

        if (insertionPoint < 0) {
            int ip = -(insertionPoint + 1);
            for (int i = 0; i < nCopies; i++) {
                elements.InsertAt(ip, x);
            }
        } else {
            for (int i = 0; i < nCopies; i++) {
                elements.InsertAt(insertionPoint, x);
            }
        }

        return true;
    }

    public T set(int i, T x) {
        int insertionPoint = elements.BinarySearch(x);
        // Math.Log((double) elements.Count) time complexity

        if (insertionPoint < 0) {
            int ip = -(insertionPoint + 1);
            if (i == ip) {
                elements[i] = x;
                return x;
            }
            return null;
        } else {
            return x; // in list already
        }
        
        // Another interpretation is to remove the element at
        // position i, and insert x.
    }

    public void clear() {
        elements.Clear();
    }

    public bool addAll(int index, ICollection<T> col) {
        bool res = elements.AddRange(col);
        elements.Sort();
        return true;
    } // More efficient to sort col and then merge

    public SortedSequence<T> unionSortedSequence(SortedSequence<T> sq) {
        List<T> res = new List<T>();

        int reached = 0;
        int i = 0;

        while (i < elements.Count && reached < sq.elements.Count) {
            IComparable<T> obj = (IComparable<T>) elements[i];
            IComparable<T> sqelem = (IComparable<T>) sq.elements[reached];

            if (obj.CompareTo(sqelem) < 0) {
                res.Add((T) obj);
                i++;
            } else {
                res.Add((T) sqelem);
                reached++;
            }
        }

        for (int j = i; j < elements.Count; j++) {
            res.Add((T) elements[j]);
        }

        for (int k = reached; k < sq.elements.Count; k++) {
            res.Add((T) sq.elements[k]);
        }

        SortedSequence<T> newsq = new SortedSequence<T>();
        newsq.elements = res;
        return newsq;
    }

    public ISet<T> uniqueSet() {
        return asSet();
    }

    public ISet<T> asSet() {
        SortedSet<T> res = new SortedSet<T>();

        if (elements.Count == 0) {
            return res;
        }

        T elem0 = elements[0];
        res.Add(elem0);

        T lastAdded = elem0;

        for (int i = 1; i < elements.Count; i++) {
            T elem = elements[i];
            if (!elem.Equals(lastAdded)) {
                res.Add(elem);
                lastAdded = elem;
            }
        }

        return res;
    }

    public SortedOrderedSet<T> asSortedOrderedSet() {
        ISet<T> rset = new HashSet<T>();
        List<T> rseq = new List<T>();

        if (elements.Count == 0) {
            return new SortedOrderedSet<T>();
        }

        T elem0 = elements[0];
        rset.Add(elem0);
        rseq.Add(elem0);

        T lastAdded = elem0;

        for (int i = 1; i < elements.Count; i++) {
            T elem = elements[i];
            if (elem.Equals(lastAdded)) {
                continue
            }
            rset.Add(elem);
            rseq.Add(elem);
            lastAdded = elem;
        }

        SortedSequence<T> rsseq = new SortedSequence<T>();
        rsseq.elements = rseq;

        SortedOrderedSet<T> res = new SortedOrderedSet<T>();
        res.setElementSet(rset);
        res.setElementSeq(rsseq);
        return res;
    }

    public bool contains(Object x) {
        int insertionPoint = elements.BinarySearch((T) x);
        return !(insertionPoint < 0);
    }

    public bool retainAll(ICollection<T> col) {
        oldElements = elements;
        elements.RemoveAll(!col.Contains);
        return elementSet != oldElementSet;
    }

    public SortedSequence<T> intersection(SortedSequence<T> sq) {
        List<T> res = new List<T>();

        int reached = 0;
        int i = 0;

        while (i < elements.Count && reached < sq.elements.Count) {
            IComparable<T> obj = (IComparable<T>) elements[i];
            IComparable<T> sqelem = (IComparable<T>) sq.elements[reached];

            if (obj.CompareTo(seqlem) == 0) {
                res.Add((T) obj);
                i++;
                reached++;
            } else if (obj.CompareTo(sqelem) < 0) {
                i++;
            } else {
                reached++;
            }
        }

        SortedSequence<T> newsq = new SortedSequence<T>();
        newsq.elements = res;
        return newsq;
    }

    public int size() {
        return elements.Count;
    }

    public bool isEmpty() {
        return elements.Count == 0;
    }

    public string toString() {
        return elements.ToString();
    }

    public bool removeAll(ICollection<T> col) {
        oldElements = elements;
        elements.RemoveAll(col.Contains);
        return oldElements != elements;
    }

    public bool addAll(ICollection<T> col) {
        return this.AddAll(0, col);
    }

    public bool containsAll(ICollection<T> col) {
        // Using BinarySearch repeatedly as an optimisation
        foreach (T element in col) {
            if (elements.BinarySearch(element) < 0) {
                return false;
            }
        }
        return true;
    }

    public Object[] toArray() {
        return elements.ToArray();
    }

    public T[] toArray(T[] arr) {
        T[] newArr = new T[elements.Count + arr.Length];
        for (int i = 0; i < elements.Count; i++) {
            newArr[i] = elements[i];
        }
        for (int i = 0; i < arr.Length; i++) {
            newArr[elements.Count + i] = arr[i];
        }
        return newArr;
    }

    public T min() {
        return elements[0];
    }

    public T max() {
        return elements[elements.Count - 1];
    }

    public int getCount(T x) {
        // Math.Log((double) elements.Count) time complexity
        int insertionPoint = this.lastIndexOf(x);

        if (insertionPoint < 0) {
            return 0;
        } else {
            int cnt = 0;
            for (int i = insertionPoint; i >= 0; i--) {
                if (x.Equals(elements[i])) {
                    cnt++;
                }
            }
            return cnt;
        }
    }

    public bool equals(Object col) {
        if (col is SortedSequence<T>) {
            SortedSequence<T> ss = (SortedSequence<T>) col;
            if (col.Count != ss.elements.Count) {
                return false;
            }
            for (int i = 0; i < col.Count; i++) {
                if (col[i] != ss.elements[i]) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    // Skipping implementation of spliterator() (for now)

    static void Main(string[] args) {
        SortedSequence<string> ss = new SortedSequence<string>();

        ss.add("bb", 2);
        ss.add("aa", 3);
        ss.add("cc", 1);
        ss.add("bb", 6);

        Console.WriteLine(ss);
        Console.WriteLine(ss.indexOf("aa"));
        Console.WriteLine(ss.indexOf("cc"));
        Console.WriteLine(ss.getCount("aa"));
        Console.WriteLine(ss.getCount("bb"));

        SortedSequence<string> tt = new SortedSequence<string>();
        tt.add("aa", 2);
        tt.add("cc", 4);
        tt.add("ee", 2);

        SortedSequence<string> pp = ss.unionSortedSequence(tt);
        Console.WriteLine(pp);

        SortedSequence<string> qq = ss.intersection(tt);
        Console.WriteLine(qq);
    }
}
