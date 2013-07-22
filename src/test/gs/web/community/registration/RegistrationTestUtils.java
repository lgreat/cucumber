package gs.web.community.registration;


import gs.data.community.User;

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

    public static User facebookUser() {
        User user = new User();
        user.setId(1);
        user.setHow("facebook");
        user.setFacebookId("facebookId");
        return user;
    }
}
