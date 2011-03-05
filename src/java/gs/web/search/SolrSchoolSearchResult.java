package gs.web.search;

import gs.data.geo.LatLon;
import gs.data.school.Grades;
import gs.data.school.LevelCode;
import gs.data.school.SchoolType;
import gs.data.search.indexers.documentBuilders.SchoolDocumentBuilder;
import gs.data.state.State;
import gs.data.state.StateManager;
import gs.data.util.Address;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.solr.client.solrj.beans.Field;

import javax.xml.bind.annotation.*;

/* http://jackson-users.ning.com/forum/topics/jaxb-xmlelement-replacement */
@XmlType(propOrder={"id","databaseState","name","address","phone","latLon","levelCode","schoolType","greatSchoolsRating","parentRating","distance"})
@XmlAccessorType(XmlAccessType.PUBLIC_MEMBER)
public class SolrSchoolSearchResult implements ISchoolSearchResult {

    private String _databaseState;

    private StateManager _stateManager;

    private Integer _id;

    private String _name;

    private String _phone;

    private Double _latitude;

    private Double _longitude;

    private String _schoolType;

    private Integer _greatSchoolsRating;

    private Integer _parentRating;

    private Grades _grades;

    private String _commaSeparatedlevelCodes;

    private String _street;

    private String _city;

    private State _state;

    private String _zip;

    private Integer _schoolReviewCount;

    private String _schoolReviewBlurb;

    private Double _distance;

    public SolrSchoolSearchResult() {
        // empty constructor required by JAXB
        _stateManager = new StateManager();
    }

    @XmlElement
    public Integer getId() {
        return _id;
    }

    @Field(SchoolDocumentBuilder.SCHOOL_ID)
    public void setId(Integer id) {
        _id = id;
    }

    @XmlElement
    public State getDatabaseState() {
        return getStateManager().getState(_databaseState);
    }

    @Field(SchoolDocumentBuilder.SCHOOL_DATABASE_STATE)
    public void setDatabaseState(String state) {
        _databaseState = state;
    }

    @XmlElement
    public String getName() {
        return _name;
    }

    @Field(SchoolDocumentBuilder.SCHOOL_NAME)
    public void setName(String name) {
        _name = name;
    }

    @XmlElement
    public Address getAddress() {
        Address address = new Address(
                getStreet(),
                getCity(),
                getState(),
                getZip()
        );
        return address;
    }

    public String getStreet() {
        return _street;
    }

    @Field(SchoolDocumentBuilder.ADDRESS_STREET)
    public void setStreet(String street) {
        _street = street;
    }

    public String getCity() {
        return _city;
    }

    @Field(SchoolDocumentBuilder.ADDRESS_CITY)
    public void setCity(String city) {
        _city = city;
    }

    public State getState() {
        return _state;
    }

    @Field(SchoolDocumentBuilder.ADDRESS_STATE)
    public void setState(String state) {
        _state = _stateManager.getState(state);
    }

    public String getZip() {
        return _zip;
    }

    @Field(SchoolDocumentBuilder.ADDRESS_ZIP)
    public void setZip(String zip) {
        _zip = zip;
    }

    @XmlElement
    public String getPhone() {
        return _phone;
    }

    @Field(SchoolDocumentBuilder.SCHOOL_PHONE)
    public void setPhone(String phone) {
        _phone = phone;
    }

    @XmlElement
    public LatLon getLatLon() {
        Double latitude = getLatitude();
        Double longitude = getLongitude();
        LatLon latLon = null;
        if (latitude != null && longitude != null) {
            latLon = new LatLon(latitude.floatValue(),longitude.floatValue());
        }
        return latLon;
    }

    @Field(SchoolDocumentBuilder.LATITUDE)
    public void setLatitude(Double latitude) {
        _latitude = latitude;
    }

    public Double getLatitude() {
        return _latitude;
    }

    @Field(SchoolDocumentBuilder.LONGITUDE)
    public void setLongitude(Double longitude) {
        _longitude = longitude;
    }

    public Double getLongitude() {
        return _longitude;
    }

    @XmlElement
    public String getLevelCode() {
        return _commaSeparatedlevelCodes;
    }

    @Field(SchoolDocumentBuilder.GRADE_LEVEL)
    public void setLevelCode(String[] levelCodeArray) {
        if (levelCodeArray != null) {
            levelCodeArray = (String[])ArrayUtils.removeElement((String[])levelCodeArray,"junior");
        }
        LevelCode levelCode = null;

        if (levelCodeArray != null && levelCodeArray.length > 0) {
            levelCode = LevelCode.createLevelCode(levelCodeArray);
        }

        if (levelCode != null) {
            _commaSeparatedlevelCodes = levelCode.getCommaSeparatedString();
        }
    }

    public Grades getGrades() {
        return _grades;
    }

    @Field(SchoolDocumentBuilder.GRADES)
    public void setGrades(String[] gradeArray) {
        Grades grades = null;

        if (gradeArray != null && gradeArray.length > 0) {
            grades = Grades.createGrades(StringUtils.join(gradeArray,","));
        }
        _grades = grades;
    }

    @XmlElement
    public String getSchoolType() {
        return _schoolType;
    }

    @Field(SchoolDocumentBuilder.SCHOOL_TYPE)
    public void setSchoolType(String schoolType) {
        SchoolType type = null;
        if (schoolType != null) {
            type = SchoolType.getSchoolType(schoolType);
        }
        _schoolType = type.getName();
    }

    @XmlElement
    public Integer getGreatSchoolsRating() {
        return _greatSchoolsRating;
    }

    @Field(SchoolDocumentBuilder.OVERALL_GS_RATING)
    public void setGreatSchoolsRating(Integer rating) {
        _greatSchoolsRating = rating;
    }

    @XmlElement
    public Integer getParentRating() {
        return _parentRating;
    }

    @Field(SchoolDocumentBuilder.COMMUNITY_RATING)
    public void setParentRating(Integer rating) {

        _parentRating = rating;
    }

    @XmlElement
    public String getReviewBlurb() {
        return _schoolReviewBlurb;
    }

    @Field(SchoolDocumentBuilder.SCHOOL_REVIEW_BLURB)
    public void setReviewBlurb(String schoolReviewBlurb) {
        _schoolReviewBlurb = schoolReviewBlurb;
    }

    @XmlElement
    public Integer getReviewCount() {
        return _schoolReviewCount;
    }

    @Field(SchoolDocumentBuilder.SCHOOL_REVIEW_COUNT)
    public void setReviewCount(Integer schoolReviewCount) {
        _schoolReviewCount = schoolReviewCount;
    }

    @XmlElement
    public Double getDistance() {
        return _distance;
    }

    @Field("distance")
    public void setDistance(Double distance) {
        _distance = distance;
    }

    @XmlTransient
    public StateManager getStateManager() {
        return _stateManager;
    }

    public void setStateManager(StateManager stateManager) {
        _stateManager = stateManager;
    }
}
