jQuery(document).ready(function() {
    // disable selection of button-1 class
    jQuery('#compareTable tbody.striped').alternateRowColors();

    GS.school.compare.initializeSchoolsInCompare();
    //Remove school from the persistent module if the school is being removed from compare tool.
    jQuery('.js_compare_tool_remove_school').on('click', function () {
        var schoolSelected = $(this).attr('id');
        if(schoolSelected != undefined && schoolSelected !== '' && schoolSelected.indexOf('js_compare_tool_school_') === 0 ){
            var stateAndSchoolId = schoolSelected.substr('js_compare_tool_school_'.length, schoolSelected.length);
            var schoolId = stateAndSchoolId.substr(2, stateAndSchoolId.length);
            var state = stateAndSchoolId.substr(0, 2).toUpperCase();
            GS.school.compare.removeSchoolFromCompare(schoolId, state);
        }
    });
});

compare_onClickAddMslLink = function(elem,omniturePageName) {
   if (s.tl) {
        s.tl(true,'o', 'Add_to_MSL_Link_' + omniturePageName);
    }

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
