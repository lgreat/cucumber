GS = GS || {};
GS.form = GS.form || {};


GS.form.ParentReviewLandingPageSchoolReviewForm = function() {};

jQuery(function() {

    GS.form.ParentReviewLandingPageSchoolReviewForm.prototype = new GS.form.SchoolReviewForm("parentReviewForm");
    GS.form.ParentReviewLandingPageSchoolReviewForm.prototype.postReview = function(email, callerFormId) {
        var url = '/school/review/postReview.page';
        
        // first, grab the email from the join/signIn form and use that with the review
        if (email !== undefined && email !== '') {
            this.email.getElement().val(email);
        }
        var formData = this.serialize();
        jQuery.post(url, formData, function(data) {
            if (data.showHover !== undefined && data.showHover === "emailNotValidated") {
                GSType.hover.emailNotValidated.show();
                return false;
            } else if (data.showHover !== undefined && data.showHover === "validateEmailSchoolReview") {
                subCookie.setObjectProperty("site_pref", "showHover", "validateEmailSchoolReview", 3);
            } else {
                if (data.reviewPosted !== undefined) {
                    if (data.reviewPosted === "true") {
                        // cookie to show schoolReviewPostedThankYou hover
                        subCookie.setObjectProperty("site_pref", "showHover", "schoolReviewPostedThankYou", 3);
                    } else {
                        // cookie to show schoolReviewNotPostedThankYou hover
                        subCookie.setObjectProperty("site_pref", "showHover", "schoolReviewNotPostedThankYou", 3);
                    }
                }
            }

            var successEvents = "";
            if (data.ratingEvent !== undefined) {
                successEvents += data.ratingEvent;
            }
            if (data.reviewEvent !== undefined) {
                successEvents += data.reviewEvent;
            }
            if (successEvents !== "") {
                pageTracking.clear();
                pageTracking.successEvents = successEvents;
                pageTracking.send();
            }
            if (callerFormId) {
                GSType.hover.signInHover.hide();
                GSType.hover.joinHover.hide();
                jQuery('#' + callerFormId).submit();
            } else {
                window.location.href = '/school/parentReviews.page?id=' + jQuery('#schoolId').val() + '&state=' + jQuery('#schoolState').val();
            }
        }, "json");
    }.gs_bind(GS.form.ParentReviewLandingPageSchoolReviewForm.prototype);
    GS.form.parentReviewLandingPageSchoolReviewForm = new GS.form.ParentReviewLandingPageSchoolReviewForm();

});
