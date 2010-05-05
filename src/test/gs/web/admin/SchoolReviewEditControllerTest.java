package gs.web.admin;

import gs.data.community.IReportedEntityDao;
import gs.data.school.review.IReviewDao;
import gs.data.school.review.Review;
import gs.web.BaseControllerTestCase;
import org.springframework.validation.BindException;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import static org.easymock.EasyMock.*;

/**
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class SchoolReviewEditControllerTest extends BaseControllerTestCase {
    private SchoolReviewEditController _controller;
//    private SchoolReviewEditCommand _command;
    private IReviewDao _reviewDao;
    private IReportedEntityDao _reportedEntityDao;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        _controller = new SchoolReviewEditController();

        _reviewDao = createStrictMock(IReviewDao.class);
        _reportedEntityDao = createStrictMock(IReportedEntityDao.class);

        _controller.setReviewDao(_reviewDao);
        _controller.setReportedEntityDao(_reportedEntityDao);

        _controller.setFormView("formView");
        _controller.setSuccessView("successView");
    }

    public void testBasics() {
        assertSame(_reviewDao, _controller.getReviewDao());
        assertSame(_reportedEntityDao, _controller.getReportedEntityDao());
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

}
