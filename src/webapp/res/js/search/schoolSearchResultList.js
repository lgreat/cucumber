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
        var value = window.location.href + '?format=json';
        return value;
    };

    this.search = function(callback) {
        var gradeLevels = [];
        var i = 0;
        jQuery('#js-gradeLevels input :checked').each(function(item) {
            gradeLevels[i++] = jQuery(this).val();
        });

        console.log("gradeLevels: " + gradeLevels.join());
        jQuery.getJSON(this.url(), {gradeLevels:gradeLevels.join()}, callback);
    };
};

GS.search.SchoolSearchResultsTable = function() {
    var thisDomElement = jQuery('#school-search-results-table-body tbody'); //TODO: pass this into constructor

    this.clear = function() {
        thisDomElement.find('.school-search-result-row').remove();
    };

    this.add = function(schoolSearchResult) {
        console.log("school search result is: ");
        console.log(schoolSearchResult);
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

    this.update = function() {
        var searcher = new GS.search.SchoolSearcher();
        searcher.search(function(data) {
            this.clear();
            this.addAll(data.schoolSearchResults);
        }.gs_bind(this));
    };
};

GS.search.schoolSearchResultsTable = new GS.search.SchoolSearchResultsTable();
jQuery(function() {
    jQuery('#js-gradeLevels input').change(function() {
        GS.search.schoolSearchResultsTable.update();
    });
});