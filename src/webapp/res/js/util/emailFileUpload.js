var GS = GS || {};
GS.util = GS.util || {};

/*
    JS FileReader - http://www.html5rocks.com/en/tutorials/file/dndfiles/
    JS FormData and AJAX - http://net.tutsplus.com/tutorials/javascript-ajax/uploading-files-with-ajax/
 */

GS.util.EmailFileUpload = function() {
    var fileUpload = $('#js-file-upload');
    var fileUploadInput = $('#js-uploader-file');
    var uploadButton = $('#js-upload');
    var browseFile = $('#js-browse');
    var errorBlock = $('#error');
    var status = $('#status');

    var isClickDisabled = false;
    var formData = false;
    var error = false;

    if (window.FormData) {
        formData = new FormData();
    }

    /*
    Trigger input file click on button click
     */
    browseFile.click(function() {
        if(isClickDisabled == true) {
            return;
        }
        fileUpload.click();
    });

    fileUpload.change(function(e) {
        status.css('display', 'none');
        fileUploadInput.val(fileUpload.val());
        if (window.FileReader) {
            var reader = new FileReader();
            var files = e.target.files;
            for(var i = 0; i < files.length; i++) {
                var file = files[i];
                var extension = this.value.match(/.+\.(.+)$/)[1];
                if(extension !== 'csv') {
                    errorBlock.css("display", "block").find('p').text('Please select csv file only.');
                    error = true;
                    break;
                }
                if (error == true) {
                    errorBlock.css('display', 'none');
                    error = false;
                }
                reader.onloadend = function () {};
                reader.readAsDataURL(file);
                if(formData) {
                    formData.append('file', file);
                }
            }
        }
    });

    uploadButton.click(function() {
        if(isClickDisabled == true || error == true) {
            return;
        }
        if(fileUploadInput.val() == '') {
            errorBlock.css("display", "block").find('p').text('Please select a file to upload.');
            error = true;
            return;
        }
        if(formData) {
            setButtonClass(true, 'button-1', 'button-1-inactive');
            fileUploadInput.prop('disabled', true);
            status.css('display', 'block').find('p').text('Uploading... Please wait.');
            $.ajax({
                url: document.location,
                type: 'POST',
                data: formData,
                processData: false,
                contentType: false
            }).done(function(data) {
                    setButtonClass(false, 'button-1-inactive', 'button-1');
                    fileUploadInput.prop('disabled', false);
                    if(data.success !== undefined) {
                        status.find('p').text(data.success);
                    }
                    else if(data.error !== undefined) {
                        status.css('display', 'none');
                        errorBlock.css("display", "block").find('p').text(data.error);
                        error = true;
                    }
                });
//                }).fail(function() {
//                    errorBlock.css("display", "block").find('p').text('Error parsing file.');
//                    error = true;
//                    setButtonClass(false, 'button-1-inactive', 'button-1');
//                });
        }
    });
    
    var setButtonClass = function(isDisabled, remove, add) {
        uploadButton.removeClass(remove).addClass(add);
        browseFile.removeClass(remove).addClass(add);
        isClickDisabled = isDisabled;
    }
}