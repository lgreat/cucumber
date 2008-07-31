package gs.web.util.feed;

import org.springframework.web.servlet.mvc.Controller;
import org.springframework.web.servlet.ModelAndView;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import static org.apache.commons.lang.StringUtils.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.*;
import javax.xml.transform.stream.StreamResult;
import java.io.Writer;

import gs.data.geo.City;
import gs.data.geo.IGeoDao;
import gs.data.test.rating.CityRating;
import gs.data.test.rating.ICityRatingDao;
import gs.data.state.State;
import gs.data.state.StateManager;
import gs.web.util.UrlBuilder;

/**
 * This controller provides the same data as GSFeed for an individual city
 *
 * @author thuss
 */
public class CityFeedController implements Controller {

    /**
     * Spring BEAN id
     */
    public static final String BEAN_ID = "/util/feed/city.page";

    private IGeoDao _geoDao;

    private ICityRatingDao _cityRatingDao;

    private StateManager _stateManager;

    /**
     * @see gs.data.util.email.EmailUtils
     */
    public ModelAndView handleRequest(HttpServletRequest request,
                                      HttpServletResponse response) throws Exception {
        Document doc;
        String error = null;
        City city = null;
        CityRating cityRating = null;

        String idString = request.getParameter("id");
        String stateName = request.getParameter("state");
        String cityName = request.getParameter("name");

        if (isNotEmpty(idString)) {
            try {
                city = _geoDao.findCityById(new Integer(idString));
                if (city == null) error = "Could not find city with id " + idString;
            } catch (NumberFormatException e) {
                error = idString + " is an invalid city id";
            }
        } else if (isNotEmpty(cityName) && isNotEmpty(stateName)) {
            // Validate the state
            State state = _stateManager.getState(stateName);
            if (state == null) {
                error = stateName + " is an invalid state abbreviation";
            } else {
                // Validate the city
                city = _geoDao.findCity(state, cityName, true);
                if (city == null) error = cityName + " is an unknown city";
            }
        } else {
            error = "You must either specify a state and city name or an id";
        }

        // Get the city rating
        if (city != null) {
            try {
                cityRating = _cityRatingDao.getCityRatingByCity(city.getState(), city.getName());
            } catch (Exception e) {
                // Do nothing
            }
        }

        if (error == null) {
            // Build the XML for the city
            doc = getDocument("city");
            appendElement(doc, "id", city.getId().toString());
            appendElement(doc, "name", city.getDisplayName());
            appendElement(doc, "state", city.getState().getAbbreviation());
            appendElement(doc, "rating", (cityRating == null) ? null : cityRating.getRating().toString());
            UrlBuilder builder = new UrlBuilder(UrlBuilder.CITY_PAGE, city.getState(), city.getName());
            appendElement(doc, "url", city.isActive() ? builder.asFullUrl(request) : null);
            appendElement(doc, "lat", String.valueOf(city.getLat()));
            appendElement(doc, "lon", String.valueOf(city.getLon()));
            appendElement(doc, "active", city.isActive() ? "1" : "0");
        } else {
            doc = getDocument("error");
            doc.getDocumentElement().setTextContent(error);
        }

        // Write out the servlet response
        response.setContentType("application/xml");
        serializeDocument(response.getWriter(), doc);
        return null;
    }

    /**
     * Create the XML document
     */
    private Document getDocument(String name) throws ParserConfigurationException {
        final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        final DocumentBuilder builder = factory.newDocumentBuilder();
        final DOMImplementation impl = builder.getDOMImplementation();
        return impl.createDocument(null, name, null);
    }

    /**
     * Write the document to the servlet response
     */
    private void serializeDocument(Writer out, Document doc) throws TransformerException {
        DOMSource domSource = new DOMSource(doc);
        StreamResult streamResult = new StreamResult(out);
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer serializer = tf.newTransformer();
        serializer.setOutputProperty(OutputKeys.INDENT, "yes");
        serializer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        serializer.transform(domSource, streamResult);
    }

    /**
     * Helper method to create an element, set its text content, and append it
     */
    private void appendElement(Document doc, String name, String content) {
        Element element = doc.createElement(name);
        if (content != null) element.setTextContent(content);
        doc.getDocumentElement().appendChild(element);
    }

    public void setGeoDao(IGeoDao geoDao) {
        _geoDao = geoDao;
    }

    public void setCityRatingDao(ICityRatingDao cityRatingDao) {
        _cityRatingDao = cityRatingDao;
    }

    public void setStateManager(StateManager stateManager) {
        _stateManager = stateManager;
    }
}