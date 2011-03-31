package gs.web.search;

import gs.data.search.SchoolSearchServiceSolrImpl;
import org.apache.commons.lang.StringUtils;

public class LooseSchoolSearchServiceSolrImpl extends SchoolSearchServiceSolrImpl {
    @Override
    public String buildQuery(String searchString) {
        if (!StringUtils.isBlank(searchString)) {
            searchString = cleanseSearchString(searchString);
            if (searchString == null) {
                return null; //Provided search string was garbage, early exit
            }
        }

        return searchString;
    }
}
