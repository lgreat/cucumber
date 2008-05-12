package gs.web.util;

import org.springframework.web.servlet.mvc.Controller;
import org.springframework.web.servlet.ModelAndView;
//import org.apache.log4j.Logger;
import org.hibernate.cache.CacheKey;
import org.hibernate.type.AnyType;
import org.hibernate.EntityMode;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;

import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Cache;

/**
 * This controller provides a web interface to gs.data.school.LevelCode.createLevelCode();
 *
 * @author Dave Roy <mailto:droy@greatschools.net>
 */
public class ArticleCacheController implements Controller {
    //private static final Logger _log = Logger.getLogger(ArticleCacheController.class);

    /** Spring BEAN id */
    public static final String BEAN_ID = "articleCacheController";
    public static final String EXPIRE_AID_PARAM = "expire-aid";

    /**
     * @see gs.data.school.LevelCode
     */
    public ModelAndView handleRequest(HttpServletRequest request,
                                      HttpServletResponse response) throws Exception {
        response.setContentType("text/plain");

        PrintWriter out = response.getWriter();
        try {
            String expireAidParam = request.getParameter(EXPIRE_AID_PARAM);
            Integer expireAid = Integer.parseInt(expireAidParam);
            if (expireAid != null) {
                CacheManager manager = CacheManager.create();
                Cache cache = manager.getCache("sessionFactory.gs.data.content.Article");

                if (expireArticle(cache, expireAid)) {
                    out.print("Expired article: " + expireAid);
                } else {
                    out.print("Article not in cache: " + expireAid);
                }

            }
        } catch (IllegalArgumentException e) {
            // do nothing, page will be blank
        } finally {
            out.flush();            
        }

        return null;
    }

    /**
     * Expire the specified article from the specified cache.
     *
     * @param cache The cache to expire the article from
     * @param expireAid The article id to expire
     * @return true if the article was cached and is now expired, false in any other case.
     */
    private boolean expireArticle(Cache cache, Integer expireAid) {
        CacheKey tmp = new CacheKey(expireAid, new AnyType(), "gs.data.content.Article", EntityMode.POJO, null);
        return cache.remove(tmp);
    }

}