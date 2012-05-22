package gs.web.admin;

import gs.data.community.*;
import gs.data.json.JSONException;
import gs.data.json.JSONObject;
import gs.web.util.ReadWriteAnnotationController;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
    protected final Log _log = LogFactory.getLog(getClass());
    private static final String VIEW = "admin/nlEmailFileUpload";
    private static final String EMAIL_ADDRESS_FIELD = "email";
    private static final String SET_SUBSCRIPTION = "1";
    private static final String LIST_MEMBER_HOW_FIELD = "manual_upload";

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

        /*
         Get the file from the request and write the contents to a temp file.
         */
        File file;
        try {
            List files = new ServletFileUpload(new DiskFileItemFactory()).parseRequest(request);
            if(files != null && !files.isEmpty()) {
                DiskFileItem fileItem = (DiskFileItem) files.get(0);

                String tempFilePath = System.getProperty("java.io.tmpdir") + System.getProperty("file.separator") + "Newsletters_Subscription_Email_List.csv";
                file = new File(tempFilePath);
                fileItem.write(file);
            }
            else {
                outputJson("error", "Unable to retrieve the uploaded file.", response);
                return;
            }
        }
        catch (FileUploadException ex) {
            outputJson("error", "Unable to upload the file.", response);
            return;
        }
        catch (Exception ex) {
            outputJson("error", "Unable to write file contents to a temp file.", response);
            return;
        }

        BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
        String rowData, uploadNotificationRecipient = "";
        StringBuilder parseErrorMessage = new StringBuilder("Error parsing the following lines -\n");
        boolean hasParsingErrors = false;
        int lineNum = 1, numFields = 0;
        Map<String, Integer> nameToCol = new HashMap<String, Integer>();

        while ((rowData = bufferedReader.readLine()) != null) {
            String[] data = rowData.split(",");
            if (lineNum < 3) {
                /*
                Line 1 must have a string that has a valid email format. This email would receive a notification when the program finishes.
                Since Apache would timeout in 20 s, for large data, it would be impossible to know if the upload was
                complete. So it is necessary to have this email address, else the program will exit and would display an
                 error in the client.
                 */
                if(lineNum == 1) {
                    uploadNotificationRecipient = data[0];
                    org.apache.commons.validator.EmailValidator emv = org.apache.commons.validator.EmailValidator.getInstance();
                    if(!emv.isValid(uploadNotificationRecipient)) {
                        outputJson("error", "Line " + lineNum + ": Email address given for receiving the upload" +
                                " status notification does not represent a valid format\n", response);
                        return;
                    }
                }
                /* Line 2 has the field headers - Email Address, greatnews, dailytip, sponsor.
                   These field names are mapped to integers that would be used to access the values when they are stored
                   in an array of comma separated words as each line is read.
                 */
                else {
                    for(int i = 0; i < data.length; i++) {
                        if(data[i].toLowerCase().contains(EMAIL_ADDRESS_FIELD)) {
                            nameToCol.put(EMAIL_ADDRESS_FIELD, i);
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
                    numFields = nameToCol.keySet().size();
                    if(numFields != 4) {
                        outputJson("error", "Please make sure that there are only 4 field names given in line 2 of the " +
                                "csv file - email address, " + SubscriptionProduct.PARENT_ADVISOR.getName() + ", " +
                                SubscriptionProduct.DAILY_TIP.getName() + ", " +
                                SubscriptionProduct.SPONSOR_OPT_IN.getName(), response);
                        return;
                    }
                }
                lineNum++;
                continue;
            }

            /*
            For all other lines, they are read into an array, and set in the database tables. If the number of comma
            separated words do not match with line 2, this would display an error for the line, and the program moves to
            the next line.
             */
            try {
                if(numFields != data.length) {
                    hasParsingErrors = true;
                    parseErrorMessage.append("Line " + lineNum + ": Number of comma separated values not equal to 4\n");
                    lineNum++;
                    continue;
                }
                User user = _userDao.findUserFromEmailIfExists(data[nameToCol.get(EMAIL_ADDRESS_FIELD)]);
                if(user != null) {
                    if(!user.getEmailVerified()) {
                        user.setEmailVerified(true);
                    }
                    _userDao.saveUser(user);
                }
                else {
                    user = new User();
                    user.setEmail(data[nameToCol.get(EMAIL_ADDRESS_FIELD)]);
                    user.setEmailVerified(true);
                    user.setHow(LIST_MEMBER_HOW_FIELD);
                    _userDao.saveUser(user);
                }
                addSubscriptions(user, data, nameToCol);
                lineNum++;
            }

            catch (NullPointerException e) {
                hasParsingErrors = true;
                parseErrorMessage.append("Line " + lineNum + ": Unable to match field name with the data\n");
                lineNum++;
            }

            catch (ArrayIndexOutOfBoundsException e) {
                hasParsingErrors = true;
                parseErrorMessage.append("Line " + lineNum + ": Unable to match field name with the data\n");
                lineNum++;
            }
        }
        bufferedReader.close();

        if(hasParsingErrors) {
            outputJson("error", parseErrorMessage.toString(), response);
            sendEmail(uploadNotificationRecipient, parseErrorMessage.toString());
        }
        else {
            outputJson("success", "Successfully uploaded the file and set the subscriptions.", response);
            sendEmail(uploadNotificationRecipient, "Signed up emails have been set with the subscriptions");
        }

        file.delete();
//        long endTime = System.currentTimeMillis();
//        long time = (endTime - startTime) / 1000;
//        System.out.println("Duration: " + time + " seconds");
    }

    private void addSubscriptions(User user, String[] data, Map<String,Integer> nameToCol)
            throws NullPointerException, ArrayIndexOutOfBoundsException{
        List<Subscription> newSubscriptions = new ArrayList<Subscription>();
        if(SET_SUBSCRIPTION.equals(data[nameToCol.get(SubscriptionProduct.PARENT_ADVISOR.getName())])) {
            newSubscriptions.add(newSubscription(user, SubscriptionProduct.PARENT_ADVISOR));
        }
        if(SET_SUBSCRIPTION.equals(data[nameToCol.get(SubscriptionProduct.DAILY_TIP.getName())])) {
            newSubscriptions.add(newSubscription(user, SubscriptionProduct.DAILY_TIP));
        }
        if(SET_SUBSCRIPTION.equals(data[nameToCol.get(SubscriptionProduct.SPONSOR_OPT_IN.getName())])) {
            newSubscriptions.add(newSubscription(user, SubscriptionProduct.SPONSOR_OPT_IN));
        }

        if(newSubscriptions.size() > 0) {
            _subscriptionDao.addNewsletterSubscriptions(user, newSubscriptions);
        }
    }

    private Subscription newSubscription(User user, SubscriptionProduct subscriptionProduct) {
        Subscription subscription = new Subscription();
        subscription.setProduct(subscriptionProduct);
        subscription.setUser(user);
        return subscription;
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
            _log.warn("Unable to send the message - \n" + message + "\n to the recipient " + email);
        }
    }

    protected void outputJson(String type, String message, HttpServletResponse response) throws JSONException, IOException {
        JSONObject jsonResponse = new JSONObject();
        jsonResponse.put(type, message);
        jsonResponse.write(response.getWriter());
        response.getWriter().flush();
    }
}
