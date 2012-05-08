define(function() {
    var init = function(state, schoolId) {
        fetchAndDisplayTestScoreSnippet(state, schoolId);
    };

    var fetchAndDisplayTestScoreSnippet = function(state, schoolId) {
        $.getJSON(
            "/school/testScoresAjax.page",
            {state:state, schoolId:schoolId}
        ).done(function(data) {
            if (data.testSubjects && data.testSubjects.length > 0) {
                var $snippetInnerDiv = $('#js_testScoreSnippetInnerDiv');
                var $newDiv = $('#js_testScoreSnippetTemplate').clone().removeAttr('id');
                $newDiv.find('.js_grade').html(data.testLabel + ' ' + data.gradeLabel);
                for (var index = 0; index < data.testSubjects.length; index++) {
                    appendTestSubjectDiv($newDiv, data.testSubjects[index]);
                }
                // clean up templates
                $newDiv.find('.js_testScoreSnippetSubjectTemplate').remove();
                $newDiv.find('.js_testScoreSnippetValueTemplate').remove();
                // append and show
                $snippetInnerDiv.append($newDiv.show());
                $('#js_testScoreSnippetOuterDiv').show();
            }
        });
    };

    var appendTestSubjectDiv = function($parentDiv, testSubject) {
        var $newDiv = $parentDiv.find('.js_testScoreSnippetSubjectTemplate').clone();
        $newDiv.removeClass('js_testScoreSnippetSubjectTemplate');
        $newDiv.find('.js_subject').html(testSubject.label);
        for (var index = 0; index < testSubject.values.length; index++) {
            appendTestValueDiv($newDiv, testSubject.values[index]);
        }
        $newDiv.append('<div class="ptm"><!-- not empty --></div>')
        $parentDiv.append($newDiv.show());
    };

    var appendTestValueDiv = function($parentDiv, testValue) {
        var $newDiv = $parentDiv.find('.js_testScoreSnippetValueTemplate').clone();
        $newDiv.removeClass('js_testScoreSnippetValueTemplate');
        $newDiv.find('.js_year').html(testValue.year);
        $newDiv.find('.js_bar > div > div').css('width', testValue.value + '%');
        $newDiv.find('.js_value').html(testValue.value + '%');
        $parentDiv.append($newDiv.show());
    };

    return {
        init:init
    }
});