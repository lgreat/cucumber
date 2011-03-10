package gs.web.search;

public interface FieldFilter {

    public String getFilterName();
    public String getFilterType();

    public enum AffiliationFilter implements FieldFilter {
        RELIGOUS("religious"),
        NONSECTARIAN("nonsectarian");

        private String _filterName;
        private static String _filterType = "Affiliation";

        AffiliationFilter(String filterName) {
            _filterName = filterName;
}

        public String getFilterName() {
            return _filterName;
        }

        public void setFilterName(String filterName) {
            _filterName = filterName;
        }

        public String getFilterType() {
            return _filterType;
        }
    }

    public enum SchoolTypeFilter implements FieldFilter {
        PRIVATE("private"),
        CHARTER("charter"),
        PUBLIC("public");

        private String _filterName;
        private static String _filterType = "SchoolType";

        SchoolTypeFilter(String filterName) {
            _filterName = filterName;
        }

        public String getFilterName() {
            return _filterName;
        }

        public void setFilterName(String filterName) {
            _filterName = filterName;
        }

        public String getFilterType() {
            return _filterType;
        }
    }

    public enum GradeLevelFilter implements FieldFilter {
        PRESCHOOL("preschool"),
        ELEMENTARY("elementary"),
        MIDDLE("middle"),
        HIGH("high");

        private String _filterName;
        private static String _filterType = "GradeLevel";

        GradeLevelFilter(String filterName) {
            _filterName = filterName;
        }

        public String getFilterName() {
            return _filterName;
        }

        public void setFilterName(String filterName) {
            _filterName = filterName;
        }

        public String getFilterType() {
            return _filterType;
        }
    }
}