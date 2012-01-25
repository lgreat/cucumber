var GS = GS || {};
GS.form = GS.form || {};
GS.form.EspForm = function() {

    //Handle State drop down change.
    this.stateChange = function() {

        var stateSelect = jQuery('#js_stateAdd');
        var citySelect = jQuery('#js_city');
        var schoolSelect = jQuery('#js_school');

        citySelect.html('<option value="0">Loading ...</option>');
        schoolSelect.html('<option value="0">- Choose school -</option>');

        jQuery.ajax({
            type: 'GET',
            url: "/community/registrationAjax.page",
            data: {state: stateSelect.val(),type: 'city',format: 'json'},
            dataType: 'json',
            async: true
        }).done(function(data) {
                GS.form.espForm.parseCities(data);
            });

    };

    //Handle City drop down change.
    this.cityChange = function() {
        var stateSelect = jQuery('#js_stateAdd');
        var citySelect = jQuery('#js_city');

        var state = stateSelect.val();
        var city = citySelect.val();

        if (state !== '' && city !== '- Choose city -' && city !== 'My city is not listed') {
            jQuery('#js_school').html("<option>Loading...</option>");
            jQuery.ajax({
                type: 'GET',
                url: "/community/registration2Ajax.page",
                data: {state:state, city:city, format:'json', type:'school'},
                dataType: 'json',
                async: true
            }).done(function(data) {
                    GS.form.espForm.parseSchools(data);
                });
        }

    };

    this.parseCities = function(data) {
        var citySelect = jQuery('#js_city');
        if (data.cities) {
            citySelect.empty();
            for (var x = 0; x < data.cities.length; x++) {
                var city = data.cities[x];
                if (city.name) {
                    citySelect.append("<option value=\"" + city.name + "\">" + city.name + "</option>");
                }
            }
        }
    };

    this.parseSchools = function(data) {
        var schoolSelect = jQuery('#js_school');
        if (data.schools) {
            schoolSelect.empty();
            for (var x = 0; x < data.schools.length; x++) {
                var school = data.schools[x];
                if (school.name && school.id) {
                    schoolSelect.append("<option value=\"" + school.id + "\">" + school.name + "</option>");
                }
            }
        }
    };

    //Checks for the following cases and displays the form accordingly
    //i)email entered is valid
    //ii)email entered belongs to an existing user who is email validated ,with no ESP membership OR with ESP membership
    //iii)email entered belongs to a provisional GS user.
    //iv)email was not found.
    this.checkUser = function() {
        var email = jQuery('#js_email').val().trim();

        if (email !== "" && email !== undefined) {
            email = email.trim();
            jQuery.ajax({
                type: 'GET',
                url: '/school/esp/checkEspUser.page',
                data: {email:email},
                dataType: 'json',
                async: true
            }).done(function(data) {

                    if (data.invalidEmail !== "" && data.invalidEmail !== undefined) {
                        jQuery('#js_invalidEmail').show();

                    } else if (data.isUserEmailValidated === true || data.isUserApprovedESPMember === true) {
                        jQuery('#js_registeredPasswordDiv').show();
                        GS.form.espForm.bindLoginSubmit();

                    } else if (data.isUserGSMember === true) {

                        //User already exists.However all the required fields are not filled in.Therefore collect them.
                        if (data.fieldsToCollect !== "" && data.fieldsToCollect !== undefined) {
                            var fields = data.fieldsToCollect.split(",");
                            for (var i = 0; i < fields.length; i++) {
                                jQuery('#js_' + fields[i] + 'Div').show();
                            }
                        }

                        jQuery('#js_regPanel').show();
                        GS.form.espForm.bindRegistrationSubmit();

                    } else if (data.userNotFound === true) {

                        jQuery('#js_regPanel').show();
                        jQuery('#js_firstNameDiv').show();
                        jQuery('#js_lastNameDiv').show();
                        jQuery('#js_screenNameDiv').show();
                        jQuery('#js_passwordDiv').show();
                        jQuery('#js_confirmPasswordDiv').show();
                        GS.form.espForm.bindRegistrationSubmit();
                    }
                });
        } else {
            jQuery('#js_invalidEmail').show();
        }

        //This should never submit the form.Hence always return false.
        return false;
    };

    this.matchUserPassword = function() {
        var email = jQuery('#js_email').val();
        var password = jQuery('#js_registeredPassword').val();
        var dfd = jQuery.Deferred();
        var pwdIncorrectErr = jQuery('#js_registeredPasswordIncorrect');
        var pwdEmptyErr = jQuery('#js_registeredPasswordEmpty');
        pwdIncorrectErr.hide();
        pwdEmptyErr.hide();

        if (password !== '' && password !== undefined) {
            jQuery.ajax({
                type: 'GET',
                url: '/school/esp/matchUserPassword.page',
                data: {email:email, registeredPassword:password},
                dataType: 'json',
                async: true
            }).done(
                function(data) {
                    if (data.matchesPassword !== true) {
                        pwdIncorrectErr.show();
                        dfd.reject();
                    } else {
                        dfd.resolve();
                    }
                }
            ).fail(function(data) {
                    dfd.reject();
                });
        } else {
            pwdEmptyErr.show();
            dfd.reject();
        }
        return dfd.promise();
    };

    this.validateRequiredFields = function(fieldName) {
        var fieldVal = jQuery('#js_' + fieldName).val();
        var fieldError = jQuery('.js_' + fieldName + '.invalid');
        var dfd = jQuery.Deferred();

        fieldError.hide();

        if (fieldVal === "" || fieldVal === undefined) {
            fieldError.show();
            dfd.reject();
        } else {
            dfd.resolve();
        }

        return dfd.promise();
    };

    this.validateSchool = function() {
        var schoolId = jQuery('#js_school').val();
        var fieldError = jQuery('.js_school.invalid');
        var dfd = jQuery.Deferred();

        fieldError.hide();

        if (schoolId === "" || schoolId === undefined || schoolId === "-1" || schoolId === "0") {
            fieldError.show();
            dfd.reject();
        } else {
            dfd.resolve();
        }

        return dfd.promise();
    };

    this.validateStateSchoolUserUnique = function() {
        var schoolId = jQuery('#js_school').val();
        var state = jQuery('#js_stateAdd').val();
        var email = jQuery('#js_email').val();
        var fieldError = jQuery('#js_uniqueError');
        var dfd = jQuery.Deferred();

        fieldError.hide();

        jQuery.ajax({
            type: 'GET',
            url: '/school/esp/checkStateSchoolUserUnique.page',
            data: {schoolId:schoolId, state:state,email:email},
            dataType: 'json',
            async: true
        }).done(
            function(data) {
                if (data.isUnique !== true) {
                    fieldError.show();
                    dfd.reject();
                } else {
                    dfd.resolve();
                }
            }
        ).fail(function() {
                dfd.reject();
            }
        );
        return dfd.promise();
    };

    this.validateFields = function(fieldName, ajaxParams) {
        var elem = jQuery('#js_' + fieldName);
        var fieldName = fieldName;
        var fieldVal = elem.val();
        var isFieldVisible = elem.is(':visible');
        var dfd = jQuery.Deferred();
        var dataParams = {field:fieldName};
        dataParams[fieldName] = fieldVal;
        jQuery.extend(dataParams, ajaxParams);

        if (isFieldVisible) {
            jQuery.ajax({
                type: 'GET',
                url: '/community/registrationValidationAjax.page',
                data:dataParams,
                dataType: 'json',
                async: true
            }).done(
                function(data) {
                    var rval = GS.form.espForm.handleValidationResponse('.js_' + fieldName, fieldName, data);
                    if (rval) {
                        dfd.resolve();
                    } else {
                        dfd.reject();
                    }
                }
            ).fail(function() {
                    dfd.reject();
                }
            );
        } else {
            dfd.resolve();
        }
        return dfd.promise();
    };

    this.handleValidationResponse = function(fieldSelector, fieldName, data) {
        var fieldError = jQuery(fieldSelector + '.invalid');
        var fieldValid = jQuery(fieldSelector + '.success');

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

    this.loginSubmit = function() {
        GS.form.espForm.matchUserPassword(
        ).done(
            function() {
                //submit the form if the password is correct.
                document.getElementById('espRegistrationCommand').submit();
            }
        ).fail(
            function() {
                // Error messages are already displayed as part of ajax validations.
            }
        )
        return false;
    };

    this.registrationSubmit = function() {
        jQuery.when(
            GS.form.espForm.validateFields('firstName'),
            GS.form.espForm.validateFields('lastName'),
            GS.form.espForm.validateFields('screenName', {email:jQuery('#js_email').val(), field:'username'}),
            GS.form.espForm.validateFields('password', {confirmPassword:jQuery('#js_confirmPassword').val()}),
            GS.form.espForm.validateFields('confirmPassword', {password:jQuery('#js_password').val()}),
            GS.form.espForm.validateRequiredFields('jobTitle'),
            GS.form.espForm.validateRequiredFields('stateAdd'),
            GS.form.espForm.validateSchool(),
            GS.form.espForm.validateStateSchoolUserUnique()
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

    this.unbindSubmitHandler = function () {
        jQuery('#js_submit').unbind('click');
    };

    this.bindEmailSubmit = function() {
        var regPanel = jQuery('#js_regPanel');

        //Additional check that if the registration panel is not visible then the form should not submit.
        if (!regPanel.is(':visible')) {

            //unbind the existing click handler.
            GS.form.espForm.unbindSubmitHandler();

            //change the text of the button.
            var submitBtn = jQuery('#js_submit');
            submitBtn.val('Continue \xBB');

            //Bind the new click handler which just validates user/email.
            submitBtn.click(
                GS.form.espForm.checkUser
            );
        }
    };

    this.bindLoginSubmit = function() {
        var passwordDiv = jQuery('#js_registeredPasswordDiv');
        var regPanel = jQuery('#js_regPanel');

        //Additional check that if the registration panel is not visible and the password field is visible then form should submit.
        if (!regPanel.is(':visible') && passwordDiv.is(':visible')) {

            //unbind the existing click handler.
            GS.form.espForm.unbindSubmitHandler();

            //change the text of the button.
            var submitBtn = jQuery('#js_submit');
            submitBtn.val('Sign In');

            //Bind the new click handler which logs in the user if the correct password is entered.
            submitBtn.click(
                GS.form.espForm.loginSubmit
            );
        }
    };

    this.bindRegistrationSubmit = function() {
        var regPanel = jQuery('#js_regPanel');

        //If the registration panel is visible then the form should submit.
        if (regPanel.is(':visible')) {

            //unbind the existing click handler.
            GS.form.espForm.unbindSubmitHandler();

            //change the text of the button.
            var submitBtn = jQuery('#js_submit');
            submitBtn.val('Submit request');

            //Bind the new click handler which validates all the visible fields and submits the form if everything is valid.
            submitBtn.click(
                GS.form.espForm.registrationSubmit
            );
        }
    };

    this.hideAllErrors = function() {
        jQuery('.error').hide();
    };
};

GS.form.espForm = new GS.form.EspForm();

jQuery(function() {

    jQuery('#js_stateAdd').change(function() {
        GS.form.espForm.stateChange();
    });

    jQuery('#js_city').change(function() {
        GS.form.espForm.cityChange();
    });

    jQuery('#js_firstName').blur(function() {
        GS.form.espForm.validateFields('firstName');
    });

    jQuery('#js_lastName').blur(function() {
        GS.form.espForm.validateFields('lastName');
    });

    jQuery('#js_screenName').blur(function() {
        GS.form.espForm.validateFields('screenName', {email:jQuery('#js_email').val(), field:'username'});
    });

    jQuery('#js_password').blur(function() {
        GS.form.espForm.validateFields('password', {confirmPassword:jQuery('#js_confirmPassword').val()});
        GS.form.espForm.validateFields('confirmPassword', {password:jQuery('#js_password').val()});
    });

    jQuery('#js_confirmPassword').blur(function() {
        GS.form.espForm.validateFields('confirmPassword', {password:jQuery('#js_password').val()});
    });

    jQuery('#js_jobTitle').change(function() {
        GS.form.espForm.validateRequiredFields('jobTitle');
    });
    jQuery('#js_school').change(GS.form.espForm.validateSchool);

    var regPanel = jQuery('#js_regPanel');

    if (regPanel.is(':visible')) {
        GS.form.espForm.bindRegistrationSubmit();

    } else if (!regPanel.is(':visible')) {
        GS.form.espForm.bindEmailSubmit();
    }

    jQuery('#js_email').blur(function() {
        var regPanel = jQuery('#js_regPanel');
        var passwordDiv = jQuery('#js_registeredPasswordDiv');
        var email = jQuery('#js_email');

        var isEmailEditable = email.attr('readonly') === 'readonly';

        //TODO should the form be cleared when reg panel is hidden?
        GS.form.espForm.hideAllErrors();

        if ((regPanel.is(':visible') || passwordDiv.is(':visible')) && !isEmailEditable) {
            regPanel.hide();
            passwordDiv.hide();
            jQuery('#js_firstNameDiv').hide();
            jQuery('#js_lastNameDiv').hide();
            jQuery('#js_screenNameDiv').hide();
            jQuery('#js_passwordDiv').hide();
            jQuery('#js_confirmPasswordDiv').hide();
            GS.form.espForm.bindEmailSubmit();
        }
    });

});



