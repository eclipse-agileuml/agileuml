import java.io.*;


public class AgileUML
{ public static void main(String[] args)
  { UmlTool window = new UmlTool();
    UCDArea area = window.getArea(); 
  
    // read mm.km3 and perform energy analysis

    File sourcefile = new File("mm.km3"); 
    if (sourcefile.exists())
    { area.loadKM3FromFile(sourcefile); }
    else 
    { System.err.println("!! ERROR: no file mm.km3"); 
      return; 
    } 
  
    area.typeCheck(); 
    area.energyAnalysisHTML();
  } 
} 
 
      