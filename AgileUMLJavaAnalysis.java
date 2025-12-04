import java.io.*;


public class AgileUMLJavaAnalysis
{ public static void main(String[] args)
  { UmlTool window = new UmlTool();
    UCDArea area = window.getArea(); 
  
    // read output/ast.txt and perform energy analysis

    area.loadFromJavaAST("code.java");
    area.typeCheck();    
    area.energyAnalysisHTML();
  } 
} 
 
      