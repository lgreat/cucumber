package gs.web.content.cms;

import org.springframework.web.servlet.mvc.AbstractController;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import gs.data.cms.IPublicationDao;
import gs.data.content.cms.Publication;
import gs.data.content.cms.ContentKey;
import gs.web.util.UrlBuilder;

import java.util.Map;
import java.util.HashMap;

public class CmsUrlTranslationController extends AbstractController {
    IPublicationDao _publicationDao;

    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
        if (!request.getParameterNames().hasMoreElements()) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return new ModelAndView("/status/error404.page");
        }

        String contentType = (String) request.getParameterNames().nextElement();

        if ("Article".equals(contentType) || "AskTheExperts".equals(contentType) || "TopicCenter".equals(contentType) ||
            "ArticleSlide".equals(contentType) || "ArticleSlideshow".equals(contentType) || "DiscussionBoard".equals(contentType)
             || "Video".equals(contentType)) {
            Long contentId;

            try {
                contentId = new Long(request.getParameter(contentType));
            } catch (NumberFormatException e) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                return new ModelAndView("/status/error404.page");
            }

            UrlBuilder builder = new UrlBuilder(new ContentKey(contentType, contentId));

            return new ModelAndView("redirect:" + builder.asSiteRelative(request));
        }

        if ("Homepage".equals(contentType)) {
            return new ModelAndView("redirect:/index.page");
        }

        if ("MostPopularContent".equals(contentType)) {
            return new ModelAndView("redirect:/content/cms/topicCenter.page");
        }

        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        return new ModelAndView("/status/error404.page");
    }

    public IPublicationDao getPublicationDao() {
        return _publicationDao;
    }

    public void setPublicationDao(IPublicationDao publicationDao) {
        _publicationDao = publicationDao;
    }
}
