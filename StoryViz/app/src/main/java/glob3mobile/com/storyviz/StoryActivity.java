package glob3mobile.com.storyviz;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.RelativeLayout;

import org.glob3.mobile.generated.AltitudeMode;
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

public class StoryActivity extends Activity {

    Session _session;
    G3MBuilder_Android _builder;
    ArrayList<Photo> _photosStory;
    MarksRenderer _photoMarkers = new MarksRenderer(false);
    MeshRenderer _arcsRenderer = new MeshRenderer();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_story);

        _session = Session.getInstance();
        _builder = new G3MBuilder_Android(this);

        _photosStory = _session.getSessionPhotos();
        Collections.sort(_photosStory, new PhotoDateComparator());

        initializeGlob3();

        G3MWidget_Android widget = _builder.createWidget();
        Log.e("Num photos", ":" + _photosStory.size());


        RelativeLayout g3mLayout = (RelativeLayout) findViewById(R.id.g3m);
        g3mLayout.addView(widget);


    }


    private void initializeGlob3() {

        final LayerSet layerSet = new LayerSet();
        final URLTemplateLayer repsolLayer = URLTemplateLayer.newMercator("https://api.mapbox.com/styles/v1/mdelacalle/cirhl4ofm000fhdm8h2l6652j/tiles/256/{z}/{x}/{y}@2x?access_token=pk.eyJ1IjoibWRlbGFjYWxsZSIsImEiOiJrZGVWbmZBIn0.v35SP2MBF-vvMPE4Q-RY_w",
                Sector.fullSphere(), true, 2, 18, TimeInterval.fromDays(30), true, 1);
        repsolLayer.setTitle("Repsol layer");
        repsolLayer.setEnable(true);
        layerSet.addLayer(repsolLayer);
        _builder.getPlanetRendererBuilder().setLayerSet(layerSet);

        createMarkers();
        createGrandArcs();


    }

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
