template<class T>
class OclIteratorOptimised
{
private:
    int position;
    int markedPosition;
    int len; 
    T* elements;
    
public:
    OclIteratorOptimised()
    {
        position = 0;
        markedPosition = 0;
        len = 0; 
        elements = NULL;
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
