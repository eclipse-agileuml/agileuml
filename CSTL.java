import java.io.*;
import javax.swing.*;

import java.util.Vector; 

/******************************
* Copyright (c) 2003--2025 Kevin Lano
* This program and the accompanying materials are made available under the
* terms of the Eclipse Public License 2.0 which is available at
* http://www.eclipse.org/legal/epl-2.0
*
* SPDX-License-Identifier: EPL-2.0
* *****************************/
/* package: CSTL */ 


public class CSTL
{ // All *.cstl files in output directory are loaded

  static boolean isCSTLVariable(String lex)
  { if ("_*".equals(lex) || "_+".equals(lex) || 
        "_$".equals(lex)) 
    { return true; } 
    for (int i = 0; i <= 99; i++) 
    { if (("_" + i).equals(lex))
      { return true; } 
    } 
    return false; 
  } 

  static boolean isMathMetavariable(String lex)
  { if (lex.startsWith("_") && lex.length() > 1) 
    { return true; } 
    return false; 
  } 

  static boolean isInbuiltFunction(String mf)
  { if ("hashCode".equals(mf)) { return true; } 
    if ("trimQuotes".equals(mf)) { return true; } 
    if ("type".equals(mf)) { return true; } 
    if ("first".equals(mf)) { return true; } 
    if ("second".equals(mf)) { return true; } 
    if ("third".equals(mf)) { return true; }  
    if ("fourth".equals(mf)) { return true; } 
    if ("fifth".equals(mf)) { return true; } 
    if ("sixth".equals(mf)) { return true; } 
    if ("seventh".equals(mf)) { return true; } 
    if ("last".equals(mf)) { return true; } 
    if ("tail".equals(mf)) { return true; } 
    if ("tail2".equals(mf)) { return true; } 
    if ("tail3".equals(mf)) { return true; } 
    if ("tail4".equals(mf)) { return true; } 
    if ("front".equals(mf)) { return true; } 
    if ("toInteger".equals(mf)) { return true; } 
    return false; 
  } 

  static java.util.Map templates = new java.util.HashMap();

  public static void loadTemplates(Vector types, Vector entities)
  { File dir = new File("./cg");
    String[] dirfiles = dir.list();
    for (int i = 0; i < dirfiles.length; i++)
    { File sub = new File("./cg/" + dirfiles[i]);
      if (sub != null && dirfiles[i].endsWith(".cstl") && 
          !(dirfiles[i].equals("cg.cstl")))
      { System.err.println(">>> Found CSTL template: " + dirfiles[i]); 
        CGSpec cg = new CGSpec(entities,types); 
        CGSpec res = loadCSTL(cg, sub,types,entities); 
        if (res != null) 
        { addTemplate(dirfiles[i], res); }         
      }
    }
  }

  public static void loadTemplates(Vector types, Vector entities, String excluding)
  { File dir = new File("./cg");
    String[] dirfiles = dir.list();
    if (dirfiles == null) { return; }
    for (int i = 0; i < dirfiles.length; i++)
    { File sub = new File("./cg/" + dirfiles[i]);
      if (sub != null && dirfiles[i].endsWith(".cstl") && 
          !(dirfiles[i].equals(excluding)))
      { System.err.println(">>> Loading CSTL template: " + dirfiles[i]); 
        CGSpec cg = new CGSpec(entities,types); 
        CGSpec res = loadCSTL(cg, sub,types,entities); 
        if (res != null) 
        { addTemplate(dirfiles[i], res); }         
      }
    }
  }

  public static void loadTemplates(Vector fileNames, Vector types, Vector entities)
  { File dir = new File("./cg");
    String[] dirfiles = dir.list();
    if (dirfiles == null) { return; }
    for (int i = 0; i < dirfiles.length; i++)
    { File sub = new File("./cg/" + dirfiles[i]);
      if (sub != null && dirfiles[i].endsWith(".cstl") && 
          fileNames.contains(dirfiles[i]))
      { System.err.println(">>> Found CSTL template: " + dirfiles[i]); 
        CGSpec cg = new CGSpec(entities,types); 
        CGSpec res = loadCSTL(cg, sub,types,entities); 
        if (res != null) 
        { addTemplate(dirfiles[i], res); }         
      }
    }
  }

  public static CGSpec loadCSTL(File file, Vector types, Vector entities)
  { CGSpec cg = new CGSpec(entities,types); 
    return loadCSTL(cg, file, types, entities);
  }  


  public static CGSpec loadCGTL(CGSpec res, Vector lines)
  { int noflines = 0; 
    String mode = "none"; 
    String category = null; 

    int rulesetCount = 0; 
    int ruleCount = 0; 

    for (int i = 0; i < lines.size(); i++)
    { String s = (String) lines.get(i); 
      s = s.trim(); 

      if (s.startsWith("/*") && s.endsWith("*/")) 
      { } 
      else if (s.length() == 0) 
      { } 
      else if (s.endsWith("::"))
      { mode = "texts";
        int colonindex = s.indexOf(":");  
        category = s.substring(0,colonindex); 
        rulesetCount++; 
      }         
      else if ("texts".equals(mode) && category != null)
      { Compiler2 c = new Compiler2(); 
        CGRule r = c.parse_TextCodegenerationrule(s.trim()); 
        if (r != null) 
        { res.addCategoryRule(category,r); 
          ruleCount++; 
        }
        else 
        { alertRule("!! Could not parse category " + category + " rule", s); }  
      }         
    }

    System.err.println(">>> Parsed " + lines.size() + " lines " + rulesetCount + " rulesets and " + ruleCount + " rules"); 
    
    return res; 
  } 

  public static CGSpec loadCSTL(CGSpec res, File file, Vector types, Vector entities)
  { 
    BufferedReader br = null;
    String s;
    boolean eof = false;
    int lineCount = 0; 
    
    try
    { br = new BufferedReader(new FileReader(file)); }
    catch (FileNotFoundException _e)
    { System.err.println("!! ERROR: File not found: " + file);
      return null; 
    }

    String mode = "none"; 
    String category = null; 

    int rulesetCount = 0; 
    int ruleCount = 0; 

    while (!eof)
    { try { s = br.readLine(); }
      catch (IOException _ex)
      { System.err.println("!!ERROR!!: Reading CSTL file failed.");
        return null; 
      }

      if (s == null) 
      { eof = true; 
        break; 
      }
      
      s = s.trim(); 
      lineCount++; 

      if (s.startsWith("/*") && s.endsWith("*/")) 
      { } 
      else if (s.length() == 0) { } 
      // else if (s.startsWith("import "))
      // { String[] strs = s.split(" "); 
      //   if (strs.length > 1) 
      //   { String toimport = strs[1]; 
      //     File ff = new File("./cg/" + toimport); 
      //     loadCSTL(res, ff, types, entities); 
      //   }
      // }  
      else if (s.equals("Type::"))
      { mode = "types"; 
        rulesetCount++; 
      }         
      else if (s.equals("Enumeration::"))
      { mode = "enumerations";  
        rulesetCount++; 
      }         
      else if (s.equals("Datatype::"))
      { mode = "datatypes";
        rulesetCount++; 
      }         
      else if (s.equals("BasicExpression::"))
      { mode = "basicexpressions";   rulesetCount++; 
      }  
      else if (s.equals("UnaryExpression::"))
      { mode = "unaryexpressions";   rulesetCount++; 
      }  
      else if (s.equals("BinaryExpression::"))
      { mode = "binaryexpressions";   rulesetCount++; 
      }  
      else if (s.equals("SetExpression::"))
      { mode = "setexpressions";   rulesetCount++; 
      }  
      else if (s.equals("ConditionalExpression::"))
      { mode = "conditionalexpressions";   rulesetCount++; 
      }  
      else if (s.equals("Class::"))
      { mode = "entities";   rulesetCount++; 
      }  
      else if (s.equals("Attribute::"))
      { mode = "attributes";   rulesetCount++; 
      }  
      else if (s.equals("Parameter::"))
      { mode = "parameters";   rulesetCount++; 
      }  
      else if (s.equals("ParameterArgument::"))
      { mode = "parameterarguments";   rulesetCount++; 
      }  
      else if (s.equals("Operation::"))
      { mode = "operations";   rulesetCount++; 
      }  
      else if (s.equals("Statement::"))
      { mode = "statements";   rulesetCount++; 
      }  
      else if (s.equals("Package::"))
      { mode = "packages";   rulesetCount++; 
      }  
      else if (s.equals("UseCase::"))
      { mode = "usecases";   rulesetCount++; 
      }  
      else if (s.endsWith("::"))
      { mode = "texts";
        int colonindex = s.indexOf(":");  
        category = s.substring(0,colonindex); 
        rulesetCount++; 
      }         
      else if ("types".equals(mode))
      { Compiler2 c = new Compiler2(); 
        CGRule r = c.parse_TypeCodegenerationrule(s.trim());
        if (r != null) 
        { res.addTypeUseRule(r); 
          ruleCount++; 
        } 
        else 
        { alertRule("Type", s); }
      }  
      else if ("basicexpressions".equals(mode))
      { Compiler2 c = new Compiler2(); 
        CGRule r = c.parse_ExpressionCodegenerationrule(s.trim());
        if (r != null) 
        { res.addBasicExpressionRule(r); 
          ruleCount++; 
        } 
        else 
        { alertRule("Basic expression", s); }
      }  
      else if ("unaryexpressions".equals(mode))
      { Compiler2 c = new Compiler2(); 
        CGRule r = c.parse_UnaryExpressionCodegenerationrule(s.trim());
        if (r != null) 
        { res.addUnaryExpressionRule(r);  
          ruleCount++; 
        } 
        else 
        { alertRule("Unary expression", s); }
      }  
      else if ("binaryexpressions".equals(mode))
      { Compiler2 c = new Compiler2(); 
        CGRule r = c.parse_ExpressionCodegenerationrule(s.trim());
        if (r != null) 
        { res.addBinaryExpressionRule(r);  
          ruleCount++; 
        } 
        else 
        { alertRule("Binary expression", s); }
      }  
      else if ("setexpressions".equals(mode))
      { Compiler2 c = new Compiler2(); 
        CGRule r = c.parse_ExpressionCodegenerationrule(s.trim());
        if (r != null) 
        { res.addSetExpressionRule(r);  
          ruleCount++; 
        }
        else 
        { alertRule("Set expression", s); }
      }  
      else if ("conditionalexpressions".equals(mode))
      { Compiler2 c = new Compiler2(); 
        CGRule r = c.parse_ExpressionCodegenerationrule(s.trim());
        if (r != null) 
        { res.addConditionalExpressionRule(r);  
          ruleCount++; 
        } 
        else 
        { alertRule("Conditional expression", s); }
      }  
      else if ("entities".equals(mode))
      { Compiler2 c = new Compiler2(); 
        CGRule r = c.parse_EntityCodegenerationrule(s.trim()); 
        if (r != null) 
        { res.addClassRule(r);  
          ruleCount++; 
        } 
        else 
        { alertRule("Entity", s); }
      }         
      else if ("enumerations".equals(mode))
      { Compiler2 c = new Compiler2(); 
        CGRule r = c.parse_EntityCodegenerationrule(s.trim()); 
        if (r != null) 
        { res.addEnumerationRule(r);  
          ruleCount++; 
        } 
        else 
        { alertRule("Enumeration", s); }
      } 
      else if ("datatypes".equals(mode))
      { Compiler2 c = new Compiler2(); 
        CGRule r = c.parse_EntityCodegenerationrule(s.trim());
        if (r != null) 
        { res.addDatatypeRule(r);  
          ruleCount++; 
        } 
        else 
        { alertRule("Datatype", s); }
      }          
      else if ("packages".equals(mode))
      { Compiler2 c = new Compiler2(); 
        CGRule r = c.parse_EntityCodegenerationrule(s.trim()); 
        if (r != null) 
        { res.addPackageRule(r);  
          ruleCount++; 
        } 
        else 
        { alertRule("Package", s); }
      }         
      else if ("attributes".equals(mode))
      { Compiler2 c = new Compiler2(); 
        CGRule r = c.parse_AttributeCodegenerationrule(s.trim()); 
        if (r != null) 
        { res.addAttributeRule(r);  
          ruleCount++; 
        }
        else 
        { alertRule("Attribute/reference", s); }
      }         
      else if ("operations".equals(mode))
      { Compiler2 c = new Compiler2(); 
        CGRule r = c.parse_OperationCodegenerationrule(s.trim()); 
        if (r != null) 
        { res.addOperationRule(r);  
          ruleCount++; 
        } 
        else 
        { alertRule("Operation", s); }
      }  
      else if ("parameters".equals(mode))
      { Compiler2 c = new Compiler2(); 
        CGRule r = c.parse_OperationCodegenerationrule(s.trim()); 
        if (r != null) 
        { res.addParameterRule(r);  
          ruleCount++; 
        }
        else 
        { alertRule("Parameter", s); } 
      }       
      else if ("parameterarguments".equals(mode))
      { Compiler2 c = new Compiler2(); 
        CGRule r = c.parse_OperationCodegenerationrule(s.trim()); 
        if (r != null) 
        { res.addParameterArgumentRule(r);  
          ruleCount++; 
        } 
        else 
        { alertRule("Parameter argument", s); } 
      }       
      else if ("statements".equals(mode))
      { Compiler2 c = new Compiler2(); 
        CGRule r = c.parse_StatementCodegenerationrule(s.trim(),entities,types); 
        if (r != null) 
        { res.addStatementRule(r);  
          ruleCount++; 
        } 
        else 
        { alertRule("Statement", s); } 
      }         
      else if ("usecases".equals(mode))
      { Compiler2 c = new Compiler2(); 
        CGRule r = c.parse_UseCaseCodegenerationrule(s.trim(),entities,types); 
        if (r != null) 
        { res.addUseCaseRule(r);  
          ruleCount++; 
        }
        else 
        { alertRule("UseCase", s); }  
      }         
      else if ("texts".equals(mode) && category != null)
      { Compiler2 c = new Compiler2(); 
        CGRule r = c.parse_TextCodegenerationrule(s.trim()); 
        if (r != null) 
        { res.addCategoryRule(category,r);  
          ruleCount++; 
        }
        else 
        { alertRule("!! Could not parse category " + category + " rule", s); }  
      }         
    }

    System.err.println(">>> Parsed " + lineCount + " lines " + rulesetCount + " rulesets and " + ruleCount + " rules"); 
    return res; 
  }

  private static void alertRule(String kind, String r)
  { System.err.println("!!! Unable to parse " + kind + " rule: " + r);
    /* JOptionPane.showMessageDialog(null, "Warning!: Unable to parse " + kind + " rule: " + r, "", 
                                  JOptionPane.ERROR_MESSAGE); */ 
  }

  public static void addTemplate(String filename, CGSpec cg) 
  { templates.put(filename,cg); }

  public static boolean hasTemplate(String filename)
  { CGSpec cg = (CGSpec) templates.get(filename); 
    if (cg != null) 
    { return true; } 
    return false; 
  } 

  public static CGSpec getTemplate(String name)
  { return (CGSpec) templates.get(name); }

  public static void main(String[] args) 
  { String[] larray = "expression::\r\n_1 & _2 |-->_1 && _2\n\r_1 or _2 |-->_1 || _2\n".split("[\\n\\r]+"); 
    Vector lines = new Vector(); 
    for (int i = 0; i < larray.length; i++) 
    { lines.add(larray[i]); } 
    CGSpec res = new CGSpec(new Vector(), new Vector()); 
    CSTL.loadCGTL(res,lines); 
  } 
}

