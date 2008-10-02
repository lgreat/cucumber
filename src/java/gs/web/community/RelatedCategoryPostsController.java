package gs.web.community;

import org.springframework.web.servlet.mvc.AbstractController;
import org.springframework.web.servlet.ModelAndView;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.lang.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.net.URLEncoder;
import java.io.UnsupportedEncodingException;

import gs.data.content.ArticleCategory;
import gs.data.content.IArticleCategoryDao;
import gs.data.content.ArticleCategoryToCommunityMapping;
import gs.data.util.feed.IFeedDao;
import gs.web.util.context.SessionContextUtil;
import com.sun.syndication.feed.synd.SyndEntry;

/**
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class RelatedCategoryPostsController extends AbstractController {
    protected static final Log _log = LogFactory.getLog(RelatedCategoryPostsController.class);

    private String _viewName;
    private IArticleCategoryDao _articleCategoryDao;
    private int _numberOfEntriesToShow;
    private IFeedDao _feedDao;

    protected ModelAndView handleRequestInternal
            (HttpServletRequest request, HttpServletResponse response) throws Exception {
        Map<String, Object> model = new HashMap<String, Object>();
        List<RelatedCommunityPost> relatedPosts = new ArrayList<RelatedCommunityPost>();

        // required parameter
        String[] categoryIds = request.getParameterValues("category");

        if (categoryIds != null) {
            // get ArticleCategory objects
            List<ArticleCategory> articleCategories = getArticleCategoriesFromIds(categoryIds);
            // get search term for article categories
            String searchTerm = getSearchTermFromArticleCategories(articleCategories);

            // get feed entries for these categories, and extract related posts from them
            if (searchTerm != null) {
                relatedPosts =
                        getRelatedPostsFromFeedEntries(_feedDao.getFeedEntries
                                (getFeedURL(request, searchTerm), _numberOfEntriesToShow));
            }
        }
        model.put("relatedPosts", relatedPosts);

        return new ModelAndView(_viewName, model);
    }

    /**
     * Converts each FeedEntry into a RelatedCommunityPost object.
     *
     * @param feedEntries
     * @return non-null list, guaranteed to have size less than or equal to _numberOfEntriesToShow
     */
    protected List<RelatedCommunityPost> getRelatedPostsFromFeedEntries(List<SyndEntry> feedEntries) {
        List<RelatedCommunityPost> relatedPosts = new ArrayList<RelatedCommunityPost>();
        for (SyndEntry entry: feedEntries) {
            // TODO: Move this into constructor?
            RelatedCommunityPost relatedPost = new RelatedCommunityPost();
            relatedPost.setTitle(entry.getTitle());
            relatedPost.setLink(entry.getLink());
            relatedPost.setType(entry.getLink());
            relatedPost.setDescription(entry.getDescription() != null ? entry.getDescription().getValue(): "");
            relatedPosts.add(relatedPost);
            // fail-safe
            if (relatedPosts.size() >= _numberOfEntriesToShow) {
                break;
            }
        }
        return relatedPosts;
    }

    /**
     * Returns the URL to the feed for the specified categories
     * @param request required to determine which community host to generate URL to.
     * @param searchTerm Search term to use in URL for feed
     */
    protected String getFeedURL(HttpServletRequest request, String searchTerm) {
        String communityHost = SessionContextUtil.
                getSessionContext(request).getSessionContextUtil().getCommunityHost(request);
        String url = null;
        try {
            url = "http://" + communityHost +
                            "/search/rss/?q=" + URLEncoder.encode(searchTerm, "UTF-8") + "&search_type=0&sort=relevance&limit=" +
                            _numberOfEntriesToShow;
        } catch (UnsupportedEncodingException e) {
            // nothing
        }
        return url;
    }

    /**
     * Pulls each ArticleCategory out of the DB using the id.
     * @param ids list of ArticleCategory ids
     */
    protected List<ArticleCategory> getArticleCategoriesFromIds(String[] ids) {
        List<ArticleCategory> articleCategories = new ArrayList<ArticleCategory>();
        for (String categoryId: ids) {
            if (StringUtils.isNotBlank(categoryId) && StringUtils.isNumeric(categoryId)) {
                ArticleCategory category = _articleCategoryDao.getArticleCategory(Integer.valueOf(categoryId));
                articleCategories.add(category);
            }
        }
        return articleCategories;
    }

    /**
     * Hard coded right now to only look up the term for the first category.
     * TODO: How to concatenate multiple category terms together?
     */
    protected String getSearchTermFromArticleCategories(List<ArticleCategory> articleCategories) {
        if (articleCategories != null && articleCategories.size() > 0) {
            return ArticleCategoryToCommunityMapping.getTermForArticleCategory(articleCategories.get(0));
        }
        return null;
    }

    public String getViewName() {
        return _viewName;
    }

    public void setViewName(String viewName) {
        _viewName = viewName;
    }

    public int getNumberOfEntriesToShow() {
        return _numberOfEntriesToShow;
    }

    public void setNumberOfEntriesToShow(int numberOfEntriesToShow) {
        _numberOfEntriesToShow = numberOfEntriesToShow;
    }

    public IArticleCategoryDao getArticleCategoryDao() {
        return _articleCategoryDao;
    }

    public void setArticleCategoryDao(IArticleCategoryDao articleCategoryDao) {
        _articleCategoryDao = articleCategoryDao;
    }

    public IFeedDao getFeedDao() {
        return _feedDao;
    }

    public void setFeedDao(IFeedDao feedDao) {
        _feedDao = feedDao;
    }

    public static class RelatedCommunityPost {
        private String _title;
        private String _link;
        private String _type;
        private String _description;

        public String getTitle() {
            return _title;
        }

        public void setTitle(String title) {
            _title = title;
        }

        public String getLink() {
            return _link;
        }

        public void setLink(String link) {
            _link = link;
        }

        public String getType() {
            return _type;
        }

        public String getDescription(){
            return _description;
        }

        public void setDescription(String description){
            _description = description;
        }

        public void setType(String url) {
            String type = "question";
            if (StringUtils.contains(url, "/advice/")) {
                type = "advice";
            } else if (StringUtils.contains(url, "/groups/") && StringUtils.contains(url, "/discussion/")) {
                type = "discussion";
            } else if (StringUtils.contains(url, "/groups/")) {
                type = "group";
            }
            _type = type;
        }
    }
}
