package gs.web.geo;

import com.vividsolutions.jts.geom.Geometry;
import gs.data.geo.BoundaryUtil;
import gs.data.json.JSONArray;
import gs.data.json.JSONException;
import gs.data.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * @author aroy@greatschools.org
 */
public class MapObject {
    private List<MapObject> _dependentObjects;
    private JSONObject _data = new JSONObject();

    public enum MarkerShapeType {
        circle, poly, rect
    }
    private double _latitude;
    private double _longitude;
    private String _icon;
    private int _width;
    private int _height;
    private Integer _originX;
    private Integer _originY;
    private Integer _anchorX;
    private Integer _anchorY;
    private int[] _shape;
    private MarkerShapeType _shapeType;
    private String _tooltip;
    private Geometry _coordinates;
    private boolean _hasMarkerInfo = false;
    private boolean _hasPolygonInfo = false;

    public MapObject(double lat, double lon, String iconUrl, int width, int height) {
        setMarkerInfo(lat, lon, iconUrl, width, height);
    }

    public MapObject(Geometry coordinates) {
        setCoordinates(coordinates);
    }

    public void setCoordinates(Geometry coordinates) {
        _hasPolygonInfo = (coordinates != null);
        _coordinates = coordinates;
    }
    
    public Geometry getCoordinates() {
        return _coordinates;
    }

    public void setMarkerInfo(double lat, double lon, String iconUrl, int width, int height) {
        _latitude = lat;
        _longitude = lon;
        _icon = iconUrl;
        _width = width;
        _height = height;
        _hasMarkerInfo = true;
    }

    public void setOrigin(int x, int y) {
        _originX = x;
        _originY = y;
    }

    public void setAnchor(int x, int y) {
        _anchorX = x;
        _anchorY = y;
    }

    public void setTooltip(String tooltip) {
        _tooltip = tooltip;
    }

    public void setShape(MarkerShapeType type, int[] shape) {
        _shapeType = type;
        _shape = shape;
    }

    public JSONObject getData() {
        return _data;
    }

    public void addDependent(MapObject dependent) {
        if (_dependentObjects == null) {
            _dependentObjects = new ArrayList<MapObject>();
        }
        _dependentObjects.add(dependent);
    }

    public JSONObject toJsonObject() throws JSONException {
        JSONObject rval = new JSONObject();
        rval.put("data", _data);
        if (_dependentObjects != null && _dependentObjects.size() > 0) {
            JSONArray dependents = new JSONArray();
            for (MapObject dependent: _dependentObjects) {
                dependents.put(dependent.toJsonObject());
            }
            rval.put("dependents", dependents);
        }
        rval.put("hasMarkerInfo", _hasMarkerInfo);
        rval.put("hasPolygonInfo", _hasPolygonInfo);
        if (_hasMarkerInfo) {
            JSONObject center = new JSONObject();
            center.put("latitude", _latitude);
            center.put("longitude", _longitude);
            rval.put("center", center);
            rval.put("url", _icon);

            JSONObject size = new JSONObject();
            size.put("width", _width);
            size.put("height", _height);
            rval.put("size", size);
            if (_tooltip != null) {
                rval.put("name", _tooltip);
            }
            if (_originX != null && _originY != null) {
                JSONObject origin = new JSONObject();
                origin.put("x", _originX);
                origin.put("y", _originY);
                rval.put("origin", origin);
            }
            if (_anchorX != null && _anchorY != null) {
                JSONObject anchor = new JSONObject();
                anchor.put("x", _anchorX);
                anchor.put("y", _anchorY);
                rval.put("anchor", anchor);
            }
            if (_shapeType != null && _shape != null) {
                JSONObject shape = new JSONObject();
                shape.put("type", _shapeType);
                shape.put("coord", new JSONArray(_shape));
                rval.put("shape", shape);
            }
        }

        if (_hasPolygonInfo) {
            rval.put("coordinates", BoundaryUtil.geometryToJsonArray(_coordinates));
            JSONObject centroid = new JSONObject();
            centroid.put("lon", _coordinates.getCentroid().getX());
            centroid.put("lat", _coordinates.getCentroid().getY());
            rval.put("centroid", centroid);
        }

        return rval;
    }
}
