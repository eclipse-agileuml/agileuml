// cppUnitTests.cpp : This file contains the 'main' function. Program execution begins and ends there.
//

#include <iostream>
// Controller.cc
#include <stdio.h>
#include <string>
#include <vector>
#include <set>
#include <map>
#include <iostream>
#include <fstream>
#include <sstream>
#include <cmath>
#include <ctime>
#include <algorithm>
#include <regex>
#include <thread>
#include <functional>
#include <cstdlib>
#include <condition_variable>
#include <sys/stat.h>
#include <direct.h>
#include <winsock2.h>
#include <ws2tcpip.h>
#include <winhttp.h>
#include <bit>

// #include <windows.h>


#pragma warning(disable : 4996)
#pragma comment(lib, "Ws2_32.lib")
#pragma comment(lib, "winhttp.lib")

#include "Controller.h"

using namespace std;




Controller* Controller::inst = new Controller();


map<string, OclType*>* OclType::ocltypenameindex = new map<string, OclType*>();



double MathLibrary::asinh(double x)
{
    double result = 0.0;
    result = log((x + sqrt((x * x + 1))));
    return result;
}


int MathLibrary::modInverse(int n, int p)
{
    int result = 0;
    if (0 >= n || n >= p) { return result; }

    { vector<int>* inverses = select_0(UmlRsdsLib<int>::integerSubrange(1, p - 1), n, p);
    if (inverses->size() > 0) { result = UmlRsdsLib<int>::min(inverses); }
    else { result = 0; }
    }
    return result;
}


double MathLibrary::asinh_mutant_0(double x)
{
    double result = 0.0;
    result = log((x + sqrt((x * x - 1))));
    return result;
}


double MathLibrary::asinh_mutant_1(double x)
{
    double result = 0.0;
    result = log((x + sqrt((x * x * 1))));
    return result;
}


double MathLibrary::asinh_mutant_2(double x)
{
    double result = 0.0;
    result = log((x - sqrt((x * x + 1))));
    return result;
}


double MathLibrary::asinh_mutant_3(double x)
{
    double result = 0.0;
    result = log((x * sqrt((x * x + 1))));
    return result;
}


int MathLibrary::modInverse_mutant_0(int n, int p)
{
    int result = 0;
    if (0 >= n || n >= p) { return result; }

    { vector<int>* inverses = select_1(UmlRsdsLib<int>::integerSubrange(1, p - 1), n, p);
    if (inverses->size() > 0) { result = UmlRsdsLib<int>::min(inverses); }
    else { result = 0; }
    }
    return result;
}


int MathLibrary::modInverse_mutant_1(int n, int p)
{
    int result = 0;
    if (0 >= n || n >= p) { return result; }

    { vector<int>* inverses = reject_2(UmlRsdsLib<int>::integerSubrange(1, p - 1), n, p);
    if (inverses->size() > 0) { result = UmlRsdsLib<int>::min(inverses); }
    else { result = 0; }
    }
    return result;
}


int MathLibrary::modInverse_mutant_2(int n, int p)
{
    int result = 0;
    if (0 >= n || n >= p) { return result; }

    { vector<int>* inverses = select_0(UmlRsdsLib<int>::integerSubrange(1, p - 1), n, p);
    if (inverses->size() > 0) { result = 0; }
    else { result = UmlRsdsLib<int>::min(inverses); }
    }
    return result;
}


void Controller::loadModel()
{
    ifstream infle("in.txt");
    if (infle.fail()) { cout << "No input file!" << endl; return; }
    string* str = new string("");
    vector<string>* res = new vector<string>();
    while (!infle.eof())
    {
        std::getline(infle, *str);
        vector<string>* words = UmlRsdsLib<string>::tokenise(res, *str);
        if (words->size() == 3 && (*words)[1] == ":")  // a : A
        {
            addObjectToClass((*words)[0], (*words)[2]);
        }
        else if (words->size() == 4 && (*words)[1] == ":") // a : b.role
        {
            addObjectToRole((*words)[2], (*words)[0], (*words)[3]);
        }
        else if (words->size() >= 4 && (*words)[2] == "=")  // a.f = val
        {
            int eqind = str->find("=");
            if (eqind < 0) { continue; }
            int f1ind = str->find_first_of("\"");
            int f2ind = str->find_last_of("\"");
            string value;
            if (f1ind != string::npos && f2ind != string::npos)
            {
                value = str->substr(f1ind, f2ind - f1ind + 1);
            }
            else if (words->size() == 4)
            {
                value = (*words)[3];
            }
            else if (words->size() == 5)
            {
                value = (*words)[3] + "." + (*words)[4];
            }
            setObjectFeatureValue((*words)[0], (*words)[1], value);
        }
        res->clear();
    }
}

void Controller::addObjectToClass(string a, string c)
{
    if (c == "MathLibrary")
    {
        MathLibrary* mathlibraryx = new MathLibrary();
        objectmap[a] = mathlibraryx;
        classmap[a] = c;
        addMathLibrary(mathlibraryx);
        return;
    }
}

void Controller::addObjectToRole(string a, string b, string role)
{ }

void Controller::setObjectFeatureValue(string a, string f, string val)
{ }


void Controller::saveModel(string f)
{
    ofstream outfile(f.c_str());
    for (int _i = 0; _i < mathlibrary_s->size(); _i++)
    {
        MathLibrary* mathlibraryx_ = (*mathlibrary_s)[_i];
        outfile << "mathlibraryx_" << (_i + 1) << " : MathLibrary" << endl;
    }


}


int MathLib::ix = 1001;

int MathLib::iy = 781;

int MathLib::iz = 913;

vector<string>* MathLib::hexdigit = UmlRsdsLib<string>::addSequenceString(UmlRsdsLib<string>::addSequenceString(UmlRsdsLib<string>::addSequenceString(UmlRsdsLib<string>::addSequenceString(UmlRsdsLib<string>::addSequenceString(UmlRsdsLib<string>::addSequenceString(UmlRsdsLib<string>::addSequenceString(UmlRsdsLib<string>::addSequenceString(UmlRsdsLib<string>::addSequenceString(UmlRsdsLib<string>::addSequenceString(UmlRsdsLib<string>::addSequenceString(UmlRsdsLib<string>::addSequenceString(UmlRsdsLib<string>::addSequenceString(UmlRsdsLib<string>::addSequenceString(UmlRsdsLib<string>::addSequenceString(UmlRsdsLib<string>::addSequence((new vector<string>()), "0"), "1"), "2"), "3"), "4"), "5"), "6"), "7"), "8"), "9"), "A"), "B"), "C"), "D"), "E"), "F");


double MathLib::pi()
{
    double result = 0.0;
    result = 3.14159265;
    return result;
}


double MathLib::e()
{
    double result = 0.0;
    result = exp(1);
    return result;
}


void MathLib::setSeeds(int x, int y, int z)
{
    MathLib::setix(x);
    MathLib::setiy(y);
    MathLib::setiz(z);
}

double MathLib::nrandom()
{
    MathLib::setix((MathLib::getix() * 171) % 30269);
    MathLib::setiy((MathLib::getiy() * 172) % 30307);
    MathLib::setiz((MathLib::getiz() * 170) % 30323);
    return ((MathLib::getix() / 30269.0) + (MathLib::getiy() / 30307.0) + (MathLib::getiz() / 30323.0));
}


double MathLib::random()
{
    double result = 0.0;
    double r = MathLib::nrandom();
    result = (r - ((int)floor(r)));
    return result;
}

bool MathLib::nextBoolean()
{
    double r = MathLib::random();
    if (r > 0.5)
    {
        return true;
    }
    return false;
}


long MathLib::combinatorial(int n, int m)
{
    long result = 0;
    if (n < m || m < 0) { return result; }
    if (n - m < m)
    {
        result = UmlRsdsLib<int>::prd(UmlRsdsLib<int>::integerSubrange(m + 1, n)) / UmlRsdsLib<int>::prd(UmlRsdsLib<int>::integerSubrange(1, n - m));
    }
    else if (n - m >= m)
    {
        result = UmlRsdsLib<int>::prd(UmlRsdsLib<int>::integerSubrange(n - m + 1, n)) / UmlRsdsLib<int>::prd(UmlRsdsLib<int>::integerSubrange(1, m));
    }
    return result;
}

long MathLib::factorial(int x)
{
    long result = 0;
    if (x < 2)
    {
        result = 1;
    }
    else if (x >= 2)
    {
        result = UmlRsdsLib<int>::prd(UmlRsdsLib<int>::integerSubrange(2, x));
    }
    return result;
}

double MathLib::asinh(double x)
{
    double result = 0.0;
    result = log((x + sqrt((x * x + 1))));
    return result;
}

double MathLib::acosh(double x)
{
    double result = 0.0;
    if (x < 1) { return result; }
    result = log((x + sqrt((x * x - 1))));
    return result;
}

double MathLib::atanh(double x)
{
    double result = 0.0;
    if (x == 1) { return result; }
    result = 0.5 * log(((1 + x) / (1 - x)));
    return result;
}

string MathLib::decimal2bits(long x)
{
    string result = "";
    if (x == 0) { result = ""; }
    else { result = MathLib::decimal2bits(x / 2).append(string("").append(std::to_string((x % 2)))); }
    return result;
}


string MathLib::decimal2binary(long x)
{
    string result = "";
    if (x < 0) { result = string("-").append(MathLib::decimal2bits(-x)); }
    else {
        if (x == 0) { result = "0"; }
        else { result = MathLib::decimal2bits(x); }
    }
    return result;
}


string MathLib::decimal2oct(long x)
{
    string result = "";
    if (x == 0) { result = ""; }
    else { result = MathLib::decimal2oct(x / 8).append(string("").append(std::to_string((x % 8)))); }
    return result;
}


string MathLib::decimal2octal(long x)
{
    string result = "";
    if (x < 0) { result = string("-").append(MathLib::decimal2oct(-x)); }
    else {
        if (x == 0) { result = "0"; }
        else { result = MathLib::decimal2oct(x); }
    }
    return result;
}


string MathLib::decimal2hx(long x)
{
    string result = "";
    if (x == 0) { result = ""; }
    else { result = MathLib::decimal2hx(x / 16).append((string("").append(((string)MathLib::gethexdigit()->at(((int)(x % 16)) + 1 - 1))))); }
    return result;
}


string MathLib::decimal2hex(long x)
{
    string result = "";
    if (x < 0) { result = string("-").append(MathLib::decimal2hx(-x)); }
    else {
        if (x == 0) { result = "0"; }
        else { result = MathLib::decimal2hx(x); }
    }
    return result;
}

int MathLib::bitwiseRotateLeft(int x, int n)
{
    if (n <= 0)
    {
        return x;
    }

    int m = n % 32; 
    int arg1 = (int) (x << m); 
    return arg1 | (x >> (32 - m));
}

int MathLib::bitwiseRotateRight(int x, int n)
{
    if (n <= 0)
    {
        return x;
    }
    int m = n % 32;
    int arg1 = x % ((int) pow(2, m));
    return (arg1 << (32 - m)) | (x >> m);
}


int MathLib::bitwiseAnd(int x, int y)
{
    return x & y;
}

int MathLib::bitwiseOr(int x, int y)
{
    return x | y;
}

int MathLib::bitwiseXor(int x, int y)
{
    return x ^ y;
}

int MathLib::bitwiseNot(int x)
{
    return ~x;
}

long MathLib::bitwiseAnd(long x, long y)
{
    return x & y;
}

long MathLib::bitwiseOr(long x, long y)
{
    return x | y;
}

long MathLib::bitwiseXor(long x, long y)
{
    return x ^ y;
}

long MathLib::bitwiseNot(long x)
{
    return ~x;
}


vector<bool>* MathLib::toBitSequence(long x)
{
    vector<bool>* result;
    long x1;
    x1 = x;
    vector<bool>* res;
    res = (new vector<bool>());
    while (x1 > 0)
    {
        if (x1 % 2 == 0)
        {
            res = UmlRsdsLib<bool>::concatenate(UmlRsdsLib<bool>::makeSequence(false), res);
        }
        else
        {
            res = UmlRsdsLib<bool>::concatenate(UmlRsdsLib<bool>::makeSequence(true), res);
        }
        x1 = x1 / 2;
    }
    return res;
}

long MathLib::modInverse(long n, long p)
{
    long x = (n % p);
    for (int i = 1; i < p; i++)
    {
        if (((i * x) % p) == 1)
        {
            return i;
        }
    }
    return 0;
}

long MathLib::modPow(long n, long m, long p)
{
    long res = 1;
    long x = (n % p);
    for (int i = 1; i <= m; i++)
    {
        res = ((res * x) % p);
    }
    return res;
}

double MathLib::bisectionDiscrete(double r, double rl, double ru,
                                  vector<double>* values)
{
    double result = 0;
    result = 0;
    if ((r <= -1 || rl <= -1 || ru <= -1))
    {
        return result;
    }

    double v = 0;
    v = MathLib::netPresentValueDiscrete(r, values);
    if (ru - rl < 0.001)
    {
        return r;
    }
    if (v > 0)
    {
        return MathLib::bisectionDiscrete((ru + r) / 2, r, ru, values);
    }
    else if (v < 0)
    {
        return MathLib::bisectionDiscrete((r + rl) / 2, rl, r, values);
    }
    return r;
}



template<class T>
bool OclIterator<T>::hasNext()
{
    bool result = false;
    if (position >= 0 && position < elements->size())
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
bool OclIteratorOptimised<T>::hasNext()
{
    bool result = false;
    if (position >= 0 && position < elements.length)
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
bool OclIterator<T>::hasPrevious()
{
    bool result = false;
    if (position > 1 && position <= elements->size() + 1)
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
    if (position > 1 && position <= elements.length)
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
bool OclIterator<T>::isAfterLast()
{
    bool result = false;
    if (position > elements->size())
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
    if (position > elements.length)
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
bool OclIterator<T>::isBeforeFirst()
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
int OclIterator<T>::nextIndex()
{
    int result = 0;
    result = position + 1;
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
int OclIterator<T>::previousIndex()
{
    int result = 0;
    result = position - 1;
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
void OclIterator<T>::moveForward()
{
    int pre_position0 = position;
    //  if (position + 1 > elements->size())) { return; } 
    this->setPosition(pre_position0 + 1);
}

template<class T>
void OclIteratorOptimised<T>::moveForward()
{
    int pre_position0 = position;
    //  if (position + 1 > elements->size())) { return; } 
    this->setPosition(pre_position0 + 1);
}

template<class T>
void OclIterator<T>::moveBackward()
{
    int pre_position1 = position;
    //  if (position <= 1)) { return; } 
    this->setPosition(pre_position1 - 1);
}

template<class T>
void OclIteratorOptimised<T>::moveBackward()
{
    int pre_position1 = position;
    //  if (position <= 1)) { return; } 
    this->setPosition(pre_position1 - 1);
}

template<class T>
void OclIterator<T>::moveTo(int i)
{ //  if ((!(0 <= i) || i > elements->size() + 1))) { return; } 
    this->setPosition(i);
}

template<class T>
void OclIteratorOptimised<T>::moveTo(int i)
{ //  if ((!(0 <= i) || i > elements->size() + 1))) { return; } 
    this->setPosition(i);
}

template<class T>
void OclIterator<T>::movePosition(int i)
{
    int pos = this->getPosition();
    this->setPosition(i + pos);
}

template<class T>
void OclIteratorOptimised<T>::movePosition(int i)
{
    int pos = this->getPosition();
    this->setPosition(i + pos);
}

template<class T>
void OclIterator<T>::moveToStart()
{
    this->setPosition(0);
}

template<class T>
void OclIteratorOptimised<T>::moveToStart()
{
    this->setPosition(0);
}

template<class T>
void OclIterator<T>::moveToEnd()
{
    this->setPosition(this->elements->size() + 1);
}

template<class T>
void OclIteratorOptimised<T>::moveToEnd()
{
    this->setPosition(this->elements.length + 1);
}

template<class T>
void OclIterator<T>::moveToFirst()
{
    this->setPosition(1);
}

template<class T>
void OclIteratorOptimised<T>::moveToFirst()
{
    this->setPosition(1);
}

template<class T>
void OclIterator<T>::moveToLast()
{
    this->setPosition(this->elements->size());
}

template<class T>
void OclIteratorOptimised<T>::moveToLast()
{
    this->setPosition(this->elements.length);
}

template<class T>
OclIterator<T>* OclIterator<T>::newOclIterator_Sequence(vector<T>* sq)
{
    OclIterator<T>* result = NULL;
    OclIterator<T>* ot = new OclIterator<T>();
    ot->setelements(sq);
    ot->setPosition(0);
    ot->markedPosition = 0;
    result = ot;
    return result;
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
    ot->markedPosition = 0;
    return ot;
}

template<class T>
OclIterator<T>* OclIterator<T>::newOclIterator_Set(std::set<T>* st)
{
    OclIterator<T>* ot = new OclIterator<T>();
    vector<T>* elems = UmlRsdsLib<T>::concatenate((new vector<T>()), st);

    ot->elements = UmlRsdsLib<T>::sort(elems);
    ot->setPosition(0);
    ot->markedPosition = 0;
    ot->columnNames = new vector<string>();
    return ot;
} // sort the elements, to account for sorted sets

template<class T>
OclIterator<T>* OclIterator<T>::newOclIterator_String(string st)
{
    OclIterator<string>* res = new OclIterator<string>();
    res->elements = UmlRsdsLib<string>::split(st, "[ \n\t\r]");
    res->position = 0;
    res->markedPosition = 0;
    res->columnNames = new vector<string>();
    return res;
}

template<class T>
OclIterator<T>* OclIterator<T>::newOclIterator_String_String(string st, string sep)
{
    OclIterator<string>* res = new OclIterator<string>();
    res->elements =
        UmlRsdsLib<string>::split(st,
            string("[").append(sep).append(string("]")));
    res->position = 0;
    res->markedPosition = 0;
    res->columnNames = new vector<string>();
    return res;
}


template<class T>
OclIterator<T>* newOclIterator_Function(function<T(int)> f)
{
    OclIterator<T>* res = new OclIterator<T>();
    res->elements = new vector<T>();
    res->generatorFunction = f;
    res->position = 0;
    res->markedPosition = 0;
    res->columnNames = new vector<string>();

    return res;
}


template<class T>
OclIterator<T> OclIterator<T>::trySplit()
{
  vector<T>* firstpart = UmlRsdsLib<T>::subrange(elements, 1, position - 1);
  elements = UmlRsdsLib<T>::subrange(elements, position, elements->size());
  position = 0;
  markedPosition = 0;
  return OclIterator.newOclIterator_Sequence(firstpart);
}


template<class T>
T OclIterator<T>::getCurrent()
{
    T result = NULL;
    if (position < 1 || position > elements->size())
    {
        return result;
    }
    result = ((T)elements->at(position - 1));
    return result;
}

template<class T>
T OclIteratorOptimised<T>::getCurrent()
{
    T result;
    if (position < 1 || position > elements.length)
    {
        return result;
    }
    result = elements[position - 1];
    return result;
}

template<class T>
void OclIterator<T>::set(T x)
{
    (*elements)[position - 1] = x;
}

template<class T>
void OclIteratorOptimised<T>::set(T x)
{
    this->elements[position - 1] = x;
}

template<class T>
void OclIterator<T>::insert(T x)
{
    elements->insert(elements->begin() + (position - 1), x);
}

template<class T>
void OclIterator<T>::remove()
{
    elements->erase(elements->begin() + (position - 1));
}

template<class T>
void OclIteratorOptimised<T>::remove()
{
   this->elements[position - 1] = NULL;
}

template<class T>
OclIteratorResult<T> OclIterator<T>::nextResult()
{
    if (generatorFunction == NULL)
    {
        OclIterator<T>* ocliteratorx = this;
        ocliteratorx->moveForward();
        return OclIteratorResult<T>::newOclIteratorResult(ocliteratorx->getCurrent());
    }
    T r = this.generatorFunction(this->position);
    this->position = this->position + 1;
    if (this->position <= this->elements->size())
    {
        this->set(r);
    }
    else
    {
        this->elements->push_back(r);
    }
    return OclIteratorResult<T>::newOclIteratorResult(r);
}

template<class T>
T OclIterator<T>::next()
{
    OclIterator<T>* ocliteratorx = this;
    ocliteratorx->moveForward();
    return ocliteratorx->getCurrent();
}

template<class T>
T OclIteratorOptimised<T>::next()
{
    this->moveForward();
    return this->getCurrent();
}

template<class T>
bool OclIterator<T>::tryAdvance(function<void*(void*)> f)
{
    if (this->position + 1 <= this->elements->size())
    {
        void* x = this->next();
        f(x);
        return true;
    }
    return false;
}


template<class T>
void OclIterator<T>::forEachRemaining(function<void*(void*)> f)
{
  vector<T>* remainingElements = UmlRsdsLib<T>::subrange(elements, position, elements->size());
  for (int i = 0; i < remainingElements->size(); i++)
  {
      T x = remainingElements->at(i); 
      f(x);
  }
}


template<class T>
T OclIterator<T>::previous()
{
    OclIterator<T>* ocliteratorx = this;
    ocliteratorx->moveBackward();
    return ocliteratorx->getCurrent();
}


template<class T>
T OclIteratorOptimised<T>::previous()
{
    this->moveBackward();
    return this->getCurrent();
}

template<class T>
T OclIterator<T>::at(int i)
{
    return elements->at(i - 1);
}

template<class T>
T OclIteratorOptimised<T>::at(int i)
{
    return elements[i - 1];
}

template<class T>
int OclIterator<T>::length()
{
    return elements->size();
}

template<class T>
int OclIteratorOptimised<T>::length()
{
    return elements.length;
}

template<class T>
void OclIterator<T>::close()
{
    position = 0;
    markedPosition = 0;
    columnNames = new vector<string>();
    elements = new vector<T>();
}

template<class T>
void OclIteratorOptimised<T>::close()
{
    position = 0;
    markedPosition = 0;
    elements = new T[];
}

template<class T>
int OclIterator<T>::getColumnCount()
{
    return columnNames->size();
}

template<class T>
string OclIterator<T>::getColumnName(int i)
{
    if (columnNames->size() >= i && i >= 1)
    {
        return columnNames->at(i - 1);
    }
    return "";
}

template<class T>
void* OclIterator<T>::getCurrentFieldByIndex(int i)
{
    string nme = getColumnName(i);
    T curr = getCurrent();
    map<string, void*> m = (map<string, void*>) curr;
    return m->at(nme);
}

template<class T>
void OclIterator<T>::setCurrentFieldByIndex(int i, void* v)
{
    string nme = getColumnName(i);
    T curr = getCurrent();
    map<string, void*> m = (map<string, void*>) curr;
    m[nme] = v;
}





template< class K, class T>
T OrderedMap<K, T>::getByIndex(int i)
{
    T result = NULL;
    if (i > 0 && i <= elements->size())
    {
        result = ((T)(items)->at(((*elements)[i - 1])));
    }
    return result;
}


template< class K, class T>
T OrderedMap<K, T>::getByKey(K k)
{
    T result = NULL;
    result = ((T)(items)->at(k));
    return result;
}


template< class K, class T>
void OrderedMap<K, T>::add(K k, T t)
{
    OrderedMap* orderedmapx = this;
    elements = UmlRsdsLib<K>::concatenate(orderedmapx->getelements(), UmlRsdsLib<K>::makeSequence(k));
    items[k] = t;

}


template< class K, class T>
void OrderedMap<K, T>::remove(int i)
{
    OrderedMap* orderedmapx = this;
    K k = ((*elements)[i - 1]);

    elements = UmlRsdsLib<K>::removeAt(orderedmapx->getelements(), i);
    items = UmlRsdsOcl<K, T, T>::antirestrict(orderedmapx->getitems(), UmlRsdsLib<K>::addSet((new set<K>()), k));
}

string StringLib::format(string f, vector<void*>* sq)
{
    int n = sq->size();
    int flength = f.length();
    if (n == 0 || flength == 0)
    {
        return f;
    }

    const char* format = f.c_str();
    void* ap;
    char* p = (char*)format;
    int i = 0;

    char* sval;
    int ival;
    unsigned int uval;
    double dval;

    ostringstream buff;
    ap = sq->at(0);

    for (; *p != '\0'; p++)
    {
        if (*p != '%')
        {
            buff << *p;
            continue;
        }

        char* tmp = (char*)calloc(flength + 1, sizeof(char));
        tmp[0] = '%';
        int k = 1;
        p++;

        while (*p != 'i' && *p != 'd' && *p != 'g' && *p != 'e' &&
            *p != 'o' && *p != 'x' && *p != 'X' && *p != 'E' &&
            *p != 'G' && *p != 's' && *p != '%' &&
            *p != 'u' && *p != 'c' && *p != 'f' && *p != 'p')
        {
            tmp[k] = *p;
            k++;
            p++;
        }  /* Now p points to flag after % */

        tmp[k] = *p;
        tmp[k + 1] = '\0';

        if (i >= n)
        {
            continue;
        }

        switch (*p)
        {
        case 'i':
            ival = *((int*)sq->at(i));
            i++;
            { char* ibuff0 = (char*)calloc(int(log10(abs(ival) + 1)) + 2, sizeof(char));
            sprintf(ibuff0, tmp, ival);
            buff << ibuff0;
            }
            break;
        case 'o':
        case 'x':
        case 'X':
        case 'u':
            uval = *((unsigned int*)sq->at(i));
            i++;
            { char* ubuff = (char*)calloc(int(log10(uval + 1)) + 2, sizeof(char));
            sprintf(ubuff, tmp, uval);
            buff << ubuff;
            }
            break;
        case 'c':
        case 'd':
            ival = *((int*)sq->at(i));

            i++;
            { char* ibuff1 = (char*)calloc(int(log10(abs(ival) + 1)) + 2, sizeof(char));
            sprintf(ibuff1, tmp, ival);
            buff << ibuff1;
            }
            break;
        case 'f':
        case 'e':
        case 'E':
        case 'g':
        case 'G':
            dval = *((double*)sq->at(i));
            i++;
            { char* dbuff = (char*)calloc(int(log10(fabs(dval) + 1)) + 2, sizeof(char));
            sprintf(dbuff, tmp, dval);
            buff << dbuff;
            }
            break;
        case 's':
            sval = ((char*)sq->at(i));

            i++;
            { // int sn = strlen(sval) + 1;
              // char sbuff[sn];
              // sprintf(sbuff, tmp, sval);
                buff << sval;
            }
            break;
        case 'p':
            i++;
            { char* pbuff = (char*)calloc(9, sizeof(char));
            sprintf(pbuff, tmp, sq->at(i));
            buff << pbuff;
            }
            break;
        default:
            buff << *p;
            break;
        }
    }
    string res = buff.str();

    return res;
}



void SustainabilityTest::op()
{
   SustainabilityTest* sustainabilitytestx = this;
   int addcount = 40000;

   int checkcount = 0;

   std::chrono::time_point<std::chrono::high_resolution_clock> t1 = std::chrono::high_resolution_clock::now();

   while (addcount > 0)
   {
      elems->push_back(addcount);
      addcount = addcount - 1;
   }

   std::chrono::time_point<std::chrono::high_resolution_clock> t2 = std::chrono::high_resolution_clock::now();
   cout << (t2 - t1) / 1ms << " for " << elems->size() << endl;

   // std::chrono::time_point<std::chrono::high_resolution_clock> t1 = std::chrono::high_resolution_clock::now();

   while (checkcount > 0)
   {
     bool b = UmlRsdsLib<int>::isIn(checkcount, elems);

     checkcount = checkcount - 1;
   }

   // std::chrono::time_point<std::chrono::high_resolution_clock> t2 = std::chrono::high_resolution_clock::now();
   // cout << (t2 - t1) / 1ms << " for " << elems->size() << endl;

}


int main(int argc, char* argv[])
{ // OclFile::newOclFile("System.in");
  // OclFile::newOclFile("System.out");
  // OclFile::newOclFile("System.err");

    /* 
    OclType* intType = OclType::createOclType("int");
    intType->setname(typeid(1).name());
    OclType* longType = OclType::createOclType("long");
    longType->setname(typeid(0L).name());
    OclType* doubleType = OclType::createOclType("double");
    doubleType->setname(typeid(1.0).name());
    OclType* booleanType = OclType::createOclType("boolean");
    booleanType->setname(typeid(true).name());
    OclType* stringType = OclType::createOclType("String");
    stringType->setname(typeid(string("")).name());
    OclType* sequenceType = OclType::createOclType("Sequence");
    sequenceType->setname(typeid(vector<void*>()).name());
    OclType* setType = OclType::createOclType("Set");
    setType->setname(typeid(set<void*>()).name());
    OclType* voidType = OclType::createOclType("void");
    voidType->setname("void");
    OclType* oclanyType = OclType::createOclType("OclAny");
    oclanyType->setname("void *");
    OclType* oclfileType = OclType::createOclType("OclFile");
    oclfileType->setname("class OclFile *");

    OclType* mathlibraryType = OclType::createOclType("MathLibrary");
    mathlibraryType->setname("class MathLibrary *");
    OclOperation* asinh_MathLibraryOperation = new OclOperation();
    asinh_MathLibraryOperation->setname("asinh");
    mathlibraryType->addoperations(asinh_MathLibraryOperation);
    OclOperation* modInverse_MathLibraryOperation = new OclOperation();
    modInverse_MathLibraryOperation->setname("modInverse");
    mathlibraryType->addoperations(modInverse_MathLibraryOperation);
    OclOperation* asinh_mutant_0_MathLibraryOperation = new OclOperation();
    asinh_mutant_0_MathLibraryOperation->setname("asinh_mutant_0");
    mathlibraryType->addoperations(asinh_mutant_0_MathLibraryOperation);
    OclOperation* asinh_mutant_1_MathLibraryOperation = new OclOperation();
    asinh_mutant_1_MathLibraryOperation->setname("asinh_mutant_1");
    mathlibraryType->addoperations(asinh_mutant_1_MathLibraryOperation);
    OclOperation* asinh_mutant_2_MathLibraryOperation = new OclOperation();
    asinh_mutant_2_MathLibraryOperation->setname("asinh_mutant_2");
    mathlibraryType->addoperations(asinh_mutant_2_MathLibraryOperation);
    OclOperation* asinh_mutant_3_MathLibraryOperation = new OclOperation();
    asinh_mutant_3_MathLibraryOperation->setname("asinh_mutant_3");
    mathlibraryType->addoperations(asinh_mutant_3_MathLibraryOperation);
    OclOperation* modInverse_mutant_0_MathLibraryOperation = new OclOperation();
    modInverse_mutant_0_MathLibraryOperation->setname("modInverse_mutant_0");
    mathlibraryType->addoperations(modInverse_mutant_0_MathLibraryOperation);
    OclOperation* modInverse_mutant_1_MathLibraryOperation = new OclOperation();
    modInverse_mutant_1_MathLibraryOperation->setname("modInverse_mutant_1");
    mathlibraryType->addoperations(modInverse_mutant_1_MathLibraryOperation);
    OclOperation* modInverse_mutant_2_MathLibraryOperation = new OclOperation();
    modInverse_mutant_2_MathLibraryOperation->setname("modInverse_mutant_2");
    mathlibraryType->addoperations(modInverse_mutant_2_MathLibraryOperation);

    /* MathLibrary* mm = new MathLibrary(); 
    int _counts[100]; 
    int _totals[100]; 
    MutationTest::asinh_mutation_tests(mm, _counts, _totals); */ 

    /* 
    int _cnts[10] = { 0 };
    _cnts[5]++; 

    cout << _cnts[5] << endl; 


    TestsGUI* tt = new TestsGUI(); 
    tt->runTests(); 
    vector<double>* vv = new vector<double>(); 
    vv->push_back(-100); 
    vv->push_back(2.0); 
    vv->push_back(102.0); 

    cout << MathLib::discountDiscrete(100, 0.1, 5) << endl; 
    cout << MathLib::netPresentValueDiscrete(0.01, vv) << endl; 
    cout << MathLib::irrDiscrete(vv) << endl; */ 

    /* OclDate* dd = OclDate::newOclDate_String("2022/01/30 09:30:45"); 
    cout << dd->toString() << endl; 
    OclDate* nn = OclDate::newOclDate(); 
    cout << nn << endl; 
    
    cout << nn->toString() << endl;
    if (dd < nn)
    {
        cout << dd << " is smaller than " << nn << endl;
    }  

    cout << MathLib::truncateN(12.345, 2) << endl; 
    cout << MathLib::roundN(12.345, 2) << endl;

    cout << MathLib::lcm(12, 18) << endl; 
    cout << MathLib::isIntegerOverflow(345, 2) << endl;

    cout << MathLib::isIntegerOverflow(45, 2) << endl;
    
    vector<double>* vv = new vector<double>(); 
    vv->push_back(1); vv->push_back(3); vv->push_back(5); vv->push_back(10);

    cout << MathLib::mean(vv) << endl; 
    cout << MathLib::median(vv) << endl;
    cout << MathLib::variance(vv) << endl;
    cout << MathLib::standardDeviation(vv) << endl;

    cout << MathLib::bisectionAsc(0, -2, 2, [=](double x) -> double { return (2 - (x * x));  }, 0.001) << endl; 

    vector<vector<double>*>* m1 = new vector<vector<double>*>(); 
    vector<double>* row1 = new vector<double>(); 
    row1->push_back(1); row1->push_back(3);
    vector<double>* row2 = new vector<double>();
    row2->push_back(7); row2->push_back(5);
    m1->push_back(row1); m1->push_back(row2); 
    // [[1, 3], [7, 5]]
    // m2 = [[6, 8], [4, 2]]
    vector<vector<double>*>* m2 = new vector<vector<double>*>();
    vector<double>* row3 = new vector<double>();
    row3->push_back(6); row3->push_back(8);
    vector<double>* row4 = new vector<double>();
    row4->push_back(4); row4->push_back(2);
    m2->push_back(row3); m2->push_back(row4);
    vector<vector<double>*>* rr = MathLib::matrixMultiplication(m1, m2); 
    cout << rr->at(0)->at(0) << " " << rr->at(0)->at(1) << endl;
    cout << rr->at(1)->at(0) << " " << rr->at(1)->at(1) << endl;

    cout << StringLib::leftTrim("  a long string  ") << "|" << endl;
    cout << StringLib::rightTrim("  a long string  ") << "|" << endl;
    cout << StringLib::padLeftWithInto(" a long ", "*", 12) << "|" << endl;
    cout << StringLib::leftAlignInto(" a long ", 12) << "|" << endl;
    cout << StringLib::rightAlignInto(" a long ", 12) << "|" << endl; */ 

  /*
    std::map<string,string>* ssr = new std::map<string,string>(); 
    (*ssr)["aa"] = "1"; (*ssr)["dd"] = "2"; (*ssr)["bb"] = "3"; (*ssr)["cc"] = "4";

    std::map<string,string>* sstail = UmlRsdsLib<string>::front(ssr); 

    for (auto iter = sstail->begin(); iter != sstail->end(); iter++)
    {
        cout << iter->first << " |-> " << iter->second << endl;
    }

    cout << endl;

    // cout << UmlRsdsLib<string>::first(ssr) << endl; 
    // cout << UmlRsdsLib<string>::last(ssr) << endl; 
 
    cout << UmlRsdsLib<string>::collectionToString(ssr) << endl; 

    cout << UmlRsdsLib<string>::excludingSubrange("a long string", 3, 6) << endl; */

    // SustainabilityTest* tt = new SustainabilityTest(); 

    // long long t1 = UmlRsdsLib<long>::getTime(); 
    // std::chrono::time_point<std::chrono::high_resolution_clock> t1 = std::chrono::high_resolution_clock::now();
    // tt->op(); 

    // long long t2 = UmlRsdsLib<long>::getTime();

    // std::chrono::time_point<std::chrono::high_resolution_clock> t2 = std::chrono::high_resolution_clock::now();
    // cout << (t2 - t1)/1ms << " for " << tt->getelems()->size() << endl;

    // cout << MathLib::bitwiseRotateLeft(5678887999, 2) << endl;

    // cout << MathLib::bitwiseRotateRight(10, 2) << endl;

    // cout << MathLib::bitwiseRotateRight(1000000, 2) << endl;

    // map<string, string>* mp = new map<string, string>(); 
    // (*mp)["aa"] = "1"; 
    // (*mp)["bb"] = "2"; 

    // cout << UmlRsdsLib<string>::includesKey(mp, "bb") << endl; 
    // cout << UmlRsdsLib<string>::includesKey(mp, "cc") << endl;

    // cout << UmlRsdsLib<string>::includesValue(mp, "3") << endl;
    // cout << UmlRsdsLib<string>::includesValue(mp, "1") << endl;

    /* SortedSequence<int>* ss = new SortedSequence<int>();
    ss->including(4); ss->including(1); ss->including(3); ss->including(2); 
    ss->including(3); ss->including(3); 
    std::ostream_iterator<int> output(cout, " "); 
    std::copy(ss->begin(), ss->end(), output); 
    cout << endl; 
    // cout << ss->includes(3) << " " << ss->includes(10) << endl; 
    // cout << ss->min() << " " << ss->max() << endl; 

    // function<bool(int)> f = [=](int x) -> bool { return x * x > 5;  }; 

    // SortedSequence<int>* subsq = ss->reject(f); 
    function<int(int)> g = [=](int x) -> int { return x * x; }; 
    vector<int>* qq = ss->collect(g); 
    std::copy(qq->begin(), qq->end(), output);
    cout << endl;

    // ss->excluding(3); 
    // std::copy(ss->begin(), ss->end(), output);
    // cout << endl; 

    SortedSequence<int>* vv = new SortedSequence<int>(); 
    vv->including(1); vv->including(3); vv->including(3); 

    ss->unionSortedSequence(vv); 

    std::copy(ss->begin(), ss->end(), output);
    cout << endl;

    /* 
    std::set<int>* st = new std::set<int>(); 
    st->insert(3); st->insert(1); 

    ss->subtract(st); 
    std::copy(ss->begin(), ss->end(), output);
    cout << endl;

    ss->intersection(vv); 
    std::copy(ss->begin(), ss->end(), output);
    cout << endl << "**************************************" << endl;

    SortedOrderedSet<int>* oss = new SortedOrderedSet<int>();
    oss->including(4); oss->including(1); oss->including(3); oss->including(2);
    oss->including(3); oss->including(3);

    std::ostream_iterator<int> ooutput(cout, " ");
    std::copy(oss->begin(), oss->end(), ooutput);
    cout << endl;
    cout << oss->includes(3) << " " << oss->includes(10) << endl; 
    cout << oss->min() << " " << oss->max() << endl; 

    function<bool(int)> ff = [=](int x) -> bool { return x * x > 5;  }; 

    SortedOrderedSet<int>* osubsq = oss->select(ff); 
    std::copy(osubsq->begin(), osubsq->end(), ooutput);
    cout << endl;
    
    function<int(int)> gg = [=](int x) -> int { return x * x; };
    vector<int>* oqq = oss->collect(gg);
    std::copy(oqq->begin(), oqq->end(), ooutput);
    cout << endl;

    oss->excluding(3);
    std::copy(oss->begin(), oss->end(), ooutput);
    cout << endl;

    vector<int>* ovv = new vector<int>();
    ovv->push_back(1); ovv->push_back(5); ovv->push_back(7);

    oss->unionSortedOrderedSet(ovv);

    std::copy(oss->begin(), oss->end(), ooutput);
    cout << endl;

    std::set<int>* ost = new std::set<int>();
    ost->insert(3); ost->insert(2); ost->insert(4); 
    oss->intersection(ost); 
    // SortedOrderedSet<int>* fos = oss->front();
    std::copy(oss->begin(), oss->end(), ooutput);
    cout << endl;
    */ 

   vector<int>* lst = new vector<int>(); 
   for (int i = 0; i < 100000; i++)
   { lst->push_back(i); }

    OclIterator<int>* iter1 = OclIterator<int>::newOclIterator_Sequence(lst);
    // long long t1 = UmlRsdsLib<long>::getTime(); 
    std::chrono::time_point<std::chrono::high_resolution_clock> t1 = std::chrono::high_resolution_clock::now();
    
    for (int j = 0; j < 100000; j++)
    { iter1->next();
      iter1->set(1);
    }

    // long long t2 = UmlRsdsLib<long>::getTime();
    std::chrono::time_point<std::chrono::high_resolution_clock> t2 = std::chrono::high_resolution_clock::now();

    cout << (t2 - t1)/1ms << endl; 

    return 0;
}


// Run program: Ctrl + F5 or Debug > Start Without Debugging menu
// Debug program: F5 or Debug > Start Debugging menu

// Tips for Getting Started: 
//   1. Use the Solution Explorer window to add/manage files
//   2. Use the Team Explorer window to connect to source control
//   3. Use the Output window to see build output and other messages
//   4. Use the Error List window to view errors
//   5. Go to Project > Add New Item to create new code files, or Project > Add Existing Item to add existing code files to the project
//   6. In the future, to open this project again, go to File > Open > Project and select the .sln file
