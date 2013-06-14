package gs.web.school.usp;

import gs.data.school.EspResponse;
import gs.data.school.EspResponseSource;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Decorator pattern. A List of EspResponses that wraps an underlying List of EspResponses and adds additional
 * functionality
 * Not thread safe
 */
public class EspResponseData extends BaseEspResponseData {

    /**
     * Make sure to default to null. "Caches" the information that this object finds out about the underlying list
     */
    private Map<EspResponseSource, IEspResponseData> _responsesBySource = null;

    public EspResponseData() {
        super();
    }

    public EspResponseData(List<EspResponse> responses) {
        super(responses);
    }

    public EspResponseData(Map<String, List<EspResponse>> responsesByKey) {
        super(responsesByKey);
    }

    /**
     * sets instance variables back to null, so that the methods that populate them will be re-executed. Should be
     * called when underlying List of responses is modified
     */
    @Override
    protected void clearCache() {
        super.clearCache();
        _responsesBySource = null;
    }

    /**
     * Helper method. Returns EspResponses with source of OSP.
     * @return A IEspResponseData that wraps the List of EspResponses
     */
    public IEspResponseData getOspResponses() {
        EspResponseData responseData = new EspResponseData();
        responseData.addAll(getResponsesBySource(EspResponseSource.osp));
        responseData.addAll(getResponsesBySource(EspResponseSource.datateam));
        return responseData;
    }

    /**
     * Method to get the EspResponses for the special keys and with source of OSP.
     * @return
     */
    public IEspResponseData getOspResponsesForSpecialKeys() {
        Iterator<EspResponse> iterator = iterator();
        EspResponseData ospResponseDataSpecialKeys = new EspResponseData();

        while (iterator.hasNext()) {
            EspResponse response = iterator.next();
            if (EspStatusManager.getOspKeySet().contains(response.getKey()) &&
                    (EspResponseSource.osp.equals(response.getSource()) || (EspResponseSource.datateam.equals(response.getSource())))) {
                ospResponseDataSpecialKeys.add(response);
            }
        }
        return ospResponseDataSpecialKeys;
    }

    /**
     * Helper method. Returns EspResponses with source of USP.
     * @return A IEspResponseData that wraps the List of EspResponses
     */
    public IEspResponseData getUspResponses() {
        return getResponsesBySource(EspResponseSource.usp);
    }

    /**
     * @return true if underlying List of EspResponses contains responses with source of OSP
     */
    public boolean hasOspResponseData() {
        return !getOspResponses().isEmpty();
    }

    /**
     * @return true if underlying List of EspResponses contains responses with source of USP
     */
    public boolean hasUspResponseData() {
        return !getUspResponses().isEmpty();
    }

    /**
     * Gets the List of EspResponses associated with a specific source. You might want to use one of the helper methods
     * instead.
     * @param source
     * @return A IEspResponseData that wraps a List of EspResponses
     */
    public IEspResponseData getResponsesBySource(EspResponseSource source) {
        if (_responsesBySource == null) {
            groupBySource();
        }

        IEspResponseData result = _responsesBySource.get(source);

        if (result == null) {
            result = new EspResponseData();
        }

        return result;
    }

    /**
     * Iterates over the underlying List of EspResponses, breaking them out into buckets by source. Saves the result
     * onto an instance variable. Probably shouldn't be called from outside of this class
     */
    protected void groupBySource() {
        // make sure to initialize to empty list when groupBySource is called
        _responsesBySource = new HashMap<EspResponseSource, IEspResponseData>();

        Iterator<EspResponse> iterator = iterator();

        while (iterator.hasNext()) {
            EspResponse response = iterator.next();

            EspResponseSource source = response.getSource();

            IEspResponseData responsesForSource = _responsesBySource.get(source);
            if (responsesForSource == null) {
                responsesForSource = new EspResponseData();
                _responsesBySource.put(response.getSource(), responsesForSource);
            }
            responsesForSource.add(response);
        }
    }

    /**
     * Returns a new EspResponseData with EspResponses matching the given member ID
     * @param memberId
     * @return a new EspResponseData
     */
    public IEspResponseData getResponsesByUser(Integer memberId) {
        EspResponseData responseData = new EspResponseData();

        Iterator<EspResponse> iterator = iterator();

        while (iterator.hasNext()) {
            EspResponse response = iterator.next();
            if (response.getMemberId() != null && response.getMemberId().equals(memberId)) {
                responseData.add(response);
            }
        }

        return responseData;
    }

}
