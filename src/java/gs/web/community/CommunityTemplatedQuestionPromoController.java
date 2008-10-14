package gs.web.community;

import gs.data.util.table.ITableRow;
import gs.web.util.context.SessionContext;

import java.util.Map;
import java.util.Set;



/**
 * Created by IntelliJ IDEA.
 * User: jnorton
 * Date: Oct 9, 2008
 * Time: 4:18:57 PM
 * To change this template use File | Settings | File Templates.
 */
public class CommunityTemplatedQuestionPromoController extends CommunityQuestionPromoController{


    public static final String CITY_TARGET = "<city>";
    public static final String STATE_TARGET = "<state>";


    protected SessionContext _sessionContext;

    protected void fillModel(Map<String, Object> model, ITableRow row) {
        if (row != null) {
            //getCityAndStateFromSession();

            if (isTemplated(row)  ){
                //when a community quesiton is templated for city and state, and the state or city isn't known,
                //don't add the question to the model and view
                if ((_state == null || _city == null)){
                    return;
                }
                model.put(MODEL_QUESTION_TEXT, replaceTargets((String)row.get("text"),false));
                model.put(MODEL_QUESTION_LINK, replaceTargets((String)row.get("link"), true));
                model.put(MODEL_QUESTION_LINK_TEXT, replaceTargets((String)row.get("linktext"), false));
            }else{
                model.put(MODEL_QUESTION_TEXT, row.get("text"));
                model.put(MODEL_QUESTION_LINK, row.get("link"));
                model.put(MODEL_QUESTION_LINK_TEXT, row.get("linktext"));
            }

            model.put(MODEL_USERNAME, row.get("username"));
            model.put(MODEL_USER_ID, row.get("memberid"));
        }
    }


    protected String replaceTargets(String s, boolean isLink){
        if (s != null){
            String cityReplaced = "";
            String stateAndCityReplaced = "";

            if(isLink)   {
                cityReplaced = s.replace(CITY_TARGET, _city.getName().replaceAll("-", "_").replaceAll(" ", "-"));
                stateAndCityReplaced = cityReplaced.replace(STATE_TARGET, _state.getLongName().replaceAll(" ", "-"));
            }else{
                cityReplaced = s.replace(CITY_TARGET, _city.getName());
                stateAndCityReplaced = cityReplaced.replace(STATE_TARGET, _state.getLongName());
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
