package gs.web.tracking;

import gs.web.util.context.SubCookie;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.HashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Created by IntelliJ IDEA.
 * User: jnorton
 * Date: Aug 4, 2008
 * Time: 11:25:17 AM
 * To change this template use File | Settings | File Templates.
 */
public class OmnitureSuccessEvent {

    protected final static Log _log = LogFactory.getLog(OmnitureSuccessEvent.class);

    public  enum SuccessEvent {
       
        CommunityRegistration(6),
        ArticleView(7),
        ParentRating(8),
        ParentReview(9),
        ParentSurvey(10),
        NewNewsLetterSubscriber(11);

        private Integer _eventNumber;
        SuccessEvent(Integer eventNumber){
            _eventNumber = eventNumber;
        }

        public String toOmnitureSuccessEvent(){
            return  "event" + _eventNumber + ";";
        }

        protected Integer getEventNumber(){
            return _eventNumber;
        }
    }


    protected HttpServletRequest _request;
    protected HttpServletResponse _response;
    protected String _events = "";

    protected SubCookie _subCookie;

    public OmnitureSuccessEvent(HttpServletRequest request, HttpServletResponse response){
        _request = request;
        _response = response;
        _subCookie = new SubCookie(request, response);
        _events = (String) _subCookie.getProperty("events");
        _events = _events == null ? "" : _events;

        _log.info("events: " + _events);
        // clean up residual???
        _subCookie.setProperty("events", _events);
    }

    public void add(SuccessEvent successEvent){
        _log.info("add(SuccessEvent." + successEvent + ", " + _events + ")");
        _events = addEvent(successEvent, _events);
        _subCookie.setProperty("events", _events);
    }



    static String addEvent(SuccessEvent successEvent, String destination){
        if (successEvent == null && destination == null){
            return "";
        }else if (successEvent == null){
            return destination;
        }else if (destination == null){
            return successEvent.toOmnitureSuccessEvent();
        }

        if (destination.contains(successEvent.toOmnitureSuccessEvent())){
            return destination;
        } else {
            return destination + successEvent.toOmnitureSuccessEvent();
        }
    }
}
