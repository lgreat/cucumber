package gs.web.admin;

import gs.data.community.IReportedEntityDao;
import gs.data.school.review.IReviewDao;
import gs.data.school.review.ITopicalSchoolReviewDao;
import gs.web.BaseControllerTestCase;

import static org.easymock.EasyMock.*;

/**
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class SchoolReviewListControllerTest extends BaseControllerTestCase {
    private SchoolReviewListController _controller;
    private IReviewDao _reviewDao;
    private IReportedEntityDao _reportedEntityDao;
    private ITopicalSchoolReviewDao _topicalSchoolReviewDao;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        _controller = new SchoolReviewListController();

        _reviewDao = createStrictMock(IReviewDao.class);
        _reportedEntityDao = createStrictMock(IReportedEntityDao.class);
        _topicalSchoolReviewDao = createStrictMock(ITopicalSchoolReviewDao.class);

        _controller.setReviewDao(_reviewDao);
        _controller.setReportedEntityDao(_reportedEntityDao);
        _controller.setTopicalSchoolReviewDao(_topicalSchoolReviewDao);

        _controller.setViewName("view");
    }

    public void testBasics() {
        assertSame(_reviewDao, _controller.getReviewDao());
        assertSame(_reportedEntityDao, _controller.getReportedEntityDao());
        assertSame(_topicalSchoolReviewDao, _controller.getTopicalSchoolReviewDao());
        assertEquals("view", _controller.getViewName());
    }
}
