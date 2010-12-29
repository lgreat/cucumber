GS = GS || {};
GS.community = GS.community || {};


GS.community.MySchoolListHelper = function() {

    this.addSchool = function(state, id, successCallback, failCallback) {
        var url = "/mySchoolListAjax.page";
        var data = {};
        data.schoolDatabaseState = state;
        data.schoolId = id;

        jQuery.post(url, data, function(data) {
            if (data.success !== undefined && data.success === true) {
                successCallback();
                this.incrementCountInHeader();
            } else {
                failCallback();
            }
        }.gs_bind(this), "json");
    };

    this.incrementCountInHeader = function() {
        var mslCountInHeader = jQuery('#utilLinks .last a');
        var mslCount = Number(mslCountInHeader.html().replace(/[^0-9]/g,''));
        mslCount = mslCount + 1;
        mslCountInHeader.html("My School List (" + mslCount + ")");
    };

    this.showHover = function(state, id) {
        /*GSType.hover.mslHover.setSchoolId(id);
        GSType.hover.mslHover.setSchoolDatabaseState(state);
        GSType.hover.mslHover.setRedirectUrl(window.location.href);*/

        jQuery('#msl-redirectUrl').val(window.location.href);
        jQuery('#msl-schoolId').val(id);
        jQuery('#msl-schoolDatabaseState').val(state);

        jQuery('#msl-form').submit(GSType.hover.mslHover.onSubmit);

        GSType.hover.mslHover.show();
    };


};