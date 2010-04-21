package gs.web.admin;

import gs.data.community.IReportedEntityDao;
import gs.data.school.review.IReviewDao;
import gs.web.BaseControllerTestCase;

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
}
