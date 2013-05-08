package gs.web.school.usp;

import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;

import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: rramachandran
 * Date: 4/30/13
 * Time: 5:18 PM
 * To change this template use File | Settings | File Templates.
 */
public class UspHelper {
    /**
     * Title for each question on usp form page
     */
    public static final String ARTS_MUSIC_TITLE = "Arts & music";
    public static final String EXTENDED_CARE_TITLE = "Extended care";
    public static final String GIRLS_SPORTS_TITLE = "Girls' sports";
    public static final String BOYS_SPORTS_TITLE = "Boys' sports";
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

    /**
     * Response key and values for foreign languages
     */
    public static final String FOREIGN_LANGUAGES_RESPONSE_KEY = "foreign_language";

    /**
     * Response key and values for transportation
     */
    public static final String TRANSPORTATION_RESPONSE_KEYS = "transportation";

    /**
     * Response key and values for parent involvement
     */
    public static final String PARENT_INVOLVEMENT_RESPONSE_KEYS = "parent_involvement";

    /**
     * Enum with form field section name and arrays of response keys.
     * "Other" field response key is not included because that is not listed in the drop down.
     */
    public static enum SectionResponseKeys {
        arts(ARTS_MUSIC_PARAM, new String[]{ARTS_MEDIA_RESPONSE_KEY, ARTS_MUSIC_RESPONSE_KEY, ARTS_PERFORMING_WRITTEN_RESPONSE_KEY,
                ARTS_VISUAL_RESPONSE_KEY}),
        extCare(EXTENDED_CARE_PARAM, new String[]{EXTENDED_CARE_RESPONSE_KEY}),
        girlsSports(GIRLS_SPORTS_PARAM, new String[]{GIRLS_SPORTS_RESPONSE_KEY}),
        staff(STAFF_PARAM, new String[]{STAFF_RESPONSE_KEY}),
        facilities(FACILITIES_PARAM, new String[]{FACILITIES_RESPONSE_KEY}),
        foreignLanguages(FOREIGN_LANGUAGES_PARAM, new String[]{FOREIGN_LANGUAGES_RESPONSE_KEY}),
        transportation(TRANSPORTATION_PARAM, new String[]{TRANSPORTATION_RESPONSE_KEYS}),
        boysSports(BOYS_SPORTS_PARAM, new String[]{BOYS_SPORTS_RESPONSE_KEY}),
        parentInvolvement(PARENT_INVOLVEMENT_PARAM, new String[]{PARENT_INVOLVEMENT_RESPONSE_KEYS});

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
     * Changing the order of the values will change the order in which they are displayed
     */
    public static final Multimap<String, String> SECTION_RESPONSE_KEY_VALUE_MAP = LinkedListMultimap.create();
    static {
        for(SectionResponseKeys sectionResponseKeys : SectionResponseKeys.values()) {
            String sectionTitle = sectionResponseKeys.getSectionFieldName();
            String[] responseKeys = sectionResponseKeys.getResponseKeys();

            for(String responseKey : responseKeys) {
                if(ARTS_MUSIC_PARAM.equals(sectionTitle)) {
                    if(ARTS_MEDIA_RESPONSE_KEY.equals(responseKey)) {
                        addKeyValueToSection(ARTS_MEDIA_RESPONSE_KEY, new String[] {
                                ARTS_MEDIA_ANIMATION_RESPONSE_VALUE , ARTS_MEDIA_GRAPHICS_RESPONSE_VALUE,
                                ARTS_MEDIA_TECH_DESIGN_RESPONSE_VALUE, ARTS_MEDIA_VIDEO_RESPONSE_VALUE,
                                NONE_RESPONSE_VALUE
                        });
                    }
                    else if(ARTS_MUSIC_RESPONSE_KEY.equals(responseKey)) {
                        addKeyValueToSection(ARTS_MUSIC_RESPONSE_KEY, new String[]{
                                ARTS_MUSIC_BAND_RESPONSE_VALUE, ARTS_MUSIC_BELLS_RESPONSE_VALUE,
                                ARTS_MUSIC_CHAMBER_RESPONSE_VALUE, ARTS_MUSIC_CHORUS_RESPONSE_VALUE,
                                ARTS_MUSIC_LESSONS_RESPONSE_VALUE, ARTS_MUSIC_JAZZ_RESPONSE_VALUE,
                                ARTS_MUSIC_OPERA_RESPONSE_VALUE, ARTS_MUSIC_ORCHESTRA_RESPONSE_VALUE,
                                ARTS_MUSIC_ROCK_RESPONSE_VALUE, ARTS_MUSIC_THEORY_RESPONSE_VALUE,
                                ARTS_MUSIC_VOICE_RESPONSE_VALUE, NONE_RESPONSE_VALUE
                        });
                    }
                    else if(ARTS_PERFORMING_WRITTEN_RESPONSE_KEY.equals(responseKey)) {
                        addKeyValueToSection(ARTS_PERFORMING_WRITTEN_RESPONSE_KEY, new String[]{
                                ARTS_PERFORMING_DANCE_RESPONSE_VALUE, ARTS_PERFORMING_DRAMA_RESPONSE_VALUE,
                                ARTS_PERFORMING_IMPROV_RESPONSE_VALUE, ARTS_PERFORMING_CREATIVE_WRITING_RESPONSE_VALUE,
                                ARTS_PERFORMING_POETRY_RESPONSE_VALUE, NONE_RESPONSE_VALUE
                        });
                    }
                    else if(ARTS_VISUAL_RESPONSE_KEY.equals(responseKey)) {
                        addKeyValueToSection(ARTS_VISUAL_RESPONSE_KEY, new String[]{
                                ARTS_VISUAL_ARCH_RESPONSE_VALUE, ARTS_VISUAL_CERAMICS_RESPONSE_VALUE,
                                ARTS_VISUAL_DESIGN_RESPONSE_VALUE, ARTS_VISUAL_DRAWING_RESPONSE_VALUE,
                                ARTS_VISUAL_PAINTING_RESPONSE_VALUE, ARTS_VISUAL_PHOTO_RESPONSE_VALUE,
                                ARTS_VISUAL_PRINT_RESPONSE_VALUE, ARTS_VISUAL_SCULPTURE_RESPONSE_VALUE,
                                ARTS_VISUAL_TEXTILES_RESPONSE_VALUE, NONE_RESPONSE_VALUE
                        });
                    }
                }

                else if(EXTENDED_CARE_PARAM.equals(sectionTitle)) {
                    if(EXTENDED_CARE_RESPONSE_KEY.equals(responseKey)) {
                        addKeyValueToSection(EXTENDED_CARE_RESPONSE_KEY, new String[]{
                                EXTENDED_CARE_BEFORE_RESPONSE_VALUE, EXTENDED_CARE_AFTER_RESPONSE_VALUE,
                                NONE_RESPONSE_VALUE
                        });
                    }
                }

                else if(GIRLS_SPORTS_PARAM.equals(sectionTitle)) {
                    if(GIRLS_SPORTS_RESPONSE_KEY.equals(responseKey)) {
                        addKeyValueToSection(GIRLS_SPORTS_RESPONSE_KEY, new String[]{
                                SPORTS_BADMINTON_RESPONSE_VALUE, SPORTS_BASKETBALL_RESPONSE_VALUE,
                                SPORTS_CHEERLEADING_RESPONSE_VALUE, SPORTS_CREW_RESPONSE_VALUE,
                                SPORTS_CROSS_COUNTRY_RESPONSE_VALUE, SPORTS_CYCLING_RESPONSE_VALUE,
                                SPORTS_DIVING_RESPONSE_VALUE, SPORTS_EQUESTRIAN_RESPONSE_VALUE,
                                SPORTS_FENCING_RESPONSE_VALUE , GIRLS_SPORTS_FIELD_HOCKEY_RESPONSE_VALUE,
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
                                SPORTS_WRESTLING_RESPONSE_VALUE
                        });
                    }
                }

                else if(BOYS_SPORTS_PARAM.equals(sectionTitle)) {
                    if(BOYS_SPORTS_RESPONSE_KEY.equals(responseKey)) {
                        addKeyValueToSection(BOYS_SPORTS_RESPONSE_KEY, new String[]{
                                SPORTS_BADMINTON_RESPONSE_VALUE, BOYS_SPORTS_BASEBALL_RESPONSE_VALUE,
                                SPORTS_BASKETBALL_RESPONSE_VALUE,
                                SPORTS_CHEERLEADING_RESPONSE_VALUE, SPORTS_CREW_RESPONSE_VALUE,
                                SPORTS_CROSS_COUNTRY_RESPONSE_VALUE, SPORTS_CYCLING_RESPONSE_VALUE,
                                SPORTS_DIVING_RESPONSE_VALUE, SPORTS_EQUESTRIAN_RESPONSE_VALUE,
                                SPORTS_FENCING_RESPONSE_VALUE , SPORTS_FLAG_FOOTBALL_RESPONSE_VALUE,
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
                                SPORTS_WRESTLING_RESPONSE_VALUE
                        });
                    }
                }
            }
        }
    };

    public static void addKeyValueToSection(String key, String[] values) {
        for(String value : values) {
            SECTION_RESPONSE_KEY_VALUE_MAP.put(key, value);
        }
    }

    /**
     * Label for each subsection (identified by response key) - not all questions have subsections
     */
    public static final Map<String, String> RESPONSE_KEY_SUB_SECTION_LABEL = new HashMap<String, String>(){{
        /**
         * Arts & music
         */
        put(ARTS_MEDIA_RESPONSE_KEY, "Media");
        put(ARTS_MUSIC_RESPONSE_KEY, "Music");
        put(ARTS_PERFORMING_WRITTEN_RESPONSE_KEY, "Performing");
        put(ARTS_VISUAL_RESPONSE_KEY, "Visual");
    }};

    /**
     * Label for response values for each question
     */
    public static final Map<String, String> RESPONSE_VALUE_LABEL = new HashMap<String, String>(){{
        /**
         * Arts & music - media
         */
        put(ARTS_MEDIA_ANIMATION_RESPONSE_VALUE, "Computer Animation");
        put(ARTS_MEDIA_GRAPHICS_RESPONSE_VALUE, "Graphics");
        put(ARTS_MEDIA_TECH_DESIGN_RESPONSE_VALUE, "Technical design and production");
        put(ARTS_MEDIA_VIDEO_RESPONSE_VALUE, "Video / Film production");

        /**
         * Arts & music - music
         */
        put(ARTS_MUSIC_BAND_RESPONSE_VALUE, "Band");
        put(ARTS_MUSIC_BELLS_RESPONSE_VALUE, "Bell / Handbell choir");
        put(ARTS_MUSIC_CHAMBER_RESPONSE_VALUE, "Chamber music");
        put(ARTS_MUSIC_CHORUS_RESPONSE_VALUE, "Choir / Chorus");
        put(ARTS_MUSIC_LESSONS_RESPONSE_VALUE, "Instrumental music lessons");
        put(ARTS_MUSIC_JAZZ_RESPONSE_VALUE, "Jazz Band");
        put(ARTS_MUSIC_OPERA_RESPONSE_VALUE, "Opera");
        put(ARTS_MUSIC_ORCHESTRA_RESPONSE_VALUE, "Orchestra");
        put(ARTS_MUSIC_ROCK_RESPONSE_VALUE, "Rock band");
        put(ARTS_MUSIC_THEORY_RESPONSE_VALUE, "Theory");
        put(ARTS_MUSIC_VOICE_RESPONSE_VALUE, "Vocal lessons / coaching");

        /**
         * Arts & music - performing
         */
        put(ARTS_PERFORMING_DANCE_RESPONSE_VALUE, "Dance");
        put(ARTS_PERFORMING_DRAMA_RESPONSE_VALUE, "Drama");
        put(ARTS_PERFORMING_IMPROV_RESPONSE_VALUE, "Improv");
        put(ARTS_PERFORMING_CREATIVE_WRITING_RESPONSE_VALUE, "Creative writing");
        put(ARTS_PERFORMING_POETRY_RESPONSE_VALUE, "Poetry");

        /**
         * Arts & music - visual
         */
        put(ARTS_VISUAL_ARCH_RESPONSE_VALUE, "Architecture");
        put(ARTS_VISUAL_CERAMICS_RESPONSE_VALUE, "Ceramics");
        put(ARTS_VISUAL_DESIGN_RESPONSE_VALUE, "Design");
        put(ARTS_VISUAL_DRAWING_RESPONSE_VALUE, "Drawing / Sketching");
        put(ARTS_VISUAL_PAINTING_RESPONSE_VALUE, "Painting");
        put(ARTS_VISUAL_PHOTO_RESPONSE_VALUE, "Photography");
        put(ARTS_VISUAL_PRINT_RESPONSE_VALUE, "Printmaking");
        put(ARTS_VISUAL_SCULPTURE_RESPONSE_VALUE, "Sculpture");
        put(ARTS_VISUAL_TEXTILES_RESPONSE_VALUE, "Textile design");

        /**
         * Extended care
         */
        put(EXTENDED_CARE_BEFORE_RESPONSE_VALUE, "Before school");
        put(EXTENDED_CARE_AFTER_RESPONSE_VALUE, "After school");

        /**
         * Girls and boys sports
         */
        put(SPORTS_BADMINTON_RESPONSE_VALUE, "Badminton");
        put(SPORTS_BASKETBALL_RESPONSE_VALUE, "Basketball");
        put(SPORTS_CHEERLEADING_RESPONSE_VALUE, "Cheerleading");
        put(SPORTS_CREW_RESPONSE_VALUE, "Crew / Rowing");
        put(SPORTS_CROSS_COUNTRY_RESPONSE_VALUE, "Cross country");
        put(SPORTS_CYCLING_RESPONSE_VALUE, "Cycling");
        put(SPORTS_DIVING_RESPONSE_VALUE, "Diving");
        put(SPORTS_EQUESTRIAN_RESPONSE_VALUE, "Equestrian");
        put(SPORTS_FENCING_RESPONSE_VALUE, "Fencing");
        put(SPORTS_FLAG_FOOTBALL_RESPONSE_VALUE, "Flag football");
        put(SPORTS_GOLF_RESPONSE_VALUE, "Golf");
        put(SPORTS_GYMNASTICS_RESPONSE_VALUE, "Gymnastics");
        put(SPORTS_ICE_HOCKEY_RESPONSE_VALUE, "Ice hockey");
        put(SPORTS_MARTIAL_ARTS_RESPONSE_VALUE, "Judo / Other martial arts");
        put(SPORTS_KAYAKING_RESPONSE_VALUE, "Kayaking");
        put(SPORTS_KICKBALL_RESPONSE_VALUE, "Kickball");
        put(SPORTS_LACROSSE_RESPONSE_VALUE, "Lacrosse");
        put(SPORTS_WEIGHT_LIFTING_RESPONSE_VALUE, "Power lifting / Weight lifting");
        put(SPORTS_RUGBY_RESPONSE_VALUE, "Rugby");
        put(SPORTS_SAILING_RESPONSE_VALUE, "Sailing");
        put(SPORTS_SKIING_RESPONSE_VALUE, "Skiing");
        put(SPORTS_SOCCER_RESPONSE_VALUE, "Soccer");
        put(SPORTS_SQUASH_RESPONSE_VALUE, "Squash");
        put(SPORTS_SURFING_RESPONSE_VALUE, "Surfing");
        put(SPORTS_SWIMMING_RESPONSE_VALUE, "Swimming");
        put(SPORTS_TENNIS_RESPONSE_VALUE, "Tennis");
        put(SPORTS_TRACK_RESPONSE_VALUE, "Track");
        put(SPORTS_ULTIMATE_RESPONSE_VALUE, "Ultimate Frisbee");
        put(SPORTS_VOLLEYBALL_RESPONSE_VALUE, "Volleyball");
        put(SPORTS_WATER_POLO_RESPONSE_VALUE, "Water polo");
        put(SPORTS_WRESTLING_RESPONSE_VALUE, "Wrestling");

        /**
         * Girls only sports
         */
        put(GIRLS_SPORTS_FIELD_HOCKEY_RESPONSE_VALUE, "Field hockey");
        put(GIRLS_SPORTS_SOFTBALL_RESPONSE_VALUE, "Softball");

        /**
         * Boys only sports
         */
        put(BOYS_SPORTS_BASEBALL_RESPONSE_VALUE, "Baseball");
        put(BOYS_SPORTS_FOOTBALL_RESPONSE_VALUE, "Football");

        /**
         * Staff
         */
        put(STAFF_ART_TEACHER_RESPONSE_VALUE, "Art teacher");
        put(STAFF_ASSISTANT_PRINCIPAL_RESPONSE_VALUE, "Assistant principal");
        put(STAFF_COLLEGE_COUNSELOR_RESPONSE_VALUE, "College counselor");
        put(STAFF_COMP_SPECIALIST_RESPONSE_VALUE, "Computer specialist");
        put(STAFF_COOKING_TEACHER_RESPONSE_VALUE, "Cooking / Nutrition teacher");
        put(STAFF_DANCE_TEACHER_RESPONSE_VALUE, "Dance teacher");
        put(STAFF_ELL_ESL_RESPONSE_VALUE, "ELL / ESL coordinator");
        put(STAFF_GARDEN_TEACHER_RESPONSE_VALUE, "Gardening teacher");
        put(STAFF_GIFTED_SPECIALIST_RESPONSE_VALUE, "Gifted specialist");
        put(STAFF_INSTRUCTIONAL_AID_RESPONSE_VALUE, "Instructional aide / coach");
        put(STAFF_LIBRARIAN_RESPONSE_VALUE, "Librarian / media specialist");
        put(STAFF_MATH_SPECIALIST_RESPONSE_VALUE, "Math specialist");
        put(STAFF_MUSIC_TEACHER_RESPONSE_VALUE, "Music teacher");
        put(STAFF_NURSE_RESPONSE_VALUE, "Nurse");
        put(STAFF_PE_INSTRUCTOR_RESPONSE_VALUE, "PE instructor");
        put(STAFF_POETRY_TEACHER_RESPONSE_VALUE, "Poetry / Creative writing teacher");
        put(STAFF_PRIEST_RESPONSE_VALUE, "Priest, pastor, or other religious personnel");
        put(STAFF_READING_SPECIALIST_RESPONSE_VALUE, "Reading specialist");
        put(STAFF_ROBOTICS_TEACHER_RESPONSE_VALUE, "Robotics / Technology specialist");
        put(STAFF_SCHOOL_PSYCHOLOGIST_RESPONSE_VALUE, "School psychologist");
        put(STAFF_SCHOOL_COUNSELOR_RESPONSE_VALUE, "School social worker / counselors");
        put(STAFF_SECURITY_RESPONSE_VALUE, "Security personnel");
        put(STAFF_SPECIAL_ED_COORD_RESPONSE_VALUE, "Special education coordinator");
        put(STAFF_SPEECH_THERAPIST_RESPONSE_VALUE, "Speech and language therapist");
        put(STAFF_TEACHER_AID_RESPONSE_VALUE, "Teacher aid / Assistant teacher");
        put(STAFF_TUTOR_RESPONSE_VALUE, "Tutor");
    }};

    /**
     * Map form field section name to form field label
     */
    public static final Map<String, String> FORM_FIELD_TITLES = new LinkedHashMap<String, String>(){{
        put(ARTS_MUSIC_PARAM, ARTS_MUSIC_TITLE);
        put(EXTENDED_CARE_PARAM, EXTENDED_CARE_TITLE);
        put(GIRLS_SPORTS_PARAM, GIRLS_SPORTS_TITLE);
        put(STAFF_PARAM, STAFF_TITLE);
        put(FACILITIES_PARAM, FACILITIES_TITLE);
        put(FOREIGN_LANGUAGES_PARAM, FOREIGN_LANGUAGES_TITLE);
        put(TRANSPORTATION_PARAM, TRANSPORTATION_TITLE);
        put(PARENT_INVOLVEMENT_PARAM, PARENT_INVOLVEMENT_TITLE);
        put(BOYS_SPORTS_PARAM, BOYS_SPORTS_TITLE);
    }};
}
