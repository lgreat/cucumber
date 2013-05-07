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

    /**
     * Param names for usp form field section names
     */
    public static final String ARTS_MUSIC_PARAM = "arts";
    public static final String EXTENDED_CARE_PARAM = "extCare";

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
     * Enum with form field section name and arrays of response keys
     */
    public static enum SectionResponseKeys {
        arts(ARTS_MUSIC_PARAM, new String[]{ARTS_MEDIA_RESPONSE_KEY, ARTS_MUSIC_RESPONSE_KEY, ARTS_PERFORMING_WRITTEN_RESPONSE_KEY,
                ARTS_VISUAL_RESPONSE_KEY}),
        extCare(EXTENDED_CARE_PARAM, new String[]{EXTENDED_CARE_RESPONSE_KEY});

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
     * Initialize static multimap that maps a response key to multiple response values.
     * For each form field section, add the response values for all response keys
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
                                ARTS_MEDIA_TECH_DESIGN_RESPONSE_VALUE, ARTS_MEDIA_VIDEO_RESPONSE_VALUE
                        });
                    }
                    else if(ARTS_MUSIC_RESPONSE_KEY.equals(responseKey)) {
                        addKeyValueToSection(ARTS_MUSIC_RESPONSE_KEY, new String[]{
                                ARTS_MUSIC_BAND_RESPONSE_VALUE, ARTS_MUSIC_BELLS_RESPONSE_VALUE,
                                ARTS_MUSIC_CHAMBER_RESPONSE_VALUE, ARTS_MUSIC_CHORUS_RESPONSE_VALUE,
                                ARTS_MUSIC_LESSONS_RESPONSE_VALUE, ARTS_MUSIC_JAZZ_RESPONSE_VALUE,
                                ARTS_MUSIC_OPERA_RESPONSE_VALUE, ARTS_MUSIC_ORCHESTRA_RESPONSE_VALUE,
                                ARTS_MUSIC_ROCK_RESPONSE_VALUE, ARTS_MUSIC_THEORY_RESPONSE_VALUE,
                                ARTS_MUSIC_VOICE_RESPONSE_VALUE
                        });
                    }
                    else if(ARTS_PERFORMING_WRITTEN_RESPONSE_KEY.equals(responseKey)) {
                        addKeyValueToSection(ARTS_PERFORMING_WRITTEN_RESPONSE_KEY, new String[]{
                                ARTS_PERFORMING_DANCE_RESPONSE_VALUE, ARTS_PERFORMING_DRAMA_RESPONSE_VALUE,
                                ARTS_PERFORMING_IMPROV_RESPONSE_VALUE, ARTS_PERFORMING_CREATIVE_WRITING_RESPONSE_VALUE,
                                ARTS_PERFORMING_POETRY_RESPONSE_VALUE
                        });
                    }
                    else if(ARTS_VISUAL_RESPONSE_KEY.equals(responseKey)) {
                        addKeyValueToSection(ARTS_VISUAL_RESPONSE_KEY, new String[]{
                                ARTS_VISUAL_ARCH_RESPONSE_VALUE, ARTS_VISUAL_CERAMICS_RESPONSE_VALUE,
                                ARTS_VISUAL_DESIGN_RESPONSE_VALUE, ARTS_VISUAL_DRAWING_RESPONSE_VALUE,
                                ARTS_VISUAL_PAINTING_RESPONSE_VALUE, ARTS_VISUAL_PHOTO_RESPONSE_VALUE,
                                ARTS_VISUAL_PRINT_RESPONSE_VALUE, ARTS_VISUAL_SCULPTURE_RESPONSE_VALUE,
                                ARTS_VISUAL_TEXTILES_RESPONSE_VALUE
                        });
                    }
                }

                else if(EXTENDED_CARE_PARAM.equals(sectionTitle)) {
                    if(EXTENDED_CARE_RESPONSE_KEY.equals(responseKey)) {
                        addKeyValueToSection(EXTENDED_CARE_RESPONSE_KEY, new String[]{
                                EXTENDED_CARE_BEFORE_RESPONSE_VALUE, EXTENDED_CARE_AFTER_RESPONSE_VALUE
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
    }};

    /**
     * Map form field section name to form field label
     */
    public static final Map<String, String> FORM_FIELD_TITLES = new LinkedHashMap<String, String>(){{
        put(ARTS_MUSIC_PARAM, ARTS_MUSIC_TITLE);
        put(EXTENDED_CARE_PARAM, EXTENDED_CARE_TITLE);
    }};
}
