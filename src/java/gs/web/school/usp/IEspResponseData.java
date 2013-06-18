package gs.web.school.usp;


import com.google.common.collect.Multimap;
import gs.data.school.EspResponse;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Decorator pattern. A List of EspResponses that wraps an underlying List of EspResponses and adds additional
 * functionality
 */
public interface IEspResponseData extends List<EspResponse> {

    public Date getOldestResponseDate();

    public Map<String, List<EspResponse>> getResponsesByKey();

    public boolean hasRecentYearOfData();

    public Multimap<String,String> getMultimap();
}
