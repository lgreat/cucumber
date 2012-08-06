package gs.web;

import gs.web.request.RequestAttributeHelper;

import static org.easymock.classextension.EasyMock.*;
/**
 * @author aroy@greatschools.org
 */
public class FruitcakeControllerFamilyResolverTest extends BaseControllerTestCase {
    public FruitcakeControllerFamilyResolver _resolver;

    private RequestAttributeHelper _requestAttributeHelper;

    public void setUp() throws Exception {
        super.setUp();

        _resolver = new FruitcakeControllerFamilyResolver();

        _requestAttributeHelper = createStrictMock(RequestAttributeHelper.class);

        _resolver.setRequestAttributeHelper(_requestAttributeHelper);
    }

    public void testBasics() {
        assertSame(_requestAttributeHelper, _resolver.getRequestAttributeHelper());
    }

    // Tests pending new implementation
}