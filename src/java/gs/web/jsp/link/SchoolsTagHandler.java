/*
 * Copyright (c) 2005-2006 GreatSchools.net. All Rights Reserved.
 * $Id: SchoolsTagHandler.java,v 1.9 2008/07/31 16:27:02 yfan Exp $
 */

package gs.web.jsp.link;

import gs.data.geo.ICity;
import gs.data.state.State;
import gs.data.school.SchoolType;
import gs.data.school.LevelCode;
import gs.web.util.UrlBuilder;
import gs.web.school.SchoolsController;
import org.apache.commons.lang.StringUtils;

import java.util.StringTokenizer;
import java.util.Set;
import java.util.HashSet;

/**
 * Access to the browse schools in {CITY || DISTRICT} page.
 *
 * @author <a href="mailto:aroy@greatschools.net">Anthony Roy</a>
 */
public class SchoolsTagHandler extends LinkTagHandler {

    private String _schoolType;
    private String _levelCode;
    private Integer _page;
    private ICity _city;
    private String _cityName;
    private Integer _districtId;
    private boolean _showAll = false;
    private Integer _resultsPerPage;
    private String _sortColumn;
    private String _sortDirection;

    protected UrlBuilder createUrlBuilder() {
        UrlBuilder urlBuilder;
        if (StringUtils.isNotEmpty(_cityName)) {
            urlBuilder = new UrlBuilder(UrlBuilder.SCHOOLS_IN_CITY_NEW_STYLE);
            Set<SchoolType> schoolTypes = new HashSet<SchoolType>();
            LevelCode levelCode = null;

            if (StringUtils.isNotBlank(_levelCode)) {
                levelCode = LevelCode.createLevelCode(_levelCode);
            }
            if (StringUtils.isNotBlank(_schoolType)) {
                StringTokenizer tok = new StringTokenizer(_schoolType, ",");
                while (tok.hasMoreTokens()) {
                    String token = tok.nextToken();
                    schoolTypes.add(SchoolType.getSchoolType(token));
                }
            }

            urlBuilder.setPath(SchoolsController.createNewCityBrowseURI(getState(), _cityName, schoolTypes, levelCode));
        } else {
            urlBuilder = new UrlBuilder(UrlBuilder.SCHOOLS_IN_DISTRICT, getState(), "");
            urlBuilder.setParameter(SchoolsController.PARAM_DISTRICT, String.valueOf(_districtId));
            urlBuilder.removeParameter(SchoolsController.PARAM_CITY);

            if (StringUtils.isNotBlank(_levelCode)) {
                urlBuilder.setParameter(SchoolsController.PARAM_LEVEL_CODE, _levelCode);
            }
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
        if (null != _sortColumn) {
            urlBuilder.setParameter(SchoolsController.PARAM_SORT_COLUMN, _sortColumn);
        }
        if (null != _sortDirection) {
            urlBuilder.setParameter(SchoolsController.PARAM_SORT_DIRECTION, _sortDirection);
        }
        return urlBuilder;

        /*
        
        UrlBuilder urlBuilder = new UrlBuilder(UrlBuilder.SCHOOLS_IN_CITY, getState(), "");
        if (null != _page) {
            urlBuilder.setParameter(SchoolsController.PARAM_PAGE, String.valueOf(_page));
        }
        if (StringUtils.isNotBlank(_levelCode)) {
            urlBuilder.setParameter(SchoolsController.PARAM_LEVEL_CODE, _levelCode);
        }
        if (StringUtils.isNotBlank(_schoolType)) {
            StringTokenizer tok = new StringTokenizer(_schoolType, ",");
            while (tok.hasMoreTokens()) {
                String token = tok.nextToken();
                urlBuilder.addParameter(SchoolsController.PARAM_SCHOOL_TYPE, token);
            }
        }
        if (StringUtils.isNotEmpty(_cityName)) {
            urlBuilder.setParameter(SchoolsController.PARAM_CITY, _cityName);
        } else {
            urlBuilder.setParameter(SchoolsController.PARAM_DISTRICT, String.valueOf(_districtId));
            urlBuilder.removeParameter(SchoolsController.PARAM_CITY);
        }
        if (_showAll) {
            urlBuilder.setParameter(SchoolsController.PARAM_SHOW_ALL, "1");
        }
        if (null != _resultsPerPage) {
            urlBuilder.setParameter(SchoolsController.PARAM_RESULTS_PER_PAGE, _resultsPerPage.toString());
        }
        if (null != _sortColumn) {
            urlBuilder.setParameter(SchoolsController.PARAM_SORT_COLUMN, _sortColumn);
        }
        if (null != _sortDirection) {
            urlBuilder.setParameter(SchoolsController.PARAM_SORT_DIRECTION, _sortDirection);
        }
        return urlBuilder;
        */
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

    public Integer getDistrictId() {
        return _districtId;
    }

    public void setDistrictId(Integer districtId) {
        _districtId = districtId;
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
}
