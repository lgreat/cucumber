package gs.web.content.cms;

import gs.web.GsMockHttpServletRequest;
import junit.framework.TestCase;

import java.util.HashMap;
import java.util.Map;

public class WorksheetGalleryControllerTest extends TestCase {
    public void testAddMetaDataToModel() throws Exception {
        Map<String, Object> model = new HashMap<String, Object>();

        model.clear();
        WorksheetGalleryController.addMetaDataToModel(model);
        assertNull(model.get(WorksheetGalleryController.MODEL_META_DESCRIPTION));
        assertNull(model.get(WorksheetGalleryController.MODEL_META_KEYWORDS));

        model.clear();
        model.put(WorksheetGalleryController.REQUESTED_SUBJECT_KEY, "math");
        WorksheetGalleryController.addMetaDataToModel(model);
        assertEquals("Free math worksheets you can print and use with children in preschool through fifth grade.", model.get(WorksheetGalleryController.MODEL_META_DESCRIPTION));
        assertEquals("math worksheets, math worksheet, mathematics worksheet, maths worksheet, worksheets for math", model.get(WorksheetGalleryController.MODEL_META_KEYWORDS));

        model.clear();
        model.put(WorksheetGalleryController.REQUESTED_SUBJECT_KEY, "reading");
        WorksheetGalleryController.addMetaDataToModel(model);
        assertEquals("Free reading worksheets you can print and use with children in preschool through fifth grade.", model.get(WorksheetGalleryController.MODEL_META_DESCRIPTION));
        assertEquals("reading worksheets, reading comprehension worksheets, language worksheets, nouns worksheets, grammar worksheets", model.get(WorksheetGalleryController.MODEL_META_KEYWORDS));

        model.clear();
        model.put(WorksheetGalleryController.REQUESTED_SUBJECT_KEY, "writing");
        WorksheetGalleryController.addMetaDataToModel(model);
        assertEquals("Free writing worksheets you can print and use with children in preschool through fifth grade.", model.get(WorksheetGalleryController.MODEL_META_DESCRIPTION));
        assertEquals("writing worksheets, writing worksheet", model.get(WorksheetGalleryController.MODEL_META_KEYWORDS));

        model.clear();
        model.put(WorksheetGalleryController.REQUESTED_GRADE_KEY, "preschool");
        WorksheetGalleryController.addMetaDataToModel(model);
        assertEquals("Free printable preschool worksheets to help your child develop early math, reading, and writing skills.", model.get(WorksheetGalleryController.MODEL_META_DESCRIPTION));
        assertEquals("preschool worksheets, preschool worksheet, pre school worksheets, worksheets for preschool kids", model.get(WorksheetGalleryController.MODEL_META_KEYWORDS));

        model.clear();
        model.put(WorksheetGalleryController.REQUESTED_GRADE_KEY, "kindergarten");
        WorksheetGalleryController.addMetaDataToModel(model);
        assertEquals("Free printable kindergarten worksheets to help your whiz kid practice math, reading, and writing skills.", model.get(WorksheetGalleryController.MODEL_META_DESCRIPTION));
        assertEquals("worksheet for kindergarten, kindergarten worksheets, worksheets for kindergarten, kindergarten worksheet", model.get(WorksheetGalleryController.MODEL_META_KEYWORDS));

        // special case
        model.clear();
        model.put(WorksheetGalleryController.REQUESTED_GRADE_KEY, "first-grade");
        WorksheetGalleryController.addMetaDataToModel(model);
        assertEquals("Free printable 1st grade worksheets to help your whiz kid practice math, reading, and writing skills.", model.get(WorksheetGalleryController.MODEL_META_DESCRIPTION));
        assertEquals("worksheets for 1st graders, 1 grade worksheets, first grade worksheets, worksheets for first grade, 1st grade worksheets", model.get(WorksheetGalleryController.MODEL_META_KEYWORDS));

        model.clear();
        model.put(WorksheetGalleryController.REQUESTED_GRADE_KEY, "third-grade");
        WorksheetGalleryController.addMetaDataToModel(model);
        assertEquals("Free printable 3rd grade worksheets to help your whiz kid practice math, reading, and writing skills.", model.get(WorksheetGalleryController.MODEL_META_DESCRIPTION));
        assertEquals("worksheets for 3rd graders, 3rd grade worksheets, third grade worksheets, worksheets for third grade", model.get(WorksheetGalleryController.MODEL_META_KEYWORDS));

        model.clear();
        model.put(WorksheetGalleryController.REQUESTED_GRADE_KEY, "preschool");
        model.put(WorksheetGalleryController.REQUESTED_SUBJECT_KEY, "math");
        WorksheetGalleryController.addMetaDataToModel(model);
        assertEquals("Free printable preschool math worksheets to help your child practice numbers, counting, and addition.", model.get(WorksheetGalleryController.MODEL_META_DESCRIPTION));
        assertEquals("preschool math worksheets", model.get(WorksheetGalleryController.MODEL_META_KEYWORDS));

        model.clear();
        model.put(WorksheetGalleryController.REQUESTED_GRADE_KEY, "preschool");
        model.put(WorksheetGalleryController.REQUESTED_SUBJECT_KEY, "reading");
        WorksheetGalleryController.addMetaDataToModel(model);
        assertEquals("Free printable preschool reading worksheets to help your child practice the alphabet, letter sounds, and new words.", model.get(WorksheetGalleryController.MODEL_META_DESCRIPTION));
        assertEquals("preschool reading worksheets, alphabet worksheets", model.get(WorksheetGalleryController.MODEL_META_KEYWORDS));

        model.clear();
        model.put(WorksheetGalleryController.REQUESTED_GRADE_KEY, "preschool");
        model.put(WorksheetGalleryController.REQUESTED_SUBJECT_KEY, "writing");
        WorksheetGalleryController.addMetaDataToModel(model);
        assertEquals("Free printable preschool writing worksheets to help your child practice tracing and coloring shapes and letters.", model.get(WorksheetGalleryController.MODEL_META_DESCRIPTION));
        assertEquals("preschool writing worksheets, coloring worksheets", model.get(WorksheetGalleryController.MODEL_META_KEYWORDS));

        model.clear();
        model.put(WorksheetGalleryController.REQUESTED_GRADE_KEY, "kindergarten");
        model.put(WorksheetGalleryController.REQUESTED_SUBJECT_KEY, "math");
        WorksheetGalleryController.addMetaDataToModel(model);
        assertEquals("Free printable kindergarten math worksheets to help your child practice addition, matching, and identifying shapes and patterns.", model.get(WorksheetGalleryController.MODEL_META_DESCRIPTION));
        assertEquals("kindergarten math worksheets", model.get(WorksheetGalleryController.MODEL_META_KEYWORDS));

        model.clear();
        model.put(WorksheetGalleryController.REQUESTED_GRADE_KEY, "kindergarten");
        model.put(WorksheetGalleryController.REQUESTED_SUBJECT_KEY, "reading");
        WorksheetGalleryController.addMetaDataToModel(model);
        assertEquals("Free printable kindergarten reading worksheets to help your child with spelling, language, grammar, and more.", model.get(WorksheetGalleryController.MODEL_META_DESCRIPTION));
        assertEquals("kindergarten reading worksheets, grammar worksheets", model.get(WorksheetGalleryController.MODEL_META_KEYWORDS));

        model.clear();
        model.put(WorksheetGalleryController.REQUESTED_GRADE_KEY, "kindergarten");
        model.put(WorksheetGalleryController.REQUESTED_SUBJECT_KEY, "writing");
        WorksheetGalleryController.addMetaDataToModel(model);
        assertEquals("Free printable kindergarten writing worksheets to help your child practice tracing and writing shapes and letters.", model.get(WorksheetGalleryController.MODEL_META_DESCRIPTION));
        assertEquals("kindergarten writing worksheets", model.get(WorksheetGalleryController.MODEL_META_KEYWORDS));

        model.clear();
        model.put(WorksheetGalleryController.REQUESTED_GRADE_KEY, "elementary-school");
        WorksheetGalleryController.addMetaDataToModel(model);
        assertEquals("Free elementary worksheets for kids in grades K-5; support learning with a supply of fun printables.", model.get(WorksheetGalleryController.MODEL_META_DESCRIPTION));
        assertEquals("elementary worksheets, worksheets for kids", model.get(WorksheetGalleryController.MODEL_META_KEYWORDS));

        model.clear();
        model.put(WorksheetGalleryController.REQUESTED_GRADE_KEY, "second-grade");
        model.put(WorksheetGalleryController.REQUESTED_SUBJECT_KEY, "math");
        WorksheetGalleryController.addMetaDataToModel(model);
        assertEquals("Free printable 2nd grade math worksheets to help your child practice skills while having fun.", model.get(WorksheetGalleryController.MODEL_META_DESCRIPTION));
        assertEquals("2nd grade math worksheets, second grade math worksheets", model.get(WorksheetGalleryController.MODEL_META_KEYWORDS));

        model.clear();
        model.put(WorksheetGalleryController.REQUESTED_GRADE_KEY, "third-grade");
        model.put(WorksheetGalleryController.REQUESTED_SUBJECT_KEY, "reading");
        WorksheetGalleryController.addMetaDataToModel(model);
        assertEquals("Free printable 3rd grade reading worksheets to help your child practice skills while having fun.", model.get(WorksheetGalleryController.MODEL_META_DESCRIPTION));
        assertEquals("3rd grade reading worksheets, third grade reading worksheets", model.get(WorksheetGalleryController.MODEL_META_KEYWORDS));

        // special case
        model.clear();
        model.put(WorksheetGalleryController.REQUESTED_GRADE_KEY, "fifth-grade");
        model.put(WorksheetGalleryController.REQUESTED_SUBJECT_KEY, "writing");
        WorksheetGalleryController.addMetaDataToModel(model);
        assertEquals("Free printable fifth grade writing worksheets to help your child practice skills while having fun.", model.get(WorksheetGalleryController.MODEL_META_DESCRIPTION));
        assertEquals("5th grade writing worksheets, fifth grade writing worksheets", model.get(WorksheetGalleryController.MODEL_META_KEYWORDS));

        // special case
        model.clear();
        model.put(WorksheetGalleryController.REQUESTED_GRADE_KEY, "first-grade");
        model.put(WorksheetGalleryController.REQUESTED_SUBJECT_KEY, "math");
        WorksheetGalleryController.addMetaDataToModel(model);
        assertEquals("Free printable 1st grade math worksheets to help your child practice key number skills.", model.get(WorksheetGalleryController.MODEL_META_DESCRIPTION));
        assertEquals("1st grade math worksheets, first grade math worksheets", model.get(WorksheetGalleryController.MODEL_META_KEYWORDS));

        // special case
        model.clear();
        model.put(WorksheetGalleryController.REQUESTED_GRADE_KEY, "first-grade");
        model.put(WorksheetGalleryController.REQUESTED_SUBJECT_KEY, "reading");
        WorksheetGalleryController.addMetaDataToModel(model);
        assertEquals("Free printable 1st grade reading worksheets to help your child practice key language arts skills.", model.get(WorksheetGalleryController.MODEL_META_DESCRIPTION));
        assertEquals("1st grade reading worksheets, first grade reading worksheets", model.get(WorksheetGalleryController.MODEL_META_KEYWORDS));

        model.clear();
        model.put(WorksheetGalleryController.REQUESTED_GRADE_KEY, "elementary-school");
        model.put(WorksheetGalleryController.REQUESTED_SUBJECT_KEY, "math");
        WorksheetGalleryController.addMetaDataToModel(model);
        assertNull(model.get(WorksheetGalleryController.MODEL_META_DESCRIPTION));
        assertNull(model.get(WorksheetGalleryController.MODEL_META_KEYWORDS));

        model.clear();
        model.put(WorksheetGalleryController.REQUESTED_GRADE_KEY, "elementary-school");
        model.put(WorksheetGalleryController.REQUESTED_SUBJECT_KEY, "reading");
        WorksheetGalleryController.addMetaDataToModel(model);
        assertNull(model.get(WorksheetGalleryController.MODEL_META_DESCRIPTION));
        assertNull(model.get(WorksheetGalleryController.MODEL_META_KEYWORDS));

        model.clear();
        model.put(WorksheetGalleryController.REQUESTED_GRADE_KEY, "elementary-school");
        model.put(WorksheetGalleryController.REQUESTED_SUBJECT_KEY, "writing");
        WorksheetGalleryController.addMetaDataToModel(model);
        assertNull(model.get(WorksheetGalleryController.MODEL_META_DESCRIPTION));
        assertNull(model.get(WorksheetGalleryController.MODEL_META_KEYWORDS));
    }

    public void testCanonicalUrl() {
        GsMockHttpServletRequest request = new GsMockHttpServletRequest();
        request.setQueryString("decorator=minimalDecorator");
        request.setRequestURI("/worksheets/");

        WorksheetGalleryController controller = new WorksheetGalleryController();
        String result = controller.getCanonicalUrl(request);
        assertEquals("http://localhost/worksheets/", result);
    }
}
