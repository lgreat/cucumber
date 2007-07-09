package gs.web.search;

import gs.data.geo.ILocation;
import gs.data.geo.LatLon;
import gs.data.school.School;
import gs.data.search.Indexer;
import org.apache.lucene.document.Document;

public class SchoolSearchResult extends SearchResult implements ILocation {
    private School _school;

    public SchoolSearchResult(Document doc) {
        super(doc);
    }

    public School getSchool() {
        return _school;
    }

    public String getGreatSchoolsRating() {
        return _doc.get(Indexer.OVERALL_RATING);
    }

    public void setSchool(School school) {
        _school = school;
    }

    public LatLon getLatLon() {
        return getSchool().getLatLon();
    }

    public String getParentRating() {
        String parentRatingsCount = _doc.get(Indexer.PARENT_RATINGS_COUNT);
        if (parentRatingsCount == null) {
            return null;
        }
        if (Integer.valueOf(parentRatingsCount) > 2) {
            return _doc.get(Indexer.PARENT_RATINGS_AVG_QUALITY);
        }
        return null;
    }
}
