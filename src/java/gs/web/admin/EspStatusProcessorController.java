package gs.web.admin;

import gs.data.school.*;
import gs.data.state.State;
import gs.web.school.usp.EspStatus;
import gs.web.school.usp.EspStatusManager;
import gs.web.util.ReadWriteAnnotationController;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

@Controller
@RequestMapping("/admin/")
public class EspStatusProcessorController implements ReadWriteAnnotationController, BeanFactoryAware {
    private final Log _log = LogFactory.getLog(getClass());

    @Autowired
    protected ISchoolDao _schoolDao;

    @Autowired
    protected IEspResponseDao _espResponseDao;

    @Autowired
    private JavaMailSender _mailSender;

    private BeanFactory _beanFactory;

    public static final String PARAM_STATE = "state";
    public static final String PARAM_SCHOOL_IDS = "schoolIds";

    @RequestMapping(value = "espStatusProcessor.page", method = RequestMethod.GET)
    public void processEspSchoolStatus(HttpServletRequest request, HttpServletResponse response) {
        String stateStr = request.getParameter(PARAM_STATE);
        String schoolIdsStr = request.getParameter(PARAM_SCHOOL_IDS);

        List<Integer> schoolIdsModified = new ArrayList<Integer>();

        if (StringUtils.isBlank(stateStr) || StringUtils.isBlank(schoolIdsStr)) {
            _log.error("Cannot access the ESP Status Processor without a state and school id parameters.");
            return;
        }

        State state = null;
        try {
            state = State.fromString(stateStr);
        } catch (Exception e) {
            _log.error("Error fetching database state:" + stateStr + " Exception:" + e);
            return;
        }

        if (state != null) {
            String[] schoolIdsArr = schoolIdsStr.split(",");

            for (int i = 0; i < schoolIdsArr.length; i++) {

                Integer schoolId = null;
                try {
                    schoolId = Integer.parseInt(schoolIdsArr[i]);
                } catch (Exception e) {
                    _log.error("Error converting school Id:" + schoolIdsArr[i] + " Exception:" + e);
                    continue;
                }
                School school = _schoolDao.getSchoolById(state, schoolId);

                if (school != null) {
                    EspStatusManager statusManager = (EspStatusManager) _beanFactory.getBean("espStatusManager", new Object[]{school});

                    EspStatus espStatus = statusManager.getEspStatus();
                    System.out.println("--------espStatus------------" + espStatus);
                    if (EspStatus.OSP_PREFERRED.equals(espStatus)) {
                        Set<EspResponseSource> responseSourcesToDeactivate =
                                new HashSet<EspResponseSource>(Arrays.asList(EspResponseSource.usp));
                        _espResponseDao.deactivateResponses(school, responseSourcesToDeactivate);
                        schoolIdsModified.add(school.getId());
                    }
                }
            }
        }

        if (!schoolIdsModified.isEmpty()) {
            String mailBody = "ESP Status Modified in " + state + " for the following schoolIds:" + StringUtils.join(schoolIdsModified, ",");
//            sendEmail(mailBody);
        }

        return;
    }

    protected void sendEmail(String mailBody) {
        SimpleMailMessage message = new SimpleMailMessage();
        String email = "datateam@greatschools.org";
        message.setTo(email);
        message.setFrom("OspStatusProcessor");
        message.setSentDate(new Date());
        message.setSubject("OSP Status Modified");
        message.setText(mailBody);
        try {
            _mailSender.send(message);
        } catch (MailException me) {
            _log.error("Unable to send the message - \n" + message + "\n to the recipient " + email + "\n Error:" + me);
        }
    }

    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        _beanFactory = beanFactory;
    }

    public void setSchoolDao(ISchoolDao schoolDao) {
        _schoolDao = schoolDao;
    }

    public void setEspResponseDao(IEspResponseDao espResponseDao) {
        _espResponseDao = espResponseDao;
    }

    public void setMailSender(JavaMailSender mailSender) {
        _mailSender = mailSender;
    }
}