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
        var a = text.replace(/\n/g, ' ').replace(/\t/g, ' ');
        var z = 0;
        for (; z < a.length; z++) {
            if (a.charAt(z) == ' ' && a.charAt(z - 1) != ' ') { count++; }
        }
        return count + 1; // # of words is # of spaces + 1
    };

    this.validateEmail = function(email, successCallback, failCallback) {
        var url = '/community/registrationValidationAjax.page';

        if (email !== undefined && email.length > 0 && successCallback !== undefined) {
            jQuery.getJSON(url, {email: email, field: 'email'},
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
            validationResult.addError('Please keep your comments under 1200 characters.');
        }
        if (this.countWords(reviewText) < 15) {
            validationResult.setValid(false);
            validationResult.addError('Please use at least 15 words in your comment.');
        }
        return validationResult;
    };

};

/**
 * Do not use new operator on this object until after form is rendered
 * @constructor
 * @param {string} id the dom id of the form but should not contain the pound sign.
 */
GS.form.SchoolReviewForm = function(id) {
    this.submitButton = jQuery('#parentReviewFormSubmit');
    var form = jQuery('#' + id);
    this.submitSuccessRedirect = window.location.href;

    this.getForm = function() {
        return form;
    };

    //Email
    this.email = new function Email() {
        var componentName = 'email';
        var formComponent = form.find('.' + componentName);
        var element = formComponent.find('input');
        var errorElement = formComponent.find('.form-error');
        var alertElement = formComponent.find('.form-alert');
        var defaultValue = '[Enter email address]';
        var valid = false;
        var validator = new GS.review.Validator();
        var error = null; // field only supports one error even though validation result might(should) support multiple
        var alertText = null;
        var emailTaken = false;

        this.getElement = function() {
            return element;
        };
        this.isValid = function() {
            return error === undefined || error === null || error.length === 0;
        };
        this.isEmailTaken = function() {
            return emailTaken;
        };
        this.setError = function(message) {
            error = message;
        };
        this.removeError = function() {
            error = null;
        };
        this.showError = function(message) {
            if (message !== undefined) {
                error = message;
            }
            errorElement.find('.bk').html(error);
            errorElement.show();
        };
        this.hideError = function() {
            errorElement.hide();
        };
        this.setAlert = function(message) {
            alertText = message;
        };
        this.removeAlert = function() {
            alertText = null;
        };
        this.showAlert = function(message) {
            if (message !== undefined) {
                alertText = message;
            }
            alertElement.find('.bk').html(alertText);
            alertElement.show();
        };
        this.hideAlert = function() {
            alertElement.hide();
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
            var email = element.val();

            var emailAjaxValidationFailed = function(validationResult) {
                var errors = validationResult.getErrors();
                var emailErrorMessage = errors[0];
                if (emailErrorMessage.indexOf('This email address is already registered') != -1) {
                    this.setAlert('You will need to sign in when you submit.');
                    this.removeError();
                    emailTaken = true;
                } else {
                    error = emailErrorMessage;
                    this.removeAlert();
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
                    this.setAlert('You must verify your email address on submit.');
                }
                emailTaken = false;
                this.updateErrorDisplay();
            }.gs_bind(this);

            if (email !== '' && email !== defaultValue) {
                validator.validateEmail(email, emailAjaxValidationPassed, emailAjaxValidationFailed);
            } else {
                error = 'Please enter a valid email address.';
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
        var componentName = 'role';
        var formComponent = form.find('.' + componentName);
        var element = formComponent.find('select');
        var errorElement = formComponent.find('.form-error');
        var defaultValue = '- Select one -';
        var error = null;
        this.isValid = function() {
            return error === undefined || error === null || error.length === 0;
        };
        this.setError = function(message) {
            error = message;
        };
        this.showError = function(message) {
            if (message !== undefined) {
                this.setError(message);
            }
            //errorElement.html(error);
            errorElement.show();
        };
        this.hideError = function() {
            errorElement.hide();
        };
        this.updateErrorDisplay = function() {
            if (this.isValid()) {
                this.hideError();
            } else {
                this.showError();
            }
        };
        this.getElement = function() {
            return element;
        };
        this.validate = function() {
            if (element.val() === '' || element.val() === defaultValue) {
                error = 'Please indicate your relationship to the school.';
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
        var componentName = 'review-text';
        var formComponent = form.find('.' + componentName);
        var element = formComponent.find('textarea');
        var errorElement = formComponent.find('.form-error');
        var defaultValue = '[Enter your review here]';
        var valid = false;
        var validator = new GS.review.Validator();
        var error = '';

        this.updateErrorDisplay = function() {
            if (this.isValid()) {
                //this.hideError();
            } else {
                this.showError();
            }
        };
        this.isValid = function() {
            return error === undefined || error === null || error.length === 0;
        };
        this.showError = function(message) {
            if (message !== undefined) {
                error = message;
            }
            alert(error);
        };
        this.getElement = function() {
            return element;
        };
        this.validate = function() {
            var reviewText = element.val();
            var result = validator.validateReviewText(reviewText);

            if (result.isValid() === false) {
                if (result.errorCount() > 0) {
                    var errors = result.getErrors();
                    error = errors[0];
                }
            } else {
                error = null;
            }

        }.gs_bind(this);

        this.validateAndDisplayErrors = function() {
            this.validate();
            this.updateErrorDisplay();
        }.gs_bind(this);
    };

    //overall star rating
    this.overallRating = new function() {
        var componentName = 'rating';
        var formComponent = form.find('.' + componentName);
        //var element = formComponent.find('input');
        var errorElement = formComponent.find('.form-error');
        var element = jQuery('#overallStarRating');
        var defaultValue = '0';
        var error = null;

        this.isValid = function() {
            return error === undefined || error === null || error.length === 0;
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
            errorElement.find('.bk').html(error);
            errorElement.show();
        };
        this.hideError = function() {
            errorElement.hide();
        };
        this.getElement = function() {
            return element;
        };
        this.getFormComponent = function() {
            return formComponent;
        };
        this.validate = function() {
            if (element.val() === defaultValue) {
                error = 'Please select a star rating for this school.';
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
        var componentName = 'terms';
        var formComponent = form.find('.' + componentName);
        var element = formComponent.find('input');
        var errorElement = formComponent.find('.form-error');
        var error = null;

        this.updateErrorDisplay = function() {
            if (this.isValid()) {
                this.hideError();
            } else {
                this.showError();
            }
        };
        this.isValid = function() {
            return error === undefined || error === null || error.length === 0;
        };
        this.showError = function(message) {
            errorElement.show();
        };
        this.hideError = function() {
            errorElement.hide();
        };
        this.getElement = function() {
            return element;
        };
        this.validate = function() {
            var checked = element.attr('checked');
            if (checked === true) {
                error = null;
                this.updateErrorDisplay();
            } else {
                error = 'Please read and accept our Terms of Use to join GreatSchools.';
            }
        }.gs_bind(this);

        this.validateAndDisplayErrors = function() {
            this.validate();
            this.updateErrorDisplay();
        }.gs_bind(this);
    };

    this.needToValidateEmail = function() {
        var disabled = this.email.getElement().attr('disabled');
        return !disabled || !GS.isMember();
    };

    this.updateAllErrors = function() {
        this.email.updateErrorDisplay();
        this.overallRating.updateErrorDisplay();
        this.poster.updateErrorDisplay();
        this.termsOfUse.updateErrorDisplay();
        this.review.updateErrorDisplay();
    }.gs_bind(this);

    this.formValid = function() {
        return (this.email.isValid() || !this.needToValidateEmail()) && this.poster.isValid() &&
                this.review.isValid() && this.overallRating.isValid() && this.termsOfUse.isValid();
    }.gs_bind(this);

    this.resetStars = function() {
        this.showStars(this.overallRating.getElement().val());
    }.gs_bind(this);

    this.setStars = function(numStars) {
        this.overallRating.getElement().val(numStars);
        this.overallRating.validateAndDisplayErrors();
        this.showStars(numStars);
    }.gs_bind(this);

    this.showStars = function(numStars) {
        document.getElementById('currentStarDisplay').style.width = 20 * numStars + '%';
        this.updateStarRatingTitle(numStars);
    }.gs_bind(this);

    this.updateStarRatingTitle = function(numStars) {
        var title = '';
        switch (parseInt(numStars)) {
            case 1: title = 'Unsatisfactory'; break;
            case 2: title = 'Below Average'; break;
            case 3: title = 'Average'; break;
            case 4: title = 'Above Average'; break;
            case 5: title = 'Excellent'; break;
            default: title = ''; break;
        }
        jQuery('#ratingTitle').html(title);
    };

    this.updateAdditionalStarRatings = function(poster) {
        var starRatingsElement = form.find('.subStarRatings');
        if (poster === 'parent') {
            starRatingsElement.show();
            starRatingsElement.find('.teachers').show();
            starRatingsElement.find('.principal').show();
            starRatingsElement.find('.parents').show();
        } else if (poster === 'student') {
            starRatingsElement.show();
            starRatingsElement.find('.teachers').show();
            starRatingsElement.find('.principal').hide();
            starRatingsElement.find('.parents').hide();
        } else {
            starRatingsElement.hide();
            starRatingsElement.find('.teachers').hide();
            starRatingsElement.find('.principal').hide();
            starRatingsElement.find('.parents').hide();
        }
    };

    this.onRoleChanged = function() {
        var poster = this.poster.getElement().val();
        this.updateAdditionalStarRatings(poster);
        this.poster.validateAndDisplayErrors();
    }.gs_bind(this);

    this.postReview = function(email, callerFormId) {
        jQuery('#parentReviewFormSubmit').attr('disabled','disabled');
        var url = '/school/review/postReview.page';
        //When this is called by the "sign in" handler, overwrite review form's email with whatever user signed in with.
        if (email != undefined && email != '') {
            this.email.getElement().val(email);
        }
        var formData = this.serialize();
        jQuery.post(url, formData, function(data) {
            if (data.showHover !== undefined && data.showHover === 'emailNotValidated') {
                GSType.hover.emailNotValidated.show();
                return false;
            } else if (data.showHover !== undefined && data.showHover === 'validateEmailSchoolReview') {
                subCookie.setObjectProperty('site_pref', 'showHover', 'validateEmailSchoolReview', 3);
            } else {
                if (data.reviewPosted !== undefined) {
                    if (data.reviewPosted === 'true') {
                        // cookie to show schoolReviewPostedThankYou hover
                        subCookie.setObjectProperty('site_pref', 'showHover', 'schoolReviewPostedThankYou', 3);
                    } else {
                        // cookie to show schoolReviewNotPostedThankYou hover
                        subCookie.setObjectProperty('site_pref', 'showHover', 'schoolReviewNotPostedThankYou', 3);
                    }
                }
            }

            var successEvents = '';
            if (data.ratingEvent !== undefined) {
                successEvents += data.ratingEvent;
            }
            if (data.reviewEvent !== undefined) {
                successEvents += data.reviewEvent;
            }
            if (successEvents !== '') {
                pageTracking.clear();
                pageTracking.successEvents = successEvents;
                pageTracking.send();
            }
            var redirectUrl = window.location.href;
            var reloading = true;
            if (data.redirectUrl !== undefined) {
                redirectUrl = data.redirectUrl;
                GSType.hover.signInHover.setRedirect(data.redirectUrl);
                reloading = false;
            }
            if (callerFormId) {
                GSType.hover.signInHover.hide();
                GSType.hover.joinHover.hide();
                jQuery('#' + callerFormId).submit();
            } else {
                window.location.href = redirectUrl;
                if (reloading) {
                    window.location.reload();
                }
            }
        }, 'json');
    }.gs_bind(this);

    this.submitHandler = function() {
        if (this.formValid()) {
            if (GS.isSignedIn() || !this.email.isEmailTaken()) {
                this.postReview();
            } else {
                jQuery('#signInHover h2 span').hide();
                //show the "sign in" hover, and tell it to execute the postReview method if users credentials are valid
                GSType.hover.signInHover.showHover(
                        '',
                        window.location.href,
                        GSType.hover.joinHover.showSchoolReviewJoin,
                        this.postReview
                );
            }
        } else {
            this.updateAllErrors();
        }
        return false;
    }.gs_bind(this);

    this.serialize = function() {
        var serialized = {};
        form.find(':input').not(':checkbox').each(function() {
            var name = jQuery(this).attr('name');

            if (name !== undefined) {
                var value = jQuery(this).val();
                serialized[name] = value;
            }
        });
        form.find(':checkbox:visible').each(function() {
            var name = jQuery(this).attr('name');
            var value = jQuery(this).attr('checked');
            serialized[name] = value;
        });
        return serialized;
    };

    /////////// form setup - register event handlers /////////
    this.attachEventHandlers = function() {
        this.submitButton.click(this.submitHandler);

        this.overallRating.getElement().blur(this.overallRating.validateAndDisplayErrors);
        this.poster.getElement().change(this.onRoleChanged);
        this.termsOfUse.getElement().click(this.termsOfUse.validateAndDisplayErrors);
        if (this.needToValidateEmail()) {
            this.email.getElement().blur(this.email.validateAndDisplayErrors);
        }
        this.review.getElement().blur(this.review.validate);

        var self = this; //jQuery sets the "this" scope in callback below. I need to reference "this" scope as it is now
        this.overallRating.getFormComponent().find('a').each(function() {
            var link = jQuery(this);
            var numStars = link.html();
            link.mouseover(function() {
                self.showStars(numStars);
            }.gs_bind(this));
            link.mouseout(function() {
                self.resetStars();
            }.gs_bind(this));
            link.click(function() {
                self.setStars(numStars);
            });
        });

    }.gs_bind(this);

    //this.review.blur(this.validateReviewText.gs_bind(this)); (GS-10810)

    this.validateOnLoad = function() {
        if (this.needToValidateEmail()) {
            this.email.validate();
        }
        this.poster.validate();
        this.overallRating.validate();
        this.termsOfUse.validate();
        this.review.validate();
    }.gs_bind(this);

    //Constructor code
    this.attachEventHandlers();
    this.validateOnLoad();
};



