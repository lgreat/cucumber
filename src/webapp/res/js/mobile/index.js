define(['uri', 'geocoder'], function(uri, geocoder) {
    var JS_GRADE_LEVELS_CONTAINER_SELECTOR = '#js-gradeLevels';


    // copied from findASchool.js
    var isTermState = function(term) {
        var stateTermList = new Array
                ("AK","Alaska","AL","Alabama","AR","Arkansas","AZ","Arizona",
                        "CA","California","CO","Colorado","CT","Connecticut","DC",
                        // Do not include state names that are identical to city names
//        "Washington, D.C.", "Washington, DC", "Washington D.C.", "Washington DC",
                "DE","Delaware","FL","Florida","GA","Georgia","HI","Hawaii","IA","Iowa",
                "ID","Idaho","IL","Illinois","IN","Indiana","KS","Kansas","KY","Kentucky",
                "LA","Louisiana","MA","Massachusetts","MD","Maryland","ME","Maine","MI","Michigan",
                "MN","Minnesota","MO","Missouri","MS","Mississippi","MT","Montana",
                "NC","North Carolina","ND","North Dakota","NE","Nebraska","NH","New Hampshire",
                "NJ","New Jersey","NM","New Mexico","NV","Nevada","NY","New York",
                "OH","Ohio","OK","Oklahoma","OR","Oregon","PA","Pennsylvania",
                "RI","Rhode Island","SC","South Carolina","SD","South Dakota",
                "TN","Tennessee","TX","Texas","UT","Utah","VA","Virginia","VT","Vermont",
                "WA","Washington","WI","Wisconsin","WV","West Virginia","WY","Wyoming");
        for (var i=0; i < stateTermList.length; i++) {
            if (stateTermList[i].toLowerCase() == term.toLowerCase()) {
                return true;
            }
        }
        return false;
    };

    var loadResultsPage = function() {
        var queryString = $('#js-searchByLocation form').serialize();
        queryString = buildQueryString(queryString);
        window.location.href = '/search/search.page' + queryString;
    };

    var buildQueryString = function(queryString) {
        var $gradeLevelsContainer = $(JS_GRADE_LEVELS_CONTAINER_SELECTOR);
        //to populate an array inside a Spring command, Spring requires data in format gradeLevels[0]=e,gradeLevels[1]=m
        queryString = uri.removeFromQueryString(queryString, "gradeLevels");
        var checkedGradeLevels = $gradeLevelsContainer.find(':checked');
        var overwriteGradeLevels = true;
        checkedGradeLevels.each(function() {
            if (jQuery(this).val() !== '') {
                queryString = uri.putIntoQueryString(queryString, "gradeLevels", jQuery(this).val(), overwriteGradeLevels);
                overwriteGradeLevels = false;
            }
        });

        return queryString;
    };

    var submitByLocationSearch = function() {
        var byLocationForm = $('#js-searchByLocation form');
        var searchQuery = byLocationForm.find('input[name="searchString"]').val();
        searchQuery = searchQuery.replace(/^\s*/, "").replace(/\s*$/, "");

        if (searchQuery != '' &&
                searchQuery != 'Search by city AND state or address ...' && !isTermState(searchQuery)) {
            byLocationForm.find('input[name="searchString"]').val(searchQuery);

            //GS-12100 Since its a by location search, strip the words 'schools' from google geocode searches.
            var searchQueryWithFilteredStopWords = searchQuery;
            if (searchQueryWithFilteredStopWords != '') {
                searchQueryWithFilteredStopWords = searchQueryWithFilteredStopWords.replace(/schools/g, "");
            }

            geocoder.geocode(searchQueryWithFilteredStopWords, function(geocodeResult) {
                GS.log('got geocode result ', geocodeResult);
                if (geocodeResult != null) {
                    byLocationForm.find('input[name="lat"]').val(geocodeResult['lat']);
                    byLocationForm.find('input[name="lon"]').val(geocodeResult['lon']);
                    byLocationForm.find('input[name="state"]').val(geocodeResult['state']);
                    byLocationForm.find('input[name="locationType"]').val(geocodeResult['type']);
//                byLocationForm.find('input[name="partialMatch"]').val(geocodeResult['partial_match']);
                    byLocationForm.find('input[name="normalizedAddress"]').val(geocodeResult['normalizedAddress']);
                    byLocationForm.find('input[name="totalResults"]').val(geocodeResult['totalResults']);

                    window.setTimeout(loadResultsPage, 1);
                } else {
                    alert("Location not found. Please enter a valid address, city, or ZIP.");
                }
            });
        } else {
            alert("Please enter an address, zip code or city and state");
        }

        return false;
    };

    var setAllGrades = function() {
        var $gradeLevelsContainer = $(JS_GRADE_LEVELS_CONTAINER_SELECTOR);
        var gradeCheckboxes = $gradeLevelsContainer.find('.jq-grade-level');

        var numGradeLevels = gradeCheckboxes.size();
        var numGradeLevelsChecked = gradeCheckboxes.filter(':checked').size();
        if (numGradeLevelsChecked === numGradeLevels) {
            jQuery('#js-grade-level-all').prop('checked', true);
            jQuery('#js-grade-level-label').empty().append('All grades')
        } else if (numGradeLevelsChecked === 0) {
            jQuery('#js-grade-level-all').prop('checked', false);
            jQuery('#js-grade-level-label').empty().append('No grades');
        } else {
            jQuery('#js-grade-level-all').prop('checked', false);
            jQuery('#js-grade-level-label').empty().append('Some grades');
        }
    };




    var init = function() {
        $(function() {
            jQuery('#js-gradeLevels input').click(function () {
                var cssId = jQuery(this).attr('id');

                var gradeCheckboxes = jQuery('#js-gradeLevels .jq-grade-level');
                GS.log('checkbox clicked:', cssId);

                if (cssId === 'js-grade-level-all') {
                    if (jQuery(this).is(':checked')) {
                        gradeCheckboxes.prop('checked',true);
                    } else {
                        gradeCheckboxes.prop('checked', false);
                    }
                    gradeCheckboxes.checkboxradio("refresh");
                }

                setAllGrades();
            });


            $('#js-searchByLocation').on('submit', 'form', function() {
                submitByLocationSearch();
                return false;
            });


            /*
            $('#js-search-tabs').find('.js-by-location').on('click', function() {
                var $byLocationTab = $('#js-search-tabs').find('.js-by-location');
                var $byNameTab = $('#js-search-tabs').find('.js-by-name');
                $byLocationTab.addClass('selected');
                $byNameTab.removeClass('selected');

                var $byLocationBody = $('#js-searchByLocation');
                var $byNameBody = $('#js-searchByName');
                $byNameBody.hide();
                $byLocationBody.show();
            });
            $('#js-search-tabs').find('.js-by-name').on('click', function() {
                var $byLocationTab = $('#js-search-tabs').find('.js-by-location');
                var $byNameTab = $('#js-search-tabs').find('.js-by-name');
                $byLocationTab.removeClass('selected');
                $byNameTab.addClass('selected');

                var $byLocationBody = $('#js-searchByLocation');
                var $byNameBody = $('#js-searchByName');
                $byNameBody.show();
                $byLocationBody.hide();
            });*/
        });

    };

    return {
        init:init
    }
});
$(document).ready( function() {
    $('.gsTabs').each(function(){
        var tab = $(this);
        tab.children('div').hide(); // Hide all content divs
        var tabNav = tab.find('ul:first'); // get only the first ul not all of the descendents
        tab.children('div:first').show(); // Show the first div
        tabNav.find('li:first a').addClass('selected'); // Set the class of the first link to active
        tabNav.find('li').each(function(){
            $(this).find('a').click(function(){ //When any link is clicked
                tab.children('div').hide(); // hide all layers
                var tabNum = tabNav.find('li').index($(this).parent());// find reference to the content
                tab.children('div').eq(tabNum).show();// show the content
                tabNav.find('li a').removeClass('selected');// turn all of them off
                $(this).addClass('selected');// turn selected on
                return false;
            });
        });
    });
});