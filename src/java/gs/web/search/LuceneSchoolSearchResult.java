package gs.web.search;

import gs.data.geo.LatLon;
import gs.data.school.LevelCode;
import gs.data.school.SchoolType;
import gs.data.search.IndexField;
import gs.data.search.Indexer;
import gs.data.state.State;
import gs.data.state.StateManager;
import gs.data.util.Address;
import org.apache.lucene.document.Document;

import java.util.HashMap;
import java.util.Map;

public class LuceneSchoolSearchResult implements ISchoolSearchResult {

    Document _document;

    StateManager _stateManager;

    public LuceneSchoolSearchResult(Document document) {
        _document = document;
        _stateManager = new StateManager();
    }

    public Document getDocument() {
        return _document;
    }

    public void setDocument(Document document) {
        _document = document;
    }

    public Integer getId() {
        String indexedSchoolId = _document.get(Indexer.ID);
        Integer returnId = null;
        if (indexedSchoolId != null) {
            returnId = Integer.valueOf(_document.get(Indexer.ID));
        }
        return null;
    }

    public State getDatabaseState() {
        return getStateManager().getState(_document.get(IndexField.STATE));
    }

    public String getName() {
        return _document.get(Indexer.SORTABLE_NAME);
    }

    public Address getAddress() {
        Address address = new Address(
                _document.get(Indexer.STREET),
                _document.get(Indexer.CITY),
                getStateManager().getState(_document.get(Indexer.STATE)),
                _document.get(Indexer.ZIP)
        );
        return address;
    }

    public String getPhone() {
        return null; //TODO: index phone
    }

    public LatLon getLatLon() {
        String latitude = _document.get(Indexer.LATITUDE);
        String longitude = _document.get(Indexer.LONGITUDE);
        LatLon latLon = null;
        if (latitude != null && longitude != null) {
            latLon = new LatLon(
                    Float.valueOf(latitude),
                    Float.valueOf(longitude)
            );
        }
        return latLon;
    }

    public LevelCode getLevelCode() {
        String[] levelCodeArray = _document.getValues(IndexField.GRADE_LEVEL);
        LevelCode levelCode = null;

        if (levelCodeArray != null && levelCodeArray.length > 0) {
            levelCode = LevelCode.createLevelCode(_document.getValues(IndexField.GRADE_LEVEL));
        }
        
        return levelCode;
    }

    public SchoolType getSchoolType() {
        SchoolType type = null;
        String t = _document.get(IndexField.SCHOOL_TYPE);
        if (t != null) {
            type = SchoolType.getSchoolType(t);
        }
        return type;
    }

    public Integer getGreatSchoolsRating() {
        String rating = _document.get(Indexer.OVERALL_RATING);
        Integer iRating = null;
        if (rating != null) {
            iRating = Integer.valueOf(rating);
        }
        return iRating;
    }

    public Integer getParentRating() {
        //some of below logic copied from SchoolSearchResult.java
        Integer rating = null;
        String parentRatingsCount = _document.get(Indexer.PARENT_RATINGS_COUNT);
        if (parentRatingsCount != null && Integer.valueOf(parentRatingsCount) > 2) {
            //TODO: test below logic on real data
            if (LevelCode.PRESCHOOL.equals(getLevelCode())) {
                rating = Integer.valueOf(_document.get(Indexer.PARENT_RATINGS_AVG_P_OVERALL));
            } else {
                rating = Integer.valueOf(_document.get(Indexer.PARENT_RATINGS_AVG_QUALITY));
            }
        }
        return rating;
    }

    public StateManager getStateManager() {
        return _stateManager;
    }

    public void setStateManager(StateManager stateManager) {
        _stateManager = stateManager;
    }

    public Map<String,Object> toMap() {
        Map<String,Object> result = new HashMap<String,Object>();
        result.put("id",getId());
        result.put("databaseState", getDatabaseState());
        result.put("name", getName());
        Address address = getAddress();
        if (address != null) {
            Map<String,Object> addressMap = new HashMap<String,Object>();
            addressMap.put("street", address.getStreet());
            addressMap.put("streetLine2", address.getStreetLine2());
            addressMap.put("cityStateZip", address.getCityStateZip());
            result.put("address", addressMap);
        }
        if (getPhone() != null) {
            result.put("phone", getPhone());
        }
        if (this.getLatLon() != null) {
            result.put("latitude", getLatLon().getLat());
            result.put("longitude", getLatLon().getLon());
        }
        if (getLevelCode() != null) {
            result.put("levelCode", getLevelCode().toString());
        }
        if (getSchoolType() != null) {
            result.put("schoolType", this.getSchoolType().getSchoolTypeName());
        }
        if (getGreatSchoolsRating() != null) {
            result.put("greatSchoolsRating", getGreatSchoolsRating());
        }
        if (getParentRating() != null) {
            result.put("parentRating", getParentRating());
        }
        return result;
    }
}
