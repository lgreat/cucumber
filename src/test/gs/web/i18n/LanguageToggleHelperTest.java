package gs.web.i18n;

import gs.web.GsMockHttpServletRequest;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class LanguageToggleHelperTest {

    @Test
    public void testHandleLanguageToggle() throws Exception {
        GsMockHttpServletRequest request = new GsMockHttpServletRequest();
        request.setParameter(LanguageToggleHelper.LANGUAGE_ABBREVIATION_PARAM, "EN");
        request.setRequestURI("http://localhost/blah");

        Map<String,Object> model = new HashMap<String,Object>();

        LanguageToggleHelper helper = new LanguageToggleHelper(request, model);
        LanguageToggleHelper.Language language = helper.getCurrentLanguage();
        helper.addDataToModel();

        assertEquals(LanguageToggleHelper.Language.EN, language);
        assertTrue(model.containsKey(LanguageToggleHelper.MODEL_ALTERNATE_LANGUAGE_URL));
        assertEquals("http://localhost/blah?language=ES", model.get(LanguageToggleHelper.MODEL_ALTERNATE_LANGUAGE_URL));
    }

    @Test
    public void testHandleLanguageToggleSpanish() throws Exception {
        GsMockHttpServletRequest request = new GsMockHttpServletRequest();
        request.setParameter(LanguageToggleHelper.LANGUAGE_ABBREVIATION_PARAM, "ES");
        request.setRequestURI("http://localhost/blah");

        Map<String,Object> model = new HashMap<String,Object>();

        LanguageToggleHelper helper = new LanguageToggleHelper(request, model);
        LanguageToggleHelper.Language language = helper.getCurrentLanguage();
        helper.addDataToModel();

        assertEquals(LanguageToggleHelper.Language.ES, language);
        assertTrue(model.containsKey(LanguageToggleHelper.MODEL_ALTERNATE_LANGUAGE_URL));
        assertEquals("http://localhost/blah?language=EN", model.get(LanguageToggleHelper.MODEL_ALTERNATE_LANGUAGE_URL));
    }

    @Test
    public void testHandleLanguageToggleDefaultsToEN() throws Exception {
        GsMockHttpServletRequest request = new GsMockHttpServletRequest();
        request.setRequestURI("http://localhost/blah");
        request.setQueryString("language=EN");

        Map<String,Object> model = new HashMap<String,Object>();

        LanguageToggleHelper helper = new LanguageToggleHelper(request, model);
        LanguageToggleHelper.Language language = helper.getCurrentLanguage();
        helper.addDataToModel();

        assertEquals(LanguageToggleHelper.Language.EN, language);
        assertTrue(model.containsKey(LanguageToggleHelper.MODEL_ALTERNATE_LANGUAGE_URL));
        assertEquals("http://localhost/blah?language=ES", model.get(LanguageToggleHelper.MODEL_ALTERNATE_LANGUAGE_URL));
    }

    @Test
    public void testHandleLanguageToggleHandlesInvalidLanguage() throws Exception {
        GsMockHttpServletRequest request = new GsMockHttpServletRequest();
        request.setParameter(LanguageToggleHelper.LANGUAGE_ABBREVIATION_PARAM, "Arrr");
        request.setRequestURI("http://localhost/blah");

        Map<String,Object> model = new HashMap<String,Object>();

        LanguageToggleHelper helper = new LanguageToggleHelper(request, model);
        LanguageToggleHelper.Language language = helper.getCurrentLanguage();
        helper.addDataToModel();

        assertEquals(LanguageToggleHelper.Language.EN, language);
        assertTrue(model.containsKey(LanguageToggleHelper.MODEL_ALTERNATE_LANGUAGE_URL));
        assertEquals("http://localhost/blah?language=ES", model.get(LanguageToggleHelper.MODEL_ALTERNATE_LANGUAGE_URL));
    }

}
