var GS = GS || {};
GS.form = GS.form || {};
GS.form.UspForm = function () {

    this.validateFirstName = function (elem) {
        var fieldVal = jQuery.trim(elem.val());
        var namereg = /[0-9<>&\\]/;
        var dfd = jQuery.Deferred();

        GS.form.uspForm.hideErrors('.js_firstNameErr', elem);

        if (fieldVal.length > 24 || fieldVal.length < 2) {
            GS.form.uspForm.handleValidationResponse('.js_firstNameErr', 'First name must be 2-24 characters long.', elem);
            dfd.reject();
        } else if (namereg.test(fieldVal)) {
            GS.form.uspForm.handleValidationResponse('.js_firstNameErr', 'Please remove the numbers or symbols.', elem);
            dfd.reject();
        } else {
            return dfd.resolve();
        }
        dfd.promise();
    };

    this.validatePassword = function (elem) {
        var fieldVal = elem.val();
        var dfd = jQuery.Deferred();

        GS.form.uspForm.hideErrors('.js_passwordErr', elem);

        if (fieldVal.length < 6 || fieldVal.length > 14) {
            GS.form.uspForm.handleValidationResponse('.js_passwordErr', 'Password should be 6-14 characters.', elem);
            dfd.reject();
        } else {
            return dfd.resolve();
        }
        return dfd.promise();
    };

    this.validateTerms = function () {
        var isValid = jQuery('.js-checkBoxSpriteOn').is(':visible');
        if (isValid === false) {
            GS.form.uspForm.handleValidationResponse('.js_termsErr', 'Check accept.');
        }
        return isValid;
    };

    this.validateUserState = function (emailField, isLogin, passwordField) {
        var email = jQuery.trim(emailField.val());
        var dfd = jQuery.Deferred();
        GS.form.uspForm.hideErrors('.js_emailErr', emailField);

        if (email !== "" && email !== undefined) {
            var data = [];
            data.push({name:"email", value:email});
            data.push({name:"isLogin", value:isLogin});
            if (passwordField != undefined) {
                var password = passwordField.val();
                data.push({name:"password", value:password});
            }

            jQuery.ajax({
                type:'GET',
                url:'/school/usp/checkUserState.page',
                data:data,
                dataType:'json',
                async:true
            }).done(
                function (data) {
                    var isValid = GS.form.uspForm.handleEmailErrors(data, email, emailField, isLogin);
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
            GS.form.uspForm.handleValidationResponse('.js_emailErr', 'Please enter a valid email address.', emailField);
            dfd.reject();
        }
        return dfd.promise();
    };


    this.handleEmailErrors = function (data, email, emailField, isLogin) {
        var isValid = false;
        if (data.isEmailValid !== true) {
            GS.form.uspForm.handleValidationResponse('.js_emailErr', 'Please enter a valid email address.', emailField);
        } else if (isLogin === true && data.isNewUser === true) {
            GS.form.uspForm.handleValidationResponse('.js_emailErr', "<a href='#' id='js_lnchUspRegistration' >Register Here</a>.", emailField);
        } else if (isLogin === false && data.isNewUser !== true) {
            GS.form.uspForm.handleValidationResponse('.js_emailErr', "<a href='#' id='js_lnchUspSignin'>Sign in Here</a>.", emailField);
        } else if (data.isNewUser !== true && data.isUserEmailValidated !== true) {
            GSType.hover.emailNotValidated.setEmail(email);
            var onclickStr = "'GSType.hover.emailNotValidated.show(); return false;'";
            GS.form.uspForm.handleValidationResponse('.js_emailErr', "Please <a href='#' onclick=" + onclickStr + ">verify your email</a>.", emailField);
        } else if (isLogin === true && data.isCookieMatched !== true) {
            GS.form.uspForm.handleValidationResponse('.js_emailErr', 'The password you entered is incorrect.', emailField);
        } else {
            isValid = true;
        }
        return isValid;
    };

    this.handleValidationResponse = function (fieldSelector, errorMsg, elem) {
        var fieldError = jQuery(fieldSelector + '.invalid');
        var fieldValid = jQuery(fieldSelector + '.success');

        if (elem != undefined) {
            GS.form.uspForm.removeWarningClassFromElem(elem);
        }

        fieldError.hide();
        fieldValid.hide();

        if (errorMsg != undefined && errorMsg != '') {
            if (elem != undefined) {
                GS.form.uspForm.addWarningClassToElem(elem);
            }
            fieldError.find('.bd').html(errorMsg); // set error message
            fieldError.show();
            return false;
        } else {
            fieldValid.show();
            return true;
        }
    };

    this.addWarningClassToElem = function (elem) {
        elem.addClass("warning");
    };

    this.removeWarningClassFromElem = function (elem) {
        elem.removeClass("warning");
    };

    this.hideErrors = function (errorClass, elem) {
        GS.form.uspForm.handleValidationResponse(errorClass, '', elem);
    };

    this.validateUspDataAndShowHover = function (uspForm, isOspUser) {
        var data = uspForm.serializeArray();
        var isUserSignedIn = GS.isSignedIn();
        if (!isOspUser && !GS.form.uspForm.doUspFormValidations(data)) {
            alert('Please fill in at-least 1 form field.');
        } else if(isOspUser && !GS.form.uspForm.doUspFormValidationsForOspUser(uspForm)) {
            alert('Please provide at least 1 response for all fields');
        }
        else if (isUserSignedIn === true) {
            GS.form.uspForm.saveForm(uspForm);
        } else {
            GSType.hover.modalUspSignIn.show();
        }
    };

    this.registerAndSaveData = function (uspForm, uspRegistrationFirstNameField, uspRegistrationPasswordField, uspRegistrationEmailField) {
        //First do validations and then save the form.
        jQuery.when(
            GS.form.uspForm.validateUserState(uspRegistrationEmailField, false),
            GS.form.uspForm.validatePassword(uspRegistrationPasswordField),
            GS.form.uspForm.validateFirstName(uspRegistrationFirstNameField),
            GS.form.uspForm.validateTerms(uspRegistrationFirstNameField)
        ).done(
            function () {
                var firstName = jQuery.trim(uspRegistrationFirstNameField.val());
                var password = uspRegistrationPasswordField.val();
                var email = jQuery.trim(uspRegistrationEmailField.val());
                jQuery.when(
                    GS.form.uspForm.saveForm(uspForm, firstName, password, email)
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
        )
    };

    this.loginAndSaveData = function (uspForm, uspLoginPasswordField, uspLoginEmailField) {
        //First do validations and then save the form.
        jQuery.when(
            GS.form.uspForm.validateUserState(uspLoginEmailField, true, uspLoginPasswordField)
        ).done(
            function () {
                var password = uspLoginPasswordField.val();
                var email = jQuery.trim(uspLoginEmailField.val());
                GS.form.uspForm.saveForm(uspForm, '', password, email);
//                jQuery.when(
//                    GS.form.uspForm.saveForm(uspForm, '', password, email)
//                ).done(function (data) {
//                        alert(data);
//                        GSType.hover.modalUspSignIn.hide();
//                    })
//                    .fail(function () {
//                        GSType.hover.modalUspSignIn.hide();
//                        alert("Sorry! There was an unexpected error saving your form. Please wait a minute and try again.");
//                    });
            }
        ).fail(
            function () {
                // Error messages are already displayed as part of ajax validations.
            }
        )
    };

    this.saveForm = function (uspForm, firstName, password, email) {
        var dfd = jQuery.Deferred();

        var data = uspForm.serializeArray();

        if (firstName != undefined && firstName != '') {
            data.push({name:"firstName", value:firstName});
        }
        if (password != undefined && password != ''
            && email != undefined && email != '') {
            data.push({name:"email", value:email}, {name:"firstName", value:firstName},
                {name:"password", value:password}, {name:"terms", value:"true"});
        }

        jQuery.ajax({type:'POST',
                async:true,
                url:document.location,
                data:data}
        ).fail(function () {
                dfd.reject();
                alert("Sorry! There was an unexpected error saving your form. Please wait a minute and try again.");
            }).done(function (data) {
                dfd.resolve();
                if(data.redirect != undefined && data.redirect != ''){
                   window.location = data.redirect;
                }

            });
        return dfd.promise();
    };

    this.hideAllErrors = function () {
        jQuery('.js_firstNameErr').hide();
        jQuery('.js_passwordErr').hide();
        jQuery('.js_emailErr').hide();
        jQuery('.js_termsErr').hide();
    };

    this.doUspFormValidations = function (data) {
        if (data.length === 0) {
            return false;
        }
        return true;
    };

    this.doUspFormValidationsForOspUser = function (uspForm) {
        var errors = [];
        var $formFields = uspForm.find('.js-formFields');
        $formFields.each(function(){
            var $this = $(this);

            var selectVal = $this.find('select').val();
            var otherVal = $this.find('.js-ospUserFields .js-otherResponse input[name=otherValue]').val();
            var noneChecked = $this.find('.js-ospUserFields .js-noneResponse .js-checkBoxSpriteOn:visible');

            if((selectVal === null || selectVal.length === 0) && otherVal.trim() === '' && noneChecked.length === 0) {
                errors.push('error')
            }
        });

        if(errors.length > 0) {
            return false;
        }

        return true;
    };
};

GS.form.uspForm = new GS.form.UspForm();

function uspSpriteCheckBoxes(containerLayer, fieldToSet, checkedValue, uncheckedValue) {
    container = $("." + containerLayer);
    checkOn = container.find(".js-checkBoxSpriteOn");
    checkOff = container.find(".js-checkBoxSpriteOff");
    checkBoxField = $("#" + fieldToSet);
    checkOff.on("click", function () {
        $(this).addClass(' dn');
        $(this).siblings().removeClass(' dn');
        // set the value for the hidden input field as [response_key]__none
        $(this).parent().find('input.js-noneValue').val(function(index, value){
            return value.substring(0, value.indexOf('__') + 2) + 'none';
        });
        checkBoxField.val(checkedValue);
    });
    checkOn.on("click", function () {
        $(this).addClass(' dn');
        $(this).siblings().removeClass(' dn');
        // set the value for the hidden input field as [response_key]__
        $(this).parent().find('input.js-noneValue').val(function(index, value){
            return value.substring(0, value.indexOf('__') + 2);
        });
        checkBoxField.val(uncheckedValue);
    });
}

jQuery(function () {
    uspSpriteCheckBoxes("js-needText", "form_needText", 1, 0);
    uspSpriteCheckBoxes("js-noneResponse", "formOther", 1, 0);
    uspSpriteCheckBoxes("js-otherResponse", "formOther", 1, 0);

    var uspForm = jQuery('#js_uspForm');

    var isOspUser = function() {
        return uspForm.find('.js-ospUserFields').length > 0;
    }

    uspForm.on('click', '.js_submit', function () {
        GS.form.uspForm.hideAllErrors();
        GS.form.uspForm.validateUspDataAndShowHover(uspForm, isOspUser());
        return false;
    });

    // sets the value of hidden other field as [response_key]__[other_text_field_value]
    jQuery('.js-otherText').on('change', function(){
        var $this = $(this);
        $this.parent().find('input.js-otherValue').val(function(index, value){
            return value.substring(0, value.indexOf('__') +  2) + $this.val();
        });
    });

    //The new way of doing modals puts duplicate Ids on the page.I dealt with it by
    //binding handlers to visible Ids.
    jQuery('body').on('blur', '.js_regFirstName:visible', function (event) {
        GS.form.uspForm.validateFirstName(jQuery(event.target));
    });

    jQuery('body').on('blur', '.js_regPassword:visible', function (event) {
        GS.form.uspForm.validatePassword(jQuery(event.target));
    });

    jQuery('body').on('blur', '.js_regEmail:visible', function (event) {
        GS.form.uspForm.validateUserState(jQuery(event.target), false);
    });

//    jQuery('body').on('blur', '.js_loginEmail:visible', function (event) {
//        GS.form.uspForm.validateUserState(jQuery(event.target), false,true);
//    });

    jQuery('body').on('click', '#js_lnchUspSignin', function () {
        GS.form.uspForm.hideAllErrors();
        GSType.hover.modalUspRegistration.hide();
        GSType.hover.modalUspSignIn.show();
    });

    jQuery('body').on('click', '#js_lnchUspRegistration', function () {
        GS.form.uspForm.hideAllErrors();
        GSType.hover.modalUspSignIn.hide();
        GSType.hover.modalUspRegistration.show();
    });

    jQuery('body').on('click', '.js_uspRegistrationSubmit:visible', function () {
        //TODO find a better way to get the form values.
        var uspRegistrationForm = jQuery('.js_uspRegistrationForm:visible');
        var uspRegistrationFirstNameField = uspRegistrationForm.find('.js_regFirstName');
        var uspRegistrationPasswordField = uspRegistrationForm.find('.js_regPassword');
        var uspRegistrationEmailField = uspRegistrationForm.find('.js_regEmail');
        GS.form.uspForm.registerAndSaveData(uspForm, uspRegistrationFirstNameField, uspRegistrationPasswordField, uspRegistrationEmailField);
    });

    jQuery('body').on('click', '.js_uspLoginSubmit:visible', function () {
        //TODO find a better way to get the form values.
        var uspLoginForm = jQuery('.js_uspLoginForm:visible');
        var uspLoginPasswordField = uspLoginForm.find('.js_loginPassword');
        var uspLoginEmailField = uspLoginForm.find('.js_loginEmail');
        GS.form.uspForm.loginAndSaveData(uspForm, uspLoginPasswordField, uspLoginEmailField);
    });

    //TODO forgot password

});