package gs.web.school.usp;


import gs.data.school.EspResponse;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Wraps a list of EspResponses, in order to provide info about the specific collection of responses
 */
public interface IEspResponseData extends List<EspResponse> {

    public Date getOldestResponseDate();

    public Map<String, List<EspResponse>> getResponsesByKey();

    public boolean hasRecentYearOfData();
}
