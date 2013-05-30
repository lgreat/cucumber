package gs.web.school.usp;

import gs.data.school.EspResponse;
import gs.data.school.School;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
@Scope("prototype")
public class EspStatusManager {

    @Autowired
    private gs.data.school.IEspResponseDao _espResponseDao;

    private School _school;

    private EspStatus _espStatus;

    private EspResponseData _espResponseData;

    public EspStatusManager(School school) {
        _school = school;
    }

    public static Set<String> getOspKeySet() {
        //TODO check for all keys or maybe just check for 1 key and its timestamp
        //TODO deal with the open text fields like other.
        return new HashSet<String>(
            Arrays.asList(
                new String[]{
                    "girls_sports",
                    "boys_sports"
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
    public static EspStatus getEspStatus(EspResponseData espResponseData) {
        EspStatus status;

        boolean allOSPQuestionsAnswered = espResponseData.getOspResponses()
            .getResponsesByKey()
            .keySet()
            .containsAll(getOspKeySet());

        boolean isThereUSPData = espResponseData.hasUspResponseData();
        boolean isThereOSPData = espResponseData.hasOspResponseData();
        boolean isOspDataRecent = espResponseData.getOspResponses().hasRecentYearOfData();

        if (allOSPQuestionsAnswered && isOspDataRecent) {
            status = EspStatus.OSP_PREFERRED;
        } else if (isThereUSPData && !isThereOSPData) {
            status = EspStatus.USP_ONLY;
        } else if (isThereUSPData && isThereOSPData && !isOspDataRecent) {
            status = EspStatus.MIX;
        } else if (!isThereUSPData && isThereOSPData && !isOspDataRecent) {
            status = EspStatus.OSP_OUTDATED;
        } else {
            status = EspStatus.NO_DATA;
        }

        return status;
    }
}
