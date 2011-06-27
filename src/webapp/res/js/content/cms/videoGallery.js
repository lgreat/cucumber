var GS = GS || {};
GS.content = GS.content || {};
GS.content.cms = GS.content.cms || {};

GS.content.cms.VideoGallery = function() {};

    GS.content.cms.VideoGallery.onUpdateSuccess = function(data) {
        $j("#js_videoGalleryData").html(data);
        $j("#spinner").hide();
        return true;
    };

    GS.content.cms.VideoGallery.onUpdateError = function(data, status, error) {
        return false;
    };

    GS.content.cms.VideoGallery.update = function() {
        var data = {};
        data.requestType = "ajax";
        data.decorator = "emptyDecorator";
        data.confirm = "true";
        var queryString = window.location.search;
        var content = GS.uri.Uri.getFromQueryString("content");
        var grades = GS.uri.Uri.getFromQueryString("grades");
        var subjects = GS.uri.Uri.getFromQueryString("subjects");
        var topics = GS.uri.Uri.getFromQueryString("topics");

        //TODO: make a lot of the code in this method generic and put it elsewhere so it can easily be reused

        //Pass the topic center Id.
        if (content != undefined && content != "") {
            data.content = content;
            queryString = GS.uri.Uri.removeFromQueryString(queryString, "content");
        }
        //Grades,subjects,topics are cms entered for the 'videos' subtopic on the topic center template.
        //Therefore pass them through.
        if (grades != undefined && grades != "") {
            data.grades = grades;
            queryString = GS.uri.Uri.removeFromQueryString(queryString, "grades");
        }

        if (subjects != undefined && subjects != "") {
            data.subjects = subjects;
            queryString = GS.uri.Uri.removeFromQueryString(queryString, "subjects");
        }

        if (topics != undefined && topics != "") {
            data.topics = topics;
            queryString = GS.uri.Uri.removeFromQueryString(queryString, "topics");
        }

        //Add the user selected grades filter to the grades params.
        if ($("#js_gradesDropdown").val() != -1) {
            if (data.grades != undefined && data.grades.length > 0) {
                data.grades = data.grades + "," + $("#js_gradesDropdown").val();
                queryString = GS.uri.Uri.removeFromQueryString(queryString, "grades");
            } else {
                data.grades = $("#js_gradesDropdown").val();
                queryString = GS.uri.Uri.removeFromQueryString(queryString, "grades");
            }
        }

        //Add the user selected topics/subjects filter to the topics params.
        if ($("#js_topicsDropdown").val() != -1) {
            if (data.topics != undefined && data.topics.length > 0) {
                data.topics = data.topics + "," + $("#js_topicsDropdown").val();
                queryString = GS.uri.Uri.removeFromQueryString(queryString, "topics");
            } else {
                data.topics = $("#js_topicsDropdown").val();
                queryString = GS.uri.Uri.removeFromQueryString(queryString, "topics");
            }
        }

        $j('#spinner').show();

        var url = window.location.protocol + "//" + window.location.host + window.location.pathname;

        $j.ajax({
            type: "get",
            url: url + queryString,
            data: data,
            success: GS.content.cms.VideoGallery.onUpdateSuccess,
            error: GS.content.cms.VideoGallery.onUpdateError
        });
    };

jQuery(function() {

    $j(".js_videoFilterDropDown").change(function() {
        GS.content.cms.VideoGallery.update();
    });

});