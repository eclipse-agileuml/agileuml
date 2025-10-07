import java.util.Vector; 
import java.io.*; 
import javax.swing.JOptionPane; 

/******************************
* Copyright (c) 2003--2025 Kevin Lano
* This program and the accompanying materials are made available under the
* terms of the Eclipse Public License 2.0 which is available at
* http://www.eclipse.org/legal/epl-2.0
*
* SPDX-License-Identifier: EPL-2.0
* *****************************/
/* Package: OCL */ 

/* Need: ->intersectAll for collections of maps. */ 
/* ->unionAll for maps for C++, Swift, Go        */ 

public class UnaryExpression extends Expression
{ String operator; 
  Expression argument; 
  Attribute accumulator = null;      // lambda


  // For expressions   e->size()  e->any()  s->isDeleted()  etc

  public UnaryExpression(String op, Expression arg)
  { operator = op; 
    argument = arg; 
  } 

  public UnaryExpression(BasicExpression be)
  { // obj.op() becomes obj->op()

    argument = ((BasicExpression) be).getObjectRef(); 
    operator = "->" + be.getData(); 
    type = be.getType();
    elementType = be.getElementType();  
  } 

  public String functionOfLambda()
  { if ("lambda".equals(operator))
    { Expression arg = argument; 
      if (arg instanceof UnaryExpression && 
          "lambda".equals(((UnaryExpression) arg).operator))
      { return 
          ((UnaryExpression) arg).functionOfLambda(); 
      }

      if (arg instanceof BasicExpression)
      { BasicExpression be = (BasicExpression) arg; 
        if (be.umlkind == QUERY && 
            "self".equals(be.objectRef + "")) 
        { return be.data; }
      } 
    }
    return null;  
  } 

  public static Expression newLambdaUnaryExpression(
                         Expression bcall, 
                         BehaviouralFeature bf)
  { // lambda par1 : Par1Type in ... in bcall(par1,...,parn)
    Vector pars = bf.getParameters(); 
    Expression res = bcall; 
    for (int i = pars.size()-1; i >= 0; i--) 
    { Attribute p = (Attribute) pars.get(i);
      Type oldtype = res.getType();  
      res.setBrackets(true); 
      res = new UnaryExpression("lambda", res); 
      ((UnaryExpression) res).setAccumulator(p); 
      Type ftype = new Type("Function", null);
      ftype.setKeyType(p.getType()); 
      ftype.setElementType(oldtype); 
      res.type = ftype; 
    } 
    return res; 
  } 

  public static Expression newLambdaUnaryExpression(
                         Vector pars, 
                         Expression body)
  { // lambda par1 : Par1Type in ... in body

    Expression res = body; 
    if (pars == null) 
    { return res; } 

    for (int i = pars.size()-1; i >= 0; i--) 
    { Attribute p = (Attribute) pars.get(i);
      Type oldtype = res.getType();  
      res.setBrackets(true); 
      res = new UnaryExpression("lambda", res); 
      ((UnaryExpression) res).setAccumulator(p); 
      Type ftype = new Type("Function", null);
      ftype.setKeyType(p.getType()); 
      ftype.setElementType(oldtype); 
      res.type = ftype; 
    } 
    return res; 
  } 
      
  public static Expression newLambdaUnaryExpression(
                         String var, Type typ, 
                         Expression body)
  { // lambda var : typ in ... in body

    Attribute par = 
      new Attribute(var, typ, ModelElement.INTERNAL); 
    body.setBrackets(true); 
    UnaryExpression res = new UnaryExpression("lambda", body); 
    res.setAccumulator(par); 

    Type ftype = new Type("Function", null);
    ftype.setKeyType(typ); 
    ftype.setElementType(body.getType()); 
    res.type = ftype; 
     
    return res; 
  } 

  public static UnaryExpression newUnaryExpression(String op, Expression expr) 
  { if (expr == null) 
    { return null; } 
    return new UnaryExpression(op,expr); 
  } 

  public static UnaryExpression newUnaryExpression(String op, 
    Attribute letvar, Statement stat, Entity cclass, 
    Vector types, Vector entities) 
  { if (stat == null) 
    { return null; }

    if ("lambda".equals(op))
    { if (Statement.isSingleReturnStatement(stat))
      { Expression expr = Statement.getReturnExpression(stat); 
        
        UnaryExpression res = new UnaryExpression(op,expr);
        if (expr != null) 
        { Vector contexts = new Vector(); 
          contexts.add(cclass); 
          Vector env = new Vector();
          env.add(letvar);  
          expr.typeCheck(types,entities,contexts,env); 
          if (expr.type != null) 
          { res.type = new Type("Function",null); 
            res.type.setKeyType(letvar.getType()); 
            res.type.setElementType(expr.type); 
          } 
        }  
        return res; 
      } 

      String opid; 
      BehaviouralFeature oper = null; 
      BasicExpression call = null; 
      UnaryExpression res = null;
      BasicExpression selfvar = 
        BasicExpression.newVariableBasicExpression("self"); 

      if (cclass != null) 
      { oper = cclass.getIdenticalOperation(stat);
        selfvar.setType(new Type(cclass)); 
      }

      if (oper != null)  
      { opid = oper.getName(); 
        call = new BasicExpression(opid); 
        call.umlkind = QUERY; 
        call.setIsEvent(); 
        call.addParameter(new BasicExpression(letvar));
        call.setObjectRef(selfvar);    
        res = new UnaryExpression(op,call);
        Type retType = oper.getType(); 
        if (retType != null) 
        { res.type = new Type("Function",null); 
          res.type.setKeyType(letvar.getType()); 
          res.type.setElementType(retType); 
        } 
      } 
      else 
      { opid = Identifier.nextIdentifier("lambdaFunction"); 
        call = new BasicExpression(opid); 
        call.umlkind = QUERY; 
        call.setIsEvent(); 
        call.addParameter(new BasicExpression(letvar));
        call.setObjectRef(selfvar);    
        res = new UnaryExpression(op,call);

        BehaviouralFeature bf = new BehaviouralFeature(opid);
        bf.addParameter(letvar);  
        bf.setActivity(stat); 
        bf.setPost(new BasicExpression(true));
          // Find the implicit return type of the stat.
        Vector returnExpressions = 
            Statement.getReturnValues(stat);  
        System.out.println(">>> Return values: " + returnExpressions); 

        if (returnExpressions.size() > 0)
        { Expression expr = 
            (Expression) returnExpressions.get(0); 
        
          if (expr != null) 
          { Vector contexts = new Vector(); 
            contexts.add(cclass); 
            Vector env = new Vector();
            env.add(letvar);  
            expr.typeCheck(types,entities,contexts,env); 
            if (expr.type != null) 
            { res.type = new Type("Function",null); 
              res.type.setKeyType(letvar.getType()); 
              res.type.setElementType(expr.type); 
              bf.setType(expr.type); 
            } 
          }  
        }
 
        if (cclass != null)
        { bf.setEntity(cclass); 
          cclass.addOperation(bf);
        }  
      } 
      return res;
    } 

    return null;  
  } 

  public static Expression newLambdaUnaryExpression( 
    Vector pars, Statement stat, Entity cclass, 
    Vector types, Vector entities) 
  { System.out.println("*** Creating new lambda expression for: " + pars + " " + stat + " " + cclass); 
    System.out.println("********************************"); 

    if (stat == null || pars == null) 
    { return null; }

    Expression res = null; 

    if (Statement.isSingleReturnStatement(stat))
    { Expression expr = Statement.getReturnExpression(stat); 
        
      if (expr != null) 
      { Vector contexts = new Vector(); 
        contexts.add(cclass); 
        Vector env = new Vector();
        env.addAll(pars);  
        expr.typeCheck(types,entities,contexts,env); 
        res = expr; 

        for (int i = pars.size()-1; i >= 0; i--) 
        { Attribute p = (Attribute) pars.get(i);
          Type oldtype = res.getType();  
          res = new UnaryExpression("lambda", res); 
          ((UnaryExpression) res).setAccumulator(p); 
          Type ftype = new Type("Function", null);
          ftype.setKeyType(p.getType()); 
          ftype.setElementType(oldtype); 
          res.type = ftype; 
        } 
        return res; 
      } 
    }

    String opid; 
    BehaviouralFeature oper = null; 
    BasicExpression call = null; 
    BasicExpression selfvar = 
        BasicExpression.newVariableBasicExpression("self"); 

    if (cclass != null) 
    { oper = cclass.getIdenticalOperation(stat);
      selfvar.setType(new Type(cclass)); 
    } // could be same stat for different parameters

    if (oper != null)  
    { opid = oper.getName(); 
      call = new BasicExpression(oper); 
      call.umlkind = QUERY; 
      call.setIsEvent(); 
      call.addParameterExpressions(pars); 
      call.setObjectRef(selfvar);    
      res = call;

      for (int i = pars.size()-1; i >= 0; i--) 
      { Attribute p = (Attribute) pars.get(i);
        Type oldtype = res.getType();  
        res = new UnaryExpression("lambda", res); 
        ((UnaryExpression) res).setAccumulator(p); 
        Type ftype = new Type("Function", null);
        ftype.setKeyType(p.getType()); 
        ftype.setElementType(oldtype); 
        res.type = ftype; 
      } 
      return res; 
    } 
    else 
    { opid = Identifier.nextIdentifier("lambdaFunction"); 
      call = new BasicExpression(opid); 
      call.umlkind = QUERY; 
      call.setIsEvent(); 
      call.addParameterExpressions(pars);
      call.setObjectRef(selfvar);    

      BehaviouralFeature bf = new BehaviouralFeature(opid);
      bf.addParameters(pars);  
      bf.setActivity(stat); 
      bf.setPost(new BasicExpression(true));
          // Find the implicit return type of the stat.
      Vector returnExpressions = 
            Statement.getReturnValues(stat);  
      System.out.println(">>> Return values: " + returnExpressions); 

      Expression expr = call; 
      if (returnExpressions.size() > 0)
      { expr = 
            (Expression) returnExpressions.get(0); 
      } 
     
      if (expr != null) 
      { Vector contexts = new Vector(); 
        contexts.add(cclass); 
        Vector env = new Vector();
        env.addAll(pars);  
        expr.typeCheck(types,entities,contexts,env); 
        Type oldtype = expr.getType();  
        bf.setReturnType(oldtype); 
          
        res = call; 

        for (int i = pars.size()-1; i >= 0; i--) 
        { Attribute p = (Attribute) pars.get(i);
          res = new UnaryExpression("lambda", res); 
          ((UnaryExpression) res).setAccumulator(p); 
          Type ftype = new Type("Function", null);
          ftype.setKeyType(p.getType()); 
          ftype.setElementType(oldtype); 
          res.type = ftype;  
          oldtype = ftype;
        }  
      }
 
      if (cclass != null)
      { bf.setEntity(cclass); 
        cclass.addOperation(bf);
      }  
     
      return res;
    }  
  } 

  public static Expression argumentsToLambdaCall(String fname,
                                                 Vector args)
  { BasicExpression f = 
      BasicExpression.newVariableBasicExpression(fname); 
    Expression res = f; 

    for (int i = 0; i < args.size(); i++) 
    { Expression arg = (Expression) args.get(i); 
      res = new BinaryExpression("->apply", res, arg); 
    } 
    return res; 
  } 

  public static Expression argumentsToLambdaCall(Expression x,
                                                 Vector args)
  { Expression res = x; 

    for (int i = 0; i < args.size(); i++) 
    { Expression arg = (Expression) args.get(i); 
      res = new BinaryExpression("->apply", res, arg); 
    } 
    return res; 
  } 


  public String getOperator()
  { return operator; } 

  public Expression getArgument()
  { return argument; } 

  public Vector getParameters() 
  { return new Vector(); } 

  public Expression getInnerObjectRef()
  { return argument.getInnerObjectRef(); } 

  public void setOperator(String op) 
  { operator = op; } 

  public void setArgument(Expression arg) 
  { argument = arg; } 

  public Attribute getAccumulator() 
  { return accumulator; } 

  public void setAccumulator(Attribute att) 
  { accumulator = att; } 

  public Object clone()
  { Expression newarg = (Expression) argument.clone(); 
    UnaryExpression res = new UnaryExpression(operator,newarg); 
    res.needsBracket = needsBracket; 
    res.umlkind = umlkind; 
    res.type = type; 
    res.elementType = elementType;  // type of elements if a set
    res.entity = entity; 
    res.multiplicity = multiplicity;
    res.formalParameter = formalParameter;
    res.refactorELV = refactorELV; 
 
    if (accumulator != null) 
    { Attribute newacc = 
         new Attribute(accumulator.getName(), 
                 accumulator.getType(), 
                 ModelElement.INTERNAL); 
      res.accumulator = newacc; 
    }
    return res;   
  }  

  public Expression transformPythonSelectExpressions()
  { Expression newarg = 
        argument.transformPythonSelectExpressions();
    UnaryExpression res = new UnaryExpression(operator,newarg); 
    res.needsBracket = needsBracket; 
    res.umlkind = umlkind; 
    res.type = type; 
    res.elementType = elementType;  // type of elements if a set
    res.entity = entity; 
    res.multiplicity = multiplicity;
    res.formalParameter = formalParameter;
    res.refactorELV = refactorELV; 
 
    if (accumulator != null) 
    { Attribute newacc = 
         new Attribute(accumulator.getName(), 
                 accumulator.getType(), 
                 ModelElement.INTERNAL); 
      res.accumulator = newacc; 
    }
    return res;   
  }  

     
    

  public boolean isLambdaExpression()
  { return "lambda".equals(operator); } 

  public void refineLambdaParameterType(Type typ)
  { if (accumulator != null) 
    { Type oldtype = accumulator.getType(); 
      if (oldtype == null || "OclAny".equals(oldtype + ""))
      { if (typ != null && !("OclAny".equals(typ + "")))
        { accumulator.setType(typ); 
          type.setKeyType(typ); 
        } 
      } 
    } 
  } 


  public boolean isTailRecursion(BehaviouralFeature bf)
  { // bfname does not occur in this 

    String bfname = bf.getName(); 

    Vector names = new Vector(); 
    names.add(bfname); 
    Vector vars = variablesUsedIn(names); 

    if (vars.size() == 0)
    { return true; } 

    return false; 
  } 

  public Expression definedness()
  { Expression res = argument.definedness();
    if ("->last".equals(operator) || 
        "->first".equals(operator) ||
        "->front".equals(operator) || 
        "->tail".equals(operator) ||
        "->max".equals(operator) || 
        "->min".equals(operator) ||
        "->any".equals(operator))
    { Expression zero = new BasicExpression(0);
      UnaryExpression orsize = 
        new UnaryExpression("->size", argument);
      Expression pos = 
        new BinaryExpression(">",orsize,zero);
      return simplify("&",pos,res,null);
    }
    else if ("->sqrt".equals(operator))
    { Expression zero = new BasicExpression(0);
      Expression nneg = 
          new BinaryExpression(">=",argument,zero);
      return simplify("&",nneg,res,null);
    }
    else if ("->log".equals(operator) || 
             "->log10".equals(operator))
    { Expression zero = new BasicExpression(0);
      Expression nneg = 
          new BinaryExpression(">",argument,zero);
      return simplify("&",nneg,res,null);
    }
    else if ("->acos".equals(operator) || 
             "->asin".equals(operator))
    { Expression minus1 = new BasicExpression(-1); 
      Expression one = new BasicExpression(1); 
      Expression pos1 = new BinaryExpression(">=",argument,minus1);
      Expression pos2 = new BinaryExpression("<=",argument,one);
      Expression res1 = simplify("&",pos2,res,null);
      return simplify("&",pos1,res1,null); 
    }
    else if ("->toReal".equals(operator))
    { Expression checkReal = new UnaryExpression("->isReal",argument); 
      return simplify("&",checkReal,res,null); 
    } 
    else if ("->toInteger".equals(operator))
    { Expression checkInt = new UnaryExpression("->isInteger",argument); 
      return simplify("&",checkInt,res,null); 
    } 
    else if ("->toLong".equals(operator))
    { Expression checkInt = new UnaryExpression("->isLong",argument); 
      return simplify("&",checkInt,res,null); 
    } 
    // if ("->oclAsType".equals(operator))
    // { UnaryExpression iko = new UnaryExpression("->oclIsKindOf",argument); 
    //   return iko; 
    // } BinaryExpression
    return res;
  }  

  public void setPre()
  { argument.setPre(); } 

  public Expression removePrestate()
  { UnaryExpression res = (UnaryExpression) clone(); 
    res.argument = argument.removePrestate();
    // res.accumulator = accumulator;  
    return res; 
  } 

  public Expression determinate()
  { Expression res = argument.determinate(); 
    Expression one = new BasicExpression(1);
      
    if ("->any".equals(operator))
    { UnaryExpression andsize = 
         new UnaryExpression("->size",argument);
      Expression isone = 
        new BinaryExpression("=",andsize,one);
      return simplify("&",res,isone,null);
    }

    if ("->asSequence".equals(operator) || 
        "->asOrderedSet".equals(operator))
    { if ("Set".equals(argument.getType().getName()) || 
          "Map".equals(argument.getType().getName()))
      { return new BinaryExpression("<=", 
                 new UnaryExpression("->size",argument),
                 one); 
      } 
      else 
      { return res; } 
    } // Introducing an arbitrary order on the collection. 

    if ("->asBag".equals(operator))
    { return res; 
      // if ("Set".equals(argument.getType().getName()))
      // { return new BasicExpression(true); } 
      // return new BinaryExpression("<=", 
      //              new UnaryExpression("->size",argument),
      //              new BasicExpression(1));  
    } // A canonical order, same as ->sort. 

    if ("->flatten".equals(operator))
    { Type et = argument.elementType; 
      if (Type.isSetType(et) && Type.isSequenceType(argument.type))
      { return new BasicExpression(false); } 
    } 

    return res;
  }  // ->asSequence will not be determinate, nor ->flatten on a sequence of sets? 

  public Expression excel2Ocl(Entity target, Vector entities, Vector qvars, Vector antes)
  { UnaryExpression enew = (UnaryExpression) clone(); 
    enew.argument = argument.excel2Ocl(target, entities, qvars, antes); 
    return enew; 
  } 

  public Vector splitToCond0Cond1(Vector conds, Vector qvars, Vector lvars, Vector allvars)
  { Expression cond0 = (Expression) conds.get(0); 
    Expression cond1 = (Expression) conds.get(1); 
    Vector res = new Vector(); 
    Expression c1 = Expression.simplifyAnd(cond1,this); 
    res.add(cond0); 
    res.add(c1); 
    
    return res; 
  } 

  // The following turns  x : e & P & l = g & Q into [x,P,l,Q] in allvars
  public Vector splitToCond0Cond1Pred(Vector conds, Vector pars, Vector qvars, Vector lvars, Vector allvars, Vector allpreds)
  { Expression cond0 = (Expression) conds.get(0); 
    Expression cond1 = (Expression) conds.get(1); 
    Vector res = new Vector(); 
    // System.out.println("Split conds on: " + this);

    Expression c1; 
    if (cond1 == null) 
    { c1 = this; } 
    else 
    { c1 = new BinaryExpression("&",cond1,this); }  
    res.add(cond0); 
    res.add(c1); 
    allvars.add(this); 
    allpreds.add(this); 
    System.out.println(">>>>> Added condition: " + this); 
    
    return res; 
  } 

  public Vector splitToTCondPost(Vector tvars, Vector conds)
  { Expression cond0 = (Expression) conds.get(0); 
    Expression cond1 = (Expression) conds.get(1); 
    Vector res = new Vector(); 

    // This is a TCond conjunct if: it only has a tvar as base of 
    // its basic expressions, or they are values

    Expression c1 = Expression.simplifyAnd(cond1,this); 
    res.add(cond0); 
    res.add(c1); 
    
    return res; 
  }  // seems like a sensible thing to do

public void findClones(java.util.Map clones, String rule, String op)
{ if (this.syntacticComplexity() < UCDArea.CLONE_LIMIT) 
  { return; }
  String val = this + "";
  Vector used = (Vector) clones.get(val);
  if (used == null)
  { used = new Vector(); }
  if (rule != null)
  { used.add(rule); }
  else if (op != null)
  { used.add(op); }
  clones.put(val,used);
  argument.findClones(clones,rule,op);
}

public void findClones(java.util.Map clones, 
                       java.util.Map cloneDefs,
                       String rule, String op)
{ if (this.syntacticComplexity() < UCDArea.CLONE_LIMIT) 
  { return; }
  String val = this + "";
  Vector used = (Vector) clones.get(val);
  if (used == null)
  { used = new Vector(); }
  if (rule != null)
  { used.add(rule); }
  else if (op != null)
  { used.add(op); }
  clones.put(val,used);
  cloneDefs.put(val, this); 
  argument.findClones(clones,cloneDefs,rule,op);
}

  public void findMagicNumbers(java.util.Map mgns, String rule, String op)
  { argument.findMagicNumbers(mgns,rule,op); } 

  public Expression checkConversions(Type propType, Type propElemType, java.util.Map interp) 
  { Expression argres = null; 
    if (argument != null) 
    { argres = argument.checkConversions(propType, propElemType, interp); 
      argument = argres; 
    } 

    if ("boolean".equals(type + "") || 
        "int".equals(type + "") || "long".equals(type + "") ||
        "double".equals(type + "") || 
        "String".equals(type + ""))
    { return this; } 

    if (operator.equals("->last") || 
        operator.equals("->first") ||
        operator.equals("->front") || 
        operator.equals("->tail") || 
        operator.equals("->flatten") || 
        operator.equals("->any") || 
        operator.equals("->max") || 
        operator.equals("->reverse") || 
        operator.equals("->copy") ||
        operator.equals("->iterator") || 
        operator.equals("->asSequence") || 
        operator.equals("->asSet") || 
        operator.equals("->asBag") || 
        operator.equals("->asOrderedSet") ||
        operator.equals("->min") || operator.equals("->sort"))
    { if (elementType != null && elementType.isEntity())
      { Entity ent = elementType.getEntity(); 
        if (ent.hasStereotype("source"))
        { Entity tent = (Entity) interp.get(ent + ""); 
          if (tent == null) 
          { if (propElemType != null && 
                propElemType.isEntity())
            { tent = propElemType.getEntity(); } 
          }  
          BasicExpression res = new BasicExpression(tent); 
          res.setType(type); 
          // res.setEntity(tent); 
          res.setMultiplicity(multiplicity); 
          res.setElementType(new Type(tent)); 
          res.setUmlKind(Expression.CLASSID); 
          // if (isMultiple())
          if (operator.equals("->front") || 
              operator.equals("->tail") || 
              operator.equals("->sort") ||
              operator.equals("->reverse") || 
              operator.equals("->asSet") || 
              operator.equals("->asBag") || 
              operator.equals("->flatten") ||
              operator.equals("->asOrderedSet") || 
              // operator.equals("->copy") ||
              operator.equals("->asSequence"))
          { BinaryExpression ind = 
              new BinaryExpression("->collect",this,new BasicExpression("$id"));
            ind.setType(type);  
            ind.setElementType(new Type("String",null)); 
            res.setArrayIndex(ind); 
            return res; 
          } // TRef[this->collect($id)] 
          else 
          { BasicExpression idexp = new BasicExpression("$id"); 
            idexp.setType(new Type("String",null));
            BasicExpression bexp = new BasicExpression(this);  
            idexp.setObjectRef(bexp); 
            res.setArrayIndex(idexp); 
            return res;
          }  // TRef[this.$id], TRef[this->collect($id)]->any()  is better
        }
      } 
    }  
    // argres = argument.checkConversions(propType, propElemType, interp); 
    // return argres; 
    return this; 
  } 

  public Expression replaceModuleReferences(UseCase uc) 
  { Expression rr = argument.replaceModuleReferences(uc); 
    Expression res = new UnaryExpression(operator,rr);
    res.setType(type); 
    res.setElementType(elementType); 
    res.umlkind = umlkind; 
    return res;  
  } 


  public Expression addPreForms(String v)
  { argument = argument.addPreForms(v); 
    return this; 
  } 

  public Map energyUse(Map res, Vector rUses, Vector aUses) 
  { // ->sort()
    // is amber flag.
    // ->select(...)->any() is a red flag.  

    argument.energyUse(res, rUses, aUses); 

    if (operator.equals("lambda") && 
        accumulator != null) 
    { Type typ = accumulator.getType(); 
      if (typ != null && 
          typ.complexity() > TestParameters.nestedTypeLimit)
      { aUses.add("! Warning (MNC) flaw: complex type with complexity " + typ.complexity() + ": " + typ);
        int ascore = (int) res.get("amber");
        ascore = ascore + 1;
        res.set("amber", ascore);
      } 
    } 

    if (operator.equals("->notEmpty") && 
        argument instanceof BinaryExpression)
    { // ->select(P)->notEmpty()  is  ->exists(P)
      BinaryExpression leftarg = (BinaryExpression) argument; 
      String leftargop = leftarg.getOperator(); 
      Expression leftargleft = leftarg.getLeft();
      Expression leftargpred = leftarg.getRight();
        
      if (leftargop.equals("->select") || 
          leftargop.equals("->reject") ||
          leftargop.equals("|") ||
          leftargop.equals("|R"))
      { rUses.add("!! OCL efficiency smell (OES): Inefficient expression in: " + this + ",\n" + 
                    ">>> instead use ->exists");
        int rscore = (int) res.get("red"); 
        res.set("red", rscore+1); 
      }
    } 
    else if (operator.equals("->isEmpty") && 
        argument instanceof BinaryExpression)
    { // ->select(P)->isEmpty()  is  ->forAll(not(P))
      BinaryExpression leftarg = (BinaryExpression) argument; 
      String leftargop = leftarg.getOperator(); 
      Expression leftargleft = leftarg.getLeft();
      Expression leftargpred = leftarg.getRight();
        
      if (leftargop.equals("->select") || 
          leftargop.equals("->reject") ||
          leftargop.equals("|") ||
          leftargop.equals("|R"))
      { rUses.add("!! OCL efficiency smell (OES): Inefficient expression in: " + this + ",\n" + 
                    ">>> instead use ->forAll");
        int rscore = (int) res.get("red"); 
        res.set("red", rscore+1); 
      }
    } 
    else if (operator.equals("->any"))
    {
      if (argument instanceof BinaryExpression) 
      { BinaryExpression lbe = (BinaryExpression) argument; 

        if (lbe.operator.equals("|") ||
            lbe.operator.equals("->select"))
        { rUses.add("!! OCL efficiency smell (OES): Inefficient col->select(x | P)->any() expression in: " + this + ",\n" + 
                    ">>> instead use:    col->any(x | P)");
          int rscore = (int) res.get("red"); 
          res.set("red", rscore+1); 
        }
        else if (lbe.operator.equals("|R") ||
            lbe.operator.equals("->reject"))
        { rUses.add("!! OCL efficiency smell (OES): Inefficient col->reject(x | P)->any() expression in " + this + ", \n" + 
            ">>> instead, use:   col->any(x | not(P))");
          int rscore = (int) res.get("red"); 
          res.set("red", rscore+1); 
        }
        else if (lbe.operator.equals("|C"))
        { rUses.add("!! OCL efficiency smell (OES): Inefficient col->collect(x | e)->any() expression in " + this + ", \n" + 
            ">>> instead, use:  let x = col->any() in e");
          int rscore = (int) res.get("red"); 
          res.set("red", rscore+1); 
        }
      } 
    }
    else if ("->sort".equals(operator))
    { if (argument.isSorted())
      { aUses.add("! Redundant ->sort operation: " + this + 
            "\n! Argument is already sorted.");
      }
      else if (argument.isSet()) 
      { aUses.add("! n*log(n) sorting algorithm used for " + this + 
            "\n>>> It may be more efficient to use a SortedSet type.");
      }
      else 
      { aUses.add("! n*log(n) sorting algorithm used for " + this); } 

      int ascore = (int) res.get("amber"); 
      res.set("amber", ascore+1); 
    } 
    else if (operator.equals("->unionAll") || 
        operator.equals("->intersectAll") || 
        operator.equals("->concatenateAll"))
    { aUses.add("! High-cost operation: " + this);

      int ascore = (int) res.get("amber"); 
      res.set("amber", ascore+1); 
    }
    else if (("->last".equals(operator) || 
              "->first".equals(operator) || 
              "->front".equals(operator) || 
              "->tail".equals(operator)) && 
             argument instanceof BasicExpression && 
             ((BasicExpression) argument).isOperationCall())
    { // redundant results computation

      aUses.add("! Possible redundant results computation in: " + this);
      int ascore = (int) res.get("amber"); 
      res.set("amber", ascore+1); 
    } 
    else if (("->last".equals(operator) || 
              "->first".equals(operator)) && 
             argument instanceof BinaryExpression && 
             "|C".equals(
                ((BinaryExpression) argument).getOperator()))
    { // redundant results computation

      aUses.add("! OCL efficiency smell (OES): Redundant results computation in: " + this);
      int ascore = (int) res.get("amber"); 
      res.set("amber", ascore+1); 
    } 

    return res; 
  } 

  public Expression simplifyOCL() 
  { Expression arg = 
       argument.simplifyOCL(); 

    if (operator.equals("->size"))
    { return Expression.simplifySize(arg); } 

    if (operator.equals("->last"))
    { return Expression.simplifyLast(arg); } 

    if (operator.equals("->first") && 
        arg instanceof UnaryExpression)
    { return Expression.simplifyFirst(arg); } 

    if (operator.equals("->first") && 
        arg instanceof SetExpression)
    { return Expression.simplifyFirst(arg); } 

    if (operator.equals("->first") && 
        arg instanceof BasicExpression)
    { return Expression.simplifyFirst(arg); } 

    if (operator.equals("not"))
    { return Expression.negate(arg); }

    if (operator.equals("->front"))
    { return Expression.simplifyFront(arg); } 

    if (operator.equals("->tail"))
    { return Expression.simplifyTail(arg); } 

    if (operator.equals("->reverse"))
    { return Expression.simplifyReverse(arg); } 

    if (operator.equals("->sum"))
    { return Expression.simplifySum(arg); } 

    if (operator.equals("->prd"))
    { return Expression.simplifyPrd(arg); } 

    if (operator.equals("->notEmpty") && 
        argument instanceof BinaryExpression)
    { // ->select(P)->notEmpty()  is  ->exists(P)
      BinaryExpression leftarg = (BinaryExpression) argument; 
      String leftargop = leftarg.getOperator(); 
      Expression leftargleft = leftarg.getLeft();
      Expression leftargpred = leftarg.getRight();
        
      if (leftargop.equals("->select"))
      { // s->select(P)->notEmpty()
        System.out.println(">> OCL efficiency smell (OES): Inefficient comparison: " + this);
          
        BinaryExpression res = 
            new BinaryExpression("->exists", leftargleft,
                                 leftargpred); 
        return res; 
      } 
      else if (leftargop.equals("->reject"))
      { // s->reject(P)->notEmpty()
        System.out.println(">> OCL efficiency smell (OES): Inefficient comparison: " + this);
          
        UnaryExpression notpred = 
            new UnaryExpression("not", leftargpred); 
        BinaryExpression res = 
            new BinaryExpression("->exists", leftargleft,
                                 notpred); 
        return res; 
      } 
      else if (leftargop.equals("|"))
      { // s->select(x | P)->notEmpty()
        System.out.println(">> OCL efficiency smell (OES): Inefficient comparison: " + this);
          
        BinaryExpression res = 
            new BinaryExpression("#", leftargleft,
                                 leftargpred); 
        return res; 
      } 
      else if (leftargop.equals("|R"))
      { // s->reject(x | P)->notEmpty()
        System.out.println(">> OCL efficiency smell (OES): Inefficient comparison: " + this);
          
        UnaryExpression notpred = 
             new UnaryExpression("not", leftargpred); 
        BinaryExpression res = 
            new BinaryExpression("#", leftargleft,
                                 notpred); 
        return res; 
      } 
    } 
    else if (operator.equals("->isEmpty") && 
        argument instanceof BinaryExpression)
    { // ->select(P)->isEmpty()  is  ->forAll(not(P))
      BinaryExpression leftarg = (BinaryExpression) argument; 
      String leftargop = leftarg.getOperator(); 
      Expression leftargleft = leftarg.getLeft();
      Expression leftargpred = leftarg.getRight();
        
      if (leftargop.equals("->select"))
      { // s->select(P)->isEmpty()
        System.out.println(">> OCL efficiency smell (OES): Inefficient comparison: " + this);
          
        UnaryExpression notpred = 
            new UnaryExpression("not", leftargpred); 
        BinaryExpression res = 
            new BinaryExpression("->forAll", leftargleft,
                                 notpred); 
        return res; 
      } 
      else if (leftargop.equals("->reject"))
      { // s->reject(P)->isEmpty()
        System.out.println(">> OCL efficiency smell (OES): Inefficient comparison: " + this);
          
        BinaryExpression res = 
            new BinaryExpression("->forAll", leftargleft,
                                 leftargpred); 
        return res; 
      } 
      else if (leftargop.equals("|"))
      { // s->select(x | P)->isEmpty()
        System.out.println(">> OCL efficiency smell (OES): Inefficient comparison: " + this);

        UnaryExpression notpred = 
            new UnaryExpression("not", leftargpred); 

        BinaryExpression res = 
            new BinaryExpression("!", leftargleft,
                                 notpred); 
        return res; 
      } 
      else if (leftargop.equals("|R"))
      { // s->reject(x | P)->isEmpty()
        System.out.println(">> OCL efficiency smell (OES): Inefficient comparison: " + this);
          
        BinaryExpression res = 
            new BinaryExpression("!", leftargleft,
                                 leftargpred); 
        return res; 
      } 
    } 
    else if (operator.equals("->any") || 
             operator.equals("->first"))
    { // col->select(P)->any() should be col->any(P), etc
      // col->collect(x|e)->first() should be 
      //                let x = col->first() in e
           

      if (argument instanceof BinaryExpression) 
      { BinaryExpression lbe = (BinaryExpression) argument; 

        if (lbe.operator.equals("|"))
        { // lbe.left | lbe.right
          
          BinaryExpression res = 
            new BinaryExpression("|A", lbe.left, lbe.right); 

          System.out.println(">> OCL efficiency smell (OES): Inefficient " + operator + " expression: " + 
            this + 
            "\n! Replaced by " + res);

          return res; 
        }
        else if (lbe.operator.equals("->select"))
        { // Inefficient col->select(P)->any() expression
          // instead use:    col->any(P)

          BinaryExpression res = 
            new BinaryExpression("->any", lbe.left, lbe.right); 

          System.out.println(">> OCL efficiency smell (OES): Inefficient " + operator + " expression: " + 
            this + 
            "\n! Replaced by " + res);

          return res; 
        }
        else if (lbe.operator.equals("|R"))
        { UnaryExpression notR = 
            new UnaryExpression("not", lbe.right); 
          BinaryExpression res = 
            new BinaryExpression("|A", lbe.left, notR); 

          System.out.println(">> OCL efficiency smell (OES): Inefficient " + operator + " expression: " + 
            this + 
            "\n! Replaced by " + res);

          return res; 
        }
        else if (lbe.operator.equals("->reject"))
        { UnaryExpression notR = 
            new UnaryExpression("not", lbe.right); 
          BinaryExpression res = 
            new BinaryExpression("->any", lbe.left, notR); 

          System.out.println(">> OCL efficiency smell (OES): Inefficient " + operator + " expression: " + 
            this + 
            "\n! Replaced by " + res);
  
          return res; 
        }
        else if (lbe.operator.equals("|C") && 
                 operator.equals("->first"))
        { Expression res = Expression.simplifyFirst(lbe); 
 
          System.out.println(">> OCL efficiency smell (OES): Inefficient " + operator + " expression: " + 
            this + 
            "\n! Replaced by " + res);
  
          return res; 
        }
        else if (lbe.operator.equals("|C") && 
                 operator.equals("->any"))
        { Expression res = Expression.simplifyAny(lbe); 
 
          System.out.println(">> OCL efficiency smell (OES): Inefficient " + operator + " expression: " + 
            this + 
            "\n! Replaced by " + res);
  
          return res; 
        }
      } 
    }
    else if ("->sort".equals(operator))
    { if (argument.isSorted())
      { // Redundant ->sort operation:  
        // Argument is already sorted.

        System.out.println("! OES: Redundant ->sort operation: " + 
            this + 
            "\n! Argument is already sorted.");
        return arg; 
      }
    }
  
    UnaryExpression res = (UnaryExpression) clone(); 
    res.argument = arg; 
    return res; 
  } 
 


  public java.util.Map collectionOperatorUses(int level, 
                                      java.util.Map res, 
                                      Vector vars)
  { //  level |-> [x.setAt(i,y), etc]

    argument.collectionOperatorUses(level, res, vars); 

    if (operator.equals("->sort") || 
        operator.equals("->unionAll") || 
        operator.equals("->intersectAll") || 
        operator.equals("->concatenateAll") ||
        operator.equals("->any") ||
        operator.equals("->first") ||
        operator.equals("->last") ||
        operator.equals("->max") ||
        operator.equals("->min"))
    { Vector opers = (Vector) res.get(level); 
      if (opers == null) 
      { opers = new Vector(); } 
      opers.add(this); 
      res.put(level, opers); 

      Vector vuses = variablesUsedIn(vars); 
      if (level > 1 && vuses.size() == 0)
      { System.out.println(); 
        System.out.println("!! (LCE) flaw: The expression " + this + " is independent of the iterator variables " + vars + "\n" + 
          "Use Extract local variable to optimise.");
        System.out.println(); 
        refactorELV = true;  
      }
          
      return res; 
    } 

    return res; 
  } // also any in the argument. 


  public int syntacticComplexity() 
  { int res = argument.syntacticComplexity() + 1; 

    if ("lambda".equals(operator) && accumulator != null) 
    { Type typ = accumulator.getType(); 
      if (typ != null) 
      { res = res + typ.complexity(); } 
    } 
 
    return res; 
  } 

  public int maximumReferenceChain() 
  { return argument.maximumReferenceChain(); } 

  public int cyclomaticComplexity() 
  { int res = argument.cyclomaticComplexity(); 
    return res; 
  } 


  public String updateFormEq(String language, java.util.Map env, String op, String val, Expression var, boolean local)
  { // update   this = var  to this, val is
    // query form of var. 

    if ("!".equals(operator))
    { if (argument instanceof BinaryExpression) 
      { BinaryExpression be = (BinaryExpression) argument; 
        if (be.left.isRef() && "+".equals(be.operator))
        { // lqf[rqf] := var
          String lqf = be.left.queryForm(language,env,local); 
          String rqf = be.right.queryForm(language,env,local);
          return "    " + lqf + "[" + rqf + "] = " + val + ";";
        } 
      }     
      String lhs = queryForm(language, env, local); 
      String rhs = var.queryForm(language, env, local); 
      return "    " + lhs + " = " + rhs + ";";
    }

   if ("->any".equals(operator))   // argument->any() := var
   { BinaryExpression isin = new BinaryExpression(":", var, argument );
     BinaryExpression se = new BinaryExpression("->includes", argument, var);
     String setin = se.updateForm(language,env,local);
    
     return "if (" + isin.queryForm(language,env,local) + ") { } else { " + setin + " }";
   }  
   else if ("->last".equals(operator))    // argument->last() := var
   { UnaryExpression selfsize = 
         new UnaryExpression("->size", argument);

     if (argument instanceof UnaryExpression && 
         "->front".equals(
             ((UnaryExpression) argument).operator))
     { // arg->front()->last() := var  is 
       // arg[arg->size()-1] := var
       Expression innerarg = 
           ((UnaryExpression) argument).getArgument(); 
       BasicExpression inargclone = 
           (BasicExpression) innerarg.clone();
       BinaryExpression newind = 
           new BinaryExpression("-", selfsize, 
                            new BasicExpression(1)); 
       inargclone.setArrayIndex(newind);
       BinaryExpression setprelast = 
           new BinaryExpression("=", inargclone, var);
       return setprelast.updateForm(language,env,local);
     }
  
     BinaryExpression eqempty = new BinaryExpression("=", selfsize, new BasicExpression(0) );
     SetExpression se = new SetExpression();
     se.setOrdered(true);
     se.addElement(var);
     BinaryExpression setarge = new BinaryExpression("=", argument, se);
     String setarg = setarge.updateForm(language,env,local);
     BasicExpression argclone = (BasicExpression) argument.clone();
     argclone.setArrayIndex(selfsize);
     BinaryExpression setlaste = new BinaryExpression("=", argclone, var);
     String setlast = setlaste.updateForm(language,env,local);
    
     return "if (" + eqempty.queryForm(language,env,local) + ") { " + setarg + " } else { " + setlast + " }";
    }  
    else if ("->first".equals(operator))
    { /* UnaryExpression selfsize = new UnaryExpression("->size", argument);
      BinaryExpression eqempty = new BinaryExpression("=", selfsize, new BasicExpression(0) );
      SetExpression se = new SetExpression();
      se.setOrdered(true);
      se.addElement(var);
      BinaryExpression setarge = new BinaryExpression("=", argument, se);
      String setarg = setarge.updateForm(language,env,local); */

      BasicExpression argclone = (BasicExpression) argument.clone();
      argclone.setArrayIndex(new BasicExpression(1));
      argclone.setType(type);
      argclone.setElementType(elementType);  
      argclone.setArrayType(argument.getType()); 

      BinaryExpression setfirste = new BinaryExpression("=", argclone, var);
      String setfirst = setfirste.updateForm(language,env,local);
    
      /* return "if (" + eqempty.queryForm(language,env,local) + ") { " + setarg + " } else { " + setfirst + " }";

      return BasicExpression.updateFormEqIndex(
                language,argument,
                new BasicExpression(1),val,var,env,local); */
 
      return setfirst;  
    }  
    else if ("->front".equals(operator))
    { UnaryExpression selfsize = new UnaryExpression("->size", argument);

      if (argument instanceof UnaryExpression && 
          "->front".equals(
             ((UnaryExpression) argument).operator))
      { // arg->front()->front() := var  is 
        // arg := arg.setSubrange(1, arg->size()-2, var)

       Expression innerarg = 
           ((UnaryExpression) argument).getArgument(); 
       BasicExpression inargclone = 
           (BasicExpression) innerarg.clone();
       BinaryExpression newind = 
           new BinaryExpression("-", selfsize, 
                            new BasicExpression(2));
       Vector pars = new Vector(); 
       pars.add(new BasicExpression(1)); 
       pars.add(newind); 
       pars.add(var); 

       BasicExpression newval = 
          BasicExpression.newFunctionBasicExpression(
                          "setSubrange", inargclone, pars); 

       BinaryExpression setprelast = 
           new BinaryExpression("=", inargclone, newval);
       return setprelast.updateForm(language,env,local);
     }

     BinaryExpression neqempty = new BinaryExpression(">", selfsize, new BasicExpression(0) );
      SetExpression se = new SetExpression();
      se.setOrdered(true);
      Type t = argument.getElementType();
      Expression d = t.getDefaultValueExpression(); 
      se.addElement(d);
      BasicExpression argclone = (BasicExpression) argument.clone();
      argclone.setArrayIndex(selfsize);
      BinaryExpression catlast = new BinaryExpression("->append", var, argclone);
      BinaryExpression catd = new BinaryExpression("^", var, se);

      BinaryExpression setarge = new BinaryExpression("=", argument, catlast);
      String setarg = setarge.updateForm(language,env,local);
      BinaryExpression setde = new BinaryExpression("=", argument, catd);
      String setd = setde.updateForm(language,env,local);
    
      return "if (" + neqempty.queryForm(language,env,local) + ") { " + setarg + " } else { " + setd + " }";
   }  
   else if ("->tail".equals(operator))
   { UnaryExpression selfsize = new UnaryExpression("->size", argument);
     BinaryExpression neqempty = new BinaryExpression(">", selfsize, new BasicExpression(0) );
     SetExpression se = new SetExpression();
     se.setOrdered(true);
     Type t = argument.getElementType();
     Expression d = t.getDefaultValueExpression(); 
     se.addElement(d);
     BasicExpression argclone = (BasicExpression) argument.clone();
     argclone.setArrayIndex(new BasicExpression(1));
     BinaryExpression catlast = new BinaryExpression("->append", argclone, var);
     BinaryExpression catd = new BinaryExpression("^", se, var);

     BinaryExpression setarge = new BinaryExpression("=", argument, catlast);
     String setarg = setarge.updateForm(language,env,local);
     BinaryExpression setde = new BinaryExpression("=", argument, catd);
     String setd = setde.updateForm(language,env,local);
    
     return "if (" + neqempty.queryForm(language,env,local) + ") { " + setarg + " } else { " + setd + " }";
   }  
   else if ("->max".equals(operator))
   { String ix = Identifier.nextIdentifier("_ind");
     BasicExpression ivar = new BasicExpression(ix,0);
     ivar.setUmlKind(VARIABLE);
     ivar.setType(argument.getElementType()); 
     BinaryExpression ind = new BinaryExpression(":", ivar, argument);
     BinaryExpression greaterset = new BinaryExpression(">", ivar, var); 
     BinaryExpression rejectgreater = new BinaryExpression("|R", ind, greaterset);
     rejectgreater.setType(argument.getType()); 
     rejectgreater.setElementType(argument.getElementType()); 
     BasicExpression argclone = (BasicExpression) argument.clone();
    
     BinaryExpression setarge = new BinaryExpression("=", argument, rejectgreater);
     String assign = setarge.updateForm(language,env,local);
     BinaryExpression isin = new BinaryExpression(":", var, argument );
     BinaryExpression se = new BinaryExpression("->includes", argument, var);
     String setin = se.updateForm(language,env,local);
    
     return assign + "\n" +
       "    if (" + isin.queryForm(language,env,local) + ") { } else { " + setin + " }"; 
   }  
   else if ("->min".equals(operator))
   { String ix = Identifier.nextIdentifier("_ind");
     BasicExpression ivar = new BasicExpression(ix,0);
     ivar.setUmlKind(VARIABLE);
     ivar.setType(argument.getElementType()); 
     BinaryExpression ind = new BinaryExpression(":", ivar, argument);
     BinaryExpression lessset = new BinaryExpression("<", ivar, var); 
     BinaryExpression rejectless = new BinaryExpression("|R", ind, lessset);
     rejectless.setType(argument.getType()); 
     rejectless.setElementType(argument.getElementType()); 
     BasicExpression argclone = (BasicExpression) argument.clone();
    
     BinaryExpression setarge = new BinaryExpression("=", argument, rejectless);
     String assign = setarge.updateForm(language,env,local);
     BinaryExpression isin = new BinaryExpression(":", var, argument );
     BinaryExpression se = new BinaryExpression("->includes", argument, var);
     String setin = se.updateForm(language,env,local);
    
     return assign + "\n" +
       "    if (" + isin.queryForm(language,env,local) + ") { } else { " + setin + " }"; 
    }
    else if ("->sqr".equals(operator))
    { UnaryExpression sqrt = new UnaryExpression("->sqrt", var);
      BinaryExpression be = new BinaryExpression("=", argument, sqrt);
      return be.updateForm(language, env, local);
    }
    else if ("->sqrt".equals(operator))
    { UnaryExpression sqr = new UnaryExpression("->sqr", var);
      BinaryExpression be = new BinaryExpression("=", argument, sqr);
      return be.updateForm(language, env, local);
    }
    else if ("->exp".equals(operator))
    { UnaryExpression sqr = new UnaryExpression("->log", var);
      BinaryExpression be = new BinaryExpression("=", argument, sqr);
      return be.updateForm(language, env, local);
    }
    else if ("->log".equals(operator))
    { UnaryExpression sqr = new UnaryExpression("->exp", var);
      BinaryExpression be = new BinaryExpression("=", argument, sqr);
      return be.updateForm(language, env, local);
    }
    else if ("->sin".equals(operator))
    { UnaryExpression sqr = new UnaryExpression("->asin", var);
      BinaryExpression be = new BinaryExpression("=", argument, sqr);
      return be.updateForm(language, env, local);
    }
    else if ("->cos".equals(operator))
    { UnaryExpression sqr = new UnaryExpression("->acos", var);
      BinaryExpression be = new BinaryExpression("=", argument, sqr);
      return be.updateForm(language, env, local);
    }
    else if ("->tan".equals(operator))
    { UnaryExpression sqr = new UnaryExpression("->atan", var);
      BinaryExpression be = new BinaryExpression("=", argument, sqr);
      return be.updateForm(language, env, local);
    }
    else if ("->characters".equals(operator))
    { UnaryExpression sqr = new UnaryExpression("->sum", var);
      BinaryExpression be = new BinaryExpression("=", argument, sqr);
      return be.updateForm(language, env, local);
    }
    else if ("->reverse".equals(operator))
    { UnaryExpression sqr = new UnaryExpression("->reverse", var);
      BinaryExpression be = new BinaryExpression("=", argument, sqr);
      return be.updateForm(language, env, local);
    }
    else if ("->sort".equals(operator) || 
             "->asSequence".equals(operator))
    { if (Type.isSequenceType(argument.getType()))
      { BinaryExpression assign = new BinaryExpression("=", argument, var); 
        return assign.updateForm(language, env, local);
      }
      UnaryExpression sqr = new UnaryExpression("->asSet", var);
      BinaryExpression be = new BinaryExpression("=", argument, sqr);
      return be.updateForm(language, env, local);
    }
    else if ("not".equals(operator))
    { UnaryExpression sqr = new UnaryExpression("not", var);
      BinaryExpression be = new BinaryExpression("=", argument, sqr);
      return be.updateForm(language, env, local);
    }
    else if ("-".equals(operator))
    { UnaryExpression mins = new UnaryExpression("-", var);
      BinaryExpression be = new BinaryExpression("=", argument, mins);
      return be.updateForm(language, env, local);
    }
    else if ("?".equals(operator))
    { String lhs = queryForm(language, env, local); 
      String rhs = var.queryForm(language, env, local); 
      return "    " + lhs + " = " + rhs + ";";
    }
    // else if ("let".equals(operator))

    return " /* No update form for " + this + " := " + var + " */ "; 
  }

public String updateFormNotIn(String language, java.util.Map env, Expression var, boolean local)
{ if ("->asSet".equals(operator) || 
      "->asSequence".equals(operator) || 
      "->asOrderedSet".equals(operator) || 
      "->sort".equals(operator))
  { Expression e1 = new BinaryExpression("/:", var, argument);
    return e1.updateForm(language,env,local);
  }
  else if ("->tail".equals(operator))
  { UnaryExpression e1 = new UnaryExpression("->size", argument);
    BinaryExpression gone = new BinaryExpression(">", e1, new BasicExpression(1));
    SetExpression seqvar = new SetExpression();
    seqvar.setOrdered(true);
    seqvar.addElement(var);
    Expression e = new BinaryExpression("-", this, seqvar);
    Expression e2 = new BinaryExpression("->prepend", e, new UnaryExpression("->first", argument));
    Expression e3 = new BinaryExpression("=", argument, e2);
    String ifcase = e3.updateForm(language,env,local);
  
    String test = gone.queryForm(language,env,local);
    return " if (" + test + ") { " + ifcase + " } ";
  }
  else if ("->reverse".equals(operator))
  { Expression assign = new BinaryExpression("/:", var, argument);
    return assign.updateForm(language,env,local);
  }
  else if ("->front".equals(operator))
  { UnaryExpression e1 = new UnaryExpression("->size", argument);
    BinaryExpression gone = new BinaryExpression(">", e1, new BasicExpression(1));
    SetExpression seqvar = new SetExpression();
    seqvar.setOrdered(true);
    seqvar.addElement(var);
    Expression e = new BinaryExpression("-", this, seqvar);
    Expression e2 = new BinaryExpression("->append", e, new UnaryExpression("->last", argument));
    Expression e3 = new BinaryExpression("=", argument, e2);
    String ifcase = e3.updateForm(language,env,local);
  
    String test = gone.queryForm(language,env,local);
    return " if (" + test + ") { " + ifcase + " }";
  }
  return "/* No update form for " + var + " : " + this + " */";
}

public String updateFormIn(String language, java.util.Map env, Expression var, boolean local)
{ if ("->asSet".equals(operator) || 
      "->asSequence".equals(operator) || 
      "->asOrderedSet".equals(operator) || 
      "->sort".equals(operator))
  { Expression e1 = new BinaryExpression(":", var, argument);
    return e1.updateForm(language,env,local);
  }
  else if ("->tail".equals(operator))
  { UnaryExpression e1 = new UnaryExpression("->size", argument);
    BinaryExpression eqzero = new BinaryExpression("=", e1, new BasicExpression(0));
    SetExpression twovar = new SetExpression();
    twovar.setOrdered(true);
    twovar.addElement(var);
    twovar.addElement(var);
    Expression e = new BinaryExpression("=", argument, twovar);
    Expression e2 = new BinaryExpression("->append", argument, var);
    Expression e3 = new BinaryExpression("=", argument, e2);
    String ifcase = e.updateForm(language,env,local);
    String elsecase = e3.updateForm(language,env,local);
    String test = eqzero.queryForm(language,env,local);
    return " if (" + test + ") { " + ifcase + " }\n" +
      "  else { " + elsecase + " } ";  
  }
  else if ("->reverse".equals(operator))
  { Expression e1 = new BinaryExpression("->prepend", argument, var);
    Expression assign = new BinaryExpression("=", argument, e1);
    return assign.updateForm(language,env,local);
  }
  else if ("->front".equals(operator))
  { UnaryExpression e1 = new UnaryExpression("->size", argument);
    BinaryExpression eqzero = new BinaryExpression("=", e1, new BasicExpression(0));
    SetExpression twovar = new SetExpression();
    twovar.setOrdered(true);
    twovar.addElement(var);
    twovar.addElement(var);
    Expression e = new BinaryExpression("=", argument, twovar);
    Expression e2 = new BinaryExpression("->prepend", argument, var);
    Expression e3 = new BinaryExpression("=", argument, e2);
    String ifcase = e.updateForm(language,env,local);
    String elsecase = e3.updateForm(language,env,local);
    String test = eqzero.queryForm(language,env,local);
    return " if (" + test + ") { " + ifcase + " }\n" +
      "  else { " + elsecase + " } ";  
  }
  return "/* No update form for " + var + " : " + this + " */";
}

public String updateFormSubtract(String language, java.util.Map env, Expression var, boolean local)
{ if ("->asSet".equals(operator) || 
      "->asSequence".equals(operator) ||
      "->asOrderedSet".equals(operator) ||
      "->sort".equals(operator))
  { Expression e1 = new BinaryExpression("->excludesAll", argument, var);
    return e1.updateForm(language,env,local);
  }
  else if ("->tail".equals(operator))
  { UnaryExpression e1 = new UnaryExpression("->size", argument);
    BinaryExpression gone = new BinaryExpression(">", e1, new BasicExpression(1));
    Expression e = new BinaryExpression("-", this, var);
    e.setBrackets(true);
    Expression e2 = new BinaryExpression("->prepend", e, new UnaryExpression("->first", argument));
    Expression e3 = new BinaryExpression("=", argument, e2);
    String ifcase = e3.updateForm(language,env,local);
  
    String test = gone.queryForm(language,env,local);
    return " if (" + test + ") { " + ifcase + " } ";
  }
  else if ("->reverse".equals(operator))
  { Expression assign = new BinaryExpression("->excludesAll", argument, var);
    return assign.updateForm(language,env,local);
  }
  else if ("->front".equals(operator))
  { UnaryExpression e1 = new UnaryExpression("->size", argument);
    BinaryExpression gone = new BinaryExpression(">", e1, new BasicExpression(1));
    Expression e = new BinaryExpression("-", this, var);
    e.setBrackets(true);
    Expression e2 = new BinaryExpression("->append", e, new UnaryExpression("->last", argument));
    Expression e3 = new BinaryExpression("=", argument, e2);
    String ifcase = e3.updateForm(language,env,local);
  
    String test = gone.queryForm(language,env,local);
    return " if (" + test + ") { " + ifcase + " } ";
  }
  return "/* No update form for " + this + "->excludesAll(" + var + ") */";
}

public String updateFormSubset(String language, java.util.Map env, Expression var, boolean local)
{ if ("->asSet".equals(operator) || 
      "->asSequence".equals(operator) ||
      "->asOrderedSet".equals(operator) ||
      "->sort".equals(operator))
  { Expression e1 = new BinaryExpression("<:", var, argument);
    return e1.updateForm(language,env,local);
  }
  else if ("->tail".equals(operator) && elementType != null)
  { UnaryExpression e1 = new UnaryExpression("->size", argument);
    BinaryExpression eqzero = new BinaryExpression("=", e1, new BasicExpression(0));
    SetExpression defaultelem = new SetExpression();
    defaultelem.setOrdered(true);
    defaultelem.addElement(elementType.getDefaultValueExpression());
    Expression dcatvar = new BinaryExpression("^", defaultelem, var);
    Expression e = new BinaryExpression("=", argument, dcatvar);
    Expression e2 = new BinaryExpression("^", argument, var);
    Expression e3 = new BinaryExpression("=", argument, e2);
    String ifcase = e.updateForm(language,env,local);
    String elsecase = e3.updateForm(language,env,local);
    String test = eqzero.queryForm(language,env,local);
    return " if (" + test + ") { " + ifcase + " }\n" +
      "  else { " + elsecase + " } ";  
  }
  else if ("->reverse".equals(operator))
  { Expression e1 = new UnaryExpression("->reverse", var);
    Expression cata = new BinaryExpression("^", e1, argument);
    Expression assign = new BinaryExpression("=", argument, cata);
    return assign.updateForm(language,env,local);
  }
  else if ("->front".equals(operator) && elementType != null)
  { UnaryExpression e1 = new UnaryExpression("->size", argument);
    BinaryExpression eqzero = new BinaryExpression("=", e1, new BasicExpression(0));
    SetExpression twovar = new SetExpression();
    twovar.setOrdered(true);
    twovar.addElement(elementType.getDefaultValueExpression());
    Expression varcatd = new BinaryExpression("^", var, twovar);
    Expression e = new BinaryExpression("=", argument, varcatd);
    Expression e2 = new BinaryExpression("^", var, argument);
    Expression e3 = new BinaryExpression("=", argument, e2);
    String ifcase = e.updateForm(language,env,local);
    String elsecase = e3.updateForm(language,env,local);
    String test = eqzero.queryForm(language,env,local);
    return " if (" + test + ") { " + ifcase + " }\n" +
      "  else { " + elsecase + " } ";  
  }
  return "/* No update form for " + var + " <: " + this + " */";
}


 
  public Statement generateDesign(java.util.Map env, boolean local)
  { String data = argument + ""; 
    if ("->isDeleted".equals(operator))
    { if (argument.umlkind == CLASSID && (argument instanceof BasicExpression) && 
          ((BasicExpression) argument).arrayIndex == null) 
      { BasicExpression killset = new BasicExpression("allInstances"); 
        killset.setObjectRef(argument); 
        killset.setType(argument.getType()); 
        killset.setElementType(argument.getElementType()); 
        BasicExpression killop = new BasicExpression("killAll" + data); 
        killop.umlkind = UPDATEOP; 
        killop.addParameter(killset); 
        return new InvocationStatement(killop); 
      } // Could be abstract class

      Type argtype = argument.type; 
      if (argtype == null || !argtype.isEntity())
      { System.err.println("!! Warning: can only delete class instances, not: " + argument);
        // But remove the argument 
        // element from any collection it belongs
        // to, as far as possible. 

        if (argument instanceof BasicExpression && 
((BasicExpression) argument).arrayIndex != null) 
        { // remove this element of the argument
          BasicExpression argcopy = (BasicExpression) argument.clone(); 
          argcopy.arrayIndex = null; 
          BinaryExpression deleteAt = new BinaryExpression("->excludingAt", argcopy, ((BasicExpression) argument).arrayIndex); 
          AssignStatement assign = new AssignStatement(argcopy, deleteAt); 
          return assign;   
        }
        return new SequenceStatement();  
      }  
 
      String eename = argtype.getName(); 
      Entity ent = null; 
      String all = ""; 
      String opname = ""; 
      if (argument.isMultiple()) 
      { all = "All";
        eename = argument.elementType.getName();
        ent = argument.elementType.entity;  
      }
      else 
      { ent = argument.type.entity; } 

      if (ent.isAbstract())
      { opname = "killAbstract" + eename; }
      else 
      { opname = "kill" + all + eename; } 
      BasicExpression killop = new BasicExpression(opname); 
      killop.umlkind = UPDATEOP; 
      killop.addParameter(argument); 
      return new InvocationStatement(killop); 
    } 
    else if ("->display".equals(operator))
    { BasicExpression dispop = null; 
      if (argument.type != null) 
      { dispop = new BasicExpression("display" + argument.type.getName()); }
      else 
      { dispop = new BasicExpression("display"); }
	  
      dispop.umlkind = UPDATEOP; 
      dispop.addParameter(argument); 
      return new InvocationStatement(dispop); 
    } // could also distinguish the type in the programming language, not here. 
    else if ("->oclIsNew".equals(operator) && (argument instanceof BasicExpression))
    { if (argument.type != null && argument.type.isEntity())
      { Entity aent = argument.type.getEntity(); 
        String ename = aent.getName(); 
        Statement skp = new InvocationStatement("skip");
        Expression isundefined = new UnaryExpression("->oclIsUndefined", argument); 
        isundefined.setType(new Type("boolean", null));
        BasicExpression createE = new BasicExpression("create" + ename); 
        createE.setParameters(new Vector()); 
        createE.setUmlKind(Expression.UPDATEOP); 
        createE.setType(new Type(aent)); 
        createE.setElementType(new Type(aent)); 
  
        AssignStatement creat = new AssignStatement(argument,createE); 
        ConditionalStatement cstat = new ConditionalStatement(isundefined, creat, skp); 
        return cstat;
      } 
      else 
      { return new InvocationStatement("skip"); }  
    }         
    else 
    { return new InvocationStatement("skip"); }  // None of the others have an update form
  }  

  public Statement statLC(java.util.Map env, boolean local)
  { String data = argument + ""; 
    if ("->isDeleted".equals(operator))
    { if (argument.umlkind == CLASSID && (argument instanceof BasicExpression) && 
          ((BasicExpression) argument).arrayIndex == null) 
      { BasicExpression killset = new BasicExpression("allInstances"); 
        killset.setObjectRef(argument); 
        killset.setType(argument.getType()); 
        killset.setElementType(argument.getElementType()); 
        BasicExpression killop = new BasicExpression("killAll" + data); 
        killop.umlkind = UPDATEOP; 
        killop.addParameter(killset); 
        return new InvocationStatement(killop); 
      } // Could be abstract class

      Type argtype = argument.type; 
      if (argtype == null || !argtype.isEntity())
      { System.err.println("!! Warning: can only delete class instances, not: " + argument);

        if (argument instanceof BasicExpression && 
((BasicExpression) argument).arrayIndex != null) 
        { // remove this element of the argument
          BasicExpression argcopy = (BasicExpression) argument.clone(); 
          argcopy.arrayIndex = null; 
          BinaryExpression deleteAt = new BinaryExpression("->excludingAt", argcopy, ((BasicExpression) argument).arrayIndex); 
          AssignStatement assign = new AssignStatement(argcopy, deleteAt); 
          return assign;   
        }
        return new SequenceStatement();  
      }  
 
      String eename = argtype.getName(); 
      Entity ent = null; 
      String all = ""; 
      String opname = ""; 
      if (argument.isMultiple()) 
      { all = "All";
        eename = argument.elementType.getName();
        ent = argument.elementType.entity;  
      }
      else 
      { ent = argument.type.entity; } 

      if (ent.isAbstract())
      { opname = "killAbstract" + eename; }
      else 
      { opname = "kill" + all + eename; } 
      BasicExpression killop = new BasicExpression(opname); 
      killop.umlkind = UPDATEOP; 
      killop.addParameter(argument); 
      return new InvocationStatement(killop); 
    } 
    else if ("->display".equals(operator))
    { BasicExpression dispop = new BasicExpression("display" + argument.type.getName()); 
      dispop.umlkind = UPDATEOP; 
      dispop.addParameter(argument); 
      return new InvocationStatement(dispop); 
    } // could also distinguish the type in the programming language, not here. 
    else 
    { return new InvocationStatement("skip"); }  // None of the others have an update form
  }  


  public boolean isExecutable()
  { if (operator.equals("->isDeleted")) 
    { return true; } 

    if (operator.equals("->display")) 
    { return true; } 

    return false; 
  } 

  public String cstlQueryForm(java.util.Map fmap)
  { String pre = argument.cstlQueryForm(fmap); 
    if (operator.startsWith("->"))
    { String op = operator.substring(2,operator.length()); 
      return pre + "`" + op; 
    } 
    return this + ""; 
  } 

  public String cstlConditionForm(java.util.Map fmap)
  { String pre = argument.cstlConditionForm(fmap); 
    if ("not".equals(operator))
    { if (argument instanceof BinaryExpression) 
      { return ((BinaryExpression) argument).negatedcstlConditionForm(fmap); } 
      return "not " + pre; 
    }

    if ("->isEmpty".equals(operator))
    { return pre + " empty"; }

    if ("->notEmpty".equals(operator))
    { return pre + " not empty"; }

    if (operator.startsWith("->"))
    { String op = operator.substring(2,operator.length()); 
      return pre + "`" + op; 
    } 
    return pre + ""; 
  } 
  
  public Expression evaluate(ModelSpecification sigma, 
                             ModelState beta)
  { // Special cases for !e and ?x in sandboxed memory 
    // space in sigma. 

    Expression pre = argument.evaluate(sigma, beta);
    return Expression.simplify(operator, pre); 
  } 

  public String updateForm(java.util.Map env, boolean local)
  { String cont = "Controller.inst()"; 
    String pre = argument.queryForm(env,local);

    if (extensionoperators.containsKey(operator))
    { String op = operator;
      String opjava = Expression.getOperatorJava(op); 
      if (opjava != null && opjava.length() > 0)
      { return opjava.replaceAll("_1",pre); } 
      if (operator.startsWith("->"))
      { op = operator.substring(2,operator.length()); } 
      return pre + "." + op + "()";
    } 

    /* if ("let".equals(operator) && accumulator != null)
    { // let acc : T = init in argument is { acc; argumentUF }

      String accname = accumulator.getName(); 
      Expression init = accumulator.getInitialExpression(); 
      Type acctype = accumulator.getType(); 
      if (init == null && acctype != null)
      { init = acctype.getDefaultValueExpression(); }  
      String res = "  { " + acctype.getJava() + " " + 
        accname + " = " + init.queryForm(env,local) + ";\n    " +
        argument.updateForm(env,local) + "  }\n"; 
      return res; 
    } */ 

    if ("->isDeleted".equals(operator))
    { if (argument.umlkind == CLASSID && 
          (argument instanceof BasicExpression) && 
          ((BasicExpression) argument).arrayIndex == null) 
      { String data = argument + ""; 
        String datas = data.toLowerCase() + "s"; 
        return "  List _" + data + " = new Vector(); \n" + 
               "  _" + data + ".addAll(" + cont + "." + datas + "); \n" + 
               "  " + cont + ".killAll" + data + "(_" + data + ");\n"; 
      } // Could be abstract class

      Type argtype = argument.type; 
      if (argtype == null || !argtype.isEntity())
      { System.err.println("!! Warning: can only delete class instances, not: " + argument);

        if (argument instanceof BasicExpression && 
((BasicExpression) argument).arrayIndex != null) 
        { // remove this element of the argument
          BasicExpression argcopy = (BasicExpression) argument.clone(); 
          argcopy.arrayIndex = null; 
          String precopy = argcopy.queryForm(env,local);
          String ind = ((BasicExpression) argument).arrayIndex.queryForm(env,local); 
          return precopy + ".remove(" + ind + " - 1);";   
        }
        else if (argument instanceof BasicExpression && 
((BasicExpression) argument).getData().equals("subrange")) 
        { // s.subrange(i,j)->isDeleted() means 
          // s := s.subrange(1,i-1)^s.subrange(j+1,s.size)
          // for sequences, likewise for strings

          BasicExpression argcopy = (BasicExpression) argument.clone(); 
          Expression updatedVar = argcopy.getObjectRef(); 
          String precopy = updatedVar.queryForm(env,local);
          Vector pars = argcopy.getParameters(); 
          Expression par1 = (Expression) pars.get(0); 
          String ind1 = par1.queryForm(env,local);
          String subrange1 = 
            "Set.subrange(" + precopy + ",1," + 
                          ind1 + "-1)"; 
          if (pars.size() > 1) 
          { Expression par2 = (Expression) pars.get(1); 
            String ind2 = par2.queryForm(env,local);
            String subrange2 = 
              "Set.subrange(" + precopy + ", " + 
                            ind2 + "+1," + 
                            precopy + ".size())";
            if (argument.isSequence())
            { return precopy + " = Set.concatenate(" + subrange1 + ", " + subrange2 + ");"; } 
            else 
            { return precopy + " = " + subrange1 + " + (" + subrange2 + ");"; }  
          }  
            
          return precopy + " = " + subrange1 + ";"; 
        }
        else if (argument instanceof BasicExpression &&
                 Type.isCollectionType(argument.getType()))
        { // Or, if it is a collection/map, clear the 
          // collection/Map. 

          return pre + ".clear();"; 
        } 

        return "{}";  
      }  
 

      String eename = argtype.getName(); 
      Entity ent = null; 
      String all = ""; 
      if (argument.isMultiple() && argument.elementType != null) 
      { all = "All";
        eename = argument.elementType.getName();
        ent = argument.elementType.entity;  
      }
      else 
      { ent = argtype.entity; } 

      if (ent.isAbstract())
      { return cont + ".killAbstract" + eename + "(" + pre + ");"; } 
      return cont + ".kill" + all + eename + "(" + pre + ");"; 
    } 
    else if ("->display".equals(operator))
    { String aqf = argument.queryForm(env,local); 
      return "  System.out.println(\"\" + " + aqf + ");\n"; 
    } 
    else if ("->oclIsNew".equals(operator) && (argument instanceof BasicExpression))
    { Type argt = argument.getType(); 
      if (argt != null && argt.isEntity())
      { Entity argent = argt.getEntity(); 
        String ename = argent.getName(); 
        String aqf = argument.queryForm(env,local); 
        return "  if (" + aqf + " == null) { " + aqf + " = Controller.inst().create" + ename + "(); }\n"; 
      } 
      return " /* No valid update form for " + this + " */ ";    
    } 
    else 
    { return " /* No valid update form for " + this + " */ "; }  
    // None of the others have an update form
  }

  public String updateFormJava6(java.util.Map env, boolean local)
  { String cont = "Controller.inst()"; 
    String pre = argument.queryFormJava6(env,local);

    if (extensionoperators.containsKey(operator))
    { String op = operator;
      String opjava = Expression.getOperatorJava(op); 
      if (opjava != null && opjava.length() > 0)
      { return opjava.replaceAll("_1",pre); } 
      if (operator.startsWith("->"))
      { op = operator.substring(2,operator.length()); } 
      return pre + "." + op + "()";
    } 

    /* if ("let".equals(operator) && accumulator != null)
    { // let acc : T = init in argument is { acc; argumentUF }
      String accname = accumulator.getName(); 
      Expression init = accumulator.getInitialExpression(); 
      Type acctype = accumulator.getType(); 
      if (init == null && acctype != null)
      { init = acctype.getDefaultValueExpression(); }  
      String res = "  { " + acctype.getJava6() + " " + 
        accname + " = " + init.queryFormJava6(env,local) + ";\n    " +
        argument.updateFormJava6(env,local) + "  }\n"; 
      return res; 
    } */ 

    if ("->isDeleted".equals(operator))
    { if (argument.umlkind == CLASSID && (argument instanceof BasicExpression) && 
          ((BasicExpression) argument).arrayIndex == null) 
      { String data = argument + ""; 
        String datas = data.toLowerCase() + "s"; 
        return "  ArrayList _" + data + " = new ArrayList(); \n" + 
               "  _" + data + ".addAll(" + cont + "." + datas + "); \n" + 
               "  " + cont + ".killAll" + data + "(_" + data + ");\n"; 
      } // Could be abstract class

      Type argtype = argument.type; 
      if (argtype == null || !argtype.isEntity())
      { System.err.println("!! Warning: can only delete class instances, not: " + argument);

        if (argument instanceof BasicExpression && 
((BasicExpression) argument).arrayIndex != null) 
        { // remove this element of the argument
          BasicExpression argcopy = (BasicExpression) argument.clone(); 
          argcopy.arrayIndex = null; 
          String precopy = argcopy.queryFormJava6(env,local);
          String ind = ((BasicExpression) argument).arrayIndex.queryFormJava6(env,local); 
          return precopy + ".remove(" + ind + " - 1);";   
        }
        else if (argument instanceof BasicExpression && 
((BasicExpression) argument).getData().equals("subrange")) 
        { // s.subrange(i,j)->isDeleted() means 
          // s := s.subrange(1,i-1)^s.subrange(j+1,s.size)
          // for sequences, likewise for strings

          BasicExpression argcopy = (BasicExpression) argument.clone(); 
          Expression updatedVar = argcopy.getObjectRef(); 
          String precopy = updatedVar.queryFormJava6(env,local);
          Vector pars = argcopy.getParameters(); 
          Expression par1 = (Expression) pars.get(0); 
          String ind1 = par1.queryFormJava6(env,local);
          String subrange1 = 
            "Set.subrange(" + precopy + ",1," + 
                          ind1 + "-1)"; 
          if (pars.size() > 1) 
          { Expression par2 = (Expression) pars.get(1); 
            String ind2 = par2.queryFormJava6(env,local);
            String subrange2 = 
              "Set.subrange(" + precopy + ", " + 
                            ind2 + "+1," + 
                            precopy + ".size())";
            if (argument.isSequence())
            { return precopy + " = Set.concatenate(" + subrange1 + ", " + subrange2 + ");"; } 
            else 
            { return precopy + " = " + subrange1 + " + (" + subrange2 + ");"; }  
          }  
            
          return precopy + " = " + subrange1 + ";"; 
        }
        else if (argument instanceof BasicExpression &&
                 Type.isCollectionType(argument.getType()))
        { // Or, if it is a collection/map, clear the 
          // collection/Map. 

          return pre + ".clear();"; 
        } 

        return "{}";  
      }  

      String eename = argument.type.getName(); 
      Entity ent = null; 
      String all = ""; 
      if (argument.isMultiple() && argument.elementType != null) 
      { all = "All";
        eename = argument.elementType.getName();
        ent = argument.elementType.entity;  
      }
      else 
      { ent = argument.type.entity; } 

      if (ent.isAbstract())
      { return cont + ".killAbstract" + eename + "(" + pre + ");"; } 
      return cont + ".kill" + all + eename + "(" + pre + ");"; 
    } 
    else if ("->display".equals(operator))
    { String aqf = argument.queryFormJava6(env,local); 
      return "  System.out.println(\"\" + " + aqf + ");\n"; 
    } 
    else if ("->oclIsNew".equals(operator) && (argument instanceof BasicExpression))
    { Type argt = argument.getType(); 
      if (argt != null && argt.isEntity())
      { Entity argent = argt.getEntity(); 
        String ename = argent.getName(); 
        String aqf = argument.queryFormJava6(env,local); 
        return "  if (" + aqf + " == null) { " + aqf + " = Controller.inst().create" + ename + "(); }\n"; 
      } 
      return " /* No valid update form for " + this + " */ ";    
    } 
    else 
    { return " /* No valid update form for " + this + " */ "; }  
    // None of the others have an update form
  }

  public String updateFormJava7(java.util.Map env, boolean local)
  { String cont = "Controller.inst()"; 
    String pre = argument.queryFormJava7(env,local);

    if (extensionoperators.containsKey(operator))
    { String op = operator;
      String opjava = Expression.getOperatorJava(op); 
      if (opjava != null && opjava.length() > 0)
      { return opjava.replaceAll("_1",pre); } 
      if (operator.startsWith("->"))
      { op = operator.substring(2,operator.length()); } 
      return pre + "." + op + "()";
    } 

    if ("->isDeleted".equals(operator))
    { if (argument.umlkind == CLASSID && (argument instanceof BasicExpression) && 
          ((BasicExpression) argument).arrayIndex == null) 
      { String data = argument + ""; 
        String datas = data.toLowerCase() + "s"; 
        return "  ArrayList<" + data + "> _" + data + " = new ArrayList<" + data + ">(); \n" + 
               "  _" + data + ".addAll(" + cont + "." + datas + "); \n" + 
               "  " + cont + ".killAll" + data + "(_" + data + ");\n"; 
      } // Could be abstract class

      Type argtype = argument.type; 
      if (argtype == null || !argtype.isEntity())
      { System.err.println("!! Warning: can only delete class instances, not: " + argument + " of type " + argument.getType());

        if (argument instanceof BasicExpression && 
((BasicExpression) argument).arrayIndex != null) 
        { // remove this element of the argument
          BasicExpression argcopy = (BasicExpression) argument.clone(); 
          argcopy.arrayIndex = null; 
          String precopy = argcopy.queryFormJava7(env,local);
          String ind = ((BasicExpression) argument).arrayIndex.queryFormJava7(env,local); 
          return precopy + ".remove(" + ind + " - 1);";   
        }
        else if (argument instanceof BasicExpression && 
((BasicExpression) argument).getData().equals("subrange")) 
        { // s.subrange(i,j)->isDeleted() means 
          // s := s.subrange(1,i-1)^s.subrange(j+1,s.size)
          // for sequences, likewise for strings

          BasicExpression argcopy = (BasicExpression) argument.clone(); 
          Expression updatedVar = argcopy.getObjectRef(); 
          String precopy = updatedVar.queryFormJava7(env,local);
          Vector pars = argcopy.getParameters(); 
          Expression par1 = (Expression) pars.get(0); 
          String ind1 = par1.queryFormJava7(env,local);
          String subrange1 = 
            "Ocl.subrange(" + precopy + ",1," + 
                          ind1 + "-1)"; 
          if (pars.size() > 1) 
          { Expression par2 = (Expression) pars.get(1); 
            String ind2 = par2.queryFormJava7(env,local);
            String subrange2 = 
              "Ocl.subrange(" + precopy + ", " + 
                            ind2 + "+1," + 
                            precopy + ".size())";
            if (argument.isSequence())
            { return precopy + " = Ocl.concatenate(" + subrange1 + ", " + subrange2 + ");"; } 
            else 
            { return precopy + " = " + subrange1 + " + (" + subrange2 + ");"; }  
          }  
            
          return precopy + " = " + subrange1 + ";"; 
        }
        else if (argument instanceof BasicExpression &&
                 Type.isCollectionType(argument.getType()))
        { // Or, if it is a collection/map, clear the 
          // collection/Map. 

          return pre + ".clear();"; 
        } 

        return "{}";  
      }  
    
      String eename = argument.type.getName(); 
      Entity ent = null; 
      String all = ""; 
      if (argument.isMultiple()) 
      { all = "All";
        eename = argument.elementType.getName();
        ent = argument.elementType.entity;  
      }
      else 
      { ent = argument.type.entity; } 

      if (ent.isAbstract())
      { return cont + ".killAbstract" + eename + "(" + pre + ");"; } 
      return cont + ".kill" + all + eename + "(" + pre + ");"; 
    } 
    else if ("->display".equals(operator))
    { String aqf = argument.queryFormJava7(env,local); 
      return "  System.out.println(\"\" + " + aqf + ");\n"; 
    } 
    else if ("->oclIsNew".equals(operator) && (argument instanceof BasicExpression))
    { Type argt = argument.getType(); 
      if (argt != null && argt.isEntity())
      { Entity argent = argt.getEntity(); 
        String ename = argent.getName(); 
        String aqf = argument.queryFormJava7(env,local); 
        return "  if (" + aqf + " == null) { " + aqf + " = Controller.inst().create" + ename + "(); }\n"; 
      } 
      return " /* No valid update form for " + this + " */ ";    
    } 
    else 
    { return " /* No valid update form for: " + this + " */ "; }  
    // None of the others have an update form
  }


  public String updateFormCSharp(java.util.Map env, boolean local)
  { String cont = "Controller.inst()"; 
    String pre = argument.queryFormCSharp(env,local);

    if (extensionoperators.containsKey(operator))
    { String op = operator;
      String opjava = Expression.getOperatorCSharp(op); 
      if (opjava != null && opjava.length() > 0)
      { return opjava.replaceAll("_1",pre); } 
      if (operator.startsWith("->"))
      { op = operator.substring(2,operator.length()); } 
      return pre + "." + op + "()";
    } 

    if ("->isDeleted".equals(operator))
    { if (argument.umlkind == CLASSID && (argument instanceof BasicExpression) && 
          ((BasicExpression) argument).arrayIndex == null) 
      { String data = argument + ""; 
        String datas = data.toLowerCase() + "_s"; 
        return "  ArrayList _" + data + " = new ArrayList(); \n" + 
               "  _" + data + ".AddRange(" + cont + ".get" + datas + "()); \n" + 
               "  " + cont + ".killAll" + data + "(_" + data + ");\n"; 
      } // Could be abstract class

      Type argtype = argument.type; 
      if (argtype == null || !argtype.isEntity())
      { System.err.println("!! Warning: can only delete class instances, not: " + argument);

        if (argument instanceof BasicExpression && 
((BasicExpression) argument).arrayIndex != null) 
        { // remove this element of the argument
          BasicExpression argcopy = (BasicExpression) argument.clone(); 
          argcopy.arrayIndex = null; 
          String precopy = argcopy.queryFormCSharp(env,local);
          String ind = ((BasicExpression) argument).arrayIndex.queryFormCSharp(env,local); 
          return precopy + ".RemoveAt(" + ind + " - 1);";   
        }
        else if (argument instanceof BasicExpression && 
((BasicExpression) argument).getData().equals("subrange")) 
        { // s.subrange(i,j)->isDeleted() means 
          // s := s.subrange(1,i-1)^s.subrange(j+1,s.size)
          // for sequences, likewise for strings

          BasicExpression argcopy = (BasicExpression) argument.clone(); 
          Expression updatedVar = argcopy.getObjectRef(); 
          String precopy = updatedVar.queryFormCSharp(env,local);
          Vector pars = argcopy.getParameters(); 
          Expression par1 = (Expression) pars.get(0); 
          String ind1 = par1.queryFormCSharp(env,local);
          String subrange1 = 
            "SystemTypes.subrange(" + precopy + ",1," + 
                          ind1 + "-1)"; 
          if (pars.size() > 1) 
          { Expression par2 = (Expression) pars.get(1); 
            String ind2 = par2.queryFormCSharp(env,local);
            String subrange2 = 
              "SystemTypes.subrange(" + precopy + ", " + 
                            ind2 + "+1," + 
                            precopy + ".Count)";
            if (argument.isSequence())
            { return precopy + " = SystemTypes.concatenate(" + subrange1 + ", " + subrange2 + ");"; } 
            else // string
            { return precopy + " = " + subrange1 + " + (" + subrange2 + ");"; }  
          }
        } 
        else if (argument instanceof BasicExpression &&
                 Type.isCollectionType(argument.getType()))
        { // Or, if it is a collection/map, clear the 
          // collection/Map. 

          return pre + ".Clear();"; 
        } 
  
        return "{}";  
      }  

      String eename = argument.type.getName(); 
      Entity ent = null; 
      String all = ""; 
      if (argument.isMultiple() && argument.elementType != null) 
      { all = "All";
        eename = argument.elementType.getName();
        ent = argument.elementType.entity;  
      }
      else 
      { ent = argument.type.entity; } 

      if (ent.isAbstract())
      { return cont + ".killAbstract" + eename + "(" + pre + ");"; } 
      return cont + ".kill" + all + eename + "(" + pre + ");"; 
    } 
    else if ("->display".equals(operator))
    { String aqf = argument.queryFormCSharp(env,local); 
      return "  Console.WriteLine(\"\" + " + aqf + ");\n"; 
    } 
    else if ("->oclIsNew".equals(operator) && (argument instanceof BasicExpression))
    { Type argt = argument.getType(); 
      if (argt != null && argt.isEntity())
      { Entity argent = argt.getEntity(); 
        String ename = argent.getName(); 
        String aqf = argument.queryFormCSharp(env,local); 
        return "  if (" + aqf + " == null) { " + aqf + " = Controller.inst().create" + ename + "(); }\n"; 
      } 
      return " /* No valid update form for " + this + " */ ";    
    } 
    else 
    { return " /* No valid update form for: " + this + " */ "; }  
    // None of the others have an update form
  }

  public String updateFormCPP(java.util.Map env, boolean local)
  { String cont = "Controller::inst->"; 
    String pre = argument.queryFormCPP(env,local);

    if ("->isDeleted".equals(operator))
    { if (argument.umlkind == CLASSID && (argument instanceof BasicExpression) &&  
          ((BasicExpression) argument).arrayIndex == null) 
      { String data = argument + ""; 
        String datas = cont + "get" + data.toLowerCase() + "_s()"; 
        return "  vector<" + data + "*>* _" + data + " = new vector<" + data + "*>(); \n" + 
               "  _" + data + "->insert(_" + data + "->end(), " + datas + "->begin(), " + 
                                        datas + "->end()); \n" + 
               "  " + cont + "killAll" + data + "(_" + data + ");\n"; 
      } // Could be abstract class

      Type argtype = argument.type; 
      if (argtype == null || !argtype.isEntity())
      { System.err.println("!! Warning: can only delete class instances, not: " + argument);

        if (argument instanceof BasicExpression && 
((BasicExpression) argument).arrayIndex != null) 
        { // remove this element of the argument
          BasicExpression argcopy = (BasicExpression) argument.clone(); 
          argcopy.arrayIndex = null; 
          String precopy = argcopy.queryFormCPP(env,local);
          String ind = ((BasicExpression) argument).arrayIndex.queryFormCPP(env,local); 
          return precopy + "->erase(" + precopy + "->begin() + (" + ind + "-1));";   
        }
        // and subrange
        else if (argument instanceof BasicExpression && 
((BasicExpression) argument).getData().equals("subrange")) 
        { // s.subrange(i,j)->isDeleted() means 
          // s->erase(s->begin() + (i-1), s->begin() + j)
          // for sequences, likewise for strings

          BasicExpression argcopy = 
             (BasicExpression) argument.clone(); 
          Expression updatedVar = argcopy.getObjectRef(); 
          String precopy = 
             updatedVar.queryFormCPP(env,local);
          Vector pars = argcopy.getParameters(); 
          Expression par1 = (Expression) pars.get(0); 
          String ind1 = par1.queryFormCPP(env,local);
          if (pars.size() > 1) 
          { Expression par2 = (Expression) pars.get(1); 
            String ind2 = par2.queryFormCPP(env,local);
            if (argument.isSequence())
            { return precopy + "->erase(" + 
                precopy + "->begin() + (" + ind1 + "-1), " + 
                precopy + "->begin() + " + ind2 + ");"; 
            } 
            else // strings
            { return precopy + ".erase(" + ind1 + "-1, " + 
                       ind2 + ");"; 
            }  
          }
          
          if (argument.isSequence())
          { return precopy + "->erase(" + 
                precopy + "->begin() + (" + ind1 + "-1), " + 
                precopy + "->end());"; 
          } 
          else // strings
          { return precopy + ".erase(" + ind1 + "-1);"; }  
        } 
        else if (argument instanceof BasicExpression &&
                 Type.isCollectionType(argument.getType()))
        { // Or, if it is a collection/map, clear the 
          // collection/Map. 

          return pre + "->clear();"; 
        } // also for sets

        return "{}";  
      }  

      String eename = argument.type.getName(); 
      Entity ent = null; 
      String all = ""; 
      if (argument.isMultiple() && argument.elementType != null) 
      { all = "All";
        eename = argument.elementType.getName();
        ent = argument.elementType.entity;  
      }
      else 
      { ent = argument.type.entity; } 

      if (ent.isAbstract())
      { return cont + "killAbstract" + eename + "(" + pre + ");"; } 
      return cont + "kill" + all + eename + "(" + pre + ");"; 
    } 
    else if ("->display".equals(operator))
    { String aqf = argument.queryFormCPP(env,local); 
      // If argument is a single object, dereference it: 
      if (argument.getType() != null && argument.getType().isEntity())
      { return "  cout << *" + aqf + " << endl;\n"; } 
      if (argument.getType() != null && 
          argument.getType().isCollection() && 
          argument.getElementType() != null)
      { Type elemT = argument.getElementType(); 
        String cet = elemT.getCPP(); 
        return "  cout << UmlRsdsLib<" + cet + ">::collectionToString(" + aqf + ") << endl;\n"; 
      } 
      
      return "  cout << " + aqf + " << endl;\n"; 
    } 
    else if ("->oclIsNew".equals(operator) && (argument instanceof BasicExpression))
    { Type argt = argument.getType(); 
      if (argt != null && argt.isEntity())
      { Entity argent = argt.getEntity(); 
        String ename = argent.getName(); 
        String aqf = argument.queryFormCSharp(env,local); 
        return "  if (" + aqf + " == NULL) { " + aqf + " = Controller::inst->create" + ename + "(); }\n"; 
      } 
      return " /* No valid update form for " + this + " */ ";    
    } 
    else 
    { return " /* No update form for: " + this + " */ "; }  
    // None of the others have an update form
  }

  public String updatedData()
  { return argument.updatedData(); } 
  // when this := val is a valid update

  public Vector allReadBasicExpressionData()
  { Vector res = new Vector(); 
    res = VectorUtil.union(res,
                       argument.allReadBasicExpressionData()); 
    return res; 
  } 

  public Vector writeFrame()
  { Vector res = new Vector(); 
 
    if ("->isDeleted".equals(operator))
    { if (argument instanceof BasicExpression) 
      { BasicExpression arg = (BasicExpression) argument; 
        res.add(arg.getData()); 

        if (argument.umlkind == CLASSID && 
            arg.getEntity() != null) 
        { Entity argent = arg.getEntity(); 
          res.addAll(argent.allDataDependents(new Vector())); 
        } 

        return res;  
      } 

      String eename = argument.type + ""; 
      if (argument.isMultiple() && 
          argument.elementType != null && 
          argument.elementType.isEntity()) 
      { 
        eename = argument.elementType + ""; 
        res.add(eename);
      } 
 
      res.addAll(argument.wr(new Vector())); 
      if (argument.elementType != null && 
          argument.elementType.isEntity())
      { res.addAll(argument.elementType.getEntity().allDataDependents(new Vector())); } 
      return res; 
    }

    return res; 
  }   // default. lambda should not have a write frame. 

  public Vector wr()
  { Vector res = new Vector(); 
 
    if ("->isDeleted".equals(operator))
    { if (argument.umlkind == CLASSID && (argument instanceof BasicExpression)) 
      { BasicExpression arg = (BasicExpression) argument; 
        res.add(arg.getData()); 
        if (arg.getEntity() != null) 
        { res.addAll(arg.getEntity().allDataDependents(new Vector())); } 
        return res;  
      } 
      String eename = argument.type + ""; 
      if (argument.isMultiple()) 
      { 
        eename = argument.elementType + ""; 
      } 
      res.add(eename); 
      res.addAll(argument.wr(new Vector())); 
      if (argument.elementType != null && argument.elementType.isEntity())
      { res.addAll(argument.elementType.getEntity().allDataDependents(new Vector())); } 
      return res; 
    }
    return res; 
  }   // default


  public Scope resultScope()
  { return null; }

  public boolean isUpdateable() { return false; }   // default
  // Although  e->asSequence(), e->reverse() can be updated by /: if e can, etc
 
  public boolean isOrdered()
  { if ("->last".equals(operator) || 
        "->first".equals(operator) ||
        "->max".equals(operator) || 
        "->min".equals(operator) ||
        "->any".equals(operator))
    { return Type.isSequenceType(argument.getElementType()); } 

    if ("->flatten".equals(operator) && Type.isSequenceType(argument.type))
    { return true; } 

    if ("->front".equals(operator) || 
        "->tail".equals(operator) || 
        "->asBag".equals(operator) ||
        "->asSequence".equals(operator) ||
        "->asOrderedSet".equals(operator) || 
        "->sort".equals(operator))
    { return true; } // front, tail may be sorted sets, maps

    if ( 
        "->reverse".equals(operator) || 
        operator.equals("->copy") )
    { return argument.isOrdered(); }

    return false; 
  } 

  public boolean isSorted()
  { // if ("->last".equals(operator) || "->first".equals(operator) ||
    //     "->max".equals(operator) || "->min".equals(operator) ||
    //     "->any".equals(operator))
    // { return Type.isSequenceType(argument.getElementType()); } 

    if ("->sort".equals(operator)) 
    { return true; } 

    if ("->front".equals(operator) || 
        "->tail".equals(operator) || 
        operator.equals("->copy") || 
        "->asSequence".equals(operator)) 
        // || "->reverse".equals(operator))
    { return argument.isSorted(); } 

    if ("->asOrderedSet".equals(operator))
    { return argument.isSorted(); }

    // also ->asSet, surely?

    return false; 
  } 

  public boolean isOrderedB()
  { if ("->last".equals(operator) || "->first".equals(operator) ||
        "->max".equals(operator) || "->min".equals(operator) ||
        "->any".equals(operator))
    { return Type.isSequenceType(argument.getElementType()); } 

    if ("->flatten".equals(operator) && Type.isSequenceType(argument.type))
    { return true; } 

    if ("->front".equals(operator) || 
        "->tail".equals(operator) ||
        "->asBag".equals(operator) || 
        "->asSequence".equals(operator) ||
        "->asOrderedSet".equals(operator) || "->sort".equals(operator))
    { return true; } 

    if ("->reverse".equals(operator) || 
        operator.equals("->copy"))
    { return argument.isOrderedB(); }

    return false; 
  } 
      
  // what about lambda?
  public Expression addReference(BasicExpression ref, Type t)
  { UnaryExpression res = (UnaryExpression) clone(); 
    res.argument = argument.addReference(ref,t); 
    return res; 
  } 

  public Expression addContainerReference(
            BasicExpression ref, String var, Vector excls)
  { UnaryExpression res = (UnaryExpression) clone();

    Vector newexcls = new Vector();
    newexcls.addAll(excls); 
 
    if ("lambda".equals(operator) && 
        accumulator != null) 
    { newexcls.add(accumulator.getName()); }
 
    res.argument = argument.addContainerReference(
                                         ref,var,newexcls); 
    return res; 
  } 

  public Expression replaceReference(BasicExpression ref, Type t)
  { UnaryExpression res = (UnaryExpression) clone(); 
    res.argument = argument.replaceReference(ref,t); 
    return res; 
  } 

  public Expression dereference(BasicExpression ref)
  { UnaryExpression res = (UnaryExpression) clone(); 
    res.argument = argument.dereference(ref); 
    return res; 
  } 

  public Expression invert()
  { UnaryExpression res = (UnaryExpression) clone(); 
    return res; 
  } 

  public Vector allEntitiesUsedIn()
  { return argument.allEntitiesUsedIn(); }  

  public Vector innermostEntities()
  { return argument.innermostEntities(); } 

  public Vector innermostVariables()
  { return argument.innermostVariables(); } 

  public Vector allAttributesUsedIn()
  { Vector res = argument.allAttributesUsedIn(); 

    if (operator.equals("lambda") && 
        accumulator != null)
    { Vector removals = new Vector(); 
      for (int i = 0; i < res.size(); i++) 
      { if ((accumulator + "").equals(res.get(i) + ""))
        { removals.add(res.get(i)); } 
      } 
      res.removeAll(removals); 
    } 

    return res; 
  } 

  public Vector allVariableNames()
  { if (operator.equals("lambda"))
    { Vector ss = argument.allVariableNames(); 
      if (accumulator != null)
      { ss.add(accumulator + ""); } 
      return ss; 
    }
 
    return argument.allVariableNames(); 
  } 

  public Vector allPreTerms()
  { return argument.allPreTerms(); } 

  public Vector allPreTerms(String var)
  { return argument.allPreTerms(var); } 

  public Vector getBaseEntityUses()
  { return argument.getBaseEntityUses(); }  

  public Vector getVariableUses()
  { if (operator.equals("lambda"))
    { Vector ss = argument.getVariableUses();
 
      Vector removals = new Vector(); 
      if (accumulator != null)
      { for (int i = 0; i < ss.size(); i++) 
        { Expression e1 = (Expression) ss.get(i); 
          if ((e1 + "").equals(accumulator + ""))
          { removals.add(e1); } 
        } 
        ss.removeAll(removals);
      }

      return ss; 
    } 
    
    return argument.getVariableUses(); 
  }  

  public Vector getUses(String feature)
  { return argument.getUses(feature); } 

  public Vector readFrame()
  { if (operator.equals("->display") || operator.equals("->isDeleted")) 
    { return new Vector(); } 
    return argument.readFrame(); 
  } 

  public Vector allReadFrame()
  { if (operator.equals("->display") || operator.equals("->isDeleted")) 
    { return new Vector(); } 
    return argument.allReadFrame(); 
  } 

  public Vector wr(Vector assocs)
  { Vector res = new Vector(); 
 
    if ("->isDeleted".equals(operator))
    { if (argument.umlkind == CLASSID && (argument instanceof BasicExpression)) 
      { BasicExpression arg = (BasicExpression) argument; 
        res.add(arg.getData()); 
        if (arg.getEntity() != null)  // really entity of the elementType 
        { res.addAll(arg.getEntity().allDataDependents(assocs)); } 
        return res;  
      } 
      Entity ent = null; 
      String eename = argument.type + ""; 

      if (argument.type != null) 
      { ent = argument.type.entity; } 

      if (argument.isMultiple()) 
      { 
        eename = argument.elementType + ""; 
        if (argument.elementType != null) 
        { ent = argument.elementType.entity; }
      } 
      res.add(eename); 
      res.addAll(argument.wr(assocs)); // wrdel
      if (ent != null) 
      { res.addAll(ent.allDataDependents(assocs)); } 

      return res; 
    }
    return res; 
  }

  public Vector allFeaturesUsedIn()
  { return argument.allFeaturesUsedIn(); }  

  public Vector allOperationsUsedIn()
  { return argument.allOperationsUsedIn(); }  

  public Vector equivalentsUsedIn()
  { return argument.equivalentsUsedIn(); }  

  public Vector allValuesUsedIn()
  { return argument.allValuesUsedIn(); }  

  public Vector allBinarySubexpressions()
  { return argument.allBinarySubexpressions(); }  

  public Vector updateVariables() { return new Vector(); } 

  public Vector allConjuncts()
  { return new Vector(); }

  public Vector metavariables() 
  { Vector res = new Vector(); 
    if ("lambda".equals(operator))
    { res.add("_1"); 
	  res.add("_2"); 
	  res.add("_3"); 
	  return res; 
	} 
    // else 
    return argument.metavariables(); 
  } 

  public boolean relevantOccurrence(String op, 
                                    Entity ent, String val, String f)
  { return false; }  

  public DataDependency getDataItems()
  { return argument.getDataItems(); } 

  public DataDependency rhsDataDependency()
  { return new DataDependency(); } 

  public boolean isPrimitive()   // needs to be wrapped in Java
  { if (operator.equals("->size") || operator.equals("->isDeleted") ||
        operator.equals("-") || operator.equals("not") ||
        operator.equals("?") ||   
        operator.equals("->display") || operator.equals("->abs") ||
        operator.equals("->sqrt") || operator.equals("->sqr") ||
        operator.equals("->ceil") || operator.equals("->round") ||
        operator.equals("->floor") || operator.equals("->log") ||
        operator.equals("->cbrt") || operator.equals("->log10") ||
        operator.equals("->exp") || operator.equals("->sin") ||
        operator.equals("->cos") || operator.equals("->tan") ||
        operator.equals("->sinh") || operator.equals("->cosh") ||
        operator.equals("->tanh") || operator.equals("->asin") ||
        operator.equals("->acos") || operator.equals("->atan") ||
        operator.equals("->oclIsUndefined") ||
        operator.equals("->oclIsInvalid") || 
        operator.equals("->oclIsNew") ||
        operator.equals("->isReal") || 
        operator.equals("->isInteger") || operator.equals("->isLong") || 
        operator.equals("->toInteger") || operator.equals("->toReal") || 
        operator.equals("->toLong") || 
        operator.equals("->toBoolean") || 
        operator.equals("->char2byte") || 
        operator.equals("->byte2char") || 
        operator.equals("->ord") || 
        operator.equals("->succ") || 
        operator.equals("->pred") || 
        operator.equals("->isEmpty") || 
        operator.equals("->notEmpty")) 
    { return true; } 

    // otherwise, if the argument element type is primitive
    if (operator.equals("->any") || 
        operator.equals("->last") ||
        operator.equals("->first") || 
        operator.equals("->max") ||
        operator.equals("->min") || 
        operator.equals("->sum") || operator.equals("!") ||
        operator.equals("->prd"))
    { if (argument.elementType != null) 
      { return argument.elementType.isPrimitive(); } 
      return true; 
    } 

    if ("lambda".equals(operator))
    { return argument.isPrimitive(); }

    return false; 
  } 

  public int typeCheck(final Vector sms)
  { return argument.typeCheck(sms); } 

  public boolean typeCheck(final Vector typs, 
                    final Vector ents,
                    final Vector contexts, final Vector env)
  { if (operator.equals("lambda") && accumulator != null)
    { Vector context = new Vector(); 
      context.addAll(contexts); 

      Vector env1 = new Vector(); 
      env1.addAll(env); 
      env1.add(accumulator); 
      boolean rtc = 
         argument.typeCheck(typs,ents,context,env1);
      type = new Type("Function",
                      accumulator.getType(),
                      argument.type);
      elementType = argument.elementType;

      // if there are variables in argument other than 
      // accumulator, issue a warning. 

      Vector allvars = argument.getVariableUses(); 
      for (int i = 0; i < allvars.size(); i++) 
      { Expression var = (Expression) allvars.get(i); 
        if ((var + "").equals(accumulator + "")) { } 
        else 
        { System.out.println("!Warning: " + var + " used in lambda body, only " + accumulator + " should be used"); } 
      } 

      Vector allatts = argument.allAttributesUsedIn(); 
      for (int i = 0; i < allatts.size(); i++) 
      { String var = "" + allatts.get(i); 
        if (var.equals(accumulator + "")) { } 
        else 
        { System.out.println("!Warning: " + var + " used in lambda body, only " + accumulator + " should be used"); } 
      } 

      System.out.println(">>> Typechecked lambda expression: " + rtc + " " + type); 
      System.out.println(); 

      return true; 
    }

    boolean res = argument.typeCheck(typs,ents,contexts,env); 
    multiplicity = ModelElement.ONE; 
    Type argumentType = argument.getType(); 

    if (operator.equals("->size") || 
        operator.equals("->toInteger") ||
        operator.equals("->ceil") || 
        operator.equals("->round") ||
        operator.equals("->char2byte") || 
        operator.equals("->floor") ||
        operator.equals("->ord") 
       )
    { type = new Type("int",null); 
      elementType = type; 
      return res; 
    } 

    if (operator.equals("->succ") || 
        operator.equals("->pred"))
    { type = argumentType; 
      elementType = argument.getElementType(); 
      return res; 
    } 
 
       
    if (operator.equals("->toLong"))
    { type = new Type("long",null); 
      elementType = type; 
      return res; 
    } 

    if (operator.equals("!"))
    { Type argtype = argumentType; 
      if (argtype != null && "Ref".equals(argtype.getName()))
      { type = argtype.getElementType(); } 
      else 
      { type = new Type("OclAny", null); }
      return res;  
    } 

    if (operator.equals("?"))
    { Type argtype = argumentType; 
      type = new Type("Ref", null); 
      type.setElementType(argtype);
      return res;   
    } 
    
    if (operator.equals("->copy"))
    { type = argumentType; 
      elementType = argument.elementType; 
      multiplicity = argument.multiplicity;
      isSorted = argument.isSorted;  
      return res; 
    }  

    if (operator.equals("->iterator"))
    { type = new Type("OclIterator", null); 
      elementType = argument.elementType; 
      return res; 
    }  
    // An OclIterator is a sequence view of a 
    // collection, via which the collection can 
    // be navigated & modified. iterators of a map
    // are iterators of its *values* (not keys)
    
    if (operator.equals("->last") || 
        operator.equals("->first"))
    { if (argumentType != null && 
          (argumentType.isMapType() || 
           argumentType.isStringType()))
      { type = argumentType; } 
      else if (argumentType != null && 
               argumentType.elementType != null) 
      { type = argumentType.elementType; } 
      else 
      { type = argument.elementType; }

      
      if (type != null && 
          type.isStringType())
      { elementType = new Type("String", null); } 
      else if (type != null && 
        (type.isCollectionType() || 
         type.isMapType()) 
        )
      { multiplicity = ModelElement.MANY; 
        elementType = type.getElementType(); 
      } 
      else if (type == null)
      { System.err.println("!! ERROR: Argument type is " + argumentType + " No type for: " + this); 
        type = new Type("OclAny", null); 
      } 
      else 
      { 
        elementType = type; 
      } // for strings

      return res; 
    } 

    if (operator.equals("-") || operator.equals("+") || 
        operator.equals("->abs"))   
    { type = argument.type; 
      elementType = argument.elementType;
      if (type == null) 
      { type = new Type("double",null); 
        elementType = new Type("double",null); 
      }  
      return res; 
    } 

    if (operator.equals("->any"))
    { type = argument.elementType; // for ->any()
      elementType = argument.elementType; 

      if (type != null && type.isCollectionType())
      { multiplicity = ModelElement.MANY; 
        elementType = type.getElementType(); 
      } 
      else if (type != null && type.isMapType())
      { multiplicity = ModelElement.MANY; 
        type = argument.type; 
        elementType = type.getElementType(); 
      } // a maplet

      return res; 
    }

    if (operator.equals("->oclType"))
    { type = new Type("OclType", null); 
      elementType = new Type("OclType", null); 
      return res; 
    }

    if (operator.equals("->isDeleted") || 
        operator.equals("->display") ||
        operator.equals("->oclIsUndefined") || 
        operator.equals("->oclIsInvalid") || 
        operator.equals("->oclIsNew") ||
        "->isLong".equals(operator) || 
        operator.equals("not") || 
        operator.equals("->isInteger") || 
        operator.equals("->isReal") ||
        operator.equals("->toBoolean") || 
        operator.equals("->isEmpty") || 
        operator.equals("->notEmpty"))
    { type = new Type("boolean",null); 
      elementType = type; 
      return res; 
    } 

    if (operator.equals("->subcollections"))
    { type = new Type("Set",null); 
      elementType = argument.getType(); 
      type.setElementType(elementType); 
      multiplicity = ModelElement.MANY;
      modality = argument.modality; 
      return res; 
    }

    if (operator.equals("->allInstances"))
    { type = new Type("Sequence",null); 
      elementType = argument.getElementType(); 
      type.setElementType(elementType); 
      multiplicity = ModelElement.MANY;
      modality = argument.modality; 
      return res; 
    }

    if (operator.equals("->unionAll"))
    { type = new Type("Set", null); 
      
      if (argument.isMultiple() && 
          argument.elementType != null) 
      { elementType = argument.elementType.getElementType(); 
        if (argument.elementType.isMap())
        { type = new Type("Map", null);
          type.keyType = argument.elementType.getKeyType();  
          type.elementType = elementType; 
        }

        type.setSorted(argument.elementType.isSorted()); 
      } 
      else 
      { elementType = argument.elementType; } 

      // System.out.println(">>> Type of " + this + " is " + type); 
      // System.out.println(">>> Element type of " + this + " is " + elementType); 
 
      return res; 
    } 

    if (operator.equals("->intersectAll"))
    { type = new Type("Set", null); 

      if (argument.isMultiple() && 
          argument.elementType != null) 
      { elementType = argument.elementType.getElementType(); 
        if (argument.elementType.isMap())
        { type = new Type("Map", null); 
          type.keyType = argument.elementType.getKeyType();  
          type.elementType = elementType; 
        }

        if (argument.elementType.isSorted())
        { type.setSorted(true); } 
        // type is sorted if the elements are
      }
      else 
      { elementType = argument.elementType; }  
      return res; 
    } 

    if (operator.equals("->concatenateAll"))
    { type = new Type("Sequence", null); 
      if (argument.isMultiple() && 
          argument.elementType != null) 
      { elementType = argument.elementType.getElementType(); }
      else 
      { elementType = argument.elementType; }  
      return res; 
    } 

    if (operator.equals("->flatten"))
    { type = argument.type; 
      if (argument.isMultiple() && 
          argument.elementType != null) 
      { elementType = argument.elementType.getElementType(); }
      else 
      { elementType = argument.elementType; }  
      return res; 
    } 

    if (operator.equals("->asSet"))
    { type = new Type("Set",null); 
      elementType = argument.elementType;
      if (argument.isMap())
      { elementType = argument.type; }  
      entity = argument.entity; 
      type.setElementType(elementType); 
      multiplicity = ModelElement.MANY; 
      return res; 
    } // But map->asSet() has elementType = argument.type

    if (operator.equals("->keys"))
    { type = new Type("Set",null); 
      elementType = new Type("String",null); 
         // or keyType of the map 
      if (argument.type != null && 
          argument.type.keyType != null) 
      { elementType = argument.type.keyType; } 
      type.setElementType(elementType); 
      multiplicity = ModelElement.MANY; 
      isSorted = argument.isSorted;  
  
      return res; 
    } // argument is sorted map => result is a sorted set

    if (operator.equals("->closure"))
    { BasicExpression closured = (BasicExpression) argument; 
      // BasicExpression arg = (BasicExpression) closured.getObjectRef(); 

      for (int j = 0; j < contexts.size(); j++) 
      { Entity ent = (Entity) contexts.get(j); 
        if (ent.hasDefinedRole(closured.data))
        { Association ast = ent.getDefinedRole(closured.data); 
          entity = ent; 
          if (ast == null)   // something very bad has happened
          { System.err.println("!! TYPE ERROR: Undefined role: " + argument); 
            return false; 
          } 
          multiplicity = ast.getCard2();
          elementType = new Type(ast.getEntity2()); 
          modality = ModelElement.INTERNAL; // ???
          if (multiplicity == ModelElement.ONE) 
          { type = new Type(ast.getEntity2()); } 
          else 
          { if (ast.isOrdered())
            { type = new Type("Sequence",null); } 
            else 
            { type = new Type("Set",null); }
            type.setElementType(elementType); 
          }   // index must be int type, 
              // and ast is ordered/sorted. Also for any att or role
        } 
      }

      return res; 
    } 

    if (operator.equals("->asSequence"))
    { type = new Type("Sequence",null); 
      elementType = argument.elementType;
      if (argument.isMap())
      { elementType = argument.type; }
      // It is a sequence of individual maplets
  
      type.setElementType(elementType); 
      multiplicity = ModelElement.MANY; 
      return res; 
    } // map->asSequence() is sequence of individual maplets        

    if (operator.equals("->asOrderedSet") ||  
        operator.equals("->sort") ||
        "->asBag".equals(operator) ||  
        operator.equals("->values"))
    { type = new Type("Sequence",null); 
      elementType = argument.elementType;
      type.setElementType(elementType); 
      multiplicity = ModelElement.MANY; 

      if (operator.equals("->sort"))
      { isSorted = true; }  
      
      return res; 
    } // bags are represented as sequences, all instances of 
      // one element are grouped together.         
 
    if (operator.equals("->toReal") || 
        operator.equals("->sqrt") || 
        operator.equals("->sqr") || 
        operator.equals("->log") ||
        operator.equals("->cbrt") || 
        operator.equals("->log10") ||
        operator.equals("->exp") || 
        operator.equals("->sin") ||
        operator.equals("->cos") || 
        operator.equals("->tan") ||
        operator.equals("->sinh") || 
        operator.equals("->cosh") ||
        operator.equals("->tanh") || 
        operator.equals("->asin") ||
        operator.equals("->acos") || 
        operator.equals("->atan"))
    { type = new Type("double",null); 
      elementType = type; 
      return res; 
    } 

    if (operator.equals("->max") ||
        operator.equals("->min") || 
        operator.equals("->sum"))
    { type = argument.getElementType(); 
      elementType = type; 
      modality = argument.modality; 
      multiplicity = ModelElement.ONE; 
      return res; 
    } 

    if (operator.equals("->prd"))
    { type = argument.getElementType();
      if (type == null) 
      { type = new Type("double", null); }  
      elementType = type; 
      modality = argument.modality; 
      multiplicity = ModelElement.ONE; 
      return res; 
    } 

    if (operator.equals("->characters"))
    { type = new Type("Sequence",null); 
      elementType = new Type("String",null); 
      type.setElementType(elementType); 
      multiplicity = ModelElement.MANY;

      if (argument.type != null && 
          argument.type.getName().equals("String"))
      { } 
      else  
      { System.err.println("! Warning: type of " + argument + " should be String in " + this); }  

      return res; 
    } 


    if (operator.equals("->reverse") || 
        operator.equals("->tail") || 
        operator.equals("->front"))  
    { type = argumentType; 
      modality = argument.modality; 
      elementType = argument.elementType;

      if (operator.equals("->front") || 
          operator.equals("->tail"))
      { isSorted = argument.isSorted; } 
      else if (operator.equals("->reverse") && 
               argument.isSorted)
      { System.err.println("! Warning: ->reverse used on sorted argument!: " + argument); } 
      // ok to take front, tail of sorted sets and maps

      if ("String".equals(argument.getType() + ""))
      { type = new Type("String",null); 
        elementType = type; 
        multiplicity = ModelElement.ONE; 
        return res;  
      }
      else if (argument.isSortedSet() || 
               argument.isSortedMap())
      { type = argumentType;
        entity = argument.entity;
        multiplicity = ModelElement.MANY;
      }
      else
      {  
        type = new Type("Sequence", null); 
        type.setElementType(elementType); 
        entity = argument.entity;
        multiplicity = ModelElement.MANY; 
      }

      // and case where it is an extension operator
      System.out.println(">>>*** Type of " + this + " is " + type);
      return res;
    }
    
    if (operator.equals("->toLowerCase") || 
        operator.equals("->toUpper") ||
        operator.equals("->toLower") || 
        operator.equals("->toUpperCase") || 
        operator.equals("->byte2char") || 
        operator.equals("->trim"))  
    { modality = argument.modality; 
      type = new Type("String",null); 
      elementType = type; 
      multiplicity = ModelElement.ONE; 
      return res; 
    }  

    if (extensionoperators.containsKey(operator))
    { type = getOperatorType(operator); 
      elementType = type.getElementType(); 
    }
  
    System.out.println(">>>*** Type of " + this + " is " + type);
      
    return res; 
  } 

  public boolean typeInference(final Vector typs, final Vector ents,
          final Vector contexts, final Vector env, 
          java.util.Map vartypes)
  { if (operator.equals("lambda") && accumulator != null)
    { Vector context = new Vector(); 
      context.addAll(contexts); 

      Vector env1 = new Vector(); 
      env1.addAll(env); 
      env1.add(accumulator); 

      String avar = accumulator.getName(); 

      java.util.Map vartypes1 = new java.util.HashMap(); 
      vartypes1.putAll(vartypes); 
      vartypes1.put(avar, 
                    accumulator.getType()); 
      boolean rtc = 
        argument.typeInference(typs,ents,context,env1,vartypes1);

      Type acctype = accumulator.getType(); 
      if (Type.isVacuousType(acctype))
      { System.err.println("!! vacuous accumulator type in lambda expression: " + this); 
        Type vtype = (Type) vartypes1.get(avar); 
        if (!Type.isVacuousType(vtype))
        { acctype = vtype; } 
        else 
        { acctype = new Type("OclAny", null); }
        accumulator.setType(acctype);  
      } 

      Type argtype = argument.getType(); 
      if (argtype == null)
      { System.err.println("!! Null argument type in lambda expression: " + this); 
        argtype = new Type("OclAny", null); 
      } 

      type = new Type("Function",acctype,argtype);
      elementType = argument.elementType; 

      System.out.println(">>> Typechecked lambda expression: " + rtc + " " + type); 
      return true; 
    }

    boolean res = argument.typeInference(typs,ents,
                                     contexts,env,vartypes);
    Type argumentType = argument.getType(); 
    if (argumentType == null) 
    { System.err.println("!! ERROR: unknown type in " + argument); 
      argument.setType(new Type("OclAny", null)); 
      argumentType = new Type("OclAny", null); 
    } 
 
    multiplicity = ModelElement.ONE; // default

    if (operator.equals("->size")) 
    { type = new Type("int",null); 
      elementType = type; 
      // argument is either a string or collection/map
      if (argumentType.isString() || argumentType.isMap() ||
          argumentType.isCollection()) 
      { } 
      else 
      { System.err.println("!! Argument of ->size() must be String or Map/Collection, not " + argumentType); 
        argument.setType(new Type("Sequence", null)); 
      }

      return res; 
    } 

    if (operator.equals("->ord") ||
        operator.equals("->succ") || 
        operator.equals("->pred"))
    { if (argumentType.isString() || 
          argumentType.isInteger() || 
          argumentType.isEnumeratedType()) 
      { } 
      else 
      { System.err.println("!! Argument of " + operator + 
          " must be String, enumeration or integer, not " + argumentType); 
      } 
    }
    

    if (operator.equals("->toInteger") ||
        operator.equals("->char2byte"))
    { if (argumentType.isString()) 
      { } 
      else 
      { System.err.println("!! Argument of " + operator + 
          " must be String, not " + argumentType); 
        
        argument.setType(new Type("String", null)); 
      } 

      if (argument instanceof BasicExpression)
      { String vname = ((BasicExpression) argument).basicString(); 
        vartypes.put(vname, new Type("String", null)); 
      } 

      type = new Type("int",null); 
      elementType = type; 
      return res; 
    } 

    if (operator.equals("->byte2char"))
    { if (argumentType.isNumeric()) 
      { } 
      else 
      { System.err.println("!! Argument of " + operator + 
          " must be integer, not " + argumentType); 
        
        argument.setType(new Type("int", null)); 
      } 

      if (argument instanceof BasicExpression)
      { String vname = ((BasicExpression) argument).basicString(); 
        vartypes.put(vname, new Type("int", null)); 
      } 

      type = new Type("String",null); 
      elementType = type; 
      return res; 
    } 
 
    if (operator.equals("->ceil") || 
        operator.equals("->round") ||
        operator.equals("->floor"))
    { if (Type.hasVacuousType(argument))
      { System.err.println("!! Argument of " + operator + 
          " must be double, not " + argumentType);
 
        argument.setType(new Type("double", null)); 
        if (argument instanceof BasicExpression)
        { String vname = argument + ""; 
          vartypes.put(vname, new Type("double", null)); 
        } 
      } 
      else if (argumentType.isNumeric()) { } 
      else 
      { System.err.println("!! Argument of " + operator + 
          " must be numeric, not " + argumentType);
        argument.setType(new Type("double", null)); 
        if (argument instanceof BasicExpression)
        { String vname = ((BasicExpression) argument).basicString(); 
          vartypes.put(vname, new Type("double", null)); 
        }
      }  

      type = new Type("int",null); 
      elementType = type; 
      return res; 
    } 

    if (operator.equals("->toLong"))
    { type = new Type("long",null); 
      elementType = type; 

      if (argumentType.isString()) 
      { } 
      else 
      { System.err.println("!! Argument of " + operator + 
          " must be String, not " + argumentType); 
        
        argument.setType(new Type("String", null)); 
      } 

      if (argument instanceof BasicExpression)
      { String vname = ((BasicExpression) argument).basicString(); 
        vartypes.put(vname, new Type("String", null)); 
      }
 
      return res; 
    } 

    if (operator.equals("!"))
    { if (argumentType != null && 
          "Ref".equals(argumentType.getName()))
      { type = argumentType.getElementType(); } 
      else 
      { System.err.println("!! Argument of " + operator + 
          " must be Ref type, not " + argumentType); 
        Type refType = new Type("Ref", null); 
        refType.setElementType(new Type("OclAny", null)); 
        argument.setType(refType); 
      
        if (argument instanceof BasicExpression)
        { String vname = ((BasicExpression) argument).basicString();
          vartypes.put(vname, refType); 
        }

        type = new Type("OclAny", null); 
      }
 
      return res; 
    } 

    if (operator.equals("?"))
    { type = new Type("Ref", null); 
      if (argumentType != null) 
      { type.setElementType(argumentType); } 
      else 
      { type.setElementType(new Type("OclAny", null)); }
      elementType = type.getElementType();   
      return res;  
    } 
    
    if (operator.equals("->copy"))
    { type = argument.type; 
      elementType = argument.elementType; 
      multiplicity = argument.multiplicity; 
      isSorted = argument.isSorted; 
      return res; 
    }  

    if (operator.equals("->iterator"))
    { type = new Type("OclIterator", null); 
      if (argumentType != null &&
          argumentType.elementType != null) 
      { type.setElementType(argumentType.elementType); } 
      else 
      { type.setElementType(new Type("OclAny", null)); }
      elementType = type.getElementType();   
      return res; 
    }  
    // An OclIterator is a sequence view of a 
    // collection, via which the collection can 
    // be navigated & modified. iterators of a map
    // are iterators of its *values* (not keys)
    
    if (operator.equals("->last") || 
        operator.equals("->first"))
    { type = argument.elementType; 

      if (type == null && argument.getType() != null)
      { Type argTyp = argument.getType(); 
        type = argTyp.getElementType(); 
      } 

      if (type != null && (type.isCollectionType() || 
                           type.isMapType()))
      { multiplicity = ModelElement.MANY; 
        elementType = type.getElementType(); 
      } 
      else if (type == null)
      { System.err.println("!! ERROR: Argument type is " + argument.getType() + " No type for: " + this); 
        type = new Type("OclAny", null); 
      } 
      else 
      { 
        elementType = type; 
      } 

      if (argumentType.isString() || 
          argumentType.isCollection() || 
          argumentType.isMap())
      { } 
      else 
      { System.err.println("!! Argument of " + operator + 
          " must be String or Map/Collection, not " + argumentType); 
        argument.setType(new Type("Sequence", null)); 
      } 

      return res; 
    } 

    if (operator.equals("-") || operator.equals("+") || 
        operator.equals("->abs"))   
    { type = argumentType; 
      elementType = argument.elementType;

      if (argumentType.isNumeric()) 
      { return res; } 

      /* if (operator.equals("-") && 
          (argumentType.isCollection() || 
           argumentType.isMap()))
      { return res; } 
      
      if (operator.equals("-")) 
      { System.err.println("!! Argument of - must be numeric, map or collection, not " + argumentType); } 
      else */ 
 
      System.err.println("!! Argument of " + operator + 
          " must be numeric, not " + argumentType); 
  
      type = new Type("double",null); 
      elementType = new Type("double",null); 

      if (argument instanceof BasicExpression)
      { String vname = ((BasicExpression) argument).basicString();
        vartypes.put(vname, type); 
      }

      return res; 
    } 

    if (operator.equals("->any"))
    { type = argument.elementType; // for ->any()
      elementType = argument.elementType; 

      if (type != null && type.isCollectionType())
      { multiplicity = ModelElement.MANY; 
        elementType = type.getElementType(); 
      } 

      if (argument.type != null && argument.type.isMapType())
      { type = argument.type; 
        multiplicity = ModelElement.MANY; 
        elementType = type.getElementType(); 
      }   

      if (type == null) 
      { type = new Type("OclAny", null); } 

      if (argumentType.isCollection() || 
          argumentType.isMap()) { } 
      else 
      { System.err.println("!! Argument of ->any() must be collection, not " + argumentType); 
        argument.setType(new Type("Sequence", null));
        if (argument instanceof BasicExpression)
        { String vname = ((BasicExpression) argument).basicString(); 
          vartypes.put(vname, argument.type); 
        }
      } 
      // also the case of maps - it is a maplet

      return res; 
    }

    if (operator.equals("->oclType"))
    { type = new Type("OclType", null); 
      elementType = new Type("OclType", null); 
      return res; 
    }

    if (operator.equals("->isDeleted") || 
        operator.equals("->display") ||
        operator.equals("->oclIsUndefined") || 
        operator.equals("->oclIsInvalid") || 
        operator.equals("->oclIsNew"))
    { type = new Type("boolean",null); 
      elementType = type; 
      return res; 
    } 

    if (operator.equals("not"))
    { if (argumentType.isBoolean())
      { } 
      else
      { System.err.println("!! Argument of not must be boolean, not " + argumentType); 
        argument.setType(new Type("boolean",null)); 
        if (argument instanceof BasicExpression)
        { String vname = 
            ((BasicExpression) argument).basicString(); 
          vartypes.put(vname, argument.type); 
        }
      } 

      type = new Type("boolean",null); 
      elementType = type; 
      return res; 
    } 
 

    if ("->isLong".equals(operator) || 
        operator.equals("->isInteger") || 
        operator.equals("->isReal") ||
        operator.equals("->toBoolean"))
    { type = new Type("boolean",null); 
      elementType = type; 

      if (argumentType.isString())
      { } 
      else
      { System.err.println("!! Argument of " + operator + 
          " must be String, not " + argumentType); 
        argument.setType(new Type("String",null)); 
        if (argument instanceof BasicExpression)
        { String vname = 
            ((BasicExpression) argument).basicString(); 
          vartypes.put(vname, argument.type); 
        }
      } 

      return res; 
    } 
 
    if (operator.equals("->isEmpty") || 
        operator.equals("->notEmpty"))
    { type = new Type("boolean",null); 
      elementType = type; 

      if (argumentType.isCollection() || 
          argumentType.isMap())
      { } 
      else
      { System.err.println("!! Argument of " + operator + 
          " must be Collection/Map, not " + argumentType); 
        argument.setType(new Type("Sequence",null)); 
        if (argument instanceof BasicExpression)
        { String vname = 
            ((BasicExpression) argument).basicString(); 
          vartypes.put(vname, argument.type); 
        }
      } 

      return res; 
    } 

    if (operator.equals("->subcollections"))
    { type = new Type("Set",null); 
      elementType = argument.getType(); 
      type.setElementType(elementType); 
      multiplicity = ModelElement.MANY;
      modality = argument.modality; 

      if (argumentType.isCollection())
      { } 
      else
      { System.err.println("!! Argument of " + operator + 
          " must be Collection, not " + argumentType); 
        argument.setType(new Type("Set",null)); 
        if (argument instanceof BasicExpression)
        { String vname = 
            ((BasicExpression) argument).basicString(); 
          vartypes.put(vname, argument.type); 
        }
      } 
      return res; 
    }

    if (operator.equals("->allInstances"))
    { type = new Type("Sequence",null); 
      elementType = argument.getElementType(); 
      type.setElementType(elementType); 
      multiplicity = ModelElement.MANY;
      modality = argument.modality;

      if (argumentType.isEntity())
      { } 
      else
      { System.err.println("!! Argument of " + operator + 
          " must be an entity (class) type, not " + argumentType); 
        argument.setType(new Type("OclAny",null)); 
        if (argument instanceof BasicExpression)
        { String vname = 
            ((BasicExpression) argument).basicString(); 
          vartypes.put(vname, argument.type); 
        }
      } 
 
      return res; 
    }

    if (operator.equals("->unionAll"))
    { type = new Type("Set", null); 
      
      if (argument.isMultiple() && 
          argument.elementType != null) 
      { elementType = argument.elementType.getElementType(); 
        if (argument.elementType.isMap())
        { type = new Type("Map", null);
          type.keyType = argument.elementType.getKeyType();  
          type.elementType = elementType; 
        }

        type.setSorted(argument.elementType.isSorted()); 
      } 
      else 
      { elementType = argument.elementType; } 

      if (argumentType.isCollection()) { } 
      else 
      { System.err.println("!! Argument of " + this + 
          " must be a collection, not " + argumentType); 

        argument.setType(new Type("Sequence",null)); 
        if (argument instanceof BasicExpression)
        { String vname = 
            ((BasicExpression) argument).basicString(); 
          vartypes.put(vname, argument.type); 
        }
      } 

      return res; 
    } 

    if (operator.equals("->intersectAll"))
    { type = new Type("Set", null); 

      if (argument.isMultiple() && 
          argument.elementType != null) 
      { elementType = argument.elementType.getElementType(); 
        if (argument.elementType.isMap())
        { type = new Type("Map", null); 
          type.keyType = argument.elementType.getKeyType();  
          type.elementType = elementType; 
        }

        type.setSorted(argument.elementType.isSorted()); 
      }
      else 
      { elementType = argument.elementType; }  


      if (argumentType.isCollection()) { } 
      else 
      { System.err.println("!! Argument of " + this + 
          " must be a collection, not " + argumentType); 

        argument.setType(new Type("Sequence",null)); 
        if (argument instanceof BasicExpression)
        { String vname = 
            ((BasicExpression) argument).basicString(); 
          vartypes.put(vname, argument.type); 
        }
      } 

      return res; 
    } 

    if (operator.equals("->concatenateAll"))
    { type = new Type("Sequence", null); 
      if (argument.isMultiple() && 
          argument.elementType != null) 
      { elementType = argument.elementType.getElementType(); }
      else 
      { elementType = argument.elementType; }  

      if (argumentType.isCollection()) { } 
      else 
      { System.err.println("!! Argument of " + this + 
          " must be a collection, not " + argumentType); 

        argument.setType(new Type("Sequence",null)); 
        if (argument instanceof BasicExpression)
        { String vname = 
            ((BasicExpression) argument).basicString(); 
          vartypes.put(vname, argument.type); 
        }
      } 

      return res; 
    } 

    if (operator.equals("->flatten"))
    { type = argument.type; 
      if (argument.isMultiple() && 
          argument.elementType != null) 
      { elementType = argument.elementType.getElementType(); }
      else 
      { elementType = argument.elementType; }

      if (argumentType.isCollection()) { } 
      else 
      { System.err.println("!! Argument of " + this + 
          " must be a collection, not " + argumentType); 

        argument.setType(new Type("Sequence",null)); 
        if (argument instanceof BasicExpression)
        { String vname = 
            ((BasicExpression) argument).basicString(); 
          vartypes.put(vname, argument.type); 
        }
      } 
  
      return res; 
    } 

    if (operator.equals("->asSet"))
    { type = new Type("Set",null); 
      elementType = argument.elementType;

      if (argument.isMap())
      { elementType = argument.type; }  

      entity = argument.entity; 
      type.setElementType(elementType); 
      multiplicity = ModelElement.MANY; 

      if (argumentType.isCollection() || argumentType.isMap()) 
      { } 
      else 
      { System.err.println("!! Argument of " + this + 
          " must be a collection or map, not " + argumentType); 

        argument.setType(new Type("Sequence",null)); 
        if (argument instanceof BasicExpression)
        { String vname = 
            ((BasicExpression) argument).basicString(); 
          vartypes.put(vname, argument.type); 
        }
      } 

      return res; 
    } // But map->asSet() has elementType = argument.type

    if (operator.equals("->keys"))
    { type = new Type("Set",null); 
      elementType = new Type("String",null); 
         // or keyType of the map 
      if (argument.type != null && 
          argument.type.keyType != null) 
      { elementType = argument.type.keyType; } 
      type.setElementType(elementType); 
      multiplicity = ModelElement.MANY;

      if (argumentType.isMap()) { } 
      else 
      { System.err.println("!! Argument of " + this + 
          " must be a map, not " + argumentType); 

        argumentType = new Type("Map", null); 
        argumentType.setKeyType(new Type("OclAny", null)); 
        argumentType.setElementType(new Type("OclAny", null)); 

        argument.setType(argumentType); 
        if (argument instanceof BasicExpression)
        { String vname = 
            ((BasicExpression) argument).basicString(); 
          vartypes.put(vname, argument.type); 
        }
      } 
 
      return res; 
    } 

    if (operator.equals("->closure"))
    { BasicExpression closured = (BasicExpression) argument; 
      // BasicExpression arg = (BasicExpression) closured.getObjectRef(); 

      for (int j = 0; j < contexts.size(); j++) 
      { Entity ent = (Entity) contexts.get(j); 
        if (ent.hasDefinedRole(closured.data))
        { Association ast = ent.getDefinedRole(closured.data); 
          entity = ent; 
          if (ast == null)   // something very bad has happened
          { System.err.println("TYPE ERROR: Undefined role: " + argument); 
            JOptionPane.showMessageDialog(null, "Undefined role " + argument, "Type error",
			                                         JOptionPane.ERROR_MESSAGE);  
            return false; 
          } 
          multiplicity = ast.getCard2();
          elementType = new Type(ast.getEntity2()); 
          modality = ModelElement.INTERNAL; // ???
          if (multiplicity == ModelElement.ONE) 
          { type = new Type(ast.getEntity2()); } 
          else 
          { if (ast.isOrdered())
            { type = new Type("Sequence",null); } 
            else 
            { type = new Type("Set",null); }
            type.setElementType(elementType); 
          }   // index must be int type, 
              // and ast is ordered/sorted. Also for any att or role
        } 
      }
    } 

    if (operator.equals("->asSequence"))
    { type = new Type("Sequence",null); 
      elementType = argument.elementType;
      if (argument.isMap())
      { elementType = argument.type; }  
      type.setElementType(elementType); 
      multiplicity = ModelElement.MANY; 

      if (argumentType.isCollection() || 
          argumentType.isMap()) { } 
      else 
      { System.err.println("!! Argument of " + this + 
          " must be a collection or map, not " + argumentType); 

        argumentType = new Type("Set", null); 
        argumentType.setElementType(new Type("OclAny", null)); 

        argument.setType(argumentType); 
        if (argument instanceof BasicExpression)
        { String vname = 
            ((BasicExpression) argument).basicString(); 
          vartypes.put(vname, argument.type); 
        }
      } 

      return res; 
    } // map->asSequence() is sequence of individual maplets        

    if (operator.equals("->asOrderedSet") ||  
        operator.equals("->sort") ||
        "->asBag".equals(operator))
    { type = new Type("Sequence",null); 
      elementType = argument.elementType;
      type.setElementType(elementType); 
      multiplicity = ModelElement.MANY; 

      if (operator.equals("->sort"))
      { if (argumentType.isMap())
        { type = new Type("Map", null); 
          type.keyType = argumentType.keyType; 
          type.elementType = argumentType.elementType;
        } 

        type.setSorted(true); 
      } 

      if (argumentType.isCollection() || 
          argumentType.isMap()) { } 
      else 
      { System.err.println("!! Argument of " + this + 
          " must be a collection or map, not " + argumentType); 

        argumentType = new Type("Sequence", null); 
        argumentType.setElementType(new Type("OclAny", null)); 

        argument.setType(argumentType); 
        if (argument instanceof BasicExpression)
        { String vname = 
            ((BasicExpression) argument).basicString(); 
          vartypes.put(vname, argument.type); 
        }
      } 

      return res; 
    }         

    if (operator.equals("->values"))
    { type = new Type("Sequence",null); 
      elementType = argument.elementType;
      type.setElementType(elementType); 
      multiplicity = ModelElement.MANY; 

      if (argumentType.isMap()) { } 
      else 
      { System.err.println("!! Argument of " + this + 
          " must be a map, not " + argumentType); 

        argumentType = new Type("Map", null); 
        argumentType.setKeyType(new Type("OclAny", null)); 
        argumentType.setElementType(new Type("OclAny", null)); 

        argument.setType(argumentType); 
        if (argument instanceof BasicExpression)
        { String vname = 
            ((BasicExpression) argument).basicString(); 
          vartypes.put(vname, argument.type); 
        }
      } 

      return res; 
    } 
 
    if (operator.equals("->toReal"))
    { type = new Type("double",null); 
      elementType = type; 

      if (argumentType.isString())
      { } 
      else
      { System.err.println("!! Argument of " + operator + 
          " must be String, not " + argumentType); 
        argument.setType(new Type("String",null)); 
        if (argument instanceof BasicExpression)
        { String vname = 
            ((BasicExpression) argument).basicString();
          vartypes.put(vname, argument.type); 
        }
      } 
      return res; 
    } 

    if (operator.equals("->sqrt") || 
        operator.equals("->sqr") || 
        operator.equals("->log") ||
        operator.equals("->cbrt") || 
        operator.equals("->log10") ||
        operator.equals("->exp") || 
        operator.equals("->sin") ||
        operator.equals("->cos") || 
        operator.equals("->tan") ||
        operator.equals("->sinh") || 
        operator.equals("->cosh") ||
        operator.equals("->tanh") || 
        operator.equals("->asin") ||
        operator.equals("->acos") || 
        operator.equals("->atan"))
    { type = new Type("double",null); 
      elementType = type; 

      if (argumentType.isNumeric())
      { } 
      else
      { System.err.println("!! Argument of " + operator + 
          " must be a numeric type, not " + argumentType); 
        argument.setType(new Type("double",null)); 
        if (argument instanceof BasicExpression)
        { String vname = 
            ((BasicExpression) argument).basicString(); 
          vartypes.put(vname, argument.type); 
        }
      } 

      return res; 
    } 

    if (operator.equals("->max") ||
        operator.equals("->min") || 
        operator.equals("->sum"))
    { type = argument.getElementType(); 
      elementType = type; 

      if (Type.isVacuousType(type))
      { type = new Type("double", null); 
        elementType = type; 
      } 
      else if (Type.isSummableType(type))
      { } // or OclDate for ->min, ->max.  
      else 
      { System.err.println("!! Element type of " + argument + " must be numeric/string type, not " + type); 
        type = new Type("double", null); 
        elementType = type;
      } 

      modality = argument.modality; 
      multiplicity = ModelElement.ONE; 

      if (argumentType.isCollection())
      { } 
      else
      { System.err.println("!! Argument of " + operator + 
          " must be a sequence or set, not " + argumentType); 
        argument.setType(new Type("Sequence",null)); 
        if (argument instanceof BasicExpression)
        { String vname = 
            ((BasicExpression) argument).basicString();
          vartypes.put(vname, argument.type); 
        }
      } 

      return res; 
    } 

    if (operator.equals("->prd"))
    { type = argument.getElementType();

      if (Type.isVacuousType(type))
      { type = new Type("double", null); 
        elementType = type; 
      } 
      else if (type.isNumeric())
      { }   
      else 
      { System.err.println("!! Element type of " + argument + " must be numeric type, not " + type); 
        type = new Type("double", null); 
        elementType = type;
      } 

      elementType = type; 
      modality = argument.modality; 
      multiplicity = ModelElement.ONE; 

      if (argumentType.isCollection())
      { } 
      else
      { System.err.println("!! Argument of " + operator + 
          " must be a sequence or set, not " + argumentType); 
        argument.setType(new Type("Sequence",null)); 
        if (argument instanceof BasicExpression)
        { String vname = 
            ((BasicExpression) argument).basicString(); 
          vartypes.put(vname, argument.type); 
        }
      } 

      return res; 
    } 

    if (operator.equals("->characters"))
    { type = new Type("Sequence",null); 
      elementType = new Type("String",null); 
      type.setElementType(elementType); 
      multiplicity = ModelElement.MANY;

      if (argumentType.isString())
      { } 
      else
      { System.err.println("!! Argument of " + operator + 
          " must be a string, not " + argumentType); 
        argument.setType(new Type("String",null)); 
        argument.setElementType(new Type("String",null)); 
        
        if (argument instanceof BasicExpression)
        { String vname = 
            ((BasicExpression) argument).basicString(); 
          vartypes.put(vname, argument.type); 
        }
      } 

      return res; 
    } 


    if (operator.equals("->reverse") || 
        operator.equals("->tail") || 
        operator.equals("->front"))  
    { type = argument.getType(); 
      modality = argument.modality; 
      elementType = argument.elementType;

      if ("String".equals(argument.getType() + ""))
      { type = new Type("String",null); 
        elementType = type; 
        multiplicity = ModelElement.ONE; 
        return res;  
      }
      
      if (type != null) 
      { type.setElementType(elementType); }
 
      entity = argument.entity;
      multiplicity = ModelElement.MANY; 

      if (operator.equals("->front") || 
          operator.equals("->tail"))
      { isSorted = argument.isSorted; } 
      
      if (argumentType.isString() || 
          argumentType.isSorted() ||
          argumentType.isSequence())
      { } 
      else
      { System.err.println("!! Argument of " + operator + 
          " must be a sequence, sorted collection/map or string, not " + argumentType); 
        argument.setType(new Type("Sequence",null)); 
        argument.setElementType(new Type("OclAny",null)); 
        
        if (argument instanceof BasicExpression)
        { String vname = 
            ((BasicExpression) argument).basicString(); 
          vartypes.put(vname, argument.type); 
        }
      } 

      // and case where it is an extension operator
      System.out.println(">>** Type of " + this + " is " + type);
      return res;
    }
    
    if (operator.equals("->toLowerCase") || 
        operator.equals("->toUpper") ||
        operator.equals("->toLower") || 
        operator.equals("->toUpperCase") || 
        operator.equals("->trim"))  
    { modality = argument.modality; 
      type = new Type("String",null); 
      elementType = type; 
      multiplicity = ModelElement.ONE; 

      if (argumentType.isString())
      { } 
      else
      { System.err.println("!! Argument of " + operator + 
          " must be a string, not " + argumentType); 
        argument.setType(new Type("String",null)); 
        argument.setElementType(new Type("String",null)); 
        
        if (argument instanceof BasicExpression)
        { String vname = 
            ((BasicExpression) argument).basicString(); 
          vartypes.put(vname, argument.type); 
        }
      } 

      return res; 
    }  

    if (extensionoperators.containsKey(operator))
    { type = getOperatorType(operator); 
      elementType = type.getElementType(); 
    }
  
    System.out.println(">>> *** Type of " + this + " is " + type);
      
    return res; 
  } 

  public int maxModality()
  { return argument.maxModality(); } 

  public int minModality() 
  { return argument.minModality(); } 

  public boolean isMultiple()
  { if (operator.equals("->subcollections") || 
        operator.equals("->unionAll") || 
        operator.equals("->intersectAll") ||
        operator.equals("->concatenateAll") || 
        operator.equals("->closure") || 
        operator.equals("->asSet") || 
        operator.equals("->values") ||
        operator.equals("->flatten") || 
        operator.equals("->characters") || 
        operator.equals("->keys") ||
        operator.equals("->asSequence") ||
        operator.equals("->asOrderedSet") ||
        operator.equals("->asBag") || 
        operator.equals("->sort"))  
    { return true; } 

    if (argument.isMultiple() &&
        (operator.equals("->reverse") || 
         operator.equals("->tail") || 
         operator.equals("lambda") ||
         operator.equals("->front")) )  
    { return true; } 

    if ("->last".equals(operator) || 
        "->first".equals(operator) ||
        "->max".equals(operator) || 
        "->min".equals(operator) ||
        "->any".equals(operator))
    { Type argelemtype = argument.getElementType(); 
      return (argelemtype != null && 
              argelemtype.isCollectionType());  
    } 

    if ("->copy".equals(operator))
    { return argument.isMultiple(); }  
       
    return false; 
  } 

  public boolean isOrExpression()
  { return false; } 

  public Expression completeness(Entity ent)
  { Expression res = new BasicExpression(true);
    return res;
  }

  public Expression createActionForm(final Vector v)
  { return argument.createActionForm(v); } 

  public Expression skolemize(Expression sourceVar, java.util.Map env)
  { return this; } 

  public String queryForm(java.util.Map env, boolean local)
  { String qf = argument.queryForm(env,local); 
    String cont = "Controller.inst()"; 

    if (operator.equals("lambda") && accumulator != null)
    { String acc = accumulator.getName(); 
      return "(" + acc + ") -> { return " + qf + "; }"; // for Java8+ 
    }
	
    if (operator.equals("-"))
    { if (needsBracket) 
      { return "(-" + qf + ")"; } 
      return "-" + qf; 
    } 

    if (operator.equals("?"))
    { String res = qf; 
      String elemT = "Object"; 
      if (argument.type != null) 
      { elemT = argument.type.getJava(); } 

      // if (argument.isCollection() || 
      //     argument.isFunctionType() || 
      //     argument.isClassEntityType())
      // { } 
      // else

      if (argument instanceof BasicExpression) 
      { BasicExpression bexpr = (BasicExpression) argument; 
        System.out.println(">>> Reference for " + bexpr + " " + bexpr.variable); 
        res = "Set.newRef" + elemT + "(" + qf + ")"; 
      } // also bexpr->at(ind) is ok: ?bexpr + ind - 1

      if (needsBracket) 
      { res = "(" + res + ")"; }
      return res;  
    } 

    if (operator.equals("!"))
    { String res = qf; 
      // Type argelemtype = argument.getElementType(); 
      // if (argelemtype != null && 
      //     (argelemtype.isCollection() || 
      //      argelemtype.isFunctionType() || 
      //      argelemtype.isClassEntityType()))
      // { } 
      // else
      // if (Type.isBasicType(argelemtype)) // or a Ref

      if (argument instanceof BinaryExpression) 
      { BinaryExpression be = (BinaryExpression) argument; 
        if (be.left.isRef() && "+".equals(be.operator))
        { // !(ptr + n) is ptr[n]

          String lqf = be.left.queryForm(env,local); 
          String rqf = be.right.queryForm(env,local);
          res = lqf + "[" + rqf + "]";
        } 
        else 
        { res = qf + "[0]"; }
      }
      else if (argument instanceof UnaryExpression)
      { UnaryExpression ue = (UnaryExpression) argument; 
        if ("?".equals(ue.operator)) 
        { res = ue.argument.queryForm(env,local); } 
        else 
        { res = qf + "[0]"; }
      }
      else   
      { res = qf + "[0]"; }  

      if (needsBracket) 
      { res = "(" + res + ")"; }
      return res;  
    } // functions, classes, collections, maps have ?x = x

    // if (operator.equals("?") || operator.equals("!"))
    // { return qf; } 

    if (operator.equals("not"))
    { return "!(" + qf + ")"; } 

    if (operator.equals("->size"))
    { if (argument.umlkind == CLASSID && 
          (argument instanceof BasicExpression) &&
          ((BasicExpression) argument).arrayIndex == null)
      { return cont + "." + qf.toLowerCase() + "s.size()"; } 
      if (argument.getType() != null && argument.getType().getName().startsWith("String"))
      { return "(" + qf + ").length()"; } 
      if (argument.isNumeric())
      { return "(\"\" + " + qf + ").length()"; } 
      return "(" + qf + ").size()";
    } 

    if (operator.equals("->allInstances"))
    { if (argument.umlkind == CLASSID && 
          (argument instanceof BasicExpression) &&
          ((BasicExpression) argument).arrayIndex == null)
      { return cont + "." + qf.toLowerCase() + "s"; }
    } // and for other languages

    if (operator.equals("->display"))
    { return "true"; } 

    if (operator.equals("->isDeleted")) 
    { // return new BasicExpression("false"); 
      if (argument.isMultiple())
      { return qf + ".size() == 0"; } 
      else if (argument.type != null) 
      { String objstype = argument.type.getName();  
        return "!(" + cont + "." + objstype.toLowerCase() + 
               "s.contains(" + qf + "))"; 
      }  
      return "false"; 
    } 

    if (operator.equals("->asSequence")) 
    { if (argument.isMap())
      { return "Set.mapAsSequence(" + qf + ")"; } 
      return qf; 
    }  

    if (operator.equals("->asSet")) 
    { if (argument.isMap())
      { return "Set.mapAsSet(" + qf + ")"; } 
      return "Set.asSet(" + qf + ")"; 
    }

    if (operator.equals("->asOrderedSet")) 
    { return "Set.asOrderedSet(" + qf + ")"; }

    if (operator.equals("->asBag")) 
    { return "Set.sort(" + qf + ")"; }  

    if (operator.equals("->characters")) 
    { return "Set.characters(" + qf + ")"; } 

    if ("->copy".equals(operator))
    { if (type == null) 
      { return qf; } 
      if (type.isEntity())
      { String tcs = type.getJava(); 
        return "((" + tcs + ") " + qf + ".clone())"; 
      }
      String tname = type.getName(); 
      if ("String".equals(tname))
      { return "(\"\"" + qf + ")"; } 
      if ("Set".equals(tname) || "Sequence".equals(tname))
      { return "Set.copyCollection(" + qf + ")"; } 
      if ("Map".equals(tname))
      { return "Set.copyMap(" + qf + ")"; } 
      return qf; 
    }    

    if (operator.equals("->iterator"))
    { if (argument.isSequenceValued())
      { return "OclIterator.newOclIterator_Sequence(" + qf + ")"; }
      else if (argument.isMap())
      { return "OclIterator.newOclIterator_Set(" + qf + ".values())"; }
      else
      { return "OclIterator.newOclIterator_Set(" + qf + ")"; }
    }

    if (operator.equals("->sqr")) 
    { return "((" + qf + ")*(" + qf + "))"; } 


    if (operator.equals("->concatenateAll")) 
    { return "Set.concatenateAll(" + qf + ")"; } 
    
    if (operator.equals("->unionAll") && 
        argument.elementType != null &&
        argument.elementType.isMap())
    { return "Set.unionAllMap(" + qf + ")"; } 

    if (operator.equals("->unionAll")) 
    { return "Set.unionAll(" + qf + ")"; } 
    
    if (operator.equals("->intersectAll") && 
        argument.elementType != null &&
        argument.elementType.isMap())
    { return "Set.intersectAllMap(" + qf + ")"; } 

    if (operator.equals("->intersectAll"))
    { return "Set.intersectAll(" + qf + ")"; } 
    
    if (operator.equals("->flatten")) 
    { if (Type.isSequenceType(argument.type))
      { return "Set.concatenateAll(" + qf + ")"; } 
      else if (Type.isSetType(argument.type))
      { return "Set.unionAll(" + qf + ")"; } 
      else if (Type.isMapType(argument.type))
      { return "Set.unionAllMap(" + qf + ")"; } 
      return qf; 
    } // but only goes one level down. Not for maps

    if (operator.equals("->isInteger")) 
    { return "Set.isInteger(" + qf + ")"; } 
      // try { Integer.parseInt(" + qf + "); return true; }\n" + 
      //       "  catch(Exception _e) { return false; }"; 

    if (operator.equals("->isLong")) 
    { return "Set.isLong(" + qf + ")"; } 

    if (operator.equals("->isReal")) 
    { return "Set.isReal(" + qf + ")"; } 
      // try { Double.parseDouble(" + qf + "); return true; }\n" + 
      //       "  catch(Exception _e) { return false; }"; 


    if (operator.equals("->toInteger")) 
    { return "Integer.decode(" + qf + ").intValue()"; } 

    if (operator.equals("->char2byte")) 
    { return "Set.char2byte(" + qf + ")"; } 

    if (operator.equals("->byte2char")) 
    { return "Set.byte2char(" + qf + ")"; } 

    if (operator.equals("->toLong")) 
    { return "Long.decode(" + qf + ").longValue()"; } 

    if (operator.equals("->toReal")) 
    { return "Double.parseDouble(" + qf + ")"; } 

    if (operator.equals("->toBoolean")) 
    { return "\"true\".equals(" + qf + " + \"\")"; } 

    if (operator.equals("->toLower")) 
    { return qf + ".toLowerCase()"; } 

    if (operator.equals("->toUpper")) 
    { return qf + ".toUpperCase()"; } 

    if (operator.equals("->toLowerCase")) 
    { return qf + ".toLowerCase()"; } 

    if (operator.equals("->toUpperCase")) 
    { return qf + ".toUpperCase()"; } 

    if (operator.equals("->trim")) 
    { return qf + ".trim()"; } 

    if (operator.equals("->ord"))
    { if (argument.isString())
      { return "Set.char2byte(" + qf + ")"; } 
      return qf; 
    } 

    if (operator.equals("->succ"))
    { if (argument.isString())
      { return "Set.byte2char(1 + Set.char2byte(" + qf + "))"; } 
      return "(" + qf + " + 1)"; 
    } 

    if (operator.equals("->pred"))
    { if (argument.isString())
      { return "Set.byte2char(Set.char2byte(" + qf + ") - 1)"; } 
      return "(" + qf + " - 1)"; 
    } 

    if (operator.equals("->oclIsUndefined")) 
    { if (argument.isNumeric())
      { return "Double.isNaN((double) " + qf + ")"; } 
      return "(" + qf + " == null)"; 
    } 

    if (operator.equals("->oclIsInvalid")) 
    { return "Double.isNaN(" + qf + ")"; } 

    if (operator.equals("->oclIsNew")) 
    { return "(" + qf + " != null)"; }

    if (operator.equals("->oclType"))
    { String wqf = argument.wrap(qf); 
      return "(" + wqf + ").getClass()"; 
    } // wrapped so that 1->oclType() becomes 
      // (new Integer(1)).getClass()  

    String pre = qf;
    String data = operator.substring(2,operator.length()); 

    if (extensionoperators.containsKey(operator))
    { String op = operator;
      String opjava = Expression.getOperatorJava(op); 
      if (opjava != null && opjava.length() > 0)
      { return opjava.replaceAll("_1",qf); } 
      if (operator.startsWith("->"))
      { op = operator.substring(2,operator.length()); } 
      return qf + "." + op + "()";
    } 

    if (argument.umlkind == CLASSID && (argument instanceof BasicExpression))
    { pre = ((BasicExpression) argument).classExtentQueryForm(env,local); } 

    if (operator.equals("->abs") || 
        operator.equals("->sin") ||
        operator.equals("->cos") || 
        operator.equals("->tan") ||
        operator.equals("->exp") || 
        operator.equals("->log") ||
        operator.equals("->atan") || 
        operator.equals("->acos") || 
        operator.equals("->asin") ||
        operator.equals("->cosh") || 
        operator.equals("->sinh") ||
        operator.equals("->tanh") || 
        operator.equals("->sqrt"))  
    { return "Math." + data + "(" + pre + ")"; }
    else if (operator.equals("->cbrt"))
    { return "Math.pow(" + pre + ", 1.0/3)"; } 
    else if (operator.equals("->log10"))
    { return "Math.log(" + pre + ")/Math.log(10)"; } 
    else if (operator.equals("->round") || 
             operator.equals("->ceil") || 
             operator.equals("->floor"))
    { return "((int) Math." + data + "(" + pre + "))"; } 
    else if (data.equals("toUpperCase") || 
             data.equals("toLowerCase"))
    { return pre + "." + data + "()"; } 
    else if (data.equals("sum"))
    { Type sumtype = argument.getElementType();  // was getType(); -- correct below 
      // System.out.println("SUM with " + sumtype + " " + type); 

      if (sumtype == null) 
      { sumtype = type; } 

      if (sumtype == null) 
      { JOptionPane.showMessageDialog(null, "No element type for: " + argument, "Type error", JOptionPane.ERROR_MESSAGE);
        return "Set.sumString(" + pre + ")"; 
      } 

      String tname = sumtype.getName();   // only int, long, double, String are valid

      if ("int".equals(tname) || "long".equals(tname) || 
          "double".equals(tname) ||
          "String".equals(tname))
      { return "Set.sum" + tname + "(" + pre + ")"; } 
      else
      { JOptionPane.showMessageDialog(null, "Incorrect type " + tname + " for summation in " + this, "Type error", JOptionPane.ERROR_MESSAGE);
        return ""; 
      }  
    } 
    else if (data.equals("prd"))
    { Type prdtype = argument.getElementType();  // was getType(); -- correct below 

      if (prdtype == null) 
      { prdtype = type; } 

      if (prdtype == null) 
      { JOptionPane.showMessageDialog(null, 
          "No element type for: " + argument, 
          "Type error", JOptionPane.ERROR_MESSAGE);
        return "Set.prddouble(" + pre + ")"; 
      } 

      String tname = prdtype.getName();   
         // only int, double, long are valid

      if ("int".equals(tname) || "long".equals(tname) || 
          "double".equals(tname))
      { return "Set.prd" + tname + "(" + pre + ")"; } 
      else 
      { JOptionPane.showMessageDialog(null, 
            "Incorrect type " + tname + " for product in " + this, 
            "Type error", JOptionPane.ERROR_MESSAGE);
        return ""; 
      }  
    } 
    else if (data.equals("isEmpty"))
    { return "(" + pre + ".size() == 0)"; } 
    else if (data.equals("notEmpty"))
    { return "(" + pre + ".size() != 0)"; } 
    else if (data.equals("reverse") || data.equals("sort") ||
             data.equals("asSet") || data.equals("asBag") || 
             data.equals("asOrderedSet")) 
    { return "Set." + data + "(" + pre + ")"; } 
    else if (data.equals("keys"))
    { return pre + ".keySet()"; }
    else if (data.equals("values"))
    { return pre + ".values()"; }
    else if (data.equals("closure"))
    { String rel = ((BasicExpression) argument).data; 
      Expression arg = ((BasicExpression) argument).objectRef; 
      if (entity == null) 
      { JOptionPane.showMessageDialog(null, "No entity for: " + this, "Semantic error", JOptionPane.ERROR_MESSAGE);
        return ""; 
      }  
      if (arg == null) 
      { return "Set.closure" + entity.getName() + rel + "(new Vector())"; }  // not correct really. 
      return "Set.closure" + entity.getName() + rel + "(" + arg.queryForm(env,local) + ")";
    } 
    else if (argument.type != null && 
             "String".equals("" + argument.getType()))
    { // last,first,front,tail on strings
      if (data.equals("first") || data.equals("any"))
      { return "(" + pre + ".charAt(0) + \"\")"; } 
      if (data.equals("last"))
      { return "(" + pre + ".charAt(" + pre + ".length()-1) + \"\")"; } 
      if (data.equals("front"))
      { return pre + ".substring(0," + pre + ".length()-1)"; } 
      if (data.equals("tail"))
      { return pre + ".substring(1," + pre + ".length())"; } 
    } 
    else if (data.equals("max") || data.equals("min"))
    { Type compertype = argument.getElementType();   
      if (compertype == null) 
      { compertype = type; } 
      if (compertype == null) 
      { JOptionPane.showMessageDialog(null, "No type for: " + this, 
                                      "Type error", JOptionPane.ERROR_MESSAGE);
        return "Set." + data + "(" + pre + ")"; 
      } 

      String ctname = compertype.getName();   // only int, double, String are valid

      if ("int".equals(ctname))
      { return "((Integer) Set." + data + "(" + pre + ")).intValue()"; }
      else if ("double".equals(ctname))
      { return "((Double) Set." + data + "(" + pre + ")).doubleValue()"; }
      else if ("long".equals(ctname))
      { return "((Long) Set." + data + "(" + pre + ")).longValue()"; }
      else 
      { return "((" + ctname + ") Set." + data + "(" + pre + "))"; }
    } 
    else if (data.equals("any") || data.equals("last") || 
             data.equals("first"))
    { Type et = argument.elementType; 
      if (et != null) 
      { if ("String".equals("" + et) || et.isEntity())
        { return "((" + et + ") Set." + data + "(" + pre + "))"; }
        else if (Type.isPrimitiveType(et))
        { return unwrap("Set." + data + "(" + pre + ")"); } 
        else 
        { String elemTyp = et.getJava(); 
          return "((" + elemTyp + ") Set." + data + "(" + pre + "))"; 
        }
      } 
      else 
      { return "Set." + data + "(" + pre + ")"; }
    }  
    else  // sum, front, tail on sequences, sorted sets/maps 
          // -- separate versions for numbers & objects?
    { return "Set." + data + "(" + pre + ")"; }

    Type et = argument.elementType; 
    if (et != null) 
    { if ("String".equals("" + et) || et.isEntity())
      { return "((" + et + ") Set." + data + "(" + pre + "))"; }
      else 
      { return unwrap("Set." + data + "(" + pre + ")"); } 
    } 
    else 
    { return "Set." + data + "(" + pre + ")"; }
    // return qf + ".get(0)"; 
  }

  public String queryFormJava6(java.util.Map env, boolean local)
  { String qf = argument.queryFormJava6(env,local); 
    String cont = "Controller.inst()"; 

    if (operator.equals("lambda") && accumulator != null)
    { String acc = accumulator.getName(); 
      return "((" + acc + ") -> { return " + qf + "; })"; // for Java8+ 
    }

    if (operator.equals("-"))
    { if (needsBracket) 
      { return "(-" + qf + ")"; } 
      return "-" + qf; 
    } 

    if (operator.equals("!"))
    { String res = qf; 
      // Type argelemtype = argument.getElementType(); 
      // if (argelemtype != null && 
      //     (argelemtype.isCollection() || 
      //      argelemtype.isFunctionType() || 
      //      argelemtype.isClassEntityType()))
      // { } 
      // else
      // if (Type.isBasicType(argelemtype)) // or a Ref 

      if (argument instanceof BinaryExpression) 
      { BinaryExpression be = (BinaryExpression) argument; 
        if (be.left.isRef() && "+".equals(be.operator))
        { // !(ptr + n) is ptr[n]

          String lqf = be.left.queryFormJava6(env,local); 
          String rqf = be.right.queryFormJava6(env,local);
          res = lqf + "[" + rqf + "]";
        } 
        else 
        { res = qf + "[0]"; }
      }
      else if (argument instanceof UnaryExpression)
      { UnaryExpression ue = (UnaryExpression) argument; 
        if ("?".equals(ue.operator)) 
        { res = ue.argument.queryFormJava6(env,local); } 
        else 
        { res = qf + "[0]"; }
      }
      else   
      { res = qf + "[0]"; }  

      if (needsBracket) 
      { res = "(" + res + ")"; }
      return res;  
    } // functions, classes, collections, maps have ?x = x

    if (operator.equals("?") || operator.equals("!"))
    { return qf; } 

    if (operator.equals("not"))
    { return "!(" + qf + ")"; } 

    if (operator.equals("->size"))
    { if (argument.umlkind == CLASSID && (argument instanceof BasicExpression) && 
          ((BasicExpression) argument).arrayIndex == null)
      { return cont + "." + qf.toLowerCase() + "s.size()"; } 
      if (argument.type != null && argument.type.getName().equals("String"))
      { return qf + ".length()"; } 
      if (argument.isNumeric())
      { return "(\"\" + " + qf + ").length()"; } 
      return qf + ".size()";
    } 

    if (operator.equals("->display"))
    { return "true"; } 

    if (operator.equals("->isDeleted")) 
    { // return new BasicExpression("false"); 
      if (argument.isMultiple())
      { return "(" + qf + ".size() == 0)"; } 
      else
      { String objstype = argument.type.getName();  
        return "!(" + cont + "." + objstype.toLowerCase() + 
               "s.contains(" + qf + "))"; 
      }  
    } 

    if (operator.equals("->isInteger")) 
    { return "Set.isInteger(" + qf + ")"; } 

    if (operator.equals("->isLong")) 
    { return "Set.isLong(" + qf + ")"; } 

    if (operator.equals("->isReal")) 
    { return "Set.isReal(" + qf + ")"; } 

    if (operator.equals("->toInteger")) 
    { return "Integer.decode(" + qf + ").intValue()"; } 

    if (operator.equals("->toLong")) 
    { return "Long.decode(" + qf + ").longValue()"; } 

    if (operator.equals("->toReal")) 
    { return "Double.parseDouble(" + qf + ")"; } 

    if (operator.equals("->toBoolean")) 
    { return "\"true\".equals(" + qf + " + \"\")"; } 

    if (operator.equals("->char2byte")) 
    { return "Set.char2byte(" + qf + ")"; } 

    if (operator.equals("->byte2char")) 
    { return "Set.byte2char(" + qf + ")"; } 

    if (operator.equals("->ord"))
    { if (argument.isString())
      { return "Set.char2byte(" + qf + ")"; } 
      return qf; 
    } 

    if (operator.equals("->succ"))
    { if (argument.isString())
      { return "Set.byte2char(1 + Set.char2byte(" + qf + "))"; } 
      return "(" + qf + " + 1)"; 
    } 

    if (operator.equals("->pred"))
    { if (argument.isString())
      { return "Set.byte2char(Set.char2byte(" + qf + ") - 1)"; } 
      return "(" + qf + " - 1)"; 
    } 

    if (operator.equals("->toLower")) 
    { return qf + ".toLowerCase()"; } 

    if (operator.equals("->toUpper")) 
    { return qf + ".toUpperCase()"; } 

    if (operator.equals("->toLowerCase")) 
    { return qf + ".toLowerCase()"; } 

    if (operator.equals("->toUpperCase")) 
    { return qf + ".toUpperCase()"; } 

    if (operator.equals("->trim")) 
    { return qf + ".trim()"; } 

    if (operator.equals("->oclIsUndefined")) 
    { if (argument.isNumeric())
      { return "Double.isNaN((double) " + qf + ")"; } 
      return "(" + qf + " == null)"; 
    } 

    if (operator.equals("->oclIsInvalid")) 
    { return "Double.isNaN(" + qf + ")"; } 

    if (operator.equals("->oclIsNew")) 
    { return "(" + qf + " != null)"; } 

    if (operator.equals("->oclType"))
    { String wqf = argument.wrapJava6(qf); 
      return "(" + wqf + ").getClass()"; 
    }  

    if (operator.equals("->asSequence")) 
    { if (argument.isMap())
      { return "Set.mapAsSequence(" + qf + ")"; } 
      return "Set.asSequence(" + qf + ")"; 
    }  
    // but maps cannot be converted 

    if (operator.equals("->asSet")) 
    { if (argument.isMap())
      { return "Set.mapAsSet(" + qf + ")"; } 
      return "Set.asSet(" + qf + ")"; 
    }

    if (operator.equals("->asOrderedSet")) 
    { return "Set.asOrderedSet(" + qf + ")"; }

    if (operator.equals("->asBag")) 
    { return "Set.sort(" + qf + ")"; }  

    if (operator.equals("->characters")) 
    { return "Set.characters(" + qf + ")"; } 

    if ("->copy".equals(operator))
    { if (type == null) 
      { return qf; } 
      if (type.isEntity())
      { String tcs = type.getJava6(); 
        return "((" + tcs + ") " + qf + ".clone())"; 
      }
      String tname = type.getName(); 
      if ("String".equals(tname))
      { return "(\"\"" + qf + ")"; } 
      if ("Set".equals(tname))
      { return "Set.copySet(" + qf + ")"; } 
      if ("Sequence".equals(tname))
      { return "Set.copySequence(" + qf + ")"; } 
      if ("Map".equals(tname))
      { return "Set.copyMap(" + qf + ")"; } 
      return qf; 
    }    

    if (operator.equals("->iterator"))
    { if (argument.isSequenceValued())
      { return "OclIterator.newOclIterator_Sequence(" + qf + ")"; }
      else if (argument.isMap())
      { return "OclIterator.newOclIterator_Set(" + qf + ".values())"; }
      else 
      { return "OclIterator.newOclIterator_Set(" + qf + ")"; }
    }

    if (operator.equals("->sqr")) 
    { return "((" + qf + ")*(" + qf + "))"; } 

    if (operator.equals("->concatenateAll")) 
    { return "Set.concatenateAll(" + qf + ")"; } 
    
    if (operator.equals("->unionAll") && 
        argument.elementType != null &&
        argument.elementType.isMap())
    { return "Set.unionAllMap(" + qf + ")"; } 

    if (operator.equals("->unionAll"))
    { return "Set.unionAll(" + qf + ")"; } 
    
    if (operator.equals("->intersectAll") && 
        argument.elementType != null &&
        argument.elementType.isMap())
    { return "Set.intersectAllMap(" + qf + ")"; } 

    if (operator.equals("->intersectAll"))
    { return "Set.intersectAll(" + qf + ")"; } 

    if (operator.equals("->flatten")) 
    { if (Type.isSequenceType(argument.type))
      { return "Set.concatenateAll(" + qf + ")"; } 
      else if (Type.isSetType(argument.type))
      { return "Set.unionAll(" + qf + ")"; } 
      else if (Type.isMapType(argument.type))
      { return "Set.unionAllMap(" + qf + ")"; } 
      return qf; 
    } // but only goes one level down. 

    String pre = qf;
    String data = operator.substring(2,operator.length()); 

    if (extensionoperators.containsKey(operator))
    { String op = operator;
      String opjava = Expression.getOperatorJava(op); 
      if (opjava != null && opjava.length() > 0)
      { return opjava.replaceAll("_1",qf); } 
      if (operator.startsWith("->"))
      { op = operator.substring(2,operator.length()); } 
      return qf + "." + op + "()";
    } 

    if (argument.umlkind == CLASSID && (argument instanceof BasicExpression))
    { pre = ((BasicExpression) argument).classExtentQueryFormJava6(env,local); } 

    if (operator.equals("->abs") || 
        operator.equals("->sin") ||
        operator.equals("->cos") || 
        operator.equals("->tan") ||
        operator.equals("->exp") || 
        operator.equals("->log") ||
        operator.equals("->log10") || 
        operator.equals("->atan") || 
        operator.equals("->acos") || 
        operator.equals("->asin") ||
        operator.equals("->cbrt") || 
        operator.equals("->cosh") || 
        operator.equals("->sinh") ||
        operator.equals("->tanh") || 
        operator.equals("->sqrt"))  
      // But exp, floor can be applied to sets/sequences. ceil not sqr? 
    { return "Math." + data + "(" + pre + ")"; }
    else if (operator.equals("->round") || 
             operator.equals("->ceil") || operator.equals("->floor"))
    { return "((int) Math." + data + "(" + pre + "))"; } 
    else if (data.equals("toUpperCase") || 
             data.equals("toLowerCase"))
    { return pre + "." + data + "()"; } 
    else if (data.equals("sum"))
    { Type sumtype = argument.getElementType();  // int, double, long, String 
      if (sumtype == null) 
      { JOptionPane.showMessageDialog(null, "!! No type for: " + this, 
                                      "Type error", JOptionPane.ERROR_MESSAGE);
        return ""; // "Set." + data + "(" + pre + ")"; 
      } 
      String tname = sumtype.getName(); 
      return "Set.sum" + tname + "(" + pre + ")"; 
    } 
    else if (data.equals("prd"))
    { Type sumtype = argument.getElementType();  // int, double, long 
      if (sumtype == null) 
      { JOptionPane.showMessageDialog(null, "No type for: " + this, 
                                      "Type error", JOptionPane.ERROR_MESSAGE);
        return ""; // "Set." + data + "(" + pre + ")"; 
      }
      String tname = sumtype.getName(); 
      return "Set.prd" + tname + "(" + pre + ")"; 
    } 
    else if (data.equals("isEmpty"))
    { return "(" + pre + ".size() == 0)"; } 
    else if (data.equals("notEmpty"))
    { return "(" + pre + ".size() != 0)"; } 
    else if (data.equals("reverse") || data.equals("sort")) 
    { return "Set." + data + "(" + pre + ")"; }   // an ArrayList
    else if (data.equals("max") || data.equals("min"))   
    { String bqf = "Collections." + data + "(" + pre + ")"; 
      Type et = argument.elementType; 
      if (et != null) 
      { if ("String".equals("" + et) || et.isEntity())
        { return "((" + et + ") " + bqf + ")"; }
        else if (Type.isPrimitiveType(et))
        { return unwrapJava6(bqf); }
        else 
        { return bqf; }  
      } 
    } 
    else if (data.equals("asSet") || 
             data.equals("asSequence") ||
             data.equals("asBag") || 
             data.equals("asOrderedSet"))
    { return "Set." + data + "(" + pre + ")"; } 
    else if (data.equals("keys"))
    { return pre + ".keySet()"; }
    else if (data.equals("values"))
    { return pre + ".values()"; }
    else if (data.equals("closure"))
    { String rel = ((BasicExpression) argument).data; 
      Expression arg = ((BasicExpression) argument).objectRef; 
      if (entity == null) 
      { JOptionPane.showMessageDialog(null, "No entity for: " + this, "Semantic error", JOptionPane.ERROR_MESSAGE);
        return ""; 
      }  
      if (arg == null) 
      { if (argument.isOrdered())
        { return "Set.closure" + entity.getName() + rel + "(new ArrayList())"; }  
        else 
        { return "Set.closure" + entity.getName() + rel + "(new HashSet())"; } 
      }  
      return "Set.closure" + entity.getName() + rel + "(" + arg.queryFormJava6(env,local) + ")";
    } 
    else if (argument.type != null && 
             "String".equals("" + argument.getType()))
    { // last,first,front,tail on strings
      if (data.equals("first") || data.equals("any"))
      { return "(" + pre + ".charAt(0) + \"\")"; } 
      if (data.equals("last"))
      { return "(" + pre + ".charAt(" + pre + ".length()-1) + \"\")"; } 
      if (data.equals("front"))
      { return pre + ".substring(0," + pre + ".length()-1)"; } 
      if (data.equals("tail"))
      { return pre + ".substring(1," + pre + ".length())"; } 
    } 
    else if (data.equals("any") || 
             data.equals("last") || data.equals("first"))
    { Type et = argument.elementType; 
      if (et != null) 
      { if ("String".equals("" + et) || et.isEntity())
        { return "((" + et + ") Set." + data + "(" + pre + "))"; }
        else if (Type.isPrimitiveType(et))
        { return unwrapJava6("Set." + data + "(" + pre + ")"); } 
        else 
        { String elemTyp = et.getJava6(); 
          return "((" + elemTyp + ") Set." + data + "(" + pre + "))"; 
        }
      } 
      else 
      { return "Set." + data + "(" + pre + ")"; }
    }  
    else  // sum, prd, front tail on sequences, subcollections 
          // -- separate versions for numbers & objects?
    { return "Set." + data + "(" + pre + ")"; }

    Type et = argument.elementType; 
    if (et != null) 
    { if ("String".equals("" + et) || et.isEntity())
      { return "((" + et + ") Set." + data + "(" + pre + "))"; }
      else if (Type.isPrimitiveType(et)) 
      { return unwrapJava6("Set." + data + "(" + pre + ")"); } 
      else 
      { String elemTyp = et.getJava6(); 
        return "((" + elemTyp + ") Set." + data + "(" + pre + "))"; 
      }
    } 
    else 
    { return "Set." + data + "(" + pre + ")"; }
    // return qf + ".get(0)"; 
  } 

  public String queryFormJava7(java.util.Map env, boolean local)
  { String qf = argument.queryFormJava7(env,local); 
    String cont = "Controller.inst()"; 

    if (operator.equals("lambda") && accumulator != null)
    { String acc = accumulator.getName(); 
      return "(" + acc + ") -> { return " + qf + "; }"; // for Java8+ 
    }

    if (operator.equals("-"))
    { if (needsBracket) 
      { return "(-" + qf + ")"; } 
      return "-" + qf; 
    } 

    if (operator.equals("!"))
    { String res = qf; 
      // Type argelemtype = argument.getElementType(); 
      // if (argelemtype != null && 
      //     (argelemtype.isCollection() || 
      //      argelemtype.isFunctionType() || 
      //      argelemtype.isClassEntityType()))
      // { } 
      // else
      // if (Type.isBasicType(argelemtype)) // or a Ref 

      if (argument instanceof BinaryExpression) 
      { BinaryExpression be = (BinaryExpression) argument; 
        if (be.left.isRef() && "+".equals(be.operator))
        { // !(ptr + n) is ptr[n]

          String lqf = be.left.queryFormJava7(env,local); 
          String rqf = be.right.queryFormJava7(env,local);
          res = lqf + "[" + rqf + "]";
        } 
        else 
        { res = qf + "[0]"; }
      }
      else if (argument instanceof UnaryExpression)
      { UnaryExpression ue = (UnaryExpression) argument; 
        if ("?".equals(ue.operator)) 
        { res = ue.argument.queryFormJava7(env,local); } 
        else 
        { res = qf + "[0]"; }
      }
      else   
      { res = qf + "[0]"; }  

      if (needsBracket) 
      { res = "(" + res + ")"; }
      return res;  
    } // functions, classes, collections, maps have ?x = x

    if (operator.equals("?") || operator.equals("!"))
    { return qf; } 

    if (operator.equals("not"))
    { return "!(" + qf + ")"; } 

    if (operator.equals("->size"))
    { if (argument.umlkind == CLASSID && 
          (argument instanceof BasicExpression) && 
          ((BasicExpression) argument).arrayIndex == null)
      { return cont + "." + qf.toLowerCase() + "s.size()"; } 
      if (argument.type != null && argument.type.getName().equals("String"))
      { return "(" + qf + ").length()"; } 
      if (argument.isNumeric())
      { return "(\"\" + " + qf + ").length()"; } 
      return "(" + qf + ").size()";
    } 

    if (operator.equals("->display"))
    { return "true"; } 

    if (operator.equals("->isDeleted")) 
    { // return new BasicExpression("false"); 
      if (argument.isMultiple())
      { return "(" + qf + ".size() == 0)"; } 
      else
      { String objstype = argument.type.getName();  
        return "!(" + cont + "." + objstype.toLowerCase() + 
               "s.contains(" + qf + "))"; 
      }  
    } 

    if (operator.equals("->isInteger")) 
    { return "Ocl.isInteger(" + qf + ")"; } 

    if (operator.equals("->isLong")) 
    { return "Ocl.isLong(" + qf + ")"; } 

    if (operator.equals("->isReal")) 
    { return "Ocl.isReal(" + qf + ")"; } 

    if (operator.equals("->toInteger")) 
    { return "Ocl.toInteger(" + qf + ")"; } 

    if (operator.equals("->toLong")) 
    { return "Long.decode(" + qf + ").longValue()"; } 

    if (operator.equals("->toReal")) 
    { return "Ocl.toDouble(" + qf + ")"; } 

    if (operator.equals("->toBoolean")) 
    { return "\"true\".equals(" + qf + " + \"\")"; } 

    if (operator.equals("->char2byte")) 
    { return "Ocl.char2byte(" + qf + ")"; } 

    if (operator.equals("->byte2char")) 
    { return "Ocl.byte2char(" + qf + ")"; } 

    if (operator.equals("->ord"))
    { if (argument.isString())
      { return "Ocl.char2byte(" + qf + ")"; } 
      return qf; 
    } 

    if (operator.equals("->succ"))
    { if (argument.isString())
      { return "Ocl.byte2char(1 + Ocl.char2byte(" + qf + "))"; } 
      return "(" + qf + " + 1)"; 
    } 

    if (operator.equals("->pred"))
    { if (argument.isString())
      { return "Ocl.byte2char(Ocl.char2byte(" + qf + ") - 1)"; } 
      return "(" + qf + " - 1)"; 
    } 

    if (operator.equals("->toLower")) 
    { return qf + ".toLowerCase()"; } 

    if (operator.equals("->toUpper")) 
    { return qf + ".toUpperCase()"; } 

    if (operator.equals("->toLowerCase")) 
    { return qf + ".toLowerCase()"; } 

    if (operator.equals("->toUpperCase")) 
    { return qf + ".toUpperCase()"; } 

    if (operator.equals("->trim")) 
    { return qf + ".trim()"; } 

    if (operator.equals("->oclIsUndefined")) 
    { if (argument.isNumeric())
      { return "Double.isNaN((double) " + qf + ")"; } 
      return "(" + qf + " == null)"; 
    } 

    if (operator.equals("->oclIsInvalid")) 
    { return "Double.isNaN(" + qf + ")"; } 

    if (operator.equals("->oclIsNew")) 
    { return "(" + qf + " != null)"; } 

    if (operator.equals("->oclType"))
    { String wqf = argument.wrap(qf); 
      return "(" + wqf + ").getClass()"; 
    }  

    if (operator.equals("->asSequence")) 
    { return "Ocl.asSequence(" + qf + ")"; }
    // but maps cannot be converted 

    if (operator.equals("->asSet")) 
    { return "Ocl.asSet(" + qf + ")"; }

    if (operator.equals("->asOrderedSet")) 
    { return "Ocl.asOrderedSet(" + qf + ")"; }

    if (operator.equals("->asBag"))
    { return "Ocl.sort(" + qf + ")"; } 

    if (operator.equals("->characters")) 
    { return "Ocl.characters(" + qf + ")"; } 

    if ("->copy".equals(operator))
    { if (type == null) 
      { return qf; } 
      if (type.isEntity())
      { String tname = type.getJava7(); 
        return "((" + tname + ") " + qf + ".clone())";
      }

      String tname = type.getName(); 

      if ("String".equals(tname))
      { return "(\"\"" + qf + ")"; } 

      if ("Set".equals(tname) && isSorted())
      { return "Ocl.copySortedSet(" + qf + ")"; } 
      if ("Set".equals(tname))
      { return "Ocl.copySet(" + qf + ")"; } 

      if ("Sequence".equals(tname))
      { return "Ocl.copySequence(" + qf + ")"; } 

      if ("Map".equals(tname) && isSorted())
      { return "Ocl.copySortedMap(" + qf + ")"; } 
      if ("Map".equals(tname))
      { return "Ocl.copyMap(" + qf + ")"; } 
      
      return qf; 
    }    

    if (operator.equals("->iterator"))
    { if (argument.isSequenceValued())
      { return "OclIterator.newOclIterator_Sequence(" + qf + ")"; }
      else if (argument.isMap())
      { return "OclIterator.newOclIterator_Set(" + qf + ".values())"; }
      else 
      { return "OclIterator.newOclIterator_Set(" + qf + ")"; }
    }

    if (operator.equals("->sqr")) 
    { return "((" + qf + ")*(" + qf + "))"; } 

    if (operator.equals("->concatenateAll")) 
    { String jtype = type.getJava7(elementType); 
      return "((" + jtype + ") Ocl.concatenateAll(" + qf + "))"; 
    } 
    
    if (operator.equals("->unionAll") && 
        argument.elementType != null &&
        argument.elementType.isMap())
    { String jtype = type.getJava7(elementType); 
      return "((" + jtype + ") Ocl.unionAllMap(" + qf + "))"; 
    } 

    if (operator.equals("->unionAll"))
    { String jtype = type.getJava7(elementType); 
      return "((" + jtype + ") Ocl.unionAll(" + qf + "))"; 
    } 
    
   if (operator.equals("->intersectAll") && 
        argument.elementType != null &&
        argument.elementType.isMap())
    { String jtype = type.getJava7(elementType); 
      return "((" + jtype + ") Ocl.intersectAllMap(" + qf + "))"; 
    } 

    if (operator.equals("->intersectAll"))
    { String jtype = type.getJava7(elementType); 
      return "((" + jtype + ") Ocl.intersectAll(" + qf + "))"; 
    } 

    if (operator.equals("->flatten")) 
    { String jtype = type.getJava7(elementType); 
      if (Type.isSequenceType(argument.type))
      { return "((" + jtype + ") Ocl.concatenateAll(" + qf + "))"; } 
      else if (Type.isSetType(argument.type))
      { return "((" + jtype + ") Ocl.unionAll(" + qf + "))"; } 
      else if (Type.isMapType(argument.type))
      { return "((" + jtype + ") Ocl.unionAllMap(" + qf + "))"; } 
      return qf; 
    } // but only goes one level down. 

    String pre = qf;
    String data = operator.substring(2,operator.length()); 

    if (extensionoperators.containsKey(operator))
    { String op = operator;
      String opjava = Expression.getOperatorJava(op); 
      if (opjava != null && opjava.length() > 0)
      { return opjava.replaceAll("_1",qf); } 
      if (operator.startsWith("->"))
      { op = operator.substring(2,operator.length()); } 
      return qf + "." + op + "()";
    } 

    if (argument.umlkind == CLASSID && (argument instanceof BasicExpression))
    { pre = ((BasicExpression) argument).classExtentQueryFormJava7(env,local); } 

    if (operator.equals("->abs") || operator.equals("->sin") ||
        operator.equals("->cos") || operator.equals("->tan") ||
        operator.equals("->exp") || operator.equals("->log") ||
        operator.equals("->log10") || operator.equals("->atan") || 
        operator.equals("->acos") || operator.equals("->asin") ||
        operator.equals("->cbrt") || operator.equals("->cosh") || data.equals("sinh") ||
        operator.equals("->tanh") || operator.equals("->sqrt"))  
      // But exp, floor can be applied to sets/sequences. ceil not sqr? 
    { return "Math." + data + "(" + pre + ")"; }
    else if (operator.equals("->round") || 
             operator.equals("->ceil") || operator.equals("->floor"))
    { return "((int) Math." + data + "(" + pre + "))"; } 
    else if (data.equals("toUpperCase") || data.equals("toLowerCase") || data.equals("trim"))
    { return pre + "." + data + "()"; } 
    else if (data.equals("sum"))
    { Type sumtype = argument.getElementType();  // int, double, long, String 
      if (sumtype == null) 
      { JOptionPane.showMessageDialog(null, 
             "No type for: " + this, 
             "Type error", JOptionPane.ERROR_MESSAGE);
        return ""; // "Ocl." + data + "(" + pre + ")"; 
      } 
      String tname = sumtype.getName(); 
      return "Ocl.sum" + tname + "(" + pre + ")"; 
    } 
    else if (data.equals("prd"))
    { Type sumtype = argument.getElementType();  // int, double, long 
      if (sumtype == null) 
      { JOptionPane.showMessageDialog(null, "No type for: " + this, 
                                      "Type error", JOptionPane.ERROR_MESSAGE);
        return ""; // "Ocl." + data + "(" + pre + ")"; 
      }
      String tname = sumtype.getName(); 
      return "Ocl.prd" + tname + "(" + pre + ")"; 
    } 
    else if (data.equals("isEmpty"))
    { return "(" + pre + ".size() == 0)"; } 
    else if (data.equals("notEmpty"))
    { return "(" + pre + ".size() != 0)"; } 
    else if (data.equals("reverse") || data.equals("sort")) 
    { String bfq = "Ocl." + data + "(" + pre + ")";   
      String cast = type.getJava7(elementType); 
      return "((" + cast + ") " + bfq + ")"; 
    } 
    else if (data.equals("max") || data.equals("min"))   
    { String bqf = "Collections." + data + "(" + pre + ")"; 
      Type et = argument.elementType; 
      if (et != null) 
      { if ("String".equals("" + et) || et.isEntity())
        { return "((" + et + ") " + bqf + ")"; }
        else if (Type.isPrimitiveType(et))
        { return unwrap(bqf); }
        else 
        { return bqf; }  
      } 
    } 
    else if (data.equals("asSet") || data.equals("asBag") ||
             data.equals("asOrderedSet") ||
             data.equals("asSequence"))
    { return "Ocl." + data + "(" + pre + ")"; } 
    else if (data.equals("keys"))
    { return pre + ".keySet()"; }
    else if (data.equals("values"))
    { return pre + ".values()"; }
    else if (data.equals("closure"))
    { String rel = ((BasicExpression) argument).data; 
      Expression arg = ((BasicExpression) argument).objectRef; 
      if (entity == null) 
      { JOptionPane.showMessageDialog(null, "No entity for: " + this, "Semantic error", JOptionPane.ERROR_MESSAGE);
        return ""; 
      }  
      if (arg == null) 
      { String emptycoll = "new HashSet()"; 
        if (argument.isOrdered())
        { emptycoll = "new ArrayList()"; } 
        else if (argument.isSorted())
        { emptycoll = "new TreeSet()"; } 
        return "Ocl.closure" + entity.getName() + rel + "(" + emptycoll + ")"; 
      }  // not correct really. 
      return "Ocl.closure" + entity.getName() + rel + "(" + arg.queryFormJava7(env,local) + ")";
    } 
    else if (argument.type != null && 
             "String".equals("" + argument.type.getName()))
    { // last,first,front,tail on strings
      if (data.equals("first") || data.equals("any"))
      { return "(" + pre + ".charAt(0) + \"\")"; } 
      if (data.equals("last"))
      { return "(" + pre + ".charAt(" + pre + ".length()-1) + \"\")"; } 
      if (data.equals("front"))
      { return pre + ".substring(0," + pre + ".length()-1)"; } 
      if (data.equals("tail"))
      { return pre + ".substring(1," + pre + ".length())"; } 
    } 
    else if (data.equals("any") || 
             data.equals("last") || 
             data.equals("first"))
    { Type et = argument.elementType; 
      if (et != null) 
      { if ("String".equals("" + et) || et.isEntity())
        { return "((" + et + ") Ocl." + data + "(" + pre + "))"; }
        else if (Type.isPrimitiveType(et))
        { return unwrap("Ocl." + data + "(" + pre + ")"); } 
        else 
        { String elemTyp = et.getJava7(et.getElementType()); 
          return "((" + elemTyp + ") Ocl." + data + "(" + pre + "))"; 
        }
      } 
      else 
      { return "Ocl." + data + "(" + pre + ")"; }
    }  
    else  // sum, prd, front tail on sequences, subcollections 
          // -- separate versions for numbers & objects?
    { return "Ocl." + data + "(" + pre + ")"; }

    Type et = argument.elementType; 
    if (et != null) 
    { if ("String".equals("" + et) || et.isEntity())
      { return "((" + et + ") Ocl." + data + "(" + pre + "))"; }
      else if (Type.isPrimitiveType(et)) 
      { return unwrap("Ocl." + data + "(" + pre + ")"); } 
      else 
      { String elemTyp = et.getJava7(et.getElementType()); 
        return "((" + elemTyp + ") Ocl." + data + "(" + pre + "))"; 
      }
    } 
    else 
    { return "Ocl." + data + "(" + pre + ")"; }
    // return qf + ".get(0)"; 
  } 

  public String queryFormCSharp(java.util.Map env, boolean local)
  { String qf = argument.queryFormCSharp(env,local); 
    String cont = "Controller.inst()"; 

    if (operator.equals("lambda") && accumulator != null)
    { String acc = accumulator.getName(); 
      return acc + " => (" + qf + ")"; // for C# 3+ 
    }

    if (operator.equals("-"))
    { if (needsBracket) 
      { return "(-" + qf + ")"; } 
      return "-" + qf; 
    } 

    if (operator.equals("+"))
    { return qf; } 

    if (operator.equals("?"))
    { String res = qf; 
      if (argument.isCollection() || 
          argument.isFunctionType() || 
          argument.isClassEntityType())
      { } // already its own reference
      else 
      { res = "&" + qf; } 

      if (needsBracket) 
      { res = "(" + res + ")"; }
      return res;  
    } 

    if (operator.equals("!"))
    { String res = qf; 
      Type argelemtype = argument.getElementType(); 
      if (argelemtype != null && 
          (argelemtype.isCollection() || 
           argelemtype.isFunctionType() || 
           argelemtype.isClassEntityType()))
      { } 
      else 
      { res = "*" + qf; }  

      if (needsBracket) 
      { res = "(" + res + ")"; }
      return res;  
    } // functions, classes, collections, maps have ?x = x

    if (operator.equals("not"))
    { return "!(" + qf + ")"; } 

    if (operator.equals("->size"))
    { if (argument.umlkind == CLASSID && 
          (argument instanceof BasicExpression) && 
          ((BasicExpression) argument).arrayIndex == null)
      { qf = ((BasicExpression) argument).classExtentQueryFormCSharp(env,local); } 
      if (argument.type != null && argument.type.getName().equals("String"))
      { return "(" + qf + ").Length"; } 
      return "(" + qf + ").Count";
    } 

    if (operator.equals("->display"))
    { return "true"; } 

    if (operator.equals("->isDeleted")) 
    { // return new BasicExpression("false"); 
      if (argument.isMultiple())
      { if (argument.umlkind == CLASSID && (argument instanceof BasicExpression) && 
            ((BasicExpression) argument).arrayIndex == null)
        { qf = ((BasicExpression) argument).classExtentQueryFormCSharp(env,local); } 
        return "(" + qf + ").Count == 0"; 
      } 
      else
      { String objstype = argument.type.getName();  
        return "!(" + cont + "." + objstype.toLowerCase() + 
               "_s.Contains(" + qf + "))"; 
      }  
    } 

    if (operator.equals("->asSequence")) 
    { if (argument.isRef())
      { return "SystemTypes.asSequence(" + qf + ")"; }
      
      if (argument.isSet())
      { return "SystemTypes.asSequence(" + qf + ")"; }
      
      if (argument.isMap())
      { return "SystemTypes.asSequence((Hashtable) " + qf + ")"; } 
       
      return qf; 
    } 

    if (operator.equals("->asSet") && type != null && 
        type.isSet())
    { return qf; } 

    if (operator.equals("->asSet") && type != null && 
        type.isSequence())
    { return "SystemTypes.asSet(" + qf + ")"; } 

    if (operator.equals("->asBag"))
    { return "SystemTypes.asBag(" + qf + ")"; } 

    if (operator.equals("->asReference"))
    { return "SystemTypes.asReference(" + qf + ")"; } 

    if (operator.equals("->isInteger")) 
    { return "SystemTypes.isInteger(" + qf + ")"; } 

    if (operator.equals("->isLong")) 
    { return "SystemTypes.isLong(" + qf + ")"; } 

    if (operator.equals("->isReal")) 
    { return "SystemTypes.isReal(" + qf + ")"; } 

    if (operator.equals("->toInteger")) 
    { return "SystemTypes.toInteger(" + qf + ")"; } 

    if (operator.equals("->toLong")) 
    { return "SystemTypes.toLong(" + qf + ")"; } 

    if (operator.equals("->toReal")) 
    { return "SystemTypes.toDouble(" + qf + ")"; } 

    if (operator.equals("->toBoolean")) 
    { return "SystemTypes.toBoolean(" + qf + ")"; } 

    if (operator.equals("->char2byte")) 
    { return "SystemTypes.char2byte(" + qf + ")"; } 

    if (operator.equals("->byte2char")) 
    { return "SystemTypes.byte2char(" + qf + ")"; } 

    if (operator.equals("->characters")) 
    { return "((ArrayList) SystemTypes.characters(" + qf + "))"; } 

    if (operator.equals("->oclIsUndefined")) 
    { if (argument.isNumeric())
      { return "double.IsNaN((double) " + qf + ")"; }
      return "(" + qf + " == null)"; 
    } 

    if (operator.equals("->oclIsInvalid")) 
    { return "double.IsNaN(" + qf + ")"; } 

    if (operator.equals("->oclIsNew")) 
    { return "(" + qf + " != null)"; } 

    if (operator.equals("->oclType"))
    { return "OclType.getOclTypeByMetatype((" + qf + ").GetType())"; }  

    if ("->copy".equals(operator))
    { if (type == null) 
      { return qf; } 

      if (type.isEntity())
      { String tcs = type.getCSharp(); 
        return "((" + tcs + ")" + qf + ".MemberwiseClone())"; 
      }

      String tname = type.getName(); 
      if ("String".equals(tname))
      { return "(\"\"" + qf + ")"; } 

      if ("Set".equals(tname) || "Sequence".equals(tname))
      { return "SystemTypes.copyCollection(" + qf + ")"; } 

      if ("Map".equals(tname))
      { return "SystemTypes.copyMap(" + qf + ")"; } 

      return qf; 
    }    

    if (operator.equals("->iterator"))
    { if (argument.isSequenceValued())
      { return "OclIterator.newOclIterator_Sequence(" + qf + ")"; }
      else 
      { return "OclIterator.newOclIterator_Set(" + qf + ")"; }
    }


    if (operator.equals("->sqr")) 
    { return "((" + qf + ")*(" + qf + "))"; } 

    if (operator.equals("->concatenateAll")) 
    { return "SystemTypes.concatenateAll(" + qf + ")"; } 
    
    if (operator.equals("->unionAll") && 
        argument.elementType != null &&
        argument.elementType.isMap())
    { return "SystemTypes.unionAllMap(" + qf + ")"; } 

    if (operator.equals("->unionAll") && 
        argument.elementType != null &&
        argument.elementType.isSortedSet())
    { return "SystemTypes.unionAllSortedSet(" + qf + ")"; } 

    if (operator.equals("->unionAll") && 
        argument.elementType != null &&
        argument.elementType.isSet())
    { return "SystemTypes.unionAllSet(" + qf + ")"; } 

    if (operator.equals("->unionAll"))
    { return "SystemTypes.unionAll(" + qf + ")"; } 
    
    if (operator.equals("->intersectAll") && 
        argument.elementType != null &&
        argument.elementType.isMap())
    { return "SystemTypes.intersectAllMap(" + qf + ")"; } 

    if (operator.equals("->intersectAll") && 
        argument.elementType != null &&
        argument.elementType.isSortedSet())
    { return "SystemTypes.intersectAllSortedSet(" + qf + ")"; } 

    if (operator.equals("->intersectAll") && 
        argument.elementType != null &&
        argument.elementType.isSet())
    { return "SystemTypes.intersectAllSet(" + qf + ")"; } 

    if (operator.equals("->intersectAll"))
    { return "SystemTypes.intersectAll(" + qf + ")"; } 

    if (operator.equals("->flatten") && 
        argument.elementType != null) 
    { Type et = argument.elementType;
 
      if (Type.isSequenceType(et))
      { return "SystemTypes.concatenateAll(" + qf + ")"; } 
      else if (Type.isSetType(et))
      { return "SystemTypes.unionAllSet(" + qf + ")"; } 
      else if (Type.isMapType(et))
      { return "SystemTypes.unionAllMap(" + qf + ")"; } 

      return qf; 
    } // but only goes one level down. 
    else if (operator.equals("->flatten"))
    { return qf; } 

    String pre = qf;
    String data = operator; 
	
    if (operator.startsWith("->"))
    { data = operator.substring(2,operator.length()); }

    if (extensionoperators.containsKey(operator))
    { String op = operator;
      String opcsharp = Expression.getOperatorCSharp(op); 
      if (opcsharp != null && opcsharp.length() > 0)
      { return opcsharp.replaceAll("_1",qf); } 
      if (operator.startsWith("->"))
      { op = operator.substring(2,operator.length()); } 
      return qf + "." + op + "()";
    } 

    if (argument.umlkind == CLASSID && (argument instanceof BasicExpression))
    { pre = ((BasicExpression) argument).classExtentQueryFormCSharp(env,local); } 

    if (operator.equals("->abs") || operator.equals("->tan") || 
        operator.equals("->sqrt") || 
        operator.equals("->sin") || operator.equals("->cos") ||  
        operator.equals("->tanh") || operator.equals("->sinh") ||  
        operator.equals("->cosh") || operator.equals("->asin") ||  
        operator.equals("->acos") || operator.equals("->atan") ||
        operator.equals("->log") || operator.equals("->log10") ||
        operator.equals("->exp") || operator.equals("->cbrt"))  
      // But exp, floor can be applied to sets/sequences. ceil not sqr? 
    { String data1 = (data.charAt(0) + "").toUpperCase() + data.substring(1,data.length()); 
      return "Math." + data1 + "(" + pre + ")"; 
    }  // upper case
    
    if (operator.equals("->floor") ||
        operator.equals("->round"))  
      // But exp, floor can be applied to sets/sequences. ceil not sqr? 
    { String data1 = (data.charAt(0) + "").toUpperCase() + data.substring(1,data.length()); 
      return "((int) Math." + data1 + "(" + pre + "))"; 
    }  // could be long for double arguments 
    else if (operator.equals("->ceil"))
    { return "((int) Math.Ceiling(" + pre + "))"; } 
    else if (data.equals("toUpperCase") || data.equals("toUpper")) 
    { return pre + ".ToUpper()"; } 
    else if (data.equals("toLowerCase") || data.equals("toLower"))
    { return pre + ".ToLower()"; } 
    else if (data.equals("trim"))
    { return pre + ".Trim()"; } 
    else if (data.equals("sum"))
    { Type sumtype = argument.getElementType();  
      // must be int, double, long, String
 
      if (sumtype == null) 
      { JOptionPane.showMessageDialog(null, 
              "No type for: " + this, 
              "Type error", JOptionPane.ERROR_MESSAGE);
        return ""; 
      }

      String tname = sumtype.getName(); 
      return "SystemTypes.sum" + tname + "(" + pre + ")"; 
    } 
    else if (data.equals("prd"))
    { Type sumtype = argument.getElementType();  
      // must be int, double, long
 
      if (sumtype == null) 
      { JOptionPane.showMessageDialog(null, 
                     "No type for: " + this, 
                     "Type error", JOptionPane.ERROR_MESSAGE);
        return ""; 
      }
      String tname = sumtype.getName(); 
      return "SystemTypes.prd" + tname + "(" + pre + ")"; 
    } 
    else if (data.equals("isEmpty"))
    { return "(" + pre + ".Count == 0)"; } 
    else if (data.equals("notEmpty"))
    { return "(" + pre + ".Count != 0)"; } 
    else if (data.equals("reverse"))
    { if (argument.isSet() || argument.isMap())
      { return pre; }
      return "SystemTypes.reverse(" + pre + ")";
    }  
    else if (data.equals("sort"))
    { if (argument.isSet() || argument.isMap())
      { return pre; }
      return "SystemTypes.sort(" + pre + ")";
    }
    else if (data.equals("asSet")) 
    { if (argument.isSet() || argument.isMap())
      { return pre; }
      return "SystemTypes.asSet(" + pre + ")"; 
    } 
    else if (data.equals("keys"))
    { return "SystemTypes.mapKeys(" + pre + ")"; }
    else if (data.equals("values"))
    { return "SystemTypes.mapValues(" + pre + ")"; }
    else if (data.equals("closure") && 
             (argument instanceof BasicExpression))
    { String rel = ((BasicExpression) argument).data;
      Expression arg = ((BasicExpression) argument).objectRef;  
      if (entity == null) 
      { JOptionPane.showMessageDialog(null, "No entity for: " + this, "Semantic error", JOptionPane.ERROR_MESSAGE);
        return ""; 
      }  
      if (arg == null) 
      { return "SystemTypes.closure" + entity.getName() + rel + "(new ArrayList())"; }  
      return "SystemTypes.closure" + entity.getName() + rel + "(" + arg.queryFormCSharp(env,local) + ")";
    } 
    else if (argument.type != null && 
             "String".equals("" + argument.getType()))
    { // last,first,front,tail on strings
      if (data.equals("first") || data.equals("any"))
      { return pre + ".Substring(0,1)"; } 
      if (data.equals("last"))
      { return pre + ".Substring(" + pre + ".Length-1, 1)"; } 
      if (data.equals("front"))
      { return pre + ".Substring(0," + pre + ".Length-1)"; } 
      if (data.equals("tail"))
      { return pre + ".Substring(1," + pre + ".Length-1)"; } 
    } 
    else if (data.equals("max") || data.equals("min") ||
             data.equals("any") || data.equals("last") || 
             data.equals("first"))
    { Type et = argument.elementType; 
      if (et != null) 
      { if ("String".equals("" + et))
        { return "((string) SystemTypes." + data + "(" + pre + "))"; } 
        else if (et.isEntity())
        { return "((" + et + ") SystemTypes." + data + "(" + pre + "))"; }
        else if (Type.isPrimitiveType(et))
        { return unwrapCSharp("SystemTypes." + data + "(" + pre + ")"); } 
        else // sequences and sorted sets
        { String elemTyp = et.getCSharp(); 
          return "((" + elemTyp + ") SystemTypes." + data + "(" + pre + "))"; 
        } 
      } 
      else 
      { return "SystemTypes." + data + "(" + pre + ")"; }
    }  
    else  // sum, prd, front tail on sequences, subcollections 
          // -- separate versions for numbers & objects?
    { return "SystemTypes." + data + "(" + pre + ")"; }

    Type et = argument.elementType; 
    if (et != null) 
    { if ("String".equals("" + et))
      { return "(string) SystemTypes." + data + "(" + pre + ")"; } 
      else if (et.isEntity())
      { return "(" + et + ") SystemTypes." + data + "(" + pre + ")"; }
      else if (Type.isPrimitiveType(et))
      { return unwrapCSharp("SystemTypes." + data + "(" + pre + ")"); } 
      else 
      { String elemTyp = et.getCSharp(); 
        return "((" + elemTyp + ") SystemTypes." + data + "(" + pre + "))"; 
      }
    } 
    else 
    { return "SystemTypes." + data + "(" + pre + ")"; }
    // return qf + ".get(0)"; 
  } 

  public String queryFormCPP(java.util.Map env, boolean local)
  { String qf = argument.queryFormCPP(env,local); 
    String cont = "Controller::inst->"; 

    Type argtype = argument.getType(); 
    String cargtype = "void*"; 

    if (argtype != null) 
    { cargtype = argtype.getCPP(argtype.getElementType()); }
 
 
    Type sumtype = argument.getElementType();  
    String celtype = "void*"; 
    if (sumtype != null) 
    { celtype = sumtype.getCPP(sumtype.getElementType()); }  
 
    if (operator.equals("lambda") && accumulator != null)
    { String acc = accumulator.getName(); 
      String acct = accumulator.getType().getCPP(
                           accumulator.getElementType()); 
	  
      return "[=](" + acct + " " + acc + ") -> " + 
             cargtype + " { return " + qf + "; }"; 
      // for C++11 onwards
    }
     
    if (operator.equals("-"))
    { if (needsBracket) 
      { return "(-" + qf + ")"; } 
      return "-" + qf; 
    } 

    if (operator.equals("?"))
    { // if (Type.isBasicType(argument.type))
      { return "&" + qf; } 
      // return qf; 
    } // objects, functions and 
      // collections are already pointers

    if (operator.equals("!"))
    { // if (Type.isBasicType(argument.getElementType()))
      { return "*" + qf; } 
      // return qf; 
    } // objects and collections are already pointers

    if (operator.equals("not"))
    { return "!(" + qf + ")"; } 

    if (operator.equals("->size"))
    { if (argument.umlkind == CLASSID && (argument instanceof BasicExpression))
      { qf = ((BasicExpression) argument).classExtentQueryFormCPP(env,local); }
      if (argument.isString())
      { return "(" + qf + ").length()"; }  
      return qf + "->size()";
    } 

    if (operator.equals("->display"))
    { return "true"; } 

    if (operator.equals("->isDeleted")) 
    { // return new BasicExpression("false"); 
      if (argument.isMultiple())
      { if (argument.umlkind == CLASSID && (argument instanceof BasicExpression))
        { qf = ((BasicExpression) argument).classExtentQueryFormCPP(env,local); } 
        return "(" + qf + "->size() == 0)"; 
      } 
      else
      { String objstype = argument.type.getName();  
        return "!(UmlRsdsLib<" + objstype + "*>::isIn(" + qf + ", " + 
                  cont + "get" + objstype.toLowerCase() + "_s()))"; 
      }  
    } 

    if ("->asBag".equals(operator))
    { return "UmlRsdsLib<" + celtype + ">::sort(" + qf + ")"; } 

    if ("->copy".equals(operator))
    { if (type == null) 
      { return qf; } 
      String tname = type.getName(); 
      if (type.isEntity())
      { return "Controller::inst->copy" + tname + "(" + qf + ")"; }
      if ("String".equals(tname))
      { return "(\"\"" + qf + ")"; } 
      if ("Set".equals(tname))
      { return "UmlRsdsLib<" + celtype + ">::copySet(" + qf + ")"; } 
      if ("Sequence".equals(tname))
      { return "UmlRsdsLib<" + celtype + ">::copySequence(" + qf + ")"; } 
      if ("Map".equals(tname))
      { return "UmlRsdsLib<" + celtype + ">::copyMap(" + qf + ")"; } 
      return qf; 
    }    

    if (operator.equals("->iterator"))
    { if (argument.isSequenceValued())
      { return "OclIterator::newOclIterator_Sequence(" + qf + ")"; }
      else 
      { return "OclIterator::newOclIterator_Set(" + qf + ")"; }
    }


    if (operator.equals("->isInteger")) 
    { return "UmlRsdsLib<string>::isInteger(" + qf + ")"; } 

    if (operator.equals("->isReal")) 
    { return "UmlRsdsLib<string>::isLong(" + qf + ")"; } 

    if (operator.equals("->isReal")) 
    { return "UmlRsdsLib<string>::isReal(" + qf + ")"; } 

    if (operator.equals("->toInteger")) 
    { return "UmlRsdsLib<int>::toInteger(" + qf + ")"; } 

    if (operator.equals("->toLong")) 
    { return "UmlRsdsLib<long>::toLong(" + qf + ")"; } 

    if (operator.equals("->toReal")) 
    { return "UmlRsdsLib<double>::toReal(" + qf + ")"; } 

    if (operator.equals("->toBoolean")) 
    { return "UmlRsdsLib<bool>::toBoolean(" + qf + ")"; } 

    if (operator.equals("->char2byte")) 
    { return "UmlRsdsLib<string>::char2byte(" + qf + ")"; } 

    if (operator.equals("->byte2char")) 
    { return "UmlRsdsLib<int>::byte2char(" + qf + ")"; } 

    if (operator.equals("->oclIsUndefined")) 
    { if (argument.isNumeric())
      { return "_isnan((double) " + qf + ")"; }
      return "(" + qf + " == NULL)"; 
    } 

    if (operator.equals("->oclIsNew")) 
    { 
      return "(" + qf + " != NULL)"; 
    } 

    if (operator.equals("->oclIsInvalid")) 
    { // return "std::isnan(" + qf + ")";
      return "_isnan((double) " + qf + ")"; 
    } 

    if (operator.equals("->oclType"))
    { return "OclType::getOclTypeByPK(string(typeid(" + qf + ").name()))"; 

   /* if (type != null) 
      { return "\"" + type.getName() + "\""; }
      else 
      { return "NULL"; } */  
      // An alternative is 
      // #include <typeinfo>
      // string(typeid(qf).name())
    }   

    if (operator.equals("->concatenateAll")) 
    { return "UmlRsdsLib<" + celtype + ">::concatenateAll(" + qf + ")"; } 
    
    if (operator.equals("->unionAll") && 
        argument.elementType != null &&
        argument.elementType.isMap())
    { Type aelemt = argument.elementType; 
      if (aelemt.elementType != null)
      { Type aeetype = aelemt.elementType; 
        celtype = aeetype.getCPP(aeetype.getElementType());
      }
      else 
      { celtype = "void*"; }   
      return "UmlRsdsLib<" + celtype + ">::unionAllMap(" + qf + ")"; 
    } 

    if (operator.equals("->unionAll"))
    { Type aelemt = argument.elementType; 
      if (aelemt != null && aelemt.elementType != null)
      { Type aeetype = aelemt.elementType; 
        celtype = aeetype.getCPP(aeetype.getElementType());
      }
      else 
      { celtype = "void*"; }   
      return "UmlRsdsLib<" + celtype + ">::unionAll(" + qf + ")"; 
    } 
    
    if (operator.equals("->intersectAll") && 
        argument.elementType != null &&
        argument.elementType.isMap())
    { Type aelemt = argument.elementType; 
      if (aelemt.elementType != null)
      { Type aeetype = aelemt.elementType; 
        celtype = aeetype.getCPP(aeetype.getElementType());
      }
      else 
      { celtype = "void*"; }   
      return "UmlRsdsLib<" + celtype + ">::intersectAllMap(" + qf + ")"; 
    } 

    if (operator.equals("->intersectAll"))
    { Type aelemt = argument.elementType; 
      if (aelemt != null && aelemt.elementType != null)
      { Type aeetype = aelemt.elementType; 
        celtype = aeetype.getCPP(aeetype.getElementType());
      }
      else 
      { celtype = "void*"; }   
      return "UmlRsdsLib<" + celtype + ">::intersectAll(" + qf + ")"; 
    } 

    if (operator.equals("->flatten")) 
    { Type aelemt = argument.elementType; 
      if (aelemt != null && aelemt.elementType != null)
      { Type aeetype = aelemt.elementType; 
        celtype = aeetype.getCPP(aeetype.getElementType());
      }
      else 
      { celtype = "void*"; }   
      
      if (Type.isSequenceType(argument.type))
      { return "UmlRsdsLib<" + celtype + ">::concatenateAll(" + qf + ")"; } 
      else if (Type.isSetType(argument.type))
      { return "UmlRsdsLib<" + celtype + ">::unionAll(" + qf + ")"; } 
      else if (Type.isMapType(argument.type))
      { return "UmlRsdsLib<" + celtype + ">::unionAllMap(" + qf + ")"; }
      return qf; 
    } // but only goes one level down. 

    if (extensionoperators.containsKey(operator))
    { String op = operator;
      String opcpp = Expression.getOperatorCPP(op); 
      if (opcpp != null && opcpp.length() > 0)
      { return opcpp.replaceAll("_1",qf); } 
      if (operator.startsWith("->"))
      { op = operator.substring(2,operator.length()); } 
      return qf + "." + op + "()";
    } 


    String pre = qf;
    String data = operator.substring(2,operator.length()); 

    if (argument.umlkind == CLASSID && (argument instanceof BasicExpression))
    { pre = ((BasicExpression) argument).classExtentQueryFormCPP(env,local); } 

    if (operator.equals("->size"))
    { if (argument.type != null && argument.type.getName().equals("String"))
      { return pre + ".length()"; } 
      else 
      { return pre + "->size()"; }
    }

    if (operator.equals("->abs") || operator.equals("->log") ||
        operator.equals("->exp") || operator.equals("->sin") ||
        operator.equals("->cos") || operator.equals("->tan") ||
        operator.equals("->exp") || operator.equals("->log") ||
        operator.equals("->log10") || operator.equals("->atan") || 
        operator.equals("->acos") || operator.equals("->asin") ||
        operator.equals("->cbrt") || operator.equals("->cosh") || 
        operator.equals("->sinh") ||
        operator.equals("->tanh") || operator.equals("->sqrt"))    
      // But exp, floor can be applied to sets/sequences
    { return data + "(" + pre + ")"; } 
    else if (operator.equals("->floor") || operator.equals("->ceil"))
    { return "((int) " + data + "(" + pre + "))"; } 
    else if (operator.equals("->round"))
    { return "UmlRsdsLib<int>::oclRound(" + pre + ")"; } 
    else if (operator.equals("->sqr"))
    { return "(" + pre + ")*(" + pre + ")"; } 
    else if (data.equals("toUpperCase") || data.equals("toUpper")) 
    { return "UmlRsdsLib<string>::toUpperCase(" + pre + ")"; } 
    else if (data.equals("toLowerCase") || data.equals("toLower"))
    { return "UmlRsdsLib<string>::toLowerCase(" + pre + ")"; } 
    else if (data.equals("sum"))
    { if (celtype.equals("string"))
      { return "UmlRsdsLib<" + celtype + ">::sumString(" + pre + ")"; }  
      return "UmlRsdsLib<" + celtype + ">::sum(" + pre + ")";
    } 
    else if (data.equals("prd"))
    { return "UmlRsdsLib<" + celtype + ">::prd(" + pre + ")"; } 
    else if (data.equals("isEmpty"))
    { return "(" + pre + "->size() == 0)"; } 
    else if (data.equals("notEmpty"))
    { return "(" + pre + "->size() != 0)"; } 
    else if (data.equals("reverse") || data.equals("sort") ||
             data.equals("asSet") || data.equals("asBag") || 
             data.equals("asOrderedSet") ||
             data.equals("asSequence")) 
    { return "UmlRsdsLib<" + celtype + ">::" + data + "(" + pre + ")"; } 
    else if (data.equals("keys"))
    { return "UmlRsdsLib<string>::keys(" + pre + ")"; }
    else if (data.equals("values"))
    { return "UmlRsdsLib<" + celtype + ">::values(" + pre + ")"; }
    else if (data.equals("closure") && (argument instanceof BasicExpression))
    { String rel = ((BasicExpression) argument).data;
      Expression arg = ((BasicExpression) argument).objectRef;  
      if (entity == null) 
      { JOptionPane.showMessageDialog(null, "No entity for: " + this, "Semantic error", JOptionPane.ERROR_MESSAGE);
        return ""; 
      }  
      Type et = argument.elementType; 
      String e1name = et + ""; // Assume an entity type
      if (arg == null) 
      { String collexp = "new set<" + e1name + "*>()"; 
        if (argument.isOrdered())
        { collexp = "new vector<" + e1name + "*>()"; } 
        return "UmlRsdsLib<" + e1name + "*>::closure" + entity.getName() + rel + "(" + collexp + ")"; 
      } 
      return "UmlRsdsLib<" + e1name + "*>::closure" + entity.getName() + rel + "(" + arg.queryFormCPP(env,local) + ")";
    } 
    else if (argument.type != null && "String".equals("" + argument.getType()))
    { // last,first,front,tail on strings
      if (data.equals("first") || data.equals("any"))
      { return pre + ".substr(0,1)"; } 
      if (data.equals("last"))
      { return pre + ".substr(" + pre + ".length()-1, 1)"; } 
      if (data.equals("front"))
      { return pre + ".substr(0," + pre + ".length() - 1)"; } 
      if (data.equals("tail"))
      { return pre + ".substr(1," + pre + ".length() - 1)"; } 
    } 
    else if (data.equals("max") || data.equals("min"))
    { return "UmlRsdsLib<" + celtype + ">::" + data + "(" + pre + ")"; } 
    else if (data.equals("any") || data.equals("last") || data.equals("first"))
    { Type et = argument.elementType; 
      if (et != null) 
      { if ("String".equals("" + et))
        { return "UmlRsdsLib<string>::" + data + "(" + pre + ")"; } 
        else if (et.isEntity())
        { return "UmlRsdsLib<" + celtype + ">::" + data + "(" + pre + ")"; }
        else if (Type.isPrimitiveType(et))
        { String cppType = et.getCPP(); 
          return "UmlRsdsLib<" + cppType + ">::" + data + "(" + pre + ")"; 
        } 
      } 
      else 
      { String cppType = type.getCPP(elementType); 
        return "UmlRsdsLib<" + cppType + ">::" + data + "(" + pre + ")"; 
      }
    }  
    else  // sum, prd, front tail on sequences, subcollections 
          // -- separate versions for numbers & objects?
    { return "UmlRsdsLib<" + celtype + ">::" + data + "(" + pre + ")"; }

    Type et = argument.elementType; 
    if (et != null) 
    { if ("String".equals("" + et))
      { return "UmlRsdsLib<string>::" + data + "(" + pre + ")"; } 
      else if (et.isEntity())
      { return "UmlRsdsLib<" + celtype + ">::" + data + "(" + pre + ")"; }
      else 
      { return "UmlRsdsLib<void*>::" + data + "(" + pre + ")"; } 
    } 
    else 
    { return "UmlRsdsLib<void*>::" + data + "(" + pre + ")"; }
    // return qf + ".get(0)"; 
  } // keys, values
  
  public boolean containsSubexpression(Expression expr) 
  { if (operator.equals("lambda") && accumulator != null &&
        accumulator.containsSubexpression(expr))
    { return true; }

    if (argument.containsSubexpression(expr))
    { return true; }
 
    return (this + "").equals(expr + ""); 
  } 

  public Vector mutants()
  { Vector res = new Vector(); 
    res.add(this); 
    Vector argmutants = argument.mutants();
	
    String mutantop = null;  
    if (operator.equals("->first"))
    { mutantop = "->last"; }
    else if (operator.equals("->last"))
    { mutantop = "->first"; }
    else if (operator.equals("->front"))
    { mutantop = "->tail"; }
    else if (operator.equals("->tail"))
    { mutantop = "->front"; }
    else if (operator.equals("->isEmpty"))
    { mutantop = "->notEmpty"; }
    else if (operator.equals("->notEmpty"))
    { mutantop = "->isEmpty"; }
	
    for (int i = 0; i < argmutants.size(); i++) 
    { Expression mutant = (Expression) argmutants.get(i); 
      if (mutantop != null) 
      { UnaryExpression thisclone = (UnaryExpression) this.clone();
        thisclone.operator = mutantop; 
        thisclone.argument = mutant;  
        res.add(thisclone); 
      }
      else if ("not".equals(operator) || 
               "-".equals(operator) || 
               "->sort".equals(operator) || 
               "->reverse".equals(operator))
      { res.add(mutant); } 
    }
    return res; 
  } 

  public Vector singleMutants()
  { Vector res = new Vector(); 
    // res.add(this); 
    Vector argmutants = argument.singleMutants();
	
    String mutantop = null;  
    if (operator.equals("->first"))
    { mutantop = "->last"; }
    else if (operator.equals("->last"))
    { mutantop = "->first"; }
    else if (operator.equals("->reverse"))
    { mutantop = "->copy"; }
    else if (operator.equals("->sort"))
    { mutantop = "->reverse"; }
    else if (operator.equals("->front"))
    { mutantop = "->tail"; }
    else if (operator.equals("->tail"))
    { mutantop = "->front"; }
    else if (operator.equals("->isEmpty"))
    { mutantop = "->notEmpty"; }
    else if (operator.equals("->notEmpty"))
    { mutantop = "->isEmpty"; }
    else if (operator.equals("->unionAll"))
    { mutantop = "->intersectAll"; }
    else if (operator.equals("->intersectAll"))
    { mutantop = "->unionAll"; }
    else if (operator.equals("->sum") && argument.isNumericCollection())
    { mutantop = "->prd"; }
    else if (operator.equals("->prd"))
    { mutantop = "->sum"; }
    else if (operator.equals("->log10"))
    { mutantop = "->log"; }
    else if (operator.equals("->log"))
    { mutantop = "->exp"; }
    else if (operator.equals("->exp"))
    { mutantop = "->log"; }
    else if (operator.equals("->sin"))
    { mutantop = "->cos"; }
    else if (operator.equals("->cos"))
    { mutantop = "->sin"; }
    else if (operator.equals("->sinh"))
    { mutantop = "->cosh"; }
    else if (operator.equals("->cosh"))
    { mutantop = "->sinh"; }
    else if (operator.equals("->tan"))
    { mutantop = "->sin"; }
    else if (operator.equals("->tanh"))
    { mutantop = "->tan"; }

    for (int i = 0; i < argmutants.size(); i++) 
    { Expression mutant = (Expression) argmutants.get(i); 
      UnaryExpression thisclone = (UnaryExpression) this.clone();
      thisclone.operator = operator; 
      thisclone.argument = mutant;
      if (VectorUtil.containsEqualString(thisclone + "",res)) 
      { } 
      else 
      { res.add(thisclone); } 
    }

    if (mutantop != null) 
    { UnaryExpression thisclone = (UnaryExpression) this.clone();
      thisclone.operator = mutantop; 
      thisclone.argument = argument;  
      if (VectorUtil.containsEqualString(thisclone + "",res)) { } 
      else 
      { res.add(thisclone); } 
    }  
    else if ("not".equals(operator) || "-".equals(operator) ||
             "->reverse".equals(operator) || 
             "->sort".equals(operator))
    { if (VectorUtil.containsEqualString(argument + "",res)) 
      { } 
      else 
      { res.add(argument); }
    } 

    return res; 
  } 


  public BExpression bqueryForm(java.util.Map env)
  { BExpression pre = argument.bqueryForm(env); 
    BExpression psimp = pre.simplify(); 
    String op; 

    // lambda expressions 
	
    if (operator.equals("-"))
    { return new BUnaryExpression("-",psimp); } 

    if (operator.equals("not"))
    { return new BUnaryExpression("not",psimp); } 

    String data = operator.substring(2,operator.length()); 

    if (operator.equals("->size"))
    { return new BUnaryExpression("card",psimp); } 
    else if (operator.equals("->isEmpty"))
    { return new BBinaryExpression("=",
                   new BUnaryExpression("card",psimp),
                   new BBasicExpression("0")); 
    } 
    else if (operator.equals("->notEmpty"))
    { return new BBinaryExpression("/=",
                   new BUnaryExpression("card",psimp),
                   new BBasicExpression("0")); 
    } 
    else if (operator.equals("->flatten"))
    { if (Type.isSequenceType(argument.type))
      { return new BUnaryExpression("dcat", psimp); } 
      else if (Type.isSetType(argument.type))
      { return new BUnaryExpression("union", psimp); } 
      return psimp; 
    } // but only goes one level down. 
    else if (operator.equals("->asSet"))
    { if (argument.isOrderedB() || 
          Type.isSequenceType(argument.getType()))
      { return new BUnaryExpression("ran",psimp); }  
      return psimp; 
    } 
    else if (data.equals("sqr"))
    { return new BBinaryExpression("*", psimp, psimp); } 
    else if (data.equals("display"))
    { return new BBasicExpression("true"); } 
    else if (data.equals("isDeleted"))
    { Type et = argument.elementType; 
      String ename = et + ""; 
      if (et != null && et.isEntity())
      { String es = ename.toLowerCase() + "s"; 
        BExpression res = new BBinaryExpression("=", 
          new BBinaryExpression("/\\", psimp, new BBasicExpression(es)), 
                   new BSetExpression());
        res.setBrackets(true); 
        return res;  
      } 
      return new BBasicExpression("true"); 
    }    
    else if (data.equals("abs") || data.equals("floor") ||
             data.equals("round") || data.equals("ceil") ||
             data.equals("sin") || data.equals("cos") || data.equals("tan") ||
             data.equals("asin") || data.equals("acos") || data.equals("atan") ||
             data.equals("sinh") || data.equals("cosh") || data.equals("tanh") ||
             data.equals("exp") || data.equals("log") || data.equals("log10") ||
             data.equals("cbrt") || data.equals("sqrt") || data.equals("max") ||
             data.equals("min"))
    { op = data;
      return new BUnaryExpression(op,psimp);
    } // can be sets { xx | yy: pre & xx = f(yy) } 
    else if (data.equals("closure") && (argument instanceof BasicExpression) &&
             ((BasicExpression) argument).objectRef != null)
    { String rel = ((BasicExpression) argument).data; 
          // the relation being closured
      BExpression objs = 
          ((BasicExpression) argument).objectRef.bqueryForm(env);
      BExpression clsre = new BUnaryExpression("closure",new BBasicExpression(rel));
      if (((BasicExpression) argument).objectRef.isMultiple()) { } 
      else 
      { BSetExpression objss = new BSetExpression();
        objss.addElement(objs);
        return new BApplySetExpression(clsre,objss); 
      }     
      return new BApplySetExpression(clsre,objs); 
    } 
    else if (data.equals("closure") && (argument instanceof BasicExpression) &&
             ((BasicExpression) argument).objectRef == null)
    { String rel = ((BasicExpression) argument).data; 
          // the relation being closured
      BExpression objs = new BSetExpression(); 
      BExpression clsre = new BUnaryExpression("closure",new BBasicExpression(rel));
      return new BApplySetExpression(clsre,objs); 
    } 
    else if (data.equals("any"))
    { return new BApplyExpression(psimp,new BBasicExpression("1")); }  // Not for sets
    else if (data.equals("subcollections")) // not for sequences
    { if (isOrderedB())
      { return subcollectionsBinvariantForm(psimp); } 
      return new BUnaryExpression("FIN",psimp); 
    } 
    else if (data.equals("asSet"))
    { if (argument.isOrderedB())
      { return new BUnaryExpression("ran",psimp); }  // only if isOrderedB()
      return psimp; 
    } 
    else if (data.equals("sum")) // Ignore toLower, toUpper
    { String xx = Identifier.nextIdentifier("xx");
      BExpression xxbe = new BBasicExpression(xx);
      BExpression pred =
         new BBinaryExpression(":",xxbe,pre);
      return new BSigmaExpression(xx,pred,xxbe);
    }
    else if (data.equals("prd")) // product
    { String xx = Identifier.nextIdentifier("xx"); 
      BExpression xxbe = new BBasicExpression(xx);
      BExpression pred =
        new BBinaryExpression(":",xxbe,pre);
      return new BPiExpression(xx,pred,xxbe);
    }
    else if (data.equals("reverse"))
    { return new BUnaryExpression("rev",pre); } 
    else // last, first, front, tail, sort, reverse
    { return new BUnaryExpression(data,pre); } 
  }   // no equivalent for ->any, ->isDeleted 
// keys, values 
	
private BExpression subcollectionsBinvariantForm(BExpression bsimp)
{ String i = Identifier.nextIdentifier("i");
  String j = Identifier.nextIdentifier("j");
  String f = Identifier.nextIdentifier("f");
  BExpression fbe = new BBasicExpression(f);
  BExpression ibe = new BBasicExpression(i);
  BExpression jbe = new BBasicExpression(j);
  String ssq = Identifier.nextIdentifier("sqxx");
  BExpression ssqe = new BBasicExpression(ssq);
  String n = Identifier.nextIdentifier("n");
  BExpression nbe = new BBasicExpression(n);
  BExpression eqcard = new BBinaryExpression("=", new BUnaryExpression("card", ssqe), nbe);
  BExpression ileqj = new BBinaryExpression("<=", ibe, jbe);
  BExpression fileqfj = new BBinaryExpression("<=", new BApplyExpression(f,ibe), new BApplyExpression(f,jbe));
  BExpression leq = new BBinaryExpression("=>", ileqj, fileqfj);
  Vector vars = new Vector();
  vars.add(i); vars.add(j);
  BExpression ord = new BQuantifierExpression("forall", vars, leq);
  BExpression ransq = new BUnaryExpression("ran", bsimp);
  BExpression ssqrange = new BBinaryExpression(":", ssqe, new BUnaryExpression("seq", ransq));
  String ii = Identifier.nextIdentifier("i");
  BExpression iibe = new BBasicExpression(ii);
  BExpression align = new BBinaryExpression("=>", 
   new BBinaryExpression(":", iibe, 
      new BUnaryExpression("dom", ssqe)),
   new BBinaryExpression("=", new BApplyExpression(ssqe, iibe),
      new BApplyExpression(bsimp, new BApplyExpression(fbe, iibe))));

  BExpression rpred2 = new BQuantifierExpression("forall", ii, align);
  BExpression rpred = new BBinaryExpression("&", eqcard, rpred2);
 //  BExpression rpred = new BBinaryExpression("&", ssqrange, rpred1);
  BExpression pred = new BBinaryExpression("&", ord, rpred); 
  BExpression domsq = new BUnaryExpression("dom", bsimp);
  BExpression frange = new BBinaryExpression(":", fbe, new BUnaryExpression("seq", domsq));
  BExpression qf = new BQuantifierExpression("exists", f, 
                                              new BBinaryExpression("&", frange, pred));
 
  BExpression nrange = new BBinaryExpression(":", nbe, domsq); 
  BExpression qn = new BQuantifierExpression("exists",n,
  new BBinaryExpression("&", nrange, qf));
  BExpression res = new BSetComprehension(ssq, ssqrange, qn);
  return res;
}


  public BExpression bqueryForm()
  { BExpression barg = argument.bqueryForm(); 
    BExpression bsimp = barg.simplify(); 

    // lambda expressions, ->keys, ->values 
	
    if (operator.equals("-"))
    { return new BUnaryExpression("-",bsimp); }

    if (operator.equals("not"))
    { return new BUnaryExpression("not",bsimp); } 

    String data = operator.substring(2,operator.length()); 
 
    if (operator.equals("->size"))
    { return new BUnaryExpression("card",barg); } 
    else if (operator.equals("->isEmpty"))
    { return new BBinaryExpression("=",
                   new BUnaryExpression("card",barg),
                   new BBasicExpression("0")); 
    } 
    else if (operator.equals("->notEmpty"))
    { return new BBinaryExpression("/=",
                   new BUnaryExpression("card",barg),
                   new BBasicExpression("0")); 
    } 
    else if (operator.equals("->flatten"))
    { if (Type.isSequenceType(argument.type))
      { return new BUnaryExpression("dcat", bsimp); } 
      else if (Type.isSetType(argument.type))
      { return new BUnaryExpression("union", bsimp); } 
      return bsimp; 
    } // but only goes one level down. 
    else if (data.equals("sqr"))
    { return new BBinaryExpression("*", bsimp, bsimp); } 
    else if (data.equals("display"))
    { return new BBasicExpression("true"); } 
    else if (data.equals("isDeleted"))
    { Type et = argument.elementType; 
      String ename = et + ""; 
      if (et != null && et.isEntity())
      { String es = ename.toLowerCase() + "s"; 
        BExpression res = new BBinaryExpression("=", 
                 new BBinaryExpression("/\\", bsimp, new BBasicExpression(es)), 
                   new BSetExpression());
        res.setBrackets(true); 
        return res;  
      } 
      return new BBasicExpression("true"); 
    }    
    else if (data.equals("abs") || data.equals("floor") ||
             data.equals("round") || data.equals("ceil") ||
             data.equals("sin") || data.equals("cos") || data.equals("tan") ||
             data.equals("asin") || data.equals("acos") || data.equals("atan") ||
             data.equals("sinh") || data.equals("cosh") || data.equals("tanh") ||
             data.equals("exp") || data.equals("log") || data.equals("log10") ||
             data.equals("cbrt") || data.equals("sqrt") || data.equals("max") ||
             data.equals("min"))
    { return new BUnaryExpression(data, bsimp);
    } // can be sets { xx | yy: pre & xx = f(yy) } 
    else if (data.equals("closure") && (argument instanceof BasicExpression) &&
             ((BasicExpression) argument).objectRef != null)
    { String rel = ((BasicExpression) argument).data; 
          // the relation being closured
      BExpression objs = 
          ((BasicExpression) argument).objectRef.bqueryForm();
      BExpression clsre = new BUnaryExpression("closure",new BBasicExpression(rel));
      if (((BasicExpression) argument).objectRef.isMultiple()) { } 
      else 
      { BSetExpression objss = new BSetExpression();
        objss.addElement(objs);
        return new BApplySetExpression(clsre,objss); 
      }     
      return new BApplySetExpression(clsre,objs); 
    } 
    else if (data.equals("any"))
    { return new BApplyExpression(bsimp,new BBasicExpression("1")); }
    else if (data.equals("subcollections")) // not for sequences
    { if (isOrderedB())
      { return subcollectionsBinvariantForm(bsimp); } 
      return new BUnaryExpression("FIN",bsimp); 
    } 
    else if (data.equals("asSet"))
    { if (argument.isOrderedB())
      { return new BUnaryExpression("ran",bsimp); }  // only if isOrdered()
      return bsimp;
    } 
    else if (data.equals("sum")) // Ignore toLower, toUpper
    { String xx = Identifier.nextIdentifier("xx");
      BExpression xxbe = new BBasicExpression(xx); 
      BExpression pred =
         new BBinaryExpression(":",xxbe,barg);
      return new BSigmaExpression(xx,pred,xxbe);
    }
    else if (data.equals("prd")) // product
    { String xx = Identifier.nextIdentifier("xx"); 
      BExpression xxbe = new BBasicExpression(xx);
      BExpression pred =
        new BBinaryExpression(":",xxbe,barg);
      return new BPiExpression(xx,pred,xxbe);
    }
    else if (data.equals("reverse"))
    { return new BUnaryExpression("rev",barg); } 
    else // last, first, front, tail, sort, reverse
    { return new BUnaryExpression(data,barg); } 
    // else 
    // { return new BBasicExpression(true); }  
  }   // no equivalent for ->any 

  public BStatement bupdateForm(java.util.Map env, boolean local)
  { if (operator.equals("->isDeleted"))
    { if (argument.umlkind == CLASSID && (argument instanceof BasicExpression) && 
          ((BasicExpression) argument).arrayIndex == null) 
      { String data = argument + ""; 
        String datas = data.toLowerCase() + "s";
        BExpression bdatas = new BBasicExpression(datas); 
        BExpression empty = new BSetExpression();  
        return new BAssignStatement(bdatas,empty);  
      } 
      BExpression pre = argument.binvariantForm(env,local);
      String eename = argument.type.getName(); 
      Entity ent = null; 
      String all = ""; 
      BExpression subtractexp; 

      if (argument.isMultiple()) 
      { 
        eename = argument.elementType.getName();
        ent = argument.elementType.entity;  
        subtractexp = pre;        
      }
      else 
      { ent = argument.type.entity; 
        subtractexp = new BSetExpression(); 
        ((BSetExpression) subtractexp).addElement(pre); 
      } 

      /* String ename = (ent + "s").toLowerCase(); 
      BExpression bes = new BBasicExpression(ename); 
      BExpression remexp = new BBinaryExpression("-",bes,subtractexp);
      BStatement assgn = new BAssignStatement(bes,remexp); 
      assgn.setWriteFrame(ename); 

      if (ent.getSuperclass() != null) 
      { BParallelStatement superremove =
            ent.getSuperclass().bDeleteCode(subtractexp); 
        superremove.addStatement(assgn); 
        return superremove; 
      } 
      return assgn; */ 

      return ent.bDeleteCode(subtractexp); 
    } 
    return new BBasicStatement("skip"); 
  } 

  public BStatement bOpupdateForm(java.util.Map env, boolean local)
  { return bupdateForm(env,local); } 


  public BExpression bInvariantForm(java.util.Map env,boolean local)
  { Vector uses = getVariableUses();
    Vector params = new Vector(); 
    Vector variables = Constraint.variableTypeAssign(uses,params); 
    BExpression pred = binvariantForm(env,local);   // add quantifiers for vbls
    return Constraint.addVariableQuantifiers(variables,pred);
  }

  public BExpression binvariantForm(java.util.Map env,boolean local)
  { BExpression pre = argument.binvariantForm(env,local);
    BExpression psimp = pre.simplify(); 

    // lambda expressions, ->keys, ->values 
	
    String op;
    String data; 
    if (operator.startsWith("->"))
    { data = operator.substring(2,operator.length()); }
    else 
    { data = operator; } 

    if (operator.equals("-"))
    { return new BUnaryExpression("-",psimp); } 

    if (operator.equals("not"))
    { return new BUnaryExpression("not",psimp); } 

    if (operator.equals("->asSet"))
    { if (argument.isOrderedB() || 
          Type.isSequenceType(argument.getType()))
      { return new BUnaryExpression("ran",psimp); }  
      return psimp; 
    } 
    
    if (data.equals("size")) // also strings
    { op = "card";
      return new BUnaryExpression(op,pre);
    }
    else if (operator.equals("->flatten"))
    { if (Type.isSequenceType(argument.type))
      { return new BUnaryExpression("dcat", psimp); } 
      else if (Type.isSetType(argument.type))
      { return new BUnaryExpression("union", psimp); } 
      return psimp; 
    } // but only goes one level down. 
    else if (data.equals("sqr"))
    { return new BBinaryExpression("*", psimp, psimp); } 
    else if (data.equals("display"))
    { return new BBasicExpression("true"); } 
    else if (data.equals("isDeleted"))
    { Type et = argument.elementType; 
      String ename = et + ""; 
      if (et != null && et.isEntity())
      { String es = ename.toLowerCase() + "s"; 
        BExpression res = new BBinaryExpression("=", 
                 new BBinaryExpression("/\\", psimp, new BBasicExpression(es)), 
                   new BSetExpression());
        res.setBrackets(true); 
        return res;  
      } 
      return new BBasicExpression("true"); 
    }    
    else if (data.equals("abs") || data.equals("floor") ||
             data.equals("round") || data.equals("ceil") ||
             data.equals("sin") || data.equals("cos") || data.equals("tan") ||
             data.equals("asin") || data.equals("acos") || data.equals("atan") ||
             data.equals("sinh") || data.equals("cosh") || data.equals("tanh") ||
             data.equals("exp") || data.equals("log") || data.equals("log10") ||
             data.equals("cbrt") || data.equals("sqrt") || data.equals("max") ||
             data.equals("min"))
    { return new BUnaryExpression(data, psimp);
    } // can be sets { xx | yy: pre & xx = f(yy) } 
    else if (data.equals("closure") && (argument instanceof BasicExpression) &&
             ((BasicExpression) argument).objectRef != null)
    { String rel = ((BasicExpression) argument).data; 
          // the relation being closured
      BExpression objs = 
          ((BasicExpression) argument).objectRef.binvariantForm(env, local);
      BExpression clsre = new BUnaryExpression("closure",new BBasicExpression(rel));
      if (((BasicExpression) argument).objectRef.isMultiple()) { } 
      else 
      { BSetExpression objss = new BSetExpression();
        objss.addElement(objs);
        return new BApplySetExpression(clsre,objss); 
      }     
      return new BApplySetExpression(clsre,objs); 
    } 
    else if (data.equals("closure") && (argument instanceof BasicExpression) &&
             ((BasicExpression) argument).objectRef == null)
    { String rel = ((BasicExpression) argument).data; 
          // the relation being closured
      BExpression objs = new BSetExpression(); 
      BExpression clsre = new BUnaryExpression("closure",new BBasicExpression(rel));
      return new BApplySetExpression(clsre,objs); 
    } 
    else if (data.equals("any"))
    { return new BApplyExpression(pre,new BBasicExpression("1")); } 
    else if (data.equals("subcollections")) // not for sequences
    { if (isOrderedB())
      { return subcollectionsBinvariantForm(pre); } 
      return new BUnaryExpression("FIN",pre); 
    } 
    else if (operator.equals("->notEmpty"))
    { return new BBinaryExpression("/=",
                   new BUnaryExpression("card",pre),
                   new BBasicExpression("0")); 
    }
    else if (operator.equals("->isEmpty"))
    { return new BBinaryExpression("=",
                   new BUnaryExpression("card",pre),
                   new BBasicExpression("0")); 
    } 
    else if (data.equals("asSet"))
    { if (argument.isOrderedB())
      { return new BUnaryExpression("ran",psimp); }  // only if isOrdered()
      return psimp;
    } 
    else if (data.equals("sum")) // sum
    { String xx = Identifier.nextIdentifier("xx"); 
      BExpression xxbe = new BBasicExpression(xx);
      BExpression pred =
        new BBinaryExpression(":",xxbe,pre);
      return new BSigmaExpression(xx,pred,xxbe);
    }
    else if (data.equals("prd")) // product
    { String xx = Identifier.nextIdentifier("xx"); 
      BExpression xxbe = new BBasicExpression(xx);
      BExpression pred =
        new BBinaryExpression(":",xxbe,pre);
      return new BPiExpression(xx,pred,xxbe);
    }
    else if (data.equals("reverse"))
    { return new BUnaryExpression("rev",pre); } 
    else // last, first, front, tail - 1st 2 not quite correct for strings
    { return new BUnaryExpression(data,pre); } 
  } 

  public void setKeyType(String ktype)
  { if ("lambda".equals(operator) && ktype != null) 
    { Type keytype = new Type(ktype,null); 
      if (accumulator != null)
      { accumulator.setType(keytype); }
      if (type != null)
      { type.setKeyType(keytype); } 
    }   
  } 

  public void setElementType(String ktype)
  { if ("lambda".equals(operator) && ktype != null && 
        type != null)
    { type.setElementType(new Type(ktype,null)); } 
  } 

  public String toString()  // RSDS version of expression
  { if (operator == null)
    { return argument + " /* Unknown unary operator */"; } 

    if (operator.equals("lambda") && accumulator != null)
    { String res = "lambda " + accumulator.getName() + " : " + accumulator.getType() + " in " + argument;
      return "(" + res + ")";
    } 
  
    if (operator.equals("-"))
    { String res = "-(" + argument + ")"; 
      if (needsBracket)
      { return "(" + res + ")"; }
      return res;
    }
  
    if (operator.equals("_"))
    { return "_" + argument; }
  
    if (operator.startsWith("->"))
    { String res = argument + operator + "()";
      if (needsBracket)
      { return "(" + res + ")"; }
      return res;
    } 

    if (operator.equals("?") || operator.equals("!"))
    { String res = operator + argument; 
      if (needsBracket)
      { return "(" + res + ")"; }
      return res; 
    } 

    return operator + "(" + argument + ")";  
  } 

  public String toAST()
  { String res = "(OclUnaryExpression "; 
    
    if (operator.equals("lambda"))
    { res = res + "lambda " + accumulator.getName() + " : " + accumulator.getType().toAST() + " in " + argument.toAST() + ")";
      // if (needsBracket)
      // { return "(BracketedExpression ( " + res + " ) )"; }
      return res; 
    } 
  
    if (operator.equals("-"))
    { res = res + "- " + argument.toAST() + ")"; 
      // if (needsBracket)
      // { return "(BracketedExpression ( " + res + " ) )"; }
      return res;
    }

    if (operator.equals("not"))
    { res = res + "not " + argument.toAST() + ")"; 
      // if (needsBracket)
      // { return "(BracketedExpression ( " + res + " ) )"; }
      return res;
    }
  
    if (operator.equals("_"))
    { return "_" + argument; }
  
    if (operator.startsWith("->"))
    { return res + argument.toAST() + " " + operator + " ( ) )"; } 

    return res + operator + " " + argument.toAST() + " )";  
  } 

  // saveTextModel
  public String saveModelData(PrintWriter out) 
  { String id = Identifier.nextIdentifier("unaryexpression_");
    out.println(id + " : UnaryExpression"); 
    out.println(id + ".expId = \"" + id + "\""); 
    out.println(id + ".operator = \"" + operator + "\""); 
    String argid = argument.saveModelData(out); 
    
    out.println(id + ".argument = " + argid); 
  
    String tname = "Integer"; 
    if (type != null) 
    { tname = type.getUMLModelName(out); 
      out.println(id + ".type = " + tname); 
    } 

    if (elementType != null) 
    { String etname = elementType.getUMLModelName(out); 
      out.println(id + ".elementType = " + etname); 
    } 
    else if (type != null && 
             type.getElementType() != null)
    { String etname = 
        type.getElementType().getUMLModelName(out); 
      out.println(id + ".elementType = " + etname);
    } 
    else if (type != null && 
             Type.isBasicType(type))
    { out.println(id + ".elementType = " + tname); } 
    else 
    { out.println(id + ".elementType = " + tname); } 

    out.println(id + ".needsBracket = " + needsBracket); 
    out.println(id + ".umlKind = " + umlkind);
 
    if (accumulator != null) 
    { out.println(id + ".variable = \"" + accumulator.getName() + "\""); 
      Type vtype = accumulator.getType(); 
      if (vtype != null) 
      { String vtypeid = vtype.getUMLModelName(out); 
        out.println(id + ".variableType = " + vtypeid);
      }  
    } 
	 // out.println(res + ".prestate = " + prestate); 
    return id; 
  }         

  public String toSQL() 
  { String pre = argument.toSQL();
    String op;
    String data; 
    if (operator.startsWith("->"))
    { data = operator.substring(2,operator.length()); }
    else 
    { data = operator; } 

    if (operator.equals("-"))
    { return "-" + pre; } 
    if (data.equals("size")) // also strings
    { return "CARD(" + pre + ")"; }
    if (data.equals("abs") || data.equals("sqr") ||
        data.equals("sqrt") || data.equals("max") || data.equals("floor") ||
        data.equals("min") || data.equals("exp") || data.equals("log") ||
        data.equals("sin") || data.equals("cos") || data.equals("tan"))
    { op = data.toUpperCase();
      return op + "(" + pre + ")"; 
    } 
    return toString(); 
  } 

  public String toJava() { return ""; }    // Java version of expression

  public String toB() { return ""; } 

  public String toZ3()
  { String op = operator; 
    if (operator.startsWith("->"))
    { op = operator.substring(2,operator.length()); } 

    if (op.equals("first"))
    { op = "head"; } 

    return "(" + op + " " + argument.toZ3() + ")"; 
  }   

  public String toOcl(java.util.Map env, boolean local) 
  { return toString(); } 

  public Expression toSmv(Vector cnames)
  { return null; }    // SMV version 

  public String toImp(final Vector comps) 
  { return ""; }   // B IMPLEMENTATION version

  public String toJavaImp(final Vector comps) 
  { return ""; }  

  public Expression buildJavaForm(final Vector comps)
  { return null; } 


  public Expression substitute(final Expression oldE,
                               final Expression newE)
  { Expression newarg = argument.substitute(oldE,newE); 
    UnaryExpression res = new UnaryExpression(operator,newarg);
    res.needsBracket = needsBracket; 
    res.umlkind = umlkind; 
    return res; 
  } 

  public Expression substituteEq(final String var,
                                 final Expression val)
  { if (var.equals(this + ""))
    { return val; } 

    if (operator.equals("lambda") && 
        accumulator != null && 
        var.equals(accumulator.getName()))
    { return this; }

    /* (lambda v : T in e)[val/v] is (lambda v : T in e) */ 
    /* Also v should not occur in val. */ 
 
    Expression newarg = argument.substituteEq(var,val);
    UnaryExpression res = new UnaryExpression(operator,newarg);
    res.needsBracket = needsBracket; 
    res.umlkind = umlkind; 
    res.accumulator = accumulator; 
    // res.type = type; 
    // res.elementType = elementType;  // type of elements if a set
    // res.entity = entity; 
    // res.multiplicity = multiplicity;
    return res; 
  } 

  public Expression removeSlicedParameters(
             BehaviouralFeature op, Vector fpars)
  {  
    Expression newarg = argument.removeSlicedParameters(op,fpars);
    UnaryExpression res = new UnaryExpression(operator,newarg);
    res.needsBracket = needsBracket; 
    res.umlkind = umlkind; 
    res.accumulator = accumulator; 
    res.type = type; 
    res.elementType = elementType;  // type of elements if a set
    res.entity = entity; 
    res.multiplicity = multiplicity;
    return res; 
  } 

  
  public boolean hasVariable(final String s)
  { return argument.hasVariable(s); } 

  // public boolean hasComponent(final Statemachine sm)

  public Vector variablesUsedIn(final Vector varNames)
  { return argument.variablesUsedIn(varNames); } 

  public Vector componentsUsedIn(final Vector sms)
  { return argument.componentsUsedIn(sms); }  

  Maplet findSubexp(final String var)
  { return argument.findSubexp(var); } 

  public Expression simplify(final Vector vars)
  { return this; } 

  public Expression simplify()
  { Expression argsimp = argument.simplify(); 

    if ("->size".equals(operator))
    { return simplifySize(argsimp); } 

    UnaryExpression clne = (UnaryExpression) clone(); 
    clne.argument = argsimp; 
    return clne; 
  } // also let x : T = e in expr when x not occuring in expr


  public Expression filter(final Vector vars)
  { return this; } 

  public void display() 
  { System.out.println(toString()); } 

  public Vector splitAnd(final Vector comps)
  { return new Vector(); } 

  public Vector splitOr(final Vector comps)
  { return new Vector(); }  

  public Expression expandMultiples(final Vector sms)
  { return this; } 

  public Expression removeExpression(final Expression e)
  { return this; } 

 
 public boolean implies(final Expression e)
 { if (equals(e)) { return true; }
   if (e.equalsString("true")) { return true; } 
   if (e instanceof BinaryExpression)
   { BinaryExpression be = (BinaryExpression) e;
     if (be.operator.equals("&"))
     { boolean res1 = implies(be.left);
       boolean res2 = implies(be.right);
       return (res1 && res2); 
     } 
     else if (be.operator.equals("or"))
     { boolean res1 = implies(be.left);
       boolean res2 = implies(be.right);
       return (res1 || res2); 
     } 
     else if (be.operator.equals("=>"))
     { return implies(be.right); } 
     else 
     { return false; } 
   }
   return false;
 } 

 public boolean consistentWith(final Expression e)
 { if (equals(e))  { return true; }
   if (e instanceof BinaryExpression)
   { BinaryExpression be = (BinaryExpression) e;
     if (be.operator.equals("&"))
     { return (consistentWith(be.left) &&
               consistentWith(be.right)); 
     }
     if (be.operator.equals("or"))
     { return (consistentWith(be.left) ||
               consistentWith(be.right)); 
     }
   } 
   return true; 
 } 

 public boolean selfConsistent(final Vector vars)
 { return true; } 

 public boolean selfConsistent()
 { return true; }   // default 

  public boolean subformulaOf(final Expression e)
  { return (e == this); } 

  public boolean conflictsWith(final Expression e)
  { if (e instanceof UnaryExpression)
    { UnaryExpression ue = (UnaryExpression) e; 
      if (operator.equals("->isEmpty") && ue.operator.equals("->notEmpty") && 
          (argument + "").equals(ue.argument + ""))
      { return true; } 
      if (operator.equals("->notEmpty") && ue.operator.equals("->isEmpty") && 
          (argument + "").equals(ue.argument + ""))
      { return true; } 
    } 
    else if (operator.equals("not") && (argument + "").equals(e + ""))
    { return true; } 

    return false; 
  }  

  public boolean conflictsWith(String op, Expression left,
                               Expression right)
  { if ("->isDeleted".equals(operator) && op.equals(":"))
    { if ((argument + "").equals(left + ""))   // l : e & l->isDeleted()
      { return true; } 
      if ((argument + "").equals(right + ""))  // l : e & e->isDeleted()
      { return true; } 
    } 

    if ("->isDeleted".equals(operator) && op.equals("->includes"))
    { if ((argument + "").equals(right + ""))   // e->includes(l) & l->isDeleted()
      { return true; } 
      if ((argument + "").equals(left + ""))  // e->includes(r) & e->isDeleted()
      { return true; } 
    } 

    if ("->isEmpty".equals(operator) && op.equals(":"))
    { if ((argument + "").equals(right + ""))  // l : e & e->isEmpty()
      { return true; } 
    } 

    if ("->isEmpty".equals(operator) && op.equals("->includes"))
    { if ((argument + "").equals(left + ""))  // e->includes(l) & e->isEmpty()
      { return true; } 
    } 

    return false; 
  } 


  public Expression computeNegation4succ(final Vector actuators)
  { if ("isDeleted".equals(operator))
    { Type et = argument.elementType; 
      if (et == null) 
      { return new BasicExpression("true"); }
      else 
      { return new BinaryExpression(":",argument,new BasicExpression(et + "")); } 
    }  
    else if (operator.equals("->isEmpty"))
    { return new UnaryExpression("->notEmpty", argument); } 
    else if (operator.equals("->notEmpty"))
    { return new UnaryExpression("->isEmpty", argument); } 
    else if (operator.equals("not"))
    { return argument; } 
    return new UnaryExpression("not", this); 
  }  

  public Vector computeNegation4ante(final Vector actuators)
  { Vector res = new Vector(); 
    res.add(computeNegation4succ(actuators)); 
    return res; 
  } 

  public Expression computeNegation4succ()
  { if ("isDeleted".equals(operator))
    { Type et = argument.elementType; 
      if (et == null) 
      { return new BasicExpression("true"); }
      else 
      { return new BinaryExpression(":",argument,new BasicExpression(et + "")); } 
    }  
    else if (operator.equals("->isEmpty"))
    { return new UnaryExpression("->notEmpty", argument); } 
    else if (operator.equals("->notEmpty"))
    { return new UnaryExpression("->isEmpty", argument); } 
    else if (operator.equals("not"))
    { return argument; } 
    return new UnaryExpression("not", this); 
  }  

  public Vector computeNegation4ante()
  { Vector res = new Vector(); 
    res.add(computeNegation4succ()); 
    return res;
  } 

  public UpdateFeatureInfo updateFeature(String f)
  { return null; } // default

  public Expression updateExpression() { return null; } // default

  public Expression updateReference() { return null; }  // default

  public Expression matchCondition(String op, String f,
                     Expression exbe)
  { // default:
    return new BasicExpression("false"); 
  }

  public Expression featureSetting(String var, String feat, Vector r)
  { return null; } 

  public void changedEntityName(String oldN, String newN)
  { argument.changedEntityName(oldN, newN); } 

  public static void main(String[] args)
  { System.out.println("->sub".substring(2,"->sub".length())); } 

  public String cg(CGSpec cgs)
  { String etext = this + "";
    Vector args = new Vector();
    Vector eargs = new Vector(); 
    if (operator.equals("lambda") && accumulator != null)
    { args.add(accumulator.getName()); 
      eargs.add(accumulator); 
      args.add(accumulator.getType().cg(cgs)); 
      eargs.add(accumulator.getType()); 
    }
    args.add(argument.cg(cgs));
    eargs.add(argument);
 
    CGRule r = cgs.matchedUnaryExpressionRule(this,etext);
    if (r != null)
    { String res = r.applyRule(args,eargs,cgs);
      System.out.println(">>> matched unary expression rule " + r + " for " + this + " args: " + args + " eargs: " + eargs); 
      System.out.println(">>> " + r.variables); 
      System.out.println(); 
	  
      if (needsBracket) 
      { return "(" + res + ")"; } 
      else 
      { return res; }
    }
    return etext;
  }


} 

