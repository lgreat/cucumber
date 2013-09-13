package gs.web.school.review;

import gs.data.school.ISchoolDao;
import gs.data.school.School;
import gs.data.school.review.*;
import gs.data.state.State;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * @author aroy@greatschools.org
 */
@Controller
@RequestMapping("/school/topicReview.page")
public class TopicalSchoolReviewTestController {
    public static final String PARAM_STATE = "state";
    public static final String PARAM_SCHOOL_ID = "schoolId";
    public static final String PARAM_TOPIC_ID = "topicId";
    public static final String VIEW = "school/topicReview";

    @Autowired
    private ITopicalSchoolReviewDao _topicalSchoolReviewDao;
    @Autowired
    private IReviewTopicDao _reviewTopicDao;
    @Autowired
    private IReviewDao _reviewDao;
    @Autowired
    private ISchoolDao _schoolDao;

    @RequestMapping(method= RequestMethod.GET)
    public String show(ModelMap modelMap, HttpServletRequest request, HttpServletResponse response,
                       @RequestParam(value=PARAM_SCHOOL_ID, required=false) Integer schoolId,
                       @RequestParam(value=PARAM_TOPIC_ID, required=false) Integer topicId,
                       @RequestParam(value=PARAM_STATE, required=false) State state) {

        List<ReviewTopic> topics = _reviewTopicDao.findAll();
        modelMap.put("topics", topics);
        ReviewTopic selectedTopic = null;
        if (topicId != null && topicId > 0) {
            for (ReviewTopic topic: topics) {
                if (topic.getId().intValue() == topicId) {
                    selectedTopic = topic;
                }
            }
        }
        modelMap.put("selectedTopic", selectedTopic);
        if (state != null && schoolId != null) {
            List<ReviewDisplay> displayReviews = new ArrayList<ReviewDisplay>();
            School school = _schoolDao.getSchoolById(state, schoolId);
            modelMap.put("school", school);
            List<TopicalSchoolReview> topicalReviews = null;
            if (selectedTopic != null) {
                topicalReviews = _topicalSchoolReviewDao.findBySchoolIdAndTopic(state, schoolId, selectedTopic.getId());
            } else {
                if (topicId == null || topicId != 0) {
                    topicalReviews = _topicalSchoolReviewDao.findBySchoolId(state, schoolId);
                }
                List<Review> reviews = _reviewDao.findPublishedNonPrincipalReviewsBySchool(school);
                for (Review review: reviews) {
                    displayReviews.add(new ReviewDisplay(review));
                }
            }
            if (topicalReviews != null) {
                for (TopicalSchoolReview topicalReview: topicalReviews) {
                    displayReviews.add(new ReviewDisplay(topicalReview));
                }
            }
            Collections.sort(displayReviews);
            modelMap.put("reviews", displayReviews);
        }
        return VIEW;
    }

    public static class ReviewDisplay implements Comparable {
        private String _comments;
        private Date _created;
        private String _who;
        private ReviewTopic _topic;
        private CategoryRating _rating;
        private Integer _memberId;

        public ReviewDisplay(Review review) {
            _comments = review.getComments();
            _created = review.getPosted();
            _who = review.getWho();
            _topic = null;
            _rating = (review.getQuality() != null)? review.getQuality() : review.getPOverall();
            _memberId = review.getMemberId();
        }
        public ReviewDisplay(TopicalSchoolReview review) {
            _comments = review.getComments();
            _created = review.getCreated();
            _who = (review.getWho() != null)?review.getWho().getName():null;
            _topic = review.getTopic();
            _rating = review.getRating();
            _memberId = review.getMemberId();
        }

        public String getComments() {
            return _comments;
        }

        public Date getCreated() {
            return _created;
        }

        public String getWho() {
            return _who;
        }

        public ReviewTopic getTopic() {
            return _topic;
        }

        public CategoryRating getRating() {
            return _rating;
        }

        public Integer getMemberId() {
            return _memberId;
        }

        public int compareTo(Object o) {
            if (o != null && o instanceof ReviewDisplay) {
                ReviewDisplay other = (ReviewDisplay) o;
                return other.getCreated().compareTo(this.getCreated());
            }
            return 1;
        }
    }
}
