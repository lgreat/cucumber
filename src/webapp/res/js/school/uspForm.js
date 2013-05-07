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

    this.validateUserState = function (elem) {
        var fieldVal = jQuery.trim(elem.val());
        var dfd = jQuery.Deferred();

        GS.form.uspForm.hideErrors('.js_emailErr', elem);

        if (fieldVal !== "" && fieldVal !== undefined) {
            jQuery.ajax({
                type:'GET',
                url:'/school/usp/checkUserState.page',
                data:{email:fieldVal},
                dataType:'json',
                async:true
            }).done(
                function (data) {
                    var isValid = GS.form.uspForm.handleEmailErrors(data, fieldVal, elem);
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
            GS.form.uspForm.handleValidationResponse('.js_emailErr', 'Please enter a valid email address.', elem);
            dfd.reject();
        }
        return dfd.promise();
    };

    this.handleEmailErrors = function (data, email, emailField) {
        var isValid = false;
        if (data.isEmailValid !== true) {
            GS.form.uspForm.handleValidationResponse('.js_emailErr', 'Please enter a valid email address.', emailField);
        } else if (data.isNewUser !== true && data.isUserEmailValidated === true) {
            GS.form.uspForm.handleValidationResponse('.js_emailErr', "Whoops!  It looks like you're already a member.  Please <a href='/official-school-profile/signin.page?email=" + encodeURIComponent(email) + "'>sign in</a> here.", emailField);
        } else if (data.isNewUser !== true && data.isUserEmailValidated !== true) {
            GSType.hover.emailNotValidated.setEmail(email);
            var onclickStr = "'GSType.hover.emailNotValidated.show(); return false;'";
            GS.form.uspForm.handleValidationResponse('.js_emailErr', "Please <a href='#' onclick=" + onclickStr + ">verify your email</a>.", emailField);
        } else {
            isValid = true;
        }
        return isValid;
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

    this.hideErrors = function (errorClass, elem) {
        GS.form.uspForm.handleValidationResponse(errorClass, '', elem);
    };

    this.validateUspDataAndShowHover = function (uspForm) {
        var data = uspForm.serializeArray();
        if (!GS.form.uspForm.doValidations(data)) {
            alert('Please fill in at-least 1 form field.');
        } else {
            GSType.hover.modalUspRegistration.show();
        }
    };

    this.saveFormAndLoginOrRegister = function (uspForm, uspRegistrationForm, uspRegistrationFirstNameField, uspRegistrationPasswordField, uspRegistrationEmailField) {
        jQuery.when(
            GS.form.uspForm.validateUserState(uspRegistrationEmailField),
            GS.form.uspForm.validatePassword(uspRegistrationPasswordField),
            GS.form.uspForm.validateFirstName(uspRegistrationFirstNameField)
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

    this.saveForm = function (uspForm, firstName, password, email) {
        var dfd = jQuery.Deferred();

        var data = uspForm.serializeArray();
        data.push({name:"email", value:email}, {name:"firstName", value:firstName},
            {name:"password", value:password}, {name:"terms", value:"true"});

        jQuery.ajax({type:'POST',
                async:true,
                url:document.location,
                data:data}
        ).fail(function () {
                dfd.reject();
                GSType.hover.modalUspRegistration.hide();
                alert("Sorry! There was an unexpected error saving your form. Please wait a minute and try again.");
            }).done(function () {
                dfd.resolve();
            });
        return dfd.promise();
    };

    this.doValidations = function (data) {
        if (data.length === 0) {
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

    var uspForm = jQuery('#js_uspForm');
    uspForm.on('click', '.js_submit', function () {
        GS.form.uspForm.validateUspDataAndShowHover(uspForm);
        return false;
    });

    //The new way of doing modals puts duplicate Ids on the page.This is a way to deal with it is to
    // bind handlers to visible Ids.
    jQuery('body').on('blur', '.js_firstName:visible', function (event) {
        GS.form.uspForm.validateFirstName(jQuery(event.target));
    });

    jQuery('body').on('blur', '.js_password:visible', function (event) {
        GS.form.uspForm.validatePassword(jQuery(event.target));
    });

    jQuery('body').on('blur', '.js_email:visible', function (event) {
        GS.form.uspForm.validateUserState(jQuery(event.target));
    });

    jQuery('body').on('click', '.js_uspRegistrationSubmit:visible', function () {
        //TODO find a better way to get the form values.
        var uspRegistrationForm = jQuery('.js_uspRegistrationForm:visible');
        var uspRegistrationFirstNameField = uspRegistrationForm.find('.js_firstName');
        var uspRegistrationPasswordField = uspRegistrationForm.find('.js_password');
        var uspRegistrationEmailField = uspRegistrationForm.find('.js_email');
        GS.form.uspForm.saveFormAndLoginOrRegister(uspForm, uspRegistrationForm, uspRegistrationFirstNameField, uspRegistrationPasswordField, uspRegistrationEmailField);
    });


});