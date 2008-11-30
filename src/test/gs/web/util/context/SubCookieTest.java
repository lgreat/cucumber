package gs.web.util.context;

import gs.web.BaseTestCase;

import javax.servlet.http.Cookie;
import java.util.Map;
import java.util.HashMap;
import java.util.LinkedHashMap;

/**
 * @author jnorton
 */
public class SubCookieTest extends BaseTestCase {
    private final String cookieName = "Omniture";
    private Map<String, String> propertySetEmpty = null;
    private Map<String, String> propertySetOneValuePair = null;
    private Map<String, String> propertySetManyValuePairs = null;

   private final String manyValuePairs = "This$$:$$And a lot of that.$$/$$That$$:$$Is the true answer$$/$$The Other Thing$$:$$don't forget that.$$/$$Me$$:$$I am";
   private final String oneValuePair =  "This$$:$$And a lot of that.";
   private final String noPairs = "a strin with no pairs";

    public void setUp() throws Exception{
        setUpPropertySetEmpty();
        setUpPropertySetOneValuePair();
        setUpPropertySetManyValuePairs();
    }
    private void setUpPropertySetEmpty(){
        propertySetEmpty = new LinkedHashMap<String, String>();
    }
    private void setUpPropertySetOneValuePair(){
        propertySetOneValuePair = new LinkedHashMap<String, String>();
        propertySetOneValuePair.put("This", "And a lot of that.");
    }

    private void setUpPropertySetManyValuePairs(){
        propertySetManyValuePairs = new LinkedHashMap<String, String>();
        propertySetManyValuePairs.put("This", "And a lot of that.");
        propertySetManyValuePairs.put("That", "Is the true answer");
        propertySetManyValuePairs.put("The Other Thing", "don't forget that.");
        propertySetManyValuePairs.put("Me", "I am");
    }

    public void testEncodeProperties(){
        String result;

        result = SubCookie.encodeProperties(propertySetEmpty);
        assertNotNull("Didn't expect an null", result);
        assertEquals("Expected an empty String", "", result);

        result = SubCookie.encodeProperties(propertySetOneValuePair);
        assertNotNull("Didn't expect an null", result);
        assertEquals(oneValuePair, result);

        result = SubCookie.encodeProperties(propertySetManyValuePairs);
        assertNotNull("Didn't expect an null", result);
        System.out.println(result);
        assertEquals(manyValuePairs, result);
    }

    public void testDecodeProperties(){

        Map<String, String> result;
        Cookie cookie;
        
        cookie = new Cookie("name","");

        String domain = cookie.getDomain();

        validateDecodeResult(cookie, propertySetEmpty) ;

        cookie = new Cookie("name",oneValuePair);
        validateDecodeResult(cookie, propertySetOneValuePair) ;

        cookie = new Cookie("name",manyValuePairs);
        validateDecodeResult(cookie, propertySetManyValuePairs);

        cookie = new Cookie("name", noPairs);
        validateDecodeResult(cookie, propertySetEmpty) ;        
    }

    private void validateDecodeResult(Cookie cookie, Map<String, String> propertySet) {
        Map<String, String> result;
        result = SubCookie.decodeProperties(cookie);
        assertNotNull(result);
        for(String key: propertySet.keySet()) {
            String value = result.get(key);
            assertNotNull(value);
            assertEquals(propertySet.get(key), value);
            result.remove(key);
        }
        assertEquals("Expected all the key value pairs have been removed",0, result.keySet().size());
    }
}
