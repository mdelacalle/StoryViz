package glob3mobile.com.storyviz;

import java.util.Comparator;

/**
 * Created by mdelacalle on 27/10/2016.
 */

public class PhotoDateComparator implements Comparator<Photo> {

    @Override
    public int compare(Photo p1, Photo p2) {

         if(p1.getDate().before(p2.getDate())){
             return -1;
         }else{
             return 1;
         }

    }
}
