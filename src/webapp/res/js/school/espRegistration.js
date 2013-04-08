var GS = GS || {};
GS.form = GS.form || {};
GS.form.EspForm = function() {

    //Handle State drop down change.
    this.stateChange = function() {

        var stateSelect = jQuery('#js_stateAdd');
        var citySelect = jQuery('#js_city');
        var schoolSelect = jQuery('#js_school');

        citySelect.html('<option value="">Loading ...</option>');
        schoolSelect.html('<option value="">- Choose school -</option>');

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
        var schoolSelect = jQuery('#js_school');

        schoolSelect.html('<option value="">- Choose school -</option>');

        var state = stateSelect.val();
        var city = citySelect.val();

        if (state !== '' && city !== '' && city !== '- Choose city -' && city !== 'My city is not listed') {
            jQuery('#js_school').html("<option>Loading...</option>");
            jQuery.ajax({
                type: 'GET',
                url: "/community/registration2Ajax.page",
                data: {state:state, city:city, format:'json', type:'school', excludePreschoolsOnly:true},
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
                if (city.name !== '' && city.name !== 'My city is not listed') {
                    var cityVal = (city.name === '- Choose city -') ? '' : city.name;
                    citySelect.append("<option value=\"" + cityVal + "\">" + city.name + "</option>");
                }
            }
        }

        var preSelectedCity = decodeURIComponent(GS.uri.Uri.getFromQueryString('city')).replace(/\+/g, " ");
        if(citySelect.find('option[value="' + preSelectedCity + '"]').length == 1) {
            citySelect.val(preSelectedCity);
            GS.form.espForm.cityChange();
            GS.form.espForm.validateRequiredFields('city');
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

        var preSelectedSchool = GS.uri.Uri.getFromQueryString('schoolId');
        schoolSelect.val(preSelectedSchool);
    };

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

    //Handles the logic to allow the registrations to go through or display an error.
    //These conditions are complicated, refer to the flow charts attached to GS-13363.
    this.handleEmailErrors = function(data,email,emailField) {
        var isValid = false;
        if (data.isEmailValid !== true) {
            GS.form.espForm.showEmailError("Please enter a valid email address.", emailField);
        }else if (data.isCookieMatched !== true ) {
            GS.form.espForm.showEmailError("An error has occurred.", emailField);
        }else if (data.isUserESPPreApproved === true ) {
            GSType.hover.espPreApprovalEmail.setEmail(jQuery('#js_email').val());
            GSType.hover.espPreApprovalEmail.setSchoolName(data.schoolName);
            var onclickStr = "'GSType.hover.espPreApprovalEmail.show(); return false;'";
            GS.form.espForm.showEmailError("You have been pre-approved for an account but must verify your email. <a href='#' onclick=" + onclickStr + ">Please verify email.</a>", emailField);
        } else if (data.isUserESPRejected === true) {
            //GS.form.espForm.showEmailError("Our records indicate you already requested a school official's account. Please contact us at gs_support@greatschools.org if you need further assistance.", emailField);
            GS.form.espForm.showEmailError("Sorry - it looks like you are not authorized as an administrator.  Try again, or <a href='/about/feedback.page?feedbackType=esp'>contact us</a>.", emailField);
        } else if (data.isUserApprovedESPMember === true && data.isUserEmailValidated !== true) {
            // users who have been approved but haven't followed through by clicking through the link in email
            GSType.hover.emailNotValidated.setEmail(email);
            var onclickStr = "'GSType.hover.emailNotValidated.show(); return false;'";
            GS.form.espForm.showEmailError("Please verify your email. <a href='#' onclick=" + onclickStr + ">Verify email</a>", emailField);
        } else if (data.isUserAwaitingESPMembership === true || (data.isUserApprovedESPMember === true && data.isUserEmailValidated === true && data.isUserCookieSet !== true)) {
            // users who have requested access but are still being processed.Prompt them to use the sign in page.
            // users who have been approved and validated their emails.However they are not logged in, therefore prompt them to log in.
            GS.form.espForm.showEmailError("Whoops!  It looks like you're already a member.  Please <a href='/official-school-profile/signin.page?email=" + encodeURIComponent(email) + "'>sign in</a> here.", emailField);
        } else if (data.isUserApprovedESPMember === true && data.isUserEmailValidated === true && data.isUserCookieSet === true) {
            // users who have been approved and validated their emails and have a cookie set. They should view the ESP dashboard.
            window.location = '/official-school-profile/dashboard/';
        } else if (data.isUserEmailValidated === true && data.isUserCookieSet !== true) {
            // valid GS users who never request ESP.Prompt them to use the sign in page.
            GS.form.espForm.showEmailError("It looks like you're already a member! Please <a href='/official-school-profile/signin.page?email=" + encodeURIComponent(email) + "'>sign in</a> here.", emailField);
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

    this.validateStateSchoolUserUnique = function() {
        var schoolId = parseInt(jQuery('#js_school').val());
        if (isNaN(schoolId)) {
            schoolId = -1;
        }
        var state = jQuery('#js_stateAdd').val();
        var email = jQuery('#js_email').val();
        var fieldError = jQuery('#js_uniqueError');
        var dfd = jQuery.Deferred();

        fieldError.hide();

        jQuery.ajax({
            type: 'GET',
            url: '/official-school-profile/checkStateSchoolUserUnique.page',
            data: {schoolId:schoolId, state:state,email:email},
            dataType: 'json',
            async: true
        }).done(
            function(data) {
                if (data.isUnique !== true && data.isDisabled === false && data.isRejected === false && data.isProcessing === false) {
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
        var isReadOnly = elem.attr('readonly') === 'readonly';
        var dfd = jQuery.Deferred();
        var dataParams = {field:fieldName};
        dataParams[fieldName] = fieldVal;
        jQuery.extend(dataParams, ajaxParams);

        if (isFieldVisible && !isReadOnly) {
            jQuery.ajax({
                type: 'GET',
                url: '/community/registrationValidationAjax.page',
                data:dataParams,
                dataType: 'json',
                async: true
            }).done(
                function(data) {
                    var rval = GS.form.espForm.handleValidationResponse('.js_' + fieldName, fieldName, data, elem);
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
            GS.form.espForm.validateFields('firstName'),
            GS.form.espForm.validateFields('lastName'),
            GS.form.espForm.validateFields('screenName', {email:jQuery('#js_email').val(), field:'username'}),
            GS.form.espForm.validateFields('password', {confirmPassword:jQuery('#js_confirmPassword').val()}),
            GS.form.espForm.validateFields('confirmPassword', {password:jQuery('#js_password').val()}),
            GS.form.espForm.validateRequiredFields('jobTitle'),
            GS.form.espForm.validateRequiredFields('stateAdd'),
            GS.form.espForm.validateRequiredFields('city'),
            GS.form.espForm.validateRequiredFields('school'),
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

    this.hideAllErrors = function() {
        jQuery('.error').hide();
    };
};

GS.form.espForm = new GS.form.EspForm();

jQuery(function() {
    if(jQuery('#js_stateAdd').val() !== '') {
        GS.form.espForm.stateChange();
        GS.form.espForm.validateRequiredFields('stateAdd');
    }

    jQuery('#js_stateAdd').change(function() {
        GS.form.espForm.stateChange();
        GS.form.espForm.validateRequiredFields('stateAdd');
    });

    jQuery('#js_city').change(function() {
        GS.form.espForm.cityChange();
        GS.form.espForm.validateRequiredFields('city');
    });

    jQuery('#js_email').blur(
        GS.form.espForm.validateUserState
    );

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

    jQuery('#js_school').change(function() {
        GS.form.espForm.validateRequiredFields('school');
    });

    //Bind the new click handler which validates all the visible fields and submits the form if everything is valid.
    jQuery('#js_submit').click(
        GS.form.espForm.registrationSubmit
    );

});