package gs.web.search;

import org.apache.lucene.document.Document;
import gs.data.school.School;
import gs.data.search.Indexer;

public class SchoolSearchResult extends SearchResult {
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
}
