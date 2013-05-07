var GS = GS || {};
GS.form = GS.form || {};
GS.form.UspForm = function () {

    this.validateFirstName = function (elem) {
        var fieldVal = jQuery.trim(elem.val());
        var namereg = /[0-9<>&\\]/;
        var dfd = jQuery.Deferred();
        //TODO have a hide errors method
        GS.form.uspForm.handleValidationResponse('.js_firstNameErr', '', elem);
        if (fieldVal.length > 24 || fieldVal.length < 2) {
            GS.form.uspForm.handleValidationResponse('.js_firstNameErr', 'First name must be 2-24 characters long.', elem);
            dfd.reject();
        } else if (namereg.test(fieldVal)) {
            GS.form.uspForm.handleValidationResponse('.js_firstNameErr', 'Please remove the numbers or symbols.', elem);
            dfd.reject();
        }
        dfd.resolve();
    };

    this.validatePassword = function (elem) {
        var fieldVal = elem.val();
        //TODO have a hide errors method
        var dfd = jQuery.Deferred();
        GS.form.uspForm.handleValidationResponse('.js_passwordErr', '', elem);
        if (fieldVal.length < 6 || fieldVal.length > 14) {
            GS.form.uspForm.handleValidationResponse('.js_passwordErr', 'Password should be 6-14 characters.', elem);
            dfd.reject();
        }
        dfd.resolve();
    };

    this.validateUserState = function (elem, errorElem) {
        var fieldVal = jQuery.trim(elem.val());
        var dfd = jQuery.Deferred();
        errorElem.hide();
        GS.form.uspForm.removeWarningClassFromElem(elem);

        if (fieldVal !== "" && fieldVal !== undefined) {
            jQuery.ajax({
                type:'GET',
                url:'/school/usp/checkUserState.page',
                data:{email:fieldVal},
                dataType:'json',
                async:true
            }).done(
                function (data) {
                    var isValid = GS.form.uspForm.handleEmailErrors(data, fieldVal, elem, errorElem);
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
            alert("else");
            GS.form.uspForm.showEmailError("Please enter a valid email address.", elem, errorElem);
            dfd.reject();
        }
    };

    this.handleEmailErrors = function (data, email, emailField, errorField) {
        var isValid = false;
        if (data.isEmailValid !== true) {
            GS.form.uspForm.showEmailError("Please enter a valid email address.", emailField, errorField);
        } else if (data.isNewUser !== true && data.isUserEmailValidated === true) {
            GS.form.uspForm.showEmailError("Whoops!  It looks like you're already a member.  Please <a href='/official-school-profile/signin.page?email=" + encodeURIComponent(email) + "'>sign in</a> here.", emailField, errorField);
        } else if (data.isNewUser !== true && data.isUserEmailValidated !== true) {
            GSType.hover.emailNotValidated.setEmail(email);
            var onclickStr = "'GSType.hover.emailNotValidated.show(); return false;'";
            GS.form.uspForm.showEmailError("Please <a href='#' onclick=" + onclickStr + ">verify your email</a>.", emailField, errorField);
        } else {
            isValid = true;
        }
        return isValid;
    };

    this.showEmailError = function (errMsg, emailField, errorField) {
        errorField.html('<div class="media"><span class="iconx16 i-16-alert img mrs"></span><div class="bd">' + errMsg + '</div></div>');
        GS.form.uspForm.addWarningClassToElem(emailField);
        errorField.show();
    };

    this.handleValidationResponse = function (fieldSelector, errorMsg, elem) {
        var fieldError = jQuery(fieldSelector + '.invalid');
        var fieldValid = jQuery(fieldSelector + '.success');

        GS.form.uspForm.removeWarningClassFromElem(elem);
        fieldError.hide();
        fieldValid.hide();

        if (errorMsg != undefined && errorMsg != '') {
            GS.form.uspForm.addWarningClassToElem(elem);
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

    this.saveForm = function (form, firstName, password, email) {
        var masterDeferred = new jQuery.Deferred();

        var data = form.serializeArray();
        if (!GS.form.uspForm.doValidations(data)) {
            alert('show error');
            return masterDeferred.reject().promise();
        }

        var data = form.serializeArray();
        data.push({name:"email", value:email}, {name:"firstName", value:firstName},
            {name:"password", value:password}, {name:"terms", value:"true"});

        jQuery.ajax({type:'POST', url:document.location, data:data}
        ).fail(function () {
                masterDeferred.reject();
                alert("Sorry! There was an unexpected error saving your form. Please wait a minute and try again.");
            }).done(function () {
            });
    };

    this.doValidations = function (data) {
        if(data.length === 0) {
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
        $(this).hide();
        $(this).siblings().show();
        checkBoxField.val(checkedValue);
    });
    checkOn.on("click", function () {
        $(this).hide();
        $(this).siblings().show();
        checkBoxField.val(uncheckedValue);
    });
}

jQuery(function () {
    uspSpriteCheckBoxes("js-needText", "form_needText", 1, 0);

    var uspRegistrationForm = jQuery('.js_uspRegistrationForm:visible');
    var uspRegistrationFirstNameField = uspRegistrationForm.find('.js_firstName');
    uspRegistrationFirstNameField.on('blur', function () {
        GS.form.uspForm.validateFirstName(uspRegistrationFirstNameField);
    });

    var uspRegistrationPasswordField = uspRegistrationForm.find('.js_password');
    uspRegistrationPasswordField.on('blur', function () {
        GS.form.uspForm.validatePassword(uspRegistrationPasswordField);
    });

    var uspRegistrationEmailField = uspRegistrationForm.find('.js_email');
    var uspRegistrationEmailErrorField = uspRegistrationForm.find('.js_emailErr');
    uspRegistrationEmailField.on('blur', function () {
        GS.form.uspForm.validateUserState(uspRegistrationEmailField, uspRegistrationEmailErrorField);
    });

    var uspRegistrationSubmitBtn = uspRegistrationForm.find('.js_uspRegistrationSubmit');
    uspRegistrationSubmitBtn.on('click', function () {
        jQuery.when(
            GS.form.uspForm.validateUserState(uspRegistrationEmailField, uspRegistrationEmailErrorField),
            GS.form.uspForm.validatePassword(uspRegistrationPasswordField),
            GS.form.uspForm.validateFirstName(uspRegistrationPasswordField)
        ).done(
            function () {
                var firstName = jQuery.trim(uspRegistrationFirstNameField.val());
                var password = uspRegistrationPasswordField.val();
                var email = jQuery.trim(uspRegistrationEmailField.val());

                GS.form.uspForm.saveForm(uspForm, firstName, password, email);
            }
        ).fail(
            function () {
                // Error messages are already displayed as part of ajax validations.
            }
        )
    });

    var uspForm = jQuery('#js_uspForm');
    uspForm.on('click', '.js_submit', function () {
        GSType.hover.modalUspRegistration.show();
        return false;
    });


});