import java.io.*; 
import java.util.Vector; 

/* Package: Requirements */ 
/******************************
* Copyright (c) 2003--2024 Kevin Lano
* This program and the accompanying materials are made available under the
* terms of the Eclipse Public License 2.0 which is available at
* http://www.eclipse.org/legal/epl-2.0
*
* SPDX-License-Identifier: EPL-2.0
* *****************************/

public class Scenario extends ModelElement
{ public String informalDescription; 
  public Sbvrse semiformalDescription; 
  public Constraint formalDescription; 

  public Scenario(String nme)
  { super(nme); }

  public void setText(String txt)
  { informalDescription = txt; } 

  public void setSbvrse(Sbvrse txt)
  { semiformalDescription = txt; } 

  public void setConstraint(Constraint con)
  { formalDescription = con; } 

  public Vector getParameters()
  { return new Vector(); } 

  public void addParameter(Attribute att)
  { } 

  public Type getType()
  { return null; } 

  public void setType(Type t)
  { } 

  public String toString() 
  { return "Scenario: " + name + "\n" + 
           informalDescription + "\n" + 
           semiformalDescription + "\n" + 
           formalDescription + "\n"; 
  } 

  public void generateJava(PrintWriter out) { } 

  public Constraint getConstraint(Vector types, Vector entities)
  { if (formalDescription == null) 
    { if (semiformalDescription != null) 
      { formalDescription = semiformalDescription.generateConstraint(types, entities); } 
    } 
    return formalDescription; 
  } 

  public String saveData(String req)
  { return "Scenario:\n" + 
           name + " " + req + "\n" + 
           informalDescription + "\n" + 
           semiformalDescription + "\n" + 
           formalDescription + "\n"; 
  }  

  public void saveModelData(PrintWriter out, String nme) 
  { String id = getName(); 
    out.println(id + " : Scenario"); 
    out.println(id + " : " + nme + ".scenarios"); 

    if (formalDescription != null) 
    { String preid = formalDescription.saveModelData(out);
      out.println(preid + " : " + nme + ".formalDescription");  
    }  
  } 
} 