function GS_countWords(textField) {
    var text = textField.value;
    var count = 0;
    var a = text.replace(/\n/g,' ').replace(/\t/g,' ');
    var z = 0;
    for (; z < a.length; z++) {
        if (a.charAt(z) == ' ' && a.charAt(z-1) != ' ') { count++; }
    }
    return count+1; // # of words is # of spaces + 1
}

Function.prototype.gs_bind = function(obj) {
    var method = this;
    return function() {
        return method.apply(obj, arguments);
    };
};


GS = GS || {};
GS.form = GS.form || {};
GS.review = GS.review || {};
GS.validation = GS.validation || {};

GS.validation.ValidationResult = function() {
    var result = {};
    result.errors = [];
    result.valid = true;

    this.addError = function(errorMessage) {
        result.errors.push(errorMessage);
    };

    this.setValid = function(valid) {
        result.valid = valid;
    };

    this.isValid = function() {
        return result.valid;
    };

    this.getErrors = function() {
        return result.errors;
    };

    this.hasErrors = function() {
        return result.errors.length > 0;
    };

    this.errorCount = function() {
        return result.errors.length;
    };
};

GS.review.Validator = function() {

    this.countWords = function(text) {
        var count = 0;
        var a = text.replace(/\n/g,' ').replace(/\t/g,' ');
        var z = 0;
        for (; z < a.length; z++) {
            if (a.charAt(z) == ' ' && a.charAt(z-1) != ' ') { count++; }
        }
        return count+1; // # of words is # of spaces + 1
    };

    this.validateEmail = function(email, successCallback, failCallback) {
        var url = '/community/registrationValidationAjax.page';

        if (email !== undefined && email.length > 0 && successCallback !== undefined) {
            jQuery.getJSON(url, {email:email, field:'email'},
                function(data) {
                    var validationResult = new GS.validation.ValidationResult();
                    var emailErrorMessage = data.email;
                    if (emailErrorMessage !== undefined && emailErrorMessage.length > 0) {
                        validationResult.addError(emailErrorMessage);
                        validationResult.setValid(false);
                        failCallback(validationResult);
                    } else {
                        validationResult.setValid(true);
                        successCallback(validationResult);
                    }
                }.gs_bind(this)
            );
        }
    };

    this.validateReviewText = function(reviewText) {
        var validationResult = new GS.validation.ValidationResult();

        if (reviewText.length > 1200) {
            validationResult.setValid(false);
            validationResult.addError("Please keep your comments to 1200 characters or less.");
        }
        if (this.countWords(reviewText) < 15) {
            validationResult.setValid(false);
            validationResult.addError("Please use at least 15 words in your comment.");
        }
        return validationResult;
    };
    
};


//id is the dom id of the form but should not contain the pound sign
GS.form.SchoolReviewForm = function(id) {
    this.formObject = jQuery('#' + id);
    this.submitButton = jQuery('#frmPRModule-submit');

    //Email
    this.email = new function() {
        this.element = jQuery('#frmPRModule-email');
        this.defaultValue = "[Enter email address]";
        this.validationResult = {};
        this.valid = false;
        this.validator = new GS.review.Validator();
        var error = null; // field only supports one error even though validation result might(should) support multiple
        var alertText = null;
        var emailTaken = false;

        this.isValid = function() {
            return error === undefined || error === null || error.length == 0;
        };
        this.isEmailTaken = function() {
            return emailTaken;
        };
        this.setError = function(message) {
            error = message;
        };
        this.showError = function(message) {
            if (message !== undefined) {
                error = message;
            }
            jQuery('#frmPRModule-email-error .bd').html(error);
            jQuery('#frmPRModule-email-error').show();
        };
        this.hideError = function() {
            jQuery('#frmPRModule-email-error').hide();
        };
        this.setAlert = function(message) {
            alertText = message;
        };
        this.showAlert = function(message) {
            if (message !== undefined) {
                alertText = message;
            }
            jQuery('#frmPRModule-email-alert .bd').html(alertText);
            jQuery('#frmPRModule-email-alert').show();
        };
        this.hideAlert = function() {
            jQuery('#frmPRModule-email-alert').hide();
        };
        this.showEmailTakenAlert = function() {
            this.hideError();
            this.showAlert("You will need to sign in when you submit your review.");
        };

        this.updateErrorDisplay = function() {
            if (!this.isValid()) {
                this.hideAlert();
                this.showError();
            } else if (alertText !== undefined && alertText !== null) {
                this.hideError();
                this.showAlert();
            } else {
                this.hideError();
                this.hideAlert();
            }
        };

        this.validate = function(displayErrors) {
            var email = this.element.val();

            var emailAjaxValidationFailed = function(validationResult) {
                var errors = validationResult.getErrors();
                var emailErrorMessage = errors[0];
                if (emailErrorMessage.indexOf("This email address is already registered") != -1) {
                    this.setAlert("You will need to sign in when you submit your review.");
                    error = null;
                    emailTaken = true;
                } else {
                    error = emailErrorMessage;
                    this.setAlert(null);
                    emailTaken = false;
                }
                if (displayErrors === true) {
                    this.updateErrorDisplay();
                }
            }.gs_bind(this);

            var emailAjaxValidationPassed = function() {
                error = null;
                if (GS.isCookieSet('emailVerified')) {
                    alertText = null;
                } else {
                    this.setAlert("You will need to verify your email <br/>address when you submit your review.");
                }
                emailTaken = false;
                this.updateErrorDisplay();
            }.gs_bind(this);

            if (email !== '' && email !== this.defaultValue) {
                this.validator.validateEmail(email, emailAjaxValidationPassed, emailAjaxValidationFailed);
            } else {
                error = "Please enter a valid email address.";
                if (displayErrors === true) {
                    this.updateErrorDisplay();
                }
            }
        }.gs_bind(this);

        this.validateAndDisplayErrors = function() {
            this.validate(true);
        }.gs_bind(this);

        this.getError = function() {
            return error;
        }.gs_bind(this);
    };
    

    //Poster dropdown
    this.poster = new function() {
        this.element = jQuery('#affiliation');
        this.defaultValue = "- Select one -";
        this.errorElement = jQuery('#affiliation-error .bd');
        var error = null;

        this.isValid = function() {
            return error === undefined || error === null || error.length == 0;
        };
        this.setError = function(message) {
            error = message;
        };
        this.showError = function(message) {
            if (message !== undefined) {
                this.setError(message);
            }
            this.errorElement.html(error);
            this.errorElement.show();
            this.errorElement.parent().show();
        };
        this.hideError = function() {
            this.errorElement.hide();
            this.errorElement.parent().hide(); //TODO: fix having to modify multiple dom elements to hide and show error
        };
        this.updateErrorDisplay = function() {
            if (this.isValid()) {
                this.hideError();
            } else {
                this.showError();
            }
        };
        this.validate = function() {
            if (this.element.val() === '' || this.element.val() === this.defaultValue) {
                error = "Please let us know how you are affiliated with this school.";
            } else {
                error = null;
                this.updateErrorDisplay();
            }
        }.gs_bind(this);

        this.validateAndDisplayErrors = function() {
            this.validate();
            this.updateErrorDisplay();
        }.gs_bind(this);
    };

    //review text/comments
    this.review = new function() {
        this.element = jQuery('#reviewText');
        this.defaultValue = "[Enter your review here]";
        this.valid = false;
        this.validated = false;
        this.validator = new GS.review.Validator();
        var error = "";

        this.updateErrorDisplay = function() {
            if (this.isValid()) {
                //this.hideError();
            } else {
                this.showError();
            }
        };
        this.isValid = function() {
            return error === undefined || error === null || error.length == 0;
        };
        this.showError = function(message) {
            if (message !== undefined) {
                error = message;
            }
            alert(error);
        };
        this.validate = function() {
            var reviewText = this.element.val();
            var result = this.validator.validateReviewText(reviewText);

            if (result.isValid() === false) {
                if (result.errorCount() > 0) {
                    var errors = result.getErrors();
                    error = errors[0];
                }
            } else {
                error = null;
            }

            this.validated = true;
        }.gs_bind(this);

        this.validateAndDisplayErrors = function() {
            this.validate();
            this.updateErrorDisplay();
        }.gs_bind(this);
    };

    //overall star rating
    this.overallRating = new function() {
        this.element = jQuery('#overallStarRating');
        this.defaultValue = "0";
        var error = null;

        this.isValid = function() {
            return error === undefined || error === null || error.length == 0;
        };
        this.updateErrorDisplay = function() {
            if (this.isValid()) {
                this.hideError();
            } else {
                this.showError();
            }
        };
        this.showError = function(message) {
            if (message !== undefined) {
                error = message;
            }
            jQuery('#frmPRModule .overallError').show();
            jQuery('#frmPRModule .overallError .error').show();
        };
        this.hideError = function() {
            jQuery('#frmPRModule .overallError').hide();
            jQuery('#frmPRModule .overallError .error').hide();
        };

        this.validate = function() {
            if (jQuery('#frmPRModule #overallStarRating').val() === this.defaultValue) {
                error = "Please select a star rating for this school.";
            } else {
                error = null;
                this.updateErrorDisplay();
            }
        }.gs_bind(this);

        this.validateAndDisplayErrors = function() {
            this.validate();
            this.updateErrorDisplay();
        }.gs_bind(this);
    };

    this.termsOfUse = new function() {
        this.element = jQuery('#terms');
        var error = null;

        this.updateErrorDisplay = function() {
            if (this.isValid()) {
                this.hideError();
            } else {
                this.showError();
            }
        };
        this.isValid = function() {
            return error === undefined || error === null || error.length == 0;
        };
        this.showError = function(message) {
            jQuery('.termsError').show();
        };
        this.hideError = function() {
            jQuery('.termsError').hide();
        };
        this.validate = function() {
            var checked = this.element.attr("checked");
            if (checked === true) {
                error = null;
                this.updateErrorDisplay();
            } else {
                error = "Please read and accept our Terms of Use to join GreatSchools.";
            }
        }.gs_bind(this);
        
        this.validateAndDisplayErrors = function() {
            this.validate();
            this.updateErrorDisplay();
        }.gs_bind(this);
    };


    this.needToValidateEmail = function() {
        var disabled = this.email.element.attr("disabled");
        return !disabled || !GS.isMember();
    };

    this.updateAllErrors = function() {
        this.email.updateErrorDisplay();
        this.overallRating.updateErrorDisplay();
        this.review.updateErrorDisplay();
        this.poster.updateErrorDisplay();
        this.termsOfUse.updateErrorDisplay();
    }.gs_bind(this);

    this.formValid = function() {
        return (this.email.isValid() || !this.needToValidateEmail()) && this.poster.isValid() && this.review.isValid()
                && this.overallRating.isValid() && this.termsOfUse.isValid();
    }.gs_bind(this);

    this.submitHandler = function() {
        if (this.formValid()) {
            if (GS.isSignedIn() || !this.email.isEmailTaken()) {
                GS_postSchoolReview();
            } else {
                jQuery('#signInHover h2 span').hide();
                GSType.hover.signInHover.showHover("", window.location.href, GSType.hover.joinHover.showSchoolReviewJoin, GS_postSchoolReview);
            }
        } else {
            this.updateAllErrors();
        }
        return false;
    }.gs_bind(this);

    this.setStars = function(numStars) {
        jQuery('#overallStarRating').val(numStars);
        document.getElementById('currentStarDisplay').style.width = 20*numStars + '%';
        this.overallRating.validate();
    }.gs_bind(this);

    this.updateStarRatings = function(poster) {
        if (poster === 'parent') {
            jQuery('#frmPRModule .subStarRatings').show();
            jQuery('#frmPRModule .subStarRatings .teachers').show();
            jQuery('#frmPRModule .subStarRatings .principal').show();
            jQuery('#frmPRModule .subStarRatings .parents').show();
        } else if (poster === 'student') {
            jQuery('#frmPRModule .subStarRatings').show();
            jQuery('#frmPRModule .subStarRatings .teachers').show();
            jQuery('#frmPRModule .subStarRatings .principal').hide();
            jQuery('#frmPRModule .subStarRatings .parents').hide();
        } else {
            jQuery('#frmPRModule .subStarRatings').hide();
            jQuery('#frmPRModule .subStarRatings .teachers').hide();
            jQuery('#frmPRModule .subStarRatings .principal').hide();
            jQuery('#frmPRModule .subStarRatings .parents').hide();
        }
    };

    this.onPosterChanged = function() {
        var poster = this.poster.element.val();
        this.updateStarRatings(poster);
        this.poster.validateAndDisplayErrors();
    }.gs_bind(this);

    /////////// form setup - register event handlers /////////
    this.attachEventHandlers = function() {
        this.submitButton.click(this.submitHandler);

        this.overallRating.element.blur(this.overallRating.validateAndDisplayErrors);
        this.poster.element.change(this.onPosterChanged);
        this.termsOfUse.element.click(this.termsOfUse.validateAndDisplayErrors);
        if (this.needToValidateEmail()) {
            this.email.element.blur(this.email.validateAndDisplayErrors);
        }
        this.review.element.blur(this.review.validate);
        
        jQuery('#frmPRModule [name="comments"]').focus(function() {
            jQuery('#rateReview .commentsPopup').fadeIn("slow");
        });
        jQuery('#frmPRModule [name="comments"]').blur(function() {
            jQuery('#rateReview .commentsPopup').hide();
        });
        
    }.gs_bind(this);


    //this.review.blur(this.validateReviewText.gs_bind(this)); (GS-10810)

    this.validateOnLoad = function() {
        this.email.validate();
        this.poster.validate();
        this.overallRating.validate();
        this.review.validate();
        this.termsOfUse.validate();
    }.gs_bind(this);
    
    this.attachEventHandlers();
    this.validateOnLoad();

};


var countWords = makeCountWords(150);

function GS_postSchoolReview(email, callerFormId) {
    //When this method is called by the "sign in" handler, overwrite review form's email with whatever user signed in with.
    if (email != undefined && email != '') {
        GS.form.schoolReviewForm.email.element.val(email);
    }
    var formData = jQuery('#frmPRModule').serialize();
    jQuery.post('/school/review/postReview.page', formData, function(data) {
        if (data.showHover != undefined && data.showHover == "emailNotValidated") {
            GSType.hover.emailNotValidated.show();
            return false;
        } else if (data.showHover != undefined && data.showHover == "validateEmailSchoolReview") {
            subCookie.setObjectProperty("site_pref", "showHover", "validateEmailSchoolReview", 3);
        } else {
            if (data.reviewPosted != undefined) {
                if (data.reviewPosted == "true") {
                // cookie to show schoolReviewPostedThankYou hover
                subCookie.setObjectProperty("site_pref", "showHover", "schoolReviewPostedThankYou", 3);
                } else {
                    // cookie to show schoolReviewNotPostedThankYou hover
                    subCookie.setObjectProperty("site_pref", "showHover", "schoolReviewNotPostedThankYou", 3);
                }
            }
        }
        
        var successEvents = "";
        if (data.ratingEvent != undefined) {
            successEvents += data.ratingEvent;
        }
        if (data.reviewEvent != undefined) {
            successEvents += data.reviewEvent;
        }
        if (successEvents != "") {
            pageTracking.clear();
            pageTracking.successEvents = successEvents;
            pageTracking.send();
        }
        var redirectUrl = window.location.href;
        var reloading = true;
        if (data.redirectUrl != undefined) {
            redirectUrl = data.redirectUrl;
            GSType.hover.signInHover.setRedirect(data.redirectUrl);
            reloading = false;
        }
        if (callerFormId) {
            GSType.hover.signInHover.hide();
            GSType.hover.joinHover.hide();
            jQuery('#' + callerFormId).submit();
        } else {
            window.location.href=redirectUrl;
            if (reloading) {
                window.location.reload();
            }
        }
    }, "json");
}

jQuery(function() {
   GS.form.schoolReviewForm = new GS.form.SchoolReviewForm("frmPRModule");

    var starHintArray = new Array('Unsatisfactory','Below average','Average','Above average','Excellent');

    jQuery('.star-rating li').mouseover(function () {
        var starHint = jQuery(this).parent().next('.js-starRater').children('.js-starHint');
        var starHintIndex = parseInt(jQuery(this).text())-1;
        //console.log('starHintIndex: '+starHintIndex);
        starHint.text(starHintArray[starHintIndex]).show();
    }).mouseout(function () {
        jQuery('.js-starRater .js-starHint').hide();
    });
});