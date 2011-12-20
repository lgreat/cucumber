GS = GS || {};
GS.form = GS.form || {};
GS.form.EspForm = function() {

    //Handle State drop down change.
    this.stateChange = function(stateSelect, citySelect, updateSchoolSelect) {

        var params = {
            state: stateSelect.val(),
            type: 'city',
            notListedOption: '2. Choose city'
        };

        if (updateSchoolSelect) {
            jQuery('#jq-school').html('<option value="0">3. Choose school</option>');
        }
        citySelect.html('<option value="0">Loading ...</option>');

        jQuery.ajax({
            type: 'GET',
            url: '/util/ajax/ajaxCity.page',
            data: params,
            dataType: 'text',
            async: false
        }).done(function(data) {
                citySelect.html(data.replace('</select>', ''));
            });
    };

    //Handle City drop down change.
    this.emailCityChange = function(citySelect) {
        var parentState = jQuery('#jq-stateAdd').val();
        var parentCity = citySelect.val();
        var school = jQuery('#jq-school');

        var params = {
            state: parentState,
            city: parentCity,
            notListedOption: '3. Choose school'
        };

        school.html('<option value="0">Loading ...</option>');

        jQuery.ajax({
            type: 'GET',
            url: '/util/ajax/ajaxCity.page',
            data: params,
            dataType: 'text',
            async: false
        }).done(function(data) {
                school.html(data.replace('</select>', ''));
            });
    };

    //Checks if the email entered is already a
    // i) Existing user with no ESP membership
    // (ii)Already a ESP member
    // (iii) New User
    this.checkUser = function(email) {
        if (email !== "" && email !== undefined) {
            jQuery.ajax({
                type: 'GET',
                url: '/school/esp/checkEspUser.page',
                data: {email:email},
                dataType: 'json',
                async: false
            }).done(function(data) {
                    if (data.fieldsToCollect !== "" && data.fieldsToCollect !== undefined) {
                        //The user is already a GS member.However he does not have all the data required for ESP.Therefore collect that.
                        var fields = data.fieldsToCollect.split(",");
                        for (var i = 0; i < fields.length; i++) {
                            jQuery('#js_' + fields[i] + 'Div').show();
                        }
                        jQuery('#js_regPanel').show();
//                        unbindSubmitHandler();
                        bindFormSubmit();

                    } else if (data.userAlreadyESPMember === true) {
                        jQuery('#js_userAlreadyMember').show();

                    } else if (data.userNotFound === true) {
                        jQuery('#js_regPanel').show();
                        jQuery('#js_firstNameDiv').show();
                        jQuery('#js_lastNameDiv').show();
                        jQuery('#js_userNameDiv').show();
                        jQuery('#js_passwordDiv').show();
                        jQuery('#js_confirmPasswordDiv').show();
//                        unbindSubmitHandler();
                        bindFormSubmit();
                    }
                });
        }
        //This should never submit the form.Hence always return false.
        return false;
    };

    this.validateEmail = function(email) {
        var validEmail = true;
        if (email === "" || email === undefined) {
            validEmail = false;
        } else {
            jQuery.ajax({
                type: 'GET',
                url: '/community/registrationAjax.page',
                data: {email:email},
                dataType: 'text',
                async: false
            }).done(function(data) {
                    if (data === "invalid") {
                        validEmail = false;
                    }
                });
        }
        if (!validEmail) {
            jQuery('#js_invalidEmail').show();
        }
        return validEmail;
    };

    this.validateFirstName = function() {
        var fName = jQuery('#js_firstName').val();
        var rval = true;
        jQuery.ajax({
            type: 'GET',
            url: '/community/registrationValidationAjax.page',
            data: {firstName:fName, field:'firstName'},
            dataType: 'json',
            async: false
        }).done(function(data) {
                rval = GS.form.espForm.handleValidationResponse('.js_firstName', 'firstName', data);
            });

        return rval;
    };


    //TODO validate last Name ?
    this.validateLastName = function() {
        var lName = jQuery('#js_lastName').val();
        var rval = true;
        if (lName === "" || lName === undefined) {
            rval = false;
        }
        return rval;
    };

    this.validateUsername = function() {
        var userName = jQuery('#js_userName').val();
        var rval = true;

        jQuery.ajax({
            type: 'GET',
            url: '/community/registrationValidationAjax.page',
            data: {screenName:userName, email:jQuery('#js_email').val(), field:'username'},
            dataType: 'json',
            async: false
        }).done(function(data) {
                rval = GS.form.espForm.handleValidationResponse('.js_userName', 'screenName', data);
            });

        return rval;
    };

    this.validatePassword = function() {
        var password = jQuery('#js_password').val();
        var confirmPassword = jQuery('#js_confirmPassword').val();
        var rval = true;
        jQuery.ajax({
            type: 'GET',
            url: '/community/registrationValidationAjax.page',
            data: {password:password, confirmPassword:confirmPassword, field:'password'},
            dataType: 'json',
            async: false
        }).done(function(data) {
                rval = GS.form.espForm.handleValidationResponse('.js_password', 'password', data);
            });

        rval = rval && GS.form.espForm.validateConfirmPassword();
        return rval;
    };

    this.validateConfirmPassword = function() {
        var password = jQuery('#js_password').val();
        var confirmPassword = jQuery('#js_confirmPassword').val();
        var rval = true;
        jQuery.ajax({
            type: 'GET',
            url: '/community/registrationValidationAjax.page',
            data: {password:password, confirmPassword:confirmPassword, field:'confirmPassword'},
            dataType: 'json',
            async: false
        }).done(function(data) {
                rval = GS.form.espForm.handleValidationResponse('.js_confirmPassword', 'confirmPassword', data);
            });

        return rval;
    };

    this.validateJobTitle = function() {
        var schoolId = jQuery('#js_jobTitle').val();
        var rval = true;
        var fieldError = jQuery('.js_jobTitle.invalid');
        fieldError.hide();
        if (schoolId === "" || schoolId === undefined) {
            fieldError.show();
            rval = false;
        }
        return rval;

    };

    this.validateSchool = function() {
        var schoolId = jQuery('#jq-school').val();
        var rval = true;
        var fieldError = jQuery('.js_school.invalid');
        fieldError.hide();
        if (schoolId === "" || schoolId === undefined || schoolId === "-1" || schoolId === "0") {
            fieldError.show();
            rval = false;
        }
        return rval;
    };


    this.handleValidationResponse = function(fieldSelector, fieldName, data) {
        var fieldError = jQuery(fieldSelector + '.invalid');
        var fieldValid = jQuery(fieldSelector + '.valid');
        fieldError.hide();
        fieldValid.hide();
        if (data && data[fieldName]) {
            fieldError.html(data[fieldName]);
            fieldError.show();
            return false;
        } else {
            fieldValid.show();
            return true;
        }
    };
};

jQuery(function() {
    GS.form.espForm = GS.form.espForm || new GS.form.EspForm();

    jQuery('#jq-stateAdd').change(function() {
        GS.form.espForm.stateChange(jQuery(this), jQuery('#jq-citySelect'), true);
    });

    jQuery('#jq-citySelect').change(function() {
        GS.form.espForm.emailCityChange(jQuery(this));
    });

    jQuery('#js_firstName').blur(GS.form.espForm.validateFirstName);
    jQuery('#js_userName').blur(GS.form.espForm.validateUsername);
    jQuery('#js_password').blur(GS.form.espForm.validatePassword);
    jQuery('#js_confirmPassword').blur(GS.form.espForm.validateConfirmPassword);

    jQuery('#js_jobTitle').change(function() {
        GS.form.espForm.validateJobTitle();
    });

    jQuery('#jq-school').change(function() {
        GS.form.espForm.validateSchool();
    });

    bindEmailSubmit();

    jQuery('#js_email').keydown(function(email) {
        var regPanel = jQuery('#js_regPanel');
        hideEmailErrors();
        if (regPanel.is(':visible')) {
            regPanel.hide();
//            unbindSubmitHandler();
            bindEmailSubmit();
        }
    });

});

function unbindSubmitHandler() {
    jQuery('#js_submit').unbind('click');
}

function bindEmailSubmit() {
    var regPanel = jQuery('#js_regPanel');
    //Additional check that if the registration panel is not visible then the form should not submit.
    if (!regPanel.is(':visible')) {
        unbindSubmitHandler();
        jQuery('#js_submit').click(function(email) {
            var email = jQuery('#js_email').val().trim();
            if (GS.form.espForm.validateEmail(email)) {
                GS.form.espForm.checkUser(email);
                //This should never submit the form.Hence always return false.
                return false;
            }
            //This should never submit the form.Hence always return false.
            return false;
        });
    }
}

function bindFormSubmit() {
    var regPanel = jQuery('#js_regPanel');
    //If the registration panel is visible then the form should submit.
    if (regPanel.is(':visible')) {
        unbindSubmitHandler();
        jQuery('#js_submit').click(function() {
            var isFirstNameValid = GS.form.espForm.validateFirstName();
            var isLastNameValid = GS.form.espForm.validateLastName();
            var isUserNameValid = GS.form.espForm.validateUsername();
            var isPasswordValid = GS.form.espForm.validatePassword();
            var isJobTitleValid = GS.form.espForm.validateJobTitle();
            var isSchoolValid = GS.form.espForm.validateSchool();

            var isAllValid = isFirstNameValid && isLastNameValid && isUserNameValid && isPasswordValid && isJobTitleValid && isSchoolValid;
            return isAllValid;
        });
    }
}

function hideEmailErrors() {
    jQuery('#js_userAlreadyMember').hide();
    jQuery('#js_invalidEmail').hide();

}

