package gs.web.feed;


import com.sun.syndication.feed.synd.*;
import com.sun.syndication.io.FeedException;
import com.sun.syndication.io.SyndFeedOutput;
import gs.data.content.cms.CmsFeature;
import gs.data.content.cms.ICmsFeatureDao;
import gs.web.util.UrlBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Controller
//@RequestMapping("/services/rss")  // TODO: uncomment to enable page
public class RSSController {

    @Autowired
    protected ICmsFeatureDao _cmsFeatureDao;

    /**
     *
     */
    @RequestMapping(method = RequestMethod.GET)
    public void get(HttpServletRequest request, HttpServletResponse response) throws IOException, FeedException {
        response.setContentType("text/xml");

        CmsFeature article = _cmsFeatureDao.get(25L); // TODO: Figure out what, and how much, content to put in feed

        List<SyndCategory> categories = new ArrayList<SyndCategory>();
        SyndCategory category = new SyndCategoryImpl();
        category.setName("Article");
        categories.add(category);

        SyndFeed feed = new SyndFeedImpl();
        feed.setFeedType("rss_2.0");
        feed.setTitle("GreatSchools Article Feed"); // TODO: get real title from product
        feed.setLink("http://localhost:8080/services/rss.xml"); // TODO: use real hostname
        feed.setDescription("Recent articles published on www.greatschools.org"); // TODO: get real description from product
        feed.setCategories(categories);

        // TODO: Loop over content here
        SyndEntry entry = new SyndEntryImpl();
        entry.setTitle(article.getTitle());
        UrlBuilder urlBuilder = new UrlBuilder(article.getContentKey(), article.getFullUri());
        entry.setLink(urlBuilder.asFullUrl(request));
        entry.setPublishedDate(article.getDateCreated());
        entry.setCategories(categories);

        SyndContent description = new SyndContentImpl();
        description.setType("text/html");
        description.setValue(article.getSummary());
        entry.setDescription(description);

        List<SyndEntry> entries = new ArrayList<SyndEntry>();
        entries.add(entry);

        feed.setEntries(entries);

        SyndFeedOutput output = new SyndFeedOutput();
        output.output(feed, response.getWriter());
    }

    public ICmsFeatureDao getCmsFeatureDao() {
        return _cmsFeatureDao;
    }

    public void setCmsFeatureDao(ICmsFeatureDao cmsFeatureDao) {
        _cmsFeatureDao = cmsFeatureDao;
    }
}
