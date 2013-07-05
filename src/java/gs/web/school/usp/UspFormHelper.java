package gs.web.school.usp;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;
import gs.data.community.User;
import gs.data.json.JSONArray;
import gs.data.json.JSONException;
import gs.data.json.JSONObject;
import gs.data.school.*;
import gs.data.state.State;
import gs.web.util.HttpCacheInterceptor;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.ui.ModelMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: rramachandran
 * Date: 4/30/13
 * Time: 5:18 PM
 * To change this template use File | Settings | File Templates.
 */
@Component
public class UspFormHelper {
    public static final String BEAN_ID = "uspFormHelper";
    private static Logger _logger = Logger.getLogger(UspFormHelper.class);

    public static final String PARAM_STATE = "state";
    public static final String PARAM_SCHOOL_ID = "schoolId";

    HttpCacheInterceptor _cacheInterceptor = new HttpCacheInterceptor();

    @Autowired
    private IEspResponseDao _espResponseDao;

    public static final String DOUBLE_UNDERSCORE_SEPARATOR = "__";

    /**
     * Title for each question on usp form page
     */
    public static final String ARTS_MUSIC_TITLE = "Arts & music";
    public static final String EXTENDED_CARE_TITLE = "Extended care";
    public static final String GIRLS_SPORTS_TITLE = "Girls sports";
    public static final String BOYS_SPORTS_TITLE = "Boys sports";
    public static final String STAFF_TITLE = "Staff";
    public static final String FACILITIES_TITLE = "Facilities";
    public static final String FOREIGN_LANGUAGES_TITLE = "Foreign languages";
    public static final String TRANSPORTATION_TITLE = "Transportation";
    public static final String PARENT_INVOLVEMENT_TITLE = "Parent Involvement";

    /**
     * Param names for usp form field section names
     */
    public static final String ARTS_MUSIC_PARAM = "arts";
    public static final String EXTENDED_CARE_PARAM = "extCare";
    public static final String GIRLS_SPORTS_PARAM = "girlsSports";
    public static final String BOYS_SPORTS_PARAM = "boysSports";
    public static final String STAFF_PARAM = "staff";
    public static final String FACILITIES_PARAM = "facilities";
    public static final String FOREIGN_LANGUAGES_PARAM = "foreignLanguages";
    public static final String TRANSPORTATION_PARAM = "transportation";
    public static final String PARENT_INVOLVEMENT_PARAM = "parentInvolvement";

    /**
     * None checkbox value
     */
    public static final String NONE_RESPONSE_VALUE = "none";
    /**
     * For boys sports, the response value in osp dashboard form is "None"
     */
    public static final String BOYS_SPORTS_NONE_RESPONSE_VALUE = "None";

    /**
     * Response keys and values for Arts & music
     * There are 4 subsections - media, music, performing written and visual
     */
    public static final String ARTS_MEDIA_RESPONSE_KEY = ARTS_MUSIC_PARAM + "_media";
    public static final String ARTS_MUSIC_RESPONSE_KEY = ARTS_MUSIC_PARAM + "_music";
    public static final String ARTS_PERFORMING_WRITTEN_RESPONSE_KEY = ARTS_MUSIC_PARAM + "_performing_written";
    public static final String ARTS_VISUAL_RESPONSE_KEY = ARTS_MUSIC_PARAM + "_visual";

    public static final String ARTS_MEDIA_ANIMATION_RESPONSE_VALUE = "animation";
    public static final String ARTS_MEDIA_GRAPHICS_RESPONSE_VALUE = "graphics";
    public static final String ARTS_MEDIA_TECH_DESIGN_RESPONSE_VALUE = "tech_design";
    public static final String ARTS_MEDIA_VIDEO_RESPONSE_VALUE = "video";

    public static final String ARTS_MUSIC_BAND_RESPONSE_VALUE = "band";
    public static final String ARTS_MUSIC_BELLS_RESPONSE_VALUE = "bells";
    public static final String ARTS_MUSIC_CHAMBER_RESPONSE_VALUE = "chamber_music";
    public static final String ARTS_MUSIC_CHORUS_RESPONSE_VALUE = "chorus";
    public static final String ARTS_MUSIC_LESSONS_RESPONSE_VALUE = "music_lessons";
    public static final String ARTS_MUSIC_JAZZ_RESPONSE_VALUE = "jazz_band";
    public static final String ARTS_MUSIC_OPERA_RESPONSE_VALUE = "opera";
    public static final String ARTS_MUSIC_ORCHESTRA_RESPONSE_VALUE = "orchestra";
    public static final String ARTS_MUSIC_ROCK_RESPONSE_VALUE = "rock_band";
    public static final String ARTS_MUSIC_THEORY_RESPONSE_VALUE = "music_theory";
    public static final String ARTS_MUSIC_VOICE_RESPONSE_VALUE = "voice";

    public static final String ARTS_PERFORMING_DANCE_RESPONSE_VALUE = "dance";
    public static final String ARTS_PERFORMING_DRAMA_RESPONSE_VALUE = "drama";
    public static final String ARTS_PERFORMING_IMPROV_RESPONSE_VALUE = "improv";
    public static final String ARTS_PERFORMING_CREATIVE_WRITING_RESPONSE_VALUE = "creative_writing";
    public static final String ARTS_PERFORMING_POETRY_RESPONSE_VALUE = "poetry";

    public static final String ARTS_VISUAL_ARCH_RESPONSE_VALUE = "architecture";
    public static final String ARTS_VISUAL_CERAMICS_RESPONSE_VALUE = "ceramics";
    public static final String ARTS_VISUAL_DESIGN_RESPONSE_VALUE = "design";
    public static final String ARTS_VISUAL_DRAWING_RESPONSE_VALUE = "drawing";
    public static final String ARTS_VISUAL_PAINTING_RESPONSE_VALUE = "painting";
    public static final String ARTS_VISUAL_PHOTO_RESPONSE_VALUE = "photography";
    public static final String ARTS_VISUAL_PRINT_RESPONSE_VALUE = "printmaking";
    public static final String ARTS_VISUAL_SCULPTURE_RESPONSE_VALUE = "sculpture";
    public static final String ARTS_VISUAL_TEXTILES_RESPONSE_VALUE = "textiles";

    /**
     * Response key and values for Extended care
     */
    public static final String EXTENDED_CARE_RESPONSE_KEY = "before_after_care";

    public static final String EXTENDED_CARE_BEFORE_RESPONSE_VALUE = "before";
    public static final String EXTENDED_CARE_AFTER_RESPONSE_VALUE = "after";
    public static final String EXTENDED_CARE_NEITHER_RESPONSE_VALUE = "neither";

    /**
     * Response key for girls sports
     */
    public static final String GIRLS_SPORTS_RESPONSE_KEY = "girls_sports";
    public static final String GIRLS_SPORTS_OTHER_RESPONSE_KEY = "girls_sports_other";

    /**
     * Response key for boys sports
     */
    public static final String BOYS_SPORTS_RESPONSE_KEY = "boys_sports";
    public static final String BOYS_SPORTS_OTHER_RESPONSE_KEY = "boys_sports_other";

    /**
     * Response values for common girls and boys sports
     */
    public static final String SPORTS_BADMINTON_RESPONSE_VALUE = "badminton";
    public static final String SPORTS_BASKETBALL_RESPONSE_VALUE = "basketball";
    public static final String SPORTS_CHEERLEADING_RESPONSE_VALUE = "cheerleading";
    public static final String SPORTS_CREW_RESPONSE_VALUE = "crew";
    public static final String SPORTS_CROSS_COUNTRY_RESPONSE_VALUE = "cross_country";
    public static final String SPORTS_CYCLING_RESPONSE_VALUE = "cycling";
    public static final String SPORTS_DIVING_RESPONSE_VALUE = "diving";
    public static final String SPORTS_EQUESTRIAN_RESPONSE_VALUE = "equestrian";
    public static final String SPORTS_FENCING_RESPONSE_VALUE = "fencing";
    public static final String SPORTS_FLAG_FOOTBALL_RESPONSE_VALUE = "flag_football";
    public static final String SPORTS_GOLF_RESPONSE_VALUE = "golf";
    public static final String SPORTS_GYMNASTICS_RESPONSE_VALUE = "gymnastics";
    public static final String SPORTS_ICE_HOCKEY_RESPONSE_VALUE = "ice_hockey";
    public static final String SPORTS_MARTIAL_ARTS_RESPONSE_VALUE = "martial_arts";
    public static final String SPORTS_KAYAKING_RESPONSE_VALUE = "kayaking";
    public static final String SPORTS_KICKBALL_RESPONSE_VALUE = "kickball";
    public static final String SPORTS_LACROSSE_RESPONSE_VALUE = "lacrosse";
    public static final String SPORTS_WEIGHT_LIFTING_RESPONSE_VALUE = "weight_lifting";
    public static final String SPORTS_RUGBY_RESPONSE_VALUE = "rugby";
    public static final String SPORTS_SAILING_RESPONSE_VALUE = "sailing";
    public static final String SPORTS_SKIING_RESPONSE_VALUE = "ski";
    public static final String SPORTS_SOCCER_RESPONSE_VALUE = "soccer";
    public static final String SPORTS_SQUASH_RESPONSE_VALUE = "squash";
    public static final String SPORTS_SURFING_RESPONSE_VALUE = "surfing";
    public static final String SPORTS_SWIMMING_RESPONSE_VALUE = "swimming";
    public static final String SPORTS_TENNIS_RESPONSE_VALUE = "tennis";
    public static final String SPORTS_TRACK_RESPONSE_VALUE = "track";
    public static final String SPORTS_ULTIMATE_RESPONSE_VALUE = "ultimate";
    public static final String SPORTS_VOLLEYBALL_RESPONSE_VALUE = "volleyball";
    public static final String SPORTS_WATER_POLO_RESPONSE_VALUE = "water_polo";
    public static final String SPORTS_WRESTLING_RESPONSE_VALUE = "wrestling";

    /**
     * Response values for girls only sports
     */
    public static final String GIRLS_SPORTS_FIELD_HOCKEY_RESPONSE_VALUE = "field_hockey";
    public static final String GIRLS_SPORTS_SOFTBALL_RESPONSE_VALUE = "softball";

    /**
     * Response values for boys only sports
     */
    public static final String BOYS_SPORTS_BASEBALL_RESPONSE_VALUE = "baseball";
    public static final String BOYS_SPORTS_FOOTBALL_RESPONSE_VALUE = "football";

    /**
     * Response key and values for staff
     */
    public static final String STAFF_RESPONSE_KEY = "staff_resources";

    public static final String STAFF_ART_TEACHER_RESPONSE_VALUE = "art_teacher";
    public static final String STAFF_ASSISTANT_PRINCIPAL_RESPONSE_VALUE = "assistant_principal";
    public static final String STAFF_COLLEGE_COUNSELOR_RESPONSE_VALUE = "college_counselor";
    public static final String STAFF_COMP_SPECIALIST_RESPONSE_VALUE = "computer_specialist";
    public static final String STAFF_COOKING_TEACHER_RESPONSE_VALUE = "cooking_teacher";
    public static final String STAFF_DANCE_TEACHER_RESPONSE_VALUE = "dance_teacher";
    public static final String STAFF_ELL_ESL_RESPONSE_VALUE = "ell_esl_coord";
    public static final String STAFF_GARDEN_TEACHER_RESPONSE_VALUE = "garden_teacher";
    public static final String STAFF_GIFTED_SPECIALIST_RESPONSE_VALUE = "gifted_specialist";
    public static final String STAFF_INSTRUCTIONAL_AID_RESPONSE_VALUE = "instructional_aid";
    public static final String STAFF_LIBRARIAN_RESPONSE_VALUE = "librarian";
    public static final String STAFF_MATH_SPECIALIST_RESPONSE_VALUE = "math_specialist";
    public static final String STAFF_MUSIC_TEACHER_RESPONSE_VALUE = "music_teacher";
    public static final String STAFF_NURSE_RESPONSE_VALUE = "nurse";
    public static final String STAFF_PE_INSTRUCTOR_RESPONSE_VALUE = "pe_instructor";
    public static final String STAFF_POETRY_TEACHER_RESPONSE_VALUE = "poetry_teacher";
    public static final String STAFF_PRIEST_RESPONSE_VALUE = "priest";
    public static final String STAFF_READING_SPECIALIST_RESPONSE_VALUE = "reading_specialist";
    public static final String STAFF_ROBOTICS_TEACHER_RESPONSE_VALUE = "robotics_teacher";
    public static final String STAFF_SCHOOL_PSYCHOLOGIST_RESPONSE_VALUE = "school_psychologist";
    public static final String STAFF_SCHOOL_COUNSELOR_RESPONSE_VALUE = "school_counselor";
    public static final String STAFF_SECURITY_RESPONSE_VALUE = "security";
    public static final String STAFF_SPECIAL_ED_COORD_RESPONSE_VALUE = "special_ed_coordinator";
    public static final String STAFF_SPEECH_THERAPIST_RESPONSE_VALUE = "speech_therapist";
    public static final String STAFF_TEACHER_AID_RESPONSE_VALUE = "teacher_aid";
    public static final String STAFF_TUTOR_RESPONSE_VALUE = "tutor";

    /**
     * Response key and values for facilities
     */
    public static final String FACILITIES_RESPONSE_KEY = "facilities";

    public static final String FACILITIES_FARM_RESPONSE_VALUE = "farm";
    public static final String FACILITIES_SPORTS_FIELDS_RESPONSE_VALUE = "sports_fields";
    public static final String FACILITIES_ARTS_RESPONSE_VALUE = "art";
    public static final String FACILITIES_AUDIO_VISUAL_RESPONSE_VALUE = "audiovisual";
    public static final String FACILITIES_AUDITORIUM_RESPONSE_VALUE = "auditorium";
    public static final String FACILITIES_CAFETERIA_RESPONSE_VALUE = "cafeteria";
    public static final String FACILITIES_CHAPEL_RESPONSE_VALUE = "chapel";
    public static final String FACILITIES_COLLEGE_CENTER_RESPONSE_VALUE = "college_center";
    public static final String FACILITIES_COMPUTER_RESPONSE_VALUE = "computer";
    public static final String FACILITIES_GAREDEN_RESPONSE_VALUE = "garden";
    public static final String FACILITIES_GYM_RESPONSE_VALUE = "gym";
    public static final String FACILITIES_INDUSTRIAL_RESPONSE_VALUE = "industrial";
    public static final String FACILITIES_INTEREST_RESPONSE_VALUE = "internet";
    public static final String FACILITIES_KITCHEN_RESPONSE_VALUE = "kitchen";
    public static final String FACILITIES_LEARNING_LAB_RESPONSE_VALUE = "learning_lab";
    public static final String FACILITIES_LIBRARY_RESPONSE_VALUE = "library";
    public static final String FACILITIES_MULTI_PURPOSE_RESPONSE_VALUE = "multi_purpose";
    public static final String FACILITIES_MUSIC_RESPONSE_VALUE = "music";
    public static final String FACILITIES_OUTDOOR_RESPONSE_VALUE = "outdoor";
    public static final String FACILITIES_PARENT_RESPONSE_VALUE = "parent";
    public static final String FACILITIES_PERFORMANCE_RESPONSE_VALUE = "performance";
    public static final String FACILITIES_PLAYGROUND_RESPONSE_VALUE = "playground";
    public static final String FACILITIES_SCIENCE_RESPONSE_VALUE = "science";
    public static final String FACILITIES_SWIMMING_RESPONSE_VALUE = "swimming";

    /**
     * Response key and values for foreign languages
     */
    public static final String FOREIGN_LANGUAGES_RESPONSE_KEY = "foreign_language";
    public static final String FOREIGN_LANGUAGES_OTHER_RESPONSE_KEY = "foreign_language_other";

    public static final String FOREIGN_LANG_ASL_RESPONSE_VALUE = "american_sign_language";
    public static final String FOREIGN_LANG_AMHARIC_RESPONSE_VALUE = "amharic";
    public static final String FOREIGN_LANG_ARABIC_RESPONSE_VALUE = "arabic";
    public static final String FOREIGN_LANG_CANTONESE_RESPONSE_VALUE = "cantonese";
    public static final String FOREIGN_LANG_MANDARIN_RESPONSE_VALUE = "mandarin";
    public static final String FOREIGN_LANG_DUTCH_RESPONSE_VALUE = "dutch";
    public static final String FOREIGN_LANG_FRENCH_RESPONSE_VALUE = "french";
    public static final String FOREIGN_LANG_GERMAN_RESPONSE_VALUE = "german";
    public static final String FOREIGN_LANG_HMONG_RESPONSE_VALUE = "hmong";
    public static final String FOREIGN_LANG_ITALIAN_RESPONSE_VALUE = "italian";
    public static final String FOREIGN_LANG_JAPANESE_RESPONSE_VALUE = "japanese";
    public static final String FOREIGN_LANG_KOREAN_RESPONSE_VALUE = "korean";
    public static final String FOREIGN_LANG_LATIN_RESPONSE_VALUE = "latin";
    public static final String FOREIGN_LANG_RUSSIAN_RESPONSE_VALUE = "russian";
    public static final String FOREIGN_LANG_SPANISH_RESPONSE_VALUE = "spanish";
    public static final String FOREIGN_LANG_TAGALOG_RESPONSE_VALUE = "tagalog";
    public static final String FOREIGN_LANG_URDU_RESPONSE_VALUE = "urdu";
    public static final String FOREIGN_LANG_VIETNAMESE_RESPONSE_VALUE = "vietnamese";

    /**
     * Response key and values for transportation
     */
    public static final String TRANSPORTATION_RESPONSE_KEY = "transportation";
    public static final String TRANSPORTATION_OTHER_RESPONSE_KEY = "transportation_other";

    public static final String TRANSPORTATION_BUSSES_RESPONSE_VALUE = "busses";
    public static final String TRANSPORTATION_SHARED_BUS_RESPONSE_VALUE = "shared_bus";
    public static final String TRANSPORTATION_SPEC_ED_ONLY_RESPONSE_VALUE = "special_ed_only";
    public static final String TRANSPORTATION_PASSES_RESPONSE_VALUE = "passes";

    /**
     * Response key and values for parent involvement
     */
    public static final String PARENT_INVOLVEMENT_RESPONSE_KEY = "parent_involvement";
    public static final String PARENT_INVOLVEMENT_OTHER_RESPONSE_KEY = "parent_involvement_other";

    public static final String PARENT_INVOLVEMENT_PARENT_NIGHTS_RESPONSE_VALUE = "parent_nights_req";
    public static final String PARENT_INVOLVEMENT_CHAPERONE_RESPONSE_VALUE = "chaperone_req";
    public static final String PARENT_INVOLVEMENT_COACH_RESPONSE_VALUE = "coach_req";
    public static final String PARENT_INVOLVEMENT_PTO_PTA_RESPONSE_VALUE = "pto_pta_req";
    public static final String PARENT_INVOLVEMENT_PLAYGROUND_RESPONSE_VALUE = "playground_req";
    public static final String PARENT_INVOLVEMENT_CULTURAL_RESPONSE_VALUE = "cultural_req";
    public static final String PARENT_INVOLVEMENT_FUNDRAISING_RESPONSE_VALUE = "fundraising_req";
    public static final String PARENT_INVOLVEMENT_PRESENTATION_RESPONSE_VALUE = "presentation_req";
    public static final String PARENT_INVOLVEMENT_GOVERNANCE_RESPONSE_VALUE = "governance_req";
    public static final String PARENT_INVOLVEMENT_TUTOR_RESPONSE_VALUE = "tutor_req";
    public static final String PARENT_INVOLVEMENT_CLASSROOM_RESPONSE_VALUE = "classroom_req";
    public static final String PARENT_INVOLVEMENT_AFTER_SCHOOL_RESPONSE_VALUE = "after_school_req";

    /**
     * Enum with form field section name and arrays of response keys.
     * "Other" field response key is not included because that is not listed in the drop down.
     * Changing the order of the values in the enum will change the order in which they are displayed
     */
    public static enum SectionResponseKeys {
        arts(ARTS_MUSIC_PARAM, new String[]{ARTS_MEDIA_RESPONSE_KEY, ARTS_MUSIC_RESPONSE_KEY, ARTS_PERFORMING_WRITTEN_RESPONSE_KEY,
                ARTS_VISUAL_RESPONSE_KEY}),
        extCare(EXTENDED_CARE_PARAM, new String[]{EXTENDED_CARE_RESPONSE_KEY}),
        girlsSports(GIRLS_SPORTS_PARAM, new String[]{GIRLS_SPORTS_RESPONSE_KEY, GIRLS_SPORTS_OTHER_RESPONSE_KEY}),
        staff(STAFF_PARAM, new String[]{STAFF_RESPONSE_KEY}),
        facilities(FACILITIES_PARAM, new String[]{FACILITIES_RESPONSE_KEY}),
        foreignLanguages(FOREIGN_LANGUAGES_PARAM, new String[]{FOREIGN_LANGUAGES_RESPONSE_KEY, FOREIGN_LANGUAGES_OTHER_RESPONSE_KEY}),
        transportation(TRANSPORTATION_PARAM, new String[]{TRANSPORTATION_RESPONSE_KEY, TRANSPORTATION_OTHER_RESPONSE_KEY}),
        boysSports(BOYS_SPORTS_PARAM, new String[]{BOYS_SPORTS_RESPONSE_KEY, BOYS_SPORTS_OTHER_RESPONSE_KEY}),
        parentInvolvement(PARENT_INVOLVEMENT_PARAM, new String[]{PARENT_INVOLVEMENT_RESPONSE_KEY, PARENT_INVOLVEMENT_OTHER_RESPONSE_KEY});

        private final String _sectionFieldName;
        private final String[] _responseKeys;

        SectionResponseKeys(String _sectionFieldName, String[] _responseKeys) {
            this._sectionFieldName = _sectionFieldName;
            this._responseKeys = _responseKeys;
        }

        public String[] getResponseKeys() {
            return _responseKeys;
        }

        public String getSectionFieldName() {
            return _sectionFieldName;
        }
    }

    /**
     * Initialize static linked list multimap that maps a response key to multiple response values.
     * For each form field section, add the response values for all response keys
     */
    public static final Multimap<String, String> SECTION_RESPONSE_KEY_VALUE_MAP = LinkedListMultimap.create();

    static {
        for (SectionResponseKeys sectionResponseKeys : SectionResponseKeys.values()) {
            String sectionFieldName = sectionResponseKeys.getSectionFieldName();
            String[] responseKeys = sectionResponseKeys.getResponseKeys();

            for (String responseKey : responseKeys) {
                if (ARTS_MUSIC_PARAM.equals(sectionFieldName)) {
                    if (ARTS_MEDIA_RESPONSE_KEY.equals(responseKey)) {
                        addKeyValueToSection(ARTS_MEDIA_RESPONSE_KEY, new String[]{
                                ARTS_MEDIA_ANIMATION_RESPONSE_VALUE, ARTS_MEDIA_GRAPHICS_RESPONSE_VALUE,
                                ARTS_MEDIA_TECH_DESIGN_RESPONSE_VALUE, ARTS_MEDIA_VIDEO_RESPONSE_VALUE,
                                NONE_RESPONSE_VALUE
                        });
                    } else if (ARTS_MUSIC_RESPONSE_KEY.equals(responseKey)) {
                        addKeyValueToSection(ARTS_MUSIC_RESPONSE_KEY, new String[]{
                                ARTS_MUSIC_BAND_RESPONSE_VALUE, ARTS_MUSIC_BELLS_RESPONSE_VALUE,
                                ARTS_MUSIC_CHAMBER_RESPONSE_VALUE, ARTS_MUSIC_CHORUS_RESPONSE_VALUE,
                                ARTS_MUSIC_LESSONS_RESPONSE_VALUE, ARTS_MUSIC_JAZZ_RESPONSE_VALUE,
                                ARTS_MUSIC_OPERA_RESPONSE_VALUE, ARTS_MUSIC_ORCHESTRA_RESPONSE_VALUE,
                                ARTS_MUSIC_ROCK_RESPONSE_VALUE, ARTS_MUSIC_THEORY_RESPONSE_VALUE,
                                ARTS_MUSIC_VOICE_RESPONSE_VALUE, NONE_RESPONSE_VALUE
                        });
                    } else if (ARTS_PERFORMING_WRITTEN_RESPONSE_KEY.equals(responseKey)) {
                        addKeyValueToSection(ARTS_PERFORMING_WRITTEN_RESPONSE_KEY, new String[]{
                                ARTS_PERFORMING_DANCE_RESPONSE_VALUE, ARTS_PERFORMING_DRAMA_RESPONSE_VALUE,
                                ARTS_PERFORMING_IMPROV_RESPONSE_VALUE, ARTS_PERFORMING_CREATIVE_WRITING_RESPONSE_VALUE,
                                ARTS_PERFORMING_POETRY_RESPONSE_VALUE, NONE_RESPONSE_VALUE
                        });
                    } else if (ARTS_VISUAL_RESPONSE_KEY.equals(responseKey)) {
                        addKeyValueToSection(ARTS_VISUAL_RESPONSE_KEY, new String[]{
                                ARTS_VISUAL_ARCH_RESPONSE_VALUE, ARTS_VISUAL_CERAMICS_RESPONSE_VALUE,
                                ARTS_VISUAL_DESIGN_RESPONSE_VALUE, ARTS_VISUAL_DRAWING_RESPONSE_VALUE,
                                ARTS_VISUAL_PAINTING_RESPONSE_VALUE, ARTS_VISUAL_PHOTO_RESPONSE_VALUE,
                                ARTS_VISUAL_PRINT_RESPONSE_VALUE, ARTS_VISUAL_SCULPTURE_RESPONSE_VALUE,
                                ARTS_VISUAL_TEXTILES_RESPONSE_VALUE, NONE_RESPONSE_VALUE
                        });
                    }
                } else if (EXTENDED_CARE_PARAM.equals(sectionFieldName) && EXTENDED_CARE_RESPONSE_KEY.equals(responseKey)) {
                    addKeyValueToSection(EXTENDED_CARE_RESPONSE_KEY, new String[]{
                            EXTENDED_CARE_BEFORE_RESPONSE_VALUE, EXTENDED_CARE_AFTER_RESPONSE_VALUE,
                            EXTENDED_CARE_NEITHER_RESPONSE_VALUE
                    });
                } else if (GIRLS_SPORTS_PARAM.equals(sectionFieldName) && GIRLS_SPORTS_RESPONSE_KEY.equals(responseKey)) {
                    addKeyValueToSection(GIRLS_SPORTS_RESPONSE_KEY, new String[]{
                            SPORTS_BADMINTON_RESPONSE_VALUE, SPORTS_BASKETBALL_RESPONSE_VALUE,
                            SPORTS_CHEERLEADING_RESPONSE_VALUE, SPORTS_CREW_RESPONSE_VALUE,
                            SPORTS_CROSS_COUNTRY_RESPONSE_VALUE, SPORTS_CYCLING_RESPONSE_VALUE,
                            SPORTS_DIVING_RESPONSE_VALUE, SPORTS_EQUESTRIAN_RESPONSE_VALUE,
                            SPORTS_FENCING_RESPONSE_VALUE, GIRLS_SPORTS_FIELD_HOCKEY_RESPONSE_VALUE,
                            SPORTS_FLAG_FOOTBALL_RESPONSE_VALUE,
                            SPORTS_GOLF_RESPONSE_VALUE, SPORTS_GYMNASTICS_RESPONSE_VALUE,
                            SPORTS_ICE_HOCKEY_RESPONSE_VALUE, SPORTS_MARTIAL_ARTS_RESPONSE_VALUE,
                            SPORTS_KAYAKING_RESPONSE_VALUE, SPORTS_KICKBALL_RESPONSE_VALUE,
                            SPORTS_LACROSSE_RESPONSE_VALUE, SPORTS_WEIGHT_LIFTING_RESPONSE_VALUE,
                            SPORTS_RUGBY_RESPONSE_VALUE, SPORTS_SAILING_RESPONSE_VALUE,
                            SPORTS_SKIING_RESPONSE_VALUE, SPORTS_SOCCER_RESPONSE_VALUE,
                            GIRLS_SPORTS_SOFTBALL_RESPONSE_VALUE,
                            SPORTS_SQUASH_RESPONSE_VALUE, SPORTS_SURFING_RESPONSE_VALUE,
                            SPORTS_SWIMMING_RESPONSE_VALUE, SPORTS_TENNIS_RESPONSE_VALUE,
                            SPORTS_TRACK_RESPONSE_VALUE, SPORTS_ULTIMATE_RESPONSE_VALUE,
                            SPORTS_VOLLEYBALL_RESPONSE_VALUE, SPORTS_WATER_POLO_RESPONSE_VALUE,
                            SPORTS_WRESTLING_RESPONSE_VALUE,
                            NONE_RESPONSE_VALUE
                    });
                } else if (GIRLS_SPORTS_PARAM.equals(sectionFieldName) && GIRLS_SPORTS_OTHER_RESPONSE_KEY.equals(responseKey)) {
                    addKeyValueToSection(GIRLS_SPORTS_OTHER_RESPONSE_KEY, new String[]{});
                } else if (BOYS_SPORTS_PARAM.equals(sectionFieldName) && BOYS_SPORTS_RESPONSE_KEY.equals(responseKey)) {
                    addKeyValueToSection(BOYS_SPORTS_RESPONSE_KEY, new String[]{
                            SPORTS_BADMINTON_RESPONSE_VALUE, BOYS_SPORTS_BASEBALL_RESPONSE_VALUE,
                            SPORTS_BASKETBALL_RESPONSE_VALUE,
                            SPORTS_CHEERLEADING_RESPONSE_VALUE, SPORTS_CREW_RESPONSE_VALUE,
                            SPORTS_CROSS_COUNTRY_RESPONSE_VALUE, SPORTS_CYCLING_RESPONSE_VALUE,
                            SPORTS_DIVING_RESPONSE_VALUE, SPORTS_EQUESTRIAN_RESPONSE_VALUE,
                            SPORTS_FENCING_RESPONSE_VALUE, SPORTS_FLAG_FOOTBALL_RESPONSE_VALUE,
                            BOYS_SPORTS_FOOTBALL_RESPONSE_VALUE,
                            SPORTS_GOLF_RESPONSE_VALUE, SPORTS_GYMNASTICS_RESPONSE_VALUE,
                            SPORTS_ICE_HOCKEY_RESPONSE_VALUE, SPORTS_MARTIAL_ARTS_RESPONSE_VALUE,
                            SPORTS_KAYAKING_RESPONSE_VALUE, SPORTS_KICKBALL_RESPONSE_VALUE,
                            SPORTS_LACROSSE_RESPONSE_VALUE, SPORTS_WEIGHT_LIFTING_RESPONSE_VALUE,
                            SPORTS_RUGBY_RESPONSE_VALUE, SPORTS_SAILING_RESPONSE_VALUE,
                            SPORTS_SKIING_RESPONSE_VALUE, SPORTS_SOCCER_RESPONSE_VALUE,
                            SPORTS_SQUASH_RESPONSE_VALUE, SPORTS_SURFING_RESPONSE_VALUE,
                            SPORTS_SWIMMING_RESPONSE_VALUE, SPORTS_TENNIS_RESPONSE_VALUE,
                            SPORTS_TRACK_RESPONSE_VALUE, SPORTS_ULTIMATE_RESPONSE_VALUE,
                            SPORTS_VOLLEYBALL_RESPONSE_VALUE, SPORTS_WATER_POLO_RESPONSE_VALUE,
                            SPORTS_WRESTLING_RESPONSE_VALUE,
                            BOYS_SPORTS_NONE_RESPONSE_VALUE
                    });
                } else if (BOYS_SPORTS_PARAM.equals(sectionFieldName) && BOYS_SPORTS_OTHER_RESPONSE_KEY.equals(responseKey)) {
                    addKeyValueToSection(BOYS_SPORTS_OTHER_RESPONSE_KEY, new String[]{});
                } else if (STAFF_PARAM.equals(sectionFieldName) && STAFF_RESPONSE_KEY.equals(responseKey)) {
                    addKeyValueToSection(STAFF_RESPONSE_KEY, new String[]{
                            STAFF_ART_TEACHER_RESPONSE_VALUE, STAFF_ASSISTANT_PRINCIPAL_RESPONSE_VALUE,
                            STAFF_COLLEGE_COUNSELOR_RESPONSE_VALUE, STAFF_COMP_SPECIALIST_RESPONSE_VALUE,
                            STAFF_COOKING_TEACHER_RESPONSE_VALUE, STAFF_DANCE_TEACHER_RESPONSE_VALUE,
                            STAFF_ELL_ESL_RESPONSE_VALUE, STAFF_GARDEN_TEACHER_RESPONSE_VALUE,
                            STAFF_GIFTED_SPECIALIST_RESPONSE_VALUE, STAFF_INSTRUCTIONAL_AID_RESPONSE_VALUE,
                            STAFF_LIBRARIAN_RESPONSE_VALUE, STAFF_MATH_SPECIALIST_RESPONSE_VALUE,
                            STAFF_MUSIC_TEACHER_RESPONSE_VALUE, STAFF_NURSE_RESPONSE_VALUE,
                            STAFF_PE_INSTRUCTOR_RESPONSE_VALUE, STAFF_POETRY_TEACHER_RESPONSE_VALUE,
                            STAFF_PRIEST_RESPONSE_VALUE, STAFF_READING_SPECIALIST_RESPONSE_VALUE,
                            STAFF_ROBOTICS_TEACHER_RESPONSE_VALUE, STAFF_SCHOOL_PSYCHOLOGIST_RESPONSE_VALUE,
                            STAFF_SCHOOL_COUNSELOR_RESPONSE_VALUE, STAFF_SECURITY_RESPONSE_VALUE,
                            STAFF_SPECIAL_ED_COORD_RESPONSE_VALUE, STAFF_SPEECH_THERAPIST_RESPONSE_VALUE,
                            STAFF_TEACHER_AID_RESPONSE_VALUE, STAFF_TUTOR_RESPONSE_VALUE,
                            NONE_RESPONSE_VALUE
                    });
                } else if (FACILITIES_PARAM.equals(sectionFieldName) && FACILITIES_RESPONSE_KEY.equals(responseKey)) {
                    addKeyValueToSection(FACILITIES_RESPONSE_KEY, new String[]{
                            FACILITIES_FARM_RESPONSE_VALUE, FACILITIES_SPORTS_FIELDS_RESPONSE_VALUE,
                            FACILITIES_ARTS_RESPONSE_VALUE, FACILITIES_AUDIO_VISUAL_RESPONSE_VALUE,
                            FACILITIES_AUDITORIUM_RESPONSE_VALUE, FACILITIES_CAFETERIA_RESPONSE_VALUE,
                            FACILITIES_CHAPEL_RESPONSE_VALUE, FACILITIES_COLLEGE_CENTER_RESPONSE_VALUE,
                            FACILITIES_COMPUTER_RESPONSE_VALUE, FACILITIES_GAREDEN_RESPONSE_VALUE,
                            FACILITIES_GYM_RESPONSE_VALUE, FACILITIES_INDUSTRIAL_RESPONSE_VALUE,
                            FACILITIES_INTEREST_RESPONSE_VALUE, FACILITIES_KITCHEN_RESPONSE_VALUE,
                            FACILITIES_LEARNING_LAB_RESPONSE_VALUE, FACILITIES_LIBRARY_RESPONSE_VALUE,
                            FACILITIES_MULTI_PURPOSE_RESPONSE_VALUE, FACILITIES_MUSIC_RESPONSE_VALUE,
                            FACILITIES_OUTDOOR_RESPONSE_VALUE, FACILITIES_PARENT_RESPONSE_VALUE,
                            FACILITIES_PERFORMANCE_RESPONSE_VALUE, FACILITIES_PLAYGROUND_RESPONSE_VALUE,
                            FACILITIES_SCIENCE_RESPONSE_VALUE, FACILITIES_SWIMMING_RESPONSE_VALUE,
                            NONE_RESPONSE_VALUE
                    });
                } else if (FOREIGN_LANGUAGES_PARAM.equals(sectionFieldName) && FOREIGN_LANGUAGES_RESPONSE_KEY.equals(responseKey)) {
                    addKeyValueToSection(FOREIGN_LANGUAGES_RESPONSE_KEY, new String[]{
                            FOREIGN_LANG_ASL_RESPONSE_VALUE, FOREIGN_LANG_AMHARIC_RESPONSE_VALUE,
                            FOREIGN_LANG_ARABIC_RESPONSE_VALUE, FOREIGN_LANG_CANTONESE_RESPONSE_VALUE,
                            FOREIGN_LANG_MANDARIN_RESPONSE_VALUE, FOREIGN_LANG_DUTCH_RESPONSE_VALUE,
                            FOREIGN_LANG_FRENCH_RESPONSE_VALUE, FOREIGN_LANG_GERMAN_RESPONSE_VALUE,
                            FOREIGN_LANG_HMONG_RESPONSE_VALUE, FOREIGN_LANG_ITALIAN_RESPONSE_VALUE,
                            FOREIGN_LANG_JAPANESE_RESPONSE_VALUE, FOREIGN_LANG_KOREAN_RESPONSE_VALUE,
                            FOREIGN_LANG_LATIN_RESPONSE_VALUE, FOREIGN_LANG_RUSSIAN_RESPONSE_VALUE,
                            FOREIGN_LANG_SPANISH_RESPONSE_VALUE, FOREIGN_LANG_TAGALOG_RESPONSE_VALUE,
                            FOREIGN_LANG_URDU_RESPONSE_VALUE, FOREIGN_LANG_VIETNAMESE_RESPONSE_VALUE,
                            NONE_RESPONSE_VALUE
                    });
                } else if (FOREIGN_LANGUAGES_PARAM.equals(sectionFieldName) && FOREIGN_LANGUAGES_OTHER_RESPONSE_KEY.equals(responseKey)) {
                    addKeyValueToSection(FOREIGN_LANGUAGES_OTHER_RESPONSE_KEY, new String[]{});
                } else if (TRANSPORTATION_PARAM.equals(sectionFieldName) && TRANSPORTATION_RESPONSE_KEY.equals(responseKey)) {
                    addKeyValueToSection(TRANSPORTATION_RESPONSE_KEY, new String[]{
                            TRANSPORTATION_BUSSES_RESPONSE_VALUE, TRANSPORTATION_SHARED_BUS_RESPONSE_VALUE,
                            TRANSPORTATION_SPEC_ED_ONLY_RESPONSE_VALUE, TRANSPORTATION_PASSES_RESPONSE_VALUE,
                            NONE_RESPONSE_VALUE
                    });
                } else if (TRANSPORTATION_PARAM.equals(sectionFieldName) && TRANSPORTATION_OTHER_RESPONSE_KEY.equals(responseKey)) {
                    addKeyValueToSection(TRANSPORTATION_OTHER_RESPONSE_KEY, new String[]{});
                } else if (PARENT_INVOLVEMENT_PARAM.equals(sectionFieldName) && PARENT_INVOLVEMENT_RESPONSE_KEY.equals(responseKey)) {
                    addKeyValueToSection(PARENT_INVOLVEMENT_RESPONSE_KEY, new String[]{
                            PARENT_INVOLVEMENT_PARENT_NIGHTS_RESPONSE_VALUE, PARENT_INVOLVEMENT_CHAPERONE_RESPONSE_VALUE,
                            PARENT_INVOLVEMENT_COACH_RESPONSE_VALUE, PARENT_INVOLVEMENT_PTO_PTA_RESPONSE_VALUE,
                            PARENT_INVOLVEMENT_PLAYGROUND_RESPONSE_VALUE, PARENT_INVOLVEMENT_CULTURAL_RESPONSE_VALUE,
                            PARENT_INVOLVEMENT_FUNDRAISING_RESPONSE_VALUE, PARENT_INVOLVEMENT_PRESENTATION_RESPONSE_VALUE,
                            PARENT_INVOLVEMENT_GOVERNANCE_RESPONSE_VALUE, PARENT_INVOLVEMENT_TUTOR_RESPONSE_VALUE,
                            PARENT_INVOLVEMENT_CLASSROOM_RESPONSE_VALUE, PARENT_INVOLVEMENT_AFTER_SCHOOL_RESPONSE_VALUE,
                            NONE_RESPONSE_VALUE
                    });
                } else if (PARENT_INVOLVEMENT_PARAM.equals(sectionFieldName) && PARENT_INVOLVEMENT_OTHER_RESPONSE_KEY.equals(responseKey)) {
                    addKeyValueToSection(PARENT_INVOLVEMENT_OTHER_RESPONSE_KEY, new String[]{});
                }
            }
        }
    }

    ;

    public static void addKeyValueToSection(String key, String[] values) {
        for (String value : values) {
            SECTION_RESPONSE_KEY_VALUE_MAP.put(key, value);
        }
    }

    /**
     * Label for each subsection (identified by response key) - not all questions have subsections
     */
    public static final Map<String, String> RESPONSE_KEY_SUB_SECTION_LABEL = new HashMap<String, String>() {{
        /**
         * Arts & music
         */
        put(ARTS_MEDIA_RESPONSE_KEY, "Media");
        put(ARTS_MUSIC_RESPONSE_KEY, "Music");
        put(ARTS_PERFORMING_WRITTEN_RESPONSE_KEY, "Performing and Written");
        put(ARTS_VISUAL_RESPONSE_KEY, "Visual");
    }};

    /**
     * Label for response values for each question.
     * Added [response key][double underscore separator] prefix to the response values so that the correct label is picked
     * if more than one response key has the same response value but different label. For example, the response value
     * "swimming" exists for boys sports, girls sports and facilities, but the labels for sports and facilities are
     * different.
     */
    public static final Map<String, String> RESPONSE_VALUE_LABEL = new HashMap<String, String>() {{
        /**
         * Arts & music - media
         */
        put(ARTS_MEDIA_RESPONSE_KEY + DOUBLE_UNDERSCORE_SEPARATOR + ARTS_MEDIA_ANIMATION_RESPONSE_VALUE, "Computer animation");
        put(ARTS_MEDIA_RESPONSE_KEY + DOUBLE_UNDERSCORE_SEPARATOR + ARTS_MEDIA_GRAPHICS_RESPONSE_VALUE, "Graphics");
        put(ARTS_MEDIA_RESPONSE_KEY + DOUBLE_UNDERSCORE_SEPARATOR + ARTS_MEDIA_TECH_DESIGN_RESPONSE_VALUE, "Technical design and production");
        put(ARTS_MEDIA_RESPONSE_KEY + DOUBLE_UNDERSCORE_SEPARATOR + ARTS_MEDIA_VIDEO_RESPONSE_VALUE, "Video / Film production");

        /**
         * Arts & music - music
         */
        put(ARTS_MUSIC_RESPONSE_KEY + DOUBLE_UNDERSCORE_SEPARATOR + ARTS_MUSIC_BAND_RESPONSE_VALUE, "Band");
        put(ARTS_MUSIC_RESPONSE_KEY + DOUBLE_UNDERSCORE_SEPARATOR + ARTS_MUSIC_BELLS_RESPONSE_VALUE, "Bell / Handbell choir");
        put(ARTS_MUSIC_RESPONSE_KEY + DOUBLE_UNDERSCORE_SEPARATOR + ARTS_MUSIC_CHAMBER_RESPONSE_VALUE, "Chamber music");
        put(ARTS_MUSIC_RESPONSE_KEY + DOUBLE_UNDERSCORE_SEPARATOR + ARTS_MUSIC_CHORUS_RESPONSE_VALUE, "Choir / Chorus");
        put(ARTS_MUSIC_RESPONSE_KEY + DOUBLE_UNDERSCORE_SEPARATOR + ARTS_MUSIC_LESSONS_RESPONSE_VALUE, "Instrumental music lessons");
        put(ARTS_MUSIC_RESPONSE_KEY + DOUBLE_UNDERSCORE_SEPARATOR + ARTS_MUSIC_JAZZ_RESPONSE_VALUE, "Jazz Band");
        put(ARTS_MUSIC_RESPONSE_KEY + DOUBLE_UNDERSCORE_SEPARATOR + ARTS_MUSIC_OPERA_RESPONSE_VALUE, "Opera");
        put(ARTS_MUSIC_RESPONSE_KEY + DOUBLE_UNDERSCORE_SEPARATOR + ARTS_MUSIC_ORCHESTRA_RESPONSE_VALUE, "Orchestra");
        put(ARTS_MUSIC_RESPONSE_KEY + DOUBLE_UNDERSCORE_SEPARATOR + ARTS_MUSIC_ROCK_RESPONSE_VALUE, "Rock band");
        put(ARTS_MUSIC_RESPONSE_KEY + DOUBLE_UNDERSCORE_SEPARATOR + ARTS_MUSIC_THEORY_RESPONSE_VALUE, "Theory");
        put(ARTS_MUSIC_RESPONSE_KEY + DOUBLE_UNDERSCORE_SEPARATOR + ARTS_MUSIC_VOICE_RESPONSE_VALUE, "Vocal lessons / coaching");

        /**
         * Arts & music - performing
         */
        put(ARTS_PERFORMING_WRITTEN_RESPONSE_KEY + DOUBLE_UNDERSCORE_SEPARATOR + ARTS_PERFORMING_DANCE_RESPONSE_VALUE, "Dance");
        put(ARTS_PERFORMING_WRITTEN_RESPONSE_KEY + DOUBLE_UNDERSCORE_SEPARATOR + ARTS_PERFORMING_DRAMA_RESPONSE_VALUE, "Drama / Theater");
        put(ARTS_PERFORMING_WRITTEN_RESPONSE_KEY + DOUBLE_UNDERSCORE_SEPARATOR + ARTS_PERFORMING_IMPROV_RESPONSE_VALUE, "Improv");
        put(ARTS_PERFORMING_WRITTEN_RESPONSE_KEY + DOUBLE_UNDERSCORE_SEPARATOR + ARTS_PERFORMING_CREATIVE_WRITING_RESPONSE_VALUE, "Creative writing");
        put(ARTS_PERFORMING_WRITTEN_RESPONSE_KEY + DOUBLE_UNDERSCORE_SEPARATOR + ARTS_PERFORMING_POETRY_RESPONSE_VALUE, "Poetry");

        /**
         * Arts & music - visual
         */
        put(ARTS_VISUAL_RESPONSE_KEY + DOUBLE_UNDERSCORE_SEPARATOR + ARTS_VISUAL_ARCH_RESPONSE_VALUE, "Architecture");
        put(ARTS_VISUAL_RESPONSE_KEY + DOUBLE_UNDERSCORE_SEPARATOR + ARTS_VISUAL_CERAMICS_RESPONSE_VALUE, "Ceramics");
        put(ARTS_VISUAL_RESPONSE_KEY + DOUBLE_UNDERSCORE_SEPARATOR + ARTS_VISUAL_DESIGN_RESPONSE_VALUE, "Design");
        put(ARTS_VISUAL_RESPONSE_KEY + DOUBLE_UNDERSCORE_SEPARATOR + ARTS_VISUAL_DRAWING_RESPONSE_VALUE, "Drawing / Sketching");
        put(ARTS_VISUAL_RESPONSE_KEY + DOUBLE_UNDERSCORE_SEPARATOR + ARTS_VISUAL_PAINTING_RESPONSE_VALUE, "Painting");
        put(ARTS_VISUAL_RESPONSE_KEY + DOUBLE_UNDERSCORE_SEPARATOR + ARTS_VISUAL_PHOTO_RESPONSE_VALUE, "Photography");
        put(ARTS_VISUAL_RESPONSE_KEY + DOUBLE_UNDERSCORE_SEPARATOR + ARTS_VISUAL_PRINT_RESPONSE_VALUE, "Printmaking");
        put(ARTS_VISUAL_RESPONSE_KEY + DOUBLE_UNDERSCORE_SEPARATOR + ARTS_VISUAL_SCULPTURE_RESPONSE_VALUE, "Sculpture");
        put(ARTS_VISUAL_RESPONSE_KEY + DOUBLE_UNDERSCORE_SEPARATOR + ARTS_VISUAL_TEXTILES_RESPONSE_VALUE, "Textile design");

        /**
         * Extended care
         */
        put(EXTENDED_CARE_RESPONSE_KEY + DOUBLE_UNDERSCORE_SEPARATOR + EXTENDED_CARE_BEFORE_RESPONSE_VALUE, "Before school");
        put(EXTENDED_CARE_RESPONSE_KEY + DOUBLE_UNDERSCORE_SEPARATOR + EXTENDED_CARE_AFTER_RESPONSE_VALUE, "After school");

        /**
         * Girls and boys sports
         */
        String[] sportsKeys = new String[]{BOYS_SPORTS_RESPONSE_KEY, GIRLS_SPORTS_RESPONSE_KEY};
        for(int i = 0; i < sportsKeys.length; i++) {
            put(sportsKeys[i] + DOUBLE_UNDERSCORE_SEPARATOR + SPORTS_BADMINTON_RESPONSE_VALUE, "Badminton");
            put(sportsKeys[i] + DOUBLE_UNDERSCORE_SEPARATOR + SPORTS_BASKETBALL_RESPONSE_VALUE, "Basketball");
            put(sportsKeys[i] + DOUBLE_UNDERSCORE_SEPARATOR + SPORTS_CHEERLEADING_RESPONSE_VALUE, "Cheerleading");
            put(sportsKeys[i] + DOUBLE_UNDERSCORE_SEPARATOR + SPORTS_CREW_RESPONSE_VALUE, "Crew / Rowing");
            put(sportsKeys[i] + DOUBLE_UNDERSCORE_SEPARATOR + SPORTS_CROSS_COUNTRY_RESPONSE_VALUE, "Cross country");
            put(sportsKeys[i] + DOUBLE_UNDERSCORE_SEPARATOR + SPORTS_CYCLING_RESPONSE_VALUE, "Cycling");
            put(sportsKeys[i] + DOUBLE_UNDERSCORE_SEPARATOR + SPORTS_DIVING_RESPONSE_VALUE, "Diving");
            put(sportsKeys[i] + DOUBLE_UNDERSCORE_SEPARATOR + SPORTS_EQUESTRIAN_RESPONSE_VALUE, "Equestrian");
            put(sportsKeys[i] + DOUBLE_UNDERSCORE_SEPARATOR + SPORTS_FENCING_RESPONSE_VALUE, "Fencing");
            put(sportsKeys[i] + DOUBLE_UNDERSCORE_SEPARATOR + SPORTS_FLAG_FOOTBALL_RESPONSE_VALUE, "Flag football");
            put(sportsKeys[i] + DOUBLE_UNDERSCORE_SEPARATOR + SPORTS_GOLF_RESPONSE_VALUE, "Golf");
            put(sportsKeys[i] + DOUBLE_UNDERSCORE_SEPARATOR + SPORTS_GYMNASTICS_RESPONSE_VALUE, "Gymnastics");
            put(sportsKeys[i] + DOUBLE_UNDERSCORE_SEPARATOR + SPORTS_ICE_HOCKEY_RESPONSE_VALUE, "Ice hockey");
            put(sportsKeys[i] + DOUBLE_UNDERSCORE_SEPARATOR + SPORTS_MARTIAL_ARTS_RESPONSE_VALUE, "Judo / Other martial arts");
            put(sportsKeys[i] + DOUBLE_UNDERSCORE_SEPARATOR + SPORTS_KAYAKING_RESPONSE_VALUE, "Kayaking");
            put(sportsKeys[i] + DOUBLE_UNDERSCORE_SEPARATOR + SPORTS_KICKBALL_RESPONSE_VALUE, "Kickball");
            put(sportsKeys[i] + DOUBLE_UNDERSCORE_SEPARATOR + SPORTS_LACROSSE_RESPONSE_VALUE, "Lacrosse");
            put(sportsKeys[i] + DOUBLE_UNDERSCORE_SEPARATOR + SPORTS_WEIGHT_LIFTING_RESPONSE_VALUE, "Power lifting / Weight lifting");
            put(sportsKeys[i] + DOUBLE_UNDERSCORE_SEPARATOR + SPORTS_RUGBY_RESPONSE_VALUE, "Rugby");
            put(sportsKeys[i] + DOUBLE_UNDERSCORE_SEPARATOR + SPORTS_SAILING_RESPONSE_VALUE, "Sailing");
            put(sportsKeys[i] + DOUBLE_UNDERSCORE_SEPARATOR + SPORTS_SKIING_RESPONSE_VALUE, "Skiing");
            put(sportsKeys[i] + DOUBLE_UNDERSCORE_SEPARATOR + SPORTS_SOCCER_RESPONSE_VALUE, "Soccer");
            put(sportsKeys[i] + DOUBLE_UNDERSCORE_SEPARATOR + SPORTS_SQUASH_RESPONSE_VALUE, "Squash");
            put(sportsKeys[i] + DOUBLE_UNDERSCORE_SEPARATOR + SPORTS_SURFING_RESPONSE_VALUE, "Surfing");
            put(sportsKeys[i] + DOUBLE_UNDERSCORE_SEPARATOR + SPORTS_SWIMMING_RESPONSE_VALUE, "Swimming");
            put(sportsKeys[i] + DOUBLE_UNDERSCORE_SEPARATOR + SPORTS_TENNIS_RESPONSE_VALUE, "Tennis");
            put(sportsKeys[i] + DOUBLE_UNDERSCORE_SEPARATOR + SPORTS_TRACK_RESPONSE_VALUE, "Track");
            put(sportsKeys[i] + DOUBLE_UNDERSCORE_SEPARATOR + SPORTS_ULTIMATE_RESPONSE_VALUE, "Ultimate Frisbee");
            put(sportsKeys[i] + DOUBLE_UNDERSCORE_SEPARATOR + SPORTS_VOLLEYBALL_RESPONSE_VALUE, "Volleyball");
            put(sportsKeys[i] + DOUBLE_UNDERSCORE_SEPARATOR + SPORTS_WATER_POLO_RESPONSE_VALUE, "Water polo");
            put(sportsKeys[i] + DOUBLE_UNDERSCORE_SEPARATOR + SPORTS_WRESTLING_RESPONSE_VALUE, "Wrestling");
        }

        /**
         * Girls only sports
         */
        put(GIRLS_SPORTS_RESPONSE_KEY + DOUBLE_UNDERSCORE_SEPARATOR + GIRLS_SPORTS_FIELD_HOCKEY_RESPONSE_VALUE, "Field hockey");
        put(GIRLS_SPORTS_RESPONSE_KEY + DOUBLE_UNDERSCORE_SEPARATOR + GIRLS_SPORTS_SOFTBALL_RESPONSE_VALUE, "Softball");

        /**
         * Boys only sports
         */
        put(BOYS_SPORTS_RESPONSE_KEY + DOUBLE_UNDERSCORE_SEPARATOR + BOYS_SPORTS_BASEBALL_RESPONSE_VALUE, "Baseball");
        put(BOYS_SPORTS_RESPONSE_KEY + DOUBLE_UNDERSCORE_SEPARATOR + BOYS_SPORTS_FOOTBALL_RESPONSE_VALUE, "Football");

        /**
         * Staff
         */
        put(STAFF_RESPONSE_KEY + DOUBLE_UNDERSCORE_SEPARATOR + STAFF_ART_TEACHER_RESPONSE_VALUE, "Art teacher");
        put(STAFF_RESPONSE_KEY + DOUBLE_UNDERSCORE_SEPARATOR + STAFF_ASSISTANT_PRINCIPAL_RESPONSE_VALUE, "Assistant principal");
        put(STAFF_RESPONSE_KEY + DOUBLE_UNDERSCORE_SEPARATOR + STAFF_COLLEGE_COUNSELOR_RESPONSE_VALUE, "College counselor");
        put(STAFF_RESPONSE_KEY + DOUBLE_UNDERSCORE_SEPARATOR + STAFF_COMP_SPECIALIST_RESPONSE_VALUE, "Computer specialist");
        put(STAFF_RESPONSE_KEY + DOUBLE_UNDERSCORE_SEPARATOR + STAFF_COOKING_TEACHER_RESPONSE_VALUE, "Cooking / Nutrition teacher");
        put(STAFF_RESPONSE_KEY + DOUBLE_UNDERSCORE_SEPARATOR + STAFF_DANCE_TEACHER_RESPONSE_VALUE, "Dance teacher");
        put(STAFF_RESPONSE_KEY + DOUBLE_UNDERSCORE_SEPARATOR + STAFF_ELL_ESL_RESPONSE_VALUE, "ELL / ESL coordinator");
        put(STAFF_RESPONSE_KEY + DOUBLE_UNDERSCORE_SEPARATOR + STAFF_GARDEN_TEACHER_RESPONSE_VALUE, "Gardening teacher");
        put(STAFF_RESPONSE_KEY + DOUBLE_UNDERSCORE_SEPARATOR + STAFF_GIFTED_SPECIALIST_RESPONSE_VALUE, "Gifted specialist");
        put(STAFF_RESPONSE_KEY + DOUBLE_UNDERSCORE_SEPARATOR + STAFF_INSTRUCTIONAL_AID_RESPONSE_VALUE, "Instructional aide / coach");
        put(STAFF_RESPONSE_KEY + DOUBLE_UNDERSCORE_SEPARATOR + STAFF_LIBRARIAN_RESPONSE_VALUE, "Librarian / media specialist");
        put(STAFF_RESPONSE_KEY + DOUBLE_UNDERSCORE_SEPARATOR + STAFF_MATH_SPECIALIST_RESPONSE_VALUE, "Math specialist");
        put(STAFF_RESPONSE_KEY + DOUBLE_UNDERSCORE_SEPARATOR + STAFF_MUSIC_TEACHER_RESPONSE_VALUE, "Music teacher");
        put(STAFF_RESPONSE_KEY + DOUBLE_UNDERSCORE_SEPARATOR + STAFF_NURSE_RESPONSE_VALUE, "Nurse");
        put(STAFF_RESPONSE_KEY + DOUBLE_UNDERSCORE_SEPARATOR + STAFF_PE_INSTRUCTOR_RESPONSE_VALUE, "PE instructor");
        put(STAFF_RESPONSE_KEY + DOUBLE_UNDERSCORE_SEPARATOR + STAFF_POETRY_TEACHER_RESPONSE_VALUE, "Poetry / Creative writing teacher");
        put(STAFF_RESPONSE_KEY + DOUBLE_UNDERSCORE_SEPARATOR + STAFF_PRIEST_RESPONSE_VALUE, "Priest, pastor, or other religious personnel");
        put(STAFF_RESPONSE_KEY + DOUBLE_UNDERSCORE_SEPARATOR + STAFF_READING_SPECIALIST_RESPONSE_VALUE, "Reading specialist");
        put(STAFF_RESPONSE_KEY + DOUBLE_UNDERSCORE_SEPARATOR + STAFF_ROBOTICS_TEACHER_RESPONSE_VALUE, "Robotics / Technology specialist");
        put(STAFF_RESPONSE_KEY + DOUBLE_UNDERSCORE_SEPARATOR + STAFF_SCHOOL_PSYCHOLOGIST_RESPONSE_VALUE, "School psychologist");
        put(STAFF_RESPONSE_KEY + DOUBLE_UNDERSCORE_SEPARATOR + STAFF_SCHOOL_COUNSELOR_RESPONSE_VALUE, "School social worker / counselor");
        put(STAFF_RESPONSE_KEY + DOUBLE_UNDERSCORE_SEPARATOR + STAFF_SECURITY_RESPONSE_VALUE, "Security personnel");
        put(STAFF_RESPONSE_KEY + DOUBLE_UNDERSCORE_SEPARATOR + STAFF_SPECIAL_ED_COORD_RESPONSE_VALUE, "Special education coordinator");
        put(STAFF_RESPONSE_KEY + DOUBLE_UNDERSCORE_SEPARATOR + STAFF_SPEECH_THERAPIST_RESPONSE_VALUE, "Speech and language therapist");
        put(STAFF_RESPONSE_KEY + DOUBLE_UNDERSCORE_SEPARATOR + STAFF_TEACHER_AID_RESPONSE_VALUE, "Teacher aid / Assistant teacher");
        put(STAFF_RESPONSE_KEY + DOUBLE_UNDERSCORE_SEPARATOR + STAFF_TUTOR_RESPONSE_VALUE, "Tutor");

        /**
         * Facilities
         */
        put(FACILITIES_RESPONSE_KEY + DOUBLE_UNDERSCORE_SEPARATOR + FACILITIES_FARM_RESPONSE_VALUE, "Access to farm or natural area");
        put(FACILITIES_RESPONSE_KEY + DOUBLE_UNDERSCORE_SEPARATOR + FACILITIES_SPORTS_FIELDS_RESPONSE_VALUE, "Access to sports fields");
        put(FACILITIES_RESPONSE_KEY + DOUBLE_UNDERSCORE_SEPARATOR + FACILITIES_ARTS_RESPONSE_VALUE, "Art room");
        put(FACILITIES_RESPONSE_KEY + DOUBLE_UNDERSCORE_SEPARATOR + FACILITIES_AUDIO_VISUAL_RESPONSE_VALUE, "Audiovisual aids");
        put(FACILITIES_RESPONSE_KEY + DOUBLE_UNDERSCORE_SEPARATOR + FACILITIES_AUDITORIUM_RESPONSE_VALUE, "Auditorium");
        put(FACILITIES_RESPONSE_KEY + DOUBLE_UNDERSCORE_SEPARATOR + FACILITIES_CAFETERIA_RESPONSE_VALUE, "Cafeteria");
        put(FACILITIES_RESPONSE_KEY + DOUBLE_UNDERSCORE_SEPARATOR + FACILITIES_CHAPEL_RESPONSE_VALUE, "Chapel / spiritual reflection center");
        put(FACILITIES_RESPONSE_KEY + DOUBLE_UNDERSCORE_SEPARATOR + FACILITIES_COLLEGE_CENTER_RESPONSE_VALUE, "College / career center");
        put(FACILITIES_RESPONSE_KEY + DOUBLE_UNDERSCORE_SEPARATOR + FACILITIES_COMPUTER_RESPONSE_VALUE, "Computer lab");
        put(FACILITIES_RESPONSE_KEY + DOUBLE_UNDERSCORE_SEPARATOR + FACILITIES_GAREDEN_RESPONSE_VALUE, "Garden / Greenhouse");
        put(FACILITIES_RESPONSE_KEY + DOUBLE_UNDERSCORE_SEPARATOR + FACILITIES_GYM_RESPONSE_VALUE, "Gym");
        put(FACILITIES_RESPONSE_KEY + DOUBLE_UNDERSCORE_SEPARATOR + FACILITIES_INDUSTRIAL_RESPONSE_VALUE, "Industrial shop");
        put(FACILITIES_RESPONSE_KEY + DOUBLE_UNDERSCORE_SEPARATOR + FACILITIES_INTEREST_RESPONSE_VALUE, "Internet access");
        put(FACILITIES_RESPONSE_KEY + DOUBLE_UNDERSCORE_SEPARATOR + FACILITIES_KITCHEN_RESPONSE_VALUE, "Kitchen");
        put(FACILITIES_RESPONSE_KEY + DOUBLE_UNDERSCORE_SEPARATOR + FACILITIES_LEARNING_LAB_RESPONSE_VALUE, "Learning lab");
        put(FACILITIES_RESPONSE_KEY + DOUBLE_UNDERSCORE_SEPARATOR + FACILITIES_LIBRARY_RESPONSE_VALUE, "Library");
        put(FACILITIES_RESPONSE_KEY + DOUBLE_UNDERSCORE_SEPARATOR + FACILITIES_MULTI_PURPOSE_RESPONSE_VALUE, "Multi-purpose room (\"cafegymatorium\")");
        put(FACILITIES_RESPONSE_KEY + DOUBLE_UNDERSCORE_SEPARATOR + FACILITIES_MUSIC_RESPONSE_VALUE, "Music room");
        put(FACILITIES_RESPONSE_KEY + DOUBLE_UNDERSCORE_SEPARATOR + FACILITIES_OUTDOOR_RESPONSE_VALUE, "Outdoor learning lab");
        put(FACILITIES_RESPONSE_KEY + DOUBLE_UNDERSCORE_SEPARATOR + FACILITIES_PARENT_RESPONSE_VALUE, "Parent center");
        put(FACILITIES_RESPONSE_KEY + DOUBLE_UNDERSCORE_SEPARATOR + FACILITIES_PERFORMANCE_RESPONSE_VALUE, "Performance stage");
        put(FACILITIES_RESPONSE_KEY + DOUBLE_UNDERSCORE_SEPARATOR + FACILITIES_PLAYGROUND_RESPONSE_VALUE, "Playground");
        put(FACILITIES_RESPONSE_KEY + DOUBLE_UNDERSCORE_SEPARATOR + FACILITIES_SCIENCE_RESPONSE_VALUE, "Science lab");
        put(FACILITIES_RESPONSE_KEY + DOUBLE_UNDERSCORE_SEPARATOR + FACILITIES_SWIMMING_RESPONSE_VALUE, "Swimming pool");

        /**
         * Foreign languages
         */
        put(FOREIGN_LANGUAGES_RESPONSE_KEY + DOUBLE_UNDERSCORE_SEPARATOR + FOREIGN_LANG_ASL_RESPONSE_VALUE, "American Sign Language - ASL");
        put(FOREIGN_LANGUAGES_RESPONSE_KEY + DOUBLE_UNDERSCORE_SEPARATOR + FOREIGN_LANG_AMHARIC_RESPONSE_VALUE, "Amharic");
        put(FOREIGN_LANGUAGES_RESPONSE_KEY + DOUBLE_UNDERSCORE_SEPARATOR + FOREIGN_LANG_ARABIC_RESPONSE_VALUE, "Arabic");
        put(FOREIGN_LANGUAGES_RESPONSE_KEY + DOUBLE_UNDERSCORE_SEPARATOR + FOREIGN_LANG_CANTONESE_RESPONSE_VALUE, "Chinese - Cantonese");
        put(FOREIGN_LANGUAGES_RESPONSE_KEY + DOUBLE_UNDERSCORE_SEPARATOR + FOREIGN_LANG_MANDARIN_RESPONSE_VALUE, "Chinese - Mandarin");
        put(FOREIGN_LANGUAGES_RESPONSE_KEY + DOUBLE_UNDERSCORE_SEPARATOR + FOREIGN_LANG_DUTCH_RESPONSE_VALUE, "Dutch");
        put(FOREIGN_LANGUAGES_RESPONSE_KEY + DOUBLE_UNDERSCORE_SEPARATOR + FOREIGN_LANG_FRENCH_RESPONSE_VALUE, "French");
        put(FOREIGN_LANGUAGES_RESPONSE_KEY + DOUBLE_UNDERSCORE_SEPARATOR + FOREIGN_LANG_GERMAN_RESPONSE_VALUE, "German");
        put(FOREIGN_LANGUAGES_RESPONSE_KEY + DOUBLE_UNDERSCORE_SEPARATOR + FOREIGN_LANG_HMONG_RESPONSE_VALUE, "Hmong");
        put(FOREIGN_LANGUAGES_RESPONSE_KEY + DOUBLE_UNDERSCORE_SEPARATOR + FOREIGN_LANG_ITALIAN_RESPONSE_VALUE, "Italian");
        put(FOREIGN_LANGUAGES_RESPONSE_KEY + DOUBLE_UNDERSCORE_SEPARATOR + FOREIGN_LANG_JAPANESE_RESPONSE_VALUE, "Japanese");
        put(FOREIGN_LANGUAGES_RESPONSE_KEY + DOUBLE_UNDERSCORE_SEPARATOR + FOREIGN_LANG_KOREAN_RESPONSE_VALUE, "Korean");
        put(FOREIGN_LANGUAGES_RESPONSE_KEY + DOUBLE_UNDERSCORE_SEPARATOR + FOREIGN_LANG_LATIN_RESPONSE_VALUE, "Latin");
        put(FOREIGN_LANGUAGES_RESPONSE_KEY + DOUBLE_UNDERSCORE_SEPARATOR + FOREIGN_LANG_RUSSIAN_RESPONSE_VALUE, "Russian");
        put(FOREIGN_LANGUAGES_RESPONSE_KEY + DOUBLE_UNDERSCORE_SEPARATOR + FOREIGN_LANG_SPANISH_RESPONSE_VALUE, "Spanish");
        put(FOREIGN_LANGUAGES_RESPONSE_KEY + DOUBLE_UNDERSCORE_SEPARATOR + FOREIGN_LANG_TAGALOG_RESPONSE_VALUE, "Tagalog");
        put(FOREIGN_LANGUAGES_RESPONSE_KEY + DOUBLE_UNDERSCORE_SEPARATOR + FOREIGN_LANG_URDU_RESPONSE_VALUE, "Urdu");
        put(FOREIGN_LANGUAGES_RESPONSE_KEY + DOUBLE_UNDERSCORE_SEPARATOR + FOREIGN_LANG_VIETNAMESE_RESPONSE_VALUE, "Vietnamese");

        /**
         * Transportation
         */
        put(TRANSPORTATION_RESPONSE_KEY + DOUBLE_UNDERSCORE_SEPARATOR + TRANSPORTATION_BUSSES_RESPONSE_VALUE, "Buses / vans provided for students");
        put(TRANSPORTATION_RESPONSE_KEY + DOUBLE_UNDERSCORE_SEPARATOR + TRANSPORTATION_SHARED_BUS_RESPONSE_VALUE, "Buses / vans shared with other schools");
        put(TRANSPORTATION_RESPONSE_KEY + DOUBLE_UNDERSCORE_SEPARATOR + TRANSPORTATION_SPEC_ED_ONLY_RESPONSE_VALUE, "Transportation provided for special education students");
        put(TRANSPORTATION_RESPONSE_KEY + DOUBLE_UNDERSCORE_SEPARATOR + TRANSPORTATION_PASSES_RESPONSE_VALUE, "Passes / tokens provided for public transportation");

        /**
         * Parent involvement
         */
        put(PARENT_INVOLVEMENT_RESPONSE_KEY + DOUBLE_UNDERSCORE_SEPARATOR + PARENT_INVOLVEMENT_PARENT_NIGHTS_RESPONSE_VALUE, "Attend parent nights");
        put(PARENT_INVOLVEMENT_RESPONSE_KEY + DOUBLE_UNDERSCORE_SEPARATOR + PARENT_INVOLVEMENT_CHAPERONE_RESPONSE_VALUE, "Chaperone school trips");
        put(PARENT_INVOLVEMENT_RESPONSE_KEY + DOUBLE_UNDERSCORE_SEPARATOR + PARENT_INVOLVEMENT_COACH_RESPONSE_VALUE, "Coach sports teams or extracurricular activities");
        put(PARENT_INVOLVEMENT_RESPONSE_KEY + DOUBLE_UNDERSCORE_SEPARATOR + PARENT_INVOLVEMENT_PTO_PTA_RESPONSE_VALUE, "Join PTO / PTA");
        put(PARENT_INVOLVEMENT_RESPONSE_KEY + DOUBLE_UNDERSCORE_SEPARATOR + PARENT_INVOLVEMENT_PLAYGROUND_RESPONSE_VALUE, "Monitor the playground");
        put(PARENT_INVOLVEMENT_RESPONSE_KEY + DOUBLE_UNDERSCORE_SEPARATOR + PARENT_INVOLVEMENT_CULTURAL_RESPONSE_VALUE, "Organize cultural events");
        put(PARENT_INVOLVEMENT_RESPONSE_KEY + DOUBLE_UNDERSCORE_SEPARATOR + PARENT_INVOLVEMENT_FUNDRAISING_RESPONSE_VALUE, "Organize fundraising events (school auction, bake sales, etc.)");
        put(PARENT_INVOLVEMENT_RESPONSE_KEY + DOUBLE_UNDERSCORE_SEPARATOR + PARENT_INVOLVEMENT_PRESENTATION_RESPONSE_VALUE, "Present special topics during curricular units");
        put(PARENT_INVOLVEMENT_RESPONSE_KEY + DOUBLE_UNDERSCORE_SEPARATOR + PARENT_INVOLVEMENT_GOVERNANCE_RESPONSE_VALUE, "Serve on school improvement team or governance council");
        put(PARENT_INVOLVEMENT_RESPONSE_KEY + DOUBLE_UNDERSCORE_SEPARATOR + PARENT_INVOLVEMENT_TUTOR_RESPONSE_VALUE, "Tutor");
        put(PARENT_INVOLVEMENT_RESPONSE_KEY + DOUBLE_UNDERSCORE_SEPARATOR + PARENT_INVOLVEMENT_CLASSROOM_RESPONSE_VALUE, "Volunteer in the classroom");
        put(PARENT_INVOLVEMENT_RESPONSE_KEY + DOUBLE_UNDERSCORE_SEPARATOR + PARENT_INVOLVEMENT_AFTER_SCHOOL_RESPONSE_VALUE, "Volunteer time after school");
    }};

    /**
     * Map form field section name to form field label
     */
    public static final Map<String, String> FORM_FIELD_TITLES = new LinkedHashMap<String, String>() {{
        put(ARTS_MUSIC_PARAM, ARTS_MUSIC_TITLE);
        put(EXTENDED_CARE_PARAM, EXTENDED_CARE_TITLE);
        put(GIRLS_SPORTS_PARAM, GIRLS_SPORTS_TITLE);
        put(STAFF_PARAM, STAFF_TITLE);
        put(FACILITIES_PARAM, FACILITIES_TITLE);
        put(FOREIGN_LANGUAGES_PARAM, FOREIGN_LANGUAGES_TITLE);
        put(TRANSPORTATION_PARAM, TRANSPORTATION_TITLE);
        put(BOYS_SPORTS_PARAM, BOYS_SPORTS_TITLE);
        put(PARENT_INVOLVEMENT_PARAM, PARENT_INVOLVEMENT_TITLE);
    }};

    /**
     * Ghost text values for fields
     */
    public static final Map<String, String> FORM_FIELD_GHOST_TEXT = new HashMap<String, String>() {{
        put(ARTS_MUSIC_PARAM, "What arts and music programs does this school offer?");
        put(FOREIGN_LANGUAGES_PARAM, "What foreign languages does this school offer?");
        put(EXTENDED_CARE_PARAM, "What before/after school care does this school offer?");
        put(TRANSPORTATION_PARAM, "What types of transportation does this school offer?");
        put(GIRLS_SPORTS_PARAM, "What girls sports does this school offer?");
        put(BOYS_SPORTS_PARAM, "What boys sports does this school offer?");
        put(STAFF_PARAM, "What types of staff work at this school?");
        put(PARENT_INVOLVEMENT_PARAM, "How are parents involved at this school?");
        put(FACILITIES_PARAM, "What facilities does this school have?");
    }};

    public void formFieldsBuilderHelper(ModelMap modelMap,
                                        HttpServletRequest request,
                                        HttpServletResponse response,
                                        School school,
                                        State state,
                                        User user,
                                        boolean isOspUser) {

        modelMap.put("school", school);
        Multimap<String, String> savedResponseKeyValues = getSavedResponses(user, school, state, isOspUser);

        List<UspFormResponseStruct> uspFormResponses = formFieldsBuilderHelper(savedResponseKeyValues, isOspUser);

        modelMap.put("uspFormResponses", uspFormResponses);
    }

    public List<UspFormResponseStruct> formFieldsBuilderHelper(Multimap<String, String> savedResponseKeyValues,
                                                                boolean isOspUser) {

        List<UspFormResponseStruct> uspFormResponses = new LinkedList<UspFormResponseStruct>();

        /**
         * For each enum value (form fields), construct the usp response object. Each section has one (no subsection) or
         * more section responses.
         * For each response key that the form field has, get all the response values from the multimap and construct
         * section response. Each section response has a list of response values objects.
         */
        for (SectionResponseKeys sectionResponseKeys : SectionResponseKeys.values()) {
            uspFormResponses.add(buildSectionResponse(sectionResponseKeys, savedResponseKeyValues, isOspUser));
        }

        return uspFormResponses;
    }

    protected UspFormResponseStruct buildSectionResponse(SectionResponseKeys sectionResponseKeys,
                                        Multimap<String, String> savedResponseKeyValues,
                                        boolean isOspUser) {
        String fieldName = sectionResponseKeys.getSectionFieldName();
        String sectionTitle = FORM_FIELD_TITLES.get(fieldName);
        String ghostText = FORM_FIELD_GHOST_TEXT.get(fieldName);
        UspFormResponseStruct uspFormResponse = new UspFormResponseStruct(fieldName, sectionTitle);
        uspFormResponse.setIsSchoolAdmin(isOspUser);
        uspFormResponse.setGhostText(ghostText);
        boolean hasNoneField = false;

        List<UspFormResponseStruct.SectionResponse> sectionResponses = uspFormResponse.getSectionResponses();

        String[] responseKeys = sectionResponseKeys.getResponseKeys();
        for (String responseKey : responseKeys) {
            Collection<String> responseValues = SECTION_RESPONSE_KEY_VALUE_MAP.get(responseKey);
            UspFormResponseStruct.SectionResponse sectionResponse = uspFormResponse.new SectionResponse(responseKey);
            sectionResponse.setTitle(RESPONSE_KEY_SUB_SECTION_LABEL.get(responseKey));

            Collection<String> savedResponses = savedResponseKeyValues.get(responseKey);

            List<UspFormResponseStruct.SectionResponse.UspResponseValueStruct> uspResponseValues = sectionResponse.getResponses();

            /**
             * If the key belongs to other field and the value is not blank, set the other checkbox and textfield value to
             * response_value for the form question
             * Set only for osp users.
             */

            if (isOspUser && responseKey.endsWith("_other")) {
                boolean isOtherFieldKey = true;
                uspFormResponse.setHasOtherField(isOtherFieldKey);
                uspFormResponse.setOtherTextLength(responseKey);

                Iterator<String> savedResponsesIter = savedResponses.iterator();
                while (savedResponsesIter.hasNext()) {
                    String responseValue = savedResponsesIter.next();
                    if (!responseValue.trim().equals("")) {
                        uspFormResponse.setIsOtherChecked(true);
                        uspFormResponse.setOtherTextValue(responseValue);
                        UspFormResponseStruct.SectionResponse.UspResponseValueStruct uspResponseValue =
                                sectionResponse.new UspResponseValueStruct(responseValue);
                        uspResponseValues.add(uspResponseValue);
                    }
                }
            }
            /**
             * There is none field for a question if there is a "none" or "None" or "neither" value in the enum
             * SectionResponseKeys. Only boys sports has "None" and only extended care has "neither"
             * Set none checkbox to true if none response value exists for the current response key and if the user
             * is a school admin and/or
             * Construct chosen select field options with the list of response values for the current response key
             */
            else {
                if (isOspUser && !hasNoneField && (responseValues.contains(NONE_RESPONSE_VALUE) ||
                        responseKey.equals(BOYS_SPORTS_RESPONSE_KEY) || responseKey.equals(EXTENDED_CARE_RESPONSE_KEY))) {
                    hasNoneField = true;
                    uspFormResponse.setHasNoneField(hasNoneField);
                }

                Iterator<String> responseValueIter = responseValues.iterator();

                while (responseValueIter.hasNext()) {
                    String responseValue = responseValueIter.next();

                    /**
                     * Ignore case so that none checkbox would be checked for boys_sports that has saved response value "None"
                     * For extended care the response value for none is "neither"
                     * If there is none field and if that is not checked, check if there is "none" or "neither" response. For arts and music, check
                     * none only if all subsections have none response.
                     * If the current response value is not "none" or "neither", build the chosen drop down.
                     */
                    if (hasNoneField && !uspFormResponse.isNoneChecked() && (NONE_RESPONSE_VALUE.equalsIgnoreCase(responseValue) ||
                            (responseKey.equals(EXTENDED_CARE_RESPONSE_KEY) && EXTENDED_CARE_NEITHER_RESPONSE_VALUE.equals(responseValue)))) {
                        boolean isArtsMusicSection = ARTS_MUSIC_TITLE.equals(sectionTitle);
                        if(isArtsMusicSection && savedResponseKeyValues.get(ARTS_MEDIA_RESPONSE_KEY).contains(responseValue)
                                && savedResponseKeyValues.get(ARTS_MUSIC_RESPONSE_KEY).contains(responseValue) &&
                                savedResponseKeyValues.get(ARTS_PERFORMING_WRITTEN_RESPONSE_KEY).contains(responseValue) &&
                                savedResponseKeyValues.get(ARTS_VISUAL_RESPONSE_KEY).contains(responseValue)) {
                            uspFormResponse.setIsNoneChecked(true);
                        }
                        else if (!isArtsMusicSection && savedResponses.contains(responseValue)) {
                            uspFormResponse.setIsNoneChecked(true);
                        }
                    } else {
                        UspFormResponseStruct.SectionResponse.UspResponseValueStruct uspResponseValue =
                                sectionResponse.new UspResponseValueStruct(responseValue);

                        uspResponseValue.setLabel(RESPONSE_VALUE_LABEL.get(responseKey + DOUBLE_UNDERSCORE_SEPARATOR
                                + responseValue));

                        if (savedResponses.contains(responseValue)) {
                            uspResponseValue.setIsSelected(true);
                        }

                        uspResponseValues.add(uspResponseValue);
                    }
                }
            }

            sectionResponse.setResponses(uspResponseValues);
            sectionResponses.add(sectionResponse);
        }

        uspFormResponse.setSectionResponses(sectionResponses);
        return uspFormResponse;
    }

    protected Multimap<String, String> getSavedResponses(User user, School school, State state, final boolean isOspUser) {
        Multimap<String, String> responseKeyValues = ArrayListMultimap.create();

        if (user != null) {
            List<EspResponseSource> responseSources = new ArrayList<EspResponseSource>(){{
                if(isOspUser) {
                    add(EspResponseSource.osp);
                    add(EspResponseSource.datateam);
                }
                else {
                    add(EspResponseSource.usp);
                }
            }};

            /**
             * for osp user, we need to get both both osp and datateam responses, so the user id needs to be null and the
             * criteria filter condition will not be set.
             */
            List<Object[]> keyValuePairs = _espResponseDao.getAllUniqueResponsesForSchoolBySourceAndByUser(school, state,
                    responseSources, isOspUser ? null : user.getId());
            for (Object[] keyValue : keyValuePairs) {
                responseKeyValues.put((String) keyValue[0], (String) keyValue[1]);
            }
        }

        return responseKeyValues;
    }

    /**
     * Returns json object that has a list of form elements (json array). Each form element has a list of section responses
     * (json array), and the length of that will most likely be 1, except for arts & music which has 4 subsections. Each
     * section in the form element has response key that will have list of response values (json array).
     * The json object returned will look like this -
     * {"formFields":
     * [{form element 1 attributes... , "responses":
     * [{section 1 attributes... , "values":
     * [{response value 1 attributes}, {response value 2 attributes}... ]},
     * {section 2 attributes}... ]},
     * {form element 2... },
     * ... ]}
     *
     * @param uspFormResponses
     * @param user
     * @param isOspUser
     * @return
     */
    public JSONObject jsonFormFieldsBuilderHelper(List<UspFormResponseStruct> uspFormResponses,
                                                  User user,
                                                  boolean isOspUser) {
        JSONObject responseJson = new JSONObject();
        try {
            JSONArray formFields = new JSONArray();
            int numFormSections = uspFormResponses.size();

            for(int i = 0; i < numFormSections; i++) {
                UspFormResponseStruct formResponseStruct = uspFormResponses.get(i);

                JSONObject formField = new JSONObject();
                formField.put(FIELD_NAME_JSON_RESPONSE_KEY, formResponseStruct.getFieldName());
                formField.put(TITLE_JSON_RESPONSE_KEY, formResponseStruct.getTitle());
                formField.put(GHOST_TEXT_JSON_RESPONSE_KEY, formResponseStruct.getGhostText());

                JSONArray responses = new JSONArray();
                List<UspFormResponseStruct.SectionResponse> sectionResponses = formResponseStruct.getSectionResponses();
                int numSectionResponses = sectionResponses.size();

                for(int j = 0; j < numSectionResponses; j++) {
                    UspFormResponseStruct.SectionResponse sectionResponse = sectionResponses.get(j);
                    String key = sectionResponse.getResponseKey();
                    if(key.endsWith("_other") && !isOspUser) {
                        continue;
                    }

                    JSONObject response = new JSONObject();
                    response.put(TITLE_JSON_RESPONSE_KEY, sectionResponse.getTitle());
                    response.put(KEY_JSON_RESPONSE_KEY, key);

                    JSONArray values = new JSONArray();
                    List<UspFormResponseStruct.SectionResponse.UspResponseValueStruct> responseValues = sectionResponse.getResponses();
                    int numresponseValues = responseValues.size();

                    for (int k = 0; k < numresponseValues; k++) {
                        UspFormResponseStruct.SectionResponse.UspResponseValueStruct responseValue = responseValues.get(k);
                        String respValue = responseValue.getResponseValue();
                        if((NONE_RESPONSE_VALUE.equalsIgnoreCase(respValue) || EXTENDED_CARE_NEITHER_RESPONSE_VALUE.equals(respValue))
                                && !isOspUser) {
                            continue;
                        }

                        JSONObject value = new JSONObject();
                        value.put(LABEL_JSON_RESPONSE_KEY, responseValue.getLabel());
                        value.put(VALUE_JSON_RESPONSE_KEY, respValue);
                        value.put(IS_SELECTED_JSON_RESPONSE_KEY, responseValue.isSelected());

                        values.put(value);
                    }

                    response.put(VALUES_JSON_RESPONSE_KEY, values);
                    responses.put(response);
                }

                formField.put(RESPONSES_JSON_RESPONSE_KEY, responses);
                formFields.put(formField);
            }
            responseJson.put(FORM_FIELDS_JSON_RESPONSE_KEY, formFields);

            if(user != null) {
                JSONObject userDetails = new JSONObject();
                userDetails.put(USER_ID_JSON_RESPONSE_KEY, user.getId());
                userDetails.put(USER_EMAIL_JSON_RESPONSE_KEY, user.getEmail());
                userDetails.put(USER_SCREENNAME_JSON_RESPONSE_KEY, user.getUserProfile() != null ? user.getUserProfile().getScreenName()
                        : "");
                userDetails.put(NUM_MSL_JSON_RESPONSE_KEY, user.getFavoriteSchools() != null ?
                        user.getFavoriteSchools().size() : 0);

                responseJson.put(USER_JSON_RESPONSE_KEY, userDetails);
            }
        } catch (JSONException ex) {
            _logger.warn("UspFormHelper - exception while trying to write json object.", ex);
        }

        return responseJson;
    }

    public static final String FORM_FIELDS_JSON_RESPONSE_KEY = "formFields";
    public static final String RESPONSES_JSON_RESPONSE_KEY = "responses";
    public static final String FIELD_NAME_JSON_RESPONSE_KEY = "fieldName";
    public static final String TITLE_JSON_RESPONSE_KEY = "title";
    public static final String GHOST_TEXT_JSON_RESPONSE_KEY = "ghostText";
    public static final String KEY_JSON_RESPONSE_KEY = "key";
    public static final String LABEL_JSON_RESPONSE_KEY = "label";
    public static final String VALUE_JSON_RESPONSE_KEY = "responseValue";
    public static final String IS_SELECTED_JSON_RESPONSE_KEY = "isSelected";
    public static final String VALUES_JSON_RESPONSE_KEY = "values";
    public static final String USER_ID_JSON_RESPONSE_KEY = "id";
    public static final String USER_EMAIL_JSON_RESPONSE_KEY = "email";
    public static final String USER_SCREENNAME_JSON_RESPONSE_KEY = "screenName";
    public static final String NUM_MSL_JSON_RESPONSE_KEY = "numberMSLItems";
    public static final String USER_JSON_RESPONSE_KEY = "user";

    public IEspResponseDao getEspResponseDao() {
        return _espResponseDao;
    }

    public void setEspResponseDao(IEspResponseDao _espResponseDao) {
        this._espResponseDao = _espResponseDao;
    }

}
