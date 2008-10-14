/**
 * Copyright (c) 2005 GreatSchools.net. All Rights Reserved.
 * $Id: AllArticlesController.java,v 1.15 2008/10/14 22:21:52 chriskimm Exp $
 */
package gs.web.content;

import gs.data.content.Article;
import gs.data.content.ArticleCategoryEnum;
import gs.data.content.ArticleManager;
import gs.data.content.IArticleDao;
import gs.web.util.context.SessionContext;
import gs.web.util.context.SessionContextUtil;
import gs.web.util.RedirectView301;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

/**
 * Controller to display all articles
 *
 * @author David Lee <mailto:dlee@greatschools.net>
 */
public class AllArticlesController extends AbstractController {

    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse httpServletResponse) throws Exception {
        return new ModelAndView(new RedirectView301("/education-topics/"));
    }
}
