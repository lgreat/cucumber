package gs.web.jsp.link;

import gs.data.school.LevelCode;
import gs.data.school.School;
import gs.web.util.UrlBuilder;

/**
 * @author aroy@greatschools.org
 */
public class DistrictBoundaryTagHandler extends LinkTagHandler {
    private School _school;
    private Double _lat;
    private Double _lon;

    @Override
    protected UrlBuilder createUrlBuilder() {
        UrlBuilder boundaryUrl = new UrlBuilder(UrlBuilder.BOUNDARY_MAP);
        if (_school != null && _school.getId() != null && _school.getDatabaseState() != null) {
            boundaryUrl.addParameter("schoolId", _school.getId().toString());
            boundaryUrl.addParameter("state", _school.getDatabaseState().toString());
            if (_school.getDistrictId() != null && _school.getDistrictId() > 0) {
                boundaryUrl.addParameter("districtId", _school.getDistrictId().toString());
            }
            if (_school.getLevelCode() != null) {
                LevelCode.Level lowestLevel = _school.getLevelCode().getLowestNonPreSchoolLevel();
                boundaryUrl.addParameter("level", lowestLevel.getName());
            }
        } else if (_lat != null && _lon != null) {
            boundaryUrl.addParameter("lat", _lat.toString());
            boundaryUrl.addParameter("lon", _lon.toString());
        }
        return boundaryUrl;
    }

    public School getSchool() {
        return _school;
    }

    public void setSchool(School school) {
        _school = school;
    }

    public Double getLat() {
        return _lat;
    }

    public void setLat(Double lat) {
        _lat = lat;
    }

    public Double getLon() {
        return _lon;
    }

    public void setLon(Double lon) {
        _lon = lon;
    }
}
