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
        var data = {};
        data.requestType = "ajax";
        data.decorator = "emptyDecorator";
        data.confirm = "true";
        var queryString = decodeURIComponent(window.location.search);
        var content = GS.uri.Uri.getFromQueryString("content");
        var topics = GS.uri.Uri.getFromQueryString("topics");

        //TODO: make a lot of the code in this method generic and put it elsewhere so it can easily be reused

        //Pass the topic center Id.
        if (content != undefined && content != "") {
            data.content = content;
            queryString = GS.uri.Uri.removeFromQueryString(queryString, "content");
        }

        if (topics != undefined && topics != "") {
            data.topics = topics;
            queryString = GS.uri.Uri.removeFromQueryString(queryString, "topics");
        }

        //Add the user selected grades filter to the grades params.
        if ($("#js_gradesDropdown").val() != -1) {
            data.grades = $("#js_gradesDropdown").val();
            queryString = GS.uri.Uri.removeFromQueryString(queryString, "grades");
        } else {
            queryString = GS.uri.Uri.removeFromQueryString(queryString, "grades");
        }

        //Add the user selected topics/subjects filter to the topics params.
        if ($("#js_subjectsDropdown").val() != -1) {
            data.subjects = $("#js_subjectsDropdown").val();
            queryString = GS.uri.Uri.removeFromQueryString(queryString, "subjects");
        } else {
            queryString = GS.uri.Uri.removeFromQueryString(queryString, "subjects");
        }

        $j('#spinner').show();

        var url = window.location.protocol + "//" + window.location.host + window.location.pathname;

        if (queryString.length > 1) {
            url = url + queryString;
        }

        $j.ajax({
            type: "get",
            url: url,
            data: data,
            success: GS.content.cms.WorksheetGallery.onUpdateSuccess,
            error: GS.content.cms.WorksheetGallery.onUpdateError
        });
    };

jQuery(function() {

    $j(".js_worksheetFilterDropDown").change(function() {
        GS.content.cms.WorksheetGallery.update();
    });

});