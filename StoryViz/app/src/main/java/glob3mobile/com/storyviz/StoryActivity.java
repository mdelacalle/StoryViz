package glob3mobile.com.storyviz;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
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
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.ToggleButton;

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
    final AtomicInteger currentPosition = new AtomicInteger(0);
    private LayerSet _layerSet;
    private String REPSOL_LAYER = "Repsol";
    private String IMAGERY_LAYER = "Imagery";




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_story);

        _session = Session.getInstance();
        _builder = new G3MBuilder_Android(this);
        _photosStory = _session.getSessionPhotos();

        Collections.sort(_photosStory, new PhotoDateComparator());

        initializeGlob3();

        _widget = _builder.createWidget();
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

        _photoContainerDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                    startButton.setVisibility(View.VISIBLE);
            }
        });

        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //TODO back to photos logic
                goToPositionAndUpdateDialog(currentPosition.get());
                _photoContainerDialog.show();
                startButton.setText(getResources().getString(R.string.backToPhotos));
                startButton.setVisibility(View.GONE);

            }
        });
        startButton.bringToFront();


        forwardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                _photoContainerDialog.cancel();
                currentPosition.addAndGet(1);
                if(currentPosition.get() >=_photosStory.size()){
                    currentPosition.set(0);
                }
                goToPositionAndUpdateDialog(currentPosition.get());
                _photoContainerDialog.show();
                startButton.setVisibility(View.GONE);
            }
        });


        backwardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                _photoContainerDialog.cancel();
                currentPosition.decrementAndGet();
                if(currentPosition.get() < 0){
                    currentPosition.set(_photosStory.size()-1);
                }
                goToPositionAndUpdateDialog(currentPosition.get());
                _photoContainerDialog.show();

                startButton.setVisibility(View.GONE);
            }
        });

        ToggleButton toggle = (ToggleButton) findViewById(R.id.changeLayer);
        toggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    _layerSet.getLayerByTitle(IMAGERY_LAYER).setEnable(true);
                    _layerSet.getLayerByTitle(REPSOL_LAYER).setEnable(false);
                } else {
                    _layerSet.getLayerByTitle(IMAGERY_LAYER).setEnable(false);
                    _layerSet.getLayerByTitle(REPSOL_LAYER).setEnable(true);
                }
            }
        });
        toggle.bringToFront();

    }

    /**
     * This method goes to the position stored in the photo object
     * The position is corrected to set the camera in a position where you can see
     * the POI and the photo at same time.
     * TODO: See things that can be a parameter. Change the animation depending the distance. ISSUE 6
     *
     * @param currentPicture the position of the picture on the Array.
     */
    public void goToPositionAndUpdateDialog(int currentPicture) {

        Geodetic3D position = _photosStory.get(currentPicture).getPosition();

        Geodetic3D nextPosition;
        float cameraDistance = 0;
        if (currentPicture < _photosStory.size() - 1) {
            nextPosition = _photosStory.get(currentPicture + 1).getPosition();
        } else {
            nextPosition = _photosStory.get(0).getPosition();
        }
        cameraDistance = GeometryUtils.getCameraHeightForDistanceBetween(position, nextPosition);
        Log.e("Camera distance:", "" + cameraDistance);
        double latitudeCorrection = 0d;
        double longitudeCorrection = 0d;
        double pitch = 0;
        if (cameraDistance == 50000) {
            latitudeCorrection = 0.3d;
            longitudeCorrection = 0.25d;
            pitch =-60d;
        }
        if (cameraDistance == 5000) {
            latitudeCorrection = 0.01d;
            longitudeCorrection = 0.02d;
            pitch =-75;
        }
        Geodetic3D correctedPosition = new Geodetic3D(Angle.fromDegrees(position._latitude._degrees - latitudeCorrection), Angle.fromDegrees(position._longitude._degrees - longitudeCorrection), cameraDistance);
        _widget.getG3MWidget().setAnimatedCameraPosition(TimeInterval.fromSeconds(3), correctedPosition, Angle.fromDegrees(0), Angle.fromDegrees(pitch), false);
        ScaleBitmap(_photosStory.get(currentPicture).getPath(), 35, (RelativeLayout) _photoContainerDialog.findViewById(R.id.photoContainer), _photosStory.get(currentPicture).getExifOrientation());
        createMarkerSelected(position);
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

         _layerSet =  new LayerSet();

         final URLTemplateLayer imageryLayer = URLTemplateLayer.newMercator("http://b.tiles.mapbox.com/v4/bobbysud.79c006a5/{z}/{x}/{y}.png?access_token=pk.eyJ1IjoiYm9iYnlzdWQiLCJhIjoiTi16MElIUSJ9.Clrqck--7WmHeqqvtFdYig",
                 Sector.fullSphere(), true, 2, 18, TimeInterval.fromDays(30), true, 1);
        imageryLayer.setTitle(IMAGERY_LAYER);
        imageryLayer.setEnable(false);
        _layerSet.addLayer(imageryLayer);

//        final URLTemplateLayer repsolLayer = URLTemplateLayer.newMercator("https://api.mapbox.com/styles/v1/mdelacalle/cirhl4ofm000fhdm8h2l6652j/tiles/256/{z}/{x}/{y}@2x?access_token=pk.eyJ1IjoibWRlbGFjYWxsZSIsImEiOiJrZGVWbmZBIn0.v35SP2MBF-vvMPE4Q-RY_w",
//                Sector.fullSphere(), true, 2, 18, TimeInterval.fromDays(30), true, 1);

        final URLTemplateLayer vectorLayer = URLTemplateLayer.newMercator("https://b.tiles.mapbox.com/v4/bobbysud.lff26ajh/{z}/{x}/{y}.png?access_token=pk.eyJ1IjoiYm9iYnlzdWQiLCJhIjoiTi16MElIUSJ9.Clrqck--7WmHeqqvtFdYig",
                Sector.fullSphere(), true, 2, 18, TimeInterval.fromDays(30), true, 1);

        vectorLayer.setTitle(REPSOL_LAYER);
        vectorLayer.setEnable(true);
        _layerSet.addLayer(vectorLayer);
        _builder.setAtmosphere(true);
        _builder.getPlanetRendererBuilder().setLayerSet(_layerSet);

        createMarkers();
        createGrandArcs();


    }

    Mark selectedMark;

    private void createMarkerSelected(Geodetic3D position) {


        if (selectedMark != null) {
            _photoMarkers.removeMark(selectedMark);
        }
        selectedMark = new Mark( //
                "", //
                new URL("file:///repsol-poi-big.png", false), //
                position, //
                AltitudeMode.RELATIVE_TO_GROUND, 0, //
                true, //
                14);
        _photoMarkers.addMark(selectedMark);
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

        GeometryUtils gaCreator = new GeometryUtils();

        for (int i = 0; i < _photosStory.size(); i++) {

            int a = i + 1;
            if (a < _photosStory.size()) {
                _arcsRenderer.addMesh(gaCreator.createMesh(_builder.getPlanet(), _photosStory.get(i).getPosition(), _photosStory.get(a).getPosition()));
            }
        }

        _builder.addRenderer(_arcsRenderer);
    }


}
