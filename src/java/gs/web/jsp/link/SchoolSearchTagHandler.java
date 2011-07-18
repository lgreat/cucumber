package gs.web.jsp.link;

import gs.data.state.State;
import gs.web.util.UrlBuilder;
import org.apache.commons.lang.StringUtils;

import java.util.StringTokenizer;

/**
 * @author Anthony Roy <mailto:aroy@greatschools.org>
 */
public class SchoolSearchTagHandler extends LinkTagHandler {
    private String _schoolType;
    private String _levelCode;
    private String _query;
    private Integer _page;
    private String _schoolSearchType;
    private State _state;

    protected UrlBuilder createUrlBuilder() {
        UrlBuilder urlBuilder = new UrlBuilder(UrlBuilder.SCHOOL_SEARCH, getState(), getQuery());
        if (null != _page) {
            urlBuilder.setParameter("p", String.valueOf(_page));
        }
        if (StringUtils.isNotBlank(_levelCode)) {
            urlBuilder.setParameter("lc", _levelCode);
        }
        if (StringUtils.isNotBlank(_schoolType)) {
            StringTokenizer tok = new StringTokenizer(_schoolType, ",");
            while (tok.hasMoreTokens()) {
                String token = tok.nextToken();
                urlBuilder.addParameter("st", token);
            }
        }
        if (_schoolSearchType != null) {
            urlBuilder.setParameter("schoolSearchType", _schoolSearchType);
        }
        return urlBuilder;
    }

    public String getSchoolType() {
        return _schoolType;
    }

    public void setSchoolType(String schoolType) {
        this._schoolType = schoolType;
    }

    public String getLevelCode() {
        return _levelCode;
    }

    public void setLevelCode(String levelCode) {
        this._levelCode = levelCode;
    }

    public String getQuery() {
        return _query;
    }

    public void setQuery(String query) {
        this._query = query;
    }

    public String getId() {
        return getStyleId();
    }

    public void setId(String id) {
        setStyleId(id);
    }

    public Integer getPage() {
        return _page;
    }

    public void setPage(Integer page) {
        _page = page;
    }

    public String getSchoolSearchType() {
        return _schoolSearchType;
    }

    public void setSchoolSearchType(String schoolSearchType) {
        _schoolSearchType = schoolSearchType;
    }

    public State getState() {
        if (_state != null) {
            return _state;
        }
        return super.getState();
    }

    public void setState(State state) {
        _state = state;
    }

    /*public void setSchoolSearchType(SchoolSearchType schoolSearchType) {
        _schoolSearchType = schoolSearchType;
    }*/
}
