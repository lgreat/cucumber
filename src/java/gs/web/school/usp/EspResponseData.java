package gs.web.school.usp;

import gs.data.school.EspResponse;
import gs.data.school.EspResponseSource;

import java.util.*;

/**
 * Not thread safe
 */
class EspResponseData extends BaseEspResponseData {

    // make sure to default to null
    private Map<EspResponseSource, IEspResponseData> _responsesBySource = null;

    public EspResponseData() {
        super();
    }

    public EspResponseData(List<EspResponse> responses) {
        super(responses);
    }

    @Override
    protected void clearCache() {
        super.clearCache();
        _responsesBySource = null;
    }

    public IEspResponseData getOspResponses() {
        return getResponsesBySource(EspResponseSource.osp);
    }

    public IEspResponseData getUspResponses() {
        return getResponsesBySource(EspResponseSource.usp);
    }

    public boolean hasOspResponseData() {
        return !getOspResponses().isEmpty();
    }

    public boolean hasUspResponseData() {
        return !getUspResponses().isEmpty();
    }

    public IEspResponseData getResponsesBySource(EspResponseSource source) {
        if (_responsesBySource == null) {
            groupBySource();
        }

        IEspResponseData result = _responsesBySource.get(source);

        if (result == null) {
            result = new BaseEspResponseData();
        }

        return result;
    }

    protected void groupBySource() {
        // make sure to initialize to empty list when groupBySource is called
        _responsesBySource = new HashMap<EspResponseSource, IEspResponseData>();

        Iterator<EspResponse> iterator = iterator();

        while (iterator.hasNext()) {
            EspResponse response = iterator.next();

            EspResponseSource source = response.getSource();

            IEspResponseData responsesForSource = _responsesBySource.get(source);
            if (responsesForSource == null) {
                responsesForSource = new BaseEspResponseData();
                _responsesBySource.put(response.getSource(), responsesForSource);
            }
            responsesForSource.add(response);
        }
    }

}
