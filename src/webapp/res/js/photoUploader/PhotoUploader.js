GS = GS || {};

Function.prototype.gs_bind = function(obj) {
    var method = this;
    return function() {
        return method.apply(obj, arguments);
    };
};

GS.PhotoUploader = function(url, maxQueuedItems, schoolId, schoolDatabaseState) {
    this.url = url;
    this.schoolId = schoolId;
    this.schoolDatabaseState = schoolDatabaseState;
    this.totalItemsInList = 0;
    this.uploader = null;
    this.uploadButton = jQuery('#jsPhotoUploadButton');
    this.queueButton = jQuery('#jsPhotoQueueButton');
    this.container = jQuery('#photo-upload-container');
    this.spinner = jQuery('.js-photoUploaderSpinner');
    this.uploadCompleteOverlay = jQuery('#jsUploadComplete');
    this.uploadErrorOverlay = jQuery('#jsUploadError');
    this.errorMessage = null; // an error message for entire uploader to be displayed after uploader done

    this.maxQueuedItems = maxQueuedItems;

    this.blankRowClass = '';
    this.filledRowClass = '';
    this.PREPARING = "Preparing..."; // resizing photo, pre-upload
    this.EMPTY_QUEUE_ROW_HTML = "<tr><td>&nbsp;</td><td>&nbsp;</td><td>&nbsp;</td><td>&nbsp;</td></tr>";

    this.createUploader();
};

GS.PhotoUploader.prototype.createUploader = function() {
    this.uploader = new plupload.Uploader({
        runtimes : 'gears,html5,flash,silverlight,browserplus',
        browse_button : 'jsPhotoQueueButton',
        container: 'photo-upload-container',
        max_file_size : '20mb',
        max_gif_file_size : '2mb',
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
            {title : "Image files", extensions : "jpg,jpeg,gif,png"}
        ]
    });

    this.uploader.init();

    this.init = function(numberOfExistingPhotos) {
        var self = this;
        this.uploader.bind('FilesAdded', this.filesQueued);

        this.uploader.bind("UploadProgress",this.updateProgress);

        this.uploader.bind("Error", this.handleError);

        this.uploader.bind("UploadComplete", function() {
            self.done();
        });

        this.uploader.bind("UploadFile", function(up, file) {
            self.setStatus(file, self.PREPARING);
        });

        this.uploader.bind("FileUploaded", function(up, file, response) {
            var stopTheUploader = false;
            var data = jQuery.parseJSON(response.response);
            if (data && data.error && data.error.message) {
                self.setStatus(file, "Error");
                if (data.error.message == "Unauthorized") {
                    self.errorMessage = "Error: Not logged in";
                    stopTheUploader = true;
                } else if (data.error.message == "Request too large") {
                    self.setStatus(file, "Error: File too large.");
                    self.errorMessage = "Some files were not uploaded. File size limit is 2MB";
                } else if (data.error.message == "File type not supported") {
                    self.setStatus(file, "Error: Images must be JPEG, GIF or PNG");
                    self.errorMessage = "Some files were not uploaded.";
                } else {
                    self.errorMessage = "One or more errors occurred while uploading. Some files might not have been uploaded.";
                }
            }

            if (stopTheUploader) {
                self.uploader.stop();
                self.done();
            }
        });

        if (numberOfExistingPhotos >= this.maxQueuedItems) {
            this.styleQueueFull();
        }

        this.queueButton.on('click', function() {
           if (this.isQueueFull()) {
               this.styleQueueFull();
           }
        }.gs_bind(this));

        this.uploadButton.click(function() {
            self.startUpload.apply(self, arguments);
        });
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
        this.queueButton.prop('disabled',true);
        this.queueButton.removeClass('button-1');
        this.queueButton.addClass('button-1-inactive');
        this.container.css('background-color', 'aaa');
        this.spinner.show();
    }.gs_bind(this);

    this.styleQueueFull = function() {
        this.queueButton.removeClass('button-1');
        this.queueButton.addClass('button-1-inactive');
        this.queueButton.prop('disabled',true);
    }.gs_bind(this);

    this.styleQueueNotFull = function() {
        this.queueButton.addClass('button-1');
        this.queueButton.removeClass('button-1-inactive');
        this.queueButton.prop('disabled',false);
    }.gs_bind(this);

    this.isQueueFull = function() {
        return (this.uploader.total.queued >= this.getMaxQueuedItems());
    }.gs_bind(this);

    this.startUpload = function() {
        this.styleUploading();
        this.setStatus(this.uploader.files[0], this.PREPARING);
        this.uploader.start();
        this.container.find('.deleteFileUpload').off('click');
    }.gs_bind(this);

    this.updateProgress = function(up, file) {
        var status = "Uploading... " + file.percent + "%";
        if (file.percent === 100) {
            status = "Upload complete";
            var deleteButton = $('#' + file.id + ' .deleteFileUpload');
            deleteButton.removeClass('i-16-close');
            deleteButton.addClass('i-16-success');
        }
        this.setStatus(file, status);
    }.gs_bind(this);

    this.done = function() {
        if (this.errorMessage !== null) {
            this.displayUploaderError(this.errorMessage);
        } else {
            this.uploadCompleteOverlay.show();
        }

        // add a namespaced event to remove all photos in uploader list the next time they click the queue button.
        this.queueButton.on('click.removeAllOnceIfDoneUploading', function() {
            this.removeAllItems();
            this.queueButton.off('click.removeAllOnceIfDoneUploading');
        }.gs_bind(this));

        this.spinner.hide();

        this.styleQueueNotFull();
    }.gs_bind(this);

    this.handleError = function(up, err) {
        var file = err.file, message;
        var fileExtension = file.name.substring(file.name.lenth-4, file.name.length);

        this.spinner.hide();

        if (file) {
            message = err.message;

            if (err.details) {
                message += " (" + err.details + ")";
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

    this.filesQueued = function(up, files) {
        var index = 0;
        var htmlblock = '';
        var tbody = jQuery ('#photo-upload-container table tbody');

        if (this.totalItemsInList > this.getMaxQueuedItems()) {
            return;
        }

        var self = this;

        // add each file to the queue as long as status is "STOPPED"
        $.each(files, function(i, file) {
            if (file.status === plupload.STOPPED) {

                self.addItemAtPosition(file, self.totalItemsInList);

                // when the item's delete icon is clicked, tell the uploader to remove it from the queue, and delete it
                // from the list
                jQuery('#' + file.id + ' .deleteFileUpload').click(function() {
                    self.uploader.removeFile(file);
                    self.removeItem(file.id);
                });

                // add our own property to plupload file object
                files[i]._gsPosition = self.totalItemsInList;

                // keep track of how many non-blank items are in the list
                self.totalItemsInList++;

                // if this is the first item added, enable the upload button
                if (self.totalItemsInList === 1) {
                    self.enableUploading();
                }

                // stop when max is reached
                if (self.totalItemsInList === self.getMaxQueuedItems()) {
                    return false; // exit the $.each
                }
            }
        });

        if (self.totalItemsInList >= self.getMaxQueuedItems()) {
            self.styleQueueFull();
        }
    }.gs_bind(this);

    this.displayUploaderError = function(message) {
        this.uploadErrorOverlay.html(message);
        this.uploadErrorOverlay.show();
    }.gs_bind(this);

    this.addItemAtPosition = function(file, position) {
        htmlblock = '<tr id="' + file.id + '" class="fileNumber'+ position +'"><td>' + file.name + '</td><td class="uploadStatus">Queued for upload</td><td>' + file.size + ' KB</td><td><span class="deleteFileUpload iconx16 i-16-close"><!-- do not collapse --></span></td></tr>';

        var tableBody = this.container.find('table tbody');
        tableBody.find('tr:eq('+ parseInt(position) +')').after(htmlblock);
        tableBody.find('tr:eq('+ parseInt(position) + ')').remove();
    }.gs_bind(this);

    this.removeAllItems = function() {
        while (this.uploader.files.length > 0) {
            var file = this.uploader.files[this.uploader.files.length-1];
            this.removeItem(file.id);
            this.uploader.removeFile(file);
        }
    }.gs_bind(this);

    this.removeItem = function(id) {
        this.totalItemsInList--;
        this.container.find('#' + id).remove();
        this.container.find('table tbody tr:last').after(this.EMPTY_QUEUE_ROW_HTML);
        this.onItemRemoved();
    }.gs_bind(this);

    this.onItemRemoved = function() {
        if (!this.isQueueFull()) {
            this.styleQueueNotFull();
        }

        // no items in list, cannot upload
        if (this.totalItemsInList === 0) {
            this.disableUploading();
        }
    }.gs_bind(this);

    this.setStatus = function(file, status) {
        jQuery('#' + file.id + ' .uploadStatus').html(status);
    }.gs_bind(this);

    this.setStatusByPosition = function(position, status) {
        this.container.find('.fileNumber' + position + ' .uploadStatus').html(status);
    }.gs_bind(this);

    // mark a file upload row as done
    this.itemComplete = function(position) {
        this.setStatusByPosition(position, "Upload complete");
    }.gs_bind(this);

    this.getMaxQueuedItems = function() {
        return this.maxQueuedItems - GS.pollingPhotoViewer.numberPhotos;
    }.gs_bind(this);

};



GS.PollingPhotoViewer = function(id, url, schoolId, schoolDatabaseState) {
    this.id = id; // dom id of the viewer
    this.url = url; // url to poll from
    this.schoolId = schoolId;
    this.schoolDatabaseState = schoolDatabaseState;

    this.container = jQuery('#' + id);

    this.STATUS_ACTIVE = 'active';
    this.STATUS_PENDING = 'pending';
    this.numberPhotos = 0;
    this.numberPending = 0;
    this.numberActive = 0;
    this.IMG_ID_PREFIX = 'js-photo-';

    this.pollFrequency = 5000; //ms
    this.pollingOn = true;
    var pollingPhotoViewerSelf = this;

    this.deletePhoto = function(deleteTrigger) {
        var pollingPhotoViewerItem = jQuery(deleteTrigger).parent();
        var photoId = pollingPhotoViewerItem.prop('id').substring(26, pollingPhotoViewerItem.prop('id').length);
        GSType.hover.photoDeleteConfirmation.updateHoverWithImage(jQuery(deleteTrigger).siblings('img').clone());
        GSType.hover.photoDeleteConfirmation.show(function() {
            var data = {
                schoolMediaId:photoId,
                schoolId:this.schoolId,
                schoolDatabaseState:this.schoolDatabaseState,
                _method:"DELETE"
            };

            var jqxhr = jQuery.ajax({
                url:'/photoUploader/photoUploaderTest.page',
                type:'POST',
                data:data
            }).fail(function() {
                alert("Sorry, an error occurred trying to delete the photo. Please try again soon.");
            }).done(function() {
                var id = '#js-photo-' + photoId;
                jQuery(id).parent().remove();
                if ($(pollingPhotoViewerItem).find('.js-photo-pending').length > 0) {
                    this.numberPending--;
                } else if ($(pollingPhotoViewerItem).find('.js-photo-active').length > 0) {
                    this.numberActive--;
                }
                this.numberPhotos--;

                // iteracts with PhotoUploader JS  TODO: give pollingPhotoViewer it's own reference to photoUploader
                // to pollingPhotoViewer;
                GS.photoUploader.onItemRemoved();

                GSType.hover.photoDeleteConfirmation.hide();
            }.gs_bind(this));
        }.gs_bind(this));
    }.gs_bind(this);

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
        pollingPhotoViewerSelf.deletePhoto(this);
    });

    this.poll = function() {
        var self = this;

        if (this.pollingOn !== true) {
            setTimeout(self.poll, self.pollFrequency);
            return;
        }

        var jqxhr = jQuery.ajax({
            url:pollingPhotoViewerSelf.url,
            type:'GET',
            data:{
                schoolId:self.schoolId,
                schoolDatabaseState:self.schoolDatabaseState
            }
        }).done(function(data) {
            var photos = data.schoolMedias;

            if (photos !== undefined) {
                self.numberPhotos = photos.length;
                for (i = 0; i < photos.length; i++) {
                    var imgId = self.IMG_ID_PREFIX + photos[i].id;
                    var domPhoto = self.container.find('#' + imgId);
                    if (photos[i].statusAsString === self.STATUS_ACTIVE) {
                        if (domPhoto.length === 1) {
                            if (domPhoto.hasClass('js-photo-pending')) {
                                domPhoto.prop('src',data.basePhotoPath + photos[i].smallSizeFile);
                                domPhoto.prop('alt','active ' + photos[i].originalFileName);
                                domPhoto.removeClass('js-photo-pending');
                                domPhoto.addClass('js-photo-active');
                                self.numberActive+=1;
                                self.numberPending-=1;
                            }
                        } else if (domPhoto.length == 0) {
                            jQuery('#js-noPhotosYet:visible').hide();
                            var newPhotoContainer = jQuery('#js-photo-template').clone();
                            newPhotoContainer.prop('id','js-pollingPhotoViewerItem-' + photos[i].id);
                            newPhotoContainer.find('#' + self.IMG_ID_PREFIX + 'placeholder').prop('id',imgId);
                            newPhotoContainer.css('display','block');
                            jQuery(self.container).find('div:last').after(newPhotoContainer);
                            jQuery('#' + imgId).prop('alt', 'active ' + photos[i].originalFileName);
                            jQuery('#' + imgId).prop('src', data.basePhotoPath + photos[i].smallSizeFile);
                        }
                    } else if (photos[i].statusAsString === self.STATUS_PENDING) {
                        if (jQuery(self.container).has('#' + imgId).length === 0) {
                            jQuery('#js-noPhotosYet:visible').hide();
                            var newPhotoContainer = jQuery('#js-photo-template').clone();
                            newPhotoContainer.prop('id','js-pollingPhotoViewerItem-' + photos[i].id);
                            newPhotoContainer.find('#' + self.IMG_ID_PREFIX + 'placeholder').prop('id',imgId);
                            newPhotoContainer.css('display','block');
                            jQuery(self.container).find('div:last').after(newPhotoContainer);
                            jQuery('#' + imgId).prop('alt', 'pending ' + photos[i].originalFileName);
                        }
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