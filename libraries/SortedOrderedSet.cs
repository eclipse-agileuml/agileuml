using System;
using System.Collections.Generic;

class SortedOrderedSet<T>: List<T>, ISet<T>, IComparable<T> {
    private HashSet<T> elementSet = new HashSet<T>();
    private SortedSequence<T> elementSeq = new SortedSequence<T>();

    public SortedOrderedSet() {}

    public SortedOrderedSet(ISet<T> col) {
        elementSet = new HashSet<T>(col);
        elementSeq = new SortedSequence<T>(col);
    }

    public SortedOrderedSet<T> clone() {
        HashSet<T> elems = (HashSet<T>) elementSet.MemberwiseClone();
        SortedSequence<T> ss = (SortedSequence<T>) elementSeq.MemberwiseClone();
        SortedOrderedSet<T> sos = new SortedOrderedSet<T>();
        sos.elementSet = elems;
        sos.elementSeq = ss;
        return sos;
    }

    public void setElementSet(ISet<T> col) {
        elementSet = new HashSet<T>(col);
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
}
