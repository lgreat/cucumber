package gs.web.content;

import org.springframework.web.servlet.mvc.SimpleFormController;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.validation.BindException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.lang.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.net.URL;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class Election2008Controller extends SimpleFormController {
    public static final String BEAN_ID = "/content/election2008.page";
    public static final String ADD_SITE_VISITOR_URL =
            "http://api.constantcontact.com/0.1/API_AddSiteVisitor.jsp?loginName=edin08" +
                    "&loginPassword=EDin@8&ic=Great%20Schools";
    protected final Log _log = LogFactory.getLog(getClass());
    protected static List<String> stats;

    static {
        stats = new ArrayList<String>();
        stats.add("70% of American 8th-graders can�t read at 8th-grade levels.");
        stats.add("24 countries outscore U.S. schools in math.");
        stats.add("20 countries outscore U.S. schools in science.");
        stats.add("93% of U.S. middle school science teachers have little or no training in science.");
        stats.add("America�s high school graduation rate ranks 21st internationally.");
        stats.add("America had 0% increase in number of bachelor�s and master�s degrees awarded.");
    }

    protected Map referenceData(HttpServletRequest request) {
        Map<String, String> map = new HashMap<String, String>();
        map.put("startlingStat", getRandomStat(stats));

        return map;
    }

    /**
     * Returns a random row out of a list of rows.
     *
     * @param stats list of rows
     * @return a random row contained in rows
     */
    protected String getRandomStat(List<String> stats) {
        int count = stats.size();
        Random ran = new Random();
        int randomIndex = ran.nextInt(count);
        return stats.get(randomIndex);
    }

    protected ModelAndView onSubmit(HttpServletRequest request, HttpServletResponse response,
                                    Object objCommand, BindException errors) {
        Election2008Command command = (Election2008Command) objCommand;

        // TODO: utilize SiteVisitor API to sync email
        //syncInfoWithConstantContact(command);

        // since I'm forwarding to another FormController, I need to pass it info
        // in the language it understands ... namely it's command
        Election2008EmailCommand emailCommand = new Election2008EmailCommand();
        emailCommand.setUserEmail(command.getEmail());
        emailCommand.setSuccess(true);

        Map<String, Object> model = new HashMap<String, Object>();
        model.put("edin08Cmd", emailCommand);
        return new ModelAndView(getSuccessView(), model);
    }

    /**
     * Syncs email/zip with constant contact. Returns true if successful, false otherwise
     */
    protected boolean syncInfoWithConstantContact(Election2008Command command) {
        try {
            String url = ADD_SITE_VISITOR_URL;
            url += "&ea=" + URLEncoder.encode(command.getEmail(), "UTF-8");
            url += "&Postal_Code=" + command.getZip();
            URL urlAddress = new URL(url);

            _log.warn(urlAddress);
            HttpURLConnection connection = (HttpURLConnection) urlAddress.openConnection();
            // Some websites don't like unknown user agents so we put Mozilla in there to appease them
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (compatible; Greatschoolsbot/1.1; +http://www.greatschools.net/cgi-bin/feedback/CA)");

            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String line = reader.readLine();
                if (!StringUtils.equals("0", line)) {
                    // According to the API, if the first line is not 0, then it must be "500" signifying error.
                    // On error, the second line is the reason for the error. Log that here
                    line = reader.readLine();
                    _log.error("Error syncing with Constant Contact: " + line);
                    return false;
                }
            } finally {
                connection.disconnect();
            }
            return true;
        } catch (Exception e) {
            _log.error("Error syncing info with constant contact", e);
        }
        return false;
    }
}
