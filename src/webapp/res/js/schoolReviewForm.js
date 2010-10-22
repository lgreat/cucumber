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

if (GS == undefined) {
    var GS = {};
}
if (GS.form == undefined) {
    GS.form = {};
}

//id is the dom id of the form but should not contain the pound sign
GS.form.SchoolReviewForm = function(id) {
    this.emailTaken = false;
    this.emailValidated = false;
    this.overallRatingValidated = false;
    this.reviewTextValidated = false;
    this.posterValidated = false;
    this.termsOfUseValidated = false;
    this.formObject = jQuery('#' + id);
    this.submitButton = this.formObject.find('.continueButton');

    this.fields = {};
    this.fields.email = jQuery('#frmPRModule-email');
    this.fields.email.error = jQuery('#frmPRModule-email-error');
    this.fields.email.alert = jQuery('#frmPRModule-email-alert');
    this.fields.poster = this.formObject.find('[name="posterAsString"]');
    this.fields.poster.error = this.formObject.find('.poster-error');
    this.fields.review = this.formObject.find('#reviewText');
    this.fields.overallRating = this.formObject.find('#overallStarRating');
    this.fields.termsOfUse = this.formObject.find('#terms');
    this.fields.termsOfUse.error = this.formObject.find('.termsError');

    this.validateEmail = function() {
        var email = this.fields.email.val();

        if (email != '') {
            jQuery.getJSON('/community/registrationValidationAjax.page', {email:email, field:'email'},
                function(data) {
                    var valid = false;
                    if (data && data['email']) {
                        var emailErrorMessage = data['email'];
                        if (emailErrorMessage.indexOf("This email address is already registered") != -1) {
                            this.fields.email.alert.find('.bd').html("You will need to sign in when you submit your review.");
                            this.fields.email.error.hide();
                            this.fields.email.alert.show();
                            valid = true;
                            this.emailTaken = true;
                        } else {
                            this.fields.email.alert.hide();
                            this.fields.email.error.find('.bd').html(data['email']);
                            this.fields.email.error.show();
                            valid = false;
                        }
                    } else {
                        this.fields.email.error.hide();
                        valid = true;
                        if (GS.isCookieSet('emailVerified')) {
                            this.fields.email.alert.hide();
                        } else {
                            this.fields.email.alert.find('.bd').html("You will need to validate your email <br/>address when you submit your review.");
                            this.fields.email.alert.show();
                        }
                    }
                    GS_resizeColumns();
                    this.emailValidated = valid;
                }.gs_bind(this)
            );
        } else {
            this.fields.email.error.show();
            this.emailValidated = false;
            GS_resizeColumns();
        }
    };

    this.validatePoster = function() {
        var valid = false;
        if (this.fields.poster.val() == '' || this.fields.poster.val() == '- Select one -') {
            this.fields.poster.error.show();
            valid = false;
        } else {
            this.fields.poster.error.hide();
            valid = true;
        }
        this.posterValidated = valid;
        GS_resizeColumns();
    };
    
    this.validateReviewText = function() {
        var valid = true;
        if (jQuery('#frmPRModule #reviewText').val().length > 1200) {
            valid = false;
            alert("Please keep your comments to 1200 characters or less.");
        }
        if (GS_countWords(document.getElementById('reviewText')) < 15) {
            valid = false;
            alert("Please use at least 15 words in your comment.");
        }
        this.reviewTextValidated = valid;
        GS_resizeColumns();
    };

    this.validateOverallRating = function() {
        var valid = false;
        if (jQuery('#frmPRModule #overallStarRating').val() == 0) {
            jQuery('#frmPRModule .overallError').show();
            jQuery('#frmPRModule .overallError .error').show();
            valid = false;
        } else {
            valid = true;
        }
        this.overallRatingValidated = valid;
        GS_resizeColumns();
    };

    this.validateTermsOfUse = function() {
        var valid = false;
        if (this.fields.termsOfUse.attr("checked")) {
            this.fields.termsOfUse.error.hide();
            valid = true;
        } else {
            this.fields.termsOfUse.error.show();
            valid = false;
        }
        this.termsOfUseValidated = valid;
        GS_resizeColumns();
    };

    this.validateAll = function() {
        this.validateEmail();
        this.validatePoster();
        this.validateReviewText();
        this.validateOverallRating();
        this.validateTermsOfUse();
        GS_resizeColumns();
    };

    this.submitHandler = function() {
        if (this.emailValidated && this.posterValidated && this.reviewTextValidated
                && this.overallRatingValidated && this.emailValidated && this.termsOfUseValidated) {
            if (GS.showSchoolReviewHover(window.location.href)) {
                GS_postSchoolReview();
            }
        } else {
            this.validateAll();
            if (this.reviewTextValidated) {
                alert("Please complete your review to submit.");
            }
            return false;
        }
    };

    /////////// form setup - register event handlers /////////
    this.submitButton.click(this.submitHandler.gs_bind(this));

    jQuery('#frmPRModule [name="posterAsString"]').change(function() {
        if (this.value == 'parent') {
            jQuery('#frmPRModule .subStarRatings').show();
            jQuery('#frmPRModule .subStarRatings .teachers').show();
            jQuery('#frmPRModule .subStarRatings .principal').show();
            jQuery('#frmPRModule .subStarRatings .parents').show();

            //jQuery('#frmPRModule .moreAboutRatings').show();
        } else if (this.value == 'student') {
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
        GS_resizeColumns();
    });

    jQuery('#frmPRModule [name="comments"]').focus(function() {
        jQuery('#rateReview .commentsPopup').fadeIn("slow");
    });
    jQuery('#frmPRModule [name="comments"]').blur(function() {
        jQuery('#rateReview .commentsPopup').hide();
    });

    this.fields.email.blur(this.validateEmail.gs_bind(this));

    this.fields.poster.blur(this.validatePoster.gs_bind(this));

    this.fields.review.blur(this.validateReviewText.gs_bind(this));

    this.fields.overallRating.blur(this.validateOverallRating.gs_bind(this));

    this.fields.termsOfUse.change(this.validateTermsOfUse.gs_bind(this));

};



function showStars(numStars) {
    setDisplay(numStars);
}

function setStars(numStars) {
    document.getElementById('overallStarRating').value = numStars;
    setDisplay(numStars);
    return false;
}

function resetStars() {
    setDisplay(document.getElementById('overallStarRating').value);
}

function setDisplay(numStars) {
    document.getElementById('currentStarDisplay').style.width = 20*numStars + '%';
    var title = '';

    switch (parseInt(numStars)) {
        case 1: title = 'Unsatisfactory'; break;
        case 2: title = 'Below Average'; break;
        case 3: title = 'Average'; break;
        case 4: title = 'Above Average'; break;
        case 5: title = 'Excellent'; break;
        default: title = document.getElementById('hdnSchoolName').value; break;
    }
    document.getElementById('ratingTitle').innerHTML = title;
}

function clearSubmitFields() {
    if (document.getElementById('reviewText').value == 'Enter your review here') {
        document.getElementById('reviewText').value = "";
    }
}

var countWords = makeCountWords(150);

function GS_postSchoolReview(email, callerFormId) {
    clearSubmitFields();
    // then post the review
    jQuery.post('/school/review/postReview.page', jQuery('#frmPRModule').serialize(), function(data) {
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


});