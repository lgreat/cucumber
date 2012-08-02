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

//            $this.addClass('selected');

            var newSort = sorts[0];
            if (sorts[0] === currentSort) {
                newSort = sorts[1];
            }

            queryData['sortBy'] = newSort;
            delete queryData.start;

            if (typeof(window.History) === 'undefined' || window.History.enabled !== true) {
                window.location.search = GS.uri.Uri.getQueryStringFromObject(queryData);
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
            // use HTML 5 history API to rewrite the current URL to represent the new state.
            window.History.replaceState(null, document.title, queryString);
        }

        refreshAds();
        jQuery.ajax({
            type: "get",
            url: url() + queryString,
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
                    jQuery("#spinner").hide();
                    //reattach callbacks to dom element events
                    attachEventHandlers();
                    // ask persistent compare to tell us what schools are in compare
                    GS.school && GS.school.compare && GS.school.compare.triggerUpdatePageWithSchoolsEvent();
                };

                jQuery('#js-school-search-results-table').html(data);
                jQuery('#js-school-search-results-table-body').css("opacity",.2);
                jQuery('#js-school-search-results-table-body').animate(
                    {opacity: 1},
                    250,
                    'linear',
                    afterFadeIn
                );
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
            };

            var onSearchError = function() {
                clear();
                jQuery("#spinner").hide();
                jQuery("#js-spinny-search").hide();
            };

            jQuery('#spinner').show();
            jQuery("#js-spinny-search").show();

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
        var queryData = GS.uri.Uri.getQueryData();
        queryData['pageSize'] = pageSize;
        delete queryData.start;
        if(queryData.view == 'map') {
            mapSearch(1, pageSize, queryData);
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

        if (selectValue !== '' && typeof(selectValue) !== 'undefined') {
            queryString = GS.uri.Uri.putIntoQueryString(queryString,"sortBy",selectValue, true);
        } else {
            queryString = GS.uri.Uri.removeFromQueryString(queryString, "sortBy");
        }

        if (typeof(window.History) === 'undefined' || window.History.enabled !== true) {
            queryString = GS.uri.Uri.removeFromQueryString(queryString, "start");
            window.location.search = queryString;
            return;
        }

        var queryStringData = GS.uri.Uri.getQueryData(queryString);

        mapSearch(1, 25, queryStringData);
    };

    var page = function(pageNumber, pageSize) {
        var start = (pageNumber-1) * pageSize;
        var queryString = window.location.search;
        queryString = GS.uri.Uri.putIntoQueryString(queryString,"start",start, true);

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
                queryData.start = start;
                update(queryData);
            }
        }
        else {
            page(pageNumber, pageSize);
        }
    }

    var mapSearch = function(pageNumber, pageSize, queryStringData) {
        var start = (pageNumber-1) * pageSize;
        if(queryStringData !== undefined) {
            var queryString = GS.uri.Uri.getQueryStringFromObject(queryStringData);
            queryStringData = GS.uri.Uri.getQueryData(queryString);
            queryStringData.start = start;
            queryString = GS.uri.Uri.getQueryStringFromObject(queryStringData);
        }
        else {
            var queryString = window.location.search;
            queryString = GS.uri.Uri.putIntoQueryString(queryString,"start",start, true);
        }

        var state = { queryString: "queryString"};

        if (typeof(window.History) !== 'undefined' && window.History.enabled === true) {
            // use HTML 5 history API to rewrite the current URL to represent the new state.
            history.pushState(state, start, queryString);
        }
        jQuery("#js-spinny-search").show();

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
            }
        ).fail(function() {
                alert("error");
                jQuery("#js-spinny-search").hide();
            }
        );
    }

    var attachEventHandlers = function() {
        jQuery('#page-size').change(onDisplayResultsSizeChanged);
        jQuery('#map-sort').change(function(){onSortChangedForMap($(this).find(":selected").val());});
        jQuery('#sort-by').change(onSortChanged);
        jQuery('.js-redobtn').click(redoSearch);
        $(body).on('click', listResultsLinkSelector, function() {
            if(GS.uri.Uri.getFromQueryString('view') === undefined) {
                return;
            }
            else {
                var uri = window.location.search;
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
                uri = GS.uri.Uri.putIntoQueryString(uri, 'view', 'map', true);
                window.location.search = uri;
            }
        });

    };

    var renderDataForMap = function(data) {
        var pageNav = $('#js-mapPageNav');
        if(data.noSchoolsFound === true) {
            $('.js-rightResultsGrid').hide();
            pageNav.find('#total-results-count').html('');
            pageNav.hide();
            $('#js-noSchoolsFound').show();
            GS.map.getMap.refreshMarkers();
            return;
        }
        else {
            $('#js-noSchoolsFound').hide();
            $('.js-rightResultsGrid').show();
        }

        var page = data.page[1];

        updateSortAndPageSize();

        // 3202 --> 2,202
        var formatWithCommas = function(num) {
            return num.toString().replace(/\B(?=(\d{3})+(?!\d))/g, ",");
        };

        console.log(page.totalResults);
        console.log(page.offset);
        console.log(page.lastOffsetOnPage);
        console.log(formatWithCommas(page.totalResults));
        console.log(formatWithCommas(page.offset));
        console.log(formatWithCommas(page.lastOffsetOnPage));
        console.log(1);
        pageNav.find('.js-search-results-paging-summary').html("Showing " + formatWithCommas(page.offset) + "-" +
                formatWithCommas(page.lastOffsetOnPage) + " of " +
            "<span id='total-results-count'>" + formatWithCommas(page.totalResults) + "</span> schools");
        pageNav.show();
        console.log(2);

        updatePageNav(page);

        var homesForSale = "block";
        if(!data.salePromo[1].homesForSale) {
            homesForSale = "none";
        }

        var schoolList = $('#js-schoolList');
        schoolList.html('');

        var points = [];
        for(var i = 1; i < data.schoolSearchResults.length; i++) {
            var school = data.schoolSearchResults[i];

            var parentRating = school.parentRating;
            var showParentRating = 'block';
            var showRateSchool = 'none';
            var starsOff = 0;
            if(parentRating == null) {
               showParentRating = 'none';
               showRateSchool = 'block';
            }
            else {
                starsOff = 5 - parentRating;
            }

            var gsRating = school.greatSchoolsRating;
            var showNonPrivate = 'block';
            var showPrivate = 'none';
            if(school.schoolType == 'private') {
                showNonPrivate = 'none';
                showPrivate = 'block';
            }
            else if(gsRating == null) {
                gsRating = 'n/a';
            }

            var isPkCompare = "none", showCompare = "inline";
            if(school.levelCode == 'p') {
                isPkCompare = "inline";
                showCompare = "none";
            }

            var existsInMsl = "none", notInMsl = "block";
            if(school.mslHasSchool) {
                existsInMsl = "block";
                notInMsl = "none";
            }

            var showDistance = "inline";
            if(school.distance == null) {
                showDistance = "none";
            }

            var infoBoxHtml = infoBoxTemplate.render({
                city: school.city,
                communityRatingUrl: school.communityRatingUrl,
                existsInMsl: existsInMsl,
                gradesRange: school.rangeString,
                id: school.id,
                isPkCompare: isPkCompare,
                homesForSale: homesForSale,
                jsEscapeName: school.jsEscapeName,
                notInMsl: notInMsl,
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
                gsRatingUrl: school.gsRatingUrl,
                id: school.id,
                parentRating: parentRating,
                schoolName: school.name,
                schoolType: school.schoolType,
                schoolUrl: school.schoolUrl,
                showDistance: showDistance,
                showParentRating: showParentRating,
                showNonPrivate: showNonPrivate,
                showPrivate: showPrivate,
                showRateSchool: showRateSchool,
                starsOff: starsOff,
                state: school.state
            });

            points.push({name: school.jsEscapeName, lat: school.latitude, lng: school.longitude,
                gsRating: school.greatSchoolsRating, schoolType: school.schoolType, infoWindowMarkup: infoBoxHtml,
                state: school.state, id: school.id});

            schoolList.append(sidebarListHtml);
            schoolList.find('a').each(function() {
                $(this).attr('href', $(this).attr('data-href'));
            });
        }
        GS.map.getMap.refreshMarkers(points);
    }

    var updatePageNav = function(page) {
        var pageNumbers = $('.js-pageNumbers');
        var pageNav = "";

        var ellipsis = "<span class='ellipsis'>...</span>\n";
        pageNumbers.html('');

        if(page.totalPages == 1) {
            return;
        }

        if(page.pageNumber > 1) {
            pageNav += setPageNavIndex(page.previousPage, page.pageSize, '« Prev');
        }

        if(page.totalPages >= 5 && page.pageNumber >= 5) {
            pageNav += setPageNavIndex(page.firstPageNum, page.pageSize, page.firstPageNum);
            pageNav += setEllipsisOrActive('ellipsis', '...');
        }

        var pageSequence = page.pageSequence;
        for(var i = 0; i < pageSequence.length; i++) {
            if(pageSequence[i] == page.pageNumber) {
                pageNav += setEllipsisOrActive('active', pageSequence[i]);
            }
            else {
                pageNav += setPageNavIndex(pageSequence[i], page.pageSize, pageSequence[i]);
            }
        }

        if(page.totalPages > 5 && (page.pageNumber < 5 || (page.pageNumber <= page.totalPages - 4 ))) {
            pageNav += setEllipsisOrActive('ellipsis', '...');
            pageNav += setPageNavIndex(page.lastPageNum, page.pageSize, page.lastPageNum);
        }

        if(page.pageNumber < page.totalPages) {
            pageNav += setPageNavIndex(page.nextPage, page.pageSize, 'Next »');
        }
        pageNumbers.html(pageNav);
    }

    var setPageNavIndex = function(pageNum, pageSize, indexValue) {
        var index = "<span class='js-prev' onclick='GS.search.results.pagination(" + pageNum + ", " + pageSize + ");'>" +
            indexValue + "</span>\n";
        return index;
    }

    var setEllipsisOrActive = function(className, value) {
        var index = "<span class=" + className + ">" + value + "</span>\n";
        return index;
    }

    var redoSearch = function() {
        var redoSearchDiv = $("#js-redoSearch");
        redoSearchDiv.dialog('close');
        var queryString = window.location.search;
        var queryStringData = GS.uri.Uri.getQueryData(queryString);
        for(var key in queryStringData) {
            if(key !== 'search_type' && key !== 'c' && key !== 'view') {
                delete queryStringData[key];
            }
        }
        queryStringData.start = 0;
        queryStringData.state = redoSearchDiv.find('#js-redoState').val();
        queryStringData.lat = redoSearchDiv.find('#js-redoLat').val();
        queryStringData.lon = redoSearchDiv.find('#js-redoLng').val();
        queryStringData.zipCode = redoSearchDiv.find('#js-redoZipCode').val();
        queryStringData.rs = true;
        window.location.search = GS.uri.Uri.getQueryStringFromObject(queryStringData);
    }

    var updateSortAndPageSize = function() {
        var queryString = window.location.search;
        var queryStringData = GS.uri.Uri.getQueryData(queryString);
        if(queryStringData.sortBy !== undefined) {
            $('#map-sort').val(queryStringData.sortBy);
        }
        else {
            $('#map-sort').val('');
        }

        if(queryStringData.pageSize !== undefined) {
            $('#page-size').val(queryStringData.pageSize);
        }
        else {
            $('#page-size').val('25');
        }
    }

    var refreshAds = function() {
        var adSlotKeys = ['Search_Site_Footer_728x90', 'Search_Site_Header_728x90', 'Search_Site_AboveFold_300x250',
            'Search_Site_BelowFold_Top_300x125', 'Search_Site_Sponsor_630x40', 'Search_Site_Custom_Welcome_Ad',
            'Search_Site_Custom_Peelback_Ad', 'Search_Site_Global_NavPromo_970x30'];
        GS.ad.refreshAds(adSlotKeys);
    };
    
    var refreshMapAds = function() {
        var adSlotKeys = ['Search_Results_Map_Footer_728x90', 'Search_Results_Map_Header_728x90', 'Search_Results_Map_AboveFold_300x250',
            'Search_Results_Map_BelowFold_Top_300x125', 'Search_Results_Map_Custom_Welcome_Ad',
            'Search_Results_Map_Custom_Peelback_Ad', 'Search_Results_Map_Global_NavPromo_970x30'];
        GS.ad.refreshAds(adSlotKeys);
    };

    return {
        init:init,
        update:update,
        page:page,
        pagination:pagination,
        mapSearch:mapSearch,
        updateSortAndPageSize:updateSortAndPageSize,
        refreshAds: refreshAds
    };

})();