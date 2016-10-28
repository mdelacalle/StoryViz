package glob3mobile.com.storyviz;

import java.util.ArrayList;

/**
 * Created by mdelacalle on 26/10/2016.
 */
public class Session {
    private static Session ourInstance = new Session();

    public static ArrayList<Photo> getSessionPhotos() {
        return sessionPhotos;
    }

    public static void setSessionPhotos(ArrayList<Photo> sessionPhotos) {
        Session.sessionPhotos = sessionPhotos;
    }

    public static Session getOurInstance() {
        return ourInstance;
    }

    public static void setOurInstance(Session ourInstance) {
        Session.ourInstance = ourInstance;
    }

    private static ArrayList<Photo> sessionPhotos;

    public static Session getInstance() {
        return ourInstance;
    }

    private Session() {
    }



}
