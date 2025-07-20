import java.util.*;
import java.util.HashMap;
import java.util.Collection;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import java.util.TreeSet;
import java.util.Collections;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Constructor;

class OclOperation 
{ OclOperation() { }

  OclOperation(String nme) 
  { name = nme; }


  String name = "";
  OclType type = null;
  ArrayList<OclAttribute> parameters = new ArrayList<OclAttribute>();
  Method actualMethod = null; 

  public String getName()
  { return name; }

  public void setType(OclType t)
  { type = t; } 

  public OclType getType()
  { return type; }


  public OclType getReturnType()
  { return type; }


  public ArrayList<OclAttribute> getParameters()
  { return parameters; } 

  public void invoke(Object obj, ArrayList<Object> pars)
  { if (obj == null || actualMethod == null) 
    { return; } 

    Object[] vals = new Object[pars.size()]; 
    for (int i = 0; i < pars.size(); i++) 
    { vals[i] = pars.get(i); }
 
    try { actualMethod.invoke(obj, vals); } 
    catch (Exception ex) { } 
  } 

}

