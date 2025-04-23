using System;
using System.Collections;
using System.Collections.Generic;

class SortedSequence: List<T> {
    ArrayList elements = new ArrayList();
    
    public SortedSequence() {}

    public SortedSequence(List<T> col) {
        elements = new ArrayList();
        elements.AddRange(col);
        elements.Sort();
    }
}
