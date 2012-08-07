package gs.web.school;

import gs.data.school.*;
import gs.web.request.RequestInfo;
import gs.web.util.RedirectView301;
import gs.web.util.UrlBuilder;
import gs.web.util.UrlUtil;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class TeachersStudentsController extends PerlFetchController {
    private String _privateSchoolContentPath;
    private String _publicSchoolContentPath;

    @Override
    public ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
        School school = (School) request.getAttribute(SCHOOL_ATTRIBUTE);
        // GS-13082 Redirect to new profile if eligible
        if (shouldRedirectToNewProfile(school)) {
            return getRedirectToNewProfileModelAndView(school, request, NewProfileTabs.demographics);
        }
        return super.handleRequestInternal(request, response);
    }

    @Override
    protected boolean shouldIndex(School school, String perlResponse) {
        if ((school.getLevelCode().equals(LevelCode.PRESCHOOL) ||
                (school.getGradeLevels().containsOnly(Grade.KINDERGARTEN) &&
                 school.getType().equals(SchoolType.PRIVATE))) &&
                (perlResponse.contains("Student data was not reported for this school.") &&
                perlResponse.contains("Teacher data was not reported for this school."))) {
            return false;
        }

        return true;
    }

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

        String href = request.getScheme() + "://localhost" +
                ((request.getServerPort() != 80)?(":" + request.getServerPort()):"") +
                relativePath;

        if (UrlUtil.isDeveloperWorkstation(request.getServerName())) {
            href = "http://" + DEV_HOST + relativePath;
        }

        return href;
    }

    /**
     * Check a school's levelcode and type to see if a redirect is needed. GS-12127
     *
     * @param request
     * @param school
     * @return A ModelAndView that includes a redirect view, otherwise null
     */
    public ModelAndView getPreschoolRedirectViewIfNeeded(HttpServletRequest request, School school) {
        ModelAndView preschoolRedirectMnadV = null;

        // Preschool profile pages should be hosted from pk.greatschools.org (GS-12127). Redirect if needed
        if (LevelCode.PRESCHOOL.equals(school.getLevelCode())) {
            UrlBuilder.VPage vpage;
            if (SchoolType.PRIVATE.equals(school.getType())) {
                vpage = UrlBuilder.SCHOOL_PROFILE_CENSUS_PRIVATE;
            } else {
                vpage = UrlBuilder.SCHOOL_PROFILE_CENSUS;
            }

            RequestInfo requestInfo = (RequestInfo) request.getAttribute(RequestInfo.REQUEST_ATTRIBUTE_NAME);
            if (!requestInfo.isOnPkSubdomain() && requestInfo.isPkSubdomainSupported()) {
                UrlBuilder urlBuilder = new UrlBuilder(school, vpage);
                preschoolRedirectMnadV = new ModelAndView(new RedirectView301(urlBuilder.asFullUrl(request)));
            }
        }
        return preschoolRedirectMnadV;
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
