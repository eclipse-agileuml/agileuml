import java.util.HashMap;
import java.util.Vector;  

/******************************
* Copyright (c) 2003--2025 Kevin Lano
* This program and the accompanying materials are made available under the
* terms of the Eclipse Public License 2.0 which is available at
* http://www.eclipse.org/legal/epl-2.0
*
* SPDX-License-Identifier: EPL-2.0
* *****************************/
/* Package: Model-based Testing */ 

public class TestParameters
{ public static int maxInteger = 1024;
  public static int maxint = 2147483647;
 
  public static int minInteger = -1025;
  public static int minint = -2147483648; 
 
  public static long maxlong = 9223372036854775807L; 
  public static long minlong = -9223372036854775808L; 

  public static int maxLong = 100000;
  public static int minLong = -999999;
  
  public static double maxFloat = 3125.0891; 
    // Double.MAX_VALUE

  public static double minFloat = -655.276; 
    // -Double.MAX_VALUE

  /* Settings for flaw/technical debt detection */ 

  public static int syntacticComplexityLimit = 12; /* for MEL */ 

  public static int cloneSizeLimit = 7;            /* for DC  */
  public static int energyCloneSizeLimit = 4; 

  public static int statementNestingLimit = 5;  
  public static int cyclomaticComplexityLimit = 10; 
  public static int referenceChainLimit = 3; 
  public static int nestedTypeLimit = 3; 

  public static int superclassesLimit = 4; 
  public static int inheritanceChainLimit = 3; 
  public static int numberOfDataFeaturesLimit = 20; 
  public static int numberOfOperationsLimit = 15; 
  public static int numberOfParametersLimit = 8; 

  public static int operationSizeLimit = 100; 
  public static int operationSizeWarning = 50; 

  public static int classSizeLimit = 1000; 

  public static int efoLimit = 6; 
  public static int efiLimit = 6; 

  public static 
    HashMap<String, HashMap<String,Double>> compComplexity; 

  public static HashMap<String, String> compExplanation; 

  static {
    compExplanation = new HashMap<String,String>(); 
    compExplanation.put("1.0", "Constant: O(1)"); 
    compExplanation.put("1.5", "Log: O(log n)"); 
    compExplanation.put("2.0", "Linear: O(n)"); 
    compExplanation.put("3.0", "Log-Linear: O(n log n)"); 
    compExplanation.put("4.0", "Quadratic: O(n*n)"); 
    compExplanation.put("100.0", "unknown"); 

    compComplexity = new HashMap<String, HashMap<String, Double>>(); 
	
    HashMap<String,Double> sequenceCC = 
                 new HashMap<String,Double>(); 

    sequenceCC.put("->includes", 2.0);   // O(n) 
    sequenceCC.put(":", 2.0);   // O(n) 
    sequenceCC.put("->excludes", 2.0);   // O(n) 
    sequenceCC.put("/:", 2.0);   // O(n) 

    sequenceCC.put("->at", 1.0);         // O(1)
    sequenceCC.put("->including", 1.5);  // O(log n) 
    sequenceCC.put("->excluding", 2.0); 
    sequenceCC.put("->excludingFirst", 2.0); 
    sequenceCC.put("->indexOf", 2.0);
    sequenceCC.put("->lastIndexOf", 2.0); 
    sequenceCC.put("->count", 2.0); 
    sequenceCC.put("->size", 1.0); 

    sequenceCC.put("->max", 2.0); 
    sequenceCC.put("->min", 2.0); 
    sequenceCC.put("->first", 1.0); 
    sequenceCC.put("->last", 1.0);
    sequenceCC.put("->append", 1.5); 
    sequenceCC.put("->prepend", 2.0);
 
    sequenceCC.put("->union", 2.0); 
    sequenceCC.put("<:", 2.0); 
    sequenceCC.put("->intersection", 4.0);  // O(n*n) 
    sequenceCC.put("-", 4.0); 

    sequenceCC.put("->includesAll", 4.0); 
    sequenceCC.put("->excludesAll", 4.0); 

    sequenceCC.put("=", 2.0); // same elements in same order

    sequenceCC.put("->select", 2.0); 
    sequenceCC.put("->collect", 2.0);   
    sequenceCC.put("->reject", 2.0); 
    sequenceCC.put("->isUnique", 2.0); 
    sequenceCC.put("->any", 2.0); 
    sequenceCC.put("|", 2.0); 
    sequenceCC.put("|C", 2.0);   
    sequenceCC.put("|R", 2.0);
    sequenceCC.put("|A", 2.0);
    sequenceCC.put("|isUnique", 2.0); 
    sequenceCC.put("->selectMinimals", 2.0); 
    sequenceCC.put("|selectMinimals", 2.0); 
    sequenceCC.put("->selectMaximals", 2.0); 
    sequenceCC.put("|selectMaximals", 2.0); 
 

    sequenceCC.put("->forAll", 2.0); 
    sequenceCC.put("->exists", 2.0);   
    sequenceCC.put("->exists1", 2.0); 
    sequenceCC.put("!", 2.0); 
    sequenceCC.put("#", 2.0);   
    sequenceCC.put("#1", 2.0); 

    sequenceCC.put("->front", 2.0); 
    sequenceCC.put("->tail", 2.0); 
    sequenceCC.put("->reverse", 2.0); 
    sequenceCC.put("->copy", 2.0); 

    sequenceCC.put("->sum", 2.0); 
    sequenceCC.put("->prd", 2.0); 
    sequenceCC.put("->sort", 3.0);     // O(n log n) 
    sequenceCC.put("->sortedBy", 3.0);     // O(n log n) 
    sequenceCC.put("|sortedBy", 3.0);     // O(n log n) 
    sequenceCC.put("->asSet", 2.0); 
     
    compComplexity.put("Sequence", sequenceCC); 

    HashMap<String,Double> sortedsequenceCC = 
                 new HashMap<String,Double>(); 

    sortedsequenceCC.put("->includes", 1.5);     // O(log n) 
    sortedsequenceCC.put(":", 2.0);    
    sortedsequenceCC.put("->excludes", 1.5);     // O(log n) 
    sortedsequenceCC.put("/:", 1.5);     // O(log n) 
    
    sortedsequenceCC.put("->at", 1.0);           // O(1)
    sortedsequenceCC.put("->including", 2.0);    // O(n) 
    sortedsequenceCC.put("->excluding", 2.0); 
    sortedsequenceCC.put("->excludingFirst", 2.0); 
    sortedsequenceCC.put("->indexOf", 2.0);
    sortedsequenceCC.put("->lastIndexOf", 2.0); 
    sortedsequenceCC.put("->count", 2.0); 
    sortedsequenceCC.put("->max", 1.0); 
    sortedsequenceCC.put("->min", 1.0); 
    sortedsequenceCC.put("->first", 1.0); 
    sortedsequenceCC.put("->last", 1.0);

    sortedsequenceCC.put("->sum", 2.0); 
    sortedsequenceCC.put("->prd", 2.0); 
    sortedsequenceCC.put("->append", 1.5); 
    sortedsequenceCC.put("->prepend", 2.0);
 
    sortedsequenceCC.put("->union", 4.0); 
    sortedsequenceCC.put("<:", 4.0); 
    sortedsequenceCC.put("->intersection", 4.0);  // O(n*n) 
    sortedsequenceCC.put("-", 4.0); 

    sortedsequenceCC.put("->includesAll", 3.0); 
    sortedsequenceCC.put("->excludesAll", 3.0); 

    sortedsequenceCC.put("=", 2.0); 

    sortedsequenceCC.put("->select", 2.0);
      // Assemble result using an ordinary sequence, because
      // we know it is sorted.  
    sortedsequenceCC.put("->collect", 2.0);   
    sortedsequenceCC.put("->reject", 2.0); 
    sortedsequenceCC.put("->any", 2.0);
    sortedsequenceCC.put("|", 2.0); 
    sortedsequenceCC.put("|C", 2.0);   
    sortedsequenceCC.put("|R", 2.0); 
    sortedsequenceCC.put("->isUnique", 2.0); 
    sortedsequenceCC.put("|isUnique", 2.0); 
    
    sortedsequenceCC.put("->forAll", 2.0); 
    sortedsequenceCC.put("->exists", 2.0);   
    sortedsequenceCC.put("->exists1", 2.0); 
    sortedsequenceCC.put("!", 2.0); 
    sortedsequenceCC.put("#", 2.0);   
    sortedsequenceCC.put("#1", 2.0);
    sortedsequenceCC.put("|A", 2.0);
 
    sortedsequenceCC.put("->selectMinimals", 2.0); 
    sortedsequenceCC.put("|selectMinimals", 2.0); 
    sortedsequenceCC.put("->selectMaximals", 2.0); 
    sortedsequenceCC.put("|selectMaximals", 2.0); 

    sortedsequenceCC.put("->front", 2.0); 
    sortedsequenceCC.put("->tail", 2.0); 
    sortedsequenceCC.put("->reverse", 2.0); 
    sortedsequenceCC.put("->copy", 2.0); 
    
    sortedsequenceCC.put("->sort", 1.0);  
    sortedsequenceCC.put("->sortedBy", 3.0);     // O(n log n) 
    sortedsequenceCC.put("|sortedBy", 3.0);     // O(n log n) 
    sortedsequenceCC.put("->asSet", 2.0); 
     
    compComplexity.put("SortedSequence", sortedsequenceCC); 

    HashMap<String,Double> setCC = 
                 new HashMap<String,Double>(); 

    setCC.put("->includes", 1.0);     // O(1) 
    setCC.put(":", 1.0);     // O(1) 
    setCC.put("->excludes", 1.0);     // O(1) 
    setCC.put("/:", 1.0);     // O(1) 

    setCC.put("->including", 1.0);  
    setCC.put("->excluding", 1.0); 
    setCC.put("->excludingFirst", 1.0); 
    setCC.put("->count", 1.0); 
    setCC.put("->max", 2.0); 
    setCC.put("->min", 2.0); 
    setCC.put("->sum", 2.0); 
    setCC.put("->prd", 2.0); 

    setCC.put("->union", 2.0); 
    setCC.put("<:", 2.0); 
    setCC.put("->intersection", 2.0);  
    setCC.put("-", 2.0); 

    setCC.put("->includesAll", 2.0); 
    setCC.put("->excludesAll", 2.0); 

    setCC.put("->select", 2.0); 
    setCC.put("->collect", 2.0);   
    setCC.put("->reject", 2.0);
    setCC.put("->any", 2.0);
 
    setCC.put("|", 2.0); 
    setCC.put("|C", 2.0);   
    setCC.put("|R", 2.0);
    setCC.put("|A", 2.0);
 
    setCC.put("->selectMinimals", 2.0); 
    setCC.put("|selectMinimals", 2.0); 
    setCC.put("->selectMaximals", 2.0); 
    setCC.put("|selectMaximals", 2.0); 

    setCC.put("->forAll", 2.0); 
    setCC.put("->exists", 2.0);   
    setCC.put("->exists1", 2.0); 
    setCC.put("!", 2.0); 
    setCC.put("#", 2.0);   
    setCC.put("#1", 2.0); 
    setCC.put("->isUnique", 2.0); 
    setCC.put("|isUnique", 2.0); 
    setCC.put("->copy", 2.0); 

    setCC.put("->sort", 3.0);     // O(n log n) 
    setCC.put("->sortedBy", 3.0); 
    setCC.put("|sortedBy", 3.0); 
    setCC.put("->asSet", 1.0); 
     
    compComplexity.put("Set", setCC); 

    HashMap<String,Double> sortedsetCC = 
                 new HashMap<String,Double>(); 

    sortedsetCC.put("->includes", 1.5);     // O(log n) 
    sortedsetCC.put(":", 1.5);     // O(log n) 
    sortedsetCC.put("->excludes", 1.5);     // O(log n) 
    sortedsetCC.put("/:", 1.5);     // O(log n) 
    
    sortedsetCC.put("->including", 1.5);  
    sortedsetCC.put("->excluding", 1.5); 
    sortedsetCC.put("->excludingFirst", 1.5); 
    sortedsetCC.put("->count", 1.5); 
    sortedsetCC.put("->max", 1.5); 
    sortedsetCC.put("->min", 1.5); 
    sortedsetCC.put("->sum", 2.0); 
    sortedsetCC.put("->prd", 2.0); 

    sortedsetCC.put("->union", 3.0); 
    sortedsetCC.put("<:", 3.0); 
    sortedsetCC.put("->intersection", 3.0);  
    sortedsetCC.put("-", 3.0); 

    sortedsetCC.put("->includesAll", 3.0); 
    sortedsetCC.put("->excludesAll", 3.0); 
    sortedsetCC.put("->copy", 2.0); 

    sortedsetCC.put("->select", 3.0); 
    sortedsetCC.put("->collect", 2.0);   
    sortedsetCC.put("->reject", 3.0);
    sortedsetCC.put("->any", 2.0);
 
    sortedsetCC.put("|", 3.0); 
    sortedsetCC.put("|C", 2.0);   
    sortedsetCC.put("|R", 3.0);
    sortedsetCC.put("|A", 2.0);
 
    sortedsetCC.put("->isUnique", 2.0); 
    sortedsetCC.put("|isUnique", 2.0); 
    sortedsetCC.put("->selectMinimals", 3.0); 
    sortedsetCC.put("|selectMinimals", 3.0); 
    sortedsetCC.put("->selectMaximals", 3.0); 
    sortedsetCC.put("|selectMaximals", 3.0); 
    
    sortedsetCC.put("->forAll", 2.0); 
    sortedsetCC.put("->exists", 2.0);   
    sortedsetCC.put("->exists1", 2.0); 
    sortedsetCC.put("!", 2.0); 
    sortedsetCC.put("#", 2.0);   
    sortedsetCC.put("#1", 2.0); 

    sortedsetCC.put("->sort", 1.0);     
    sortedsetCC.put("->sortedBy", 3.0); 
    sortedsetCC.put("|sortedBy", 3.0); 
    sortedsetCC.put("->asSet", 1.0); 
     
    compComplexity.put("SortedSet", sortedsetCC); 

    HashMap<String,Double> mapCC = 
                 new HashMap<String,Double>(); 

    mapCC.put("->includesKey", 1.0);      
    mapCC.put("->excludesKey", 1.0);  
    mapCC.put("->includesValue", 2.0);      
    mapCC.put("->excludesValue", 2.0);  
    mapCC.put("->excludingKey", 1.0);  
    mapCC.put("->excludingValue", 2.0); 
    mapCC.put("->at", 1.0); 
    mapCC.put("->count", 2.0);
    mapCC.put("->keys", 2.0);
    mapCC.put("->values", 2.0);

    mapCC.put("->union", 2.0);
    mapCC.put("<:", 2.0);
    mapCC.put("->intersection", 2.0);
    mapCC.put("-", 2.0);
    mapCC.put("->copy", 2.0); 

    mapCC.put("->sort", 3.0);
    mapCC.put("->restrict", 3.0);
    mapCC.put("->antirestrict", 3.0);
    mapCC.put("->select", 3.0);
    mapCC.put("->reject", 3.0);
    mapCC.put("->collect", 2.0);
    mapCC.put("->select", 2.0);

    mapCC.put("|", 3.0);
    mapCC.put("|R", 3.0);
    mapCC.put("|C", 2.0);
    mapCC.put("|A", 2.0);

    mapCC.put("->forAll", 3.0);
    mapCC.put("->exists", 3.0);
    mapCC.put("->exists1", 3.0);
    mapCC.put("!", 3.0);
    mapCC.put("#", 3.0);
    mapCC.put("#1", 3.0);
   
    compComplexity.put("Map", mapCC); 

    HashMap<String,Double> sortedmapCC = 
                 new HashMap<String,Double>(); 

    sortedmapCC.put("->includesKey", 1.5);      
    sortedmapCC.put("->excludesKey", 1.5);  
    sortedmapCC.put("->includesValue", 2.0);      
    sortedmapCC.put("->excludesValue", 2.0);  
    sortedmapCC.put("->excludingKey", 1.5);  
    sortedmapCC.put("->excludingValue", 2.0); 
    sortedmapCC.put("->at", 1.5); 
    sortedmapCC.put("->count", 2.0);
    sortedmapCC.put("->keys", 2.0);
    sortedmapCC.put("->values", 2.0);

    sortedmapCC.put("->union", 3.0);
    sortedmapCC.put("->intersection", 3.0);
    sortedmapCC.put("-", 3.0);

    sortedmapCC.put("->sort", 1.0);
    sortedmapCC.put("->restrict", 3.0);
    sortedmapCC.put("->antirestrict", 3.0);
    sortedmapCC.put("->copy", 2.0); 

    sortedmapCC.put("->select", 3.0);
    sortedmapCC.put("->reject", 3.0);
    sortedmapCC.put("->collect", 2.0);
    sortedmapCC.put("->any", 2.0);
    sortedmapCC.put("|", 3.0);
    sortedmapCC.put("|R", 3.0);
    sortedmapCC.put("|C", 2.0);

    sortedmapCC.put("->forAll", 3.0);
    sortedmapCC.put("->exists", 3.0);
    sortedmapCC.put("->exists1", 3.0);
    sortedmapCC.put("!", 3.0);
    sortedmapCC.put("#", 3.0);
    sortedmapCC.put("#1", 3.0);
    sortedmapCC.put("|A", 2.0);

    compComplexity.put("SortedMap", sortedmapCC); 
  } 


  public static Vector 
     getOperationsComplexityScore(Vector ops, Vector messes)
  { // for the op : ops find best data structures

    Vector res = new Vector(); 
    if (ops.size() == 0) 
    { return res; } 

    double bestScore = 100.0; 

    java.util.Set<String> datatypes = compComplexity.keySet(); 
    for (String dt : datatypes)
    { HashMap<String,Double> dtscores = 
                  compComplexity.get(dt); 
      double dtscore = 0; 
      boolean applicable = true; 

      for (int i = 0; i < ops.size(); i++) 
      { String op = (String) ops.get(i); 
        if (dtscores.containsKey(op))
        { double score = dtscores.get(op); 
          dtscore = Math.max(dtscore, score); 
        } 
        else 
        { applicable = false; 
          break; 
        } // not applicable datastructure
      }

      if (applicable) 
      { if (dtscore < bestScore) 
        { res.clear(); 
          res.add(dt); 
          bestScore = dtscore; 
        } 
        else if (dtscore == bestScore)
        { res.add(dt); } 
      } 
    }

    messes.add("");  
    messes.add(">> The best data structures for operations " + ops + " are " + res); 
    messes.add(">> With overall " + 
       TestParameters.compExplanation.get("" + bestScore) + 
       " complexity"); 
 
    return res; 
  }         

  public static void main(String[] args)
  { Vector ops = new Vector(); 
    ops.add("->includes"); 
    ops.add("->including"); 
    ops.add("->at"); 
    Vector mess = new Vector(); 
    TestParameters.getOperationsComplexityScore(ops, mess);
    System.out.println(mess);  
  } 
}

