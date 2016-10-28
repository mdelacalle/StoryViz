package glob3mobile.com.storyviz;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.Button;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.lang.GeoLocation;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import com.drew.metadata.exif.GpsDirectory;

import org.glob3.mobile.generated.Geodetic3D;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;


public class MainActivity extends Activity {

    final int MY_PERMISSIONS_REQUEST_READ_MEDIA = 3;
    private Cursor cursor;
    Session _session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        _session = Session.getInstance();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);

        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_READ_MEDIA);
        } else {

            //TODO: Criteria for creating the visualizations
            final Calendar calFrom = Calendar.getInstance();
            calFrom.set(Calendar.YEAR, 2015);
            calFrom.set(Calendar.MONTH, 8);
            calFrom.set(Calendar.DAY_OF_MONTH, 1);

            final Calendar calTo = Calendar.getInstance();
            calTo.set(Calendar.YEAR, 2015);
            calTo.set(Calendar.MONTH, 9);
            calTo.set(Calendar.DAY_OF_MONTH, 2);


            Button createStoryButton = (Button) findViewById(R.id.createStory);
            createStoryButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    try {
                        createStory(calFrom, calTo);
                    } catch (ImageProcessingException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
            });
        }


    }

    private void createStory(Calendar calFrom, Calendar calTo) throws ImageProcessingException, IOException {

        //TODO: See criteria for create the visualization
        // See what metadata can offer for us
        // Positioning is a good candidate
        ArrayList<Photo> candidateImages = getCandidateImagery(calFrom, calTo);
        _session.setSessionPhotos(candidateImages);
        Intent intent = new Intent(this, StoryActivity.class);
        startActivity(intent);
    }

    private ArrayList<Photo> getCandidateImagery(Calendar calFrom, Calendar calTo) throws ImageProcessingException, IOException {
        ArrayList<Photo> candidateImages = new ArrayList<>();

        String[] projection = {MediaStore.Images.Media.DATA};

        cursor = getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                projection, // Which columns to return
                null,       // Return all rows
                null,
                null);
        if (cursor.getCount() == 0) {
            //TODO: do something when we do not have any images ()
        } else {

            int i = 0;
            while (cursor.moveToNext()) {


                int file_ColumnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                File jpegFile = new File(cursor.getString(file_ColumnIndex));
                Metadata metadata = ImageMetadataReader.readMetadata(jpegFile);

                ExifSubIFDDirectory directory
                        = metadata.getFirstDirectoryOfType(ExifSubIFDDirectory.class);
                GpsDirectory gpsDirectory = metadata.getFirstDirectoryOfType(GpsDirectory.class);
                if (directory != null) {
                    Date date = directory.getDate(ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL);
                    if (date != null) {
                        Calendar photoCalendar = Calendar.getInstance();
                        photoCalendar.setTime(date);

                        if (photoCalendar.after(calFrom) && photoCalendar.before(calTo)) {

                            if (gpsDirectory != null) {
                                Photo photo = new Photo();
                                photo.setPath(jpegFile.getAbsolutePath());
                                photo.setDate(photoCalendar);
                                GeoLocation geoPosition = gpsDirectory.getGeoLocation();
                                photo.setPosition(Geodetic3D.fromDegrees(geoPosition.getLatitude(), geoPosition.getLongitude(), 0));
                                candidateImages.add(photo);
                            }
                        }
                    }
                }

            }
        }


        return candidateImages;
    }
}
