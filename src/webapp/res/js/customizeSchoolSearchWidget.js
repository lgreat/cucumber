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

function setIFrameSrc() {

    var form = $('customizeForm');
    var searchZipCode = form['searchQuery'];
    var textColor = form['textColor'];
    var bordersColor = form['bordersColor'];
    var backgroundColor = form['backgroundColor'];
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

    if(($F(backgroundColor).match('[^a-zA-Z0-9]')) != null) {
      $(backgroundColor).value = 'FFFFFF';
    }
    if(($F(textColor).match('[^a-zA-Z0-9]')) != null) {
      $(textColor).value = 'FFFFFF';
    }
    if(($F(bordersColor).match('[^a-zA-Z0-9]')) != null) {
      $(bordersColor).value = 'FFFFFF';
    }


    if(!errors) {
       var params = {'checkAjaxCall':'foo','searchQuery':$F(searchZipCode),'textColor':$F(textColor),'bordersColor':$F(bordersColor),'width':$F(width),'height':$F(height),'zoom':$F(zoom)};
        new Ajax.Request(
            url,
        {
        method: 'post',
        parameters: params,
        onSuccess: showResponse
        });
    }
    else{
        return;
    }

    function showResponse(x) {
        $('widgetIFrame').src = x.responseText.replace(/amp;/g,'');
    }

    var iFrameWidth = parseInt($F(width))-10;
    var iFrameHeight =  parseInt($F(height)) - 66;
    $('widgetIFrame').width = iFrameWidth;
    $('widgetIFrame').height = iFrameHeight;
    $('GS_schoolSearchWidget').style.width = iFrameWidth+2;
    $('GS_preview_div').style.width = parseInt($F(width)) -40;
    $('GS_schoolSearchWidget').style.border = "solid 4px #"+ $F(backgroundColor);
    $('GS_widget_innerBorder_id').style.border = "solid 1px #"+ $F(bordersColor);
}
function initWidth(){
    var form = $('customizeForm');
    var width = form['width'];
    var height = form['height'];
    var iFrameWidth = parseInt($F(width))-10;
    var iFrameHeight =  parseInt($F(height)) - 66;
    $('widgetIFrame').width = iFrameWidth;
    $('widgetIFrame').height = iFrameHeight;
    $('GS_schoolSearchWidget').style.width = iFrameWidth+2;
    $('GS_preview_div').style.width = parseInt($F(width)) -40;
}


