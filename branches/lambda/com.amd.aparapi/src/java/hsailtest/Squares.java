package hsailtest;

import com.amd.aparapi.*;


public class Squares{


   void run(float[] in, float[] out, int gid){
      out[gid] = in[gid] * in[gid];
   }


   public static void main(String[] args) throws AparapiException{
      ClassModel classModel = ClassModel.getClassModel(Squares.class);
      ClassModel.ClassModelMethod method = classModel.getMethod("run", "([F[FI)V");
      method.getInstructions();

      OkraRunner runner = new OkraRunner();
      float in[] = new float[100];
      float out[] = new float[in.length];
      for (int i=0; i< in.length; i++){
         in[i]=i;

         out[i]=0f;
      }
      RegISARenderer renderer = new RegISARenderer();
      renderer.setShowLineNumbers(false);
      renderer.setShowComments(true);
      new RegISA(method).render(renderer);
      System.out.println(renderer.toString());

      Squares s = new Squares();
      runner.run(renderer.toString(), in.length, s, in, out, in.length);
      for (int i=0; i< in.length; i++){
         System.out.println(i+" "+in[i]+" "+out[i]);

      }

         //System.out.println(InstructionHelper.getLabel(i,true, false, false));



   }
}
