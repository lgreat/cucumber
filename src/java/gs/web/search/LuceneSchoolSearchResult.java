package gs.web.search;

import gs.data.geo.LatLon;
import gs.data.school.Grades;
import gs.data.school.LevelCode;
import gs.data.school.SchoolType;
import gs.data.search.IndexField;
import gs.data.search.Indexer;
import gs.data.state.State;
import gs.data.state.StateManager;
import gs.data.util.Address;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.document.Document;

import javax.xml.bind.annotation.*;

/* http://jackson-users.ning.com/forum/topics/jaxb-xmlelement-replacement */
@XmlType(propOrder={"id","databaseState","name","address","phone","latLon","levelCode","schoolType","greatSchoolsRating","parentRating"})
@XmlAccessorType(XmlAccessType.PUBLIC_MEMBER)
public class LuceneSchoolSearchResult implements ISchoolSearchResult {

    private Document _document;

    private StateManager _stateManager;

    public LuceneSchoolSearchResult() {
        // empty constructor required by JAXB
    }

    public LuceneSchoolSearchResult(Document document) {
        _document = document;
        _stateManager = new StateManager();
    }

    @XmlTransient
    public Document getDocument() {
        return _document;
    }

    public void setDocument(Document document) {
        _document = document;
    }

    @XmlElement
    public Integer getId() {
        String indexedSchoolId = _document.get(Indexer.ID);
        Integer returnId = null;
        if (indexedSchoolId != null) {
            returnId = Integer.valueOf(_document.get(Indexer.ID));
        }
        return returnId;
    }

    @XmlElement
    public State getDatabaseState() {
        return getStateManager().getState(_document.get(IndexField.STATE));
    }

    @XmlElement
    public String getName() {
        return _document.get(Indexer.SORTABLE_NAME);
    }

    @XmlElement
    public Address getAddress() {
        Address address = new Address(
                _document.get(Indexer.STREET),
                _document.get(Indexer.CITY),
                getStateManager().getState(_document.get(Indexer.STATE)),
                _document.get(Indexer.ZIP)
        );
        return address;
    }

    @XmlElement
    public String getPhone() {
        return _document.get(Indexer.SCHOOL_PHONE);
    }

    @XmlElement
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

    @XmlElement
    public String getLevelCode() {
        String[] levelCodeArray = _document.getValues(IndexField.GRADE_LEVEL);
        if (levelCodeArray != null) {
            levelCodeArray = (String[])ArrayUtils.removeElement((String[])levelCodeArray,"junior");
        }
        LevelCode levelCode = null;

        if (levelCodeArray != null && levelCodeArray.length > 0) {
            levelCode = LevelCode.createLevelCode(levelCodeArray);
        }
        
        if (levelCode != null) {
            return levelCode.getCommaSeparatedString();
        }
        return null;
    }

    public Grades getGrades() {
        String[] gradeArray = _document.getValues(Indexer.GRADES);
        Grades grades = null;

        if (gradeArray != null && gradeArray.length > 0) {
            grades = Grades.createGrades(StringUtils.join(gradeArray,","));
        }
        
        return grades;
    }

    @XmlElement
    public String getSchoolType() {
        SchoolType type = null;
        String t = _document.get(IndexField.SCHOOL_TYPE);
        if (t != null) {
            type = SchoolType.getSchoolType(t);
            return type.getName();
        }
        return null;
    }

    @XmlElement
    public Integer getGreatSchoolsRating() {
        String rating = _document.get(Indexer.OVERALL_RATING);
        Integer iRating = null;
        if (rating != null) {
            iRating = Integer.valueOf(rating);
        }
        return iRating;
    }

    @XmlElement
    public Integer getParentRating() {
        String rating = _document.get(Indexer.COMMUNITY_RATING_SORTED_ASC);
        if ("99".equals(rating)) {
            rating = null; //used to push an otherwise null value to end of results when sorted by this rating  :'(
        }
        Integer iRating = null;
        if (rating != null) {
            iRating = Integer.valueOf(rating);
        }
        return iRating;
    }

    @XmlTransient
    public StateManager getStateManager() {
        return _stateManager;
    }

    public void setStateManager(StateManager stateManager) {
        _stateManager = stateManager;
    }
}
