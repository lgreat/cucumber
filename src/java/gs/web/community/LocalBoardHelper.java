package gs.web.community;


import gs.data.community.local.ILocalBoardDao;
import gs.data.community.local.LocalBoard;
import gs.data.geo.City;
import gs.web.search.SchoolSearchCommandWithFields;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component("localBoardHelper")
public class LocalBoardHelper {

    public static final String MODEL_LOCAL_BOARD_ID = "localBoardId";
    public static final String MODEL_LOCAL_CITY_NAME = "localCityName";

    @Autowired
    ILocalBoardDao _localBoardDao;

    public void putLocalBoardInfoIntoModel(Map<String,Object> model, City localCity) {
        if (localCity != null) {
            LocalBoard localBoard = _localBoardDao.findByCityId(localCity.getId());
            if (localBoard != null) {
                model.put(MODEL_LOCAL_BOARD_ID, localBoard.getBoardId());
                model.put(MODEL_LOCAL_CITY_NAME, localCity.getName());
            }
        }
    }

    public void putLocalBoardInfoIntoModel(Map<String,Object> model, SchoolSearchCommandWithFields commandAndFields) {
        City cityFromUrl = commandAndFields.getCityFromUrl();
        City localCity = (cityFromUrl != null ? cityFromUrl : commandAndFields.getCityFromSearchString());
        putLocalBoardInfoIntoModel(model, localCity);
    }
}
