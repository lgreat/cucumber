var GS = GS || {};
GS.content = GS.content || {};
GS.content.cms = GS.content.cms || {};

GS.content.cms.WorksheetGallery = function() {};

    GS.content.cms.WorksheetGallery.onUpdateSuccess = function(data) {
        $j("#js_worksheetGalleryData").html(data);
        $j("#spinner").hide();
        return true;
    };

    GS.content.cms.WorksheetGallery.onUpdateError = function(data, status, error) {
        return false;
    };

    GS.content.cms.WorksheetGallery.update = function() {

        var grade = $("#js_gradesDropdown").val();
        var subject = $("#js_subjectsDropdown").val();
        var queryString = decodeURIComponent(window.location.search);

        queryString = GS.uri.Uri.removeFromQueryString(queryString, "start");
        if (queryString === '?') {
            queryString = '';
        }

        var pathName = '/worksheets';
        if (grade.length > 0) {
            pathName = pathName + '/' + grade;
        }
        if (subject.length > 0) {
            pathName = pathName + '/' + subject;
        }
        var url = window.location.protocol + "//" + window.location.host + pathName + queryString;

        window.location = url;
    };

jQuery(function() {

    $j(".js_worksheetFilterDropDown").change(function() {
        GS.content.cms.WorksheetGallery.update();
    });

});