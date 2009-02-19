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
    return true;
}


function setIFrameSrc(){

    /*var separator = "?";
    var customizeForm = document.forms['customizeForm'];
    var searchZipCode = customizeForm.searchQuery.value;
    var textColor = customizeForm.textColor.value;
    var borderColor = customizeForm.bordersColor.value;
    var backgroundColor = customizeForm.backgroundColor.value;
    var iFrameWidth = customizeForm.width.value -10;
    var iFrameHeight = customizeForm.height.value -66;
    var zoom = customizeForm.zoom.value;
    //var path = "http://localhost:8080/schoolfinder/widget/customize.page";
    var path = document.getElementById('widgetIFrame').src;
    var url = path.substring(0,path.indexOf('?'));
    var iFrameSrc = "http://localhost:8080/widget/schoolSearch.page";
    var rval=iFrameSrc;
    if (searchZipCode != '') {
                rval += separator  + "searchQuery=" + searchZipCode;
                separator = "&";
            }
       rval += separator  + "textColor=" +textColor;
       rval += separator  + "bordersColor=" + borderColor;
       rval += separator  + "width=" + iFrameWidth;
       rval += separator  + "height=" + iFrameHeight;
       rval += separator  + "zoom=" + zoom;
    document.getElementById('widgetIFrame').src = rval;
    document.getElementById('widgetIFrame').width = iFrameWidth;
    document.getElementById('widgetIFrame').height = iFrameHeight;
    document.getElementById('GS_schoolSearchWidget').style.width = iFrameWidth+2;
    document.getElementById('GS_schoolSearchWidget').style.border = "solid 4px #"+ backgroundColor;*/

    var customizeForm = document.forms['customizeForm'];
    var backgroundColor = customizeForm.backgroundColor.value;
    var iFrameWidth = customizeForm.width.value -10;
    var iFrameHeight = customizeForm.height.value -66;
    
    document.getElementById('GS_schoolSearchWidget').style.border = "solid 4px #"+ backgroundColor;
    document.getElementById('widgetIFrame').width = iFrameWidth;
    document.getElementById('widgetIFrame').height = iFrameHeight;
    document.getElementById('GS_schoolSearchWidget').style.width = 740+2;
    getIFrameSrc();
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

function getIFrameSrc() {

    var form = $('customizeForm');
    var searchZipCode = form['searchQuery'];
    var textColor = form['textColor'];
    var borderColor = form['bordersColor'];
    var width = form['width'];
    var height = form['height'];
    var zoom = form['zoom'];
    var url = '/schoolfinder/widget/customize.page';
    var params = {'tester':'foo','searchQuery':$F(searchZipCode),'textColor':$F(textColor),'bordersColor':$F(borderColor),'width':$F(width),'height':$F(height),'zoom':$F(zoom)};
    
    //var iFrameWidth = parseInt($F(width))-10;
    //var iFrameHeight =  parseInt($F(height)) - 66;
    //var backgroundColor = form['backgroundColor'];
    //$('widgetIFrame').width = iFrameWidth;
    //$('widgetIFrame').height = iFrameHeight;
    //$('GS_schoolSearchWidget').style.width = iFrameWidth+2;
   // $('GS_schoolSearchWidget').style.border = "solid 4px #"+ $F(backgroundColor);
    
    new Ajax.Request(
            url,
    {
        method: 'post',
        parameters: params,
        onSuccess: showResponse
    });
    
    function showResponse(x) {
        $('widgetIFrame').src = x.responseText.replace(/amp;/g,'');
     }


}


