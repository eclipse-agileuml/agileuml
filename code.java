class Test {  
  public static long factorial(int x)
  { if (x <= 1) { return 1; } 

    return x*factorial(x-1); 
  } 

  public void test() {
    double result = factorial(25) + factorial(25);
  }

}
