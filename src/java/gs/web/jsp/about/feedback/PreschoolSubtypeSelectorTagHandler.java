package gs.web.jsp.about.feedback;

import gs.web.jsp.SelectorTagHandler;

public class PreschoolSubtypeSelectorTagHandler extends SelectorTagHandler {
    private static String[] OPTION_VALUES = new String[] {
        "american_child_care",
        "bright_horizons",
        "childrens_discovery",
        "childrens_world",
        "childtime_learning_center",
        "college_university_based",
        "employer_corporate_sponsored",
        "family_day_residence",
        "federal_based",
        "goddard",
        "head_start",
        "hospital_centers",
        "kiddie_academy",
        "kids_r_us",
        "kindercare",
        "la_petite_academy",
        "military",
        "montessori",
        "new_horizon",
        "primrose",
        "rainbow_child",
        "religious",
        "salvation_army",
        "sunshine_house",
        "tutor_time",
        "ymca_ywca"
    };

    private static String[] OPTION_DISPLAY_NAMES = new String[] {
        "American Child Care",
        "Bright Horizons",
        "Children's Discovery",
        "Children's World",
        "Childtime Learning Center",
        "College/university-based",
        "Employer sponsored",
        "Family day residence",
        "Federal-based",
        "Goddard",
        "Head Start",
        "Hospital center",
        "Kiddie Academy",
        "Kids R Us",
        "Kindercare",
        "La Petite Academy",
        "Military",
        "Montessori",
        "New Horizon",
        "Primrose",
        "Rainbow Child",
        "Religious",
        "Salvation Army",
        "Sunshine House",
        "Tutor Time",
        "YMCA/YWCA"
    };

    public String[] getOptionValues() {
        return OPTION_VALUES;
    }

    public String[] getOptionDisplayNames() {
        return OPTION_DISPLAY_NAMES;
    }
}
