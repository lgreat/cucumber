GS = GS || {};
GS.form = GS.form || {};
GS.form.EspForm = function() {

    //Handle State drop down change.
    this.stateChange = function(stateSelect, citySelect) {
        var params = {
            state: stateSelect.val(),
            type: 'city',
            notListedOption: '2. Choose city'
        };

        jQuery('#js_school').html('<option value="0">3. Choose school</option>');
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
        var parentState = jQuery('#js_stateAdd').val();
        var parentCity = citySelect.val();
        var school = jQuery('#js_school');

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

    //Checks if the
    //i)email entered is valid
    //ii)email entered belongs to Existing user with no ESP membership OR already a ESP member
    //(iii)email was not found.
    this.checkUser = function(email) {

        if (email !== "" && email !== undefined) {
            jQuery.ajax({
                type: 'GET',
                url: '/school/esp/checkEspUser.page',
                data: {email:email},
                dataType: 'json',
                async: true
            }).done(function(data) {

                    if (data.invalidEmail !== "" && data.invalidEmail !== undefined) {
                        jQuery('#js_invalidEmail').show();


                    } else if (data.isUserEmailValidated === true) {
                        jQuery('#js_registeredPasswordDiv').show();
                        bindLoginSubmit();

                    } else if (data.isUserMember === true || data.isUserESPMember === true) {

                        //User already exists.However all the required fields are not filled in.Therefore collect them.
                        if (data.fieldsToCollect !== "" && data.fieldsToCollect !== undefined) {
                            var fields = data.fieldsToCollect.split(",");
                            for (var i = 0; i < fields.length; i++) {
                                jQuery('#js_' + fields[i] + 'Div').show();
                            }
                        }

                        //User is already ESP member.Therefore show a message.
                        if (data.isUserESPMember === true) {
                            jQuery('#js_userESPMember').show();
                        }

                        jQuery('#js_regPanel').show();
                        bindFormSubmit();

                    } else if (data.userNotFound === true) {

                        jQuery('#js_regPanel').show();
                        jQuery('#js_firstNameDiv').show();
                        jQuery('#js_lastNameDiv').show();
                        jQuery('#js_screenNameDiv').show();
                        jQuery('#js_passwordDiv').show();
                        jQuery('#js_confirmPasswordDiv').show();
                        bindFormSubmit();
                    }
                });
        }
        //This should never submit the form.Hence always return false.
        return false;
    };

    this.checkUserPassword = function() {
        var email = jQuery('#js_email').val();
        var password = jQuery('#js_registeredPassword').val();
        var rval = true;
        if (password !== '' && password !== undefined) {
            jQuery.ajax({
                type: 'GET',
                url: '/school/esp/checkUserPassword.page',
                data: {email:email, registeredPassword:password},
                dataType: 'json',
                async: false
            }).done(function(data) {
                    if (data.incorrectPassword !== '' && data.incorrectPassword !== undefined) {
                        jQuery('#js_registeredPasswordIncorrect').show();
                        rval = false;
                    }
                });
        } else {
            rval = false;
            jQuery('#js_registeredPasswordEmpty').show();
        }
        return rval;
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

    this.validateLastName = function() {
        var lName = jQuery('#js_lastName').val();
        var rval = true;
        jQuery.ajax({
            type: 'GET',
            url: '/community/registrationValidationAjax.page',
            data: {lastName:lName, field:'lastName'},
            dataType: 'json',
            async: false
        }).done(function(data) {
                rval = GS.form.espForm.handleValidationResponse('.js_lastName', 'lastName', data);
            });

        return rval;
    };

    this.validateScreenName = function() {
        var screenName = jQuery('#js_screenName').val();
        var rval = true;

        jQuery.ajax({
            type: 'GET',
            url: '/community/registrationValidationAjax.page',
            data: {screenName:screenName, email:jQuery('#js_email').val(), field:'username'},
            dataType: 'json',
            async: false
        }).done(function(data) {
                rval = GS.form.espForm.handleValidationResponse('.js_screenName', 'screenName', data);
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
        var jobTitle = jQuery('#js_jobTitle').val();
        var fieldError = jQuery('.js_jobTitle.invalid');
        var rval = true;

        fieldError.hide();

        if (jobTitle === "" || jobTitle === undefined) {
            fieldError.show();
            rval = false;
        }
        return rval;

    };

    this.validateState = function() {
        var state = jQuery('#js_stateAdd').val();
        var fieldError = jQuery('.js_state.invalid');
        var rval = true;

        fieldError.hide();
        if (state === "" || state === undefined ) {
            fieldError.show();
            rval = false;
        }
        return rval;
    };

    this.validateSchool = function() {
        var schoolId = jQuery('#js_school').val();
        var fieldError = jQuery('.js_school.invalid');
        var rval = true;

        fieldError.hide();

        if (schoolId === "" || schoolId === undefined || schoolId === "-1" || schoolId === "0") {
            fieldError.show();
            rval = false;
        }
        return rval;
    };

    this.validateStateSchoolUserUnique = function() {
        var schoolId = jQuery('#js_school').val();
        var state = jQuery('#js_stateAdd').val();
        var email = jQuery('#js_email').val();
        var rval = true;

        jQuery.ajax({
            type: 'GET',
            url: '/school/esp/checkStateSchoolUserUnique.page',
            data: {schoolId:schoolId, state:state,email:email},
            dataType: 'json',
            async: false
        }).done(function(data) {
                if (data.isUnique !== true) {
                    jQuery('#js_userESPMember').hide();
                    jQuery('#js_uniqueError').show();
                    rval = false;
                }
            });

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

    jQuery('#js_stateAdd').change(function() {
        GS.form.espForm.stateChange(jQuery(this), jQuery('#js_citySelect'));
    });

    jQuery('#js_citySelect').change(function() {
        GS.form.espForm.emailCityChange(jQuery(this));
    });

    jQuery('#js_firstName').blur(GS.form.espForm.validateFirstName);
    jQuery('#js_lastName').blur(GS.form.espForm.validateLastName);
    jQuery('#js_screenName').blur(GS.form.espForm.validateScreenName);
    jQuery('#js_password').blur(GS.form.espForm.validatePassword);
    jQuery('#js_confirmPassword').blur(GS.form.espForm.validateConfirmPassword);

    jQuery('#js_jobTitle').change(GS.form.espForm.validateJobTitle);
    jQuery('#js_school').change(GS.form.espForm.validateSchool);

    var regPanel = jQuery('#js_regPanel');

    if (regPanel.is(':visible')) {
        bindFormSubmit();

    } else if (!regPanel.is(':visible')) {
        bindEmailSubmit();
    }

    jQuery('#js_email').keydown(function() {
        var regPanel = jQuery('#js_regPanel');
        //TODO should the form be cleared when reg panel is hidden?
        hideEmailErrors();
        if (regPanel.is(':visible')) {
            regPanel.hide();
            jQuery('#js_firstNameDiv').hide();
            jQuery('#js_lastNameDiv').hide();
            jQuery('#js_screenNameDiv').hide();
            jQuery('#js_passwordDiv').hide();
            jQuery('#js_confirmPasswordDiv').hide();
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

        //unbind the existing click handler.
        unbindSubmitHandler();

        //Bind the new click handler which just validates user/email.
        jQuery('#js_submit').click(function() {
            var email = jQuery('#js_email').val().trim();
            GS.form.espForm.checkUser(email);
            //This should never submit the form.Hence always return false.
            return false;
        });

    }
}

function bindLoginSubmit() {
    var passwordDiv = jQuery('#js_registeredPasswordDiv');
    var regPanel = jQuery('#js_regPanel');
    var shouldSubmitForm = false;

    //Additional check that if the registration panel is not visible and the password field is visible then form should submit.
    if (!regPanel.is(':visible') && passwordDiv.is(':visible')) {

         //unbind the existing click handler.
        unbindSubmitHandler();

         //Bind the new click handler
        jQuery('#js_submit').click(function() {
            shouldSubmitForm = GS.form.espForm.checkUserPassword();
            return shouldSubmitForm;
        });

    }
}

function bindFormSubmit() {
    var regPanel = jQuery('#js_regPanel');

    //If the registration panel is visible then the form should submit.
    if (regPanel.is(':visible')) {

        //unbind the existing click handler.
        unbindSubmitHandler();

        //Bind the new click handler which validates all the visible fields and submits the form if everything is valid.
        jQuery('#js_submit').click(function() {

            //check if the following fields are visible.
            var firstNameDivVisible = jQuery('#js_firstName').is(':visible');
            var lastNameDivVisible = jQuery('#js_lastName').is(':visible');
            var screenNameDivVisible = jQuery('#js_screenName').is(':visible');
            var passwordDivVisible = jQuery('#js_password').is(':visible');
            var confirmPasswordDivVisible = jQuery('#js_confirmPassword').is(':visible');

            //If they are visible then validate else return true.
            var isFirstNameValid = firstNameDivVisible ? GS.form.espForm.validateFirstName() : true;
            var isLastNameValid = lastNameDivVisible ? GS.form.espForm.validateLastName() : true;
            var isScreenNameValid = screenNameDivVisible ? GS.form.espForm.validateScreenName() : true;
            var isPasswordValid = passwordDivVisible ? GS.form.espForm.validatePassword() : true;
            var isConfirmPasswordValid = confirmPasswordDivVisible ? GS.form.espForm.validateConfirmPassword() : true;

            //The school, state and the job title fields are always present.Hence always validate them.
            var isJobTitleValid = GS.form.espForm.validateJobTitle();
            var isStateValid = GS.form.espForm.validateState();
            var isSchoolValid = GS.form.espForm.validateSchool();
            var isUniqueEsp = GS.form.espForm.validateStateSchoolUserUnique();

            return isFirstNameValid && isLastNameValid && isScreenNameValid && isPasswordValid && isConfirmPasswordValid
                && isJobTitleValid && isStateValid && isSchoolValid && isUniqueEsp;
        });
    }
}

function hideEmailErrors() {
    jQuery('#js_userESPMember').hide();
    jQuery('#js_invalidEmail').hide();
}

