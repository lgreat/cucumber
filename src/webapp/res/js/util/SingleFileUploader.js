GS = GS || {};

Function.prototype.gs_bind = function(obj) {
    var method = this;
    return function() {
        return method.apply(obj, arguments);
    };
};

GS.PhotoUploader = function(httpPostUrl, idSuffix, schoolId, schoolDatabaseState) {
    this.idSuffix = idSuffix; //a unique ID suffix for this specific uploader instance
    this.httpPostUrl = httpPostUrl;
    this.schoolId = schoolId;
    this.schoolDatabaseState = schoolDatabaseState;
    this.uploader = null;
    this.container = jQuery('#file-uploader-container-' + this.idSuffix);
    this.uploadButton = this.container.find('.js-uploader-upload-button');
    this.browseButton = this.container.find('.js-uploader-browse-button');
    this.fakeBrowseButton = this.container.find('.js-uploader-fake-browse-button');
    this.fileBox = this.container.find('.js-uploader.file');
    this.statusBox = this.container.find('.js-uploader-status');
    this.spinner = this.container.find('.spinner');

    this.errorMessage = null; // an error message for entire uploader to be displayed after uploader done
    this.FLASH_ENABLED_STYLE = 'position: absolute; top: 191px; background: none repeat scroll 0% 0% transparent; z-index: 9999999; width: 101px; height: 23px; left: 11px;';
    this.LOGGING_ENABLED = false;

    this.createUploader();
};

GS.PhotoUploader.prototype.createUploader = function() {
    this.uploader = new plupload.Uploader({
        runtimes : 'flash,html5,silverlight',
        browse_button : 'jsPhotoBrowseButton',
        container: 'file-upload-container',
        max_file_size : '20mb',
        url: this.httpPostUrl,
        flash_swf_url : '/res/js/plupload/plupload.flash.swf',
        multipart_params : {
            'schoolId' : this.schoolId,
            'schoolDatabaseState' :  this.schoolDatabaseState
        },
        filters : [
            {title : "PDF files", extensions : "pdf"}
        ],
        urlstream_upload: true
    });

    this.uploader.bind('Init', function(uploader) {

    }.gs_bind(this));

    // initialize the plupload uploader instance
    this.uploader.init();

    this.init = function() {
        var self = this;

        this.uploader.bind('FilesAdded', this.filesQueued);

        this.uploader.bind("UploadProgress",this.updateProgress);

        this.uploader.bind("Error", this.handleError);

        this.uploader.bind("UploadComplete", function() {
            self.done();
        });

        this.uploader.bind("UploadFile", function(up, file) {

        }.gs_bind(this));

        this.uploader.bind("FileUploaded", function(up, file, response) {
            var data = jQuery.parseJSON(response.response);
            if (data && data.error && data.error.message) {
                self.setStatus(file, "Error");
                if (data.error.message == "Unauthorized") {
                    self.errorMessage = "Error: Not logged in";
                } else {
                    self.errorMessage = "One or more errors occurred while uploading. Your file may not have been uploaded.";
                }
            } else {
                if (file.percent === 100 && file.status === plupload.DONE) {
                    var status = "Upload complete";
                }
            }
        });

        this.uploadButton.click(function() {
            self.startUpload.apply(self, arguments);
        });
    }.gs_bind(this);

    this.disableBrowseButton = function() {
        this.browseButton.hide();
        this.fakeBrowseButton.show();
        $('.plupload.flash').css('left','-10000px');
    }.gs_bind(this);

    this.enableBrowseButton = function() {
        this.fakeBrowseButton.hide();
        this.browseButton.show();
        $('.plupload.flash').attr('style',this.FLASH_ENABLED_STYLE);
    }.gs_bind(this);

    this.enableUploading = function() {
        this.uploadButton.prop('disabled',false);
        this.uploadButton.addClass('button-1');
        this.uploadButton.removeClass('button-1-inactive');
    }.gs_bind(this);

    this.disableUploading = function() {
        this.uploadButton.prop('disabled',true);
        this.uploadButton.addClass('button-1-inactive');
        this.uploadButton.removeClass('button-1');
    }.gs_bind(this);

    this.styleUploading = function() {
        this.uploadButton.prop('disabled',true);
        this.uploadButton.removeClass('button-1');
        this.uploadButton.addClass('button-1-inactive');
        this.disableBrowseButton();
        this.spinner.show();
    }.gs_bind(this);


    this.startUpload = function() {
        this.styleUploading();
        this.setStatus(this.uploader.files[0], this.PREPARING);
        this.uploader.start();
        this.container.find('.deleteFileUpload').off('click');
    }.gs_bind(this);

    this.updateProgress = function(up, file) {
        if (file.status === plupload.UPLOADING) {
            var status = "Uploading... " + file.percent + "%";
            this.setStatus(file, status);
        }
    }.gs_bind(this);

    this.done = function() {
        this.styleUploadComplete();
        this.spinner.hide();
    }.gs_bind(this);

    this.styleUploadComplete = function() {

    }.gs_bind(this);

    this.handleError = function(up, err) {
        var file = err.file, message;

        this.spinner.hide();

        /*if (err.code === plupload.INIT_ERROR) {
            this.styleInitializationError();
            return;
        }*/

        if (file) {
            var fileExtension = file.name.substring(file.name.length-4, file.name.length);
            message = err.message;

            if (err.details) {
                message += " (" + err.details + ")";
            }
            if (err.code == plupload.FILE_SIZE_ERROR) {
                alert("There was an error. " + fileExtension.toUpperCase() + " files have a limit of 20MB. File: " + file.name + " cannot be uploaded.");
            } else if (err.code == plupload.FILE_EXTENSION_ERROR) {
                alert("There was an error. " + fileExtension.toUpperCase() + " is not a PDF file.");
            } else {
                alert("We're sorry, an unknown error has occurred.");
            }

        } else {
            //this.displayUploaderError(message);
            alert("We're sorry, an unknown error has occurred");
            this.done();
        }
    }.gs_bind(this);

    this.filesQueued = function(up, files) {
        // make sure there's never more than one item in the plupload queue
        if(this.uploader.files.length > 1) {
            this.uploader.files = this.uploader.files.slice(this.uploader.files.length-1,this.uploader.files.length);
        }

        this.fileBox.html(files[0].name);
    }.gs_bind(this);


    this.setStatus = function(file, status) {
        this.statusBox.html(status);
    }.gs_bind(this);

    this.log = function() {
        if (typeof this.LOGGING_ENABLED !== 'undefined' && this.LOGGING_ENABLED === true) {
            console.log(arguments);
        }
    }.gs_bind(this);

};

