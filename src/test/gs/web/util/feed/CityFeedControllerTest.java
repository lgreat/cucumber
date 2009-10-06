package gs.web.util.feed;

import gs.web.BaseControllerTestCase;
import org.custommonkey.xmlunit.XMLTestCase;

/**
 * @author thuss
 */
public class CityFeedControllerTest extends BaseControllerTestCase {

    private CityFeedController _cityFeedController;

    /* Gives us access to XMLUnit's XMLTestCase assert methods */
    private XMLTestCase _xml;

    protected void setUp() throws Exception {
        super.setUp();
        _cityFeedController = (CityFeedController) getApplicationContext().getBean(CityFeedController.BEAN_ID);
        _xml = new XMLTestCase();
    }

    public void testNoParameters() throws Exception {
        _cityFeedController.handleRequest(_request, _response);
        assertEquals("application/xml", _response.getContentType());
        _xml.assertXMLEqual("<error>You must either specify a state and city name or an id</error>", _response.getContentAsString());
    }

    public void testInvalidState() throws Exception {
        _request.setParameter("name", "foo");
        _request.setParameter("state", "XX");
        _cityFeedController.handleRequest(_request, _response);
        _xml.assertXMLEqual("<error>XX is an invalid state abbreviation</error>", _response.getContentAsString());
    }

    public void testMissingCityParameter() throws Exception {
        _request.setParameter("state", "CA");
        _cityFeedController.handleRequest(_request, _response);
        _xml.assertXMLEqual("<error>You must either specify a state and city name or an id</error>", _response.getContentAsString());
    }

    public void testInvalidCity() throws Exception {
        _request.setParameter("name", "Xyz");
        _request.setParameter("state", "CA");
        _cityFeedController.handleRequest(_request, _response);
        _xml.assertXMLEqual("<error>Xyz is an unknown city</error>", _response.getContentAsString());
    }

    public void testUnknownId() throws Exception {
        _request.setParameter("id", "123");
        _cityFeedController.handleRequest(_request, _response);
        _xml.assertXMLEqual("<error>Could not find city with id 123</error>", _response.getContentAsString());
    }

    public void testInvalidId() throws Exception {
        _request.setParameter("id", "XYZ");
        _cityFeedController.handleRequest(_request, _response);
        _xml.assertXMLEqual("<error>XYZ is an invalid city id</error>", _response.getContentAsString());
    }

    public void testActiveCityWithRating() throws Exception {
        _request.setParameter("name", "Alameda");
        _request.setParameter("state", "CA");
        _cityFeedController.handleRequest(_request, _response);
        _xml.assertXMLEqual(
                "<city>\n" +
                        "<id>135457</id>\n" +
                        "<name>Alameda</name>\n" +
                        "<state>CA</state>\n" +
                        "<rating>8</rating>\n" +
                        "<url>http://www.greatschools.net/california/alameda/</url>\n" +
                        "<lat>37.764</lat>\n" +
                        "<lon>-122.257</lon>\n" +
                        "<active>1</active>\n" +
                        "</city>",
                _response.getContentAsString());
    }

    public void testCallingCityById() throws Exception {
        _request.setParameter("id", "135457");
        _cityFeedController.handleRequest(_request, _response);
        _xml.assertXMLEqual(
                "<city>\n" +
                        "<id>135457</id>\n" +
                        "<name>Alameda</name>\n" +
                        "<state>CA</state>\n" +
                        "<rating>8</rating>\n" +
                        "<url>http://www.greatschools.net/california/alameda/</url>\n" +
                        "<lat>37.764</lat>\n" +
                        "<lon>-122.257</lon>\n" +
                        "<active>1</active>\n" +
                        "</city>",
                _response.getContentAsString());
    }

    public void testActiveCityWithoutRating() throws Exception {
        _request.setParameter("name", "Moss Landing");
        _request.setParameter("state", "CA");
        _cityFeedController.handleRequest(_request, _response);
        _xml.assertXMLEqual(
                "<city>\n" +
                        "<id>154529</id>\n" +
                        "<name>Moss Landing</name>\n" +
                        "<state>CA</state>\n" +
                        "<rating/>\n" +
                        "<url>http://www.greatschools.net/california/moss-landing/</url>\n" +
                        "<lat>36.7953</lat>\n" +
                        "<lon>-121.785</lon>\n" +
                        "<active>1</active>\n" +
                        "</city>",
                _response.getContentAsString());
    }

    public void testInactiveCity() throws Exception {
        _request.setParameter("name", "Roytown");
        _request.setParameter("state", "CA");
        _cityFeedController.handleRequest(_request, _response);
        _xml.assertXMLEqual(
                "<city>\n" +
                        "<id>154577</id>\n" +
                        "<name>Roytown</name>\n" +
                        "<state>CA</state>\n" +
                        "<rating/>\n" +
                        "<url/>\n" +
                        "<lat>43.1107</lat>\n" +
                        "<lon>-75.2733</lon>\n" +
                        "<active>0</active>\n" +
                        "</city>",
                _response.getContentAsString());
    }
}
