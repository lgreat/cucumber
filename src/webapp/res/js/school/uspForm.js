var GS = GS || {};
GS.form = GS.form || {};

GS.form.uspForm = (function ($) {
    "use strict";

    var USP_FORM_SELECTOR = '#js_uspForm';
    var OSP_USER_FIELDS_SELECTOR = '.js-ospUserFields';
    var SUBMIT_SELECTOR = '.js_submit';

    var REGISTRATION_FORM_SELECTOR = '.js_uspRegistrationForm:visible';
    var REGISTRATION_PASSWORD_SELECTOR = '.js_regPassword:visible';
    var REGISTRATION_EMAIL_SELECTOR = '.js_regEmail:visible';
    var REGISTRATION_TERMS_OF_USE_SELECTOR = '.js-tou .js-checkBoxSprite:visible';
    var REGISTRATION_SUBMIT_SELECTOR = '.js_uspRegistrationSubmit:visible';

    var LOGIN_SUBMIT_SELECTOR = '.js_uspLoginSubmit:visible';
    var LOGIN_FORM_SELECTOR = '.js_uspLoginForm:visible';
    var LOGIN_EMAIL_SELECTOR = '.js_loginEmail:visible';
    var LOGIN_PASSWORD_SELECTOR = '.js_loginPassword:visible';

    var PASSWORD_ERROR_SELECTOR = '.js_passwordErr';
    var EMAIL_ERROR_SELECTOR = '.js_emailErr';
    var TERMS_OF_USE_ERROR_SELECTOR = '.js_termsErr';
    var EXISTING_ANSWERS_ERROR = '#js-existingAnswersError';
    var SELECT_ONE_ERROR_SELECTOR = '.js-uspSelectNone';

    var CHECK_USER_STATE_URL = '/school/QandA/checkUserState.page';

    var validateTermsOfUse = function(termsField) {
        var deferred = $.Deferred();
        var valid = termsField.parent().hasClass('js-checkBoxSpriteOn');
        if (valid) {
            hideErrors(TERMS_OF_USE_ERROR_SELECTOR);
            deferred.resolve();
        } else {
            handleValidationResponse(TERMS_OF_USE_ERROR_SELECTOR, 'You must agree to the terms of use.', termsField);
            deferred.reject();
        }
        return deferred.promise();
    };

    var validatePassword = function (elem) {
        var fieldVal = elem.val();
        var dfd = $.Deferred();

        hideErrors(PASSWORD_ERROR_SELECTOR, elem);

        if (fieldVal.length < 6 || fieldVal.length > 14) {
            handleValidationResponse(PASSWORD_ERROR_SELECTOR, 'Password should be 6-14 characters.', elem);
            dfd.reject();
        } else {
            return dfd.resolve();
        }
        return dfd.promise();
    };

    var validateUserState = function (emailField, validateUserLogin, isSignInHover, passwordField) {
        var email = $.trim(emailField.val());
        var dfd = $.Deferred();
        hideErrors(EMAIL_ERROR_SELECTOR, emailField);

        if (email !== "" && email !== undefined) {
            var data = [];
            data.push({name:"email", value:email});
            data.push({name:"isLogin", value:validateUserLogin});

            if (passwordField !== undefined) {
                var password = passwordField.val();
                data.push({name:"password", value:password});
            }

            $.ajax({
                type:'GET',
                url:CHECK_USER_STATE_URL,
                data:data,
                dataType:'json',
                async:true
            }).done(
                function (data) {
                    var isValid = handleEmailErrors(data, email, emailField, isSignInHover);
                    if (isValid === false) {
                        dfd.reject();
                    } else {
                        dfd.resolve();
                    }
                }
            ).fail(function () {
                    dfd.reject();
                });
        } else {
            handleValidationResponse(EMAIL_ERROR_SELECTOR, 'Please enter a valid email address.', emailField);
            dfd.reject();
        }
        return dfd.promise();
    };

    var handleEmailErrors = function (data, email, emailField, isLogin) {
        var isValid = false;
        if (data.isEmailValid !== true) {
            handleValidationResponse(EMAIL_ERROR_SELECTOR, 'Please enter a valid email address.', emailField);
        } else if (isLogin === true && data.isNewUser === true) {
            handleValidationResponse(EMAIL_ERROR_SELECTOR, "Please <a href='javascript:void(0);' class='js_lnchUspRegistration' >register here</a>.", emailField);
        } else if (isLogin === false && data.isNewUser !== true) {
            handleValidationResponse(EMAIL_ERROR_SELECTOR, "Please <a href='javascript:void(0);' class='js_lnchUspSignin'>sign in here</a>.", emailField);
        } else if (data.isNewUser !== true && data.isUserEmailValidated !== true) {
            var onclickStr = "'GS.form.uspForm.handleEmailVerification(); return false;'";
            handleValidationResponse(EMAIL_ERROR_SELECTOR, "Please <a href='javascript:void(0);' onclick=" + onclickStr + ">verify your email</a>.", emailField);
        } else if (isLogin === true && data.isCookieMatched !== true) {
            handleValidationResponse(EMAIL_ERROR_SELECTOR, 'The password you entered is incorrect.', emailField);
        } else {
            isValid = true;
        }
        return isValid;
    };

    var handleEmailVerification = function () {
        var uspForm = $(USP_FORM_SELECTOR);
        var emailField = $(LOGIN_EMAIL_SELECTOR);
        var data = uspForm.serializeArray();
        data.push({name:"email", value:emailField.val()});
        data.push({name:"action",value:"sendVerificationEmail"});
        saveForm(data);
    };

    var handleValidationResponse = function (fieldSelector, errorMsg, elem) {
        var fieldError = $(fieldSelector + '.invalid');
        var fieldValid = $(fieldSelector + '.success');

        if (elem !== undefined) {
            removeWarningClassFromElem(elem);
        }

        fieldError.hide();
        fieldValid.hide();

        if (errorMsg !== undefined && errorMsg !== '') {
            if (elem !== undefined) {
                addWarningClassToElem(elem);
            }
            fieldError.find('.bd').html(errorMsg); // set error message
            fieldError.show();
            return false;
        } else {
            fieldValid.show();
            return true;
        }
    };

    var addWarningClassToElem = function (elem) {
        elem.addClass("warning");
    };

    var removeWarningClassFromElem = function (elem) {
        elem.removeClass("warning");
    };

    var hideErrors = function (errorClass, elem) {
        handleValidationResponse(errorClass, '', elem);
    };

    var validateUspDataAndShowHover = function (uspForm, isFromFacebook) {
        var data = uspForm.serializeArray();
        var isUserSignedIn = GS.isSignedIn();
        if (!isOspUser() && !doUspFormValidations(data)) {
            window.scrollTo(0,0);
            $(".js-uspSelectNone").removeClass("dn");
        } else if (isOspUser() && !doUspFormValidationsForOspUser(uspForm)) {
            window.scrollTo(0,0);
            $(".js-uspSelectOneEach").removeClass("dn");
        }
        else {
            if(s) {
                pageTracking.clear();
                pageTracking.successEvents = "event81";
                pageTracking.send();
            }
            if (isUserSignedIn === true) {
                $(SUBMIT_SELECTOR).prop('disabled',true);
                if (isFromFacebook === true) {
                    data.push({name:"action", value:"facebookUserInSession"});
                } else {
                    data.push({name:"action", value:"userInSession"});
                }
                saveForm(data);
            } else {
                GSType.hover.modalUspRegistration.showHover();
            }
        }
    };

    var registerAndSaveData = function (uspForm, uspRegistrationPasswordField, uspRegistrationEmailField, uspTermsOfUseField) {
        //First do validations and then save the form.
        $.when(
            validateUserState(uspRegistrationEmailField, false, false),
            validatePassword(uspRegistrationPasswordField),
            validateTermsOfUse(uspTermsOfUseField)
        ).done(
            function () {
                var password = uspRegistrationPasswordField.val();
                var email = $.trim(uspRegistrationEmailField.val());
                var terms = true; // must be true for above validation to pass

                var data = uspForm.serializeArray();
                data.push({name:"email", value:email});
                data.push({name:"password", value:password});
                data.push({name:"terms", value:terms});
                data.push({name:"action",value:"registration"});

                if ($('.js-joinHover:visible').find('.js-mss .js-checkBoxSpriteOn:visible').length > 0) {
                    data.push({name:"mss", value:true});
                }

                GSType.hover.modalUspRegistration.hide();
                saveForm(data);
            }
        ).fail(
            function () {
                // Error messages are already displayed as part of ajax validations.
            }
        );
    };

    var loginAndSaveData = function (uspForm, uspLoginPasswordField, uspLoginEmailField, uspTermsOfUseField) {
        //First do validations and then save the form.
        $.when(
            validateUserState(uspLoginEmailField, true, true, uspLoginPasswordField)
        ).done(
            function () {
                var password = uspLoginPasswordField.val();
                var email = $.trim(uspLoginEmailField.val());
                var data = uspForm.serializeArray();
                data.push({name:"email", value:email});
                data.push({name:"password", value:password});
                data.push({name:"action", value:"login"});

                saveForm(data);
            }
        ).fail(
            function () {
                // Error messages are already displayed as part of ajax validations.
            }
        );
    };

    var saveForm = function (data) {

        $.ajax({type:'POST',
                async:true,
                url:document.location,
                data:data}
        ).fail(function () {
                alert("Sorry! There was an unexpected error saving your form. Please wait a minute and try again.");
            }).done(function (data) {
                if (data.redirect !== undefined && data.redirect !== '') {
                    window.location = data.redirect;
                }
                else if(data.hasExistingSavedResponses === 'true') {
                    hideAllErrors();
                    $(SUBMIT_SELECTOR).prop('disabled',false);
                    if(data.formFields === undefined) {
                        return;
                    }
                    var uspForm = $(USP_FORM_SELECTOR);
                    updateUspFormFields(data, uspForm);

                }
            });
    };

    var updateUspFormFields = function(data, uspForm) {
        var formFields = data.formFields;
        var formElements = uspForm.find('select.js-chosenSelect');
        // first clear the chosen fields to remove any selected values
        formElements.val('').trigger("liszt:updated");
        var user = data.user;
        if(user !== undefined) {
            // if user data is able, update the global header to show that the user is signed in
            GS.GlobalUI.updateUIForLogin(user.id, user.email, user.screenName, user.numberMSLItems);
        }
        var numFormFields = data.formFields.length;
        for(var i = 0; i < numFormFields; i++) {
            var formField = formFields[i];
            // for each select element, check if the response value has isSelected is set to true then update the option's selected property
            var formElement = uspForm.find('select[name=' + formField.fieldName + ']');
            var sectionResponses = formField.responses;
            var numSectionResponses = sectionResponses.length;
            for(var j = 0; j < numSectionResponses; j++) {
                var sectionResponse = sectionResponses[j];
                var responseKey = sectionResponse.key;

                var responseValues = sectionResponse.values;
                var numResponseValues = responseValues.length;
                for(var k = 0; k < numResponseValues; k++) {
                    var responseValue = responseValues[k];
                    var respValue = responseValue.responseValue;
                    var isSelected = responseValue.isSelected;

                    if(isSelected) {
                        formElement.find('option[value=' + responseKey + '__' +
                            respValue + ']').prop("selected", true);
                    }
                }
            }
        }

        // once all the elements in the form have been updated with the previously saved responses, trigger chosen update to display the saved responses
        formElements.trigger("liszt:updated");
        GSType.hover.modalUspSignIn.hide();
        jQuery(EXISTING_ANSWERS_ERROR).show();
        window.scrollTo(0,0);
    }

    var hideAllErrors = function () {
        $(PASSWORD_ERROR_SELECTOR).hide();
        $(EMAIL_ERROR_SELECTOR).hide();
        $(TERMS_OF_USE_ERROR_SELECTOR).hide();
        $(EXISTING_ANSWERS_ERROR).hide();
        //Not sure why this is done differently.
        $(SELECT_ONE_ERROR_SELECTOR).addClass("dn");
    };

    var doUspFormValidations = function (data) {
        if (data.length === 0) {
            return false;
        }
        return true;
    };

    var doUspFormValidationsForOspUser = function (uspForm) {
        var errors = [];
        var $formFields = uspForm.find('.js-formFields');
        $formFields.each(function () {
            var $this = $(this);

            var selectVal = $this.find('select').val();
            var otherVal = $this.find('.js-ospUserFields .js-otherResponse input[name=otherValue]').val();
            var noneChecked = $this.find('.js-ospUserFields .js-noneResponse .js-checkBoxSpriteOn:visible');

            if ((selectVal === null || selectVal.length === 0) && otherVal.trim() === '' && noneChecked.length === 0) {
                errors.push('error');
            }
        });

        if (errors.length > 0) {
            return false;
        }

        return true;
    };

    var isOspUser = function() {
        var $uspForm = $(USP_FORM_SELECTOR);
        return $uspForm.find(OSP_USER_FIELDS_SELECTOR).length > 0;
    };

    var attachEventHandlers = function() {
        var $uspForm = $(USP_FORM_SELECTOR);
        var $body = $('body');

        $uspForm.on('click', SUBMIT_SELECTOR, function () {
            hideAllErrors();
            validateUspDataAndShowHover($uspForm);
            return false;
        });
        GSType.hover.modalUspSignIn.setOnSubmitCallback(validateUspDataAndShowHover.bind(this, $uspForm, true));
        GSType.hover.modalUspRegistration.setOnSubmitCallback(validateUspDataAndShowHover.bind(this, $uspForm, true));

        // sets the value of hidden other field as [response_key]__[other_text_field_value]
        $('.js-otherText').on('change', function () {
            var $this = $(this);
            $this.parent().find('input.js-otherValue').val(function (index, value) {
                return value.substring(0, value.indexOf('__') + 2) + $this.val();
            });
        });

        $(SUBMIT_SELECTOR).prop('disabled',false);

        //The new way of doing modals puts duplicate Ids on the page.I dealt with it by
        //binding handlers to visible elements.
        $body.on('blur', REGISTRATION_PASSWORD_SELECTOR, function (event) {
            validatePassword($(event.target));
        });

        $body.on('blur', REGISTRATION_EMAIL_SELECTOR, function (event) {
            validateUserState($(event.target), false, false);
        });

        $body.on('blur', LOGIN_EMAIL_SELECTOR, function (event) {
            validateUserState($(event.target), false, true);
        });

        $body.on('click', '.js_lnchUspSignin', function () {
            hideAllErrors();
            GSType.hover.modalUspRegistration.hide();
            GSType.hover.modalUspSignIn.showHover();
        });

        $body.on('click', '.js_modalUspSignIn_launchForgotPassword', function () {
            hideAllErrors();
            GSType.hover.modalUspSignIn.hide();
            GSType.hover.forgotPassword.setOsp("true");
            GSType.hover.forgotPassword.show();
            jQuery('button[name=forgotPasswordCancel]:visible').addClass('js_showSignIn').removeClass('js_closeHover');
        });

        $body.on('click', '.js_showSignIn', function() {
            GSType.hover.forgotPassword.hide();
            GSType.hover.modalUspSignIn.showHover();
        });

        $body.on('click', '.js_lnchUspRegistration', function () {
            hideAllErrors();
            GSType.hover.modalUspSignIn.hide();
            GSType.hover.modalUspRegistration.showHover();

            if(s) {
                pageTracking.clear();
                pageTracking.pageName = "USP Join Hover";
                pageTracking.hierarchy =  "Hovers, Join, USP Join Hover";
                pageTracking.send();
            }
        });

        $body.on('click', REGISTRATION_SUBMIT_SELECTOR, function () {
            var uspRegistrationForm = $(REGISTRATION_FORM_SELECTOR);
            var uspRegistrationPasswordField = uspRegistrationForm.find(REGISTRATION_PASSWORD_SELECTOR);
            var uspRegistrationEmailField = uspRegistrationForm.find(REGISTRATION_EMAIL_SELECTOR);
            var uspTermsOfUseField = uspRegistrationForm.find(REGISTRATION_TERMS_OF_USE_SELECTOR);

            registerAndSaveData($uspForm, uspRegistrationPasswordField, uspRegistrationEmailField, uspTermsOfUseField);
        });

        $body.on('click', LOGIN_SUBMIT_SELECTOR, function () {
            var uspLoginForm = $(LOGIN_FORM_SELECTOR);
            var uspLoginPasswordField = uspLoginForm.find(LOGIN_PASSWORD_SELECTOR);
            var uspLoginEmailField = uspLoginForm.find(LOGIN_EMAIL_SELECTOR);

            loginAndSaveData($uspForm, uspLoginPasswordField, uspLoginEmailField);
        });

    };

    return {
        validateTermsOfUse: validateTermsOfUse,
        validatePassword: validatePassword,
        validateUserState: validateUserState,
        handleEmailErrors: handleEmailErrors,
        handleEmailVerification: handleEmailVerification,
        handleValidationResponse: handleValidationResponse,
        addWarningClassToElem: addWarningClassToElem,
        removeWarningClassFromElem: removeWarningClassFromElem,
        hideErrors: hideErrors,
        validateUspDataAndShowHover: validateUspDataAndShowHover,
        registerAndSaveData: registerAndSaveData,
        loginAndSaveData: loginAndSaveData,
        saveForm: saveForm,
        hideAllErrors: hideAllErrors,
        doUspFormValidations: doUspFormValidations,
        doUspFormValidationsForOspUser: doUspFormValidationsForOspUser,
        attachEventHandlers: attachEventHandlers,
        isOspUser: isOspUser
    };
})(jQuery);

function uspSpriteCheckBoxes(containerLayer, fieldToSet, checkedValue, uncheckedValue) {
    var container = $("." + containerLayer);
    var checkOn = container.find(".js-checkBoxSpriteOn");
    var checkOff = container.find(".js-checkBoxSpriteOff");
    var checkBoxField = $("#" + fieldToSet);
    checkOff.on("click", function () {
        var $this = $(this);
        $(this).addClass(' dn');
        $(this).siblings().removeClass(' dn');
        // set the value for the hidden input field as [response_key]__None for boys sports,
        // [response_key]__neither for extended care, [response_key]__none for other questions
        $(this).parent().find('input.js-noneValue').val(function (index, value) {
            var $this = $(this);
            var responseVal = 'none';
            if($this.hasClass('js-boys_sports')) {
                responseVal = 'None';
            }
            else if($this.hasClass('js-before_after_care')) {
                responseVal = 'neither';
            }
            return value.substring(0, value.indexOf('__') + 2) + responseVal;
        });
        checkBoxField.val(checkedValue);
    });
    checkOn.on("click", function () {
        var $this = $(this);
        $this.addClass(' dn');
        $this.siblings().removeClass(' dn');
        // set the value for the hidden input field as [response_key]__
        $this.parent().find('input.js-noneValue').val(function (index, value) {
            return value.substring(0, value.indexOf('__') + 2);
        });
        checkBoxField.val(uncheckedValue);
    });
}

jQuery(function () {
    uspSpriteCheckBoxes("js-needText", "form_needText", 1, 0);
    uspSpriteCheckBoxes("js-noneResponse", "formOther", 1, 0);
    uspSpriteCheckBoxes("js-otherResponse", "formOther", 1, 0);
    uspSpriteCheckBoxes("js-tou", "formOther", 1, 0);

    GS.form.uspForm.attachEventHandlers();
});