function previewValid() {
    var customizeForm = document.forms['customizeForm'];
    if (customizeForm.width.value < 300) {
        customizeForm.width.value = 300;
        alert("Minimum width is 300");
        return false;
    }
    if (customizeForm.height.value < 434) {                                                  
        customizeForm.height.value = 434;
        alert("Minimum height is 434");
        return false;
    }
    document.getElementById('widgetCodeCheck').value = "no";
    return true;
}

function inputsValid() {
    if (!previewValid()) {
        return false;
    }
    var customizeForm = document.forms['customizeForm'];
    if (customizeForm.email.value == '') {
        customizeForm.email.select();
        alert("You must provide an email address");
        return false;
    }
    if (!customizeForm.terms.checked) {
        customizeForm.terms.select();
        alert("You must agree to the GreatSchools Terms of Use");
        return false;
    }
    document.getElementById('widgetCodeCheck').value = "yes";
    return true;
}

function selectDim(width, height) {

    document.forms['customizeForm'].width.value=width;
    document.forms['customizeForm'].height.value=height;
    setIFrameSrc();
}

function customDim() {
    document.forms['customizeForm'].dimensions3.checked = true;
}

function updateColor(selectElement, inputElementId, hexValue) {
    selectElement.style.backgroundColor = "#" + hexValue;
    document.getElementById(inputElementId).value = hexValue;
    setIFrameSrc();
}

function updateColorSelect(selectElementId, inputElement){
    setIFrameSrc();
    document.getElementById(selectElementId).style.backgroundColor = "#"+inputElement.value;
    document.getElementById(selectElementId).value = inputElement.value;
}

// also in schoolSearchWidget.js
// http://stackoverflow.com/questions/237104/javascript-array-containsobj
Array.prototype.contains = function(obj) {
  var i = this.length;
  while (i--) {
    if (this[i] === obj) {
      return true;
    }
  }
  return false;
}

// also in schoolSearchWidget.js
// requires http://maps.google.com/maps/api/js?sensor=false
function gsGeocode(searchInput, callbackFunction) {
    var geocoder = new google.maps.Geocoder();
    if (geocoder && searchInput) {
        geocoder.geocode( { 'address': searchInput}, function(results, status) {
        if (status == google.maps.GeocoderStatus.OK) {
            var geocodeResult = new Array();
            geocodeResult['lat'] = results[0].geometry.location.lat();
            geocodeResult['lon'] = results[0].geometry.location.lng();
            geocodeResult['normalizedAddress'] = results[0].formatted_address.replace(", USA","");
            for (var i = 0; i < results[0].address_components.length; i++) {
                if (results[0].address_components[i].types.contains('administrative_area_level_1')) {
                    geocodeResult['state'] = results[0].address_components[i].short_name;
                }
                if (results[0].address_components[i].types.contains('locality')) {
                    geocodeResult['city'] = results[0].address_components[i].short_name;
                } else if (!('city' in geocodeResult) && results[0].address_components[i].types.contains('administrative_area_level_3')) {
                    geocodeResult['city'] = results[0].address_components[i].short_name;
                }
                if (results[0].address_components[i].types.contains('country')) {
                    geocodeResult['country'] = results[0].address_components[i].short_name;
                }
            }
            // http://stackoverflow.com/questions/1098040/checking-if-an-associative-array-key-exists-in-javascript
            if (!('lat' in geocodeResult && 'lon' in geocodeResult &&
                  'city' in geocodeResult && 'state' in geocodeResult &&
                  'normalizedAddress' in geocodeResult &&
                  'country' in geocodeResult) ||
                geocodeResult['country'] != 'US') {
                geocodeResult = null;
            }
        } else {
            geocodeResult = null;
        }
        callbackFunction(geocodeResult);
      });
    }
}

function setIFrameSrc() {
    var form = $('customizeForm');
    var searchZipCode = form['searchQuery'];
    var textColor = form['textColor'];
    var bordersColor = form['bordersColor'];

    var width = form['width'];
    var height = form['height'];
    var zoom = form['zoom'];
    var url = '/schoolfinder/widget/customize.page';
    var errors = false;

    if($F(width) < 300) {
        $('width').value = 300;
        alert("Minimum width is 300");
        errors = true;
    }
    else if($F(height) <434) {
        $('height').value = 434;
        alert("Minimum height is 434");
        errors = true;
    }
    else if(trimString($F(searchZipCode)) == '') {
        alert("Please enter an address, zip code or city and state");
        errors = true;
    }


    if(((trimString($F(textColor))).length != 3 && (trimString($F(textColor))).length != 6) || (trimString($F(textColor))).match('[^a-fA-F0-9]') != null) {
      $(textColor).value = 'FFFFFF';
    }
    if((trimString(($F(bordersColor))).length != 3 && (trimString($F(bordersColor))).length != 6) || (trimString($F(bordersColor))).match('[^a-fA-F0-9]') != null) {
      $(bordersColor).value = 'FFFFFF';
    }


    if(!errors) {
        gsGeocode(trimString($F(searchZipCode)), function(geocodeResult) {
            if (geocodeResult != null) {
                document.getElementById('lat').value = geocodeResult['lat'];
                document.getElementById('lon').value = geocodeResult['lon'];
                document.getElementById('cityName').value = geocodeResult['city'];
                document.getElementById('state').value = geocodeResult['state'];
                document.getElementById('normalizedAddress').value = geocodeResult['normalizedAddress'];

                var params = {'checkAjaxCall':'foo','lat':geocodeResult['lat'],'lon':geocodeResult['lon'],'cityName':geocodeResult['cityName'],'state':geocodeResult['state'],'normalizedAddress':geocodeResult['normalizedAddress'],'searchQuery':trimString($F(searchZipCode)),'textColor':trimString($F(textColor)),'bordersColor':trimString($F(bordersColor)),'width':trimString($F(width)),'height':trimString($F(height)),'zoom':trimString($F(zoom))};
                new Ajax.Request(
                        url,
                {
                    method: 'post',
                    parameters: params,
                    onSuccess: showResponse
                });
            } else {
                alert('Please enter an address, zip code or city and state');
            }

        });
    }
    else{
        return;
    }

    function showResponse(x) {
      $('widgetIFrame').src = x.responseText.replace(/amp;/g,'');
    }

    setWidthHeight($F(width),$F(height));
    $('GS_widget_innerBorder_id').style.border = "solid 1px #"+ $F(bordersColor);
}
function initWidth(){
    var form = $('customizeForm');
    var width = form['width'];
    var height = form['height'];
    setWidthHeight($F(width),$F(height));
}

function setWidthHeight(width,height){
    var iFrameWidth = parseInt(width)-10;
    var iFrameHeight =  parseInt(height) - 66;
    $('widgetIFrame').width = iFrameWidth;
    $('widgetIFrame').height = iFrameHeight;
    // the prototype versions of the next statements weren't working
    jQuery('#GS_schoolSearchWidget').css('width',iFrameWidth+2);
    jQuery('#GS_preview_div').css('width',parseInt(width) -40);
}

function setBackgroundColorDropdown(selectElementId, inputElement){
    setFormBackgroundColor();     
    document.getElementById(selectElementId).style.backgroundColor = "#"+inputElement.value;
    document.getElementById(selectElementId).value = inputElement.value;
}

function updateBackgroundColor(selectElement, inputElementId, hexValue){
    selectElement.style.backgroundColor = "#" + hexValue;
    document.getElementById(inputElementId).value = hexValue;
    setFormBackgroundColor();
}

function setFormBackgroundColor(){
    var form = $('customizeForm');
     var backgroundColor = form['backgroundColor'];
     if((trimString(($F(backgroundColor))).length != 3 && (trimString($F(backgroundColor))).length != 6) || (trimString($F(backgroundColor))).match('[^a-fA-F0-9]') != null) {
      $(backgroundColor).value = 'FFFFFF';
    }
    $('GS_schoolSearchWidget').style.border = "solid 4px #"+ $F(backgroundColor);
    $('GS_schoolSearchWidget').style.borderBottomWidth = "20px";
}

function trimString(sInString) {
  sInString = sInString.replace( /^\s+/g, "" );// strip leading
  return sInString.replace( /\s+$/g, "" );// strip trailing
}

function disableEnterKey(e)
{
    var key;
     if(window.event)
          key = window.event.keyCode; //IE
     else
          key = e.which; //firefox
     return (key != 13);
}