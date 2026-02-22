import java.util.HashMap;
import java.util.Vector;  

/******************************
* Copyright (c) 2003--2026 Kevin Lano
* This program and the accompanying materials are made available under the
* terms of the Eclipse Public License 2.0 which is available at
* http://www.eclipse.org/legal/epl-2.0
*
* SPDX-License-Identifier: EPL-2.0
* *****************************/
/* Package: Model Data */ 

public class ModelState
{ Vector variableValues = new Vector(); 
  // ordered list of environments HashMap<String,Expression>
  // The most-local environments are further down the list

  public Object clone()
  { ModelState res = new ModelState(); 
    res.variableValues = (Vector) variableValues.clone(); 
    return res; 
  } 

  public String toString()
  { String res = ""; 
    for (int i = 0; i < variableValues.size(); i++) 
    { java.util.HashMap env = 
           (java.util.HashMap) variableValues.get(i); 
      res = res + env + " "; 
    } 
    return res; 
  }   

  public void addNewEnvironment()
  { java.util.HashMap env = new java.util.HashMap(); 
    variableValues.add(env); 
  } 

  public void removeLastEnvironment()
  { int n = variableValues.size(); 
    if (n > 0)
    { variableValues.remove(n-1); } 
  } 

  public java.util.HashMap addVariable(String var, Expression expr)
  { int n = variableValues.size(); 

    if (n > 0)
    { java.util.HashMap env = 
           (java.util.HashMap) variableValues.get(n-1); 
      env.put(var,expr); 
      return env; 
    } 

    return null; 
  } // else - error

  public java.util.HashMap addVariable(
            ModelSpecification sigma, 
            String var, Expression expr)
  { String pid = Identifier.nextIdentifier("&_"); 
    return addVariable(sigma, var, pid, expr); 
  } 

  public java.util.HashMap addVariable(
            ModelSpecification sigma, 
            String var, String pid, Expression expr)
  { int n = variableValues.size(); 

    if (n > 0)
    { java.util.HashMap env = 
           (java.util.HashMap) variableValues.get(n-1); 
      env.put(var, new BasicExpression(pid));
      sigma.setMemoryValue(pid, expr);  
      return env; 
    } 

    return null; 
  } // else - error

  public void 
     setVariableValue(String var, Expression expr)
  { Expression res = null; 
    int n = variableValues.size(); 

    for (int i = n-1; i >= 0; i--) 
    { java.util.HashMap env = 
          (java.util.HashMap) variableValues.get(i); 

      Expression ee = (Expression) env.get(var); 
      if (ee != null) 
      { env.put(var, expr); } 
    } 
  }         

  public void 
     setVariableValue(ModelSpecification sigma, 
                      String var, Expression expr)
  { Expression ref = getVariableValue(var); 
    if (ref != null) 
    { sigma.setMemoryValue(ref + "", expr); } 
  }         

  public Expression getVariableValue(String var)
  { Expression res = null; 
    int n = variableValues.size(); 

    for (int i = n-1; i >= 0; i--) 
    { java.util.HashMap env = 
          (java.util.HashMap) variableValues.get(i); 

      Expression expr = (Expression) env.get(var); 
      if (expr != null) 
      { return expr; } 
    } 

    return res; 
  }         

  public Expression 
     getVariableValue(ModelSpecification sigma, String var)
  { Expression ref = getVariableValue(var); 
    if (ref == null) 
    { return null; } 

    return sigma.getMemoryValue(ref + ""); 
  } // get the address then look up address in memory        

  public java.util.HashMap 
                   getEnvironment(String var)
  { java.util.HashMap res = null; 
    int n = variableValues.size(); 

    for (int i = n-1; i >= 0; i--) 
    { java.util.HashMap env = 
          (java.util.HashMap) variableValues.get(i); 

      Expression expr = (Expression) env.get(var); 
      if (expr != null) 
      { return env; } 
    } 

    return res; 
  }         

  public void updateState(ModelSpecification sigma, 
                          Expression lhs, 
                          Expression rhsValue)
  { // updates lhs with rhsValue

    if (lhs instanceof BasicExpression)
    { BasicExpression lbe = (BasicExpression) lhs;
      Expression obj = lbe.getObjectRef(); 
      Expression indx = lbe.getArrayIndex(); 
      String var = lbe.getData(); 

      // System.out.println("LHS: " + obj + "." + var + indx + " " + lhs.isAttribute() + " " + beta); 
      
      if (obj == null && 
          indx == null)
      { // simple variable or attribute

        if (lhs.isAttribute()) // of "self"
        { Expression oid = 
                 this.getVariableValue(sigma, "self"); 
          ObjectSpecification ref = 
                sigma.getObjectSpec("" + oid); 
          if (ref != null)
          { ref.setOCLValue(var, rhsValue); }
        }   
        else 
        { this.setVariableValue(sigma, var, rhsValue); } 
      } 
      else if (obj == null)
      { // simple array variable 
        Expression indv = indx.evaluate(sigma, this); 
        Expression arr = this.getVariableValue(sigma, var); 

        if (arr instanceof SetExpression)
        { int indval = Integer.parseInt("" + indv); 
          ((SetExpression) arr).setExpression(indval, rhsValue); 
        } 
      }  
      else if (obj != null && 
               indx == null)
      { // object attribute
        Expression oid = obj.evaluate(sigma, this); 
        ObjectSpecification ref = sigma.getObjectSpec("" + oid); 
        if (ref != null)
        { ref.setOCLValue(var, rhsValue); }   
      } 
    } 
  } 

  public static void main(String[] args)
  { ModelState ms = new ModelState(); 
    ms.addNewEnvironment(); 
    ms.addVariable("x", new BasicExpression(2)); 
    ms.addNewEnvironment(); 
    ms.addVariable("x", new BasicExpression(1)); 
    System.out.println(ms.getVariableValue("x")); 
  } 
}

