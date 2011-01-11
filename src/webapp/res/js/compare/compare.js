jQuery(document).ready(function() {
    // disable selection of button-1 class
    jQuery('#compareTable tbody.striped').alternateRowColors();
});

compare_onClickAddMslLink = function(elem) {
    var statePlusId = jQuery(elem).attr('id');

    statePlusId = statePlusId.replace("js-add-msl-link-", "");
    var state = statePlusId.substring(0,2).toLowerCase();
    var id = statePlusId.substring(2);

    var memId = subCookie.getObject("MEMID");

    var mslHelper = new GS.community.MySchoolListHelper();

    if (memId !== undefined && memId !== null) {
        mslHelper.addSchool(state, id, function() {
            jQuery('.js-add-msl-' + statePlusId).find('.js-msl-text').html("Added to <a href=\"/mySchoolList.page\">My School List</a>");
            jQuery('.js-add-msl-' + statePlusId).find('.sprite').attr("class", "sprite i-checkmark-sm img");
        }, function() {});
    } else {
        //show hover, create msl, save school to msl, round trip to log user in
        mslHelper.showHover(state,id);
    }

    return false;
};
