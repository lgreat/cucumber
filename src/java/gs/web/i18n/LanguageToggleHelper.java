package gs.web.i18n;


import gs.data.pagination.DefaultPaginationConfig;
import gs.web.util.UrlUtil;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

public class LanguageToggleHelper {

    public static final String BEAN_ID = "languageToggleHelper";

    public static String LANGUAGE_ABBREVIATION_PARAM = "language";
    public static String MODEL_ALTERNATE_LANGUAGE_URL = "alternateLanguageUrl";

    private final Language _currentLanguage;
    private final Language _alternateLanguage;
    private final String _alternateLanguageUrl;

    private final Map<String,Object> _model;

    public LanguageToggleHelper(HttpServletRequest request, Map<String,Object> model) {
        _model = model;
        _currentLanguage = getCurrentLanguage(request);
        _alternateLanguage = toggleLanguage(_currentLanguage);
        _alternateLanguageUrl = getAlternateLanguageUrl(request);
    }

    public static Language getCurrentLanguage(HttpServletRequest request) {
        Language language = Language.EN; //default to english
        String languageAbbreviation = request.getParameter(LANGUAGE_ABBREVIATION_PARAM);
        if (Language.ES.name().equalsIgnoreCase(languageAbbreviation)) {
            language = Language.ES;
        }
        return language;
    }

    public String getAlternateLanguageUrl(HttpServletRequest request) {
        String queryString = request.getQueryString();
        //toggle current language, overwrite language param if it exists, create url
        String alternateLanguageQueryString = UrlUtil.putQueryParamIntoQueryString(queryString, LANGUAGE_ABBREVIATION_PARAM, _alternateLanguage.name());
        //removing the offset param from Url here is questionable. Would be better if paging params were standardized accross all uses of pagination
        //currently VideoGalleryController is the only user of LanguageToggleHelper
        alternateLanguageQueryString = UrlUtil.removeParamsFromQueryString(
                alternateLanguageQueryString, DefaultPaginationConfig.DEFAULT_OFFSET_PARAM,
                DefaultPaginationConfig.DEFAULT_PAGE_NUMBER_PARAM
        );
        String alternateLanguageUrl = request.getRequestURI().toString() + "?" + alternateLanguageQueryString;
        return alternateLanguageUrl;
    }

    public void addDataToModel() {
        _model.put(MODEL_ALTERNATE_LANGUAGE_URL, _alternateLanguageUrl);
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

    public Language getCurrentLanguage() {
        return _currentLanguage;
    }

    public Language getAlternateLanguage() {
        return _alternateLanguage;
    }

    public String getAlternateLanguageUrl() {
        return _alternateLanguageUrl;
    }
}
