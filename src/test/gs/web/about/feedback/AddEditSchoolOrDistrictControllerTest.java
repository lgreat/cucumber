package gs.web.about.feedback;

import gs.data.school.ISchoolDao;
import gs.web.BaseControllerTestCase;

import static org.easymock.EasyMock.createStrictMock;

/**
 * Created with IntelliJ IDEA.
 * User: eddie
 * Date: 3/28/13
 * Time: 5:20 PM
 * To change this template use File | Settings | File Templates.
 */
public class AddEditSchoolOrDistrictControllerTest  extends BaseControllerTestCase {
    private AddEditSchoolOrDistrictController _controller;
    private ISchoolDao _schoolDao;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        _controller = new AddEditSchoolOrDistrictController();

        _schoolDao = createStrictMock(ISchoolDao.class);
    }

    public void testBasics() {
        assertEquals("command", _controller.getCommandName());
    }

}
