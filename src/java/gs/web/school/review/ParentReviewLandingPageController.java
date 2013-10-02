package gs.web.school.review;


import gs.data.community.User;
import gs.data.school.School;
import gs.data.school.review.IReviewTopicDao;
import gs.data.school.review.ReviewTag;
import gs.data.school.review.ReviewTopic;
import gs.web.request.RequestAttributeHelper;
import gs.web.util.PageHelper;
import gs.web.util.context.SessionContext;
import gs.web.util.context.SessionContextUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

/**
 * Most review submission handling done by <code>SchoolReviewAjaxController</code>
 *
 * @author <a href="mailto:ssprouse@greatschools.org">Samson Sprouse</a>
 */
public class ParentReviewLandingPageController extends AbstractController {

    public static final String BEAN_ID = "parentReview";
    private static final String MODEL_MORGAN_STANLEY = "morganStanley";
    private String _viewName;
    private boolean _morganStanley = false;
    @Autowired
    RequestAttributeHelper _requestAttributeHelper;
    @Autowired
    private IReviewTopicDao _reviewTopicDao;

    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
        Map<String,Object> model = new HashMap<String,Object>();
        SessionContext sessionContext = SessionContextUtil.getSessionContext(request);
        User user;
        if(PageHelper.isMemberAuthorized(request)){
            user = sessionContext.getUser();
            if (user != null) {
                model.put("validUser", user);
            }
        }
        School school = _requestAttributeHelper.getSchool(request);
        model.put("school", school);

        List<ReviewTopic> topics = _reviewTopicDao.findAll();
        model.put("topics", topics);
        Integer topicId = null;
        if (request.getParameter("topicId") != null) {
            try {
                topicId = Integer.parseInt(request.getParameter("topicId"));
            } catch (NumberFormatException nfe) {
                // log?
            }
        }
        if (topicId != null && topicId > 0) {
            for (ReviewTopic topic: topics) {
                if (topic.getId().intValue() == topicId) {
                    model.put("selectedTopic", topic);
                    Collections.sort(topic.getTags(), new Comparator<ReviewTag>() {
                        public int compare(ReviewTag tag1, ReviewTag tag2) {
                            if (tag1.isMain()) {
                                return -1;
                            } else if (tag2.isMain()) {
                                return 1;
                            } else {
                                return tag1.getName().compareTo(tag2.getName());
                            }
                        }
                    });
                    break;
                }
            }
        }

        model.put(MODEL_MORGAN_STANLEY, isMorganStanley());
        return new ModelAndView(getViewName(), model);
    }

    public String getViewName() {
        return _viewName;
    }

    public void setViewName(String viewName) {
        _viewName = viewName;
    }

    public boolean isMorganStanley() {
        return _morganStanley;
    }

    public void setMorganStanley(boolean morganStanley) {
        _morganStanley = morganStanley;
    }
}
