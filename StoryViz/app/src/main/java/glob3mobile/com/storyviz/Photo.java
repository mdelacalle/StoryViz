package glob3mobile.com.storyviz;

import org.glob3.mobile.generated.Geodetic3D;

import java.util.Calendar;

/**
 * Created by mdelacalle on 26/10/2016.
 */

public class Photo {
    String path = "";
    Geodetic3D position;
    Calendar date;

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Geodetic3D getPosition() {
        return position;
    }

    public void setPosition(Geodetic3D position) {
        this.position = position;
    }

    public Calendar getDate() {
        return date;
    }

    public void setDate(Calendar date) {
        this.date = date;
    }
}
