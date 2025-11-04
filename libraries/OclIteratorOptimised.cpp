template<class T>
bool OclIteratorOptimised<T>::hasNext()
{
    bool result = false;
    if (position >= 0 && position < len)
    {
        result = true;
    }
    else
    {
        result = false;
    }
    return result;
}

template<class T>
bool OclIteratorOptimised<T>::hasPrevious()
{
    bool result = false;
    if (position > 1 && position <= len)
    {
        result = true;
    }
    else
    {
        result = false;
    }
    return result;
}

template<class T>
bool OclIteratorOptimised<T>::isAfterLast()
{
    bool result = false;
    if (position > len)
    {
        result = true;
    }
    else
    {
        result = false;
    }
    return result;
}

template<class T>
bool OclIteratorOptimised<T>::isBeforeFirst()
{
    bool result = false;
    if (position < 1)
    {
        result = true;
    }
    else
    {
        result = false;
    }
    return result;
}

template<class T>
int OclIteratorOptimised<T>::nextIndex()
{
    int result = 0;
    result = position + 1;
    return result;
}

template<class T>
int OclIteratorOptimised<T>::previousIndex()
{
    int result = 0;
    result = position - 1;
    return result;
}


template<class T>
void OclIteratorOptimised<T>::moveForward()
{
    int pre_position0 = position;
    //  if (position + 1 > elements->size())) { return; } 
    this->setPosition(pre_position0 + 1);
}

template<class T>
void OclIteratorOptimised<T>::moveBackward()
{
    int pre_position1 = position;
    //  if (position <= 1)) { return; } 
    this->setPosition(pre_position1 - 1);
}

template<class T>
void OclIteratorOptimised<T>::moveTo(int i)
{ this->setPosition(i); }

template<class T>
void OclIteratorOptimised<T>::movePosition(int i)
{
    int pos = this->getPosition();
    this->setPosition(i + pos);
}

template<class T>
void OclIteratorOptimised<T>::moveToStart()
{
    this->setPosition(0);
}

template<class T>
void OclIteratorOptimised<T>::moveToEnd()
{
    this->setPosition(this->len + 1);
}

template<class T>
void OclIteratorOptimised<T>::moveToFirst()
{
    this->setPosition(1);
}

template<class T>
void OclIteratorOptimised<T>::moveToLast()
{
    this->setPosition(this->len);
}

template<class T>
OclIteratorOptimised<T>* OclIteratorOptimised<T>::newOclIterator_Sequence(vector<T>* sq)
{
    OclIteratorOptimised<T>* ot = new OclIteratorOptimised<T>();
    int sze = sq->size(); 
    ot->elements = new T[sze]; 
    for (int i = 0; i < sze; i++)
    {
        ot->elements[i] = sq->at(i);
    }
    ot->setPosition(0);
    ot->len = sze; 
    ot->markedPosition = 0;
    return ot;
}

template<class T>
T OclIteratorOptimised<T>::getCurrent()
{
    T result = NULL;
    if (position < 1 || position > len)
    {
        return result;
    }
    result = elements[position - 1];
    return result;
}

template<class T>
void OclIteratorOptimised<T>::set(T x)
{
    this->elements[position - 1] = x;
}

template<class T>
void OclIteratorOptimised<T>::remove()
{
   this->elements[position - 1] = NULL;
}

template<class T>
T OclIteratorOptimised<T>::next()
{
    this->moveForward();
    return this->getCurrent();
}

template<class T>
T OclIteratorOptimised<T>::previous()
{
    this->moveBackward();
    return this->getCurrent();
}

template<class T>
T OclIteratorOptimised<T>::at(int i)
{
    return elements[i - 1];
}

template<class T>
int OclIteratorOptimised<T>::length()
{
    return this->len;
}

template<class T>
void OclIteratorOptimised<T>::close()
{
    position = 0;
    markedPosition = 0;
    elements = NULL;
    len = 0; 
}

