package gs.web.content;

import gs.data.content.cms.ContentKey;
import gs.web.util.UrlBuilder;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletRequest;


/**
 * Controller redirects permalinks used by facebook comments on facebook site to the canonical content url.
 */
@Controller
@RequestMapping("/content/")
public class CallbackUrlTranslationController {

    protected final Log _log = LogFactory.getLog(getClass());
    private static final String PARAM_TYPE = "type";
    private static final String PARAM_ID = "id";
    private static final String PARAM_FBC_ID = "fb_comment_id";
    private static final String PARAM_S_CID = "s_cid";

    @RequestMapping(value="view.page", method=RequestMethod.GET)
    public void redirectCallback(
        HttpServletRequest request,
        HttpServletResponse response,
        @RequestParam(value=PARAM_TYPE, required=true) String typeName,
        @RequestParam(value=PARAM_ID, required=true) String id,
        @RequestParam(value=PARAM_FBC_ID, required=false) String fbCommentId,
        @RequestParam(value=PARAM_S_CID, required=false) String sCid) {

        Long contentId = null;

        try {
            contentId = new Long(id);
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.setHeader("Location", "/status/error404.page");
            return;
        }

        UrlBuilder builder = new UrlBuilder(new ContentKey(typeName, contentId));
        String relativePath = builder.asSiteRelative(request);
        if (StringUtils.isNotBlank(relativePath)) {
            if (StringUtils.isNotBlank(sCid)) {
                builder.addParameter(PARAM_S_CID, sCid);
            }
            response.setStatus(HttpServletResponse.SC_MOVED_PERMANENTLY);
            response.setHeader("Location", response.encodeRedirectURL(builder.asSiteRelative(request)));
        } else {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.setHeader("Location", "/status/error404.page");
        }
    }
}