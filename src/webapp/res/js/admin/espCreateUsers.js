var GS = GS || {};
GS.form = GS.form || {};
GS.form.EspCreateUsersForm = function() {

    this.createUsers = function() {
        if (GS.form.EspCreateUsersForm.validateFields() === true) {
            var email = jQuery('#js_email').val();
            var state = jQuery('#js_state').val();
            var schoolId = jQuery('#js_schoolId').val();
            var firstName = jQuery('#js_firstName').val();
            var lastName = jQuery('#js_lastName').val();
            var jobTitle = jQuery('#js_jobTitle').val();
            jQuery.ajax({
                type: 'POST',
                url: "/admin/createEspUsers.page",
                data: {email:email, state:state, schoolId:schoolId, firstName:firstName, lastName:lastName, jobTitle:jobTitle},
                dataType: 'json',
                async: true
            }).done(function(data) {
                    alert("user created");
                });
        }
    };

    this.validateFields = function() {
        var isEmailValid = GS.form.EspCreateUsersForm.validateRequiredInput("js_email");
        var isStateValid = GS.form.EspCreateUsersForm.validateRequiredInput("js_state");
        var isSchoolIdValid = GS.form.EspCreateUsersForm.validateRequiredInput("js_schoolId");
        return isEmailValid && isStateValid && isSchoolIdValid;
    };

    this.validateRequiredInput = function(fieldName) {
        var field = jQuery('#' + fieldName);
        var fieldVal = field.val();
        var fieldError = jQuery('#' + fieldName + '_error');
        var isValid = true;
        if (field === undefined) {
            isValid = false;
        } else {
            fieldVal = jQuery.trim(fieldVal);
            if (fieldVal === '') {
                fieldError.show();
                isValid = false;
            }
        }

        return isValid;
    };
};

GS.form.EspCreateUsersForm = new GS.form.EspCreateUsersForm();

jQuery(function() {
    jQuery('#js_submit').click(
        GS.form.EspCreateUsersForm.createUsers
    );
});