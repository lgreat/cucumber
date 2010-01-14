package gs.web.community;

import gs.data.community.IUserDao;
import gs.data.community.User;
import gs.data.util.CommunityUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class AvatarController extends AbstractController {
    protected static final Log _log = LogFactory.getLog(AvatarController.class);
    private IUserDao _userDao;
    public static final String MEMBER_ID_PARAM = "id";
    public static final String WIDTH_PARAM = "width";

    @Override
    protected ModelAndView handleRequestInternal(HttpServletRequest request,
                                                 HttpServletResponse response) throws Exception {
        int width = 48;
        try {
            width = Integer.valueOf(request.getParameter(WIDTH_PARAM));
        } catch (Exception e) {
            // ignore
            _log.warn("Can't determine width from request");
        }
        width = CommunityUtil.getClosestValidAvatarSize(width);
        String avatarPath = "stock/default-" + width + ".jpg";
        try {
            int userId = Integer.valueOf(request.getParameter(MEMBER_ID_PARAM));
            User user = _userDao.findUserFromId(userId);
            if (user == null) {
                _log.warn("Can't find user with id=" + userId);
            } else if (user.getUserProfile().getAvatarType().equals("custom")) {
                avatarPath = CommunityUtil.getAvatarPath(user.getId());
                avatarPath += CommunityUtil.getAvatarFilename(user.getId(), width, "jpg");
                avatarPath += "?v=" + user.getUserProfile().getAvatarVersion();
            } else {
                avatarPath = "stock/" + user.getUserProfile().getAvatarType() + "-" + width + ".jpg";
            }
        } catch (Exception e) {
            _log.warn("Error determining user information: " + e, e);
            // ignore. This will use the default stock avatar
        }
        response.setHeader("Cache-Control", "no-cache");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 0);
        String avatarUrl = CommunityUtil.getAvatarURLPrefix() + avatarPath;
        return new ModelAndView("redirect:" + avatarUrl);  //To change body of implemented methods use File | Settings | File Templates.
    }

    public IUserDao getUserDao() {
        return _userDao;
    }

    public void setUserDao(IUserDao userDao) {
        _userDao = userDao;
    }
}
