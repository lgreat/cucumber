package gs.web.authorization;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Controller
@RequestMapping(OAuthCallbackHandler.PATH)
public class OAuthCallbackHandler {
    public static final String PATH = "/authorization/callback.page";


    @RequestMapping(method= RequestMethod.GET)
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        // TODO: Persist authentication code and perhaps access token. Needs to be done securely
        // TODO: this controller might not be necessary if we use a filter or interceptor to do this
        // TODO: unless we want to display an error view here if auth has failed

        String callback = request.getParameter(FacebookHelper.OAUTH_DONE_CALLBACK_PARAM);

        response.sendRedirect(callback);
    }
}
