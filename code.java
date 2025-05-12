public class EquilibriumIndexFinder {
   static int findEquilibriumIndex(int[] array, int length) {
      int totalSum = 0;  
      int leftSum = 0;   
      for (int i = 0; i < length; ++i) 
         totalSum += array[i];
      for (int i = 0; i < length; ++i) {
         totalSum -= array[i];  
         if (leftSum == totalSum) 
            return i;
         leftSum += array[i];  
      }
      return -1;
   }

   public static void main(String[] args) {
      int[] array = {1, 3, 5, 2, 2};  
      int result = findEquilibriumIndex(array, array.length);
      if (result != -1)
         System.out.println("Equilibrium index found at: " + result);
      else
         System.out.println("No equilibrium index found.");
   }
}
