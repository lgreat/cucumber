define(function() {
    var init = function(state, schoolId) {
        fetchAndDisplayTestScoreSnippet(state, schoolId);
    };

    var fetchAndDisplayTestScoreSnippet = function(state, schoolId) {
        $.getJSON(
            "/school/testScoresAjax.page",
            {state:state, schoolId:schoolId}
        ).done(function(data) {
                if (data.testScores && data.testScores.length > 0) {
                    var $snippetInnerDiv = $('#js_testScoreSnippetInnerDiv');
                    for (var index = 0; index < data.testScores.length; index++) {
                        $snippetInnerDiv.append(getTestScoreDiv(data.testScores[index]));
                    }
                    $('#js_testScoreSnippetOuterDiv').show();
                }
            });
    };

    var getTestScoreDiv = function(testScore) {
        var newDiv = $('#js_testScoreSnippetTemplate').clone();
        newDiv.removeAttr('id');
        newDiv.find('.js_grade').html('Grade ' + testScore.gradeName);
        newDiv.find('.js_subject').html(testScore.subject);
        newDiv.find('.js_year').html(testScore.year);
        newDiv.find('.js_bar > div > div').css('width', testScore.value + '%');
        newDiv.find('.js_value').html(testScore.value + '%');
        return newDiv.show();
    };

    return {
        init:init
    }
});