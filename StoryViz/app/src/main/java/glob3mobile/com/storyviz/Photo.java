package glob3mobile.com.storyviz;

import org.glob3.mobile.generated.Geodetic3D;

import java.util.Calendar;

/**
 * Created by mdelacalle on 26/10/2016.
 * Photo Object
 */

class Photo {
    private String path = "";
    private Geodetic3D position;
    private Calendar date;

     int getExifOrientation() {
        return exifOrientation;
    }

     void setExifOrientation(int exifOrientation) {
        this.exifOrientation = exifOrientation;
    }

    private int exifOrientation = 0;

     String getPath() {
        return path;
    }

     void setPath(String path) {
        this.path = path;
    }

     Geodetic3D getPosition() {
        return position;
    }

     void setPosition(Geodetic3D position) {
        this.position = position;
    }

     Calendar getDate() {
        return date;
    }

     void setDate(Calendar date) {
        this.date = date;
    }
}
