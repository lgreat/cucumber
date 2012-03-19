GS = GS || {};

Function.prototype.gs_bind = function(obj) {
    var method = this;
    return function() {
        return method.apply(obj, arguments);
    };
};

GS.SingleFileUploader = function(httpPostUrl, idSuffix, schoolId, schoolDatabaseState, extraPostParams) {
    this.idSuffix = idSuffix; //a unique ID suffix for this specific uploader instance
    this.httpPostUrl = httpPostUrl;
    this.schoolId = schoolId;
    this.schoolDatabaseState = schoolDatabaseState;
    this.uploader = null;
    this.container = jQuery('#file-uploader-container-' + this.idSuffix);
    this.uploadButton = this.container.find('.js-uploader-upload-button');
    this.browseButton = this.container.find('.js-uploader-browse-button');
    this.deleteButton = this.container.find('.js-uploader-delete');
    this.fakeBrowseButton = this.container.find('.js-uploader-fake-browse-button');
    this.fileBox = this.container.find('.js-uploader-file');
    this.statusBox = this.container.find('.js-uploader-status');
    this.errorBox = this.container.find('.js-uploader-error');
    this.spinner = this.container.find('.spinner');
    this.completeIcon = this.container.find('.js-uploader-complete');
    this.extraPostParams = extraPostParams;

    this.FLASH_ENABLED_STYLE = 'position: absolute; top: 191px; background: none repeat scroll 0% 0% transparent; z-index: 9999999; width: 101px; height: 23px; left: 11px;';
    this.LOGGING_ENABLED = false;

    this.createUploader();

    this.deleteButton.on('click', '.img', function() {
        this.clear();
    }.gs_bind(this));
};

GS.SingleFileUploader.prototype.createUploader = function() {
    this.uploader = new plupload.Uploader({
        runtimes : 'flash',
        browse_button : 'js-plupload-browse-' + this.idSuffix,
        container: 'file-uploader-container-' + this.idSuffix,
        max_file_size : '5mb',
        url: this.httpPostUrl,
        flash_swf_url : '/res/js/plupload/plupload.flash.swf',
        multipart_params : $.extend(this.extraPostParams, {
            'schoolId' : this.schoolId,
            'schoolDatabaseState' :  this.schoolDatabaseState
        }),
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

        this.uploader.bind("Error", this.handleError);

        this.uploader.bind("UploadComplete", function() {
            self.done();
        });

        this.uploader.bind("UploadFile", function(up, file) {

        }.gs_bind(this));

        this.uploader.bind("FileUploaded", function(up, file, response) {
            this.errorBox.hide();
            var data = jQuery.parseJSON(response.response);
            if (data && data.errorMessage) {
                if (data.errorMessage == "Unauthorized") {
                    self.showError(file, "Error: Not authorized to upload a PDF for this school.");
                } else {
                    self.showError(file, "One or more errors occurred while uploading. Your file may not have been uploaded.");
                }
            } else {
                if (file.percent === 100 && file.status === plupload.DONE) {
                    this.completeIcon.show();
                    var status = "Upload complete";
                }
            }
        }.gs_bind(this));

        this.uploadButton.click(function() {
            self.startUpload.apply(self, arguments);
        });
    }.gs_bind(this);

    this.disableBrowseButton = function() {
        this.browseButton.hide();
        this.fakeBrowseButton.show();
        this.container.find('.plupload.flash').css('left','-10000px');
    }.gs_bind(this);

    this.enableBrowseButton = function() {
        this.fakeBrowseButton.hide();
        this.browseButton.show();
        this.container.find('.plupload.flash').attr('style',this.FLASH_ENABLED_STYLE);
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
        this.uploader.start();
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
                alert("There was an error. PDF files have a limit of 20MB. File: " + file.name + " cannot be uploaded.");
            } else if (err.code == plupload.FILE_EXTENSION_ERROR) {
                alert("There was an error. " + file.name + " is not a PDF file.");
            } else {
                alert("We're sorry, an unknown error has occurred.");
            }

        } else {
            alert("We're sorry, an unknown error has occurred");
            this.done();
        }
    }.gs_bind(this);

    this.filesQueued = function(up, files) {
        this.errorBox.hide();
        // make sure there's never more than one item in the plupload queue
        if(this.uploader.files.length > 1) {
            this.uploader.files = this.uploader.files.slice(this.uploader.files.length-1,this.uploader.files.length);
        }

        this.deleteButton.show();

        this.fileBox.val(files[0].name);
        this.enableUploading();
    }.gs_bind(this);

    this.clear = function() {
        var filesToDelete = this.uploader.files;
        for (var i = 0; i < filesToDelete.length; i++) {
            this.uploader.removeFile(filesToDelete[i]);
        }
        this.fileBox.val('');
        this.deleteButton.hide();
    };

    this.setStatus = function(file, status) {
        this.statusBox.html(status);
    }.gs_bind(this);

    this.showError = function(file, status) {
        this.errorBox.find('.bk').html(status);
        this.errorBox.show();
    }.gs_bind(this);

    this.log = function() {
        if (typeof this.LOGGING_ENABLED !== 'undefined' && this.LOGGING_ENABLED === true) {
            console.log(arguments);
        }
    }.gs_bind(this);

};

