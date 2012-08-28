package gs.web.school;

import gs.data.community.FavoriteSchool;
import gs.data.community.User;
import gs.data.school.School;
import gs.web.ControllerFamily;
import gs.web.IControllerFamilySpecifier;
import gs.web.path.IDirectoryStructureUrlController;
import gs.web.util.RedirectView301;
import gs.web.util.UrlBuilder;
import gs.web.util.context.SessionContext;
import gs.web.util.context.SessionContextUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

/**
 * @author Dave Roy <mailto:droy@greatschools.org>
 */
public class SchoolProfileController extends AbstractSchoolController implements IDirectoryStructureUrlController, IControllerFamilySpecifier {
    protected static final Log _log = LogFactory.getLog(SchoolProfileController.class.getName());

    private String _viewName;
    private ControllerFamily _controllerFamily;
    private SchoolProfileDataHelper _schoolProfileDataHelper;
    private SchoolProfileHelper _schoolProfileHelper;

    @Override
    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
        Map<String, Object> model = new HashMap<String, Object>();

        School school = _requestAttributeHelper.getSchool(request);
        model.put("school", school);

        SessionContext sc = SessionContextUtil.getSessionContext(request);
        User user = sc.getUser();
        if (user != null) {
            Set<FavoriteSchool> favoriteSchools = user.getFavoriteSchools();
            if (favoriteSchools != null && favoriteSchools.size() > 0) {
                for (FavoriteSchool fave: favoriteSchools) {
                    if (fave.getState().equals(school.getDatabaseState())
                            && fave.getSchoolId().equals(school.getId())) {
                        model.put("schoolInMSL", true);
                    }
                }
            }
        }
        // Create a map on the request used to save references to data that is to be reused.
        // This needs to be done early in the request cycle or the item attached to the request can be lost.
        AbstractDataHelper.initialize( request );

        // TODO-13114: Audit SchoolOverview2010Controller and refactor all shared logic.  The
        // new profile and old profile will coexist side by side for a while, so they need to share code.

        // TODO-13114: we'll need to eventually include the 301-redirect of preschool pages to pk.greatschools.org
        //             but we're currently not serving preschool pages on the new profile pages, so we can ignore for now

        // TODO-13114 refactor this to reuse same urlbuilder as the one in SchoolProfileHelper's handlePinItButton
        UrlBuilder urlBuilder = new UrlBuilder(school, UrlBuilder.SCHOOL_PROFILE);
        String fullCanonicalUrl = urlBuilder.asFullUrl(request);
        model.put("relCanonical", fullCanonicalUrl);

        Map<String, Object> ratingsMap =  _schoolProfileDataHelper.getGsRatings(request);
        Integer overallRating = null;
        if (ratingsMap != null) {
            overallRating = (Integer)ratingsMap.get(_schoolProfileDataHelper.DATA_OVERALL_RATING);
            model.put(_schoolProfileDataHelper.DATA_OVERALL_RATING, overallRating);
        }

        // if reviews tab of profile and wrong page, redirect to first page
        if (request.getParameter("tab")!=null &&
                NewProfileTabs.valueOf(request.getParameter("tab")).equals(NewProfileTabs.reviews)){

            Integer currentPage = _schoolProfileDataHelper.getReviewsCurrentPage( request );
            Integer totalPages = _schoolProfileDataHelper.getReviewsTotalPages( request );

            if (currentPage<1 || currentPage>totalPages) {
                urlBuilder.addParametersFromRequest(request);
                // filter out certain parameters we don't want passed through
                urlBuilder.removeParameter("tab");
                urlBuilder.removeParameter("state");
                urlBuilder.removeParameter("id");
                urlBuilder.removeParameter("page");
                urlBuilder.addParameter("tab", NewProfileTabs.reviews.getParameterValue());
                return new ModelAndView(new RedirectView301(urlBuilder.asSiteRelative(request)));
            }
        }

        _schoolProfileHelper.updateModel(request, response, school, model, overallRating);

        model.put("schoolEnrollment", _schoolProfileDataHelper.getEnrollment(request));

        return new ModelAndView(_viewName, model);
    }

    public String getViewName() {
        return _viewName;
    }

    public void setViewName(String viewName) {
        _viewName = viewName;
    }


    public ControllerFamily getControllerFamily() {
        return _controllerFamily;
    }

    public void setControllerFamily(ControllerFamily controllerFamily) {
        _controllerFamily = controllerFamily;
    }

    public SchoolProfileDataHelper getSchoolProfileDataHelper() {
        return _schoolProfileDataHelper;
    }

    public void setSchoolProfileDataHelper(SchoolProfileDataHelper schoolProfileDataHelper) {
        _schoolProfileDataHelper = schoolProfileDataHelper;
    }

    public SchoolProfileHelper getSchoolProfileHelper() {
        return _schoolProfileHelper;
    }

    public void setSchoolProfileHelper(SchoolProfileHelper schoolProfileHelper) {
        _schoolProfileHelper = schoolProfileHelper;
    }
}