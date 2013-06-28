package gs.web.util.validator;

import gs.web.about.feedback.AddEditSchoolOrDistrictCommand;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.validator.*;
import org.springframework.validation.Errors;
import org.apache.commons.validator.EmailValidator;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by IntelliJ IDEA.
 * User: eddie
 * Date: 12/21/11
 * Time: 12:39 PM
 * To change this template use File | Settings | File Templates.
 */
public class AddEditSchoolOrDistrictCommandValidator implements IRequestAwareValidator  {
    public static final String BEAN_ID = "addEditSchoolOrDistrictCommandValidator";
    protected final Log _log = LogFactory.getLog(getClass());

    static final String ERROR_SUBMITTER_NAME_MISSING =
            "Please enter your name.";
    static final String ERROR_SUBMITTER_EMAIL_MISSING =
            "Please enter your email address.";
    static final String ERROR_SUBMITTER_EMAIL_INVALID =
            "Please enter a valid email address.";
    static final String ERROR_SUBMITTER_EMAIL_UNMATCHED =
            "Please re-enter your email address.";
    static final String ERROR_SUBMITTER_CONNECTION_TO_SCHOOL_MISSING =
            "Please enter your connection to the school.";
    static final String ERROR_SCHOOL_OR_DISTRICT_MISSING =
            "Please choose whether this is a school or district.";
    static final String ERROR_ADD_EDIT =
            "Please choose whether you wish to add or edit.";
    static final String ERROR_GRADES =
            "Please choose at least one grade.";
    static final String ERROR_DISTRICTID_MISSING =
            "Please select a district.";
    static final String ERROR_COUNTY_MISSING =
            "Please select a county.";
    static final String ERROR_SCHOOLID_MISSING =
            "Please select a school.";
    static final String ERROR_SCHOOLTYPE_MISSING =
            "Please select if the school is public, private or charter.";
    static final String ERROR_SCHOOLNAME_MISSING =
            "Please enter the school name.";
    static final String ERROR_STREET_MISSING =
            "Please enter the physical street address.";
    static final String ERROR_CITY_MISSING =
            "Please enter the city.";
    static final String ERROR_ZIPCODE_MISSING =
            "Please enter the ZIP code.";
    static final String ERROR_PHONE_MISSING =
            "Please enter the phone number.";
    static final String ERROR_ENROLLMENT_MISSING =
            "Please enter the number of students enrolled.";
    static final String ERROR_LOWAGE_MISSING =
            "Please select the lowest age served.";
    static final String ERROR_HIGHAGE_MISSING =
            "Please select the highest age served.";

    static final String ERROR_OPEN_MISSING =
            "Please select whether the school is open or not.";
    static final String ERROR_OPENSEASONYEAR_MISSING =
            "Please select the season and year the school will open.";

    static final String ERROR_CLOSE_MISSING =
            "Please select whether the school is closing or not.";
    static final String ERROR_CLOSESEASONYEAR_MISSING =
            "Please select the season and year the school will close.";


    public void validate(HttpServletRequest request, Object object, Errors errors) {
        AddEditSchoolOrDistrictCommand command = (AddEditSchoolOrDistrictCommand)object;
        EmailValidator emv = EmailValidator.getInstance();

        // personal information

        if (StringUtils.isBlank(command.getCity())) {
            errors.rejectValue("city", null, ERROR_CITY_MISSING);
        }

        if (StringUtils.isBlank(command.getZipcode())) {
            errors.rejectValue("Zipcode", null, ERROR_ZIPCODE_MISSING);
        }

        if (StringUtils.isBlank(command.getPhone())) {
            errors.rejectValue("phone", null, ERROR_PHONE_MISSING);
        }

        if (StringUtils.isBlank(command.getSubmitterName())) {
            errors.rejectValue("submitterName", null, ERROR_SUBMITTER_NAME_MISSING);
        }
        if (StringUtils.isBlank(command.getSubmitterEmail())) {
            errors.rejectValue("submitterEmail", null, ERROR_SUBMITTER_EMAIL_MISSING);
        } else if (!emv.isValid(command.getSubmitterEmail())) {
            errors.rejectValue("submitterEmail", null, ERROR_SUBMITTER_EMAIL_INVALID);
        } else if (StringUtils.isBlank(command.getSubmitterEmailConfirm()) ||
                !command.getSubmitterEmail().equals(command.getSubmitterEmailConfirm())) {
            errors.rejectValue("submitterEmailConfirm", null, ERROR_SUBMITTER_EMAIL_UNMATCHED);
        }

        if (StringUtils.isBlank(command.getSubmitterConnectionToSchool())) {
            errors.rejectValue("submitterConnectionToSchool", null, ERROR_SUBMITTER_CONNECTION_TO_SCHOOL_MISSING);
        }
        if (StringUtils.isBlank(command.getSchoolOrDistrict())) {
            errors.rejectValue("schoolOrDistrict", null, ERROR_SCHOOL_OR_DISTRICT_MISSING);
        }
        if (StringUtils.isBlank(command.getAddEdit())) {
            errors.rejectValue("addEdit", null, ERROR_ADD_EDIT);
        }

        if (StringUtils.isBlank(command.getGrades())) {
            errors.rejectValue("grades", null, ERROR_GRADES);
        }else{
            if(command.getGrades().equals("PK") || command.getGrades().equals("PK,KG")){
                if (StringUtils.isBlank(command.getLowAge())) {
                    errors.rejectValue("lowAge", null, ERROR_LOWAGE_MISSING);
                }
                if (StringUtils.isBlank(command.getHighAge())) {
                    errors.rejectValue("highAge", null, ERROR_HIGHAGE_MISSING);
                }
            }
        }
        if (StringUtils.isBlank(command.getStreet())) {
            errors.rejectValue("street", null, ERROR_STREET_MISSING);
        }

        /* DISTRICT SPECIFIC */
        if (command.getSchoolOrDistrict() != null && command.getSchoolOrDistrict().equals("district")){
            if (StringUtils.isBlank(command.getName())) {
                errors.rejectValue("name", null, ERROR_SCHOOLNAME_MISSING.replaceAll("school","district"));
            }
            if (StringUtils.isBlank(command.getDistrictId())) {
                errors.rejectValue("districtId", null, ERROR_DISTRICTID_MISSING);
            }
            if (StringUtils.isBlank(command.getCounty())) {
                errors.rejectValue("county", null, ERROR_COUNTY_MISSING);
            }
        }


        /* SCHOOL SPECIFIC */
        if (command.getSchoolOrDistrict() != null && command.getSchoolOrDistrict().equals("school")){
            if (command.getSchoolType() == null ) {
                errors.rejectValue("schoolType", null, ERROR_SCHOOLTYPE_MISSING);
            }

            if (StringUtils.isBlank(command.getName())) {
                errors.rejectValue("name", null, ERROR_SCHOOLNAME_MISSING);
            }

            /* EDIT SPECIFIC */
            if (command.getAddEdit() !=null &&  command.getAddEdit().equals("edit")){
                if(StringUtils.isBlank(command.getSchoolId())) {
                    errors.rejectValue("schoolId", null, ERROR_SCHOOLID_MISSING);
                }

                //Question is: Will this school be closing?
                if (StringUtils.isBlank(command.getOpen())) {
                    errors.rejectValue("open", null, ERROR_CLOSE_MISSING);
                }else{
                    if(command.getOpen().equals("Yes")){
                        if (StringUtils.isBlank(command.getOpenSeason()) || StringUtils.isBlank(command.getOpenYear()) ) {
                            errors.rejectValue("openSeason", null, ERROR_CLOSESEASONYEAR_MISSING);
                        }
                    }
                }
            }

            /* ADD SPECIFIC */
            if(command.getAddEdit() !=null &&  command.getAddEdit().equals("add")){
                if (StringUtils.isBlank(command.getEnrollment())) {
                    errors.rejectValue("enrollment", null, ERROR_ENROLLMENT_MISSING);
                }
                if (StringUtils.isBlank(command.getOpen())) {
                    errors.rejectValue("open", null, ERROR_OPEN_MISSING);
                }else{
                    if(command.getOpen().equals("No")){
                        if (StringUtils.isBlank(command.getOpenSeason()) || StringUtils.isBlank(command.getOpenYear())) {
                            errors.rejectValue("openSeason", null, ERROR_OPENSEASONYEAR_MISSING);
                        }
                    }
                }
            }
        }





    }

}
