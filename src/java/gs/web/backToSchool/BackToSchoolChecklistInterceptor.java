package gs.web.backToSchool;

import gs.data.community.User;
import gs.web.util.context.SessionContextUtil;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.IOException;

public class BackToSchoolChecklistInterceptor extends HandlerInterceptorAdapter {

        public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException, ServletException, ParserConfigurationException, TransformerException {

            User user = SessionContextUtil.getSessionContext(request).getUser();

            request.setAttribute("backToSchoolChecklistNumberCompleted", BackToSchoolChecklist.getNumberOfCompletedItems(user));
            request.setAttribute("backToSchoolChecklistComplete", BackToSchoolChecklist.hasCompletedChecklist(user));

            return true;
        }
}
