template<class S, class T, class R>
  class UmlRsdsOcl {
  public:

  static T iterate(vector<S>* _s, T initialValue, std::function<T(S,T)> _f)
  { T acc = initialValue; 
    for (auto _pos = _s->begin(); _pos != _s->end(); ++_pos) 
    { S _x = *_pos;
      acc = _f(_x,acc); 
    }
    return acc; 
  } 

     static map<S,T>* includingMap(map<S,T>* m, S src, T trg) 
   { map<S,T>* copy = new map<S,T>(); 
     auto iter = m->begin(); 
     for ( ; iter != m->end(); ++iter) 
     { S key = iter->first; 
       (*copy)[key] = iter->second; 
     }     
     (*copy)[src] = trg; 
     return copy; 
   } 


  static map<S,T>* unionMap(map<S,T>* m1, map<S,T>* m2)  
  { map<S,T>* res = new map<S,T>(); 
    auto iter = m1->begin(); 
    for ( ; iter != m1->end(); ++iter) 
    { S key = iter->first; 
      if (m2->count(key) == 0) 
      { (*res)[key] = iter->second; } 
    }     
    for (iter = m2->begin(); iter != m2->end(); ++iter) 
    { S key = iter->first; 
      (*res)[key] = iter->second; 
    }     
    return res; 
  } 

  
  static bool includesAllMap(map<S,T>* sup, map<S,T>* sub) 
  { auto iter = sub->begin(); 
    for ( ; iter != sub->end(); ++iter) 
    { S key = iter->first; 
      auto f = sup->find(key); 
      if (f != sup->end())  
      { if (iter->second == f->second) {} 
        else 
        { return false; } 
      } 
      else  
      { return false; } 
    }     
    return true; 
  } 


  static bool excludesAllMap(map<S,T>*  sup, map<S,T>* sub) 
  { auto iter = sub->begin(); 
    for ( ; iter != sub->end(); ++iter) 
    { S key = iter->first; 
      auto f = sup->find(key); 
      if (f != sup->end())  
      { if (iter->second == f->second) 
        { return false; } 
      } 
    }     
    return true; 
  } 


   static map<S,T>* excludeAllMap(map<S,T>* m1, map<S,T>* m2) 
   { map<S,T>* res = new map<S,T>(); 
     auto iter = m1->begin(); 
     for ( ; iter != m1->end(); ++iter) 
     { S key = iter->first; 
       auto f = m2->find(key); 
       if (f != m2->end())  
       { if (iter->second == f->second)  {  } 
         else  
  	   { (*res)[key] = iter->second; } 
       } 
       else  
       { (*res)[key] = iter->second; } 
    }     
    return res; 
  }   


  static map<S,T>* excludingMapKey(map<S,T>* m, string k) 
  { // m - { k |-> m(k) }  
    map<S,T>* res = new map<S,T>(); 
    auto iter = m->begin(); 
    for ( ; iter != m->end(); ++iter) 
    { S key = iter->first; 
      if (key == k) {} 
      else       
      { (*res)[key] = iter->second; } 
    }     
    return res; 
  } 


  static map<S,T>* excludingMapValue(map<S,T>* m, T v) 
  { // m - { k |-> v }  
    map<S,T>* res = new map<S,T>(); 
    auto iter = m->begin(); 
    for ( ; iter != m->end(); ++iter) 
    { S key = iter->first; 
      if (iter->second == v) {} 
      else       
      { (*res)[key] = iter->second; } 
    }     
    return res; 
  } 




  static map<S,T>* intersectionMap(map<S,T>* m1, map<S,T>* m2) 
  { map<S,T>* res = new map<S,T>(); 
    auto iter = m1->begin(); 
    for ( ; iter != m1->end(); ++iter) 
    { S key = iter->first; 
      if (m2->count(key) > 0) 
      { if (m2->at(key) == iter->second) 
        { (*res)[key] = iter->second; } 
      } 
    }     
    return res; 
  } 

  static std::set<S>* keys(map<S,T>* s)
  { auto iter = s->begin();
    std::set<S>* res = new std::set<S>();
  
    for ( ; iter != s->end(); ++iter)
    { S key = iter->first;
      res->insert(key);
    }    
    return res;
  }


  static vector<T>* values(map<S,T>* s)
  { auto iter = s->begin();
    vector<T>* res = new vector<T>();
  
    for ( ; iter != s->end(); ++iter)
    { T value = iter->second;
      res->push_back(value);
    }    
    return res;
  }


  static map<S,T>* restrict(map<S,T>* m1, std::set<S>* ks)
  { map<S,T>* res = new map<S,T>();
    auto iter = m1->begin();
    for ( ; iter != m1->end(); ++iter)
    { S key = iter->first;
      if (ks->find(key) != ks->end())
      { (*res)[key] = iter->second; }
    }    
    return res;
  }

  static map<S,T>* antirestrict(map<S,T>* m1, std::set<S>* ks)
  { map<S,T>* res = new map<S,T>();
    auto iter = m1->begin();
    for ( ; iter != m1->end(); ++iter)
    { S key = iter->first;
      if (ks->find(key) == ks->end())
      { (*res)[key] = iter->second; }
    }    
    return res;
  }

    static map<S,T>* selectMap(map<S,T>* m1, std::function<bool(T)> f)
  { map<S,T>* res = new map<S,T>();
    auto iter = m1->begin();
    for ( ; iter != m1->end(); ++iter)
    { S key = iter->first;
      T val = iter->second;
	  if (f(val))
      { (*res)[key] = val; }
    }    
    return res;
  }

   static map<S,T>* rejectMap(map<S,T>* m1, std::function<bool(T)> f)
  { map<S,T>* res = new map<S,T>();
    auto iter = m1->begin();
    for ( ; iter != m1->end(); ++iter)
    { S key = iter->first;
      T val = iter->second;
	  if (f(val)) { } 
	  else 
      { (*res)[key] = val; }
    }    
    return res;
  }

  static map<S,R>* collectMap(map<S,T>* m1, 
                              std::function<R(T)> f)
  { map<S,R>* res = new map<S,R>();
    auto iter = m1->begin();
    for ( ; iter != m1->end(); ++iter)
    { S key = iter->first;
      T val = iter->second;
	  
      (*res)[key] = f(val); 
    }    
    return res;
  }


  };


