package Sfrrf03h.Abean;
 
import java.beans.*;
 
public class Sfrrf03sMntOnlineCcPaymentsBundleStatusPropertyEditor extends PropertyEditorSupport {
 
   public String[] getTags() {
      String values[] = {
         "RC",
         "RR",
         "AC",
         "NB",
         "CM",
         "ER",
         "BE",
         "RJ",
         "BL",
         "BC",
         "AP",
         "BA",
         "  ",
      };
      return values;
   }

   public boolean checkTag(String s) {
      String values[] = getTags();
      StringBuffer temp = new StringBuffer(s);
      while (temp.length() < 2)
         temp.append(' ');
      s = temp.toString();
      for(int n=0; n< values.length; n++) {
         if (s.compareTo(values[n]) == 0) {
            return true;
         }
      }
      return false;
   }
}
