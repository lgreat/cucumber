package gs.web.school;

import gs.data.school.School;
import gs.data.school.SchoolType;
import gs.web.util.UrlUtil;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class TeachersStudentsController extends PerlFetchController {
    private String _privateSchoolContentPath;
    private String _publicSchoolContentPath;

    @Override
    protected String getAbsoluteHref(School school, HttpServletRequest request) {
        String relativePath;
        if (school.getType() != null && SchoolType.PRIVATE.equals(school.getType())) {
            relativePath = getPrivateSchoolContentPath();
        } else {
            relativePath = getPublicSchoolContentPath();
        }

        relativePath = relativePath.replaceAll("\\$STATE", school.getDatabaseState().getAbbreviationLowerCase());
        relativePath = relativePath.replaceAll("\\$ID", String.valueOf(school.getId()));

        String href = request.getScheme() + "://" + request.getServerName() +
                ((request.getServerPort() != 80)?(":" + request.getServerPort()):"") +
                relativePath;

        if (UrlUtil.isDeveloperWorkstation(request.getServerName())) {
            href = "http://" + DEV_HOST + relativePath;
        }

        return href;
    }

    public String getPrivateSchoolContentPath() {
        return _privateSchoolContentPath;
    }

    public void setPrivateSchoolContentPath(String privateSchoolContentPath) {
        _privateSchoolContentPath = privateSchoolContentPath;
    }

    public String getPublicSchoolContentPath() {
        return _publicSchoolContentPath;
    }

    public void setPublicSchoolContentPath(String publicSchoolContentPath) {
        _publicSchoolContentPath = publicSchoolContentPath;
    }
}
