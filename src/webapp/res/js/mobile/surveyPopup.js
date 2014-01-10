define(['cookies'],function(cookies) {
    var init = function() {
        var surveyHtmlElem =  jQuery("#js-mobileCustomerSurvey");
        if (surveyHtmlElem.length === 1){
            surveyHtmlElem.slideDown('slow');
            jQuery('#js-popup-spacer').slideDown('slow');
            cookies.createCookie('survey_hover_seen', '1',15);
//            TODO fix how long it will run for
            setTimeout(function(){
                surveyHtmlElem.slideUp ('slow');
                jQuery('#js-popup-spacer').slideUp('slow');
            }, 5000);
            surveyHtmlElem.click(function(){
//                alert("boom");
                document.location = surveyHtmlElem.attr("data-survey-url")
            });
        }

    };

    return {
        init:init
    }
});