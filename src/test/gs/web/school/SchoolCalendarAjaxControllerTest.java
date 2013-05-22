package gs.web.school;

import gs.web.BaseControllerTestCase;
import gs.web.IntegrationTest;
import gs.web.SkipTest;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.UnsupportedEncodingException;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(value = {"classpath:gs/data/dao/hibernate/applicationContext-hibernate.xml", "classpath:gs/data/applicationContext-data.xml", "classpath:applicationContext.xml", "classpath:annotated-tests.xml", "classpath:pages-servlet.xml"})
@Category({SkipTest.class})
public class SchoolCalendarAjaxControllerTest extends BaseControllerTestCase {

    @Autowired
    SchoolCalendarAjaxController _controller;

    @Before
    public void setUp() {

    }

    @Test
    public void testRealApiCallForSchoolData() throws UnsupportedEncodingException {
        String ncesCode = "181281002036"; // Rhoades Elementary

        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        _controller.handleGet(ncesCode, response);

        assertTrue(
            "Expect api to return the correct school associated with our nces code",
            response.getContentAsString().contains("<xcal"));
    }
}
