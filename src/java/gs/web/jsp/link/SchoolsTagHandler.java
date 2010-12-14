/*
 * Copyright (c) 2005-2006 GreatSchools.org. All Rights Reserved.
 * $Id: SchoolsTagHandler.java,v 1.24 2010/12/14 01:59:58 yfan Exp $
 */

package gs.web.jsp.link;

import gs.data.geo.ICity;
import gs.data.school.SchoolType;
import gs.data.school.LevelCode;
import gs.data.school.district.District;
import gs.data.state.State;
import gs.web.util.UrlBuilder;
import gs.web.school.SchoolsController;
import org.apache.commons.lang.StringUtils;

import java.util.StringTokenizer;
import java.util.Set;
import java.util.HashSet;

/**
 * Access to the browse schools in {CITY || DISTRICT} page.
 *
 * @author <a href="mailto:aroy@greatschools.org">Anthony Roy</a>
 */
public class SchoolsTagHandler extends LinkTagHandler {

    private String _schoolType;
    private String _levelCode;
    private Integer _page;
    private ICity _city;
    private State _state;
    private String _cityName;
    private District _district;
    private Integer _districtId;
    private String _districtName;
    private boolean _showAll = false;
    private Integer _resultsPerPage;
    private String _sortColumn;
    private String _sortDirection;
    private String _sortSelection;

    protected UrlBuilder createUrlBuilder() {
        UrlBuilder urlBuilder;
        LevelCode levelCode = null;
        if (StringUtils.isNotBlank(_levelCode)) {
            levelCode = LevelCode.createLevelCode(_levelCode);
        }
        if ((_district == null || _district.getId() == 0) && (_districtId == null || _districtName == null)) {
            Set<SchoolType> schoolTypes = new HashSet<SchoolType>();
            if (StringUtils.isNotBlank(_schoolType)) {
                StringTokenizer tok = new StringTokenizer(_schoolType, ",");
                while (tok.hasMoreTokens()) {
                    String token = tok.nextToken();
                    schoolTypes.add(SchoolType.getSchoolType(token));
                }
            }

            State myState = _city != null ? _city.getState() : (_state != null ? _state : super.getState());
            urlBuilder = new UrlBuilder(UrlBuilder.SCHOOLS_IN_CITY, myState, _cityName, schoolTypes, levelCode);
        } else {
            if (_district != null) {
                urlBuilder = new UrlBuilder(_district, levelCode, UrlBuilder.SCHOOLS_IN_DISTRICT);
            } else if (_districtId != null && _districtName != null && _cityName != null && _state != null) {
                urlBuilder = new UrlBuilder(_state, _districtId, _districtName, _cityName, levelCode, UrlBuilder.SCHOOLS_IN_DISTRICT);
            } else {
                throw new IllegalStateException("Either district, or district ID and district name should be available");
            }
            urlBuilder.removeParameter(SchoolsController.PARAM_CITY);

            if (StringUtils.isNotBlank(_schoolType)) {
                StringTokenizer tok = new StringTokenizer(_schoolType, ",");
                while (tok.hasMoreTokens()) {
                    String token = tok.nextToken();
                    urlBuilder.addParameter(SchoolsController.PARAM_SCHOOL_TYPE, token);
                }
            }
        }

        if (null != _page) {
            urlBuilder.setParameter(SchoolsController.PARAM_PAGE, String.valueOf(_page));
        }
        if (_showAll) {
            urlBuilder.setParameter(SchoolsController.PARAM_SHOW_ALL, "1");
        }
        if (null != _resultsPerPage) {
            urlBuilder.setParameter(SchoolsController.PARAM_RESULTS_PER_PAGE, _resultsPerPage.toString());
        }
        if (!StringUtils.isBlank(_sortColumn)) {
            urlBuilder.setParameter(SchoolsController.PARAM_SORT_COLUMN, _sortColumn);
        }
        if (!StringUtils.isBlank(_sortDirection)) {
            urlBuilder.setParameter(SchoolsController.PARAM_SORT_DIRECTION, _sortDirection);
        }
        if (!StringUtils.isBlank(_sortSelection)) {
            urlBuilder.setParameter("sortSelection", _sortSelection);
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

    public ICity getCity() {
        return _city;
    }

    public void setCity(ICity city) {
        _city = city;
        if (city != null) {
            setCityName(city.getName());
        }
    }

    public String getCityName() {
        return _cityName;
    }

    public void setCityName(String cityName) {
        _cityName = cityName;
    }

    public boolean getShowAll() {
        return _showAll;
    }

    public void setShowAll(boolean showAll) {
        _showAll = showAll;
    }

    public String getSortColumn(){
        return _sortColumn;
    }

    public void setSortColumn(String sortColumn){
        _sortColumn = sortColumn;
    }

    public String getSortDirection(){
        return _sortDirection;
    }

    public void setSortDirection(String sortDirection){
        _sortDirection = sortDirection;
    }

    public Integer getResultsPerPage(){
        return _resultsPerPage;
    }

    public void setResultsPerPage(Integer resultsPerPage){
        _resultsPerPage = resultsPerPage;
    }

    public String getSortSelection() {
        return _sortSelection;
    }

    public void setSortSelection(String sortSelection) {
        _sortSelection = sortSelection;
    }

    public District getDistrict() {
        return _district;
    }

    public void setDistrict(District district) {
        _district = district;
    }

    public State getState() {
        return _state;
    }

    public void setState(State state) {
        _state = state;
    }

    public Integer getDistrictId() {
        return _districtId;
    }

    public void setDistrictId(Integer districtId) {
        _districtId = districtId;
    }

    public String getDistrictName() {
        return _districtName;
    }

    public void setDistrictName(String districtName) {
        _districtName = districtName;
    }
}