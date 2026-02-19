import java.util.*;
import java.util.HashMap;
import java.util.Collection;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import java.util.TreeSet;
import java.util.Collections;

class Matrix {

  private double[] elements; 
  private int size = 0; 
  private ArrayList<Integer> shape = new ArrayList<Integer>(); 
  private ArrayList<Integer> factors = new ArrayList<Integer>(); 

  public Matrix(ArrayList<Integer> sh)
  { this.shape = sh; 

    int prd = 1; 
    for (int i = 0; i < shape.size(); i++) 
    { int dim = shape.get(i); 
      prd = prd*dim; 
    } 

    this.elements = new double[prd]; 
    this.size = prd; 

    for (int i = 0; i < prd; i++) 
    { elements[i] = 0.0; } 

    this.factors.add(prd); 
    for (int i = 1; i <= shape.size(); i++) 
    { prd = prd/shape.get(i-1); 
      this.factors.add(prd); 
    } 
  } 
  
  public Matrix reshape(ArrayList<Integer> sh)  
  { this.shape = sh; 
    this.factors = new ArrayList<Integer>(); 

    int prd = 1; 
    for (int i = 0; i < shape.size(); i++) 
    { int dim = shape.get(i); 
      prd = prd*dim; 
    } 

    this.factors.add(prd); 
    for (int i = 1; i <= shape.size(); i++) 
    { prd = prd/shape.get(i-1); 
      this.factors.add(prd); 
    } 
	
	return this; 
  } 


  public static Matrix singleValueMatrix(ArrayList<Integer> sh, double value)  
  { Matrix res = new Matrix(sh); 
    for (int ind = 0; ind < res.size; ind++)  
    { res.elements[ind] = value; } 

    return res; 
  } 



  public int size()
  { return this.size; } 

  public ArrayList<Integer> shape()
  { return this.shape; } 

  public void set(ArrayList<Integer> indexes, double value)
  { int position = 0; 
    for (int i = 0; i < indexes.size()-1; i++) 
    { position = position + indexes.get(i)*factors.get(i+1); }
    position = position + indexes.get(indexes.size()-1);  
    elements[position] = value; 
  } 

  public void set(int[] indexes, double value)
  { int position = 0; 
    for (int i = 0; i < indexes.length-1; i++) 
    { position = position + indexes[i]*factors.get(i+1); }
    position = position + indexes[indexes.length-1];  
    elements[position] = value; 
  } 

  public double get(ArrayList<Integer> indexes)
  { int position = 0; 
    for (int i = 0; i < indexes.size()-1; i++) 
    { position = position + indexes.get(i)*factors.get(i+1); }
    position = position + indexes.get(indexes.size()-1);  
    return elements[position]; 
  } 

  public double get(int[] indexes)
  { int position = 0; 
    for (int i = 0; i < indexes.length-1; i++) 
    { position = position + indexes[i]*factors.get(i+1); }
    position = position + indexes[indexes.length-1];  
    return elements[position]; 
  } 
  
  private double multrowcolumn(int i, int j)
  { double res = 0.0; 
    int dim = factors.get(1); 
    for (int k = 0; k < dim; k++) 
    { res = res + elements[i*dim + k]*elements[j + k*dim]; }
    return res; 
  }

  private double multrowcolumn(Matrix m, int i, int j)
  { double res = 0.0; 
    int dim = factors.get(1); // size of each row 
    for (int k = 0; k < dim; k++) 
    { res = res + elements[i*dim + k] * m.elements[j + k*dim]; }
	return res; 
  }
  
  public Matrix square()
  { int dim = factors.get(1); 
    ArrayList<Integer> ss = new ArrayList<Integer>(); 
    ss.add(dim); ss.add(dim); 
    Matrix sq = new Matrix(ss); 
    for (int i = 0; i < dim; i++) 
      for (int j = 0; j < dim; j++) 
      { sq.set(new int[]{i,j}, this.multrowcolumn(i,j)); } 
    return sq; 
  } 

  public Matrix multiply(Matrix m)
  { int nrows = factors.get(1); 
    int ncols = m.shape.get(1); 
    ArrayList<Integer> ss = new ArrayList<Integer>(); 
    ss.add(nrows); ss.add(ncols); 
    Matrix sq = new Matrix(ss); 
    for (int i = 0; i < nrows; i++) 
      for (int j = 0; j < ncols; j++) 
      { sq.set(new int[]{i,j}, this.multrowcolumn(m,i,j)); } 
    return sq; 
  } 

  public Matrix add(Matrix m)
  { Matrix sum = new Matrix(this.shape); 
    for (int i = 0; i < size; i++) 
    { sum.elements[i] = 
            this.elements[i] + m.elements[i]; 
    } 
    return sum; 
  } 
  
  public void display()
  { int nrows = factors.get(1); 
    int ncols = shape.get(1); 
    for (int i = 0; i < nrows; i++) 
    { for (int j = 0; j < ncols; j++) 
      { System.out.print(this.get(new int[]{i,j}) + " "); }
      System.out.println(); 
    }
  }

  public static void main(String[] args)
  { 
  /*  ArrayList<Integer> ss = new ArrayList<Integer>(); 
  
    int sze = 80; 
	
    ss.add(sze); ss.add(sze);   
    Matrix mm = new Matrix(ss); 
    
    for (int i = 0; i < sze; i++) 
      for (int j = 0; j < sze; j++)
      { mm.set(new int[]{i,j}, i+j); }

    
	java.util.Date d1 = new java.util.Date(); 
	long t1 = d1.getTime(); 
	
	Matrix sq = mm.square(); 
	
	java.util.Date d2 = new java.util.Date(); 
	long t2 = d2.getTime(); 
	System.out.println(t2 - t1); */ 

    int sze = 80; 

	java.util.Date d1 = new java.util.Date(); 
	long t1 = d1.getTime(); 

    ArrayList<Integer> sh = new ArrayList<Integer>(); 
	sh.add(sze); sh.add(sze); sh.add(sze); 
    Matrix m1 = Matrix.singleValueMatrix(sh, 10.0); 
	
	ArrayList<Integer> rsh = new ArrayList<Integer>(); 
	rsh.add(sze/2); rsh.add(2); rsh.add(sze/2); rsh.add(2); rsh.add(sze); 
    
    Matrix m2 = m1.reshape(rsh); 

	java.util.Date d2 = new java.util.Date(); 
	long t2 = d2.getTime(); 
	System.out.println(t2 - t1); 
	
    /* ArrayList<Integer> sh = new ArrayList<Integer>(); 
     sh.add(2); sh.add(2); 

     Matrix m1 = new Matrix(sh); 
     Matrix m2 = new Matrix(sh); 

     m1.set(new int[]{0,0}, 1); 
     m1.set(new int[]{0,1}, 2); 
     m1.set(new int[]{1,0}, 3); 
     m1.set(new int[]{1,1}, 4); 

     m2.set(new int[]{0,0}, 5); 
     m2.set(new int[]{0,1}, 6); 
     m2.set(new int[]{1,0}, 7); 
     m2.set(new int[]{1,1}, 8); 

     Matrix mm = m1.multiply(m2); 
     mm.display(); 

     Matrix nn = m1.add(m2); 
     nn.display(); */ 
	
  } 
}


  /* static double Log(double y) {
    double x = (y-1)/(y+1); 
    double x2 = x*x; 
    double result = x;
        
    for (int i = 1; i <= 200; i++) {
      int j = 2*i + 1;
      x = x*x2;
      result = result + (1.0/j)*x;
    }
        
    return 2*result;
  } */ 
