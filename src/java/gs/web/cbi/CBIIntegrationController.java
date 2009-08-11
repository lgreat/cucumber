package gs.web.cbi;

import org.springframework.web.servlet.mvc.Controller;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import gs.data.integration.exacttarget.ExactTargetAPI;

import java.io.PrintWriter;

public class CBIIntegrationController implements Controller {

    /** Spring BEAN id */
    public static final String BEAN_ID = "cbiIntegrationController";

    private static final String SECRET_KEY = "cbounder";

    /** Keys to the actions supported by this controller */
    public static final String SEND_TRIGGERED_EMAIL = "send_triggered_email";

    private ExactTargetAPI _exactTargetAPI;

    public ModelAndView handleRequest(HttpServletRequest request,
                                      HttpServletResponse response) throws Exception {
        PrintWriter out = response.getWriter();

        String secretKey = request.getParameter("key");
        if (SECRET_KEY.equals(secretKey)) {
            String action = request.getParameter("action");
            if (SEND_TRIGGERED_EMAIL.equals(action)) {
                out.print(sendTargetTriggeredEmail());
            }
        }
        response.setContentType("text/plain");
        out.flush();
        return null;
    }

    protected String sendTargetTriggeredEmail() {
        StringBuilder response = new StringBuilder();
        return response.toString();
    }

    public ExactTargetAPI getExactTargetAPI() {
        return _exactTargetAPI;
    }

    public void setExactTargetAPI(ExactTargetAPI _exactTargetAPI) {
        this._exactTargetAPI = _exactTargetAPI;
    }
}
