package glob3mobile.com.storyviz;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RelativeLayout;

import org.glob3.mobile.generated.AltitudeMode;
import org.glob3.mobile.generated.Angle;
import org.glob3.mobile.generated.Geodetic3D;
import org.glob3.mobile.generated.LayerSet;
import org.glob3.mobile.generated.Mark;
import org.glob3.mobile.generated.MarksRenderer;
import org.glob3.mobile.generated.MeshRenderer;
import org.glob3.mobile.generated.Sector;
import org.glob3.mobile.generated.TimeInterval;
import org.glob3.mobile.generated.URL;
import org.glob3.mobile.generated.URLTemplateLayer;
import org.glob3.mobile.specific.G3MBuilder_Android;
import org.glob3.mobile.specific.G3MWidget_Android;

import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicInteger;

public class StoryActivity extends Activity {

    Session _session;
    G3MBuilder_Android _builder;
    ArrayList<Photo> _photosStory;
    MarksRenderer _photoMarkers = new MarksRenderer(false);
    MeshRenderer _arcsRenderer = new MeshRenderer();
    private Dialog _photoContainerDialog;
    private G3MWidget_Android _widget;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_story);

        final AtomicInteger currentPosition = new AtomicInteger(0);
        _session = Session.getInstance();
        _builder = new G3MBuilder_Android(this);
        _photosStory = _session.getSessionPhotos();

        Collections.sort(_photosStory, new PhotoDateComparator());

        initializeGlob3();

        final G3MWidget_Android _widget = _builder.createWidget();
        Log.e("Num photos", ":" + _photosStory.size());

        RelativeLayout g3mLayout = (RelativeLayout) findViewById(R.id.g3m);
        g3mLayout.addView(_widget);

        final Button startButton = (Button) findViewById(R.id.button_start);

        _photoContainerDialog = new Dialog(StoryActivity.this);
        WindowManager.LayoutParams wmlp = _photoContainerDialog.getWindow().getAttributes();
        wmlp.gravity = Gravity.TOP | Gravity.START;
        wmlp.x = 100;   //x position
        wmlp.y = 100;   //y position

        _photoContainerDialog.setContentView(R.layout.photo_container_dialog);

        final RelativeLayout forwardButton = (RelativeLayout) _photoContainerDialog.findViewById(R.id.forward);
        final RelativeLayout backwardButton = (RelativeLayout) _photoContainerDialog.findViewById(R.id.back);


        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //TODO back to photos logic
                goToPositionAndUpdateDialog(0);
                _photoContainerDialog.show();
                startButton.setText(getResources().getString(R.string.backToPhotos));

            }
        });
        startButton.bringToFront();


        forwardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                _photoContainerDialog.cancel();
                currentPosition.addAndGet(1);
                goToPositionAndUpdateDialog(currentPosition.get());
                _photoContainerDialog.show();
            }
        });


        backwardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                _photoContainerDialog.cancel();
                currentPosition.decrementAndGet();
                goToPositionAndUpdateDialog(currentPosition.get());
                _photoContainerDialog.show();
            }
        });


    }

    public void goToPositionAndUpdateDialog(int currentPicture) {
        Geodetic3D position = _photosStory.get(currentPicture).getPosition();
        Geodetic3D correctedPosition = new Geodetic3D(Angle.fromDegrees(position._latitude._degrees - 1d), Angle.fromDegrees(position._longitude._degrees - 0.005d), 50000);
        _widget.getG3MWidget().setAnimatedCameraPosition(TimeInterval.fromSeconds(3), correctedPosition, Angle.fromDegrees(0), Angle.fromDegrees(-15.743281), true);
        ScaleBitmap(_photosStory.get(0).getPath(), 30, (RelativeLayout) _photoContainerDialog.findViewById(R.id.photoContainer), _photosStory.get(0).getExifOrientation());
    }


    /**
     * This method scales and rotates the given image on path parameter
     * and set as background on the container
     *
     * @param path           The image path
     * @param scaleToUse     Scale in percentage to apply to the image
     * @param photoContainer The container
     * @param rotation       Image' exif rotation
     */
    private void ScaleBitmap(String path, int scaleToUse, RelativeLayout photoContainer, int rotation) {

        Bitmap bmp = BitmapFactory.decodeFile(path);
        DisplayMetrics displaymetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        int sizeY = displaymetrics.widthPixels * scaleToUse / 100;
        int sizeX = bmp.getWidth() * sizeY / bmp.getHeight();

        Matrix mat = new Matrix();

        if (rotation == 90 || rotation == 270) {
            photoContainer.getLayoutParams().height = sizeX;  // replace 100 with your dimensions
            photoContainer.getLayoutParams().width = sizeY;
        } else {
            photoContainer.getLayoutParams().height = sizeY;  // replace 100 with your dimensions
            photoContainer.getLayoutParams().width = sizeX;
        }
        mat.postRotate(rotation);
        Bitmap bMapRotate = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), mat, true);
        photoContainer.setBackground(new BitmapDrawable(Bitmap.createScaledBitmap(bMapRotate, sizeX, sizeY, false)));
    }


    private void initializeGlob3() {

        final LayerSet layerSet = new LayerSet();

//        final URLTemplateLayer repsolLayer = URLTemplateLayer.newMercator("https://api.mapbox.com/styles/v1/mdelacalle/cirhl4ofm000fhdm8h2l6652j/tiles/256/{z}/{x}/{y}@2x?access_token=pk.eyJ1IjoibWRlbGFjYWxsZSIsImEiOiJrZGVWbmZBIn0.v35SP2MBF-vvMPE4Q-RY_w",
//                Sector.fullSphere(), true, 2, 18, TimeInterval.fromDays(30), true, 1);
//        repsolLayer.setTitle("Repsol layer");
//        repsolLayer.setEnable(true);
//        layerSet.addLayer(repsolLayer);


        final URLTemplateLayer repsolLayer = URLTemplateLayer.newMercator("https://api.mapbox.com/styles/v1/mdelacalle/cirhl4ofm000fhdm8h2l6652j/tiles/256/{z}/{x}/{y}@2x?access_token=pk.eyJ1IjoibWRlbGFjYWxsZSIsImEiOiJrZGVWbmZBIn0.v35SP2MBF-vvMPE4Q-RY_w",
                Sector.fullSphere(), true, 2, 18, TimeInterval.fromDays(30), true, 1);
        repsolLayer.setTitle("Repsol layer");
        repsolLayer.setEnable(true);
        layerSet.addLayer(repsolLayer);
        _builder.setAtmosphere(true);
        _builder.getPlanetRendererBuilder().setLayerSet(layerSet);

        createMarkers();
        createGrandArcs();


    }

    /**
     * Creating the markers with pictures (in this case with logo)
     * TODO: Show photo on click
     */
    private void createMarkers() {
        for (Photo photo : _photosStory) {
            _photoMarkers.addMark(new Mark( //
                    "", //
                    new URL("file:///repsol-poi.png", false), //
                    photo.getPosition(), //
                    AltitudeMode.RELATIVE_TO_GROUND, 0, //
                    true, //
                    14));
        }
        _builder.addRenderer(_photoMarkers);
    }

    /**
     * Arcs
     */
    private void createGrandArcs() {

        GrandArcsCreator gaCreator = new GrandArcsCreator();

        for (int i = 0; i < _photosStory.size(); i++) {

            int a = i + 1;
            if (a < _photosStory.size()) {
                _arcsRenderer.addMesh(gaCreator.createMesh(_builder.getPlanet(), _photosStory.get(i).getPosition(), _photosStory.get(a).getPosition()));
            }
        }

        _builder.addRenderer(_arcsRenderer);
    }


}
