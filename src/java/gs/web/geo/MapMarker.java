package gs.web.geo;

import gs.data.json.JSONArray;
import gs.data.json.JSONException;
import gs.data.json.JSONObject;

/**
 * @author aroy@greatschools.org
 */
public class MapMarker extends MapObject {
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

    public MapMarker(double lat, double lon, String iconUrl, int width, int height) {
        _latitude = lat;
        _longitude = lon;
        _icon = iconUrl;
        _width = width;
        _height = height;
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

    public JSONObject toJsonObject() throws JSONException {
        JSONObject rval = super.toJsonObject();

        JSONObject center = new JSONObject();
        center.put("latitude", _latitude);
        center.put("longitude", _longitude);
        rval.put("center", center);

        rval.put("type", "marker");
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

        return rval;
    }
}
