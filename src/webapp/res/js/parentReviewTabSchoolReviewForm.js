GS = GS || {};
GS.form = GS.form || {};


GS.form.ParentReviewTabSchoolReviewForm = function() {};

jQuery(function() {

    GS.form.ParentReviewTabSchoolReviewForm.prototype = new GS.form.SchoolReviewForm("frmPRModule");
    GS.form.ParentReviewTabSchoolReviewForm.prototype.postReview = function(email, callerFormId) {
        var url = GS.uri.Uri.getBaseHostname() + '/school/review/postReview2.page';
        
        //When this method is called by the "sign in" handler, overwrite review form's email with whatever user signed in with.
        if (email !== undefined && email !== '') {
            this.email.getElement().val(email);
        }
        var formData = this.serialize();
        var jqxhr = jQuery.ajax({
            url:url,
            data:formData,
            dataType:"json"
        }).done(function(data) {
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
                window.location.href=redirectUrl;
                if (reloading) {
                    window.location.reload();
                }
            }
        }).fail(function(data){
            alert("We're sorry, but we were not able to process your review submission. Please try again soon.");
        });
    }.gs_bind(GS.form.ParentReviewTabSchoolReviewForm.prototype);
   //GS.form.schoolReviewForm = new GS.form.SchoolReviewForm("frmPRModule");
    GS.form.parentReviewTabSchoolReviewForm = new GS.form.ParentReviewTabSchoolReviewForm();

});
