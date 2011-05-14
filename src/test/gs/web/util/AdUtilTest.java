package gs.web.util;

import junit.framework.TestCase;

public class AdUtilTest extends TestCase {
    public void testGetK12ClickThroughUrl() throws Exception {
        String referrer = "http://www.greatschools.org/";
        String hostname = "www.greatschools.org";
        String topicCenterUri = "online-education-ca";

        // search
        referrer = "http://www.greatschools.org/search/search.page?search_type=0&q=san+francisco&state=CA&c=school";
        assertEquals("http://ww2.k12.com/cm/?affl=gr8t&page=sr&school=CA", AdUtil.getK12ClickThroughUrl(referrer, hostname, topicCenterUri));
        // nearby search
        referrer = "http://www.greatschools.org/search/nearbySearch.page?gradeLevels=e&distance=5&zipCode=94105&redirectUrl=%2F";
        assertEquals("http://ww2.k12.com/cm/?affl=gr8t&page=sr&school=CA", AdUtil.getK12ClickThroughUrl(referrer, hostname, topicCenterUri));
        // city browse
        referrer = "http://www.greatschools.org/california/san-francisco/schools/";
        assertEquals("http://ww2.k12.com/cm/?affl=gr8t&page=sr&school=CA", AdUtil.getK12ClickThroughUrl(referrer, hostname, topicCenterUri));
        referrer = "http://www.greatschools.org/california/san-francisco/elementary-schools/";
        assertEquals("http://ww2.k12.com/cm/?affl=gr8t&page=sr&school=CA", AdUtil.getK12ClickThroughUrl(referrer, hostname, topicCenterUri));
        referrer = "http://www.greatschools.org/california/san-francisco/middle-schools/";
        assertEquals("http://ww2.k12.com/cm/?affl=gr8t&page=sr&school=CA", AdUtil.getK12ClickThroughUrl(referrer, hostname, topicCenterUri));
        referrer = "http://www.greatschools.org/california/san-francisco/high-schools/";
        assertEquals("http://ww2.k12.com/cm/?affl=gr8t&page=sr&school=CA", AdUtil.getK12ClickThroughUrl(referrer, hostname, topicCenterUri));
        referrer = "http://www.greatschools.org/california/san-francisco/preschools/";
        assertEquals("http://ww2.k12.com/cm/?affl=gr8t&page=sr&school=CA", AdUtil.getK12ClickThroughUrl(referrer, hostname, topicCenterUri));
        referrer = "http://www.greatschools.org/california/san-francisco/public/schools/";
        assertEquals("http://ww2.k12.com/cm/?affl=gr8t&page=sr&school=CA", AdUtil.getK12ClickThroughUrl(referrer, hostname, topicCenterUri));
        referrer = "http://www.greatschools.org/california/san-francisco/charter/schools/";
        assertEquals("http://ww2.k12.com/cm/?affl=gr8t&page=sr&school=CA", AdUtil.getK12ClickThroughUrl(referrer, hostname, topicCenterUri));
        referrer = "http://www.greatschools.org/california/san-francisco/private/schools/";
        assertEquals("http://ww2.k12.com/cm/?affl=gr8t&page=sr&school=CA", AdUtil.getK12ClickThroughUrl(referrer, hostname, topicCenterUri));
        referrer = "http://www.greatschools.org/california/san-francisco/public-charter/schools/";
        assertEquals("http://ww2.k12.com/cm/?affl=gr8t&page=sr&school=CA", AdUtil.getK12ClickThroughUrl(referrer, hostname, topicCenterUri));
        referrer = "http://www.greatschools.org/california/san-francisco/public-charter/elementary-schools/";
        assertEquals("http://ww2.k12.com/cm/?affl=gr8t&page=sr&school=CA", AdUtil.getK12ClickThroughUrl(referrer, hostname, topicCenterUri));
        // district browse
        referrer = "http://www.greatschools.org/california/san-francisco/San-Francisco-Unified-School-District/schools/";
        assertEquals("http://ww2.k12.com/cm/?affl=gr8t&page=sr&school=CA", AdUtil.getK12ClickThroughUrl(referrer, hostname, topicCenterUri));
        referrer = "http://www.greatschools.org/california/san-francisco/San-Francisco-Unified-School-District/preschools/";
        assertEquals("http://ww2.k12.com/cm/?affl=gr8t&page=sr&school=CA", AdUtil.getK12ClickThroughUrl(referrer, hostname, topicCenterUri));
        referrer = "http://www.greatschools.org/california/san-francisco/San-Francisco-Unified-School-District/elementary-schools/";
        assertEquals("http://ww2.k12.com/cm/?affl=gr8t&page=sr&school=CA", AdUtil.getK12ClickThroughUrl(referrer, hostname, topicCenterUri));
        referrer = "http://www.greatschools.org/california/san-francisco/San-Francisco-Unified-School-District/middle-schools/";
        assertEquals("http://ww2.k12.com/cm/?affl=gr8t&page=sr&school=CA", AdUtil.getK12ClickThroughUrl(referrer, hostname, topicCenterUri));
        referrer = "http://www.greatschools.org/california/san-francisco/San-Francisco-Unified-School-District/high-schools/";
        assertEquals("http://ww2.k12.com/cm/?affl=gr8t&page=sr&school=CA", AdUtil.getK12ClickThroughUrl(referrer, hostname, topicCenterUri));

        // city page (city home)
        referrer = "http://www.greatschools.org/california/san-francisco/";
        assertEquals("http://ww2.k12.com/cm/?affl=gr8t&page=cp&school=CA", AdUtil.getK12ClickThroughUrl(referrer, hostname, topicCenterUri));
        referrer = "http://www.greatschools.org/california/san-francisco/asdf";
        assertEquals("http://ww2.k12.com/cm/?affl=gr8t&page=other&school=CA", AdUtil.getK12ClickThroughUrl(referrer, hostname, topicCenterUri));
        referrer = "http://www.greatschools.org/community/questions/";
        assertEquals("http://ww2.k12.com/cm/?affl=gr8t&page=other&school=CA", AdUtil.getK12ClickThroughUrl(referrer, hostname, topicCenterUri));

        // research & compare
        referrer = "http://www.greatschools.org/california/";
        assertEquals("http://ww2.k12.com/cm/?affl=gr8t&page=rc&school=CA", AdUtil.getK12ClickThroughUrl(referrer, hostname, topicCenterUri));
        referrer = "http://www.greatschools.org/back-to-school/";
        assertEquals("http://ww2.k12.com/cm/?affl=gr8t&page=other&school=CA", AdUtil.getK12ClickThroughUrl(referrer, hostname, topicCenterUri));

        referrer = "http://www.greatschools.org/search/search.page?search_type=0&q=san+francisco&state=CA&c=school";

        // topic center uri
        topicCenterUri = "online-education-int";
        assertEquals("http://ww2.k12.com/cm/?affl=gr8t&page=sr&school=INT", AdUtil.getK12ClickThroughUrl(referrer, hostname, topicCenterUri));
        topicCenterUri = "online-education-or";
        assertEquals("http://ww2.k12.com/cm/?affl=gr8t&page=sr&school=OR", AdUtil.getK12ClickThroughUrl(referrer, hostname, topicCenterUri));
        topicCenterUri = "anything-that-does-not-match-k12-topic-center-uri-pattern";
        assertEquals("http://ww2.k12.com/cm/?affl=gr8t&page=sr&school=INT", AdUtil.getK12ClickThroughUrl(referrer, hostname, topicCenterUri));
        topicCenterUri = "online-education-abcd";
        assertEquals("http://ww2.k12.com/cm/?affl=gr8t&page=sr&school=INT", AdUtil.getK12ClickThroughUrl(referrer, hostname, topicCenterUri));
        topicCenterUri = "online-education-123";
        assertEquals("http://ww2.k12.com/cm/?affl=gr8t&page=sr&school=INT", AdUtil.getK12ClickThroughUrl(referrer, hostname, topicCenterUri));
        topicCenterUri = "online-education-abc";
        assertEquals("http://ww2.k12.com/cm/?affl=gr8t&page=sr&school=INT", AdUtil.getK12ClickThroughUrl(referrer, hostname, topicCenterUri));

        // referred by this/another site
        referrer = "http://www.google.com/search/search.page?search_type=0&q=san+francisco&state=CA&c=school";
        hostname = "www.greatschools.org";
        topicCenterUri = "online-education-ca";
        assertEquals("http://ww2.k12.com/cm/?affl=gr8t&page=other&school=CA", AdUtil.getK12ClickThroughUrl(referrer, hostname, topicCenterUri));
        referrer = "http://localhost:8080/search/search.page?search_type=0&q=san+francisco&state=CA&c=school";
        hostname = "localhost";
        assertEquals("http://ww2.k12.com/cm/?affl=gr8t&page=sr&school=CA", AdUtil.getK12ClickThroughUrl(referrer, hostname, topicCenterUri));
        referrer = "http://schoolrankings.nj.com/search/search.page?search_type=0&q=san+francisco&state=CA&c=school";
        hostname = "schoolrankings.nj.com";
        assertEquals("http://ww2.k12.com/cm/?affl=gr8t&page=sr&school=CA", AdUtil.getK12ClickThroughUrl(referrer, hostname, topicCenterUri));
        referrer = "http://dev.greatschools.org/search/search.page?search_type=0&q=san+francisco&state=CA&c=school";
        hostname = "dev.greatschools.org";
        assertEquals("http://ww2.k12.com/cm/?affl=gr8t&page=sr&school=CA", AdUtil.getK12ClickThroughUrl(referrer, hostname, topicCenterUri));
        referrer = "http://qa.greatschools.org/search/search.page?search_type=0&q=san+francisco&state=CA&c=school";
        hostname = "qa.greatschools.org";
        assertEquals("http://ww2.k12.com/cm/?affl=gr8t&page=sr&school=CA", AdUtil.getK12ClickThroughUrl(referrer, hostname, topicCenterUri));
        referrer = "http://www.greatschools.org/search/search.page?search_type=0&q=san+francisco&state=CA&c=school";
        hostname = "qa.greatschools.org";
        assertEquals("http://ww2.k12.com/cm/?affl=gr8t&page=other&school=CA", AdUtil.getK12ClickThroughUrl(referrer, hostname, topicCenterUri));

        topicCenterUri = "online-education-ca";

        // no referrer
        referrer = null;
        hostname = "www.greatschools.org";
        assertEquals("http://ww2.k12.com/cm/?affl=gr8t&page=other&school=CA", AdUtil.getK12ClickThroughUrl(referrer, hostname, topicCenterUri));
        referrer = "";
        assertEquals("http://ww2.k12.com/cm/?affl=gr8t&page=other&school=CA", AdUtil.getK12ClickThroughUrl(referrer, hostname, topicCenterUri));

        // no hostname
        referrer = "http://www.greatschools.org/search/search.page?search_type=0&q=san+francisco&state=CA&c=school";
        hostname = null;
        assertEquals("http://ww2.k12.com/cm/?affl=gr8t&page=other&school=CA", AdUtil.getK12ClickThroughUrl(referrer, hostname, topicCenterUri));
        hostname = "";
        assertEquals("http://ww2.k12.com/cm/?affl=gr8t&page=other&school=CA", AdUtil.getK12ClickThroughUrl(referrer, hostname, topicCenterUri));

        // no topic center uri
        referrer = "http://www.greatschools.org/search/search.page?search_type=0&q=san+francisco&state=CA&c=school";
        hostname = "www.greatschools.org";
        topicCenterUri = "students";
        assertEquals("http://ww2.k12.com/cm/?affl=gr8t&page=sr&school=INT", AdUtil.getK12ClickThroughUrl(referrer, hostname, topicCenterUri));
        topicCenterUri = null;
        assertEquals("http://ww2.k12.com/cm/?affl=gr8t&page=sr&school=INT", AdUtil.getK12ClickThroughUrl(referrer, hostname, topicCenterUri));
        topicCenterUri = "";
        assertEquals("http://ww2.k12.com/cm/?affl=gr8t&page=sr&school=INT", AdUtil.getK12ClickThroughUrl(referrer, hostname, topicCenterUri));

    }
}
