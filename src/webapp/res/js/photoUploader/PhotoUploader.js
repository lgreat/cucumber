GS = GS || {};

GS.PhotoUploader = function(url, maxQueuedItems, schoolId, schoolDatabaseState) {
    this.url = url;
    this.schoolId = schoolId;
    this.schoolDatabaseState = schoolDatabaseState;
    this.totalItemsInList = 0;
    this.uploader = null;
    this.uploadButton = jQuery('#jsPhotoUploadButton');
    this.queueButton = jQuery('#jsPhotoQueueButton');
    this.container = jQuery('#container');
    this.spinner = jQuery('.js-photoUploadSpinner');
    this.errorMessage = null; // an error message for entire uploader to be displayed after uploader done

    this.maxQueuedItems = maxQueuedItems;

    this.blankRowClass = '';
    this.filledRowClass = '';

    this.createUploader();
};

GS.PhotoUploader.prototype.createUploader = function() {
    this.uploader = new plupload.Uploader({
        runtimes : 'gears,html5,flash,silverlight,browserplus',
        browse_button : 'jsPhotoQueueButton',
        container: 'container',
        max_file_size : '20mb',
        max_actual_queue : this.maxQueuedItems,
        url : this.url,
        resize : {width : 500, height : 500, quality : 80},
        flash_swf_url : '/res/js/plupload/plupload.flash.swf',
        silverlight_xap_url : '/res/js/plupload/plupload.silverlight.xap',
        multipart_params : {
            'schoolId' : this.schoolId,
            'schoolDatabaseState' :  this.schoolDatabaseState
        },
        filters : [
            {title : "Image files", extensions : "jpg,gif,png"},
            {title : "Zip files", extensions : "zip"}
        ]
    });

    this.uploader.init();
};

GS.PhotoUploader.prototype.init = function() {
    self = this;
    this.uploader.bind('FilesAdded', function() {
        self.filesQueued.apply(self, arguments);
    });

    this.uploader.bind("UploadProgress",function() {
        self.updateProgress.apply(self, arguments);
    });

    this.uploader.bind("Error", function() {
        self.handleError.apply(self, arguments);
    });

    this.uploader.bind("UploadComplete", function() {
        self.done();
    });

    this.uploader.bind("FileUploaded", function(up, file, response) {
        var stopTheUploader = false;
        var data = jQuery.parseJSON(response.response);
        if (data && data.error && data.error.message) {
            self.setStatus(file, "Error");
            if (data.error.message == "Unauthorized") {
                self.errorMessage = "Error: Not logged in";
                stopTheUploader = true;
            }
        }

        if (stopTheUploader) {
            self.uploader.stop();
            self.done();
        } else {
            self.setStatusByPosition(file._gsPosition+1, "Preparing...");
        }
    });

    this.uploadButton.click(function() {
        self.startUpload.apply(self, arguments);
    });
};

GS.PhotoUploader.prototype.styleUploading = function() {
    this.container.fadeTo('slow', 0.7);
    this.uploadButton.removeClass('button-1');
    this.uploadButton.addClass('button-1-inactive');
    this.queueButton.removeClass('button-1');
    this.queueButton.addClass('button-1-inactive');
    this.container.css('background-color', 'aaa');
    this.spinner.show();
};

GS.PhotoUploader.prototype.styleQueueFull = function() {
    this.queueButton.removeClass('button-1');
    this.queueButton.addClass('button-1-inactive');
    this.queueButton.prop('disabled',true);
};

GS.PhotoUploader.prototype.styleDone = function() {
    //this.container.fadeTo('slow', 1);
    this.spinner.hide();
};

GS.PhotoUploader.prototype.startUpload = function() {
    this.styleUploading();
    this.setStatusByPosition(0, "Preparing...");
    this.uploader.start();
};

GS.PhotoUploader.prototype.updateProgress = function(up, file) {
    var status = "Uploading... " + file.percent + "%";
    if (file.percent === 100) {
        status = "Done";
    }
    this.setStatus(file, status);
};

GS.PhotoUploader.prototype.done = function() {
    if (this.errorMessage !== null) {
        this.displayUploaderError(this.errorMessage);
    } else {
        jQuery('#jsUploadComplete').show();
    }
    jQuery('.deleteFileUpload').off('click');
    this.styleDone();
};

GS.PhotoUploader.prototype.handleError = function(up, err) {
    var file = err.file, message;

    this.spinner.hide();

    if (file) {
        message = err.message;

        if (err.details) {
            message += " (" + err.details + ")";
        }
        if (err.code == plupload.FILE_SIZE_ERROR) {
            alert("Error: File too large: " + file.name);
        } else if (err.code == plupload.FILE_EXTENSION_ERROR) {
            alert("Error: Invalid file extension: " + file.name);
        } else {
            this.setStatus(file, "Error");
        }

        //file.hint = message;
        //$('#' + file.id).attr('class', 'plupload_failed').find('a').css('display', 'block').attr('title', message);
    } else {
        this.displayUploaderError(message);
        this.done();
    }
};

GS.PhotoUploader.prototype.filesQueued = function(up, files) {
    var index = 0;
    var htmlblock = '';
    var tbody = jQuery ('#container table tbody');

    if (this.totalItemsInList === this.maxQueuedItems) {
        return;
    }

    self = this;

    $.each(files, function(i, file) {
        // add an item to the list

        self.addItemAtPosition(file, self.totalItemsInList);

        // when the item's delete icon is clicked, tell the uploader to remove it from the queue, and delete it
        // from the list
        jQuery('#' + file.id + ' .deleteFileUpload').click(function() {
            console.log('deleting item with file id' + file.id);
            self.uploader.removeFile(file);
            self.removeItem(file.id);
        });

        // add our own property to plupload file object
        files[i]._gsPosition = self.totalItemsInList;

        // keep track of how many non-blank items are in the list
        self.totalItemsInList++;

        // stop when max is reached
        if (self.totalItemsInList == self.maxQueuedItems) {
            return false; // exit the $.each
        }

    });

    if (self.totalItemsInList == this.maxQueuedItems) {
        self.styleQueueFull();
    }
};

GS.PhotoUploader.prototype.displayUploaderError = function(message) {
    var errorElement = jQuery('#jsUploadError');
    errorElement.html(message);
    errorElement.show();
}

GS.PhotoUploader.prototype.addItemAtPosition = function(file, position) {
    htmlblock = '<tr id="' + file.id + '" class="fileNumber'+ position +'"><td>' + file.name + '</td><td class="uploadStatus">Queued</td><td>' + file.size + '</td><td><span class="deleteFileUpload">X</span></td></tr>';

    this.container.find('table tbody tr:eq('+ parseInt(position) +')').after(htmlblock);
    this.container.find('table tbody tr:eq('+ parseInt(position) + ')').remove();
};

GS.PhotoUploader.prototype.removeItem = function(id) {
    var htmlBlock = "<tr><td>&nbsp;</td><td>&nbsp;</td><td>&nbsp;</td><td>&nbsp;</td></tr>";
    this.totalItemsInList--;
    console.log("total items in list = " + this.totalItemsInList);

    if (this.totalItemsInList < this.maxQueuedItems) {
        this.queueButton.addClass('button-1');
        this.queueButton.removeClass('button-1-inactive');
        this.queueButton.prop('disabled',false);
    }

    this.container.find('#' + id).remove();
    this.container.find('table tbody tr:last').after(htmlBlock);
};

GS.PhotoUploader.prototype.setStatus = function(file, status) {
    jQuery('#' + file.id + ' .uploadStatus').html(status);
};

GS.PhotoUploader.prototype.setStatusByPosition = function(position, status) {
    this.container.find('.fileNumber' + position + ' .uploadStatus').html(status);
};

// mark a file upload row as done
GS.PhotoUploader.prototype.itemComplete = function(position) {
    this.setStatusByPosition(position, "Done");
};