package gs.web.admin;

import gs.data.community.IReportedEntityDao;
import gs.data.community.IUserDao;
import gs.data.community.ReportedEntity;
import gs.data.integration.exacttarget.ExactTargetAPI;
import gs.data.school.ISchoolDao;
import gs.data.school.School;
import gs.data.school.review.ITopicalSchoolReviewDao;
import gs.data.school.review.Poster;
import gs.data.school.review.TopicalSchoolReview;
import gs.data.state.State;
import gs.web.BaseControllerTestCase;
import org.springframework.web.servlet.ModelAndView;

import java.util.Map;

import static org.easymock.classextension.EasyMock.*;

/**
 * @author aroy@greatschools.org
 */
public class TopicalSchoolReviewEditControllerTest extends BaseControllerTestCase {
    private TopicalSchoolReviewEditController _controller;
    private ITopicalSchoolReviewDao _topicalSchoolReviewDao;
    private IReportedEntityDao _reportedEntityDao;
    private IUserDao _userDao;
    private ISchoolDao _schoolDao;
    private ExactTargetAPI _exactTargetAPI;
    private TopicalSchoolReviewEditCommand _command;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        _controller = new TopicalSchoolReviewEditController();

        _topicalSchoolReviewDao = createStrictMock(ITopicalSchoolReviewDao.class);
        _reportedEntityDao = createStrictMock(IReportedEntityDao.class);
        _userDao = createStrictMock(IUserDao.class);
        _schoolDao = createStrictMock(ISchoolDao.class);
        _exactTargetAPI = createStrictMock(ExactTargetAPI.class);

        _controller.setTopicalSchoolReviewDao(_topicalSchoolReviewDao);
        _controller.setReportedEntityDao(_reportedEntityDao);
        _controller.setUserDao(_userDao);
        _controller.setSchoolDao(_schoolDao);
        _controller.setExactTargetAPI(_exactTargetAPI);

        _controller.setFormView("formView");
        _controller.setSuccessView("successView");

        _command = new TopicalSchoolReviewEditCommand();
    }

    public void replayAllMocks() {
        replayMocks(_topicalSchoolReviewDao, _reportedEntityDao, _userDao, _schoolDao, _exactTargetAPI);
    }

    public void verifyAllMocks() {
        verifyMocks(_topicalSchoolReviewDao, _reportedEntityDao, _userDao, _schoolDao, _exactTargetAPI);
    }

    public void testBasics() {
        assertSame(_topicalSchoolReviewDao, _controller.getTopicalSchoolReviewDao());
        assertSame(_reportedEntityDao, _controller.getReportedEntityDao());
        assertSame(_userDao, _controller.getUserDao());
        assertSame(_schoolDao, _controller.getSchoolDao());
        assertSame(_exactTargetAPI, _controller.getExactTargetAPI());
    }

    public void testDisableReview() throws Exception {
        TopicalSchoolReview review = new TopicalSchoolReview();
        review.setId(1L);
        review.setStatus("p");
        _command.setReview(review);
        assertNull("Expect process date to be null on new review", review.getProcessDate());

        getRequest().setParameter("disableReview", "true");

        _topicalSchoolReviewDao.save(review);
        replayAllMocks();
        _controller.onSubmit(getRequest(), getResponse(), _command, null);
        verifyAllMocks();
        assertEquals("Expect review to be disabled", "d", review.getStatus());
        assertNotNull("Controller should set processed date to current date if review becomes disabled",
                review.getProcessDate());
    }

    public void testSubmitNote() throws Exception {
        getRequest().setParameter("submitNote", "true");
        _command.setNote("Note");

        TopicalSchoolReview review = new TopicalSchoolReview();
        review.setId(1L);
        review.setStatus("d");
        _command.setReview(review);
        assertNull("Expect note to be null on new review", review.getNote());

        _topicalSchoolReviewDao.save(review);
        replayAllMocks();
        _controller.onSubmit(getRequest(), getResponse(), _command, null);
        verifyAllMocks();
        assertNotNull("Expect note to have been set by the controller", review.getNote());
        assertEquals("Expect note to have been set to command", _command.getNote(), review.getNote());
        assertEquals("Expect review status to be unchanged", "d", review.getStatus());
    }

    public void testCancel() throws Exception {
        getRequest().setParameter("formCancel", "true");

        TopicalSchoolReview review = new TopicalSchoolReview();
        review.setId(1L);
        review.setStatus("d");
        _command.setReview(review);
        _command.setFrom("flagged");

        replayAllMocks();
        ModelAndView modelAndView = _controller.onSubmit(getRequest(), getResponse(), _command, null);
        verifyAllMocks(); // expect no database calls
        assertEquals("Expect form to send user to success view on cancel", "successView", modelAndView.getViewName());
    }

    public void testResolveReports() throws Exception {
        getRequest().setParameter("resolveReports", "true");

        TopicalSchoolReview review = new TopicalSchoolReview();
        review.setId(1L);
        review.setStatus("d");
        _command.setReview(review);

        // It's not clear to me that saving the review is required when resolving the reports
        // So I'm making it optional in the test
        _topicalSchoolReviewDao.save(review);
        expectLastCall().times(0, 1);
        _reportedEntityDao.resolveReportsFor(ReportedEntity.ReportedEntityType.topicalSchoolReview, 1L);
        replayAllMocks();
        _controller.onSubmit(getRequest(), getResponse(), _command, null);
        verifyAllMocks();
    }

    public void testEnableReviewParent() throws Exception{
        getRequest().setParameter("enableReview", "true");

        TopicalSchoolReview review = new TopicalSchoolReview();
        review.setId(1L);
        review.setStatus("d");
        _command.setReview(review);
        assertNull("Expect process date to be null on new review", review.getProcessDate());

        _topicalSchoolReviewDao.save(review);
        replayAllMocks();
        _controller.onSubmit(getRequest(), getResponse(), _command, null);
        verifyAllMocks();
        assertEquals("p", review.getStatus());
        assertNotNull(review.getProcessDate());
    }

    public void testEnableReviewStudent() throws Exception{
        getRequest().setParameter("enableReview", "true");

        TopicalSchoolReview review = new TopicalSchoolReview();
        review.setId(1L);
        review.setStatus("u");
        review.setWho(Poster.STUDENT);
        review.setSchool(getSchool());
        _command.setReview(review);
        assertNull("Expect process date to be null on new review", review.getProcessDate());

        _topicalSchoolReviewDao.save(review);
        _exactTargetAPI.sendTriggeredEmail(eq("review_posted_trigger"),eq(review.getUser()), isA(Map.class));
        replayAllMocks();
        _controller.onSubmit(getRequest(), getResponse(), _command, null);
        verifyAllMocks();
        assertEquals("p", review.getStatus());
        assertNotNull(review.getProcessDate());
    }

    public void testEnableReviewHeld() throws Exception{
        getRequest().setParameter("enableReview", "true");

        TopicalSchoolReview review = new TopicalSchoolReview();
        review.setId(1L);
        review.setStatus("h");
        review.setSchool(getSchool());
        _command.setReview(review);
        assertNull("Expect process date to be null on new review", review.getProcessDate());

        _topicalSchoolReviewDao.save(review);
        _exactTargetAPI.sendTriggeredEmail(eq("review_posted_trigger"),eq(review.getUser()), isA(Map.class));
        replayAllMocks();
        _controller.onSubmit(getRequest(), getResponse(), _command, null);
        verifyAllMocks();
        assertEquals("p", review.getStatus());
        assertNotNull(review.getProcessDate());
    }

    private School getSchool() {
        School school = new School();
        school.setName("Alameda High School");
        school.setId(1);
        school.setDatabaseState(State.CA);
        school.setCity("Alameda");
        return school;
    }
}
