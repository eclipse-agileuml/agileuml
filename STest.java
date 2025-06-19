import java.util.Stack; 

public class STest 
{ public static void main(String[] args)
  { Stack st = new Stack(); 
    st.push("aa"); 
    st.push("bb"); 
    st.push("cc"); 

    String pp = (String) st.peek(); 

    int x = st.search("bb");
 
    pp = (String) st.pop();
 
    boolean bb = st.empty();
  }  
}
