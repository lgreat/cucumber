package gs.web.content.cms;

import gs.data.content.cms.CmsConstants;
import org.apache.commons.lang.StringUtils;
import org.springframework.web.servlet.mvc.AbstractController;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import gs.data.cms.IPublicationDao;
import gs.data.content.cms.ContentKey;
import gs.web.util.UrlBuilder;

public class CmsUrlTranslationController extends AbstractController {
    IPublicationDao _publicationDao;

    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
        if (!request.getParameterNames().hasMoreElements()) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return new ModelAndView("/status/error404.page");
        }

        String contentType = (String) request.getParameterNames().nextElement();

        if (CmsConstants.ARTICLE_CONTENT_TYPE.equals(contentType) || CmsConstants.ASK_THE_EXPERTS_CONTENT_TYPE.equals(contentType) ||
            CmsConstants.TOPIC_CENTER_CONTENT_TYPE.equals(contentType) || CmsConstants.ARTICLE_SLIDE_CONTENT_TYPE.equals(contentType) ||
            CmsConstants.ARTICLE_SLIDESHOW_CONTENT_TYPE.equals(contentType) || CmsConstants.DISCUSSION_BOARD_CONTENT_TYPE.equals(contentType) ||
            CmsConstants.VIDEO_CONTENT_TYPE.equals(contentType) || CmsConstants.WORKSHEET_CONTENT_TYPE.equals(contentType) ||
            CmsConstants.PAGE_CONTENT_TYPE.equals(contentType)) {
            Long contentId;

            try {
                contentId = new Long(request.getParameter(contentType));
            } catch (NumberFormatException e) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                return new ModelAndView("/status/error404.page");
            }

            UrlBuilder builder = new UrlBuilder(new ContentKey(contentType, contentId));
            // if content can't be found, the url becomes "null"
            if (StringUtils.equals("null", builder.asSiteRelative(request))) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                return new ModelAndView("/status/error404.page");
            }

            return new ModelAndView("redirect:" + builder.asSiteRelative(request));
        }

        if (CmsConstants.HOMEPAGE_CONTENT_TYPE.equals(contentType)) {
            return new ModelAndView("redirect:/index.page");
        }

        if (CmsConstants.MOST_POPULAR_CONTENT_CONTENT_TYPE.equals(contentType)) {
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
