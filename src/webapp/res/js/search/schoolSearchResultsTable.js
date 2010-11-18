GS = GS || {};
GS.search = GS.search || {};

GS.search.SchoolSearchResult = function() {
    var element = jQuery('#' + 'js-school-search-result-template');//TODO: pass into constructor
    var nameObject = element.find('.js-school-search-result-name');
    var streetObject = element.find('.js-school-search-result-street');
    var cityStateZipObject = element.find('.js-school-search-result-citystatezip');

    this.setName = function(name) {
        nameObject.html(name);
    };

    this.setStreet = function(street) {
        streetObject.html(street);
    };

    this.setCityStateZip = function(cityStateZip) {
        cityStateZipObject.html(cityStateZip);
    };

    this.getElement = function() {
        return element;
    }
};

GS.search.SchoolSearcher = function() {
    this.url = function() {
        var value = window.location.href;
        return value;
    };

    this.search = function(callback) {
        var i = 0;
        var data = {};
        var gradeLevels = [];
        var schoolTypes = [];

        data.format = "json";
        
        //to populate an array inside a Spring command, Spring requires data in format gradeLevels[0]=e,gradeLevels[1]=m
        jQuery('#js-gradeLevels :checked').each(function() {
            gradeLevels[i++] = jQuery(this).val();
        });

        i = 0;
        jQuery('#js-schoolTypes :checked').each(function() {
            schoolTypes[i++] = jQuery(this).val();
        });
        data["gradeLevels"] = gradeLevels;
        data["schoolTypes"] = schoolTypes;

        jQuery.getJSON(this.url(), data , callback);
    };
};

GS.search.SchoolSearchResultsTable = function() {
    var thisDomElement = jQuery('#school-search-results-table-body tbody'); //TODO: pass this into constructor

    this.clear = function() {
        thisDomElement.find('.school-search-result-row').remove();
    };

    this.add = function(schoolSearchResult) {
        var template = new GS.search.SchoolSearchResult();
        template.setName(schoolSearchResult.name);
        template.setStreet(schoolSearchResult.address.street);
        template.setCityStateZip(schoolSearchResult.address.cityStateZip);
        var item = template.getElement().clone();
        item.attr("class","school-search-result-row");
        item.attr("id","school-search-result-" + schoolSearchResult.databaseState + schoolSearchResult.id);
        item.show();
        thisDomElement.append(item);
        return;
    };

    this.addAll = function(schoolSearchResults) {
        thisDomElement.hide();

        var length = schoolSearchResults.length;

        for (var i = 0; i < length; i++ ) {
            this.add(schoolSearchResults[i]);
        }

        thisDomElement.show();
        return;
    };

    this.transformToMapSchools = function(schoolSearchResults) {
        var numberOfSearchResults = schoolSearchResults.length;
        var mapSchools = [];
        if (numberOfSearchResults > 0) {
            for (var i = 0; i < numberOfSearchResults; i++) {
                var searchResult = schoolSearchResults[i];
                var mapSchool = new GS.map.MapSchool(
                    searchResult.id,
                    searchResult.databaseState,
                    searchResult.name,
                    searchResult.latLon.lat,
                    searchResult.latLon.lon
                );
                mapSchool.gsRating = searchResult.greatSchoolsRating;
                mapSchool.parentRating = searchResult.parentRating;
                mapSchool.type = searchResult.schoolType;
                mapSchools.push(mapSchool);
            }
        }
        return mapSchools;
    };

    this.update = function() {
        var searcher = new GS.search.SchoolSearcher();
        searcher.search(function(data) {
            var start = data.start;
            var currentPage = data.currentPage;
            var totalResults = data.totalResults;
            var pageSize = data.pageSize;
            var totalPages = data.totalPages;

            start++;

            this.clear();

            //update paging info. TODO: move out of this method
            jQuery('#js-search-results-paging-summary').html("Showing results " + start + "-" + currentPage*pageSize + " of " + totalResults);

            var pagerHtml = "";
            for (i = 1; i <= totalPages; i++) {
                var pagerTabClass = "pager-tab";
                if (i === currentPage) {
                    var pagerTabClass = "pager-tab active";
                }
                pagerHtml = pagerHtml + "<span class='" + pagerTabClass + "' onclick='page(" + i + ")'>" + i + "</span>";
            }
            pagerHtml = pagerHtml + "<span class='prv_nxt'>Next</span>";

            jQuery('#pagination').html(pagerHtml);

            this.addAll(data.schoolSearchResults);

            var mapSchools = this.transformToMapSchools(data.schoolSearchResults);
            GS.map.schoolMap.removeAllSchools();
            console.log(mapSchools);
            GS.map.schoolMap.addSchools(mapSchools, function() {});
            GS.map.schoolMap.drawMarkers();
        }.gs_bind(this));
    };
};

jQuery(function() {
    GS.search.schoolSearchResultsTable = new GS.search.SchoolSearchResultsTable();

    jQuery('#topicbarGS input').change(function() {
        GS.search.schoolSearchResultsTable.update();
    });
});