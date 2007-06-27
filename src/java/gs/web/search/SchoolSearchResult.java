package gs.web.search;

import org.apache.lucene.document.Document;
import gs.data.school.School;
import gs.data.search.Indexer;
import gs.data.geo.ILocation;
import gs.data.geo.LatLon;

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
}
