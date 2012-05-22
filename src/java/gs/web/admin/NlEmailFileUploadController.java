package gs.web.admin;

import gs.data.community.*;
import gs.data.json.JSONException;
import gs.data.json.JSONObject;
import gs.web.util.ReadWriteAnnotationController;
import org.apache.commons.fileupload.disk.DiskFileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: rramachandran
 * Date: 5/17/12
 * Time: 3:34 PM
 * To change this template use File | Settings | File Templates.
 */

@Controller
@RequestMapping("/admin/nlEmailFileUpload.page")
public class NlEmailFileUploadController implements ReadWriteAnnotationController {
    private static final String VIEW = "admin/nlEmailFileUpload";

    @Autowired
    private IUserDao _userDao;
    @Autowired
    private ISubscriptionDao _subscriptionDao;
    @Autowired
    private JavaMailSender _mailSender;

    @RequestMapping(method = RequestMethod.GET)
    public String display(HttpServletRequest request) {
        return VIEW;
    }


    /*
    Some links for servlet file upload - http://sacharya.com/file-upload/
                                         http://www.technicaladvices.com/2011/12/10/ajax-file-upload-to-a-java-servlet-in-html5/
     */
    @RequestMapping (method = RequestMethod.POST)
    public void processFile (HttpServletRequest request,
                             HttpServletResponse response) throws JSONException, IOException {
        response.setContentType("application/json");
//        long startTime = System.currentTimeMillis();
        File file;
        try {
            List files = new ServletFileUpload(new DiskFileItemFactory()).parseRequest(request);
            DiskFileItem fileItem = (DiskFileItem) files.get(0);

            String tempFilePath = System.getProperty("java.io.tmpdir") + System.getProperty("file.separator") + "Newsletters_Subscription_Email_List.csv";
            file = new File(tempFilePath);
            fileItem.write(file);
        }
        catch (Exception ex) {
            outputJson("error", "Unable to upload the file.", response);
            return;
        }

        BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
        String rowData, email = "";
        StringBuilder parseErrorMessage = new StringBuilder("Error parsing the following lines -\n");
        boolean hasParsingErrors = false;
        int lineNum = 1, numColumns = 0;
        Map<String, Integer> nameToCol = new HashMap<String, Integer>();

        while ((rowData = bufferedReader.readLine()) != null) {
            String[] data = rowData.split(",");
            if (lineNum < 3) {
                if(lineNum == 1) {
                    try {
                        email = data[0];
                        org.apache.commons.validator.EmailValidator emv = org.apache.commons.validator.EmailValidator.getInstance();
                        if(!emv.isValid(email)) {
                            throw new Exception("Invalid email format in line 1.");
                        }
                    }
                    catch (Exception e) {
                        parseErrorMessage.append("Line " + lineNum + ": " + e.getMessage() + "\n");
                        outputJson("error", parseErrorMessage.toString(), response);
                        return;
                    }
                }
                else {
                    for(int i = 0; i < data.length; i++) {
                        if(data[i].toLowerCase().contains("email")) {
                            nameToCol.put("email", i);
                        }
                        else if(data[i].toLowerCase().contains(SubscriptionProduct.PARENT_ADVISOR.getName())) {
                            nameToCol.put(SubscriptionProduct.PARENT_ADVISOR.getName(), i);
                        }
                        else if(data[i].toLowerCase().contains(SubscriptionProduct.DAILY_TIP.getName())) {
                            nameToCol.put(SubscriptionProduct.DAILY_TIP.getName(), i);
                        }
                        else if(data[i].toLowerCase().contains(SubscriptionProduct.SPONSOR_OPT_IN.getName())) {
                            nameToCol.put(SubscriptionProduct.SPONSOR_OPT_IN.getName(), i);
                        }
                    }
                    numColumns = data.length;
                }
                lineNum++;
                continue;
            }

            try {
                if(numColumns != data.length) {
                    throw new Exception("Number of values does not match with line 1.");
                }
                User user = _userDao.findUserFromEmailIfExists(data[nameToCol.get("email")]);
                if(user != null) {
                    if(!user.getEmailVerified()) {
                        user.setEmailVerified(true);
                    }
                    _userDao.saveUser(user);
                }
                else {
                    user = new User();
                    user.setEmail(data[nameToCol.get("email")]);
                    user.setEmailVerified(true);
                    _userDao.saveUser(user);
                }
                addSubscriptions(user, data, nameToCol);
                lineNum++;
            }

            catch (Exception e) {
                hasParsingErrors = true;
                if(e.getMessage() != null) {
                    parseErrorMessage.append("Line " + lineNum + ": " + e.getMessage() + "\n");
                }
                else {
                    parseErrorMessage.append("Line " + lineNum + ": " + e.getClass().getSimpleName() + "\n");
                }
                lineNum++;
            }
        }
        bufferedReader.close();

        if(hasParsingErrors) {
            outputJson("error", parseErrorMessage.toString(), response);
            sendEmail(email, parseErrorMessage.toString());
        }
        else {
            outputJson("success", "Successfully uploaded the file and set the subscriptions.", response);
            sendEmail(email, "Signed up emails have been set with the subscriptions");
        }

        file.delete();
//        long endTime = System.currentTimeMillis();
//        long time = (endTime - startTime) / 1000;
//        System.out.println("Duration: " + time + " seconds");
    }

    private void addSubscriptions(User user, String[] data, Map<String,Integer> nameToCol) throws Exception {
        List<Subscription> newSubscriptions = new ArrayList<Subscription>();
        Subscription subscription;
        if("1".equals(data[nameToCol.get(SubscriptionProduct.PARENT_ADVISOR.getName())])) {
            subscription = new Subscription();
            subscription.setProduct(SubscriptionProduct.PARENT_ADVISOR);
            subscription.setUser(user);
            newSubscriptions.add(subscription);
        }
        if("1".equals(data[nameToCol.get(SubscriptionProduct.DAILY_TIP.getName())])) {
            subscription = new Subscription();
            subscription.setProduct(SubscriptionProduct.DAILY_TIP);
            subscription.setUser(user);
            newSubscriptions.add(subscription);
        }
        if("1".equals(data[nameToCol.get(SubscriptionProduct.SPONSOR_OPT_IN.getName())])) {
            subscription = new Subscription();
            subscription.setProduct(SubscriptionProduct.SPONSOR_OPT_IN);
            subscription.setUser(user);
            newSubscriptions.add(subscription);
        }

        if(newSubscriptions.size() > 0) {
            _subscriptionDao.addNewsletterSubscriptions(user, newSubscriptions);
        }
    }

    protected void sendEmail(String email, String mailBody) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setFrom(email);
        message.setSentDate(new Date());
        message.setSubject("Email Upload");
        message.setText(mailBody);
        try {
            _mailSender.send(message);
        }
        catch (MailException me) {

        }
    }

    protected void outputJson(String type, String message, HttpServletResponse response) throws JSONException, IOException {
        JSONObject jsonResponse = new JSONObject();
        jsonResponse.put(type, message);
        jsonResponse.write(response.getWriter());
        response.getWriter().flush();
    }
}
