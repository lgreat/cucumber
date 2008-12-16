function previewValid() {
    var customizeForm = document.forms['customizeForm'];
    if (customizeForm.width.value < 300) {
        customizeForm.width.value = 300;
        alert("Minimum width is 300");
        return false;
    }
    if (customizeForm.height.value < 412) {
        customizeForm.height.value = 412;
        alert("Minimum height is 412");
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
}

function customDim() {
    document.forms['customizeForm'].dimensions3.checked = true;
}

function updateColor(selectElement, inputElementId, hexValue) {
    selectElement.style.backgroundColor = "#" + hexValue;
    document.getElementById(inputElementId).value = hexValue;
}
