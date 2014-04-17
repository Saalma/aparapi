package hsailtest;

import com.amd.aparapi.Aparapi;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class AparapiParallelCountJUnit{

   @Test
   public void test(){

      int evenCount = Aparapi.range(0, 12).parallel().count(i -> i%2==0);

      System.out.println("evenCount="+evenCount);
      assertTrue("evenCount==6", evenCount == 6);

   }


}