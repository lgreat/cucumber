package gs.web.school;

import gs.data.school.EspResponse;
import gs.data.school.School;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: rramachandran
 * Date: 8/20/12
 * Time: 11:41 AM
 * To change this template use File | Settings | File Templates.
 */

@Controller
@RequestMapping("/school/profileEnrollment.page")
public class SchoolProfileEnrollmentController extends AbstractSchoolProfileController {
    protected static final Log _log = LogFactory.getLog(SchoolProfileEnrollmentController.class.getName());
    public static final String VIEW = "school/profileEnrollment";

    private static final String MODEL_APPLY = "apply";
    private static final String MODEL_LEARN_MORE_URL = "learnMoreUrl";
    private static final String MODEL_DEADLINE_DATE = "deadlineDate";
    private static final String MODEL_DAYS_LEFT = "daysLeft";
    private static final String MODEL_ENROLLMENT_STATE = "enrollmentState";
    private static final String MODEL_NUM_MONTHS_PAST_DEADLINE = "monthsPastDeadline";

    private static final String MODEL_PLANNING_AHEAD = "planningAhead";
    private static final String MODEL_CHANCES = "chances";
    private static final String MODEL_COST = "cost";
    private static final String MODEL_NUM_RESPONSE = "numResponse";
    private static final String MODEL_QUESTION = "question";
    private static final String MODEL_RESPONSE = "response";
    private static final String MODEL_CHANCES_ACCEPTANCE_RATE = "acceptanceRate";
    private static final String MODEL_CHANCES_ACCEPTANCE_RATE_YEAR = "acceptanceRateYear";

    private static final String[] PLANNING_AHEAD_PG_KEYS = {"_2yr", "_4yr", "_military", "_vocational", "_workforce",
            "_year"};

    @Autowired
    private SchoolProfileDataHelper _schoolProfileDataHelper;

    @RequestMapping(method = RequestMethod.GET)
    public String handle(ModelMap modelMap, HttpServletRequest request) {
        School school = getSchool(request);
        modelMap.put( "school", school );

        // There are two versions of this page, one if there is OSP (aka ESP) data available or not.
        // This can be determined by retrieving the esp data and seeing it if any data is returned.
        // Then execute the Esp or non-Esp code
        Map<String, List<EspResponse>> espResults = _schoolProfileDataHelper.getEspDataForSchool( request );
        modelMap.put( "espData", espResults );

        if( espResults != null && !espResults.isEmpty() ) {
            // OSP case
            handleEspPage( modelMap, request, school, espResults );
        }
        else {
            // Non-OSP case
            handleNonEspPage( modelMap, request, school );
        }
        return VIEW;
    }

    private void handleEspPage(Map model, HttpServletRequest request, School school, Map<String,List<EspResponse>> espData) {
        Map applyTileMap = getApplInfoEspTile(request, school, espData);
        model.put( MODEL_APPLY, applyTileMap );

        model.put( MODEL_PLANNING_AHEAD, getPlanningAheadEspTile(request, school, espData) );

        if((Boolean) applyTileMap.get("hasApplicationProcess")) {
            model.put(MODEL_CHANCES, getChancesEspTile(model, request, school, espData));
        }

        model.put(MODEL_COST, getCostEspTile(request, school, espData));
    }

    Map getApplInfoEspTile(HttpServletRequest request, School school, Map<String, List<EspResponse>> espData) {
        Map<String, Object> model = new HashMap<String, Object>(2);

        // Has application process - true or false
        List<EspResponse> applicationProcess = espData.get( "application_process" );
        boolean hasApplicationProcess = checkEspResponseListForValue(applicationProcess, new String[]{"yes"});
        model.put("hasApplicationProcess", hasApplicationProcess);

        if(hasApplicationProcess) {
            //learn more text
            String learnMoreUrl = null;
            if(espData.get( "admissions_url" ) != null && espData.get( "admissions_url" ).size() > 0) {
                learnMoreUrl = espData.get( "admissions_url" ).get(0).getValue();
            }
            else if(school.getWebSite() != null) {
                learnMoreUrl = school.getWebSite();
            }

            if(learnMoreUrl != null && learnMoreUrl.trim().length() > 0 && !learnMoreUrl.startsWith("http")) {
                learnMoreUrl = "http://" + learnMoreUrl;
            }
            model.put(MODEL_LEARN_MORE_URL, learnMoreUrl);

            //deadline info
            List<EspResponse> appDeadlineResponse = espData.get( "application_deadline" );
            String applicationDeadline = null;
            List<EspResponse> appDeadlineDateResponse = espData.get( "application_deadline_date" );
            Date applicationDeadlineDate = null;
            if(appDeadlineResponse != null && appDeadlineResponse.size() > 0) {
                applicationDeadline = appDeadlineResponse.get(0).getValue();
            }
            if("date".equalsIgnoreCase(applicationDeadline) && appDeadlineDateResponse != null && appDeadlineDateResponse.size() > 0) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
                try {
                    applicationDeadlineDate = dateFormat.parse(appDeadlineDateResponse.get(0).getValue());
                }
                catch (ParseException ex) {
                    _log.warn("SchoolProfileEnrollmentController : could not parse date.\n" + ex.getMessage());
                }
            }

            String deadlineInfoDate = null;
            Calendar today = Calendar.getInstance();
            Calendar nextYear = Calendar.getInstance();
            nextYear.add(Calendar.YEAR, 1);

            if("date".equals(applicationDeadline) && applicationDeadlineDate != null) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM dd, yyyy");
                deadlineInfoDate = dateFormat.format(applicationDeadlineDate);
                model.put(MODEL_DEADLINE_DATE, deadlineInfoDate);
                if(applicationDeadlineDate.after(today.getTime())) {
                    //some time in future
                    if(applicationDeadlineDate.before(nextYear.getTime())) {
                        int numDaysLeft = (int)((applicationDeadlineDate.getTime() - today.getTime().getTime())/(1000*60*60*24));
                        model.put(MODEL_DAYS_LEFT, numDaysLeft);
                    }
                    model.put(MODEL_ENROLLMENT_STATE, 1);
                }
                else if(applicationDeadlineDate.before(today.getTime())) {//find num of months past deadline
                    Calendar appDeadlineDateCal = Calendar.getInstance();
                    appDeadlineDateCal.setTime(applicationDeadlineDate);
                    int months = (today.get(Calendar.YEAR) - appDeadlineDateCal.get(Calendar.YEAR)) * 12 +
                            (today.get(Calendar.MONTH) - appDeadlineDateCal.get(Calendar.MONTH)) +
                            (today.get(Calendar.DAY_OF_MONTH) >= appDeadlineDateCal.get(Calendar.DAY_OF_MONTH)? 0: -1);
                    if(months > 12) {
                        model.put(MODEL_ENROLLMENT_STATE, 3);
                    }
                    else if(months < 3) {
                        model.put(MODEL_NUM_MONTHS_PAST_DEADLINE, months);
                        model.put(MODEL_ENROLLMENT_STATE, 2);
                    }
                    else {
                        model.put(MODEL_ENROLLMENT_STATE, 1);
                    }
                }
            }
            else if("yearround".equals(applicationDeadline)) {
                model.put(MODEL_ENROLLMENT_STATE, 4);
            }
            else if(("date".equals(applicationDeadline) && applicationDeadlineDate == null) ||
                    "parents_contacts".equals(applicationDeadline) || applicationDeadlineDate == null) {
                model.put(MODEL_ENROLLMENT_STATE, 3);
            }
        }

        return model;
    }

    List getPlanningAheadEspTile(HttpServletRequest request, School school, Map<String, List<EspResponse>> espData) {
        List<Map> rows = new ArrayList<Map>();

        Map<String, Object> destinationSchoolRow = new HashMap<String, Object>();
        //if there are destination schools response values, display them. if not get the destination colleges.
        int numDest = 0;
        List<String> destinationResponseValues = new ArrayList<String>();
        for(int i = 1; i < 4; i++) {
            String schoolDestination = "destination_school" + i;
            List<EspResponse> destinationSchool = espData.get(schoolDestination);
            if(destinationSchool != null && destinationSchool.size() > 0) {
                destinationResponseValues.add(destinationSchool.get(0).getValue());
                numDest++;
            }
        }

        if(numDest == 0) {
            for(int i = 1; i < 4; i++) {
                String collegeDestination = "college_destination_" + i;
                List<EspResponse> destinationCollege = espData.get(collegeDestination);
                if(destinationCollege != null && destinationCollege.size() > 0) {
                    destinationResponseValues.add(destinationCollege.get(0).getValue());
                    numDest++;
                }
            }
        }
        if(numDest > 0) {
            destinationSchoolRow.put(MODEL_RESPONSE, destinationResponseValues);
            destinationSchoolRow.put(MODEL_NUM_RESPONSE, numDest);
            destinationSchoolRow.put(MODEL_QUESTION, "Students typically attend these schools after graduating");
            rows.add(destinationSchoolRow);
        }

        Map<String, Object> collegePrepRow = new HashMap<String, Object>();
        List<EspResponse> collegePreparation = espData.get("college_prep");
        List<String> collegePrepResponseValues = new ArrayList<String>();
        int numCollegePrepResponse = 0;
        if(collegePreparation != null && collegePreparation.size() > 0) {
            if(espData.get("college_prep_other") != null) {
                collegePreparation.addAll(espData.get("college_prep_other"));
            }
            for(int i = 1; i <= collegePreparation.size(); i++) {
                String response = collegePreparation.get(i-1).getPrettyValue();
                if(collegePreparation.size() > 1 && "none".equalsIgnoreCase(response)) {
                    continue;
                }
                collegePrepResponseValues.add(response);
            }
            numCollegePrepResponse = collegePreparation.size();
        }
        if(numCollegePrepResponse > 0) {
            collegePrepRow.put(MODEL_RESPONSE, collegePrepResponseValues);
            collegePrepRow.put(MODEL_NUM_RESPONSE, numCollegePrepResponse);
            collegePrepRow.put(MODEL_QUESTION, "College preparation / awareness  offered");
            rows.add(collegePrepRow);
        }

        Map<String, Object> pgPlansRow = new HashMap<String, Object>();
        String pgResponse = "post_graduation";
        int numPgPlans = 0;
        List<String> pgPlansResponseValues = new ArrayList<String>();
        List<EspResponse> pgPlansYear = espData.get(pgResponse + "_year");
        if(pgPlansYear != null && pgPlansYear.size() > 0) {
            for(int i = 1; i <= PLANNING_AHEAD_PG_KEYS.length; i++) {
                String responseKey = pgResponse + PLANNING_AHEAD_PG_KEYS[i-1];
                List<EspResponse> postGraduationPlans = espData.get(responseKey);
                if(postGraduationPlans != null && postGraduationPlans.size() > 0) {
                    String percent = postGraduationPlans.get(0).getValue() + "%";
                    switch (i) {
                        case 1: pgPlansResponseValues.add("2 year college - " + percent); break;
                        case 2: pgPlansResponseValues.add("4 year college - " + percent); break;
                        case 3: pgPlansResponseValues.add("Military - " + percent); break;
                        case 4: pgPlansResponseValues.add("Vocational - " + percent); break;
                        case 5: pgPlansResponseValues.add("Workforce - " + percent); break;
                    }
                    numPgPlans++;
                }
            }
        }
        if(numPgPlans > 0) {
            pgPlansRow.put(MODEL_RESPONSE, pgPlansResponseValues);
            pgPlansRow.put(MODEL_QUESTION, "Students' post-graduation plans in " + pgPlansYear.get(0).getValue());
            pgPlansRow.put(MODEL_NUM_RESPONSE, numPgPlans);
            rows.add(pgPlansRow);
        }

        return rows;
    }

    List getChancesEspTile(Map model, HttpServletRequest request, School school, Map<String, List<EspResponse>> espData) {
        List<Map> rows = new ArrayList<Map>();

        List<EspResponse> studentsAcceptedYear = espData.get("students_accepted_year");
        List<EspResponse> applicationsReceivedYear = espData.get("applications_received_year");
        String studentsAcceptedYrResponse = null;
        String studentsAcceptedForYear = null;
        String applRecvdYearResponse = null;
        String applRecvdForYear = null;

        if(studentsAcceptedYear != null && studentsAcceptedYear.size() > 0) {
            Map<String, Object> studentsAcceptedRow = new HashMap<String, Object>();
            studentsAcceptedYrResponse = studentsAcceptedYear.get(0).getValue();
            List<EspResponse> studentsAccepted = espData.get("students_accepted");
            if(studentsAccepted != null && studentsAccepted.size() > 0) {
                studentsAcceptedRow.put(MODEL_QUESTION, "Students accepted for the " +
                        studentsAcceptedYrResponse + " school year");
                studentsAcceptedForYear = studentsAccepted.get(0).getValue();
                studentsAcceptedRow.put(MODEL_RESPONSE, studentsAcceptedForYear);
                rows.add(studentsAcceptedRow);
            }
        }

        if(applicationsReceivedYear != null && applicationsReceivedYear.size() > 0) {
            Map<String, Object> applicationsReceivedRow = new HashMap<String, Object>();
            applRecvdYearResponse = applicationsReceivedYear.get(0).getValue();
            List<EspResponse> applicationsReceived = espData.get("applications_received");
            if(applicationsReceived != null && applicationsReceived.size() > 0) {
                applicationsReceivedRow.put(MODEL_QUESTION, "Applications received for the " +
                        applRecvdYearResponse + " school year");
                applRecvdForYear = applicationsReceived.get(0).getValue();
                applicationsReceivedRow.put(MODEL_RESPONSE, applRecvdForYear);
                rows.add(applicationsReceivedRow);
            }
        }

        if(studentsAcceptedYrResponse != null && studentsAcceptedForYear != null && applRecvdYearResponse != null &&
                applRecvdForYear !=null && studentsAcceptedYrResponse.equals(applRecvdYearResponse)) {
            Float percentage = (Float.parseFloat(studentsAcceptedForYear)/Float.parseFloat(applRecvdForYear))*10;
            int acceptance = Math.round(percentage);
            if(percentage < 1) {
                acceptance = 1;
            }
            else if (percentage > 10) {
                acceptance = 10;
            }
            model.put(MODEL_CHANCES_ACCEPTANCE_RATE, acceptance);
            model.put(MODEL_CHANCES_ACCEPTANCE_RATE_YEAR, applRecvdYearResponse);
        }

        Map<String, Object> studentsComeFromRow = new HashMap<String, Object>();
        List<String> schools = new ArrayList<String>();
        for(int i = 1; i < 4; i++) {
            List<EspResponse> schoolResponse = espData.get("feeder_school_" + i);
            if(schoolResponse != null && schoolResponse.size() > 0) {
                schools.add(schoolResponse.get(0).getValue());
            }
        }
        if(schools.size() > 0) {
            studentsComeFromRow.put(MODEL_QUESTION, "Students typically come from these schools");
            studentsComeFromRow.put(MODEL_RESPONSE, schools);
            rows.add(studentsComeFromRow);
        }

        return rows;
    }

    List getCostEspTile(HttpServletRequest request, School school, Map<String, List<EspResponse>> espData) {
        List<Map> rows = new ArrayList<Map>();

        Map<String, Object> tuitionFeeRow = new HashMap<String, java.lang.Object>();
        List<EspResponse> tuitionYear = espData.get("tuition_year");
        List<EspResponse> tuitionLow = espData.get("tuition_low");
        List<EspResponse> tuitionHigh = espData.get("tuition_high");
        if(tuitionYear != null && tuitionYear.size() > 0 && tuitionLow != null && tuitionLow.size() > 0 &&
                tuitionHigh != null && tuitionHigh.size() > 0) {
            tuitionFeeRow.put(MODEL_QUESTION, "Tuition range for the " + tuitionYear.get(0).getValue() + " school year");
            try {
                Integer low = Integer.parseInt(tuitionLow.get(0).getValue().substring(1));
                Integer high = Integer.parseInt(tuitionHigh.get(0).getValue().substring(1));
                if(low < high) {
                    tuitionFeeRow.put(MODEL_RESPONSE, "$" + low + " - " + "$" + high);
                }
                else {
                    tuitionFeeRow.put(MODEL_RESPONSE, "$" + high + " - " + "$" + low);
                }
            }
            catch (NumberFormatException ex) {
                _log.warn("SchoolProfileEnrollmentController: Tuition range for school year.\n" + ex.getMessage());
                tuitionFeeRow.put(MODEL_RESPONSE, tuitionLow.get(0).getValue() + " - " + tuitionHigh.get(0).getValue());
            }
            rows.add(tuitionFeeRow);
        }

        Map<String, Object> vouchersAcceptedRow = new HashMap<String, Object>();
        List<EspResponse> vouchers = espData.get("students_vouchers");
        if(vouchers != null && vouchers.size() > 0) {
            vouchersAcceptedRow.put(MODEL_QUESTION, "Vouchers accepted");
            vouchersAcceptedRow.put(MODEL_RESPONSE, vouchers.get(0).getPrettyValue());
            rows.add(vouchersAcceptedRow);
        }

        Map<String, Object> financialAidRow = new HashMap<String, Object>();
        List<EspResponse> financialAid = espData.get("financial_aid");
        if(financialAid != null && financialAid.size() > 0) {
            String financialAidResponse = financialAid.get(0).getPrettyValue();
            financialAidRow.put(MODEL_QUESTION, "Financial aid offered");

            List<String> financialAidTypeResponses = new ArrayList<String>();
            financialAidTypeResponses.add(financialAidResponse + ".");
            List<EspResponse> financialAidTypes = espData.get("financial_aid_type");
            if("Yes".equalsIgnoreCase(financialAidResponse) && financialAidTypes != null && financialAidTypes.size() > 0) {
                for(int i = 0; i < financialAidTypes.size(); i++) {
                    financialAidTypeResponses.add(financialAidTypes.get(i).getPrettyValue());
                }
            }
            financialAidRow.put(MODEL_RESPONSE, financialAidTypeResponses);
            rows.add(financialAidRow);
        }

        Map<String, Object> applicationFeeRow = new HashMap<String, Object>();
        List<EspResponse> applicationFee = espData.get("application_fee");
        if(applicationFee != null && applicationFee.size() > 0) {
            applicationFeeRow.put(MODEL_QUESTION, "Application fee");
            List<String> applicationFeeResponse = new ArrayList<String>();
            StringBuilder applicationFeeAmountResponse = new StringBuilder(applicationFee.get(0).getPrettyValue());

            List<EspResponse> applicationFeeAmount = espData.get("application_fee_amount");
            if("Yes".equalsIgnoreCase(applicationFeeAmountResponse.toString()) && applicationFeeAmount != null &&
                    applicationFeeAmount.size() > 0) {
                applicationFeeAmountResponse.append(". $" + applicationFeeAmount.get(0).getValue() + ".");
                applicationFeeResponse.add(applicationFeeAmountResponse.toString());
                List<EspResponse> feeWaivers = espData.get("fee_waivers");
                if(feeWaivers != null && feeWaivers.size() > 0) {
                    applicationFeeResponse.add(feeWaivers.get(0).getPrettyValue());
                }
            }
            else {
                applicationFeeResponse.add(applicationFeeAmountResponse.toString());
            }
            applicationFeeRow.put(MODEL_RESPONSE, applicationFeeResponse);
            rows.add(applicationFeeRow);
        }

        return rows;
    }

    /**
     * Helper function to go through a list of EspResponse objects looking for one of the specified values
     * @param espResponses The EspResponse objects to check
     * @param valuesToLookFor The values to look for
     * @return True if any value is found in the EspResponses
     */
    private boolean checkEspResponseListForValue(List<EspResponse> espResponses, String[] valuesToLookFor) {
        if( (espResponses==null) || (espResponses.size()==0) ) {
            return true;
        }

        for( String val : valuesToLookFor ) {
            for( EspResponse r : espResponses ) {
                if( r.getValue().equals( val ) ) {
                    return true;    // Found, we are done
                }
            }
        }
        return false;   // If we get here the answer no match was found
    }

    private void handleNonEspPage(Map model, HttpServletRequest request, School school) {
        model.put( MODEL_APPLY, getApplInfoNonEspTile(request, school) );
    }

    Map getApplInfoNonEspTile(HttpServletRequest request, School school) {
        Map<String, Object> model = new HashMap<String, Object>(2);
        String applyUrl = null;
        if(school.getWebSite() != null) {
            applyUrl = school.getWebSite();
        }
        model.put(MODEL_LEARN_MORE_URL, applyUrl);
        return model;
    }
}
