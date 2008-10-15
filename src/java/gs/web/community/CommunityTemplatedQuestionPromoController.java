package gs.web.community;

import gs.data.util.table.ITableRow;
import gs.data.state.State;
import gs.data.geo.City;
import gs.web.util.context.SessionContext;
import gs.web.util.context.SessionContextUtil;

import java.util.Map;
import java.util.Set;
import java.util.HashMap;
import java.net.URLEncoder;
import java.io.UnsupportedEncodingException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.http.HttpServletRequest;


/**
 * Created by IntelliJ IDEA.
 * User: jnorton
 * Date: Oct 9, 2008
 * Time: 4:18:57 PM
 * To change this template use File | Settings | File Templates.
 */
public class CommunityTemplatedQuestionPromoController extends CommunityQuestionPromoController{
    private static final Log _log = LogFactory.getLog(CommunityTemplatedQuestionPromoController.class);

    public static final String CITY_TARGET = "<city>";
    public static final String STATE_TARGET = "<state>";



    protected SessionContext _sessionContext;

    protected void fillModel(HttpServletRequest request, Map<String, Object> model, ITableRow row) {
        if (row != null) {
            SessionContext sessionContext = SessionContextUtil.getSessionContext(request);
            State state = sessionContext.getState();
            City city =  sessionContext.getCity();

            if (isTemplated(row)  ){
                //when a community quesiton is templated for city and state, and the state or city isn't known,
                //don't add the question to the model and view
                if ((state == null || city == null)){
                    return;
                }
                Map<String, String> targets = new HashMap<String, String>();
                targets.put("city", city.getName());
                targets.put("state", state.getLongName());
                model.put(MODEL_QUESTION_TEXT, replaceTargets(targets,(String)row.get("text"),false));
                model.put(MODEL_QUESTION_LINK, replaceTargets(targets,(String)row.get("link"), true));
                model.put(MODEL_QUESTION_LINK_TEXT, replaceTargets(targets,(String)row.get("linktext"), false));
            }else{
                model.put(MODEL_QUESTION_TEXT, row.get("text"));
                model.put(MODEL_QUESTION_LINK, row.get("link"));
                model.put(MODEL_QUESTION_LINK_TEXT, row.get("linktext"));
            }

            model.put(MODEL_USERNAME, row.get("username"));
            model.put(MODEL_USER_ID, row.get("memberid"));
        }
    }

    protected String urlEncode(String s){
        String result = "";
        try {
            result = URLEncoder.encode(s, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            _log.warn("Unable to urlEncode: " + s);
        }

        return result;
    }


    protected String replaceTargets(Map<String, String> targets, String s, boolean isLink){
        if (s != null){
            String cityReplaced = "";
            String stateAndCityReplaced = "";

            String state = targets.get("state");
            String city =  targets.get("city");

            if(isLink)   {
                cityReplaced = s.replace(CITY_TARGET, urlEncode(city.replaceAll("-", "_").replaceAll(" ", "-")));
                stateAndCityReplaced = cityReplaced.replace(STATE_TARGET, state.replaceAll(" ", "-"));
            }else{
                cityReplaced = s.replace(CITY_TARGET, city);
                stateAndCityReplaced = cityReplaced.replace(STATE_TARGET, state);
            }

            return stateAndCityReplaced;
        } else {
            return "";
        }
    }

    protected boolean isTemplated(ITableRow row){
        if (row != null) {
            Set columnNames = row.getColumnNames();

            for(Object columnName: columnNames){
                Object columnValue = row.get(columnName);
                if (columnValue != null && columnValue.getClass().isAssignableFrom(String.class)){
                    String val = (String)columnValue;
                    if (val.contains(CITY_TARGET) || val.contains(STATE_TARGET)){
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
