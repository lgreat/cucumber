GS = GS || {};
GS.form = GS.form || {};


GS.form.ParentReviewLandingPageSchoolReviewForm = function() {};

jQuery(function() {

    GS.form.ParentReviewLandingPageSchoolReviewForm.prototype = new GS.form.SchoolReviewForm("parentReviewForm");
    GS.form.ParentReviewLandingPageSchoolReviewForm.prototype.postReview = function(email, callerFormId) {
        var url = '/school/review/postReview.page';
        
        var formData = this.serialize();

        jQuery.post(url, formData, function(data) {

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
                jQuery('#' + callerFormId).submit();
            } else {
                window.location.href = '/school/parentReviews.page?id=' + jQuery('#schoolId').val() + '&state=' + jQuery('#schoolState').val();
            }
        }, "json");
    }.gs_bind(GS.form.ParentReviewLandingPageSchoolReviewForm.prototype);

    GS.form.ParentReviewLandingPageSchoolReviewForm.prototype.formValid = function() {

        return true;
    };

    GS.form.ParentReviewLandingPageSchoolReviewForm.prototype.updateAllErrors = function() {

        return true;
    };

    /////////// form setup - register event handlers /////////
    GS.form.ParentReviewLandingPageSchoolReviewForm.prototype.attachEventHandlers = function() {

        $('#js-submitParentReview').click(function() {
            if (GS.form.parentReviewLandingPageSchoolReviewForm.formValid()) {
                GS.form.parentReviewLandingPageSchoolReviewForm.postReview();
            } else {
                GS.form.parentReviewLandingPageSchoolReviewForm.updateAllErrors();
            }
        });
        return false;

    }.gs_bind(GS.form.ParentReviewLandingPageSchoolReviewForm.prototype);


    GS.form.parentReviewLandingPageSchoolReviewForm = new GS.form.ParentReviewLandingPageSchoolReviewForm();
    GS.form.parentReviewLandingPageSchoolReviewForm.attachEventHandlers();

});
