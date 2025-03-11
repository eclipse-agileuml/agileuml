template<class _T>
class SortedOrderedSet 
{
private:
    set<_T>* elementSet = new set<_T>(); // or unordered_set
    SortedSequence<_T>* elementSeq = new SortedSequence<_T>();
public:
    SortedOrderedSet()
    { }

    SortedOrderedSet(vector<_T>* sq)
    {
        std::copy(sq->begin(), sq->end(), elementSet->begin());
        std::copy(sq->begin(), sq->end(), elementSeq->begin());
        std::sort(elementSeq->begin(), elementSeq->end());
    }

    bool includes(_T x)
    {
        if (elementSet->find(x) != elementSet->end())
        {
            return true;
        }
        return false;
    }

    void including(_T x)
    {
        if (elementSet->find(x) != elementSet->end())
        {
            return;
        }
        elementSet->insert(x);
        elementSeq->including(x); 
    }

    _T at(int i)
    {
        return elementSeq->at(i);
    }

    void excluding(_T x)
    {
        elementSet->erase(x);
        elementSeq->excluding(x); 
    }

    void subtract(vector<_T>* b)
    {
        for (int i = 0; i < b->size(); i++)
        {
            this->excluding((*b)[i]);
        }
    }

    void subtract(std::set<_T>* b)
    {
        for (auto iter = b->begin(); iter != b->end(); iter++)
        {
            this->excluding(*iter);
        }
    }

    void unionSortedOrderedSet(vector<_T>* b)
    {
        for (int i = 0; i < b->size(); i++)
        {
            this->including((*b)[i]);
        }
    }

    void unionSortedOrderedSet(std::set<_T>* b)
    {
        for (auto iter = b->begin(); iter != b->end(); iter++)
        {
            this->including(*iter);
        }
    }

    void intersection(vector<_T>* sq)
    {
        std::set<_T>* removed = new std::set<_T>();

        for (int i = 0; i < elementSeq->size(); i++)
        {
            _T x = elementSeq->at(i);
            if (std::find(sq->begin(), sq->end(), x) != sq->end()) {}
            else
            {
                removed->insert(x);
            }
        }

        for (auto iter = removed->begin(); iter != removed->end(); iter++)
        {
            this->excluding(*iter);
        }
    }

    void intersection(std::set<_T>* st)
    {
        std::set<_T>* removed = new std::set<_T>();

        for (int i = 0; i < elementSeq->size(); i++)
        {
            _T x = elementSeq->at(i);
            if (st->find(x) != st->end()) {}
            else
            {
                removed->insert(x);
            }
        }

        for (auto iter = removed->begin(); iter != removed->end(); iter++)
        {
            this->excluding(*iter);
        }
    }

    auto begin() { return elementSeq->begin(); }

    auto end() { return elementSeq->end(); }

    int size() { return elementSeq->size(); }

    _T min() { return elementSeq->at(0); }

    _T max()
    {
        int sze = elementSeq->size();
        if (sze == 0) { return NULL; }
        return elementSeq->at(sze - 1);
    }

    SortedOrderedSet<_T>* subSortedOrderedSet(int i, int j)
    { /* OCL indexing i, j: 1..n */

        int n = elementSeq->size();
        if (n == 0)
        {
            return new SortedOrderedSet<_T>();
        }

        if (i < 1) { i = 1; }

        SortedSequence<_T>* elems = new SortedSequence<_T>();
        set<_T>* eset = new set<_T>(); 

        SortedOrderedSet<_T>* res = new SortedOrderedSet<_T>();

        for (int k = i - 1; k < j && k < n; k++)
        {
            _T x = elementSeq->at(k); 
            eset->insert(x); 
            elems->including(x);
        }

        res->elementSet = eset; 
        res->elementSeq = elems;
        return res;
    }

    SortedOrderedSet<_T>* select(std::function<bool(_T)> f)
    {
        SortedSequence<_T>* elems = new SortedSequence<_T>();
        set<_T>* eset = new set<_T>();

        SortedOrderedSet<_T>* res = new SortedOrderedSet<_T>();

        for (int i = 0; i < elementSeq->size(); ++i)
        {
            _T x = elementSeq->at(i);
            if (f(x))
            {
                eset->insert(x); 
                elems->including(x);
            }
        }

        res->elementSet = eset; 
        res->elementSeq = elems;
        return res;
    }

    SortedOrderedSet<_T>* reject(std::function<bool(_T)> f)
    {
        SortedSequence<_T>* elems = new SortedSequence<_T>();
        set<_T>* eset = new set<_T>();

        SortedOrderedSet<_T>* res = new SortedOrderedSet<_T>();

        for (int i = 0; i < elementSeq->size(); ++i)
        {
            _T x = elementSeq->at(i);
            if (f(x)) {} 
            else 
            {
                eset->insert(x);
                elems->including(x);
            }
        }

        res->elementSet = eset;
        res->elementSeq = elems;
        return res;
    }

    SortedOrderedSet<_T>* front()
    {
        int n = elementSeq->size();
        if (n == 0)
        {
            return new SortedOrderedSet<_T>();
        }

        return subSortedOrderedSet(1, n - 1);
    }

    SortedOrderedSet<_T>* tail()
    {
        int n = elementSeq->size();
        if (n == 0)
        {
            return new SortedOrderedSet<_T>();
        }

        return subSortedOrderedSet(2, n);
    }

    template<class R>
    vector<R>* collect(std::function<R(_T)> f)
    {
        vector<R>* res = new vector<R>();
        for (int i = 0; i < elementSeq->size(); i++)
        {
            _T x = elementSeq->at(i);
            res->push_back(f(x));
        }

        return res;
    }
};
