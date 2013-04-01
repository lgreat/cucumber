GS = GS || {};

Function.prototype.gs_bind = function(obj) {
    var method = this;
    return function() {
        return method.apply(obj, arguments);
    };
};

GS.MediaUploader = function() {
    this.url = null;
    this.schoolId = null;
    this.schoolDatabaseState = null;

    this.params = null;
    this.totalItemsInList = 0;
    this.uploader = null;
    this.filterType = null;

    this.uploadButton = null;
    this.addButton = null;
    this.fakeAddButton = null;
    this.buttonClass = null;

    this.container = null;
    this.spinner = null;
    this.uploadCompleteOverlay = null;
    this.uploadErrorOverlay = null;
    this.errorMessage = null; // an error message for entire uploader to be displayed after uploader done

    this.maxQueuedItems = 0;
    this.PREPARING = "Preparing..."; // resizing photo, pre-upload
    this.EMPTY_QUEUE_ROW_HTML = "<!-- do not collapse -->";
    this.FLASH_ENABLED_STYLE = null;

    this.multi_selection = null;
    this.browseButton = null;
    this.uploadContainer = null;
};

GS.MediaUploader.prototype.createUploader = function() {
    this.uploader = new plupload.Uploader({
        runtimes : 'html5,flash,html4',
        browse_button : this.browseButton,
        container: this.uploadContainer,
        max_file_size : '20mb',
        max_gif_file_size : '2mb',
        max_actual_queue : this.maxQueuedItems,
        url : this.url,
        multi_selection : this.multi_selection,
        resize : {width : 500, height : 500, quality : 80},
        flash_swf_url : '/res/js/plupload-new/Moxie.swf',
        urlstream_upload: true
    });

    if(this.filterType === GS.MediaUploader.MediaType.Image) {
        this.uploader.settings.filters = [{title : "Image files", extensions : "jpg,jpeg,gif,png"}];
    }

    if(this.filterType === GS.MediaUploader.MediaType.Pdf) {
        this.uploader.settings.filters = [{title : "Pdf files", extensions : "pdf"}];
    }

    if(this.schoolId !== null && this.schoolDatabaseState !== null) {
        this.uploader.settings.multipart_params = {'schoolId' : this.schoolId, 'schoolDatabaseState' : this.schoolDatabaseState};
    }

    if(this.params != null) {
        this.uploader.settings.multipart_params = this.params;
    }

    this.uploader.init();

    // hack to find out if plupload has been init with multi_selection abilities, since it doesnt seem to trigger
    // the Init or Error events when it fails to initialize properly
    setTimeout(function() {
        if (!this.uploader.settings.hasOwnProperty('multi_selection')) {
            this.styleInitializationError();
        }
    }.gs_bind(this), 5 * 1000);

    this.styleInitializationError = function() {
        this.container.fadeTo(0,0.5);
        this.disableAddButton();
        this.container.find(this.buttonClass).prop('disabled',true);
        if (!this.uploader.features.hasOwnProperty('multi_selection')) {
            this.uploadErrorOverlay.html('You must have <a href="http://get.adobe.com/flashplayer/" target="_blank">Adobe Flash Player</a> installed to upload photos.').show();
        }
        return;
    }.gs_bind(this);

    this.uploader.bind('Init', function(uploader) {
        if (!uploader.features.hasOwnProperty('multi_selection')) {
            this.styleInitializationError();
        }
    }.gs_bind(this));

    this.init = function(numberOfExistingPhotos) {
        var self = this;

        this.uploader.bind('FilesAdded', this.filesAdded);

        this.uploader.bind("UploadProgress",this.updateProgress);

        this.uploader.bind("Error", this.handleError);

        this.uploader.bind("StateChanged", this.stateChanged);

        this.uploader.bind("UploadComplete", function() {
            self.done();
        });

        // gets triggered when the file begins to be resized (if that option is on)
        this.uploader.bind("UploadFile", function(up, file) {
            this.setStatus(file, self.PREPARING);

            // for each file that begins resize/upload, start a timer, and set the status to failed and restart
            // plupload if the file hasn't finished by then
            setTimeout(function() {
                if (file.status === plupload.UPLOADING) {
                    file.status = plupload.FAILED;
                    this.setStatus(file, "Error: Photo upload failed. You may need to resize your photo and try again.");
                    this.uploader.stop();
                    this.uploader.start();
                }
            }.gs_bind(this), 30*1000);
        }.gs_bind(this));

        this.uploader.bind("FileUploaded", function(up, file, response) {
            var stopTheUploader = false;
            var data = jQuery.parseJSON(response.response);
            if (data && data.error && data.error.message) {
                self.setStatus(file, "Error");
                if (data.error.message == "Unauthorized") {
                    self.errorMessage = "Error: Not logged in";
                    stopTheUploader = true;
                } else if (data.error.message == "Request too large") {
                    file.status = plupload.FAILED;
                    self.setStatus(file, "Error: File too large.");
                    self.errorMessage = "Some files were not uploaded. File size limit is 2MB";
                } else if (data.error.message == "File type not supported") {
                    file.status = plupload.FAILED;
                    self.setStatus(file, "Error: Images must be JPEG, GIF or PNG");
                    self.errorMessage = "Some files were not uploaded.";
                } else {
                    self.errorMessage = "One or more errors occurred while uploading. Some files might not have been uploaded.";
                }
            } else {
                if (file.percent === 100 && file.status === plupload.DONE) {
                    var status = "Upload complete";
                    self.setStatus(file, status);
                    var deleteButton = $('#' + file.id + ' .deleteFileUpload');
                    deleteButton.removeClass('i-16-close');
                    deleteButton.addClass('i-16-success');
                }
            }

            if (stopTheUploader) {
                self.uploader.stop();
                self.done();
            }
        });

        if (numberOfExistingPhotos >= this.maxQueuedItems) {
            this.styleAdded();
        }

        this.addButton.on('click', function() {
            if (this.isAdded()) {
                this.styleAdded();
            }
        }.gs_bind(this));

        // Start upload
        if(this.uploadButton !== null) {
            this.uploadButton.click(function() {
                self.startUpload.apply(self, arguments);
            });
        }
    }.gs_bind(this);

    this.disableAddButton = function() {
        this.addButton.hide();
        this.fakeAddButton.show();
        this.container.find('.plupload.flash').css('left','-10000px');
    }.gs_bind(this);

    this.enableAddButton = function() {
        this.fakeAddButton.hide();
        this.addButton.show();
        this.container.find('.plupload.flash').attr('style',this.FLASH_ENABLED_STYLE);
        this.uploader.refresh();
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

    this.styleAdded = function() {
        this.disableAddButton();
    }.gs_bind(this);

    this.styleNotAdded = function() {
        this.enableAddButton();
    }.gs_bind(this);

    this.styleUploading = function() {
        this.uploadButton.prop('disabled',true);
        this.uploadButton.removeClass('button-1');
        this.uploadButton.addClass('button-1-inactive');
        this.disableAddButton();
        this.spinner.show();
    }.gs_bind(this);

    this.updateProgress = function(up, file) {
        if (file.status === plupload.UPLOADING) {
            var status = "Uploading... " + file.percent + "%";
            this.setStatus(file, status);
        }
    }.gs_bind(this);

    this.setStatus = function(file, status) {
        jQuery('#' + file.id + ' .uploadStatus').text(status);
    }.gs_bind(this);

    this.startUpload = function() {
        this.styleUploading();
        this.setStatus(this.uploader.files[0], this.PREPARING);
        this.uploader.start();
        this.container.find('.deleteFileUpload').off('click');
    }.gs_bind(this);

    this.filesAdded = function(up, files) {
        var addedFile = this.container.find('.addedFile');

        var self = this;

        if(this.uploadButton === null) {
            this.uploader.start();
        }
        else {
            // add each file to the queue as long as status is "STOPPED"
            $.each(files, function(i, file) {
                if (file.status === plupload.STOPPED) {

                    self.addItem(file);

                    // when the item's delete icon is clicked, tell the uploader to remove it from the queue, and delete it
                    // from the list
                    jQuery('#' + file.id).on('click', '.deleteFileUpload', function() {
                        self.uploader.removeFile(file);
                        self.removeItem(file.id);
                    });

                    // keep track of how many non-blank items are in the list
                    self.totalItemsInList++;

                    // if this is the first item added, enable the upload button
                    if (self.totalItemsInList === 1) {
                        self.enableUploading();
                    }

                    // stop when max is reached
                    if (self.totalItemsInList > 1) {
                        return false; // exit the $.each
                    }
                }
            });
        }

        if (self.totalItemsInList >= 1) {
            self.styleAdded();
        }
    }.gs_bind(this);

    this.isAdded = function() {
        return (this.uploader.total.queued > this.getMaxQueuedItems());
    }.gs_bind(this);

    this.getMaxQueuedItems = function() {
        if(this.maxQueuedItems == 1) {
            return 1;
        }
        else {
//            return this.maxQueuedItems - GS.pollingPhotoViewer.numberPhotos;
        }
    }.gs_bind(this);

    this.log = function() {
        if (typeof this.LOGGING_ENABLED !== 'undefined' && this.LOGGING_ENABLED === true) {
            console.log(arguments);
        }
    }.gs_bind(this);

    this.addItem = function(file) {
        var htmlBlock = '<span id="' + file.id + '" class="fileNumber">' + file.name + '<span class="uploadStatus">Added for upload</span>' + file.size + ' KB<span class="deleteFileUpload iconx16 i-16-close"><!-- do not collapse --></span></span>';

        var pBody = this.container.find('.addedFile');
        pBody.html(htmlBlock);
    }.gs_bind(this);

    this.removeItem = function(id) {
        this.totalItemsInList--;
        this.container.find('#' + id).remove();
        var pBody = this.container.find('addedFile');
        pBody.html(this.EMPTY_QUEUE_ROW_HTML);
        this.onItemRemoved();
    }.gs_bind(this);

    this.onItemRemoved = function() {
        if (!this.isAdded()) {
            this.styleNotAdded();
        }

        // no items in list, cannot upload
        if (this.totalItemsInList === 0) {
            this.disableUploading();
        }
    }.gs_bind(this);

    this.displayUploaderError = function(message) {
        this.uploadErrorOverlay.html(message);
        this.uploadErrorOverlay.show();
    }.gs_bind(this);

    this.done = function() {
        if (this.errorMessage !== null) {
            this.displayUploaderError(this.errorMessage);
            this.disableAddButton();
        } else {
            this.uploadCompleteOverlay.show();
            this.styleNotAdded();
        }

        if (this.uploader.files.length > 0) {
            var file = this.uploader.files[this.uploader.files.length-1];
            this.uploader.removeFile(file);
        }
//        this.resetOnNextQueue = true;

        this.spinner.hide();
    }.gs_bind(this);

    this.stateChanged = function(up) {
        console.log(up);
    }

    this.handleError = function(up, err) {
        var file = err.file, message;
        alert('error')
        this.spinner.hide();
        alert('error');
        if (err.code === plupload.INIT_ERROR) {
            this.styleInitializationError();
            return;
        }

        if (file) {
            var fileExtension = file.name.substring(file.name.length-4, file.name.length);
            this.errorMessage = err.message;
            if (err.details) {
                this.errorMessage += " (" + err.details + ")";
            }
            if (err.code == plupload.FILE_SIZE_ERROR) {
                if (file.name.indexOf('.gif',file.name.length - 4) !== -1) {
                    alert("There was an error. GIF files have a limit of 2MB. File: " + file.name + " will not be added to the queue.");
                } else {
                    alert("There was an error. " + fileExtension.toUpperCase() + " files have a limit of 20MB before resizing. File: " + file.name + " will not be added to the queue.");
                }
                alert("Error: File too large: " + file.name);
            } else if (err.code == plupload.FILE_EXTENSION_ERROR) {
                alert("There was an error. " + fileExtension.toUpperCase() + " files cannot be uploaded as a photo. Images must be JPEG, GIF or PNG. File: " + file.name + " will not be added to the queue.");
            } else if (err.code == plupload.IMAGE_DIMENSIONS_ERROR) {
                alert("There was an error. Dimensions of file " + file.name + " are too large to process.");
                if (file.status === plupload.FAILED) {
                    // stop-start hack to stop uploader from failing on next photo after Dimensions too large error
                    this.uploader.stop();
                    this.uploader.start();
                    this.setStatus(file, "Error: Dimensions too large");
                }
            } else if (err.code == plupload.IMAGE_FORMAT_ERROR) {
                alert("There was an error. " + file.name + " is corrupted or an unsupported format.");
                if (file.status === plupload.FAILED) {
                    this.setStatus(file, "Error: Unsupported format");
                }
            } else if (err.code == plupload.IMAGE_MEMORY_ERROR) {
                alert("There was an error. Not enough system memory to resize " + file.name + ".");
                if (file.status === plupload.FAILED) {
                    this.setStatus(file, "Error: Out of memory");
                }
            } else {
                this.setStatus(file, "Error");
            }

            //file.hint = message;
            //$('#' + file.id).attr('class', 'plupload_failed').find('a').css('display', 'block').attr('title', message);
        } else {
            this.displayUploaderError(message);
            this.done();
        }
    }.gs_bind(this);

};

GS.MediaUploader.MediaType = {Image : "Image", Pdf : "PDF"};

GS.PollingMediaViewer = function() {
    this.id = null; // dom id of the viewer
    this.url = null; // url to poll from
    this.schoolId = null;
    this.schoolDatabaseState = null;

    this.container = jQuery('#' + this.id);
    this.containerClasses = {};

    this.STATUS_ACTIVE = 'active';
    this.STATUS_PENDING = 'pending';
    this.numberMedias = 0;
    this.numberPending = 0;
    this.numberActive = 0;
    this.IMG_ID_PREFIX = 'js-photo-';

    this.pollFrequency = 5000; //ms
    this.pollingOn = true;
    this.data = null;
};

GS.PollingMediaViewer.prototype.mediaViewer = function () {
    /* not including delete photo. could be added when required during a future release. */

    this.turnPollingOn = function() {
        this.pollingOn = true;
    }.gs_bind(this);

    this.turnPollingOff = function() {
        this.pollingOn = false;
    }.gs_bind(this);

    this.init = function() {
        this.numberPending = this.container.find('.js-photo-pending').not('#js-photo-placeholder').length;
        this.numberActive = this.container.find('.js-photo-active').length;
        this.numberPhotos = this.numberPending + this.numberActive;
        setTimeout(this.poll, this.pollFrequency);
    }.gs_bind(this);

    this.container.on('click', '.js-deletePhoto', function() {
        this.deletePhoto(this);
    });

    this.setImageByClassType = function(type, id, addClass, removeClass, src) {
        var domPhoto = jQuery(this.containerClasses[type]).find(id);
        if(addClass !== null && addClass !== '') {
            domPhoto.addClass(addClass);
        }
        if(removeClass !== null && removeClass !== '') {
            domPhoto.removeClass(removeClass);
        }
        domPhoto.prop('src', src);
    };

    this.poll = function() {
        var self = this;

        if (this.pollingOn !== true) {
            setTimeout(self.poll, self.pollFrequency);
            return;
        }

        var jqxhr = jQuery.ajax({
            url: this.url,
            type:'GET',
            data: this.data
        }).done(function(data) {
                if (data.type === 'realEstateAgent') {
                    var media = data.media;
                    if(media !=  null && media.hasOwnProperty('photoMediaPath') && media.hasOwnProperty('logoMediaPath')) {
                        if(media.photoMediaPath !== null && data.baseMediaPath !== null) {
                            self.setImageByClassType('photoClass', '#js-photo-preview', '', 'js-photo-pending', data.baseMediaPath + media.photoMediaPath)
                        }
                        else {
                            self.setImageByClassType('photoClass', '#js-photo-preview', 'js-photo-pending', '', '/res/img/realEstate/upload_placeholder.png')
                        }
                        if(media.logoMediaPath !== null && data.baseMediaPath !== null) {
                            self.setImageByClassType('logoClass', '#js-logo-preview', '', 'js-logo-pending', data.baseMediaPath + media.logoMediaPath)
                        }
                        else {
                            self.setImageByClassType('logoClass', '#js-logo-preview', 'js-logo-pending', '', '/res/img/realEstate/upload_placeholder.png')
                        }
                    }
                }

                setTimeout(self.poll, self.pollFrequency);

            }).fail(function() {
                //alert("error");
                //do nothing
            });
    }.gs_bind(this);
};