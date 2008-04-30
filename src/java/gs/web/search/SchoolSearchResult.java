package gs.web.search;

import gs.data.geo.ILocation;
import gs.data.geo.LatLon;
import gs.data.school.School;
import gs.data.school.LevelCode;
import gs.data.school.SchoolType;
import gs.data.school.review.IReviewDao;
import gs.data.school.review.Ratings;
import gs.data.school.review.Review;
import gs.data.school.review.CategoryRating;
import gs.data.search.Indexer;
import gs.data.util.NameValuePair;
import gs.web.jsp.Util;
import gs.web.util.context.SessionContextUtil;
import gs.web.util.context.SessionContext;
import org.apache.lucene.document.Document;
import org.springframework.context.ApplicationContext;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

public class SchoolSearchResult extends SearchResult implements ILocation {

    private School _school;

    /**
      * The allowed length of the parent review blurb
      */

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
            if (LevelCode.PRESCHOOL.equals(_school.getLevelCode())) {
                return _doc.get(Indexer.PARENT_RATINGS_AVG_P_OVERALL);
            } else {
                return _doc.get(Indexer.PARENT_RATINGS_AVG_QUALITY);
            }
        }
        return null;
    }
    public int getParentRatingCount() {
        String parentRatingsCount = _doc.get(Indexer.PARENT_RATINGS_COUNT);
        if (parentRatingsCount == null) {
            return 0;
        }
        return Integer.valueOf(parentRatingsCount);

    }

    public Map getReviewMap(){
        Map reviewMap = new HashMap();
        reviewMap.put("reviewCount",_doc.get("reviewCount"));
        reviewMap.put("reviewBlurb",_doc.get("reviewBlurb"));
        return reviewMap;
    }



}
