package gs.web.search;

import gs.data.school.SchoolType;
import gs.data.search.Searcher;
import org.apache.commons.lang.StringUtils;

public interface FieldFilter {

    public String getFilterName();

    public enum SchoolTypeFilter implements FieldFilter {
        PRIVATE(Searcher.PRIVATE_SCHOOLS),
        CHARTER(Searcher.CHARTER_SCHOOLS),
        PUBLIC(Searcher.PUBLIC_SCHOOLS);

        private String _filterName;

        SchoolTypeFilter(String filterName) {
            _filterName = filterName;
        }

        public String getFilterName() {
            return _filterName;
        }

        public void setFilterName(String filterName) {
            _filterName = filterName;
        }
    }

    public enum GradeLevelFilter implements FieldFilter {
        PRESCHOOL("preschool"),
        ELEMENTARY("elementary"),
        MIDDLE("middle"),
        HIGH("high");

        private String _filterName;

        GradeLevelFilter(String filterName) {
            _filterName = filterName;
        }

        public String getFilterName() {
            return _filterName;
        }

        public void setFilterName(String filterName) {
            _filterName = filterName;
        }
    }
}