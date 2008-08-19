package gs.web.util.validator;

import gs.web.BaseTestCase;
import gs.web.GsMockHttpServletRequest;
import gs.web.community.registration.UserCommand;
import gs.web.about.feedback.SubmitSchoolCommand;
import gs.data.state.State;
import org.springframework.validation.Errors;
import org.springframework.validation.BindException;

/**
 * @author Young Fan
 */
public class SubmitSchoolCommandValidatorTest extends BaseTestCase {
    private static final String GOOD_SUBMITTER_NAME = "First Last";
    private static final String GOOD_SUBMITTER_EMAIL = "submitter@yahoo.com";
    private static final String GOOD_SUBMITTER_EMAIL_CONFIRM = "submitter@yahoo.com";
    private static final String GOOD_SUBMITTER_CONNECTION_TO_SCHOOL = "Connection";
    private static final String GOOD_SCHOOL_NAME = "School name";
    private static final String GOOD_STREET_ADDRESS = "123 Avenue Road";
    private static final String GOOD_CITY = "San Francisco";
    private static final State GOOD_STATE = State.CA;
    private static final String GOOD_ZIP_CODE = "94105";
    private static final String GOOD_ZIP_CODE_LONG = "94105-1234";
    private static final String GOOD_COUNTY = "San Francisco";
    private static final String GOOD_NUM_STUDENTS_ENROLLED = "5";
    private static final String GOOD_PHONE_NUMBER = "1234567890";
    private static final String GOOD_FAX_NUMBER = "1234567890";
    private static final String GOOD_SCHOOL_WEB_SITE = "www.greatschools.net";
    private static final String GOOD_SCHOOL_WEB_SITE_FULL = "http://www.greatschools.net";
    private static final String GOOD_RELIGION = "Religion";
    private static final String GOOD_ASSOCIATION = "Association";


    private SubmitSchoolCommandValidator _validator;
    private GsMockHttpServletRequest _request;

    protected void setUp() throws Exception {
        super.setUp();
        _validator = new SubmitSchoolCommandValidator();
        _request = new GsMockHttpServletRequest();
    }

    private SubmitSchoolCommand setupCommand() {
        SubmitSchoolCommand command = new SubmitSchoolCommand();
        command.setSubmitterName(GOOD_SUBMITTER_NAME);
        command.setSubmitterEmail(GOOD_SUBMITTER_EMAIL);
        command.setSubmitterEmailConfirm(GOOD_SUBMITTER_EMAIL_CONFIRM);
        command.setSubmitterConnectionToSchool(GOOD_SUBMITTER_CONNECTION_TO_SCHOOL);
        command.setSchoolName(GOOD_SCHOOL_NAME);
        command.setStreetAddress(GOOD_STREET_ADDRESS);
        command.setCity(GOOD_CITY);
        command.setState(GOOD_STATE);
        command.setZipCode(GOOD_ZIP_CODE);
        command.setCounty(GOOD_COUNTY);
        command.setNumStudentsEnrolled(GOOD_NUM_STUDENTS_ENROLLED);
        command.setPhoneNumber(GOOD_PHONE_NUMBER);
        command.setFaxNumber(GOOD_FAX_NUMBER);
        command.setSchoolWebSite(GOOD_SCHOOL_WEB_SITE);
        command.setReligion(GOOD_RELIGION);
        command.setAssociationMemberships(GOOD_ASSOCIATION);
        return command;
    }

    public void testSuccess() {
        SubmitSchoolCommand command = setupCommand();
        Errors errors = new BindException(command, "");
        _validator.validate(_request, command, errors);
        assertFalse(errors.toString(), errors.hasErrors());

        command.setZipCode(GOOD_ZIP_CODE_LONG);
        command.setSchoolWebSite(GOOD_SCHOOL_WEB_SITE_FULL);
        _validator.validate(_request, command, errors);
        assertFalse(errors.toString(), errors.hasErrors());
    }

    public void testSubmitterNameMissing() {
        SubmitSchoolCommand command = setupCommand();
        command.setSubmitterName("");
        Errors errors = new BindException(command, "");
        _validator.validate(_request, command, errors);
        assertTrue(errors.hasErrors());
        assertTrue(errors.hasFieldErrors("submitterName"));
        assertEquals(SubmitSchoolCommandValidator.ERROR_SUBMITTER_NAME_MISSING,
            errors.getFieldError("submitterName").getDefaultMessage());
    }

    public void testSubmitterEmailMissing() {
        SubmitSchoolCommand command = setupCommand();
        command.setSubmitterEmail("");
        Errors errors = new BindException(command, "");
        _validator.validate(_request, command, errors);
        assertTrue(errors.hasErrors());
        assertTrue(errors.hasFieldErrors("submitterEmail"));
        assertEquals(SubmitSchoolCommandValidator.ERROR_SUBMITTER_EMAIL_MISSING,
            errors.getFieldError("submitterEmail").getDefaultMessage());
    }

    public void testSubmitterEmailInvalid() {
        SubmitSchoolCommand command = setupCommand();
        command.setSubmitterEmail("asdf");
        Errors errors = new BindException(command, "");
        _validator.validate(_request, command, errors);
        assertTrue(errors.hasErrors());
        assertTrue(errors.hasFieldErrors("submitterEmail"));
        assertEquals(SubmitSchoolCommandValidator.ERROR_SUBMITTER_EMAIL_INVALID,
            errors.getFieldError("submitterEmail").getDefaultMessage());
    }

    public void testSubmitterEmailUnmatched() {
        SubmitSchoolCommand command = setupCommand();
        command.setSubmitterEmailConfirm("");
        Errors errors = new BindException(command, "");
        _validator.validate(_request, command, errors);
        assertTrue(errors.hasErrors());
        assertTrue(errors.hasFieldErrors("submitterEmailConfirm"));
        assertEquals(SubmitSchoolCommandValidator.ERROR_SUBMITTER_EMAIL_UNMATCHED,
            errors.getFieldError("submitterEmailConfirm").getDefaultMessage());
    }

    public void testSubmitterConnectionMissing() {
        SubmitSchoolCommand command = setupCommand();
        command.setSubmitterConnectionToSchool("");
        Errors errors = new BindException(command, "");
        _validator.validate(_request, command, errors);
        assertTrue(errors.hasErrors());
        assertTrue(errors.hasFieldErrors("submitterConnectionToSchool"));
        assertEquals(SubmitSchoolCommandValidator.ERROR_SUBMITTER_CONNECTION_TO_SCHOOL_MISSING,
            errors.getFieldError("submitterConnectionToSchool").getDefaultMessage());
    }

    public void testSchoolNameMissing() {
        SubmitSchoolCommand command = setupCommand();
        command.setSchoolName("");
        Errors errors = new BindException(command, "");
        _validator.validate(_request, command, errors);
        assertTrue(errors.hasErrors());
        assertTrue(errors.hasFieldErrors("schoolName"));
        assertEquals(SubmitSchoolCommandValidator.ERROR_SCHOOL_NAME_MISSING,
            errors.getFieldError("schoolName").getDefaultMessage());
    }

    public void testStreetAddressMissing() {
        SubmitSchoolCommand command = setupCommand();
        command.setStreetAddress("");
        Errors errors = new BindException(command, "");
        _validator.validate(_request, command, errors);
        assertTrue(errors.hasErrors());
        assertTrue(errors.hasFieldErrors("streetAddress"));
        assertEquals(SubmitSchoolCommandValidator.ERROR_STREET_ADDRESS_MISSING,
            errors.getFieldError("streetAddress").getDefaultMessage());
    }

    public void testCityMissing() {
        SubmitSchoolCommand command = setupCommand();
        command.setCity("");
        Errors errors = new BindException(command, "");
        _validator.validate(_request, command, errors);
        assertTrue(errors.hasErrors());
        assertTrue(errors.hasFieldErrors("city"));
        assertEquals(SubmitSchoolCommandValidator.ERROR_CITY_MISSING,
            errors.getFieldError("city").getDefaultMessage());
    }

    public void testStateMissing() {
        SubmitSchoolCommand command = setupCommand();
        command.setState(null);
        Errors errors = new BindException(command, "");
        _validator.validate(_request, command, errors);
        assertTrue(errors.hasErrors());
        assertTrue(errors.hasFieldErrors("state"));
        assertEquals(SubmitSchoolCommandValidator.ERROR_STATE_MISSING,
            errors.getFieldError("state").getDefaultMessage());
    }

    public void testZipCodeMissing() {
        SubmitSchoolCommand command = setupCommand();
        command.setZipCode("");
        Errors errors = new BindException(command, "");
        _validator.validate(_request, command, errors);
        assertTrue(errors.hasErrors());
        assertTrue(errors.hasFieldErrors("zipCode"));
        assertEquals(SubmitSchoolCommandValidator.ERROR_ZIP_CODE_MISSING,
            errors.getFieldError("zipCode").getDefaultMessage());
    }

    public void testZipCodeInvalid() {
        SubmitSchoolCommand command = setupCommand();
        command.setZipCode("1234");
        Errors errors = new BindException(command, "");
        _validator.validate(_request, command, errors);
        assertTrue(errors.hasErrors());
        assertTrue(errors.hasFieldErrors("zipCode"));
        assertEquals(SubmitSchoolCommandValidator.ERROR_ZIP_CODE_INVALID,
            errors.getFieldError("zipCode").getDefaultMessage());
    }

    public void testCountyMissing() {
        SubmitSchoolCommand command = setupCommand();
        command.setCounty("");
        Errors errors = new BindException(command, "");
        _validator.validate(_request, command, errors);
        assertTrue(errors.hasErrors());
        assertTrue(errors.hasFieldErrors("county"));
        assertEquals(SubmitSchoolCommandValidator.ERROR_COUNTY_MISSING,
            errors.getFieldError("county").getDefaultMessage());
    }

    public void testNumStudentsEnrolledMissing() {
        SubmitSchoolCommand command = setupCommand();
        command.setNumStudentsEnrolled("");
        Errors errors = new BindException(command, "");
        _validator.validate(_request, command, errors);
        assertTrue(errors.hasErrors());
        assertTrue(errors.hasFieldErrors("numStudentsEnrolled"));
        assertEquals(SubmitSchoolCommandValidator.ERROR_NUM_STUDENTS_ENROLLED_MISSING,
            errors.getFieldError("numStudentsEnrolled").getDefaultMessage());

        command.setNumStudentsEnrolled("asdf");
        errors = new BindException(command, "");
        _validator.validate(_request, command, errors);
        assertTrue(errors.hasErrors());
        assertTrue(errors.hasFieldErrors("numStudentsEnrolled"));
        assertEquals(SubmitSchoolCommandValidator.ERROR_NUM_STUDENTS_ENROLLED_MISSING,
            errors.getFieldError("numStudentsEnrolled").getDefaultMessage());
    }

    public void testPhoneNumberMissing() {
        SubmitSchoolCommand command = setupCommand();
        command.setPhoneNumber("");
        Errors errors = new BindException(command, "");
        _validator.validate(_request, command, errors);
        assertTrue(errors.hasErrors());
        assertTrue(errors.hasFieldErrors("phoneNumber"));
        assertEquals(SubmitSchoolCommandValidator.ERROR_PHONE_NUMBER_MISSING,
            errors.getFieldError("phoneNumber").getDefaultMessage());
    }

    public void testPhoneNumberInvalid() {
        SubmitSchoolCommand command = setupCommand();
        command.setPhoneNumber("1234567");
        Errors errors = new BindException(command, "");
        _validator.validate(_request, command, errors);
        assertTrue(errors.hasErrors());
        assertTrue(errors.hasFieldErrors("phoneNumber"));
        assertEquals(SubmitSchoolCommandValidator.ERROR_PHONE_NUMBER_INVALID,
            errors.getFieldError("phoneNumber").getDefaultMessage());
    }

    public void testFaxNumberInvalid() {
        SubmitSchoolCommand command = setupCommand();
        command.setFaxNumber("1234567");
        Errors errors = new BindException(command, "");
        _validator.validate(_request, command, errors);
        assertTrue(errors.hasErrors());
        assertTrue(errors.hasFieldErrors("faxNumber"));
        assertEquals(SubmitSchoolCommandValidator.ERROR_FAX_NUMBER_INVALID,
            errors.getFieldError("faxNumber").getDefaultMessage());
    }

    public void testSchoolWebSiteInvalid() {
        SubmitSchoolCommand command = setupCommand();
        command.setSchoolWebSite("asdf");
        Errors errors = new BindException(command, "");
        _validator.validate(_request, command, errors);
        assertTrue(errors.hasErrors());
        assertTrue(errors.hasFieldErrors("schoolWebSite"));
        assertEquals(SubmitSchoolCommandValidator.ERROR_SCHOOL_WEB_SITE_INVALID,
            errors.getFieldError("schoolWebSite").getDefaultMessage());
    }
}
