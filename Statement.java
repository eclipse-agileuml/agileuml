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
/* package: Activity */ 

abstract class Statement implements Cloneable
{ private int indent = 0; 
  protected Entity entity = null;  
      // owner of the statement/its method

  protected boolean brackets = false; 
  protected boolean unusedStatement = false; 

  // Enumeration of loop kinds: 
  public static final int WHILE = 0; 
  public static final int FOR = 1; 
  public static final int REPEAT = 2; 

  // Enumeration of execution status: 
  public static final int NORMAL = 0; 
  public static final int CONTINUE = 1; 
  public static final int BREAK = 2;
  public static final int RETURN = 3; 
  public static final int EXCEPTION = 4; 

  public static final String[] spaces = { "", "  ", "    ", "      ", "        ", "          ", "            ", "              ", "                ", "                  ", "                    " }; 
  // spaces[i] is i*2 spaces

  public void setEntity(Entity e)
  { entity = e; } 

  public void setBrackets(boolean b)
  { brackets = b; } 

  public boolean hasBrackets()
  { return brackets; } 

  abstract protected Object clone(); 

  abstract void display(); 

  abstract String getOperator(); 

  /* public abstract String getOclType(); */ 

  public static boolean isOclBasicStatement(Statement st)
  { if (st instanceof ContinueStatement) 
    { return true; } 
    if (st instanceof BreakStatement) 
    { return true; } 
    if (st instanceof ReturnStatement) 
    { ReturnStatement rt = (ReturnStatement) st; 
      if (rt.getExpression() == null) 
      { return true; } 
    } 
    return false; 
  } 

  public static boolean isCumulativeRecursion(
                    BehaviouralFeature bf, Statement stat)
  { // stat involves semi-tail recursive calls to bf & 
    // only one base return statement - return using 
    // values and parameters only. 

    // stat does not write any of the parameters or 
    // any attributes - only local variables. 

    if (stat == null) 
    { return false; }

    Vector pars = bf.getParameters(); 
    if (pars.size() >= 1) 
    { /* 1st one should be integer, used for recursion */ } 
    else 
    { return false; } 

    Vector parnames = VectorUtil.getStrings(pars); 

    Vector newvars = new Vector(); // updated in stat
    Vector wrfr = stat.writeFrame();

    for (int i = 0; i < wrfr.size(); i++) 
    { String wrv = (String) wrfr.get(i); 
      int k = wrv.indexOf("::"); 
      if (k >= 0) 
      { // newvars.add(wrv.substring(k+2));
        System.err.println("! " + bf + 
                           " updates attribute " + wrv); 
        return false; 
      } 
      else if (parnames.contains(wrv))
      { System.err.println("! " + bf + 
                           " updates parameter " + wrv); 
        return false; 
      } 
      else 
      { newvars.add(wrv); } 
    }  

    System.out.println(">> local variables " + newvars + " are written in " + stat); 

    Attribute par = (Attribute) pars.get(0); 
    Type partype = par.getType(); 

    if ("int".equals(partype.getName()) || 
        "long".equals(partype.getName()))
    { } 
    else 
    { return false; } 

    String pname = par.getName(); 

    String nme = bf.getName(); 
    Vector names = new Vector(); 
    names.add(nme); 
    names.addAll(newvars); // local variables

    if (stat instanceof ConditionalStatement) 
    { ConditionalStatement conds = 
        (ConditionalStatement) stat; 
      Expression tst = conds.getTest(); 
      Statement statif = conds.getIf();
      Statement statelse = conds.getElse();

      // Test: = or <= or < between par and
      // expression without par 
      // if statement doesn't have par or nme
      // else statement has only tail/semitail
      // recursive calls. 

      boolean boundedAbove = false; 
      boolean boundedBelow = false; 

      if (tst instanceof BinaryExpression && 
          ((BinaryExpression) tst).variableBoundedAbove(pname) 
          &&
          statif instanceof ReturnStatement)
      { Vector names2 = new Vector(); 
        names2.add(nme); 
        names2.add(pname); 
        Vector ifvars = 
          statif.variablesUsedIn(names2); 
        if (ifvars.contains(nme) || ifvars.contains(pname))
        { return false; }
        boundedAbove = true; 
      } 
      else // other case of variableBoundedBelow
      if (tst instanceof BinaryExpression && 
          ((BinaryExpression) tst).variableBoundedBelow(pname) 
          &&
          statelse instanceof ReturnStatement)
      { Vector names2 = new Vector(); 
        names2.add(nme); 
        names2.add(pname); 
        Vector elsevars = 
          statelse.variablesUsedIn(names2); 
        if (elsevars.contains(nme) || 
            elsevars.contains(pname))
        { return false; }
        boundedBelow = true; 
      } 
      else 
      { return false; } 
    }       

    Vector rets = getReturnValues(stat); 
    
    int nontail = 0; 
    int nonrecursive = 0; 
    int tailrecursive = 0;
    int semitail = 0;  

    Vector semitails = new Vector(); 

    for (int i = 0; i < rets.size(); i++) 
    { Expression expr = (Expression) rets.get(i); 
      Vector uses = expr.variablesUsedIn(names); 
      if (uses.size() == 0) 
      { nonrecursive++; } 
      else if (expr.isSelfCallDecrement(bf, pname))
      { tailrecursive++; } 
      else if (expr instanceof BinaryExpression && 
        ((BinaryExpression) 
           expr).isSemiTailRecursionDecrement(bf, pname)) 
      { semitail++; 
        semitails.add(expr); 
      } 
      else 
      { nontail++; } 
    } 

    System.err.println(">> " + bf + " has " + 
         nonrecursive + " non-recursive returns, " + 
         tailrecursive + " tail recursive decrement returns,\n>>" + 
         " and " + 
         nontail + " non-tail recursive returns,\n>> " + 
         semitail + " semi-tail recursive decrement returns: " + 
         semitails);

    if (nonrecursive == 1 &&  
        semitail >= 1 && nontail == 0)
    { if (BinaryExpression.allOperatorsSame(semitails))
      { BinaryExpression rec = 
                   (BinaryExpression) semitails.get(0);
        String op = rec.getOperator(); // + or *
        if ("+".equals(op) || "*".equals(op))
        { return true; } 
      } 
    }  

    return false;  
  } 

  public static Expression conditionalBranches2Expressions(
      Statement st, BehaviouralFeature bf, String op, String par,
      Vector nonrecs, Vector tailrecs, Vector semirecs) 
  { // Assume code is ReturnStatement's nested in Conditionals

    if (st instanceof ReturnStatement) 
    { ReturnStatement rs = (ReturnStatement) st; 
      Expression retval = rs.getValue(); 

      if (nonrecs.contains(retval))
      { return retval; } 
    
      if (tailrecs.contains(retval))
      { // 0 if +, 1 if *
        Expression ifvalue = null; 

        if ("+".equals(op))
        { ifvalue = new BasicExpression(0); } 
        else
        { ifvalue = new BasicExpression(1); } 
        return ifvalue;
      } // Not included in sum/product
      
      if (semirecs.contains(retval) && 
          retval instanceof BinaryExpression)
      { // extract value part of sum or product
        return 
          ((BinaryExpression) 
              retval).replacedSemiTailRecursionDecrement(
                                                   bf, par);
      } 
    }

    if (st instanceof ConditionalStatement)
    { ConditionalStatement cs = (ConditionalStatement) st;
      Expression tst = cs.getTest();
      Statement ifc = cs.getIf(); 
      Statement elsec = cs.getElse(); 
 
      Expression ifexpr = 
        Statement.conditionalBranches2Expressions(ifc, 
                 bf, op, par, nonrecs, tailrecs, semirecs);
      Expression remainder = 
        Statement.conditionalBranches2Expressions(
                 elsec, bf, op, par, 
                 nonrecs, tailrecs, semirecs); 

      return 
         new ConditionalExpression(tst, ifexpr, remainder);
    }
     
    return null; 
  } 


  public static Statement simplifyCumulativeRecursion(
                     BehaviouralFeature bf, Statement stat)
  { // Replace  if n <= lbound then return expr0
    //          else return expr*bf(n-1)
    // By return of  
    // expr0*Integer.subrange(lbound+1,n)->collect(n|expr)->prd() 
    
    Vector pars = bf.getParameters(); 
    if (pars.size() >= 1) 
    { /* 1st one should be integer, used for recursion */ } 
    else 
    { return stat; } 

    Vector parnames = VectorUtil.getStrings(pars); 

    Vector newvars = new Vector(); // updated in stat
    Vector wrfr = stat.writeFrame();

    for (int i = 0; i < wrfr.size(); i++) 
    { String wrv = (String) wrfr.get(i); 
      int k = wrv.indexOf("::"); 
      if (k >= 0) 
      { // newvars.add(wrv.substring(k+2));
        System.err.println("! " + bf + 
                           " updates attribute " + wrv); 
        return stat; 
      } 
      else if (parnames.contains(wrv))
      { System.err.println("! " + bf + 
                           " updates parameter " + wrv); 
        return stat; 
      } 
      else 
      { newvars.add(wrv); } 
    }  

    Attribute par = bf.getParameter(0); 
    String pname = par.getName(); 
    String nme = bf.getName(); 

    Vector names = new Vector(); 
    names.add(nme); 
    names.addAll(newvars); 

    ConditionalStatement conds = 
        (ConditionalStatement) stat; 
    BinaryExpression tst = (BinaryExpression) conds.getTest(); 
    Statement statif = conds.getIf();
    Statement statelse = conds.getElse();

    Expression n0 = null;  // tst.variableBoundAbove(pname);
    Expression expr0 = null;  // statif.getValue();
    Expression iterbound = null;   

    boolean boundedAbove = false; 
    boolean boundedBelow = false; 

    if (tst instanceof BinaryExpression && 
        ((BinaryExpression) tst).variableBoundedAbove(pname) &&
        statif instanceof ReturnStatement)
    { BinaryExpression btest = (BinaryExpression) tst; 
      Vector names2 = new Vector(); 
      names2.add(nme); 
      names2.add(pname); 
      Vector ifvars = 
          statif.variablesUsedIn(names2); 
      if (ifvars.contains(nme) || ifvars.contains(pname))
      { return stat; }
      boundedAbove = true;
      n0 = btest.variableBoundAbove(pname);
      iterbound = btest.iterationBoundAbove(pname); 
      expr0 = ((ReturnStatement) statif).getValue();  
    } 
    else // other case of variableBoundedBelow
      if (tst instanceof BinaryExpression && 
          ((BinaryExpression) tst).variableBoundedBelow(pname) &&
          statelse instanceof ReturnStatement)
      { BinaryExpression btest = (BinaryExpression) tst; 
        Vector names2 = new Vector(); 
        names2.add(nme); 
        names2.add(pname); 
        Vector elsevars = 
          statelse.variablesUsedIn(names2); 
        if (elsevars.contains(nme) || 
            elsevars.contains(pname))
        { return stat; }
        boundedBelow = true; 
        n0 = btest.variableBoundBelow(pname);
        iterbound = btest.iterationBoundBelow(pname); 
        expr0 = ((ReturnStatement) statelse).getValue();  
      } 
      else 
      { return stat; }       

    Vector rangepars = new Vector(); 
    rangepars.add(n0); 
    Expression parexpr = new BasicExpression(par);  
    rangepars.add(parexpr); 

    Vector pars1 = new Vector(); 
    pars1.add(iterbound); 
    pars1.add(parexpr); 

    Vector rets = getReturnValues(stat); 
    
    int nontail = 0; 
    int nonrecursive = 0; 
    int tailrecursive = 0;
    int semitail = 0;  

    Vector nonrecs = new Vector(); 
    Vector tailrecs = new Vector(); 
    Vector semirecs = new Vector(); 

    for (int i = 0; i < rets.size(); i++) 
    { Expression expr = (Expression) rets.get(i); 
      Vector uses = expr.variablesUsedIn(names); 
      if (uses.size() == 0) 
      { nonrecursive++;
        nonrecs.add(expr); 
      } 
      else if (expr.isSelfCallDecrement(bf,pname))
      { tailrecursive++; 
        tailrecs.add(expr); 
      } 
      else if (expr instanceof BinaryExpression && 
        ((BinaryExpression) 
             expr).isSemiTailRecursionDecrement(bf,pname)) 
      { semitail++; 
        semirecs.add(expr); 
      } 
      else 
      { nontail++; } 
    } 

    if (semitail == 1 && tailrecursive == 0)
    { // Just single else case return expr*self.bf(n-1)
      BinaryExpression rec = 
                (BinaryExpression) semirecs.get(0);
      String op = rec.getOperator(); // + or * 

      Expression expr = 
        rec.replacedSemiTailRecursionDecrement(bf,pname); 

      Expression subrange = 
        BasicExpression.newFunctionBasicExpression(
             "subrange", "Integer", rangepars);
      Type subrangetype = new Type("Sequence", null); 
      subrangetype.setElementType(new Type("int", null)); 
      subrange.setType(subrangetype); 

      Expression rng = 
        new BinaryExpression(":", parexpr, subrange);  
      Expression col = 
        new BinaryExpression("|C", rng, expr); 

      if (op.equals("+"))
      { Expression sumexpr = 
          new UnaryExpression("->sum", col); 
        Expression res = 
          new BinaryExpression("+", expr0, sumexpr); 
        return new ReturnStatement(res); 
      } 

      if (op.equals("*"))
      { Expression prdexpr = 
          new UnaryExpression("->prd", col); 
        Expression res = 
          new BinaryExpression("*", expr0, prdexpr); 
        return new ReturnStatement(res); 
      } 
    }
    else if (semitail >= 1) // general case
    { BinaryExpression rec = 
          (BinaryExpression) semirecs.get(0);
      String op = rec.getOperator(); // + or * 
    
      Expression collectexpr = 
        Statement.conditionalBranches2Expressions(
                        stat, bf, op, 
                        pname, nonrecs, tailrecs, semirecs); 

      Expression subrange = 
        BasicExpression.newFunctionBasicExpression("subrange", 
                                            "Integer", pars1);
      Type subrangetype = new Type("Sequence", null); 
      subrangetype.setElementType(new Type("int", null)); 
      subrange.setType(subrangetype); 

      Expression rng = 
        new BinaryExpression(":", parexpr, subrange);  
      Expression col = 
        new BinaryExpression("|C", rng, collectexpr); 

      if (op.equals("+"))
      { Expression sumexpr = 
          new UnaryExpression("->sum", col); 
        return new ReturnStatement(sumexpr); 
      } 

      if (op.equals("*"))
      { Expression prdexpr = 
          new UnaryExpression("->prd", col); 
        return new ReturnStatement(prdexpr); 
      } 
    } 

    return stat; 
  } 

  public int execute(ModelSpecification sigma, ModelState beta)
  { return Statement.NORMAL; } // default is to do nothing

  public static Statement cumulativeCode(Expression var,
                                         Expression rng, 
                                         Statement st)
  { if (st == null) 
    { return null; }

    System.out.println(">> Converting cumulative code: " + var + 
                       " : " + rng + " @ " + st); 

    if (st instanceof AssignStatement)
    { // patterns are s := s + var, s := s + var*var,
      // s := s * var, s := s - var, s := s / var
      // s := s & var, s := s or var
      // Also same with expr instead of var,
      // not involving var or s. 

      AssignStatement asm = (AssignStatement) st; 
          
      Expression lhs = asm.getLeft(); 
      Expression rhs = asm.getRight();
      rhs.setBrackets(false); 

      if ((lhs + " + " + var).equals("" + rhs))
      { // lhs := lhs + loopRange->sum()

        Expression smm = new UnaryExpression("->sum", rng); 
        Expression newrhs = 
            new BinaryExpression("+", lhs, smm); 
        return new AssignStatement(lhs, newrhs); 
      } 
      else if (rhs instanceof BinaryExpression && 
        ((BinaryExpression) rhs).getOperator().equals("+") &&
        (((BinaryExpression) rhs).getLeft() + "").equals(lhs + ""))
      { // lhs := lhs + expr
        Expression expr = ((BinaryExpression) rhs).getRight(); 
        Vector vuses = expr.getVariableUses();

        if (VectorUtil.containsEqualString(lhs+"", vuses))
        { return null; } 

        if (VectorUtil.containsEqualString(var+"", vuses))
        { // lhs := lhs + rng->collect(var|expr)->sum()
          Expression coll = 
            new BinaryExpression("|C", 
              new BinaryExpression(":", var, rng), expr); 
          Expression smm = new UnaryExpression("->sum", coll); 
          Expression newrhs = 
            new BinaryExpression("+", lhs, smm); 
          return new AssignStatement(lhs, newrhs);
        } 

        // lhs := lhs + (rng->size())*expr
        Expression sze = Expression.simplifySize(rng);
        sze.setBrackets(true); 
 
        Expression newrhs = 
            new BinaryExpression("+", lhs, 
              new BinaryExpression("*", expr, sze)); 
        return new AssignStatement(lhs, newrhs); 
      }         
      else if ((lhs + " - " + var).equals("" + rhs))
      { // lhs := lhs - loopRange->sum()

        Expression smm = new UnaryExpression("->sum", rng); 
        Expression newrhs = 
            new BinaryExpression("-", lhs, smm); 
        return new AssignStatement(lhs, newrhs); 
      } 
      else if (rhs instanceof BinaryExpression && 
        ((BinaryExpression) rhs).getOperator().equals("-") &&
        (((BinaryExpression) rhs).getLeft() + "").equals(lhs + ""))
      { // lhs := lhs - expr
        Expression expr = ((BinaryExpression) rhs).getRight(); 
        Vector vuses = expr.getVariableUses();

        if (VectorUtil.containsEqualString(lhs+"", vuses))
        { return null; } 

        if (VectorUtil.containsEqualString(var+"", vuses))
        { // lhs := lhs - rng->collect(var|expr)->sum()
          Expression coll = 
            new BinaryExpression("|C", 
              new BinaryExpression(":", var, rng), expr); 
          Expression smm = new UnaryExpression("->sum", coll); 
          Expression newrhs = 
            new BinaryExpression("-", lhs, smm); 
          return new AssignStatement(lhs, newrhs);
        } 

        // lhs := lhs - (rng->size())*expr
        Expression sze = Expression.simplifySize(rng);
        sze.setBrackets(true); 
 
        Expression newrhs = 
            new BinaryExpression("-", lhs, 
              new BinaryExpression("*", expr, sze)); 
        return new AssignStatement(lhs, newrhs); 
      }         
      else if ((lhs + " * " + var).equals("" + rhs))
      { // lhs := lhs * loopRange->prd()

        Expression prd = new UnaryExpression("->prd", rng); 
        Expression newrhs = 
            new BinaryExpression("*", lhs, prd); 
        return new AssignStatement(lhs, newrhs); 
      }   
      else if (rhs instanceof BinaryExpression && 
        ((BinaryExpression) rhs).getOperator().equals("*") &&
        (((BinaryExpression) rhs).getLeft() + "").equals(lhs + ""))
      { // lhs := lhs * expr
        Expression expr = ((BinaryExpression) rhs).getRight(); 
        Vector vuses = expr.getVariableUses();

        if (VectorUtil.containsEqualString(lhs+"", vuses))
        { return null; } 

        if (VectorUtil.containsEqualString(var+"", vuses))
        { // lhs := lhs * rng->collect(var|expr)->prd()
          Expression coll = 
            new BinaryExpression("|C", 
              new BinaryExpression(":", var, rng), expr); 
          Expression smm = new UnaryExpression("->prd", coll); 
          Expression newrhs = 
            new BinaryExpression("*", lhs, smm); 
          return new AssignStatement(lhs, newrhs);
        } 

        // lhs := lhs * expr->pow(rng->size())
        Expression sze = Expression.simplifySize(rng);
        // sze.setBrackets(true); 
 
        Expression newrhs = 
            new BinaryExpression("*", lhs, 
              new BinaryExpression("->pow", expr, sze)); 
        return new AssignStatement(lhs, newrhs); 
      }         
      else if ((lhs + " / " + var).equals("" + rhs))
      { // lhs := lhs / loopRange->prd()

        Expression prd = new UnaryExpression("->prd", rng); 
        Expression newrhs = 
            new BinaryExpression("/", lhs, prd); 
        return new AssignStatement(lhs, newrhs); 
      }   
      else if (rhs instanceof BinaryExpression && 
        ((BinaryExpression) rhs).getOperator().equals("/") &&
        (((BinaryExpression) rhs).getLeft() + "").equals(lhs + ""))
      { // lhs := lhs / expr
        Expression expr = ((BinaryExpression) rhs).getRight(); 
        Vector vuses = expr.getVariableUses();

        if (VectorUtil.containsEqualString(lhs+"", vuses))
        { return null; } 

        if (VectorUtil.containsEqualString(var+"", vuses))
        { // lhs := lhs / rng->collect(var|expr)->prd()
          Expression coll = 
            new BinaryExpression("|C", 
              new BinaryExpression(":", var, rng), expr); 
          Expression smm = new UnaryExpression("->prd", coll); 
          Expression newrhs = 
            new BinaryExpression("/", lhs, smm); 
          return new AssignStatement(lhs, newrhs);
        } 

        // lhs := lhs / expr->pow(rng->size())
        Expression sze = Expression.simplifySize(rng);
        // sze.setBrackets(true); 
 
        Expression newrhs = 
            new BinaryExpression("/", lhs, 
              new BinaryExpression("->pow", expr, sze)); 
        return new AssignStatement(lhs, newrhs); 
      }         
      else if ((lhs + " & " + var).equals("" + rhs))
      { // lhs := lhs & loopRange->forAll(self)

        Type elemt = rng.getElementType(); 
        BasicExpression selfvar = 
          BasicExpression.newVariableBasicExpression(
                                             "self",elemt); 
        Expression prd = 
          new BinaryExpression("->forAll", rng, selfvar); 
        Expression newrhs = 
            new BinaryExpression("&", lhs, prd); 
        return new AssignStatement(lhs, newrhs); 
      }   
      else if (rhs instanceof BinaryExpression && 
        ((BinaryExpression) rhs).getOperator().equals("&") &&
        (((BinaryExpression) rhs).getLeft() + "").equals(lhs + ""))
      { // lhs := lhs & expr
        Expression expr = ((BinaryExpression) rhs).getRight(); 
        Vector vuses = expr.getVariableUses();

        if (VectorUtil.containsEqualString(lhs+"", vuses))
        { return null; } 

        // lhs := lhs & rng->forAll(var|expr)

        Expression coll = 
            new BinaryExpression("!", 
              new BinaryExpression(":", var, rng), expr); 
        Expression newrhs = 
            new BinaryExpression("&", lhs, coll); 
        return new AssignStatement(lhs, newrhs);
      }         
      else if ((lhs + " or " + var).equals("" + rhs))
      { // lhs := lhs or loopRange->exists(self)

        Type elemt = rng.getElementType(); 
        BasicExpression selfvar = 
          BasicExpression.newVariableBasicExpression(
                                             "self",elemt); 
        Expression prd = 
          new BinaryExpression("->exists", rng, selfvar); 
        Expression newrhs = 
            new BinaryExpression("or", lhs, prd); 
        return new AssignStatement(lhs, newrhs); 
      }   
      else if (rhs instanceof BinaryExpression && 
        ((BinaryExpression) rhs).getOperator().equals("or") &&
        (((BinaryExpression) rhs).getLeft() + "").equals(lhs + ""))
      { // lhs := lhs or expr
        Expression expr = ((BinaryExpression) rhs).getRight(); 
        Vector vuses = expr.getVariableUses();

        if (VectorUtil.containsEqualString(lhs+"", vuses))
        { return null; } 

        // lhs := lhs or rng->exists(var|expr)

        Expression coll = 
            new BinaryExpression("#", 
              new BinaryExpression(":", var, rng), expr); 
        Expression newrhs = 
            new BinaryExpression("or", lhs, coll); 
        return new AssignStatement(lhs, newrhs);
      }         
    /*  else if (rhs instanceof BinaryExpression && 
        (((BinaryExpression) rhs).getLeft() + "").equals(
                                                    lhs + ""))
      { BinaryExpression brhs = (BinaryExpression) rhs; 
        Expression expr = brhs.getRight();
        String oper = brhs.getOperator(); 
 
        Vector vars = expr.getVariableUses(); 

        if (VectorUtil.containsEqualString(var + "", vars) || 
            VectorUtil.containsEqualString(lhs + "", vars))
        { return null; } 

        if ("+".equals(oper))
        { // lhs := lhs + expr*(loopRange->size())

          Expression smm = Expression.simplifySize(rng);
          smm.setBrackets(true); 
 
          Expression newrhs = 
            new BinaryExpression("+", lhs, 
              new BinaryExpression("*", expr, smm)); 
          return new AssignStatement(lhs, newrhs); 
        } 
        else if ("-".equals(oper))
        { // lhs := lhs - expr*(loopRange->size())

          Expression smm = Expression.simplifySize(rng);
          smm.setBrackets(true); 
 
          Expression newrhs = 
            new BinaryExpression("-", lhs, 
              new BinaryExpression("*", expr, smm)); 
          return new AssignStatement(lhs, newrhs); 
        } 
        else if ("*".equals(oper))
        { // lhs := lhs * expr->pow(loopRange->size())

          Expression smm = Expression.simplifySize(rng);
          // smm.setBrackets(true); 
 
          Expression newrhs = 
            new BinaryExpression("*", lhs, 
              new BinaryExpression("->pow", expr, smm)); 
          return new AssignStatement(lhs, newrhs); 
        } 
        else if ("/".equals(oper))
        { // lhs := lhs / expr->pow(loopRange->size())

          Expression smm = Expression.simplifySize(rng);
          // smm.setBrackets(true); 
 
          Expression newrhs = 
            new BinaryExpression("/", lhs, 
              new BinaryExpression("->pow", expr, smm)); 
          return new AssignStatement(lhs, newrhs); 
        } 
      } */ 
    }
    else if (st instanceof SequenceStatement) 
    { SequenceStatement sq = (SequenceStatement) st; 

      // JOptionPane.showInputDialog(sq + " " + sq.size()); 

      if (sq.size() == 0) 
      { return null; }

      if (sq.size() == 1)
      { // patterns are s := s + var
        // s := s * var

        Statement stat0 = sq.getStatement(0); 

        if (stat0 instanceof SequenceStatement)
        { return Statement.cumulativeCode(var,rng,stat0); } 

        if (stat0 instanceof AssignStatement) 
        { return Statement.cumulativeCode(var,rng,stat0); }

        return null; 
      }

      if (sq.size() == 2)
      { // patterns are I := I + 1; V := V + I
        // I := I + 1; V := V - I
        // I := I + 1; V := V * I   var /= I
        
        Statement stat1 = sq.getStatement(0); 
        Statement stat2 = sq.getStatement(1);

        // JOptionPane.showInputDialog("**** Potential code reduction of loop >>: " + 
        //   stat1 + " ; " + stat2 + 
        //   " " + (stat1 instanceof AssignStatement) + " " + 
        //         (stat2 instanceof AssignStatement));

        if (stat1 instanceof AssignStatement && 
            stat2 instanceof AssignStatement)
        { AssignStatement ast1 = (AssignStatement) stat1; 
          AssignStatement ast2 = (AssignStatement) stat2;

          Expression lhs1 = ast1.getLeft(); 
          Expression rhs1 = ast1.getRight();
          Expression lhs2 = ast2.getLeft(); 
          Expression rhs2 = ast2.getRight();

          if ((var + "").equals("" + lhs1) || 
              (var + "").equals("" + lhs2))
          { return null; } 

          Vector vars1 = rhs1.getVariableUses(); 
          Vector vars2 = rhs2.getVariableUses(); 

          if (VectorUtil.containsEqualString(var + "", vars1) || 
              VectorUtil.containsEqualString(var + "", vars2))
          { return null; } 
          
          rhs1.setBrackets(false); 
          rhs2.setBrackets(false); 

          // JOptionPane.showInputDialog("**** Potential code reduction of loop: " + lhs1 + " := " + rhs1 + " ; " + lhs2 + " ; " + rhs2);

          if ((lhs1 + " + 1").equals("" + rhs1) && 
              (lhs2 + " + " + lhs1).equals("" + rhs2))
          { // I := I + 1; V := V + I
            // reduces to V := V + n*I + (n*(n+1))/2; 
            //            I := I + n

            Expression rsize = 
               Expression.simplifySize(rng);
            Expression rsize1 = new BinaryExpression("+", rsize, 
                    new BasicExpression(1));
            rsize1.setBrackets(true);  
            Expression prd = new BinaryExpression("*", rsize,
                                                  rsize1);
            prd.setBrackets(true);  
 
            BinaryExpression isum = 
              new BinaryExpression("/", prd, 
                                   new BasicExpression(2));
            BinaryExpression nsum = 
              new BinaryExpression("*", rsize, lhs1); 
            AssignStatement asn1 = 
              new AssignStatement(lhs2,
                new BinaryExpression("+", lhs2, 
                  new BinaryExpression("+", nsum, isum)));
            AssignStatement asn2 = 
              new AssignStatement(lhs1, 
                new BinaryExpression("+", lhs1, rsize)); 
            SequenceStatement ss = new SequenceStatement(); 
            ss.addStatement(asn1); 
            ss.addStatement(asn2);

            // JOptionPane.showInputDialog(">> Code reduction of " + st + " to: " + ss);
 
            return ss;    
          } 
          else if ((lhs2 + " + 1").equals("" + rhs2) && 
              (lhs1 + " + " + lhs2).equals("" + rhs1))
          { // V := V + I ; I := I + 1
            // reduces to V := V + n*I + (n*(n-1))/2; 
            //            I := I + n

           /* JOptionPane.showInputDialog("**** Potential code reduction of loop: " + lhs1 + " := " + rhs1 + " ; " + lhs2 + " ; " + rhs2); */ 
      
            Expression rsize = 
               Expression.simplifySize(rng);
            Expression rsize1 = new BinaryExpression("-", rsize, 
                    new BasicExpression(1));
            rsize1.setBrackets(true);  
            Expression prd = new BinaryExpression("*", rsize,
                                                  rsize1);
            prd.setBrackets(true);  
 
            BinaryExpression isum = 
              new BinaryExpression("/", prd, 
                                   new BasicExpression(2));
            BinaryExpression nsum = 
              new BinaryExpression("*", rsize, lhs2); 
            AssignStatement asn1 = 
              new AssignStatement(lhs1,
                new BinaryExpression("+", lhs1, 
                  new BinaryExpression("+", nsum, isum)));
            AssignStatement asn2 = 
              new AssignStatement(lhs2, 
                new BinaryExpression("+", lhs2, rsize)); 
            SequenceStatement ss = new SequenceStatement(); 
            ss.addStatement(asn1); 
            ss.addStatement(asn2);

            // JOptionPane.showInputDialog(">> Code reduction of " + st + " to: " + ss);
 
            return ss;    
          } 
          else if ((lhs1 + " + 1").equals("" + rhs1) && 
              (lhs2 + " - " + lhs1).equals("" + rhs2))
          { // I := I + 1; V := V - I
            // reduces to V := V - n*I - (n*(n+1))/2; 
            //            I := I + n

            Expression rsize = 
               Expression.simplifySize(rng);
            Expression rsize1 = new BinaryExpression("+", rsize, 
                    new BasicExpression(1));
            rsize1.setBrackets(true);  
            Expression prd = new BinaryExpression("*", rsize,
                                                  rsize1);
            prd.setBrackets(true);  
 
            BinaryExpression isum = 
              new BinaryExpression("/", prd, 
                                   new BasicExpression(2));
            BinaryExpression nsum = 
              new BinaryExpression("*", rsize, lhs1); 
            AssignStatement asn1 = 
              new AssignStatement(lhs2,
                new BinaryExpression("-", lhs2, 
                  new BinaryExpression("-", nsum, isum)));
            AssignStatement asn2 = 
              new AssignStatement(lhs1, 
                new BinaryExpression("+", lhs1, rsize)); 
            SequenceStatement ss = new SequenceStatement(); 
            ss.addStatement(asn1); 
            ss.addStatement(asn2);

            // JOptionPane.showInputDialog(">> Code reduction of " + st + " to: " + ss);
 
            return ss;    
          } 
          else if ((lhs2 + " + 1").equals("" + rhs2) && 
              (lhs1 + " - " + lhs2).equals("" + rhs1))
          { // V := V - I ; I := I + 1
            // reduces to V := V - n*I - (n*(n-1))/2; 
            //            I := I + n

           /* JOptionPane.showInputDialog("**** Potential code reduction of loop: " + lhs1 + " := " + rhs1 + " ; " + lhs2 + " ; " + rhs2); */ 
      
            Expression rsize = 
               Expression.simplifySize(rng);
            Expression rsize1 = 
               new BinaryExpression("-", rsize, 
                    new BasicExpression(1));
            rsize1.setBrackets(true);  
            Expression prd = new BinaryExpression("*", rsize,
                                                  rsize1);
            prd.setBrackets(true);  
 
            BinaryExpression isum = 
              new BinaryExpression("/", prd, 
                                   new BasicExpression(2));
            BinaryExpression nsum = 
              new BinaryExpression("*", rsize, lhs2); 
            AssignStatement asn1 = 
              new AssignStatement(lhs1,
                new BinaryExpression("-", lhs1, 
                  new BinaryExpression("-", nsum, isum)));
            AssignStatement asn2 = 
              new AssignStatement(lhs2, 
                new BinaryExpression("+", lhs2, rsize)); 
            SequenceStatement ss = new SequenceStatement(); 
            ss.addStatement(asn1); 
            ss.addStatement(asn2);

            // JOptionPane.showInputDialog(">> Code reduction of " + st + " to: " + ss);
 
            return ss;    
          } 
          else if ((lhs1 + " + 1").equals("" + rhs1) && 
              (lhs2 + " * " + lhs1).equals("" + rhs2))
          { // I := I + 1; V := V * I
            // reduces to V := V * MathLib.factorial(I + n)/
            //                       MathLib.factorial(I); 
            //            I := I + n

            Expression rsize = 
               Expression.simplifySize(rng);
            Expression rsize1 = 
                         new BinaryExpression("+", lhs1, rsize);
            Expression fact1 = 
              BasicExpression.newStaticCallExpression("MathLib",
                                            "factorial", rsize1);
            Expression fact2 = 
              BasicExpression.newStaticCallExpression("MathLib",
                                            "factorial", lhs1);
            
            BinaryExpression idiv = 
              new BinaryExpression("/", fact1, fact2);
            AssignStatement asn1 = 
              new AssignStatement(lhs2,
                new BinaryExpression("*", lhs2, idiv));
            AssignStatement asn2 = 
              new AssignStatement(lhs1, 
                new BinaryExpression("+", lhs1, rsize)); 
            SequenceStatement ss = new SequenceStatement(); 
            ss.addStatement(asn1); 
            ss.addStatement(asn2);

            // JOptionPane.showInputDialog(">> Code reduction of " + st + " to: " + ss);
 
            return ss;    
          } 
          else if ((lhs2 + " + 1").equals("" + rhs2) && 
              (lhs1 + " * " + lhs2).equals("" + rhs1))
          { // V := V * I ; I := I + 1
            // reduces to V := V * (I+n-1)!/I!; 
            //            I := I + n

            Expression rsize = 
               Expression.simplifySize(rng);
            Expression rsize1 = 
               new BinaryExpression("+", lhs2, 
                    new BinaryExpression("-", rsize, 
                           new BasicExpression(1)));
            Expression fact1 = 
              BasicExpression.newStaticCallExpression("MathLib",
                                            "factorial", rsize1);
            Expression fact2 = 
              BasicExpression.newStaticCallExpression("MathLib",
                                            "factorial", lhs2);
            
            BinaryExpression idiv = 
              new BinaryExpression("/", fact1, fact2);
            AssignStatement asn1 = 
              new AssignStatement(lhs1,
                new BinaryExpression("*", lhs1, idiv));
            AssignStatement asn2 = 
              new AssignStatement(lhs2, 
                new BinaryExpression("+", lhs2, rsize)); 
            SequenceStatement ss = new SequenceStatement(); 
            ss.addStatement(asn1); 
            ss.addStatement(asn2);

            // JOptionPane.showInputDialog(">> Code reduction of " + st + " to: " + ss);
 
            return ss;    
          } 
        }
      }
    } 

    return null; 
  }

  public static boolean isAdditionToCollection(Statement stat, Expression x, Expression st)
  { if (stat instanceof AssignStatement) 
    { AssignStatement ifassign = (AssignStatement) stat; 
      // ifassign is  st := st->including(x) or 
      //              st := st->append(x)
      
      Expression ifvar = ifassign.getLhs(); 
      ifvar.setBrackets(false); 
 
      if (ifassign.getRhs() instanceof BinaryExpression) 
      { BinaryExpression ifbe = 
                  (BinaryExpression) ifassign.getRhs(); 
        Expression ifbeLeft = ifbe.getLeft(); // st
        Expression ifbeRight = ifbe.getRight(); // x

        ifbeLeft.setBrackets(false);  
        ifbeRight.setBrackets(false);  

        if ((st + "").equals(ifvar + "") && 
            (st + "").equals(ifbeLeft + "") && 
            (x + "").equals(ifbeRight + ""))
        { if (ifbe.getOperator().equals("->including") || 
              ifbe.getOperator().equals("->append"))
          { return true; } 
        } 
      } 
    } 
    else if (stat instanceof ImplicitInvocationStatement) 
    { Expression callExpr = 
        ((ImplicitInvocationStatement) stat).callExp;

      if (callExpr instanceof BinaryExpression)
      { BinaryExpression bexpr = (BinaryExpression) callExpr; 

        Expression bleft = bexpr.getLeft(); 
        Expression bright = bexpr.getRight(); 
        bleft.setBrackets(false); 
        bright.setBrackets(false); 

        // it is x : st
        if (":".equals(bexpr.getOperator()) && 
            (x + "").equals(bleft + "") && 
            (st + "").equals(bright + ""))
        { return true; } 
      }  
    } 

    return false; 
  } 


  public static boolean isCumulativeBody(Expression var,
                                         Statement st)
  { if (st == null) 
    { return false; }

    if (st instanceof AssignStatement)
    { // patterns are s := s + var
      // s := s * var

      AssignStatement asm = (AssignStatement) st; 
          // Vector vars = asm.getVariableUses(); 
      Expression lhs = asm.getLeft(); 
      Expression rhs = asm.getRight();
      rhs.setBrackets(false); 

      if ((lhs + " + " + var).equals("" + rhs))
      { // lhs := lhs + loopRange->sum()
        return true; 
      } 
      else if ((lhs + " - " + var).equals("" + rhs))
      { // lhs := lhs - loopRange->sum()
        return true; 
      } 
      else if ((lhs + " * " + var).equals("" + rhs))
      { // lhs := lhs * loopRange->prd()
        return true; 
      }   
      else if ((lhs + " / " + var).equals("" + rhs))
      { // lhs := lhs / loopRange->prd()
        return true; 
      } 
      else if ((lhs + " & " + var).equals("" + rhs))
      { // lhs := lhs & loopRange->forAll(self)
        return true; 
      } 
      else if ((lhs + " or " + var).equals("" + rhs))
      { // lhs := lhs or loopRange->exists(self)
        return true; 
      } 
      else if (rhs instanceof BinaryExpression && 
        (((BinaryExpression) rhs).getLeft() + "").equals(
                                                    lhs + ""))
      { BinaryExpression brhs = (BinaryExpression) rhs; 
        Expression expr = brhs.getRight();
        String oper = brhs.getOperator(); 
 
        Vector vars = expr.getVariableUses(); 

        if (// VectorUtil.containsEqualString(var + "", vars) || 
            VectorUtil.containsEqualString(lhs + "", vars))
        { return false; } 

        if ("+".equals(oper) || "-".equals(oper) || 
            "*".equals(oper) || "/".equals(oper) ||
            "&".equals(oper) || "or".equals(oper))
        { return true; }
 
        return false;
      } 
    }
    else if (st instanceof SequenceStatement) 
    { SequenceStatement sq = (SequenceStatement) st; 

      // JOptionPane.showInputDialog(sq + " " + sq.size()); 

      if (sq.size() == 0) 
      { return false; }

      if (sq.size() == 1)
      { // patterns are s := s + var
        // s := s * var

        Statement stat0 = sq.getStatement(0); 

        if (stat0 instanceof SequenceStatement)
        { return Statement.isCumulativeBody(var,stat0); } 

        if (stat0 instanceof AssignStatement) 
        { return Statement.isCumulativeBody(var, stat0); }

        return false; 
      } 
      else if (sq.size() == 2)
      { // patterns are I := I + 1; V := V + I
        // I := I + 1; V := V * I   var /= I
        
        Statement stat1 = sq.getStatement(0); 
        Statement stat2 = sq.getStatement(1);

        /* JOptionPane.showInputDialog("++++ Potential code reduction of loop >>: " + 
           stat1 + " ; " + stat2 + 
           " " + (stat1 instanceof AssignStatement) + " " + 
                 (stat2 instanceof AssignStatement)); */ 
      
        if (stat1 instanceof AssignStatement && 
            stat2 instanceof AssignStatement)
        { AssignStatement ast1 = (AssignStatement) stat1; 
          AssignStatement ast2 = (AssignStatement) stat2;

          Expression lhs1 = ast1.getLeft(); 
          Expression rhs1 = ast1.getRight();
          Expression lhs2 = ast2.getLeft(); 
          Expression rhs2 = ast2.getRight();

          if ((var + "").equals("" + lhs1) || 
              (var + "").equals("" + lhs2))
          { return false; } 

          Vector vars1 = rhs1.getVariableUses(); 
          Vector vars2 = rhs2.getVariableUses(); 

          if (VectorUtil.containsEqualString(var + "", vars1) || 
              VectorUtil.containsEqualString(var + "", vars2))
          { return false; } 
          
          // JOptionPane.showInputDialog("++ Potential code reduction of loop: " + st);
         
          return true; 
        } 
      }  
    } 

    return false; 
  } 


  public static boolean isEmpty(Statement st)
  { if (st == null) { return true; } 
    if (st instanceof SequenceStatement) 
    { SequenceStatement sq = (SequenceStatement) st; 
      if (sq.size() == 0) 
      { return true; } 
    } 
    return false; 
  } 

  public static boolean isPathEnd(Statement st) 
  { if (isSingleReturnStatement(st))
    { return true; } 
    if (isSingleBreakStatement(st))
    { return true; } 
    return false; 
  } // or continue or exit? 

  public static void addBeforeEnd(Statement blk, Statement st)
  { if (blk instanceof SequenceStatement && st != null) 
    { SequenceStatement ss = (SequenceStatement) blk; 
      ss.addBeforeEnd(st); 
    } 
  } 

  public static boolean isSingleReturnStatement(Statement st)
  { if (st == null) 
    { return false; } 
    if (st instanceof SequenceStatement) 
    { SequenceStatement sq = (SequenceStatement) st; 
      if (sq.size() == 1) 
      { Statement stat = sq.getStatement(0); 
        if (stat instanceof ReturnStatement ||
            isSingleReturnStatement(stat)) 
        { return true; }
      } 
      return false;  
    } 
    return (st instanceof ReturnStatement); 
  } 

  public static boolean isSingleBreakStatement(Statement st)
  { if (st == null) 
    { return false; } 
    if (st instanceof SequenceStatement) 
    { SequenceStatement sq = (SequenceStatement) st; 
      if (sq.size() == 1) 
      { Statement stat = sq.getStatement(0); 
        if (stat instanceof BreakStatement || 
            isSingleBreakStatement(stat)) 
        { return true; }
      } 
      return false;  
    } 
    return (st instanceof BreakStatement); 
  } 

  public static Expression getReturnExpression(Statement st)
  { if (st == null) 
    { return new BasicExpression("null"); }
 
    if (st instanceof SequenceStatement) 
    { SequenceStatement sq = (SequenceStatement) st; 
      if (sq.size() == 1) 
      { Statement stat = sq.getStatement(0); 
        if (stat instanceof ReturnStatement) 
        { return getReturnExpression(stat); }
      } 
      return new BasicExpression("null");
    } 
    
    if (st instanceof ReturnStatement)
    { ReturnStatement ret = (ReturnStatement) st; 
      Expression res = ret.getExpression(); 
      if (res == null) 
      { return new BasicExpression("null"); }
      return res; 
    } 
    return new BasicExpression("null");
  } 

  public static Statement getFirstStatement(Statement st)
  { if (st == null) 
    { return null; }
 
    if (st instanceof SequenceStatement) 
    { SequenceStatement sq = (SequenceStatement) st; 

      if (sq.size() >= 1) 
      { Statement stat = sq.getStatement(0); 
        return Statement.getFirstStatement(stat); 
      } 

      return null;
    } 

    return st; 
  } 

  public static boolean hasSingleStatement(Statement st)
  { if (st == null) 
    { return false; }
 
    if (st instanceof SequenceStatement) 
    { SequenceStatement sq = (SequenceStatement) st; 

      if (sq.size() == 1) 
      { return true; } // or sq[1] is single

      return false;
    } 

    return true; 
  } 

  public static Vector getReturnValues(Statement st)
  { Vector res = new Vector(); 
    if (st == null) 
    { return res; }
 
    if (st instanceof SequenceStatement) 
    { SequenceStatement sq = (SequenceStatement) st; 
      Vector stats = sq.getStatements(); 
      for (int i = 0; i < stats.size(); i++) 
      { if (stats.get(i) instanceof Statement)
        { Statement stat = (Statement) stats.get(i); 
          res.addAll(Statement.getReturnValues(stat));
        }  
      } 
      return res;
    } 
    
    if (st instanceof ReturnStatement)
    { ReturnStatement ret = (ReturnStatement) st; 
      Expression retExpr = ret.getExpression(); 
      if (retExpr == null) 
      { return res; }
      res.add(retExpr); 
      return res; 
    } 

    if (st instanceof ConditionalStatement) 
    { ConditionalStatement cs = (ConditionalStatement) st; 
      res.addAll(getReturnValues(cs.ifPart())); 
      res.addAll(getReturnValues(cs.elsePart())); 
      return res; 
    } 

    if (st instanceof WhileStatement) 
    { WhileStatement ws = (WhileStatement) st; 
      res.addAll(getReturnValues(ws.getLoopBody())); 
      return res; 
    } 

    if (st instanceof TryStatement) 
    { TryStatement ts = (TryStatement) st; 
      res.addAll(getReturnValues(ts.getBody())); 
      Vector stats = ts.getClauses(); 
      for (int i = 0; i < stats.size(); i++) 
      { if (stats.get(i) instanceof Statement)
        { Statement stat = (Statement) stats.get(i); 
          res.addAll(getReturnValues(stat));
        }  
      } 
      res.addAll(getReturnValues(ts.getEndStatement())); 
    } 

    return res;
  } // Other cases, for all other forms of statement. 

  public static boolean hasLoopStatement(Statement st)
  { boolean res = false; 
    if (st == null) 
    { return res; }
 
    if (st instanceof SequenceStatement) 
    { SequenceStatement sq = (SequenceStatement) st; 
      Vector stats = sq.getStatements(); 
      for (int i = 0; i < stats.size(); i++) 
      { if (stats.get(i) instanceof Statement)
        { Statement stat = (Statement) stats.get(i); 
          if (Statement.hasLoopStatement(stat))
          { return true; }
        }  
      } 
      return res;
    } 
    
    if (st instanceof ReturnStatement)
    { return res; } 

    if (st instanceof ConditionalStatement) 
    { ConditionalStatement cs = (ConditionalStatement) st; 
      if (Statement.hasLoopStatement(cs.ifPart()))
      { return true; }  
      return Statement.hasLoopStatement(cs.elsePart());
    } 

    if (st instanceof WhileStatement) 
    { return true; } 

    if (st instanceof TryStatement) 
    { TryStatement ts = (TryStatement) st; 
      if (Statement.hasLoopStatement(ts.getBody()))
      { return true; } 
 
      Vector stats = ts.getClauses(); 
      for (int i = 0; i < stats.size(); i++) 
      { if (stats.get(i) instanceof Statement)
        { Statement stat = (Statement) stats.get(i); 
          if (Statement.hasLoopStatement(stat))
          { return true; } 
        }  
      } 
      return Statement.hasLoopStatement(ts.getEndStatement()); 
    } 

    return res;
  } // Other cases, for all other forms of statement. 

  public Vector allVariableNames()
  { return new Vector(); } // default

  public abstract Statement optimiseOCL();

  public Expression definedness()
  { return new BasicExpression(true); } 

  public abstract Map energyUse(Map uses, 
                                Vector rUses, Vector oUses);

  public abstract java.util.Map collectionOperatorUses(
                             int nestingLevel, 
                             java.util.Map operatorsAtLevel, 
                             Vector vars);
  // collection operation uses at each nesting level
  // level |-> [expr1, expr2, ...] with iterators vars


  public static boolean isSemiTailRecursive(
            BehaviouralFeature bf, String nme, Statement st)
  { // There is only one return that does not involve bf
    // There is only one non-tail return involving bf
    // All other returns are direct calls of bf

    // More generally, all non-tail returns have same operator 
    // (numeric +, *, or ->including or ->union on sets/bags)
 
    Vector pars = bf.getParameters(); 
    Vector parnames = VectorUtil.getStrings(pars); 

    Vector newvars = new Vector(); // updated in st
    Vector wrfr = st.writeFrame();

    for (int i = 0; i < wrfr.size(); i++) 
    { String wrv = (String) wrfr.get(i); 
      int k = wrv.indexOf("::"); 
      if (k >= 0) 
      { // newvars.add(wrv.substring(k+2));
        System.err.println("! " + bf + 
                           " updates attribute " + wrv); 
        return false; 
      } 
      else if (parnames.contains(wrv))
      { System.err.println("! " + bf + 
                           " updates parameter " + wrv); 
        return false; 
      } 
      else 
      { newvars.add(wrv); } 
    }  

    System.out.println(">> Local variables of " + st + 
                       " are " + newvars); 

    Vector names = new Vector(); 
    names.add(nme); 
    names.addAll(newvars); 

    Vector rets = getReturnValues(st); 
    
    int nontail = 0; 
    int nonrecursive = 0; 
    int tailrecursive = 0;
    int semitail = 0;  

    for (int i = 0; i < rets.size(); i++) 
    { Expression expr = (Expression) rets.get(i); 
      Vector uses = expr.variablesUsedIn(names); 
      if (uses.size() == 0) 
      { nonrecursive++; } 
      else if (expr.isSelfCall(bf))
      { tailrecursive++; } 
      else if (expr instanceof BinaryExpression && 
        ((BinaryExpression) expr).isSemiTailRecursion(bf)) 
      { semitail++; } 
      else 
      { nontail++; } 
    } 

    System.err.println(">> " + nme + " has " + 
         nonrecursive + " non-recursive returns, " + 
         tailrecursive + " tail recursive returns,\n>>" + 
         " and " + 
         nontail + " non-tail recursive returns,\n>> " + 
         semitail + " semi-tail recursive returns: " + rets);

    if (nonrecursive == 1 && semitail == 1 && nontail == 0)
    { return true; } 
    return false;  
  } 

  public static boolean isTailRecursion(
            BehaviouralFeature bf, String nme, Statement st)
  { // There is only one return that does not involve bf
    // All other returns are direct calls of bf

    Vector pars = bf.getParameters(); 
    Vector parnames = VectorUtil.getStrings(pars); 

    Vector newvars = new Vector(); // updated in st
    Vector wrfr = st.writeFrame();

    for (int i = 0; i < wrfr.size(); i++) 
    { String wrv = (String) wrfr.get(i); 
      int k = wrv.indexOf("::"); 
      if (k >= 0) 
      { // newvars.add(wrv.substring(k+2));
        System.err.println("! " + bf + 
                           " updates attribute " + wrv); 
        return false; 
      } 
      else if (parnames.contains(wrv))
      { System.err.println("! " + bf + 
                           " updates parameter " + wrv); 
        return false; 
      } 
      else 
      { newvars.add(wrv); } 
    }  

    System.out.println(">> Local variables of " + st + 
                       " are " + newvars); 

    Vector names = new Vector(); 
    names.add(nme); 
    names.addAll(newvars); 

    Vector rets = getReturnValues(st); 
    
    int nontail = 0; 
    int nonrecursive = 0; 
    int tailrecursive = 0;
    int semitail = 0;  

    for (int i = 0; i < rets.size(); i++) 
    { Expression expr = (Expression) rets.get(i); 
      Vector uses = expr.variablesUsedIn(names); 
      if (uses.size() == 0) 
      { nonrecursive++; } 
      else if (expr.isSelfCall(bf))
      { tailrecursive++; } 
      else if (expr instanceof BinaryExpression && 
        ((BinaryExpression) expr).isSemiTailRecursion(bf)) 
      { semitail++; } 
      else 
      { nontail++; } 
    } 

    System.err.println(">> " + nme + " has " + 
         nonrecursive + " non-recursive returns, " + 
         tailrecursive + " tail recursive returns,\n>>" + 
         " and " + 
         nontail + " non-tail recursive returns,\n>> " + 
         semitail + " semi-tail recursive returns: " + rets);

    if (semitail == 0 && nontail == 0)
    { return true; } 
    return false;  
  } 

  public static boolean isTailRecursive(
            BehaviouralFeature bf, String nme, Statement st)
  { // if all calls of bf are direct calls in invocation 
    // statements or return statements. But not allowed to 
    // modify result of a self-call before returning it.

    if (st == null) 
    { return true; }
 
    if (st instanceof SequenceStatement) 
    { SequenceStatement sq = (SequenceStatement) st; 
      Vector stats = sq.getStatements();

      for (int i = 0; i < stats.size(); i++) 
      { Statement stat = (Statement) stats.get(i); 
        if (stat.isTailRecursive(bf,nme,stat)) { } 
        else 
        { return false; } 
      } 

      return true; 
    } 

    Vector names = new Vector(); 
    names.add(nme); 

    if (st instanceof InvocationStatement)
    { InvocationStatement invok = 
        (InvocationStatement) st;
      
      Expression expr = invok.getCallExp();
      Vector vars1 =
        expr.variablesUsedIn(names);
    
      if (expr != null && expr.isSelfCall(bf))
      { return true; } 
      else if (expr != null && vars1.size() > 0)
      { return false; } // call to bf within an expression

      return true; // null expression or no occurrence of bf 
    } 

    if (st instanceof ImplicitInvocationStatement)
    { ImplicitInvocationStatement invok = 
        (ImplicitInvocationStatement) st;
      
      Expression expr = invok.getCallExp();
      Vector vars1 =
        expr.variablesUsedIn(names);
    
      if (expr != null && expr.isSelfCall(bf))
      { return true; } 
      else if (expr != null && vars1.size() > 0)
      { return false; } // call to bf within an expression

      return true; // null expression or no occurrence of bf 
    } 

    if (st instanceof ReturnStatement)
    { ReturnStatement retstat = (ReturnStatement) st; 
      
      Expression expr = retstat.getReturnValue();

      if (expr == null) { return true; } 

      Vector vars1 =
        expr.variablesUsedIn(names);
 
      // System.out.println(">> " + expr.isSelfCall(bf) + " " + vars1); 

      if (expr.isSelfCall(bf))
      { return true; } 
      
      if (vars1.size() > 0)
      { return false; } // call to bf within an expression

      return true; // no occurrence of bf 
    } 

    if (st instanceof ConditionalStatement) 
    { ConditionalStatement cs = (ConditionalStatement) st; 
    
      if (Statement.isTailRecursive(bf,nme,cs.ifPart()))
      { return Statement.isTailRecursive(
                                 bf,nme,cs.elsePart()); 
      } 
    
      return false; 
    } 

    if (st instanceof WhileStatement) 
    { return false; } 
    // Nested loops cannot be handled within a recursion. 

    if (st instanceof TryStatement) 
    { TryStatement ts = (TryStatement) st; 

      if (Statement.isTailRecursive(bf,nme,ts.getBody())) 
      { Vector stats = ts.getClauses(); 
        for (int i = 0; i < stats.size(); i++) 
        { if (stats.get(i) instanceof Statement)
          { Statement stat = (Statement) stats.get(i); 
            if (Statement.isTailRecursive(bf,nme,stat)) { } 
            else 
            { return false; } 
          }
          else 
          { return false; } 
        }  
      }
      else 
      { return false; }
  
      if (ts.getEndStatement() == null) 
      { return true; } 

      return Statement.isTailRecursive(
                               bf,nme,ts.getEndStatement()); 
    } 

    return true;
  } // Other cases, for all other forms of statement. 

  public static boolean endsWithSelfCall(
            BehaviouralFeature bf, String nme, Statement st)
  { // if every branch ends with a self-call

    if (st == null) 
    { return false; }
 
    if (st instanceof SequenceStatement) 
    { SequenceStatement sq = (SequenceStatement) st; 
      Vector stats = sq.getStatements(); 
      Statement stat = (Statement) stats.get(stats.size()-1); 
      return Statement.endsWithSelfCall(bf,nme,stat);
    } 

    if (st instanceof InvocationStatement)
    { InvocationStatement invok = 
        (InvocationStatement) st;
      
      Expression expr = invok.getCallExp();
 
      if (expr != null && expr.isSelfCall(bf))
      { return true; } 

      return false; 
    } 

    if (st instanceof ReturnStatement)
    { ReturnStatement retstat = (ReturnStatement) st; 
      
      Expression expr = retstat.getReturnValue();
 
      if (expr != null && expr.isSelfCall(bf))
      { return true; } 
    
      return false; 
    } 

    if (st instanceof ConditionalStatement) 
    { ConditionalStatement cs = (ConditionalStatement) st; 
    
      if (Statement.endsWithSelfCall(bf,nme,cs.ifPart()))
      { return Statement.endsWithSelfCall(
                                 bf,nme,cs.elsePart()); 
      } 
    
      return false; 
    } 

    if (st instanceof WhileStatement) 
    { return false; } 
    // Nested loops cannot be handled within a recursion. 

    if (st instanceof TryStatement) 
    { TryStatement ts = (TryStatement) st; 

      if (Statement.endsWithSelfCall(bf,nme,ts.getBody())) 
      { Vector stats = ts.getClauses(); 
        for (int i = 0; i < stats.size(); i++) 
        { if (stats.get(i) instanceof Statement)
          { Statement stat = (Statement) stats.get(i); 
            if (Statement.endsWithSelfCall(bf,nme,stat)) { } 
            else 
            { return false; } 
          }
          else 
          { return false; } 
        }  
      }
      else 
      { return false; }
  
      if (ts.getEndStatement() == null) 
      { return false; } 

      return Statement.endsWithSelfCall(
                               bf,nme,ts.getEndStatement()); 
    } 

    return false;
  } // Other cases, for all other forms of statement. 

  public static boolean endsWithReturn(Statement st)
  { if (st == null) 
    { return false; }
 
    if (st instanceof SequenceStatement) 
    { SequenceStatement sq = (SequenceStatement) st; 
      Vector stats = sq.getStatements();
      if (stats.size() == 0) 
      { return false; } // just a skip 
      Statement stat = (Statement) stats.get(stats.size()-1); 
      return Statement.endsWithReturn(stat);
    } 
    
    if (st instanceof ReturnStatement)
    { return true; } 

    if (st instanceof ConditionalStatement) 
    { ConditionalStatement cs = (ConditionalStatement) st; 
      if (Statement.endsWithReturn(cs.ifPart()))
      { return Statement.endsWithReturn(cs.elsePart()); } 
      return false; 
    } 

    if (st instanceof WhileStatement) 
    { return false; } 
    // Nested loops cannot be handled within a recursion. 

    if (st instanceof TryStatement) 
    { TryStatement ts = (TryStatement) st; 

      if (Statement.endsWithReturn(ts.getBody())) 
      { Vector stats = ts.getClauses(); 
        for (int i = 0; i < stats.size(); i++) 
        { if (stats.get(i) instanceof Statement)
          { Statement stat = (Statement) stats.get(i); 
            if (Statement.endsWithReturn(stat)) { } 
            else 
            { return false; } 
          }
          else 
          { return false; } 
        }  
      }
      else 
      { return false; }  
      if (ts.getEndStatement() == null) { return false; } 
      return Statement.endsWithReturn(ts.getEndStatement()); 
    } 

    return false;
  } // Other cases, for all other forms of statement. 

  public static boolean endsWithContinue(Statement st)
  { if (st == null) 
    { return false; }
 
    if (st instanceof SequenceStatement) 
    { SequenceStatement sq = (SequenceStatement) st; 
      Vector stats = sq.getStatements(); 
      if (stats.size() == 0) 
      { return false; } // just a skip 
      Statement stat = (Statement) stats.get(stats.size()-1); 
      return Statement.endsWithContinue(stat);
    } 
    
    if (st instanceof ContinueStatement)
    { return true; } 

    if (st instanceof ConditionalStatement) 
    { ConditionalStatement cs = (ConditionalStatement) st; 
      if (Statement.endsWithContinue(cs.ifPart()))
      { return Statement.endsWithContinue(cs.elsePart()); } 
      return false; 
    } 

    if (st instanceof WhileStatement) 
    { return false; } 
    // Nested loops cannot be handled within a recursion. 

    if (st instanceof TryStatement) 
    { TryStatement ts = (TryStatement) st; 

      if (Statement.endsWithContinue(ts.getBody())) 
      { Vector stats = ts.getClauses(); 
        for (int i = 0; i < stats.size(); i++) 
        { if (stats.get(i) instanceof Statement)
          { Statement stat = (Statement) stats.get(i); 
            if (Statement.endsWithContinue(stat)) { } 
            else 
            { return false; } 
          }
          else 
          { return false; } 
        }  
      }
      else 
      { return false; }  
      if (ts.getEndStatement() == null) { return false; } 
      return Statement.endsWithContinue(ts.getEndStatement()); 
    } 

    return false;
  } // Other cases, for all other forms of statement. 

  public static boolean endsWithBreak(Statement st)
  { if (st == null) 
    { return false; }
 
    if (st instanceof SequenceStatement) 
    { SequenceStatement sq = (SequenceStatement) st; 
      Vector stats = sq.getStatements(); 
      if (stats.size() == 0) 
      { return false; } // just a skip 
      Statement stat = (Statement) stats.get(stats.size()-1); 
      return Statement.endsWithBreak(stat);
    } 
    
    if (st instanceof BreakStatement)
    { return true; } 

    if (st instanceof ConditionalStatement) 
    { ConditionalStatement cs = (ConditionalStatement) st; 
      if (Statement.endsWithBreak(cs.ifPart()))
      { return Statement.endsWithBreak(cs.elsePart()); } 
      return false; 
    } 

    if (st instanceof WhileStatement) 
    { return false; } 
    // Nested loops cannot be handled within a recursion. 

    if (st instanceof TryStatement) 
    { TryStatement ts = (TryStatement) st; 

      if (Statement.endsWithBreak(ts.getBody())) 
      { Vector stats = ts.getClauses(); 
        for (int i = 0; i < stats.size(); i++) 
        { if (stats.get(i) instanceof Statement)
          { Statement stat = (Statement) stats.get(i); 
            if (Statement.endsWithBreak(stat)) { } 
            else 
            { return false; } 
          }
          else 
          { return false; } 
        }  
      }
      else 
      { return false; }  
      if (ts.getEndStatement() == null) { return false; } 
      return Statement.endsWithBreak(ts.getEndStatement()); 
    } 

    return false;
  } // Other cases, for all other forms of statement. 

  public static boolean endsWithExit(Statement st)
  { if (st == null) 
    { return false; }
 
    if (st instanceof SequenceStatement) 
    { SequenceStatement sq = (SequenceStatement) st; 
      Vector stats = sq.getStatements(); 
      if (stats.size() == 0) 
      { return false; } // just a skip 
      Statement stat = (Statement) stats.get(stats.size()-1); 
      return Statement.endsWithExit(stat);
    } 
    
    if (st instanceof InvocationStatement)
    { String called = 
        ((InvocationStatement) st).calledOperation(); 
      if ("exit".equals(called))
      { return true; }
      return false; 
    }  // OclProcess.exit(n)

    if (st instanceof ConditionalStatement) 
    { ConditionalStatement cs = (ConditionalStatement) st; 
      if (Statement.endsWithExit(cs.ifPart()))
      { return Statement.endsWithExit(cs.elsePart()); } 
      return false; 
    } 

    if (st instanceof WhileStatement) 
    { return false; } 
    // Nested loops cannot be handled within a recursion. 

    if (st instanceof TryStatement) 
    { TryStatement ts = (TryStatement) st; 

      if (Statement.endsWithExit(ts.getBody())) 
      { Vector stats = ts.getClauses(); 
        for (int i = 0; i < stats.size(); i++) 
        { if (stats.get(i) instanceof Statement)
          { Statement stat = (Statement) stats.get(i); 
            if (Statement.endsWithExit(stat)) { } 
            else 
            { return false; } 
          }
          else 
          { return false; } 
        }  
      }
      else 
      { return false; }  
      if (ts.getEndStatement() == null) { return false; } 
      return Statement.endsWithExit(ts.getEndStatement()); 
    } 

    return false;
  } // Other cases, for all other forms of statement. 

  public static boolean endsWithControlFlowBreak(Statement stat)
  { if (Statement.endsWithReturn(stat))
    { return true; } 

    if (Statement.endsWithBreak(stat))
    { return true; } 

    if (Statement.endsWithContinue(stat))
    { return true; } 

    return Statement.endsWithExit(stat); 
  } 

  public static boolean isControlFlowEnd(Statement stat)
  { if (Statement.hasSingleStatement(stat)) { } 
    else 
    { return false; } 

    return Statement.endsWithControlFlowBreak(stat); 
  } 

  public static Statement replaceReturnBySkip(Statement st)
  { if (st == null) 
    { return st; }
 
    if (st instanceof SequenceStatement) 
    { SequenceStatement sq = (SequenceStatement) st; 
      Vector newstats = new Vector(); 
      Vector stats = sq.getStatements(); 
      for (int i = 0; i < stats.size(); i++) 
      { if (stats.get(i) instanceof Statement)
        { Statement stat = (Statement) stats.get(i); 
          Statement newstat = 
            Statement.replaceReturnBySkip(stat);
          newstats.add(newstat); 
        }  
      } 

      SequenceStatement newsq = 
            new SequenceStatement(newstats);
      newsq.setBrackets(sq.hasBrackets());  
      return newsq; 
    } 
    
    if (st instanceof ReturnStatement)
    { return new InvocationStatement("skip"); } 

    if (st instanceof ConditionalStatement) 
    { ConditionalStatement cs = (ConditionalStatement) st; 
      Statement newif = 
         Statement.replaceReturnBySkip(cs.ifPart()); 
      Statement newelse = 
         Statement.replaceReturnBySkip(cs.elsePart());
      ConditionalStatement res = 
         new ConditionalStatement(cs.getTest(), 
                                  newif, newelse);  
      return res; 
    } 

    if (st instanceof WhileStatement) 
    { WhileStatement ws = (WhileStatement) st; 
      Statement newbody = 
         Statement.replaceReturnBySkip(ws.getLoopBody());
      WhileStatement wsnew = 
        new WhileStatement(ws.getTest(), newbody); 
      wsnew.loopKind = ws.loopKind;  
      wsnew.loopVar = ws.loopVar;
      wsnew.loopRange = ws.loopRange;
  
      return wsnew; 
    } 

    if (st instanceof TryStatement) 
    { TryStatement ts = (TryStatement) st; 
      Statement newbody = 
         Statement.replaceReturnBySkip(ts.getBody());
      Vector newclauses = new Vector();  
      Vector stats = ts.getClauses(); 
      for (int i = 0; i < stats.size(); i++) 
      { if (stats.get(i) instanceof Statement)
        { Statement stat = (Statement) stats.get(i); 
          Statement newstat = 
             Statement.replaceReturnBySkip(stat);
          newclauses.add(newstat); 
        }  
      } 
      
      Statement newend = 
         Statement.replaceReturnBySkip(ts.getEndStatement()); 
      TryStatement newtry = 
         new TryStatement(newbody, newclauses, newend); 
      return newtry; 
    } 

    return st;
  } // Other cases, for all other forms of statement. 

  public static Statement replaceElseBySequence(Statement st)
  { if (st == null) 
    { return st; }
 
    if (st instanceof SequenceStatement) 
    { SequenceStatement sq = (SequenceStatement) st; 
      Vector newstats = new Vector(); 
      Vector stats = sq.getStatements(); 
      for (int i = 0; i < stats.size(); i++) 
      { if (stats.get(i) instanceof Statement)
        { Statement stat = (Statement) stats.get(i); 
          Statement newstat = 
            Statement.replaceElseBySequence(stat);
          newstats.add(newstat); 
        }  
      } 

      SequenceStatement newsq = 
            new SequenceStatement(newstats);
      newsq.setBrackets(sq.hasBrackets());  
      return newsq; 
    } 
    
    if (st instanceof ReturnStatement)
    { return st; } 

    if (st instanceof ConditionalStatement) 
    { ConditionalStatement cs = (ConditionalStatement) st; 
      Statement newif = 
         Statement.replaceElseBySequence(cs.ifPart()); 
      Statement newelse = 
         Statement.replaceElseBySequence(cs.elsePart());
      if (Statement.endsWithReturn(newif))
      { ConditionalStatement res = 
          new ConditionalStatement(cs.getTest(), 
                newif, 
                new InvocationStatement("skip"));
        SequenceStatement ss = new SequenceStatement(); 
        ss.addStatement(res); 
        ss.addStatement(newelse); 
        return ss; 
      } 
      else 
      { ConditionalStatement res = 
          new ConditionalStatement(cs.getTest(), 
                newif, 
                newelse);
  
        return res;
      } 
    } 

    if (st instanceof WhileStatement) 
    { WhileStatement ws = (WhileStatement) st; 
      Statement newbody = 
         Statement.replaceElseBySequence(ws.getLoopBody());
      WhileStatement wsnew = 
        new WhileStatement(ws.getTest(), newbody); 
      wsnew.loopKind = ws.loopKind;  
      wsnew.loopVar = ws.loopVar;
      wsnew.loopRange = ws.loopRange;
  
      return wsnew; 
    } 

    if (st instanceof TryStatement) 
    { TryStatement ts = (TryStatement) st; 
      Statement newbody = 
         Statement.replaceElseBySequence(ts.getBody());
      Vector newclauses = new Vector();  
      Vector stats = ts.getClauses(); 
      for (int i = 0; i < stats.size(); i++) 
      { if (stats.get(i) instanceof Statement)
        { Statement stat = (Statement) stats.get(i); 
          Statement newstat = 
             Statement.replaceElseBySequence(stat);
          newclauses.add(newstat); 
        }  
      } 
      
      Statement newend = 
         Statement.replaceElseBySequence(
                             ts.getEndStatement()); 
      TryStatement newtry = 
         new TryStatement(newbody, newclauses, newend); 
      return newtry; 
    } 

    return st;
  } // Other cases, for all other forms of statement. 

  public static Statement tryInsertCloneDeclaration(
                         Statement st,
                         Expression expr, Type typ, Type et)
  { // The declaration of vname must immediately precede 
    // first use of expr. 
    // st : SequenceStatement.

    if (st == null || expr == null) 
    { return st; }

    if (typ == null) 
    { typ = new Type("OclAny", null); } 

    String vname = 
        Identifier.nextIdentifier("factored_expr");

    CreationStatement dec = 
      CreationStatement.newCreationStatement(vname,typ,expr);
    dec.setElementType(et); 

    BasicExpression var = 
      BasicExpression.newVariableBasicExpression(vname,typ); 

    if (st instanceof SequenceStatement) 
    { SequenceStatement sq = (SequenceStatement) st; 
      Vector stats = sq.flattenSequenceStatement(); 

      Vector precedingStats = new Vector(); 

      for (int i = 0; i < stats.size(); i++) 
      { Statement stat = (Statement) stats.get(i); 
          
        if (stat.containsSubexpression(expr))
        { 
          precedingStats.add(dec);
             
          for (int j = i; j < stats.size(); j++) 
          { Statement ss = 
                 (Statement) stats.get(j); 
            Statement newstat = 
                        ss.substituteEq(expr + "", var); 

            precedingStats.add(newstat);
          }  

          SequenceStatement remStat = 
              new SequenceStatement(precedingStats);

          return remStat;  
        } // insert creation statement before remainingStats
        else 
        { precedingStats.add(stat); }   
      } 

      return st; 
    } 
    
    /* for (int j = 0; j < rdfr.size(); j++) 
    { String rv = rdfr.get(j) + ""; 
      if (wrfr.contains(rv)) 
      { return null; } 
    } */ 


    Statement newstat = st.substituteEq(expr + "", var); 

    Vector newstats = new Vector(); 
    newstats.add(dec); 
    newstats.add(newstat);
    return new SequenceStatement(newstats); 
  }  

  public static Vector getLocalDeclarations(Statement st)
  { // Local declarations for "hoist declarations" 

    Vector res = new Vector(); 
    if (st == null) 
    { return res; }
 
    if (st instanceof SequenceStatement) 
    { SequenceStatement sq = (SequenceStatement) st; 
      Vector stats = sq.getStatements(); 
      for (int i = 0; i < stats.size(); i++) 
      { if (stats.get(i) instanceof Statement)
        { Statement stat = (Statement) stats.get(i); 
          res.addAll(Statement.getLocalDeclarations(stat));
        }  
      } 
      return res;
    } 
    
    if (st instanceof CreationStatement)
    { res.add(st); 
      return res; 
    } 

    if (st instanceof ConditionalStatement) 
    { ConditionalStatement cs = (ConditionalStatement) st; 
      res.addAll(getLocalDeclarations(cs.ifPart())); 
      res.addAll(getLocalDeclarations(cs.elsePart())); 
      return res; 
    } 

    if (st instanceof WhileStatement) 
    { WhileStatement ws = (WhileStatement) st; 
      res.addAll(getLocalDeclarations(ws.getLoopBody())); 
      return res; 
    } 

    if (st instanceof TryStatement) 
    { TryStatement ts = (TryStatement) st; 
      res.addAll(getLocalDeclarations(ts.getBody())); 
      Vector stats = ts.getClauses(); 
      for (int i = 0; i < stats.size(); i++) 
      { if (stats.get(i) instanceof Statement)
        { Statement stat = (Statement) stats.get(i); 
          res.addAll(getLocalDeclarations(stat));
        }  
      } 
      res.addAll(getLocalDeclarations(ts.getEndStatement())); 
    } 

    return res;
  } // Other cases, for all other forms of statement. 

  public static Vector getBreaksContinues(Statement st)
  { Vector res = new Vector(); 
    if (st == null) 
    { return res; }
 
    if (st instanceof SequenceStatement) 
    { SequenceStatement sq = (SequenceStatement) st; 
      Vector stats = sq.getStatements(); 
      for (int i = 0; i < stats.size(); i++) 
      { if (stats.get(i) instanceof Statement)
        { Statement stat = (Statement) stats.get(i); 
          res.addAll(Statement.getBreaksContinues(stat));
        }  
      } 
      return res;
    } 
    
    if (st instanceof ContinueStatement)
    { res.add(st); 
      return res; 
    } 

    if (st instanceof BreakStatement)
    { res.add(st); 
      return res; 
    } 

    if (st instanceof ConditionalStatement) 
    { ConditionalStatement cs = (ConditionalStatement) st; 
      res.addAll(getBreaksContinues(cs.ifPart())); 
      res.addAll(getBreaksContinues(cs.elsePart())); 
      return res; 
    } 

    if (st instanceof WhileStatement) 
    { WhileStatement ws = (WhileStatement) st; 
      res.addAll(getBreaksContinues(ws.getLoopBody())); 
      return res; 
    } 

    if (st instanceof TryStatement) 
    { TryStatement ts = (TryStatement) st; 
      res.addAll(getBreaksContinues(ts.getBody())); 
      Vector stats = ts.getClauses(); 
      for (int i = 0; i < stats.size(); i++) 
      { if (stats.get(i) instanceof Statement)
        { Statement stat = (Statement) stats.get(i); 
          res.addAll(getBreaksContinues(stat));
        }  
      } 
      res.addAll(getBreaksContinues(ts.getEndStatement())); 
    } 

    return res;
  } // Other cases, for all other forms of statement. 

  public static Vector getAssignments(Statement st)
  { Vector res = new Vector(); 
    if (st == null) 
    { return res; }
 
    if (st instanceof SequenceStatement) 
    { SequenceStatement sq = (SequenceStatement) st; 
      Vector stats = sq.getStatements(); 
      for (int i = 0; i < stats.size(); i++) 
      { if (stats.get(i) instanceof AssignStatement)
        { res.add(stats.get(i)); } 
        else if (stats.get(i) instanceof SequenceStatement)
        { Statement stat = (Statement) stats.get(i); 
          res.addAll(Statement.getAssignments(stat));
        }  
      } 
      return res;
    } 
    
    if (st instanceof ConditionalStatement) 
    { ConditionalStatement cs = (ConditionalStatement) st; 
      res.addAll(getAssignments(cs.ifPart())); 
      res.addAll(getAssignments(cs.elsePart())); 
      return res; 
    } 

    if (st instanceof WhileStatement) 
    { WhileStatement ws = (WhileStatement) st; 
      res.addAll(getAssignments(ws.getLoopBody())); 
      return res; 
    } 

    if (st instanceof TryStatement) 
    { TryStatement ts = (TryStatement) st; 
      res.addAll(getAssignments(ts.getBody())); 
      Vector stats = ts.getClauses(); 
      for (int i = 0; i < stats.size(); i++) 
      { if (stats.get(i) instanceof Statement)
        { Statement stat = (Statement) stats.get(i); 
          res.addAll(getAssignments(stat));
        }  
      } 
      res.addAll(getAssignments(ts.getEndStatement())); 
    } 

    return res;
  } // Other cases, for all other forms of statement. 

  public static Vector getOperationCalls(Statement st)
  { Vector res = new Vector(); 
    if (st == null) 
    { return res; }

    // System.out.println(">> Operation calls in " + st); 

    if (st instanceof InvocationStatement)
    { if ("skip".equals(st + "")) { } 
      else 
      { res.add(st); }  
      return res; 
    } 

    // if (st instanceof ReturnStatement) 
    // { 
 
    if (st instanceof SequenceStatement) 
    { SequenceStatement sq = (SequenceStatement) st; 
      Vector stats = sq.getStatements(); 
      for (int i = 0; i < stats.size(); i++) 
      { Statement ss = (Statement) stats.get(i); 
        res.addAll(Statement.getOperationCalls(ss)); 

        /* if (stats.get(i) instanceof InvocationStatement)
        { res.add(stats.get(i)); } 
        else if (stats.get(i) instanceof SequenceStatement)
        { Statement stat = (Statement) stats.get(i); 
          res.addAll(Statement.getOperationCalls(stat));
        }  */ 
      } 
      return res;
    } 
    
    if (st instanceof ConditionalStatement) 
    { ConditionalStatement cs = (ConditionalStatement) st; 
      res.addAll(getOperationCalls(cs.ifPart())); 
      res.addAll(getOperationCalls(cs.elsePart())); 
      return res; 
    } 

    if (st instanceof IfStatement) 
    { IfStatement cs = (IfStatement) st; 
      System.err.println("! Warning: do not use IfStatement"); 
      Statement ifpart = cs.getIfPart(); 
      if (ifpart != null) 
      { res.addAll(getOperationCalls(ifpart)); } 
      Statement elsepart = cs.getElsePart(); 
      if (elsepart != null) 
      { res.addAll(getOperationCalls(elsepart)); }  
      return res; 
    } 

    if (st instanceof WhileStatement) 
    { WhileStatement ws = (WhileStatement) st; 
      res.addAll(getOperationCalls(ws.getLoopBody())); 
      return res; 
    } 

    if (st instanceof TryStatement) 
    { TryStatement ts = (TryStatement) st; 
      res.addAll(getOperationCalls(ts.getBody())); 
      Vector stats = ts.getClauses(); 
      for (int i = 0; i < stats.size(); i++) 
      { if (stats.get(i) instanceof Statement)
        { Statement stat = (Statement) stats.get(i); 
          res.addAll(getOperationCalls(stat));
        }  
      } 
      res.addAll(getOperationCalls(ts.getEndStatement())); 
    } 

    return res;
  } // Other cases, for all other forms of statement. 

  public static Vector getOperationCallsContexts(
      String nme, Statement st, 
      Vector contexts, Vector remainders)
  { // Each context is preceding/enclosing code of each call
    // Each remainder is the code after call. 

    Vector res = new Vector(); 
    if (st == null) 
    { return res; }

    if (st instanceof InvocationStatement)
    { InvocationStatement invok = 
        (InvocationStatement) st; 
      Expression expr = invok.getCallExp();
 
      // if ((expr + "").startsWith("self." + nme + "("))
      if (expr != null && expr.isSelfCall(nme))
      { res.add(st);
        Vector thiscall = new Vector(); 
        thiscall.add(st); 
        contexts.add(thiscall);  
        remainders.add(new Vector()); 
      }
      return res; 
    } 

    if (st instanceof ReturnStatement)
    { ReturnStatement retstat = (ReturnStatement) st; 
      Expression expr = retstat.getReturnValue();
 
      // if ((expr + "").startsWith("self." + nme + "("))
      if (expr != null && expr.isSelfCall(nme))
      { res.add(st);
        Vector thiscall = new Vector(); 
        thiscall.add(st); 
        contexts.add(thiscall);  
        remainders.add(new Vector()); 
      }
      return res; 
    } 
 
    if (st instanceof SequenceStatement) 
    { SequenceStatement sq = (SequenceStatement) st; 
      Vector stats = sq.getStatements();
 
      Vector precedingStats = new Vector(); 
      for (int i = 0; i < stats.size(); i++) 
      { Statement ss = (Statement) stats.get(i); 
        
        Vector newcontexts = new Vector(); 
        Vector newrems = new Vector(); 
        Vector calls = Statement.getOperationCallsContexts(
                             nme,ss,newcontexts,newrems);
        for (int j = 0; j < calls.size(); j++) 
        { Vector context = (Vector) newcontexts.get(j); 
          Vector newcontext = new Vector(); 
          newcontext.addAll(precedingStats); 
          newcontext.addAll(context); 
          newcontexts.set(j,newcontext); 

          Vector rem = (Vector) newrems.get(j);           
          Vector remainder = new Vector(); 
          remainder.addAll(rem); 
          for (int k = i+1; k < stats.size(); k++) 
          { remainder.add(stats.get(k)); } 
          newrems.set(j,remainder); 
        }  
        res.addAll(calls);
        contexts.addAll(newcontexts); 
        remainders.addAll(newrems); 
         

        /* if (stats.get(i) instanceof InvocationStatement)
        { res.add(stats.get(i)); } 
        else if (stats.get(i) instanceof SequenceStatement)
        { Statement stat = (Statement) stats.get(i); 
          res.addAll(Statement.getOperationCalls(stat));
        }  */ 

        precedingStats.add(ss); 
      } 
      return res;
    } 
    
    if (st instanceof ConditionalStatement) 
    { ConditionalStatement cs = (ConditionalStatement) st;
      Expression tst = cs.getTest(); 

      Vector ctxs1 = new Vector();  
      Vector rems1 = new Vector();  
      Vector calls1 = 
         getOperationCallsContexts(
                            nme,cs.ifPart(),ctxs1,rems1);
 
      Vector ctxs2 = new Vector();  
      Vector rems2 = new Vector();  
      Vector calls2 = 
         getOperationCallsContexts(
                         nme,cs.elsePart(),ctxs2,rems2); 

      if (calls1.size() > 0 && 
          calls2.size() > 0) 
      { // calls in both branches
        Vector ifelsecall = new Vector(); 
        ifelsecall.add("ifelse"); 
        ifelsecall.add(tst); 
        ifelsecall.add(ctxs1); 
        ifelsecall.add(ctxs2); 

        res.add(calls1.get(0));
        res.add(calls2.get(0)); 

        Vector sts0 = new Vector(); 
        sts0.add(ifelsecall); 
        contexts.add(sts0); 
        contexts.add(sts0); 
        remainders.add(new Vector()); 
        remainders.add(new Vector()); 
      } // no remainders if both branches are covered
      else if (calls1.size() > 0) // Only expect 1 at most
      { res.add(calls1.get(0));

        Vector ifcall = new Vector(); 
        ifcall.add("if");
        ifcall.add(tst); 
        ifcall.add(ctxs1); 
        ifcall.add(cs.elsePart()); 

        Vector sts1 = new Vector(); 
        sts1.add(ifcall); 
        contexts.add(sts1); 
        remainders.add(rems1.get(0)); 
      } 
      else if (calls2.size() > 0) 
      { res.add(calls2.get(0)); 

        Vector elsecall = new Vector(); 
        elsecall.add("else");
        elsecall.add(tst); 
        elsecall.add(cs.ifPart()); 
        elsecall.add(ctxs2); 

        Vector sts2 = new Vector(); 
        sts2.add(elsecall); 
        contexts.add(sts2); 
        remainders.add(rems2.get(0)); 
      } 

      return res; 
    } 

    if (st instanceof TryStatement) 
    { TryStatement ts = (TryStatement) st; 
      Vector ctxs1 = new Vector();  
      Vector rems1 = new Vector();  
      Vector calls1 = 
         getOperationCallsContexts(
                            nme,ts.getBody(),ctxs1,rems1);
      res.addAll(calls1);
      contexts.addAll(ctxs1); 
      remainders.addAll(rems1); 
   
      Vector stats = ts.getClauses(); 
      for (int i = 0; i < stats.size(); i++) 
      { if (stats.get(i) instanceof Statement)
        { Statement stat = (Statement) stats.get(i); 
          Vector ctxs2 = new Vector();  
          Vector rems2 = new Vector();  
          Vector calls2 = 
            getOperationCallsContexts(
                            nme,stat,ctxs2,rems2);
          res.addAll(calls2);
          contexts.addAll(ctxs2); 
          remainders.addAll(rems2); 
        }  
      }
      Vector ctxs3 = new Vector();  
      Vector rems3 = new Vector();  
      Vector calls3 = 
         getOperationCallsContexts(
               nme,
               ts.getEndStatement(),ctxs3,rems3);
      res.addAll(calls3);
      contexts.addAll(ctxs3); 
      remainders.addAll(rems3); 
    } 

    return res;
  } // Other cases, for all other forms of statement. 

  public static Statement replaceSelfCallByContinue(String nme, Vector branch, Statement asgns)
  { // sequence statement of branch elements except 
    // self.nme() replaced by continue

    // System.out.println("+++ REPLACING: " + branch);

    Vector vect = new Vector(); 
    if (branch.get(0) instanceof Vector)
    { vect = (Vector) branch.get(0);

      if (vect.get(0) instanceof Vector)
      { vect = (Vector) vect.get(0); 
        // System.out.println("+++ REPLACING code: " + vect);

        if (vect.size() == 4 && 
            "if".equals(vect.get(0) + ""))      
        { Expression tst = (Expression) vect.get(1);
          Vector sts = (Vector) vect.get(2); 
          Statement cde = 
            Statement.replaceSelfCallByContinue(
                                      nme,sts,asgns);
          Statement elsePart = (Statement) vect.get(3); 
  
          Statement newelse = 
            SequenceStatement.combineSequenceStatements(
                            elsePart,new BreakStatement()); 
          ConditionalStatement cs = 
            new ConditionalStatement(tst,
                cde, 
                newelse);
          SequenceStatement res = new SequenceStatement(); 
          res.addStatement(cs); 
          return res;
        } 
      } 
    }  
    else 
    { vect.addAll(branch); } 

    SequenceStatement res = new SequenceStatement(); 
    int blen = vect.size(); 

    for (int i = 0; i < blen-1; i++) 
    { Statement st = (Statement) vect.get(i); 
      res.addStatement(st); 
    } 

    if (asgns != null && 
        asgns instanceof SequenceStatement)
    { res.addStatements((SequenceStatement) asgns); } 
    res.addStatement(new ContinueStatement()); 

    return res;
  } // Other cases, for all other forms of statement. 

  public static Statement replaceSelfCallsByContinue(
           BehaviouralFeature bf, String nme, Statement st)
  { // self.nme(exprs) replaced by pars := exprs; continue
    // Likewise for return self.nme(exprs)
    // Any branch that does not terminate in nme call/return
    // or exit/return is ended by break. 

    if (st == null) 
    { return st; }

    if (st instanceof InvocationStatement)
    { InvocationStatement invok = 
        (InvocationStatement) st;
      
      Expression expr = invok.getCallExp();
 
      // if ((expr + "").startsWith("self." + nme + "("))
      if (expr != null && expr.isSelfCall(bf))
      { Statement res = new ContinueStatement();  
        Statement passigns = 
             bf.parameterAssignments(expr);

        if (passigns == null) 
        { return res; } 
        else if (passigns instanceof SequenceStatement)
        { ((SequenceStatement) passigns).addStatement(res); 
          return passigns; 
        } // passigns.setBrackets(true) 
      }
      return st; 
    } 

    if (st instanceof ReturnStatement)
    { ReturnStatement retstat = (ReturnStatement) st; 
      
      Expression expr = retstat.getReturnValue();
 
      // if ((expr + "").startsWith("self." + nme + "("))
      if (expr != null && expr.isSelfCall(bf))
      { Statement res = new ContinueStatement();  
        Statement passigns = 
             bf.parameterAssignments(expr);
        
        if (passigns == null) 
        { return res; } 
        else if (passigns instanceof SequenceStatement)
        { ((SequenceStatement) passigns).addStatement(res); 
          return passigns; 
        } 
      }
      return st; 
    } 
 
    if (st instanceof SequenceStatement) 
    { SequenceStatement sq = (SequenceStatement) st; 
      Vector stats = sq.getStatements();
      Vector res = new Vector(); 
 
      for (int i = 0; i < stats.size(); i++) 
      { Statement ss = (Statement) stats.get(i); 
        Statement newstat = 
            Statement.replaceSelfCallsByContinue(bf,nme,ss); 
        if (newstat != null) 
        { res.add(newstat); } 
      } 

      // if res does not end with return, continue or exit, 
      // add a break statement: 

      SequenceStatement newss = new SequenceStatement(res);

      if (Statement.endsWithReturn(newss) || 
          Statement.endsWithContinue(newss) ||
          Statement.endsWithSelfCall(bf,nme,st) ||  
          Statement.endsWithBreak(newss) || 
          Statement.endsWithExit(newss)) { } 
      else 
      { BreakStatement bs = new BreakStatement(); 
        newss.addStatement(bs); 
      } 

      return newss;
    } 
    
    if (st instanceof ConditionalStatement) 
    { ConditionalStatement cs = (ConditionalStatement) st;
      Expression tst = cs.getTest(); 

      Statement ifstat = cs.ifPart();
      Statement elsestat = cs.elsePart(); 
      
      Statement newif = 
        Statement.replaceSelfCallsByContinue(bf,nme,ifstat); 
      Statement newelse = 
        Statement.replaceSelfCallsByContinue(bf,nme,elsestat); 

      return new ConditionalStatement(tst,newif,newelse); 
    } 

    if (st instanceof TryStatement) 
    { TryStatement ts = (TryStatement) st; 
      Statement bdy = ts.getBody(); 
      Statement newbdy = 
        Statement.replaceSelfCallsByContinue(bf,nme,bdy);
   
      Vector stats = ts.getClauses(); 
      Vector newstats = new Vector();
 
      for (int i = 0; i < stats.size(); i++) 
      { if (stats.get(i) instanceof Statement)
        { Statement stat = (Statement) stats.get(i); 
          Statement newstat = 
            Statement.replaceSelfCallsByContinue(
                                        bf,nme,stat);
          newstats.add(newstat); 
        }  
      }

      Statement endstat = ts.getEndStatement(); 
      Statement newend =
        Statement.replaceSelfCallsByContinue(
               bf, nme, endstat); 
      Statement newtry = 
        new TryStatement(newbdy, newstats, newend); 
      return newtry; 
    } 

    return st;
  } // Other cases, for all other forms of statement. 

  public static Statement replaceSemiTailCallsByContinue(
           BehaviouralFeature bf, String nme, Statement st, 
           Vector inits)
  { Vector names = new Vector(); 
    names.add(nme); 

    BasicExpression _result = 
      BasicExpression.newVariableBasicExpression("_result"); 
    _result.setType(bf.getResultType()); 

    Vector rets = getReturnValues(st); 
    
    Vector nonrecReturns = new Vector(); 
    Vector semitailReturns = new Vector(); 

    for (int i = 0; i < rets.size(); i++) 
    { Expression expr = (Expression) rets.get(i); 
      Vector uses = expr.variablesUsedIn(names); 
      if (uses.size() == 0) 
      { nonrecReturns.add(expr); } 
      else if (expr.isSelfCall(bf))
      { } 
      else if (expr instanceof BinaryExpression && 
        ((BinaryExpression) expr).isSemiTailRecursion(bf)) 
      { semitailReturns.add(expr); } 
      else 
      { } 
    } 

    Expression baseValue = (Expression) nonrecReturns.get(0); 
    CreationStatement init = 
       new CreationStatement(_result, baseValue); 
    inits.add(init); 

    return Statement.replaceSelfCallsByContinue(bf, nme, st,
              _result, nonrecReturns, semitailReturns); 
  } 

  public static Statement replaceSelfCallsByContinue(
           BehaviouralFeature bf, String nme, Statement st,
           BasicExpression _result, 
           Vector nonrecReturns, Vector semitailReturns)
  { // self.nme(exprs) replaced by pars := exprs; continue
    // Likewise for tail-recursive return self.nme(exprs)
    // Non-recursive return replaced by return _result
    // Non-tail return expr*self.nme(exprs) replaced by
    // _result := expr*_result; pars := exprs; continue
    // 
    // Any branch that does not terminate in nme call/return
    // or exit/return is ended by break. 

    if (st == null) 
    { return st; }

    if (st instanceof InvocationStatement)
    { InvocationStatement invok = 
        (InvocationStatement) st;
      
      Expression expr = invok.getCallExp();
 
      // if ((expr + "").startsWith("self." + nme + "("))
      if (expr != null && expr.isSelfCall(bf))
      { Statement res = new ContinueStatement();  
        Statement passigns = 
             bf.parameterAssignments(expr);

        if (passigns == null) 
        { return res; } 
        else if (passigns instanceof SequenceStatement)
        { ((SequenceStatement) passigns).addStatement(res); 
          return passigns; 
        } // passigns.setBrackets(true) 
      }
      return st; 
    } 

    if (st instanceof ReturnStatement)
    { ReturnStatement retstat = (ReturnStatement) st; 
      
      Expression expr = retstat.getReturnValue();

      // System.out.println(">> Return expression " + expr + 
      //             " Semi-tail returns: " + semitailReturns); 
 
      if (nonrecReturns.contains(expr))
      { Statement newret = new ReturnStatement(_result); 
        return newret; 
      } 

      if (semitailReturns.contains(expr) && 
          expr instanceof BinaryExpression)
      { // replace call by _result, assign to _result
        BinaryExpression bexpr = 
              (BinaryExpression) expr; 

        Expression newexpr = 
           bexpr.replacedSemiTailRecursion(bf, _result); 
        Expression selfcall = bexpr.getSelfCall(bf); 

        if (newexpr != null && selfcall != null)
        { Statement res = new ContinueStatement(); 
          AssignStatement assgn = 
            new AssignStatement(_result, newexpr); 
          SequenceStatement ss = new SequenceStatement(); 
          ss.addStatement(assgn);  
          Statement passigns = 
             bf.parameterAssignments(selfcall);
          ss.addStatements(passigns); 
          ss.addStatement(res); 
          return ss; 
        } 
      } 

      if (expr != null && expr.isSelfCall(bf))
      { Statement res = new ContinueStatement();  
        Statement passigns = 
             bf.parameterAssignments(expr);
        
        if (passigns == null) 
        { return res; } 
        else if (passigns instanceof SequenceStatement)
        { ((SequenceStatement) passigns).addStatement(res); 
          return passigns; 
        } 
      }

      return st; 
    } 
 
    if (st instanceof SequenceStatement) 
    { SequenceStatement sq = (SequenceStatement) st; 
      Vector stats = sq.getStatements();
      Vector res = new Vector(); 
 
      for (int i = 0; i < stats.size(); i++) 
      { Statement ss = (Statement) stats.get(i); 
        Statement newstat = 
            Statement.replaceSelfCallsByContinue(bf,nme,ss,
                        _result, nonrecReturns, semitailReturns); 

        if (newstat != null) 
        { res.add(newstat); } 
      } 

      // if res does not end with return, continue or exit, 
      // add a break statement: 

      SequenceStatement newss = new SequenceStatement(res);

      if (Statement.endsWithReturn(newss) || 
          Statement.endsWithContinue(newss) ||
          Statement.endsWithSelfCall(bf,nme,st) ||  
          Statement.endsWithBreak(newss) || 
          Statement.endsWithExit(newss)) { } 
      else 
      { BreakStatement bs = new BreakStatement(); 
        newss.addStatement(bs); 
      } 

      return newss;
    } 
    
    if (st instanceof ConditionalStatement) 
    { ConditionalStatement cs = (ConditionalStatement) st;
      Expression tst = cs.getTest(); 

      Statement ifstat = cs.ifPart();
      Statement elsestat = cs.elsePart(); 
      
      Statement newif = 
        Statement.replaceSelfCallsByContinue(bf,nme,ifstat,
                     _result, nonrecReturns, semitailReturns); 
      Statement newelse = 
        Statement.replaceSelfCallsByContinue(bf,nme,elsestat,
                     _result, nonrecReturns, semitailReturns); 

      return new ConditionalStatement(tst,newif,newelse); 
    } 

    if (st instanceof TryStatement) 
    { TryStatement ts = (TryStatement) st; 
      Statement bdy = ts.getBody(); 
      Statement newbdy = 
        Statement.replaceSelfCallsByContinue(bf,nme,bdy, 
                       _result, nonrecReturns, semitailReturns);
   
      Vector stats = ts.getClauses(); 
      Vector newstats = new Vector();
 
      for (int i = 0; i < stats.size(); i++) 
      { if (stats.get(i) instanceof Statement)
        { Statement stat = (Statement) stats.get(i); 
          Statement newstat = 
            Statement.replaceSelfCallsByContinue(
                               bf,nme,stat, _result, 
                               nonrecReturns, semitailReturns);
          newstats.add(newstat); 
        }  
      }

      Statement endstat = ts.getEndStatement(); 
      Statement newend =
        Statement.replaceSelfCallsByContinue(
            bf, nme, endstat, _result, 
            nonrecReturns, semitailReturns); 
      Statement newtry = 
        new TryStatement(newbdy, newstats, newend); 
      return newtry; 
    } 

    return st;
  } // Other cases, for all other forms of statement. 

  public static Statement replaceLocalDeclarations(Statement st, Vector vars)
  { // replace each var v : T := e statement in st by v := e
    // if v : vars

    if (st == null) 
    { return st; }
 
    if (st instanceof SequenceStatement) 
    { SequenceStatement sq = (SequenceStatement) st; 
      Vector stats = sq.getStatements(); 

      Vector res = new Vector(); 
      for (int i = 0; i < stats.size(); i++) 
      { if (stats.get(i) instanceof Statement)
        { Statement stat = (Statement) stats.get(i); 
          res.add(
            Statement.replaceLocalDeclarations(stat,vars));
        }  
      } 
      return new SequenceStatement(res);
    } 
    
    if (st instanceof CreationStatement)
    { CreationStatement cs = (CreationStatement) st; 
      String lhs = cs.assignsTo;
      if (vars.contains(lhs) && 
          cs.initialExpression != null)
      { Expression lhsexpr = new BasicExpression(lhs); 
        lhsexpr.type = cs.getType(); 
        lhsexpr.elementType = cs.getElementType();  
        AssignStatement newst = 
          new AssignStatement(lhsexpr,
                              cs.initialExpression); 
        return newst; 
      } 
      return st; 
    } 

    if (st instanceof ConditionalStatement) 
    { ConditionalStatement cs = (ConditionalStatement) st;
      Statement newif = 
        Statement.replaceLocalDeclarations(cs.ifPart(), vars);  
      Statement newelse = 
        Statement.replaceLocalDeclarations(cs.elsePart(), vars);
      ConditionalStatement res = 
        new ConditionalStatement(cs.test, newif, newelse);  
      return res; 
    } 

    if (st instanceof WhileStatement) 
    { WhileStatement ws = (WhileStatement) st; 
      Statement newbody = 
        Statement.replaceLocalDeclarations(
                                     ws.getLoopBody(),vars); 
      WhileStatement res = 
             new WhileStatement(ws.getLoopTest(),newbody);
      res.loopKind = ws.loopKind; 
      return res;  
    } 

    /* if (st instanceof TryStatement) 
    { TryStatement ts = (TryStatement) st; 
      Statement newBody = 
        Statement.replaceLocalDeclarations(ts.getBody(),vars); 
      Vector stats = ts.getClauses(); 
      for (int i = 0; i < stats.size(); i++) 
      { if (stats.get(i) instanceof Statement)
        { Statement stat = (Statement) stats.get(i); 
          Statement newstat = 
            Statement.replaceLocalDeclarations(stat,vars);
        }  
      } 
      res.addAll(getLocalDeclarations(ts.getEndStatement())); 
    } */ 

    return st;
  } // Other cases, for all other forms of statement. 

  public static Statement unfoldCall(
           Statement stat, String nme, Statement defn)
  { // self.nme() replaced by defn in stat.
    // where return is replaced by skip in defn.

    // System.out.println(">> Unfolding " + nme + " in " + stat); 

    if (defn == null || stat == null) 
    { return stat; }

    if (stat instanceof InvocationStatement)
    { InvocationStatement invok = 
        (InvocationStatement) stat;
      
      Expression expr = invok.getCallExp();
 
      if ((expr + "").startsWith("self." + nme + "("))
      // if (expr != null && expr.isSelfCall(nme))
      { return defn; }
      return stat; 
    } // Doesn't handle parameters. 

    /* if (st instanceof ReturnStatement)
    { ReturnStatement retstat = (ReturnStatement) st; 
      Statement res = new ContinueStatement();  
      
      Expression expr = retstat.getReturnValue();
 
      // if ((expr + "").startsWith("self." + nme + "("))
      if (expr != null && expr.isSelfCall(nme))
      { Statement passigns = 
             bf.parameterAssignments(expr);
        if (passigns == null) 
        { return res; } 
        else if (passigns instanceof SequenceStatement)
        { ((SequenceStatement) passigns).addStatement(res); 
          return passigns; 
        } 
      }
      return st; 
    } */ 
 
    if (stat instanceof SequenceStatement) 
    { SequenceStatement sq = (SequenceStatement) stat; 
      Vector stats = sq.getStatements();
      Vector res = new Vector(); 
 
      for (int i = 0; i < stats.size(); i++) 
      { Statement ss = (Statement) stats.get(i); 
        Statement newstat = 
            Statement.unfoldCall(ss,nme,defn); 
        if (newstat != null) 
        { res.add(newstat); } 
      } 
      return new SequenceStatement(res);
    } 
    
    if (stat instanceof ConditionalStatement) 
    { ConditionalStatement cs = (ConditionalStatement) stat;
      Expression tst = cs.getTest(); 

      Statement ifstat = cs.ifPart();
      Statement elsestat = cs.elsePart(); 
      
      Statement newif = 
        Statement.unfoldCall(ifstat,nme,defn); 
      Statement newelse = 
        Statement.unfoldCall(elsestat,nme,defn); 

      newif.setBrackets(true); 
      newelse.setBrackets(true); 
      return new ConditionalStatement(tst,newif,newelse); 
    } 

    if (stat instanceof WhileStatement) 
    { WhileStatement ws = (WhileStatement) stat; 
      Statement code = ws.getLoopBody();
      Statement newcode = Statement.unfoldCall(code,nme,defn); 
      WhileStatement wsnew = 
          new WhileStatement(ws.getLoopTest(), newcode); 
      // wsnew.loopTest = ws.loopTest;
      wsnew.loopKind = ws.loopKind;  
      wsnew.loopVar = ws.loopVar;
      wsnew.loopRange = ws.loopRange;
 
      return wsnew; 
    } 

   /* 
    if (st instanceof TryStatement) 
    { TryStatement ts = (TryStatement) st; 
      Statement bdy = ts.getBody(); 
      Statement newbdy = 
        Statement.replaceSelfCallsByContinue(bf,nme,bdy);
   
      Vector stats = ts.getClauses(); 
      Vector newstats = new Vector();
 
      for (int i = 0; i < stats.size(); i++) 
      { if (stats.get(i) instanceof Statement)
        { Statement stat = (Statement) stats.get(i); 
          Statement newstat = 
            Statement.replaceSelfCallsByContinue(
                                        bf,nme,stat);
          newstats.add(newstat); 
        }  
      }

      Statement endstat = ts.getEndStatement(); 
      Statement newend =
        Statement.replaceSelfCallsByContinue(
               bf, nme, endstat); 
      Statement newtry = 
        new TryStatement(newbdy, newstats, newend); 
      return newtry; 
    } */ 

    return stat;
  } // Other cases, for all other forms of statement. 

  public static Statement removeUnusedStatements(Statement st)
  { 
    if (st == null) 
    { return st; }

    if (st instanceof SequenceStatement) 
    { SequenceStatement sq = (SequenceStatement) st; 
      Vector stats = sq.getStatements();
      Vector res = new Vector(); 
 
      for (int i = 0; i < stats.size(); i++) 
      { Statement ss = (Statement) stats.get(i); 
        if (ss.unusedStatement) { } 
        else 
        { Statement newss = ss.removeUnusedStatements(ss); 
          res.add(newss); 
        } 
      } 
      return new SequenceStatement(res);
    } 
    
    if (st instanceof ConditionalStatement) 
    { ConditionalStatement cs = (ConditionalStatement) st;
      Expression tst = cs.getTest(); 

      Statement ifstat = cs.ifPart();
      Statement elsestat = cs.elsePart(); 
      
      Statement newif = 
        Statement.removeUnusedStatements(ifstat); 
      Statement newelse = 
        Statement.removeUnusedStatements(elsestat); 

      return new ConditionalStatement(tst,newif,newelse); 
    } 

    if (st instanceof TryStatement) 
    { TryStatement ts = (TryStatement) st; 
      Statement bdy = ts.getBody(); 
      Statement newbdy = 
        Statement.removeUnusedStatements(bdy);
   
      Vector stats = ts.getClauses(); 
      Vector newstats = new Vector();
 
      for (int i = 0; i < stats.size(); i++) 
      { if (stats.get(i) instanceof Statement)
        { Statement stat = (Statement) stats.get(i); 
          if (stat.unusedStatement) { } 
          else 
          { newstats.add(stat); } 
        }  
      }

      Statement endstat = ts.getEndStatement(); 
      Statement newend =
        Statement.removeUnusedStatements(endstat); 
      Statement newtry = 
        new TryStatement(newbdy, newstats, newend); 
      return newtry; 
    } 

    return st;
  } // Other cases, for all other forms of statement. 


  /* All cloned expressions/substatements in this statement */ 
  public abstract void findClones(java.util.Map clones, String rule, String op);

  /* All cloned expressions/substatements in this statement */ 
  public abstract void findClones(java.util.Map clones, 
                                  java.util.Map cloneDefs,
                                  String rule, String op);

  /* All magic number expressions in this statement */ 
  public void findMagicNumbers(java.util.Map mgns, String rule, String op)
  { return; } 

  /* All expression uses of var in this statement */ 
  public Vector getUses(String var) 
  { return new Vector(); } 

  /* Occurrences of any variable of vars in this statement */ 
  public Vector variablesUsedIn(Vector vars) 
  { return new Vector(); } 

  /* Occurrences of any attribute/role in this statement */ 
  public Vector allFeaturesUsedIn() 
  { return new Vector(); } 

  /* Expression occurrences of any variable in this statement */ 

  public Vector getVariableUses()
  { return new Vector(); } 

  public Vector getVariableUses(Vector unused)
  { return getVariableUses(); } 

  public Vector allAttributesUsedIn()
  { return new Vector(); } 

  
  public static boolean hasResultDeclaration(Statement st)
  { if (st == null) { return false; } 
    if (st instanceof SequenceStatement) 
    { SequenceStatement sq = (SequenceStatement) st; 
      if (sq.size() == 0) 
      { return false; }
      Statement st1 = (Statement) sq.getStatements().get(0);  
      return hasResultDeclaration(st1); 
    } 
    else if (st instanceof CreationStatement)
    { CreationStatement cs = (CreationStatement) st; 
      if (cs.isResultDeclaration())
      { return true; } 
    } 

    return false; 
  } 

  public String cg(CGSpec cgs)
  { return this + ""; }

  public abstract Vector metavariables(); 

  public abstract Vector cgparameters(); 

  public abstract boolean containsSubexpression(Expression expr); 

  public abstract Vector singleMutants(); 

  abstract String bupdateForm(); 

  public abstract BStatement bupdateForm(java.util.Map env, boolean local);

  abstract void display(PrintWriter out); 

  public void displayImp(String var) 
  { display(); }    /* Default */ 

  public void displayImp(String var, PrintWriter out) 
  { display(out); } 

  abstract void displayJava(String t); 

  abstract void displayJava(String t, PrintWriter out); 
 
  abstract Statement substituteEq(String oldE, Expression newE); 

  abstract Statement removeSlicedParameters(
             BehaviouralFeature op, Vector fpars); 

  abstract Statement addContainerReference(
                                  BasicExpression ref,
                                  String var, 
                                  Vector excludes); 

  public static Vector
    addContainerReferences(Vector cstats, BasicExpression ref,
                                  String var, 
                                  Vector excludes)
  { Vector rstats = new Vector(); 
    for (int j = 0; j < cstats.size(); j++) 
    { Statement jstat = (Statement) cstats.get(j); 
      Statement rstat = 
          jstat.addContainerReference(ref,var,excludes); 
      rstats.add(rstat); 
    } 
    return rstats; 
  } 

  abstract Expression wpc(Expression post); 

  abstract Expression wpc(Expression inv, Expression post); 

  // The variables in allvars whose pre-values can affect 
  // the vars post-values as a result of this 
  // statement execution
  abstract Vector dataDependents(Vector allvars, Vector vars); 

  abstract Vector dataDependents(Vector allvars, Vector vars, Map mp, Map dlin); 

  abstract String toStringJava(); 

  abstract String toAST(); 

  /* as text model */
  abstract String saveModelData(PrintWriter out); 

  public String saveModelData(PrintWriter out, Entity ent)
  { return saveModelData(out); }  

  abstract boolean typeCheck(Vector types, Vector entities, 
                             Vector contexts, Vector env); 

  abstract boolean typeInference(Vector types, Vector entities, Vector ctxs, Vector env, java.util.Map vartypes); 

  boolean typeCheck(Vector types, Vector entities, Vector env)
  { Vector contexts = new Vector(); 
    if (entity != null) 
    { contexts.add(entity); } 
    return typeCheck(types,entities,contexts,env); 
  }  
  
  public static Statement buildIf(Vector conds, Vector stats)
  { IfStatement res = new IfStatement();
    for (int i = 0; i < conds.size(); i++)
    { Expression cond = (Expression) conds.get(i);
      Statement stat = (Statement) stats.get(i);
      IfCase ic = new IfCase(cond,stat);
      res.addCase(ic);
    }
    return res;
  }

  // The statement can update any of the variables in v
  abstract boolean updates(Vector v); 

  Expression toExpression() 
  { return new BasicExpression("skip"); } 

  public Statement generateDesign(java.util.Map env, boolean local)
  { return this; }  

  public Statement statLC(java.util.Map env, boolean local)
  { return this; }  

  public String updateForm(String lang, java.util.Map env, boolean local)
  { if ("Java4".equals(lang))
    { return updateForm(env,local); } 
    if ("Java6".equals(lang))
    { return updateFormJava6(env,local); } 
    if ("Java7".equals(lang))
    { return updateFormJava7(env,local); } 
    if ("CSharp".equals(lang))
    { return updateFormCSharp(env,local); } 
    else
    { return updateFormCPP(env,local); } 
  } 

  public String updateForm(java.util.Map env, boolean local)
  { Vector entities = new Vector(); 
    Vector types = new Vector(); 
    Vector vars = new Vector(); 
    return updateForm(env,local,types,entities,vars); 
  } 

  public abstract String updateForm(java.util.Map env, boolean local, Vector types, 
                                    Vector entities, Vector vars);

  public abstract String updateFormJava6(java.util.Map env, boolean local);

  public abstract String updateFormJava7(java.util.Map env, boolean local);

  public abstract String updateFormCSharp(java.util.Map env, boolean local);

  public abstract String updateFormCPP(java.util.Map env, boolean local);

  public Vector allPreTerms()
  { return new Vector(); } 

  public Vector allPreTerms(String var)
  { return new Vector(); } 

  public boolean isSkip() 
  { return false; }

  public abstract Statement dereference(BasicExpression var); 

  public String processPreTerms(Statement post, Vector preterms, java.util.Map env, boolean local,
                                Vector types, Vector entities, Vector vars)
  { if (preterms.size() > 0) 
    { Statement newpost = (Statement) post.clone(); 
      System.out.println(">>> PRE terms in statement " + post + " : " + preterms); 
      Vector processed = new Vector(); 
      Vector localatts = new Vector(); 

      String newdecs = ""; 
      for (int i = 0; i < preterms.size(); i++)
      { BasicExpression preterm = (BasicExpression) preterms.get(i);
        if (processed.contains(preterm)) { continue; }  
        // also skip if the preterm is not valid. 
        Type typ = preterm.getType();  // but actual type may be list if multiple
        Type actualtyp; 
        String newdec = ""; 
        String pre_var = Identifier.nextIdentifier("pre_" + preterm.data);
        String pretermqf = preterm.classqueryForm(env,true); 
          
        BasicExpression prebe = new BasicExpression(pre_var); 

        if (preterm.isMultiple())
        { if (preterm.isOrdered())
          { actualtyp = new Type("Sequence", null); } 
          else 
          { actualtyp = new Type("Set",null); }  
          actualtyp.setElementType(preterm.getElementType()); 
 
          if (preterm.umlkind == Expression.CLASSID && preterm.arrayIndex == null) 
          { pretermqf = "Controller.inst()." + pretermqf.toLowerCase() + "s"; } 
          newdec = actualtyp.getJava() + " " + pre_var + " = new Vector();\n" + 
                 "    " + pre_var + ".addAll(" + pretermqf + ");\n"; 
        } 
        else 
        { actualtyp = typ;
          newdec = "  " + actualtyp.getJava() + " " + pre_var + " = " + pretermqf + ";\n";
        } 
        newdecs = newdecs + "    " + newdec; 
        prebe.type = actualtyp; 
        prebe.elementType = preterm.elementType; 
        prebe.entity = preterm.getEntity(); 
        System.out.println(">> PRE variable " + prebe + " type= " + actualtyp + 
                              " elemtype= " + prebe.elementType); 
          
        Attribute preatt = 
            new Attribute(pre_var,actualtyp,ModelElement.INTERNAL); 
        preatt.setElementType(preterm.elementType); 
        preatt.setEntity(preterm.getEntity()); 
        localatts.add(preatt); 
        System.out.println(">>> New preterm variable: " + preatt + " : " + actualtyp + " " + 
                           preterm.getEntity()); 
        newpost = newpost.substituteEq("" + preterm,prebe); 
        processed.add(preterm); 
      }  // substitute(preterm,prebe) more appropriate 

      Vector context = new Vector(); 
      if (entity != null) 
      { context.add(entity); } 
      newpost.typeCheck(types,entities,context,localatts);  // and the vars
      return newdecs + "\n  " + newpost.updateForm(env,local,types,entities,localatts);
    } 
    return post.updateForm(env,local,types,entities,vars);  
  }  

  public String processPreTermsJava6(Statement post, Vector preterms, java.util.Map env, boolean local)
  { if (preterms.size() > 0) 
    { Statement newpost = (Statement) post.clone(); 
      System.out.println(">> PRE terms: " + preterms); 
      Vector processed = new Vector(); 
      Vector localatts = new Vector(); 

      String newdecs = ""; 
      for (int i = 0; i < preterms.size(); i++)
      { BasicExpression preterm = (BasicExpression) preterms.get(i);
        if (processed.contains(preterm)) { continue; }  
        Type typ = preterm.getType();  // but actual type may be list if multiple
        Type actualtyp; 
        String newdec = ""; 
        String pre_var = Identifier.nextIdentifier("pre_" + preterm.data);
        String pretermqf = preterm.classqueryFormJava6(env,true); 
          
        BasicExpression prebe = new BasicExpression(pre_var); 

        if (preterm.isMultiple())
        { if (preterm.isOrdered())
          { actualtyp = new Type("Sequence", null); } 
          else 
          { actualtyp = new Type("Set",null); }  
          actualtyp.setElementType(preterm.getElementType()); 
          newdec = actualtyp.getJava6() + " " + pre_var + " = " + 
                   actualtyp.initialValueJava6() + ";\n" + 
                   "    " + pre_var + ".addAll(" + pretermqf + ");\n"; 
        } 
        else 
        { actualtyp = typ;
          newdec = actualtyp.getJava6() + " " + pre_var + " = " + pretermqf + ";\n";
        } 
        newdecs = newdecs + "    " + newdec; 
        prebe.type = actualtyp; 
        prebe.elementType = preterm.elementType; 
        // System.out.println("PRE variable " + prebe + " type= " + actualtyp + 
        //                      " elemtype= " + prebe.elementType); 
          
        Attribute preatt = 
            new Attribute(pre_var,actualtyp,ModelElement.INTERNAL); 
        preatt.setElementType(preterm.elementType); 
        localatts.add(preatt); 
        newpost = newpost.substituteEq("" + preterm,prebe); 
        processed.add(preterm); 
      }  // substitute(preterm,prebe) more appropriate 

      // newpost.typeCheck(types,entities,context,localatts);
      return newdecs + "\n  " + newpost.updateFormJava6(env,local);
    } 
    return post.updateFormJava6(env,local);  
  }  

  public String processPreTermsJava7(Statement post, Vector preterms, java.util.Map env, boolean local)
  { if (preterms.size() > 0) 
    { Statement newpost = (Statement) post.clone(); 
      // System.out.println("PRE terms: " + preterms); 
      Vector processed = new Vector(); 
      Vector localatts = new Vector(); 

      String newdecs = ""; 
      for (int i = 0; i < preterms.size(); i++)
      { BasicExpression preterm = (BasicExpression) preterms.get(i);
        if (processed.contains(preterm)) { continue; }  
        Type typ = preterm.getType();  // but actual type may be list if multiple
        Type actualtyp; 
        String newdec = ""; 
        String pre_var = Identifier.nextIdentifier("pre_" + preterm.data);
        String pretermqf = preterm.classqueryFormJava7(env,true); 
          
        BasicExpression prebe = new BasicExpression(pre_var); 

        if (preterm.isMultiple())
        { if (preterm.isOrdered())
          { actualtyp = new Type("Sequence",null); } 
          else 
          { actualtyp = new Type("Set",null); } 
          actualtyp.setElementType(preterm.getElementType()); 
          newdec = actualtyp.getJava7(preterm.getElementType()) + " " + pre_var + " = " + 
                   actualtyp.initialValueJava7() + ";\n" + 
                   "    " + pre_var + ".addAll(" + pretermqf + ");\n"; 
        } 
        else 
        { actualtyp = typ;
          newdec = actualtyp.getJava7(preterm.getElementType()) + " " + pre_var + " = " + pretermqf + ";\n";
        } 
        newdecs = newdecs + "    " + newdec; 
        prebe.type = actualtyp; 
        prebe.elementType = preterm.elementType; 
        // System.out.println("PRE variable " + prebe + " type= " + actualtyp + 
        //                      " elemtype= " + prebe.elementType); 
          
        Attribute preatt = 
            new Attribute(pre_var,actualtyp,ModelElement.INTERNAL); 
        preatt.setElementType(preterm.elementType); 
        localatts.add(preatt); 
        newpost = newpost.substituteEq("" + preterm,prebe); 
        processed.add(preterm); 
      }  // substitute(preterm,prebe) more appropriate 

      // newpost.typeCheck(types,entities,context,localatts);
      return newdecs + "\n  " + newpost.updateFormJava7(env,local);
    } 
    return post.updateFormJava7(env,local);  
  }  

  public String processPreTermsCSharp(Statement post, Vector preterms, java.util.Map env, boolean local)
  { if (preterms.size() > 0) 
    { Statement newpost = (Statement) post.clone(); 
      // System.out.println("PRE terms: " + preterms); 
      Vector processed = new Vector(); 
      Vector localatts = new Vector(); 

      String newdecs = ""; 
      for (int i = 0; i < preterms.size(); i++)
      { BasicExpression preterm = (BasicExpression) preterms.get(i);
        if (processed.contains(preterm)) { continue; }  
        // also skip if the preterm is not valid. 
        Type typ = preterm.getType();  // but actual type may be list if multiple
        Type actualtyp; 
        String newdec = ""; 
        String pre_var = Identifier.nextIdentifier("pre_" + preterm.data);
        String pretermqf = preterm.classqueryFormCSharp(env,true); 
          
        BasicExpression prebe = new BasicExpression(pre_var); 

        if (preterm.isMultiple())
        { if (preterm.isOrdered())
          { actualtyp = new Type("Sequence", null); } 
          else 
          { actualtyp = new Type("Set",null); }  
          actualtyp.setElementType(preterm.getElementType());
          String csharptype = actualtyp.getCSharp(); 
 
          newdec = 
            "    " + csharptype + " " + pre_var + " = new " + csharptype + "();\n" + 
            "    " + pre_var + " = SystemTypes.union(" + pre_var + ", " + pretermqf + ");\n"; 
        } 
        else 
        { actualtyp = typ;
          newdec = actualtyp.getCSharp() + " " + pre_var + " = " + pretermqf + ";\n";
        } 
        newdecs = newdecs + "    " + newdec; 
        prebe.type = actualtyp; 
        prebe.elementType = preterm.elementType; 
        // System.out.println("PRE variable " + prebe + " type= " + actualtyp + 
        //                      " elemtype= " + prebe.elementType); 
          
        Attribute preatt = 
            new Attribute(pre_var,actualtyp,ModelElement.INTERNAL); 
        preatt.setElementType(preterm.elementType); 
        localatts.add(preatt); 
        newpost = newpost.substituteEq("" + preterm,prebe); 
        processed.add(preterm); 
      }  // substitute(preterm,prebe) more appropriate 

      // newpost.typeCheck(types,entities,context,localatts);
      return newdecs + "\n  " + newpost.updateFormCSharp(env,local);
    } 

    return post.updateFormCSharp(env,local);  
  }  

  public String processPreTermsCPP(Statement post, Vector preterms, java.util.Map env, boolean local)
  { if (preterms.size() > 0) 
    { Statement newpost = (Statement) post.clone(); 
      // System.out.println("PRE terms: " + preterms); 
      Vector processed = new Vector(); 
      Vector localatts = new Vector(); 

      String newdecs = ""; 
      for (int i = 0; i < preterms.size(); i++)
      { BasicExpression preterm = (BasicExpression) preterms.get(i);
        if (processed.contains(preterm)) { continue; }  
        // also skip if the preterm is not valid. 
        Type typ = preterm.getType();  // but actual type may be list if multiple
        Type actualtyp; 
        String newdec = ""; 
        String pre_var = Identifier.nextIdentifier("pre_" + preterm.data);
        String pretermqf = preterm.classqueryFormCPP(env,true); 
          
        BasicExpression prebe = new BasicExpression(pre_var); 

        if (preterm.isMultiple())
        { if (preterm.isOrdered())
          { actualtyp = new Type("Sequence", null); } 
          else 
          { actualtyp = new Type("Set",null); }  
          actualtyp.setElementType(preterm.getElementType()); 
          String cpptype = actualtyp.getCPP(preterm.getElementType()); 
          newdec = cpptype + " " + pre_var + " = new " + cpptype + "();\n"; 
          if (preterm.isOrdered())
          { newdec = newdec +  
              "    " + pre_var + "->insert(" + pre_var + "->end(), " + pretermqf + "->begin(), " + pretermqf + "->end());\n"; 
          } 
          else 
          { newdec = newdec +  
              "    " + pre_var + "->insert(" + pretermqf + "->begin(), " + pretermqf + "->end());\n"; 
          } 
        } 
        else 
        { actualtyp = typ;
          newdec = actualtyp.getCPP(preterm.getElementType()) + " " + pre_var + " = " + pretermqf + ";\n";
        } 
        newdecs = newdecs + "    " + newdec; 
        prebe.type = actualtyp; 
        prebe.elementType = preterm.elementType; 
        // System.out.println("PRE variable " + prebe + " type= " + actualtyp + 
        //                      " elemtype= " + prebe.elementType); 
          
        Attribute preatt = 
            new Attribute(pre_var,actualtyp,ModelElement.INTERNAL); 
        preatt.setElementType(preterm.elementType); 
        localatts.add(preatt); 
        newpost = newpost.substituteEq("" + preterm,prebe); 
        processed.add(preterm); 
      }  // substitute(preterm,prebe) more appropriate 

      // newpost.typeCheck(types,entities,context,localatts);
      return newdecs + "\n  " + newpost.updateFormCPP(env,local);
    } 
    return post.updateFormCPP(env,local);  
  }  

  public abstract Vector readFrame(); 

  public abstract Vector writeFrame(); 

  public abstract Statement checkConversions(Entity e, Type propType, Type propElemType, java.util.Map interp);  

  public abstract Statement replaceModuleReferences(UseCase uc);  

  public static Statement combineIfStatements(Statement s1, Statement s2) 
  { if (s1 instanceof IfStatement)
    { IfStatement ifstat = (IfStatement) s1; 
      Statement stat2 = ifstat.getElsePart(); 
      if (stat2 == null) 
      { ifstat.setElse(s2); }
      else 
      { SequenceStatement ep = new SequenceStatement(); 
        ep.addStatement(stat2); 
        ep.addStatement(s2); 
        ifstat.setElse(ep); 
      } 
      return ifstat; 
    } 
    else
    { SequenceStatement res = new SequenceStatement(); 
      res.addStatement(s1); 
      res.addStatement(s2); 
      return res; 
    } 
  }          

  abstract public int syntacticComplexity(); 

  abstract public int cyclomaticComplexity(); 

  abstract public int epl(); 

  abstract public Vector allOperationsUsedIn(); 

  abstract public Vector equivalentsUsedIn(); 

  abstract public String toEtl(); 
}


class ReturnStatement extends Statement
{ Expression value = null; 
  
  public ReturnStatement()
  { value = null; } 

  public ReturnStatement(Expression e)
  { value = e; } 

  public ReturnStatement(Vector exprs)
  { if (exprs == null || exprs.size() == 0) 
    { value = null; } 
    value = (Expression) exprs.get(0); 
  } 

  public String getOperator() 
  { return "return"; } 

  public Expression getExpression() 
  { return value; } 

  public Expression getReturnValue() 
  { return value; } 

  public Expression getValue() 
  { return value; } 

  public int execute(ModelSpecification sigma, 
                      ModelState beta)
  { if (value != null)
    { Expression expr = value.evaluate(sigma, beta); 
      beta.setVariableValue("result", expr); 
    } 

    return Statement.RETURN; 
  } 

  public Expression definedness()
  { if (value != null) 
    { return value.definedness(); } 
    return new BasicExpression(true); 
  } 

  public Object clone()
  { return new ReturnStatement(value); } 

  public void findClones(java.util.Map clones, String rule, String op)
  { if (value == null || 
        value.syntacticComplexity() < UCDArea.CLONE_LIMIT) 
    { return; }
  /*  String val = value + ""; 
    Vector used = (Vector) clones.get(val);
    if (used == null)  
    { used = new Vector(); }
    if (rule != null)
    { used.add(rule); }
    else if (op != null)
    { used.add(op); }
    clones.put(val,used); */ 

    value.findClones(clones,rule,op); 
  }

  public void findClones(java.util.Map clones, 
                         java.util.Map cloneDefs,
                         String rule, String op)
  { if (value == null || 
        value.syntacticComplexity() < UCDArea.CLONE_LIMIT) 
    { return; }
  /*  String val = value + ""; 
    Vector used = (Vector) clones.get(val);
    if (used == null)  
    { used = new Vector(); }
    if (rule != null)
    { used.add(rule); }
    else if (op != null)
    { used.add(op); }
    clones.put(val,used); */ 

    value.findClones(clones,cloneDefs,rule,op); 
  }

  public Map energyUse(Map uses, 
                       Vector rUses, Vector oUses)
  { if (value == null) 
    { return uses; } 
    value.energyUse(uses, rUses, oUses); 

    int syncomp = value.syntacticComplexity(); 

    if (syncomp > TestParameters.syntacticComplexityLimit)
    { System.err.println("!!! Code smell (MEL): too high expression complexity (" + syncomp + ") for " + value); 
      System.err.println(">>> Recommend OCL refactoring"); 
    } // amber flaw

    return uses; 
  }  

  public Statement optimiseOCL()
  { if (value == null) 
    { return this; } 
    Expression newval = value.simplifyOCL(); 
    return new ReturnStatement(newval); 
  }  

  public java.util.Map collectionOperatorUses(
                             int nestingLevel, 
                             java.util.Map operatorsAtLevel,
                             Vector vars)
  { if (value == null) 
    { return operatorsAtLevel; } 
    value.collectionOperatorUses(nestingLevel, 
                                 operatorsAtLevel, vars); 
    return operatorsAtLevel; 
  }  


  public void findMagicNumbers(java.util.Map mgns, String rule, String op)
  { if (value == null) 
    { return; }
    String val = this + ""; 
    value.findMagicNumbers(mgns,val,op); 
  }

  public boolean hasValue()
  { return value != null; } 

  public void display()
  { System.out.print("  return"); 
    if (value != null)
    { System.out.print(" " + value); } 
    System.out.println(";"); 
  }  

  public void display(PrintWriter out)
  { out.print("  return"); 
    if (value != null)
    { out.print(" " + value); } 
    out.println(";"); 
  }  

  public void displayJava(String t)
  { display(); }  

  public void displayJava(String t, PrintWriter out)
  { display(out); }  
 
  public Statement substituteEq(String oldE, Expression newE)
  { if (value != null)
    { Expression newval = value.substituteEq(oldE,newE); 
      ReturnStatement res = new ReturnStatement(newval);
      res.setEntity(entity); 
      return res;  
    } 
    return this; 
  } 

  public Statement removeSlicedParameters(
             BehaviouralFeature op, Vector fpars)
  { if (value != null)
    { Expression newval = 
                 value.removeSlicedParameters(op,fpars); 
      ReturnStatement res = new ReturnStatement(newval);
      res.setEntity(entity); 
      return res;  
    } 
    return this; 
  } 
  

  public Statement addContainerReference(
                                  BasicExpression ref,
                                  String var,
                                  Vector excludes)
  { if (value != null)
    { Expression newval = value.addContainerReference(
                                    ref,var,excludes); 
      ReturnStatement res = new ReturnStatement(newval);
      res.setEntity(entity); 
      return res;  
    } 
    return this; 
  }  


  public String toString()
  { if (value == null)
    { return "return "; } 
    return "return " + value;
  } 

  public String toAST()
  { String res = ""; 
    if (value == null)
    { res = "(OclStatement return)"; } 
    else 
    { res = "(OclStatement return " + value.toAST() + ")"; } 

    // if (brackets)
    // { res = "(OclStatement ( " + res + " ) )"; } 

    return res; 
  } 

  public boolean containsSubexpression(Expression expr) 
  { if (value == null) 
    { return false; } 
    return value.containsSubexpression(expr); 
  } 

  public Vector singleMutants()
  { if (value == null) 
    { return new Vector(); } 
    Vector exprs = value.singleMutants(); 
    Vector res = new Vector(); 
    for (int i = 0; i < exprs.size(); i++) 
    { Expression mvalue = (Expression) exprs.get(i); 
      res.add(new ReturnStatement(mvalue)); 
    } 
    return res; 
  } 

  public String saveModelData(PrintWriter out)
  { String res = Identifier.nextIdentifier("returnstatement_"); 
    out.println(res + " : ReturnStatement"); 
    out.println(res + ".statId = \"" + res + "\""); 
    if (value != null) 
    { String valueid = value.saveModelData(out); 
      out.println(valueid + " : " + res + ".returnValue"); 
    } 
    return res; 
  } 

  public String saveModelData(PrintWriter out, Entity ent)
  { return saveModelData(out); }

  public String bupdateForm()
  { return " "; } 

  public BStatement bupdateForm(java.util.Map env, boolean local)
  { return new BBasicStatement("skip"); } 

  public String toStringJava()
  { String res = "  return"; 
    if (value != null)
    { java.util.Map env = new java.util.HashMap(); 
      if (entity != null) 
      { env.put(entity.getName(),"this"); 
        res = res + " " + value.queryForm(env,true);
      }
      else 
      { res = res + " " + value.queryForm(env,true); } 
    } 
    res = res + ";"; 
    return res; 
  }  

  public String toEtl()
  { String res = "  return"; 
    if (value != null)
    { res = res + " " + value; } 
    res = res + ";"; 
    return res; 
  }  

  public boolean typeCheck(Vector types, Vector entities, Vector ctxs, Vector env)
  { if (value == null) { return true; } 
    return value.typeCheck(types,entities,ctxs,env); 
  }  

  public boolean typeInference(Vector types, Vector entities, Vector cs, Vector env, java.util.Map vartypes)
  { if (value == null) { return true; } 
    value.typeInference(types,entities,cs,env,vartypes);
    vartypes.put("result", value.getType()); 
    return true;  
  } 

  public void displayImp(String var, PrintWriter out) 
  { } 
 
  public Expression wpc(Expression post)
  { return post; }  

  public Expression wpc(Expression inv, Expression post)
  { return post; }  

  public Vector dataDependents(Vector allvars, Vector vars)
  { return vars; }  

  public Vector dataDependents(Vector allvars, Vector vars, Map mp, Map dlin)
  { return vars; }  

  public boolean updates(Vector v) 
  { return false; } 

  public String updateForm(java.util.Map env, boolean local, Vector types, Vector entities,
                           Vector vars)
  { String res = "    return"; 
    if (value != null)
    { res = res + " " + value.queryForm(env,local); } 
    res = res + ";"; 
    return res; 
  }  

  public String updateFormJava6(java.util.Map env, boolean local)
  { String res = "    return"; 
    if (value != null)
    { res = res + " " + value.queryFormJava6(env,local); } 
    res = res + ";"; 
    return res; 
  }  

  public String updateFormJava7(java.util.Map env, boolean local)
  { String res = "    return"; 
    if (value != null)
    { res = res + " " + value.queryFormJava7(env,local); } 
    res = res + ";"; 
    return res; 
  }  

  public String updateFormCSharp(java.util.Map env, boolean local)
  { String res = "    return"; 
    if (value != null)
    { res = res + " " + value.queryFormCSharp(env,local); } 
    res = res + ";"; 
    return res; 
  }  

  public String updateFormCPP(java.util.Map env, boolean local)
  { String res = "    return"; 
    if (value != null)
    { res = res + " " + value.queryFormCPP(env,local); } 
    res = res + ";"; 
    return res; 
  }  

  public Vector allPreTerms()
  { Vector res = new Vector();
    if (value == null) 
    { return res; } 
    return value.allPreTerms(); 
  }  

  public Vector allPreTerms(String var)
  { Vector res = new Vector();
    if (value == null) 
    { return res; } 
    return value.allPreTerms(var); 
  }  

  public Statement dereference(BasicExpression var)
  { if (value == null) 
    { return new ReturnStatement(value); }
    Expression val = value.dereference(var); 
    return new ReturnStatement(val); 
  }  

  public Vector metavariables()
  { Vector res = new Vector(); 
    if (value != null) 
    { return value.metavariables(); }  
    return res; 
  } 

  public Vector readFrame() 
  { Vector res = new Vector();
    if (value == null) 
    { return res; } 
    return value.allReadFrame(); 
  } 

  public Vector writeFrame() 
  { Vector res = new Vector();
    return res;
  } 

  public Statement checkConversions(Entity e, Type propType, Type propElemType, java.util.Map interp)
  { if (value == null) 
    { return this; } 
    Expression val = value.checkConversions(propType,propElemType,interp); 
    return new ReturnStatement(val); 
  }   

  public Statement replaceModuleReferences(UseCase uc)
  { if (value == null) 
    { return this; } 
    Expression val = value.replaceModuleReferences(uc); 
    return new ReturnStatement(val); 
  }   

  public int syntacticComplexity()
  { if (value == null) 
    { return 1; } 

    int syncomp = value.syntacticComplexity(); 

    return syncomp + 1; 
  } 

  public int cyclomaticComplexity()
  { return 0; }  // no predicate nodes

  public int epl()
  { return 0; }  

  public Vector allOperationsUsedIn()
  { Vector res = new Vector(); 
    if (value == null) 
    { return res; } 
    return value.allOperationsUsedIn(); 
  } 

  public Vector getUses(String var)
  { Vector res = new Vector(); 
    if (value == null) 
    { return res; } 
    return value.getUses(var); 
  } 

  public Vector getVariableUses()
  { Vector res = new Vector(); 
    if (value == null) 
    { return res; } 
    return value.getVariableUses(); 
  } 

  public Vector getVariableUses(Vector unused)
  { Vector res = new Vector(); 
    if (value == null) 
    { return res; } 
    return value.getVariableUses(); 
  } 

  public Vector allAttributesUsedIn()
  { Vector res = new Vector(); 
    if (value == null) 
    { return res; } 
    return value.allAttributesUsedIn(); 
  } 

  public Vector allFeaturesUsedIn()
  { Vector res = new Vector(); 
    if (value == null) 
    { return res; } 
    return value.allFeaturesUsedIn(); 
  } 

  public Vector equivalentsUsedIn()
  { Vector res = new Vector(); 
    if (value == null) 
    { return res; } 
    return value.equivalentsUsedIn(); 
  } 

  public Vector allVariableNames()
  { Vector res = new Vector(); 
    if (value == null) 
    { return res; } 
    return value.allVariableNames(); 
  } 

  public String cg(CGSpec cgs)
  { String etext = this + "";
    Vector args = new Vector();
    if (value != null) 
    { args.add(value.cg(cgs)); } 
    CGRule r = cgs.matchedStatementRule(this,etext);

    System.out.println(">> Matched statement rule: " + r + " for " + this); 

    if (r != null)
    { return r.applyRule(args); }
    return etext;
  }

  public Vector cgparameters()
  { Vector args = new Vector();
    if (value != null) 
    { args.add(value); } 
    return args; 
  } 

}


class BreakStatement extends Statement
{ public void display()
  { System.out.println("  break;"); }  

  public String getOperator() 
  { return "break"; } 

  public Object clone()
  { return new BreakStatement(); } 

  public String toString() 
  { return "break"; } 

  public String toAST() 
  { String res = "(OclStatement break)"; 

    // if (brackets)
    // { res = "(OclStatement ( " + res + " ) )"; }

    return res;  
  } 

  public int execute(ModelSpecification sigma, ModelState beta)
  { return Statement.BREAK; } 

  public boolean containsSubexpression(Expression expr) 
  { return false; } 

  public Vector singleMutants()
  { Vector res = new Vector(); 
    res.add(new ContinueStatement()); 
    // res.add(new ReturnStatement()); 
    return res; 
  } 

  public String bupdateForm()
  { return " "; } 

  public BStatement bupdateForm(java.util.Map env, boolean local)
  { return new BBasicStatement("skip"); } 

  public void display(PrintWriter out)
  { out.println("  break;"); }  

  public void displayJava(String t)
  { display(); }  

  public void displayJava(String t, PrintWriter out)
  { display(out); }  
 
  public Statement substituteEq(String oldE, Expression newE)
  { return this; } 

  public Statement removeSlicedParameters(
             BehaviouralFeature op, Vector fpars)
  { return this; } 


  public Statement addContainerReference(
                                  BasicExpression ref,
                                  String var, Vector excludes)
  { return this; }  


  public String toStringJava()
  { return "  break;"; }

  public String toEtl()
  { return "  break;"; }

  public String saveModelData(PrintWriter out)
  { String res = Identifier.nextIdentifier("breakstatement_"); 
    out.println(res + " : BreakStatement"); 
    out.println(res + ".statId = \"" + res + "\""); 
    return res; 
  } 

  public String saveModelData(PrintWriter out, Entity ent)
  { return saveModelData(out); }

  public boolean typeCheck(Vector types, Vector entities, Vector cs, Vector env)
  { return true; }  
 
  public boolean typeInference(Vector types, Vector entities, Vector cs, Vector env, java.util.Map vartypes)
  { return true; } 

  public Expression wpc(Expression post)
  { return post; }  

  public Expression wpc(Expression inv, Expression post)
  { return inv; }  

  public Vector dataDependents(Vector allvars, Vector vars)
  { return vars; }  

  public Vector dataDependents(Vector allvars, Vector vars, Map mp, Map dlin)
  { return vars; }  

  public boolean updates(Vector v) 
  { return false; } 

  public String updateForm(java.util.Map env, boolean local, Vector types, 
                           Vector entities, Vector vars)
  { return toStringJava(); }  

  public String updateFormJava6(java.util.Map env, boolean local)
  { return toStringJava(); }  

  public String updateFormJava7(java.util.Map env, boolean local)
  { return toStringJava(); }  

  public String updateFormCSharp(java.util.Map env, boolean local)
  { return toStringJava(); }  

  public String updateFormCPP(java.util.Map env, boolean local)
  { return toStringJava(); }  

  public Statement dereference(BasicExpression var)
  { return new BreakStatement(); }  

  public Vector readFrame() 
  { Vector res = new Vector();
    return res; 
  } 

  public Vector writeFrame() 
  { Vector res = new Vector();
    return res;
  } 

  public Statement checkConversions(Entity e, Type propType, Type propElemType, java.util.Map interp)
  { return new BreakStatement(); } 

  public Statement replaceModuleReferences(UseCase uc)
  { return new BreakStatement(); } 

  public int syntacticComplexity()
  { return 1; } 

  public int cyclomaticComplexity()
  { return 0; }  // no predicate nodes

  public int epl()
  { return 0; }  

  public Vector allOperationsUsedIn()
  { Vector res = new Vector(); 
    return res; 
  } 

  public Vector equivalentsUsedIn()
  { Vector res = new Vector(); 
    return res; 
  } 

  public Vector metavariables()
  { Vector res = new Vector(); 
    return res; 
  }
 
  public String cg(CGSpec cgs)
  { String etext = this + "";
    Vector args = new Vector();
    CGRule r = cgs.matchedStatementRule(this,etext);

    System.out.println(">> Matched statement rule: " + r + " for " + this); 

    if (r != null)
    { return r.applyRule(args); }
    return etext;
  }

  public Vector cgparameters()
  { Vector args = new Vector();
    return args; 
  } 

  public void findClones(java.util.Map clones, String op, String rule)
  { return; } 

  public void findClones(java.util.Map clones, 
                         java.util.Map cloneDefs,
                         String op, String rule)
  { return; } 

  public Statement optimiseOCL()
  { return this; }  

  public Map energyUse(Map uses, 
                       Vector rUses, Vector oUses)
  { return uses; }  

  public java.util.Map collectionOperatorUses(
                             int nestingLevel, 
                             java.util.Map operatorsAtLevel, 
                             Vector vars)
  { return operatorsAtLevel; }  
}

class ContinueStatement extends Statement
{ 
  public ContinueStatement()
  { super(); } 

  public void display()
  { System.out.println("  continue;"); }  

  public String getOperator() 
  { return "continue"; } 

  public Object clone()
  { return new BreakStatement(); } 

  public String toString() 
  { return "continue"; } 

  public int execute(ModelSpecification sigma, ModelState beta)
  { return Statement.CONTINUE; } 

  public String toAST()
  { String res = "(OclStatement continue)"; 

    // if (brackets)
    // { res = "(OclStatement ( " + res + " ) )"; } 

    return res; 
  } 

  public boolean containsSubexpression(Expression expr) 
  { return false; } 
   
  public Vector singleMutants()
  { Vector res = new Vector(); 
    res.add(new BreakStatement()); 
    return res; 
  } 

  public String bupdateForm()
  { return " "; } 

  public BStatement bupdateForm(java.util.Map env, boolean local)
  { return new BBasicStatement("skip"); } 

  public void display(PrintWriter out)
  { out.println("  continue;"); }  

  public void displayJava(String t)
  { display(); }  

  public void displayJava(String t, PrintWriter out)
  { display(out); }  
 
  public Statement substituteEq(String oldE, Expression newE)
  {  
    return this; 
  } 

  public Statement removeSlicedParameters(
             BehaviouralFeature op, Vector fpars)
  { return this; }


  public Statement addContainerReference(
                                  BasicExpression ref,
                                  String var, Vector excludes)
  { return this; }  


  public String toStringJava()
  { return "  continue;"; }

  public String toEtl()
  { return "  continue;"; }

  public String saveModelData(PrintWriter out)
  { String res = Identifier.nextIdentifier("continuestatement_"); 
    out.println(res + " : ContinueStatement"); 
    out.println(res + ".statId = \"" + res + "\""); 
    return res; 
  } 

  public String saveModelData(PrintWriter out, Entity ent)
  { return saveModelData(out); }

  public boolean typeCheck(Vector types, Vector entities, Vector cs, Vector env)
  { return true; }  

  public boolean typeInference(Vector types, Vector entities, Vector cs, Vector env, java.util.Map vartypes)
  { return true; }  
 
  public Expression wpc(Expression post)
  { return post; }  

  public Expression wpc(Expression inv, Expression post)
  { return inv; }  // goes to the head of the loop again


  public Vector dataDependents(Vector allvars, Vector vars)
  { return vars; }  

  public Vector dataDependents(Vector allvars, Vector vars, Map mp, Map dlin)
  { return vars; }  

  public boolean updates(Vector v) 
  { return false; } 

  public String updateForm(java.util.Map env, boolean local, Vector types, 
                           Vector entities, Vector vars)
  { return toStringJava(); }  

  public String updateFormJava6(java.util.Map env, boolean local)
  { return toStringJava(); }  

  public String updateFormJava7(java.util.Map env, boolean local)
  { return toStringJava(); }  

  public String updateFormCSharp(java.util.Map env, boolean local)
  { return toStringJava(); }  

  public String updateFormCPP(java.util.Map env, boolean local)
  { return toStringJava(); }  

  public Statement dereference(BasicExpression var)
  { return new ContinueStatement(); }  

  public Vector readFrame() 
  { Vector res = new Vector();
    return res; 
  } 

  public Vector writeFrame() 
  { Vector res = new Vector();
    return res;
  } 

  public Statement checkConversions(Entity e, Type propType, Type propElemType, java.util.Map interp)
  { return new ContinueStatement(); } 

  public Statement replaceModuleReferences(UseCase uc)
  { return new ContinueStatement(); } 

  public int syntacticComplexity()
  { return 1; } 

  public int cyclomaticComplexity()
  { return 0; }  // no predicate nodes

  public int epl()
  { return 0; }  

  public Vector allOperationsUsedIn()
  { Vector res = new Vector(); 
    return res; 
  } 

  public Vector equivalentsUsedIn()
  { Vector res = new Vector(); 
    return res; 
  } 

  public Vector metavariables()
  { Vector res = new Vector(); 
    return res; 
  }
 
  public String cg(CGSpec cgs)
  { String etext = this + "";
    Vector args = new Vector();
    CGRule r = cgs.matchedStatementRule(this,etext);

    System.out.println(">> Matched statement rule: " + r + " for " + this); 

    if (r != null)
    { return r.applyRule(args); }
    return etext;
  }

  public Vector cgparameters()
  { Vector args = new Vector();
    return args; 
  } 

  public void findClones(java.util.Map clones, String op, String rule)
  { return; } 

  public void findClones(java.util.Map clones, 
                         java.util.Map cloneDefs,
                         String op, String rule)
  { return; } 

  public Map energyUse(Map uses, 
                                Vector rUses, Vector oUses)
  { return uses; }  

  public Statement optimiseOCL()
  { return this; }  

  public java.util.Map collectionOperatorUses(
                             int nestingLevel, 
                             java.util.Map operatorsAtLevel,
                             Vector vars)
  { return operatorsAtLevel; }  
}


class InvocationStatement extends Statement
{ String action; 
  String target; 
  // Event event;
  String assignsTo = "";
  String assignsType = ""; 

  private Vector parameters = new Vector();  
  Expression callExp = new BasicExpression("skip"); 


  public InvocationStatement(Event ee)
  { // event = ee; 
    action = ee.label; 
    assignsTo = null; 
    target = null; 
  }

  /* InvocationStatement(String var, Event ee) 
  { assignsTo = var; 
    action = ee.label;  // ??? 
    event = ee; }  */ 

  public String getOperator() 
  { return "call"; } 

  InvocationStatement(String act, String targ, String assigns)
  { action = act; 
    target = targ; 
    assignsTo = assigns; 
  } 

  InvocationStatement(BehaviouralFeature bf)
  { action = bf.getName(); 
    target = null; 
    assignsTo = null; 
    parameters = new Vector(); 
    parameters.addAll(bf.getParameters()); 
    BasicExpression calle = 
         new BasicExpression(bf + "", 0);
    Expression callee = calle.checkIfSetExpression();
    if (callee == null) { return; }
    if (bf.isQuery())
    { callee.setUmlKind(Expression.QUERY); } 
    else 
    { callee.setUmlKind(Expression.UPDATEOP); } 
    callee.setType(bf.getResultType());
    callee.setElementType(bf.getElementType());
    callee.setEntity(bf.getEntity());
    callExp = callee; 
  } 

  InvocationStatement(String obj, BehaviouralFeature bf)
  { action = bf.getName(); 
    target = null; 
    assignsTo = null; 
    parameters = new Vector(); 
    parameters.addAll(bf.getParameters()); 
    BasicExpression calle = 
         new BasicExpression(obj + "." + bf + "", 0);
    Expression callee = calle.checkIfSetExpression();
    if (callee == null) { return; }
    if (bf.isQuery())
    { callee.setUmlKind(Expression.QUERY); } 
    else 
    { callee.setUmlKind(Expression.UPDATEOP); } 
    callee.setType(bf.getResultType());
    callee.setElementType(bf.getElementType());
    callee.setEntity(bf.getEntity());
    callExp = callee; 
  } 

  InvocationStatement(String obj, String bfname)
  { action = bfname; 
    target = null; 
    assignsTo = null; 
    parameters = new Vector(); 
    BasicExpression calle = 
         new BasicExpression(obj + "." + bfname + "()", 0);
    Expression callee = calle.checkIfSetExpression();
    if (callee == null) { return; }
    callee.setUmlKind(Expression.UPDATEOP); 
    // callee.setType(bf.getResultType());
    // callee.setElementType(bf.getElementType());
    // callee.setEntity(bf.getEntity());
    callExp = callee; 
  } 

  InvocationStatement(BasicExpression be)
  { action = be.getData(); 
    target = null; 
    assignsTo = null; 
    parameters = new Vector();
    if (be.getParameters() != null)  
    { parameters.addAll(be.getParameters()); }  
    callExp = be; 
  } 

  InvocationStatement(Expression be)
  { action = be + ""; 
    target = null; 
    assignsTo = null; 
    parameters = new Vector();
    if (be instanceof BasicExpression) 
    { BasicExpression bexpr = (BasicExpression) be; 
      if (bexpr.getParameters() != null)  
      { parameters.addAll(bexpr.getParameters()); } 
    }  
    callExp = be; 
  } 

  InvocationStatement(String act)
  { action = act; 
    target = null; 
    assignsTo = null; 
    callExp = new BasicExpression(act); 
  } 

  public static InvocationStatement newInvocationStatement(
                                       Expression expr, 
                                       Vector pars) 
  { InvocationStatement res = 
        new InvocationStatement(expr + ""); 
    res.target = null; 
    res.assignsTo = null; 
    res.parameters = new Vector();
    res.parameters.addAll(pars);   
    res.callExp = expr;
    return res;  
  } 

  public static InvocationStatement newInvocationStatement(
                                       Expression expr, 
                                       Expression par) 
  { InvocationStatement res = 
        new InvocationStatement(expr + ""); 
    res.target = null; 
    res.assignsTo = null; 
    res.parameters = new Vector();
    res.parameters.add(par);   
    res.callExp = expr;
    return res;  
  } 

  public static InvocationStatement newInvocationStatement(
                                       Expression expr) 
  { InvocationStatement res = 
        new InvocationStatement(expr + ""); 
    res.target = null; 
    res.assignsTo = null; 
    res.parameters = new Vector();
    res.callExp = expr;
    return res;  
  } 

  public int execute(ModelSpecification sigma, 
                      ModelState beta)
  { int res = Statement.NORMAL; 

    if (callExp == null) 
    { return res; } 

    if ("skip".equals(callExp + "")) 
    { return res; } 

    if (callExp instanceof BasicExpression)
    { BasicExpression cexpr = (BasicExpression) callExp; 
      Expression obj = cexpr.getObjectRef(); 
      // if null, it is a call on self. 
      String op = cexpr.getData(); 
      Vector actualPars = cexpr.getParameters(); 
      int npars = actualPars.size(); 

      Expression selfobject; 

      if (obj != null) 
      { selfobject = obj.evaluate(sigma, beta); } 
      else 
      { selfobject = beta.getVariableValue("self"); } 

      if (selfobject == null) // error
      { return res; } 

      ObjectSpecification ospec = 
                 sigma.getObjectSpec("" + selfobject);

      if (ospec == null) // error
      { return res; }
 
      Entity ent = ospec.getEntity(); 

      if (ent == null) 
      { return res; } 

      BehaviouralFeature bf = ent.getOperation(op, npars);
      // assume not static:  

      if (bf == null) 
      { return res; } 

      ModelState opstackframe = (ModelState) beta.clone(); 
      opstackframe.addNewEnvironment(); 
      opstackframe.addVariable("self", selfobject); 

      Vector parValues = new Vector(); 
      for (int i = 0; i < actualPars.size(); i++) 
      { Expression pval = (Expression) actualPars.get(i); 
        Expression parval = pval.evaluate(sigma, beta); 
        parValues.add(parval); // could be null; 
      } 

      bf.execute(sigma, opstackframe, parValues);  
    }

    return res; 
  } 

  public Statement removeSlicedParameters(
             BehaviouralFeature op, Vector fpars)
  { // action(parameters) becomes action(pars) where
    // pars are the parameters *not* in the range of 
    // the removed formal parameters fpars of op
    // op.name = action. op is original version of 
    // operation before slicing. 

    // System.out.println("++ Invocation statement " + 
    //                    action + " " + callExp + " " + 
    //                    parameters); 
					   
    if (callExp == null) { return this; }

    Vector oldpars = new Vector(); 
    // Substitute in the callExp, in fact. 
    if (parameters == null || parameters.size() == 0) 
    { Vector callpars = callExp.getParameters(); 
      if (callpars != null)
      { oldpars.addAll(callpars); }
    }  
    else 
    { oldpars.addAll(parameters); } 

    // System.out.println("++ " + 
    //                    op + " " + fpars + " " + 
    //                    oldpars); 

    if (action.equals(op.getName()) || 
        action.startsWith(op.getName() + "(") || 
        action.startsWith("self." + op.getName() + "("))
    { Vector newpars = new Vector(); 
      Vector oppars = op.getParameters(); 
      for (int i = 0; i < oppars.size(); i++) 
      { Attribute att = (Attribute) oppars.get(i); 
        if (fpars.contains(att.getName()))
        { System.out.println("++ Removing parameter " + att); } 
        else 
        { newpars.add(oldpars.get(i)); } 
      } 

      InvocationStatement res =
        new InvocationStatement(action); 
      // res.parameters = newpars; 
      res.callExp = 
        BasicExpression.newCallBasicExpression(
                    "self." + op.getName(),newpars); 
      return res;
    } 
    else 
    { return this; }  
  } // just callExp.removeSlicedParameters(op,fpars)

  public boolean isSkip()
  { if ("skip".equals(action)) 
    { return true; } 
    if ("skip".equals(callExp + "")) 
    { return true; } 
    return false; 
  } 

  public Expression getCallExp()
  { return callExp; } 

  public void setCallExp(Expression e)
  { callExp = e; } 

  public String calledOperation()
  { return action; } 

  public void setAssignsTo(String atype, String avar)
  { assignsType = atype; 
    assignsTo = avar; 
  } 

  public void setEntity(Entity ent)
  { entity = ent; 
    if (callExp != null) 
    { callExp.setEntity(ent); }  
  } 

  public void setParameters(Vector pars)
  { parameters = pars; } 

  public Object clone()
  { InvocationStatement res = 
       new InvocationStatement(action,target,assignsTo);
    res.setCallExp(callExp); // clone it 
    res.setAssignsTo(assignsType,assignsTo); 
    res.entity = entity; 
    return res; 
  } // parameters? 

  public void findClones(java.util.Map clones, String rule, String op)
  { /* if (this.syntacticComplexity() < UCDArea.CLONE_LIMIT) 
    { return; }
    String val = callExp + ""; 
    Vector used = (Vector) clones.get(val);
    if (used == null)  
    { used = new Vector(); }
    if (rule != null)
    { used.add(rule); }
    else if (op != null)
    { used.add(op); }
    clones.put(val,used); */ 
  }

  public void findClones(java.util.Map clones, 
                       java.util.Map cloneDefs,
                       String rule, String op)
  { } 

  public Vector allVariableNames()
  { return callExp.allVariableNames(); } 

  public Statement optimiseOCL()
  { Expression cexp = callExp.simplifyOCL(); 
    return new InvocationStatement(cexp); 
  }  

  public Map energyUse(Map uses, 
                                Vector rUses, Vector oUses)
  { callExp.energyUse(uses, rUses, oUses);  

    int syncomp = callExp.syntacticComplexity(); 
    if (syncomp > TestParameters.syntacticComplexityLimit)
    { System.err.println("!!! Code smell (MEL): too high expression complexity (" + syncomp + ") for " + callExp); 
      System.err.println(">>> Recommend OCL refactoring"); 
    } 

    return uses; 
  }  

  public java.util.Map collectionOperatorUses(
                             int nestingLevel, 
                             java.util.Map operatorsAtLevel, 
                             Vector vars)
  { callExp.collectionOperatorUses(nestingLevel, 
                                   operatorsAtLevel, vars); 
    return operatorsAtLevel; 
  }  


  public void findMagicNumbers(java.util.Map mgns, String rule, String op)
  { String val = callExp + ""; 
    callExp.findMagicNumbers(mgns, val, op); 
  }

  public Statement dereference(BasicExpression var)
  { InvocationStatement res = new InvocationStatement(action,target,assignsTo); 
    if (callExp != null) 
    { res.setCallExp(callExp.dereference(var)); }
    res.entity = entity; 
    return res; 
  }  // parameters? 


  public Statement substituteEq(String oldE, Expression newE)
  { String act = action; 
    String targ = target; 
    String ast = assignsTo; 
    
    if (target != null && target.equals(oldE))
    { targ = newE.toString(); } 

    if (assignsTo != null && assignsTo.equals(oldE))
    { ast = newE.toString(); }

    InvocationStatement res = 
        new InvocationStatement(act,targ,ast);
    res.entity = entity;

    if (parameters != null) 
    { Vector newpars = new Vector(); 
      for (int i = 0; i < parameters.size(); i++) 
      { Expression oldpar = (Expression) parameters.get(i); 
        Expression newpar = oldpar.substituteEq(oldE,newE);
        newpars.add(newpar); 
      } 
      res.setParameters(newpars); 
    } 

  
    if (callExp != null)
    { Expression newce = callExp.substituteEq(oldE,newE); 
      res.setCallExp(newce);
    }

    return res; 
  }  

  public Statement addContainerReference(
                                  BasicExpression ref,
                                  String var, Vector excludes)
  {  
    String act = action; 
    String targ = target; 
    String ast = assignsTo; 
    
    if (target != null && excludes.contains(target))
    { } 
    else 
    { targ = ref + "." + target; } 

    if (assignsTo != null && excludes.contains(assignsTo))
    { } 
    else 
    { ast = ref + "." + assignsTo; }

    InvocationStatement res = 
        new InvocationStatement(act,targ,ast);
    res.entity = entity;

    if (parameters != null) 
    { Vector newpars = new Vector(); 
      for (int i = 0; i < parameters.size(); i++) 
      { Expression oldpar = (Expression) parameters.get(i); 
        Expression newpar = oldpar.addContainerReference(
                                               ref,var,
                                               excludes);
        newpars.add(newpar); 
      } 
      res.setParameters(newpars); 
    } 
  
    if (callExp != null)
    { Expression newce = callExp.addContainerReference(
                                         ref,var,excludes); 
      res.setCallExp(newce);
    }

    return res; 
  }  

  public String toStringB()  /* B display */  
  { String res = ""; 
    if (assignsTo != null) 
    { res = assignsTo + " <-- "; } 
    res = res + action; 
    if (target != null)   /* Instance of multiple component */ 
    { res = res + "(" + target + ")"; } 
    return res; 
  } 

  public String toString()   
  { String res = ""; 
    // if (assignsTo != null) 
    // { res = assignsTo + " <-- "; } 
    res = res + callExp; 
    return res; 
  } 

  public String toAST()
  { String res = "(OclStatement call " + callExp.toAST() + " )"; 

    // if (brackets)
    // { res = "(OclStatement ( " + res + " ) )"; }

    return res;  
  } 

  public boolean containsSubexpression(Expression expr) 
  { return callExp.containsSubexpression(expr); } 

  public Vector singleMutants()
  { Vector res = new Vector(); 
    Vector exprs = callExp.singleMutants(); 
    for (int i = 0; i < exprs.size(); i++) 
    { Expression mut = (Expression) exprs.get(i); 
      res.add(new InvocationStatement(mut));
    }  
    return res; 
  } 


  public String saveModelData(PrintWriter out)
  { String res = Identifier.nextIdentifier("operationcallstatement_"); 
    out.println(res + " : OperationCallStatement"); 
    out.println(res + ".statId = \"" + res + "\""); 

    if (assignsTo != null) 
    { out.println(res + ".assignsTo = " + assignsTo); } 

    if (callExp != null)
    { String callid = callExp.saveModelData(out); 
      out.println(res + ".callExp = " + callid);
    }

    return res; 
  } 

  public String saveModelData(PrintWriter out, Entity ent)
  { return saveModelData(out); }

  public String bupdateForm()
  { return toString(); } 

  public BStatement bupdateForm(java.util.Map env, boolean local)
  { if (callExp != null)
    { if (callExp instanceof BasicExpression)
      { BasicExpression cex = (BasicExpression) callExp; 
        String callString = cex.data; 
        BExpression uf = cex.objectRef.binvariantForm(env,local);
        Vector pars = new Vector(); 
        pars.add(uf); 
        return new BOperationCall(callString, pars);
      }
    } 
    return new BBasicStatement("skip"); 
  }  

  public String toStringJava() 
  { String res = ""; 
    if ("skip".equals(action)) { return res; } 
    
    if (assignsTo != null)  
    { res = assignsTo + " = "; }
    if (target != null) 
    { res = res + target + "."; }  
    res = res + action + ";";  
    return res; 
  } 

  public String toStringJava(String targ)
  { String res = "";
    if (assignsTo != null)
    { res = assignsTo + " = "; }
    if (targ != null)        /* Overrides target */ 
    { res = res + targ + "."; }
    res = res + action + ";";
    return res;
  }


  public String toEtl()
  { String res = "";
    if (assignsTo != null)
    { res = assignsTo + " = "; }
    res = res + action + ";";
    return res;
  }


  public void display()
  { 
    System.out.print(toString()); 
  }

  public void display(PrintWriter out)
  { out.print(toString()); }

  public void displayJava(String targ)
  { if (targ != null) 
    { System.out.print(toStringJava(targ)); }
    else 
    { System.out.print(toStringJava()); } 
  }

  public void displayJava(String targ, PrintWriter out)
  { if (targ != null) 
    { out.print(toStringJava(targ)); }
    else 
    { out.print(toStringJava()); }  
  }

  public boolean typeCheck(Vector types, Vector entities, Vector ctxs, Vector env)
  { if (callExp != null)
    { callExp.typeCheck(types,entities,ctxs,env); } 
    return true;
  }  

  public boolean typeInference(Vector types, Vector entities, Vector ctxs, Vector env, java.util.Map vartypes)
  { if (callExp != null)
    { callExp.typeInference(types,entities,ctxs,env,vartypes); } 
    return true;
  } // infer the callee type from the operation called on it 

  public Expression wpc(Expression post)
  { return post; }

  public Expression wpc(Expression inv, Expression post)
  { return inv; }  


  public Vector dataDependents(Vector allvars, Vector vars)
  { if ("skip".equals(callExp + ""))
    { return vars; }

    if (callExp instanceof BasicExpression)
    { BasicExpression be = (BasicExpression) callExp; 
      Vector readVars = be.readData(); 
      // Vector readBEs = be.readBasicExpressionData(); 
      String upd = be.updatedData(); 

      // System.out.println(upd + " --from--> " + readBEs); 
      // System.out.println(">>>--- written data: " + upd); 

      if (upd != null && vars.contains(upd))
      { Vector vbls = VectorUtil.union(vars,readVars); 
        return vbls; 
      } 
    } 
    
    return vars; 
  }    
  // if called object : vars, then all variables/attributes of
  // parameters, plus object.

  public Vector dataDependents(Vector allvars, Vector vars, Map mp, Map dlin)
  { if ("skip".equals(callExp + ""))
    { return vars; }

    if (callExp instanceof BasicExpression)
    { BasicExpression be = (BasicExpression) callExp; 
      Vector readBEs = be.readBasicExpressionData(); 
      Vector readVars = be.readData(); 
      String upd = be.getObjectRef() + ""; 

      if (upd != null) 
      { for (int i = 0; i < readVars.size(); i++) 
        { String rv = "" + readVars.get(i); 
          mp.add_pair(rv, upd);
        }
        
        // System.out.println(upd + " --from--> " + readBEs);

        for (int i = 0; i < readBEs.size(); i++) 
        { String rv = "" + readBEs.get(i); 
          dlin.add_pair(rv, upd);
        } 
      }  
 
      // System.out.println(">>>--- written data: " + upd); 

      if (upd != null && vars.contains(upd))
      { Vector vbls = VectorUtil.union(vars,readVars); 
        return vbls; 
      } 
    } 
    
    return vars; 
  }    
  // if called object : vars, then all variables/attributes of
  // parameters, plus object.

  public boolean updates(Vector v) 
  { if ("skip".equals(callExp + ""))
    { return false; }

    if (callExp instanceof BasicExpression)
    { BasicExpression be = (BasicExpression) callExp; 
      String upd = be.updatedData(); 
      if (upd != null && v.contains(upd))
      { return true; } 
    } 

    return false; 
  } 
  // if called object : vars

  public String updateForm(java.util.Map env, boolean local, 
                           Vector types, Vector entities, 
                           Vector vars)
  { if (callExp != null)
    { if (callExp instanceof BasicExpression)
      { String callString = ((BasicExpression) callExp).data; 
        if ("loadModel".equals(callString))
        { return "    " + callExp + ";"; } 
        if ("saveModel".equals(callString))
        { return "    Controller.inst()." + callExp + ";"; } 
        else
        { String call = assignsType + " " + assignsTo; 
          String uf = callExp.updateForm(env,local);
          if (assignsTo != null && assignsTo.length() > 0)
          { return call + " = " + uf; } 
          else 
          { return "    " + uf; }  
        }
      }
      else 
      { return toStringJava(); }  
    } 
    else 
    { return toStringJava(); }  
  }

  public String deltaUpdateForm(java.util.Map env, boolean local)
  { if (callExp != null)
    { if (callExp instanceof BasicExpression)
      { String callString = ((BasicExpression) callExp).data; 
        if ("loadModel".equals(callString))
        { return "    " + callExp + ";"; } 
        if ("saveModel".equals(callString))
        { return "    Controller.inst()." + callExp + ";"; } 
        else
        { String call = assignsType + " " + assignsTo; 
          String uf = ((BasicExpression) callExp).deltaUpdateForm(env,local);
          if (assignsTo != null && assignsTo.length() > 0)
          { return call + " = " + uf; } 
          else 
          { return "    " + uf; }  
        }
      }
      else 
      { return toStringJava(); }  
    } 
    else 
    { return toStringJava(); }  
  }

  public String updateFormJava6(java.util.Map env, boolean local)
  { if (callExp != null)
    { if (callExp instanceof BasicExpression)
      { String callString = ((BasicExpression) callExp).data; 
        if ("loadModel".equals(callString))
        { return "    " + callExp + ";"; } 
        if ("saveModel".equals(callString))
        { return "    Controller.inst()." + callExp + ";"; } 
        else
        { String call = assignsType + " " + assignsTo; 
          String uf = callExp.updateFormJava6(env,local);
          if (assignsTo != null && assignsTo.length() > 0)
          { return call + " = " + uf; } 
          else 
          { return "    " + uf; }  
        }
      }
      else 
      { return toStringJava(); }  
    } 
    else 
    { return toStringJava(); }  
  }

  public String updateFormJava7(java.util.Map env, boolean local)
  { if (callExp != null)
    { if (callExp instanceof BasicExpression)
      { String callString = ((BasicExpression) callExp).data; 
        if ("loadModel".equals(callString))
        { return "    " + callExp + ";"; } 
        if ("saveModel".equals(callString))
        { return "    Controller.inst()." + callExp + ";"; } 
        else
        { String call = assignsType + " " + assignsTo; 
          String uf = callExp.updateFormJava7(env,local);
          if (assignsTo != null && assignsTo.length() > 0)
          { return call + " = " + uf; } 
          else 
          { return "    " + uf; }  
        }
      }
      else 
      { return toStringJava(); }  
    } 
    else 
    { return toStringJava(); }  
  }


  public String updateFormCSharp(java.util.Map env, boolean local)
  { if (callExp != null)
    { if (callExp instanceof BasicExpression)
      { String callString = ((BasicExpression) callExp).data; 
        if ("loadModel".equals(callString))
        { return "    " + callExp + ";"; } 
        if ("saveModel".equals(callString))
        { return "    Controller.inst()." + callExp + ";"; } 
        else
        { String call = assignsType + " " + assignsTo; 
          String uf = callExp.updateFormCSharp(env,local);
          if (assignsTo != null && assignsTo.length() > 0)
          { return call + " = " + uf; } 
          else 
          { return "    " + uf; }  
        }
      }
      else 
      { return toStringJava(); }  
    } 
    else 
    { return toStringJava(); }  
  }

  public String updateFormCPP(java.util.Map env, boolean local)
  { if (callExp != null)
    { if (callExp instanceof BasicExpression)
      { String callString = ((BasicExpression) callExp).data; 
        if ("loadModel".equals(callString))
        { return "    " + callExp + ";"; } 
        if ("saveModel".equals(callString))
        { return "    Controller::inst->" + callExp + ";"; } 
        else
        { String call = assignsType + " " + assignsTo; 
          String uf = callExp.updateFormCPP(env,local);
          if (assignsTo != null && assignsTo.length() > 0)
          { return call + " = " + uf; } 
          else 
          { return "    " + uf; }  
        }
      }
      else 
      { return toStringJava(); }  
    } 
    else 
    { return toStringJava(); }  
  }
  // But the assignsType needs to be converted to C++, likewise for C#

  public Vector allPreTerms()
  { Vector res = new Vector();
    if (callExp == null) 
    { return res; } 
    return callExp.allPreTerms(); 
  }  

  public Vector allPreTerms(String var)
  { Vector res = new Vector();
    if (callExp == null) 
    { return res; } 
    return callExp.allPreTerms(var); 
  }  

  public Vector readFrame() 
  { Vector res = new Vector();
    if (callExp == null) 
    { return res; } 

    if (callExp instanceof BasicExpression)
    { BasicExpression callbe = (BasicExpression) callExp; 
      String callString = callbe.data;

      if ("skip".equals(callbe + ""))
      { return res; }  

      Vector callpars = callbe.getParameters();
      if (callpars == null) 
      { callpars = new Vector(); } 

      for (int i = 0; i < callpars.size(); i++) 
      { Expression callpar = (Expression) callpars.get(i); 
        res.addAll(callpar.allReadFrame()); 
      } 
 
      if (entity != null) 
      { BehaviouralFeature op = entity.getDefinedOperation(callString); 
        if (op != null) 
        { Expression post = op.getPost(); 
          Vector params = op.getParameters(); 
          if (params == null) 
          { params = new Vector(); } 

          Vector postrd = post.allReadFrame(); 
          // subtract each params name:
          res.addAll(postrd);  

          Vector parstrings = new Vector(); 
          for (int p = 0; p < params.size(); p++) 
          { String par = "" + params.get(p); 
            parstrings.add(par); 
          } 
          res.removeAll(parstrings); 
        }
        // System.out.println("Invocation " + callString + " READ FRAME= " + res); 
        return res; 
      } 
    }   
    return callExp.allReadFrame(); 
  } 

  public Vector writeFrame() 
  { Vector res = new Vector();
    if (callExp == null) 
    { return res; } 
    if (callExp instanceof BasicExpression)
    { BasicExpression callbe = (BasicExpression) callExp; 
      String callString = callbe.data;
      Vector callpars = callbe.getParameters();
      if (callpars == null) 
      { callpars = new Vector(); } 
      
      if (entity != null) 
      { BehaviouralFeature op = 
          entity.getDefinedOperation(callString); 
        if (op != null) 
        { Expression post = op.getPost(); 
          Vector params = op.getParameters(); 
          Vector postrd = post.writeFrame(); 
          // subtract each params name:
          res.addAll(postrd);  

          Vector parstrings = new Vector(); 
          for (int p = 0; p < params.size(); p++) 
          { String par = "" + params.get(p); 
            parstrings.add(par); 
          } 

          res.removeAll(parstrings); 
        }
        // System.out.println("Invocation " + callString + " WRITE FRAME= " + res); 

        return res; 
      } 
    }   

    return res; 
  } 

  public Statement checkConversions(Entity e, Type propType, Type propElemType, java.util.Map interp)
  { return this; } 

  public Statement replaceModuleReferences(UseCase uc)
  { if (callExp == null) { return this; } 
    BasicExpression ce = (BasicExpression) callExp.replaceModuleReferences(uc);
    return new InvocationStatement(ce); 
  } 

  public int syntacticComplexity()
  { if (callExp == null) 
    { return 1; } 

    int syncomp = callExp.syntacticComplexity(); 

    return syncomp + 1; 
  } 

  public int cyclomaticComplexity()
  { return 0; }  // no predicate nodes

  public int epl()
  { return 0; }  

  public Vector allOperationsUsedIn()
  { Vector res = new Vector(); 
    if (callExp == null) 
    { return res; } 
    return callExp.allOperationsUsedIn(); 
  } 

  public Vector getUses(String var) 
  { if (callExp != null) 
    { return callExp.getUses(var); } 
    return new Vector(); 
  } 

  public Vector getVariableUses() 
  { if (callExp != null) 
    { return callExp.getVariableUses(); } 
    return new Vector(); 
  } 

  public Vector getVariableUses(Vector unused) 
  { if (callExp != null) 
    { return callExp.getVariableUses(); } 
    return new Vector(); 
  } 

  public Vector allAttributesUsedIn() 
  { if (callExp != null) 
    { return callExp.allAttributesUsedIn(); } 
    return new Vector(); 
  } 

  public Vector equivalentsUsedIn()
  { Vector res = new Vector(); 
    if (callExp == null) 
    { return res; } 
    return callExp.equivalentsUsedIn(); 
  } 

  public Vector metavariables()
  { Vector res = new Vector();
    if (callExp != null) 
    { return callExp.metavariables(); }  
    return res; 
  } 

  public String cg(CGSpec cgs)
  { String etext = this + "";
    if (etext.equals("skip")) 
    { etext = ""; }
	
    Vector args = new Vector();
   /* if (callExp != null && callExp instanceof BasicExpression) 
    { String res = ""; 
      BasicExpression call = (BasicExpression) callExp; 
      Vector pps = call.getParameters();
      String parstring = "(";  
      if (pps != null) 
      { for (int i = 0; i < pps.size(); i++) 
        { Expression par = (Expression) pps.get(i);
	     parstring = parstring + par.cg(cgs); 
	     if (i < pps.size() - 1) 
	      { parstring = parstring + ","; }
	    }
	  }
	  parstring = parstring + ")";
	   
	  if (call.getObjectRef() != null)
	  { res = "    " + call.objectRef.cg(cgs) + "." + call.getData() + parstring + ";\n"; }
	  else 
	  { res = "    " + call.getData() + parstring + ";\n"; }
	  return res; 
	}   
	else */ 
    if (callExp != null) 
    { args.add(callExp.cg(cgs));  
      Vector eargs = new Vector();
      eargs.add(callExp);  
      CGRule r = cgs.matchedStatementRule(this,etext);

    System.out.println(">> Matched statement rule: " + r + " for " + this); 

      if (r != null)
      { return r.applyRule(args,eargs,cgs); }
    } 
    else 
    { CGRule r1 = cgs.matchedStatementRule(this,""); 
      Vector eargs = new Vector();

    System.out.println(">> Matched statement rule: " + r1 + " for " + this); 

      if (r1 != null) 
      { return r1.applyRule(args,eargs,cgs); } 
    } 
    return etext;
  }

  public Vector cgparameters()
  { Vector args = new Vector();
    if (callExp != null) 
    { args.add(callExp); } 
    return args; 
  } 

}


class ImplicitInvocationStatement extends Statement
{ Expression callExp; 


  public ImplicitInvocationStatement(Expression ee)
  { callExp = ee; } 

  public ImplicitInvocationStatement(String ss)
  { callExp = new BasicExpression(ss); } 

  public void setEntity(Entity ent)
  { entity = ent; 
    callExp.setEntity(ent); 
  } 

  public String getOperator() 
  { return "execute"; } 

  public Expression getCallExp() 
  { return callExp; } 

  public boolean isSkip()
  { if ("true".equals(callExp + "")) 
    { return true; } 
    return false; 
  } 

  public Object clone()
  { ImplicitInvocationStatement res = 
      new ImplicitInvocationStatement(callExp);
    res.entity = entity; 
    return res; 
  } 

  public void findClones(java.util.Map clones, String rule, String op)
  { if (callExp == null || 
        callExp.syntacticComplexity() < UCDArea.CLONE_LIMIT) 
    { return; }
    /* String val = callExp + ""; 
    Vector used = (Vector) clones.get(val);
    if (used == null)  
    { used = new Vector(); }
    if (rule != null)
    { used.add(rule); }
    else if (op != null)
    { used.add(op); }
    clones.put(val,used); */ 
    callExp.findClones(clones,rule,op); 
  }

  public void findClones(java.util.Map clones, 
                         java.util.Map cloneDefs,
                         String rule, String op)
  { if (callExp == null || 
        callExp.syntacticComplexity() < UCDArea.CLONE_LIMIT) 
    { return; }
    callExp.findClones(clones,cloneDefs,rule,op); 
  }

  public Vector allVariableNames()
  { return callExp.allVariableNames(); } 

  public Map energyUse(Map uses, 
                                Vector rUses, Vector oUses)
  { callExp.energyUse(uses, rUses, oUses); 

    int syncomp = callExp.syntacticComplexity(); 
    if (syncomp > TestParameters.syntacticComplexityLimit)
    { System.err.println("!!! Code smell (MEL): too high expression complexity (" + syncomp + ") for " + callExp); 
      System.err.println(">>> Recommend OCL refactoring");
      System.err.println();  
    } 

    return uses; 
  }  

  public java.util.Map collectionOperatorUses(
                             int nestingLevel, 
                             java.util.Map operatorsAtLevel, 
                             Vector vars)
  { callExp.collectionOperatorUses(nestingLevel, 
                                   operatorsAtLevel, vars); 
    return operatorsAtLevel; 
  }  

  public void findMagicNumbers(java.util.Map mgns, String rule, String op)
  { callExp.findMagicNumbers(mgns, this + "", op); } 

  public Statement dereference(BasicExpression var)
  { ImplicitInvocationStatement res = 
      new ImplicitInvocationStatement(callExp.dereference(var));
    res.entity = entity; 
    return res; 
  } 

  public int execute(ModelSpecification sigma, ModelState beta)
  { callExp.execute(sigma, beta); 
    return Statement.NORMAL; 
  }
 
  public Statement substituteEq(String oldE, Expression newE)
  { Expression newExp = callExp.substituteEq(oldE,newE); 

    return new ImplicitInvocationStatement(newExp); 
  } 

  public Statement removeSlicedParameters(
             BehaviouralFeature op, Vector fpars)
  { Expression newExp = 
      callExp.removeSlicedParameters(op,fpars); 
    return new ImplicitInvocationStatement(newExp); 
  } 

  public Statement addContainerReference(
                      BasicExpression ref, String var,
                      Vector excl)
  { Expression newExp = 
        callExp.addContainerReference(ref,var,excl); 

    return new ImplicitInvocationStatement(newExp); 
  } 

  public Statement optimiseOCL()
  { Expression cexp = callExp.simplifyOCL(); 
    return new ImplicitInvocationStatement(cexp); 
  }

  public String toString()  /* B display */  
  { String res = "execute ( " + callExp + " )"; 
    return res; 
  } 

  public String toAST()
  { String res = "(OclStatement execute " + callExp.toAST() + " )"; 

    // if (brackets)
    // { res = "(OclStatement ( " + res + " ) )"; } 

    return res; 
  } 

  public boolean containsSubexpression(Expression expr) 
  { return callExp.containsSubexpression(expr); } 


  public Vector singleMutants()
  { Vector res = new Vector(); 
    Vector exprs = callExp.singleMutants(); 
    for (int i = 0; i < exprs.size(); i++) 
    { Expression expr = (Expression) exprs.get(i); 
      res.add(new ImplicitInvocationStatement(expr)); 
    } 
    return res; 
  } 


  public String saveModelData(PrintWriter out)
  { String res = Identifier.nextIdentifier("implicitcallstatement_"); 
    out.println(res + " : ImplicitCallStatement"); 
    out.println(res + ".statId = \"" + res + "\""); 

    if (callExp != null)
    { String callid = callExp.saveModelData(out); 
      out.println(res + ".callExp = " + callid);
    }

    return res; 
  } 

  public String saveModelData(PrintWriter out, Entity ent)
  { return saveModelData(out); }


  public String bupdateForm()
  { return " " + callExp; }   
  // ANY vars' WHERE callExp[vars'/vars] THEN vars := vars' 
  // where vars are variables of callExp 

  public BStatement bupdateForm(java.util.Map env, boolean local)
  { return callExp.bupdateForm(env,local); 
    /* Vector fs = callExp.allFeaturesUsedIn(); 
    BExpression qual = callExp.binvariantForm(env,local);
    BParallelStatement bps = new BParallelStatement(); 
    Vector newfs = new Vector(); 
    for (int i = 0; i < fs.size(); i++) 
    { String feat = (String) fs.get(i); 
      String featnew = feat + "_new"; 
      newfs.add(featnew); 
      BBasicExpression newbfeat = new BBasicExpression(featnew); 
      qual = qual.substituteEq(feat,newbfeat);
      bps.addStatement(new BAssignStatement(new BBasicExpression(feat), newbfeat));  
    }  
    return new BAnyStatement(newfs,qual,bps); */     
  } 

  public String toStringJava() 
  { String res = "execute ( " + callExp + " )"; 
    return res; 
  } 

  public String toEtl() 
  { String res = "  " + callExp + ";"; 
    return res; 
  } 


  public String toStringJava(String targ)
  { return toStringJava(); }


  public void display()
  { 
    System.out.print(toString()); 
  }

  public void display(PrintWriter out)
  { out.print(toString()); }

  public void displayJava(String targ)
  { if (targ != null) 
    { System.out.print(toStringJava(targ)); }
    else 
    { System.out.print(toStringJava()); } 
  }

  public void displayJava(String targ, PrintWriter out)
  { if (targ != null) 
    { out.print(toStringJava(targ)); }
    else 
    { out.print(toStringJava()); }  
  }

  public boolean typeCheck(Vector types, Vector entities, Vector ctxs, Vector env)
  { if (callExp != null)
    { callExp.typeCheck(types,entities,ctxs,env); } 
    return true;
  }  

  public boolean typeInference(Vector types, Vector entities, Vector ctxs, Vector env, java.util.Map vartypes)
  { if (callExp != null)
    { callExp.typeInference(types,entities,
                            ctxs,env,vartypes); 
    } 
    return true;
  }  

  public Expression wpc(Expression post)
  { return post; }

  public Expression wpc(Expression inv, Expression post)
  { return inv; }  

  public Vector dataDependents(Vector allvars, Vector vars)
  { return vars; }  // assuming no side-effect

  public Vector dataDependents(Vector allvars, Vector vars, Map mp, Map dlin)
  { return vars; }  // assuming no side-effect

  public boolean updates(Vector v) 
  { return false; } 

  public Statement generateDesign(java.util.Map env, boolean local)
  { return callExp.generateDesign(env,local); }  

  public Statement statLC(java.util.Map env, boolean local)
  { return callExp.statLC(env,local); }  

  public String updateForm(java.util.Map env, 
                      boolean local, 
                      Vector types, Vector entities, 
                      Vector vars)
  { if (callExp != null)
    { String uf = callExp.updateForm(env,local);
      return "   " + uf;   
    } 
    else 
    { return toStringJava(); }  
  }

  public String updateFormJava6(java.util.Map env, boolean local)
  { if (callExp != null)
    { String uf = callExp.updateFormJava6(env,local);
      return "   " + uf;   
    } 
    else 
    { return toStringJava(); }  
  }

  public String updateFormJava7(java.util.Map env, boolean local)
  { if (callExp != null)
    { String uf = callExp.updateFormJava7(env,local);
      return "   " + uf;   
    } 
    else 
    { return toStringJava(); }  
  }


  public String updateFormCSharp(java.util.Map env, boolean local)
  { if (callExp != null)
    { String uf = callExp.updateFormCSharp(env,local);
      return "   " + uf;   
    } 
    else 
    { return toStringJava(); }  
  }

  public String updateFormCPP(java.util.Map env, boolean local)
  { if (callExp != null)
    { String uf = callExp.updateFormCPP(env,local);
      return "   " + uf;   
    } 
    else 
    { return toStringJava(); }  
  }


  public Vector allPreTerms()
  { Vector res = new Vector();
    if (callExp == null) 
    { return res; } 
    return callExp.allPreTerms(); 
  }  

  public Vector allPreTerms(String var)
  { Vector res = new Vector();
    if (callExp == null) 
    { return res; } 
    return callExp.allPreTerms(var); 
  }  

  public Vector readFrame() 
  { Vector res = new Vector();
    if (callExp == null) 
    { return res; } 
    return callExp.readFrame(); 
  } 

  public Vector writeFrame() 
  { Vector res = new Vector();
    if (callExp == null) 
    { return res; } 
    return callExp.writeFrame(); 
  } 

  public Statement checkConversions(Entity e, Type propType, Type propElemType, java.util.Map interp)
  { return this; } 

  public Statement replaceModuleReferences(UseCase uc)
  { if (callExp == null) { return this; } 
    Expression ce = callExp.replaceModuleReferences(uc);
    return new ImplicitInvocationStatement(ce); 
  } 

  public int syntacticComplexity()
  { if (callExp == null) 
    { return 1; } 

    int syncomp = callExp.syntacticComplexity(); 
    return syncomp + 1; 
  } 

  public int cyclomaticComplexity()
  { return 0; }  // no predicate nodes

  public int epl()
  { return 0; }  

  public Vector allOperationsUsedIn()
  { Vector res = new Vector(); 
    if (callExp == null) 
    { return res; } 
    return callExp.allOperationsUsedIn(); 
  } 

  public Vector getUses(String var) 
  { if (callExp != null) 
    { return callExp.getUses(var); } 
    return new Vector();
  } 

  public Vector getVariableUses() 
  { if (callExp != null) 
    { return callExp.getVariableUses(); } 
    return new Vector();
  } 

  public Vector getVariableUses(Vector unused) 
  { if (callExp != null) 
    { return callExp.getVariableUses(); } 
    return new Vector();
  } 

  public Vector allAttributesUsedIn() 
  { if (callExp != null) 
    { return callExp.allAttributesUsedIn(); } 
    return new Vector(); 
  } 

  public Vector equivalentsUsedIn()
  { Vector res = new Vector(); 
    if (callExp == null) 
    { return res; } 
    return callExp.equivalentsUsedIn(); 
  } 

  public Vector metavariables()
  { Vector res = new Vector();
    if (callExp != null) 
    { return callExp.metavariables(); }  
    return res; 
  } 

  public String cg(CGSpec cgs)
  { 
    String etext = this + "";
    
    Vector eargs = new Vector();
    Vector args = new Vector();

    if (callExp != null) 
    { callExp.setBrackets(false); 
      eargs.add(callExp); 
      args.add(callExp.cg(cgs)); 
    }
 
    CGRule r = cgs.matchedStatementRule(this,etext);

    System.out.println(">> Matched statement rule: " + r + " for " + this); 

    if (r != null)
    { return r.applyRule(args,eargs,cgs); }

    java.util.Map env = new java.util.HashMap(); 
    Statement stat = callExp.generateDesign(env,true); 
    if (stat != null) 
    { return stat.cg(cgs); } 
  
    return etext;
  } 

  public Vector cgparameters()
  { Vector args = new Vector();
    if (callExp != null) 
    { args.add(callExp); } 
    return args; 
  } 
}



class WhileStatement extends Statement
{ private Expression loopTest; 
  private Statement body; 
  // also need invariant and variant for B
  private Expression invariant; 
  private Expression variant; 
  int loopKind = WHILE; 
  // FOR for a for loop, REPEAT for repeat ... until
  
  Expression loopVar; // for (loopVar : loopRange) do ...
  Expression loopRange; 

  public WhileStatement()
  { loopTest = new BasicExpression(true); 
    body = new InvocationStatement("skip");
  } 

  public WhileStatement(Expression e, Statement b)
  { loopTest = e; 
    if (b == null) 
    { body = new InvocationStatement("skip"); } 
    else 
    { body = b;
      body.setBrackets(true);
    } 
  } 

  public WhileStatement(Expression e, Vector b)
  { loopTest = e; 
    if (b == null || b.size() == 0) 
    { body = new InvocationStatement("skip"); } 
    else if (b.size() == 1)
    { body = (Statement) b.get(0);
      body.setBrackets(true);
    }
    else 
    { body = new SequenceStatement(b);
      body.setBrackets(true);
    }  
  } 

  public WhileStatement(Expression lv, Expression lr, 
                        Vector b)
  { 
    if (lv instanceof SetExpression &&
        lr.isMap()) // [k,v] : map
    { SetExpression sv = (SetExpression) lv; 
      if (sv.size() == 2)
      { lv = (Expression) sv.getElement(0); 
        Expression mv = (Expression) sv.getElement(1); 
        UnaryExpression kys = 
          new UnaryExpression("->keys",lr); 
        kys.setType(new Type("Set", null)); 
        // kys.setElementType(lr.getKeyType());  
        loopTest = 
          new BinaryExpression(":", lv, kys); 
        loopTest.setType(new Type("boolean", null)); 
        CreationStatement cs = 
          new CreationStatement(mv,lr.getElementType());
        Expression lrAtlv = 
          new BinaryExpression("->at", lr, lv);  
        AssignStatement asgn = 
          new AssignStatement(mv,lrAtlv); 
        b.add(0,asgn); 
        b.add(0,cs); 
      }  
      else 
      { loopTest = new BinaryExpression(":", lv, lr);
        loopTest.setType(new Type("boolean", null)); 
      } 
    } 
    else 
    { loopTest = new BinaryExpression(":", lv, lr);
      loopTest.setType(new Type("boolean", null)); 
    } 

    loopKind = FOR;  
    if (b == null || b.size() == 0) 
    { body = new InvocationStatement("skip"); } 
    else if (b.size() == 1)
    { body = (Statement) b.get(0); 
      body.setBrackets(true);
    }
    else 
    { body = new SequenceStatement(b);
      body.setBrackets(true);
    }  
    loopVar = lv;
    loopRange = lr;
  } 

  public WhileStatement(Expression lv, Expression lr, 
                        Statement stat)
  { loopTest = new BinaryExpression(":", lv, lr);
    loopTest.setType(new Type("boolean", null)); 

    loopKind = FOR;  
    body = stat;
    body.setBrackets(true);
    loopVar = lv;
    loopRange = lr;
  } 

  public String getOperator() 
  { if (loopKind == WHILE) 
    { return "while"; }
    else if (loopKind == REPEAT)
    { return "repeat"; } 
    return "for"; 
  } 

  public Expression getLoopTest()
  { return loopTest; } 

  public void setTest(Expression tst)
  { loopTest = tst;

    if (loopRange == null &&
        loopTest != null && 
        loopTest instanceof BinaryExpression)
    { BinaryExpression bexpr = (BinaryExpression) loopTest; 
      if (bexpr.getOperator().equals(":") && 
          (loopVar + "").equals(bexpr.getLeft() + ""))
      { loopRange = bexpr.getRight(); } 
    } // only meaningful for FOR loops. 
  } 

  public void setBody(Statement stat)
  { body = stat; } 

  public void setLoopKind(int lk)
  { loopKind = lk; } 

  public void setLoopRange(Expression lv, Expression lr)
  { loopVar = lv;
    loopRange = lr;
  }

  public void setLoopVar(Expression lv)
  { loopVar = lv; }

  public void setLoopRangeVarFromTest(Expression expr)
  { if (expr != null && 
        expr instanceof BinaryExpression)
    { BinaryExpression binexpr = (BinaryExpression) expr; 
      loopVar = binexpr.getLeft(); 
      loopRange = binexpr.getRight(); 
    } 
  } 

  public void setIterationRange(Expression expr)
  { loopRange = expr; } 

  public void setEntity(Entity e)
  { entity = e; 
    if (body != null) 
    { body.setEntity(e); } 
  }

  public Statement getBody()
  { return body; } 

  public Statement getLoopBody()
  { return body; } 

  public Expression getLoopVar()
  { return loopVar; } 

  public Expression getTest()
  { return loopTest; } 

  public Object clone()
  { Expression lv = null; 
    if (loopVar != null) 
    { lv = (Expression) loopVar.clone(); }  
    Expression lr = null; 
    if (loopRange != null) 
    { lr = (Expression) loopRange.clone(); }  
    Expression lt = null; 
    if (loopTest != null) 
    { lt = (Expression) loopTest.clone(); }  
    Statement newbody = (Statement) body.clone(); 
    WhileStatement res = new WhileStatement(lt,newbody); 
    res.setEntity(entity); 
    res.setLoopKind(loopKind); 
    res.setLoopRange(lv,lr); 
    res.setBrackets(brackets); 
    Expression inv = null; 
    if (invariant != null) 
    { inv = (Expression) invariant.clone(); }  
    res.setInvariant(inv); 
    Expression var = null; 
    if (variant != null) 
    { var = (Expression) variant.clone(); }  
    res.setVariant(var); 

    return res; 
  } 

  public int execute(ModelSpecification sigma, 
                      ModelState beta)
  { int res = Statement.NORMAL; 

    if (loopKind == Statement.WHILE)
    { Expression testvalue = 
         loopTest.evaluate(sigma, beta); 
      while ("true".equals(testvalue + ""))
      { res = body.execute(sigma, beta);
        System.out.println("---> iteration of while loop: " + sigma + ", " + beta + " " + res);

        if (res == Statement.BREAK)
        { return Statement.NORMAL; } 

        if (res == Statement.RETURN)
        { return res; }   

        testvalue = loopTest.evaluate(sigma, beta); 
      } 

      return Statement.NORMAL; 
    } 
    else if (loopKind == Statement.REPEAT)
    { res = body.execute(sigma, beta); 

      if (res == Statement.BREAK)
      { return Statement.NORMAL; } 

      if (res == Statement.RETURN)
      { return res; }   

      Expression testvalue = 
         loopTest.evaluate(sigma, beta); 
      while ("false".equals(testvalue + ""))
      { res = body.execute(sigma, beta);
        System.out.println("---> iteration of repeat loop: " + sigma + ", " + beta + " " + res);

        if (res == Statement.BREAK)
        { return Statement.NORMAL; } 

        if (res == Statement.RETURN)
        { return res; }   
  
        testvalue = loopTest.evaluate(sigma, beta); 
      }

      return Statement.NORMAL;  
    } 
    else if (loopKind == Statement.FOR)
    { Expression rng = loopRange.evaluate(sigma, beta); 
      // must be a SetExpression
      if (rng instanceof SetExpression)
      { SetExpression serange = (SetExpression) rng;   
        int n = serange.size();     

        // ModelState local = (ModelState) beta.clone(); 

        String lv = "" + loopVar; 
        beta.addNewEnvironment(); 
        beta.addVariable(lv, new BasicExpression("null")); 
   
        for (int i = 0; i < n; i++) 
        { Expression val = serange.getElement(i); 
          beta.setVariableValue(lv, val); 
          res = body.execute(sigma, beta); 
          System.out.println("---> iteration of for loop: " + sigma + ", " + beta + " " + res);

          if (res == Statement.BREAK)
          { return Statement.NORMAL; } 

          if (res == Statement.RETURN)
          { return res; }   
        } 
     
        beta.removeLastEnvironment();

        return Statement.NORMAL;  
      } 
    } 

    return Statement.NORMAL; 
  } 


  public Statement loopContinuation()
  { // FOR i : Integer.subrange(a,b) loop: 
    //   while i < b do (i := i + 1; loopBody)
    // Other loops, just the loop itself

    if (loopKind == Statement.FOR)
    { Expression lv = null; 
      if (loopVar != null) 
      { lv = (Expression) loopVar.clone(); } 
 
      BasicExpression lr = null; 
      if (loopRange != null && 
          (loopRange + "").startsWith("Integer.subrange(") &&
          loopRange.getParameters() != null) 
      { lr = (BasicExpression) loopRange.clone(); 
        Expression par2 = lr.getParameter(2); 
        Expression newtest = new BinaryExpression("<", lv, par2); 
        Statement newassign = 
           new AssignStatement(lv, 
             new BinaryExpression("+", lv, 
                                  new BasicExpression(1)));  
        SequenceStatement newbody = new SequenceStatement(); 
        newbody.addStatement(newassign); 
        newbody.addStatement(body); 
        WhileStatement ws = 
           new WhileStatement(newtest, newbody); 
        ws.setLoopKind(Statement.WHILE); 
        return ws; 
      } 
    } 
  
    return (Statement) this.clone(); 
  } 

  public Statement dereference(BasicExpression var)
  { Expression lv = null; 
    if (loopVar != null) 
    { lv = (Expression) loopVar.clone(); }  
    Expression lr = null; 
    if (loopRange != null) 
    { lr = (Expression) loopRange.dereference(var); }  
    Expression lt = null; 
    if (loopTest != null) 
    { lt = (Expression) loopTest.dereference(var); }
    if ((var + "").equals(loopVar + ""))
    { WhileStatement res1 = new WhileStatement(lt,body); 
      res1.setEntity(entity); 
      res1.setLoopKind(loopKind); 
      res1.setLoopRange(lv,lr); 
      res1.setBrackets(brackets); 
      res1.setInvariant(invariant); 
      res1.setVariant(variant); 
      return res1; 
    } 
    Statement newbody = (Statement) body.dereference(var); 
    WhileStatement res = new WhileStatement(lt,newbody); 
    res.setEntity(entity); 
    res.setLoopKind(loopKind); 
    res.setLoopRange(lv,lr); 
    res.setBrackets(brackets); 
    Expression inv = null; 
    if (invariant != null) 
    { inv = (Expression) invariant.dereference(var); }  
    res.setInvariant(inv); 
    Expression vv = null; 
    if (variant != null) 
    { vv = (Expression) variant.dereference(var); }  
    res.setVariant(vv); 

    return res; 
  } 

  public Expression definedness()
  { Expression rtest = new BasicExpression(true); 

    if (loopRange != null) 
    { rtest = loopRange.definedness(); } 
    else if (loopTest != null) 
    { rtest = loopTest.definedness(); } 
     
    Expression bdef = body.definedness(); 

    return Expression.simplify("&", rtest, bdef, null); 
  } 

  public void findClones(java.util.Map clones, String rule, String op)
  { if (loopRange != null && 
        loopRange.syntacticComplexity() >= UCDArea.CLONE_LIMIT) 
    { /* String val = loopRange + ""; 
      Vector used = (Vector) clones.get(val);
      if (used == null)  
      { used = new Vector(); }
      if (rule != null)
      { used.add(rule); }
      else if (op != null)
      { used.add(op); }
      clones.put(val,used); */ 

      loopRange.findClones(clones,rule,op); 
    }  
    else if (loopTest != null && 
        loopTest.syntacticComplexity() >= UCDArea.CLONE_LIMIT) 
    { /* String val = loopTest + ""; 
      Vector used = (Vector) clones.get(val);
      if (used == null)  
      { used = new Vector(); }
      if (rule != null)
      { used.add(rule); }
      else if (op != null)
      { used.add(op); }
      clones.put(val,used); */ 
     
      loopTest.findClones(clones,rule,op); 
    } 
    body.findClones(clones,rule,op); 
  }

  public void findClones(java.util.Map clones, 
                         java.util.Map cdefs,
                         String rule, String op)
  { if (loopRange != null && 
        loopRange.syntacticComplexity() >= 
                                UCDArea.CLONE_LIMIT) 
    { loopRange.findClones(clones,cdefs,rule,op); }  
    else if (loopTest != null && 
        loopTest.syntacticComplexity() >= UCDArea.CLONE_LIMIT) 
    { 
      loopTest.findClones(clones,cdefs,rule,op); 
    } 
    body.findClones(clones,cdefs,rule,op); 
  }

  public Map energyUse(Map uses, Vector rUses, Vector aUses)
  { if (loopRange != null) 
    { loopRange.energyUse(uses,rUses,aUses); }
    else if (loopTest != null)
    { loopTest.energyUse(uses,rUses,aUses); }
  
    body.energyUse(uses,rUses,aUses);

    if (loopKind == FOR) 
    { if (loopRange != null) 
      { int rcomp = loopRange.syntacticComplexity();

        if (rcomp > TestParameters.syntacticComplexityLimit)
        { int acount = (int) uses.get("amber"); 
          uses.set("amber", acount + 1); 
          aUses.add("! Code smell (MEL): too high expression complexity (" + rcomp + ") for " + loopRange + "\n" +  
                    ">>> Recommend OCL refactoring"); 
        } 
      } 
    }
    else if (loopTest != null)
    { int syncomp = loopTest.syntacticComplexity(); 
      if (syncomp > TestParameters.syntacticComplexityLimit)
      { int acount = (int) uses.get("amber"); 
        uses.set("amber", acount + 1); 
        aUses.add("! Code smell (MEL): too high expression complexity (" + syncomp + ") for " + loopTest + "\n" +  
                  ">>> Recommend OCL refactoring"); 
      }
    }  

    if (loopKind == WHILE || loopKind == REPEAT)
    { if (loopTest != null && loopKind == WHILE &&
          "true".equals("" + loopTest)) 
      { int rcount = (int) uses.get("red"); 
        uses.set("red", rcount + 1); 
        rUses.add("!!! Unbounded while loop with true condition: may not terminate!: " + this); 
      }
      else if (loopTest != null && loopKind == REPEAT &&
               "false".equals("" + loopTest)) 
      { int rcount = (int) uses.get("red"); 
        uses.set("red", rcount + 1); 
        rUses.add("!!! Unbounded repeat loop with false condition: may not terminate!: " + this); 
      }
      else 
      { int acount = (int) uses.get("amber"); 
        uses.set("amber", acount + 1); 
        aUses.add("! Unbounded loops can be inefficient: " + 
                  this + 
                  "\n>> Recommend replacing by a bounded loop");
      }  
    } 

    if (Statement.hasLoopStatement(body))
    { int rcount = (int) uses.get("amber"); 
      uses.set("amber", rcount + 1); 
      aUses.add("! Nested loops can be very inefficient: " + this); 
    } // or indeed if there is a collection iteration expr
    else if (loopKind == FOR && 
             Statement.isCumulativeBody(loopVar,body))
    { int rcount = (int) uses.get("amber"); 
      uses.set("amber", rcount + 1); 
      aUses.add("! Possible code reduction of loop to assignment(s): " + this);
    }

    return uses; 
  } // red if nested loops. Amber for a while loop.

  public java.util.Map collectionOperatorUses(
                             int nestingLevel, 
                             java.util.Map operatorsAtLevel, 
                             Vector vars)
  { if (loopRange != null) 
    { loopRange.collectionOperatorUses(nestingLevel, 
                                       operatorsAtLevel, 
                                       vars); 
    }
    else if (loopTest != null)
    { loopTest.collectionOperatorUses(nestingLevel, 
                                      operatorsAtLevel, 
                                      vars); 
    }

    Vector newvars = new Vector(); 
    newvars.addAll(vars); 
    
    if (loopVar != null) 
    { newvars.add("" + loopVar); }
    else if (loopTest != null)
    { Vector evuses = loopTest.getVariableUses(); 
      Vector vuses = 
                VectorUtil.getStrings(evuses); 
      newvars.addAll(vuses); 
    }  
  
    // Also add the write frame variables of body to newvars

    Vector wrfr = body.writeFrame();
    for (int i = 0; i < wrfr.size(); i++) 
    { String wrv = (String) wrfr.get(i); 
      int k = wrv.indexOf("::"); 
      if (k >= 0) 
      { newvars.add(wrv.substring(k+2)); } 
      else 
      { newvars.add(wrv); } 
    }  
 
    body.collectionOperatorUses(nestingLevel + 1,
                                operatorsAtLevel, newvars);

    return operatorsAtLevel; 
  }  

  public void findMagicNumbers(java.util.Map mgns, String rule, String op)
  { if (loopRange != null) 
    { String val = loopRange + ""; 
      loopRange.findMagicNumbers(mgns,val,op); 
    }  
    else if (loopTest != null) 
    { loopTest.findMagicNumbers(mgns,"" + loopTest,op); } 
    body.findMagicNumbers(mgns,rule,op); 
  }

  public Statement addContainerReference(BasicExpression ref,
                                         String var,
                                         Vector excl)
  { Vector newexcls = new Vector(); 
    newexcls.addAll(excl); 
    Expression lv = null; 

    if (loopVar != null) 
    { lv = (Expression) loopVar.clone();
      newexcls.add(lv + ""); 
    }  

    Expression lr = null; 
    if (loopRange != null) 
    { lr = loopRange.addContainerReference(
                               ref,var,newexcls); 
    }  

    Expression lt = null; 
    if (loopTest != null) 
    { lt = loopTest.addContainerReference(ref,var,newexcls); }

    Statement newbody = 
         body.addContainerReference(ref,var,newexcls); 
    WhileStatement res = new WhileStatement(lt,newbody); 
    res.setEntity(entity); 
    res.setLoopKind(loopKind); 
    res.setLoopRange(lv,lr); 
    res.setBrackets(brackets); 
    Expression inv = null; 
    if (invariant != null) 
    { inv = invariant.addContainerReference(
                                 ref,var,newexcls); 
    }  
    res.setInvariant(inv); 
    Expression vv = null; 
    if (variant != null) 
    { vv = variant.addContainerReference(ref,var,newexcls); }  
    res.setVariant(vv); 

    return res; 
  } 

  public Statement optimiseOCL()
  { Expression lv = loopVar; 
    if (loopVar != null) 
    { lv = (Expression) loopVar.clone(); }

    // System.out.println(">>> Loop statement with " + loopVar + " " + loopRange + " " + loopTest); 
  
    Expression lr = loopRange; 
    if (loopRange != null) 
    { lr = loopRange.simplifyOCL(); }
    else if (loopTest != null && 
             loopTest instanceof BinaryExpression)
    { BinaryExpression bexpr = (BinaryExpression) loopTest; 
      if (bexpr.getOperator().equals(":") && 
          (lv + "").equals(bexpr.getLeft() + ""))
      { lr = bexpr.getRight(); } 
    } 
  
    Expression lt = loopTest; 
    if (loopTest != null) 
    { lt = loopTest.simplifyOCL(); }

    Statement newbody = body.optimiseOCL();
 
    if (loopKind == FOR)
    { // check for cumulative patterns 

      if (Statement.isCumulativeBody(lv,newbody))
      { 
        Statement newcode = 
           Statement.cumulativeCode(lv,lr,newbody);

        // JOptionPane.showInputDialog(">> Cumulative while code: " + lv + " " + lr + " " + newbody + " " + newcode);
 
        if (newcode != null) 
        { return newcode; }  
      } 
    } 


    WhileStatement res = new WhileStatement(lt,newbody); 
    res.setEntity(entity); 
    res.setLoopKind(loopKind); 
    res.setLoopRange(lv,lr); 
    res.setBrackets(brackets); 

    Expression inv = null; 
    if (invariant != null) 
    { inv = invariant.simplifyOCL(); }  
    res.setInvariant(inv); 

    Expression vv = null; 
    if (variant != null) 
    { vv = variant.simplifyOCL(); }  
    res.setVariant(vv); 

    return res; 
  } 

  public void setInvariant(Expression inv) 
  { invariant = inv; } 

  public void setVariant(Expression inv) 
  { variant = inv; } 

  public static WhileStatement createInvocationLoop(BasicExpression call, Expression range)
  { String v = Identifier.nextIdentifier("loopvar$"); 
    BasicExpression ve = new BasicExpression(v); 

    Type elemt = range.getElementType(); 
    ve.setType(elemt);
    if (elemt != null) 
    { ve.setElementType(elemt.getElementType()); } 
    ve.umlkind = Expression.VARIABLE; 

    BinaryExpression test = new BinaryExpression(":", ve, range); 
    test.setType(new Type("boolean", null)); 
    test.setElementType(new Type("boolean", null)); 

    BasicExpression invokee = (BasicExpression) call.clone(); 
    invokee.setObjectRef(ve); 

    InvocationStatement invoke = new InvocationStatement(invokee); 
    WhileStatement lp = new WhileStatement(test, invoke); 
    lp.setLoopKind(Statement.FOR);
    lp.setLoopRange(ve,range);  
        // lp.setLoopTest(test); 
    return lp;
  } 
  
  public String bupdateForm()
  { String res = "  WHILE (" + loopTest + ")"; 
    res = res + "  DO \n "; 
    res = res + body.bupdateForm(); 
    if (invariant != null) 
    { res = res + "  INVARIANT " + invariant; } 
    if (variant != null) 
    { res = res + "  VARIANT " + variant; } 
    res = res + "  END";
    return res;  
  } // for loops: introduce new index variable
    // repeat loops: repeat body.

  public BStatement bupdateForm(java.util.Map env, boolean local)
  { BExpression btest = new BBasicExpression("true"); 
    if (loopRange != null && loopVar != null)
    { // for sequence: 
      if (loopRange.isOrdered())
      { String ind = Identifier.nextIdentifier(loopVar + "_ind");
        BasicExpression indbe = new BasicExpression(ind); 
        BBasicExpression indbeb = new BBasicExpression(ind);   
        Expression loopRangeSize0 = new UnaryExpression("->size",loopRange); 
        Expression tst0 = new BinaryExpression("<=", indbe, loopRangeSize0); 
        btest = tst0.binvariantForm(env,local); 
        BParallelStatement ss = new BParallelStatement(false); 
        BAssignStatement bast = new BAssignStatement(indbeb,new BBasicExpression("1"));
        BApplyExpression seqAtInd = new BApplyExpression(loopRange.binvariantForm(env,local),indbeb); 
        BinaryExpression loopRangeSize = new BinaryExpression("+",
                                           new UnaryExpression("->size",loopRange),
                                           new BasicExpression("1")); 
        Expression tst = new BinaryExpression("<=", indbe, loopRangeSize); 
        BExpression invb = tst.binvariantForm(env,local);
        BStatement bbody = body.bupdateForm(env,local); 
        BAssignStatement bast0 = new BAssignStatement(
            new BBasicExpression(loopVar + ""), seqAtInd); 
        ss.addStatement(bast0); 
        ss.addStatement(bbody); 
        BAssignStatement bast1 = new BAssignStatement(indbeb,
                                   new BBinaryExpression("+", indbeb, new BBasicExpression("1")));
        ss.addStatement(bast1); 
        BinaryExpression var1 = 
          new BinaryExpression("+",
            new BinaryExpression("-",new UnaryExpression("->size",loopRange),indbe), 
                                        new BasicExpression(1)); 
        BExpression bvar1 = var1.binvariantForm(env,local); 
        BStatement loop1 = new BLoopStatement(btest,invb,bvar1,
                                  new BVarStatement(loopVar + "", ss) );
        BParallelStatement ss0 = new BParallelStatement(false); 
        ss0.addStatement(bast); 
        ss0.addStatement(loop1); 
        BStatement res = new BVarStatement(ind,ss0);
        return res;  
      }
      else 
      { String ind = Identifier.nextIdentifier(loopVar + "_unprocessed");
        BasicExpression indbe = new BasicExpression(ind); 
        BBasicExpression indbeb = new BBasicExpression(ind);  
        BExpression loopvarb = loopVar.binvariantForm(env,local);  
        BExpression brange = loopRange.binvariantForm(env,local); 
        BExpression emptysetb = new BSetExpression(); 
        btest = new BBinaryExpression("/=", indbeb, emptysetb); 
        BParallelStatement ss = new BParallelStatement(false); 
        BAssignStatement bast = new BAssignStatement(indbeb,brange);
        BExpression indInRange = new BBinaryExpression(":",loopvarb,indbeb); 
        Expression tst = new BinaryExpression("<:", indbe, loopRange); 
        BExpression invb = tst.binvariantForm(env,local);
        BStatement bbody = body.bupdateForm(env,local); 
        Vector indsetelems = new Vector(); 
        indsetelems.add(loopVar); 
        SetExpression indset = new SetExpression(indsetelems); 
        
        BExpression indsetb = indset.binvariantForm(env,local); 
        BAssignStatement bast0 = new BAssignStatement(indbeb, 
                                       new BBinaryExpression("-", indbeb, indsetb)); 
        ss.addStatement(bast0); 
        ss.addStatement(bbody); 
        Expression var1 = new UnaryExpression("->size",indbe); 
        BExpression bvar1 = var1.binvariantForm(env,local); 
        Vector loopanyvars = new Vector(); 
        loopanyvars.add(loopVar + ""); 
        BStatement loop1 = new BLoopStatement(btest,invb,bvar1,
                                   new BAnyStatement(loopanyvars, indInRange, ss) );
        BParallelStatement ss0 = new BParallelStatement(false); 
        ss0.addStatement(bast); 
        ss0.addStatement(loop1); 
        BStatement res = new BVarStatement(ind,ss0);
        return res;  
      } 
    } 
    if (loopTest != null) 
    { btest = loopTest.binvariantForm(env,local); }  
    BExpression binv = new BBasicExpression("true"); 
    if (invariant != null) 
    { binv = invariant.binvariantForm(env,local); } 
    BExpression bvar = new BBasicExpression("true"); 
    if (variant != null) 
    { bvar = variant.binvariantForm(env,local); }  

    // System.out.println("LOOP BODY = " + body); 

    BStatement bbody = body.bupdateForm(env,local); 

    // System.out.println("LOOP BODY = " + bbody); 

    return new BLoopStatement(btest,binv,bvar,bbody); 
  } 

  public void display()
  { System.out.println("  WHILE (" + loopTest + ")"); 
    if (invariant != null) 
    { System.out.println("  INVARIANT " + invariant); } 
    if (variant != null) 
    { System.out.println("  VARIANT " + variant); } 

    System.out.println("  DO \n "); 
    body.display(); 
    System.out.println("  END"); 
  } 

  public void display(PrintWriter out)
  { out.println("  WHILE (" + loopTest + ")"); 
    if (invariant != null) 
    { out.println("  INVARIANT " + invariant); } 
    if (variant != null) 
    { out.println("  VARIANT " + variant); } 
    out.println("  DO\n "); 
    body.display(out); 
    out.println("  END"); 
  } 

  public void displayImp(String var, PrintWriter out) 
  { out.println("  WHILE (" + loopTest + ")"); 
    if (invariant != null) 
    { out.println("  INVARIANT " + invariant); } 
    if (variant != null) 
    { out.println("  VARIANT " + variant); } 
    out.println("  DO\n "); 
    body.displayImp(var,out); 
    out.println("  END"); 
  } 

 
  public void displayJava(String t) 
  { String loop = "while"; 
    if (loopKind == FOR)
    { loop = "for"; } 
    else if (loopKind == REPEAT)
    { System.out.println("  do {"); 
      body.displayJava(t); 
      System.out.println("  } while (!(" + loopTest.toJava() + "));"); 
      return;
    } 

    if (brackets)
    { System.out.println(" ( " + loop + " ( " + loopTest.toJava() + " )"); 
      System.out.println("   {\n "); 
      body.displayJava(t); 
      System.out.println("   } )"); 
    } 
    else 
    { System.out.println("  " + loop + " ( " + loopTest.toJava() + " )"); 
      System.out.println("  {\n "); 
      body.displayJava(t); 
      System.out.println("  }");
    }  
  } 

  public void displayJava(String t, PrintWriter out)
  { String loop = "while"; 
    if (loopKind == FOR)
    { loop = "for"; } 
    else if (loopKind == REPEAT)
    { out.println("  do {"); 
      body.displayJava(t,out); 
      out.println("  } while (!(" + loopTest.toJava() + "));"); 
      return;
    } 

    if (brackets)
    { out.println(" ( " + loop + " ( " + loopTest.toJava() + " )"); 
      out.println("   {\n "); 
      body.displayJava(t,out); 
      out.println("  } )");
    } 
    else  
    { out.println("  " + loop + " ( " + loopTest.toJava() + " )"); 
      out.println("  {\n "); 
      body.displayJava(t,out); 
      out.println("  }");
    }  
  }   

  public String saveModelData(PrintWriter out)
  { String res = ""; 

    if (loopKind == FOR)
    { res = Identifier.nextIdentifier("boundedloopstatement_"); 
      out.println(res + " : BoundedLoopStatement");
    } 
    else 
    { res = Identifier.nextIdentifier("unboundedloopstatement_"); 
      out.println(res + " : UnboundedLoopStatement");
    } 
    out.println(res + ".statId = \"" + res + "\""); 

    Statement actualBody = body; 
    Expression actualTest = loopTest; 

    if (loopKind == REPEAT)
    { // same as 
      // while true do (body; if loopTest then break else skip)

      actualTest = new BasicExpression(true); 
      Statement breakOut = new BreakStatement(); 
      Statement skipStatement = new InvocationStatement("skip"); 

      ConditionalStatement newif = 
        new ConditionalStatement(loopTest, 
                                 breakOut, skipStatement); 
      actualBody = new SequenceStatement(); 
      ((SequenceStatement) actualBody).addStatement(body); 
      ((SequenceStatement) actualBody).addStatement(newif); 
      actualBody.setBrackets(true); 
    } 

    String testid = actualTest.saveModelData(out); 
    String bodyid = actualBody.saveModelData(out); 
    out.println(res + ".test = " + testid); 
    out.println(res + ".body = " + bodyid);  

    if (loopVar != null) 
    { String lvid = loopVar.saveModelData(out); 
      out.println(res + ".loopVar = " + lvid);
    } 

    if (loopRange != null) 
    { String lrid = loopRange.saveModelData(out); 
      out.println(res + ".loopRange = " + lrid);
    } 
 
    
    return res; 
  } // Distinguish while, repeat

  public String saveModelData(PrintWriter out, Entity ent)
  { String res = ""; 

    if (loopKind == FOR)
    { res = Identifier.nextIdentifier("boundedloopstatement_"); 
      out.println(res + " : BoundedLoopStatement");
    } 
    else 
    { res = Identifier.nextIdentifier("unboundedloopstatement_"); 
      out.println(res + " : UnboundedLoopStatement");
    } 
    out.println(res + ".statId = \"" + res + "\""); 

    Statement actualBody = body; 
    Expression actualTest = loopTest; 

    if (loopKind == REPEAT)
    { // same as 
      // while true do (body; if loopTest then break else skip)

      actualTest = new BasicExpression(true); 
      Statement breakOut = new BreakStatement(); 
      Statement skipStatement = new InvocationStatement("skip"); 

      ConditionalStatement newif = 
        new ConditionalStatement(loopTest, 
                                 breakOut, skipStatement); 
      actualBody = new SequenceStatement(); 
      ((SequenceStatement) actualBody).addStatement(body); 
      ((SequenceStatement) actualBody).addStatement(newif); 
      actualBody.setBrackets(true); 
    } 

    String testid = actualTest.saveModelData(out); 
    String bodyid = actualBody.saveModelData(out, ent); 
    out.println(res + ".test = " + testid); 
    out.println(res + ".body = " + bodyid);  

    if (loopVar != null) 
    { String lvid = loopVar.saveModelData(out); 
      out.println(res + ".loopVar = " + lvid);
    } 

    if (loopRange != null) 
    { String lrid = loopRange.saveModelData(out); 
      out.println(res + ".loopRange = " + lrid);
    } 
 
    
    return res; 
  } // Distinguish while, repeat

 
  public Statement substituteEq(String oldE, Expression newE)
  { Statement newbody; 
    if (oldE.equals(body + ""))
    { newbody = new InvocationStatement(newE); } 
    else 
    { newbody = body.substituteEq(oldE,newE); }

    Expression lv = null; 
    if (loopVar != null) 
    { lv = loopVar.substituteEq(oldE,newE); }  
    Expression lr = null; 
    if (loopRange != null) 
    { lr = loopRange.substituteEq(oldE,newE); }  
    Expression lt = null; 
    if (loopTest != null) 
    { lt = loopTest.substituteEq(oldE,newE); }  
    WhileStatement res = new WhileStatement(lt,newbody); 
    res.setEntity(entity); 
    res.setLoopKind(loopKind); 
    res.setLoopRange(lv,lr); 
    res.setBrackets(brackets);
 
    Expression inv = null; 
    if (invariant != null) 
    { inv = (Expression) invariant.substituteEq(oldE,newE); }  
    res.setInvariant(inv); 

    Expression var = null; 
    if (variant != null) 
    { var = (Expression) variant.substituteEq(oldE,newE); }  
    res.setVariant(var); 

    return res; 
  }  

  public Statement removeSlicedParameters(
             BehaviouralFeature op, Vector fpars)
  { Statement newbody = body.removeSlicedParameters(op,fpars); 
    // Expression lv = null; 
    // if (loopVar != null) 
    // { lv = loopVar.substituteEq(oldE,newE); }  

    Expression lr = null; 
    if (loopRange != null) 
    { lr = loopRange.removeSlicedParameters(op,fpars); }  
    Expression lt = null; 
    if (loopTest != null) 
    { lt = loopTest.removeSlicedParameters(op,fpars); }  
    WhileStatement res = new WhileStatement(lt,newbody); 
    res.setEntity(entity); 
    res.setLoopKind(loopKind); 
    res.setLoopRange(loopVar,lr); 
    res.setBrackets(brackets);
 
    // Expression inv = null; 
    // if (invariant != null) 
    // { inv = (Expression) invariant.substituteEq(oldE,newE); }  
    res.setInvariant(invariant); 

    // Expression var = null; 
    // if (variant != null) 
    // { var = (Expression) variant.substituteEq(oldE,newE); }  
    res.setVariant(variant); 

    return res; 
  }  

  public Statement checkConversions(Entity e, Type propType, Type propElemType, java.util.Map interp)
  { Statement newbody = body.checkConversions(e,propType, propElemType, interp); 
    WhileStatement res = new WhileStatement(loopTest,newbody); 
    res.setEntity(entity); 
    res.setLoopKind(loopKind); 
    res.setLoopRange(loopVar,loopRange); 
    res.setBrackets(brackets);
    res.setInvariant(invariant); 
    res.setVariant(variant); 

    return res; 
  } 

  public Statement replaceModuleReferences(UseCase uc)
  { Statement newbody = body.replaceModuleReferences(uc);
    Expression lt = loopTest.replaceModuleReferences(uc);  
    WhileStatement res = new WhileStatement(lt,newbody); 
    res.setEntity(entity); 
    res.setLoopKind(loopKind);
    Expression lr = loopRange; 
    if (loopRange != null) 
    { lr = loopRange.replaceModuleReferences(uc); } 
 
    res.setLoopRange(loopVar,lr); 
    res.setBrackets(brackets);
    res.setInvariant(invariant); 
    res.setVariant(variant); 

    return res; 
  } 

  public String toStringJava() 
  { java.util.Map env = new java.util.HashMap(); 
    if (entity != null)
    { String ename = entity.getName(); 
      env.put(ename,"this");
    } 
    String loop = "while"; 
    if (loopKind == FOR)
    { loop = "for"; } 
    else if (loopKind == REPEAT)
    { String rres = "  do {\n"; 
      rres = rres + "  " + body.toStringJava(); 
      rres = rres + "  } while (!(" + loopTest.queryForm(env,false) + "));\n"; 
      return rres;
    } 

    String res = "  " + loop + " (" + loopTest.queryForm(env,false) + ")"; 
    res = res + "  {\n "; 
    res = res + body.toStringJava(); 
    return res + "  }"; 
  } 

  public String toEtl() 
  { String loop = "while"; 
    if (loopKind == FOR)
    { loop = "for"; } 

    String res = "  " + loop + " (" + loopTest + ")"; 
    res = res + "  {\n "; 
    res = res + body.toEtl(); 
    return res + "  }"; 
  } // no repeat

  public String toString()
  { String res = " while "; 
    if (loopKind == FOR)
    { res = " for "; }
    else if (loopKind == REPEAT)
    { res = "  repeat "; 
      res = res + body.toString(); 
      res = res + "  until " + loopTest + " "; 
      return res;
    } 
 
    res = res + loopTest + " do " + body + " "; 
    if (brackets)
    { res = "( " + res + " )"; } 
    return res; 
  } 

  public String toAST()
  { String res = "(OclStatement "; 
    if (loopKind == FOR)
    { res = res + "for " + loopVar + " : " + loopRange.toAST() + " do " + body.toAST() + " )"; 
    }
    else if (loopKind == REPEAT)
    { res = res + " repeat " + body.toAST() + " until " + 
            loopTest.toAST() + " )"; 
    } 
    else 
    { res = res + "while " + loopTest.toAST() + " do " + 
            body.toAST() + " )"; 
    }

    // if (brackets)
    // { res = "(OclStatement ( " + res + " ) )"; } 

    return res;  
  }  

  public boolean containsSubexpression(Expression expr) 
  { if (loopKind == WHILE || loopKind == REPEAT)
    { if (loopTest.containsSubexpression(expr))
      { return true; }  
      return body.containsSubexpression(expr); 
    }

    if (loopRange.containsSubexpression(expr))
    { return true; } 

    return body.containsSubexpression(expr); 
  } 
 

  public Vector singleMutants()
  { Vector res = new Vector();

    if (loopKind == WHILE || loopKind == REPEAT)
    { Vector exprs = loopTest.singleMutants(); 
      for (int i = 0; i < exprs.size(); i++) 
      { Expression mut = (Expression) exprs.get(i); 
        WhileStatement clne = (WhileStatement) clone(); 
        clne.loopTest = mut; 
        res.add(clne);
      } 
    }  // FOR -> mutate the loopRange.

    Vector bodymutants = body.singleMutants(); 
    for (int i = 0; i < bodymutants.size(); i++) 
    { Statement mut = (Statement) bodymutants.get(i); 
      WhileStatement clne = (WhileStatement) clone(); 
      clne.body = mut; 
      res.add(clne);
    } 
 
    return res; 
  } 

  public boolean typeCheck(Vector types, Vector entities, Vector ctxs, Vector env)
  { Vector env1 = new Vector(); 
    env1.addAll(env);
    /* A copy should be made of env, 
       but lots of bad things happen if this is done. We 
       don't know why. */

    System.out.println(">>> Type-checking " + this + " " + loopRange); 
 
    boolean res = loopTest.typeCheck(types,entities,ctxs,env1);
  
    if (loopRange != null) 
    { res = loopRange.typeCheck(types,entities,ctxs,env1);
      Type lrt = loopRange.getType(); 
      Type lret = loopRange.getElementType(); 

      // JOptionPane.showMessageDialog(null, 
      //    ">>> Type of loop range " + loopRange + " is " + lrt + "(" + lret + ")",
      //    "Type error", JOptionPane.ERROR_MESSAGE); 

      System.out.println(">>> Type of loop range " + loopRange + " is " + lrt + "(" + lret + ")");
      System.out.println(); 

      if (lret == null)
      { if (loopVar.type != null) 
        { lret = loopVar.type; } 
        else 
        { lret = new Type("OclAny", null); } 
      } 
   
      Attribute lv = new Attribute(loopVar + "", lret, ModelElement.INTERNAL); 

      if (lret != null) 
      { lv.setElementType(lret.getElementType()); 
        if (lret.isEntity())
        { lv.setEntity(lret.getEntity()); } 
      } 

      System.out.println(">>> Type of loop variable " + lv + " is " + lv.getType() + " entity: " + lv.getEntity());   

      env1.add(lv); 

      // variables of loopRange should be disjoint from 
      // write frame of body: 

      Vector wrf = body.writeFrame(); 
      Vector actuses = loopRange.getVariableUses();
      actuses = ModelElement.removeExpressionByName("skip", actuses); 
      actuses.add(loopVar + ""); 
      Vector attrs = loopRange.allAttributesUsedIn(); 

      for (int i = 0; i < wrf.size(); i++) 
      { String wv = (String) wrf.get(i); 
        int indx = wv.indexOf("::"); 
        if (indx > 0) 
        { wv = wv.substring(indx + 2); }

        // System.out.println(wv + " " + actuses); 
 
        if (VectorUtil.containsEqualString(wv,actuses))
        { System.err.println("!! ERROR: writing loop var/range variable " + wv + " in loop body\n"); } 

        // System.out.println(wv + " " + attrs); 

        if (VectorUtil.containsEqualString(wv,attrs))
        { System.err.println("!! ERROR: writing loop range attribute " + wv + " in loop body\n"); } 
      } 

    } 

    return body.typeCheck(types,entities,ctxs,env1); 
  }  

  public boolean typeInference(Vector types, Vector entities, Vector ctxs, Vector env, java.util.Map vartypes)
  { Vector env1 = new Vector(); 
    env1.addAll(env);
    /* A copy should be made of env, 
       but lots of bad things happen if this is done. We 
       don't know why. */

    System.out.println(">>> Type-checking " + this + " " + loopRange); 
 
    boolean res = loopTest.typeInference(
                     types,entities,ctxs,env1,vartypes);
  
    if (loopRange != null) 
    { res = loopRange.typeInference(types,entities,
                                    ctxs,env1,vartypes);
      Type lrt = loopRange.getType(); 
      Type lret = loopRange.getElementType(); 

      // JOptionPane.showMessageDialog(null, 
      //    ">>> Type of loop range " + loopRange + " is " + lrt + "(" + lret + ")",
      //    "Type error", JOptionPane.ERROR_MESSAGE); 
      System.out.println(">>> Type of loop range " + loopRange + " is " + lrt + "(" + lret + ")");
      System.out.println(); 
      if (lret == null)
      { if (loopVar.type != null) 
        { lret = loopVar.type; } 
        else 
        { lret = new Type("OclAny", null); } 
      } 
   
      Attribute lv = new Attribute(loopVar + "", lret, ModelElement.INTERNAL); 

      if (lret != null) 
      { lv.setElementType(lret.getElementType()); 
        if (lret.isEntity())
        { lv.setEntity(lret.getEntity()); } 
      } 

      System.out.println(">>> Type of loop variable " + lv + " is " + lv.getType() + " entity: " + lv.getEntity());   

      env1.add(lv); 
    } 

    return body.typeInference(types,entities,ctxs,env1,vartypes); 
  }  

  public Expression wpc(Expression post)
  { if (loopKind == WHILE)
    { // post & (loopTest & post => wpc(body,post))

      Expression bodywpc = body.wpc(post); 
      Expression nextIter = 
        Expression.simplifyImp(
          Expression.simplifyAnd(loopTest,post),bodywpc); 
      nextIter.setBrackets(true); 
      return Expression.simplifyAnd(post, nextIter); 
    } 

    if (loopKind == REPEAT)
    { // wpc(body,post) & 
      // (not(loopTest) & post => wpc(body,post))

      Expression bodywpc = body.wpc(post);
      Expression ntest = Expression.negate(loopTest);  
      Expression nextIter = 
        Expression.simplifyImp(
          Expression.simplifyAnd(ntest,post),bodywpc); 
      nextIter.setBrackets(true); 
      return Expression.simplifyAnd(bodywpc, nextIter); 
    } 

    return loopTest; 
  } // actually the invariant

  public Expression wpc(Expression inv, Expression post)
  { if (loopKind == WHILE)
    { // inv & (loopTest & inv => wpc(body,inv,post))

      Expression bodywpc = body.wpc(inv, post); 
      Expression nextIter = 
        Expression.simplifyImp(
          Expression.simplifyAnd(loopTest,inv),bodywpc); 
      nextIter.setBrackets(true); 
      return Expression.simplifyAnd(inv, nextIter); 
    } 

    if (loopKind == REPEAT)
    { // wpc(body,inv,post) & 
      // (not(loopTest) & inv => wpc(body,inv,post))

      Expression bodywpc = body.wpc(inv,post);
      Expression ntest = Expression.negate(loopTest);  
      Expression nextIter = 
        Expression.simplifyImp(
          Expression.simplifyAnd(ntest,inv),bodywpc); 
      nextIter.setBrackets(true); 
      return Expression.simplifyAnd(bodywpc, nextIter); 
    } 

    return inv; 
  }  

  public Vector dataDependents(Vector allvars, Vector vars)
  { Vector bodydeps = body.dataDependents(allvars,vars);
    Vector result = new Vector(); 
    result.addAll(bodydeps); 
 
    if (loopKind == FOR && loopVar != null && 
        loopRange != null)
    { Vector rangevars = loopRange.allReadData(); 
      // System.out.println(loopVar + " --from--> " + rangevars);
      String lv = loopVar + ""; 

      if (vars.contains(lv)) 
      { // Same as assignment  lv := range
        result = VectorUtil.union(result,rangevars); 
        result.remove(lv); 

        // System.out.println(vars + " --from--> " + result);
      } 
      return result;  
    } // loopVar depends on range data

    if ((loopKind == WHILE || loopKind == REPEAT) && 
        loopTest != null && body.updates(vars)) 
    { Vector testvars = loopTest.allReadData(); 
      result = VectorUtil.union(result,testvars); 
      
      // System.out.println(vars + " --from--> " + result); 
    } 
      
    return result; 
  }  
  // while test do stat
  // if [stat]vars non-empty, rd(test) also in datadependents
  // and closure of this under [stat]

  public Vector dataDependents(Vector allvars, Vector vars, Map mp, Map dlin)
  { Map bodymap = new Map(); 
    Map bodydlin = new Map(); 

    Vector bodydeps = body.dataDependents(allvars,vars, bodymap, bodydlin);

    Vector result = new Vector(); 
    result.addAll(bodydeps); 

    Vector updatedvariables = bodymap.range(); 
    mp.unionWith(bodymap); 

    Vector modifiedvariables = bodydlin.range(); 
    dlin.unionWith(bodydlin); 

    if (loopKind == FOR && loopVar != null && 
        loopRange != null)
    { Vector rangevars = loopRange.allReadData(); 
      Vector rangeBEs = loopRange.allReadBasicExpressionData(); 
      // System.out.println(loopVar + " --from--> " + rangeBEs);

      String lv = loopVar + ""; 

      for (int i = 0; i < rangevars.size(); i++) 
      { String rv = "" + rangevars.get(i); 
        mp.add_pair(rv, lv); 
        for (int j = 0; j < updatedvariables.size(); j++) 
        { String vv = "" + updatedvariables.get(j); 
          mp.add_pair(rv, vv); 
        } 
      }

      for (int i = 0; i < rangeBEs.size(); i++) 
      { String rv = "" + rangeBEs.get(i); 
        dlin.add_pair(rv, lv);
        for (int k = 0; k < modifiedvariables.size(); k++) 
        { String vv = "" + modifiedvariables.get(k); 
          dlin.add_pair(rv, vv); 
        } 
      } 

      if (vars.contains(lv)) 
      { // Same as assignment  lv := range
        result = VectorUtil.union(result,rangevars); 
        result.remove(lv); 
      } 

      return result;  
    } // loopVar depends on range data

    if ((loopKind == WHILE || loopKind == REPEAT) && 
        loopTest != null && body.updates(vars)) 
    { Vector testvars = loopTest.allReadData(); 
      result = VectorUtil.union(result,testvars); 
      
      // Vector bevars = loopTest.allReadBasicExpressionData(); 
      
      // System.out.println(vars + " --from--> " + result); 
      for (int i = 0; i < updatedvariables.size(); i++) 
      { String vv = "" + updatedvariables.get(i); 
        for (int j = 0; j < testvars.size(); j++) 
        { String rv = "" + testvars.get(j); 
          mp.add_pair(rv, vv); 
        } 
      } 
    } // add rv --> vv for each updated var of body. 
      
    return result; 
  }  
  // while test do stat
  // if [stat]vars non-empty, rd(test) also in datadependents
  // and closure of this under [stat]

  public boolean updates(Vector v) 
  { return body.updates(v); } 

  public Statement generateDesign(java.util.Map env, boolean local)
  { Statement bdy = body.generateDesign(env,local); 
    WhileStatement result = (WhileStatement) clone(); 
    if (loopRange != null && loopRange instanceof BasicExpression)
    { if (loopRange.umlkind == Expression.CLASSID) 
      { BasicExpression lr = new BasicExpression("allInstances"); 
        lr.umlkind = Expression.FUNCTION;
        lr.setIsEvent(); 
        lr.setParameters(null);  
        lr.type = loopRange.type; 
        lr.elementType = loopRange.elementType;
        BasicExpression lrang = (BasicExpression) loopRange.clone(); 
        lrang.setObjectRef(null);  
        lr.setObjectRef(lrang); 
        result.loopRange = lr; 
      } // only if loopRange.objectRef == null
    } 
    result.body = bdy; 
    return result; 
  }  

  public Statement statLC(java.util.Map env, boolean local)
  { Statement bdy = body.statLC(env,local); 
    WhileStatement result = (WhileStatement) clone(); 
    result.body = bdy; 
    return result; 
  }  

  public String updateForm(java.util.Map env, 
                           boolean local, Vector types, 
                           Vector entities, Vector vars)
  { if (loopKind == FOR)
    { if (loopVar != null && loopRange != null)
      { String lv = loopVar.queryForm(new java.util.HashMap(), local);  // env?
        String lr; 
        if (loopRange instanceof BasicExpression &&  loopRange.umlkind == Expression.CLASSID)
        { BasicExpression lran = (BasicExpression) loopRange; 
          lr = lran.classqueryForm(env, local); 
        } 
        else 
        { lr = loopRange.queryForm(env, local); } 

        Type et = loopRange.getElementType(); 
        String etr = "Object"; 
        if (et == null) 
        { System.err.println("!! Error: null element type for loop range: " + loopRange);
          JOptionPane.showMessageDialog(null, "ERROR: No element type for: " + loopRange,
                                        "Type error", JOptionPane.ERROR_MESSAGE); 
          if (loopVar.getType() != null)
          { etr = loopVar.getType().getJava(); }
        }  
        else
        { etr = et.getJava(); }
 
        String ind = Identifier.nextIdentifier("_i");
        String rang = Identifier.nextIdentifier("_range"); 

        java.util.Map env1 = (java.util.HashMap) ((java.util.HashMap) env).clone();
        env1.put(etr,lv); 

        Vector preterms = body.allPreTerms(lv); 
        String newbody = processPreTerms(body, preterms, env1, local, types, entities, vars); 

        String extract = "(" + etr + ") " + rang + ".get(" + ind + ")"; 
        if ("int".equals(etr))
        { extract = "((Integer) " + rang + ".get(" + ind + ")).intValue()"; } 
        else if ("double".equals(etr))
        { extract = "((Double) " + rang + ".get(" + ind + ")).doubleValue()"; } 
        else if ("long".equals(etr))
        { extract = "((Long) " + rang + ".get(" + ind + ")).longValue()"; } 
        else if ("boolean".equals(etr))
        { extract = "((Boolean) " + rang + ".get(" + ind + ")).booleanValue()"; } 

        return "  List " + rang + " = new Vector();\n" + 
               "  " + rang + ".addAll(" + lr + ");\n" + 
               "  for (int " + ind + " = 0; " + ind + " < " + rang + ".size(); " + ind + "++)\n" + 
               "  { " + etr + " " + lv + " = " + extract + ";\n" +
               "    " + newbody + "\n" + 
               "  }"; 
      } // All pre terms within body, involving lv, should be evaluated before body. 
      else if (loopTest != null && 
               (loopTest instanceof BinaryExpression))
      { // assume it is  var : exp 
        BinaryExpression lt = (BinaryExpression) loopTest; 
        String lv = lt.left.queryForm(env, local); 
        String lr; 

        java.util.Map env1 = (java.util.HashMap) ((java.util.HashMap) env).clone();
 
        if (lt.right instanceof BasicExpression)
        { BasicExpression lran = (BasicExpression) lt.right; 
          lr = lran.classqueryForm(env, local); 
        } 
        else 
        { lr = lt.right.queryForm(env, local); } 
        Type et = lt.right.getElementType();
        if (et == null) 
        { et = new Type("OclAny", null); }  
        String etr = et.getJava(); 
        String ind = Identifier.nextIdentifier("_i");
        String rang = Identifier.nextIdentifier("_range"); 
        env1.put(etr,lv); 

        Vector preterms = body.allPreTerms(lv); 
        String newbody = processPreTerms(body, preterms, env1, local, types, entities, vars); 

        return "  List " + rang + " = new Vector();\n" + 
               "  " + rang + ".addAll(" + lr + ");\n" + 
               "  for (int " + ind + " = 0; " + ind + " < " + rang + ".size(); " + ind + "++)\n" + 
               "  { " + etr + " " + lv + " = (" + etr + ") " + rang + ".get(" + ind + ");\n" +
               "    " + newbody + "\n" + 
               "  }"; 
      } 
      return "  for (" + loopTest.queryForm(env,local) + ") \n" + 
             "  { " + body.updateForm(env,local,types,entities,vars) + " }"; 
    } 
    else if (loopKind == REPEAT)
    { return "  do {\n  " +  
          body.updateForm(env,local,types,entities,vars) + 
          "\n  } while (!(" + loopTest.queryForm(env,local) + "));\n"; 
    } 
    else
    { return "  while (" + loopTest.queryForm(env,local) + ") \n" + 
        "  { " + body.updateForm(env,local,types,entities,vars) + 
        " }"; 
    } 
 }  

  public String updateFormJava6(java.util.Map env, boolean local)
  { if (loopKind == FOR)
    { if (loopVar != null && loopRange != null)
      { String lv = loopVar.queryFormJava6(new java.util.HashMap(), local);  // env?
        String lr; 
        if (loopRange instanceof BasicExpression)
        { BasicExpression lran = (BasicExpression) loopRange; 
          lr = lran.classqueryFormJava6(env, local); 
        } 
        else 
        { lr = loopRange.queryFormJava6(env, local); } 

        Type et = loopRange.getElementType(); 
        String etr = "Object"; 
        if (et == null) 
        { System.err.println("!! Error: null element type for " + loopRange);
          // JOptionPane.showMessageDialog(null, "ERROR: No element type for: " + loopRange,
          //    "Type error", JOptionPane.ERROR_MESSAGE); 
          if (loopVar.getType() != null)
          { etr = loopVar.getType().getJava6(); }
          else 
          { etr = "Object"; }  
        }  
        else
        { etr = et.getJava6(); }
 
        String ind = Identifier.nextIdentifier("_i");
        String rang = Identifier.nextIdentifier("_range"); 

        java.util.Map env1 = (java.util.HashMap) ((java.util.HashMap) env).clone();
        env1.put(etr,lv); 
        Vector preterms = body.allPreTerms(lv); 
        String newbody = processPreTermsJava6(body, preterms, env1, local); 

        String extract = "(" + etr + ") " + rang + ".get(" + ind + ")"; 
        if ("int".equals(etr))
        { extract = "((Integer) " + rang + ".get(" + ind + ")).intValue()"; } 
        else if ("double".equals(etr))
        { extract = "((Double) " + rang + ".get(" + ind + ")).doubleValue()"; } 
        else if ("long".equals(etr))
        { extract = "((Long) " + rang + ".get(" + ind + ")).longValue()"; } 
        else if ("boolean".equals(etr))
        { extract = "((Boolean) " + rang + ".get(" + ind + ")).booleanValue()"; } 

        return "  ArrayList " + rang + " = new ArrayList();\n" +
               "  " + rang + ".addAll(" + lr + ");\n" + 
               "  for (int " + ind + " = 0; " + ind + " < " + rang + ".size(); " + ind + "++)\n" + 
               "  { " + etr + " " + lv + " = " + extract + ";\n" +
               "    " + newbody + "\n" + 
               "  }"; 
      } 
      else if (loopTest != null && (loopTest instanceof BinaryExpression))
      { // assume it is  var : exp 
        BinaryExpression lt = (BinaryExpression) loopTest; 
        String lv = lt.left.queryFormJava6(env, local); 
        String lr; 

        java.util.Map env1 = (java.util.HashMap) ((java.util.HashMap) env).clone();
 
        if (lt.right instanceof BasicExpression)
        { BasicExpression lran = (BasicExpression) lt.right; 
          lr = lran.classqueryFormJava6(env, local); 
        } 
        else 
        { lr = lt.right.queryFormJava6(env, local); } 
        Type et = lt.right.getElementType();   
        if (et == null) 
        { System.err.println("!! Warning!: no element type for loop iteration " + this); 
          et = new Type("OclAny", null); 
        } 
        String etr = et.typeWrapperJava6();  // ok for String, entities, collections. Not prims
        String ind = Identifier.nextIdentifier("_i");
        String rang = Identifier.nextIdentifier("_range"); 
        env1.put(etr,lv); 
        Vector preterms = body.allPreTerms(lv); 
        String newbody = processPreTermsJava6(body, preterms, env1, local); 

        return "  ArrayList " + rang + " = new ArrayList();\n" +
               "  " + rang + ".addAll(" + lr + ");\n" + 
               "  for (int " + ind + " = 0; " + ind + " < " + rang + ".size(); " + ind + "++)\n" + 
               "  { " + etr + " " + lv + " = (" + etr + ") " + rang + ".get(" + ind + ");\n" +
               "    " + newbody + "\n" + 
               "  }"; 
      } 
      return "  for (" + loopTest.queryFormJava6(env,local) + ") \n" + 
             "  { " + body.updateFormJava6(env,local) + " }"; 
    } 
    else if (loopKind == REPEAT)
    { return "  do {\n  " +  
        body.updateFormJava6(env,local) + 
        "\n  } while (!(" + loopTest.queryFormJava6(env,local) + "));\n"; 
    } 
    else // loopKind == WHILE
    { return "  while (" + loopTest.queryFormJava6(env,local) + ") \n" + 
             "  { " + body.updateFormJava6(env,local) + " }"; 
    } 
 }  

  public String updateFormJava7(java.util.Map env, boolean local)
  { if (loopKind == FOR)
    { // JOptionPane.showMessageDialog(null, "For loop " + loopVar + " " + env, "", JOptionPane.ERROR_MESSAGE);  
          

      if (loopVar != null && loopRange != null)
      { String lv = 
          loopVar.queryFormJava7(new java.util.HashMap(), local);  // env?

        if (env.values().contains(loopVar))
        { lv = loopVar + ""; } 

        String lr; 
        if (loopRange instanceof BasicExpression)
        { BasicExpression lran = (BasicExpression) loopRange; 
          lr = lran.classqueryFormJava7(env, local); 
        } 
        else 
        { lr = loopRange.queryFormJava7(env, local); } 

        Type et = loopRange.getElementType(); 
        String etr = "Object"; 
        if (et == null) 
        { System.err.println("!! Error: null element type for " + loopRange);
          // JOptionPane.showMessageDialog(null, "ERROR: No element type for: " + loopRange,
          // "Type error", JOptionPane.ERROR_MESSAGE); 
          if (loopVar.getType() != null)
          { etr = loopVar.getType().getJava7(loopVar.getElementType()); }
        }  
        else
        { etr = et.getJava7(et.getElementType()); }
 
        String ind = Identifier.nextIdentifier("_i");
        String rang = Identifier.nextIdentifier("_range"); 

        java.util.Map env1 = (java.util.HashMap) ((java.util.HashMap) env).clone();
        env1.put(etr,lv); 
        Vector preterms = body.allPreTerms(lv); 
        String newbody = processPreTermsJava7(body, preterms, env1, local); 

        String extract = "(" + etr + ") " + rang + ".get(" + ind + ")"; 

        if ("int".equals(etr))
        { extract = "((Integer) " + rang + ".get(" + ind + ")).intValue()"; } 
        else if ("double".equals(etr))
        { extract = "((Double) " + rang + ".get(" + ind + ")).doubleValue()"; } 
        else if ("long".equals(etr))
        { extract = "((Long) " + rang + ".get(" + ind + ")).longValue()"; } 
        else if ("boolean".equals(etr))
        { extract = "((Boolean) " + rang + ".get(" + ind + ")).booleanValue()"; } 

        String wrappedElemType = Type.typeWrapperJava(et); 

        String res = 
          "  ArrayList<" + wrappedElemType + "> " + rang + " = new ArrayList<" + wrappedElemType + ">();\n" +
          "    " + rang + ".addAll(" + lr + ");\n" + 
          "    for (int " + ind + " = 0; " + ind + " < " + rang + ".size(); " + ind + "++)\n"; 
        if (env.values().contains(loopVar))
        { res = res +  
               "    { " + lv + " = " + extract + ";\n"; 
        } 
        else 
        { res = res + 
               "    { " + etr + " " + lv + " = " + extract + ";\n"; 
        } 
    
        return res + "    " + newbody + "\n" + 
               "  }"; 
      } 
      else if (loopTest != null && (loopTest instanceof BinaryExpression))
      { // assume it is  var : exp 
        BinaryExpression lt = (BinaryExpression) loopTest; 
        String lv = lt.left.queryFormJava7(env, local); 
        String lr; 

        java.util.Map env1 = (java.util.HashMap) ((java.util.HashMap) env).clone();
 
        if (lt.right instanceof BasicExpression)
        { BasicExpression lran = (BasicExpression) lt.right; 
          lr = lran.classqueryFormJava7(env, local); 
        } 
        else 
        { lr = lt.right.queryFormJava7(env, local); } 
        Type et = lt.right.getElementType(); 
        if (et == null) 
        { System.err.println("! Warning!: no element type for loop iteration " + this); 
          et = new Type("OclAny", null); 
        } 

        String etr = et.typeWrapperJava7();  
        String ind = Identifier.nextIdentifier("_i");
        String rang = Identifier.nextIdentifier("_range"); 
        env1.put(etr,lv); 
        Vector preterms = body.allPreTerms(lv); 
        String newbody = processPreTermsJava7(body, preterms, env1, local); 

        return "  ArrayList<" + etr + "> " + rang + " = new ArrayList<" + etr + ">();\n" +
               "    " + rang + ".addAll(" + lr + ");\n" + 
               "    for (int " + ind + " = 0; " + ind + " < " + rang + ".size(); " + ind + "++)\n" + 
               "    { " + etr + " " + lv + " = (" + etr + ") " + rang + ".get(" + ind + ");\n" +
               "    " + newbody + "\n" + 
               "  }"; 
      } 
      return "  for (" + loopTest.queryFormJava7(env,local) + ") \n" + 
             "  { " + body.updateFormJava7(env,local) + " }"; 
    } 
    else if (loopKind == REPEAT)
    { return "  do {\n  " +  
        body.updateFormJava7(env,local) + 
        "\n  } while (!(" + loopTest.queryFormJava7(env,local) + "));\n"; 
    } 
    else // loopKind == WHILE
    { return "  while (" + loopTest.queryFormJava7(env,local) + ") \n" + 
             "  { " + body.updateFormJava7(env,local) + " }"; 
    } 
 }  

  /* Also for REPEAT: */ 
  public String updateFormCSharp(java.util.Map env, boolean local)
  { if (loopKind == FOR)
    { if (loopVar != null && loopRange != null)
      { String lv = loopVar.queryFormCSharp(new java.util.HashMap(), local);  // env?
        String lr; 
        if (loopRange instanceof BasicExpression)
        { BasicExpression lran = (BasicExpression) loopRange; 
          lr = lran.classqueryFormCSharp(env, local); 
        } 
        else 
        { lr = loopRange.queryFormCSharp(env, local); } 

        Type et = loopRange.getElementType(); 
        String etr = "object"; 
        if (et == null) 
        { System.err.println("!! Error: null element type for " + loopRange);
          if (loopVar.getType() != null)
          { etr = loopVar.getType().getCSharp(); }
        }  
        else
        { etr = et.getCSharp(); }
 
        String ind = Identifier.nextIdentifier("_i");
        String rang = Identifier.nextIdentifier("_range"); 

        java.util.Map env1 = (java.util.HashMap) ((java.util.HashMap) env).clone();
        env1.put(etr,lv); 

        Vector preterms = body.allPreTerms(lv); 
        String newbody = processPreTermsCSharp(body, preterms, env1, local); 

        return "  ArrayList " + rang + 
                 " = SystemTypes.asSequence(" + lr + ");\n" + 
               "  for (int " + ind + " = 0; " + ind + " < " + rang + ".Count; " + ind + "++)\n" + 
               "  { " + etr + " " + lv + " = (" + etr + ") " + rang + "[" + ind + "];\n" +
               "    " + newbody + "\n  }"; 
      } 
      else if (loopTest != null && 
               (loopTest instanceof BinaryExpression))
      { // assume it is  var : exp 
        BinaryExpression lt = (BinaryExpression) loopTest; 
        String lv = lt.left.queryFormCSharp(env, local); 
        String lr; 

        java.util.Map env1 = (java.util.HashMap) ((java.util.HashMap) env).clone();
 
        if (lt.right instanceof BasicExpression)
        { BasicExpression lran = (BasicExpression) lt.right; 
          lr = lran.classqueryFormCSharp(env, local); 
        } 
        else 
        { lr = lt.right.queryFormCSharp(env, local); } 
        Type et = lt.right.getElementType();
        if (et == null) 
        { et = new Type("OclAny", null); } 
 
        String etr = et.getCSharp(); 
        String ind = Identifier.nextIdentifier("_i");
        String rang = Identifier.nextIdentifier("_range"); 
        env1.put(etr,lv); 

        Vector preterms = body.allPreTerms(lv); 
        String newbody = processPreTermsCSharp(body, preterms, env1, local); 

        return "  ArrayList " + rang + " = SystemTypes.asSequence(" + lr + ");\n" + 
               "  for (int " + ind + " = 0; " + ind + " < " + rang + ".Count; " + ind + "++)\n" + 
               "  { " + etr + " " + lv + " = (" + etr + ") " + rang + "[" + ind + "];\n" +
               "    " + newbody + "\n  }"; 
      } 
      return "  for (" + loopTest.queryFormCSharp(env,local) + ") \n" + 
             "  { " + body.updateFormCSharp(env,local) + " }"; 
    } 
    else if (loopKind == REPEAT)
    { return "  do {\n  " +  
        body.updateFormCSharp(env,local) + 
        "\n  } while (!(" + loopTest.queryFormCSharp(env,local) + "));\n"; 
    } 
    else // loopKind == WHILE
    { return "  while (" + loopTest.queryFormCSharp(env,local) + ") \n" + 
             "  { " + body.updateFormCSharp(env,local) + " }"; 
    } 
 }  

  public String updateFormCPP(java.util.Map env, boolean local)
  { if (loopKind == FOR)
    { if (loopVar != null && loopRange != null)
      { String lv = loopVar.queryFormCPP(new java.util.HashMap(), local);  // env?
        String lr; 
        if (loopRange instanceof BasicExpression)
        { BasicExpression lran = (BasicExpression) loopRange; 
          lr = lran.classqueryFormCPP(env, local); 
        } 
        else 
        { lr = loopRange.queryFormCPP(env, local); } 

        Type et = loopRange.getElementType(); 
        String etr = "void*"; 
        if (et == null) 
        { System.err.println("!! Error: null element type for " + loopRange);
          if (loopVar.getType() != null)
          { etr = loopVar.getType().getCPP(); }
        }  
        else
        { etr = et.getCPP(); }
 
        String ind = Identifier.nextIdentifier("_i");
        String rang = Identifier.nextIdentifier("_range"); 
        String rangesource = Identifier.nextIdentifier("_range_source"); 

        java.util.Map env1 = (java.util.HashMap) ((java.util.HashMap) env).clone();
        env1.put(etr,lv); 

        Vector preterms = body.allPreTerms(lv); 
        String newbody = processPreTermsCPP(body, preterms, env1, local); 

        return "    vector<" + etr + ">* " + rangesource + " = " + lr + ";\n" + 
               "    vector<" + etr + ">* " + rang + " = new vector<" + etr + ">();\n" + 
               "    " + rang + "->insert(" + rang + "->end(), " + rangesource + "->begin(), " + 
                                       rangesource + "->end());\n" + 
               "    for (int " + ind + " = 0; " + ind + " < " + rang + "->size(); " + ind + "++)\n" + 
               "    { " + etr + " " + lv + " = (*" + rang + ")[" + ind + "];\n" +
               "      " + newbody + "\n" + 
               "    }"; 
      } 
      else if (loopTest != null && (loopTest instanceof BinaryExpression))
      { // assume it is  var : exp 
        BinaryExpression lt = (BinaryExpression) loopTest; 
        String lv = lt.left.queryFormCPP(env, local); 
        String lr; 

        java.util.Map env1 = (java.util.HashMap) ((java.util.HashMap) env).clone();
 
        if (lt.right instanceof BasicExpression)
        { BasicExpression lran = (BasicExpression) lt.right; 
          lr = lran.classqueryFormCPP(env, local); 
        } 
        else 
        { lr = lt.right.queryFormCPP(env, local); } 

        Type et = lt.right.getElementType(); 
        if (et == null) 
        { et = new Type("OclAny", null); } 

        String etr = et.getCPP(); 
        String ind = Identifier.nextIdentifier("_i");
        String rang = Identifier.nextIdentifier("_range");
        String rangesource = Identifier.nextIdentifier("_range_source"); 
 
        env1.put(etr,lv); 
        Vector preterms = body.allPreTerms(lv); 
        String newbody = processPreTermsCPP(body, preterms, env1, local); 

        return "    vector<" + etr + ">* " + rangesource + " = " + lr + ";\n" + 
               "    vector<" + etr + ">* " + rang + " = new vector<" + etr + ">();\n" + 
               "    " + rang + "->insert(" + rang + "->end(), " + rangesource + "->begin(), " + 
                                       rangesource + "->end());\n" + 
               "    for (int " + ind + " = 0; " + ind + " < " + rang + "->size(); " + ind + "++)\n" + 
               "    { " + etr + " " + lv + " = (*" + rang + ")[" + ind + "];\n" +
               "      " + newbody + "\n" + 
               "    }"; 
      } 
      return "  for (" + loopTest.queryFormCPP(env,local) + ") \n" + 
             "  { " + body.updateFormCPP(env,local) + " }"; 
    } 
    else if (loopKind == REPEAT)
    { return "  do {\n  " +  
        body.updateFormCPP(env,local) + 
        "\n  } while (!(" + loopTest.queryFormCPP(env,local) + "));\n"; 
    } 
    else // loopKind == WHILE
    { return "  while (" + loopTest.queryFormCPP(env,local) + ") \n" + 
             "  { " + body.updateFormCPP(env,local) + " }"; 
    } 
  }  /* and REPEAT */ 

  public Vector allPreTerms()
  { Vector res = body.allPreTerms();
    Vector res1 = new Vector(); 
    if (loopVar != null) 
    { res1 = body.allPreTerms(loopVar + ""); } // These must be handled *within* the loop
     
    if (loopTest == null) 
    { return res; } 
    Vector res2 = loopTest.allPreTerms();
    res.addAll(res2); // union, and loopRange?
    res.removeAll(res1); 
    return res;  
  }  

  public Vector allPreTerms(String var)
  { Vector res = body.allPreTerms(var);

    // if (loopRange != null) 
    // { res.addAll(loopRange.allPreTerms(var)); } 

    if (loopTest == null) 
    { return res; } 
    Vector res1 = loopTest.allPreTerms(var);
    res.addAll(res1); // union, and loopRange?
    return res;  
  }  

  public Vector readFrame()
  { Vector res = body.readFrame();

    if (loopRange != null) 
    { res.addAll(loopRange.allReadFrame()); } 
     
    if (loopTest == null) 
    { return res; } 
    Vector res2 = loopTest.allReadFrame();
    res.addAll(res2);  

    // remove the loopVar if it exists
    if (loopVar != null) 
    { Vector res1 = new Vector(); 
      res1.add(loopVar); 
      res.removeAll(res1); 
    } 

    return res;  
  }  

  public Vector writeFrame() 
  { Vector res = body.writeFrame();

    // remove the loopVar if it exists
    if (loopVar != null) 
    { Vector res1 = new Vector(); 

      if (res.contains("" + loopVar))
      { System.err.println("!! Error: iteration variable " + 
           loopVar + " cannot be written in loop body " + body); 
      } 
    }
 
    return res; 
  } 

  public int syntacticComplexity()
  { int res = body.syntacticComplexity(); 

    if (loopKind == FOR) 
    { if (loopRange != null) 
      { int rcomp = loopRange.syntacticComplexity();

        return res + rcomp + 1; 
      } 
    } 

    if (loopTest == null) 
    { return res + 1; }

    int syncomp = loopTest.syntacticComplexity(); 
 
    return syncomp + res + 1; 
  } 

  public int cyclomaticComplexity()
  { int res = body.cyclomaticComplexity(); 

    if (loopKind == FOR && loopRange != null) 
    { return res; } // bounded loop fixed number of iterations. 

    if (loopTest == null) 
    { return res; } 
    return loopTest.cyclomaticComplexity() + res; 
  } 

  public int epl()
  { return body.epl(); }  

  public Vector allOperationsUsedIn()
  { Vector res = body.allOperationsUsedIn();

    if (loopRange != null) 
    { res.addAll(loopRange.allOperationsUsedIn()); } 
     
    if (loopTest == null) 
    { return res; } 
    Vector res2 = loopTest.allOperationsUsedIn();
    res.addAll(res2);  

    // System.out.println("LOOP READ FRAME = " + res); 

    return res;  
  }  

  public Vector allAttributesUsedIn()
  { Vector res = body.allAttributesUsedIn();

    if (loopRange != null) 
    { res.addAll(loopRange.allAttributesUsedIn()); } 
     
    if (loopTest == null) 
    { return res; } 
    Vector res2 = loopTest.allAttributesUsedIn();
    res.addAll(res2);  

    return res;  
  }  

  public Vector getUses(String var)
  { Vector res = body.getUses(var);

    if (loopRange != null) 
    { res.addAll(loopRange.getUses(var)); } 
     
    if (loopTest == null) 
    { return res; } 
    Vector res2 = loopTest.getUses(var);
    res.addAll(res2);  

    // System.out.println("LOOP READ FRAME = " + res); 

    return res;  
  }  

  public Vector getVariableUses()
  { Vector res = body.getVariableUses();

    String lv = ""; 

    if (loopVar != null) 
    { lv = loopVar + ""; 
      Expression expr = 
        ModelElement.lookupExpressionByName(lv, res); 
      if (expr == null) 
      { System.err.println("! Warning: no use of loop variable " +
                 loopVar + " in loop body: " + body); 
      } 
      res = ModelElement.removeExpressionByName(lv,res); 
    } 

    if (loopRange != null) 
    { Vector lrvars = loopRange.getVariableUses(); 
      res.addAll(lrvars); 
      Expression rexpr = 
        ModelElement.lookupExpressionByName(lv, lrvars); 
      if (loopVar != null && rexpr != null) 
      { System.err.println("!! Error: loop variable " +
               loopVar + " used in loop range: " + loopRange); 
      } 
    } 
     
    if (loopTest == null) 
    { return res; } 

    Vector res2 = loopTest.getVariableUses();
    res.addAll(res2);  

    return res;  
  }  

  public Vector getVariableUses(Vector unused)
  { Vector res = body.getVariableUses(unused);

    String lv = ""; 

    if (loopVar != null) 
    { lv = loopVar + ""; 
      Expression expr = 
        ModelElement.lookupExpressionByName(lv, res); 
      if (expr == null) 
      { System.err.println("! Warning: no use of loop variable " +
                 loopVar + " in loop body: " + body); 
      } 
      res = ModelElement.removeExpressionByName(lv,res); 
    } 

    if (loopRange != null) 
    { Vector lrvars = loopRange.getVariableUses(); 
      res.addAll(lrvars); 
      Expression rexpr = 
        ModelElement.lookupExpressionByName(lv, lrvars); 
      if (loopVar != null && rexpr != null) 
      { System.err.println("!! Semantic error: loop variable " +
               loopVar + " used in loop range: " + loopRange); 
      } 
    } 
     
    if (loopTest == null) 
    { return res; } 

    Vector res2 = loopTest.getVariableUses();
    res.addAll(res2);  

    return res;  
  }  

  public Vector equivalentsUsedIn()
  { Vector res = body.equivalentsUsedIn();

    if (loopRange != null) 
    { res.addAll(loopRange.equivalentsUsedIn()); } 
     
    if (loopTest == null) 
    { return res; } 
    Vector res2 = loopTest.equivalentsUsedIn();
    res.addAll(res2);  

    // System.out.println("LOOP READ FRAME = " + res); 

    return res;  
  }  

  public String cg(CGSpec cgs)
  { String etext = this + "";

    Vector eargs = new Vector(); 
    Vector args = new Vector();

    String bodyText = ""; 
    if (body != null) 
    { bodyText = body.cg(cgs); } 
    else 
    { bodyText = (new SequenceStatement()).cg(cgs); } 

    Expression ltest = loopTest; 
    if (loopTest == null) 
    { ltest = new BasicExpression(true); } 
    
    if (loopKind == WHILE) 
    { args.add(ltest.cg(cgs));  
      args.add(bodyText);
      eargs.add(ltest); 
      eargs.add(body); 
    } 
    else if (loopKind == REPEAT) 
    { args.add(bodyText);
      args.add(ltest.cg(cgs));  
      eargs.add(body); 
      eargs.add(ltest); 
    } 
    else 
    { args.add(loopVar + ""); 
      if (loopRange != null) 
      { args.add(loopRange.cg(cgs)); } 
      else 
      { args.add(""); }  
      args.add(bodyText); 
      eargs.add(loopVar); 
      eargs.add(loopRange); 
      eargs.add(body); 
    } 

    CGRule r = cgs.matchedStatementRule(this,etext);

    System.out.println(">> Matched statement rule: " + r + " for " + this); 

    if (r != null)
    { return r.applyRule(args,eargs,cgs); }

    return etext;
  } // for FOR, need the loopVar : loopRange
    // instead of test. 

  public Vector allVariableNames()
  { Vector res = new Vector(); 
    if (loopKind == WHILE && loopTest != null) 
    { res.addAll(loopTest.allVariableNames()); } 
    else if (loopKind == FOR && 
             loopVar != null && loopRange != null) 
    { res.addAll(loopVar.allVariableNames()); 
      res.addAll(loopRange.allVariableNames()); 
    }  
    res = VectorUtil.union(res,body.allVariableNames()); 
    if (loopKind == REPEAT && loopTest != null) 
    { res.addAll(loopTest.allVariableNames()); } 
    return res; 
  } 

  public Vector metavariables()
  { Vector res = new Vector(); 
    if (loopKind == WHILE && loopTest != null) 
    { res.addAll(loopTest.metavariables()); } 
    else if (loopKind == FOR && 
             loopVar != null && loopRange != null) 
    { res.addAll(loopVar.metavariables()); 
      res.addAll(loopRange.metavariables()); 
    }  
    res = VectorUtil.union(res,body.metavariables()); 
    if (loopKind == REPEAT && loopTest != null) 
    { res.addAll(loopTest.metavariables()); } 
    return res; 
  } 

  public Vector cgparameters()
  { Vector eargs = new Vector(); 
    if (loopKind == WHILE) 
    { eargs.add(loopTest); 
      eargs.add(body); 
    } 
    else if (loopKind == REPEAT) 
    { eargs.add(body); 
      eargs.add(loopTest); 
    } 
    else 
    { eargs.add(loopVar); 
      eargs.add(loopRange); 
      eargs.add(body); 
    } 
    return eargs;
  } // for FOR, need the loopVar : loopRange
    // instead of test. 

} 
 

class CreationStatement extends Statement
{ String createsInstanceOf = null;
  String assignsTo = null;
  private Type instanceType = null; 
  private Type elementType = null; 
  boolean declarationOnly = false; 
  String initialValue = null;
  Expression initialExpression = null;  
  boolean isFrozen = false;  // true when a constant is declared. 
  Attribute variable = null; // for the LHS

  public Object clone()
  { CreationStatement cs = 
       new CreationStatement(createsInstanceOf,assignsTo); 
    cs.instanceType = instanceType; 
    cs.elementType = elementType; 
    cs.declarationOnly = declarationOnly; 
    cs.initialValue = initialValue; 
    if (initialExpression != null) 
    { cs.initialExpression = 
         (Expression) initialExpression.clone();
    }  
    cs.isFrozen = isFrozen; 
    cs.variable = variable; 
    return cs; 
  } 

  public Type getType()
  { return instanceType; } 

  public String getOclType()
  { if ("int".equals(createsInstanceOf) || 
        "long".equals(createsInstanceOf))
    { return "Integer"; }
    if ("double".equals(createsInstanceOf))
    { return "Real"; }
    if ("boolean".equals(createsInstanceOf))
    { return "Boolean"; }
    if ("String".equals(createsInstanceOf))
    { return "String"; }
    if (createsInstanceOf.startsWith("Sequence"))
    { return "Sequence"; } 
    if (createsInstanceOf.startsWith("Set"))
    { return "Set"; } 
    if (createsInstanceOf.startsWith("Map"))
    { return "Map"; } 
    if (createsInstanceOf.startsWith("Ref"))
    { return "Ref"; } 
    if (createsInstanceOf.startsWith("Function"))
    { return "Function"; } 
    return "Object"; 
      // if (Type.isOclClassifierType(createsInstanceOf))
  } // also case of enumerations. 

  public Type getElementType()
  { return elementType; } 

  public String getVar()
  { return assignsTo; } 

  public String getDeclaredVariable()
  { return assignsTo; } 

  public CreationStatement(String cio, String ast)
  { createsInstanceOf = cio;
    assignsTo = ast; 
  }

  public CreationStatement(String vbl, Type typ)
  { createsInstanceOf = typ.getName();
    instanceType = typ; 
    elementType = typ.getElementType();
    if (Type.isStringType(typ))
    { elementType = new Type("String", null); }  
    assignsTo = vbl; 
  }

  public CreationStatement(Expression vbl, Type typ)
  { createsInstanceOf = typ.getName();
    instanceType = typ; 
    elementType = typ.getElementType();
    if (Type.isStringType(typ))
    { elementType = new Type("String", null); }  
    assignsTo = vbl + ""; 
  }

  public CreationStatement(BasicExpression var, Expression init)
  { instanceType = var.getType(); 
    elementType = var.getElementType(); 
    if (Type.isStringType(instanceType))
    { elementType = new Type("String", null); }
  
    initialExpression = init; 
    createsInstanceOf = instanceType.getName(); 
    assignsTo = var + ""; 
  }

  public CreationStatement defaultVersion()
  { CreationStatement res = (CreationStatement) clone(); 
    Expression defaultInit = 
      Type.defaultInitialValueExpression(instanceType);
    res.initialExpression = defaultInit; 
    res.initialValue = defaultInit + ""; 
    return res; 
  } 

  public int execute(ModelSpecification sigma, ModelState beta)
  { // add assignsTo as new variable, set to initialExpression

    if (initialExpression != null) 
    { Expression val = initialExpression.evaluate(sigma, beta); 
      beta.addVariable(assignsTo, val);
    } // else use default value 
    else if (instanceType != null)  
    { Expression defaultInit = 
        Type.defaultInitialValueExpression(instanceType);
      Expression val = defaultInit.evaluate(sigma, beta); 
      beta.addVariable(assignsTo, val);
    }

    return Statement.NORMAL; 
  } 

  public Expression definedness()
  { if (initialExpression != null) 
    { return initialExpression.definedness(); } 
    return new BasicExpression(true); 
  } 

  public Vector allVariableNames()
  { Vector res = new Vector(); 
    res.add(assignsTo); 
    if (initialExpression != null)
    { res.addAll(initialExpression.allVariableNames()); }
    return res; 
  }  

  public Map energyUse(Map uses, Vector rUses, Vector aUses)
  { if (instanceType != null) 
    { int tcomp = instanceType.complexity(); 
      if (tcomp > TestParameters.nestedTypeLimit) 
      { int acount = (int) uses.get("amber"); 
        uses.set("amber", acount + 1); 
        aUses.add("! Warning (MNC) flaw: complex type with complexity " + tcomp + ": " + instanceType); 
      } 
    } 

    if (initialExpression != null) 
    { initialExpression.energyUse(uses, rUses, aUses); 

      int syncomp = initialExpression.syntacticComplexity(); 
      if (syncomp > TestParameters.syntacticComplexityLimit)
      { int acount = (int) uses.get("amber"); 
        uses.set("amber", acount + 1); 
        aUses.add("! Code smell (MEL): too high expression complexity (" + syncomp + ") for " + initialExpression + "\n" +  
                  ">>> Recommend OCL refactoring");  
      } 
    } 

    return uses; 
  } 

  public java.util.Map collectionOperatorUses(int lev, 
                                 java.util.Map uses,
                                 Vector vars)
  { 
    if (initialExpression != null) 
    { initialExpression.collectionOperatorUses(lev, 
                                        uses, vars); 
    } 

    return uses; 
  } 

  /* public CreationStatement(Attribute vbl, Type typ)
  { createsInstanceOf = typ.getName();
    instanceType = typ; 
    elementType = vbl.getElementType(); 
    assignsTo = vbl.getName();
    variable = vbl;  
  } */ 

  public CreationStatement(Attribute vbl)
  { Type typ = vbl.getType(); 
    createsInstanceOf = typ.getName();
    instanceType = typ; 
    elementType = vbl.getElementType(); 
    assignsTo = vbl.getName();
    variable = vbl;  
  }

  public CreationStatement(Attribute vbl, Attribute val)
  { Type typ = vbl.getType(); 
    createsInstanceOf = typ.getName();
    instanceType = typ; 
    elementType = vbl.getElementType(); 
    assignsTo = vbl.getName();
    variable = vbl;  
    initialExpression = new BasicExpression(val); 
    initialValue = initialExpression + ""; 
  }

  public CreationStatement(BasicExpression vbl)
  { Type typ = vbl.getType(); 
    createsInstanceOf = typ.getName();
    instanceType = typ; 
    elementType = vbl.getElementType(); 
    assignsTo = vbl.getData();
    variable = vbl.variable;  
  }

  public static CreationStatement newCreationStatement(
            String vbl, Type typ, Expression einit) 
  { CreationStatement cs = 
       new CreationStatement(typ.getName(), vbl); 
    cs.setType(typ);
    cs.setInitialisation(einit);   
    return cs; 
  } 

  public static CreationStatement newCreationStatement(String vbl, String typ) 
  { CreationStatement cs = new CreationStatement(typ, vbl); 
    Type t = Type.getTypeFor(typ);
    if (t == null)
    { t = new Type("OclAny", null); } 
    cs.setType(t);  
    return cs; 
  } 

  public static CreationStatement newCreationStatement(String vbl, String typ, Vector enumtypes, Vector ents) 
  { CreationStatement cs = new CreationStatement(typ, vbl); 
    Type t = Type.getTypeFor(typ, enumtypes, ents);
    if (t == null)
    { t = new Type("OclAny", null); } 
    cs.setType(t);  
    cs.setElementType(t.getElementType()); 
    return cs; 
  } 

  public String getDefinedVariable()
  { return assignsTo; } 

  public void setInitialValue(String init)
  { initialValue = init; } 

  public void setInitialExpression(Expression expr) 
  { initialExpression = expr; 
    initialValue = expr + ""; 
  } 

  public void setInitialisation(Expression expr) 
  { initialExpression = expr; 
    initialValue = expr + ""; 
  } 

  public Expression getInitialisation()
  { return initialExpression; } 

  public void setAssignsTo(Expression expr)
  { assignsTo = expr + ""; } 

  public void setFrozen(boolean froz)
  { isFrozen = froz; } 

  public String getOperator() 
  { return "var"; } 

  public boolean isResultDeclaration()
  { if (assignsTo.equals("result") && 
        (instanceType != null || createsInstanceOf != null))
    { return true; } 
    return false; 
  } 

/*  public String cg(CGSpec cgs)
  { String etext = "var " + assignsTo + " : " + createsInstanceOf; 
    Vector args = new Vector();
    args.add(assignsTo);
    args.add(createsInstanceOf);
    CGRule r = cgs.matchedStatementRule(this,etext);
    if (r != null)
    { return r.applyRule(args); }
    return etext;
  } */ 

  public void setInstanceType(Type t)
  { instanceType = t; 
    if (instanceType != null) 
    { createsInstanceOf = instanceType.getName(); }
  }  

  public void setType(Type t)
  { instanceType = t; 
    if (instanceType != null) 
    { createsInstanceOf = instanceType.getName(); 
      if ("String".equals(instanceType.getName()))
      { elementType = new Type("String", null); }
    } 
  }  

  public void setKeyType(Type t)
  { // keyType = t; 
    if (instanceType != null) 
    { instanceType.setKeyType(t); }  
  } 

  public void setElementType(Type t)
  { elementType = t; 
    if (instanceType != null) 
    { instanceType.setElementType(t); }  
  } 

  public Statement dereference(BasicExpression var)
  { return this; } 

  public Statement substituteEq(String oldE, Expression newE)
  { String cio = createsInstanceOf; 
    String ast = assignsTo; 

    if (oldE.equals(createsInstanceOf))
    { cio = newE.toString(); }
    if (oldE.equals(assignsTo))
    { ast = newE.toString(); }

    CreationStatement res = new CreationStatement(cio,ast);
    res.setType(instanceType); 
    res.setElementType(elementType);  
	
    if (initialExpression != null) 
    { Expression newExpr = initialExpression.substituteEq(oldE,newE); 
      res.setInitialisation(newExpr); 
    }
    return res; 
  } 

  public Statement optimiseOCL()
  { String cio = createsInstanceOf; 
    String ast = assignsTo; 

    CreationStatement res = new CreationStatement(cio,ast);
    res.setType(instanceType); 
    res.setElementType(elementType);  
	
    if (initialExpression != null) 
    { Expression newExpr = initialExpression.simplifyOCL(); 
      res.setInitialisation(newExpr); 
    }

    return res; 
  } 

  public Statement removeSlicedParameters(
                     BehaviouralFeature bf, Vector fpars)
  { String cio = createsInstanceOf; 
    String ast = assignsTo; 

    CreationStatement res = new CreationStatement(cio,ast);
    res.setType(instanceType); 
    res.setElementType(elementType);  
	
    if (initialExpression != null) 
    { Expression newExpr = 
        initialExpression.removeSlicedParameters(bf,fpars); 
      res.setInitialisation(newExpr); 
    }
    return res; 
 } // substitute in the initialExpression

  public Statement addContainerReference(
               BasicExpression ref, String var, Vector excl)
  { String cio = createsInstanceOf; 
    String ast = assignsTo; 

    CreationStatement res = new CreationStatement(cio,ast);
    res.setType(instanceType); 
    res.setElementType(elementType);  
	
    if (initialExpression != null) 
    { Expression newExpr = 
        initialExpression.addContainerReference(ref,var,excl); 
      res.setInitialisation(newExpr); 
    }

    excl.add(assignsTo); // for succeeding statements

    return res; 
  } 

  public String toString()
  { String declType = createsInstanceOf; 
    if (instanceType != null && instanceType.isEntity()) 
    { declType = instanceType.getEntity().getCompleteName(); }
    else if (instanceType != null) 
    { declType = instanceType + ""; }  

    if (initialValue != null) 
    { return "  var " + assignsTo + " : " + declType + " := " + initialValue; } 
    else if (initialExpression != null) 
    { return "  var " + assignsTo + " : " + declType + " := " + initialExpression; } 
    else
    { return "  var " + assignsTo + " : " + declType; }
  } // isFrozen? 

  public String toAST()
  { String res = "(OclStatement var " + assignsTo + " : " + instanceType.toAST() + " )"; 

    // And initialisation. initialExpression != null

    // if (brackets)
    // { res = "(OclStatement ( " + res + " ) )"; } 

    return res; 
  } 

  public boolean containsSubexpression(Expression expr)
  { if (initialExpression != null) 
    { return initialExpression.containsSubexpression(expr); } 
    return false; 
  } 

  public Vector singleMutants()
  { Vector res = new Vector(); 
    return res; 
  } // mutate the initialExpression

  public void findClones(java.util.Map clones, String op, String rule)
  { if (initialExpression != null) 
    { initialExpression.findClones(clones,op,rule); } 
  } 

  public void findClones(java.util.Map clones, 
                         java.util.Map cdefs,
                         String op, String rule)
  { if (initialExpression != null) 
    { initialExpression.findClones(clones,cdefs,op,rule); } 
  } 


  public String saveModelData(PrintWriter out) 
  { String res = Identifier.nextIdentifier("creationstatement_"); 
    out.println(res + " : CreationStatement");
    out.println(res + ".statId = \"" + res + "\"");  
    out.println(res + ".createsInstanceOf = \"" + createsInstanceOf + "\""); 
    out.println(res + ".assignsTo = \"" + assignsTo + "\""); 
    String tname = "OclAny"; // default
    String etname = "OclAny"; 

    if (instanceType != null) 
    { tname = instanceType.getUMLModelName(out); } 
    out.println(res + ".type = " + tname); 

    // System.out.println("Creation STAT TYpe = " + instanceType + 
    //                    " (" + instanceType.elementType + ")");  

    if (elementType != null) 
    { etname = elementType.getUMLModelName(out); 
      out.println(res + ".elementType = " + etname); 
    } 
    else if (instanceType != null && 
             instanceType.getElementType() != null)
    { etname = 
        instanceType.getElementType().getUMLModelName(out); 
      out.println(res + ".elementType = " + etname);
    } 
    else if (instanceType != null && 
             Type.isBasicType(instanceType))
    { out.println(res + ".elementType = " + tname); } 
    else 
    { out.println(res + ".elementType = " + etname); } 

    if (initialExpression != null) 
    { String exprId = initialExpression.saveModelData(out); 
      out.println(exprId + " : " + res + ".initialExpression"); 
    } 


    return res; 
  } 

  public String bupdateForm()
  { return assignsTo + " :: " + createsInstanceOf; } 

  public BStatement bupdateForm(java.util.Map env, boolean local)
  { Vector updates = new Vector(); 
    updates.add(assignsTo); 
    Expression assignsToE = new BasicExpression(assignsTo); 
    Expression createsInstanceOfE = new BasicExpression(createsInstanceOf); 
    Expression whereexp = new BinaryExpression(":", assignsToE, createsInstanceOfE); 
    BExpression bqual = whereexp.binvariantForm(env,local); 
    return new BAnyStatement(updates, bqual, new BBasicStatement("skip"));
  } // No - add to the entity involved. 

  public String toEtl()
  { if (initialValue != null) 
    { return "  var " + assignsTo + " = " + initialValue + ";"; } 
    else 
    { return "  var " + assignsTo + ";"; } 
  }

  public String toStringJava()
  { String mode = ""; 
    if (isFrozen) 
    { mode = "final "; } 

    java.util.Map env = new java.util.HashMap(); 
    
    if (instanceType != null)
    { String jType = instanceType.getJava(); 

      System.out.println(">>> Creation instance type: " + instanceType); 
      System.out.println(">>> Creation Java type: " + jType); 
      System.out.println(); 

      if (initialExpression != null && assignsTo != null)
      { return "  " + jType + " " + assignsTo + " = " + initialExpression.queryForm(env,true) + ";\n"; }
    } 

    if (initialValue != null && instanceType != null) 
    { String jType = instanceType.getJava(); 
      return "  " + mode + jType + " " + assignsTo + " = " + initialValue + ";"; 
    } 
    else if (instanceType != null)
    { String jType = instanceType.getJava(); 
      if (initialExpression != null)
      { return "  " + jType + " " + assignsTo + " = " + initialExpression.queryForm(env,true) + ";\n"; }
      else if (Type.isRefType(instanceType))
      { String rt = jType; 
        if (instanceType.getElementType() != null) 
        { Type elemT = instanceType.getElementType();
          rt = elemT.getJava(); 
          // if (Type.isBasicType(elemT) ||
          //     elemT.isStructEntityType() ||  
          //     "Ref".equals(elemT.getName()))
          // { 
          return "  " + rt + "[] " + assignsTo + " = new " + rt + "[1];"; 
          // }
        }
        return "  " + rt + " " + assignsTo + ";";   
      }   
      else if (Type.isBasicType(instanceType)) 
      { return "  " + mode + jType + " " + assignsTo + ";"; } 
      else if (declarationOnly) 
      { return "  " + mode + jType + " " + assignsTo + ";"; } 
      else if (Type.isMapType(instanceType))
      { return "  " + mode + "Map " + assignsTo + " = new HashMap();"; } 
      else if (Type.isFunctionType(instanceType))
      { return "  " + jType + " " + assignsTo + " = null;"; } 
      else if (Type.isCollectionType(instanceType))
      { return "  " + mode + "List " + assignsTo + ";"; } 
      else if (instanceType.isEntity())
      { Entity ent = instanceType.getEntity(); 
        if (ent.hasStereotype("external"))
        { return "  " + jType + " " + assignsTo + " = new " + jType + "();\n"; } 
        else  
        { return "  " + jType + " " + assignsTo + " = new " + jType + "();\n" + 
               "  Controller.inst().add" + jType + "(" + assignsTo + ");"; 
        } 
      } // The 2nd statement only if is an entity of this system, not external
    } 
    else if (createsInstanceOf.equals("boolean") || 
             createsInstanceOf.equals("int") ||
        createsInstanceOf.equals("long") || 
        createsInstanceOf.equals("String") || 
        createsInstanceOf.equals("double"))
    { return "  " + mode + createsInstanceOf + " " + assignsTo + ";"; } 

    if (createsInstanceOf.startsWith("Set") || 
        createsInstanceOf.startsWith("Sequence"))
    { return "  List " + assignsTo + ";"; } 
    else if (createsInstanceOf.startsWith("Map"))
    { return "  Map " + assignsTo + ";"; } 
    else if (createsInstanceOf.startsWith("Function"))
    { return "  Evaluation<String,Object> " + assignsTo + ";"; } 
    else if (createsInstanceOf.startsWith("Ref"))
    { return "  Object[] " + assignsTo + " = new Object[1];"; }
    else if (createsInstanceOf.equals("OclAny"))
    { return "  Object " + assignsTo + ";"; }
    else if (createsInstanceOf.equals("OclType"))
    { return "  Class " + assignsTo + ";"; }
    else if (createsInstanceOf.equals("OclDate"))
    { return "  Date " + assignsTo + ";"; }
    else if (createsInstanceOf.equals("OclRandom"))
    { return "  OclRandom " + assignsTo + ";"; }
    else if (Type.isOclLibraryType(createsInstanceOf))
    { return "  " + createsInstanceOf + " " + assignsTo + ";"; }

    return "  " + mode + createsInstanceOf + " " + assignsTo + " = new " + createsInstanceOf + "();\n" + 
           "  Controller.inst().add" + createsInstanceOf + "(" + assignsTo + ");"; 
  }

  public String toStringJava6()
  { java.util.Map env = new java.util.HashMap(); 

    String mode = ""; 
    if (isFrozen) 
    { mode = "final "; } 
    
    if (instanceType != null)
    { String jType = instanceType.getJava6(); 
      if (initialExpression != null)
      { return "  " + mode + jType + " " + assignsTo + " = " + initialExpression.queryFormJava6(env,true) + ";\n"; }
      else if (Type.isRefType(instanceType))
      { String rt = jType; 
        if (instanceType.getElementType() != null) 
        { Type elemT = instanceType.getElementType();
          rt = elemT.getJava6(); 
          // if (Type.isBasicType(elemT) ||
          //     elemT.isStructEntityType() ||  
          //     "Ref".equals(elemT.getName()))
          // { 
          return "  " + mode + rt + "[] " + assignsTo + " = new " + rt + "[1];"; 
          // }
        }
        return "  " + mode + rt + " " + assignsTo + ";";   
      }   
      else if (Type.isBasicType(instanceType)) 
      { return "  " + mode + jType + " " + assignsTo + ";"; } 
      else if (Type.isMapType(instanceType))
      { return "  " + mode + "Map " + assignsTo + " = new HashMap();"; } 
      else if (Type.isFunctionType(instanceType))
      { return "  " + mode + jType + " " + assignsTo + " = null;"; } 
      else if (Type.isSetType(instanceType))
      { return "  " + mode + "HashSet " + assignsTo + ";"; } 
      else if (Type.isSequenceType(instanceType))
      { return "  " + mode + "ArrayList " + assignsTo + ";"; } 
      else if (instanceType.isEntity())
      { Entity ent = instanceType.getEntity(); 
        if (ent.hasStereotype("external"))
        { return "  " + mode + jType + " " + assignsTo + " = new " + jType + "();\n"; } 
        else
        { return "  " + mode + jType + " " + assignsTo + " = new " + jType + "();\n" + 
                 "  Controller.inst().add" + jType + "(" + assignsTo + ");"; 
        }
      }  
    } 
    else if (createsInstanceOf.equals("boolean") || 
        createsInstanceOf.equals("int") ||
        createsInstanceOf.equals("long") || 
        createsInstanceOf.equals("String") || 
        createsInstanceOf.equals("double"))
    { return "  " + mode + createsInstanceOf + " " + assignsTo + ";"; } 

    if (createsInstanceOf.startsWith("Set"))
    { return "  HashSet " + assignsTo + ";"; } 
    else if (createsInstanceOf.startsWith("Sequence"))
    { return "  ArrayList " + assignsTo + ";"; } 
    else if (createsInstanceOf.startsWith("Map"))
    { return "  Map " + assignsTo + ";"; } 
    else if (createsInstanceOf.startsWith("Function"))
    { return "  Evaluation<String,Object> " + assignsTo + ";"; } 
    else if (createsInstanceOf.startsWith("Ref"))
    { return "  Object[] " + assignsTo + " = new Object[1];"; }
    else if (createsInstanceOf.equals("OclAny"))
    { return "  Object " + assignsTo + ";"; }
    else if (createsInstanceOf.equals("OclType"))
    { return "  Class " + assignsTo + ";"; }
    else if (createsInstanceOf.equals("OclDate"))
    { return "  Date " + assignsTo + ";"; }
    else if (createsInstanceOf.equals("OclRandom"))
    { return "  OclRandom " + assignsTo + ";"; }
    else if (Type.isOclLibraryType(createsInstanceOf))
    { return "  " + createsInstanceOf + " " + assignsTo + ";"; }

    return "  " + createsInstanceOf + " " + assignsTo + " = new " + createsInstanceOf + "();\n" + 
           "  Controller.inst().add" + createsInstanceOf + "(" + assignsTo + ");"; 
  }

  public String toStringJava7()
  { 
    java.util.Map env = new java.util.HashMap(); 

    String mode = ""; 
    if (isFrozen) 
    { mode = "final "; } 
    
    if (instanceType != null)
    { String jType = instanceType.getJava7(elementType); 
      if (initialExpression != null)
      { return "  " + mode + jType + " " + assignsTo + " = " + initialExpression.queryFormJava7(env,true) + ";\n"; }
      else if (Type.isRefType(instanceType))
      { String rt = jType; 
        if (instanceType.getElementType() != null) 
        { Type elemT = instanceType.getElementType();
          rt = elemT.getJava7(); 
          // if (Type.isBasicType(elemT) ||
          //     elemT.isStructEntityType() ||  
          //     "Ref".equals(elemT.getName()))
          // { 
          return "  " + mode + rt + "[] " + assignsTo + " = new " + rt + "[1];"; 
          // }
        }
        return "  " + mode + rt + " " + assignsTo + ";";   
      }   
      else if (Type.isBasicType(instanceType)) 
      { return "  " + mode + jType + " " + assignsTo + ";"; } 
      else if (Type.isMapType(instanceType))
      { return "  " + mode + jType + " " + assignsTo + " = new " + jType + "();"; } 
      else if (Type.isFunctionType(instanceType))
      { return "  " + mode + jType + " " + assignsTo + " = null;"; } 
      else if (Type.isBasicType(instanceType) || 
               Type.isSetType(instanceType) || 
               Type.isSequenceType(instanceType)) 
      { return "  " + mode + jType + " " + assignsTo + ";"; } 
      else if (instanceType.isEntity())
      { Entity ent = instanceType.getEntity(); 
        if (ent.hasStereotype("external"))
        { return "  " + mode + jType + " " + assignsTo + " = new " + jType + "();\n"; } 
        else
        { return "  " + mode + jType + " " + assignsTo + " = new " + jType + "();\n" + 
                 "  Controller.inst().add" + jType + "(" + assignsTo + ");"; 
        }
      }  
    } 
    else if (createsInstanceOf.equals("boolean") || 
        createsInstanceOf.equals("int") ||
        createsInstanceOf.equals("long") || 
        createsInstanceOf.equals("String") || 
        createsInstanceOf.equals("double"))
    { return "  " + mode + createsInstanceOf + " " + assignsTo + ";"; } 

    if (createsInstanceOf.startsWith("Set"))
    { return "  HashSet " + assignsTo + ";"; } 
    else if (createsInstanceOf.startsWith("Sequence"))
    { return "  ArrayList " + assignsTo + ";"; } 
    else if (createsInstanceOf.startsWith("Map"))
    { return "  HashMap " + assignsTo + ";"; } 
    else if (createsInstanceOf.startsWith("Function"))
    { return "  Function<String,Object> " + assignsTo + ";"; } 
    else if (createsInstanceOf.startsWith("Ref"))
    { return "  Object[] " + assignsTo + " = new Object[1];"; }
    else if (createsInstanceOf.equals("OclAny"))
    { return "  Object " + assignsTo + ";"; }
    else if (createsInstanceOf.equals("OclType"))
    { return "  Class " + assignsTo + ";"; }
    else if (createsInstanceOf.equals("OclDate"))
    { return "  OclDate " + assignsTo + ";"; }
    else if (createsInstanceOf.equals("OclRandom"))
    { return "  OclRandom " + assignsTo + ";"; }
    else if (createsInstanceOf.equals("OclIterator"))
    { return "  OclIterator " + assignsTo + ";"; }
    else if (Type.isOclLibraryType(createsInstanceOf))
    { return "  " + createsInstanceOf + " " + assignsTo + ";"; }

    return "  " + mode + createsInstanceOf + " " + assignsTo + " = new " + createsInstanceOf + "();\n" + 
           "  Controller.inst().add" + createsInstanceOf + "(" + assignsTo + ");"; 
  }


  public String toStringCSharp()
  { String cstype = createsInstanceOf;
 
    if (instanceType != null)
    { String jType = instanceType.getCSharp(); 

      System.out.println(">>> Instance type: " + instanceType); 
      System.out.println(">>> C# type: " + jType); 

      if (initialExpression != null && assignsTo != null)
      { return "  " + jType + " " + assignsTo + " = " + initialExpression.toCSharp() + ";\n"; }
      else if (Type.isRefType(instanceType))
      { String rt = "object"; 
        if (instanceType.getElementType() != null) 
        { Type elemT = instanceType.getElementType();
          rt = elemT.getCSharp(); 
          if (Type.isBasicType(elemT) ||
              elemT.isStructEntityType() ||  
              "Ref".equals(elemT.getName()))
          { return "  " + rt + "* " + assignsTo + ";"; }
        }
        return "  " + rt + " " + assignsTo + ";";   
      }   
      else if (Type.isBasicType(instanceType)) 
      { return "  " + jType + " " + assignsTo + ";"; } 
      else if (Type.isMapType(instanceType))
      { return "  Hashtable " + assignsTo + ";"; }
      else if (Type.isSetType(instanceType))
      { Type et = instanceType.getElementType(); 
        return "  HashSet<" + Type.getCSharptype(et) + "> " + assignsTo + ";"; 
      }
      else if (Type.isSequenceType(instanceType))
      { return "  ArrayList " + assignsTo + ";"; } 
      else if (Type.isFunctionType(instanceType))
      { String kt = "object"; 
        if (instanceType.getKeyType() != null) 
        { kt = instanceType.getKeyType().getCSharp(); } 
        String rt = "object"; 
        if (instanceType.getElementType() != null) 
        { rt = instanceType.getElementType().getCSharp(); } 
        return "  Func<" + kt + "," + rt + "> " + assignsTo + ";"; 
      }   
      else if (Type.isExceptionType(instanceType))
      { return "  " + jType + " " + assignsTo + ";"; }  
      else if (instanceType.isEntity())
      { Entity ent = instanceType.getEntity();
        String ename = ent.getName(); 

        if (ent.genericParameter)
        { return "  " + ename + " " + assignsTo + ";\n"; } 

        String gpars = ""; // ent.typeParameterTextCSharp(); 
       

        if (ent.hasStereotype("external"))
        { return "  " + jType + gpars + " " + assignsTo + " = new " + jType + gpars + "();\n"; } 
        else
        { return "  " + jType + gpars + " " + assignsTo + " = new " + jType + gpars + "();\n" + 
                 "  Controller.inst().add" + ename + "(" + assignsTo + ");"; 
        } 
      } 
    } 
    else if (createsInstanceOf.startsWith("Set"))
    { return "  HashSet<object> " + assignsTo + ";"; }  
    else if (createsInstanceOf.startsWith("Sequence"))
    { return "  ArrayList " + assignsTo + ";"; } 
    else if (createsInstanceOf.startsWith("Map"))
    { return "  Hashtable "  + assignsTo + ";"; }
    else if (createsInstanceOf.startsWith("Function"))
    { return "  Func<object,object> " + assignsTo + ";"; }
    else if (createsInstanceOf.startsWith("Ref"))
    { return "  object* " + assignsTo + ";"; }
        
    if (createsInstanceOf.equals("boolean")) 
    { cstype = "bool"; 
      return "  " + cstype + " " + assignsTo + ";";   
    } 
    else if (createsInstanceOf.equals("String")) 
    { cstype = "string"; 
      return "  " + cstype + " " + assignsTo + ";"; 
    } 
    else if (createsInstanceOf.equals("int") || 
             createsInstanceOf.equals("long") ||  
             createsInstanceOf.equals("double"))
    { return "  " + createsInstanceOf + " " + assignsTo + ";"; } 
    else if (createsInstanceOf.equals("OclAny"))
    { return "  object " + assignsTo + ";"; }
    else if (createsInstanceOf.equals("OclType"))
    { return "  OclType " + assignsTo + ";"; }
    else if (createsInstanceOf.equals("OclRandom"))
    { return "  OclRandom " + assignsTo + ";"; } 
    else if (createsInstanceOf.equals("OclProcess"))
    { return "  OclProcess " + assignsTo + ";"; }
    else if (createsInstanceOf.equals("OclDate"))
    { return "  DateTime " + assignsTo + ";"; }
    else if (createsInstanceOf.equals("OclIterator"))
    { return "  OclIterator " + assignsTo + ";"; }
    else if (createsInstanceOf.equals("OclFile"))
    { return "  OclFile " + assignsTo + ";"; }
    else if (Type.isOclLibraryType(createsInstanceOf))
    { return "  " + createsInstanceOf + " " + assignsTo + ";"; }

    return createsInstanceOf + " " + assignsTo + " = new " + createsInstanceOf + "();\n" + 
                 "  Controller.inst().add" + createsInstanceOf + "(" + assignsTo + ");";  
  } // ignores enumerations

  public String toStringCPP()  // Function types?
  { String cstype = createsInstanceOf; 
    String cet = "void*"; 
    java.util.Map env = new java.util.HashMap(); 

    if (instanceType != null)
    { String jType = instanceType.getCPP(elementType); 
      if (initialExpression != null && assignsTo != null)
      { return "  " + jType + " " + assignsTo + " = " + initialExpression.queryFormCPP(env,true) + ";\n"; }

      if (Type.isBasicType(instanceType)) 
      { return "  " + jType + " " + assignsTo + ";"; } 
      else if (Type.isRefType(instanceType))
      { String rt = "void*"; 
        if (instanceType.getElementType() != null) 
        { Type elemT = instanceType.getElementType();
          rt = elemT.getCPP(); 
          if (Type.isBasicType(elemT) ||
              elemT.isStructEntityType() ||  
              "Ref".equals(elemT.getName()))
          { return "  " + rt + "* " + assignsTo + ";"; }
        }
        return "  " + rt + " " + assignsTo + ";";   
      }   
      else if (Type.isCollectionType(instanceType) || 
               Type.isMapType(instanceType) || 
               Type.isFunctionType(instanceType))
      { if (variable != null && elementType == null) 
        { elementType = variable.getElementType(); 
          jType = instanceType.getCPP(elementType);    
        }     
        return "  " + jType + " " + assignsTo + ";"; 
      } 
      else if (instanceType.isEntity())
      { Entity ent = instanceType.getEntity(); 
        String ename = ent.getName(); 

        if (ent.genericParameter)
        { return "  " + ename + " " + assignsTo + ";\n"; } 

        String gpars = ent.typeParameterTextCPP(); 

        if (initialExpression != null)
        { return "  " + jType + gpars + " " + assignsTo + " = " + initialExpression.toCPP() + ";\n"; }
        else if (ent.hasStereotype("external"))
        { return "  " + jType + gpars + " " + assignsTo + " = new " + ename + gpars + "();\n"; } 
        else
        { return "  " + jType + gpars + " " + assignsTo + " = new " + ename + gpars + "();\n" + 
                 "  Controller::inst->add" + ename + "(" + assignsTo + ");";
        }  
      } 
      else if (Type.isExceptionType(instanceType))
      { return "  " + jType + "* " + assignsTo + ";"; }  
      else if (instanceType.getName().equals("OclType"))
      { return "  OclType* " + assignsTo + ";"; }
      else if (instanceType.getName().equals("OclDate"))
      { return "  OclDate* " + assignsTo + ";"; }
      else if (instanceType.getName().equals("OclRandom"))
      { return "  OclRandom* " + assignsTo + ";"; }
      else if (instanceType.getName().equals("OclFile"))
      { return "  OclFile* " + assignsTo + ";"; }
      else if (instanceType.getName().equals("OclProcess"))
      { return "  OclProcess* " + assignsTo + ";"; }
      else if (instanceType.getName().equals("OclIterator"))
      { if (elementType != null) 
        { String celemt = elementType.getCPP(); 
          return "  OclIterator<" + celemt + ">* " + assignsTo + ";"; 
        } 
        if (variable != null && variable.getElementType() != null) 
        { String celemt = variable.getElementType().getCPP();
          return "  OclIterator<" + celemt + ">* " + assignsTo + ";"; 
        } 
 
        return "  OclIterator* " + assignsTo + ";"; 
      }
      else if (Type.isOclLibraryType(instanceType.getName()))
      { return "  " + createsInstanceOf + "* " + assignsTo + ";"; }
    } 
    else if (elementType != null) 
    { cet = elementType.getCPP(); }
    else if (variable != null && 
             variable.getElementType() != null) 
    { cet = variable.getElementType().getCPP(); }

    if (createsInstanceOf.startsWith("Set"))
    { return "  std::set<" + cet + ">* " + assignsTo + ";"; } 

    if (createsInstanceOf.startsWith("Sequence"))
    { return "  vector<" + cet + ">* " + assignsTo + ";"; } 

    if (createsInstanceOf.startsWith("Map"))
    { return "  map<string, " + cet + ">* " + assignsTo + ";"; } 

    if (createsInstanceOf.startsWith("Function"))
    { return "  function<" + cet + "(string)> " + assignsTo + ";"; } 

    if (createsInstanceOf.startsWith("Ref"))
    { return "  void* " + assignsTo + ";"; }
    
    if (createsInstanceOf.equals("boolean")) 
    { cstype = "bool"; 
      return "  " + cstype + " " + assignsTo + ";";   
    } 
    else if (createsInstanceOf.equals("String")) 
    { cstype = "string"; 
      return "  " + cstype + " " + assignsTo + ";"; 
    } 
    else if (createsInstanceOf.equals("int") || 
             createsInstanceOf.equals("long") || createsInstanceOf.equals("double"))
    { return "  " + createsInstanceOf + " " + assignsTo + ";"; } 
    else if (createsInstanceOf.equals("OclAny"))
    { return "  void* " + assignsTo + ";"; }
    else if (createsInstanceOf.equals("OclType"))
    { return "  OclType* " + assignsTo + ";"; }
    else if (createsInstanceOf.equals("OclDate"))
    { return "  OclDate* " + assignsTo + ";"; }
    else if (createsInstanceOf.equals("OclRandom"))
    { return "  OclRandom* " + assignsTo + ";"; }
    else if (createsInstanceOf.equals("OclFile"))
    { return "  OclFile* " + assignsTo + ";"; }
    else if (createsInstanceOf.equals("OclIterator"))
    { return "  OclIterator<" + cet + ">* " + assignsTo + ";"; }
    else if (createsInstanceOf.equals("OclProcess"))
    { return "  OclProcess* " + assignsTo + ";"; }
    else if (Type.isExceptionType(createsInstanceOf))
    { return "  " + createsInstanceOf + "* " + assignsTo + ";"; }  
    else if (Type.isOclLibraryType(createsInstanceOf))
    { return "  " + createsInstanceOf + "* " + assignsTo + ";"; }

    return createsInstanceOf + " " + assignsTo + " = new " + createsInstanceOf + "();\n" + 
        "  Controller::inst->add" + createsInstanceOf + "(" + assignsTo + ");"; 
  } // and add to the set of instances? 

  public void display()
  { System.out.print(toString()); }

  public void display(PrintWriter out)
  { out.print(toString()); } 

  public void displayJava(String target)
  { System.out.println(toStringJava()); } 

  public void displayJava(String target, PrintWriter out)
  { out.println(toStringJava()); } 

  public boolean typeCheck(Vector types, Vector entities, Vector ctxs, Vector env)
  { Attribute att = 
      new Attribute(assignsTo,instanceType,
                    ModelElement.INTERNAL); 

    Type typ = Type.getTypeFor(createsInstanceOf, 
                               types, entities); 
    if (instanceType == null && typ != null) 
    { instanceType = typ; } 
    if (elementType != null) 
    { instanceType.setElementType(elementType); } 
    else if (typ != null) 
    { elementType = typ.elementType; 
      if ("String".equals(typ.getName()))
      { elementType = new Type("String", null); } 
    }
 
    if (instanceType == null) 
    { att.setType(typ); } 
    
    if (elementType != null) 
    { att.setElementType(elementType); } 
 
    variable = att; 
    env.add(att); 
	
    if (initialExpression != null) 
    { initialExpression.typeCheck(types,entities,ctxs,env); }
	
    // if typ == null && instanceType == null use the 
    // initialExpression type instead. 

    return true; 
  }  // createsInstanceOf must be a primitive type, String or entity, if Sequence, Set
     // there is not necessarily an element type. 
     // The element type needs be set when the statement is 
     // parsed or analysed. 

  public boolean typeInference(Vector types, Vector entities, 
    Vector ctxs, Vector env, java.util.Map vartypes)
  { Attribute att = 
      new Attribute(assignsTo,instanceType,
                    ModelElement.INTERNAL); 

    Type typ = Type.getTypeFor(createsInstanceOf, 
                               types, entities); 
    if (instanceType == null && typ != null) 
    { instanceType = typ; } 
    if (elementType != null) 
    { instanceType.setElementType(elementType); } 
    else if (typ != null) 
    { elementType = typ.elementType; 
      if ("String".equals(typ.getName()))
      { elementType = new Type("String", null); } 
    }
 
    if (instanceType == null) 
    { att.setType(typ); } 
    
    if (elementType != null) 
    { att.setElementType(elementType); } 
 
	
    if (initialExpression != null) 
    { initialExpression.typeInference(types,entities,
                                      ctxs,env,vartypes); 

      // JOptionPane.showInputDialog("--- " + vartypes + " -- Type inference for: " + initialExpression.getType() + " for " + initialExpression); 

      System.out.println(">>> Inferred type " + 
        initialExpression.getType() + "(" + 
        initialExpression.getElementType() + 
        ") for variable " + att); 

      Type initType = initialExpression.getType(); 
      Type initElemType = initialExpression.getElementType(); 

      if (!Type.isVacuousType(initType))
      { instanceType = initType;  
        att.setType(instanceType);
      } 
 
      if (!Type.isVacuousType(initElemType))
      { elementType = initElemType;
        instanceType.setElementType(initElemType);  
        att.setElementType(initElemType); 
      } 
    } 

    variable = att; 
    env.add(att); 
    vartypes.put(assignsTo, att.getType()); 

    return true; 
  }  

  public Expression wpc(Expression post)
  { return post; }

  public Expression wpc(Expression inv, Expression post)
  { return inv; }  

  public Vector dataDependents(Vector allvars, Vector vars)
  { // System.out.println(">---- " + assignsTo + " := " + initialExpression); 
    // System.out.println(">---- " + vars + " " + allvars); 

    if (initialExpression != null && assignsTo != null)
    { if (vars.contains("" + assignsTo))
      { Vector vused = 
            initialExpression.allAttributesUsedIn(); 
        // System.out.println(">-Attributes:--- " + vused); 
        Vector result = new Vector(); 
        result.addAll(vused); 
        Vector vs = initialExpression.getVariableUses(); 
        result = VectorUtil.union(result,vs);
       
        result.remove("" + assignsTo); 
        // System.out.println(assignsTo + " --from--> " + result); 
        result = VectorUtil.union(result,vars);
        result.remove("" + assignsTo); 
      
        return result; 
      } 
    }     
    return vars; 
  }  

  public Vector dataDependents(Vector allvars, Vector vars, Map mp, Map dlin)
  { // System.out.println(">---- " + assignsTo + " := " + initialExpression); 
    // System.out.println(">---- " + vars + " " + allvars); 

    if (initialExpression != null && assignsTo != null)
    { String lv = "" + assignsTo; 

      if (vars.contains(lv))
      { Vector vused = 
            initialExpression.allAttributesUsedIn(); 
        // System.out.println(">-Attributes:--- " + vused); 
        Vector result = new Vector(); 
        result.addAll(vused); 
        Vector vs = initialExpression.getVariableUses(); 
        result = VectorUtil.union(result,vs);
       
        result.remove(lv); 

        Vector readBEs = 
           initialExpression.allReadBasicExpressionData(); 

        for (int i = 0; i < readBEs.size(); i++) 
        { String rv = "" + readBEs.get(i); 
          dlin.add_pair(rv, lv);
        } 
        // System.out.println(assignsTo + " --from--> " + readBEs); 

        for (int i = 0; i < result.size(); i++) 
        { String rv = "" + result.get(i); 
          mp.add_pair(rv, lv); 
        } 

        result = VectorUtil.union(result,vars);
        result.remove("" + assignsTo); 
      
        return result; 
      } 
    }     
    return vars; 
  }  

  public boolean updates(Vector vars) 
  { if (vars.contains("" + assignsTo))
    { return true; }
    
    return false; 
  } 

  public String updateForm(java.util.Map env, boolean local, 
                           Vector types, Vector entities,
                           Vector vars)
  { return toStringJava(); }  

  public String updateFormJava6(java.util.Map env, boolean local)
  { return toStringJava6(); }  

  public String updateFormJava7(java.util.Map env, boolean local)
  { return toStringJava7(); }  

  public String updateFormCSharp(java.util.Map env, boolean local)
  { return toStringCSharp(); }  

  public String updateFormCPP(java.util.Map env, boolean local)
  { return toStringCPP(); }  

  public Vector readFrame()
  { Vector res = new Vector(); 
    // res.add(createsInstanceOf); 
    String declType = createsInstanceOf; 
        
    if (initialExpression != null) 
    { res.addAll(initialExpression.readFrame()); }  

    return res; 
  } 

  public Vector writeFrame()
  { Vector res = new Vector(); 
    String declType = createsInstanceOf; 
    Vector declTypes = new Vector(); 
    declTypes.add(declType); 

    if (assignsTo != null)
    { res.add(assignsTo); } 
    res.removeAll(declTypes); 

    return res; 
  } 

  public Statement checkConversions(Entity e, Type propType, Type propElemType, java.util.Map interp)
  { return this; } 

  public Statement replaceModuleReferences(UseCase uc)
  { return this; } 

  public int syntacticComplexity()
  { if (initialExpression == null)
    { return 3; } // depends upon the type really.

    int syncomp = initialExpression.syntacticComplexity(); 
    
    return 3 + syncomp; 
  } 

  public int cyclomaticComplexity()
  { return 0; } 

  public int epl()
  { return 1; }  

  public Vector allOperationsUsedIn()
  { Vector res = new Vector(); 
    if (initialExpression != null) 
    { res.addAll(initialExpression.allOperationsUsedIn()); }  
    return res; 
  } 

  public Vector allAttributesUsedIn()
  { Vector res = new Vector(); 
    if (initialExpression != null) 
    { res.addAll(initialExpression.allAttributesUsedIn()); }  
    return res; 
  } 

  public Vector getUses(String var)
  { Vector res = new Vector(); 
    if (initialExpression != null) 
    { res.addAll(initialExpression.getUses(var)); }  
    return res; 
  } 

  public Vector getVariableUses()
  { Vector res = new Vector(); 
    if (initialExpression != null) 
    { res.addAll(initialExpression.getVariableUses()); }  
    return res; 
  } 

  public Vector getVariableUses(Vector unused)
  { Vector res = new Vector(); 
    if (initialExpression != null) 
    { res.addAll(initialExpression.getVariableUses()); }  
    return res; 
  } 

  public Vector equivalentsUsedIn()
  { Vector res = new Vector(); 
    if (initialExpression != null) 
    { res.addAll(initialExpression.equivalentsUsedIn()); }  
    return res; 
  } 

  public Vector metavariables()
  { Vector res = new Vector(); 
    if (assignsTo != null) 
    { if (assignsTo.startsWith("_") && 
          assignsTo.length() == 2 && 
          Character.isDigit(assignsTo.charAt(1)))
      { res.add(assignsTo); }
      else if (assignsTo.startsWith("_") && 
          assignsTo.length() == 3 && 
          Character.isDigit(assignsTo.charAt(1)) && 
          Character.isDigit(assignsTo.charAt(2)))
      { res.add(assignsTo); } 
    } // can't be _* or _+
    
    if (instanceType != null) 
    { res.addAll(instanceType.metavariables()); }  

    if (initialExpression != null) 
    { res.addAll(initialExpression.metavariables()); }  

    return res; 
  } 

  public Vector cgparameters()
  { Vector args = new Vector();
    if (assignsTo != null) 
    { args.add(assignsTo); } 
    if (instanceType != null) 
    { args.add(instanceType); }
    if (initialExpression != null) 
    { args.add(initialExpression); }  
    return args; 
  } 


  public String cg(CGSpec cgs)
  { String etext = this + "";
    Vector args = new Vector();
    Vector eargs = new Vector(); 
	
    if (assignsTo != null) 
    { args.add(assignsTo); 
      eargs.add(assignsTo); 
    } 
	
    if (instanceType != null) 
    { args.add(instanceType.cg(cgs)); 
      eargs.add(instanceType); 
    }
	 
    if (initialExpression != null) 
    { args.add(initialExpression.cg(cgs)); 
      eargs.add(initialExpression); 
    }  

    CGRule r = cgs.matchedStatementRule(this,etext);

    System.out.println(">> Matched statement rule: " + r + " for " + this); 

    if (r != null)
    { return r.applyRule(args,eargs,cgs); }
    return etext;
  } // case of frozen
}



class SequenceStatement extends Statement
{ private Vector statements = new Vector();

  public SequenceStatement(Vector stats)
  { statements = stats; } 

  public void addStatements(SequenceStatement ss)
  { statements.addAll(ss.getStatements()); } 

  public void addStatements(Statement st)
  { if (st == null) 
    { return; } 

    if (st instanceof SequenceStatement)
    { statements.addAll(
          ((SequenceStatement) st).getStatements());
    } 
  }  

  public SequenceStatement(Statement s1, Statement s2)
  { statements = new Vector(); 
    statements.add(s1); 
    statements.add(s2); 
  } 

  public static Statement composedStatement(
                   Statement s1, Statement s2)
  { if (s1 == null) 
    { return s2; } 
    if (s2 == null) 
    { return s1; }
    return new SequenceStatement(s1,s2);
  } 

  public static Statement composedStatement(
                   Statement s1, Statement s2, Statement s3)
  { if (s1 == null && s2 == null) 
    { return s3; } 
    if (s1 == null && s3 == null) 
    { return s2; } 
    if (s2 == null && s3 == null) 
    { return s1; }
    if (s1 == null) 
    { return new SequenceStatement(s2,s3); } 
    if (s2 == null) 
    { return new SequenceStatement(s1,s3); } 
    if (s3 == null)
    { return new SequenceStatement(s1,s2); }
    SequenceStatement res = new SequenceStatement(); 
    res.addStatement(s1); 
    res.addStatement(s2);
    res.addStatement(s3); 
    return res; 
  } 
 

  public static Statement combineSequenceStatements(Statement s1, Statement s2) 
  { if (s1 == null) 
    { return s2; } 
    if (s2 == null) 
    { return s1; }
    
    if (s1 instanceof SequenceStatement)
    { SequenceStatement sqstat1 = (SequenceStatement) s1; 
      if (s2 instanceof SequenceStatement)
      { SequenceStatement sqstat2 = (SequenceStatement) s2; 
        sqstat1.addStatements(sqstat2.getStatements()); 
        return sqstat1; 
      } 
      else  
      { sqstat1.addStatement(s2); 
        return sqstat1; 
      } 
    } 
    else if (s2 instanceof SequenceStatement) 
    { SequenceStatement res = (SequenceStatement) s2; 
      res.addStatement(0,s1); 
      return res; 
    } 
    return new SequenceStatement(s1,s2); 
  }          

    
  public String getOperator() 
  { return ";"; } 

  public Object clone()
  { Vector newstats = new Vector(); 
    for (int i = 0; i < statements.size(); i++) 
    { Statement stat = (Statement) statements.get(i); 
      Statement newstat = (Statement) stat.clone(); 
      newstats.add(newstat); 
    } 
    SequenceStatement res = new SequenceStatement(newstats);
    res.setEntity(entity); 
    res.setBrackets(brackets); 
    return res;  
  } 

  public int execute(ModelSpecification sigma, ModelState beta)
  { // create new environment, then execute each statement 
    // in turn.

    int res = Statement.NORMAL; 

    // ModelState local = (ModelState) beta.clone(); 
    beta.addNewEnvironment(); 

    for (int i = 0; i < statements.size(); i++) 
    { Statement stat = (Statement) statements.get(i); 
      res = stat.execute(sigma, beta);

      if (res == Statement.BREAK || res == Statement.RETURN ||
          res == Statement.CONTINUE)
      { beta.removeLastEnvironment();
        return res; 
      }   
    } 

    beta.removeLastEnvironment();
    return Statement.NORMAL;  
  } 


  public Expression definedness()
  { Expression res = new BasicExpression(true); 
    Expression post = new BasicExpression(true); 
    
    for (int i = statements.size() - 1; i >= 0; i--) 
    { Statement stat = (Statement) statements.get(i); 
      Expression def = stat.definedness();
      def.setBrackets(true);  
      Expression inv = stat.wpc(res, post); 
      inv.setBrackets(true); 
      res = Expression.simplify("&", inv, def, null); 
    } 

    return res;  
  } 

  public int size()
  { return statements.size(); } 

  public boolean isEmpty()
  { return statements.size() == 0; } 

  public boolean notEmpty()
  { return statements.size() > 0; } 

  public boolean isSkip()
  { for (int i = 0; i < statements.size(); i++) 
    { Statement st = (Statement) statements.get(i); 
      if (st.isSkip()) { } 
      else 
      { return false; } 
    } 
    return true; 
  } 

  public String cg(CGSpec cgs)
  { String etext = this + "";
    Vector args = new Vector();
    if (statements.size() == 0)
    { etext = "skip"; 
      return ""; 
    }
    else if (statements.size() == 1)
    { Statement st = (Statement) statements.get(0);
      return st.cg(cgs);
    }
    else
    { SequenceStatement tailst = new SequenceStatement();
      Statement st0 = (Statement) statements.get(0);
      Vector newsts = new Vector();
      newsts.addAll(statements);
      newsts.remove(0);
      tailst.statements = newsts;
      args.add(st0.cg(cgs));
      args.add(tailst.cg(cgs));
    }
    CGRule r = cgs.matchedStatementRule(this,etext);

    // System.out.println(">> Matched statement rule: " + r + " for " + this); 

    if (r != null)
    { // System.out.println(">>> Sequence rule: " + r + 
      //                    " " + args); 
      String res = r.applyRule(args);
      // System.out.println(">>> Applied sequence rule: " + res);  
      return res; 
    }

    return etext;
  }

  public Vector cgparameters()
  {
    Vector args = new Vector();
    if (statements.size() == 0)
    { return args; }
    else if (statements.size() == 1)
    { Statement st = (Statement) statements.get(0);
      args.add(st);
      return args; 
    }
    else
    { SequenceStatement tailst = new SequenceStatement();
      Statement st0 = (Statement) statements.get(0);
      Vector newsts = new Vector();
      newsts.addAll(statements);
      newsts.remove(0);
      tailst.statements = newsts;
      args.add(st0);
      args.add(tailst);
    }
    return args;
  }

  public void findClones(java.util.Map clones, String rule, String op)
  { for (int i = 0; i < statements.size(); i++) 
    { Statement stat = (Statement) statements.get(i); 
      stat.findClones(clones,rule,op); 
    }

    // Clones of statements, at least 2: 

    Vector fstats = flattenSequenceStatement(); 

    Vector substats = VectorUtil.allSubsegments(fstats,2);

    // System.out.println(">>> All subsegments = " + substats); 
 
    for (int i = 0; i < substats.size(); i++) 
    { Vector subs = (Vector) substats.get(i); 
      Statement sq = new SequenceStatement(subs); 
      if (sq.syntacticComplexity() < UCDArea.CLONE_LIMIT) 
      { continue; } 

      String val = sq + ""; 
      Vector used = (Vector) clones.get(val); 
      if (used == null)
      { used = new Vector(); }
      if (rule != null && !used.contains(rule))
      { used.add(rule); }
      else if (op != null && !used.contains(op))
      { used.add(op); }
      clones.put(val,used);
    } 

    // System.out.println(">>> Clones: " + clones); 

  } 

  public void findClones(java.util.Map clones, 
                         java.util.Map cdefs, 
                         String rule, String op)
  { for (int i = 0; i < statements.size(); i++) 
    { Statement stat = (Statement) statements.get(i); 
      stat.findClones(clones,cdefs,rule,op); 
    }

    // Clones of statements, at least 2: 

    Vector fstats = flattenSequenceStatement(); 

    // System.out.println(">>> Flatttended seq: " + fstats);

    Vector substats = VectorUtil.allSubsegments(fstats,2); 

    // System.out.println(">>> All subsegments = " + substats); 

    for (int i = 0; i < substats.size(); i++) 
    { Vector subs = (Vector) substats.get(i); 
      Statement sq = new SequenceStatement(subs); 
      if (sq.syntacticComplexity() < UCDArea.CLONE_LIMIT) 
      { continue; } 

      String val = sq + ""; 
      Vector used = (Vector) clones.get(val); 
      if (used == null)
      { used = new Vector(); }
      if (rule != null && !used.contains(rule))
      { used.add(rule); }
      else if (op != null && !used.contains(op))
      { used.add(op); }
      clones.put(val,used);
      cdefs.put(val, sq); 
    } 

    // System.out.println(">>> Clones: " + clones); 

  } 

  public Vector allVariableNames()
  { Vector res = new Vector(); 
    for (int i = 0; i < statements.size(); i++) 
    { Statement stat = (Statement) statements.get(i); 
      res = VectorUtil.union(res,
                         stat.allVariableNames()); 
    }

    return res; 
  } 
  

  public Map energyUse(Map uses, Vector rUses, Vector aUses)
  { for (int i = 0; i < statements.size(); i++) 
    { Statement stat = (Statement) statements.get(i); 
      stat.energyUse(uses, rUses, aUses);

      if (i < statements.size()-1 && 
          Statement.endsWithControlFlowBreak(stat))
      { int acount = (int) uses.get("amber"); 
        uses.set("amber", acount+1); 
        aUses.add("!! Unreachable code: statements after " + stat + " in sequence cannot be reached -- they should be deleted"); 
      } 
    }

    return uses; 
  } 

  public java.util.Map collectionOperatorUses(int lev, 
                              java.util.Map uses, 
                              Vector vars)
  { for (int i = 0; i < statements.size(); i++) 
    { Statement stat = (Statement) statements.get(i); 
      stat.collectionOperatorUses(lev, uses, vars); 
    }

    return uses; 
  } 

  public void findMagicNumbers(java.util.Map mgns, String rule, String op)
  { for (int i = 0; i < statements.size(); i++) 
    { Statement stat = (Statement) statements.get(i); 
      stat.findMagicNumbers(mgns,rule,op); 
    }
  } 

  public Statement optimiseOCL()
  { Vector newstats = new Vector(); 
    for (int i = 0; i < statements.size(); i++) 
    { Statement stat = (Statement) statements.get(i); 

      if (stat.isSkip()) 
      { continue; } 
      
      Statement newstat = stat.optimiseOCL();

      if (newstat instanceof SequenceStatement && 
          ((SequenceStatement) newstat).size() == 1)
      { Statement ss = 
          ((SequenceStatement) newstat).getStatement(0); 
        newstats.add(ss); 
      }  
      else 
      { newstats.add(newstat); }

      if (i < statements.size() - 1 &&
          Statement.endsWithControlFlowBreak(newstat)) 
      { System.out.println(">> Deleting statements after " + newstat); 
        break; 
      } 
    } 

    SequenceStatement res = new SequenceStatement(newstats);
    res.setEntity(entity); 
    res.setBrackets(brackets); 
    return res;  
  } 

  public Statement dereference(BasicExpression var)
  { Vector newstats = new Vector(); 
    for (int i = 0; i < statements.size(); i++) 
    { Statement stat = (Statement) statements.get(i); 
      Statement newstat = (Statement) stat.dereference(var); 
      newstats.add(newstat); 
    } 
    SequenceStatement res = new SequenceStatement(newstats);
    res.setEntity(entity); 
    res.setBrackets(brackets); 
    return res;  
  } 

  public Statement addContainerReference(BasicExpression ref,
                                         String var, 
                                         Vector excl)
  { Vector newstats = new Vector(); 
    for (int i = 0; i < statements.size(); i++) 
    { Statement stat = (Statement) statements.get(i); 
      Statement newstat =
             stat.addContainerReference(ref,var,excl); 
      newstats.add(newstat); 
    } 
    SequenceStatement res = new SequenceStatement(newstats);
    res.setEntity(entity); 
    res.setBrackets(brackets); 
    return res;  
  } 

  public Statement checkConversions(Entity e, Type propType, Type propElemType, java.util.Map interp)
  { Vector newstats = new Vector(); 
    for (int i = 0; i < statements.size(); i++) 
    { Statement stat = (Statement) statements.get(i); 
      Statement newstat = stat.checkConversions(e,propType,propElemType,interp); 
      newstats.add(newstat); 
    } 
    SequenceStatement res = new SequenceStatement(newstats);
    res.setEntity(entity); 
    res.setBrackets(brackets); 
    return res;  
  } 

  public Statement replaceModuleReferences(UseCase uc)
  { Vector newstats = new Vector(); 
    for (int i = 0; i < statements.size(); i++) 
    { Statement stat = (Statement) statements.get(i); 
      Statement newstat = stat.replaceModuleReferences(uc); 
      newstats.add(newstat); 
    } 
    SequenceStatement res = new SequenceStatement(newstats);
    res.setEntity(entity); 
    res.setBrackets(brackets); 
    return res;  
  } 

  public Statement generateDesign(java.util.Map env, boolean local)
  { Vector newstats = new Vector(); 
    for (int i = 0; i < statements.size(); i++) 
    { Statement stat = (Statement) statements.get(i); 
      Statement newstat = stat.generateDesign(env,local); 
      newstats.add(newstat); 
    } 
    SequenceStatement res = new SequenceStatement(newstats);
    res.setEntity(entity); 
    res.setBrackets(brackets); 
    return res;  
  } 

  public Statement statLC(java.util.Map env, boolean local)
  { Vector newstats = new Vector(); 
    for (int i = 0; i < statements.size(); i++) 
    { Statement stat = (Statement) statements.get(i); 
      Statement newstat = stat.statLC(env,local); 
      newstats.add(newstat); 
    } 
    SequenceStatement res = new SequenceStatement(newstats);
    res.setEntity(entity); 
    res.setBrackets(brackets); 
    return res;  
  } 

  public static Statement statLC(Vector preds, java.util.Map env, boolean local) 
  { if (preds.size() == 0) 
    { return new SequenceStatement(); } 
    else if (preds.size() == 1) 
    { Expression e = (Expression) preds.get(0); 
      return e.statLC(env,local); 
    } 
    else 
    { SequenceStatement sts = new SequenceStatement(); 
      for (int i = 0; i < preds.size(); i++) 
      { Expression p = (Expression) preds.get(i); 
        Statement st = p.statLC(env,local); 
        sts.addStatement(st);
      } 
      return sts; 
    } 
  } 
 

  public SequenceStatement()
  { statements = new Vector(); } 

  public int getSize()
  { return statements.size(); } 

  public void setEntity(Entity e)
  { entity = e;
    for (int i = 0; i < statements.size(); i++) 
    { Statement stat = (Statement) statements.get(i);
      if (stat.entity == null) 
      { stat.setEntity(e); }  
    } 
  }

  public void addStatement(Statement s)
  { if (s != null) 
    { statements.add(s); }
  } 

  public void addStatements(Vector stats)
  { if (stats != null) 
    { statements.addAll(stats); }
  } 

  public void addStatement(int pos, Statement s)
  { if (pos >= statements.size() && s != null)
    { statements.add(s); } 
    else if (s != null) 
    { statements.add(pos,s); }
  }

  public void addBeforeEnd(Statement s)
  { int sz = statements.size(); 
    if (sz == 0 && s != null)
    { statements.add(s); } 
    else if (s != null) 
    { statements.add(sz-1,s); } 
  } 

  public Vector getStatements()
  { return statements; } 

  public Statement getStatement(int i) 
  { return (Statement) statements.get(i); } 

  public Statement substituteEq(String oldE, Expression newE)
  { Vector fstats = flattenSequenceStatement(); 

    Vector substats = VectorUtil.allSubsegments(fstats,2); 
    for (int i = 0; i < substats.size(); i++) 
    { Vector subs = (Vector) substats.get(i); 
      Statement sq = new SequenceStatement(subs); 
      if (oldE.equals(sq + "")) 
      { InvocationStatement istat = 
          new InvocationStatement(newE);
        Vector newv = new Vector(); 
        newv.add(istat); 
        Vector newfstats = 
          VectorUtil.replaceSubsequence(fstats, subs, newv); 
        return new SequenceStatement(newfstats); 
      } 
    }

    SequenceStatement stats = new SequenceStatement(); 
    for (int i = 0; i < statements.size(); i++) 
    { Statement stat = 
        ((Statement) statements.get(i)).substituteEq(oldE,newE);
      stats.addStatement(stat);
    } 
    stats.entity = entity; 
    stats.setBrackets(brackets); 
    return stats;
  } 

  public Statement removeSlicedParameters(BehaviouralFeature bf, Vector fpars)
  { SequenceStatement stats = new SequenceStatement(); 
    for (int i = 0; i < statements.size(); i++) 
    { Statement stat = 
        (Statement) statements.get(i); 
      Statement stat1 = 
         stat.removeSlicedParameters(bf,fpars); 
      stats.addStatement(stat1);
    } 
    stats.entity = entity; 
    stats.setBrackets(brackets); 
    return stats;
  } 
  

  public void display()
  { for (int i = 0; i < statements.size(); i++)
    { Statement ss = (Statement) statements.elementAt(i);
      // if (i > 0) 
      // { 
      System.out.print("  ");
      ss.display();  /* Problem if invocation statements have NL's */
      if (i < statements.size() - 1) 
      { System.out.println(" || "); }
    } 
  }

  public String saveModelData(PrintWriter out)
  { String res = Identifier.nextIdentifier("sequencestatement_"); 
    out.println(res + " : SequenceStatement");
    out.println(res + ".statId = \"" + res + "\"");  
    // out.println(res + ".kind = sequence");
    for (int i = 0; i < statements.size(); i++)
    { Statement ss = (Statement) statements.elementAt(i);
      String ssid = ss.saveModelData(out); 
      out.println(ssid + " : " + res + ".statements"); 
    } 
    return res; 
  } 

  public String saveModelData(PrintWriter out, Entity ent)
  { String res = Identifier.nextIdentifier("sequencestatement_"); 
    out.println(res + " : SequenceStatement");
    out.println(res + ".statId = \"" + res + "\"");  
    // out.println(res + ".kind = sequence");
    for (int i = 0; i < statements.size(); i++)
    { Statement ss = (Statement) statements.elementAt(i);
      String ssid = ss.saveModelData(out, ent); 
      out.println(ssid + " : " + res + ".statements"); 
    } 
    return res; 
  } 

  public String bupdateForm()
  { String res = ""; 
    for (int i = 0; i < statements.size(); i++)
    { Statement ss = (Statement) statements.elementAt(i);
      res = res + "  " + ss.bupdateForm(); 
      if (i < statements.size() - 1)
      { res = res + ";\n"; }
    } 
    return res; 
  }

  public BStatement bupdateForm(java.util.Map env, boolean local)
  { BParallelStatement res = new BParallelStatement(false); 
    for (int i = 0; i < statements.size(); i++)
    { Statement ss = (Statement) statements.elementAt(i);
      res.addStatement(ss.bupdateForm(env,local)); 
    } 
    return res; 
  }


  public void displayImp(String var)
  { for (int i = 0; i < statements.size(); i++)
    { Statement ss = (Statement) statements.elementAt(i);
      System.out.print("  "); ss.displayImp(var); 
      if (i < statements.size() - 1)
      { System.out.println(";"); }
    } 
  }

  public void displayImp(String var, PrintWriter out)
  { for (int i = 0; i < statements.size(); i++)
    { Statement ss = (Statement) statements.elementAt(i);
      out.print("  "); ss.displayImp(var,out);
      if (i < statements.size() - 1)
      { out.println(";"); }
    }
  }

  public void display(PrintWriter out)
  { for (int i = 0; i < statements.size(); i++)
    { Statement ss = (Statement) statements.elementAt(i);
      out.print("  "); ss.display(out);
      if (i < statements.size() - 1)
      { out.println(" || "); }
    } 
  }    

  public void displayJava(String target)
  { for (int i = 0; i < statements.size(); i++)
    { Statement ss = (Statement) statements.elementAt(i);
      if (i > 0)                 /* Hack */ 
      { System.out.print("  "); }
      if (ss != null)
      { ss.displayJava(target); } 
      System.out.println(); 
    } 
  }

  public void displayJava(String target, PrintWriter out)
  { for (int i = 0; i < statements.size(); i++)
    { Statement ss = (Statement) statements.elementAt(i);
      if (i > 0)                 /* Hack */ 
      { out.print("  "); }
      if (ss != null)
      { ss.displayJava(target,out); }
      out.println(); 
    } 
  }

  public String toStringJava()
  { String res = ""; 
    for (int i = 0; i < statements.size(); i++)
    { Statement ss = (Statement) statements.elementAt(i);
      if (i > 0)                 /* Hack */ 
      { res = res + "  "; }
      if (ss != null)
      { res = res + ss.toStringJava(); }
      res = res + "\n"; 
    } 
    // if (brackets)
    // { res = "( " + res + " )"; } 
    return res; 
  }

  public String toEtl()
  { String res = ""; 
    for (int i = 0; i < statements.size(); i++)
    { Statement ss = (Statement) statements.elementAt(i);
      if (i > 0)                 /* Hack */ 
      { res = res + "  "; }
      if (ss != null)
      { res = res + ss.toEtl(); }
      res = res + "\n"; 
    } 
    // if (brackets)
    // { res = "( " + res + " )"; } 
    return res; 
  }

  public String toString()
  { String res = ""; 
    for (int i = 0; i < statements.size(); i++)
    { if (statements.get(i) instanceof Statement) 
	  { Statement ss = (Statement) statements.elementAt(i);
        if (i < statements.size() - 1)     
        { res = res + ss + " ; "; }
        else 
        { res = res + ss + " "; }
      }  
    }
 
    if (brackets)
    { res = "( " + res + " )"; } 

    return res; 
  }

  public static boolean isBlock0(Statement tt)
  { if (tt instanceof SequenceStatement)
    { SequenceStatement ss = (SequenceStatement) tt; 
      if (ss.statements.size() == 0) 
      { return true; } 
    }
    return false; 
  } 

  public static boolean isBlock1(Statement tt)
  { if (tt instanceof SequenceStatement)
    { SequenceStatement ss = (SequenceStatement) tt; 
      if (ss.statements.size() == 1) 
      { return true; } 
    }
    return false; 
  } 

  public static boolean isBlockN(Statement tt)
  { if (tt instanceof SequenceStatement)
    { SequenceStatement ss = (SequenceStatement) tt; 
      if (ss.statements.size() > 1) 
      { return true; } 
    }
    return false; 
  } 

  public Vector flattenSequenceStatement()
  { Vector res = new Vector(); 
    if (statements.size() == 0) 
    { return res; } 
    for (int i = 0; i < statements.size(); i++) 
    { Statement si = (Statement) statements.get(i);
      if (si instanceof SequenceStatement)
      { Vector subseq = 
          ((SequenceStatement) si).flattenSequenceStatement(); 
        res.addAll(subseq); 
      } 
      else 
      { res.add(si); } 
    } 
    return res; 
  } 

  public String toFlatAST()
  { String res = ""; 

    if (statements.size() > 0)
    { res = res + " ; "; } 

    for (int i = 0; i < statements.size(); i++)
    { Statement si = (Statement) statements.get(i);
      res = res + si.toAST(); 
      if (i < statements.size()-1) 
      { res = res + " ; "; } 
    } 
    return res; 
  } 

  public String toAST()
  { String res = "";  
    if (statements.size() == 0)
    { res = "(OclStatement call skip)"; }
    else 
    { Vector stats = flattenSequenceStatement(); 

      res = "(OclStatement ( (OclStatementList "; 

      Statement s1 = (Statement) stats.get(0); 
      res = res + s1.toAST() + " "; 
      
      for (int i = 1; i < stats.size(); i++) 
      { Statement s2 = (Statement) stats.get(i); 
        res = res + " ; " + s2.toAST();  
      }  

      res = res + " ) ) )";
      return res;   
    }

    return res; 
  }

  public boolean containsSubexpression(Expression expr)
  { for (int i = 0; i < statements.size(); i++)
    { Statement si = (Statement) statements.get(i);
      if (si.containsSubexpression(expr)) 
      { return true; }
    }  
    return false; 
  } 

  public Vector singleMutants()
  { // a single mutant of s1 followed by seqrem, or 
    // s1 followed by single mutant of seqrem

    Vector res = new Vector(); 

    if (statements.size() == 0)
    { return res; }

    if (statements.size() == 1)
    { Statement s1 = (Statement) statements.get(0); 
      res = s1.singleMutants(); 
    } 
    else 
    { Statement s1 = (Statement) statements.get(0); 
      Vector s1muts = s1.singleMutants();
      Vector remstats = new Vector(); 
      remstats.addAll(statements); 
      remstats.remove(0); 

      for (int k = 0; k < s1muts.size(); k++)
      { Statement s1mut = (Statement) s1muts.get(k); 
        Vector s1rem = new Vector(); 
        s1rem.add(s1mut); 
        s1rem.addAll(remstats); 
        Statement seqrem = new SequenceStatement(s1rem); 
        res.add(seqrem);
      }

      Statement srest = new SequenceStatement(remstats); 
      Vector restmuts = srest.singleMutants(); 

      for (int k = 0; k < restmuts.size(); k++)
      { Statement restmut = (Statement) restmuts.get(k); 
        Vector s1rem = new Vector(); 
        s1rem.add(s1); 
        s1rem.add(restmut); 
        Statement seqrem = new SequenceStatement(s1rem); 
        res.add(seqrem);
      }
 
    }
    return res; 
  } 

  public boolean typeCheck(Vector types, Vector entities, Vector cs, Vector env)
  { boolean res = true;  
    // Vector newenv = new Vector(); 
    // newenv.addAll(env); 

    // if (statements.size() > 0 && (statements.get(0) instanceof CreationStatement))
    // { CreationStatement crs = (CreationStatement) statements.get(0); 
    //  // Add crs.assignsTo as new env of type crs.createsInstanceOf
    //  Type typ = Type.getTypeFor(crs.createsInstanceOf,types,entities); 
    //  Attribute param = new Attribute(crs.assignsTo, typ, ModelElement.INTERNAL); 
    //  newenv.add(param); 
    // } 

    for (int i = 0; i < statements.size(); i++) 
    { Statement s = (Statement) statements.get(i); 
      Vector context = new Vector(); 
      Entity ee = s.entity; 
      if (ee != null) 
      { if (cs.size() > 0 && (ee + "").equals(cs.get(0) + "")) { } 
        else 
        { context.add(ee); }
      } 
      context.addAll(cs); 
      res = s.typeCheck(types,entities,context,env);
      // System.err.println("ENV = " + env);  
    } 
    return res; 
  }  

  public boolean typeInference(Vector types, Vector entities, Vector cs, Vector env, java.util.Map vartypes)
  { boolean res = true; 

    for (int i = 0; i < statements.size(); i++) 
    { Statement s = (Statement) statements.get(i); 
      Vector context = new Vector(); 
      Entity ee = s.entity; 
      if (ee != null) 
      { if (cs.size() > 0 && 
            (ee + "").equals(cs.get(0) + "")) 
        { } 
        else 
        { context.add(ee); }
      } 
      context.addAll(cs); 
      res = s.typeInference(types,entities,context,env,vartypes);
    } 

    return res; 
  }  


  public Expression wpc(Expression post)
  { Expression e1 = (Expression) post.clone();
    for (int i = statements.size()-1; i >= 0; i--)
    { Statement stat = (Statement) statements.get(i);
      Expression e2 = stat.wpc(e1);
      e1 = e2;
    } 
    return e1; 
  }

  public Expression wpc(Expression inv, Expression post)
  { Expression e1 = (Expression) inv.clone();
    for (int i = statements.size()-1; i >= 0; i--)
    { Statement stat = (Statement) statements.get(i);
      Expression e2 = stat.wpc(e1,post);
      e1 = e2;
    } 
    return e1; 
  }  

  public Vector dataDependents(Vector allvars, Vector vars)
  { Vector vbls = new Vector(); 
    vbls.addAll(vars); 

    for (int i = statements.size() - 1; i >= 0; i--) 
    { Statement stat = (Statement) statements.get(i); 
      Vector v = stat.dataDependents(allvars, vbls); 
      vbls = new Vector(); 
      vbls.addAll(v); 
    } 
    return vbls; 
  }  

  public Vector dataDependents(Vector allvars, Vector vars, Map mp, Map dlin)
  { Vector vbls = new Vector(); 
    vbls.addAll(vars); 

    for (int i = statements.size() - 1; i >= 0; i--) 
    { Statement stat = (Statement) statements.get(i); 
      Vector v = stat.dataDependents(allvars, vbls, mp, dlin); 
      vbls = new Vector(); 
      vbls.addAll(v); 
    } 
    return vbls; 
  }  

  public Vector slice(Vector allvars, Vector vars)
  { Vector vbls = new Vector(); 
    vbls.addAll(vars); 
    Vector deleted = new Vector(); 

    for (int i = statements.size() - 1; i >= 0; i--) 
    { Statement stat = (Statement) statements.get(i); 
      if (stat instanceof SequenceStatement)
      { SequenceStatement stat1 = (SequenceStatement) stat; 
        Vector ss = stat1.slice(allvars,vbls); 
        statements.remove(stat); 
        statements.add(i,new SequenceStatement(ss)); 
      } 
      else if (stat.updates(vbls)) 
      { System.out.println(stat + " updates " + vbls); } // include in slice
      else 
      { deleted.add(stat); 
        System.out.println(">> Deleting statement: " + stat); 
      } 
      Vector v = stat.dataDependents(allvars, vbls); 
      vbls = new Vector(); 
      vbls.addAll(v); 
    } 
    
    for (int j = 0; j < deleted.size(); j++) 
    { statements.remove(deleted.get(j)); } 
    return statements; 
  } 

  public boolean updates(Vector v) 
  { for (int i = 0; i < statements.size(); i++) 
    { Statement st = (Statement) statements.get(i);
      if (st.updates(v)) 
      { return true; }
    }
    return false; 
 } 

  public Expression toExpression()
  { Expression res = new BasicExpression("skip");
    for (int i = 0; i < statements.size(); i++)
    { Statement st = (Statement) statements.get(i);
      Expression e = st.toExpression();
      if (i > 0)
      { res = new BinaryExpression("&",res,e); }
      else 
      { res = e; }
    }
    return res;
  }

  public void mergeSequenceStatements(Statement s)
  { if (s instanceof SequenceStatement)
    { statements.addAll(((SequenceStatement) s).statements); }
    else 
    { statements.add(s); }
  }

  public String updateForm(java.util.Map env, boolean local, Vector types, Vector entities,
                           Vector vars)
  { String res = ""; 
    for (int i = 0; i < statements.size(); i++)
    { Statement ss = (Statement) statements.elementAt(i);
      if (i > 0)                 /* Hack */ 
      { res = res + "  "; }
      if (ss != null)
      { res = res + ss.updateForm(env,local,types,entities,vars); }
      res = res + "\n"; 
    } 
    return res; 
  }

  public String deltaUpdateForm(java.util.Map env, boolean local)
  { String res = "";   // interprets A.op(pars) as iteration over A's in _modobjs
    for (int i = 0; i < statements.size(); i++)
    { Statement ss = (Statement) statements.elementAt(i);
      if (i > 0)                 /* Hack */ 
      { res = res + "  "; }
      if (ss != null)
      { if (ss instanceof InvocationStatement) 
        { res = res + ((InvocationStatement) ss).deltaUpdateForm(env,local); }
        else if (ss instanceof SequenceStatement) 
        { res = res + ((SequenceStatement) ss).deltaUpdateForm(env,local); } 
        else 
        { res = res + ss.updateForm(env,local,new Vector(), new Vector(), new Vector()); }
      } 
      res = res + "\n"; 
    } 
    return res; 
  }

  public String updateFormJava6(java.util.Map env, boolean local)
  { String res = ""; 
    for (int i = 0; i < statements.size(); i++)
    { Statement ss = (Statement) statements.elementAt(i);
      if (i > 0)                 /* Hack */ 
      { res = res + "  "; }
      if (ss != null)
      { res = res + ss.updateFormJava6(env,local); }
      res = res + "\n"; 
    } 
    return res; 
  }

  public String updateFormJava7(java.util.Map env, boolean local)
  { String res = ""; 
    for (int i = 0; i < statements.size(); i++)
    { Statement ss = (Statement) statements.elementAt(i);
      if (i > 0)                 /* Hack */ 
      { res = res + "  "; }
      if (ss != null)
      { res = res + ss.updateFormJava7(env,local); }
      res = res + "\n"; 
    } 
    return res; 
  }

  public String updateFormCSharp(java.util.Map env, boolean local)
  { String res = ""; 
    for (int i = 0; i < statements.size(); i++)
    { Statement ss = (Statement) statements.elementAt(i);
      if (i > 0)                 /* Hack */ 
      { res = res + "  "; }
      if (ss != null)
      { res = res + ss.updateFormCSharp(env,local); }
      res = res + "\n"; 
    } 
    return res; 
  }

  public String updateFormCPP(java.util.Map env, boolean local)
  { String res = ""; 
    for (int i = 0; i < statements.size(); i++)
    { Statement ss = (Statement) statements.elementAt(i);
      if (i > 0)                 /* Hack */ 
      { res = res + "  "; }
      if (ss != null)
      { res = res + ss.updateFormCPP(env,local); }
      res = res + "\n"; 
    } 
    return res; 
  }

  public Vector allPreTerms()
  { Vector res = new Vector();
    for (int i = 0; i < statements.size(); i++) 
    { res.addAll(((Statement) statements.get(i)).allPreTerms()); }  
    return res; 
  }  

  public Vector allPreTerms(String var)
  { Vector res = new Vector();
    for (int i = 0; i < statements.size(); i++) 
    { res.addAll(((Statement) statements.get(i)).allPreTerms(var)); }  
    return res; 
  }  

  public Vector readFrame()
  { Vector res = new Vector(); 
    for (int i = 0; i < statements.size(); i++) 
    { Statement stat = (Statement) statements.get(i); 
      res.addAll(stat.readFrame()); 
    } 
    return res; 
  } 

  public Vector writeFrame()
  { Vector res = new Vector(); 

    for (int i = 0; i < statements.size(); i++) 
    { Statement stat = (Statement) statements.get(i); 
      res = VectorUtil.union(res, stat.writeFrame()); 
    } 

    return res; 
  } 

  public int syntacticComplexity()
  { int res = 0; 

    for (int i = 0; i < statements.size(); i++) 
    { Statement stat = (Statement) statements.get(i); 
      res = res + stat.syntacticComplexity(); 
    } 

    if (res > 0) 
    { res = res + statements.size() - 1; } 
 
    return res; 
  } 

  public int cyclomaticComplexity()
  { int res = 0; 

    for (int i = 0; i < statements.size(); i++) 
    { Statement stat = (Statement) statements.get(i); 
      res = res + stat.cyclomaticComplexity(); 
    } 

    return res; 
  } 

  public int epl()
  { int res = 0; 

    for (int i = 0; i < statements.size(); i++) 
    { Statement stat = (Statement) statements.get(i); 
      res = res + stat.epl(); 
    } 

    return res; 
  } 

  public Vector allOperationsUsedIn()
  { Vector res = new Vector(); 
    for (int i = 0; i < statements.size(); i++) 
    { Statement stat = (Statement) statements.get(i); 
      res.addAll(stat.allOperationsUsedIn()); 
    } 
    return res; 
  } 

  public Vector allAttributesUsedIn()
  { Vector res = new Vector(); 
    for (int i = 0; i < statements.size(); i++) 
    { Statement stat = (Statement) statements.get(i); 
      res.addAll(stat.allAttributesUsedIn()); 
    } 
    return res; 
  } 

  public Vector getUses(String var)
  { Vector res = new Vector(); 
    for (int i = 0; i < statements.size(); i++) 
    { Statement stat = (Statement) statements.get(i); 
      res.addAll(stat.getUses(var)); 
    } 
    return res; 
  } 

  public Vector getVariableUses()
  { Vector res = new Vector(); 

    if (statements.size() == 0)
    { return res; } 

    Statement s1 = (Statement) statements.get(0); 

    if (statements.size() == 1) 
    { return s1.getVariableUses(); } 

    Vector tailseq = new Vector(); 
    tailseq.addAll(statements); 
    tailseq.remove(0); 
    SequenceStatement sstail = 
        new SequenceStatement(tailseq); 
    res = sstail.getVariableUses(); 

    if (s1 instanceof CreationStatement)
    { CreationStatement cs = (CreationStatement) s1; 
      String var = cs.getDefinedVariable(); 
      Expression use = 
        ModelElement.lookupExpressionByName(var,res); 
      if (use == null) 
      { System.err.println("!! Code smell (UVA): no use of local variable " + var + " in statements " + sstail); 
        System.err.println(); 
        cs.unusedStatement = true; 
      } 
      res = ModelElement.removeExpressionByName(var,res); 
      return res; 
    } 

    res.addAll(s1.getVariableUses()); 

    return res; 
  } 

  public Vector getVariableUses(Vector unused)
  { Vector res = new Vector(); 

    if (statements.size() == 0)
    { return res; } 

    Statement s1 = (Statement) statements.get(0); 

    if (statements.size() == 1) 
    { res = s1.getVariableUses(unused); 
      return res; 
    } 

    Vector tailseq = new Vector(); 
    tailseq.addAll(statements); 
    tailseq.remove(0); 
    SequenceStatement sstail = 
        new SequenceStatement(tailseq); 
    res = sstail.getVariableUses(unused); 

    if (s1 instanceof CreationStatement)
    { CreationStatement cs = (CreationStatement) s1; 
      String var = cs.getDefinedVariable(); 
      Expression use = 
        ModelElement.lookupExpressionByName(var,res); 
      if (use == null) 
      { System.err.println("!! Code smell (UVA): no use of local variable " + var + " in statements " + sstail); 
        System.err.println(); 
        unused.add(var); 
        cs.unusedStatement = true; 
      } 
      res = ModelElement.removeExpressionByName(var,res); 
      return res; 
    } 

    res.addAll(s1.getVariableUses()); 

    return res; 
  } 

  public Vector equivalentsUsedIn()
  { Vector res = new Vector(); 
    for (int i = 0; i < statements.size(); i++) 
    { Statement stat = (Statement) statements.get(i); 
      res.addAll(stat.equivalentsUsedIn()); 
    } 
    return res; 
  } 

  public Vector metavariables()
  { Vector res = new Vector(); 
    for (int i = 0; i < statements.size(); i++) 
    { Statement stat = (Statement) statements.get(i); 
      { res.addAll(stat.metavariables()); } 
    } 
    
    return res; 
  } 

  public Vector segments()
  { // subsequences of the sequence which consist of 
    // 1+ creation statements followed by 1+ other statements

    Vector res = new Vector(); 

    if (statements.size() == 0)
    { return res; }

    Vector allstatements = flattenSequenceStatement(); 

    Vector segment = new Vector();  
    Statement previous = null; 

    for (int i = 0; i < allstatements.size(); i++) 
    { Statement ss = (Statement) allstatements.get(i); 
      if (ss instanceof CreationStatement)
      { if (previous == null) 
        { segment.add(ss); 
          previous = ss; 
        } 
        else if (previous instanceof CreationStatement) 
        { segment.add(ss); 
          previous = ss; 
        } 
        else // new segment
        { res.add(segment); 
          segment = new Vector(); 
          segment.add(ss); 
          previous = ss; 
        } 
      } 
      else // another kind of statement     
      { segment.add(ss); 
        previous = ss; 
      } 
    }  
    res.add(segment); 
    return res; 
  } 
}


class CaseStatement extends Statement
{ Map cases = new Map();

  // This statement should never arise except from operation statemachines

  public Object clone() { return this; } 

  public String getOperator() 
  { return "case"; } 

  public Statement dereference(BasicExpression var) { return this; } 

  public Statement substituteEq(String oldE, Expression newE)
  { CaseStatement cs = new CaseStatement(); 
    Vector ss = cases.elements; 
    for (int i = 0; i < ss.size(); i++) 
    { Maplet mm = (Maplet) ss.get(i); 
      Statement stat = ((Statement) mm.dest).substituteEq(oldE,newE); 
      Maplet nn = new Maplet(mm.source,stat); 
      cs.addCase(nn); 
    } 
    return cs; 
  } 

  public Statement removeSlicedParameters(BehaviouralFeature bf, Vector fpars)
  { CaseStatement cs = new CaseStatement(); 
    Vector ss = cases.elements; 
    for (int i = 0; i < ss.size(); i++) 
    { Maplet mm = (Maplet) ss.get(i); 
      Statement stat = 
        ((Statement) mm.dest).removeSlicedParameters(bf,fpars); 
      Maplet nn = new Maplet(mm.source,stat); 
      cs.addCase(nn); 
    } 
    return cs; 
  } 

  public Statement addContainerReference(
                    BasicExpression ref, String var,
                    Vector excl)
  { CaseStatement cs = new CaseStatement(); 
    Vector ss = cases.elements; 
    for (int i = 0; i < ss.size(); i++) 
    { Maplet mm = (Maplet) ss.get(i); 
      Statement cse = (Statement) mm.dest; 
      Statement stat = 
        cse.addContainerReference(ref,var,excl); 
      Maplet nn = new Maplet(mm.source,stat); 
      cs.addCase(nn); 
    } 
    return cs; 
  } 

  public Statement optimiseOCL()
  { CaseStatement cs = new CaseStatement(); 
    Vector ss = cases.elements; 
    for (int i = 0; i < ss.size(); i++) 
    { Maplet mm = (Maplet) ss.get(i); 
      Statement cse = (Statement) mm.dest; 
      Statement stat = cse.optimiseOCL(); 
      Maplet nn = new Maplet(mm.source,stat); 
      cs.addCase(nn); 
    } 
    return cs; 
  } 

  public void addCase(Maplet mm)
  { cases.add_element(mm); }

  public void addCase(Named n, Statement s)
  { Maplet mm = new Maplet(n,s);
    cases.add_element(mm); }

  public Statement getCaseFor(Named nn)
  { Statement res = (Statement) cases.apply(nn);
    return res; }

  public void display()  /* Unused. */ 
  { int n = cases.elements.size();

    for (int i = 0; i < n; i++)
    { Maplet mm = (Maplet) cases.elements.elementAt(i);
      System.out.println("IF " + ((Named) mm.source).label + " THEN ");
      System.out.print("  "); 
      ((Statement) mm.dest).display(); 
      if (i < n-1) 
      { System.out.println("ELSE"); } } 
    for (int j = 0; j < n; j++)
    { System.out.print("END  "); } 
    System.out.println(" "); 
  } 

  public String toAST()
  { String res = "(OclStatement "; 

    int n = cases.elements.size();

    for (int i = 0; i < n; i++)
    { Maplet mm = (Maplet) cases.elements.elementAt(i);
      res = res + "if " + ((Named) mm.source).label + " then ";
      res = res + ((Statement) mm.dest).toAST() + " "; 
      if (i < n-1) 
      { res = res + "else "; } 
    }
    res = res + ")";

    // if (brackets)
    // { res = "(OclStatement ( " + res + " ) )"; } 
  
    return res; 
  } 

  public boolean containsSubexpression(Expression expr)
  { int n = cases.elements.size();

    for (int i = 0; i < n; i++)
    { Maplet mm = (Maplet) cases.elements.elementAt(i);
      Statement ss = (Statement) mm.dest; 
      if (ss.containsSubexpression(expr)) 
      { return true; } 
    } 

    return false; 
  } 

  public Vector singleMutants() 
  { return new Vector(); } 

  public String saveModelData(PrintWriter out)
  { String res = Identifier.nextIdentifier("sequencestatement_"); 
    out.println(res + " : SequenceStatement");
    out.println(res + ".statId = \"" + res + "\"");  
    out.println(res + ".kind = choice");
    for (int i = 0; i < cases.elements.size(); i++)
    { Maplet mm = (Maplet) cases.elements.elementAt(i);
      Statement ss = (Statement) mm.dest; 
      String ssid = ss.saveModelData(out); 
      out.println(ssid + " : " + res + ".statements"); 
    } 
    return res; 
  } // and the labels? 

  public String saveModelData(PrintWriter out, Entity ent)
  { String res = Identifier.nextIdentifier("sequencestatement_"); 
    out.println(res + " : SequenceStatement");
    out.println(res + ".statId = \"" + res + "\"");  
    out.println(res + ".kind = choice");
    for (int i = 0; i < cases.elements.size(); i++)
    { Maplet mm = (Maplet) cases.elements.elementAt(i);
      Statement ss = (Statement) mm.dest; 
      String ssid = ss.saveModelData(out, ent); 
      out.println(ssid + " : " + res + ".statements"); 
    } 
    return res; 
  } // and the labels? 

  public String bupdateForm()  /* Unused. */ 
  { int n = cases.elements.size();
    String res = ""; 

    for (int i = 0; i < n; i++)
    { Maplet mm = (Maplet) cases.elements.elementAt(i);
      res = res + "IF " + ((Named) mm.source).label + " THEN ";
      res = res + "  "; 
      res = res + ((Statement) mm.dest).bupdateForm() + "\n"; 
      if (i < n-1) 
      { res = res + "ELSE\n"; } } 
    for (int j = 0; j < n; j++)
    { res = res + "END  "; } 
    res = res + " \n"; 
    return res; 
  } 

  public BStatement bupdateForm(java.util.Map env, boolean local) 
  { return new BBasicStatement("skip"); } // should never be called. 

  public void display(PrintWriter out)   /* Unused */ 
  { int n = cases.elements.size();

    for (int i = 0; i < n; i++)
    { Maplet mm = (Maplet) cases.elements.elementAt(i);
      out.println("IF " + ((Named) mm.source).label + " THEN ");
      out.print("  ");
      ((Statement) mm.dest).display(out);
      if (i < n-1)
      { System.out.println("ELSE"); } }
    for (int j = 0; j < n; j++)
    { out.print("END  "); }
    out.println(" "); }  

  public void display(String s)
  { int n = cases.elements.size();
    if (n == 0) 
    { System.out.println("  skip"); 
      return; 
    } 

    for (int i = 0; i < n; i++)
    { Maplet mm = (Maplet) cases.elements.elementAt(i);
      System.out.println("  IF " + s + " = " + 
                         ((Named) mm.source).label + " THEN ");
      System.out.print("    ");
      ((Statement) mm.dest).display();  
      if (i < n-1)
      { System.out.println("  ELSE"); } 
    }
     
     /* System.out.print("  "); */ 
    for (int j = 0; j < n; j++)
    { System.out.print("  END"); }
    /* System.out.println(" "); */ 
  }

  public void displayMult(String s)
  { int n = cases.elements.size();
    if (n == 0) 
    { System.out.println("  skip");
      return;
    }

    for (int i = 0; i < n; i++)
    { Maplet mm = (Maplet) cases.elements.elementAt(i);
      System.out.println("  IF " + s + "(oo) = " + 
                         ((Named) mm.source).label + " THEN ");
      System.out.print("    ");
      ((Statement) mm.dest).display();  // add (oo) to calls.
      if (i < n-1)
      { System.out.println("  ELSE"); } 
    }
     
     /* System.out.print("  "); */ 
    for (int j = 0; j < n; j++)
    { System.out.print("  END"); }
    /* System.out.println(" "); */ 
  }

  public void display(String s, PrintWriter out)
  { int n = cases.elements.size();
    if (n == 0)
    { out.println("  skip");
      return;
    }

    for (int i = 0; i < n; i++)
    { Maplet mm = (Maplet) cases.elements.elementAt(i);
      out.println("  IF " + s + " = " +
                         ((Named) mm.source).label + " THEN ");
      out.print("    ");
      ((Statement) mm.dest).display(out);
      if (i < n-1)
      { out.println("  ELSE"); } 
    }
    
     /* System.out.print("  "); */
    for (int j = 0; j < n; j++)
    { out.print("  END"); }
    /* System.out.println(" "); */ 
  }


  public void displayJava(String s)
  /* s is name of actuator/sensor */
  { int n = cases.elements.size();

    // if (n == 0)
    // { System.out.println("  skip");
      // return;
    // }


    for (int i = 0; i < n; i++)
    { Maplet mm = (Maplet) cases.elements.elementAt(i);
      System.out.println("  if (M" + s + "." + s + " == " + 
                          ((Named) mm.source).label + ")");
       System.out.print("    { ");
       ((Statement) mm.dest).displayJava("M" + s);
       System.out.println("    }"); 
       if (i < n-1)
       { System.out.println("  else {"); } 
    }
  } 
     
  public void displayJava(String s, PrintWriter out)
  /* s is name of actuator/sensor */
  { int n = cases.elements.size();

    for (int i = 0; i < n; i++)
    { Maplet mm = (Maplet) cases.elements.elementAt(i);
      out.println("  if (M" + s + "." + s + " == " + 
                  ((Named) mm.source).label + ")");
      out.print("    { ");
      ((Statement) mm.dest).displayJava("M" + s, out);
      out.println("    }"); 
      if (i < n-1)
      { out.println("  else {"); } 
    }
  }

  public String toStringJava()
  /* s is name of actuator/sensor */
  { int n = cases.elements.size();
    String res = ""; 
    String s = "s"; 

    for (int i = 0; i < n; i++)
    { Maplet mm = (Maplet) cases.elements.elementAt(i);
      res = res + "  if (M" + s + "." + s + " == " + 
                  ((Named) mm.source).label + ")";
      res = res + "    {\n";
      res = res + ((Statement) mm.dest).toStringJava();
      res = res + "    }\n"; 
      if (i < n-1)
      { res = res + "  else {\n"; } 
    }
    return res; 
  }

  public String toEtl()
  /* s is name of actuator/sensor */
  { int n = cases.elements.size();
    String res = ""; 
    String s = "s"; 

    for (int i = 0; i < n; i++)
    { Maplet mm = (Maplet) cases.elements.elementAt(i);
      res = res + "  if (M" + s + "." + s + " == " + 
                  ((Named) mm.source).label + ")";
      res = res + "    {\n";
      res = res + ((Statement) mm.dest).toEtl();
      res = res + "    }\n"; 
      if (i < n-1)
      { res = res + "  else {\n"; } 
    }
    return res; 
  }

  public boolean typeCheck(Vector types, Vector entities, Vector cs, Vector env)
  { return true; }   // type check each case dest? 

  public boolean typeInference(Vector types, Vector entities, Vector cs, Vector env, java.util.Map vartypes)
  { return true; }   // type check each case dest? 

  public Expression wpc(Expression post)
  { return post; }  // Will not occur in a transition action. 

  public Expression wpc(Expression inv, Expression post)
  { return inv; }  

  public Vector dataDependents(Vector allvars, Vector vars)
  { return vars; }  

  public Vector dataDependents(Vector allvars, Vector vars, Map mp, Map dlin)
  { return vars; }  

  public boolean updates(Vector v) 
  { return false; } 

  /* Should never be called: */ 
  public String updateForm(java.util.Map env, boolean local, Vector types, Vector entities, 
                           Vector vars)
  { return toStringJava(); }

  public String updateFormJava6(java.util.Map env, boolean local)
  { return toStringJava(); }

  public String updateFormJava7(java.util.Map env, boolean local)
  { return toStringJava(); }

  public String updateFormCSharp(java.util.Map env, boolean local)
  { return toStringJava(); }

  public String updateFormCPP(java.util.Map env, boolean local)
  { return toStringJava(); }

  public Vector readFrame()
  { Vector res = new Vector(); 
    return res; 
  } 

  public Vector writeFrame()
  { Vector res = new Vector(); 
    return res; 
  } 

  public Statement checkConversions(Entity e, Type propType, Type propElemType, java.util.Map interp)
  { return this; } 

  public Statement replaceModuleReferences(UseCase uc)
  { return this; } 

  public int syntacticComplexity() 
  { int res = 0; 
    int n = cases.elements.size();

    for (int i = 0; i < n; i++)
    { Maplet mm = (Maplet) cases.elements.elementAt(i);
      Statement cse = (Statement) mm.dest;
      res = res + cse.syntacticComplexity() + 1; 
    }
    return res; 
  }

  public int cyclomaticComplexity() 
  { int res = 0; 
    int n = cases.elements.size();

    for (int i = 0; i < n; i++)
    { Maplet mm = (Maplet) cases.elements.elementAt(i);
      Statement cse = (Statement) mm.dest;
      res = res + cse.cyclomaticComplexity() + 1; 
    }
    return res; 
  }

  public Map energyUse(Map uses, Vector ruses, Vector ouses) 
  { 
    int n = cases.elements.size();

    for (int i = 0; i < n; i++)
    { Maplet mm = (Maplet) cases.elements.elementAt(i);
      Statement cse = (Statement) mm.dest;
      cse.energyUse(uses, ruses, ouses); 
    }

    return uses; 
  }

  public java.util.Map collectionOperatorUses(int lev, 
                              java.util.Map uses, Vector vars) 
  { 
    int n = cases.elements.size();

    for (int i = 0; i < n; i++)
    { Maplet mm = (Maplet) cases.elements.elementAt(i);
      Statement cse = (Statement) mm.dest;
      cse.collectionOperatorUses(lev, uses, vars); 
    }

    return uses; 
  }

  public int epl() 
  { int res = 0; 
    int n = cases.elements.size();

    for (int i = 0; i < n; i++)
    { Maplet mm = (Maplet) cases.elements.elementAt(i);
      Statement cse = (Statement) mm.dest;
      res = res + cse.epl() + 1; 
    }
    return res; 
  }


  public Vector allOperationsUsedIn()
  { Vector res = new Vector(); 
    for (int i = 0; i < cases.elements.size(); i++) 
    { Maplet mm = (Maplet) cases.elements.get(i); 
      res.addAll(((Statement) mm.dest).allOperationsUsedIn()); 
    } 
    return res; 
  } 

  public Vector allAttributesUsedIn()
  { Vector res = new Vector(); 
    for (int i = 0; i < cases.elements.size(); i++) 
    { Maplet mm = (Maplet) cases.elements.get(i); 
      res.addAll(((Statement) mm.dest).allAttributesUsedIn()); 
    } 
    return res; 
  } 

  public Vector equivalentsUsedIn()
  { Vector res = new Vector(); 
    for (int i = 0; i < cases.elements.size(); i++) 
    { Maplet mm = (Maplet) cases.elements.get(i); 
      res.addAll(((Statement) mm.dest).equivalentsUsedIn()); 
    } 
    return res; 
  } 

  public Vector metavariables()
  { Vector res = new Vector(); 
    for (int i = 0; i < cases.elements.size(); i++) 
    { Maplet mm = (Maplet) cases.elements.get(i); 
      res.addAll(((Statement) mm.dest).metavariables()); 
    } 
    return res; 
  } 

  public Vector cgparameters()
  { Vector res = new Vector(); 
    for (int i = 0; i < cases.elements.size(); i++) 
    { Maplet mm = (Maplet) cases.elements.get(i); 
      res.add(mm.dest); 
    } 
    return res; 
  } 

  public void findClones(java.util.Map clones, String op, String rule)
  { return; } 

  public void findClones(java.util.Map clones, 
                         java.util.Map cdefs, 
                         String op, String rule)
  { return; } 

}


class ErrorStatement extends Statement
{ // This represents a throw, raise or abort statement
  Expression thrownObject = null; 

  public ErrorStatement(Expression expr) 
  { thrownObject = expr; } 

  public void display()
  { // System.out.println("SELECT false THEN skip END"); 
    System.out.println("  error " + thrownObject); 
  }

  public String getOperator() 
  { return "error"; } 

  public Object clone() 
  { return new ErrorStatement(thrownObject); } 

  public Statement dereference(BasicExpression var) 
  { if (thrownObject != null) 
    { return new ErrorStatement(thrownObject.dereference(var)); }
    return new ErrorStatement(null); 
  }  

  public Statement substituteEq(String oldE, Expression newE)
  { if (thrownObject != null) 
    { Expression tobj = thrownObject.substituteEq(oldE,newE); 
      return new ErrorStatement(tobj); 
    } 
    return new ErrorStatement(null); 
  } 

  public Statement optimiseOCL()
  { if (thrownObject != null) 
    { Expression tobj = thrownObject.simplifyOCL(); 
      return new ErrorStatement(tobj); 
    } 
    return new ErrorStatement(null); 
  } 

  public Map energyUse(Map uses, Vector rUses, Vector aUses)
  { if (thrownObject != null) 
    { thrownObject.energyUse(uses, rUses, aUses);

      int syncomp = thrownObject.syntacticComplexity(); 
      if (syncomp > TestParameters.syntacticComplexityLimit)
      { int acount = (int) uses.get("amber"); 
        uses.set("amber", acount + 1); 
        aUses.add("! Code smell (MEL): too high expression complexity (" + syncomp + ") for " + thrownObject + "\n" +  
                  ">>> Recommend OCL refactoring");  
      }
    }
 
    return uses; 
  } 

  public java.util.Map collectionOperatorUses(int lev, 
                                 java.util.Map uses, 
                                 Vector vars)
  { if (thrownObject != null) 
    { thrownObject.collectionOperatorUses(lev, uses, vars); } 
    return uses; 
  } 

  public Statement removeSlicedParameters(BehaviouralFeature bf, Vector fpars)
  { if (thrownObject != null) 
    { Expression tobj = 
          thrownObject.removeSlicedParameters(bf,fpars); 
      return new ErrorStatement(tobj); 
    } 
    return new ErrorStatement(null); 
  } 

  public Statement addContainerReference(
                     BasicExpression ref, 
                     String var, Vector excl)
  { if (thrownObject != null) 
    { Expression tobj = 
         thrownObject.addContainerReference(ref,var,excl); 
      return new ErrorStatement(tobj); 
    } 
    return new ErrorStatement(null); 
  } 

  public String toString()
  { return "  error " + thrownObject; }

  public String toAST()
  { String res = "(OclStatement error " + thrownObject.toAST() + " )"; 

    // if (brackets)
    // { res = "(OclStatement ( " + res + " ) )"; } 

    return res; 
  } 

  public boolean containsSubexpression(Expression expr)
  { if (thrownObject == null) 
    { return thrownObject.containsSubexpression(expr); } 
    return false; 
  } 

  public Vector singleMutants()
  { if (thrownObject == null) 
    { return new Vector(); } 
    Vector exprs = thrownObject.singleMutants(); 
    Vector res = new Vector(); 
    for (int i = 0; i < exprs.size(); i++) 
    { Expression mvalue = (Expression) exprs.get(i); 
      res.add(new ErrorStatement(mvalue)); 
    } 
    return res; 
  } 


  public void display(PrintWriter out)
  { out.println("  error " + thrownObject); }

  public String bupdateForm()
  { return "SELECT false THEN skip END\n"; }

  public BStatement bupdateForm(java.util.Map env, boolean local)
  { return new BBasicStatement("SELECT false THEN skip END"); }

  public void displayJava(String t)
  { if (thrownObject == null) 
    { System.out.println("  throw null;"); } 
    else 
    { java.util.Map env = new java.util.HashMap(); 
      String qf = thrownObject.throwQueryForm(env,true); 
      System.out.println("  throw " + qf + ";"); 
    } 
  }

  public String saveModelData(PrintWriter out)
  { String res = Identifier.nextIdentifier("errorstatement_"); 
    out.println(res + " : ErrorStatement"); 
    out.println(res + ".statId = \"" + res + "\"");  

    if (thrownObject == null) 
    { out.println(res + ".thrownObject = null"); } 
    else 
    { String expId = thrownObject.saveModelData(out);
      out.println(res + ".thrownObject = " + expId);
    } 
    return res; 
  } 

  public String saveModelData(PrintWriter out, Entity ent)
  { return saveModelData(out); } 

  public String toStringJava()
  { if (thrownObject == null) 
    { return "  throw null;"; } 
    else 
    { java.util.Map env = new java.util.HashMap(); 
      String qf = thrownObject.throwQueryForm(env,true); 
      return "  throw " + qf + ";"; 
    }
  }
  
  public String toEtl()
  { return ""; }

  public void displayJava(String t, PrintWriter out)
  { out.println(toStringJava()); } 

  public boolean typeCheck(Vector types, Vector entities, Vector cs, Vector env)
  { if (thrownObject != null) 
    { thrownObject.typeCheck(types,entities,cs,env); } 
    return true;
  } 

  public boolean typeInference(Vector types, Vector entities, Vector cs, Vector env, java.util.Map vartypes)
  { if (thrownObject != null) 
    { thrownObject.typeInference(types,entities,
                                 cs,env,vartypes); 
    } 
    return true;
  } 
  
  public Expression wpc(Expression post)
  { return post; }

  public Expression wpc(Expression inv, Expression post)
  { return inv; }  


  public Vector dataDependents(Vector allvars, Vector vars)
  { return vars; }  

  public Vector dataDependents(Vector allvars, Vector vars, Map mp, Map dlin)
  { return vars; }  

  public boolean updates(Vector v) 
  { return false; } 

  public String updateForm(java.util.Map env, boolean local, Vector types, Vector entities, 
                           Vector vars)
  { if (thrownObject == null) 
    { return "  throw null;"; } 
    else 
    { String qf = thrownObject.throwQueryForm(env,true); 
      return "  throw " + qf + ";"; 
    }
 }

  public String updateFormJava6(java.util.Map env, boolean local)
  { if (thrownObject == null) 
    { return "  throw null;"; } 
    else 
    { String qf = thrownObject.throwQueryForm(env,true); 
      return "  throw " + qf + ";"; 
    }
  }

  public String updateFormJava7(java.util.Map env, boolean local)
  { if (thrownObject == null) 
    { return "  throw null;"; } 
    else 
    { String qf = thrownObject.throwQueryForm(env,true); 
      return "  throw " + qf + ";"; 
    }
  }

  public String updateFormCSharp(java.util.Map env, boolean local)
  { if (thrownObject == null) 
    { return "  throw null;"; } 
    else 
    { String qf = thrownObject.throwQueryFormCSharp(env,true); 
      return "  throw " + qf + ";"; 
    } 
  }

  public String updateFormCPP(java.util.Map env, boolean local)
  { if (thrownObject == null) 
    { return "  throw null;"; } 
    else 
    { String qf = thrownObject.throwQueryFormCPP(env,true); 
      return "  throw " + qf + ";"; 
    }
  }

  public Vector readFrame()
  { Vector res = new Vector();
    if (thrownObject != null) 
    { res.addAll(thrownObject.readFrame()); }  
    return res; 
  } 

  public Vector writeFrame()
  { Vector res = new Vector(); 
    return res; 
  } 

  public Statement checkConversions(Entity e, Type propType, Type propElemType, java.util.Map interp)
  { return this; } 

  public Statement replaceModuleReferences(UseCase uc)
  { return this; } 

  public int syntacticComplexity()
  { if (thrownObject != null) 
    { int syncomp = thrownObject.syntacticComplexity(); 
       
      return 1 + syncomp; 
    }
 
    return 1;
  } 

  public int cyclomaticComplexity()
  { return 0; } 

  public int epl()
  { return 0; } 

  public Vector allOperationsUsedIn()
  { Vector res = new Vector(); 
    if (thrownObject != null) 
    { res = thrownObject.allOperationsUsedIn(); } 
    return res; 
  } 

  public Vector allAttributesUsedIn()
  { Vector res = new Vector(); 
    if (thrownObject != null) 
    { res = thrownObject.allAttributesUsedIn(); } 
    return res; 
  } 

  public Vector getUses(String var)
  { Vector res = new Vector(); 
    if (thrownObject != null) 
    { res = thrownObject.getUses(var); } 
    return res; 
  } 

  public Vector getVariableUses()
  { Vector res = new Vector(); 
    if (thrownObject != null) 
    { res = thrownObject.getVariableUses(); } 
    return res; 
  } 

  public Vector equivalentsUsedIn()
  { Vector res = new Vector(); 
    if (thrownObject != null) 
    { res = thrownObject.equivalentsUsedIn(); } 
    return res; 
  } 

  public Vector metavariables()
  { Vector res = new Vector(); 
    if (thrownObject != null) 
    { res = thrownObject.metavariables(); } 
    return res; 
  } 

  public Vector cgparameters()
  { Vector args = new Vector();
    if (thrownObject != null) 
    { args.add(thrownObject); } 
    return args; 
  } 

  public Vector cgterms()
  { Vector args = new Vector();
    args.add("error"); 
    if (thrownObject != null) 
    { args.add(thrownObject); } 
    return args; 
  } 

  public String cg(CGSpec cgs)
  { String etext = this + "";
    Vector args = new Vector();
    if (thrownObject != null) 
    { args.add(thrownObject.cg(cgs)); } 

    CGRule r = cgs.matchedStatementRule(this,etext);

    System.out.println(">> Matched statement rule: " + r + " for " + this); 

    if (r != null)
    { return r.applyRule(args); }

    return etext;
  }

  public void findClones(java.util.Map clones, String op, String rule)
  { if (thrownObject != null) 
    { thrownObject.findClones(clones,op,rule); }
  }  

  public void findClones(java.util.Map clones, 
                         java.util.Map cdefs, 
                         String op, String rule)
  { if (thrownObject != null) 
    { thrownObject.findClones(clones,cdefs,op,rule); }
  }  


}

class AssertStatement extends Statement
{ // This represents an assert statement
  Expression condition = null; 
  Expression message = null; 

  public AssertStatement(Expression expr) 
  { condition = expr; } 

  public AssertStatement(Expression expr, Expression msg) 
  { condition = expr; 
    message = msg; 
  } 

  public void display()
  { // System.out.println("SELECT false THEN skip END"); 
    if (message == null) 
    { System.out.println("  assert " + condition); } 
    else 
    { System.out.println("  assert " + condition + " do " + message); } 
  }

  public String getOperator() 
  { return "assert"; } 

  public Object clone() 
  { return new AssertStatement(condition,message); } 

  public Map energyUse(Map uses, Vector rUses, Vector aUses)
  { if (condition != null) 
    { condition.energyUse(uses, rUses, aUses); 

      int res = condition.syntacticComplexity();

      if (res > TestParameters.syntacticComplexityLimit)
      { int acount = (int) uses.get("amber"); 
        uses.set("amber", acount + 1); 
        aUses.add("! Code smell (MEL): too high expression complexity (" + res + ") for " + condition + "\n" +  
                  ">>> Recommend OCL refactoring");  
      } 
    }
 
    if (message != null)
    { message.energyUse(uses, rUses, aUses); } 
    return uses; 
  } 

  public java.util.Map collectionOperatorUses(int lev, 
                                    java.util.Map uses, 
                                    Vector vars)
  { if (condition != null) 
    { condition.collectionOperatorUses(lev, uses, vars); } 
    if (message != null) 
    { message.collectionOperatorUses(lev, uses, vars); } 
    return uses; 
  } 


  public Statement dereference(BasicExpression var) 
  { Expression newcond = condition; 
    if (condition != null) 
    { newcond = condition.dereference(var); }
    Expression newmessage = message; 
    if (message != null) 
    { newmessage = message.dereference(var); }
    return new AssertStatement(newcond,newmessage); 
  }  

  public Statement optimiseOCL() 
  { Expression newcond = condition; 
    if (condition != null) 
    { newcond = condition.simplifyOCL(); }
    Expression newmessage = message; 
    if (message != null) 
    { newmessage = message.simplifyOCL(); }
    return new AssertStatement(newcond,newmessage); 
  }  

  public Statement addContainerReference(BasicExpression ref,
                                         String var,
                                         Vector excl) 
  { Expression newcond = condition; 
    if (condition != null) 
    { newcond = 
         condition.addContainerReference(ref,var,excl);
    }
    Expression newmessage = message; 
    if (message != null) 
    { newmessage = 
         message.addContainerReference(ref,var,excl); 
    }
    return new AssertStatement(newcond,newmessage); 
  }  

  public Statement substituteEq(String oldE, Expression newE)
  { Expression newcond = condition; 
    if (condition != null) 
    { newcond = condition.substituteEq(oldE,newE); }
    Expression newmessage = message; 
    if (message != null) 
    { newmessage = message.substituteEq(oldE,newE); }
    return new AssertStatement(newcond,newmessage); 
  } 

  public Statement removeSlicedParameters(BehaviouralFeature bf, Vector fpars)
  { Expression newcond = condition; 
    if (condition != null) 
    { newcond = condition.removeSlicedParameters(bf,fpars); }
    Expression newmessage = message; 
    if (message != null) 
    { newmessage = message.removeSlicedParameters(bf,fpars); }
    return new AssertStatement(newcond,newmessage); 
  } 

  public String toString()
  { if (message == null) 
    { return "  assert " + condition; } 
    else 
    { return "  assert " + condition + " do " + message; }
  }

  public String toAST()
  { String res = ""; 

    if (message == null)
    { res = "(OclStatement assert " + condition.toAST() + " )"; } 
    else
    { res = "(OclStatement assert " + condition.toAST() + " do " + message.toAST() + " )"; } 

    // if (brackets)
    // { res = "(OclStatement ( " + res + " ) )"; } 

    return res; 
  } 

  public boolean containsSubexpression(Expression expr) 
  { return condition.containsSubexpression(expr); } 

  public Vector singleMutants()
  { if (condition == null) 
    { return new Vector(); } 
    Vector exprs = condition.singleMutants(); 
    Vector res = new Vector(); 
    for (int i = 0; i < exprs.size(); i++) 
    { Expression mvalue = (Expression) exprs.get(i); 
      res.add(new AssertStatement(mvalue,message)); 
    } 
    return res; 
  } 


  public void display(PrintWriter out)
  { if (message == null) 
    { out.println("  assert " + condition); } 
    else 
    { out.println("  assert " + condition + " do " + message); }
  }

  public String bupdateForm()
  { return "SELECT false THEN skip END\n"; }

  public BStatement bupdateForm(java.util.Map env, boolean local)
  { return new BBasicStatement("SELECT false THEN skip END"); }

  public void displayJava(String t)
  { if (message == null) 
    { System.out.println("  assert " + condition); } 
    else 
    { System.out.println("  assert " + condition + " : " + message); } 
  }

  public String saveModelData(PrintWriter out)
  { String res = Identifier.nextIdentifier("assertstatement_"); 
    out.println(res + " : AssertStatement"); 
    out.println(res + ".statId = \"" + res + "\"");  
    
    if (condition == null) 
    { /* Not a valid assert statement */ } 
    else 
    { String expId = condition.saveModelData(out);
      out.println(res + ".condition = " + expId);
    } 
    if (message == null) 
    { } 
    else 
    { String expIdm = message.saveModelData(out);
      out.println(expIdm + " : " + res + ".message");
    } 
    return res; 
  } 

  public String saveModelData(PrintWriter out, Entity ent)
  { return saveModelData(out); } 

  public String toStringJava()
  { java.util.Map env = new java.util.HashMap(); 
    String qf = condition.queryForm(env,true); 
    if (message == null) 
    { return "    assert " + qf + ";\n"; }
    else 
    { String mqf = message.queryForm(env,true); 
      return "    assert " + qf + " : " + mqf + ";\n"; 
    } 
  }
  
  public String toEtl()
  { return ""; }

  public void displayJava(String t, PrintWriter out)
  { out.println(toStringJava()); } 

  public boolean typeCheck(Vector types, Vector entities, Vector cs, Vector env)
  { condition.typeCheck(types,entities,cs,env); 
    if (message != null) 
    { message.typeCheck(types,entities,cs,env); } 
    return true;
  } 

  public boolean typeInference(Vector types, Vector entities, Vector cs, Vector env, java.util.Map vartypes)
  { condition.typeInference(types,entities,cs,env,vartypes); 
    if (message != null) 
    { message.typeInference(types,entities,cs,env,vartypes); } 
    return true;
  } 
  
  public Expression wpc(Expression post)
  { return post; }

  public Expression wpc(Expression inv, Expression post)
  { return inv; }

  public Vector dataDependents(Vector allvars, Vector vars)
  { return vars; }  

  public Vector dataDependents(Vector allvars, Vector vars, Map mp, Map dlin)
  { return vars; }  

  public boolean updates(Vector v) 
  { return false; } 

  public String updateForm(java.util.Map env, boolean local, Vector types, Vector entities, 
                           Vector vars)
  { String qf = condition.queryForm(env,local); 
    if (message == null) 
    { return "    assert " + qf + ";\n"; }
    else 
    { String mqf = message.queryForm(env,local); 
      return "    assert " + qf + " : " + mqf + ";\n"; 
    }
  }

  public String updateFormJava6(java.util.Map env, boolean local)
  { String qf = condition.queryFormJava6(env,local); 
    if (message == null) 
    { return "    assert " + qf + ";\n"; }
    else 
    { String mqf = message.queryFormJava6(env,local); 
      return "    assert " + qf + " : " + mqf + ";\n"; 
    }
  }

  public String updateFormJava7(java.util.Map env, boolean local)
  { String qf = condition.queryFormJava7(env,local); 
    if (message == null) 
    { return "    assert " + qf + ";\n"; }
    else 
    { String mqf = message.queryFormJava7(env,local); 
      return "    assert " + qf + " : " + mqf + ";\n"; 
    }
  }

  public String updateFormCSharp(java.util.Map env, boolean local)
  { String qf = condition.queryFormCSharp(env,local); 
    if (message == null) 
    { return "    Debug.Assert(" + qf + ");\n"; }
    else 
    { String mqf = message.queryFormCSharp(env,local); 
      return "    Debug.Assert(" + qf + ", " + mqf + ");\n"; 
    }
  }

  public String updateFormCPP(java.util.Map env, boolean local)
  { String qf = condition.queryFormCPP(env,local); 
    return "    assert(" + qf + ");";
  }

  public Vector readFrame()
  { Vector res = new Vector();
    res = condition.readFrame(); 
    
    if (message != null) 
    { res = VectorUtil.union(res, message.readFrame()); }  
    return res; 
  } 

  public Vector writeFrame()
  { Vector res = new Vector(); 
    res = condition.writeFrame(); 
    
    if (message != null) 
    { res = VectorUtil.union(res, message.writeFrame()); }
  
    return res; 
  } 

  public Statement checkConversions(Entity e, Type propType, Type propElemType, java.util.Map interp)
  { return this; } 

  public Statement replaceModuleReferences(UseCase uc)
  { return this; } 

  public int syntacticComplexity()
  { int res = condition.syntacticComplexity();
 
    res++; 

    if (message != null) 
    { return res + message.syntacticComplexity(); } 

    return res;
  } 

  public int cyclomaticComplexity()
  { return 1; } 

  public int epl()
  { return 0; } 

  public Vector allOperationsUsedIn()
  { Vector res = new Vector(); 
    res = condition.allOperationsUsedIn(); 
    
    if (message != null) 
    { res = VectorUtil.union(res, message.allOperationsUsedIn()); } 
    return res; 
  } 

  public Vector allAttributesUsedIn()
  { Vector res = new Vector(); 
    res = condition.allAttributesUsedIn(); 
    
    if (message != null) 
    { res = VectorUtil.union(res, message.allAttributesUsedIn()); } 
    return res; 
  } 

  public Vector getUses(String var)
  { Vector res = new Vector(); 
    res = condition.getUses(var); 
    
    if (message != null) 
    { res = VectorUtil.union(res, message.getUses(var)); } 
    return res; 
  } 

  public Vector getVariableUses()
  { Vector res = new Vector(); 
    res = condition.getVariableUses(); 
    
    if (message != null) 
    { res = VectorUtil.union(res, message.getVariableUses()); } 
    return res; 
  } 

  public Vector equivalentsUsedIn()
  { Vector res = new Vector(); 
    res = condition.equivalentsUsedIn(); 
    
    if (message != null) 
    { res = VectorUtil.union(res, message.equivalentsUsedIn()); } 
    return res; 
  } 

  public Vector metavariables()
  { Vector res = new Vector(); 
    res = condition.metavariables(); 
    
    if (message != null) 
    { res = VectorUtil.union(res, message.metavariables()); } 
    return res; 
  } 

  public Vector cgparameters()
  { Vector args = new Vector();
    if (condition != null) 
    { args.add(condition); } 
    if (message != null) 
    { args.add(message); }
    return args; 
  } 

  public Vector cgterms()
  { Vector args = new Vector();
    args.add("assert"); 
    if (condition != null) 
    { args.add(condition); } 
    if (message != null) 
    { args.add("do"); 
      args.add(message); 
    }
    return args; 
  } 


  public String cg(CGSpec cgs)
  { String etext = this + "";
    Vector args = new Vector();
    if (condition != null) 
    { args.add(condition.cg(cgs)); } 
    if (message != null) 
    { args.add(message.cg(cgs)); } 
    CGRule r = cgs.matchedStatementRule(this,etext);

    System.out.println(">> Matched statement rule: " + r + " for " + this); 

    if (r != null)
    { return r.applyRule(args); }
    return etext;
  }

  public void findClones(java.util.Map clones, String op, String rule)
  { if (condition != null)
    { condition.findClones(clones,op,rule); } 
    if (message != null)
    { message.findClones(clones,op,rule); }
  } 

  public void findClones(java.util.Map clones, 
                         java.util.Map cdefs, 
                         String op, String rule)
  { if (condition != null)
    { condition.findClones(clones,cdefs,op,rule); } 
    if (message != null)
    { message.findClones(clones,cdefs,op,rule); }
  } 
}

class CatchStatement extends Statement
{ // This represents a catch clause

  Expression caughtObject = null; 
  Statement action = null; 
  // Expect both to be non-null

  public CatchStatement(Expression expr, Statement stat) 
  { caughtObject = expr; 
    action = stat; 
  } 

  public CatchStatement(Expression var, Vector stats) 
  { Type t = new Type("OclAny", null);
    if (var.getType() != null) 
    { t = var.getType(); } 
 
    caughtObject = 
      new BinaryExpression(":", var, new BasicExpression(t)); 

    if (stats.size() == 0) 
    { action = new InvocationStatement("skip"); } 
    else if (stats.size() == 1)
    { action = (Statement) stats.get(0); } 
    else 
    { action = new SequenceStatement(stats); }  
  } 

  public void display()
  { System.out.println("  catch ( " + caughtObject + ") do " + action); 
  }

  public String toString()
  { return "  catch ( " + caughtObject + ") do " + action; }

  public String getOperator() 
  { return "catch"; } 

  public String toAST()
  { String res = "(OclStatement catch " + caughtObject.toAST() + " )"; 

    // if (brackets)
    // { res = "(OclStatement ( " + res + " ) )"; } 

    return res; 
  } 

  public boolean containsSubexpression(Expression expr) 
  { if (caughtObject.containsSubexpression(expr))
    { return true; }
    return action.containsSubexpression(expr);
  } 

  public Vector singleMutants()
  { return new Vector(); }

  public Object clone() 
  { return new CatchStatement(caughtObject,action); } 

  public Statement dereference(BasicExpression var) 
  { return 
      new CatchStatement(caughtObject.dereference(var), 
                         action.dereference(var)); 
  }

  public Statement optimiseOCL() 
  { Expression cobj = null; 
    if (caughtObject != null) 
    { cobj = caughtObject.simplifyOCL(); }

    Statement cact = null;  
    if (action != null) 
    { cact = action.optimiseOCL(); } 

    return new CatchStatement(cobj,cact); 
  }

  public void findClones(java.util.Map clones, String rule, String op)
  { if (action != null)
    { action.findClones(clones,rule,op); }
  } 

  public void findClones(java.util.Map clones, 
                         java.util.Map cdefs,
                         String rule, String op)
  { if (action != null)
    { action.findClones(clones,cdefs,rule,op); }
  } 

  public void findMagicNumbers(java.util.Map mgns, String rule, String op)
  { if (action != null)
    { action.findMagicNumbers(mgns, this + "", op); }
  } 

  public Map energyUse(Map uses, Vector rUses, Vector aUses)
  { if (action != null) 
    { action.energyUse(uses, rUses, aUses); } 
    return uses; 
  } 

  public java.util.Map collectionOperatorUses(int lev, 
                                    java.util.Map uses, 
                                    Vector vars)
  { if (action != null) 
    { action.collectionOperatorUses(lev, uses, vars); } 
    return uses; 
  } 

  public Statement addContainerReference(BasicExpression ref,
                                         String var,
                                         Vector excls) 
  { Vector newexcls = new Vector();
    newexcls.addAll(excls); 
 
    if (caughtObject instanceof BinaryExpression)
    { BinaryExpression ex = (BinaryExpression) caughtObject; 
      if (":".equals(ex.getOperator()))
      { newexcls.add(ex.getLeft() + ""); } 
    }  

    Statement newact = action.addContainerReference(ref,var,
                                                 newexcls); 

    return new CatchStatement(caughtObject, newact); 
  }

  public Statement substituteEq(String oldE, Expression newE)
  { Expression cobj = caughtObject.substituteEq(oldE,newE); 
    Statement astat = action.substituteEq(oldE,newE); 
    return new CatchStatement(cobj,astat); 
  } 
  
  public Statement removeSlicedParameters(BehaviouralFeature bf, Vector fpars)
  { Expression cobj = 
       caughtObject.removeSlicedParameters(bf,fpars); 
    Statement astat = action.removeSlicedParameters(bf,fpars); 
    return new CatchStatement(cobj,astat); 
  } 

  public void display(PrintWriter out)
  { out.println("  catch (" + caughtObject + ") do " + action); }

  public String bupdateForm()
  { return "SELECT false THEN skip END\n"; }

  public BStatement bupdateForm(java.util.Map env, boolean local)
  { return new BBasicStatement("SELECT false THEN skip END"); }

  public void displayJava(String t)
  { java.util.Map env = new java.util.HashMap(); 
    String qf = caughtObject.declarationQueryForm(env,true); 
    System.out.println("  catch (" + qf + ") { ");
    action.displayJava(t); 
    System.out.println("  }");  
  }

  public String saveModelData(PrintWriter out)
  { String res = Identifier.nextIdentifier("catchstatement_"); 
    out.println(res + " : CatchStatement"); 
    out.println(res + ".statId = \"" + res + "\"");  
    
    if (caughtObject == null) 
    { out.println(res + ".caughtObject = null"); } 
    else 
    { String expId = caughtObject.saveModelData(out);
      out.println(res + ".caughtObject = " + expId);
    } 

    if (action == null) 
    { out.println(res + ".action = null"); } 
    else 
    { String sId = action.saveModelData(out);
      out.println(res + ".action = " + sId);
    } 

    return res; 
  } 

  public String saveModelData(PrintWriter out, Entity ent)
  { String res = Identifier.nextIdentifier("catchstatement_"); 
    out.println(res + " : CatchStatement"); 
    out.println(res + ".statId = \"" + res + "\"");  
    
    if (caughtObject == null) 
    { out.println(res + ".caughtObject = null"); } 
    else 
    { String expId = caughtObject.saveModelData(out);
      out.println(res + ".caughtObject = " + expId);
    } 

    if (action == null) 
    { out.println(res + ".action = null"); } 
    else 
    { String sId = action.saveModelData(out, ent);
      out.println(res + ".action = " + sId);
    } 

    return res; 
  } 

  public String toStringJava()
  { java.util.Map env = new java.util.HashMap(); 
    String qf = caughtObject.declarationQueryForm(env,true); 
    return "    catch (" + qf + ") {\n" + 
           "      " + action.toStringJava() + "\n" + 
           "    }"; 
  }
  
  public String toEtl()
  { return ""; }

  public void displayJava(String t, PrintWriter out)
  { out.println(toStringJava()); } 

  public boolean typeCheck(Vector types, Vector entities, Vector cs, Vector env)
  { Vector localEnv = new Vector(); 
    localEnv.addAll(env); 

    // The caught exception variable is added to the localEnv

    caughtObject.typeCheck(types,entities,cs,localEnv);
    action.typeCheck(types,entities,cs,localEnv);  
    return true;
  } 

  public boolean typeInference(Vector types, Vector entities, Vector cs, Vector env, java.util.Map vartypes)
  { Vector localEnv = new Vector(); 
    localEnv.addAll(env); 

    // The caught exception variable is added to the localEnv

    caughtObject.typeInference(types,entities,
                               cs,localEnv,vartypes);
    action.typeInference(types,entities,cs,localEnv,vartypes);  
    return true;
  } 
  
  public Expression wpc(Expression post)
  { return post; }

  public Expression wpc(Expression inv, Expression post)
  { return inv; }

  public Vector dataDependents(Vector allvars, Vector vars)
  { if (action != null) 
    { return action.dataDependents(allvars,vars); } 
    return vars; 
  }  

  public Vector dataDependents(Vector allvars, Vector vars, Map mp, Map dlin)
  { if (action != null) 
    { return action.dataDependents(allvars,vars,mp,dlin); } 
    return vars; 
  }  

  public boolean updates(Vector v) 
  { return action.updates(v); } 

  public String updateForm(java.util.Map env, boolean local, Vector types, Vector entities, 
                           Vector vars)
  { String qf = caughtObject.declarationQueryForm(env,true); 
    return "    catch (" + qf + ") {\n" + 
           "      " + action.updateForm(env,local,types,entities,vars) + "\n" + 
           "    }"; 
  }

  public String updateFormJava6(java.util.Map env, boolean local)
  { String qf = caughtObject.declarationQueryForm(env,true); 
    return "    catch (" + qf + ") {\n" + 
           "      " + action.updateFormJava6(env,local) + "\n" + 
           "    }";
  }

  public String updateFormJava7(java.util.Map env, boolean local)
  { String qf = caughtObject.declarationQueryForm(env,true); 
    return "    catch (" + qf + ") {\n" + 
           "      " + action.updateFormJava7(env,local) + "\n" + 
           "    }";
  }

  public String updateFormCSharp(java.util.Map env, boolean local)
  { String qf = caughtObject.declarationQueryFormCSharp(env,local); 
    return "    catch (" + qf + ") {\n" + 
           "      " + action.updateFormCSharp(env,local) + "\n" + 
           "    }"; 
  }

  public String updateFormCPP(java.util.Map env, boolean local)
  { String qf = caughtObject.declarationQueryFormCPP(env,local); 
    return "    catch (" + qf + ") {\n" + 
           "      " + action.updateFormCPP(env,local) + "\n" + 
           "    }";
  }

  public Vector readFrame()
  { Vector res = new Vector();
    res.addAll(action.readFrame());  
    return res; 
  } 

  public Vector writeFrame()
  { Vector res = new Vector(); 
    res.addAll(action.writeFrame());  

    return res; 
  } 

  public Statement checkConversions(Entity e, Type propType, Type propElemType, java.util.Map interp)
  { return this; } 

  public Statement replaceModuleReferences(UseCase uc)
  { return this; } 

  public int syntacticComplexity()
  { return 1 + action.syntacticComplexity(); } 
  
  public int cyclomaticComplexity()
  { return 1 + action.cyclomaticComplexity(); } 

  public int epl()
  { return 0; } 

  public Vector allOperationsUsedIn()
  { Vector res = new Vector(); 
    res = action.allOperationsUsedIn();  
    return res; 
  } 

  public Vector allAttributesUsedIn()
  { Vector res = new Vector(); 
    res = action.allAttributesUsedIn();  
    return res; 
  } 

  public Vector getUses(String var)
  { Vector res = new Vector(); 
    res = action.getUses(var);  
    return res; 
  } 

  public Vector getVariableUses()
  { Vector res = new Vector(); 
    res = action.getVariableUses();  
    return res; 
  } 

  public Vector equivalentsUsedIn()
  { Vector res = new Vector(); 
    res = action.equivalentsUsedIn();  
    return res; 
  } 

  public Vector metavariables()
  { Vector res = new Vector(); 
    res = caughtObject.metavariables(); 
    res.addAll(action.metavariables());  
    return res; 
  } 

  public Vector cgparameters()
  { Vector args = new Vector();
    args.add(caughtObject);  
    args.add(action); 
    return args; 
  } 

  public Vector cgterms()
  { Vector args = new Vector();
    args.add("catch"); 

    if (caughtObject != null) 
    { args.add("("); 
      args.add(caughtObject);
      args.add(")"); 
    } 

    if (action != null) 
    { args.add("do"); 
      args.add(action); 
    }
    return args; 
  } 

  public String cg(CGSpec cgs)
  { String etext = this + "";
    Vector args = new Vector();
    Vector eargs = new Vector();
    args.add(caughtObject.cg(cgs));  
    args.add(action.cg(cgs));  
    eargs.add(caughtObject);  
    eargs.add(action);  
    CGRule r = cgs.matchedStatementRule(this,etext);
   
    System.out.println(">> Matched statement rule: " + r + " for " + this); 

    if (r != null)
    { return r.applyRule(args,eargs,cgs); }
    
    return etext;
  }

}

class TryStatement extends Statement
{ // This represents a try,catch,finally statement
  Statement body = null; 
  Vector catchClauses = new Vector(); // CatchStatement 
  Statement endStatement = null; 

  public TryStatement(Statement stat) 
  { body = stat; } 

  public TryStatement(Statement stat, Vector cclauses, Statement es) 
  { body = stat; 
    catchClauses = cclauses; 
    endStatement = es; 
  } 

  public TryStatement(Vector stats, Vector cclauses, Vector ends)
  { if (stats.size() == 0) 
    { body = new InvocationStatement("skip"); } 
    else if (stats.size() == 1)
    { body = (Statement) stats.get(0); } 
    else 
    { body = new SequenceStatement(stats); }  
    catchClauses = cclauses; 

    if (ends.size() == 0)
    { endStatement = null; } 
    else if (ends.size() == 1)
    { endStatement = (Statement) ends.get(0); } 
    else 
    { endStatement = new SequenceStatement(ends); }  
 
  }

  public TryStatement(Vector stats, Vector ends)
  { if (stats.size() == 0) 
    { body = new InvocationStatement("skip"); } 
    else if (stats.size() == 1)
    { body = (Statement) stats.get(0); } 
    else 
    { body = new SequenceStatement(stats); }  

    if (ends.size() == 0)
    { endStatement = null; } 
    else if (ends.size() == 1)
    { Statement stat = (Statement) ends.get(0); 
      if (stat instanceof FinalStatement)
      { endStatement = stat; }
      else 
      { catchClauses = ends; 
        endStatement = null; 
      } 
    }  
    else 
    { catchClauses = ends; 
      endStatement = null; 
    }  
  }

  public void addBody(Statement stat)
  { if (stat == null) 
    { return; } 

    if (body == null) 
    { body = stat; } 
    else if (body instanceof SequenceStatement)
    { ((SequenceStatement) body).addStatement(stat); } 
    else 
    { SequenceStatement ss = new SequenceStatement(); 
      ss.addStatement(body);  
      ss.addStatement(stat); 
      body = ss; 
    } 
  } 

  public void setClauses(Vector stats)
  { catchClauses = stats; } 

  public void addClause(Statement stat)
  { if (stat instanceof CatchStatement) 
    { catchClauses.add(stat); } 
    else if (stat instanceof FinalStatement)
    { endStatement = stat; } 
    else 
    { System.err.println("!! Warning: can only have catch and finally statements in a try statement: " + stat); } 
  } 

  public void setEndStatement(Statement stat)
  { endStatement = stat; } 

  public void display()
  { if (body == null) 
    { return; } 

    System.out.println("  try "); 
    body.display(); 
 
    for (int i = 0; i < catchClauses.size(); i++) 
    { Statement cs = (Statement) catchClauses.get(i); 
      cs.display(); 
    }

    if (endStatement != null) 
    { endStatement.display(); }  
  }

  public String toString()
  { String res = ""; 
    if (body == null) 
    { return res; } 

    res = "    try\n" + 
          "  " + body;
  
    for (int i = 0; i < catchClauses.size(); i++) 
    { Statement cs = (Statement) catchClauses.get(i); 
      res = res + cs; 
    }

    if (endStatement != null) 
    { res = res + "  " + endStatement + "\n"; }
  
    return res; 
  }

  public String toAST()
  { String res = "(OclStatement try ";
    if (body != null) 
    { res = res + body.toAST() + " "; } 

    for (int i = 0; i < catchClauses.size(); i++) 
    { Statement cs = (Statement) catchClauses.get(i); 
      res = res + cs.toAST() + " "; 
    }

    if (endStatement != null) 
    { res = res + endStatement.toAST() + " "; }
  
    res = res + ")";

    // if (brackets)
    // { res = "(OclStatement ( " + res + " ) )"; } 

    return res; 
  }

  public void findClones(java.util.Map clones, String rule, String op)
  { if (body != null) 
    { body.findClones(clones,rule,op); } 
    for (int i = 0; i < catchClauses.size(); i++) 
    { Statement stat = (Statement) catchClauses.get(i); 
      stat.findClones(clones,rule,op); 
    }
    if (endStatement != null) 
    { endStatement.findClones(clones,rule,op); } 
  } 

  public void findClones(java.util.Map clones, 
                         java.util.Map cdefs,
                         String rule, String op)
  { if (body != null) 
    { body.findClones(clones,cdefs,rule,op); } 
    for (int i = 0; i < catchClauses.size(); i++) 
    { Statement stat = (Statement) catchClauses.get(i); 
      stat.findClones(clones,cdefs,rule,op); 
    }
    if (endStatement != null) 
    { endStatement.findClones(clones,cdefs,rule,op); } 
  } 

  public Map energyUse(Map uses, Vector rUses, Vector aUses)
  { if (body != null) 
    { body.energyUse(uses, rUses, aUses); } 
    
    for (int i = 0; i < catchClauses.size(); i++) 
    { Statement stat = (Statement) catchClauses.get(i); 
      stat.energyUse(uses, rUses, aUses); 
    }

    if (endStatement != null)
    { endStatement.energyUse(uses, rUses, aUses); } 

    return uses; 
  } 

  public java.util.Map collectionOperatorUses(int lev, 
                                    java.util.Map uses, 
                                    Vector vars)
  { if (body != null) 
    { body.collectionOperatorUses(lev, uses, vars); } 
    
    for (int i = 0; i < catchClauses.size(); i++) 
    { Statement stat = (Statement) catchClauses.get(i); 
      stat.collectionOperatorUses(lev, uses, vars); 
    }

    if (endStatement != null)
    { endStatement.collectionOperatorUses(lev, uses, vars); } 

    return uses; 
  } 


  public void findMagicNumbers(java.util.Map mgns, String rule, String op)
  { if (body != null) 
    { body.findMagicNumbers(mgns,rule,op); } 

    for (int i = 0; i < catchClauses.size(); i++) 
    { Statement stat = (Statement) catchClauses.get(i); 
      stat.findMagicNumbers(mgns,rule,op); 
    }

    if (endStatement != null) 
    { endStatement.findMagicNumbers(mgns,rule,op); } 
  } 

  public boolean containsSubexpression(Expression expr) 
  { if (body != null) 
    { if (body.containsSubexpression(expr))
      { return true; }
    } 
 
    for (int i = 0; i < catchClauses.size(); i++) 
    { Statement stat = (Statement) catchClauses.get(i); 
      if (stat.containsSubexpression(expr))
      { return true; }
    } 
 
    if (endStatement != null) 
    { return endStatement.containsSubexpression(expr); }

    return false;  
  } 

  public Vector singleMutants()
  { if (body == null) 
    { return new Vector(); } 
    
    Vector stats = body.singleMutants(); 
    Vector res = new Vector(); 
    for (int i = 0; i < stats.size(); i++) 
    { Statement mvalue = (Statement) stats.get(i); 
      res.add(new TryStatement(mvalue, catchClauses, endStatement)); 
    } 

    return res; 
  } 

  public String getOperator() 
  { return "try"; } 

  public Statement getBody() 
  { return body; } 

  public Vector getClauses() 
  { return catchClauses; }

  public Statement getEndStatement()
  { return endStatement; }  

  public Object clone() 
  { Statement s1 = null; 
    if (body != null) 
    { s1 = (Statement) body.clone(); }  
    TryStatement res = new TryStatement(s1);
    Vector catchClones = new Vector(); 
    for (int i = 0; i < catchClauses.size(); i++) 
    { Statement cc = (Statement) catchClauses.get(i); 
      Statement ccClone = (Statement) cc.clone(); 
      catchClones.add(ccClone); 
    } 
    res.setClauses(catchClones); 
    if (endStatement != null) 
    { res.setEndStatement((Statement) endStatement.clone()); } 
    return res; 
  } 

  public Statement optimiseOCL() 
  { Statement s1 = null; 
    if (body != null) 
    { s1 = body.optimiseOCL(); }
  
    TryStatement res = new TryStatement(s1);

    Vector catchClones = new Vector(); 
    for (int i = 0; i < catchClauses.size(); i++) 
    { Statement cc = (Statement) catchClauses.get(i); 
      Statement ccClone = (Statement) cc.optimiseOCL(); 
      catchClones.add(ccClone); 
    } 
    res.setClauses(catchClones); 

    if (endStatement != null) 
    { res.setEndStatement(endStatement.optimiseOCL()); } 
    return res; 
  } 

  public Statement dereference(BasicExpression var) 
  { Statement s1 = null; 
    if (body != null) 
    { s1 = body.dereference(var); }  
    TryStatement res = new TryStatement(s1);
    Vector catchClones = new Vector(); 
    for (int i = 0; i < catchClauses.size(); i++) 
    { Statement cc = (Statement) catchClauses.get(i); 
      Statement ccClone = cc.dereference(var); 
      catchClones.add(ccClone); 
    } 
    res.setClauses(catchClones); 
    if (endStatement != null) 
    { res.setEndStatement(endStatement.dereference(var)); } 
    return res;
  } 

  public Statement addContainerReference(BasicExpression ref,
                                         String var,
                                         Vector excl) 
  { Statement s1 = null; 

    if (body != null) 
    { s1 = body.addContainerReference(ref,var,excl); }  

    TryStatement res = new TryStatement(s1);
    Vector catchClones = new Vector(); 

    for (int i = 0; i < catchClauses.size(); i++) 
    { Statement cc = (Statement) catchClauses.get(i); 
      Statement ccClone =
        cc.addContainerReference(ref,var,excl); 
      catchClones.add(ccClone); 
    }
 
    res.setClauses(catchClones); 

    if (endStatement != null) 
    { res.setEndStatement(
         endStatement.addContainerReference(ref,var,excl)); 
    } 
    return res;
  } 

  public Statement substituteEq(String oldE, Expression newE)
  { Statement s1 = null; 
    if (body != null) 
    { s1 = body.substituteEq(oldE,newE); }  
    TryStatement res = new TryStatement(s1);
    Vector catchClones = new Vector(); 
    for (int i = 0; i < catchClauses.size(); i++) 
    { Statement cc = (Statement) catchClauses.get(i); 
      Statement ccClone = cc.substituteEq(oldE,newE); 
      catchClones.add(ccClone); 
    } 
    res.setClauses(catchClones); 
    if (endStatement != null) 
    { res.setEndStatement(
             endStatement.substituteEq(oldE,newE)); 
    } 
    return res; 
  } 

  public Statement removeSlicedParameters(BehaviouralFeature bf, Vector fpars)
  { Statement s1 = null; 
    if (body != null) 
    { s1 = body.removeSlicedParameters(bf,fpars); }  
    TryStatement res = new TryStatement(s1);
    Vector catchClones = new Vector(); 
    for (int i = 0; i < catchClauses.size(); i++) 
    { Statement cc = (Statement) catchClauses.get(i); 
      Statement ccClone = cc.removeSlicedParameters(bf,fpars); 
      catchClones.add(ccClone); 
    } 
    res.setClauses(catchClones); 
    if (endStatement != null) 
    { res.setEndStatement(
        endStatement.removeSlicedParameters(bf,fpars)); 
    } 
    return res; 
  } 

  public void display(PrintWriter out)
  { if (body == null) 
    { return; } 

    out.println("  try "); 
    body.display(out); 
 
    for (int i = 0; i < catchClauses.size(); i++) 
    { Statement cs = (Statement) catchClauses.get(i); 
      cs.display(out); 
    }

    if (endStatement != null) 
    { endStatement.display(out); }  
  }

  public String bupdateForm()
  { return "SELECT false THEN skip END\n"; }

  public BStatement bupdateForm(java.util.Map env, boolean local)
  { return new BBasicStatement("SELECT false THEN skip END"); }

  public void displayJava(String t)
  { if (body == null) 
    { return; } 

    System.out.println("  try { "); 
    body.displayJava(t); 
    System.out.println(" }");
 
    for (int i = 0; i < catchClauses.size(); i++) 
    { Statement cs = (Statement) catchClauses.get(i); 
      cs.displayJava(t); 
    }

    if (endStatement != null) 
    { endStatement.displayJava(t); }  
  }

  public String saveModelData(PrintWriter out)
  { String res = Identifier.nextIdentifier("trystatement_"); 
    out.println(res + " : TryStatement"); 
    out.println(res + ".statId = \"" + res + "\"");  
    
    if (body != null) 
    { String s1 = body.saveModelData(out); 
      out.println(res + ".body = " + s1); 
    }
  
    for (int i = 0; i < catchClauses.size(); i++) 
    { Statement cc = (Statement) catchClauses.get(i); 
      String ccid = cc.saveModelData(out); 
      out.println(ccid + " : " + res + ".catchClauses");  
    } 

    if (endStatement != null) 
    { String endId = endStatement.saveModelData(out); 
      out.println(endId + " : " + res + ".endStatement");  
    } 

    return res;
  } 

  public String saveModelData(PrintWriter out, Entity ent)
  { String res = Identifier.nextIdentifier("trystatement_"); 
    out.println(res + " : TryStatement"); 
    out.println(res + ".statId = \"" + res + "\"");  
    
    if (body != null) 
    { String s1 = body.saveModelData(out, ent); 
      out.println(res + ".body = " + s1); 
    }
  
    for (int i = 0; i < catchClauses.size(); i++) 
    { Statement cc = (Statement) catchClauses.get(i); 
      String ccid = cc.saveModelData(out, ent); 
      out.println(ccid + " : " + res + ".catchClauses");  
    } 

    if (endStatement != null) 
    { String endId = endStatement.saveModelData(out, ent); 
      out.println(endId + " : " + res + ".endStatement");  
    } 

    return res;
  } 

  public String toStringJava()
  { String res = "  try"; 
    if (body == null) 
    { res = res + " { }\n"; }
    else  
    { res = res + "  { " + body.toStringJava() + " }\n"; }
 
    for (int i = 0; i < catchClauses.size(); i++) 
    { Statement cs = (Statement) catchClauses.get(i); 
      res = res + cs.toStringJava(); 
    }

    if (endStatement != null) 
    { res = res + endStatement.toStringJava(); }  
    return res; 
  }
  /* This syntax is identical for C# also */

  public String toEtl()
  { return ""; }

  public void displayJava(String t, PrintWriter out)
  { out.println("  try"); 
    if (body == null) 
    { out.println(" { }"); }
    else  
    { out.println("  { "); 
      body.displayJava(t,out); 
      out.println("  }"); 
    }
 
    for (int i = 0; i < catchClauses.size(); i++) 
    { Statement cs = (Statement) catchClauses.get(i); 
      cs.displayJava(t,out); 
    }

    if (endStatement != null) 
    { endStatement.displayJava(t,out); }  
 } 

  public boolean typeCheck(Vector types, Vector entities, Vector cs, Vector env)
  { if (body != null) 
    { body.typeCheck(types,entities,cs,env); }  
    for (int i = 0; i < catchClauses.size(); i++) 
    { Statement cc = (Statement) catchClauses.get(i); 
      cc.typeCheck(types,entities,cs,env);
    } 

    if (endStatement != null) 
    { endStatement.typeCheck(types,entities,cs,env); } 
    return true; 
  }  

  public boolean typeInference(Vector types, Vector entities, Vector cs, Vector env, java.util.Map vartypes)
  { if (body != null) 
    { body.typeInference(types,entities,cs,env,vartypes); } 
 
    for (int i = 0; i < catchClauses.size(); i++) 
    { Statement cc = (Statement) catchClauses.get(i); 
      cc.typeInference(types,entities,cs,env,vartypes);
    } 

    if (endStatement != null) 
    { endStatement.typeInference(types,entities,
                                 cs,env,vartypes); 
    }
 
    return true; 
  }  
  
  public Expression wpc(Expression post)
  { return post; }

  public Expression wpc(Expression inv, Expression post)
  { return inv; }

  public Vector dataDependents(Vector allvars, Vector vars)
  { Vector vbls = new Vector(); 
    

    if (endStatement != null) 
    { vbls = endStatement.dataDependents(allvars,vars); }
    else 
    { vbls.addAll(vars); } 

    for (int i = 0; i < catchClauses.size(); i++) 
    { Statement cc = (Statement) catchClauses.get(i); 
      vbls = cc.dataDependents(allvars, vbls);
    } // but they are optional

    if (body != null) 
    { return body.dataDependents(allvars, vbls); }
 
    return vbls; 
  }  

  public Vector dataDependents(Vector allvars, Vector vars, Map mp, Map dlin)
  { Vector vbls = new Vector(); 
    

    if (endStatement != null) 
    { vbls = endStatement.dataDependents(allvars,vars,mp,dlin); }
    else 
    { vbls.addAll(vars); } 

    for (int i = 0; i < catchClauses.size(); i++) 
    { Statement cc = (Statement) catchClauses.get(i); 
      vbls = cc.dataDependents(allvars, vbls, mp, dlin);
    } // but they are optional

    if (body != null) 
    { return body.dataDependents(allvars, vbls, mp, dlin); }
 
    return vbls; 
  }  

  public boolean updates(Vector v) 
  { boolean res = false; 
    if (body != null) 
    { if (body.updates(v))
      { return true; }
    }   

    for (int i = 0; i < catchClauses.size(); i++) 
    { Statement cc = (Statement) catchClauses.get(i); 
      if (cc.updates(v))
      { return true; } 
    } 

    if (endStatement != null) 
    { if (endStatement.updates(v))
      { return true; }
    }  

    return res; 
  } 

  public String updateForm(java.util.Map env, boolean local, Vector types, Vector entities, 
                           Vector vars)
  { String res = "  try"; 
    if (body == null) 
    { res = res + " { }\n"; }
    else  
    { res = res + "  { " + body.updateForm(env,local,entities,types,vars) + " }\n"; }
 
    for (int i = 0; i < catchClauses.size(); i++) 
    { Statement cs = (Statement) catchClauses.get(i); 
      res = res + cs.updateForm(env,local,entities,types,vars); 
    }

    if (endStatement != null) 
    { res = res + endStatement.updateForm(env,local,entities,types,vars); }  
    return res; 
 }

  public String updateFormJava6(java.util.Map env, boolean local)
  { String res = "  try"; 
    if (body == null) 
    { res = res + " { }\n"; }
    else  
    { res = res + "  { " + body.updateFormJava6(env,local) + " }\n"; }
 
    for (int i = 0; i < catchClauses.size(); i++) 
    { Statement cs = (Statement) catchClauses.get(i); 
      res = res + cs.updateFormJava6(env,local); 
    }

    if (endStatement != null) 
    { res = res + endStatement.updateFormJava6(env,local); }  
    return res; 
 }

  public String updateFormJava7(java.util.Map env, boolean local)
  { String res = "  try"; 
    if (body == null) 
    { res = res + " { }\n"; }
    else  
    { res = res + "  { " + body.updateFormJava7(env,local) + " }\n"; }
 
    for (int i = 0; i < catchClauses.size(); i++) 
    { Statement cs = (Statement) catchClauses.get(i); 
      res = res + cs.updateFormJava7(env,local); 
    }

    if (endStatement != null) 
    { res = res + endStatement.updateFormJava7(env,local); }  
    return res; 
 }

  public String updateFormCSharp(java.util.Map env, boolean local)
  { String res = "  try"; 
    if (body == null) 
    { res = res + " { }\n"; }
    else  
    { res = res + "  { " + body.updateFormCSharp(env,local) + " }\n"; }
 
    for (int i = 0; i < catchClauses.size(); i++) 
    { Statement cs = (Statement) catchClauses.get(i); 
      res = res + cs.updateFormCSharp(env,local) + "\n"; 
    }

    if (endStatement != null) 
    { res = res + endStatement.updateFormCSharp(env,local); }  
    return res; 
 }

  public String updateFormCPP(java.util.Map env, boolean local)
  { String res = "  try"; 
    if (body == null) 
    { res = res + " { }\n"; }
    else  
    { res = res + "  { " + body.updateFormCPP(env,local) + " }\n"; }
 
    for (int i = 0; i < catchClauses.size(); i++) 
    { Statement cs = (Statement) catchClauses.get(i); 
      res = res + cs.updateFormCPP(env,local) + "\n"; 
    }


    if (endStatement != null) 
    { res = res + endStatement.updateFormCPP(env,local); }  
    return res; 
  }

  public Vector readFrame()
  { Vector res = new Vector(); 
    if (body != null) 
    { res = VectorUtil.union(res,body.readFrame()); }   

    for (int i = 0; i < catchClauses.size(); i++) 
    { Statement cc = (Statement) catchClauses.get(i); 
      Vector vv = cc.readFrame(); 
      res = VectorUtil.union(res,vv); 
    } 

    if (endStatement != null) 
    { Vector endrd = endStatement.readFrame(); 
      res = VectorUtil.union(res,endrd); 
    }  

    return res; 
  } 

  public Vector writeFrame()
  { Vector res = new Vector(); 
    if (body != null) 
    { res = VectorUtil.union(res,body.writeFrame()); }   

    for (int i = 0; i < catchClauses.size(); i++) 
    { Statement cc = (Statement) catchClauses.get(i); 
      Vector vv = cc.writeFrame(); 
      res = VectorUtil.union(res,vv); 
    } 

    if (endStatement != null) 
    { Vector endrd = endStatement.writeFrame(); 
      res = VectorUtil.union(res,endrd); 
    }  

    return res; 
  } 

  public Statement checkConversions(Entity e, Type propType, Type propElemType, java.util.Map interp)
  { return this; } 

  public Statement replaceModuleReferences(UseCase uc)
  { return this; } 

  public int syntacticComplexity()
  { int res = 1; 
    if (body != null) 
    { res = res + body.syntacticComplexity(); }   

    for (int i = 0; i < catchClauses.size(); i++) 
    { Statement cc = (Statement) catchClauses.get(i); 
      int vv = cc.syntacticComplexity(); 
      res = res + vv; 
    } 

    if (endStatement != null) 
    { int endsc = endStatement.syntacticComplexity(); 
      res = res + endsc; 
    }  
    return res; 
  } 

  public int cyclomaticComplexity()
  { int res = 1; 
    if (body != null) 
    { res = res + body.cyclomaticComplexity(); }   

    for (int i = 0; i < catchClauses.size(); i++) 
    { Statement cc = (Statement) catchClauses.get(i); 
      int vv = cc.cyclomaticComplexity(); 
      res = res + vv; 
    } 

    if (endStatement != null) 
    { int endsc = endStatement.cyclomaticComplexity(); 
      res = res + endsc; 
    }  
    return res; 
  }

  public int epl()
  { return 0; } 

  public Vector allOperationsUsedIn()
  { Vector res = new Vector(); 
    if (body != null) 
    { res = VectorUtil.union(res,body.allOperationsUsedIn()); }   

    for (int i = 0; i < catchClauses.size(); i++) 
    { Statement cc = (Statement) catchClauses.get(i); 
      Vector vv = cc.allOperationsUsedIn(); 
      res = VectorUtil.union(res,vv); 
    } 

    if (endStatement != null) 
    { Vector endrd = endStatement.allOperationsUsedIn(); 
      res = VectorUtil.union(res,endrd); 
    }  
    return res; 
  } 

  public Vector allAttributesUsedIn()
  { Vector res = new Vector(); 
    if (body != null) 
    { res = VectorUtil.union(res,body.allAttributesUsedIn()); }   

    for (int i = 0; i < catchClauses.size(); i++) 
    { Statement cc = (Statement) catchClauses.get(i); 
      Vector vv = cc.allAttributesUsedIn(); 
      res = VectorUtil.union(res,vv); 
    } 

    if (endStatement != null) 
    { Vector endrd = endStatement.allAttributesUsedIn(); 
      res = VectorUtil.union(res,endrd); 
    }  
    return res; 
  } 

  public Vector getUses(String var)
  { Vector res = new Vector(); 
    if (body != null) 
    { res = VectorUtil.union(res,body.getUses(var)); }   

    for (int i = 0; i < catchClauses.size(); i++) 
    { Statement cc = (Statement) catchClauses.get(i); 
      Vector vv = cc.getUses(var); 
      res = VectorUtil.union(res,vv); 
    } 

    if (endStatement != null) 
    { Vector endrd = endStatement.getUses(var); 
      res = VectorUtil.union(res,endrd); 
    }  
    return res; 
  } 

  public Vector getVariableUses()
  { Vector res = new Vector(); 
    if (body != null) 
    { res = VectorUtil.union(res,body.getVariableUses()); }   

    for (int i = 0; i < catchClauses.size(); i++) 
    { Statement cc = (Statement) catchClauses.get(i); 
      Vector vv = cc.getVariableUses(); 
      res = VectorUtil.union(res,vv); 
    } 

    if (endStatement != null) 
    { Vector endrd = endStatement.getVariableUses(); 
      res = VectorUtil.union(res,endrd); 
    }  
    return res; 
  } 

  public Vector getVariableUses(Vector unused)
  { Vector res = new Vector(); 
    if (body != null) 
    { Vector bodyuses = body.getVariableUses(unused); 
      res = VectorUtil.union(res,bodyuses); 
    }   

    for (int i = 0; i < catchClauses.size(); i++) 
    { Statement cc = (Statement) catchClauses.get(i); 
      Vector vv = cc.getVariableUses(); 
      res = VectorUtil.union(res,vv); 
    } 

    if (endStatement != null) 
    { Vector endrd = endStatement.getVariableUses(); 
      res = VectorUtil.union(res,endrd); 
    }  
    return res; 
  } 

  public Vector equivalentsUsedIn()
  { Vector res = new Vector(); 
    if (body != null) 
    { res = VectorUtil.union(res,body.equivalentsUsedIn()); }   

    for (int i = 0; i < catchClauses.size(); i++) 
    { Statement cc = (Statement) catchClauses.get(i); 
      Vector vv = cc.equivalentsUsedIn(); 
      res = VectorUtil.union(res,vv); 
    } 

    if (endStatement != null) 
    { Vector endrd = endStatement.equivalentsUsedIn(); 
      res = VectorUtil.union(res,endrd); 
    }  
    return res; 
  } 

  public Vector metavariables()
  { Vector res = new Vector(); 
    if (body != null) 
    { res = VectorUtil.union(res,body.metavariables()); }   

    for (int i = 0; i < catchClauses.size(); i++) 
    { Statement cc = (Statement) catchClauses.get(i); 
      Vector vv = cc.metavariables(); 
      res = VectorUtil.union(res,vv); 
    } 

    if (endStatement != null) 
    { Vector endrd = endStatement.metavariables(); 
      res = VectorUtil.union(res,endrd); 
    }  
    return res; 
  } 

  public Vector cgparameters()
  { Vector args = new Vector();
    if (body != null) 
    { args.add(body); } 
    if (catchClauses != null) 
    { args.add(catchClauses); }
    if (endStatement != null) 
    { args.add(endStatement); }
    return args; 
  } 

  public String cg(CGSpec cgs)
  { String etext = this + "";
    Vector args = new Vector();
    Vector eargs = new Vector();

    if (body != null) 
    { args.add(body.cg(cgs)); 
      eargs.add(body); 
    } 

    if (catchClauses != null) 
    { String ccres = ""; 
      for (int i = 0; i < catchClauses.size(); i++) 
      { Statement cc = (Statement) catchClauses.get(i); 
        ccres = ccres + cc.cg(cgs); 
      }   
      args.add(ccres); 
      eargs.add(catchClauses); 
    }

    if (endStatement != null) 
    { args.add(endStatement.cg(cgs)); 
      eargs.add(endStatement); 
    } 
 
    CGRule r = cgs.matchedStatementRule(this,etext);

    System.out.println(">> Matched statement rule: " + r + " for " + this); 

    if (r != null)
    { return r.applyRule(args,eargs,cgs); }

    return etext;
  }

}

/* Deprecated class, please use ConditionalStatement */ 
class IfStatement extends Statement
{ Vector cases = new Vector();    /* of IfCase */

  public String getOperator() 
  { return "if"; } 

  public IfStatement() { } 

  public IfStatement(Expression test, Statement ifpart, Statement elsepart)
  { IfCase ic1 = new IfCase(test,ifpart); 
    cases.add(ic1); 
    if ("skip".equals(elsepart + "")) { } 
    else 
    { IfCase ic2 = new IfCase(new BasicExpression(true),elsepart); 
      cases.add(ic2);
    }  
  } 

  public IfStatement(Expression test, Statement ifpart)
  { IfCase ic1 = new IfCase(test,ifpart); 
    cases.add(ic1); 
  } 

  public IfStatement(Statement ifpart, Statement elsepart)
  { if (ifpart instanceof IfStatement)
    { cases.addAll(((IfStatement) ifpart).cases); } 
    else 
    { cases.add(new IfCase(new BasicExpression(true), ifpart)); }  
    cases.add(new IfCase(new BasicExpression(true), elsepart)); 
  } 

  public Object clone() 
  { Vector newcases = new Vector(); 
    for (int i = 0; i < cases.size(); i++) 
    { IfCase cse = (IfCase) cases.get(i); 
      IfCase newcse = (IfCase) cse.clone(); 
      newcases.add(newcse); 
    } 
    IfStatement res = new IfStatement(); 
    res.cases = newcases; 
    res.setEntity(entity); 
    return res; 
  }  // clone the conditions

  public Statement optimiseOCL() 
  { Vector newcases = new Vector(); 
    for (int i = 0; i < cases.size(); i++) 
    { IfCase cse = (IfCase) cases.get(i); 
      IfCase newcse = (IfCase) cse.optimiseOCL(); 
      newcases.add(newcse); 
    } 
    IfStatement res = new IfStatement(); 
    res.cases = newcases; 
    res.setEntity(entity); 
    return res; 
  }  // clone the conditions

  public Statement generateDesign(java.util.Map env, boolean local)
  { Vector newcases = new Vector(); 
    for (int i = 0; i < cases.size(); i++) 
    { IfCase cse = (IfCase) cases.get(i); 
      IfCase newcse = (IfCase) cse.generateDesign(env,local); 
      newcases.add(newcse); 
    } 
    IfStatement res = new IfStatement(); 
    res.cases = newcases; 
    res.setEntity(entity); 
    return res; 
  }  // clone the conditions

  public Expression getTest()
  { if (cases.size() > 0)
    { IfCase case1 = (IfCase) cases.get(0); 
      return case1.getTest(); 
    } 
    return new BasicExpression(true); 
  } 

  public Statement getIfPart()
  { if (cases.size() > 0)
    { IfCase case1 = (IfCase) cases.get(0); 
      return case1.getIf(); 
    } 
    return null; 
  } 

  public Statement getElsePart()
  { if (cases.size() > 1)
    { IfCase case1 = (IfCase) cases.get(1); 
      return case1.getIf(); 
    } 
    return null; 
  } 

  public void setElse(Statement s)
  { if (cases.size() > 1)
    { IfCase case1 = (IfCase) cases.get(1); 
      case1.setIf(s); 
    } 
  } 
  
  public void findClones(java.util.Map clones, String rule, String op)
  { for (int i = 0; i < cases.size(); i++) 
    { IfCase cse = (IfCase) cases.get(i); 
      cse.findClones(clones,rule,op); 
    } 
  }

  public void findClones(java.util.Map clones, 
                         java.util.Map cdefs, 
                         String rule, String op)
  { for (int i = 0; i < cases.size(); i++) 
    { IfCase cse = (IfCase) cases.get(i); 
      cse.findClones(clones,cdefs,rule,op); 
    } 
  }

  public Map energyUse(Map uses, Vector ruses, Vector ouses)
  { for (int i = 0; i < cases.size(); i++) 
    { IfCase cse = (IfCase) cases.get(i); 
      cse.energyUse(uses, ruses, ouses); 
    } 

    return uses; 
  }

  public java.util.Map collectionOperatorUses(int lev, 
                          java.util.Map uses, Vector vars)
  { for (int i = 0; i < cases.size(); i++) 
    { IfCase cse = (IfCase) cases.get(i); 
      cse.collectionOperatorUses(lev, uses, vars); 
    } 

    return uses; 
  }


  public void findMagicNumbers(java.util.Map mgns, String rule, String op)
  { for (int i = 0; i < cases.size(); i++) 
    { IfCase cse = (IfCase) cases.get(i); 
      cse.findMagicNumbers(mgns,rule,op); 
    } 
  }


  public Statement dereference(BasicExpression var) 
  { Vector newcases = new Vector(); 
    for (int i = 0; i < cases.size(); i++) 
    { IfCase cse = (IfCase) cases.get(i); 
      IfCase newcse = (IfCase) cse.dereference(var); 
      newcases.add(newcse); 
    } 
    IfStatement res = new IfStatement(); 
    res.cases = newcases; 
    res.setEntity(entity); 
    return res; 
  }  // clone the conditions


  public Statement addContainerReference(BasicExpression ref,
                                         String var,
                                         Vector excl) 
  { Vector newcases = new Vector(); 
    for (int i = 0; i < cases.size(); i++) 
    { IfCase cse = (IfCase) cases.get(i); 
      IfCase newcse = 
           (IfCase) cse.addContainerReference(     
                                  ref,var,excl); 
      newcases.add(newcse); 
    } 
    IfStatement res = new IfStatement(); 
    res.cases = newcases; 
    res.setEntity(entity); 
    return res; 
  }  // clone the conditions

  public void setEntity(Entity e)
  { entity = e; 
    for (int i = 0; i < cases.size(); i++)
    { IfCase ic = (IfCase) cases.get(i); 
      ic.setEntity(e); 
    }
  }

  public boolean isEmpty() 
  { return cases.size() == 0; } 

  public void addCase(Expression test, Statement action) 
  { IfCase ic = new IfCase(test,action); 
    cases.add(ic); 
  } 

  public void addCase(IfCase ic)
  { cases.add(ic); }

  public void addCases(IfStatement stat) 
  { cases.addAll(stat.cases); } 

  public Statement substituteEq(String oldE, Expression newE)
  { IfStatement istat = new IfStatement(); 
    for (int i = 0; i < cases.size(); i++) 
    { IfCase ic = (IfCase) cases.get(i); 
      IfCase ic2 = ic.substituteEq(oldE,newE); 
      istat.addCase(ic2); 
    } 
    return istat; 
  } 

  public Statement removeSlicedParameters(BehaviouralFeature bf, Vector fpars)
  { IfStatement istat = new IfStatement(); 
    for (int i = 0; i < cases.size(); i++) 
    { IfCase ic = (IfCase) cases.get(i); 
      IfCase ic2 = ic.removeSlicedParameters(bf,fpars); 
      istat.addCase(ic2); 
    } 
    return istat;
  } 

  public void display()
  { int n = cases.size();
    if (n == 0) 
    { System.out.println("      skip");
      return; } 
    for (int j = 0; j < n; j++)
    { IfCase ic = (IfCase) cases.elementAt(j);
      System.out.print("    "); 
      ic.display(); 
      if (j < n-1) 
      { System.out.println("    ELSE"); } 
    }

    System.out.print("  "); 
    for (int k = 0; k < n; k++)
    { System.out.print("  END"); }
    System.out.println(""); 
  }

  public String bupdateForm()
  { String res = ""; 
    int n = cases.size();
    if (n == 0) 
    { res = res + "      skip\n";
      return res; 
    } 
    for (int j = 0; j < n; j++)
    { IfCase ic = (IfCase) cases.elementAt(j);
      res = res + "    "; 
      res = res + ic.bupdateForm(); 
      if (j < n-1) 
      { res = res + "    ELSE\n"; } 
    }

    System.out.print("  "); 
    for (int k = 0; k < n; k++)
    { res = res + "  END"; }
    res = res + "\n"; 
    return res;
  }

  public BStatement bupdateForm(java.util.Map env, boolean local)
  { int n = cases.size();
    if (n == 0) 
    { return new BBasicStatement("skip"); }
     
    IfCase ic1 = (IfCase) cases.get(0); 
    Expression test1 = ic1.getTest(); 
    Statement if1 = ic1.getIf(); 
    

    BIfStatement res = new BIfStatement(test1.binvariantForm(env,local), 
                                        if1.bupdateForm(env,local)); 
    BIfStatement bifelse = res; 
    for (int j = 1; j < n; j++) 
    { IfCase ic = (IfCase) cases.get(j); 
      Expression tst = ic.getTest(); 
      Statement ifstat = ic.getIf(); 
      BIfStatement remif = new BIfStatement(tst.binvariantForm(env,local),
                                          ifstat.bupdateForm(env,local)); 
      bifelse.setElse(remif);
      bifelse = remif;  
    }
    return res;  
  }

  public void displayImp(String var)
  { int n = cases.size();
    if (n == 0) 
    { System.out.println("      skip");
      return; } 

    for (int j = 0; j < n; j++)
    { IfCase ic = (IfCase) cases.elementAt(j);
      System.out.print("    "); 
      ic.displayImp(var); 
      if (j < n-1) 
      { System.out.println("    ELSE"); } }

    System.out.print("  "); 
    for (int k = 0; k < n; k++)
    { System.out.print("  END"); }
      System.out.println(""); }

  public void displayImp(String var, PrintWriter out)
  { int n = cases.size();
    if (n == 0)
    { out.println("      skip");
      return; }

    for (int j = 0; j < n; j++)
    { IfCase ic = (IfCase) cases.elementAt(j);
      out.print("    ");
      ic.displayImp(var,out);
      if (j < n-1)
      { out.println("    ELSE"); } }

    out.print("  ");
    for (int k = 0; k < n; k++)
    { out.print("  END"); }
    out.println(""); 
  }


  public void display(PrintWriter out)
  { int n = cases.size();
    if (n == 0) 
    { out.println("      skip");
      return; } 

    for (int j = 0; j < n; j++)
    { IfCase ic = (IfCase) cases.elementAt(j);
      out.print("    "); 
      ic.display(out); 
      if (j < n-1) 
      { out.println("    ELSE"); } }

    out.print("  "); 
    for (int k = 0; k < n; k++)
    { out.print("  END"); }
      out.println(""); 
  }

  public String saveModelData(PrintWriter out)
  { Statement cs = convertToConditionalStatement(); 
    return cs.saveModelData(out); 
  } 

  public String saveModelData(PrintWriter out, Entity ent)
  { Statement cs = convertToConditionalStatement(); 
    return cs.saveModelData(out, ent); 
  } 

  /* String res = Identifier.nextIdentifier("conditionalstatement_"); 
    out.println(res + " : ConditionalStatement");
    out.println(res + ".statId = \"" + res + "\"");  

    if (cases.size() > 0) 
    { IfCase case1 = (IfCase) cases.elementAt(0); 
      Expression test = case1.getTest(); 
      String testid = test.saveModelData(out); 
      out.println(res + ".test = " + testid); 
      Statement ifPart = case1.getIf(); 
      String ifpartid = ifPart.saveModelData(out); 
      out.println(res + ".ifPart = " + ifpartid); 
    } 

    if (cases.size() > 1) 
    { IfCase case2 = (IfCase) cases.get(1); 
      Statement elsePart = case2.getIf(); 
      String elsepartid = elsePart.saveModelData(out); 
      out.println(res + ".elsePart = " + elsepartid); 
    } // not quite right

    return res; 
  } */

   public void displayJava(String target)
   { int n = cases.size();
     if (n == 0) 
     { return; } 

     for (int j = 0; j < n; j++)
     { IfCase ic = (IfCase) cases.elementAt(j);
       System.out.print("    "); 
       ic.displayJava(target); 
       if (j < n-1) 
       { System.out.println("    else"); } }
   } 

   public void displayJava(String target, PrintWriter out)
   { int n = cases.size();
     if (n == 0) 
     { return; } 

     for (int j = 0; j < n; j++)
     { IfCase ic = (IfCase) cases.elementAt(j);
       out.print("    "); 
       ic.displayJava(target, out); 
       if (j < n-1) 
       { out.println("    else"); } 
     }
   } 

   public String toStringJava()
   { String res = ""; 
     int n = cases.size();
     if (n == 0) 
     { return res; } 

     for (int j = 0; j < n; j++)
     { IfCase ic = (IfCase) cases.elementAt(j);
       res = res + "    "; 
       res = res + ic.toStringJava(); 
       if (j < n-1) 
       { res = res + "    else\n"; } 
     }
     return res; 
   } 

   public String toEtl()
   { String res = ""; 
     int n = cases.size();
     if (n == 0) 
     { return res; } 

     for (int j = 0; j < n; j++)
     { IfCase ic = (IfCase) cases.elementAt(j);
       Expression test = ic.getTest();
       Statement stat = ic.getIf();
       if ("true".equals(test + ""))
       { res = res + stat; }
       else 
       { res = res + "  if (" + test + ") { " + stat.toEtl() + " }\n";
         if (j < n-1)
         { res = res + "  else "; }
       }       
     }
     return res; 
   } 

   public String toString()
   { int n = cases.size();
     String res = "";
     for (int i = 0; i < n; i++)
     { IfCase ic = (IfCase) cases.get(i);
       Expression test = ic.getTest();
       Statement stat = ic.getIf();
       if ("true".equals(test + ""))
       { res = res + stat; }
       else 
       { res = res + "  if " + test + " then " + stat;
         if (i < n-1)
         { res = res + " else "; }
       }
     }
     return res;
   }

   public String toAST()
   { int n = cases.size();
     String res = "(OclStatement if ";
     for (int i = 0; i < n; i++)
     { IfCase ic = (IfCase) cases.get(i);
       Expression test = ic.getTest();
       Statement stat = ic.getIf();
       if ("true".equals(test + ""))
       { res = res + stat.toAST() + " "; }
       else 
       { res = res + "  if " + test.toAST() + " then " + stat.toAST() + " ";
         if (i < n-1)
         { res = res + " else "; }
       }
     }
     res = res + ")";

    // if (brackets)
    // { res = "(OclStatement ( " + res + " ) )"; } 

    return res; 
  }

  public boolean containsSubexpression(Expression expr)
  { return false; } 

  public Vector singleMutants() 
  { return new Vector(); } 

  public boolean typeCheck(Vector types, Vector entities, Vector cs, Vector env)
  { boolean res = true;
    for (int i = 0; i < cases.size(); i++) 
    { IfCase ic = (IfCase) cases.get(i); 
      res = ic.typeCheck(types,entities,cs,env) && res; 
    }
    return res;
  }

  public boolean typeInference(Vector types, Vector entities, Vector cs, Vector env, java.util.Map vartypes)
  { boolean res = true;
    for (int i = 0; i < cases.size(); i++) 
    { IfCase ic = (IfCase) cases.get(i); 
      res = ic.typeInference(types,entities,cs,env,vartypes) && res; 
    }
    return res;
  }

  public Expression wpc(Expression post)
  { Expression res = null;
    for (int i = 0; i < cases.size(); i++)
    { IfCase ic = (IfCase) cases.get(i);
      Expression test = ic.getTest();
      Statement ifS = ic.getIf();
      Expression e1 = ifS.wpc(post);
      Expression disj =
        new BinaryExpression("&",test,e1);
      if (res == null)
      { res = disj; }
      else
      { res = new BinaryExpression("or",res,disj); }
    }
    return res;
  }

  public Expression wpc(Expression inv, Expression post)
  { Expression res = null;
    for (int i = 0; i < cases.size(); i++)
    { IfCase ic = (IfCase) cases.get(i);
      Expression test = ic.getTest();
      Statement ifS = ic.getIf();
      Expression e1 = ifS.wpc(inv, post);
      Expression disj =
        new BinaryExpression("&",test,e1);
      if (res == null)
      { res = disj; }
      else
      { res = new BinaryExpression("or",res,disj); }
    }
    return res;
  }

  public Vector dataDependents(Vector allvars, Vector vars)
  { return vars; }  

  public Vector dataDependents(Vector allvars, Vector vars, Map mp, Map dlin)
  { return vars; }  

  public boolean updates(Vector v) 
  { return false; } 

  public String updateForm(java.util.Map env, boolean local, Vector types, Vector entities, 
                           Vector vars)
  { String res = ""; 
    int n = cases.size();
    if (n == 0) 
    { return res; } 

    if (n == 1)
    { IfCase ic0 = (IfCase) cases.get(0);
      res = "   " + ic0.updateForm(env,local,types,entities,vars); 
      return res; 
    } 
    else if (n == 2) 
    { IfCase ic0 = (IfCase) cases.get(0);
      IfCase ic1 = (IfCase) cases.get(1);
      res = "   " + ic0.updateForm(env,local,types,entities,vars); 
      if ("true".equals(ic1.getTest()))
      { Statement ep = ic1.getIf(); 
        res = res + "    else { " + ep.updateForm(env,local,types,entities,vars) + " }"; 
      } 
      else 
      { res = res + " else " + ic1.updateForm(env,local,types,entities,vars); }  
      return res; 
    } 
    else
    { for (int j = 0; j < n; j++)
      { IfCase ic = (IfCase) cases.elementAt(j);
        res = res + "    "; 
        res = res + ic.updateForm(env,local,types,entities,vars); 
        if (j < n-1) 
        { IfCase next = (IfCase) cases.get(j+1); 
          if (next.isNull()) { } 
          else 
          { res = res + "    else\n"; } 
        }
      }
      return res;
    }  
  } 

  public String updateFormJava6(java.util.Map env, boolean local)
  { String res = ""; 
    int n = cases.size();
    if (n == 0) 
    { return res; } 

    if (n == 1)
    { IfCase ic0 = (IfCase) cases.get(0);
      res = "   " + ic0.updateFormJava6(env,local); 
      return res; 
    } 
    else if (n == 2) 
    { IfCase ic0 = (IfCase) cases.get(0);
      IfCase ic1 = (IfCase) cases.get(1);
      res = "   " + ic0.updateFormJava6(env,local); 
      if ("true".equals(ic1.getTest()))
      { Statement ep = ic1.getIf(); 
        res = res + "    else { " + ep.updateFormJava6(env,local) + " }"; 
      } 
      else 
      { res = res + " else " + ic1.updateFormJava6(env,local); }  
      return res; 
    } 
    else
    { for (int j = 0; j < n; j++)
      { IfCase ic = (IfCase) cases.elementAt(j);
        res = res + "    "; 
        res = res + ic.updateFormJava6(env,local); 
        if (j < n-1) 
        { IfCase next = (IfCase) cases.get(j+1); 
          if (next.isNull()) { } 
          else 
          { res = res + "    else\n"; } 
        }
      }
      return res;
    }  
  } 

  public String updateFormJava7(java.util.Map env, boolean local)
  { String res = ""; 
    int n = cases.size();
    if (n == 0) 
    { return res; } 

    if (n == 1)
    { IfCase ic0 = (IfCase) cases.get(0);
      res = "   " + ic0.updateFormJava7(env,local); 
      return res; 
    } 
    else if (n == 2) 
    { IfCase ic0 = (IfCase) cases.get(0);
      IfCase ic1 = (IfCase) cases.get(1);
      res = "   " + ic0.updateFormJava7(env,local); 
      if ("true".equals(ic1.getTest()))
      { Statement ep = ic1.getIf(); 
        res = res + "    else { " + ep.updateFormJava7(env,local) + " }"; 
      } 
      else 
      { res = res + " else " + ic1.updateFormJava7(env,local); }  
      return res; 
    } 
    else
    { for (int j = 0; j < n; j++)
      { IfCase ic = (IfCase) cases.elementAt(j);
        res = res + "    "; 
        res = res + ic.updateFormJava7(env,local); 
        if (j < n-1) 
        { IfCase next = (IfCase) cases.get(j+1); 
          if (next.isNull()) { } 
          else 
          { res = res + "    else\n"; } 
        }
      }
      return res;
    }  
  } 

  public String updateFormCSharp(java.util.Map env, boolean local)
  { String res = ""; 
    int n = cases.size();
    if (n == 0) 
    { return res; } 

    if (n == 1)
    { IfCase ic0 = (IfCase) cases.get(0);
      res = "   " + ic0.updateFormCSharp(env,local); 
      return res; 
    } 
    else if (n == 2) 
    { IfCase ic0 = (IfCase) cases.get(0);
      IfCase ic1 = (IfCase) cases.get(1);
      res = "   " + ic0.updateFormCSharp(env,local); 
      if ("true".equals(ic1.getTest()))
      { Statement ep = ic1.getIf(); 
        res = res + "    else { " + ep.updateFormCSharp(env,local) + " }"; 
      } 
      else 
      { res = res + " else " + ic1.updateFormCSharp(env,local); }  
      return res; 
    } 
    else
    { for (int j = 0; j < n; j++)
      { IfCase ic = (IfCase) cases.elementAt(j);
        res = res + "    "; 
        res = res + ic.updateFormCSharp(env,local); 
        if (j < n-1) 
        { IfCase next = (IfCase) cases.get(j+1); 
          if (next.isNull()) { } 
          else 
          { res = res + "    else\n"; } 
        }
      }
      return res;
    }  
  } 

  public String updateFormCPP(java.util.Map env, boolean local)
  { String res = ""; 
    int n = cases.size();
    if (n == 0) 
    { return res; } 

    if (n == 1)
    { IfCase ic0 = (IfCase) cases.get(0);
      res = "   " + ic0.updateFormCPP(env,local); 
      return res; 
    } 
    else if (n == 2) 
    { IfCase ic0 = (IfCase) cases.get(0);
      IfCase ic1 = (IfCase) cases.get(1);
      res = "   " + ic0.updateFormCPP(env,local); 
      if ("true".equals(ic1.getTest()))
      { Statement ep = ic1.getIf(); 
        res = res + "    else { " + ep.updateFormCPP(env,local) + " }"; 
      } 
      else 
      { res = res + " else " + ic1.updateFormCPP(env,local); }  
      return res; 
    } 
    else
    { for (int j = 0; j < n; j++)
      { IfCase ic = (IfCase) cases.elementAt(j);
        res = res + "    "; 
        res = res + ic.updateFormCPP(env,local); 
        if (j < n-1) 
        { IfCase next = (IfCase) cases.get(j+1); 
          if (next.isNull()) { } 
          else 
          { res = res + "    else\n"; } 
        }
      }
      return res;
    }  
  } 


  public Vector allPreTerms()
  { Vector res = new Vector();
    for (int i = 0; i < cases.size(); i++) 
    { IfCase ic = (IfCase) cases.get(i); 
      res.addAll(ic.allPreTerms());
    } 
    return res;  
  }  

  public Vector allPreTerms(String var)
  { Vector res = new Vector();
    for (int i = 0; i < cases.size(); i++) 
    { IfCase ic = (IfCase) cases.get(i); 
      res.addAll(ic.allPreTerms(var));
    } 
    return res;  
  }  

  public Vector readFrame()
  { Vector res = new Vector();
    for (int i = 0; i < cases.size(); i++) 
    { IfCase ic = (IfCase) cases.get(i); 
      res.addAll(ic.readFrame());
    } 
    return res;  
  }  

  public Vector writeFrame()
  { Vector res = new Vector();
    for (int i = 0; i < cases.size(); i++) 
    { IfCase ic = (IfCase) cases.get(i); 
      res.addAll(ic.writeFrame());
    } 
    return res;  
  }  

  public Statement checkConversions(Entity e, Type propType, Type propElemType, java.util.Map interp)
  { return this; } 

  public Statement replaceModuleReferences(UseCase uc) 
  { Vector newcases = new Vector(); 
    for (int i = 0; i < cases.size(); i++) 
    { IfCase cse = (IfCase) cases.get(i); 
      IfCase newcse = (IfCase) cse.replaceModuleReferences(uc); 
      newcases.add(newcse); 
    } 
    IfStatement res = new IfStatement(); 
    res.cases = newcases; 
    res.setEntity(entity); 
    return res; 
  }  // clone the conditions

  public Statement convertToConditionalStatement()
  { int n = cases.size();
    if (n == 0) { return null; }
    return convert2Conditional(cases);
  }

  private static Statement convert2Conditional(Vector cases)
  { if (cases.size() == 1)
    { IfCase ic = (IfCase) cases.get(0);
      Expression test = ic.getTest();
      Statement stat = ic.getIf();
      if ("true".equals(test + ""))
      { return stat; }
      else 
      { return new ConditionalStatement(test, stat); }
    }
    else 
    { IfCase ic = (IfCase) cases.get(0);
      Expression test = ic.getTest();
      Statement stat = ic.getIf();

      Vector tail = new Vector();
      tail.addAll(cases);
      tail.remove(0);
      Statement tailstat = convert2Conditional(tail);
      return new ConditionalStatement(test,stat,tailstat);
    } 
  }

  public int syntacticComplexity()
  { int res = 0;
    for (int i = 0; i < cases.size(); i++) 
    { IfCase ic = (IfCase) cases.get(i); 
      res = res + ic.syntacticComplexity() + 1;
    } 
    return res;  
  }  

  public int cyclomaticComplexity()
  { int res = 0;
    for (int i = 0; i < cases.size(); i++) 
    { IfCase ic = (IfCase) cases.get(i); 
      res = res + ic.cyclomaticComplexity();
    } 
    return res;  
  }  

  public int epl()
  { int res = 0; 
    for (int i = 0; i < cases.size(); i++) 
    { IfCase ic = (IfCase) cases.get(i); 
      res = res + ic.epl();
    } 
    return res; 
  }

  public Vector allOperationsUsedIn()
  { Vector res = new Vector();
    for (int i = 0; i < cases.size(); i++) 
    { IfCase ic = (IfCase) cases.get(i); 
      res.addAll(ic.allOperationsUsedIn());
    } 
    return res;  
  }  

  public Vector allAttributesUsedIn()
  { Vector res = new Vector();
    for (int i = 0; i < cases.size(); i++) 
    { IfCase ic = (IfCase) cases.get(i); 
      res.addAll(ic.allAttributesUsedIn());
    } 
    return res;  
  }  

  public Vector getUses(String var)
  { Vector res = new Vector();
    for (int i = 0; i < cases.size(); i++) 
    { IfCase ic = (IfCase) cases.get(i); 
      res.addAll(ic.getUses(var));
    } 
    return res;  
  }  

  public Vector getVariableUses()
  { Vector res = new Vector();
    for (int i = 0; i < cases.size(); i++) 
    { IfCase ic = (IfCase) cases.get(i); 
      res.addAll(ic.getVariableUses());
    } 
    return res;  
  }  

  public Vector getVariableUses(Vector unused)
  { Vector res = new Vector();
    for (int i = 0; i < cases.size(); i++) 
    { IfCase ic = (IfCase) cases.get(i); 
      Vector icuses = ic.getVariableUses(unused); 
      res.addAll(icuses);
    } 
    return res;  
  }  

  public Vector equivalentsUsedIn()
  { Vector res = new Vector();
    for (int i = 0; i < cases.size(); i++) 
    { IfCase ic = (IfCase) cases.get(i); 
      res.addAll(ic.equivalentsUsedIn());
    } 
    return res;  
  }  

  public Vector metavariables()
  { Vector res = new Vector(); 
    for (int i = 0; i < cases.size(); i++) 
    { IfCase ic = (IfCase) cases.get(i); 
      res.addAll(ic.metavariables());
    } 
    return res; 
  } 

  public Vector cgparameters()
  { Vector res = new Vector(); 
    for (int i = 0; i < cases.size(); i++) 
    { IfCase ic = (IfCase) cases.get(i); 
      res.add(ic);
    } 
    return res; 
  } 

  public String cg(CGSpec cgs)
  { String etext = this + "";
    Vector args = new Vector();

    if (cases.size() > 0) 
    { IfCase ic1 = (IfCase) cases.get(0); 
      Expression test1 = ic1.getTest(); 
      if ("true".equals(test1 + ""))
      { Statement stat1 = ic1.getIf(); 
        return stat1.cg(cgs); 
      } 
    } 

    if (cases.size() > 0) 
    { IfCase ic1 = (IfCase) cases.get(0); 
      args.add(ic1.getTest().cg(cgs));
      args.add(ic1.getIf().cg(cgs)); 
    } 
    
    if (cases.size() > 1) // if then else
    { IfCase ic2 = (IfCase) cases.get(1); 
      args.add(ic2.getIf().cg(cgs));
    } 
    else  // if then
    { args.add(""); }
	
    CGRule r = cgs.matchedStatementRule(this,etext);

    System.out.println(">> Matched statement rule: " + r + " for " + this); 

    if (r != null)
    { return r.applyRule(args); }
    return etext;
  }
}


class AssignStatement extends Statement 
{ private Type type = null;  // for declarations 
  private Expression lhs;
  private Expression rhs;
  private boolean copyValue = false; 
  private String operator = ":=";  // default
  
  /* Note that the version with a type is depricated 
     and is replaced by 
     var lhs : type := rhs */ 

  public AssignStatement(Expression left, Expression right)
  { lhs = left;
    rhs = right; 
  }

  public AssignStatement(Attribute left, Expression right)
  { lhs = new BasicExpression(left);
    rhs = right; 
  }

  public AssignStatement(Binding b) 
  { lhs = new BasicExpression(b.getPropertyName()); 
    rhs = b.expression; 
  } 

  public AssignStatement(String left, Expression right) 
  { lhs = new BasicExpression(left); 
    rhs = right; 
  } 

  public AssignStatement(String op, Expression left, Expression right)
  { if ("=".equals(op))
    { lhs = left; 
      rhs = right; 
    } 
    else if ("+=".equals(op))
    { lhs = left; 
      rhs = new BinaryExpression("+", left, right); 
    } 
    else if ("-=".equals(op))
    { lhs = left; 
      rhs = new BinaryExpression("-", left, right); 
    }  
    else if ("*=".equals(op))
    { lhs = left; 
      rhs = new BinaryExpression("*", left, right); 
    } 
    else if ("/=".equals(op))
    { lhs = left; 
      rhs = new BinaryExpression("/", left, right); 
    } 
    else if ("|=".equals(op))
    { lhs = left; 
      rhs = new BinaryExpression("or", left, right); 
    } 
    else if ("&=".equals(op))
    { lhs = left; 
      rhs = new BinaryExpression("&", left, right); 
    } 
    else if ("^=".equals(op))
    { lhs = left; 
      rhs = new BinaryExpression("xor", left, right); 
    } 
    else if ("%=".equals(op))
    { lhs = left; 
      rhs = new BinaryExpression("mod", left, right); 
    } 
    else if ("<<=".equals(op))
    { lhs = left; 
      rhs = new BinaryExpression("*", left, 
              new BinaryExpression("->pow", 
                new BasicExpression(2), right)); 
    } 
    else if (">>=".equals(op) || ">>>=".equals(op))
    { lhs = left; 
      rhs = new BinaryExpression("/", left, 
              new BinaryExpression("->pow", 
                new BasicExpression(2), right)); 
    } 
    lhs = left; 
    rhs = right;
  } 

  public String getOperator() 
  { return ":="; } 

  public Expression getLeft()
  { return lhs; } 

  public Expression getRight()
  { return rhs; } 

  public Expression getLhs()
  { return lhs; } 

  public Expression getRhs()
  { return rhs; } 


  public void setType(Type t)
  { type = t; } 

  public void setElementType(Type t)
  { lhs.elementType = t; 
    // rhs.elementType = t; 
  } 

  public int execute(ModelSpecification sigma, ModelState beta)
  { Expression rhsValue = rhs.evaluate(sigma, beta); 

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
        { Expression oid = beta.getVariableValue("self"); 
          ObjectSpecification ref = 
                sigma.getObjectSpec("" + oid); 
          if (ref != null)
          { ref.setOCLValue(var, rhsValue); }
        }   
        else 
        { beta.setVariableValue(var, rhsValue); } 
      } 
      else if (obj == null)
      { // simple array variable 
        Expression indv = indx.evaluate(sigma, beta); 
        Expression arr = beta.getVariableValue(var); 

        if (arr instanceof SetExpression)
        { int indval = Integer.parseInt("" + indv); 
          ((SetExpression) arr).setExpression(indval, rhsValue); 
        } 
      }  
      else if (obj != null && 
          indx == null)
      { // object attribute
        Expression oid = obj.evaluate(sigma, beta); 
        ObjectSpecification ref = sigma.getObjectSpec("" + oid); 
        if (ref != null)
        { ref.setOCLValue(var, rhsValue); }   
      } 
    } 

    System.out.println(">> Updated state: " + beta);
    return Statement.NORMAL;  
  } 

  public Expression definedness()
  { Expression ldef = lhs.definedness(); 
    Expression rdef = rhs.definedness(); 

    Expression res = Expression.simplify("&", ldef, rdef, null); 
    return res; 
  } 

  public Vector cgparameters()
  { Vector args = new Vector();
    Expression rhsnopre = rhs.removePrestate(); 
    
    if (lhs instanceof BasicExpression)
    { BasicExpression lhsbe = (BasicExpression) lhs; 
      if (lhsbe.arrayIndex != null) 
      { BasicExpression lhs0 = (BasicExpression) lhsbe.clone(); 
        lhs0.arrayIndex = null;
        lhs0.type = lhsbe.arrayType;  
        args.add(lhs0); 
        args.add(lhsbe.arrayIndex); 
        args.add(rhsnopre);
        return args; 
      } 
    } 
    args.add(lhs);
    args.add(rhsnopre);
    return args; 
  } 


  public String basiccg(CGSpec cgs)
  { // assumes type == null 
    String etext = this + "";
    Vector args = new Vector();
    Vector eargs = new Vector(); 
    Expression rhsnopre = rhs.removePrestate(); 
    
    if (lhs instanceof BasicExpression)
    { BasicExpression lhsbe = (BasicExpression) lhs; 
      if (lhsbe.arrayIndex != null) 
      { BasicExpression lhs0 = (BasicExpression) lhsbe.clone(); 
        lhs0.arrayIndex = null; 
        args.add(lhs0.cg(cgs)); 
        eargs.add(lhs0); 
        args.add(lhsbe.arrayIndex.cg(cgs)); 
        eargs.add(lhsbe.arrayIndex); 
        args.add(rhsnopre.cg(cgs));
        eargs.add(rhsnopre);
        CGRule r = cgs.matchedStatementRule(this,etext);

    System.out.println(">> Matched statement rule: " + r + " for " + this); 

        if (r != null)
        { return r.applyRule(args,eargs,cgs); }
        return etext; 
      }
    }
    args.add(lhs.cg(cgs));
    eargs.add(lhs); 
    args.add(rhsnopre.cg(cgs));
    eargs.add(rhsnopre);
 
    CGRule r = cgs.matchedStatementRule(this,etext);

    System.out.println(">> Matched statement rule: " + r + " for " + this); 

    if (r != null)
    { return r.applyRule(args,eargs,cgs); }
    return etext; 
  }
  
  public Vector basiccgparameters()
  { // assumes type == null 
    Vector eargs = new Vector(); 
    Expression rhsnopre = rhs.removePrestate(); 
    
    if (lhs instanceof BasicExpression)
    { BasicExpression lhsbe = (BasicExpression) lhs; 
      if (lhsbe.arrayIndex != null) 
      { BasicExpression lhs0 = (BasicExpression) lhsbe.clone(); 
        lhs0.arrayIndex = null; 
        eargs.add(lhs0); 
        eargs.add(lhsbe.arrayIndex); 
        eargs.add(rhsnopre);
        return eargs; 
      }
    }
    eargs.add(lhs); 
    eargs.add(rhsnopre);
 
    return eargs; 
  }
  
  
  
  public String cg(CGSpec cgs)
  { if (type == null) 
    { return basiccg(cgs); } 
    else // if (type != null) 
    { // process as  var lhs : type ; lhs := rhs; 
      SequenceStatement stat = new SequenceStatement(); 
      CreationStatement cre = new CreationStatement(type + "", lhs + "");
      cre.setType(type); 
      cre.setElementType(lhs.elementType);  
      AssignStatement newas = new AssignStatement(lhs,rhs);
      newas.type = null;  
      stat.addStatement(cre); 
      stat.addStatement(newas); 
      return stat.cg(cgs); 
    } 

    /* String etext = this + "";
    Vector args = new Vector();
    args.add(lhs.cg(cgs));
    Vector eargs = new Vector(); 
    eargs.add(lhs); 
    Expression rhsnopre = rhs.removePrestate(); 
    args.add(rhsnopre.cg(cgs));
    eargs.add(rhsnopre);
 
    CGRule r = cgs.matchedStatementRule(this,etext);
    if (r != null)
    { return r.applyRule(args,eargs,cgs); }
    return etext; */ 
  }

  public void setCopyValue(boolean b)
  { copyValue = b; } 

  public void setOperator(String op)
  { operator = op; } 

  public Object clone()
  { Expression newlhs = (Expression) lhs.clone(); 
    Expression newrhs = (Expression) rhs.clone(); 
    AssignStatement res = new AssignStatement(newlhs,newrhs); 
    res.setType(type); 
    res.setCopyValue(copyValue); 
    return res; 
  } 

  public Statement optimiseOCL()
  { Expression newlhs = lhs.simplifyOCL(); 
    Expression newrhs = rhs.simplifyOCL(); 
    AssignStatement res = new AssignStatement(newlhs,newrhs); 
    res.setType(type); 
    res.setCopyValue(copyValue); 
    return res; 
  } 

  public void findClones(java.util.Map clones, String rule, String op)
  { if (rhs.syntacticComplexity() < UCDArea.CLONE_LIMIT) 
    { return; }
    /* String val = rhs + ""; 
    Vector used = (Vector) clones.get(val);
    if (used == null)  
    { used = new Vector(); }
    if (rule != null)
    { used.add(rule); }
    else if (op != null)
    { used.add(op); }
    clones.put(val,used); */ 
    rhs.findClones(clones,rule,op); 
  }

  public void findClones(java.util.Map clones, 
                         java.util.Map cdefs,
                         String rule, String op)
  { if (rhs.syntacticComplexity() < UCDArea.CLONE_LIMIT) 
    { return; }
    rhs.findClones(clones,cdefs,rule,op); 
  }

  public Vector allVariableNames()
  { Vector res = lhs.allVariableNames();
    if (rhs != null)  
    { res = VectorUtil.union(res, rhs.allVariableNames()); } 
    return res; 
  } 

  public Map energyUse(Map uses, 
                                Vector rUses, Vector oUses)
  { lhs.energyUse(uses, rUses, oUses); 
    rhs.energyUse(uses, rUses, oUses);

    int syncomp = rhs.syntacticComplexity(); 
    if (syncomp > TestParameters.syntacticComplexityLimit)
    { int acount = (int) uses.get("amber"); 
      uses.set("amber", acount + 1); 
      oUses.add("! Code smell (MEL): too high expression complexity (" + syncomp + ") for " + rhs + "\n" + 
                ">>> Recommend OCL refactoring");  
    } 

    return uses; 
  }  

  public java.util.Map collectionOperatorUses(int lev, 
                          java.util.Map uses, 
                          Vector vars)
  { rhs.collectionOperatorUses(lev, uses, vars); 
    lhs.collectionOperatorUses(lev, uses, vars); 
    return uses; 
  } 

  public void findMagicNumbers(java.util.Map mgns, String rule, String op)
  { lhs.findMagicNumbers(mgns, "" + this, op);
    rhs.findMagicNumbers(mgns, "" + this, op); 
  } 

  public Statement dereference(BasicExpression var)
  { Expression newlhs = (Expression) lhs.dereference(var); 
    Expression newrhs = (Expression) rhs.dereference(var); 
    AssignStatement res = new AssignStatement(newlhs,newrhs); 
    res.setType(type); 
    res.setCopyValue(copyValue); 
    return res; 
  } 

  public Statement addContainerReference(BasicExpression ref,
                                         String var,
                                         Vector excl)
  { Expression newlhs = 
        lhs.addContainerReference(ref,var,excl); 
    Expression newrhs = 
        rhs.addContainerReference(ref,var,excl); 
    AssignStatement res = new AssignStatement(newlhs,newrhs); 
    res.setType(type); 
    res.setCopyValue(copyValue); 
    return res; 
  } 

  public Statement substituteEq(String oldE, Expression newE)
  { Expression lhs2 = // (Expression) lhs.clone(); 
         lhs.substituteEq(oldE,newE); 
    Expression rhs2 = rhs.substituteEq(oldE,newE); 
    AssignStatement res = new AssignStatement(lhs2,rhs2); 
    res.setType(type); 
    res.setCopyValue(copyValue); 
    return res; 
  } 

  public Statement removeSlicedParameters(BehaviouralFeature bf, Vector fpars)
  { Expression lhs2 = (Expression) lhs.clone(); 
        /* lhs.substituteEq(oldE,newE); */ 
    Expression rhs2 = rhs.removeSlicedParameters(bf,fpars); 
    AssignStatement res = new AssignStatement(lhs2,rhs2); 
    res.setType(type); 
    res.setCopyValue(copyValue); 
    return res; 
  } 

  public String toString() 
  { if (type == null) 
    { return lhs + " " + operator + " " + rhs + " "; }
    else 
    { return lhs + " : " + type + " := " + rhs + " "; } 
  }  

  public String toAST() 
  { String res = "(OclStatement " + lhs.toAST() + " := " + rhs.toAST() + " )";
 
    // if (brackets)
    // { res = "(OclStatement ( " + res + " ) )"; } 

    return res;  
  }  

  public boolean containsSubexpression(Expression expr)
  { return rhs.containsSubexpression(expr); } 

  public Vector singleMutants()
  { Vector exprs = rhs.singleMutants(); 
    Vector res = new Vector(); 
    for (int i = 0; i < exprs.size(); i++) 
    { Expression mut = (Expression) exprs.get(i); 
      res.add(new AssignStatement(lhs,mut)); 
    } 
    return res; 
  } // also lhs?

  public String saveModelData(PrintWriter out)
  { String res = Identifier.nextIdentifier("assignstatement_"); 
    out.println(res + " : AssignStatement"); 
    out.println(res + ".statId = \"" + res + "\""); 
    String lhsid = lhs.saveModelData(out); 
    String rhsid = rhs.saveModelData(out); 
    out.println(res + ".left = " + lhsid); 
    out.println(res + ".right = " + rhsid); 
    if (type != null) 
    { String typeid = type.getUMLModelName(out); 
      out.println(typeid + " : " + res + ".type"); 
    }
    return res; 
  }  

  public String saveModelData(PrintWriter out, Entity ent) 
  { java.util.Map env = new java.util.HashMap(); 

    Vector cons = ent.getInvariants(); 
    String ename = ent.getName(); 
    SequenceStatement ss = new SequenceStatement(); 
    ss.addStatement(this); 

    System.out.println(">>> " + ename + " invariants are: " + cons); 

    // Plus: cc.generateDesign(env,true) for any 
    // constraint of current class E, for which
    // cc.isBehavioural() && cc.dependsUpon(ename,lhs+"")

    for (int i = 0; i < cons.size(); i++) 
    { Constraint cc = (Constraint) cons.get(i); 
      if (cc.dependsUpon(ename,lhs+""))
      { System.out.println(">>> Invariant " + cc + " affected by update to " + lhs); 

        Statement act = cc.generateDesign(env,true); 
        if (act != null) 
        { ss.addStatement(act); 
          System.out.println(">>> Additional action " + act + " for " + this + " due to invariant " + cc); 
        } 
      } 
    } 

    if (ss.size() == 1)  
    { return saveModelData(out); }
    else 
    { return ss.saveModelData(out); } 
  } 

  public void display()
  { if (type == null) 
    { System.out.println(lhs + " := " + rhs + " "); }
    else 
    { System.out.println(lhs + " : " + type + " := " + rhs + " "); } 
  } 

  public String bupdateForm()
  { return lhs + " := " + rhs; }

  public BStatement bupdateForm(java.util.Map env, boolean local)
  { BExpression brhs = rhs.binvariantForm(env,local); 
    BStatement stat = ((BasicExpression) lhs).bEqupdateForm(env,brhs,local); 
    return stat; 
  } // ignores type

  public void displayImp(String var) 
  { System.out.print(lhs + "_STO_VAR(" + rhs + ")"); }

  public void displayImp(String var, PrintWriter out)
  { out.print(lhs + "_STO_VAR(" + rhs + ")"); }

  public void display(PrintWriter out)
  { out.print(lhs + " := " + rhs); }

  public void displayJava(String target)
  { if (type != null) 
    { System.out.print("  " + type.getJava() + " "); } 
    System.out.print(lhs + " = " + rhs + ";  " + 
                     "System.out.println(\"" + lhs + " set to " + rhs + "\");");
  }

  public void displayJava(String target, PrintWriter out)
  { if (type != null) 
    { out.print("  " + type.getJava() + " "); } 
    out.print(lhs + " = " + rhs + ";  " + 
              "System.out.println(\"" + lhs + " set to " + 
              rhs + "\");"); 
  }

  public String toStringJava()
  { java.util.Map env = new java.util.HashMap(); 
    if (entity != null) 
    { env.put(entity.getName(),"this"); } 
    String res = (new BinaryExpression("=",lhs,rhs)).updateForm(env,true);  
    if (type != null) 
    { res = "  " + type.getJava() + " " + res; } 
    
    return res; 
  }

  public String toEtl()
  { String res = lhs + " = " + rhs + ";";  
    return res; 
  }

  // public String toString()
  // { return lhs + " := " + rhs + " "; } 

  public boolean typeCheck(Vector types, Vector entities, Vector cs, Vector env)
  { // Also recognise the type as an entity or enumeration if it exists
    boolean res = lhs.typeCheck(types,entities,cs,env); 
    res = rhs.typeCheck(types,entities,cs,env);
    if (lhs.type == null && rhs.type != null) 
    { lhs.type = rhs.type; } 
    if (rhs.elementType != null && lhs.elementType == null) 
    { lhs.elementType = rhs.elementType; } 
    else if (lhs.elementType != null && rhs.elementType == null) 
    { rhs.elementType = lhs.elementType; } 

    if (BasicExpression.hasVariable(lhs))
    { BasicExpression.updateVariableType(lhs,rhs); } 
    else if (BasicExpression.isMapAccess(lhs))
    { // update key and element types appropriately
      BasicExpression.updateMapType(lhs,rhs); 
    } 

    if (type != null)  // declare it
    { Attribute att = new Attribute(lhs + "", rhs.type, ModelElement.INTERNAL); 
      att.setElementType(lhs.elementType);
      System.out.println(">>> " + lhs + " has type " + att.getType());  
      env.add(att); 
    } 

    return res; 
  }

  public boolean typeInference(Vector types, Vector entities, Vector cs, Vector env, java.util.Map vartypes)
  { // Also recognise the type as an entity or enumeration if it exists

    // boolean res = lhs.typeCheck(types,entities,cs,env);
    // res = rhs.typeCheck(types,entities,cs,env);
    boolean res = rhs.typeInference(types,entities,cs,env,vartypes);
    Type rhsType = rhs.getType();  
    
    vartypes.put(lhs + "", rhsType); 

    if (Type.isVacuousType(lhs.type) && 
        !Type.isVacuousType(rhsType)) 
    { lhs.type = rhsType; } 

    if (rhs.elementType != null && lhs.elementType == null) 
    { lhs.elementType = rhs.elementType; } 
    else if (lhs.elementType != null && rhs.elementType == null) 
    { rhs.elementType = lhs.elementType; } 

    if (BasicExpression.hasVariable(lhs))
    { BasicExpression.updateVariableType(lhs,rhs); } 
    else if (BasicExpression.isMapAccess(lhs))
    { // update key and element types appropriately
      BasicExpression.updateMapType(lhs,rhs); 
    } 

    if (type != null)  // declare it
    { Attribute att = new Attribute(lhs + "", rhs.type, ModelElement.INTERNAL); 
      att.setElementType(lhs.elementType);
      System.out.println(">>> local variable " + lhs + " has type " + att.getType());  
      env.add(att); 
    } 

    Type declaredType = (Type) vartypes.get(lhs + ""); 
    if (!Type.isVacuousType(declaredType) && 
        Type.isVacuousType(lhs.type))
    { lhs.type = declaredType;
      System.out.println(">>> " + lhs + " actual type is " + declaredType);  
    } // does not allow for changing the type 

    // JOptionPane.showInputDialog("--- deduced type " + rhsType + " for " + lhs); 

    return res; 
  }


  public Expression wpc(Expression post)
  { return post.substituteEq(lhs.toString(),rhs); }
  // But more complex than this if the lhs is an array index

  public Expression wpc(Expression inv, Expression post)
  { return inv.substituteEq(lhs.toString(),rhs); }
  // But more complex than this if the lhs is an array index

  public Vector dataDependents(Vector allvars, Vector vars)
  { Vector vbls = new Vector(); 
    vbls.addAll(vars); 

    String updatedVar = lhs.updatedData(); 

    if (updatedVar != null && vars.contains(updatedVar))
    { // remove this variable and add all vars of rhs to vars
      vbls.remove(updatedVar); 
      Vector es = rhs.allAttributesUsedIn(); 
      // System.out.println(">----Attributes:-------- " + es);
      Vector vs = rhs.getVariableUses(); 
      // System.out.println(">----Variables:-------- " + vs);
      es.addAll(vs); 
      
      for (int i = 0; i < es.size(); i++) 
      { String var = "" + es.get(i); 
        if (vbls.contains(var)) { } 
        else 
        { vbls.add(var); } 
      } 

      // System.out.println(updatedVar + " --from--> " + es); 
    } 

    // Case of updates to arrays/sequence elements

    return vbls; 
  }  

  public Vector dataDependents(Vector allvars, Vector vars, Map mp, Map dlin)
  { Vector vbls = new Vector(); 
    vbls.addAll(vars); 

    String updatedVar = lhs + ""; 

    if (updatedVar != null) //  && vars.contains(updatedVar))
    { // remove this variable and add all vars of rhs to vars
      vbls.remove(updatedVar); 
      Vector es = rhs.allAttributesUsedIn(); 
      // System.out.println(">----Attributes:-------- " + es);
      Vector vs = rhs.getVariableUses(); 
      // System.out.println(">----Variables:-------- " + vs);
      es.addAll(vs); 
      
      Vector rhsBEs = rhs.allReadBasicExpressionData(); 
      
      for (int i = 0; i < es.size(); i++) 
      { String var = "" + es.get(i); 
        mp.add_pair(var, updatedVar); 
        if (vbls.contains(var)) { } 
        else 
        { vbls.add(var); } 
      } 

      // System.out.println(updatedVar + " --from--> " + rhsBEs);
 
      for (int i = 0; i < rhsBEs.size(); i++) 
      { String rv = "" + rhsBEs.get(i); 
        dlin.add_pair(rv, updatedVar);
      } 

    } 

    // Case of updates to arrays/sequence elements

    return vbls; 
  }  

  public boolean updates(Vector v) 
  { String updatedVar = lhs.updatedData(); 
    if (updatedVar != null && v.contains(updatedVar))
    { return true; }
    return false; 
  }  // contains(lhs.data) or contains the base identifier of lhs

  public Vector slice(Vector allvars, Vector vars)
  { Vector res = new Vector(); 
    if (vars.contains(lhs.toString()))  // lhs.data
    { res.add(this); } 
    else 
    { System.out.println(">> Deleting statement from slice: " + this); } 
    return res; 
  }  

  public Expression toExpression()
  { return new BinaryExpression("=",lhs,rhs); }

  /* public Statement generateDesign(java.util.Map env, boolean local)
  { if (type == null) 
    { Statement res = (new BinaryExpression("=", lhs, rhs)).generateDesign(env,local); 
      System.out.println("++++ Generated design " + res); 
      return res; 
    } 
    return this; 
  } */ 

  public String updateForm(java.util.Map env, boolean local, Vector types, Vector entities,
                           Vector vars)
  { // if (entity != null) 
    // { env.put(entity.getName(),"this"); } 
    if (copyValue && type != null && type.isMapType())
    { String res = "  " + type.getJava() + " " + lhs + " = new HashMap();\n"; 
      res = res + "  " + lhs + ".putAll(" + rhs.queryForm(env,local) + ");\n"; 
      return res; 
    } // For type.isEntityType() or strings, clone the rhs. 
    else if (copyValue && lhs.getType() != null && lhs.getType().isMapType())
    { String res = "  " + lhs + " = new HashMap();\n"; 
      res = res + "  " + lhs + ".putAll(" + rhs.queryForm(env,local) + ");\n"; 
      return res; 
    } // For type.isEntityType() or strings, clone the rhs. 

    if (copyValue && type != null && type.isCollectionType())
    { String res = "  " + type.getJava() + " " + lhs + " = new Vector();\n"; 
      res = res + "  " + lhs + ".addAll(" + rhs.queryForm(env,local) + ");\n"; 
      return res; 
    } // For type.isEntityType() or strings, clone the rhs. 
    else if (copyValue && lhs.getType() != null && lhs.getType().isCollectionType())
    { String res = "  " + lhs + " = new Vector();\n"; 
      res = res + "  " + lhs + ".addAll(" + rhs.queryForm(env,local) + ");\n"; 
      return res; 
    } // For type.isEntityType() or strings, clone the rhs. 

    // if lhs has a target entity element type, and rhs has a source entity element type
    // do a type conversion  lhs := TRef[rhs.$id]
    Type letype = lhs.getElementType(); 
    Type retype = rhs.getElementType(); 
    if (letype != null && retype != null && letype.isEntity() && retype.isEntity())
    { Entity srcent = retype.getEntity(); 
      Entity trgent = letype.getEntity(); 
      if (srcent.isSourceEntity() && trgent.isTargetEntity())
      { BasicExpression fid = new BasicExpression("$id");
        fid.setType(new Type("String",null));
        fid.setUmlKind(Expression.ATTRIBUTE);
        fid.setEntity(srcent);
        fid.setObjectRef(rhs); 

        BasicExpression felem = new BasicExpression(trgent.getName());
        felem.setUmlKind(Expression.CLASSID);
        felem.setEntity(trgent);
        felem.setArrayIndex(fid);
        felem.setType(letype);
        felem.setElementType(letype);

        BinaryExpression feq = new BinaryExpression("=", lhs, felem);
        String fres = feq.updateForm(env,local);
        if (type != null) 
        { fres = "  " + type.getJava() + " " + fres; }
        return fres;  
      } 
    } 


    String res = (new BinaryExpression("=",lhs,rhs)).updateForm(env,local);  
    if (type != null) 
    { res = "  " + type.getJava() + " " + res; } 
    
    return res; 
  } 

  public String updateFormJava6(java.util.Map env, boolean local)
  { // if (entity != null) 
    // { env.put(entity.getName(),"this"); } 
    if (copyValue && type != null && type.isMapType())
    { String res = "  " + type.getJava6() + " " + lhs + " = new HashMap();\n"; 
      res = res + "  " + lhs + ".putAll(" + rhs.queryFormJava6(env,local) + ");\n"; 
      return res; 
    } // For type.isEntityType() or strings, clone the rhs. 
    else if (copyValue && lhs.getType() != null && lhs.getType().isMapType())
    { String res = "  " + lhs + " = new HashMap();\n"; 
      res = res + "  " + lhs + ".putAll(" + rhs.queryFormJava6(env,local) + ");\n"; 
      return res; 
    } // For type.isEntityType() or strings, clone the rhs. 

    if (copyValue && type != null && type.isCollectionType())
    { String res = "  " + type.getJava6() + " " + lhs + " = " + type.initialValueJava6() + ";\n"; 
      res = res + "  " + lhs + ".addAll(" + rhs.queryFormJava6(env,local) + ");\n"; 
      return res; 
    } // For type.isEntityType() or strings, clone the rhs. 
    else if (copyValue && lhs.getType() != null && lhs.getType().isCollectionType())
    { String res = "  " + lhs + " = " + lhs.getType().initialValueJava6() + ";\n"; 
      res = res + "  " + lhs + ".addAll(" + rhs.queryFormJava6(env,local) + ");\n"; 
      return res; 
    } // For type.isEntityType() or strings, clone the rhs. 

    String res = (new BinaryExpression("=",lhs,rhs)).updateFormJava6(env,local);  
    if (type != null) 
    { res = "  " + type.getJava6() + " " + res; } 
    
    return res; 
  } 

  public String updateFormJava7(java.util.Map env, boolean local)
  { // if (entity != null) 
    // { env.put(entity.getName(),"this"); } 
    if (copyValue && type != null && type.isCollectionType())
    { String res = "  " + type.getJava7(lhs.elementType) + " " + lhs + " = " + type.initialValueJava7() + ";\n"; 
      res = res + "  " + lhs + ".addAll(" + rhs.queryFormJava7(env,local) + ");\n"; 
      return res; 
    } // For type.isEntityType() or strings, clone the rhs. 
    else if (copyValue && lhs.getType() != null && lhs.getType().isCollectionType())
    { String res = "  " + lhs + " = " + lhs.getType().initialValueJava7() + ";\n"; 
      res = res + "  " + lhs + ".addAll(" + rhs.queryFormJava7(env,local) + ");\n"; 
      return res; 
    } // For type.isEntityType() or strings, clone the rhs. 

    String res = (new BinaryExpression("=",lhs,rhs)).updateFormJava7(env,local);  
    if (type != null) 
    { res = "  " + type.getJava7(lhs.elementType) + " " + res; } 
    
    return res; 
  } 

  public String updateFormCSharp(java.util.Map env, boolean local)
  { // if (entity != null) 
    // { env.put(entity.getName(),"this"); } 
    if (copyValue && type != null && type.isCollectionType())
    { String res = "    " + type.getCSharp() + " " + lhs + " = new ArrayList();\n"; 
      res = res + "    " + lhs + ".AddRange(" + rhs.queryFormCSharp(env,local) + ");\n"; 
      return res; 
    } // For type.isEntityType() or strings, clone the rhs. 
    else if (copyValue && lhs.getType() != null && lhs.getType().isCollectionType())
    { String res = "    " + lhs + " = new ArrayList();\n"; 
      res = res + "    " + lhs + ".AddRange(" + rhs.queryFormCSharp(env,local) + ");\n"; 
      return res; 
    } // For type.isEntityType() or strings, clone the rhs. 

    String res = (new BinaryExpression("=",lhs,rhs)).updateFormCSharp(env,local);
  
    if (type != null) 
    { res = "    " + type.getCSharp() + "   " + res; } 
    
    return res; 
  } 

  public String updateFormCPP(java.util.Map env, boolean local)
  { // if (entity != null) 
    // { env.put(entity.getName(),"this"); } 

    if (copyValue && type != null && Type.isSequenceType(type))
    { String elemt = rhs.getElementType().getCPP(); 
      String res = "  vector<" + elemt + ">* " + lhs + " = new vector<" + elemt + ">();\n"; 
      String rqf = rhs.queryFormCPP(env,local); 
      res = res + "  " + lhs + "->insert(" + lhs + "->end(), " + rqf + "->begin(), " + 
                                         rqf + "->end());\n"; 
      return res; 
    } // For type.isEntityType() or strings, clone the rhs. 
    else if (copyValue && type != null && Type.isSetType(type))
    { String elemt = rhs.getElementType().getCPP(); 
      String res = "  std::set<" + elemt + ">* " + lhs + " = new std::set<" + elemt + ">();\n"; 
      String rqf = rhs.queryFormCPP(env,local); 
      res = res + "  " + lhs + "->insert(" + rqf + "->begin(), " + rqf + "->end());\n"; 
      return res; 
    } // For type.isEntityType() or strings, clone the rhs. 
    else if (copyValue && lhs.getType() != null && Type.isSequenceType(lhs.getType()))
    { String elemt = rhs.getElementType().getCPP(); 
      String rqf = rhs.queryFormCPP(env,local); 
      String res = "  " + lhs + " = new vector<" + elemt + ">();\n"; 
      res = res + "  " + lhs + "->insert(" + lhs + "->end(), " + rqf + "->begin(), " + 
                                         rqf + "->end());\n"; 
      return res; 
    } // For type.isEntityType() or strings, clone the rhs. 
    else if (copyValue && lhs.getType() != null && Type.isSetType(lhs.getType()))
    { String elemt = rhs.getElementType().getCPP(); 
      String rqf = rhs.queryFormCPP(env,local); 
      String res = "  " + lhs + " = new std::set<" + elemt + ">();\n"; 
      res = res + "  " + lhs + "->insert(" + rqf + "->begin(), " + 
                                         rqf + "->end());\n"; 
      return res; 
    } // For type.isEntityType() or strings, clone the rhs. 



    String res = (new BinaryExpression("=",lhs,rhs)).updateFormCPP(env,local);  
    if (type != null) 
    { res = "  " + type.getCPP(rhs.getElementType()) + " " + res; } 
    
    return res; 
  } 

  public Vector allPreTerms()
  { Vector res = rhs.allPreTerms();
    return res;  
  }  

  public Vector allPreTerms(String var)
  { Vector res = rhs.allPreTerms(var);
    return res;  
  }  

  public Vector readFrame()
  { Vector res = new Vector();
    res.addAll(rhs.allReadFrame());  
    return res;  
  }  

  public Vector writeFrame()
  { Vector res = new Vector();

    if (lhs instanceof BasicExpression) 
    { String frame = ((BasicExpression) lhs).data; 
      Entity e = lhs.getEntity(); 
      if (e != null) 
      { frame = e.getName() + "::" + frame; } 
      res.add(frame); 
    } // also case of v->at(i) := expr, etc

    // res.add(lhs + "");  // lhs.data if a BasicExpression
    return res;  
  }  

  public Statement checkConversions(Entity e, Type _propType, Type _propElemType, java.util.Map interp)
  { if (lhs instanceof BasicExpression)
    { BasicExpression belhs = (BasicExpression) lhs; 
      // Type propType = lhs.getType(); 
      // Type propElemType = lhs.getElementType();
      String propertyName = belhs.getData(); 
      Type propType = e.getDefinedFeatureType(propertyName); 
      Type propElemType = e.getDefinedFeatureElementType(propertyName); 
      // System.out.println("CONVERTING " + rhs + " TO TYPE " + propType + " " + propElemType); 

      Expression newrhs = rhs.checkConversions(propType,propElemType,interp); 
      AssignStatement res = new AssignStatement(lhs,newrhs); 
      res.setType(type); 
      res.setCopyValue(copyValue); 
      return res; 
    } 
    else  
    { return this; }
  }  

  public Statement replaceModuleReferences(UseCase uc)
  { if (lhs instanceof BasicExpression)
    { BasicExpression belhs = (BasicExpression) lhs; 

      Expression newlhs = lhs.replaceModuleReferences(uc); 
      Expression newrhs = rhs.replaceModuleReferences(uc); 
      AssignStatement res = new AssignStatement(newlhs,newrhs); 
      res.setType(type); 
      res.setCopyValue(copyValue); 
      return res; 
    } 
    else  
    { return this; }
  }  

  public int syntacticComplexity()
  { int syncomp = rhs.syntacticComplexity(); 
    return lhs.syntacticComplexity() + syncomp + 1; 
  } 

  public int cyclomaticComplexity()
  { return 0; } 

  public int epl()
  { return 0; }  // although a typed assignment should be 1

  public Vector allOperationsUsedIn()
  { Vector res = new Vector();
    res.addAll(rhs.allOperationsUsedIn());  
    return res;  
  }  

  public Vector allAttributesUsedIn()
  { Vector res = new Vector();
    res.addAll(lhs.allAttributesUsedIn());  
    res.addAll(rhs.allAttributesUsedIn());  
    return res;  
  }  

  public Vector getUses(String var)
  { Vector res = new Vector();
    res.addAll(lhs.getUses(var));  
    res.addAll(rhs.getUses(var));  
    return res;  
  }  

  public Vector getVariableUses()
  { Vector res = new Vector();
    res.addAll(lhs.getVariableUses());  
    res.addAll(rhs.getVariableUses());  
    return res;  
  }  

  public Vector equivalentsUsedIn()
  { Vector res = new Vector();
    res.addAll(rhs.equivalentsUsedIn());  
    return res;  
  }  

  public Vector metavariables()
  { Vector res = lhs.metavariables();
    res.addAll(rhs.metavariables());  
    return res;  
  }  
}


class IfCase
{ private Expression test; 
  private Statement ifPart;
  private Entity entity;

  IfCase(Expression t, Statement i)
  { test = t; 
    ifPart = i; 
  }

  public Object clone()
  { Expression newtest = (Expression) test.clone(); 
    Statement newif = (Statement) ifPart.clone(); 
    IfCase res = new IfCase(newtest,newif); 
    res.setEntity(entity); 
    return res; 
  }  

  public IfCase optimiseOCL()
  { Expression newtest = test.simplifyOCL(); 
    Statement newif = ifPart.optimiseOCL(); 
    IfCase res = new IfCase(newtest,newif); 
    res.setEntity(entity); 
    return res; 
  }  

  public IfCase dereference(BasicExpression var)
  { Expression newtest = (Expression) test.dereference(var); 
    Statement newif = (Statement) ifPart.dereference(var); 
    IfCase res = new IfCase(newtest,newif); 
    res.setEntity(entity); 
    return res; 
  } 

  public void findClones(java.util.Map clones, String rule, String op)
  { if (test.syntacticComplexity() >= UCDArea.CLONE_LIMIT) 
    { test.findClones(clones,rule,op); }
    ifPart.findClones(clones,rule,op);
  }

  public void findClones(java.util.Map clones, 
                         java.util.Map cdefs,
                         String rule, String op)
  { if (test.syntacticComplexity() >= UCDArea.CLONE_LIMIT) 
    { test.findClones(clones,cdefs,rule,op); } 
    
    ifPart.findClones(clones,cdefs,rule,op);
  }

  public Map energyUse(Map uses, Vector ruses, Vector ouses)
  { test.energyUse(uses, ruses, ouses); 
    ifPart.energyUse(uses, ruses, ouses);
    return uses; 
  }

  public java.util.Map collectionOperatorUses(int lev, 
                          java.util.Map uses, 
                          Vector vars)
  { test.collectionOperatorUses(lev, uses, vars); 
    ifPart.collectionOperatorUses(lev, uses, vars);
    return uses; 
  }


  public void findMagicNumbers(java.util.Map mgns, String rule, String op)
  { test.findMagicNumbers(mgns,this + "",op); 
    ifPart.findMagicNumbers(mgns,rule,op);
  }
 

  public IfCase addContainerReference(BasicExpression ref,
                                      String var,
                                      Vector excl)
  { Expression newtest = test.addContainerReference(ref,var,excl); 
    Statement newif = ifPart.addContainerReference(ref,var,excl); 
    IfCase res = new IfCase(newtest,newif); 
    res.setEntity(entity); 
    return res; 
  }  

  public IfCase generateDesign(java.util.Map env, boolean local)
  { Statement newif = ifPart.generateDesign(env,local); 
    IfCase res = new IfCase(test,newif); 
    res.setEntity(entity); 
    return res; 
  }  

  public boolean isNull()
  { return "true".equals(test + "") && "skip".equals(ifPart + ""); } 

  public Expression getTest() 
  { return test; } 

  public Statement getIf()
  { return ifPart; } 

  public void setIf(Statement s)
  { ifPart = s; } 

  public void setEntity(Entity e)
  { entity = e; 
    ifPart.setEntity(e);   // surely?? 
  }

  public IfCase substituteEq(String oldE, Expression newE) 
  { Expression e = test.substituteEq(oldE,newE); 
    Statement stat = ifPart.substituteEq(oldE,newE);
    IfCase res = new IfCase(e,stat);
    res.setEntity(entity); 
    return res; 
  } 

  public IfCase removeSlicedParameters(BehaviouralFeature bf, Vector fpars)
  { Expression e = test.removeSlicedParameters(bf,fpars); 
    Statement stat = ifPart.removeSlicedParameters(bf,fpars);
    IfCase res = new IfCase(e,stat);
    res.setEntity(entity); 
    return res;
  } 


  public void display()
  { System.out.print("IF " + test + " THEN "); 
    ifPart.display();
    System.out.println(""); 
  }

  public String bupdateForm()
  { String res = "IF " + test + " THEN "; 
    res = res + ifPart.bupdateForm();
    return res + "\n"; 
  }

  public BStatement bupdateForm(java.util.Map env, boolean local)
  { BExpression btest = test.binvariantForm(env,local); 
    BStatement bif = ifPart.bupdateForm(env,local); 
    return new BIfStatement(btest,bif); 
  } 

  public void displayImp(String var) 
  { System.out.print("IF " + test + " THEN "); 
    ifPart.displayImp(var); 
    System.out.println(""); 
  }

  public void displayImp(String var, PrintWriter out)
  { out.print("IF " + test + " THEN ");
    ifPart.displayImp(var,out);
    out.println(""); 
  }
 
  public void display(PrintWriter out)
  { out.print("IF " + test + " THEN ");
    ifPart.display(out);
    out.println(""); 
  }

  public void displayJava(String t)
  { System.out.print("if (" + test.toJava() + 
                       ") { "); 
    ifPart.displayJava(t); 
    System.out.println(" }"); 
  }

  public String toStringJava()
  { java.util.Map env = new java.util.HashMap();
    if (entity != null)
    { env.put(entity.getName(),"this"); } 
    String res = "if (" + test.queryForm(env,false) + 
                       ") { "; 
    res = res + ifPart.toStringJava(); 
    return res + " }\n";
  }

  public String toEtl()
  { String res = "if (" + test + ") { "; 
    res = res + ifPart.toEtl(); 
    return res + " }\n";
  }

  public boolean typeCheck(Vector types, Vector entities, Vector cs, Vector env)
  { boolean res1 = test.typeCheck(types,entities,cs,env);
    boolean res2 = ifPart.typeCheck(types,entities,cs,env);
    return res1 && res2; 
  }

  public boolean typeInference(Vector types, Vector entities, Vector cs, Vector env, java.util.Map vartypes)
  { boolean res1 = test.typeInference(types,entities,
                                      cs,env,vartypes);
    // Must be boolean

    boolean res2 = ifPart.typeInference(types,entities,
                                        cs,env,vartypes);
    return res1 && res2; 
  }


  public void displayJava(String t, PrintWriter out)
  { out.print("if (" + test.toJava() + 
                       ") { "); 
    ifPart.displayJava(t, out); 
    out.println(" }"); 
  }

  public String updateForm(java.util.Map env, boolean local, Vector types, Vector entities, 
                           Vector vars)
  { // if (entity != null)
    // { env.put(entity.getName(),"this"); } 

    if ("true".equals("" + test))
    { return ifPart.updateForm(env,local,types,entities,vars); } 

    String res = "if (" + test.queryForm(env,false) + 
                       ") { "; 
    res = res + ifPart.updateForm(env,local,types,entities,vars); 
    return res + " }\n";
  }  // (env,local) in both places. 

  public String updateFormJava6(java.util.Map env, boolean local)
  { // if (entity != null)
    // { env.put(entity.getName(),"this"); } 

    if ("true".equals("" + test))
    { return ifPart.updateFormJava6(env,local); } 

    String res = "if (" + test.queryFormJava6(env,false) + 
                       ") { "; 
    res = res + ifPart.updateFormJava6(env,local); 
    return res + " }\n";
  }

  public String updateFormJava7(java.util.Map env, boolean local)
  { // if (entity != null)
    // { env.put(entity.getName(),"this"); } 

    if ("true".equals("" + test))
    { return ifPart.updateFormJava7(env,local); } 

    String res = "if (" + test.queryFormJava7(env,false) + 
                       ") { "; 
    res = res + ifPart.updateFormJava7(env,local); 
    return res + " }\n";
  }


  public String updateFormCSharp(java.util.Map env, boolean local)
  { // if (entity != null)
    // { env.put(entity.getName(),"this"); } 

    if ("true".equals("" + test))
    { return ifPart.updateFormCSharp(env,local); } 

    String res = "if (" + test.queryFormCSharp(env,false) + 
                       ") { "; 
    res = res + ifPart.updateFormCSharp(env,local); 
    return res + " }\n";
  }

  public String updateFormCPP(java.util.Map env, boolean local)
  { // if (entity != null)
    // { env.put(entity.getName(),"this"); } 

    if ("true".equals("" + test))
    { return ifPart.updateFormCPP(env,local); } 

    String res = "if (" + test.queryFormCPP(env,false) + 
                       ") { "; 
    res = res + ifPart.updateFormCPP(env,local); 
    return res + " }\n";
  }

  public Vector allPreTerms()
  { Vector res1 = test.allPreTerms(); 
    return VectorUtil.union(res1,ifPart.allPreTerms()); 
  }  

  public Vector allPreTerms(String var)
  { Vector res1 = test.allPreTerms(var); 
    return VectorUtil.union(res1,ifPart.allPreTerms(var)); 
  }  

  public Vector readFrame()
  { Vector res = new Vector();
    res.addAll(test.allReadFrame()); 
    res.addAll(ifPart.readFrame()); 
    return res;  
  }  

  public Vector writeFrame()
  { Vector res = new Vector();
    res.addAll(ifPart.writeFrame()); 
    return res;  
  }  

  public IfCase replaceModuleReferences(UseCase uc) 
  { Expression e = test.replaceModuleReferences(uc); 
    Statement stat = ifPart.replaceModuleReferences(uc);
    IfCase res = new IfCase(e,stat);
    res.setEntity(entity); 
    return res; 
  } 

  public int syntacticComplexity()
  { int res = test.syntacticComplexity();
    if (res > TestParameters.syntacticComplexityLimit)
    { System.err.println("!!! Code smell (MEL): too high expression complexity (" + res + ") for " + test); 
      System.err.println(">>> Recommend OCL refactoring");  
    } 
 
    res = res + ifPart.syntacticComplexity();
    return res + 1; 
  }

  public int cyclomaticComplexity()
  { int res = test.cyclomaticComplexity(); 
    res = res + ifPart.cyclomaticComplexity();
    return res; 
  }

  public int epl()
  { int res = 0; 
    res = res + ifPart.epl();
    return res; 
  }

  public Vector allOperationsUsedIn()
  { Vector res = new Vector();
    res.addAll(test.allOperationsUsedIn()); 
    res.addAll(ifPart.allOperationsUsedIn()); 
    return res;  
  }  

  public Vector allAttributesUsedIn()
  { Vector res = new Vector();
    res.addAll(test.allAttributesUsedIn()); 
    res.addAll(ifPart.allAttributesUsedIn()); 
    return res;  
  }  

  public Vector getUses(String var)
  { Vector res = new Vector();
    res.addAll(test.getUses(var)); 
    res.addAll(ifPart.getUses(var)); 
    return res;  
  }  

  public Vector getVariableUses()
  { Vector res = new Vector();
    res.addAll(test.getVariableUses()); 
    res.addAll(ifPart.getVariableUses()); 
    return res;  
  }  

  public Vector getVariableUses(Vector unused)
  { Vector res = new Vector();
    res.addAll(test.getVariableUses()); 
    Vector ifuses = ifPart.getVariableUses(unused); 
    res.addAll(ifuses); 
    return res;  
  }  

  public Vector equivalentsUsedIn()
  { Vector res = new Vector();
    res.addAll(test.equivalentsUsedIn()); 
    res.addAll(ifPart.equivalentsUsedIn()); 
    return res;  
  }  

  public Vector metavariables()
  { Vector res = test.metavariables();
    res.addAll(ifPart.metavariables());  
    return res;  
  }  
} 


class ConditionalStatement extends Statement
{ Expression test;
  Statement ifPart;
  Statement elsePart;

  ConditionalStatement(Expression e, Statement s)
  { test = e;
    ifPart = s;
    elsePart = null;
  }

  ConditionalStatement(Expression e, Statement s1, Statement s2)
  { test = e;
    ifPart = s1;
    elsePart = s2;
  }

  ConditionalStatement(Expression e, Vector ss1, Vector ss2)
  { test = e;

    if (ss1.size() == 0)
    { ifPart = new InvocationStatement("skip"); } 
    else if (ss1.size() == 1) 
    { ifPart = (Statement) ss1.get(0); } 
    else 
    { ifPart = new SequenceStatement(ss1); } 

    if (ss2.size() == 0)
    { elsePart = new InvocationStatement("skip"); } 
    else if (ss2.size() == 1) 
    { elsePart = (Statement) ss2.get(0); } 
    else 
    { elsePart = new SequenceStatement(ss2); } 
  }

  public void setElse(Statement stat) 
  { elsePart = stat; } 

  public String getOperator() 
  { return "if"; } 

  public Expression getTest()
  { return test; } 

  public void setTest(Expression tst)
  { test = tst; } 

  public Statement ifPart()
  { return ifPart; } 

  public Statement elsePart()
  { return elsePart; } 

  public Statement getIf()
  { return ifPart; } 

  public Statement getElse()
  { return elsePart; } 

  public boolean hasSkipElse()
  { if (elsePart == null) 
    { return true; } 
    return elsePart.isSkip(); 
  } 

  public void setIfPart(Statement st)
  { ifPart = st; } 

  public void setIf(Statement st)
  { ifPart = st; } 

  public void setElsePart(Statement st)
  { elsePart = st; } 

  public static void addToIfBranch(Statement st, Statement sx)
  { if (sx == null) 
    { return; } 

    if (st instanceof ConditionalStatement)
    { ConditionalStatement cs = (ConditionalStatement) st; 
      Statement ifp = cs.ifPart(); 
      if (ifp instanceof SequenceStatement)
      { ((SequenceStatement) ifp).addStatement(sx); } 
      else 
      { SequenceStatement ss = new SequenceStatement(); 
        ss.addStatement(ifp); 
        ss.addStatement(sx); 
        cs.ifPart = ss; 
      } 
    } 
  } 

  public static Statement mergeConditionals(Expression tst, 
                            Statement stat)
  { if (stat instanceof ConditionalStatement)
    { ConditionalStatement cs = (ConditionalStatement) stat; 
      Expression newexpr = 
        new BinaryExpression("or", tst, cs.test); 
      cs.test = newexpr; 
      return cs; 
    } 

    Statement els = new InvocationStatement("skip"); 
    return new ConditionalStatement(tst,stat,els); 
  } 

  public int execute(ModelSpecification sigma, ModelState beta)
  { Expression tval = test.evaluate(sigma, beta); 
    if ("true".equals(tval + ""))
    { int res = ifPart.execute(sigma, beta); 
      return res; 
    } 
    else 
    { int res = elsePart.execute(sigma, beta); 
      return res; 
    } 
  } // assuming no side-effects in the test. 

  public String cg(CGSpec cgs)
  { String etext = this + "";
    Vector args = new Vector();
	
    if ("true".equals(test + ""))
    { return ifPart.cg(cgs); }

    if ("false".equals(test + "") && 
        elsePart != null)
    { return elsePart.cg(cgs); }
	
    args.add(test.cg(cgs));
    args.add(ifPart.cg(cgs));
    if (elsePart == null) 
    { elsePart = new SequenceStatement(); } 
    args.add(elsePart.cg(cgs));

    CGRule r = cgs.matchedStatementRule(this,etext);
    if (r != null)
    { return r.applyRule(args); }
    return etext;
  }

  public Vector cgparameters()
  { Vector args = new Vector();
	
    if ("true".equals(test + ""))
    { args.add(ifPart);
      return args;
    }

    if ("false".equals(test + "") && 
        elsePart != null)
    { args.add(elsePart);
      return args;
    }
	
    args.add(test);
    args.add(ifPart);
    if (elsePart == null) 
    { elsePart = new SequenceStatement(); } 
    args.add(elsePart);

    return args;
  }

  public Expression definedness()
  { Expression testd = test.definedness(); 
    Expression ifdef = ifPart.definedness(); 
    Expression res = 
      Expression.simplify("&", testd, ifdef, null); 
    if (elsePart != null) 
    { res = 
        Expression.simplify("&", res, 
                            elsePart.definedness(), null); 
    }
    return res; 
  } 


  public Object clone()
  { Expression testc = (Expression) test.clone(); 
    Statement ifc = (Statement) ifPart.clone(); 
    Statement elsec = null; 
    if (elsePart != null) 
    { elsec = (Statement) elsePart.clone(); }
    return new ConditionalStatement(testc, ifc, elsec); 
  }  

  public Statement optimiseOCL()
  { Expression testc = test.simplifyOCL(); 
    Statement ifc = ifPart.optimiseOCL(); 

    Statement elsec = null; 
    if (elsePart != null) 
    { elsec = elsePart.optimiseOCL(); }

    testc.setBrackets(false); 

    if ("true".equals(testc + "")) 
    { return ifc; } 

    if ("false".equals(testc + "")) 
    { if (elsec == null) 
      { return new InvocationStatement("skip"); } 
      return elsec; 
    } 

    Statement elseStat = 
       Statement.getFirstStatement(elsePart); 
 
    // if st->includes(x) then skip else (st := st->including(x))
    // is just st := st->including(x) for set st

    if (testc instanceof BinaryExpression && 
        Statement.hasSingleStatement(elsePart) &&
        ifPart.isSkip())
    { BinaryExpression testbe = (BinaryExpression) testc;

      Expression testbeLeft = testbe.getLeft(); // st
      Expression testbeRight = testbe.getRight(); // x

      testbeLeft.setBrackets(false);  
      testbeRight.setBrackets(false);  
 
      if (Statement.isAdditionToCollection(elseStat, 
                        testbeRight, testbeLeft) && 
          "->includes".equals(testbe.getOperator()) && 
          testbeLeft.hasSetType())
      { System.out.println("! Removing redundant test for set addition: " + this); 
        return elseStat; 
      } // valid for Set and SortedSet, not OrderedSet 
    } 

    Statement ifStat = Statement.getFirstStatement(ifPart); 

    // if st->excludes(x) then st := st->including(x) else skip
    // is just st := st->including(x) for set st

    if (testc instanceof BinaryExpression && 
        Statement.hasSingleStatement(ifPart) &&
        (elsePart == null || elsePart.isSkip()))
    { BinaryExpression testbe = (BinaryExpression) testc;
    
      Expression testbeLeft = testbe.getLeft(); // st
      Expression testbeRight = testbe.getRight(); // x

      testbeLeft.setBrackets(false);  
      testbeRight.setBrackets(false);  
 
      if (Statement.isAdditionToCollection(ifStat, 
                        testbeRight, testbeLeft) &&
          "->excludes".equals(testbe.getOperator()) && 
          testbeLeft.hasSetType())
      { System.out.println("! Removing redundant test for set addition: " + this); 
        return ifStat; 
      } // valid for Set and SortedSet, not OrderedSet
    } 

    if (elsec != null && elsec.isSkip()) { } 
    else if (Statement.endsWithControlFlowBreak(ifc))
    { Statement skipstat = new InvocationStatement("skip");
 
      SequenceStatement ss = new SequenceStatement(); 
      ss.addStatement(
           new ConditionalStatement(testc, ifc, skipstat)); 
      ss.addStatement(elsec);

      System.out.println(">> Promoting nested statements from else branch: " + elsec); 
 
      return ss; 
    } 

    return new ConditionalStatement(testc, ifc, elsec); 
  }  

  public java.util.Map collectionOperatorUses(int lev, 
                          java.util.Map uses, 
                          Vector vars)

  { test.collectionOperatorUses(lev, uses, vars); 
    ifPart.collectionOperatorUses(lev, uses, vars);
    elsePart.collectionOperatorUses(lev, uses, vars);
    return uses; 
  } 

 
  public Map energyUse(Map uses, 
                                Vector rUses, Vector oUses)
  { test.energyUse(uses, rUses, oUses); 
    ifPart.energyUse(uses, rUses, oUses);

    int res = test.syntacticComplexity();

    if (res > TestParameters.syntacticComplexityLimit)
    { int acount = (int) uses.get("amber"); 
      uses.set("amber", acount + 1); 
      oUses.add("! Code smell (MEL): too high expression complexity (" + res + ") for " + test + "\n" +  
                ">>> Recommend OCL refactoring");  
    } 

    if (elsePart != null) 
    { elsePart.energyUse(uses, rUses, oUses); } 

    Statement elseStat = 
                     Statement.getFirstStatement(elsePart); 
    Statement ifStat = Statement.getFirstStatement(ifPart); 
 
    // JOptionPane.showInputDialog("Code: " + elseStat + " " + test); 

    Expression testSimplified = test; 
    if (test instanceof UnaryExpression && 
        "not".equals(((UnaryExpression) test).getOperator()))
    { testSimplified = 
          Expression.negate(
             ((UnaryExpression) test).getArgument()); 
    } 

    if (testSimplified instanceof BinaryExpression)
    { BinaryExpression testbe = 
          (BinaryExpression) testSimplified;

      Expression testbeLeft = testbe.getLeft(); // st 
      testbeLeft.setBrackets(false); 
      Expression testbeRight = testbe.getRight(); // x
      testbeRight.setBrackets(false); 

      if ("->includes".equals(testbe.getOperator()) && 
          Statement.hasSingleStatement(elsePart) &&
          ifPart.isSkip() &&                  
          Statement.isAdditionToCollection(
                         elseStat, testbeRight, testbeLeft)) 
      { // adds to testbeLeft only if not in there already

        if (testbeLeft.hasSequenceType())
        { rUses.add("!! Possibly using sequence " + testbeLeft + " as set in: " + this + 
               "\n>> Recommend declaring " + testbeLeft + " as a Set or SortedSet"); 

          int rscore = (int) uses.get("red"); 
          uses.set("red", rscore + 1); 
        } 
        else if (testbeLeft.hasSetType())
        { oUses.add("! Redundant test on set addition " + testbeLeft + " in: " + this); 

          int oscore = (int) uses.get("amber"); 
          uses.set("amber", oscore + 1); 
        } 
      } 
      else if ("->includes".equals(testbe.getOperator()) && 
          Statement.isControlFlowEnd(ifPart) &&                  
          (elsePart.isSkip() || 
           Statement.isAdditionToCollection(
                         elseStat, testbeRight, testbeLeft))) 
      { // adds to testbeLeft only if not in there already

        if (testbeLeft.hasSequenceType())
        { oUses.add("!! Possibly using sequence " + testbeLeft + " as set in: " + this + 
               "\n>> Recommend declaring " + testbeLeft + " as a Set or SortedSet"); 

          int ascore = (int) uses.get("amber"); 
          uses.set("amber", ascore + 1); 
        } 
      } 
      else if ("->excludes".equals(testbe.getOperator()) &&
               Statement.hasSingleStatement(ifPart) &&
               (elsePart == null || elsePart.isSkip() ||
                Statement.isControlFlowEnd(elsePart)) &&                  
               Statement.isAdditionToCollection(
                         ifStat, testbeRight, testbeLeft)) 
      { // adds to testbeLeft only if not in there already

        if (testbeLeft.hasSequenceType())
        { oUses.add("!! Possibly using sequence " + testbeLeft + " as set in: " + this + 
             "\n>> Recommend declaring " + testbeLeft + " as a Set or SortedSet"); 

          int ascore = (int) uses.get("amber"); 
          uses.set("amber", ascore + 1); 
        } 
        else if (testbeLeft.hasSetType())
        { oUses.add("! Redundant test on set addition " + testbeLeft + " in: " + this); 

          int oscore = (int) uses.get("amber"); 
          uses.set("amber", oscore + 1); 
        } 
      } 
    } 

    return uses; 
  } // if s->includes(x) then skip else s := s->including(x)

  public void findClones(java.util.Map clones, String rule, String op)
  { if (test.syntacticComplexity() >= UCDArea.CLONE_LIMIT)
    { /* String val = test + ""; 
      Vector used = (Vector) clones.get(val);
      if (used == null)  
      { used = new Vector(); }
      if (rule != null)
      { used.add(rule); }
      else if (op != null)
      { used.add(op); }
      clones.put(val,used); */ 
      test.findClones(clones,rule,op); 
    } 
    ifPart.findClones(clones,rule,op); 
    if (elsePart != null) 
    { elsePart.findClones(clones,rule,op); } 
  }

  public void findClones(java.util.Map clones, 
                         java.util.Map cdefs, 
                         String rule, String op)
  { if (test.syntacticComplexity() >= UCDArea.CLONE_LIMIT)
    { test.findClones(clones,cdefs,rule,op); } 
    ifPart.findClones(clones,cdefs,rule,op); 
    if (elsePart != null) 
    { elsePart.findClones(clones,cdefs,rule,op); } 
  }

  public void findMagicNumbers(java.util.Map mgns, String rule, String op)
  { test.findMagicNumbers(mgns,"" + this,op); 
    ifPart.findMagicNumbers(mgns,rule,op); 
    if (elsePart != null) 
    { elsePart.findMagicNumbers(mgns,rule,op); } 
  }

  public Vector allVariableNames()
  { Vector res = test.allVariableNames(); 
    res = VectorUtil.union(res, ifPart.allVariableNames()); 
    if (elsePart != null) 
    { res = VectorUtil.union(res, 
                             elsePart.allVariableNames()); 
    }
    return res; 
  } 

  public Statement generateDesign(java.util.Map env, boolean local)
  { Statement ifc = ifPart.generateDesign(env,local);
    if ("true".equals(test + ""))
    { return ifc; } 
    Statement elsec = null; 
    if (elsePart != null) 
    { elsec = elsePart.generateDesign(env,local); }
    return new ConditionalStatement(test, ifc, elsec); 
  }  

  public String toString()
  { String res = "if " + test + " then " + ifPart;

    if (elsePart == null || "skip".equals(elsePart + "")) 
    { res = res + " else skip "; } 
    else 
    { res = res + " else ( " + elsePart + " )"; }

    return res;
  }

  public String toAST()
  { String res = "(OclStatement if " + test.toAST() + " then " + ifPart.toAST() + " ";

    if (elsePart == null || "skip".equals(elsePart + "")) 
    { res = res + " else (OclStatement skip) )"; } 
    else 
    { res = res + " else " + elsePart.toAST() + " )"; }

    // if (brackets)
    // { res = "(OclStatement ( " + res + " ) )"; } 

    return res;
  }

  public boolean containsSubexpression(Expression expr)
  { if (test.containsSubexpression(expr))
    { return true; } 

    if (ifPart != null &&
        ifPart.containsSubexpression(expr))
    { return true; } 

    if (elsePart != null &&
        elsePart.containsSubexpression(expr))
    { return true; } 
 
    return false; 
  } 

  public Vector singleMutants()
  { Vector res = new Vector(); 
    
    Vector exprs = test.singleMutants(); 
    for (int i = 0; i < exprs.size(); i++) 
    { Expression mut = (Expression) exprs.get(i); 
      ConditionalStatement ifclone = (ConditionalStatement) clone(); 
      ifclone.setTest(mut); 
      res.add(ifclone); 
    } 

    if (ifPart == null) 
    { return res; } 

    Vector ifmuts = ifPart.singleMutants(); 
    for (int i = 0; i < ifmuts.size(); i++) 
    { Statement mut = (Statement) ifmuts.get(i); 
      ConditionalStatement ifclone = (ConditionalStatement) clone(); 
      ifclone.setIfPart(mut); 
      res.add(ifclone); 
    } 
    
    if (elsePart == null) 
    { return res; } 

    Vector elsemuts = elsePart.singleMutants(); 
    for (int i = 0; i < elsemuts.size(); i++) 
    { Statement mut = (Statement) elsemuts.get(i); 
      ConditionalStatement ifclone = (ConditionalStatement) clone(); 
      ifclone.setElsePart(mut); 
      res.add(ifclone); 
    } 

    return res;
  } 

  public String toStringJava()
  { String res = "if (" + test + ") { " + ifPart + " } ";
    if (elsePart != null)
    { res = res + " else { " + elsePart + " }"; }
    return res;
  }

  public String toEtl()
  { String res = "  if (" + test + ") { " + ifPart.toEtl() + " }\n";
    if (elsePart != null)
    { res = res + "  else { " + elsePart.toEtl() + " }"; }
    return res;
  }

  public void display(java.io.PrintWriter out)
  { String res = "if " + test + " then " + ifPart;
    if (elsePart != null)
    { res = res + " else " + elsePart; }
    out.println(res);
  }

  public void display()
  { String res = "if " + test + " then " + ifPart;
    if (elsePart != null)
    { res = res + " else " + elsePart; }
    System.out.println(res);
  }

  public void displayJava(String v, java.io.PrintWriter out)
  { out.println("    if (" + test + ")"); 
    out.println("    { " + ifPart + " }");
    if (elsePart != null)
    { out.println("    else "); 
      out.println("    { " + elsePart + " }"); 
    }
  }

  public void displayJava(String v)
  { String res = "if (" + test + ") { " + ifPart + " }";
    if (elsePart != null)
    { res = res + " else { " + elsePart + " }"; }
    System.out.println(res);
  }

  public String saveModelData(PrintWriter out)
  { String res = Identifier.nextIdentifier("conditionalstatement_");
    out.println(res + " : ConditionalStatement");
    out.println(res + ".statId = \"" + res + "\"");
    String testid = test.saveModelData(out);
    out.println(res + ".test = " + testid);
    String ifpartid = ifPart.saveModelData(out);
    out.println(res + ".ifPart = " + ifpartid);
    if (elsePart != null)
    { String elsepartid = elsePart.saveModelData(out);
      out.println(elsepartid + " : " + res + ".elsePart");
    }
    return res;
  }

  public String saveModelData(PrintWriter out, Entity ent)
  { String res = Identifier.nextIdentifier("conditionalstatement_");
    out.println(res + " : ConditionalStatement");
    out.println(res + ".statId = \"" + res + "\"");
    String testid = test.saveModelData(out);
    out.println(res + ".test = " + testid);
    String ifpartid = ifPart.saveModelData(out, ent);
    out.println(res + ".ifPart = " + ifpartid);
    if (elsePart != null)
    { String elsepartid = elsePart.saveModelData(out, ent);
      out.println(elsepartid + " : " + res + ".elsePart");
    }
    return res;
  }

  public Statement dereference(BasicExpression v)
  { Expression testc = test.dereference(v); 
    Statement ifc = ifPart.dereference(v); 
    Statement elsec = null; 
    if (elsePart != null) 
    { elsec = elsePart.dereference(v); }
    return new ConditionalStatement(testc, ifc, elsec); 
  }  

  public Statement addContainerReference(BasicExpression ref,
                                         String var,
                                         Vector excl)
  { Expression testc = test.addContainerReference(ref,var,excl); 
    Statement ifc = ifPart.addContainerReference(ref,var,excl); 
    Statement elsec = null; 
    if (elsePart != null) 
    { elsec = elsePart.addContainerReference(ref,var,excl); }
    return new ConditionalStatement(testc, ifc, elsec); 
  }  

  public Statement substituteEq(String oldE, Expression newE)
  { Expression testc = test.substituteEq(oldE, newE); 
    Statement ifc = ifPart.substituteEq(oldE, newE); 
    Statement elsec = null; 
    if (elsePart != null) 
    { elsec = elsePart.substituteEq(oldE, newE); }
    return new ConditionalStatement(testc, ifc, elsec); 
  }  

  public Statement removeSlicedParameters(BehaviouralFeature bf, Vector fpars)
  { Expression testc = test.removeSlicedParameters(bf,fpars); 
    Statement ifc = ifPart.removeSlicedParameters(bf,fpars); 
    Statement elsec = null; 
    if (elsePart != null) 
    { elsec = elsePart.removeSlicedParameters(bf,fpars); }
    return new ConditionalStatement(testc, ifc, elsec); 
  }  
 


  public boolean typeCheck(Vector types, Vector entities, Vector cs, Vector env)
  { boolean res = test.typeCheck(types,entities,cs,env); 
    res = ifPart.typeCheck(types,entities,cs,env);
    if (elsePart != null) 
    { res = elsePart.typeCheck(types, entities, cs, env); } 
    return res; 
  }

  public boolean typeInference(Vector types, Vector entities, Vector cs, Vector env, java.util.Map vartypes)
  { boolean res = test.typeInference(types,entities,
                                     cs,env,vartypes); 
    res = ifPart.typeInference(types,entities,cs,env,vartypes);
    if (elsePart != null) 
    { res = elsePart.typeInference(types, entities, 
                               cs, env,vartypes); 
    } 
    return res; 
  }

  public Expression wpc(Expression post)
  { Expression ifwpc = ifPart.wpc(post); 
    ifwpc.setBrackets(true); 

    Expression ifimpl = 
      Expression.simplifyImp(test, ifwpc);

    if (elsePart != null) 
    { Expression ntest = 
        Expression.negate(test); 
      Expression elsewpc = elsePart.wpc(post); 
      elsewpc.setBrackets(true);  
      Expression elseimpl = 
        Expression.simplifyImp(ntest, elsewpc);
 
      return Expression.simplify("&", ifimpl, elseimpl, null); 
    }

    return ifimpl; 
  }  

  public Expression wpc(Expression inv, Expression post)
  { Expression ifwpc = ifPart.wpc(inv, post); 
    ifwpc.setBrackets(true); 

    Expression ifimpl = 
      Expression.simplifyImp(test, ifwpc);

    if (elsePart != null) 
    { Expression ntest = 
        Expression.negate(test); 
      Expression elsewpc = elsePart.wpc(inv, post); 
      elsewpc.setBrackets(true);  
      Expression elseimpl = 
        Expression.simplifyImp(ntest, elsewpc);
 
      return Expression.simplify("&", ifimpl, elseimpl, null); 
    }

    return ifimpl; 
  }  

  public Vector dataDependents(Vector allvars, Vector vars)
  { if (ifPart.updates(vars))
    { } 
    else
    { if (elsePart == null) 
      { return vars; } 
      else if (elsePart.updates(vars)) { } 
      else 
      { return vars; } 
    } // Don't include testvars unless if/else update vars

    Vector vars1 = ifPart.dataDependents(allvars, vars); 
    Vector testvars = new Vector(); 
    testvars.addAll(test.getVariableUses()); 
    testvars = VectorUtil.union(testvars,
                                test.allAttributesUsedIn()); 
    if (elsePart != null) 
    { vars1 = VectorUtil.union(vars1, 
                 elsePart.dataDependents(allvars,vars)); 
    }
 
    vars1 = VectorUtil.union(vars1, testvars); 
    // System.out.println(vars + " --from--> " + vars1); 
    return vars1; 
  }  

  public Vector dataDependents(Vector allvars, Vector vars, Map mp, Map dlin)
  { 
    Map mp1 = new Map();
    Vector vars1 = ifPart.dataDependents(allvars, vars, mp1, dlin); 
    Vector testvars = new Vector(); 
    testvars.addAll(test.getVariableUses()); 
    testvars = VectorUtil.union(testvars,
                                test.allAttributesUsedIn());
    Vector range1 = mp1.range(); 
    mp.unionWith(mp1); 

    if (elsePart != null) 
    { Map mp2 = new Map(); 
      Vector vars2 = elsePart.dataDependents(allvars,vars,mp2,dlin); 
      vars1 = VectorUtil.union(vars1,vars2);  
      Vector range2 = mp2.range(); 
      range1.addAll(range2); // updated in either branch
      mp.unionWith(mp2); 
    }
 
    vars1 = VectorUtil.union(vars1, testvars); 
    // System.out.println(vars + " --from--> " + vars1);

    for (int i = 0; i < range1.size(); i++) 
    { String vv = "" + range1.get(i); 
      for (int j = 0; j < testvars.size(); j++) 
      { String rv = "" + testvars.get(j); 
        mp.add_pair(rv, vv); 
      } 
    } // rv --> vv
 
    return vars1; 
  }  

  public boolean updates(Vector v) 
  { if (ifPart.updates(v))
    { return true; }
    else if (elsePart != null && elsePart.updates(v))
    { return true; } 
    return false; 
  }  


  public String updateForm(java.util.Map env, boolean local, Vector types,
                           Vector entities, Vector vars)
  { if ("true".equals(test + ""))
    { return "    { " + ifPart.updateForm(env,local,types,entities,vars) + " }\n"; } 
	
	String res = "    if (" + test.queryForm(env,local) + ")\n";
    res = res +  "    { " + ifPart.updateForm(env,local,types,entities,vars) + " }\n";
    if (elsePart != null)
    { res = res + "    else { " + elsePart.updateForm(env,local,types,entities,vars) + " }\n"; }
    return res;
  } 

  public String updateFormJava6(java.util.Map env, boolean local)
  { if ("true".equals(test + ""))
    { return "    { " + ifPart.updateFormJava6(env,local) + " }\n"; } 
	
	String res = "if (" + test.queryFormJava6(env,local) + ")\n";
    res = res + "{ " + ifPart.updateFormJava6(env,local) + " }\n";
    if (elsePart != null)
    { res = res + "else { " + elsePart.updateFormJava6(env,local) + " }\n"; }
    return res;
  } 

  public String updateFormJava7(java.util.Map env, boolean local)
  { if ("true".equals(test + ""))
    { return "    { " + ifPart.updateFormJava7(env,local) + " }\n"; } 
	
	String res = "if (" + test.queryFormJava7(env,local) + ")\n";
    res = res + "{ " + ifPart.updateFormJava7(env,local) + " }\n";
    if (elsePart != null)
    { res = res + "else { " + elsePart.updateFormJava7(env,local) + " }\n"; }
    return res;
  } 

  public String updateFormCSharp(java.util.Map env, boolean local)
  { if ("true".equals(test + ""))
    { return "    { " + ifPart.updateFormCSharp(env,local) + " }\n"; } 
	
	String res = "if (" + test.queryFormCSharp(env,local) + ")\n";
    res = res + "{ " + ifPart.updateFormCSharp(env,local) + " }\n";
    if (elsePart != null)
    { res = res + "else { " + elsePart.updateFormCSharp(env,local) + " }\n"; }
    return res;
  } 

  public String updateFormCPP(java.util.Map env, boolean local)
  { if ("true".equals(test + ""))
    { return "    { " + ifPart.updateFormCPP(env,local) + " }\n"; } 
	
	String res = "if (" + test.queryFormCPP(env,local) + ")\n";
    res = res + "{ " + ifPart.updateFormCPP(env,local) + " }\n";
    if (elsePart != null)
    { res = res + "else { " + elsePart.updateFormCPP(env,local) + " }\n"; }
    return res;
  } 

  public BStatement bupdateForm(java.util.Map env, boolean local)
  { BExpression cond = test.binvariantForm(env,local); 
    BStatement ifstat = ifPart.bupdateForm(env,local);
    if (elsePart != null)
    { return new BIfStatement(cond,ifstat,
                     elsePart.bupdateForm(env,local)); 
    } 
    else 
    { return new BIfStatement(cond,ifstat); } 
  } 

  public String bupdateForm()
  { BExpression cond = test.bqueryForm(); 
    String ifstat = ifPart.bupdateForm();
    if (elsePart != null)
    { return "IF " + cond + " THEN " + ifstat + " ELSE " + elsePart.bupdateForm() + " END"; } 
    else 
    { return "IF " + cond + " THEN " + ifstat + " END"; } 
  } 

  public Vector allPreTerms()
  { Vector res = new Vector();
    res.addAll(test.allPreTerms()); 
    res.addAll(ifPart.allPreTerms()); 
    if (elsePart != null) 
    { res.addAll(elsePart.allPreTerms()); }  
    return res;  
  }  

  public Vector allPreTerms(String var)
  { Vector res = new Vector();
    res.addAll(test.allPreTerms(var)); 
    res.addAll(ifPart.allPreTerms(var)); 
    if (elsePart != null) 
    { res.addAll(elsePart.allPreTerms(var)); }  
    return res;  
  }  

  public Vector readFrame()
  { Vector res = new Vector();
    res.addAll(test.allReadFrame()); 
    res.addAll(ifPart.readFrame()); 
    if (elsePart != null) 
    { res.addAll(elsePart.readFrame()); } 
    return res;  
  }  

  public Vector writeFrame()
  { Vector res = new Vector();
    res.addAll(ifPart.writeFrame()); 
    if (elsePart != null) 
    { res.addAll(elsePart.writeFrame()); } 
    return res;  
  }  

  public Statement checkConversions(Entity e, Type propType, Type propElemType, 
                                    java.util.Map interp)
  { Statement ifc = ifPart.checkConversions(e,propType,propElemType,interp); 
    Statement elsec = null; 
    if (elsePart != null) 
    { elsec = elsePart.checkConversions(e,propType,propElemType,interp); }
    return new ConditionalStatement(test, ifc, elsec); 
  }  

  public Statement replaceModuleReferences(UseCase uc)
  { Statement ifc = ifPart.replaceModuleReferences(uc);
    Expression tt = test.replaceModuleReferences(uc);  
    Statement elsec = null; 
    if (elsePart != null) 
    { elsec = elsePart.replaceModuleReferences(uc); }
    return new ConditionalStatement(tt, ifc, elsec); 
  }  


  public int syntacticComplexity()
  { int res = test.syntacticComplexity();

    res = res + ifPart.syntacticComplexity(); 
    if (elsePart != null)
    { res = res + elsePart.syntacticComplexity(); }
    return res + 1;
  }

  public int cyclomaticComplexity()
  { int res = test.cyclomaticComplexity(); 
    res = res + ifPart.cyclomaticComplexity();
    if (elsePart != null) 
    { res = res + elsePart.cyclomaticComplexity(); } 
    return res; 
  }

  public int epl()
  { int res = 0; 
    res = res + ifPart.epl();
    if (elsePart != null) 
    { res = res + elsePart.epl(); } 
    return res; 
  }

  public Vector allOperationsUsedIn()
  { Vector res = new Vector();
    res.addAll(test.allOperationsUsedIn()); 
    res.addAll(ifPart.allOperationsUsedIn()); 
    if (elsePart != null) 
    { res.addAll(elsePart.allOperationsUsedIn()); } 
    return res;  
  }  

  public Vector allAttributesUsedIn()
  { Vector res = new Vector();
    res.addAll(test.allAttributesUsedIn()); 
    res.addAll(ifPart.allAttributesUsedIn()); 
    if (elsePart != null) 
    { res.addAll(elsePart.allAttributesUsedIn()); } 
    return res;  
  }  

  public Vector getUses(String var)
  { Vector res = new Vector();
    res.addAll(test.getUses(var)); 
    res.addAll(ifPart.getUses(var)); 
    if (elsePart != null) 
    { res.addAll(elsePart.getUses(var)); } 
    return res;  
  }  

  public Vector getVariableUses()
  { Vector res = new Vector();
    res.addAll(test.getVariableUses()); 
    res.addAll(ifPart.getVariableUses()); 
    if (elsePart != null) 
    { res.addAll(elsePart.getVariableUses()); } 
    return res;  
  }  

  public Vector getVariableUses(Vector unused)
  { Vector res = new Vector();
    res.addAll(test.getVariableUses()); 
    Vector ifuses = ifPart.getVariableUses(unused); 
    res.addAll(ifuses); 
    if (elsePart != null) 
    { Vector elseuses = elsePart.getVariableUses(unused); 
      res.addAll(elseuses); 
    } 
    return res;  
  }  

  public Vector equivalentsUsedIn()
  { Vector res = new Vector();
    res.addAll(test.equivalentsUsedIn()); 
    res.addAll(ifPart.equivalentsUsedIn()); 
    if (elsePart != null) 
    { res.addAll(elsePart.equivalentsUsedIn()); } 
    return res;  
  }  

  public Vector metavariables()
  { Vector res = test.metavariables();
    res.addAll(ifPart.metavariables());  
    if (elsePart != null) 
    { res.addAll(elsePart.metavariables()); }   
    return res;  
  }  
}


class FinalStatement extends Statement
{ Statement body;

  FinalStatement(Statement s)
  { body = s; }

  FinalStatement(Vector stats)
  { if (stats.size() == 0)
    { body = new InvocationStatement("skip"); } 
    else if (stats.size() == 1)
    { body = (Statement) stats.get(0); } 
    else 
    { body = new SequenceStatement(stats); }  
  } 

  public String getOperator() 
  { return "finally"; } 

  public String cg(CGSpec cgs)
  { String etext = this + "";
    Vector args = new Vector();
	
    args.add(body.cg(cgs));
    
    CGRule r = cgs.matchedStatementRule(this,etext);
    if (r != null)
    { return r.applyRule(args); }
    return etext;
  }

  public Vector cgparameters()
  { 
    Vector args = new Vector();
	
    args.add(body);
    return args;
  }

  public Vector cgterms()
  { 
    Vector args = new Vector();
    args.add("finally"); 
    args.add(body);
    return args;
  }

  public Object clone()
  { Statement ifc = (Statement) body.clone(); 
    return new FinalStatement(ifc); 
  }  

  public Statement optimiseOCL()
  { Statement ifc = body.optimiseOCL(); 
    return new FinalStatement(ifc); 
  }  

  public void findClones(java.util.Map clones, String rule, String op)
  { body.findClones(clones,rule,op); } 

  public void findClones(java.util.Map clones, 
                         java.util.Map cdefs,
                         String rule, String op)
  { body.findClones(clones,cdefs,rule,op); } 

  public Map energyUse(Map uses, Vector rUses, Vector aUses)
  { body.energyUse(uses, rUses, aUses);  
    return uses; 
  } 

  public java.util.Map collectionOperatorUses(int lev, 
                                    java.util.Map uses, 
                                    Vector vars)
  { body.collectionOperatorUses(lev, uses, vars); 
    return uses; 
  } 

  public void findMagicNumbers(java.util.Map mgns, String rule, String op)
  { body.findMagicNumbers(mgns,this + "",op); } 


  public Statement generateDesign(java.util.Map env, boolean local)
  { return this; }  

  public String toString()
  { String res = "    finally ( " + body + " )";
    return res;
  }

  public String toAST()
  { String res = "(OclStatement finally " + body.toAST() + " )";

    // if (brackets)
    // { res = "(OclStatement ( " + res + " ) )"; } 

    return res;
  }

  public boolean containsSubexpression(Expression expr)
  { if (body != null) 
    { return body.containsSubexpression(expr); } 

    return false; 
  } 

  public Vector singleMutants()
  { Vector res = new Vector(); 

    if (body == null) 
    { return res; } 

    Vector jb = body.singleMutants();
    for (int i = 0; i < jb.size(); i++) 
    { Statement st = (Statement) jb.get(i); 
      res.add(new FinalStatement(st)); 
    } 
    return res; 
  }

  public String toStringJava()
  { String jb = body.toStringJava();
    return "    finally " + jb; 
  }

  public String toEtl()
  { return toStringJava(); }

  public void display(java.io.PrintWriter out)
  { out.println(toString()); }

  public void display()
  { System.out.println(toString()); }

  public void displayJava(String v, java.io.PrintWriter out)
  { System.out.println(toStringJava()); }

  public void displayJava(String v)
  { System.out.println(toStringJava()); }

  public String saveModelData(PrintWriter out)
  { String res = Identifier.nextIdentifier("finalstatement_");
    out.println(res + " : FinalStatement");
    out.println(res + ".statId = \"" + res + "\"");
    String bodyid = body.saveModelData(out);
    out.println(res + ".body = " + bodyid);
    return res;
  }

  public String saveModelData(PrintWriter out, Entity ent)
  { String res = Identifier.nextIdentifier("finalstatement_");
    out.println(res + " : FinalStatement");
    out.println(res + ".statId = \"" + res + "\"");
    String bodyid = body.saveModelData(out, ent);
    out.println(res + ".body = " + bodyid);
    return res;
  }

  public Statement dereference(BasicExpression v)
  { Statement bodyc = body.dereference(v); 
    return new FinalStatement(bodyc); 
  }  

  public Statement addContainerReference(BasicExpression ref,
                                         String var,
                                         Vector excl)
  { Statement bodyc = 
       body.addContainerReference(ref,var,excl); 
    return new FinalStatement(bodyc); 
  }  

  public Statement substituteEq(String oldE, Expression newE)
  { Statement ifc = body.substituteEq(oldE, newE); 
    return new FinalStatement(ifc); 
  }  

  public Statement removeSlicedParameters(BehaviouralFeature bf, Vector fpars)
  { Statement ifc = body.removeSlicedParameters(bf,fpars); 
    return new FinalStatement(ifc);
  } 

  public boolean typeCheck(Vector types, Vector entities, Vector cs, Vector env)
  { boolean res = body.typeCheck(types,entities,cs,env);
    return res; 
  }

  public boolean typeInference(Vector types, Vector entities, Vector cs, Vector env, java.util.Map vartypes)
  { boolean res = body.typeInference(types,entities,
                                     cs,env,vartypes);
    return res; 
  }

  public Expression wpc(Expression post)
  { return body.wpc(post); }  

  public Expression wpc(Expression inv, Expression post)
  { return body.wpc(inv, post); }  

  public Vector dataDependents(Vector allvars, Vector vars)
  { return vars; }  

  public Vector dataDependents(Vector allvars, Vector vars, Map mp, Map dlin)
  { return vars; }  

  public boolean updates(Vector v) 
  { return body.updates(v); }  


  public String updateForm(java.util.Map env, boolean local, Vector types,
                           Vector entities, Vector vars)
  { String bup = body.updateForm(env,local,types,entities,vars);  
    return "    finally " + bup; 	
  } 

  public String updateFormJava6(java.util.Map env, boolean local)
  { String bup = body.updateFormJava6(env,local);  
    return "    finally " + bup; 	
  } 

  public String updateFormJava7(java.util.Map env, boolean local)
  { String bup = body.updateFormJava7(env,local);  
    return "    finally { " + bup + " }\n";
  } 

  public String updateFormCSharp(java.util.Map env, boolean local)
  { String bup = body.updateFormCSharp(env,local);  
    return "    finally { " + bup + " }\n";
  } 

  public String updateFormCPP(java.util.Map env, boolean local)
  { String bup = body.updateFormCPP(env,local);  
    return "    catch(...) { " + bup + " }\n";
  } 

  public BStatement bupdateForm(java.util.Map env, boolean local)
  { BStatement ifstat = body.bupdateForm(env,local);
    return ifstat; 
  } 

  public String bupdateForm()
  { String ifstat = body.bupdateForm();
    return ifstat; 
  } 

  public Vector allPreTerms()
  { return body.allPreTerms(); }  

  public Vector allPreTerms(String var)
  { return body.allPreTerms(var); }  

  public Vector readFrame()
  { return body.readFrame(); }  

  public Vector writeFrame()
  { return body.writeFrame(); }  

  public Statement checkConversions(Entity e, Type propType, Type propElemType, 
                                    java.util.Map interp)
  { Statement ifc = body.checkConversions(e,propType,propElemType,interp); 
    return ifc; 
  }  

  public Statement replaceModuleReferences(UseCase uc)
  { Statement ifc = body.replaceModuleReferences(uc);
    return ifc; 
  }  


  public int syntacticComplexity()
  { int res = body.syntacticComplexity(); 
    return res + 1;
  }

  public int cyclomaticComplexity()
  { int res = body.cyclomaticComplexity();
    return res; 
  }

  public int epl()
  { int res = body.epl();
    return res; 
  }

  public Vector allOperationsUsedIn()
  { Vector res = new Vector();
    res.addAll(body.allOperationsUsedIn()); 
    return res;  
  }  

  public Vector allAttributesUsedIn()
  { Vector res = new Vector();
    res.addAll(body.allAttributesUsedIn()); 
    return res;  
  }  

  public Vector getUses(String var)
  { Vector res = new Vector();
    res.addAll(body.getUses(var)); 
    return res;  
  }  

  public Vector getVariableUses()
  { Vector res = new Vector();
    res.addAll(body.getVariableUses()); 
    return res;  
  }  

  public Vector getVariableUses(Vector unused)
  { Vector res = new Vector();
    Vector bodyuses = body.getVariableUses(unused); 
    res.addAll(bodyuses); 
    return res;  
  }  

  public Vector equivalentsUsedIn()
  { Vector res = new Vector();
    res.addAll(body.equivalentsUsedIn()); 
    return res;  
  }  

  public Vector metavariables()
  { Vector res = new Vector();
    res.addAll(body.metavariables());  
    return res;  
  }  
}

