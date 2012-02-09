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
                data: {state:state, city:city, format:'json', type:'school',excludePreschoolsOnly:true},
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
                if (city.name !== '') {
                    var cityVal = city.name === '- Choose city -' ? '' : city.name;
                    citySelect.append("<option value=\"" + cityVal + "\">" + city.name + "</option>");
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

    //Checks the various states of the user and displays messages accordingly.
    this.validateUserState = function() {
        var emailField = jQuery('#js_email');
        var email = emailField.val().trim();
        jQuery('.js_emailErr').hide();
        var dfd = jQuery.Deferred();

        if (email !== "" && email !== undefined) {
            email = email.trim();
            jQuery.ajax({
                type: 'GET',
                url: '/official-school-profile/checkUserState.page',
                data: {email:email},
                dataType: 'json',
                async: true
            }).done(
                function(data) {
                    var isValid = GS.form.espForm.handleEmailErrors(data,email);
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
            dfd.reject();
        }
        return dfd.promise();
    };

    //Handles the logic to allow the registrations to go through or display an error.
    this.handleEmailErrors = function(data,email) {
        var isValid = false;
        if (data.isEmailValid !== true) {
            GS.form.espForm.showEmailError("Please enter a valid email address.");
        } else if (data.isUserApprovedESPMember === true && data.isUserEmailValidated !== true) {
            // users who have been approved but haven't followed through by clicking through the link in email
            GSType.hover.emailNotValidated.setEmail(email);
            var onclickStr = "GSType.hover.emailNotValidated.show()";
            GS.form.espForm.showEmailError("Please verify your email.<a href='#' onclick=" + onclickStr + ">Verify email</a>");
        } else if (data.isUserAwaitingESPMembership === true) {
            // users who have requested access but are still being processed
            GS.form.espForm.showEmailError("You have already requested access to this school's Official School Profile. We are reviewing your request currently and will email you within a few days with a link to get started on the profile.");
        } else if (data.isUserApprovedESPMember === true && data.isUserEmailValidated === true && data.isUserCookieSet !== true) {
            // users who have been approved and validated their emails.We check the cookie, since a signed in user should be able to submit one request.
            GS.form.espForm.showEmailError("You already have access to this school's Official School Profile.<br/><a href='/official-school-profile/signin.page'>Sign in</a> to your account here.");
        } else if (data.isUserEmailValidated === true && data.isUserCookieSet !== true) {
            // valid GS users who never request ESP.We check the cookie, since a signed in user should be able to submit one request.
            var onclickStr = "GSType.hover.signInHover.showHover('" + email + "','/official-school-profile/register.page')";
            GS.form.espForm.showEmailError("This email address is already registered.<a href='#' onclick=" + onclickStr + ">Log in.</a>");
        }else if (data.isCookieMatched !== true ) {
            GS.form.espForm.showEmailError("An error has occurred.");
        } else {
            isValid = true;
        }
        return isValid;
    };

    this.showEmailError = function(errMsg) {
        jQuery('#js_emailError').html('<span class="sprite i-alert vam"></span>&nbsp;' + errMsg);
        jQuery('#js_emailError').show();
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
            url: '/official-school-profile/checkStateSchoolUserUnique.page',
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
            fieldError.html('<span class="sprite i-alert vam"></span>&nbsp;' + data[fieldName]);
            fieldError.show();
            return false;
        } else {
            fieldValid.show();
            return true;
        }
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
    jQuery('#js_school').change(GS.form.espForm.validateSchool);


    //Bind the new click handler which validates all the visible fields and submits the form if everything is valid.
    jQuery('#js_submit').click(
        GS.form.espForm.registrationSubmit
    );

});


