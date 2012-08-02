package gs.web.search;

import gs.data.school.EspResponse;
import gs.data.school.School;

import java.util.List;
import java.util.Map;

public interface CmsRelatedFeatureSearchService {

    public List<ICmsFeatureSearchResult> getRelatedFeatures(School school, Map<String, List<EspResponse>> espResponses, int rows);

}
