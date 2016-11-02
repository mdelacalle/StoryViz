package glob3mobile.com.storyviz;

import android.util.Log;

import org.glob3.mobile.generated.Color;
import org.glob3.mobile.generated.DirectMesh;
import org.glob3.mobile.generated.FloatBufferBuilderFromGeodetic;
import org.glob3.mobile.generated.GLPrimitive;
import org.glob3.mobile.generated.Geodetic2D;
import org.glob3.mobile.generated.Geodetic3D;
import org.glob3.mobile.generated.IMathUtils;
import org.glob3.mobile.generated.Mesh;
import org.glob3.mobile.generated.Planet;

/**
 * Created by mdelacalle on 27/10/2016.
 */

public class GeometryUtils {

    private double getMiddleHeight(Planet planet, Geodetic3D from, Geodetic3D to) {
        // curve parameters
        final double distanceInDegreesMaxHeight = 180;
        //  const double maxHeight = planet->getRadii().axisAverage() * 5;
        final double maxHeight = planet.getRadii().axisAverage() * 0.75;


        // rough estimation of distance using lat/lon degrees
        final double deltaLatInDeg = from.asGeodetic2D()._latitude._degrees - to.asGeodetic2D()._latitude._degrees;
        final double deltaLonInDeg = from.asGeodetic2D()._longitude._degrees - to.asGeodetic2D()._longitude._degrees;
        final double distanceInDeg = IMathUtils.instance().sqrt((deltaLatInDeg * deltaLatInDeg) + (deltaLonInDeg * deltaLonInDeg));

        if (distanceInDeg >= distanceInDegreesMaxHeight) {
            return maxHeight;
        }

        final double middleHeight = (distanceInDeg / distanceInDegreesMaxHeight) * maxHeight;

        return middleHeight;

    }

    public final Mesh createMesh(Planet planet, Geodetic3D from, Geodetic3D to) {
        final IMathUtils mu = IMathUtils.instance();

        double middleHeight = getMiddleHeight(planet, from, to);

        final Geodetic2D center = Geodetic2D.linearInterpolation(from.asGeodetic2D(), to.asGeodetic2D(), 0.5);

        FloatBufferBuilderFromGeodetic verticesBuilder = FloatBufferBuilderFromGeodetic.builderWithGivenCenter(planet, new Geodetic3D(center, middleHeight / 2));

        int steps = 60;
        for (int i = 0; i <= steps; i++) {
            final double alpha = (double) i / steps;


            final double height = mu.quadraticBezierInterpolation(0, middleHeight, 0, alpha);

            verticesBuilder.add(mu.greatCircleIntermediatePoint(from.asGeodetic2D()._latitude, from.asGeodetic2D()._longitude, to.asGeodetic2D()._latitude, to.asGeodetic2D()._longitude, alpha), height);
        }

        Mesh mesh = new DirectMesh(GLPrimitive.lineStrip(), true, verticesBuilder.getCenter(), verticesBuilder.create(), 10.0f, 1.0f, Color.newFromRGBA(1.0f, 0f, 0f, 0.5f)); // flatColor -  pointSize -  lineWidth -  vertices -  center -  owner,


        if (verticesBuilder != null)
            verticesBuilder.dispose();

        return mesh;
    }

    static float getCameraHeightForDistanceBetween(Geodetic3D position1, Geodetic3D position2) {
        double earthRadius = 6371000; //meters
        double dLat = Math.toRadians(position2._latitude._degrees - position1._latitude._degrees);
        double dLng = Math.toRadians(position2._longitude._degrees - position1._longitude._degrees);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(position1._latitude._degrees)) * Math.cos(Math.toRadians(position2._latitude._degrees)) *
                        Math.sin(dLng / 2) * Math.sin(dLng / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        float dist = (float) (earthRadius * c);

        Log.e("Distance:",""+dist);
        if (dist < 200000) {
            return 5000f;
        }
        if (dist >= 200000 ) {
            return 50000f;
        }

        return 0;
    }


}
