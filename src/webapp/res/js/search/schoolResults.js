var GS = GS || {};
GS.search = GS.search || {};
GS.search.results = GS.search.results || (function() {
    var $thisDomElement = jQuery('#js-school-search-results-table-body'); //TODO: pass this into constructor
    var filtersModule;
    var compareModule;
    var customLinksModule;
    var infoBoxTemplate, sidebarListTemplate;
    var listResultsLinkSelector = '.js-listResultsLink';
    var mapResultsLinkSelector = '.js-mapResultsLink';
    var body = 'body';
    var numResultsPerPage;
    var setProp15ForMap = false;

    // http://stackoverflow.com/questions/1744310/how-to-fix-array-indexof-in-javascript-for-ie-browsers
    // we use indexOf on some arrays in this .js file, but IE doesn't support it natively, so we have to implement it here
    if (!Array.prototype.indexOf) {
        Array.prototype.indexOf = function(obj, start) {
            for (var i = (start || 0), j = this.length; i < j; i++) {
                if (this[i] == obj) { return i; }
            }
            return -1;
        }
    }

    var init = function(_filtersModule) {
        filtersModule = _filtersModule;

        attachEventHandlers();

//        compareModule.initializeRowsAndCheckedSchoolsArray();

        var blackTriangleDown = 'i-16-sort-list-arrow-down';
        var whiteTriangleDown = 'i-16-white-space';
        var blackTriangleUp = 'i-16-sort-list-arrow-up';
        var whiteTriangleUp = 'i-16-white-space';

        $('body').on('click', '[data-gs-sort-toggle]', function() {
            var $this = $(this);
            var $span = $this.find('span');
            var sorts = $this.data('gs-sort-toggle').split(',');
            var queryData = GS.uri.Uri.getQueryData();
            var currentSort = queryData['sortBy'];
            /*
            seo urls do not have query parameters. Examples - http://dev.greatschools.org/indiana/speedway/schools/
                                                    http://dev.greatschools.org/indiana/indianapolis/public/schools/
            The default sort for these pages is GS_RATING_DESCENDING. Assuming that this is the sort for all pages that
            have empty query string data, the current sort is set to this value.
             */
            if(jQuery.isEmptyObject(queryData)) {
                currentSort = 'GS_RATING_DESCENDING';
            }

//            $this.addClass('selected');

            var newSort = sorts[0];
            if (sorts[0] === currentSort) {
                newSort = sorts[1];
            }

            queryData['sortBy'] = newSort;
            delete queryData.start;

            if (typeof(window.History) === 'undefined' || window.History.enabled !== true) {
                var queryString = window.location.search;
                var queryStringWithFilters = filtersModule.getUpdatedQueryString();
                if(queryString !== queryStringWithFilters) {
                    queryString = queryStringWithFilters;
                }
                queryString = GS.uri.Uri.putIntoQueryString(queryString,"sortBy", newSort, true);
                queryString = GS.uri.Uri.removeFromQueryString(queryString, "start");
                window.location.search = queryString;
                return;
            }

            $('body [data-gs-sort-toggle]span').each(function() {
//                $span.html('&#160;');      //remove all other arrows
                $span.addClass('i-16-white-space');
                $this.removeClass('selected');
            });
            if (newSort.indexOf('DESCENDING') !== -1) {
//                $span.attr(blackTriangleUp);
                $span.addClass('i-16-sort-list-arrow-down');
//                $this.addClass('selected');
            } else {
//                $span.attr(blackTriangleDown);
                $span.addClass('i-16-sort-list-arrow-up');
//                $this.addClass('selected');
            }

            update(queryData);
        });

        $(function() {
            if(typeof Hogan != 'undefined') {
                infoBoxTemplate = Hogan.compile($('#js-infoBoxTemplate').html());
                sidebarListTemplate = Hogan.compile($('#js-schoolListTemplate').html());
            }
        });
    };

    var url = function() {
        var value = window.location.protocol + "//" + window.location.host + window.location.pathname;
        return value;
    };


    var search = function(callback, errorCallback, queryStringData) {
        var queryString = GS.uri.Uri.getQueryStringFromObject(queryStringData);
        var data = {};
        data.requestType = "ajax";
        data.decorator="emptyDecorator";
        data.confirm="true";

        if (typeof(window.History) !== 'undefined' && window.History.enabled === true) {
            // patch the response from getQueryStringFromObject, because if we pass an empty string into
            // window.History.replaceState, it's treated the same as a null, and the query string isn't modified at all
            if (queryString === '') {
                queryString = ' ';
            } else if (queryString !== undefined) {
                queryString = $.trim(queryString);
            }

            // use HTML 5 history API to rewrite the current URL to represent the new state.
            window.History.replaceState(null, document.title, queryString);

            // *sigh*  if a single space is passed into replaceState, and we want to change it to something else later
            // such as '?st=private', then History.js will append %20/ to the end of the URL first.
            // so we have to pass is a single space, and then call replaceState *again* with an empty string
            if (queryString === ' ') {
                window.History.replaceState(null, document.title, '');
            }
        }

        jQuery.ajax({
            type: "get",
            url: url() + $.trim(queryString),
            data:data,
            success: callback,
            error: errorCallback,
            traditional: true
        });
    };


    var clear = function() {
        $thisDomElement.find('.school-search-result-row').remove();
    };

    var update = function(queryStringData) {
        if (queryStringData === undefined) {
            queryStringData = filtersModule.getUpdatedQueryStringData();
            delete queryStringData.start;
        }

        if(queryStringData.view == 'map') {
            mapSearch(1, 25, queryStringData);
        }
        else {
            var onSearchSuccess = function(data) {
                var afterFadeIn = function() {
                    //reattach callbacks to dom element events
                    attachEventHandlers();
                    // ask persistent compare to tell us what schools are in compare
                    GS.school && GS.school.compare && GS.school.compare.triggerUpdatePageWithSchoolsEvent();
                };

                // the Sponsor_630x40 ad is in the search results response, but we don't want to execute the googletag.display
                // call, since we will be calling refreshAds() very shortly, and the display() call would interfere.
                // Let's just save the Sponsor_630x40 dom object, and reuse it, and then call refreshAds()
                data = $(data).filter(':not(script:contains(googletag.display))');
                data.find('#Sponsor_630x40').replaceWith($('#Sponsor_630x40'));

                jQuery('#js-school-search-results-table').html(data);
                jQuery('#js-school-search-results-table-body').css("opacity",.2);
                jQuery('#js-school-search-results-table-body').animate(
                    {opacity: 1},
                    250,
                    'linear',
                    afterFadeIn
                );
                var seoTitle = jQuery('#js_seoCityBrowseTitle').text();
                if(seoTitle !== '') {
                    jQuery('#js_seoTitle').text(seoTitle);
                }
                GS.util.htmlMirrors.updateAll();
                if(jQuery("#js_totalResultsCountReturn").html() == "0" || jQuery("#js_totalResultsCountReturn").html() == 0 || jQuery("#js_totalResultsCountReturn").html() == ""){
                    if(jQuery("#js_totalResultsCountReturn").html() == ""){jQuery("#js_totalResultsCountReturn").html("0");jQuery("#js-moreThanOne").show()}
                    jQuery("#js_totalResultsCountReturn").popover('show');
                    jQuery(".js_closeOopsPopover").click(function() {
                        jQuery("#js_totalResultsCountReturn").popover('hide');
                    });
                }
                else{
                    $("#js_totalResultsCountReturn").popover('hide');
                }

                // now that DOM is updated with new search results, do an ad refresh. An ad exists within search results
                refreshAds();
                showFacebookFriendsForResults();
                pageTracking.clear();
                pageTracking.pageName = $('#jq-omniturePageName').val();
                pageTracking.props['prop15'] = '';
                pageTracking.send();
                GS_notifyQuantcastComscore();
            };

            var onSearchError = function() {
                clear();
                jQuery("#js-spinny-search").hide();
            };

            jQuery('#js-school-search-results-table-body').animate(
                { opacity: .2 },
                250,
                'linear',
                function() {
                    search(onSearchSuccess, onSearchError, queryStringData);
                }
            );
        }
    };

    var onPageSizeChanged = function() {
        var queryString = window.location.search;
        queryString = GS.uri.Uri.putIntoQueryString(queryString,"pageSize",jQuery('#page-size').val(), true);
        queryString = GS.uri.Uri.removeFromQueryString(queryString, "start");
        window.location.search = queryString;
    };

    var onDisplayResultsSizeChanged = function() {
        var pageSize = jQuery('#page-size').val();
        if (typeof(window.History) === 'undefined' || window.History.enabled !== true) {
            var queryString = window.location.search;
            var queryStringWithFilters = filtersModule.getUpdatedQueryString();
            if(queryString !== queryStringWithFilters) {
                queryString = queryStringWithFilters;
            }
            queryString = GS.uri.Uri.putIntoQueryString(queryString,"pageSize",pageSize, true);
            queryString = GS.uri.Uri.removeFromQueryString(queryString, "start");
            window.location.search = queryString;
            return;
        }
        var queryData = GS.uri.Uri.getQueryData();
        queryData['pageSize'] = pageSize;
        delete queryData.start;
        if(queryData.view == 'map') {
            mapSearch(1, pageSize, queryData);

            var schoolList = $('#js-schoolList');
            var firstItem = schoolList.find('div:first');
            if(parseInt(numResultsPerPage) > parseInt(pageSize) && firstItem !== null) {
                var listTop = schoolList.offset().top;
                var listBottom = listTop + schoolList.height();

                var itemTop = firstItem.offset().top;
                var itemBottom = itemTop + firstItem.height();

                if((itemBottom >= listBottom) || (itemTop <= listTop)) {
                    schoolList.scrollTop(itemTop - listTop + schoolList.scrollTop());
                }
            }
        }
        else {
            update(queryData);
            if(parseInt(numResultsPerPage) > parseInt(pageSize)) {
                window.scrollTo(0,0);
            }
        }
        numResultsPerPage = pageSize;
    };

    var onSortChanged = function() {
        var i = 0;
        var gradeLevels = [];
        var schoolTypes = [];
        var queryString = window.location.search;

        queryString = GS.uri.Uri.putIntoQueryString(queryString,"sortChanged",true, true);
        queryString = GS.uri.Uri.removeFromQueryString(queryString, "start");
        if (jQuery('#sort-by').val() !== '' && typeof(jQuery('#sort-by').val()) !== 'undefined') {
            queryString = GS.uri.Uri.putIntoQueryString(queryString,"sortBy",jQuery('#sort-by').val(), true);
        } else {
            queryString = GS.uri.Uri.removeFromQueryString(queryString, "sortBy");
        }
        window.location.search = queryString;
    };

    var onSortChangedForMap = function(selectValue) {
        var queryString = window.location.search;
        if (typeof(window.History) === 'undefined' || window.History.enabled !== true) {
            var queryStringWithFilters = filtersModule.getUpdatedQueryString();
            if(queryString !== queryStringWithFilters) {
                queryString = queryStringWithFilters;
            }
            if (selectValue !== '' && typeof(selectValue) !== 'undefined') {
                queryString = GS.uri.Uri.putIntoQueryString(queryString,"sortBy",selectValue, true);
            } else {
                queryString = GS.uri.Uri.removeFromQueryString(queryString, "sortBy");
            }
            queryString = GS.uri.Uri.removeFromQueryString(queryString, "start");
            window.location.search = queryString;
            return;
        }

        if (selectValue !== '' && typeof(selectValue) !== 'undefined') {
            queryString = GS.uri.Uri.putIntoQueryString(queryString,"sortBy",selectValue, true);
        } else {
            queryString = GS.uri.Uri.removeFromQueryString(queryString, "sortBy");
        }

        var queryStringData = GS.uri.Uri.getQueryData(queryString);

        setProp15ForMap = true;
        mapSearch(1, 25, queryStringData);
    };

    var page = function(pageNumber, pageSize) {
        var start = (pageNumber-1) * pageSize;
        var queryString = window.location.search;
        var queryStringWithFilters = filtersModule.getUpdatedQueryString();
        if(queryString !== queryStringWithFilters) {
            queryString = queryStringWithFilters;
        }
        if (start !== 0) {
            queryString = GS.uri.Uri.putIntoQueryString(queryString,"start",start, true);
        } else {
            queryString = GS.uri.Uri.removeFromQueryString(queryString, "start");
        }

        window.location.search = queryString;
    };

    var pagination = function(pageNumber, pageSize) {
        if (typeof(window.History) !== 'undefined' && window.History.enabled === true) {
            var queryData = GS.uri.Uri.getQueryData();
            if(queryData.view === 'map') {
                mapSearch(pageNumber, pageSize);
            }
            else {
                var start = (pageNumber-1) * pageSize;
                if (start !== 0) {
                    queryData.start = start;
                } else {
                    delete queryData.start;
                }
                update(queryData);
            }
        }
        else {
            page(pageNumber, pageSize);
        }
    }

    var mapSearch = function(pageNumber, pageSize, queryStringData) {
        var start = (pageNumber-1) * pageSize;
        var queryString = '';
        if(queryStringData !== undefined) {
            if (start !== 0) {
                queryStringData.start = start;
            } else {
                delete queryStringData.start;
            }
            queryString = GS.uri.Uri.getQueryStringFromObject(queryStringData);
        }
        else {
            queryString = window.location.search;
            if (start !== 0) {
                queryString = GS.uri.Uri.putIntoQueryString(queryString,"start",start, true);
            } else {
                queryString = GS.uri.Uri.removeFromQueryString(queryString, "start");
            }
        }

        var state = { queryString: "queryString"};

        if (typeof(window.History) !== 'undefined' && window.History.enabled === true) {
            // use HTML 5 history API to rewrite the current URL to represent the new state.
            history.pushState(state, start, queryString);
        }

        refreshMapAds();
        $.ajax({
            type: 'POST',
            url: url() + queryString
        }).done(function(data) {
                renderDataForMap(data);
                GS.util.htmlMirrors.updateAll();
                if(jQuery("#js_totalResultsCountReturn").html() == "0" || jQuery("#js_totalResultsCountReturn").html() == 0 || jQuery("#js_totalResultsCountReturn").html() == ""){
                    if(jQuery("#js_totalResultsCountReturn").html() == ""){jQuery("#js_totalResultsCountReturn").html("0");jQuery("#js-moreThanOne").show()}
                    jQuery("#js_totalResultsCountReturn").popover('show');
                    jQuery(".js_closeOopsPopover").click(function() {
                        jQuery("#js_totalResultsCountReturn").popover('hide');
                    });
                }
                else{
                    jQuery("#js_totalResultsCountReturn").popover('hide');
                }
                pageTracking.clear();
                pageTracking.pageName = data.page[1].omniturePageName;
                if(setProp15ForMap) {
                    pageTracking.props['prop15'] = $('#map-sort').val();
                }
                else {
                    pageTracking.props['prop15'] = '';
                }
                pageTracking.send();
                GS_notifyQuantcastComscore();
                setProp15ForMap = false;
            }
        ).fail(function() {
                alert("error");
                jQuery("#js-spinny-search").hide();
            }
        );
    }

    var onDistanceChanged = function() {
        var $this=$(this);
        if (s && $this.is(':checked')) {
            s.prop47 = $this.val();
        }
    };

    var attachEventHandlers = function() {
        jQuery('#page-size').change(onDisplayResultsSizeChanged);
        jQuery('#map-sort').change(function(){onSortChangedForMap($(this).find(":selected").val());});
        jQuery('#js-radius').on('change', 'input', onDistanceChanged);
        jQuery('#sort-by').change(onSortChanged);
        $(body).on('click', listResultsLinkSelector, function() {
            if(GS.uri.Uri.getFromQueryString('view') === undefined) {
                return;
            }
            else {
                var uri = window.location.search;
                if (typeof(window.History) !== 'undefined' && window.History.enabled === false) {
                    uri = filtersModule.getUpdatedQueryString();
                }
                uri = GS.uri.Uri.removeFromQueryString(uri, 'view');
                window.location.search = uri;
            }
        });
        $(body).on('click', mapResultsLinkSelector, function() {
            if(GS.uri.Uri.getFromQueryString('view') === 'map') {
                return;
            }
            else {
                var uri = window.location.search;
                if (typeof(window.History) !== 'undefined' && window.History.enabled === false) {
                    uri = filtersModule.getUpdatedQueryString();
                }
                uri = GS.uri.Uri.putIntoQueryString(uri, 'view', 'map', true);
                window.location.search = uri;
            }
        });

    };

    var renderDataForMap = function(data) {
        var pageNav = $('#js-mapPageNav');
        if(data.seoTitle !== '') {
            $('#js_seoTitle').text(data.seoTitle + ' in ' + data.schoolSearchResults[1].city);
        }
        else {
            $('#js_seoTitle').text(data.schoolSearchResults[1].city + ' Schools');
        }
        if(data.page !== undefined && data.page[1].noSchoolsFound === true) {
            $('.js-rightResultsGrid').hide();
            pageNav.find('#total-results-count').html('');
            pageNav.hide();
            $('#js-noSchoolsFound').show();
            $('#js-searchResultsNav').hide();
            GS.map.getMap.refreshMarkers();
            return;
        }
        else {
            $('#js-searchResultsNav').show();
            $('#js-noSchoolsFound').hide();
            $('.js-rightResultsGrid').show();
        }

        var page = data.page[1];

        updateSortAndPageSize();

        // 3202 --> 2,202
        var formatWithCommas = function(num) {
            return num.toString().replace(/\B(?=(\d{3})+(?!\d))/g, ",");
        };

        pageNav.find('.js-search-results-paging-summary').html("Showing " + formatWithCommas(page.offset) + "-" +
                formatWithCommas(page.lastOffsetOnPage) + " of " +
            "<span id='total-results-count'>" + formatWithCommas(page.totalResults) + "</span> schools");
        pageNav.show();

        updatePageNav(page);
        updateSideBarPageNav(page);

        var showHomesForSale = 'block';
        if(!data.salePromo[1].homesForSale) {
            showHomesForSale = 'dn';
        }

        var schoolList = $('#js-schoolList');
        schoolList.html('');

        var points = [];
        for(var i = 1; i < data.schoolSearchResults.length; i++) {
            var school = data.schoolSearchResults[i];

            var parentRating = school.parentRating;
            var showParentRating = 'block';
            var showRateSchool = 'dn';
            var starsOff = 0;
            if(parentRating == null) {
               showParentRating = 'dn';
               showRateSchool = 'block';
            }
            else {
                starsOff = 5 - parentRating;
            }

            var gsRating = school.greatSchoolsRating;
            var gsRatingUrl = school.gsRatingUrl;
            var showNonPrivate = 'block';
            var showNewRatingNonPrivate ='dn';
            var showPrivate = 'dn';
            var showNewRatingPrivate = 'dn';
            var showPreschool = 'dn';

            if (school.levelCode == 'p') {
                showNonPrivate = 'dn';
                showNewRatingNonPrivate = 'dn';
                showPrivate = 'dn';
                showNewRatingPrivate = 'dn';
                showPreschool = 'block';
                gsRatingUrl = school.schoolUrl;
            } else if (school.schoolType == 'private') {
                showNonPrivate = 'dn';
                showNewRatingNonPrivate = 'dn';
                showPreschool = 'dn';
                showNewRatingPrivate = school.isNewGSRating === true ? 'block' : 'dn';
                showPrivate = school.isNewGSRating === true ? 'dn' : 'block';
                gsRatingUrl = school.schoolUrl;
            } else if ((school.schoolType == 'public' || school.schoolType == 'charter')) {
                showPrivate = 'dn';
                showNewRatingPrivate = 'dn';
                showPreschool = 'dn';
                showNewRatingNonPrivate = school.isNewGSRating === true ? 'block' : 'dn';
                showNonPrivate = school.isNewGSRating === true ? 'dn' : 'block';
            }

            if (gsRating === null) {
                gsRating = 'NR';
            }
            var isPkCompare = "none", showCompare = "inline";
            if(school.levelCode == 'p') {
                isPkCompare = "inline";
                showCompare = "dn";
            }

            var showExistsInMsl = "dn", showNotInMsl = "inline";
            if(school.mslHasSchool) {
                showExistsInMsl = 'inline';
                showNotInMsl = 'dn';
            }

            var showDistance = "inline";
            if(school.distance == null) {
                showDistance = "dn";
            }

            var infoBoxHtml = infoBoxTemplate.render({
                city: school.city,
                communityRatingUrl: school.communityRatingUrl,
                showExistsInMsl: showExistsInMsl,
                gradesRange: school.rangeString,
                id: school.id,
                isPkCompare: isPkCompare,
                showHomesForSale: showHomesForSale,
                jsEscapeName: school.jsEscapeName,
                showNotInMsl: showNotInMsl,
                omniturePageName: page.omniturePageName,
                parentRating: parentRating,
                schoolName: school.name,
                schoolType: school.schoolType,
                schoolUrl: school.schoolUrl,
                showCompare: showCompare,
                showParentRating: showParentRating,
                showRateSchool: showRateSchool,
                starsOff: starsOff,
                state: school.state,
                street: school.street,
                zip: school.zip
            });

            var sidebarListHtml = sidebarListTemplate.render({
                city: school.city,
                communityRatingUrl: school.communityRatingUrl,
                distance: school.distance,
                gradesRange: school.rangeString,
                gsRating: gsRating,
                gsRatingUrl: gsRatingUrl,
                id: school.id,
                parentRating: parentRating,
                schoolName: school.name,
                schoolType: school.schoolType,
                schoolUrl: school.schoolUrl,
                showDistance: showDistance,
                showParentRating: showParentRating,
                showNonPrivate: showNonPrivate,
                showNewRatingNonPrivate:showNewRatingNonPrivate,
                showPrivate: showPrivate,
                showNewRatingPrivate:showNewRatingPrivate,
                showPreschool: showPreschool,
                showRateSchool: showRateSchool,
                starsOff: starsOff,
                state: school.state
            });

            points.push({name: school.jsEscapeName, lat: school.latitude, lng: school.longitude,
                gsRating: school.greatSchoolsRating, schoolType: school.schoolType, infoWindowMarkup: infoBoxHtml,
                state: school.state, id: school.id, levelCode: school.levelCode,isNewGSRating: school.isNewGSRating});

            schoolList.append(sidebarListHtml);
            schoolList.find('a').each(function() {
                $(this).attr('href', $(this).attr('data-href'));
            });
        }
        GS.map.getMap.refreshMarkers(points);
    }

    var updateSideBarPageNav = function(page) {
        var currentPage = page.pageNumber;
        var sidebarPageNav = $('#sidebarPageNav');
        var pageNav = '';
        if(currentPage > 1) {
            pageNav += setPageNavIndex(currentPage - 1, page.pageSize, '« Prev')+" ";
        }
        if(currentPage < page.totalPages) {
            pageNav += setPageNavIndex(currentPage +  1, page.pageSize, 'Next »');
        }
        sidebarPageNav.html(pageNav);
    }

    var updatePageNav = function(page) {
        var pageNumbers = $('.js-pageNumbers');
        var pageNav = "";
        var totalPages = page.totalPages;
        var currentPage = page.pageNumber;
        var pageSize = page.pageSize;
        var previousPage = currentPage-1;
        var nextPage = currentPage+1;

        var ellipsis = "<span class='ellipsis'>...</span>\n";
        pageNumbers.html('');

        if(totalPages <= 1) {
            return;
        }

        if(currentPage > 1) {
            pageNav += setPageNavIndex(previousPage, pageSize, '«');
        }

        if(totalPages <= 7){
           //show all pages
            for(var i=1; i<= totalPages; i++){
                if(i == currentPage) {
                    pageNav += setEllipsisOrActive('active', i);
                }
                else {
                    pageNav += setPageNavIndex(i, pageSize, i);
                }
            }
        }
        else{
            if(currentPage > (totalPages -4)){
                pageNav += setPageNavIndex(1, pageSize, 1);
                pageNav += setEllipsisOrActive('ellipsis', '...');
                for(var i=totalPages-4; i<= totalPages; i++){
                    if(i == currentPage) {
                        pageNav += setEllipsisOrActive('active', i);
                    }
                    else {
                        pageNav += setPageNavIndex(i, pageSize, i);
                    }
                }
            }
            else{
                if(currentPage <= 4){
                    for(var i=1; i<= 5; i++){
                        if(i == currentPage) {
                            pageNav += setEllipsisOrActive('active', i);
                        }
                        else {
                            pageNav += setPageNavIndex(i, pageSize, i);
                        }
                    }
                    pageNav += setEllipsisOrActive('ellipsis', '...');
                    pageNav += setPageNavIndex(totalPages, pageSize, totalPages);
                }
                else{
                    pageNav += setPageNavIndex(1, pageSize, 1);
                    pageNav += setEllipsisOrActive('ellipsis', '...');
                    for(var i=currentPage-1; i<= currentPage+1; i++){
                        if(i == currentPage) {
                            pageNav += setEllipsisOrActive('active', i);
                        }
                        else {
                            pageNav += setPageNavIndex(i, pageSize, i);
                        }
                    }
                    pageNav += setEllipsisOrActive('ellipsis', '...');
                    pageNav += setPageNavIndex(totalPages, pageSize, totalPages);
                }
            }
        }

        if(currentPage < totalPages) {
            pageNav += setPageNavIndex(nextPage, page.pageSize, '»');
        }
        pageNumbers.html(pageNav);
    }

    var setPageNavIndex = function(pageNum, pageSize, indexValue) {
        var index = "<span><a class='js-prev' href='javascript:void(0)' onclick='GS.search.results.pagination(" + pageNum + ", " + pageSize + ");'>" +
            indexValue + "</a></span>\n";
        return index;
    }

    var setEllipsisOrActive = function(className, value) {
        var index = "<span class=" + className + ">" + value + "</span>\n";
        return index;
    }

    var updateSortAndPageSize = function() {
        var queryString = window.location.search;
        var queryStringData = GS.uri.Uri.getQueryData(queryString);
        if(queryStringData.sortBy !== undefined) {
            $('#map-sort').val(queryStringData.sortBy);
        }

        if(queryStringData.pageSize !== undefined) {
            $('#page-size').val(queryStringData.pageSize);
        }
    }

    var refreshAds = function() {
        var adSlotKeys = ['Search_Site_Footer_728x90', 'Search_Site_Header_728x90', 'Search_Site_AboveFold_300x250',
            'Search_Site_BelowFold_Top_300x125', 'Search_Site_Sponsor_630x40'];
        GS.ad.unhideGhostTextForAdSlots(adSlotKeys);
        GS.ad.refreshAds(adSlotKeys);
    };
    
    var refreshMapAds = function() {
        var adSlotKeys = ['Search_Results_Map_Footer_728x90', 'Search_Results_Map_Header_728x90', 'Search_Results_Map_AboveFold_300x250',
            'Search_Results_Map_BelowFold_Top_300x125'];
        GS.ad.unhideGhostTextForAdSlots(adSlotKeys);
        GS.ad.refreshAds(adSlotKeys);
    };

    var showFacebookFriendsForResults = function() {
        GS.facebook.status({
            connected: function() {
                GS.facebook.getUserFriendsSchoolPageData(handleUIForFacebookResults);
                GS.search.results.showAskAFriendLinks();
            }
        })
    };

    var showAskAFriendLinks = function() {
        $('.js-ask-a-friend').show();
    };

    var handleUIForFacebookResults = function(schoolPagesByFacebookUrl, schoolPagesBySchoolHash) {
        var $schoolResults = $('.js-school-search-result');

        $schoolResults.each(function() {
            var text = '';
            var $this = $(this);
            var numberFans = 0;
            var phrase;
            var $schoolData = $this.find('.js-school-data');
            var $facepile = $this.find('.js-facepile');
            var schoolName = $schoolData.data('gs-school-name');
            var city = $schoolData.data('gs-city');
            var state = $schoolData.data('gs-state');
            var facebookUrl = $schoolData.data('gs-facebook-url');
            var schoolHash = GS.facebook.createSchoolHash(schoolName, city, state);
            var fans = [];
            var dedupedFans = [];
            var faces = 0;
            var i;

            // stores all photos shown per result. used to prevent duplicate faces from being shown
            var photos = [];

            // Sometimes facebook URLs from Factual and from Facebook have either https or http, and they will mismatch
            facebookUrl = facebookUrl.replace("https:","http:");

            if (facebookUrl !== "" && schoolPagesByFacebookUrl.hasOwnProperty(facebookUrl)) {
                fans = fans.concat(schoolPagesByFacebookUrl[facebookUrl].fans);
            }
            if (schoolPagesBySchoolHash.hasOwnProperty(schoolHash)) {
                fans = fans.concat(schoolPagesBySchoolHash[schoolHash].fans);
            }

            i = fans.length;
            while (i--) {
                var fan = fans[i];
                if ($.inArray(fan.pic_square, photos) === -1) {
                    photos.push(fan.pic_square);
                    dedupedFans.push(fan);
                }
            }

            numberFans = dedupedFans.length;

            $.map(dedupedFans, function(fan) {
                if (faces < 5 ){
                    text = text + '<img style="padding-right:2px;" width="30px" height="30px" class="vam" title="' + fan.name + '" src="' + fan.pic_square + '"/>';
                    faces = faces + 1;
                }
            });

            if (text !== '') {
                if (numberFans === 1) {
                    phrase = " friend is";
                } else {
                    phrase = " friends are";
                }
                text = text +"<span class='iconx16 i-16-fbFacepile vam mlm'><!--Do not collapse--></span><span class='small vam mls'>" + numberFans + phrase + " connected to this school.</span>";
                $facepile.html(text);
                $facepile.show();
            }
        });
    };

    return {
        init:init,
        update:update,
        page:page,
        pagination:pagination,
        mapSearch:mapSearch,
        updateSortAndPageSize:updateSortAndPageSize,
        refreshAds: refreshAds,
        handleUIForFacebookResults: handleUIForFacebookResults,
        showFacebookFriendsForResults: showFacebookFriendsForResults,
        showAskAFriendLinks: showAskAFriendLinks
    };

})();