package gs.web.auth;


import facebook4j.Facebook;
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
