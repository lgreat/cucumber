package gs.web.content;

import junit.framework.TestCase;

/**
 * Created by IntelliJ IDEA.
 * User: rramachandran
 * Date: 1/17/12
 * Time: 3:20 PM
 */
public class BlogFeedContentUtilTest extends TestCase {

    public void testFetchAuthorImageSource() throws Exception {
        assertEquals("/catalog/images/blog/greatschools_40x40.png", BlogFeedContentUtil.generateAuthorImageURL("GreatSchools"));
        assertEquals("/catalog/images/blog/billjackson_40x40.png", BlogFeedContentUtil.generateAuthorImageURL("Bill Jackson"));
        assertEquals("/catalog/images/blog/kelseyparker_40x40.png", BlogFeedContentUtil.generateAuthorImageURL("Kelsey Parker"));
        assertEquals("/catalog/images/blog/davesteer_40x40.png", BlogFeedContentUtil.generateAuthorImageURL("Dave Steer"));
        assertEquals("/catalog/images/blog/jimdaly_40x40.png", BlogFeedContentUtil.generateAuthorImageURL("Jim Daly"));
        assertEquals("/catalog/images/blog/chasenelson_40x40.png", BlogFeedContentUtil.generateAuthorImageURL("Chase Nelson"));
        assertEquals("/catalog/images/blog/clareellis_40x40.png", BlogFeedContentUtil.generateAuthorImageURL("Clare Ellis"));
        assertEquals("/catalog/images/blog/carollloyd_40x40.png", BlogFeedContentUtil.generateAuthorImageURL("Carol Lloyd"));
        assertEquals("/catalog/images/blog/karinakinik_40x40.png", BlogFeedContentUtil.generateAuthorImageURL("Karina Kinik"));
        assertEquals("/catalog/images/blog/lesliecrawford_40x40.png", BlogFeedContentUtil.generateAuthorImageURL("Leslie Crawford"));
        assertEquals("/catalog/images/blog/patticonstantakis_40x40.png", BlogFeedContentUtil.generateAuthorImageURL("Patti Constantakis"));
        assertEquals("/catalog/images/blog/ryanclark_40x40.png", BlogFeedContentUtil.generateAuthorImageURL("Ryan Clark"));

        assertEquals("/catalog/images/blog/namewithapostrophe_40x40.png", BlogFeedContentUtil.generateAuthorImageURL("Name With'Apostrophe"));

        assertEquals("/res/img/pixel.gif", BlogFeedContentUtil.generateAuthorImageURL(null));
        assertEquals("/res/img/pixel.gif", BlogFeedContentUtil.generateAuthorImageURL(""));
    }
}
