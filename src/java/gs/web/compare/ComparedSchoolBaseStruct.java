package gs.web.compare;

import gs.data.school.School;
import gs.data.school.district.District;
import gs.data.school.review.Review;
import gs.data.state.State;
import gs.web.jsp.Util;
import org.apache.commons.lang.StringUtils;

/**
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class ComparedSchoolBaseStruct {
    private School _school;
    private Integer _gsRating;
    private int _communityRating = 0;
    private Review _recentReview;
    private int _numRatings = 0;
    private int _numReviews = 0;

    public School getSchool() {
        return _school;
    }

    public void setSchool(School school) {
        _school = school;
    }

    // used by overview, map
    public Integer getGsRating() {
        return _gsRating;
    }

    public void setGsRating(Integer gsRating) {
        _gsRating = gsRating;
    }

    // used by overview, map, ratings
    public int getCommunityRating() {
        return _communityRating;
    }

    public void setCommunityRating(int communityRating) {
        _communityRating = communityRating;
    }

    // used by overview, ratings
    public Review getRecentReview() {
        return _recentReview;
    }

    public void setRecentReview(Review recentReview) {
        _recentReview = recentReview;
    }

    // used by overview, ratings
    public int getNumRatings() {
        return _numRatings;
    }

    public void setNumRatings(int numRatings) {
        _numRatings = numRatings;
    }

    // used by overview, ratings
    public int getNumReviews() {
        return _numReviews;
    }

    public void setNumReviews(int numReviews) {
        _numReviews = numReviews;
    }

    /* Convenience methods */

    public String getName() {
        return getSchool().getName();
    }

    public String getStreet() {
        return getSchool().getPhysicalAddress().getStreet();
    }

    public String getCityStateZip() {
        return getSchool().getPhysicalAddress().getCityStateZip();
    }

    public Integer getId() {
        return getSchool().getId();
    }

    public State getState() {
        return getSchool().getDatabaseState();
    }

    // used by overview, map
    public String getEnrollment() {
        Integer enrollment = getSchool().getEnrollmentOrCapacity();
        if (enrollment != null && enrollment > 0) {
            return enrollment + " students";
        }
        return "";
    }

    // used by overview, map
    public District getDistrict() {
        return getSchool().getDistrict();
    }

    // used by overview, map
    public String getType() {
        return Util.capitalize(getSchool().getType().getSchoolTypeName());
    }

    public boolean isPrivate() {
        return "Private".equals(getType());
    }

    public String getUniqueIdentifier() {
        return getSchool().getDatabaseState().getAbbreviationLowerCase() + getSchool().getId();
    }

    // used by overview, map
    public String getGradeLevels() {
        // empty school.gradeLevels or school.gradeLevels eq 'n/a' or school.gradeLevels eq 'N/A'
        String rangeString = getSchool().getGradeLevels().getRangeString();
        if (StringUtils.equalsIgnoreCase("n/a", rangeString)) {
            rangeString = "";
        }
        return rangeString;
    }
    
    public boolean getHasReviews() {
        return _numReviews > 0;
    }
}
