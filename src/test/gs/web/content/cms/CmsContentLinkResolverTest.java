package gs.web.content.cms;

import gs.web.BaseControllerTestCase;
import gs.data.content.cms.CmsContent;
import gs.data.content.cms.CmsEmbeddedLinks;
import gs.data.content.cms.CmsLink;

import java.util.List;
import java.util.ArrayList;

public class CmsContentLinkResolverTest extends BaseControllerTestCase {
    public void testReplaceAnnotetedProperties() throws Exception {
        class CmsTestContent extends CmsContent {
            private String replaceMe;
            private String leaveMe;

            @CmsEmbeddedLinks
            public String getReplaceMe() {
                return replaceMe;
            }

            public void setReplaceMe(String replaceMe) {
                this.replaceMe = replaceMe;
            }

            public String getLeaveMe() {
                return leaveMe;
            }

            public void setLeaveMe(String leaveMe) {
                this.leaveMe = leaveMe;
            }
        }

        CmsTestContent testContent = new CmsTestContent();
        testContent.setReplaceMe("gs://schoolProfile?state=CA&id=1");
        testContent.setLeaveMe("gs://schoolProfile?state=CA&id=1");

        new CmsContentLinkResolver().replaceEmbeddedLinks(testContent);

        assertEquals("/california/alameda/1-Alameda-High-School/", testContent.getReplaceMe());
        assertEquals("gs://schoolProfile?state=CA&id=1", testContent.getLeaveMe());

    }

    public void testReplaceEmbeddedLink() throws Exception {
        class CmsTestContent extends CmsContent {
            private CmsLink replaceMe;
            private CmsLink leaveMe;

            @CmsEmbeddedLinks
            public CmsLink getReplaceMe() {
                return replaceMe;
            }

            public void setReplaceMe(CmsLink replaceMe) {
                this.replaceMe = replaceMe;
            }

            public CmsLink getLeaveMe() {
                return leaveMe;
            }

            public void setLeaveMe(CmsLink leaveMe) {
                this.leaveMe = leaveMe;
            }
        }

        CmsTestContent testContent = new CmsTestContent();

        CmsLink link = new CmsLink();
        link.setUrl("gs://schoolProfile?state=CA&id=1");
        testContent.setReplaceMe(link);

        link = new CmsLink();
        link.setUrl("gs://schoolProfile?state=CA&id=1");
        testContent.setLeaveMe(link);

        new CmsContentLinkResolver().replaceEmbeddedLinks(testContent);
        assertEquals("/california/alameda/1-Alameda-High-School/", testContent.getReplaceMe().getUrl());
        assertEquals("gs://schoolProfile?state=CA&id=1", testContent.getLeaveMe().getUrl());
    }

    public void testReplaceEmbeddedLinkHandle() throws Exception {
        class CmsTestContent extends CmsContent {
            private CmsLink empty;

            @CmsEmbeddedLinks
            public CmsLink getEmpty() {
                return empty;
            }

            public void setEmpty(CmsLink empty) {
                this.empty = empty;
            }
        }

        CmsTestContent testContent = new CmsTestContent();

        new CmsContentLinkResolver().replaceEmbeddedLinks(testContent);
        // Shouldn't blow up.
    }

    public void testReplaceAnnotetedPropertiesHandlesCmsLinkCollections() throws Exception {
        class CmsTestContent extends CmsContent {
            private List<CmsLink> replaceMe;
            private List<CmsLink> leaveMe;

            @CmsEmbeddedLinks
            public List<CmsLink> getReplaceMe() {
                return replaceMe;
            }

            public void setReplaceMe(List<CmsLink> replaceMe) {
                this.replaceMe = replaceMe;
            }

            public List<CmsLink> getLeaveMe() {
                return leaveMe;
            }

            public void setLeaveMe(List<CmsLink> leaveMe) {
                this.leaveMe = leaveMe;
            }
        }
        CmsTestContent testContent = new CmsTestContent();
        CmsLink link = new CmsLink();
        link.setUrl("gs://schoolProfile?state=CA&id=1");
        List<CmsLink> links = new ArrayList<CmsLink>();
        links.add(link);
        testContent.setReplaceMe(links);

        link = new CmsLink();
        link.setUrl("gs://schoolProfile?state=CA&id=1");
        links = new ArrayList<CmsLink>();
        links.add(link);
        testContent.setLeaveMe(links);

        new CmsContentLinkResolver().replaceEmbeddedLinks(testContent);

        assertEquals("/california/alameda/1-Alameda-High-School/", testContent.getReplaceMe().get(0).getUrl());
        assertEquals("gs://schoolProfile?state=CA&id=1", testContent.getLeaveMe().get(0).getUrl());

    }
}
