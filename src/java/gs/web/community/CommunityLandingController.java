package gs.web.community;

import org.springframework.web.servlet.mvc.AbstractController;
import org.springframework.web.servlet.ModelAndView;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.lang.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.util.*;

import gs.web.util.UrlBuilder;
import gs.web.util.RedirectView301;
import gs.data.content.cms.ICmsDiscussionBoardDao;
import gs.data.content.cms.CmsDiscussionBoard;

import gs.data.community.IUserContentDao;
import gs.data.community.Discussion;

/**
 * @author Dave Roy <mailto:droy@greatschools.org>
 */
public class CommunityLandingController extends AbstractController {
    protected final Log _log = LogFactory.getLog(getClass());

    public static final String PARAM_ID = "id";
    protected String _viewName;
    protected ICmsDiscussionBoardDao _cmsDiscussionBoardDao;
    protected IUserContentDao _userContentDao;

    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) {
        Map<String, Object> model = new HashMap<String, Object>();

        String idParam = request.getParameter(PARAM_ID);
        if (StringUtils.isNotBlank(idParam)) {
            String redirectUrl = null;
            try {
                int id = Integer.parseInt(idParam);
                redirectUrl = getDiscussionUrl(id, request);
            } catch (NumberFormatException nfe) {
                // invalid id.  Ignore, will be handled by default.
                _log.warn("CommunityLandingController invoked with invalid id param: '" + idParam + "'");
            }

            if (redirectUrl != null) {
                // 301 redirect to the UserContent
                return new ModelAndView(new RedirectView301(redirectUrl));
            } else {
                // If we couldn't determine a valid place to redirect to based on that legacy id then redirect
                // to this landing page without the parameter.  This looks cleaner than lots of old links coming
                // here with ?id=99 params that mean nothing.
                UrlBuilder urlBuilder = new UrlBuilder(UrlBuilder.COMMUNITY_LANDING);
                // 301 redirect to the Community Landing without id
                return new ModelAndView(new RedirectView301(urlBuilder.asSiteRelative(request)));
            }
        }

        return new ModelAndView(_viewName, model);
    }

    protected String getDiscussionUrl(int legacyId, HttpServletRequest request) {
        String redirectUrl = null;

        // find a UserContent by legacy id
        Discussion discussion = (Discussion)_userContentDao.findByLegacyId(legacyId, Discussion.class);
        if (discussion != null && discussion.isActive()) {
            CmsDiscussionBoard board = _cmsDiscussionBoardDao.get(discussion.getBoardId());
            if (board != null) {
                UrlBuilder urlBuilder = new UrlBuilder(UrlBuilder.COMMUNITY_DISCUSSION, board.getFullUri(),
                    Long.valueOf(discussion.getId()));
                redirectUrl = urlBuilder.asSiteRelative(request);
            }
        } else {
            _log.warn("CommunityLandingController invoked with non-discussion id param: '" + legacyId + "'");
        }

        return redirectUrl;
    }

    /*
     * Bean accessor/mutators below
     */
    public String getViewName() {
        return _viewName;
    }

    public void setViewName(String viewName) {
        _viewName = viewName;
    }

    public ICmsDiscussionBoardDao getCmsDiscussionBoardDao() {
        return _cmsDiscussionBoardDao;
    }

    public void setCmsDiscussionBoardDao(ICmsDiscussionBoardDao cmsDiscussionBoardDao) {
        _cmsDiscussionBoardDao = cmsDiscussionBoardDao;
    }

    public IUserContentDao getUserContentDao() {
        return _userContentDao;
    }

    public void setUserContentDao(IUserContentDao userContentDao) {
        _userContentDao = userContentDao;
    }
}
