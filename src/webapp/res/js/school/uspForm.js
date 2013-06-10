var GS = GS || {};
GS.form = GS.form || {};

GS.form.uspForm = (function ($) {
    "use strict";

    var validateTermsOfUse = function(termsField) {
        var deferred = $.Deferred();
        var valid = termsField.parent().hasClass('js-checkBoxSpriteOn');
        if (valid) {
            hideErrors('.js_termsErr');
            deferred.resolve();
        } else {
            handleValidationResponse('.js_termsErr', 'You must agree to the terms of use.', termsField);
            deferred.reject();
        }
        return deferred.promise();
    };

    var validatePassword = function (elem) {
        var fieldVal = elem.val();
        var dfd = $.Deferred();

        hideErrors('.js_passwordErr', elem);

        if (fieldVal.length < 6 || fieldVal.length > 14) {
            handleValidationResponse('.js_passwordErr', 'Password should be 6-14 characters.', elem);
            dfd.reject();
        } else {
            return dfd.resolve();
        }
        return dfd.promise();
    };

    var validateUserState = function (emailField, validateUserLogin, isSignInHover, passwordField) {
        var email = $.trim(emailField.val());
        var dfd = $.Deferred();
        hideErrors('.js_emailErr', emailField);

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
                url:'/school/QandA/checkUserState.page',
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
            handleValidationResponse('.js_emailErr', 'Please enter a valid email address.', emailField);
            dfd.reject();
        }
        return dfd.promise();
    };

    var handleEmailErrors = function (data, email, emailField, isLogin) {
        var isValid = false;
        if (data.isEmailValid !== true) {
            handleValidationResponse('.js_emailErr', 'Please enter a valid email address.', emailField);
        } else if (isLogin === true && data.isNewUser === true) {
            handleValidationResponse('.js_emailErr', "Please <a href='#' class='js_lnchUspRegistration' >register here</a>.", emailField);
        } else if (isLogin === false && data.isNewUser !== true) {
            handleValidationResponse('.js_emailErr', "Please <a href='#' class='js_lnchUspSignin'>sign in here</a>.", emailField);
        } else if (data.isNewUser !== true && data.isUserEmailValidated !== true) {
            var onclickStr = "'GS.form.uspForm.handleEmailVerification(); return false;'";
            handleValidationResponse('.js_emailErr', "Please <a href='#' onclick=" + onclickStr + ">verify your email</a>.", emailField);
        } else if (isLogin === true && data.isCookieMatched !== true) {
            handleValidationResponse('.js_emailErr', 'The password you entered is incorrect.', emailField);
        } else {
            isValid = true;
        }
        return isValid;
    };

    var handleEmailVerification = function () {
        var uspForm = $('#js_uspForm');
        var emailField = $('.js_loginEmail:visible');
        saveForm(uspForm, '', emailField.val());
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

    var validateUspDataAndShowHover = function (uspForm, isOspUser) {
        var data = uspForm.serializeArray();
        var isUserSignedIn = GS.isSignedIn();
        if (!isOspUser && !doUspFormValidations(data)) {
            window.scrollTo(0,0);
            $(".js-uspSelectNone").removeClass("dn");
        } else if (isOspUser && !doUspFormValidationsForOspUser(uspForm)) {
            window.scrollTo(0,0);
            $(".js-uspSelectOneEach").removeClass("dn");
        }
        else if (isUserSignedIn === true) {
            saveForm(uspForm);
        } else {
            GSType.hover.modalUspSignIn.show();
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
                $.when(
                    saveForm(uspForm, password, email, terms)
                ).done(function () {
                        GSType.hover.modalUspRegistration.hide();
                    })
                    .fail(function () {
                        GSType.hover.modalUspRegistration.hide();
                        alert("Sorry! There was an unexpected error saving your form. Please wait a minute and try again.");
                    });
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
                saveForm(uspForm, password, email);
            }
        ).fail(
            function () {
                // Error messages are already displayed as part of ajax validations.
            }
        );
    };

    var saveForm = function (uspForm, password, email, terms) {
        var dfd = $.Deferred();

        var data = uspForm.serializeArray();

        if (password !== undefined && password !== '') {
            data.push({name:"password", value:password});
        }
        if (email !== undefined && email !== '') {
            data.push({name:"email", value:email});
        }

        if($('.js-joinHover:visible').find('.js-mss .js-checkBoxSpriteOn:visible').length > 0) {
            data.push({name:"mss", value:true});
        }

        if (terms !== undefined && terms !== '') {
            data.push({name:"terms", value:terms});
        }

        $.ajax({type:'POST',
                async:true,
                url:document.location,
                data:data}
        ).fail(function () {
                dfd.reject();
                alert("Sorry! There was an unexpected error saving your form. Please wait a minute and try again.");
            }).done(function (data) {
                dfd.resolve();
                if (data.redirect !== undefined && data.redirect !== '') {
                    window.location = data.redirect;
                }

            });
        return dfd.promise();
    };

    var hideAllErrors = function () {
        $('.js_passwordErr').hide();
        $('.js_emailErr').hide();
        $('.js_termsErr').hide();
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
        doUspFormValidationsForOspUser: doUspFormValidationsForOspUser
    };
})(jQuery);

function uspSpriteCheckBoxes(containerLayer, fieldToSet, checkedValue, uncheckedValue) {
    var container = $("." + containerLayer);
    var checkOn = container.find(".js-checkBoxSpriteOn");
    var checkOff = container.find(".js-checkBoxSpriteOff");
    var checkBoxField = $("#" + fieldToSet);
    checkOff.on("click", function () {
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
        $(this).addClass(' dn');
        $(this).siblings().removeClass(' dn');
        // set the value for the hidden input field as [response_key]__
        $(this).parent().find('input.js-noneValue').val(function (index, value) {
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

    var uspForm = $('#js_uspForm');
    var $body = $('body');

    var isOspUser = function () {
        return uspForm.find('.js-ospUserFields').length > 0;
    };

    uspForm.on('click', '.js_submit', function () {
        GS.form.uspForm.hideAllErrors();
        GS.form.uspForm.validateUspDataAndShowHover(uspForm, isOspUser());

        if(s) {
            pageTracking.clear();
            pageTracking.successEvents = "event81";
            pageTracking.send();
        }
        return false;
    });

    // sets the value of hidden other field as [response_key]__[other_text_field_value]
    $('.js-otherText').on('change', function () {
        var $this = $(this);
        $this.parent().find('input.js-otherValue').val(function (index, value) {
            return value.substring(0, value.indexOf('__') + 2) + $this.val();
        });
    });

    //The new way of doing modals puts duplicate Ids on the page.I dealt with it by
    //binding handlers to visible elements.
    $body.on('blur', '.js_regPassword:visible', function (event) {
        GS.form.uspForm.validatePassword($(event.target));
    });

    $body.on('blur', '.js_regEmail:visible', function (event) {
        GS.form.uspForm.validateUserState($(event.target), false, false);
    });

    $body.on('blur', '.js_loginEmail:visible', function (event) {
        GS.form.uspForm.validateUserState($(event.target), false, true);
    });

    $body.on('click', '.js_lnchUspSignin', function () {
        GS.form.uspForm.hideAllErrors();
        GSType.hover.modalUspRegistration.hide();
        GSType.hover.modalUspSignIn.show();
    });

    $body.on('click', '.js_modalUspSignIn_launchForgotPassword', function () {
        GS.form.uspForm.hideAllErrors();
        GSType.hover.modalUspSignIn.hide();
        GSType.hover.forgotPassword.setOsp("true");
        GSType.hover.forgotPassword.show();
    });

    $body.on('click', '.js_lnchUspRegistration', function () {
        GS.form.uspForm.hideAllErrors();
        GSType.hover.modalUspSignIn.hide();
        GSType.hover.modalUspRegistration.show();

        if(s) {
            pageTracking.clear();
            pageTracking.pageName = "USP Join Hover";
            pageTracking.hierarchy =  "Hovers, Join, USP Join Hover";
            pageTracking.send();
        }
    });

    $body.on('click', '.js_uspRegistrationSubmit:visible', function () {
        if(s) {
            pageTracking.clear();
            pageTracking.successEvents = "event82";
            pageTracking.send();
        }
        //TODO find a better way to get the form values.
        var uspRegistrationForm = $('.js_uspRegistrationForm:visible');
        var uspRegistrationPasswordField = uspRegistrationForm.find('.js_regPassword');
        var uspRegistrationEmailField = uspRegistrationForm.find('.js_regEmail');
        var uspTermsOfUseField = uspRegistrationForm.find('.js-tou .js-checkBoxSprite:visible');
        GS.form.uspForm.registerAndSaveData(uspForm, uspRegistrationPasswordField, uspRegistrationEmailField, uspTermsOfUseField);
    });

    $body.on('click', '.js_uspLoginSubmit:visible', function () {
        //TODO find a better way to get the form values.
        var uspLoginForm = $('.js_uspLoginForm:visible');
        var uspLoginPasswordField = uspLoginForm.find('.js_loginPassword');
        var uspLoginEmailField = uspLoginForm.find('.js_loginEmail');
        GS.form.uspForm.loginAndSaveData(uspForm, uspLoginPasswordField, uspLoginEmailField);
    });

});