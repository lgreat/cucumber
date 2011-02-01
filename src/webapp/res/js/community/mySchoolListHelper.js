GS = GS || {};
GS.community = GS.community || {};


GS.community.MySchoolListHelper = function() {

    this.addSchool = function(state, id, successCallback, failCallback, email) {
        var url = "/mySchoolListAjax.page";
        var data = {};
        data.schoolDatabaseState = state;
        data.schoolId = id;
        if (email) {
            data.email = email;
            data.redirectUrl = '';
        }

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
};