package gs.web.authorization;


import facebook4j.Facebook;
import facebook4j.FacebookFactory;
import gs.web.request.RequestInfo;
import org.apache.commons.lang.StringEscapeUtils;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;

@Component
public class FacebookSignin {

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Facebook facebook = FacebookSession.builder().build();

        String oauthCallback = FacebookHelper.getOAuthCallbackUrl(request);

        response.sendRedirect(facebook.getOAuthAuthorizationURL(
            URLEncoder.encode(oauthCallback, "UTF-8")
        ));
    }
}
