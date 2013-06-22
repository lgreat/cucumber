package gs.web.authorization;


import facebook4j.FacebookException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Controller
@RequestMapping("/authorization/facebookTest")
public class FacebookTestController {

    @RequestMapping(method = RequestMethod.GET)
    public String doGet(ModelMap modelMap, HttpServletRequest request, HttpServletResponse response) {
        FacebookSession facebookSession = FacebookHelper.getFacebookSession(request);

        if (!facebookSession.isValid()) {
            try{
                new FacebookSignin().doGet(request, response);
            } catch (ServletException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            } catch (IOException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        } else {
            modelMap.put("accessToken", facebookSession.getOAuthAccessToken().getToken());
            modelMap.put("expires", String.valueOf(facebookSession.getOAuthAccessToken().getExpires()));
            try {
                modelMap.put("email", facebookSession.getMe().getEmail());
            } catch (FacebookException e) {
            }

        }


        return "/sandbox/facebookTest";
    }



}
