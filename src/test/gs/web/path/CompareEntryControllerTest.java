package gs.web.path;

import gs.web.BaseControllerTestCase;
import org.springframework.web.servlet.ModelAndView;

/**
 * @author Chris Kimm <mailto:chriskimm@greatschools.net>
 */
public class CompareEntryControllerTest extends BaseControllerTestCase {

    public void testOnSubmit() throws Exception {
        CompareEntryController controller = new CompareEntryController();
        CompareEntryCommand command = new CompareEntryCommand();
        //ModelAndView mAndV = controller.onSubmit(command);
    }
}
