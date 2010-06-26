package gs.web.backToSchool;

import gs.data.community.User;
import gs.data.dao.hibernate.ThreadLocalTransactionManager;
import gs.web.util.context.SessionContextUtil;
import org.apache.log4j.Logger;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.IOException;

public class BackToSchoolChecklistInterceptor extends HandlerInterceptorAdapter {

    private BackToSchoolChecklist _backToSchoolChecklist;

        public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException, ServletException, ParserConfigurationException, TransformerException {

            User user = SessionContextUtil.getSessionContext(request).getUser();
            
            //New requirement asks that BTS checklist items be checked if the user is signed in and if the user has viewed specific articles
            String content = request.getParameter("content");

            if (content != null && user != null) {
                boolean isReadOnly = ThreadLocalTransactionManager.isReadOnly();

                int contentId = (new Integer(content)).intValue();
                String item;

                switch(contentId) {
                    case 109:
                        item = BackToSchoolChecklist.BackToSchoolChecklistItem.BACK_TO_SCHOOL_SUPPLY_LIST.name();
                        break;
                    case 1082:
                        item = BackToSchoolChecklist.BackToSchoolChecklistItem.BACK_TO_SCHOOL_SUPPLY_LIST.name();
                        break;
                    case 1084:
                        item = BackToSchoolChecklist.BackToSchoolChecklistItem.BACK_TO_SCHOOL_SUPPLY_LIST.name();
                        break;
                    case 1085:
                        item = BackToSchoolChecklist.BackToSchoolChecklistItem.BACK_TO_SCHOOL_SUPPLY_LIST.name();
                        break;
                    case 1510:
                        item = BackToSchoolChecklist.BackToSchoolChecklistItem.ARTICLE1.name();
                        break;
                    case 71:
                        item = BackToSchoolChecklist.BackToSchoolChecklistItem.ARTICLE2.name();
                        break;
                    default:
                        item = null;
                }

                if (item != null) {
                    ThreadLocalTransactionManager.commitOrRollback();
                    _backToSchoolChecklist.addChecklistItem(item,user);
                    ThreadLocalTransactionManager.commitOrRollback();
                    //set things back the way they were
                    if (isReadOnly) {
                        ThreadLocalTransactionManager.setReadOnly();
                    }
                }
            }

            request.setAttribute("backToSchoolChecklistNumberCompleted", BackToSchoolChecklist.getNumberOfCompletedItems(user));
            request.setAttribute("backToSchoolChecklistComplete", BackToSchoolChecklist.hasCompletedChecklist(user));

            return true;
        }

    public BackToSchoolChecklist getBackToSchoolChecklist() {
        return _backToSchoolChecklist;
    }

    public void setBackToSchoolChecklist(BackToSchoolChecklist backToSchoolChecklist) {
        _backToSchoolChecklist = backToSchoolChecklist;
    }
}
