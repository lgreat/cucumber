var GS = GS || {};
GS.form = GS.form || {};
GS.form.EspForm = function() {

    //Checks the various states of the user and displays messages accordingly.
    this.validateUserState = function() {
        var emailField = jQuery('#js_email');
        var email = jQuery.trim(emailField.val());
        jQuery('.js_emailErr').hide();
        GS.form.espForm.removeWarningClassFromElem(emailField);
        var dfd = jQuery.Deferred();

        if (email !== "" && email !== undefined) {
            jQuery.ajax({
                type: 'GET',
                url: '/official-school-profile/checkUserState.page',
                data: {email:email},
                dataType: 'json',
                async: true
            }).done(
                function(data) {
                    var isValid = GS.form.espForm.handleEmailErrors(data, email, emailField);
                    if (isValid === false) {
                        dfd.reject();
                    } else {
                        dfd.resolve();
                    }
                }
            ).fail(function() {
                    dfd.reject();
                });
        } else {
            jQuery('#js_invalidEmail').show();
            GS.form.espForm.addWarningClassToElem(emailField);
            dfd.reject();
        }
        return dfd.promise();
    };

    //Handles the logic to allow the signin's to go through or display an error.
    //These conditions are complicated, refer to the flow charts attached to GS-13363.
    this.handleEmailErrors = function(data,email,emailField) {
        var isValid = false;
        if (data.isNewUser == true) {
            GS.form.espForm.showEmailError("Whoops! You are not currently registered as an administrator.  <a href='/official-school-profile/register.page?email=" + encodeURIComponent(email) + "'>Click here to register.</a>", emailField);
        } else if (data.isUserESPPreApproved === true ) {
            GSType.hover.espPreApprovalEmail.setEmail(jQuery('#js_email').val());
            GSType.hover.espPreApprovalEmail.setSchoolName(data.schoolName);
            var onclickStr = "'GSType.hover.espPreApprovalEmail.show(); return false;'";
            GS.form.espForm.showEmailError("You have been pre-approved for an account but must verify your email. <a href='#' onclick=" + onclickStr + ">Please verify email.</a>", emailField);
        } else if ((data.isUserApprovedESPMember === true || data.isUserAwaitingESPMembership === true) && data.isUserEmailValidated !== true) {
            // users who have been approved but haven't followed through by clicking through the link in email
            GSType.hover.emailNotValidated.setEmail(email);
            var onclickStr = "'GSType.hover.emailNotValidated.show(); return false;'";
            GS.form.espForm.showEmailError("Please . <a href='#' onclick=" + onclickStr + ">verify your email</a>.", emailField);
        } else {
            isValid = true;
        }
        return isValid;
    };

    this.showEmailError = function(errMsg, emailField) {
        jQuery('#js_emailError').html('<div class="media"><span class="iconx16 i-16-alert img mrs"></span><div class="bd">' + errMsg + '</div></div>');
        GS.form.espForm.addWarningClassToElem(emailField);
        jQuery('#js_emailError').show();
    };


    this.validateRequiredFields = function(fieldName) {
        var field = jQuery('#js_' + fieldName);
        var fieldVal = field.val();
        var fieldError = jQuery('.js_' + fieldName + '.invalid');
        var dfd = jQuery.Deferred();

        fieldError.hide();
        GS.form.espForm.removeWarningClassFromElem(field);

        if (fieldVal === '' || fieldVal === undefined || fieldVal === '-1' || fieldVal === '0' || fieldVal === 'My city is not listed' || fieldVal === 'Loading...') {
            fieldError.show();
            GS.form.espForm.addWarningClassToElem(field);
            dfd.reject();
        } else {
            dfd.resolve();
        }

        return dfd.promise();
    };

    this.handleValidationResponse = function(fieldSelector, fieldName, data, elem) {
        var fieldError = jQuery(fieldSelector + '.invalid');
        var fieldValid = jQuery(fieldSelector + '.success');

        GS.form.espForm.removeWarningClassFromElem(elem);
        fieldError.hide();
        fieldValid.hide();

        if (data && data[fieldName]) {
            GS.form.espForm.addWarningClassToElem(elem);
            fieldError.find('.bd').html(data[fieldName]); // set error message
            fieldError.show();
            return false;
        } else {
            fieldValid.show();
            return true;
        }
    };

    this.addWarningClassToElem = function(elem) {
        elem.addClass("warning");
    };

    this.removeWarningClassFromElem = function(elem) {
        elem.removeClass("warning");
    };

    this.registrationSubmit = function() {
        jQuery.when(
                GS.form.espForm.validateUserState(),
                GS.form.espForm.validateRequiredFields('password')
            ).done(
            function() {
                //submit the form if all validations pass.
                document.getElementById('espRegistrationCommand').submit();
            }
        ).fail(
            function() {
                // Error messages are already displayed as part of ajax validations.
            }
        )
        return false;
    };

    this.hideAllErrors = function() {
        jQuery('.error').hide();
    };
};

GS.form.espForm = new GS.form.EspForm();

jQuery(function() {
    jQuery('#js_email').blur(
        GS.form.espForm.validateUserState
    );

    jQuery('#js_password').blur(function() {
        GS.form.espForm.validateRequiredFields('password');
    });

    //Bind the new click handler which validates all the visible fields and submits the form if everything is valid.
    jQuery('#js_submit').click(
        GS.form.espForm.registrationSubmit
    );

});