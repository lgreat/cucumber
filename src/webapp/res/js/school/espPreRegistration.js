GS.form.espForm.registrationSubmit = function() {
    jQuery.when(
        GS.form.espForm.validateFields('firstName'),
        GS.form.espForm.validateFields('lastName'),
        GS.form.espForm.validateFields('screenName', {email:jQuery('#js_email').val(), field:'username'}),
        GS.form.espForm.validateFields('password', {confirmPassword:jQuery('#js_confirmPassword').val()}),
        GS.form.espForm.validateFields('confirmPassword', {password:jQuery('#js_password').val()}),
        GS.form.espForm.validateRequiredFields('jobTitle')
    ).done(
        function() {
            //submit the form if all validations pass.
            document.getElementById('espRegistrationCommand').submit();
        }
    );
    return false;
};



