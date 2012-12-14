/**
 * Created with IntelliJ IDEA.
 * User: mseltzer
 * Date: 12/11/12
 * Time: 10:31 AM
 * To change this template use File | Settings | File Templates.
 */
function trim(stringToTrim) {
    return stringToTrim.replace(/^\s+|\s+$/g, "");
}

function rdcGetExistingHomeUrl(formElem) {
    var searchURL = 'http://www.realtor.com/realestateandhomes-search';
    var loc = trim(document.getElementById('rdcCity').value);
    var cityRegex = /^.*,\s*(AK|AL|AR|AZ|CA|CO|CT|DC|DE|FL|GA|HI|IA|ID|IL|IN|KS|KY|LA|MA|MD|ME|MI|MN|MO|MS|MT|NC|ND|NE|NH|NJ|NM|NV|NY|OH|OK|OR|PA|RI|SC|SD|TN|TX|UT|VA|VT|WA|WI|WV|WY)$/i;
    if (cityRegex.test(loc)) {
        var parts = loc.split(",");
        var cityPart = trim(parts[0]);
        var statePart = trim(parts[1]);
        cityPart = cityPart.replace(/ /g, "-");
        searchURL += "/" + cityPart + "_" + statePart;
    } else {
        var locParam = loc.replace(/ /g, "-");
        searchURL += "/" + locParam;
    }

    var rdcBedsId = document.getElementById('rdcBeds');
    if (rdcBedsId != undefined) {
        var rdcBeds = trim(rdcBedsId.value).replace(/[^0-9]/g, "");
        if (rdcBeds != "" && rdcBeds >= 1 && rdcBeds <= 5) {
            searchURL += "/beds-" + rdcBeds;
        }
    }

    var rdcBathsId = document.getElementById('rdcBaths');
    if (rdcBathsId != undefined) {
        var rdcBaths = trim(rdcBathsId.value).replace(/[^0-9]/g, "");
        if (rdcBaths != "" && rdcBaths >= 1 && rdcBaths <= 5) {
            searchURL += "/baths-" + rdcBaths;
        }
    }

    var rdcPriceMinId = document.getElementById('rdcPriceMin');
    var rdcPriceMaxId = document.getElementById('rdcPriceMax');
    if (rdcPriceMinId != undefined && rdcPriceMaxId != undefined) {
        var rdcPriceMin = trim(rdcPriceMinId.value).replace(/[^0-9]/g, "");
        var rdcPriceMax = trim(rdcPriceMaxId.value).replace(/[^0-9]/g, "");
        if (rdcPriceMin != "" || rdcPriceMax != "") {
            if (rdcPriceMin == "") {
                rdcPriceMin = "na";
            }
            if (rdcPriceMax == "") {
                rdcPriceMax = "na";
            }

            searchURL += "/price-" + rdcPriceMin + "-" + rdcPriceMax;
        }
    }

    return searchURL;
}

function rdcGetNewHomeUrl(formElem) {
    var searchURL = 'http://newhomes.move.com/GS/locationhandler/searchtype-qscr';
    var loc = trim(document.getElementById('rdcCity').value);
    var cityRegex = /^.*,\s*?(AK|AL|AR|AZ|CA|CO|CT|DC|DE|FL|GA|HI|IA|ID|IL|IN|KS|KY|LA|MA|MD|ME|MI|MN|MO|MS|MT|NC|ND|NE|NH|NJ|NM|NV|NY|OH|OK|OR|PA|RI|SC|SD|TN|TX|UT|VA|VT|WA|WI|WV|WY)$/i;
    var searchText = "";
    if (cityRegex.test(loc)) {
        var parts = loc.split(",");
        var cityPart = trim(parts[0]);
        var statePart = trim(parts[1]);
        searchURL += "/state-" + statePart;
        searchText = cityPart;
    } else {
        searchText = loc;
    }

    var rdcBedsId = document.getElementById('rdcBeds');
    if (rdcBedsId != undefined) {
        var rdcBeds = trim(rdcBedsId.value).replace(/[^0-9]/g, "");
        if (rdcBeds != "" && rdcBeds >= 1 && rdcBeds <= 5) {
            searchURL += "/" + "bedrooms-" + rdcBeds;
        }
    }

    var rdcBathsId = document.getElementById('rdcBaths');
    if (rdcBathsId != undefined) {
        var rdcBaths = trim(rdcBathsId.value).replace(/[^0-9]/g, "");
        if (rdcBaths != "" && rdcBaths >= 1 && rdcBaths <= 5) {
            searchURL += "/" + "bathrooms-" + rdcBaths;
        }
    }

    var rdcPriceMinId = document.getElementById('rdcPriceMin');
    if (rdcPriceMinId != undefined) {
        var rdcPriceMin = trim(rdcPriceMinId.value).replace(/[^0-9]/g, "");
        if (rdcPriceMin != "") {
            searchURL += "/pricelow-" + rdcPriceMin;
        }
    }

    var rdcPriceMaxId = document.getElementById('rdcPriceMax');
    if (rdcPriceMaxId != undefined) {
        var rdcPriceMax = trim(rdcPriceMaxId.value).replace(/[^0-9]/g, "");
        if (rdcPriceMax != "") {
            searchURL += "/pricehigh-" + rdcPriceMax;
        }
    }

    if (searchText != "") {
        formElem.searchtext.value = searchText;
    }

    return searchURL;
}

function rdcSearchSubmit() {
    var searchURL;
    var submitForm = document.getElementById('rdcSubmitForm');
    if (document.getElementById('rdcTypeNew').checked) {
        submitForm.gate.disabled = true;
        submitForm.cid.disabled = true;
        submitForm.searchtext.disabled = false;
        searchURL = rdcGetNewHomeUrl(submitForm);
    } else {
        submitForm.gate.disabled = false;
        submitForm.cid.disabled = false;
        submitForm.searchtext.disabled = true;
        searchURL = rdcGetExistingHomeUrl(submitForm);
    }

    submitForm.action = searchURL;

    if (s.tl) {
        s.tl(this, 'o', 'RealtorModule_${requestScope.omniturePageName}_${requestScope.size}_searchGo');
    }
    submitForm.submit();
    return false;
}
