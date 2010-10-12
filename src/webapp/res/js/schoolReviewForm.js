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

jQuery(function() {
    jQuery('#frmPRModule [name="posterAsString"]').change(function() {
        if (this.value == 'parent') {
            jQuery('#frmPRModule .subStarRatings').show();
            jQuery('#frmPRModule .subStarRatings li').show();
            jQuery('#frmPRModule .moreAboutRatings').show();
        } else if (this.value == 'student') {
            jQuery('#frmPRModule .subStarRatings').show();
            jQuery('#frmPRModule .subStarRatings > li').hide();
            jQuery('#frmPRModule .subStarRatings .student_only').show();
            jQuery('#frmPRModule .moreAboutRatings').show();
            jQuery('#frmPRModule .ratingCategoryPrompt').parent().show();
        } else {
            jQuery('#frmPRModule .subStarRatings').hide();
            jQuery('#frmPRModule .moreAboutRatings').hide();
        }
        GS_resizeColumns();
    });

    jQuery('#frmPRModule [name="comments"]').focus(function() {
        jQuery('#rateReview .commentsPopup').fadeIn("slow");
    });

    jQuery('#frmPRModule [name="comments"]').blur(function() {
        jQuery('#rateReview .commentsPopup').hide();
    });

    jQuery('#frmPRModule .continueButton').click(function() {
        //TODO: validate email
        //TODO: check for existing email
        jQuery('#frmPRModule .errors').hide();
        var hasError = false;
        if (jQuery('#frmPRModule #overallStarRating').val() == 0) {
            jQuery('#frmPRModule .overallError').show();
            jQuery('#frmPRModule .overallError .error').show();
            hasError = true;
        }
        if (jQuery('#frmPRModule [name="posterAsString"]').val() == '') {
            jQuery('#frmPRModule .whoError').show();
            jQuery('#frmPRModule .whoError .error').show();
            hasError = true;
        }
        if (jQuery('#frmPRModule #reviewText').val().length > 1200) {
            hasError = true;
            alert("Please keep your comments to 1200 characters or less.")
        }
        if (GS_countWords(document.getElementById('reviewText')) < 15) {
            hasError = true;
            alert("Please use at least 15 words in your comment.")
        }

        // If the user is signed in, dont validate their email. Instead, check for existing errors and act accordingly.
        // If user is not signed in, validate the provided email. Email validation should fail with "email exists" error
        // if email exists and the associated account has a password. Otherwise as long as provided email is not
        // malformed, validation should succeed and a new account is created.
        if (!GS.isSignedIn()) {
            alert("user is not signed in");
            var email = jQuery('#frmPRModule [name="email"]').val();
            if (email == '') {
                jQuery('#frmPRModule .emailError').show();
                jQuery('#frmPRModule .emailError .error').show();
            } else {
                jQuery.getJSON('/community/registrationValidationAjax.page', {email:email, field:'email'},
                function(data) {
                    alert(data['email']);
                    if (data && data['email']) {
                        alert('email error: ' + data['email']);
                        jQuery('#frmPRModule .emailError').html(data['email']);
                        jQuery('#frmPRModule .emailError').show();
                        jQuery('#frmPRModule .emailError a.launchSignInHover').click(function() {
                            GSType.hover.joinHover.showSignin();
                            return false;
                        });
                        hasError = true;
                    } else {
                        if (!hasError) {
                            GS_postSchoolReview(email);
                        } else {
                            GS_resizeColumns();
                        }
                    }
                });
            }
        } else {
            if (!hasError) {
                GS_postSchoolReview(email);
            } else {
                GS_resizeColumns();
            }
        }

        return false;
    });
});