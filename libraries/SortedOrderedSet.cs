using System;
using System.Collections.Generic;

class SortedOrderedSet<T>: List<T>, ISet<T> where T: IComparable<T> {
    private SortedSet<T> elementSet = new SortedSet<T>();
    private SortedSequence<T> elementSeq = new SortedSequence<T>();

    public SortedOrderedSet() {}

    public SortedOrderedSet(ISet<T> col) {
        elementSet = new SortedSet<T>(col);
        elementSeq = new SortedSequence<T>(col);
    }

    public SortedOrderedSet<T> clone() {
        SortedSet<T> elems = (SortedSet<T>) elementSet.MemberwiseClone();
        SortedSequence<T> ss = (SortedSequence<T>) elementSeq.MemberwiseClone();
        SortedOrderedSet<T> sos = new SortedOrderedSet<T>();
        sos.elementSet = elems;
        sos.elementSeq = ss;
        return sos;
    }

    public void setElementSet(ISet<T> col) {
        elementSet = new SortedSet<T>(col);
    }

    public void setElementSeq(SortedSequence<T> col) {
        elementSeq = col;
    }

    public bool contains(Object x) {
        return elementSet.Contains(x);
    }

    public bool isEmpty() {
        return elementSet.Count == 0;
    }

    public int size() {
        return elementSet.Count;
    }

    public void clear() {
        elementSet.Clear();
        elementSeq.Clear();
    }

    public bool removeAll(IEnumerable<T> col) {
        oldElementSet = elementSet;
        elementSeq.RemoveAll(col.Contains);
        elementSet.ExceptWith(col);
        if (elementSet == oldElementSet) {
            return false;
        }
        return true;
    }

    public bool retainAll(IEnumerable<T> col) {
        oldElementSet = elementSet;
        elementSeq.RemoveAll(!col.Contains);
        foreach (T element in col) {
            if (elementSet.Contains(element)) {
                elementSet.Remove(element);
            }
        }
        if (elementSet == oldElementSet) {
            return false;
        }
        return true;
    }

    public SortedOrderedSet<T> intersection(SortedOrderedSet<T> sq) {
        SortedSequence<T> rseq = elementSeq.intersection(sq.elementSeq);
        return rseq.asSortedOrderedSet();
    }

    public bool addAll(ICollection<T> col) {
        bool changed = false;
        foreach (Object obj in col) {
            bool added = elementSet.Add((T) obj);
            if (added) {
                changed = true;
                elementSeq.Add((T) obj);
            }
        }
        return changed;
    }

    public bool addAll(int i, ICollection<T> col) {
        return addAll(col); // ignores i
    }

    public SortedOrderedSet<T> unionSortedOrderedSet(SortedOrderedSet<T> sq) {
        SortedSequence<T> rseq = elementSeq.unionSortedSequence(sq.elementSeq);
        return rseq.asSortedOrderedSet();
    }

    public bool containsAll(ICollection<T> col) {
        foreach (T item in col) {
            if (!elementSet.Contains(item)) {
                return false;
            }
        }
        return true;
    }

    public bool remove(Object x) {
        bool removed = elementSet.Remove((T) x);
        if (removed) {
            elementSeq.Remove((T) x);
        }
        return removed;
    }

    public bool add(T x) {
        bool added = elementSet.Add(x);
        if (added) {
            elementSeq.Add(x);
        }
        return added;
    }

    public Object[] toArray() {
        return elementSeq.toArray();
    }

    public Object[] toArray(Object[] arr) {
        return elementSeq.toArray(arr);
    }

    public ISet<T> asSet() {
        return elementSet;
    }

    public IEnumerator<T> iterator() {
        return elementSeq.iterator();
    }

    public List<T>.Enumerator listIterator(int x) {
        return elementSeq.listIterator(x);
    }

    public List<T>.Enumerator listIterator() {
        return elementSeq.listIterator();
    }

    // Skipping implementation of spliterator() (for now)

    public T get(int i) {
        return elementSeq[i];
    }

    public List<T> subList(int i, int j) {
        return elementSeq.subList(i, j);
    }

    public int lastIndexOf(Object x) {
        return elementSeq.lastIndexOf((T) x);
    }

    public int indexOf(Object x) {
        return elementSeq.indexOf((T) x);
    }

    public T remove(int i) {
        return elementSeq.remove(i);
    }

    public void add(int i, T x) {
        bool added = elementSet.Add((T) x);
        if (added) {
            elementSeq.add((T) x);
        }
    } // ignores i

    // public override T set(int i, T x) {
    public T set(int i, T x) {
        bool added = elementSet.Add((T) x);
        if (added) {
            elementSeq.add((T) x);
        }
        return null;
    } // ignores i

    public String toString() {
        return elementSeq.toString();
    }

    public T min() {
        return elementSeq.min();
    }

    public T max() {
        return elementSeq.max();
    }

    public int getCount(Object x) {
        if (elementSet.Contains((T) x)) {
            return 1;
        }
        return 0;
    }

    public bool equals(Object col) {
        if (col is SortedOrderedSet) {
            SortedOrderedSet<T> ss = (SortedOrderedSet<T>) col;
            return elementSeq.equals(ss.elementSeq);
        }
        return false;
    }

    static void Main(string[] args) {
        SortedOrderedSet<string> sos = new SortedOrderedSet<string>();

        sos.add("cc");
        sos.add("bb");
        sos.add("aa");
        sos.add("bb");
        sos.add("aa");

        Console.Writeline(sos);

        SortedOrderedSet<string> newelems = new SortedOrderedSet<string>();

        newelems.add("dd");
        newelems.add("cc");
        newelems.add("bb");

        Console.Writeline(sos.intersection(newelems));
    }
}
