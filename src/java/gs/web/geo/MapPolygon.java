package gs.web.geo;

import com.vividsolutions.jts.geom.Geometry;
import gs.data.geo.BoundaryUtil;
import gs.data.json.JSONException;
import gs.data.json.JSONObject;

/**
 * @author aroy@greatschools.org
 */
public class MapPolygon extends MapObject {
    private Geometry _coordinates;

    public MapPolygon(Geometry coordinates) {
        _coordinates = coordinates;
    }

    public Geometry getCoordinates() {
        return _coordinates;
    }

    public JSONObject toJsonObject() throws JSONException {
        JSONObject rval = super.toJsonObject();

        rval.put("coordinates", BoundaryUtil.geometryToJsonArray(getCoordinates()));
        JSONObject centroid = new JSONObject();
        centroid.put("lon", getCoordinates().getCentroid().getX());
        centroid.put("lat", getCoordinates().getCentroid().getY());
        rval.put("centroid", centroid);
        rval.put("type", "polygon");

        return rval;
    }
}
