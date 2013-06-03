package gs.web.school.usp;

import gs.data.school.EspResponse;
import gs.data.school.IEspResponseDao;
import gs.data.school.School;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.*;

@Component(value="espStatusManager")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class EspStatusManager {

    private School _school;

    private EspStatus _espStatus;

    private EspResponseData _espResponseData;

    @Autowired
    private IEspResponseDao _espResponseDao;

    public EspStatusManager(School school) {
        _school = school;
    }

    public EspStatusManager(School school, EspResponseData espResponseData) {
        this(school);
        _espResponseData = espResponseData;
    }

    public static Set<String> getOspKeySet() {
        //TODO check for all keys or maybe just check for 1 key and its timestamp
        return new HashSet<String>(
                Arrays.asList(
                        new String[]{
                                UspFormHelper.ARTS_MEDIA_RESPONSE_KEY,
                                UspFormHelper.ARTS_MUSIC_RESPONSE_KEY,
                                UspFormHelper.ARTS_PERFORMING_WRITTEN_RESPONSE_KEY,
                                UspFormHelper.ARTS_VISUAL_RESPONSE_KEY,
                                UspFormHelper.EXTENDED_CARE_RESPONSE_KEY,
                                UspFormHelper.GIRLS_SPORTS_RESPONSE_KEY,
                                UspFormHelper.GIRLS_SPORTS_OTHER_RESPONSE_KEY,
                                UspFormHelper.STAFF_RESPONSE_KEY,
                                UspFormHelper.FACILITIES_RESPONSE_KEY,
                                UspFormHelper.FOREIGN_LANGUAGES_RESPONSE_KEY,
                                UspFormHelper.FOREIGN_LANGUAGES_OTHER_RESPONSE_KEY,
                                UspFormHelper.TRANSPORTATION_RESPONSE_KEY,
                                UspFormHelper.TRANSPORTATION_OTHER_RESPONSE_KEY,
                                UspFormHelper.BOYS_SPORTS_RESPONSE_KEY,
                                UspFormHelper.BOYS_SPORTS_OTHER_RESPONSE_KEY,
                                UspFormHelper.PARENT_INVOLVEMENT_RESPONSE_KEY,
                                UspFormHelper.PARENT_INVOLVEMENT_OTHER_RESPONSE_KEY
                        }
                )
        );
    }

    public void onEspStatusChange(EspStatus oldStatus, EspStatus newStatus) {
        _espStatus = newStatus;

        switch (newStatus) {
            case OSP_PREFERRED:
                break;
            default:
                break;
        }

        /// ???
    }

    /**
     * Gets the EspStatus for this object's School. If it was previously calculated, just returns the old status.
     * Loads Esp data from the database if it has not already been done. If the underlying data has changed, a new
     * status is not returned.
     *
     * @return
     */
    public EspStatus getEspStatus() {
        if (_espStatus != null) {
            return _espStatus;
        }

        if (_school == null) {
            throw new IllegalStateException("School cannot be null");
        }

        if (_espResponseData == null) {
            loadEspResponsesFromDatabase();
        }

        _espStatus = getEspStatus(_espResponseData);
        return _espStatus;
    }

    /**
     * Loads a List of EspResponses from DB, and decorators it with a EspResponseData.
     * Stores result onto instance variable
     */
    public void loadEspResponsesFromDatabase() {
        List<EspResponse> ospResponses = _espResponseDao.getResponsesByKeys(_school, getOspKeySet());
        _espResponseData = new EspResponseData(ospResponses);
    }

    public EspStatus checkObjectForNewStatus() {

        EspStatus newStatus = getEspStatus(_espResponseData);

        if (_espStatus != newStatus) {
            onEspStatusChange(_espStatus, newStatus);
        }

        return newStatus;
    }

    /**
     * Gets the EspStatus for this object's School. Does not use any previous data that was loaded. Will load fresh
     * data each time
     * @return An updated EspStatus
     */
    public EspStatus checkDatabaseForNewStatus() {
        loadEspResponsesFromDatabase();

        return checkObjectForNewStatus();
    }

    /**
     * Given an EspResponseData, gets info about the responses and calculates an EspStatus
     * @return the correct EspStatus for a list of ESP data
     */
    public EspStatus getEspStatus(EspResponseData espResponseData) {
        EspStatus status;

        boolean allOSPQuestionsAnswered = allOSPQuestionsAnswered(espResponseData.getOspResponses());

        boolean isThereUSPData = espResponseData.hasUspResponseData();
        boolean isThereOSPData = espResponseData.hasOspResponseData();
        boolean isOspDataRecent = espResponseData.getOspResponses().hasRecentYearOfData();

        if (allOSPQuestionsAnswered && isOspDataRecent) {
            status = EspStatus.OSP_PREFERRED;
        } else if (isThereUSPData && !isThereOSPData) {
            status = EspStatus.USP_ONLY;
        } else if (!isThereUSPData && isThereOSPData && !isOspDataRecent) {
            status = EspStatus.OSP_OUTDATED;
        } else if (isThereUSPData && isThereOSPData) {
            status = EspStatus.MIX;
        } else {
            status = EspStatus.NO_DATA;
        }

        return status;
    }

    /**
     * Checks if all the question in the OSP gateway have been filled out.
     * Some of the questions on the gateway form have "other" text boxes. Hence need to check if either the
     * "other" text box or the multi-choice checkbox was answered in order to determine if a question has a response.
     * @param ospResponses
     * @return
     */
    public boolean allOSPQuestionsAnswered(IEspResponseData ospResponses) {
        Map<String, List<EspResponse>> ospResponsesByKey = ospResponses.getResponsesByKey();
        return allOSPQuestionsAnswered(ospResponsesByKey);
    }

    public boolean allOSPQuestionsAnswered(Map<String, ? extends Object> responseKeyLookUpMap) {
        boolean isGirlSportsAnswered = responseKeyLookUpMap.containsKey(UspFormHelper.GIRLS_SPORTS_RESPONSE_KEY) ||
                responseKeyLookUpMap.containsKey(UspFormHelper.GIRLS_SPORTS_OTHER_RESPONSE_KEY);

        boolean isForeignLanguagesAnswered = responseKeyLookUpMap.containsKey(UspFormHelper.FOREIGN_LANGUAGES_RESPONSE_KEY) ||
                responseKeyLookUpMap.containsKey(UspFormHelper.FOREIGN_LANGUAGES_OTHER_RESPONSE_KEY);

        boolean isTransportationAnswered = responseKeyLookUpMap.containsKey(UspFormHelper.TRANSPORTATION_RESPONSE_KEY) ||
                responseKeyLookUpMap.containsKey(UspFormHelper.TRANSPORTATION_OTHER_RESPONSE_KEY);

        boolean isBoySportsAnswered = responseKeyLookUpMap.containsKey(UspFormHelper.BOYS_SPORTS_RESPONSE_KEY) ||
                responseKeyLookUpMap.containsKey(UspFormHelper.BOYS_SPORTS_OTHER_RESPONSE_KEY);

        boolean isParentInvolvementAnswered = responseKeyLookUpMap.containsKey(UspFormHelper.PARENT_INVOLVEMENT_RESPONSE_KEY) ||
                responseKeyLookUpMap.containsKey(UspFormHelper.PARENT_INVOLVEMENT_OTHER_RESPONSE_KEY);

        Set<String> ospKeysNonOther = new HashSet<String>(
                Arrays.asList(
                        new String[]{
                                UspFormHelper.ARTS_MEDIA_RESPONSE_KEY,
                                UspFormHelper.ARTS_MUSIC_RESPONSE_KEY,
                                UspFormHelper.ARTS_PERFORMING_WRITTEN_RESPONSE_KEY,
                                UspFormHelper.ARTS_VISUAL_RESPONSE_KEY,
                                UspFormHelper.EXTENDED_CARE_RESPONSE_KEY,
                                UspFormHelper.STAFF_RESPONSE_KEY,
                                UspFormHelper.FACILITIES_RESPONSE_KEY
                        }
                ));

        if (responseKeyLookUpMap.keySet().containsAll(ospKeysNonOther) && isGirlSportsAnswered && isForeignLanguagesAnswered
                && isTransportationAnswered && isBoySportsAnswered && isParentInvolvementAnswered) {
            return true;
        }
        return false;
    }

    public IEspResponseDao getEspResponseDao() {
        return _espResponseDao;
    }

    public void setEspResponseDao(IEspResponseDao espResponseDao) {
        _espResponseDao = espResponseDao;
    }

}
