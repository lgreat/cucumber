GS = GS || {};

GS.schoolSearchResultsPage = GS.schoolSearchResultsPage || (function() {
    var body = '#contentGS';

    var init = function() {
        registerEventHandlers();
        GS.search.schoolSearchForm.init(GS.search.filters, GS.ui.contentDropdowns);
        GS.school.compare.initializeSchoolsInCompare(function() {
            return window.location.pathname + GS.search.filters.getUpdatedQueryString();
        });

        $(function() {
            // statusOnLoadDeferred is rejected if the user is not already connected after FB has initialized
            GS.facebook.getStatusOnLoadDeferred().fail(function() {
                GS.tracking.sendAndRestore({
                    prop53:"Facebook friend help signon"
                });
            });

            GS.facebook.getLoginDeferred().done(function() {
                GS.facebook.getUserFriendsSchoolPageData(GS.search.results.handleUIForFacebookResults);
                GS.search.results.showAskAFriendLinks();
            });
        });
    };

    var writeCompareNowLink = function(schoolId, state) {
        //The 'compare' label should be switched to 'compare now' link.
        var compareLabel = $('#js_' + state + schoolId + '_compare_label');
        compareLabel.html('<a href="javascript:void(0);" class="js_compare_link noInterstitial">Compare Now</a>');
    };

    var removeCompareNowLinks = function(schoolId, state) {
        //The 'compare now' link should be switched to 'compare' label for the school that has been deleted.
        removeCompareNowLink(schoolId, state);

        //If there are less than 2 schools remaining in the compare module after the school has been deleted
        //then switch all the the 'compare now' links to 'compare' label.
        var schoolsInCompare = GS.school.compare.getSchoolsInCompare();
        if (schoolsInCompare.length < 2) {
            for (var i = 0; i < schoolsInCompare.length; i++) {
                var schoolIdentifier = schoolsInCompare[i].schoolId;
                var stateStr = schoolsInCompare[i].state;
                removeCompareNowLink(schoolIdentifier, stateStr);
            }
        }
    };

    var removeCompareNowLink = function(schoolId, state) {
        //The 'compare now' link should be switched to 'compare' label .
        var compareLabel = $('#js_' + state + schoolId + '_compare_label');
        compareLabel.html('Compare');
    };

    var trackAskAFriendClicked = function() {
        // "askAFriend" is a link on each search result
        omnitureEventNotifier.clear();
        omnitureEventNotifier.successEvents = "event80;";
        omnitureEventNotifier.send();
    };

    var registerEventHandlers = function() {
        // Bind the behavior when clicking on a compare checkbox in the list
        $(body).on('click', '.compare-school-checkbox', function() {
            var schoolCheckbox = $(this);
            var schoolSelected = schoolCheckbox.attr('id');
            var schoolId = schoolSelected.substr(2, schoolSelected.length);
            var state = schoolSelected.substr(0, 2);
            var checked = schoolCheckbox.is(':checked');
            if (checked === true) {
                //If the checkbox was checked then try adding a school to the compare module.
                //If adding a school fails then un check the checkbox.
                GS.school.compare.addSchoolToCompare(schoolId, state).done(
                    function() {
                        //If there are more than 2 schools in compare then the 'compare' label
                        //should be switched to 'compare now' link.
                        var schoolsInCompare = GS.school.compare.getSchoolsInCompare();
                        if (schoolsInCompare.length >= 2) {
                            for (var i = 0; i < schoolsInCompare.length; i++) {
                                var schoolId = schoolsInCompare[i].schoolId;
                                var state = schoolsInCompare[i].state;
                                writeCompareNowLink(schoolId, state);
                            }
                        }
                    }).fail(
                    function() {
                        schoolCheckbox.prop('checked', false);
                    }
                );
            } else {
                GS.school.compare.removeSchoolFromCompare(schoolId, state);
                //After the school is removed, the 'compare now' link should be switched to 'compare' label.
                //Also if there are less than 2 schools remaining in the compare module after this school has been deleted
                //then switch all the the 'compare now' links to 'compare' label.
                removeCompareNowLinks(schoolId, state);
            }

            //Add omniture tracking to the check/un check of the compare checkbox.
            if (s.tl) {
                s.tl(true, 'o', 'Search_List_Compare_Check');
            }

        });

        //Bind the custom event that gets triggered after the schools are initialized in the compare module.
        //This is used to check the check boxes for the schools that are in the compare module.
        $(body).on('schoolsInitialized', function(event, schoolsInCompare) {
            if (schoolsInCompare.length==0){
                $('.compare-school-checkbox').prop('checked',false);
            }
            for (var i = 0; i < schoolsInCompare.length; i++) {
                var schoolId = schoolsInCompare[i].schoolId;
                var state = schoolsInCompare[i].state;

                var schoolCheckBox = $('#' + state + schoolId);
                schoolCheckBox.prop("checked", true);

                //If there are more than 2 schools in compare then the 'compare' label
                //should be switched to 'compare now' link.
                if (schoolsInCompare.length >= 2) {
                    writeCompareNowLink(schoolId, state);
                }
            }
        });

        //Bind the custom event that gets triggered after a school is removed from the compare module.
        //This is used to un check the check boxes for the school that is removed from the compare module.
        $(body).on('schoolRemoved', function(event, schoolId, state) {
            var checkBox = $('#' + state + schoolId);
            checkBox.prop('checked', false);
            //After the school is removed, the 'compare now' link should be switched to 'compare' label.
            //Also if there are less than 2 schools remaining in the compare module after this school has been deleted
            //then switch all the the 'compare now' links to 'compare' label.
            removeCompareNowLinks(schoolId, state);
        });

        // Bind the behavior when clicking on the compare now link that appears when at least two schools are checked
        $(body).on('click', '.js_compare_link', function() {
            return GS.school.compare.compareSchools();
        });

        $('.js-ask-a-friend').click(function(e) {
            trackAskAFriendClicked();

            var $this = $(this);
            var $schoolData = $this.parent().find('.js-school-data');
            var schoolName = $schoolData.data('gs-school-name');
            var autoText = $schoolData.data('gs-school-autotext');
            var link = $this.attr('href');
            var photoUrl = 'http://www.gscdn.org/res/img/logo/logo_GS_276x50_logo.png'

            GS.facebook.postToFeed(link, photoUrl, schoolName, link, autoText);
            return false;
        });
    };

    return {
        init:init

    }

})();
