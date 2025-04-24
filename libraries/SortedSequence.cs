using System;
using System.Collections;
using System.Collections.Generic;

class SortedSequence<T>: List<T>, IComparable<T> {
    List<T> elements = new List<T>();
    
    public SortedSequence() {}

    public SortedSequence(List<T> col) {
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
    }
}
