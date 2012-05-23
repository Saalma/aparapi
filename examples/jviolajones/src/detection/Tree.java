package detection;

/**
This project is based on the open source jviolajones project created by Simon
Houllier and is used with his permission. Simon's jviolajones project offers 
a pure Java implementation of the Viola-Jones algorithm.

http://en.wikipedia.org/wiki/Viola%E2%80%93Jones_object_detection_framework

The original Java source code for jviolajones can be found here
http://code.google.com/p/jviolajones/ and is subject to the
gnu lesser public license  http://www.gnu.org/licenses/lgpl.html

Many thanks to Simon for his excellent project and for permission to use it 
as the basis of an Aparapi example.
**/

import java.util.ArrayList;
import java.util.List;

public class Tree{
   final static int LEFT = 0;

   final static int RIGHT = 1;

   List<Feature> features;

   public Tree() {

      features = new ArrayList<Feature>();

   }

   public void addFeature(Feature f) {

      features.add(f);

   }

   public float getVal(int[] grayImage, int[] squares, int width, int height, int i, int j, float scale) {

      Feature cur_node = features.get(0);
      while (true) {
         int where = cur_node.getLeftOrRight(grayImage, squares, width, height, i, j, scale);
         if (where == LEFT) {
            if (cur_node.has_left_val) {
               //System.out.println("LEFT");

               return cur_node.left_val;
            } else {
               // System.out.println("REDIRECTION !");
               //System.exit(0);
               cur_node = features.get(cur_node.left_node);
            }
         } else {
            if (cur_node.has_right_val) {

               //  System.out.println("RIGHT");

               return cur_node.right_val;
            } else {
               //  System.out.println("REDIRECTION !");
               //System.exit(0);
               cur_node = features.get(cur_node.right_node);
            }
         }
      }

   }

}