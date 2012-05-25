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

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;

public class HaarCascade{

   private static class Stage{
      final int id;

      final List<Tree> trees = new ArrayList<Tree>();

      final float threshold;

      public Stage(int _id, float _threshold) {
         id = _id;
         threshold = _threshold;
      }

      public Tree addTree(Tree tree) {
         trees.add(tree);
         return (tree);
      }
   }

   private static class Tree{
      final int id;

      final List<Feature> features = new ArrayList<Feature>();

      public Tree(int _id) {
         id = _id;
      }

      public Feature addFeature(Feature feature) {
         features.add(feature);
         return (feature);
      }
   }

   private static class Feature{

      final int id;

      final List<Rect> rects = new ArrayList<Rect>();

      final float threshold;

      final float left_val;

      final float right_val;

      final int left_node;

      final int right_node;

      final Tree tree;

      public Feature(int _id, Tree _tree, float _threshold, float _left_val, int _left_node, float _right_val, int _right_node) {
         id = _id;
         tree = _tree;

         threshold = _threshold;
         left_val = _left_val;
         left_node = _left_node;
         right_val = _right_val;
         right_node = _right_node;
      }

      public Rect add(Rect rect) {
         rects.add(rect);
         return (rect);
      }

   }

   private static class Rect{
      final int id; // we use this to access from global parallel arrays

      final int x1, x2, y1, y2;

      final float weight;

      public Rect(int _id, int _x1, int _x2, int _y1, int _y2, float _weight) {
         id = _id;
         x1 = _x1;
         x2 = _x2;
         y1 = _y1;
         y2 = _y2;
         weight = _weight;
      }
   }

   final static int FEATURE_INTS = 5;

   final static int FEATURE_FLOATS = 3;

   static int[] feature_r1r2r3LnRn;

   static float[] feature_LvRvThres;

   static int feature_ids;

   final static int RECT_INTS = 4;

   final static int RECT_FLOATS = 1;

   static int rect_x1y1x2y2[];

   static float rect_w[];

   static int rect_ids;

   final static int STAGE_INTS = 2;

   final static int STAGE_FLOATS = 1;

   static int stage_ids;

   static int stage_startEnd[];

   static float stage_thresh[];

   final static int TREE_INTS = 2;

   static int tree_ids;

   static int tree_startEnd[];

   /** The list of classifiers that the test image should pass to be considered as an image.*/
   int[] stageIds;

   int width;

   int height;

   /**Factory method. Builds a detector from an XML file.
    * @param filename The XML file (generated by OpenCV) describing the HaarCascade.
    * @return The corresponding detector.
    */
   public static HaarCascade create(String filename) {

      org.jdom.Document document = null;
      SAXBuilder sxb = new SAXBuilder();
      try {
         document = sxb.build(new File(filename));
      } catch (Exception e) {
         e.printStackTrace();
      }

      return new HaarCascade(document);

   }

   /** Detector constructor.
    * Builds, from a XML document (i.e. the result of parsing an XML file, the corresponding Haar cascade.
    * @param document The XML document (parsing of file generated by OpenCV) describing the Haar cascade.
    * 
    * http://code.google.com/p/jjil/wiki/ImplementingHaarCascade
    */

   static class Itr implements Iterable<Element>{
      Element element;

      Itr(Element _element) {
         element = _element;
      }

      @SuppressWarnings("unchecked") @Override public Iterator<Element> iterator() {
         return (element.getChildren("_").iterator());
      }

   }

   /** Detector constructor.
    * Builds, from a XML document (i.e. the result of parsing an XML file, the corresponding Haar cascade.
    * @param document The XML document (parsing of file generated by OpenCV) describing the Haar cascade.
    * 
    * http://code.google.com/p/jjil/wiki/ImplementingHaarCascade
    */
   public HaarCascade(Document document) {
      List<Tree> tree_instances = new ArrayList<Tree>();
      List<Feature> feature_instances = new ArrayList<Feature>();
      List<Rect> rect_instances = new ArrayList<Rect>();
      List<Stage> stage_instances = new ArrayList<Stage>();
      List<Stage> stageList = new LinkedList<Stage>();

      Element racineElement = (Element) document.getRootElement().getChildren().get(0);
      Element sizeElement = racineElement.getChild("size");
      String[] dims = sizeElement.getTextTrim().split(" ");
      width = Integer.parseInt(dims[0]);
      height = Integer.parseInt(dims[1]);
      Element stagesElement = racineElement.getChild("stages");
      for (Element stageElement : new Itr(stagesElement)) {
         Element stageThresholdElement = stageElement.getChild("stage_threshold");
         Stage stage = new Stage(HaarCascade.stage_ids++, Float.parseFloat(stageThresholdElement.getText()));
         stage_instances.add(stage);
         Element treesElement = stageElement.getChild("trees");
         for (Element treeElement : new Itr(treesElement)) {
            Tree tree = new Tree(HaarCascade.tree_ids++);
            tree_instances.add(stage.addTree(tree));
            for (Element featureElement : new Itr(treeElement)) {
               Element leftNodeElement = featureElement.getChild("left_node");
               Element rightNodeElement = featureElement.getChild("right_node");
               Element rightValElement = featureElement.getChild("right_val");
               Element leftValElement = featureElement.getChild("left_val");
               Feature feature = new Feature(HaarCascade.feature_ids++, tree,//
                     Float.parseFloat(featureElement.getChild("threshold").getText()),//
                     (leftValElement != null) ? Float.parseFloat(leftValElement.getText()) : 0f,//
                     (leftNodeElement != null) ? Integer.parseInt(leftNodeElement.getText()) : -1,//
                     (rightValElement != null) ? Float.parseFloat(rightValElement.getText()) : 0f,//
                     (rightNodeElement != null) ? Integer.parseInt(rightNodeElement.getText()) : -1// 
               );
               feature_instances.add(tree.addFeature(feature));
               Element rectsElement = featureElement.getChild("feature").getChild("rects");
               for (Element rectElement : new Itr(rectsElement)) {
                  String[] rectValues = rectElement.getTextTrim().split(" ");
                  Rect rect = new Rect(rect_ids++, Integer.parseInt(rectValues[0]), Integer.parseInt(rectValues[1]),
                        Integer.parseInt(rectValues[2]), Integer.parseInt(rectValues[3]), Float.parseFloat(rectValues[4]));
                  rect_instances.add(feature.add(rect));
               }
            }

         }
         stageList.add(stage);
      }

      // now we take the above generated data structure apart and create a data parallel friendly form. 

      stageIds = new int[stageList.size()];
      for (int i = 0; i < stageIds.length; i++) {
         stageIds[i] = stageList.get(i).id;
      }

      HaarCascade.rect_x1y1x2y2 = new int[HaarCascade.rect_ids * HaarCascade.RECT_INTS];
      HaarCascade.rect_w = new float[HaarCascade.rect_ids * HaarCascade.RECT_FLOATS];
      for (int i = 0; i < HaarCascade.rect_ids; i++) {
         Rect r = rect_instances.get(i);
         HaarCascade.rect_w[i * HaarCascade.RECT_FLOATS + 0] = r.weight;
         HaarCascade.rect_x1y1x2y2[i * HaarCascade.RECT_INTS + 0] = r.x1;
         HaarCascade.rect_x1y1x2y2[i * HaarCascade.RECT_INTS + 1] = r.y1;
         HaarCascade.rect_x1y1x2y2[i * HaarCascade.RECT_INTS + 2] = r.x2;
         HaarCascade.rect_x1y1x2y2[i * HaarCascade.RECT_INTS + 3] = r.y2;
      }

      HaarCascade.feature_r1r2r3LnRn = new int[HaarCascade.feature_ids * HaarCascade.FEATURE_INTS];
      HaarCascade.feature_LvRvThres = new float[HaarCascade.feature_ids * HaarCascade.FEATURE_FLOATS];
      for (int i = 0; i < HaarCascade.feature_ids; i++) {
         Feature f = feature_instances.get(i);
         HaarCascade.feature_LvRvThres[i * HaarCascade.FEATURE_FLOATS + 0] = f.left_val;
         HaarCascade.feature_LvRvThres[i * HaarCascade.FEATURE_FLOATS + 1] = f.right_val;
         HaarCascade.feature_LvRvThres[i * HaarCascade.FEATURE_FLOATS + 2] = f.threshold;
         HaarCascade.feature_r1r2r3LnRn[i * HaarCascade.FEATURE_INTS + 0] = (f.rects.size() > 0) ? f.rects.get(0).id : -1;
         HaarCascade.feature_r1r2r3LnRn[i * HaarCascade.FEATURE_INTS + 1] = (f.rects.size() > 1) ? f.rects.get(1).id : -1;
         HaarCascade.feature_r1r2r3LnRn[i * HaarCascade.FEATURE_INTS + 2] = (f.rects.size() > 2) ? f.rects.get(2).id : -1;
         HaarCascade.feature_r1r2r3LnRn[i * HaarCascade.FEATURE_INTS + 3] = (f.left_node == -1) ? -1 : f.tree.features
               .get(f.left_node).id;
         HaarCascade.feature_r1r2r3LnRn[i * HaarCascade.FEATURE_INTS + 4] = (f.right_node == -1) ? -1 : f.tree.features
               .get(f.right_node).id;
      }

      HaarCascade.tree_startEnd = new int[HaarCascade.tree_ids * HaarCascade.TREE_INTS];

      for (int i = 0; i < HaarCascade.tree_ids; i++) {
         Tree t = tree_instances.get(i);
         HaarCascade.tree_startEnd[i * HaarCascade.TREE_INTS + 0] = t.features.get(0).id;
         HaarCascade.tree_startEnd[i * HaarCascade.TREE_INTS + 1] = t.features.get(t.features.size() - 1).id;
      }

      HaarCascade.stage_startEnd = new int[HaarCascade.stage_ids * HaarCascade.STAGE_INTS];
      HaarCascade.stage_thresh = new float[HaarCascade.stage_ids * HaarCascade.STAGE_FLOATS];
      for (int i = 0; i < HaarCascade.stage_ids; i++) {
         Stage t = stage_instances.get(i);
         HaarCascade.stage_startEnd[i * HaarCascade.STAGE_INTS + 0] = t.trees.get(0).id;
         HaarCascade.stage_startEnd[i * HaarCascade.STAGE_INTS + 1] = t.trees.get(t.trees.size() - 1).id;
         HaarCascade.stage_thresh[i * HaarCascade.STAGE_FLOATS + 0] = t.threshold;
      }
   }

}
