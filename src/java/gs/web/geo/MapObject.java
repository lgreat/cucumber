package gs.web.geo;

import gs.data.json.JSONArray;
import gs.data.json.JSONException;
import gs.data.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author aroy@greatschools.org
 */
public class MapObject {
    private List<MapObject> _dependentObjects;
    private JSONObject _data = new JSONObject();

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
        return rval;
    }
}
