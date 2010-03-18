function trim(stringToTrim) {
    return stringToTrim.replace(/^\s+|\s+$/g,"");
}

function rdcSearchSubmit() {
    var searchURL = 'http://www.realtor.com/realestateandhomes-search';
    var loc = trim (document.getElementById('rdcCity').value);
    var cityRegex = /^.*, (AK|AL|AR|AZ|CA|CO|CT|DC|DE|FL|GA|HI|IA|ID|IL|IN|KS|KY|LA|MA|MD|ME|MI|MN|MO|MS|MT|NC|ND|NE|NH|NJ|NM|NV|NY|OH|OK|OR|PA|RI|SC|SD|TN|TX|UT|VA|VT|WA|WI|WV|WY)$/i;
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

    var rdcBeds = trim (document.getElementById('rdcBeds').value).replace(/[^0-9]/g, "");
    if (rdcBeds != "" && rdcBeds >=1 && rdcBeds <= 5) {
        searchURL += "/" + "beds-" + rdcBeds;
    }

    var rdcBaths = trim (document.getElementById('rdcBaths').value).replace(/[^0-9]/g, "");
    if (rdcBaths != "" && rdcBaths >= 1 && rdcBaths <= 5) {
        searchURL += "/" + "baths-" + rdcBaths;
    }

    var rdcPriceMin = trim (document.getElementById('rdcPriceMin').value).replace(/[^0-9]/g, "");
    var rdcPriceMax = trim (document.getElementById('rdcPriceMax').value).replace(/[^0-9]/g, "");
    if (rdcPriceMin != "" || rdcPriceMax != "") {
        if (rdcPriceMin == "") {
            rdcPriceMin = "na";
        }
        if (rdcPriceMax == "") {
            rdcPriceMax = "na";
        }

        searchURL += "/" + "price-" + rdcPriceMin + "-" + rdcPriceMax;
    }

    var submitForm = document.getElementById('rdcSubmitForm');
    submitForm.action = searchURL;
    submitForm.submit();
    return false;
}
