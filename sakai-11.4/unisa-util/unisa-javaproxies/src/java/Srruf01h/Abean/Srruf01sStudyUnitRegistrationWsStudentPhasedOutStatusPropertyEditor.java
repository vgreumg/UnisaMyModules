package Srruf01h.Abean;
 
import java.beans.*;
 
public class Srruf01sStudyUnitRegistrationWsStudentPhasedOutStatusPropertyEditor extends PropertyEditorSupport {
 
   public String[] getTags() {
      String values[] = {
         "0",
         "3",
         "1",
         "2",
         "4",
         "0",
      };
      return values;
   }

   public boolean checkTag(String s) {
      String values[] = getTags();
      for(int n=0; n< values.length; n++) {
         if (s.compareTo(values[n]) == 0) {
            return true;
         }
      }
      return false;
   }
}
