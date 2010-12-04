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
        value = window.location.protocol + "//" + window.location.host + window.location.pathname;
        return value;
    };

    this.search = function(callback) {
        var i = 0;
        var data = {};
        var gradeLevels = [];
        var schoolTypes = [];

        data.requestType = "ajax";
        data.decorator="emptyDecorator";
        data.confirm="true";
        
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

        var queryString = window.location.search;
        queryString = removeFromQueryString(queryString, "gradeLevels");
        queryString = removeFromQueryString(queryString, "schoolTypes");

        jQuery.get(this.url() + queryString, data , callback);
    };
};

GS.search.SchoolSearchResultsTable = function() {
    var thisDomElement = jQuery('#school-search-results-table-body tbody'); //TODO: pass this into constructor
    var checkedSchoolsList = getFromQueryString("compareSchools");
    var checkedSchools = [];

    if (checkedSchoolsList !== undefined && checkedSchoolsList.length > 0) {
        checkedSchools = checkedSchoolsList.split(',');
    }

    jQuery('.compare-school-checkbox').change(function(item) {
        var checkbox = jQuery(item.currentTarget);
        var checked = checkbox.attr("checked");
        var rowId = checkbox.parent().parent().attr("id");
        var row = jQuery('#' + rowId);
        var statePlusSchoolId = row.find('input.compare-school-checkbox').attr('id');

        if (checked) {
            checkedSchools.push(statePlusSchoolId);
            this.selectRow(checkbox.parent().parent().attr("id"));
        } else {
            var index = checkedSchools.indexOf(statePlusSchoolId);
            if (index !== -1) {
                checkedSchools.splice(index,1);
            }
            this.deselectRow(checkbox.parent().parent().attr("id"));
        }

        console.log("checkboxes: " + checkedSchools.join(','));
        this.updateAllCompareButtons();

    }.gs_bind(this));  //now, references to "this" in method will reference containing method's scope

    this.updateAllCompareButtons = function() {
        var count = checkedSchools.length;

        for (var i = 0; i < count; i++) {
            var id = checkedSchools[i];
            this.updateCompareButton(jQuery('#'+id).parent().parent().attr("id"));
        }
    };

    this.updateCompareButton = function(rowDomId) {
        var row = jQuery('#' + rowDomId);
        var compareLabel = row.find('td.js-checkbox-column > .js-compareLabel');
        var compareHelperMessage = row.find('td.js-checkbox-column > .js-compareHelperMessage');
        var compareButton = row.find('td.js-checkbox-column > .js-compareButton');

        compareLabel.hide();
        compareHelperMessage.hide();
        compareButton.hide();

        if (checkedSchools.length === 0) {
            compareLabel.show();
        } else if (checkedSchools.length === 1) {
            compareHelperMessage.show();
        } else if (checkedSchools.length > 1) {
            compareButton.show();
        }
    };

    this.selectRow = function(rowDomId) {
        var row = jQuery('#' + rowDomId);
        var stars = row.find('td.stars-column > span');
        var starsClass = stars.attr('class');
        var badge = row.find('td.badge-column > span');

        jQuery(row).find('td').addClass("bg-color-f4fafd");

        if (badge.length !== 0) {
            var badgeClass = badge.attr('class');
            var blueBadgeClass = badgeClass + '_b';
            badge.removeClass(badgeClass).addClass(blueBadgeClass);
        }

        var blueStarsClass = starsClass + '_b';
        stars.removeClass(starsClass).addClass(blueStarsClass);
        this.updateCompareButton(rowDomId);
    };

    this.deselectRow = function(rowDomId) {
        var row = jQuery('#' + rowDomId);
        var isBlue = null;
        var pattern1 = /_b/gi;
        var pattern3 = /sprite badge_sm_(\d{1,2}|[a-z]{2})/gi;
        var pattern4 = /sprite stars_sm_(\d|[a-z_]{7})/gi;
        var stars = row.find('td.stars-column > span');
        var starsClass = stars.attr('class');
        (starsClass.match(pattern1) === null) ? isBlue = false : isBlue = true;
        var whiteStarsClass = starsClass.match(pattern4)[0];
        var badge = row.find('td.badge-column > span');
        if (badge.length != 0) {
            var badgeClass = badge.attr('class');
            var whiteBadgeClass = badgeClass.match(pattern3)[0];
            badge.removeClass(badgeClass).addClass(whiteBadgeClass);
        }
        stars.removeClass(starsClass).addClass(whiteStarsClass);

        jQuery(row).find('td').removeClass("bg-color-f4fafd");

        var compareLabel = row.find('td.js-checkbox-column > .js-compareLabel');
        var compareHelperMessage = row.find('td.js-checkbox-column > .js-compareHelperMessage');
        var compareButton = row.find('td.js-checkbox-column > .js-compareButton');

        compareLabel.show();
        compareHelperMessage.hide();
        compareButton.hide();
    };

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

        jQuery('#spinner').show();

        jQuery('#school-search-results-table-body').animate(
            { opacity: .2 },
            250,
            'linear',
            function() {

            }
        );

        searcher.search(function(data) {

            jQuery('#js-school-search-results-table').html(data);
            jQuery('#school-search-results-table-body').css("opacity",.2);
            jQuery('#school-search-results-table-body').animate(
                {opacity: 1},
                250,
                'linear',
                function() {
                    jQuery("#spinner").hide();
                }
            );

        }.gs_bind(this));
    };

    this.getCheckedSchools = function() {
        return checkedSchools;
    }

    this.updateAllCompareButtons();

};

jQuery(function() {
    GS.search.schoolSearchResultsTable = new GS.search.SchoolSearchResultsTable();

    jQuery('#topicbarGS input').change(function() {
        //jQuery("#spinner").css("top",jQuery(this).position().top+118);
        //jQuery("#spinner").css("left",jQuery(this).position().left+25);
        GS.search.schoolSearchResultsTable.update();
    });
});