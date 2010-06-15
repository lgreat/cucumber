package gs.web.admin;

import gs.data.community.IReportedEntityDao;
import gs.data.community.IUserDao;
import gs.data.integration.exacttarget.ExactTargetAPI;
import gs.data.school.ISchoolDao;
import gs.data.school.School;
import gs.data.school.SchoolType;
import gs.data.school.review.IReviewDao;
import gs.data.school.review.Poster;
import gs.data.school.review.Review;
import gs.data.state.State;
import gs.web.BaseControllerTestCase;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import static org.easymock.classextension.EasyMock.*;

/**
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class SchoolReviewEditControllerTest extends BaseControllerTestCase {
    private SchoolReviewEditController _controller;
//    private SchoolReviewEditCommand _command;
    private IReviewDao _reviewDao;
    private IReportedEntityDao _reportedEntityDao;
    private IUserDao _userDao;
    private ISchoolDao _schoolDao;
    private ExactTargetAPI _exactTargetAPI;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        _controller = new SchoolReviewEditController();

        _reviewDao = createStrictMock(IReviewDao.class);
        _reportedEntityDao = createStrictMock(IReportedEntityDao.class);
        _userDao = createStrictMock(IUserDao.class);
        _schoolDao = createStrictMock(ISchoolDao.class);
        _exactTargetAPI = createStrictMock(ExactTargetAPI.class);

        _controller.setReviewDao(_reviewDao);
        _controller.setReportedEntityDao(_reportedEntityDao);
        _controller.setUserDao(_userDao);
        _controller.setSchoolDao(_schoolDao);
        _controller.setExactTargetAPI(_exactTargetAPI);

        _controller.setFormView("formView");
        _controller.setSuccessView("successView");
    }

    public void testBasics() {
        assertSame(_reviewDao, _controller.getReviewDao());
        assertSame(_reportedEntityDao, _controller.getReportedEntityDao());
        assertSame(_userDao, _controller.getUserDao());
        assertSame(_schoolDao, _controller.getSchoolDao());
        assertSame(_exactTargetAPI, _controller.getExactTargetAPI());
    }
    
    public void testProcessDateSetOnDisable() throws Exception {
        SchoolReviewEditCommand command = new SchoolReviewEditCommand();

        Review review = new Review();
        review.setId(1);
        command.setReview(review);

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        getRequest().setParameter("disableReview", "true");
        _controller.onSubmit(getRequest(), getResponse(), command, null);
        assertEquals("Controller should set processed date to current date if review becomes disabled", format.format(Calendar.getInstance().getTime()), format.format(review.getProcessDate()));
    }

    public void testPrincipalReviewsDisabledWhenPrincipalReviewEnabled() throws Exception {
        //Only one principal review should be published and enabled for a school.

        SchoolReviewEditCommand command = new SchoolReviewEditCommand();

        Review review = new Review();
        review.setId(1);
        School school = new School();
        school.setId(1);
        school.setDatabaseState(State.CA);
        school.setType(SchoolType.PUBLIC);
        review.setSchool(school);
        review.setPoster(Poster.PRINCIPAL);

        command.setReview(review);

        getRequest().setParameter("enableReview", "true");

        List<Review> reviews = new ArrayList<Review>();
        Review oldReview1 = new Review();
        oldReview1.setId(2);
        oldReview1.setPoster(Poster.PRINCIPAL);
        oldReview1.setNote("Moderator note.");
        Review oldReview2 = new Review();
        oldReview2.setId(3);
        oldReview2.setPoster(Poster.PRINCIPAL);
        oldReview2.setStatus("p");
        reviews.add(oldReview1);
        reviews.add(oldReview2);

        expect(_reviewDao.findPublishedPrincipalReviewsBySchool(isA(School.class))).andReturn(reviews);
        _reviewDao.saveReview(eq(oldReview1));
        _reviewDao.saveReview(eq(oldReview2));
        _reviewDao.saveReview(eq(review));

        replay(_reviewDao);
        _controller.onSubmit(getRequest(), getResponse(), command, null);
        verify(_reviewDao);

        assertEquals("Review should have been disabled", "d", oldReview1.getStatus());
        assertEquals("Review should have been disabled", "d", oldReview2.getStatus());
        assertEquals("Review should have been enabled", "p", review.getStatus());
    }

}
