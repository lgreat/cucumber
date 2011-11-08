package gs.web.i18n;


import gs.web.util.UrlUtil;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

public class LanguageToggleHelper {

    public static String LANGUAGE_ABBREVIATION_PARAM = "language";
    public static String MODEL_ALTERNATE_LANGUAGE_URL = "alternateLanguageUrl";

    /**
     * Inspects the request and looks for current language. Adds a URL that points to the alternate language to the model
     * @param request
     * @param model
     * @return the current Language
     */
    public static Language handleLanguageToggle(HttpServletRequest request, Map<String, Object> model) {
        Language language = Language.EN; //default to english
        String languageAbbreviation = request.getParameter(LANGUAGE_ABBREVIATION_PARAM);
        if (Language.ES.name().equalsIgnoreCase(languageAbbreviation)) {
            language = Language.ES;
        }

        String queryString = request.getQueryString();
        //toggle current language, overwrite language param if it exists, create url
        String alternateLanguageQueryString = UrlUtil.putQueryParamIntoQueryString(queryString, LANGUAGE_ABBREVIATION_PARAM, toggleLanguage(language).name());
        String alternateLanguageUrl = request.getRequestURI().toString() + "?" + alternateLanguageQueryString;
        model.put(MODEL_ALTERNATE_LANGUAGE_URL, alternateLanguageUrl);

        return language;
    }

    public static enum Language {
        EN,
        ES
    }

    public static Language toggleLanguage(Language language) {
        if (Language.EN == language) {
            return Language.ES;
        } else {
            return Language.EN;
        }
    }
}
