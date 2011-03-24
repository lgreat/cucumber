package gs.web.search;

public interface FieldFilter {

    public enum LowestAgeAccepted implements FieldFilter {
        UNDER_ONE,
        ONE,
        TWO,
        THREE,
        FOUR_AND_ABOVE
    }

    public enum SchoolSize implements FieldFilter {
        UNDER_20,
        UNDER_50,
        UNDER_200,
        UNDER_500,
        UNDER_1000,
        OVER_1000
    }

    public enum StudentTeacherRatio implements FieldFilter {
        UNDER_10,
        UNDER_15,
        UNDER_20,
        UNDER_25
    }

    public enum AffiliationFilter implements FieldFilter {
        RELIGIOUS,
        NONSECTARIAN
    }

    public enum SchoolTypeFilter implements FieldFilter {
        PRIVATE,
        CHARTER,
        PUBLIC
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