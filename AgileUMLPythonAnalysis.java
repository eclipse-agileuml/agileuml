import java.io.*;


public class AgileUMLPythonAnalysis
{ public static void main(String[] args)
  { UmlTool window = new UmlTool();
    UCDArea area = window.getArea(); 
  
    area.loadFromPython("code.py");
    area.typeCheck();    
    area.energyAnalysisHTML();
  } 
} 
 
      