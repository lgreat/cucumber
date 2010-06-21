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
    // first, grab the email from the join/signIn form and use that with the review
    if (email) {
        jQuery('#frmPRModule [name="email"]').val(email);
    }
    clearSubmitFields();
    // then post the review
    jQuery.post('/school/review/postReview.page', jQuery('#frmPRModule').serialize(), function(data) {
        if (data.reviewPosted != undefined) {
            if (data.reviewPosted == "true") {
                // cookie to show schoolReviewPostedThankYou hover
                subCookie.setObjectProperty("site_pref", "showHover", "schoolReviewPostedThankYou", 3);
            } else {
                // cookie to show schoolReviewNotPostedThankYou hover
                subCookie.setObjectProperty("site_pref", "showHover", "schoolReviewNotPostedThankYou", 3);
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
        if (!hasError) {
            if (GS.showSchoolReviewHover(window.location.href)) {
                GS_postSchoolReview();
            }
        } else {
            GS_resizeColumns();
        }
        return false;
    });
});