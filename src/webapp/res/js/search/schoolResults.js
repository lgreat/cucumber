var GS = GS || {};
GS.search = GS.search || {};
GS.search.results = GS.search.results || (function() {
    var $thisDomElement = jQuery('#js-school-search-results-table-body'); //TODO: pass this into constructor
    var filtersModule;
    var compareModule;
    var customLinksModule;
    var infoBoxTemplate, sidebarListTemplate;

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

    var init = function(_filtersModule, _compareModule) {
        filtersModule = _filtersModule;
        compareModule = _compareModule;

        attachEventHandlers();

        compareModule.initializeRowsAndCheckedSchoolsArray();

        var blackTriangleDown = '&#9662;';
//        var whiteTriangleDown = '&#9663;';
        var blackTriangleUp = '&#9652;';
//        var whiteTriangleUp = '&#9653;';
        $('body').on('click', '[data-gs-sort-toggle]', function() {
            var $this = $(this);
            var sorts = $this.data('gs-sort-toggle').split(',');
            var queryData = GS.uri.Uri.getQueryData();
            var currentSort = queryData['sortBy'];

            var newSort = sorts[0];
            if (sorts[0] === currentSort) {
                newSort = sorts[1];
            }

            queryData['sortBy'] = newSort;
            delete queryData.start;

            $('body [data-gs-sort-toggle]span').each(function() {
//                $this.attributes["-webkit-transform: rotate(180deg);" -moz-transform: rotate(180deg); -ms-transform: rotate(180deg); -o-transform: rotate(180deg);transform: rotate(180deg);] ;
//                $(this).html($(this).html().replace('2','3')); // just change whichever triangle is currently in use, to make it white
            });

            if (newSort.indexOf('DESCENDING') !== -1) {
                $this.html(blackTriangleUp);
            } else {
                $this.html(blackTriangleDown);
            }

            refreshAds();
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
                    compareModule.updateAllCheckedRows();
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
            };

            var onSearchError = function() {
                clear();
                jQuery("#spinner").hide();
            };

            jQuery('#spinner').show();

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

    var onPageSizeChangedForMap = function() {
        var queryString = window.location.search;
        var pageSize = jQuery('#page-size').val();
        queryString = GS.uri.Uri.putIntoQueryString(queryString,"pageSize", pageSize, true);
        queryString = GS.uri.Uri.removeFromQueryString(queryString, "start");
        var pageSizeState = { queryString: "queryString"};
        history.pushState(pageSizeState, pageSize, queryString);
        refreshAds();
        mapSearch(1, pageSize);
    };

    var onSortChanged = function() {
        var i = 0;
        var gradeLevels = [];
        var schoolTypes = [];
        var queryString = window.location.search;

        queryString = persistCompareCheckboxesToQueryString(queryString);
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
        queryString = persistCompareCheckboxesToQueryString(queryString);

        if (selectValue !== '' && typeof(selectValue) !== 'undefined') {
            queryString = GS.uri.Uri.putIntoQueryString(queryString,"sortBy",selectValue, true);
        } else {
            queryString = GS.uri.Uri.removeFromQueryString(queryString, "sortBy");
        }
        var sortState = { queryString: "queryString"};
        history.pushState(sortState, selectValue, queryString);
        refreshAds();
        mapSearch(1, 25);
    };

    var page = function(pageNumber, pageSize) {
        var start = (pageNumber-1) * pageSize;
        var queryString = window.location.search;
        queryString = persistCompareCheckboxesToQueryString(queryString);
        queryString = GS.uri.Uri.putIntoQueryString(queryString,"start",start, true);

        window.location.search = queryString;
    };

    var pagination = function(pageNumber, pageSize) {
        refreshAds();
        mapSearch(pageNumber, pageSize);
    }

    var mapSearch = function(pageNumber, pageSize, queryStringData) {
        var start = (pageNumber-1) * pageSize;
        if(queryStringData !== undefined) {
            var queryString = GS.uri.Uri.getQueryStringFromObject(queryStringData);
            queryString = persistCompareCheckboxesToQueryString(queryString);
            queryStringData = GS.uri.Uri.getQueryData(queryString);
            queryStringData.start = start;
            queryString = GS.uri.Uri.getQueryStringFromObject(queryStringData);
        }
        else {
            var queryString = window.location.search;
            queryString = persistCompareCheckboxesToQueryString(queryString);
            queryString = GS.uri.Uri.putIntoQueryString(queryString,"start",start, true);
        }

        var state = { queryString: "queryString"};
        history.pushState(state, start, queryString);

        $.ajax({
            type: 'POST',
            url: document.location
        }).done(function(data) {
                renderDataForMap(data);
                GS.util.htmlMirrors.updateAll();
            }
        ).fail(function() {
                alert("error");
            }
        );
    }

    var persistCompareCheckboxesToQueryString = function(queryString) {
        var compareSchoolsList = compareModule.getCheckedSchools().join(',');
        if (compareSchoolsList !== undefined && compareSchoolsList.length > 0) {
            queryString = GS.uri.Uri.putIntoQueryString(queryString, "compareSchools", compareSchoolsList, true);
        } else {
            queryString = GS.uri.Uri.removeFromQueryString(queryString, "compareSchools");
        }
        return queryString;
    };


    var sendToCompare = function() {
        var checkedSchools = compareModule.getCheckedSchools();
        if (checkedSchools !== undefined && checkedSchools.length > 0) {
            var encodedCurrentUrl = encodeURIComponent(window.location.pathname + filtersModule.getUpdatedQueryString());
            window.location ='/school-comparison-tool/results.page?schools=' + checkedSchools.join(',') +
                    '&source=' + encodedCurrentUrl;
        }
    };

    var attachEventHandlers = function() {
        jQuery('.compare-school-checkbox').click(compareModule.onCompareCheckboxClicked);
        jQuery('#page-size').change(onPageSizeChangedForMap);
        jQuery('.js-compareButton').click(sendToCompare());
        jQuery('.js-num-checked-send-to-compare').click(sendToCompare());
        jQuery('.js-compare-uncheck-all').click(compareModule.onCompareUncheckAllClicked);
        jQuery('#map-sort').change(function(){onSortChangedForMap($(this).find(":selected").val());});
        jQuery('#sort-by').change(onSortChanged);
        jQuery('.js-redobtn').click(redoSearch);
    };

    var renderDataForMap = function(data) {
        if(data.noSchoolsFound == true) {
            $('.js-rightResultsGrid').hide();
            $('.js-leftResultsGrid').hide();
            $('#js-school-search-results-table-body').show();
            return;
        }
        else {
            $('#js-school-search-results-table-body').hide();
            $('.js-rightResultsGrid').show();
            $('.js-leftResultsGrid').show();
        }

        var page = data.page[1];

        updateSortAndPageSize();

        $('.js-search-results-paging-summary').html("Showing " + page.offset + "-" + page.lastOffsetOnPage + " of " +
            "<span id='total-results-count'>" + page.totalResults + "</span> schools");

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
            if(parentRating == null) {
                parentRating = "rate_it";
            }
            var gsRating = school.greatSchoolsRating;
            if(school.schoolType == 'private') {
                gsRating = 'pr';
            }
            else if(gsRating == null) {
                gsRating = 'na';
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
            'Search_Site_BelowFold_Top_300x125', 'Search_Site_SponsoredSearch_Top_423x120',
            'Search_Site_SponsoredSearch_Bottom_423x68', 'Search_Site_SponsoredSearch_Top_423x68'];
        GS.ad.refreshAds(adSlotKeys);
    }

    return {
        init:init,
        update:update,
        page:page,
        sendToCompare:sendToCompare,
        pagination:pagination,
        mapSearch:mapSearch,
        updateSortAndPageSize:updateSortAndPageSize,
        refreshAds: refreshAds
    };

})();