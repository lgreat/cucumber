package gs.web.community.registration;


public class RegistrationTestUtils {
    public static UserRegistrationCommand validUserRegistrationCommand() {
        UserRegistrationCommand command = new UserRegistrationCommand();
        return command
            .email("programmers@greatschools.org")
            .password("password")
            .confirmPassword("password")
            .terms(true)
            .how("test");
    }
}
