package gs.web.community;

import gs.data.community.local.ILocalBoardDao;
import gs.data.community.local.LocalBoard;
import gs.data.geo.City;
import gs.data.util.CommunityUtil;
import gs.web.util.PageHelper;
import gs.web.util.context.SessionContextUtil;
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
    public static final String MODEL_LOCAL_BOARD_ID = "localBoardId";
    public static final String MODEL_CITY_NAME = "cityName";
    protected String _viewName;
    protected ICmsDiscussionBoardDao _cmsDiscussionBoardDao;
    protected IUserContentDao _userContentDao;
    private ILocalBoardDao _localBoardDao;

    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) {
        Map<String, Object> model = new HashMap<String, Object>();

        City city = SessionContextUtil.getSessionContext(request).getCity();
        if (city != null) {
            LocalBoard localBoard = _localBoardDao.findByCityId(city.getId());
            if (localBoard != null) {
                model.put(MODEL_LOCAL_BOARD_ID, localBoard.getBoardId());
                model.put(MODEL_CITY_NAME, city.getDisplayName());
            }
        }

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

        PageHelper pageHelper = (PageHelper) request.getAttribute(PageHelper.REQUEST_ATTRIBUTE_NAME);
        if (pageHelper != null) {
            pageHelper.addAdKeyword(CommunityUtil.COMMUNITY_GAM_AD_ATTRIBUTE_KEY, String.valueOf(true));
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

    public ILocalBoardDao getLocalBoardDao() {
        return _localBoardDao;
    }

    public void setLocalBoardDao(ILocalBoardDao localBoardDao) {
        _localBoardDao = localBoardDao;
    }
}
