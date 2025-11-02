#pragma once

#undef max
#undef min

using namespace std;

class MathLibrary;

class SustainabilityTest;

class Controller;

class Runnable
{
public:
    virtual void run() { };
}; // Interface for <<active>> classes

// C++19

template<class _T>
class SortedSequence {
private: 
    vector<_T>* elements = new vector<_T>(); 
public:
    SortedSequence()
    { }

    SortedSequence(vector<_T>* sq)
    {
        std::copy(sq->begin(), sq->end(), elements->begin()); 
        std::sort(elements->begin(), elements->end()); 
    }

    bool includes(_T x)
    {
       return std::binary_search(elements->begin(), elements->end(), x); 
    }

    void including(_T x)
    {
        auto pos = std::lower_bound(elements->begin(), elements->end(), x);
        elements->insert(pos, x); 
    }

    _T at(int i)
    {
        return elements->at(i);
    }

    void excluding(_T x)
    {
        auto pos = std::equal_range(elements->begin(), elements->end(), x); 
        elements->erase(pos.first, pos.second); 
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

    void unionSortedSequence(SortedSequence<_T>* sq)
    { // addAll
        vector<_T>* newelems = new vector<_T>();

        int i = 0;
        int j = 0;
        while (i < elements->size() && j < sq->elements->size())
        {
            _T x = elements->at(i);
            _T y = sq->elements->at(j); 
            if (x == y)
            {
                newelems->push_back(x); newelems->push_back(y); i++; j++;
            }
            else if (x < y) {
                newelems->push_back(x); i++;
            }
            else
            {
                newelems->push_back(y); j++;
            }
        }

        while (i < elements->size())
        {
            newelems->push_back(elements->at(i)); i++;
        }

        while (j < sq->elements->size())
        {
            newelems->push_back(sq->elements->at(j)); j++;
        }

        elements = newelems;
    }

    void unionSortedSequence(vector<_T>* b)
    {
        for (int i = 0; i < b->size(); i++)
        {
            this->including((*b)[i]);
        }
    }

    void unionSortedSequence(std::set<_T>* b)
    {
        for (auto iter = b->begin(); iter != b->end(); iter++)
        {
            this->including(*iter);
        }
    }

    void intersection(SortedSequence<_T>* sq)
    { // retainAll
        vector<_T>* newelems = new vector<_T>(); 

        int i = 0; 
        int j = 0; 
        while (i < elements->size() && j < sq->elements->size())
        {
           _T x = elements->at(i);
           if (x == sq->elements->at(j))
           {
             newelems->push_back(x); i++; j++;
           }
           else if (x < sq->elements->at(j)) {
                    i++;
           }
           else
           {
             j++;
           }
       }
       elements = newelems; 
    }

    void intersection(vector<_T>* sq)
    {
        std::set<_T>* removed = new std::set<_T>(); 

        for (int i = 0; i < elements->size(); i++)
        {
            _T x = (*elements)[i];  
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

            for (int i = 0; i < elements->size(); i++)
            {
                _T x = (*elements)[i]; 
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


    SortedSequence<_T>* select(std::function<bool(_T)> f)
    {
        vector<_T>* elems = new vector<_T>(); 

        for (int i = 0; i < elements->size(); ++i)
        {
            _T x = elements->at(i); 
            if (f(x))
            {
                elems->push_back(x); 
            }
        }

        SortedSequence<_T>* res = new SortedSequence<_T>();
        res->elements = elems; 
        return res;
    }

    SortedSequence<_T>* reject(std::function<bool(_T)> f)
    {
        vector<_T>* elems = new vector<_T>();

        for (int i = 0; i < elements->size(); ++i)
        {
            _T x = elements->at(i);
            if (f(x)) {} 
            else
            {
                elems->push_back(x);
            }
        }

        SortedSequence<_T>* res = new SortedSequence<_T>();
        res->elements = elems;
        return res;
    }

    template<class R>
    vector<R>* collect(std::function<R(_T)> f)
    {
        vector<R>* res = new vector<R>();
        for (int i = 0; i < elements->size(); i++)
        {
            _T x = elements->at(i); 
            res->push_back(f(x));
        }

        return res;
    }

    auto begin() { return elements->begin(); }

    auto end() { return elements->end(); }

    int size() { return elements->size(); }

    _T min() { return elements->at(0); }

    _T max() 
    { int sze = elements->size();
      if (sze == 0) { return NULL; } 
      return elements->at(sze-1);
    }

    SortedSequence<_T>* subSequence(int i, int j)
    { /* OCL indexing i, j: 1..n */

        int n = elements->size();
        if (n == 0)
        {
            return new SortedSequence<_T>();
        }

        if (i < 1) { i = 1; }

        vector<_T>* elems = new vector<_T>();
        SortedSequence<_T>* res = new SortedSequence<_T>(); 

        for (int k = i-1; k < j && k < n; k++)
        {
            elems->push_back(elements->at(k)); 
        }
        res->elements = elems; 
        return res; 
    }

    SortedSequence<_T>* front()
    {
        int n = elements->size(); 
        if (n == 0)
        {
            return new SortedSequence<_T>();
        }
        return subSequence(1, n - 1); 
    }

    SortedSequence<_T>* tail()
    {
        int n = elements->size();
        if (n == 0)
        {
            return new SortedSequence<_T>();
        }
        return subSequence(2, n);
    }


};

template<class _T>
class SortedOrderedSet 
{
private:
    set<_T>* elementSet = new set<_T>(); 
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

template<class _T>
class UmlRsdsLib {
public:
    static long long getTime()
    {
        return 1000 * time(NULL);
    }

    static struct tm* getDate()
    {
        time_t t = time(NULL);
        struct tm* res = localtime(&t);
        return res;
    }


    static double roundTo(double x, int n)
    { if (n == 0) 
      { return round(x); }
      double y = x*pow(10,n); 
      return round(y)/pow(10,n);
    }

    static double truncateTo(double x, int n)
    { if (n < 0) 
      { return (int) x; }
      double y = x*pow(10,n); 
      return ((int) y)/pow(10,n);
    }


    static bool isIn(_T x, std::set<_T>* st)
    {
        return (st->find(x) != st->end());
    }

    static bool isIn(_T x, vector<_T>* sq)
    {
        return (find(sq->begin(), sq->end(), x) != sq->end());
    }

    static bool isSubset(std::set<_T>* s1, set<_T>* s2)
    {
        bool res = true;
        for (auto _pos = s1->begin(); _pos != s1->end(); ++_pos)
        {
            if (isIn(*_pos, s2)) {}
            else { return false; }
        }
        return res;
    }

    static bool isSubset(std::set<_T>* s1, vector<_T>* s2)
    {
        bool res = true;
        for (auto _pos = s1->begin(); _pos != s1->end(); ++_pos)
        {
            if (isIn(*_pos, s2)) {}
            else { return false; }
        }
        return res;
    }

    static bool isSubset(std::vector<_T>* s1, vector<_T>* s2)
    {
        bool res = true;
        for (auto _pos = s1->begin(); _pos != s1->end(); ++_pos)
        {
            if (isIn(*_pos, s2)) {}
            else { return false; }
        }
        return res;
    }

    static bool isSubset(std::vector<_T>* s1, set<_T>* s2)
    {
        bool res = true;
        for (auto _pos = s1->begin(); _pos != s1->end(); ++_pos)
        {
            if (isIn(*_pos, s2)) {}
            else { return false; }
        }
        return res;
    }

    static std::set<_T>* makeSet(_T x)
    {
        std::set<_T>* res = new std::set<_T>();
        res->insert(x);
        return res;
    }

    static vector<_T>* makeSequence(_T x)
    {
        vector<_T>* res = new vector<_T>();
        res->push_back(x);
        return res;
    }

    static std::set<_T>* addSet(std::set<_T>* s, _T x)
    {
        s->insert(x);
        return s;
    }

    static vector<_T>* addSequence(vector<_T>* s, _T x)
    {
        s->push_back(x);
        return s;
    }

    static vector<string>* addSequenceString(vector<string>* s, string x)
    {
        s->push_back(x);
        return s;
    }

    static vector<_T>* asSequence(std::set<_T>* c)
    {
        vector<_T>* res = new vector<_T>();
        for (auto _pos = c->begin(); _pos != c->end(); ++_pos)
        {
            res->push_back(*_pos);
        }
        return res;
    }

    static vector<_T>* asSequence(std::vector<_T>* c)
    {
        return c;
    }


    static vector<string>* tokenise(vector<string>* res, string str)
    {
        bool inspace = true;
        string* current = new string("");
        for (int i = 0; i < str.length(); i++)
        {
            if (str[i] == '.' || isspace(str[i]) > 0)
            {
                if (inspace) {}
                else
                {
                    res->push_back(*current);
                    current = new string("");
                    inspace = true;
                }
            }
            else
            {
                if (inspace) { inspace = false; }
                current->append(str.substr(i, 1));
            }
        }
        if (current->length() > 0) { res->push_back(*current); }
        delete current;
        return res;
    }



    static set<_T>* copySet(set<_T>* a)
    {
        set<_T>* res = new set<_T>();
        res->insert(a->begin(), a->end());
        return res;
    }

    static vector<_T>* copySequence(vector<_T>* a)
    {
        vector<_T>* res = new vector<_T>();
        res->insert(res->end(), a->begin(), a->end());
        return res;
    }

    static map<string, _T>* copyMap(map<string, _T>* m)
    {
        map<string, _T>* res = new map<string, _T>();
        for (auto iter = m->begin(); iter != m->end(); ++iter)
        {
            string key = iter->first;
            (*res)[key] = iter->second;
        }
        return res;
    }

    static string collectionToString(vector<_T>* c)
    {
        ostringstream buff;
        buff << "Sequence{";
        for (auto _pos = c->begin(); _pos != c->end(); ++_pos)
        {
            buff << *_pos;
            if (_pos + 1 < c->end())
            {
                buff << ", ";
            }
        }
        buff << "}";
        return buff.str();
    }

    static string collectionToString(std::set<_T>* c)
    {
        ostringstream buff;
        buff << "Set{";
        auto _pos = c->begin(); 
        if (_pos == c->end())
        {
            buff << "}"; 
        }
        else
        {
            buff << *_pos;
            ++_pos;
            for (; _pos != c->end(); ++_pos)
            {
                buff << ", ";
                buff << *_pos;
            }
            buff << "}";
        }
        return buff.str();
    }

    static string collectionToString(map<string, _T>* c)
    {
        ostringstream buff;
        buff << "Map{";
        int sze = c->size();
        int count = 0;
        for (auto it = c->begin(); it != c->end(); it++)
        {
            buff << (*it).first;
            buff << " |-> ";
            buff << (*it).second;
            if (count + 1 < sze)
            {
                buff << ", ";
            }
            count++;
        }
        buff << "}";
        return buff.str();
    }



    static int oclRound(double d)
    {
        int f = (int)floor(d);
        if (d >= f + 0.5)
        {
            return f + 1;
        }
        else
        {
            return f;
        }
    }



    static _T max(std::set<_T>* l)
    {
        return *std::max_element(l->begin(), l->end());
    }
    static _T max(vector<_T>* l)
    {
        return *std::max_element(l->begin(), l->end());
    }


    static _T min(std::set<_T>* l)
    {
        return *std::min_element(l->begin(), l->end());
    }
    static _T min(vector<_T>* l)
    {
        return *std::min_element(l->begin(), l->end());
    }


    static std::set<_T>* unionSet(std::set<_T>* a, std::set<_T>* b)
    {
        std::set<_T>* res = new std::set<_T>();
        res->insert(a->begin(), a->end());
        res->insert(b->begin(), b->end());
        return res;
    }

    static std::set<_T>* unionSet(std::vector<_T>* a, std::set<_T>* b)
    {
        std::set<_T>* res = new std::set<_T>();
        res->insert(a->begin(), a->end());
        res->insert(b->begin(), b->end());
        return res;
    }

    static std::set<_T>* unionSet(std::set<_T>* a, vector<_T>* b)
    {
        std::set<_T>* res = new std::set<_T>();
        res->insert(a->begin(), a->end());
        res->insert(b->begin(), b->end());
        return res;
    }

    static std::set<_T>* unionSet(std::vector<_T>* a, vector<_T>* b)
    {
        std::set<_T>* res = new std::set<_T>();
        res->insert(a->begin(), a->end());
        res->insert(b->begin(), b->end());
        return res;
    }



    static vector<_T>* subtract(vector<_T>* a, vector<_T>* b)
    {
        vector<_T>* res = new vector<_T>();
        for (int i = 0; i < a->size(); i++)
        {
            if (UmlRsdsLib<_T>::isIn((*a)[i], b)) {}
            else { res->push_back((*a)[i]); }
        }
        return res;
    }

    static vector<_T>* subtract(vector<_T>* a, std::set<_T>* b)
    {
        vector<_T>* res = new vector<_T>();
        for (int i = 0; i < a->size(); i++)
        {
            if (UmlRsdsLib<_T>::isIn((*a)[i], b)) {}
            else
            {
                res->push_back((*a)[i]);
            }
        }
        return res;
    }

    static std::set<_T>* subtract(std::set<_T>* a, std::set<_T>* b)
    {
        std::set<_T>* res = new std::set<_T>();
        for (auto _pos = a->begin(); _pos != a->end(); ++_pos)
        {
            if (UmlRsdsLib<_T>::isIn(*_pos, b)) {}
            else
            {
                res->insert(*_pos);
            }
        }
        return res;
    }

    static std::set<_T>* subtract(std::set<_T>* a, vector<_T>* b)
    {
        std::set<_T>* res = new std::set<_T>();
        for (auto _pos = a->begin(); _pos != a->end(); ++_pos)
        {
            if (UmlRsdsLib<_T>::isIn(*_pos, b)) {}
            else
            {
                res->insert(*_pos);
            }
        }
        return res;
    }

    static string subtract(string a, string b)
    {
        string res = "";
        for (int i = 0; i < a.length(); i++)
        {
            if (b.find(a[i]) == string::npos) { res = res + a[i]; }
        }
        return res;
    }



    static std::set<_T>* intersection(std::set<_T>* a, std::set<_T>* b)
    {
        std::set<_T>* res = new std::set<_T>();
        for (auto _pos = a->begin(); _pos != a->end(); ++_pos)
        {
            if (UmlRsdsLib<_T>::isIn(*_pos, b))
            {
                res->insert(*_pos);
            }
        }
        return res;
    }

    static bool excludesAll(std::set<_T>* a, std::set<_T>* b)
    {
        for (auto _pos = a->begin(); _pos != a->end(); ++_pos)
        {
            if (UmlRsdsLib<_T>::isIn(*_pos, b))
            {
                return false;
            }
        }
        return true;
    }

    static std::set<_T>* intersection(std::set<_T>* a, vector<_T>* b)
    {
        std::set<_T>* res = new std::set<_T>();
        for (auto _pos = a->begin(); _pos != a->end(); ++_pos)
        {
            if (UmlRsdsLib<_T>::isIn(*_pos, b))
            {
                res->insert(*_pos);
            }
        }
        return res;
    }

    static vector<_T>* intersection(vector<_T>* a, std::set<_T>* b)
    {
        vector<_T>* res = new vector<_T>();
        for (int i = 0; i < a->size(); i++)
        {
            if (UmlRsdsLib<_T>::isIn((*a)[i], b))
            {
                res->push_back((*a)[i]);
            }
        }
        return res;
    }

    static vector<_T>* intersection(vector<_T>* a, vector<_T>* b)
    {
        vector<_T>* res = new vector<_T>();
        for (int i = 0; i < a->size(); i++)
        {
            if (UmlRsdsLib<_T>::isIn((*a)[i], b))
            {
                res->push_back((*a)[i]);
            }
        }
        return res;
    }

    static bool excludesAll(vector<_T>* a, vector<_T>* b)
    {
        for (int i = 0; i < a->size(); i++)
        {
            if (UmlRsdsLib<_T>::isIn((*a)[i], b))
            {
              return true;
            }
        }
        return false;
    }


    static std::set<_T>* symmetricDifference(vector<_T>* a, vector<_T>* b)
    {
        std::set<_T>* res = new std::set<_T>();
        for (int i = 0; i < a->size(); i++)
        {
            if (UmlRsdsLib<_T>::isIn((*a)[i], b)) {}
            else { res->insert((*a)[i]); }
        }
        for (int i = 0; i < b->size(); i++)
        {
            if (UmlRsdsLib<_T>::isIn((*b)[i], a)) {}
            else { res->insert((*b)[i]); }
        }
        return res;
    }

    static std::set<_T>* symmetricDifference(std::set<_T>* a, vector<_T>* b)
    {
        std::set<_T>* res = new std::set<_T>();
        for (auto _pos = a->begin(); _pos != a->end(); _pos++)
        {
            if (UmlRsdsLib<_T>::isIn(*_pos, b)) {}
            else { res->insert(*_pos); }
        }
        for (int i = 0; i < b->size(); i++)
        {
            if (UmlRsdsLib<_T>::isIn((*b)[i], a)) {}
            else { res->insert((*b)[i]); }
        }
        return res;
    }

    static std::set<_T>* symmetricDifference(vector<_T>* a, std::set<_T>* b)
    {
        std::set<_T>* res = new std::set<_T>();
        for (int i = 0; i < a->size(); i++)
        {
            if (UmlRsdsLib<_T>::isIn((*a)[i], b)) {}
            else { res->insert((*a)[i]); }
        }
        for (auto _pos = b->begin(); _pos != b->end(); _pos++)
        {
            if (UmlRsdsLib<_T>::isIn(*_pos, a)) {}
            else { res->insert(*_pos); }
        }
        return res;
    }

    static std::set<_T>* symmetricDifference(std::set<_T>* a, std::set<_T>* b)
    {
        std::set<_T>* res = new std::set<_T>();
        for (auto _pos = a->begin(); _pos != a->end(); _pos++)
        {
            if (UmlRsdsLib<_T>::isIn(*_pos, b)) {}
            else { res->insert(*_pos); }
        }
        for (auto _pos = b->begin(); _pos != b->end(); _pos++)
        {
            if (UmlRsdsLib<_T>::isIn(*_pos, a)) {}
            else { res->insert(*_pos); }
        }
        return res;
    }



    static bool isUnique(vector<_T>* evals)
    {
        std::set<_T> vals;
        for (int i = 0; i < evals->size(); i++)
        {
            _T ob = (*evals)[i];
            if (vals.find(ob) != vals.end()) { return false; }
            vals.insert(ob);
        }
        return true;
    }
    static bool isUnique(std::set<_T>* evals)
    {
        return true;
    }


    static long gcd(long xx, long yy)
    {
        long x = labs(xx);
        long y = labs(yy);
        while (x != 0 && y != 0)
        {
            long z = y;
            y = x % y;
            x = z;
        }
        if (y == 0)
        {
            return x;
        }
        if (x == 0)
        {
            return y;
        }
        return 0;
    }


    static string byte2char(int b)
    {
        int arr[] = { 0 };
        arr[0] = b;
        string str = string((char*)arr);
        return str;
    }

    static int char2byte(string str)
    {
        if (str.length() == 0)
        {
            return -1;
        }
        char x = str[0];
        return (int)x;
    }


    static string sumString(vector<string>* a)
    {
        string _sum("");
        for (int i = 0; i < a->size(); i++)
        {
            _sum.append((*a)[i]);
        }
        return _sum;
    }

    static string sumString(std::set<string>* a)
    {
        string _sum("");
        std::set<string>::iterator _pos;
        for (_pos = a->begin(); _pos != a->end(); ++_pos)
        {
            _sum.append(*_pos);
        }
        return _sum;
    }

    static _T sum(vector<_T>* a)
    {
        _T _sum(0);
        for (int i = 0; i < a->size(); i++)
        {
            _sum += (*a)[i];
        }
        return _sum;
    }

    static _T sum(std::set<_T>* a)
    {
        _T _sum(0);
        for (auto _pos = a->begin(); _pos != a->end(); ++_pos)
        {
            _sum += *_pos;
        }
        return _sum;
    }



    static _T prd(vector<_T>* a)
    {
        _T _prd(1);
        for (int i = 0; i < a->size(); i++)
        {
            _prd *= (*a)[i];
        }
        return _prd;
    }

    static _T prd(std::set<_T>* a)
    {
        _T _prd(1);
        for (auto _pos = a->begin(); _pos != a->end(); ++_pos)
        {
            _prd *= *_pos;
        }
        return _prd;
    }



    static vector<_T>* concatenate(vector<_T>* a, vector<_T>* b)
    {
        vector<_T>* res = new vector<_T>();
        res->insert(res->end(), a->begin(), a->end());
        res->insert(res->end(), b->begin(), b->end());
        return res;
    }

    static vector<_T>* concatenate(vector<_T>* a, std::set<_T>* b)
    {
        vector<_T>* res = new vector<_T>();
        res->insert(res->end(), a->begin(), a->end());
        res->insert(res->end(), b->begin(), b->end());
        return res;
    }




    static std::set<_T>* asSet(vector<_T>* c)
    {
        std::set<_T>* res = new std::set<_T>();
        res->insert(c->begin(), c->end());
        return res;
    }

    static std::set<_T>* asSet(std::set<_T>* c)
    {
        return c;
    }

    static vector<_T>* asOrderedSet(vector<_T>* c)
    {
        vector<_T>* res = new vector<_T>();
        for (auto _pos = c->begin(); _pos != c->end(); ++_pos)
        {
            if (isIn(*_pos, res)) {}
            else
            {
                res->push_back(*_pos);
            }
        }
        return res;
    }

    static vector<_T>* asOrderedSet(set<_T>* c)
    {
        vector<_T>* res = new vector<_T>();
        for (auto _pos = c->begin(); _pos != c->end(); ++_pos)
        {
            res->push_back(*_pos);
        }
        return res;
    }

    static vector<_T>* randomiseSequence(vector<_T>* sq)
    {
        vector<_T>* res = new vector<_T>();
        for (auto _pos = sq->begin(); _pos != sq->end(); ++_pos)
        {
            res->push_back(*_pos);
        }
        std::random_shuffle(res->begin(), res->end());
        return res;
    }



    static vector<_T>* reverse(vector<_T>* a)
    {
        vector<_T>* res = new vector<_T>();
        res->insert(res->end(), a->begin(), a->end());
        std::reverse(res->begin(), res->end());
        return res;
    }

    static string reverse(string a)
    {
        string res("");
        for (int i = a.length() - 1; i >= 0; i--)
        {
            res = res + a[i];
        }
        return res;
    }



    static vector<_T>* front(vector<_T>* a)
    {
        vector<_T>* res = new vector<_T>();
        if (a->size() == 0) { return res; }
        auto _pos = a->end();
        _pos--;
        res->insert(res->end(), a->begin(), _pos);
        return res;
    }


    static vector<_T>* tail(vector<_T>* a)
    {
        vector<_T>* res = new vector<_T>();
        if (a->size() == 0) { return res; }
        auto _pos = a->begin();
        _pos++;
        res->insert(res->end(), _pos, a->end());
        return res;
    }

    static set<_T>* front(set<_T>* a)
    {
        set<_T>* res = new set<_T>();
        if (a->size() == 0) { return res; }
        auto _pos = a->end();
        _pos--;
        res->insert(a->begin(), _pos);
        return res;
    }

    static set<_T>* tail(set<_T>* a)
    {
        set<_T>* res = new set<_T>();
        if (a->size() == 0) { return res; }
        auto _pos = a->begin();
        _pos++;
        res->insert(_pos, a->end());
        return res;
    }

    static map<string,_T>* front(map<string,_T>* a)
    {
        map<string,_T>* res = new map<string,_T>();
        if (a->size() == 0) { return res; }
        auto _pos = a->end();
        _pos--;
        res->insert(a->begin(), _pos);
        return res;
    }

    static map<string,_T>* tail(map<string,_T>* a)
    {
        map<string,_T>* res = new map<string,_T>();
        if (a->size() == 0) { return res; }
        auto _pos = a->begin();
        _pos++;
        res->insert(_pos, a->end());
        return res;
    }

    static vector<_T>* sort(vector<_T>* a)
    {
        vector<_T>* res = new vector<_T>();
        res->insert(res->end(), a->begin(), a->end());
        std::sort(res->begin(), res->end());
        return res;
    }

    static vector<_T>* sort(std::set<_T>* a)
    {
        vector<_T>* res = new vector<_T>();
        res->insert(res->end(), a->begin(), a->end());
        std::sort(res->begin(), res->end());
        return res;
    }


    static vector<_T>* sort(vector<_T>* a, std::function<bool(_T, _T)> f)
    {
        vector<_T>* res = new vector<_T>();
        res->insert(res->end(), a->begin(), a->end());
        std::sort(res->begin(), res->end(), f);
        return res;
    }

    static vector<_T>* sort(std::set<_T>* a, std::function<bool(_T, _T)> f)
    {
        vector<_T>* res = new vector<_T>();
        res->insert(res->end(), a->begin(), a->end());
        std::sort(res->begin(), res->end(), f);
        return res;
    }

    static vector<int>* integerSubrange(int i, int j)
    {
        vector<int>* tmp = new vector<int>();
        for (int k = i; k <= j; k++)
        {
            tmp->push_back(k);
        }
        return tmp;
    }

    static string subrange(string s, int i, int j)
    {
        if (i < 1) { i = 1; }
        return s.substr(i - 1, j - i + 1);
    }

    static vector<_T>* subrange(vector<_T>* l, int i, int j)
    {
        if (i < 1) { i = 1; }
        if (j > l->size()) { j = l->size(); }
        vector<_T>* tmp = new vector<_T>();
        tmp->insert(tmp->end(), (l->begin()) + (i - 1), (l->begin()) + j);
        return tmp;
    }



    static vector<_T>* prepend(vector<_T>* l, _T ob)
    {
        vector<_T>* res = new vector<_T>();
        res->push_back(ob);
        res->insert(res->end(), l->begin(), l->end());
        return res;
    }


    static vector<_T>* append(vector<_T>* l, _T ob)
    {
        vector<_T>* res = new vector<_T>();
        res->insert(res->end(), l->begin(), l->end());
        res->push_back(ob);
        return res;
    }


    static int count(std::set<_T>* l, _T obj)
    {
        if (l->find(obj) != l->end()) { return 1; }
        else { return 0; }
    }

    static int count(vector<_T>* l, _T obj)
    {
        return std::count(l->begin(), l->end(), obj);
    }

    static int count(string s, string x)
    {
        int res = 0;
        if (s.length() == 0) { return res; }
        int ind = s.find(x);
        if (ind == string::npos) { return res; }
        string ss = s.substr(ind + 1, s.length() - ind - 1);
        res++;
        while (ind != string::npos)
        {
            ind = ss.find(x);
            if (ind == string::npos || ss.length() == 0) { return res; }
            res++;
            ss = ss.substr(ind + 1, ss.length() - ind - 1);
        }
        return res;
    }



    static vector<string>* characters(string str)
    {
        vector<string>* _res = new vector<string>();
        for (int i = 0; i < str.size(); i++)
        {
            _res->push_back(str.substr(i, 1));
        }
        return _res;
    }



    static _T any(vector<_T>* v)
    {
        if (v->size() == 0) { return 0; }
        return v->at(0);
    }

    static _T any(std::set<_T>* v)
    {
        if (v->size() == 0) { return 0; }
        auto _pos = v->begin();
        return *_pos;
    }



    static _T first(vector<_T>* v)
    {
        if (v->size() == 0) { return 0; }
        return v->at(0);
    }

    static _T first(std::set<_T>* v)
    {
        if (v->size() == 0) { return 0; }
        auto _pos = v->begin();
        return *_pos;
    }



    static _T last(vector<_T>* v)
    {
        if (v->size() == 0) { return 0; }
        return v->at(v->size() - 1);
    }

    static _T last(std::set<_T>* v)
    {
        if (v->size() == 0) { return 0; }
        auto _pos = v->end();
        _pos--;
        return *_pos;
    }



    static vector<_T>* maximalElements(vector<_T>* s, vector<int>* v)
    {
        vector<_T>* res = new vector<_T>();
        if (s->size() == 0) { return res; }
        int largest = (*v)[0];
        res->push_back((*s)[0]);

        for (int i = 1; i < s->size(); i++)
        {
            int next = (*v)[i];
            if (next > largest)
            {
                largest = next;
                res->clear();
                res->push_back((*s)[i]);
            }
            else if (largest == next)
            {
                res->push_back((*s)[i]);
            }
        }
        return res;
    }

    static vector<_T>* maximalElements(vector<_T>* s, vector<long>* v)
    {
        vector<_T>* res = new vector<_T>();
        if (s->size() == 0) { return res; }
        long largest = (*v)[0];
        res->push_back((*s)[0]);

        for (int i = 1; i < s->size(); i++)
        {
            long next = (*v)[i];
            if (next > largest)
            {
                largest = next;
                res->clear();
                res->push_back((*s)[i]);
            }
            else if (largest == next)
            {
                res->push_back((*s)[i]);
            }
        }
        return res;
    }

    static vector<_T>* maximalElements(vector<_T>* s, vector<string>* v)
    {
        vector<_T>* res = new vector<_T>();
        if (s->size() == 0) { return res; }
        string largest = (*v)[0];
        res->push_back((*s)[0]);

        for (int i = 1; i < s->size(); i++)
        {
            string next = (*v)[i];
            if (next > largest)
            {
                largest = next;
                res->clear();
                res->push_back((*s)[i]);
            }
            else if (largest == next)
            {
                res->push_back((*s)[i]);
            }
        }
        return res;
    }

    static vector<_T>* maximalElements(vector<_T>* s, vector<double>* v)
    {
        vector<_T>* res = new vector<_T>();
        if (s->size() == 0) { return res; }
        double largest = (*v)[0];
        res->push_back((*s)[0]);

        for (int i = 1; i < s->size(); i++)
        {
            double next = (*v)[i];
            if (next > largest)
            {
                largest = next;
                res->clear();
                res->push_back((*s)[i]);
            }
            else if (largest == next)
            {
                res->push_back((*s)[i]);
            }
        }
        return res;
    }


    static vector<_T>* minimalElements(vector<_T>* s, vector<int>* v)
    {
        vector<_T>* res = new vector<_T>();
        if (s->size() == 0) { return res; }
        int smallest = (*v)[0];
        res->push_back((*s)[0]);

        for (int i = 1; i < s->size(); i++)
        {
            int next = (*v)[i];
            if (next < smallest)
            {
                smallest = next;
                res->clear();
                res->push_back((*s)[i]);
            }
            else if (smallest == next)
            {
                res->push_back((*s)[i]);
            }
        }
        return res;
    }

    static vector<_T>* minimalElements(vector<_T>* s, vector<long>* v)
    {
        vector<_T>* res = new vector<_T>();
        if (s->size() == 0) { return res; }
        long smallest = (*v)[0];
        res->push_back((*s)[0]);

        for (int i = 1; i < s->size(); i++)
        {
            long next = (*v)[i];
            if (next < smallest)
            {
                smallest = next;
                res->clear();
                res->push_back((*s)[i]);
            }
            else if (smallest == next)
            {
                res->push_back((*s)[i]);
            }
        }
        return res;
    }

    static vector<_T>* minimalElements(vector<_T>* s, vector<string>* v)
    {
        vector<_T>* res = new vector<_T>();
        if (s->size() == 0) { return res; }
        string smallest = (*v)[0];
        res->push_back((*s)[0]);

        for (int i = 1; i < s->size(); i++)
        {
            string next = (*v)[i];
            if (next < smallest)
            {
                smallest = next;
                res->clear();
                res->push_back((*s)[i]);
            }
            else if (smallest == next)
            {
                res->push_back((*s)[i]);
            }
        }
        return res;
    }

    static vector<_T>* minimalElements(vector<_T>* s, vector<double>* v)
    {
        vector<_T>* res = new vector<_T>();
        if (s->size() == 0) { return res; }
        double smallest = (*v)[0];
        res->push_back((*s)[0]);

        for (int i = 1; i < s->size(); i++)
        {
            double next = (*v)[i];
            if (next < smallest)
            {
                smallest = next;
                res->clear();
                res->push_back((*s)[i]);
            }
            else if (smallest == next)
            {
                res->push_back((*s)[i]);
            }
        }
        return res;
    }


    static std::set<_T>* intersectAll(std::set<std::set<_T>*>* se)
    {
        std::set<_T>* res = new std::set<_T>();
        if (se->size() == 0) { return res; }
        auto _pos = se->begin();
        std::set<_T>* frst = *_pos;
        res->insert(frst->begin(), frst->end());
        ++_pos;
        for (; _pos != se->end(); ++_pos)
        {
            res = UmlRsdsLib<_T>::intersection(res, *_pos);
        }
        return res;
    }

    static std::set<_T>* intersectAll(std::set<vector<_T>*>* se)
    {
        std::set<_T>* res = new std::set<_T>();
        if (se->size() == 0) { return res; }
        auto _pos = se->begin();
        vector<_T>* frst = *_pos;
        res->insert(frst->begin(), frst->end());
        ++_pos;
        for (; _pos != se->end(); ++_pos)
        {
            res = UmlRsdsLib<_T>::intersection(res, *_pos);
        }
        return res;
    }

    static std::set<_T>* intersectAll(vector<std::set<_T>*>* se)
    {
        std::set<_T>* res = new std::set<_T>();
        if (se->size() == 0) { return res; }
        std::set<_T>* frst = (*se)[0];
        res->insert(frst->begin(), frst->end());
        for (int i = 1; i < se->size(); ++i)
        {
            res = UmlRsdsLib<_T>::intersection(res, (*se)[i]);
        }
        return res;
    }

    static vector<_T>* intersectAll(vector<vector<_T>*>* se)
    {
        vector<_T>* res = new vector<_T>();
        if (se->size() == 0) { return res; }
        vector<_T>* frst = (*se)[0];
        res->insert(res->end(), frst->begin(), frst->end());
        for (int i = 1; i < se->size(); ++i)
        {
            res = UmlRsdsLib<_T>::intersection(res, (*se)[i]);
        }
        return res;
    }



    static std::set<_T>* unionAll(std::set<set<_T>*>* se)
    {
        std::set<_T>* res = new std::set<_T>();
        if (se->size() == 0) { return res; }

        for (auto _pos = se->begin(); _pos != se->end(); ++_pos)
        {
            res = UmlRsdsLib<_T>::unionSet(res, *_pos);
        }
        return res;
    }

    static std::set<_T>* unionAll(std::set<vector<_T>*>* se)
    {
        std::set<_T>* res = new std::set<_T>();
        if (se->size() == 0) { return res; }
        for (auto _pos = se->begin(); _pos != se->end(); ++_pos)
        {
            res = UmlRsdsLib<_T>::unionSet(res, *_pos);
        }
        return res;
    }

    static std::set<_T>* unionAll(vector<set<_T>*>* se)
    {
        std::set<_T>* res = new std::set<_T>();
        if (se->size() == 0) { return res; }
        for (int i = 0; i < se->size(); ++i)
        {
            res = UmlRsdsLib<_T>::unionSet(res, (*se)[i]);
        }
        return res;
    }

    static std::map<string,_T>* unionAllMap(vector<map<string,_T>*>* se)
    { std::map<string,_T>* res = new std::map<string,_T>();
      if (se->size() == 0) { return res; }
      for (int i = 0; i < se->size(); ++i)
      { res = UmlRsdsLib<_T>::unionMap(res, (*se)[i]); }
      return res;
    }


    static vector<_T>* insertAt(vector<_T>* l, int ind, _T ob)
    {
        vector<_T>* res = new vector<_T>();
        res->insert(res->end(), l->begin(), l->end());
        res->insert(res->begin() + (ind - 1), ob);
        return res;
    }
    static string insertAt(string l, int ind, string ob)
    {
        string res(l);
        res.insert(ind - 1, ob);
        return res;
    }


    static int indexOf(_T x, vector<_T>* a)
    {
        int res = 0;
        for (int i = 0; i < a->size(); i++)
        {
            if (x == (*a)[i])
            {
                return i + 1;
            }
        }
        return res;
    }

    static int indexOf(vector<_T>* a, vector<_T>* b)
    { /* Index of a subsequence a of sequence b in b */

        if (a->size() == 0 || b->size() == 0)
        {
            return 0;
        }
        int i = 0;
        while (i < b->size() && (*b)[i] != (*a)[0])
        {
            i++;
        }

        if (i >= b->size())
        {
            return 0;
        }

        int j = 0;
        while (j < a->size() && i + j < b->size() && (*b)[i + j] == (*a)[j])
        {
            j++;
        }

        if (j >= a->size())
        {
            return i + 1;
        }

        vector<_T>* subr = subrange(b, i + 2, b->size());
        int res1 = indexOf(a, subr);
        if (res1 == 0)
        {
            return 0;
        }
        return res1 + i + 1;
    }

    static int lastIndexOf(vector<_T>* a, vector<_T>* b)
    {
        int res = 0;
        if (a->size() == 0 || b->size() == 0)
        {
            return res;
        }

        vector<_T>* arev = reverse(a);
        vector<_T>* brev = reverse(b);
        int i = indexOf(arev, brev);
        if (i == 0)
        {
            return res;
        }
        res = b->size() - i - a->size() + 2;
        return res;
    }

    static int indexOf(string x, string str)
    {
        int res = str.find(x);
        if (res == string::npos) { return 0; }
        return res + 1;
    }

    static int lastIndexOf(_T x, vector<_T>* a)
    {
        int res = 0;
        for (int i = a->size() - 1; i >= 0; i--)
        {
            if (x == (*a)[i])
            {
                return i + 1;
            }
        }
        return res;
    }

    static int lastIndexOf(string x, string str)
    {
        int res = str.rfind(x);
        if (res == string::npos) { return 0; }
        return res + 1;
    }



    static vector<_T>* setAt(vector<_T>* l, int ind, _T ob)
    {
        vector<_T>* res = new vector<_T>();
        res->insert(res->end(), l->begin(), l->end());
        if (ind >= 1 && ind <= res->size())
        {
            (*res)[(ind - 1)] = ob;
        }
        return res;
    }

    static string setAt(string st, int ind, string ch)
    {
        string res = "";
        if (ind >= 1 && ind <= st.length())
        {
            res = st.substr(0, ind - 1).append(ch).append(st.substr(ind, st.length() - ind));
        }
        else
        {
            res = st;
        }
        return res;
    }

    static vector<_T>* removeAt(vector<_T>* l, int ind)
    {
        if (ind >= 1 && ind <= l->size())
        {
            vector<_T>* res = new vector<_T>();
            res->insert(res->end(), l->begin(), l->begin() + (ind - 1));
            res->insert(res->end(), l->begin() + ind, l->end());
            return res;
        }
        return l;
    }

    static string removeAt(string ss, int ind)
    {
        if (ind >= 1 && ind <= ss.length())
        {
            string res = ss.substr(0, ind - 1);
            res = res + ss.substr(ind);
            return res;
        }
        return ss;
    }

    /* excludingSubrange */ 

  static vector<_T>* excludingSubrange(vector<_T>* l, int ind1, int ind2)
  { vector<_T>* res = new vector<_T>();
    if (ind1 < 1) { ind1 = 1; }
    if (ind2 > l->size()) { ind2 = l->size(); }
    if (ind1 > 1 && ind1 <= l->size())
    {
      res->insert(res->end(), l->begin(), l->begin() + (ind1 - 2));
    }
    if (ind2 >= ind1 && ind2 < l->size())
    { res->insert(res->end(), l->begin() + ind2, l->end());

    }
    return l;
  }

  static string excludingSubrange(string l, int ind1, int ind2)
  { string res = "";
    if (ind1 < 1) { ind1 = 1; }
    if (ind2 > l.size()) { ind2 = l.size(); }
    if (ind1 > 1 && ind1 <= l.size())
    { res = l.substr(0,ind1-1); }
    if (ind2 >= ind1 && ind2 < l.size())
    { res = res + l.substr(ind2); }
    return res;
  }


    static vector<_T>* removeFirst(vector<_T>* sq, _T x)
    {
        vector<_T>* res = new vector<_T>();
        res->insert(res->end(), sq->begin(), sq->end());
        auto iter = find(res->begin(), res->end(), x);
        if (iter != res->end())
        {
            res->erase(iter);
        }
        return res;
    }



    static string toLowerCase(string str)
    {
        string res(str);
        for (int i = 0; i < str.length(); i++)
        {
            res[i] = tolower(str[i]);
        }
        return res;
    }

    static string toUpperCase(string str)
    {
        string res(str);
        for (int i = 0; i < str.length(); i++)
        {
            res[i] = toupper(str[i]);
        }
        return res;
    }

    static bool equalsIgnoreCase(string str1, string str2)
    {
        int len1 = str1.length();
        int len2 = str2.length();
        if (len1 != len2) { return false; }
        for (int i = 0; i < len1; i++)
        {
            if (tolower(str1[i]) == tolower(str2[i]))
            {
            }
            else
            {
                return false;
            }
        }
        return true;
    }



    static bool startsWith(string s1, string s2)
    {
        int l1 = s1.length();
        int l2 = s2.length();
        if (l1 < l2) { return false; }
        if (s1.substr(0, l2) == s2) { return true; }
        return false;
    }

    static bool endsWith(string s1, string s2)
    {
        int l1 = s1.length();
        int l2 = s2.length();
        if (l1 < l2) { return false; }
        if (s1.substr(l1 - l2, l2) == s2) { return true; }
        return false;
    }



    static bool isInteger(string str)
    {
        try { std::stoi(str); return true; }
        catch (exception _e) { return false; }
    }


    static bool isReal(string str)
    {
        try { std::stod(str); return true; }
        catch (exception _e)
        {
            return false;
        }
    }

    static bool toBoolean(string str)
    {
        if ("true" == str || "1" == str)
        {
            return true;
        }
        return false;
    }

    static int toInteger(string str)
    {
        if (str.length() == 0)
        {
            return 0;
        }

        if (str[0] == '0' && str.length() > 1 && str[1] == 'x')
        {
            try {
                int x = std::stoi(str, 0, 16);
                return x;
            }
            catch (exception e) { return 0; }
        }
        else if (str[0] == '0' && str.length() > 1)
        {
            try {
                int y = std::stoi(str, 0, 8);
                return y;
            }
            catch (exception f)
            {
                return 0;
            }
        }
        try {
            int z = std::stoi(str, 0, 10);
            return z;
        }
        catch (exception g) { return 0; }
        return 0;
    }

    static long toLong(string str)
    {
        if (str.length() == 0)
        {
            return 0;
        }

        if (str[0] == '0' && str.length() > 1 && str[1] == 'x')
        {
            try {
                long x = std::stol(str, 0, 16);
                return x;
            }
            catch (exception e) { return 0; }
        }
        else if (str[0] == '0' && str.length() > 1)
        {
            try {
                long y = std::stol(str, 0, 8);
                return y;
            }
            catch (exception f)
            {
                return 0;
            }
        }
        try {
            long z = std::stol(str, 0, 10);
            return z;
        }
        catch (exception g) { return 0; }
        return 0;
    }

    static double toReal(string str)
    {
        if (str.length() == 0)
        {
            return 0.0;
        }

        try {
            double x = std::stod(str);
            return x;
        }
        catch (exception e) { return 0.0; }
    }



    static bool isLong(string str)
    {
        try { std::stol(str); return true; }
        catch (exception _e) { return false; }
    }

    static string before(string s, string sep)
    {
        if (sep.length() == 0) { return s; }
        if (s.find(sep) == string::npos) { return s; }
        return s.substr(0, s.find(sep));
    }


    static string after(string s, string sep)
    {
        int seplength = sep.length();
        if (s.find(sep) == string::npos) { return ""; }
        if (seplength == 0) { return ""; }
        return s.substr(s.find(sep) + seplength, s.length() - (s.find(sep) + seplength));
    }

    static bool hasMatch(string str, string patt)
    {
        std::regex rr(patt);
        return std::regex_search(str, rr);
    }



    static bool isMatch(string str, string patt)
    {
        std::regex rr(patt);
        return std::regex_match(str, rr);
    }



    static vector<string>* allMatches(string s, string patt)
    {
        int slen = s.length();
        vector<string>* res = new vector<string>();
        if (slen == 0)
        {
            return res;
        }
        std::regex patt_regex(patt);
        auto words_begin = std::sregex_iterator(s.begin(), s.end(), patt_regex);
        auto words_end = std::sregex_iterator();

        for (std::sregex_iterator i = words_begin; i != words_end; ++i)
        {
            std::smatch match = *i;
            std::string match_str = match.str();
            if (match_str.length() > 0)
            {
                res->push_back(match_str);
            }
        }
        return res;
    }


    static string firstMatch(string s, string patt)
    {
        int slen = s.length();
        string res = "";
        if (slen == 0)
        {
            return res;
        }
        std::regex patt_regex(patt);
        auto words_begin = std::sregex_iterator(s.begin(), s.end(), patt_regex);
        auto words_end = std::sregex_iterator();

        for (std::sregex_iterator i = words_begin; i != words_end; ++i)
        {
            std::smatch match = *i;
            std::string match_str = match.str();
            if (match_str.length() > 0)
            {
                return match_str;
            }
        }
        return res;
    }


    static string replaceAll(string text, string patt, string rep)
    {
        std::regex patt_re(patt);
        std::string res = std::regex_replace(text, patt_re, rep);
        return res;
    }


    static string replaceFirstMatch(string text, string patt, string rep)
    {
        std::regex patt_re(patt);
        std::regex_constants::match_flag_type fonly =
            std::regex_constants::format_first_only;
        std::string res = std::regex_replace(text, patt_re, rep, fonly);
        return res;
    }


    static vector<string>* split(string s, string patt)
    {
        int slen = s.length();
        vector<string>* res = new vector<string>();
        if (slen == 0)
        {
            res->push_back(s);
            return res;
        }
        std::regex patt_regex(patt);
        auto words_begin = std::sregex_iterator(s.begin(), s.end(), patt_regex);
        auto words_end = std::sregex_iterator();
        int prev = 0;
        for (std::sregex_iterator i = words_begin; i != words_end; ++i)
        {
            std::smatch match = *i;
            int pos = match.position(0);
            int ln = match.length(0);
            if (ln > 0)
            {
                string subst = s.substr(prev, pos - prev + 1);
                res->push_back(subst);
                prev = pos + ln;
            }
        }
        if (prev <= slen)
        {
            string lastst = s.substr(prev, slen - prev + 1);
            res->push_back(lastst);
        }
        return res;
    }


    static string replace(string s1, string s2, string rep)
    {
        int s1len = s1.length();
        int s2len = s2.length();
        int replen = rep.length();
        if (s1len == 0 || s2len == 0 || replen == 0)
        {
            return s1;
        }
        string result = "";
        int prev = 0;
        int m1 = s1.find(s2);
        if (m1 >= 0)
        {
            result = result + s1.substr(prev, m1 - prev) + rep;
            string remainder = s1.substr(m1 + s2len, s1len - (m1 + s2len));
            return result + replace(remainder, s2, rep);
        }
        return s1;
    }


    static string trim(string str)
    {
        int i = str.find_first_not_of("\n\t ");
        int j = str.find_last_not_of("\n\t ");
        if (i > j)
        {
            return "";
        }
        return str.substr(i, j - i + 1);
    }


    static bool includesAllMap(map<string, _T>* sup, map<string, _T>* sub)
    {
        for (auto iter = sub->begin(); iter != sub->end(); ++iter)
        {
            string key = iter->first;
            auto f = sup->find(key);
            if (f != sup->end())
            {
                if (iter->second == f->second) {}
                else
                {
                    return false;
                }
            }
            else
            {
                return false;
            }
        }
        return true;
    }


    static bool excludesAllMap(map<string, _T>* sup, map<string, _T>* sub)
    {
        for (auto iter = sub->begin(); iter != sub->end(); ++iter)
        {
            string key = iter->first;
            auto f = sup->find(key);
            if (f != sup->end())
            {
                if (iter->second == f->second)
                {
                    return false;
                }
            }
        }
        return true;
    }


    static map<string, _T>* includingMap(map<string, _T>* m, string src, _T trg)
    {
        map<string, _T>* copy = new map<string, _T>();
        for (auto iter = m->begin(); iter != m->end(); ++iter)
        {
            string key = iter->first;
            (*copy)[key] = iter->second;
        }
        (*copy)[src] = trg;
        return copy;
    }


    static map<string, _T>* excludeAllMap(map<string, _T>* m1, map<string, _T>* m2)
    {
        map<string, _T>* res = new map<string, _T>();
        for (auto iter = m1->begin(); iter != m1->end(); ++iter)
        {
            string key = iter->first;
            auto f = m2->find(key);
            if (f != m2->end())
            {
                if (iter->second == f->second) {}
                else
                {
                    (*res)[key] = iter->second;
                }
            }
            else
            {
                (*res)[key] = iter->second;
            }
        }
        return res;
    }


    static map<string, _T>* excludingMapKey(map<string, _T>* m, string k)
    { // m - { k |-> m(k) }  
        map<string, _T>* res = new map<string, _T>();
        for (auto iter = m->begin(); iter != m->end(); ++iter)
        {
            string key = iter->first;
            if (key == k) {}
            else
            {
                (*res)[key] = iter->second;
            }
        }
        return res;
    }


    static map<string, _T>* excludingMapValue(map<string, _T>* m, _T v)
    { // m - { k |-> v }  
        map<string, _T>* res = new map<string, _T>();
        for (auto iter = m->begin(); iter != m->end(); ++iter)
        {
            string key = iter->first;
            if (iter->second == v) {}
            else
            {
                (*res)[key] = iter->second;
            }
        }
        return res;
    }


    static map<string, _T>* unionMap(map<string, _T>* m1, map<string, _T>* m2)
    {
        map<string, _T>* res = new map<string, _T>();

        for (auto iter = m1->begin(); iter != m1->end(); ++iter)
        {
            string key = iter->first;
            if (m2->count(key) == 0)
            {
                (*res)[key] = iter->second;
            }
        }

        for (auto iter = m2->begin(); iter != m2->end(); ++iter)
        {
            string key = iter->first;
            (*res)[key] = iter->second;
        }
        return res;
    }


    static map<string, _T>* intersectionMap(map<string, _T>* m1, map<string, _T>* m2)
    {
        map<string, _T>* res = new map<string, _T>();
        for (auto iter = m1->begin(); iter != m1->end(); ++iter)
        {
            string key = iter->first;
            if (m2->count(key) > 0)
            {
                if (m2->at(key) == iter->second)
                {
                    (*res)[key] = iter->second;
                }
            }
        }
        return res;
    }

  static std::map<string, _T>* intersectAllMap(vector<map<string, _T>*>* se)
  { std::map<string, _T>* res = new std::map<string, _T>();
    if (se->size() == 0) { return res; }
    res = (*se)[0]; 
    for (int i = 1; i < se->size(); ++i)
    { res = UmlRsdsLib<_T>::intersectionMap(res, (*se)[i]); }
    return res;
  }


    static std::set<string>* keys(map<string, _T>* s)
    {
        std::set<string>* res = new std::set<string>();

        for (auto iter = s->begin(); iter != s->end(); ++iter)
        {
            string key = iter->first;
            res->insert(key);
        }
        return res;
    }


    static vector<_T>* values(map<string, _T>* s)
    {
        vector<_T>* res = new vector<_T>();

        for (auto iter = s->begin(); iter != s->end(); ++iter)
        {
            _T value = iter->second;
            res->push_back(value);
        }
        return res;
    }

    static bool includesKey(map<string, _T>* m, string k)
    {
      if (m->find(k) == m->end())
      {
        return false;
      }
      return true; 
    }

    static bool excludesKey(map<string, _T>* m, string k)
    {
        if (m->find(k) == m->end())
        {
            return true;
        }
        return false;
    }

    static bool includesValue(map<string, _T>* s, _T val)
    {
        for (auto iter = s->begin(); iter != s->end(); ++iter)
        {
            _T value = iter->second;
            if (val == value)
            {
                return true;
            }
        }

        return false;
    }

    static bool excludesValue(map<string, _T>* s, _T val)
    {
        for (auto iter = s->begin(); iter != s->end(); ++iter)
        {
            _T value = iter->second;
            if (val == value)
            {
                return false;
            }
        }

        return true;
    }

    static map<string, _T>* restrict(map<string, _T>* m1, std::set<string>* ks)
    { map<string, _T>* res = new map<string, _T>();
    for (auto iter = m1->begin(); iter != m1->end(); ++iter)
    {
        string key = iter->first;
        if (ks->find(key) != ks->end())
        {
            (*res)[key] = iter->second;
        }
    }
    return res;
    }

    static map<string, _T>* antirestrict(map<string, _T>* m1, std::set<string>* ks)
    {
        map<string, _T>* res = new map<string, _T>();
        for (auto iter = m1->begin(); iter != m1->end(); ++iter)
        {
            string key = iter->first;
            if (ks->find(key) == ks->end())
            {
                (*res)[key] = iter->second;
            }
        }
        return res;
    }

};


template<class S, class T, class R>
class UmlRsdsOcl {
public:

    static T iterate(vector<S>* _s, T initialValue, std::function<T(S, T)> _f)
    {
        T acc = initialValue;
        for (auto _pos = _s->begin(); _pos != _s->end(); ++_pos)
        {
            S _x = *_pos;
            acc = _f(_x, acc);
        }
        return acc;
    }

    static map<S, T>* includingMap(map<S, T>* m, S src, T trg)
    {
        map<S, T>* copy = new map<S, T>();
        for (auto iter = m->begin(); iter != m->end(); ++iter)
        {
            S key = iter->first;
            (*copy)[key] = iter->second;
        }
        (*copy)[src] = trg;
        return copy;
    }


    static map<S, T>* unionMap(map<S, T>* m1, map<S, T>* m2)
    {
        map<S, T>* res = new map<S, T>();
        for (auto iter = m1->begin(); iter != m1->end(); ++iter)
        {
            S key = iter->first;
            if (m2->count(key) == 0)
            {
                (*res)[key] = iter->second;
            }
        }
        for (auto iter = m2->begin(); iter != m2->end(); ++iter)
        {
            S key = iter->first;
            (*res)[key] = iter->second;
        }
        return res;
    }


    static bool includesAllMap(map<S, T>* sup, map<S, T>* sub)
    {
        for (auto iter = sub->begin(); iter != sub->end(); ++iter)
        {
            S key = iter->first;
            auto f = sup->find(key);
            if (f != sup->end())
            {
                if (iter->second == f->second) {}
                else
                {
                    return false;
                }
            }
            else
            {
                return false;
            }
        }
        return true;
    }


    static bool excludesAllMap(map<S, T>* sup, map<S, T>* sub)
    {
        for (auto iter = sub->begin(); iter != sub->end(); ++iter)
        {
            S key = iter->first;
            auto f = sup->find(key);
            if (f != sup->end())
            {
                if (iter->second == f->second)
                {
                    return false;
                }
            }
        }
        return true;
    }


    static map<S, T>* excludeAllMap(map<S, T>* m1, map<S, T>* m2)
    {
        map<S, T>* res = new map<S, T>();
        for (auto iter = m1->begin(); iter != m1->end(); ++iter)
        {
            S key = iter->first;
            auto f = m2->find(key);
            if (f != m2->end())
            {
                if (iter->second == f->second) {}
                else
                {
                    (*res)[key] = iter->second;
                }
            }
            else
            {
                (*res)[key] = iter->second;
            }
        }
        return res;
    }


    static map<S, T>* excludingMapKey(map<S, T>* m, string k)
    { // m - { k |-> m(k) }  
        map<S, T>* res = new map<S, T>();
        for (auto iter = m->begin(); iter != m->end(); ++iter)
        {
            S key = iter->first;
            if (key == k) {}
            else
            {
                (*res)[key] = iter->second;
            }
        }
        return res;
    }


    static map<S, T>* excludingMapValue(map<S, T>* m, T v)
    { // m - { k |-> v }  
        map<S, T>* res = new map<S, T>();
        for (auto iter = m->begin(); iter != m->end(); ++iter)
        {
            S key = iter->first;
            if (iter->second == v) {}
            else
            {
                (*res)[key] = iter->second;
            }
        }
        return res;
    }




    static map<S, T>* intersectionMap(map<S, T>* m1, map<S, T>* m2)
    {
        map<S, T>* res = new map<S, T>();
        for (auto iter = m1->begin(); iter != m1->end(); ++iter)
        {
            S key = iter->first;
            if (m2->count(key) > 0)
            {
                if (m2->at(key) == iter->second)
                {
                    (*res)[key] = iter->second;
                }
            }
        }
        return res;
    }

    static std::set<S>* keys(map<S, T>* s)
    {
        std::set<S>* res = new std::set<S>();

        for (auto iter = s->begin(); iter != s->end(); ++iter)
        {
            S key = iter->first;
            res->insert(key);
        }
        return res;
    }


    static vector<T>* values(map<S, T>* s)
    {
        vector<T>* res = new vector<T>();

        for (auto iter = s->begin(); iter != s->end(); ++iter)
        {
            T value = iter->second;
            res->push_back(value);
        }
        return res;
    }


    static map<S, T>* restrict(map<S, T>* m1, std::set<S>* ks)
    { map<S, T>* res = new map<S, T>();
    for (auto iter = m1->begin(); iter != m1->end(); ++iter)
    {
        S key = iter->first;
        if (ks->find(key) != ks->end())
        {
            (*res)[key] = iter->second;
        }
    }
    return res;
    }

    static map<S, T>* antirestrict(map<S, T>* m1, std::set<S>* ks)
    {
        map<S, T>* res = new map<S, T>();
        for (auto iter = m1->begin(); iter != m1->end(); ++iter)
        {
            S key = iter->first;
            if (ks->find(key) == ks->end())
            {
                (*res)[key] = iter->second;
            }
        }
        return res;
    }

    static map<S, T>* selectMap(map<S, T>* m1, std::function<bool(T)> f)
    {
        map<S, T>* res = new map<S, T>();
        for (auto iter = m1->begin(); iter != m1->end(); ++iter)
        {
            S key = iter->first;
            T val = iter->second;
            if (f(val))
            {
                (*res)[key] = val;
            }
        }
        return res;
    }

    static map<S, T>* rejectMap(map<S, T>* m1, std::function<bool(T)> f)
    {
        map<S, T>* res = new map<S, T>();
        for (auto iter = m1->begin(); iter != m1->end(); ++iter)
        {
            S key = iter->first;
            T val = iter->second;
            if (f(val)) {}
            else
            {
                (*res)[key] = val;
            }
        }
        return res;
    }

    static map<S, R>* collectMap(map<S, T>* m1, std::function<R(T)> f)
    {
        map<S, R>* res = new map<S, R>();
        for (auto iter = m1->begin(); iter != m1->end(); ++iter)
        {
            S key = iter->first;
            T val = iter->second;

            (*res)[key] = f(val);
        }
        return res;
    }


};




class OclAttribute;
class OclOperation;
class OclType;


class OclAttribute
{
private:
    string name;
    OclType* type;

public:
    OclAttribute(OclType* type_x)
    {
        name = "";
        type = type_x;
    }

    OclAttribute() { }


    friend ostream& operator<<(ostream& s, OclAttribute& x)
    {
        return s << "(OclAttribute) " << x.getName() << endl;
    }


    void setname(string name_x) { name = name_x; }

    void settype(OclType* typexx) { type = typexx; }

    string getName() { return name; }

    OclType* getType() { return type; }
};

class OclOperation
{
private:
    string name;
    OclType* type;
    vector<OclAttribute*>* parameters;

public:
    OclOperation(OclType* type_x)
    {
        name = "";
        type = type_x;
        parameters = new vector<OclAttribute*>();
    }

    OclOperation() { }


    friend ostream& operator<<(ostream& s, OclOperation& x)
    {
        return s << "(OclOperation) " << x.getname() << endl;
    }

    void setname(string name_x) { name = name_x; }

    void settype(OclType* typexx) { type = typexx; }

    void setparameters(vector<OclAttribute*>* parametersxx)
    {
        parameters->clear();
        parameters->insert(parameters->end(), parametersxx->begin(), parametersxx->end());
    }

    void setparameters(int ind_x, OclAttribute* parametersxx)
    {
        if (ind_x >= 0 && ind_x < parameters->size()) { (*parameters)[ind_x] = parametersxx; }
    }

    void addparameters(OclAttribute* parametersxx)
    {
        parameters->push_back(parametersxx);
    }

    string getname() { return name; }

    OclType* gettype() { return type; }

    vector<OclAttribute*>* getparameters() { return parameters; }

    string getName() { return name; }

    OclType* getType() { return type; }

    OclType* getReturnType() { return type; }

    vector<OclAttribute*>* getParameters()
    {
        return parameters;
    }

};


class OclType
{
private:
    string name;
    string oclname;
    vector<OclAttribute*>* attributes;
    vector<OclOperation*>* operations;
    vector<OclOperation*>* constructors;
    vector<OclType*>* innerClasses;
    vector<OclType*>* componentType;
    vector<OclType*>* superclasses;
    vector<OclType*>* subclasses;
    bool isinterface;
    void* (*creator)();
    static map<string, OclType*>* ocltypenameindex;

public:
    OclType()
    {
        oclname = "";
        name = "";
        attributes = new vector<OclAttribute*>();
        operations = new vector<OclOperation*>();
        constructors = new vector<OclOperation*>();
        innerClasses = new vector<OclType*>();
        componentType = new vector<OclType*>();
        superclasses = new vector<OclType*>();
        subclasses = new vector<OclType*>();
        isinterface = false;
    }

    static OclType* createOclType(string nme)
    {
        OclType* res = new OclType();
        res->oclname = nme;
        (*ocltypenameindex)[nme] = res;
        return res;
    }

    static OclType* getOclTypeByPK(string namex)
    {
        if (ocltypenameindex->find(namex) == ocltypenameindex->end())
        {
            return 0;
        }
        return ocltypenameindex->at(namex);
    }

    friend ostream& operator<<(ostream& s, OclType& x)
    {
        return s << "(OclType) " << x.getname() << endl;
    }

    void setname(string name_x)
    {
        name = name_x;
        (*OclType::ocltypenameindex)[name_x] = this;
    }

    void setoclname(string name_x)
    {
        oclname = name_x;
    }

    void setcreator(void* (*f)())
    {
        creator = f;
    }

    void setattributes(vector<OclAttribute*>* attributesxx)
    {
        attributes->clear();
        attributes->insert(attributes->end(), attributesxx->begin(), attributesxx->end());
    }

    void setattributes(int ind_x, OclAttribute* attributesxx)
    {
        if (ind_x >= 0 && ind_x < attributes->size())
        {
            (*attributes)[ind_x] = attributesxx;
        }
    }

    void addattributes(OclAttribute* attributesxx)
    {
        attributes->push_back(attributesxx);
    }

    void setoperations(vector<OclOperation*>* operationsxx)
    {
        operations->clear();
        operations->insert(operations->end(), operationsxx->begin(), operationsxx->end());
    }

    void setoperations(int ind_x, OclOperation* operationsxx)
    {
        if (ind_x >= 0 && ind_x < operations->size())
        {
            (*operations)[ind_x] = operationsxx;
        }
    }

    void addoperations(OclOperation* operationsxx)
    {
        operations->push_back(operationsxx);
    }

    void setsuperclasses(vector<OclType*>* superclassesxx)
    {
        superclasses->clear();
        superclasses->insert(superclasses->end(), superclassesxx->begin(), superclassesxx->end());
    }

    void setsuperclasses(int ind_x, OclType* superclassesxx)
    {
        if (ind_x >= 0 && ind_x < superclasses->size())
        {
            (*superclasses)[ind_x] = superclassesxx;
        }
    }

    void setisinterface(bool intf)
    {
        isinterface = intf;
    }

    void addsuperclasses(OclType* superclassesxx)
    {
        superclasses->push_back(superclassesxx);
        superclassesxx->subclasses->push_back(this);
    }

    string getname() { return name; }

    vector<OclAttribute*>* getattributes()
    {
        return attributes;
    }

    vector<OclOperation*>* getoperations()
    {
        return operations;
    }

    string getName()
    {
        return oclname;
    }

    // vector<OclType*>* getClasses();

    // vector<OclType*>* getDeclaredClasses();

    // OclType* getComponentType();

    vector<OclAttribute*>* getFields()
    {
        return attributes;
    }

    OclAttribute* getDeclaredField(string s)
    {
        return getField(s);
    }

    OclAttribute* getField(string s)
    {
        for (int i = 0; i < attributes->size(); i++)
        {
            OclAttribute* att = attributes->at(i);
            if (att->getName() == s)
            {
                return att;
            }
        }
        return NULL;
    }

    vector<OclOperation*>* getConstructors()
    {
        return constructors;
    }

    OclType* getSuperclass()
    {
        if (superclasses->size() > 0)
        {
            return superclasses->at(0);
        }
        return NULL;
    }


    vector<OclAttribute*>* getDeclaredFields()
    {
        return attributes;
    }

    vector<OclOperation*>* getMethods()
    {
        return operations;
    }

    template<class X>
    static bool hasAttribute(X obj, string nme)
    {   // cout << "Type of obj is: " << typeid(obj).name() << endl;

        OclType* typ = getOclTypeByPK(typeid(obj).name());
        if (typ == NULL)
        {
            return false;
        }

        OclAttribute* fld = typ->getField(nme);
        if (fld == NULL)
        {
            return false;
        }
        return true;
    }

    /* getAttributeValue and setAttributeValue and removeAttribute are unavailable */

    bool isArray()
    {
        return "Sequence" == oclname;
    }

    bool isPrimitive()
    {
        return "int" == oclname || "double" == oclname || "long" == oclname ||
            "boolean" == oclname;
    }

    bool isInterface()
    {
        return isinterface;
    }

    bool isAssignableFrom(OclType* c)
    {
        if (c->oclname == oclname)
        {
            return true;
        }

        for (int i = 0; i < subclasses->size(); i++)
        {
            OclType* sub = subclasses->at(i);
            if (sub->isAssignableFrom(c))
            {
                return true;
            }
        }

        return false;
    }

    template<class X>
    bool isInstance(X obj)
    {
        OclType* typ = getOclTypeByPK(typeid(obj).name());
        if (typ == NULL)
        {
            return false;
        }

        return isAssignableFrom(typ);
    }

    void* newInstance()
    {
        if (creator == NULL)
        {
            return NULL;
        }
        return creator();
    }
};




class io_exception : exception
{
public:
    io_exception() : message("IO exception") { }

    io_exception* copyio_exception(io_exception* self)
    {
        io_exception* ex = new io_exception();
        ex->message = self->message;
        return ex;
    }

    const char* what() const { return message; }

    string getMessage() { return string(message); }

    void printStackTrace()
    {
        cout << what() << endl;
    }

    exception* getCause()
    {
        return this;
    }

    static io_exception* newIOException(string m)
    {
        io_exception* res = new io_exception();
        res->message = m.c_str();
        return res;
    }
private:
    const char* message;
};

class null_access_exception : exception
{
public:
    null_access_exception() : message("Null access exception") { }

    null_access_exception* copynull_access_exception(null_access_exception* self)
    {
        null_access_exception* ex = new null_access_exception();
        ex->message = self->message;
        return ex;
    }

    const char* what() const { return message; }

    string getMessage() { return string(message); }

    void printStackTrace()
    {
        cout << what() << endl;
    }

    exception* getCause()
    {
        return this;
    }

    static null_access_exception* newNullAccessException(string m)
    {
        null_access_exception* res = new null_access_exception();
        res->message = m.c_str();
        return res;
    }
private:
    const char* message;
};

class assertion_exception : exception
{
public:
    assertion_exception() : message("Assertion exception") { }

    assertion_exception* copyassertion_exception(assertion_exception* self)
    {
        assertion_exception* ex = new assertion_exception();
        ex->message = self->message;
        return ex;
    }

    const char* what() const { return message; }

    string getMessage() { return string(message); }

    void printStackTrace()
    {
        cout << what() << endl;
    }

    exception* getCause()
    {
        return this;
    }

    static assertion_exception* newAssertionException(string m)
    {
        assertion_exception* res = new assertion_exception();
        res->message = m.c_str();
        return res;
    }
private:
    const char* message;
};

class accessing_exception : exception
{
public:
    accessing_exception() : message("Accessing exception") { }

    accessing_exception* copyaccessing_exception(accessing_exception* self)
    {
        accessing_exception* ex = new accessing_exception();
        ex->message = self->message;
        return ex;
    }

    const char* what() const { return message; }

    string getMessage() { return string(message); }

    void printStackTrace()
    {
        cout << what() << endl;
    }

    exception* getCause()
    {
        return this;
    }

    static accessing_exception* newAccessingException(string m)
    {
        accessing_exception* res = new accessing_exception();
        res->message = m.c_str();
        return res;
    }
private:
    const char* message;
};


class MathLibrary
{

public:
    MathLibrary()
    {

    }



    friend ostream& operator<<(ostream& s, MathLibrary& x)
    {
        return s << "(MathLibrary)  " << endl;
    }

    virtual string toString() {
        return "(MathLibrary) ";
    }



    static double asinh(double x);

    static int modInverse(int n, int p);

    static double asinh_mutant_0(double x);

    static double asinh_mutant_1(double x);

    static double asinh_mutant_2(double x);

    static double asinh_mutant_3(double x);

    static int modInverse_mutant_0(int n, int p);

    static int modInverse_mutant_1(int n, int p);

    static int modInverse_mutant_2(int n, int p);


    ~MathLibrary() {
    }

};

class MathLib
{
private:
    static int ix;
    static int iy;
    static int iz;
    static vector<string>* hexdigit;

public:
    MathLib() {
        ix = 0;
        iy = 0;
        iz = 0;
        hexdigit = (new vector<string>());
    }


      /* static int bitwiseRotateRight(int x, int n)
        {
            return std::rotr(x, n);
        } */ 
    
    static void setix(int ix_x) { ix = ix_x; }

    static void setiy(int iy_x) { iy = iy_x; }

    static void setiz(int iz_x) { iz = iz_x; }

    static void sethexdigit(vector<string>* hexdigit_x)
    {
        hexdigit = hexdigit_x;
    }

    static void sethexdigit(int _ind, string hexdigit_x)
    {
        (*hexdigit)[_ind] = hexdigit_x;
    }

    static void addhexdigit(string hexdigit_x)
    {
        hexdigit->push_back(hexdigit_x);
    }

    static int getix() { return ix; }

    static int getiy() { return iy; }

    static int getiz() { return iz; }

    static vector<string>* gethexdigit() { return hexdigit; }

    static double pi();

    static double e();

    static void setSeeds(int x, int y, int z);

    static double nrandom();

    static double random();

    static bool nextBoolean();

    static long combinatorial(int n, int m);

    static long factorial(int x);

    static double asinh(double x);

    static double acosh(double x);

    static double atanh(double x);

    static string decimal2bits(long x);

    static string decimal2binary(long x);

    static string decimal2oct(long x);

    static string decimal2octal(long x);

    static string decimal2hx(long x);

    static string decimal2hex(long x);

    static int bitwiseAnd(int x, int y);

    static int bitwiseOr(int x, int y);

    static int bitwiseXor(int x, int y);

    static int bitwiseNot(int x);

    static long bitwiseAnd(long x, long y);

    static long bitwiseOr(long x, long y);

    static long bitwiseXor(long x, long y);

    static long bitwiseNot(long x);

    static int bitwiseRotateLeft(int x, int n); 

    static int bitwiseRotateRight(int x, int n);

    static vector<bool>* toBitSequence(long x);

    static long modInverse(long n, long p);

    static long modPow(long n, long m, long p);

    static inline long doubleToLongBits(double x) {
        uint64_t bits;
        memcpy(&bits, &x, sizeof bits);
        return long(bits);
    }

    static inline double longBitsToDouble(long x) {
        uint64_t bits;
        memcpy(&bits, &x, sizeof bits);
        return double(bits);
    }

    static double discountDiscrete(double amount, double rate, double time)
    {
        double result = 0;
        result = 0.0;
        if ((rate <= -1 || time < 0))
        {
            return result;
        }

        result = amount / pow((1 + rate), time);
        return result;
    }

    static double bisectionDiscrete(double r, double rl, double ru,
        vector<double>* values); 

    static double netPresentValueDiscrete(double rate, vector<double>* values)
    {
        double result = 0;
        result = 0.0;
        if ((rate <= -1))
        {
            return result;
        }

        int upper = values->size();
        int i = 0;
        for (; i < upper; i++)
        {
            result = result + discountDiscrete(values->at(i), rate, i);
        }
        return result;
    }


    static double irrDiscrete(vector<double>* values)
    {
        double res = bisectionDiscrete(0.1, -0.5, 1.0, values);
        return res;
    }

    static double roundN(double x, int n)
    {
        if (n < 0)
        {
            return round(x);
        }
        double y = x * pow(10, n);
        return round(y) / pow(10, n);
    }

    static double truncateN(double x, int n)
    {
        if (n < 0)
        {
            return (int)x;
        }
        double y = x * pow(10, n);
        return ((int)y) / pow(10, n);
    }

    static double toFixedPoint(double x, int m, int n)
    {
        if (m < 0 || n < 0)
        {
            return x;
        }
        int y = (int)(x * pow(10, n));
        int z = y % ((int) pow(10, m + n));
        return z / pow(10.0, n);
    }

    static double toFixedPointRound(double x, int m, int n)
    {
        if (m < 0 || n < 0)
        {
            return x;
        }
        int y = (int) round(x * pow(10, n));
        int z = y % ((int) pow(10, m + n));
        return z / pow(10.0, n);
    }

    static bool isIntegerOverflow(double x, int m)
    {
        bool result = false;
        int y = ((int)x);
        if (y > 0)
        {
            result = ((int) log10(y)) + 1 > m;
        }
        else
        {
            if (y < 0)
            {
                result = (((int) log10(-y)) + 1 > m);
            }
            else
            { result = (m < 1);  }
        }
        return result;
    }

    static double mean(vector<double>* sq)
    {
        double result = 0.0;
        if (sq->size() <= 0) { return result; }
        result = UmlRsdsLib<double>::sum(sq) / sq->size();
        return result;
    }

    static double median(vector<double>* sq)
    {
        double result = 0.0;
        if (sq->size() <= 0) { return result; }
        vector<double>* s1 = UmlRsdsLib<double>::sort(sq);
        int sze = sq->size();
        if (sze % 2 == 1)
        {
            result = ((double)(s1)->at((1 + sze) / 2 - 1));
        }
        else
            if (sze % 2 == 0)
            {
                result = (((double)(s1)->at(sze / 2 - 1)) + ((double)(s1)->at(1 + (sze / 2) - 1))) / 2.0;
            }
        return result;
    }

    static double variance(vector<double>* sq)
    {
        double result = 0.0;
        if (sq->size() <= 0) { return result; }
        double m = MathLib::mean(sq);
        double sumsq = 0.0; 
        for (int _icollect = 0; _icollect < sq->size(); _icollect++)
        {
          double x = (*sq)[_icollect];
          sumsq = sumsq + ((x - m)) * ((x - m));
        }
        result = sumsq / sq->size();
        return result;
    }


    static double standardDeviation(vector<double>* sq)
    {
        double result = 0.0;
        if (sq->size() <= 0) { return result; }
        double m = MathLib::variance(sq);
        result = sqrt(m);
        return result;
    }


    static int lcm(int x, int y)
    {
        int result = 0;
        if (x == 0 && y == 0)
        {
            result = 0;
        }
        else
        {
           result = (x * y) / UmlRsdsLib<long>::gcd(x, y);
        }
        return result;
    }


    static double bisectionAsc(double r, double rl, double ru, std::function<double(double)> f, double tol)
    {
        double result = 0.0;
        double v = f(r);
        
        if (v < tol && v > -tol)
        {
            return r;
        }
        else
        {
            if (v > 0)
            {
                return MathLib::bisectionAsc((rl + r) / 2.0, rl, r, f, tol);
            }
            else
                if (v < 0)
                {
                    return MathLib::bisectionAsc((r + ru) / 2.0, r, ru, f, tol);
                }
        }
        return result;
    }

    static vector<double>* rowMult(vector<double>* s, vector<vector<double>*>* m)
    {
        vector<double>* result = new vector<double>();
        vector<double>* _results_5 = new vector<double>();
        for (int _icollect = 0; _icollect < s->size(); _icollect++)
        {
            int i = _icollect + 1; 
            vector<double>* _results_4 = new vector<double>();
            for (int _icollect = 0; _icollect < m->size(); _icollect++)
            {
                int k = _icollect + 1;
                _results_4->push_back((s)->at(k - 1) * ((double)((m)->at(k - 1))->at(i - 1)));
            }
            _results_5->push_back(UmlRsdsLib<double>::sum(_results_4));
        }
        result = _results_5;
        return result;
    }

    static vector<vector<double>*>* matrixMultiplication(vector<vector<double>*>* m1, vector<vector<double>*>* m2)
    {
        vector<vector<double>*>* result = new vector<vector<double>*>();
        for (int _icollect = 0; _icollect < m1->size(); _icollect++)
        {
            vector<double>* row = (*m1)[_icollect];
            result->push_back(MathLib::rowMult(row, m2));
        }
        return result;
    }


    ~MathLib() {
    }

};


template<class T>
class OclIteratorResult
{
    bool done;
    T value;

    static OclIteratorResult* newOclIteratorResult(T v)
    {
        OclIteratorResult* res = new OclIteratorResult();
        res->value = v;
        res->done = false;
        if (v == NULL) { res->done = true; }
        return res;
    }
};

template<class T>
class OclIterator
{
private:
    int position;
    int markedPosition;
    vector<T>* elements;
    function<T(int)> generatorFunction;
    vector<string>* columnNames;

public:
    OclIterator()
    {
        position = 0;
        markedPosition = 0;
        elements = (new vector<T>());
        columnNames = (new vector<string>());
        generatorFunction = NULL;
    }

    void setColumnNames(vector<string>* cols)
    {
        columnNames = cols;
    }

    void setPosition(int position_x)
    {
        position = position_x;
    }

    void markPosition()
    {
        markedPosition = position;
    }

    void moveToMarkedPosition()
    {
        position = markedPosition;
    }

    void setelements(vector<T>* elements_x)
    {
        elements = elements_x;
    }

    void setelements(int _ind, void* elements_x)
    {
        (*elements)[_ind] = elements_x;
    }

    void addelements(T elements_x)
    {
        elements->push_back(elements_x);
    }

    int getPosition()
    {
        return position;
    }

    vector<T>* getelements()
    {
        return elements;
    }

    bool hasNext();

    bool hasPrevious();

    bool isAfterLast();

    bool isBeforeFirst();

    int nextIndex();

    int previousIndex();

    void moveForward();

    void moveBackward();

    void moveTo(int i);

    void moveToStart();

    void moveToEnd();

    void moveToFirst();

    void moveToLast();

    void movePosition(int i);

    static OclIterator* newOclIterator_Sequence(vector<T>* sq);

    static OclIterator* newOclIterator_Set(set<T>* st);

    static OclIterator* newOclIterator_String(string st);

    static OclIterator* newOclIterator_String_String(string st, string sep);

    static OclIterator* newOclIterator_Function(function<T(int)> f);

    OclIterator<T> trySplit();

    T getCurrent();

    void set(T x);

    void insert(T x);

    void remove();

    T next();

    OclIteratorResult<T> nextResult();

    bool tryAdvance(function<void* (void*)> f);

    void forEachRemaining(function<void* (void*)> f); 

    T previous();

    T at(int i);

    int length();

    void close();

    int getColumnCount();

    string getColumnName(int i);

    void* getCurrentFieldByIndex(int i);

    void setCurrentFieldByIndex(int i, void* v);

    ~OclIterator() {
    }
};

template<class T>
class OclIteratorOptimised
{
private:
    int position;
    int markedPosition;
    T* elements;
    
public:
    OclIteratorOptimised()
    {
        position = 0;
        markedPosition = 0;
        elements = new T[];
    }

    void setPosition(int position_x)
    {
        position = position_x;
    }

    void markPosition()
    {
        markedPosition = position;
    }

    void moveToMarkedPosition()
    {
        position = markedPosition;
    }

    void setelements(T* elements_x)
    {
        this->elements = elements_x;
    }

    void setelements(int _ind, void* elements_x)
    {
        this->elements[_ind] = elements_x;
    }

    int getPosition()
    {
        return position;
    }

    T* getelements()
    {
        return elements;
    }

    bool hasNext();

    bool hasPrevious();

    bool isAfterLast();

    bool isBeforeFirst();

    int nextIndex();

    int previousIndex();

    void moveForward();

    void moveBackward();

    void moveTo(int i);

    void moveToStart();

    void moveToEnd();

    void moveToFirst();

    void moveToLast();

    void movePosition(int i);

    static OclIteratorOptimised* newOclIterator_Sequence(vector<T>* sq);

    T getCurrent();

    void set(T x);

    void remove();

    T next();

    T previous();

    T at(int i);

    int length();

    void close();

    ~OclIteratorOptimised() {
    }
};

class OclDate {
private:
    long long time;
    long long systemTime;
    struct tm* actualDate;
    int year; 
    int month; 
    int day; 
    int weekday; 
    int hour; 
    int minute; 
    int second; 

public:
    OclDate()
    {
        time = 0;
        systemTime = 0;
        actualDate = NULL;
        year = 0; 
        month = 0; 
        day = 0; 
        weekday = 0; 
        hour = 0; 
        minute = 0; 
        second = 0; 
    }

    static long long getCurrentTime()
    {
        return (long long) 1000 * std::time(NULL);
    }

    static struct tm* getDate(long long t)
    {
        time_t tx = (time_t) t / 1000;
        struct tm* res = localtime(&tx);
        // res->tm_year = res->tm_year + 1900; 
        return res;
    }

    static OclDate* newOclDate_Time(long long t)
    {
        OclDate* res = new OclDate();
        res->time = t;
        res->systemTime = getCurrentTime();
        res->actualDate = getDate(t);
        res->year = res->actualDate->tm_year + 1900; 
        res->month = res->actualDate->tm_mon + 1;
        res->day = res->actualDate->tm_mday;
        res->weekday = res->actualDate->tm_wday;
        res->hour = res->actualDate->tm_hour;
        res->minute = res->actualDate->tm_min;
        res->second = res->actualDate->tm_sec;

        return res;
    }

    static OclDate* newOclDate()
    {
        OclDate* res = new OclDate();
        res->systemTime = getCurrentTime();
        res->time = res->systemTime;
        res->actualDate = getDate(res->time);
        res->year = res->actualDate->tm_year + 1900;
        res->month = res->actualDate->tm_mon + 1;
        res->day = res->actualDate->tm_mday;
        res->weekday = res->actualDate->tm_wday;
        res->hour = res->actualDate->tm_hour;
        res->minute = res->actualDate->tm_min;
        res->second = res->actualDate->tm_sec;
        return res;
    }

    static OclDate* newOclDate_YMD(int y, int m, int d)
    {
        OclDate* res = new OclDate();
        res->systemTime = getCurrentTime();
        res->year = y;
        res->month = m;
        res->day = m;

        res->actualDate = getDate(0);
        res->actualDate->tm_year = y - 1900;
        res->actualDate->tm_mon = m - 1;
        res->actualDate->tm_mday = d;
        res->time = mktime(res->actualDate);
        return res;
    }

    static OclDate* newOclDate_YMDHMS(int y, int m, int d, int h, int mn, int s)
    {
        OclDate* res = new OclDate();
        res->systemTime = getCurrentTime();

        res->year = y; 
        res->month = m; 
        res->day = m;
        res->hour = h;
        res->minute = mn; 
        res->second = s; 

        res->actualDate = getDate(0);
        res->actualDate->tm_year = y - 1900;
        res->actualDate->tm_mon = m - 1;
        res->actualDate->tm_mday = d;
        res->actualDate->tm_hour = h;
        res->actualDate->tm_min = mn;
        res->actualDate->tm_sec = s;
        res->time = mktime(res->actualDate);
        return res;
    }

    static OclDate* newOclDate_String(string s)
    {
        vector<string>* items = UmlRsdsLib<string>::allMatches(s, "[0-9]+");

        OclDate* dd = OclDate::newOclDate_Time(0);
        if (items->size() >= 3)
        {
            dd->year = std::stoi(items->at(0), 0, 10);
            dd->month = std::stoi(items->at(1), 0, 10);
            dd->day = std::stoi(items->at(2), 0, 10);
        }
        if (items->size() >= 6)
        {
            dd->hour = std::stoi(items->at(3), 0, 10);
            dd->minute = std::stoi(items->at(4), 0, 10);
            dd->second = std::stoi(items->at(5), 0, 10);
        }
        return OclDate::newOclDate_YMDHMS(dd->getYear(), dd->getMonth(), dd->getDay(), 
                                          dd->getHour(), dd->getMinute(), dd->getSecond());
    } 

    bool operator<(const OclDate* right) const 
    {
        return time < right->time; 
    }

    bool operator<=(const OclDate* right) const
    {
        return time <= right->time;
    }

    bool operator>(const OclDate* right) const
    {
        return time > right->time;
    }

    bool operator>=(const OclDate* right) const
    {
        return time >= right->time;
    }

    bool operator!=(const OclDate* right) const
    {
        return time != right->time;
    }

    bool operator==(const OclDate* right) const
    {
        return time == right->time;
    }

    void setTime(long long t)
    {
        time = t;
    }

    long long getTime()
    {
        return time;
    }

    long long getSystemTime()
    {
        systemTime = getCurrentTime();
        return systemTime;
    }

    bool dateBefore(OclDate* d)
    {
        if (time < d->time)
        {
            return true;
        }
        return false;
    }

    bool dateAfter(OclDate* d)
    {
        if (time > d->time)
        {
            return true;
        }
        return false;
    }

    int getYear()
    {
        return year;
    }

    int getMonth()
    {
        return month;
    }

    int getDate()
    {
        return day;
    }

    int getDay()
    {
        return weekday;
    }

    int getHour()
    {
        return hour;
    }

    int getMinute()
    {
        return minute;
    }

    int getMinutes()
    {
        return minute;
    }

    int getSecond()
    {
        return second;
    }

    int getSeconds()
    {
        return second;
    }

    OclDate* addYears(int y)
    {
        long long newtime = time + (long long) y * (30758400000L);
        return newOclDate_Time(newtime);
    }

    OclDate* addMonths(int y)
    {
        long long newtime = time + (long long) y * (2563200000L);
        return newOclDate_Time(newtime);
    }

    OclDate* addDays(int y)
    {
        long long newtime = time + (long long) y * (86400000L);
        return newOclDate_Time(newtime);
    }

    OclDate* addHours(int y)
    {
        long long newtime = time + (long long) y * (3600000L);
        return newOclDate_Time(newtime);
    }

    OclDate* addMinutes(int y)
    {
        long long newtime = time + (long long) y * (60000);
        return newOclDate_Time(newtime);
    }

    OclDate* addSeconds(int y)
    {
        long long newtime = time + (long long) y * (1000);
        return newOclDate_Time(newtime);
    }

    long yearDifference(OclDate* d)
    {
        long result = (time - d->time) / 31536000000L;
        return result;
    }

    long monthDifference(OclDate* d)
    {
        long result = (time - d->time) / 2563200000L;
        return result;
    }

    long dayDifference(OclDate* d)
    {
        long result = (time - d->time) / 86400000L;
        return result;
    }

    long hourDifference(OclDate* d)
    {
        long result = (time - d->time) / 3600000L;
        return result;
    }

    long minuteDifference(OclDate* d)
    {
        long result = (time - d->time) / 60000;
        return result;
    }

    long secondDifference(OclDate* d)
    {
        long result = (time - d->time) / 1000;
        return result;
    }

    friend ostream& operator<<(ostream& s, OclDate* d)
    {
        return s << d->getYear() << "/" << d->getMonth() << "/" << d->getDay() << " " <<
            d->getHour() << ":" << d->getMinute() << ":" << d->getSecond();
    }

    virtual string toString() {
        return "" + std::to_string(this->getYear()) + "/" + std::to_string(this->getMonth()) + "/" + 
            std::to_string(this->getDay()) + " " + std::to_string(this->getHour()) + ":" + 
            std::to_string(this->getMinute()) + ":" + std::to_string(this->getSecond());
    }
};

class StringLib
{

public:
    StringLib()
    {

    }



    friend ostream& operator<<(ostream& s, StringLib& x)
    {
        return s << "(StringLib)  " << endl;
    }

    virtual string toString() {
        return "(StringLib) ";
    }


    static string leftTrim(string s)
    {
        string result = string("");
        result = UmlRsdsLib<string>::subrange(s, UmlRsdsLib<void*>::indexOf(UmlRsdsLib<void*>::trim(s), s), (s).length());
        return result;
    }


    static string rightTrim(string s)
    {
        string result = string("");
        string trimmeds = UmlRsdsLib<string>::trim(s);
        result = string(UmlRsdsLib<string>::before(s,trimmeds).append(trimmeds));
        return result;
    }

    static string nCopies(int n, string c)
    { string res = "";
   
      for (int i = 0; i < n; i++)
      {
        res = res + c;
      }

      return res;
    }

    static string padLeftWithInto(string s, string c, int n)
    {
        string result = string("");
        result = string(StringLib::nCopies(n - s.length(), c)).append(s);
        return result;
    }

    static string leftAlignInto(string s, int n)
    {
        int slen = s.length(); 
        string result = string("");
        if (n <= slen)
        {
            result = UmlRsdsLib<string>::subrange(s, 1, n);
        }
        else
        {
            result = string(s).append(StringLib::nCopies(n - slen, " "));
        }
        return result;
    }

    static string rightAlignInto(string s, int n)
    {
        int slen = s.length();
        string result = string("");
        if (n <= slen)
        {
            result = UmlRsdsLib<string>::subrange(s, 1, n);
        }
        else
        { result = string(StringLib::nCopies(n - slen, " ")).append(s); }
        return result;
    }


    static string format(string fmt, vector<void*>* sq);

    static vector<void*>* scan(string s, string fmt);


    ~StringLib() {
    }

};


template< class K, class T>
class OrderedMap
{
private:
    vector<K>* elements;
    map<string, T>* items;

public:
    OrderedMap()
    {
        elements = (new vector<void*>());
        items = (new map<string, void*>());

    }



    friend ostream& operator<<(ostream& s, OrderedMap& x)
    {
        return s << "(OrderedMap)  " << "elements = " << x.getelements() << "," << "items = " << x.getitems() << endl;
    }

    virtual string toString() {
        return "(OrderedMap) " + UmlRsdsLib<K>::collectionToString(getelements()) + ", " + UmlRsdsLib<T>::collectionToString(getitems());
    }



    void setelements(vector<K>* elements_x) { elements = elements_x; }


    void setelements(int _ind, K elements_x) { (*elements)[_ind] = elements_x; }


    void addelements(K elements_x)
    {
        elements->push_back(elements_x);
    }

    void removeelements(K elements_x)
    {
        auto _pos = find(elements->begin(), elements->end(), elements_x);
        while (_pos != elements->end())
        {
            elements->erase(_pos);
            _pos = find(elements->begin(), elements->end(), elements_x);
        }

    }



    void setitems(map<string, T>* items_x) { items = items_x; }


    vector<K>* getelements() { return elements; }





    map<string, T>* getitems() { return items; }





    T getByIndex(int i);

    T getByKey(K k);

    void add(K k, T t);

    void remove(int i);


    ~OrderedMap() {
    }

};


class MutationTest
{
public:
    static void asinh_mutation_tests_0(MathLibrary* _self, int _counts[], int _totals[])
    {
        double x = 0.0;

        try {
            double asinh_result = MathLibrary::asinh(x);
            cout << "Test " << 0 << " of asinh on " << _self << " result = " << asinh_result << endl;

            try {
                double asinh_mutant_0_result = MathLibrary::asinh_mutant_0(x);
                if (asinh_result == asinh_mutant_0_result) { _totals[0]++; }
                else
                {
                    cout << "Test " << 0 << " of " << 5 << " detects asinh mutant " << 0 << " of " << 4 << endl;
                    _counts[0]++;
                    _totals[0]++;
                }
            }
            catch (...) {}
            try {
                double asinh_mutant_1_result = MathLibrary::asinh_mutant_1(x);
                if (asinh_result == asinh_mutant_1_result) { _totals[0]++; }
                else
                {
                    cout << "Test " << 0 << " of " << 5 << " detects asinh mutant " << 1 << " of " << 4 << endl;
                    _counts[0]++;
                    _totals[0]++;
                }
            }
            catch (...) {}
            try {
                double asinh_mutant_2_result = MathLibrary::asinh_mutant_2(x);
                if (asinh_result == asinh_mutant_2_result) { _totals[0]++; }
                else
                {
                    cout << "Test " << 0 << " of " << 5 << " detects asinh mutant " << 2 << " of " << 4 << endl;
                    _counts[0]++;
                    _totals[0]++;
                }
            }
            catch (...) {}
            try {
                double asinh_mutant_3_result = MathLibrary::asinh_mutant_3(x);
                if (asinh_result == asinh_mutant_3_result) { _totals[0]++; }
                else
                {
                    cout << "Test " << 0 << " of " << 5 << " detects asinh mutant " << 3 << " of " << 4 << endl;
                    _counts[0]++;
                    _totals[0]++;
                }
            }
            catch (...) {}
        }
        catch (...) {}
    }



    static void asinh_mutation_tests_1(MathLibrary* _self, int _counts[], int _totals[])
    {
        double x = -1.0;

        try {
            double asinh_result = MathLibrary::asinh(x);
            cout << "Test " << 1 << " of asinh on " << _self << " result = " << asinh_result << endl;

            try {
                double asinh_mutant_0_result = MathLibrary::asinh_mutant_0(x);
                if (asinh_result == asinh_mutant_0_result) { _totals[1]++; }
                else
                {
                    cout << "Test " << 1 << " of " << 5 << " detects asinh mutant " << 0 << " of " << 4 << endl;
                    _counts[1]++;
                    _totals[1]++;
                }
            }
            catch (...) {}
            try {
                double asinh_mutant_1_result = MathLibrary::asinh_mutant_1(x);
                if (asinh_result == asinh_mutant_1_result) { _totals[1]++; }
                else
                {
                    cout << "Test " << 1 << " of " << 5 << " detects asinh mutant " << 1 << " of " << 4 << endl;
                    _counts[1]++;
                    _totals[1]++;
                }
            }
            catch (...) {}
            try {
                double asinh_mutant_2_result = MathLibrary::asinh_mutant_2(x);
                if (asinh_result == asinh_mutant_2_result) { _totals[1]++; }
                else
                {
                    cout << "Test " << 1 << " of " << 5 << " detects asinh mutant " << 2 << " of " << 4 << endl;
                    _counts[1]++;
                    _totals[1]++;
                }
            }
            catch (...) {}
            try {
                double asinh_mutant_3_result = MathLibrary::asinh_mutant_3(x);
                if (asinh_result == asinh_mutant_3_result) { _totals[1]++; }
                else
                {
                    cout << "Test " << 1 << " of " << 5 << " detects asinh mutant " << 3 << " of " << 4 << endl;
                    _counts[1]++;
                    _totals[1]++;
                }
            }
            catch (...) {}
        }
        catch (...) {}
    }



    static void asinh_mutation_tests_2(MathLibrary* _self, int _counts[], int _totals[])
    {
        double x = 1.0;

        try {
            double asinh_result = MathLibrary::asinh(x);
            cout << "Test " << 2 << " of asinh on " << _self << " result = " << asinh_result << endl;

            try {
                double asinh_mutant_0_result = MathLibrary::asinh_mutant_0(x);
                if (asinh_result == asinh_mutant_0_result) { _totals[2]++; }
                else
                {
                    cout << "Test " << 2 << " of " << 5 << " detects asinh mutant " << 0 << " of " << 4 << endl;
                    _counts[2]++;
                    _totals[2]++;
                }
            }
            catch (...) {}
            try {
                double asinh_mutant_1_result = MathLibrary::asinh_mutant_1(x);
                if (asinh_result == asinh_mutant_1_result) { _totals[2]++; }
                else
                {
                    cout << "Test " << 2 << " of " << 5 << " detects asinh mutant " << 1 << " of " << 4 << endl;
                    _counts[2]++;
                    _totals[2]++;
                }
            }
            catch (...) {}
            try {
                double asinh_mutant_2_result = MathLibrary::asinh_mutant_2(x);
                if (asinh_result == asinh_mutant_2_result) { _totals[2]++; }
                else
                {
                    cout << "Test " << 2 << " of " << 5 << " detects asinh mutant " << 2 << " of " << 4 << endl;
                    _counts[2]++;
                    _totals[2]++;
                }
            }
            catch (...) {}
            try {
                double asinh_mutant_3_result = MathLibrary::asinh_mutant_3(x);
                if (asinh_result == asinh_mutant_3_result) { _totals[2]++; }
                else
                {
                    cout << "Test " << 2 << " of " << 5 << " detects asinh mutant " << 3 << " of " << 4 << endl;
                    _counts[2]++;
                    _totals[2]++;
                }
            }
            catch (...) {}
        }
        catch (...) {}
    }



    static void asinh_mutation_tests_3(MathLibrary* _self, int _counts[], int _totals[])
    {
        double x = -655.276;

        try {
            double asinh_result = MathLibrary::asinh(x);
            cout << "Test " << 3 << " of asinh on " << _self << " result = " << asinh_result << endl;

            try {
                double asinh_mutant_0_result = MathLibrary::asinh_mutant_0(x);
                if (asinh_result == asinh_mutant_0_result) { _totals[3]++; }
                else
                {
                    cout << "Test " << 3 << " of " << 5 << " detects asinh mutant " << 0 << " of " << 4 << endl;
                    _counts[3]++;
                    _totals[3]++;
                }
            }
            catch (...) {}
            try {
                double asinh_mutant_1_result = MathLibrary::asinh_mutant_1(x);
                if (asinh_result == asinh_mutant_1_result) { _totals[3]++; }
                else
                {
                    cout << "Test " << 3 << " of " << 5 << " detects asinh mutant " << 1 << " of " << 4 << endl;
                    _counts[3]++;
                    _totals[3]++;
                }
            }
            catch (...) {}
            try {
                double asinh_mutant_2_result = MathLibrary::asinh_mutant_2(x);
                if (asinh_result == asinh_mutant_2_result) { _totals[3]++; }
                else
                {
                    cout << "Test " << 3 << " of " << 5 << " detects asinh mutant " << 2 << " of " << 4 << endl;
                    _counts[3]++;
                    _totals[3]++;
                }
            }
            catch (...) {}
            try {
                double asinh_mutant_3_result = MathLibrary::asinh_mutant_3(x);
                if (asinh_result == asinh_mutant_3_result) { _totals[3]++; }
                else
                {
                    cout << "Test " << 3 << " of " << 5 << " detects asinh mutant " << 3 << " of " << 4 << endl;
                    _counts[3]++;
                    _totals[3]++;
                }
            }
            catch (...) {}
        }
        catch (...) {}
    }



    static void asinh_mutation_tests_4(MathLibrary* _self, int _counts[], int _totals[])
    {
        double x = 3125.0891;

        try {
            double asinh_result = MathLibrary::asinh(x);
            cout << "Test " << 4 << " of asinh on " << _self << " result = " << asinh_result << endl;

            try {
                double asinh_mutant_0_result = MathLibrary::asinh_mutant_0(x);
                if (asinh_result == asinh_mutant_0_result) { _totals[4]++; }
                else
                {
                    cout << "Test " << 4 << " of " << 5 << " detects asinh mutant " << 0 << " of " << 4 << endl;
                    _counts[4]++;
                    _totals[4]++;
                }
            }
            catch (...) {}
            try {
                double asinh_mutant_1_result = MathLibrary::asinh_mutant_1(x);
                if (asinh_result == asinh_mutant_1_result) { _totals[4]++; }
                else
                {
                    cout << "Test " << 4 << " of " << 5 << " detects asinh mutant " << 1 << " of " << 4 << endl;
                    _counts[4]++;
                    _totals[4]++;
                }
            }
            catch (...) {}
            try {
                double asinh_mutant_2_result = MathLibrary::asinh_mutant_2(x);
                if (asinh_result == asinh_mutant_2_result) { _totals[4]++; }
                else
                {
                    cout << "Test " << 4 << " of " << 5 << " detects asinh mutant " << 2 << " of " << 4 << endl;
                    _counts[4]++;
                    _totals[4]++;
                }
            }
            catch (...) {}
            try {
                double asinh_mutant_3_result = MathLibrary::asinh_mutant_3(x);
                if (asinh_result == asinh_mutant_3_result) { _totals[4]++; }
                else
                {
                    cout << "Test " << 4 << " of " << 5 << " detects asinh mutant " << 3 << " of " << 4 << endl;
                    _counts[4]++;
                    _totals[4]++;
                }
            }
            catch (...) {}
        }
        catch (...) {}
    }



    static void asinh_mutation_tests(MathLibrary* _self, int _counts[], int _totals[])
    {
        MutationTest::asinh_mutation_tests_0(_self, _counts, _totals);

        MutationTest::asinh_mutation_tests_1(_self, _counts, _totals);

        MutationTest::asinh_mutation_tests_2(_self, _counts, _totals);

        MutationTest::asinh_mutation_tests_3(_self, _counts, _totals);

        MutationTest::asinh_mutation_tests_4(_self, _counts, _totals);


        for (int i = 0; i < 100; i++)
        {
            if (_totals[i] > 0)
            {
                cout << "Test " << i << " detects " << (100.0 * _counts[i]) / _totals[i] << "% asinh mutants" << endl;
            }
        }
    }



    static void modInverse_mutation_tests_0(MathLibrary* _self, int _counts[], int _totals[])
    {
        int n = 0;
        int p = 0;

        try {
            int modInverse_result = MathLibrary::modInverse(n, p);
            cout << "Test " << 0 << " of modInverse on " << _self << " result = " << modInverse_result << endl;

            try {
                int modInverse_mutant_0_result = MathLibrary::modInverse_mutant_0(n, p);
                if (modInverse_result == modInverse_mutant_0_result) { _totals[0]++; }
                else
                {
                    cout << "Test " << 0 << " of " << 20 << " detects modInverse mutant " << 0 << " of " << 3 << endl;
                    _counts[0]++;
                    _totals[0]++;
                }
            }
            catch (...) {}
            try {
                int modInverse_mutant_1_result = MathLibrary::modInverse_mutant_1(n, p);
                if (modInverse_result == modInverse_mutant_1_result) { _totals[0]++; }
                else
                {
                    cout << "Test " << 0 << " of " << 20 << " detects modInverse mutant " << 1 << " of " << 3 << endl;
                    _counts[0]++;
                    _totals[0]++;
                }
            }
            catch (...) {}
            try {
                int modInverse_mutant_2_result = MathLibrary::modInverse_mutant_2(n, p);
                if (modInverse_result == modInverse_mutant_2_result) { _totals[0]++; }
                else
                {
                    cout << "Test " << 0 << " of " << 20 << " detects modInverse mutant " << 2 << " of " << 3 << endl;
                    _counts[0]++;
                    _totals[0]++;
                }
            }
            catch (...) {}
        }
        catch (...) {}
    }



    static void modInverse_mutation_tests_1(MathLibrary* _self, int _counts[], int _totals[])
    {
        int n = 0;
        int p = -1;

        try {
            int modInverse_result = MathLibrary::modInverse(n, p);
            cout << "Test " << 1 << " of modInverse on " << _self << " result = " << modInverse_result << endl;

            try {
                int modInverse_mutant_0_result = MathLibrary::modInverse_mutant_0(n, p);
                if (modInverse_result == modInverse_mutant_0_result) { _totals[1]++; }
                else
                {
                    cout << "Test " << 1 << " of " << 20 << " detects modInverse mutant " << 0 << " of " << 3 << endl;
                    _counts[1]++;
                    _totals[1]++;
                }
            }
            catch (...) {}
            try {
                int modInverse_mutant_1_result = MathLibrary::modInverse_mutant_1(n, p);
                if (modInverse_result == modInverse_mutant_1_result) { _totals[1]++; }
                else
                {
                    cout << "Test " << 1 << " of " << 20 << " detects modInverse mutant " << 1 << " of " << 3 << endl;
                    _counts[1]++;
                    _totals[1]++;
                }
            }
            catch (...) {}
            try {
                int modInverse_mutant_2_result = MathLibrary::modInverse_mutant_2(n, p);
                if (modInverse_result == modInverse_mutant_2_result) { _totals[1]++; }
                else
                {
                    cout << "Test " << 1 << " of " << 20 << " detects modInverse mutant " << 2 << " of " << 3 << endl;
                    _counts[1]++;
                    _totals[1]++;
                }
            }
            catch (...) {}
        }
        catch (...) {}
    }



    static void modInverse_mutation_tests_2(MathLibrary* _self, int _counts[], int _totals[])
    {
        int n = 0;
        int p = 1;

        try {
            int modInverse_result = MathLibrary::modInverse(n, p);
            cout << "Test " << 2 << " of modInverse on " << _self << " result = " << modInverse_result << endl;

            try {
                int modInverse_mutant_0_result = MathLibrary::modInverse_mutant_0(n, p);
                if (modInverse_result == modInverse_mutant_0_result) { _totals[2]++; }
                else
                {
                    cout << "Test " << 2 << " of " << 20 << " detects modInverse mutant " << 0 << " of " << 3 << endl;
                    _counts[2]++;
                    _totals[2]++;
                }
            }
            catch (...) {}
            try {
                int modInverse_mutant_1_result = MathLibrary::modInverse_mutant_1(n, p);
                if (modInverse_result == modInverse_mutant_1_result) { _totals[2]++; }
                else
                {
                    cout << "Test " << 2 << " of " << 20 << " detects modInverse mutant " << 1 << " of " << 3 << endl;
                    _counts[2]++;
                    _totals[2]++;
                }
            }
            catch (...) {}
            try {
                int modInverse_mutant_2_result = MathLibrary::modInverse_mutant_2(n, p);
                if (modInverse_result == modInverse_mutant_2_result) { _totals[2]++; }
                else
                {
                    cout << "Test " << 2 << " of " << 20 << " detects modInverse mutant " << 2 << " of " << 3 << endl;
                    _counts[2]++;
                    _totals[2]++;
                }
            }
            catch (...) {}
        }
        catch (...) {}
    }



    static void modInverse_mutation_tests_3(MathLibrary* _self, int _counts[], int _totals[])
    {
        int n = 0;
        int p = 1024;

        try {
            int modInverse_result = MathLibrary::modInverse(n, p);
            cout << "Test " << 3 << " of modInverse on " << _self << " result = " << modInverse_result << endl;

            try {
                int modInverse_mutant_0_result = MathLibrary::modInverse_mutant_0(n, p);
                if (modInverse_result == modInverse_mutant_0_result) { _totals[3]++; }
                else
                {
                    cout << "Test " << 3 << " of " << 20 << " detects modInverse mutant " << 0 << " of " << 3 << endl;
                    _counts[3]++;
                    _totals[3]++;
                }
            }
            catch (...) {}
            try {
                int modInverse_mutant_1_result = MathLibrary::modInverse_mutant_1(n, p);
                if (modInverse_result == modInverse_mutant_1_result) { _totals[3]++; }
                else
                {
                    cout << "Test " << 3 << " of " << 20 << " detects modInverse mutant " << 1 << " of " << 3 << endl;
                    _counts[3]++;
                    _totals[3]++;
                }
            }
            catch (...) {}
            try {
                int modInverse_mutant_2_result = MathLibrary::modInverse_mutant_2(n, p);
                if (modInverse_result == modInverse_mutant_2_result) { _totals[3]++; }
                else
                {
                    cout << "Test " << 3 << " of " << 20 << " detects modInverse mutant " << 2 << " of " << 3 << endl;
                    _counts[3]++;
                    _totals[3]++;
                }
            }
            catch (...) {}
        }
        catch (...) {}
    }



    static void modInverse_mutation_tests_4(MathLibrary* _self, int _counts[], int _totals[])
    {
        int n = 0;
        int p = -1025;

        try {
            int modInverse_result = MathLibrary::modInverse(n, p);
            cout << "Test " << 4 << " of modInverse on " << _self << " result = " << modInverse_result << endl;

            try {
                int modInverse_mutant_0_result = MathLibrary::modInverse_mutant_0(n, p);
                if (modInverse_result == modInverse_mutant_0_result) { _totals[4]++; }
                else
                {
                    cout << "Test " << 4 << " of " << 20 << " detects modInverse mutant " << 0 << " of " << 3 << endl;
                    _counts[4]++;
                    _totals[4]++;
                }
            }
            catch (...) {}
            try {
                int modInverse_mutant_1_result = MathLibrary::modInverse_mutant_1(n, p);
                if (modInverse_result == modInverse_mutant_1_result) { _totals[4]++; }
                else
                {
                    cout << "Test " << 4 << " of " << 20 << " detects modInverse mutant " << 1 << " of " << 3 << endl;
                    _counts[4]++;
                    _totals[4]++;
                }
            }
            catch (...) {}
            try {
                int modInverse_mutant_2_result = MathLibrary::modInverse_mutant_2(n, p);
                if (modInverse_result == modInverse_mutant_2_result) { _totals[4]++; }
                else
                {
                    cout << "Test " << 4 << " of " << 20 << " detects modInverse mutant " << 2 << " of " << 3 << endl;
                    _counts[4]++;
                    _totals[4]++;
                }
            }
            catch (...) {}
        }
        catch (...) {}
    }



    static void modInverse_mutation_tests_5(MathLibrary* _self, int _counts[], int _totals[])
    {
        int n = -1;
        int p = 0;

        try {
            int modInverse_result = MathLibrary::modInverse(n, p);
            cout << "Test " << 5 << " of modInverse on " << _self << " result = " << modInverse_result << endl;

            try {
                int modInverse_mutant_0_result = MathLibrary::modInverse_mutant_0(n, p);
                if (modInverse_result == modInverse_mutant_0_result) { _totals[5]++; }
                else
                {
                    cout << "Test " << 5 << " of " << 20 << " detects modInverse mutant " << 0 << " of " << 3 << endl;
                    _counts[5]++;
                    _totals[5]++;
                }
            }
            catch (...) {}
            try {
                int modInverse_mutant_1_result = MathLibrary::modInverse_mutant_1(n, p);
                if (modInverse_result == modInverse_mutant_1_result) { _totals[5]++; }
                else
                {
                    cout << "Test " << 5 << " of " << 20 << " detects modInverse mutant " << 1 << " of " << 3 << endl;
                    _counts[5]++;
                    _totals[5]++;
                }
            }
            catch (...) {}
            try {
                int modInverse_mutant_2_result = MathLibrary::modInverse_mutant_2(n, p);
                if (modInverse_result == modInverse_mutant_2_result) { _totals[5]++; }
                else
                {
                    cout << "Test " << 5 << " of " << 20 << " detects modInverse mutant " << 2 << " of " << 3 << endl;
                    _counts[5]++;
                    _totals[5]++;
                }
            }
            catch (...) {}
        }
        catch (...) {}
    }



    static void modInverse_mutation_tests_6(MathLibrary* _self, int _counts[], int _totals[])
    {
        int n = -1;
        int p = -1;

        try {
            int modInverse_result = MathLibrary::modInverse(n, p);
            cout << "Test " << 6 << " of modInverse on " << _self << " result = " << modInverse_result << endl;

            try {
                int modInverse_mutant_0_result = MathLibrary::modInverse_mutant_0(n, p);
                if (modInverse_result == modInverse_mutant_0_result) { _totals[6]++; }
                else
                {
                    cout << "Test " << 6 << " of " << 20 << " detects modInverse mutant " << 0 << " of " << 3 << endl;
                    _counts[6]++;
                    _totals[6]++;
                }
            }
            catch (...) {}
            try {
                int modInverse_mutant_1_result = MathLibrary::modInverse_mutant_1(n, p);
                if (modInverse_result == modInverse_mutant_1_result) { _totals[6]++; }
                else
                {
                    cout << "Test " << 6 << " of " << 20 << " detects modInverse mutant " << 1 << " of " << 3 << endl;
                    _counts[6]++;
                    _totals[6]++;
                }
            }
            catch (...) {}
            try {
                int modInverse_mutant_2_result = MathLibrary::modInverse_mutant_2(n, p);
                if (modInverse_result == modInverse_mutant_2_result) { _totals[6]++; }
                else
                {
                    cout << "Test " << 6 << " of " << 20 << " detects modInverse mutant " << 2 << " of " << 3 << endl;
                    _counts[6]++;
                    _totals[6]++;
                }
            }
            catch (...) {}
        }
        catch (...) {}
    }



    static void modInverse_mutation_tests_7(MathLibrary* _self, int _counts[], int _totals[])
    {
        int n = -1;
        int p = 1;

        try {
            int modInverse_result = MathLibrary::modInverse(n, p);
            cout << "Test " << 7 << " of modInverse on " << _self << " result = " << modInverse_result << endl;

            try {
                int modInverse_mutant_0_result = MathLibrary::modInverse_mutant_0(n, p);
                if (modInverse_result == modInverse_mutant_0_result) { _totals[7]++; }
                else
                {
                    cout << "Test " << 7 << " of " << 20 << " detects modInverse mutant " << 0 << " of " << 3 << endl;
                    _counts[7]++;
                    _totals[7]++;
                }
            }
            catch (...) {}
            try {
                int modInverse_mutant_1_result = MathLibrary::modInverse_mutant_1(n, p);
                if (modInverse_result == modInverse_mutant_1_result) { _totals[7]++; }
                else
                {
                    cout << "Test " << 7 << " of " << 20 << " detects modInverse mutant " << 1 << " of " << 3 << endl;
                    _counts[7]++;
                    _totals[7]++;
                }
            }
            catch (...) {}
            try {
                int modInverse_mutant_2_result = MathLibrary::modInverse_mutant_2(n, p);
                if (modInverse_result == modInverse_mutant_2_result) { _totals[7]++; }
                else
                {
                    cout << "Test " << 7 << " of " << 20 << " detects modInverse mutant " << 2 << " of " << 3 << endl;
                    _counts[7]++;
                    _totals[7]++;
                }
            }
            catch (...) {}
        }
        catch (...) {}
    }



    static void modInverse_mutation_tests_8(MathLibrary* _self, int _counts[], int _totals[])
    {
        int n = -1;
        int p = 1024;

        try {
            int modInverse_result = MathLibrary::modInverse(n, p);
            cout << "Test " << 8 << " of modInverse on " << _self << " result = " << modInverse_result << endl;

            try {
                int modInverse_mutant_0_result = MathLibrary::modInverse_mutant_0(n, p);
                if (modInverse_result == modInverse_mutant_0_result) { _totals[8]++; }
                else
                {
                    cout << "Test " << 8 << " of " << 20 << " detects modInverse mutant " << 0 << " of " << 3 << endl;
                    _counts[8]++;
                    _totals[8]++;
                }
            }
            catch (...) {}
            try {
                int modInverse_mutant_1_result = MathLibrary::modInverse_mutant_1(n, p);
                if (modInverse_result == modInverse_mutant_1_result) { _totals[8]++; }
                else
                {
                    cout << "Test " << 8 << " of " << 20 << " detects modInverse mutant " << 1 << " of " << 3 << endl;
                    _counts[8]++;
                    _totals[8]++;
                }
            }
            catch (...) {}
            try {
                int modInverse_mutant_2_result = MathLibrary::modInverse_mutant_2(n, p);
                if (modInverse_result == modInverse_mutant_2_result) { _totals[8]++; }
                else
                {
                    cout << "Test " << 8 << " of " << 20 << " detects modInverse mutant " << 2 << " of " << 3 << endl;
                    _counts[8]++;
                    _totals[8]++;
                }
            }
            catch (...) {}
        }
        catch (...) {}
    }



    static void modInverse_mutation_tests_9(MathLibrary* _self, int _counts[], int _totals[])
    {
        int n = -1;
        int p = -1025;

        try {
            int modInverse_result = MathLibrary::modInverse(n, p);
            cout << "Test " << 9 << " of modInverse on " << _self << " result = " << modInverse_result << endl;

            try {
                int modInverse_mutant_0_result = MathLibrary::modInverse_mutant_0(n, p);
                if (modInverse_result == modInverse_mutant_0_result) { _totals[9]++; }
                else
                {
                    cout << "Test " << 9 << " of " << 20 << " detects modInverse mutant " << 0 << " of " << 3 << endl;
                    _counts[9]++;
                    _totals[9]++;
                }
            }
            catch (...) {}
            try {
                int modInverse_mutant_1_result = MathLibrary::modInverse_mutant_1(n, p);
                if (modInverse_result == modInverse_mutant_1_result) { _totals[9]++; }
                else
                {
                    cout << "Test " << 9 << " of " << 20 << " detects modInverse mutant " << 1 << " of " << 3 << endl;
                    _counts[9]++;
                    _totals[9]++;
                }
            }
            catch (...) {}
            try {
                int modInverse_mutant_2_result = MathLibrary::modInverse_mutant_2(n, p);
                if (modInverse_result == modInverse_mutant_2_result) { _totals[9]++; }
                else
                {
                    cout << "Test " << 9 << " of " << 20 << " detects modInverse mutant " << 2 << " of " << 3 << endl;
                    _counts[9]++;
                    _totals[9]++;
                }
            }
            catch (...) {}
        }
        catch (...) {}
    }



    static void modInverse_mutation_tests_10(MathLibrary* _self, int _counts[], int _totals[])
    {
        int n = 1;
        int p = 0;

        try {
            int modInverse_result = MathLibrary::modInverse(n, p);
            cout << "Test " << 10 << " of modInverse on " << _self << " result = " << modInverse_result << endl;

            try {
                int modInverse_mutant_0_result = MathLibrary::modInverse_mutant_0(n, p);
                if (modInverse_result == modInverse_mutant_0_result) { _totals[10]++; }
                else
                {
                    cout << "Test " << 10 << " of " << 20 << " detects modInverse mutant " << 0 << " of " << 3 << endl;
                    _counts[10]++;
                    _totals[10]++;
                }
            }
            catch (...) {}
            try {
                int modInverse_mutant_1_result = MathLibrary::modInverse_mutant_1(n, p);
                if (modInverse_result == modInverse_mutant_1_result) { _totals[10]++; }
                else
                {
                    cout << "Test " << 10 << " of " << 20 << " detects modInverse mutant " << 1 << " of " << 3 << endl;
                    _counts[10]++;
                    _totals[10]++;
                }
            }
            catch (...) {}
            try {
                int modInverse_mutant_2_result = MathLibrary::modInverse_mutant_2(n, p);
                if (modInverse_result == modInverse_mutant_2_result) { _totals[10]++; }
                else
                {
                    cout << "Test " << 10 << " of " << 20 << " detects modInverse mutant " << 2 << " of " << 3 << endl;
                    _counts[10]++;
                    _totals[10]++;
                }
            }
            catch (...) {}
        }
        catch (...) {}
    }



    static void modInverse_mutation_tests_11(MathLibrary* _self, int _counts[], int _totals[])
    {
        int n = 1;
        int p = -1;

        try {
            int modInverse_result = MathLibrary::modInverse(n, p);
            cout << "Test " << 11 << " of modInverse on " << _self << " result = " << modInverse_result << endl;

            try {
                int modInverse_mutant_0_result = MathLibrary::modInverse_mutant_0(n, p);
                if (modInverse_result == modInverse_mutant_0_result) { _totals[11]++; }
                else
                {
                    cout << "Test " << 11 << " of " << 20 << " detects modInverse mutant " << 0 << " of " << 3 << endl;
                    _counts[11]++;
                    _totals[11]++;
                }
            }
            catch (...) {}
            try {
                int modInverse_mutant_1_result = MathLibrary::modInverse_mutant_1(n, p);
                if (modInverse_result == modInverse_mutant_1_result) { _totals[11]++; }
                else
                {
                    cout << "Test " << 11 << " of " << 20 << " detects modInverse mutant " << 1 << " of " << 3 << endl;
                    _counts[11]++;
                    _totals[11]++;
                }
            }
            catch (...) {}
            try {
                int modInverse_mutant_2_result = MathLibrary::modInverse_mutant_2(n, p);
                if (modInverse_result == modInverse_mutant_2_result) { _totals[11]++; }
                else
                {
                    cout << "Test " << 11 << " of " << 20 << " detects modInverse mutant " << 2 << " of " << 3 << endl;
                    _counts[11]++;
                    _totals[11]++;
                }
            }
            catch (...) {}
        }
        catch (...) {}
    }



    static void modInverse_mutation_tests_12(MathLibrary* _self, int _counts[], int _totals[])
    {
        int n = 1;
        int p = 1;

        try {
            int modInverse_result = MathLibrary::modInverse(n, p);
            cout << "Test " << 12 << " of modInverse on " << _self << " result = " << modInverse_result << endl;

            try {
                int modInverse_mutant_0_result = MathLibrary::modInverse_mutant_0(n, p);
                if (modInverse_result == modInverse_mutant_0_result) { _totals[12]++; }
                else
                {
                    cout << "Test " << 12 << " of " << 20 << " detects modInverse mutant " << 0 << " of " << 3 << endl;
                    _counts[12]++;
                    _totals[12]++;
                }
            }
            catch (...) {}
            try {
                int modInverse_mutant_1_result = MathLibrary::modInverse_mutant_1(n, p);
                if (modInverse_result == modInverse_mutant_1_result) { _totals[12]++; }
                else
                {
                    cout << "Test " << 12 << " of " << 20 << " detects modInverse mutant " << 1 << " of " << 3 << endl;
                    _counts[12]++;
                    _totals[12]++;
                }
            }
            catch (...) {}
            try {
                int modInverse_mutant_2_result = MathLibrary::modInverse_mutant_2(n, p);
                if (modInverse_result == modInverse_mutant_2_result) { _totals[12]++; }
                else
                {
                    cout << "Test " << 12 << " of " << 20 << " detects modInverse mutant " << 2 << " of " << 3 << endl;
                    _counts[12]++;
                    _totals[12]++;
                }
            }
            catch (...) {}
        }
        catch (...) {}
    }



    static void modInverse_mutation_tests_13(MathLibrary* _self, int _counts[], int _totals[])
    {
        int n = 1;
        int p = 1024;

        try {
            int modInverse_result = MathLibrary::modInverse(n, p);
            cout << "Test " << 13 << " of modInverse on " << _self << " result = " << modInverse_result << endl;

            try {
                int modInverse_mutant_0_result = MathLibrary::modInverse_mutant_0(n, p);
                if (modInverse_result == modInverse_mutant_0_result) { _totals[13]++; }
                else
                {
                    cout << "Test " << 13 << " of " << 20 << " detects modInverse mutant " << 0 << " of " << 3 << endl;
                    _counts[13]++;
                    _totals[13]++;
                }
            }
            catch (...) {}
            try {
                int modInverse_mutant_1_result = MathLibrary::modInverse_mutant_1(n, p);
                if (modInverse_result == modInverse_mutant_1_result) { _totals[13]++; }
                else
                {
                    cout << "Test " << 13 << " of " << 20 << " detects modInverse mutant " << 1 << " of " << 3 << endl;
                    _counts[13]++;
                    _totals[13]++;
                }
            }
            catch (...) {}
            try {
                int modInverse_mutant_2_result = MathLibrary::modInverse_mutant_2(n, p);
                if (modInverse_result == modInverse_mutant_2_result) { _totals[13]++; }
                else
                {
                    cout << "Test " << 13 << " of " << 20 << " detects modInverse mutant " << 2 << " of " << 3 << endl;
                    _counts[13]++;
                    _totals[13]++;
                }
            }
            catch (...) {}
        }
        catch (...) {}
    }



    static void modInverse_mutation_tests_14(MathLibrary* _self, int _counts[], int _totals[])
    {
        int n = 1;
        int p = -1025;

        try {
            int modInverse_result = MathLibrary::modInverse(n, p);
            cout << "Test " << 14 << " of modInverse on " << _self << " result = " << modInverse_result << endl;

            try {
                int modInverse_mutant_0_result = MathLibrary::modInverse_mutant_0(n, p);
                if (modInverse_result == modInverse_mutant_0_result) { _totals[14]++; }
                else
                {
                    cout << "Test " << 14 << " of " << 20 << " detects modInverse mutant " << 0 << " of " << 3 << endl;
                    _counts[14]++;
                    _totals[14]++;
                }
            }
            catch (...) {}
            try {
                int modInverse_mutant_1_result = MathLibrary::modInverse_mutant_1(n, p);
                if (modInverse_result == modInverse_mutant_1_result) { _totals[14]++; }
                else
                {
                    cout << "Test " << 14 << " of " << 20 << " detects modInverse mutant " << 1 << " of " << 3 << endl;
                    _counts[14]++;
                    _totals[14]++;
                }
            }
            catch (...) {}
            try {
                int modInverse_mutant_2_result = MathLibrary::modInverse_mutant_2(n, p);
                if (modInverse_result == modInverse_mutant_2_result) { _totals[14]++; }
                else
                {
                    cout << "Test " << 14 << " of " << 20 << " detects modInverse mutant " << 2 << " of " << 3 << endl;
                    _counts[14]++;
                    _totals[14]++;
                }
            }
            catch (...) {}
        }
        catch (...) {}
    }



    static void modInverse_mutation_tests_15(MathLibrary* _self, int _counts[], int _totals[])
    {
        int n = 1024;
        int p = 0;

        try {
            int modInverse_result = MathLibrary::modInverse(n, p);
            cout << "Test " << 15 << " of modInverse on " << _self << " result = " << modInverse_result << endl;

            try {
                int modInverse_mutant_0_result = MathLibrary::modInverse_mutant_0(n, p);
                if (modInverse_result == modInverse_mutant_0_result) { _totals[15]++; }
                else
                {
                    cout << "Test " << 15 << " of " << 20 << " detects modInverse mutant " << 0 << " of " << 3 << endl;
                    _counts[15]++;
                    _totals[15]++;
                }
            }
            catch (...) {}
            try {
                int modInverse_mutant_1_result = MathLibrary::modInverse_mutant_1(n, p);
                if (modInverse_result == modInverse_mutant_1_result) { _totals[15]++; }
                else
                {
                    cout << "Test " << 15 << " of " << 20 << " detects modInverse mutant " << 1 << " of " << 3 << endl;
                    _counts[15]++;
                    _totals[15]++;
                }
            }
            catch (...) {}
            try {
                int modInverse_mutant_2_result = MathLibrary::modInverse_mutant_2(n, p);
                if (modInverse_result == modInverse_mutant_2_result) { _totals[15]++; }
                else
                {
                    cout << "Test " << 15 << " of " << 20 << " detects modInverse mutant " << 2 << " of " << 3 << endl;
                    _counts[15]++;
                    _totals[15]++;
                }
            }
            catch (...) {}
        }
        catch (...) {}
    }



    static void modInverse_mutation_tests_16(MathLibrary* _self, int _counts[], int _totals[])
    {
        int n = 1024;
        int p = -1;

        try {
            int modInverse_result = MathLibrary::modInverse(n, p);
            cout << "Test " << 16 << " of modInverse on " << _self << " result = " << modInverse_result << endl;

            try {
                int modInverse_mutant_0_result = MathLibrary::modInverse_mutant_0(n, p);
                if (modInverse_result == modInverse_mutant_0_result) { _totals[16]++; }
                else
                {
                    cout << "Test " << 16 << " of " << 20 << " detects modInverse mutant " << 0 << " of " << 3 << endl;
                    _counts[16]++;
                    _totals[16]++;
                }
            }
            catch (...) {}
            try {
                int modInverse_mutant_1_result = MathLibrary::modInverse_mutant_1(n, p);
                if (modInverse_result == modInverse_mutant_1_result) { _totals[16]++; }
                else
                {
                    cout << "Test " << 16 << " of " << 20 << " detects modInverse mutant " << 1 << " of " << 3 << endl;
                    _counts[16]++;
                    _totals[16]++;
                }
            }
            catch (...) {}
            try {
                int modInverse_mutant_2_result = MathLibrary::modInverse_mutant_2(n, p);
                if (modInverse_result == modInverse_mutant_2_result) { _totals[16]++; }
                else
                {
                    cout << "Test " << 16 << " of " << 20 << " detects modInverse mutant " << 2 << " of " << 3 << endl;
                    _counts[16]++;
                    _totals[16]++;
                }
            }
            catch (...) {}
        }
        catch (...) {}
    }



    static void modInverse_mutation_tests_17(MathLibrary* _self, int _counts[], int _totals[])
    {
        int n = 1024;
        int p = 1;

        try {
            int modInverse_result = MathLibrary::modInverse(n, p);
            cout << "Test " << 17 << " of modInverse on " << _self << " result = " << modInverse_result << endl;

            try {
                int modInverse_mutant_0_result = MathLibrary::modInverse_mutant_0(n, p);
                if (modInverse_result == modInverse_mutant_0_result) { _totals[17]++; }
                else
                {
                    cout << "Test " << 17 << " of " << 20 << " detects modInverse mutant " << 0 << " of " << 3 << endl;
                    _counts[17]++;
                    _totals[17]++;
                }
            }
            catch (...) {}
            try {
                int modInverse_mutant_1_result = MathLibrary::modInverse_mutant_1(n, p);
                if (modInverse_result == modInverse_mutant_1_result) { _totals[17]++; }
                else
                {
                    cout << "Test " << 17 << " of " << 20 << " detects modInverse mutant " << 1 << " of " << 3 << endl;
                    _counts[17]++;
                    _totals[17]++;
                }
            }
            catch (...) {}
            try {
                int modInverse_mutant_2_result = MathLibrary::modInverse_mutant_2(n, p);
                if (modInverse_result == modInverse_mutant_2_result) { _totals[17]++; }
                else
                {
                    cout << "Test " << 17 << " of " << 20 << " detects modInverse mutant " << 2 << " of " << 3 << endl;
                    _counts[17]++;
                    _totals[17]++;
                }
            }
            catch (...) {}
        }
        catch (...) {}
    }



    static void modInverse_mutation_tests_18(MathLibrary* _self, int _counts[], int _totals[])
    {
        int n = 1024;
        int p = 1024;

        try {
            int modInverse_result = MathLibrary::modInverse(n, p);
            cout << "Test " << 18 << " of modInverse on " << _self << " result = " << modInverse_result << endl;

            try {
                int modInverse_mutant_0_result = MathLibrary::modInverse_mutant_0(n, p);
                if (modInverse_result == modInverse_mutant_0_result) { _totals[18]++; }
                else
                {
                    cout << "Test " << 18 << " of " << 20 << " detects modInverse mutant " << 0 << " of " << 3 << endl;
                    _counts[18]++;
                    _totals[18]++;
                }
            }
            catch (...) {}
            try {
                int modInverse_mutant_1_result = MathLibrary::modInverse_mutant_1(n, p);
                if (modInverse_result == modInverse_mutant_1_result) { _totals[18]++; }
                else
                {
                    cout << "Test " << 18 << " of " << 20 << " detects modInverse mutant " << 1 << " of " << 3 << endl;
                    _counts[18]++;
                    _totals[18]++;
                }
            }
            catch (...) {}
            try {
                int modInverse_mutant_2_result = MathLibrary::modInverse_mutant_2(n, p);
                if (modInverse_result == modInverse_mutant_2_result) { _totals[18]++; }
                else
                {
                    cout << "Test " << 18 << " of " << 20 << " detects modInverse mutant " << 2 << " of " << 3 << endl;
                    _counts[18]++;
                    _totals[18]++;
                }
            }
            catch (...) {}
        }
        catch (...) {}
    }



    static void modInverse_mutation_tests_19(MathLibrary* _self, int _counts[], int _totals[])
    {
        int n = 1024;
        int p = -1025;

        try {
            int modInverse_result = MathLibrary::modInverse(n, p);
            cout << "Test " << 19 << " of modInverse on " << _self << " result = " << modInverse_result << endl;

            try {
                int modInverse_mutant_0_result = MathLibrary::modInverse_mutant_0(n, p);
                if (modInverse_result == modInverse_mutant_0_result) { _totals[19]++; }
                else
                {
                    cout << "Test " << 19 << " of " << 20 << " detects modInverse mutant " << 0 << " of " << 3 << endl;
                    _counts[19]++;
                    _totals[19]++;
                }
            }
            catch (...) {}
            try {
                int modInverse_mutant_1_result = MathLibrary::modInverse_mutant_1(n, p);
                if (modInverse_result == modInverse_mutant_1_result) { _totals[19]++; }
                else
                {
                    cout << "Test " << 19 << " of " << 20 << " detects modInverse mutant " << 1 << " of " << 3 << endl;
                    _counts[19]++;
                    _totals[19]++;
                }
            }
            catch (...) {}
            try {
                int modInverse_mutant_2_result = MathLibrary::modInverse_mutant_2(n, p);
                if (modInverse_result == modInverse_mutant_2_result) { _totals[19]++; }
                else
                {
                    cout << "Test " << 19 << " of " << 20 << " detects modInverse mutant " << 2 << " of " << 3 << endl;
                    _counts[19]++;
                    _totals[19]++;
                }
            }
            catch (...) {}
        }
        catch (...) {}
    }



    static void modInverse_mutation_tests(MathLibrary* _self, int _counts[], int _totals[])
    {
        MutationTest::modInverse_mutation_tests_0(_self, _counts, _totals);

        MutationTest::modInverse_mutation_tests_1(_self, _counts, _totals);

        MutationTest::modInverse_mutation_tests_2(_self, _counts, _totals);

        MutationTest::modInverse_mutation_tests_3(_self, _counts, _totals);

        MutationTest::modInverse_mutation_tests_4(_self, _counts, _totals);

        MutationTest::modInverse_mutation_tests_5(_self, _counts, _totals);

        MutationTest::modInverse_mutation_tests_6(_self, _counts, _totals);

        MutationTest::modInverse_mutation_tests_7(_self, _counts, _totals);

        MutationTest::modInverse_mutation_tests_8(_self, _counts, _totals);

        MutationTest::modInverse_mutation_tests_9(_self, _counts, _totals);

        MutationTest::modInverse_mutation_tests_10(_self, _counts, _totals);

        MutationTest::modInverse_mutation_tests_11(_self, _counts, _totals);

        MutationTest::modInverse_mutation_tests_12(_self, _counts, _totals);

        MutationTest::modInverse_mutation_tests_13(_self, _counts, _totals);

        MutationTest::modInverse_mutation_tests_14(_self, _counts, _totals);

        MutationTest::modInverse_mutation_tests_15(_self, _counts, _totals);

        MutationTest::modInverse_mutation_tests_16(_self, _counts, _totals);

        MutationTest::modInverse_mutation_tests_17(_self, _counts, _totals);

        MutationTest::modInverse_mutation_tests_18(_self, _counts, _totals);

        MutationTest::modInverse_mutation_tests_19(_self, _counts, _totals);


        for (int i = 0; i < 100; i++)
        {
            if (_totals[i] > 0)
            {
                cout << "Test " << i << " detects " << (100.0 * _counts[i]) / _totals[i] << "% modInverse mutants" << endl;
            }
        }
    }



};



class Controller
{
private:
    map<string, void*> objectmap;
    map<string, string> classmap;
    vector<MathLibrary*>* mathlibrary_s;

public:

    static Controller* inst;


    Controller() {
        mathlibrary_s = new vector<MathLibrary*>();
    }


    void loadModel();
    void addObjectToClass(string a, string c);
    void addObjectToRole(string a, string b, string role);
    void setObjectFeatureValue(string a, string f, string val);
    void saveModel(string f);


    void addMathLibrary(MathLibrary* _oo) { mathlibrary_s->push_back(_oo); }



    void createAllMathLibrary(vector<MathLibrary*>* mathlibraryx)
    {
        for (int i = 0; i < mathlibraryx->size(); i++)
        {
            MathLibrary* mathlibraryx_x = new MathLibrary();
            (*mathlibraryx)[i] = mathlibraryx_x;
            addMathLibrary(mathlibraryx_x);
        }
    }


    MathLibrary* createMathLibrary()
    {
        MathLibrary* mathlibraryx = new MathLibrary();
        addMathLibrary(mathlibraryx);

        return mathlibraryx;
    }

    MathLibrary* copyMathLibrary(MathLibrary* self)
    {
        MathLibrary* mathlibraryx = new MathLibrary();
        addMathLibrary(mathlibraryx);

        return mathlibraryx;
    }

    vector<MathLibrary*>* getmathlibrary_s() { return mathlibrary_s; }




    void killAllMathLibrary(vector<MathLibrary*>* mathlibraryxx)
    {
        for (int _i = 0; _i < mathlibraryxx->size(); _i++)
        {
            killMathLibrary((*mathlibraryxx)[_i]);
        }
    }

    void killAllMathLibrary(std::set<MathLibrary*>* mathlibraryxx)
    {
        for (std::set<MathLibrary*>::iterator _i = mathlibraryxx->begin(); _i != mathlibraryxx->end(); ++_i)
        {
            killMathLibrary(*_i);
        }
    }

    void killMathLibrary(MathLibrary* mathlibraryxx)
    {
        mathlibrary_s->erase(find(mathlibrary_s->begin(), mathlibrary_s->end(), mathlibraryxx));
        // delete mathlibraryxx;
    }





    ~Controller() {
        delete mathlibrary_s;
    }



};



static std::set<int>* select_0(std::set<int>* _l, int n, int p)
{ // implements: Integer.subrange(1,p - 1)->select( m | ( ( m * n ) mod p ) = 1 )
    std::set<int>* _results_0 = new std::set<int>();
    for (std::set<int>::iterator _iselect = _l->begin(); _iselect != _l->end(); ++_iselect)
    {
        int m = *_iselect;
        if (((m * n) % p) == 1)
        {
            _results_0->insert(m);
        }
    }
    return _results_0;
}

static std::vector<int>* select_0(std::vector<int>* _l, int n, int p)
{
    std::vector<int>* _results_0 = new std::vector<int>();
    for (std::vector<int>::iterator _iselect = _l->begin(); _iselect != _l->end(); ++_iselect)
    {
        int m = *_iselect;
        if (((m * n) % p) == 1)
        {
            _results_0->push_back(m);
        }
    }
    return _results_0;
}

static std::set<int>* select_1(std::set<int>* _l, int n, int p)
{ // implements: Integer.subrange(1,p - 1)->select( m | ( ( m * n ) div p ) = 1 )
    std::set<int>* _results_1 = new std::set<int>();
    for (std::set<int>::iterator _iselect = _l->begin(); _iselect != _l->end(); ++_iselect)
    {
        int m = *_iselect;
        if ((((int)((m * n) / p))) == 1)
        {
            _results_1->insert(m);
        }
    }
    return _results_1;
}

static std::vector<int>* select_1(std::vector<int>* _l, int n, int p)
{
    std::vector<int>* _results_1 = new std::vector<int>();
    for (std::vector<int>::iterator _iselect = _l->begin(); _iselect != _l->end(); ++_iselect)
    {
        int m = *_iselect;
        if ((((int)((m * n) / p))) == 1)
        {
            _results_1->push_back(m);
        }
    }
    return _results_1;
}


static std::set<int>* reject_2(std::set<int>* _l, int n, int p)
{ // implements: Integer.subrange(1,p - 1)->reject( m | ( ( m * n ) mod p ) = 1 )
    std::set<int>* _results_2 = new std::set<int>();
    for (std::set<int>::iterator _ireject = _l->begin(); _ireject != _l->end(); ++_ireject)
    {
        int m = *_ireject;
        if (((m * n) % p) == 1) {}
        else { _results_2->insert(m); }
    }
    return _results_2;
}

static std::vector<int>* reject_2(std::vector<int>* _l, int n, int p)
{
    std::vector<int>* _results_2 = new std::vector<int>();
    for (std::vector<int>::iterator _ireject = _l->begin(); _ireject != _l->end(); ++_ireject)
    {
        int m = *_ireject;
        if (((m * n) % p) == 1) {}
        else { _results_2->push_back(m); }
    }
    return _results_2;
}

class TestsGUI
{
public:
    Controller* cont = Controller::inst;


    TestsGUI()
    {
        cont->loadModel();
    }

    void runTests() {
        { int _mathlibrary_s_instances = Controller::inst->getmathlibrary_s()->size();
        int mathlibrary_s_asinh_counts[100] = { 0 };
        int mathlibrary_s_asinh_totals[100] = { 0 };
        if (_mathlibrary_s_instances > 0)
        {
            MathLibrary* _ex = Controller::inst->getmathlibrary_s()->at(0);
            MutationTest::asinh_mutation_tests(_ex, mathlibrary_s_asinh_counts, mathlibrary_s_asinh_totals);
        }
        cout << endl;
        int mathlibrary_s_modInverse_counts[100] = { 0 };
        int mathlibrary_s_modInverse_totals[100] = { 0 };
        if (_mathlibrary_s_instances > 0)
        {
            MathLibrary* _ex = Controller::inst->getmathlibrary_s()->at(0);
            MutationTest::modInverse_mutation_tests(_ex, mathlibrary_s_modInverse_counts, mathlibrary_s_modInverse_totals);
        }
        cout << endl;
        }


    }



    void testUseCases() {
        int intTestValues[] = { 0, -1, 1, -1025, 1024 };
        long longTestValues[] = { 0, -1, 1, -999999, 100000 };
        double doubleTestValues[] = { 0, -1, 1, 3125.0891, 4.9E-324 };
        bool booleanTestValues[] = { false, true };
        string stringTestValues[] = { "", " abc_XZ ", "#�$* &~@'" };
    }


};

class SustainabilityTest
{
private:
    vector<int>* elems;

public:
    SustainabilityTest()
    {
        elems = (new vector<int>());
    }

    friend ostream& operator<<(ostream& s, SustainabilityTest& x)
    {
        return s << "(SustainabilityTest)  " << "elems = " << x.getelems() << endl;
    }

    virtual string toString() {
        return "(SustainabilityTest) " + UmlRsdsLib<int>::collectionToString(getelems());
    }

    void setelems(vector<int>* elems_x) { elems = elems_x; }

    vector<int>* getelems() { return elems; }

    void op();

    ~SustainabilityTest() {
    }

};


