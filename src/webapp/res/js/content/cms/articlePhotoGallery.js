var GS = GS || {};
GS.photoGallery = GS.photoGallery || {};
Function.prototype.gs_bind = function(obj) {
    var method = this;
    return function() {
        return method.apply(obj, arguments);
    };
};
/**
 * Constructor
 */
GS.photoGallery.PhotoGallery = function(prefix,multiSizeImageArray,debug) {
    this.closeButtonDomId = prefix+"-photo-gallery-close"; //close button
    this.backButtonId = prefix+"-photo-gallery-back";
    this.nextButtonId = prefix+"-photo-gallery-next";
    this.thumbnailIdPrefix = prefix+"-gallery-thumbnail";
    this.thumbnailSelectedCssClass = "gallery-thumbnail-selected";
    this.fullSizeImageIdPrefix = prefix+"-gallery-fullsize";

    this.id = prefix+"-photo-gallery";
    this.currentFullSizeImage = 0;
    this.numberOfImages = multiSizeImageArray.length;
    this.multiSizeImageArray = multiSizeImageArray;

    this.thumbnailLoaderPosition = 0;
    this.fullSizeImageLoaderPosition = 0;
    this.chosenTimeout = 50; //ms
    this.debug = debug;
    this.photoMargins = [];
};

GS.photoGallery.PhotoGallery.prototype.showFullSizeImage = function(index) {
    var id;
    //hide all other images
    for(var i=0; i<this.multiSizeImageArray.length; i++) {
        if (i === index) {
            continue;
        }
        id = this.fullSizeImageIdPrefix + '-' + i;
        jQuery('#' + id).hide();
        jQuery('#' + this.thumbnailIdPrefix + '-' + i).removeClass(this.thumbnailSelectedCssClass);
    }
    //show desired image
    id = this.fullSizeImageIdPrefix + '-' + index;
    jQuery('#' + id).show();

    jQuery('#' + this.thumbnailIdPrefix + '-' + index).addClass(this.thumbnailSelectedCssClass);

    //track change
    this.currentFullSizeImage = index;
};

GS.photoGallery.PhotoGallery.prototype.showNextImage = function() {
    var targetIndex = parseInt(this.currentFullSizeImage) + 1;

    if (targetIndex >= this.numberOfImages) {
        targetIndex = 0;
    }
    this.showFullSizeImage(targetIndex);
};

GS.photoGallery.PhotoGallery.prototype.showPreviousImage = function() {
    var targetIndex = parseInt(this.currentFullSizeImage) - 1;
    if (targetIndex < 0) {
        targetIndex = this.numberOfImages -1;
    }
    this.showFullSizeImage(targetIndex);
};

GS.photoGallery.PhotoGallery.prototype.loadThumbnail = function(index) {
    var image = this.multiSizeImageArray[index].thumbnailImage;
    if (!image.loaded) {
        var container = jQuery('#' + this.thumbnailIdPrefix + '-' + index);
        container.find('img').attr('src',image.src);
        image.loaded = true;
        if (this.debug) {
            console.log("thumbnail " + index + " loaded.");
        }
        return true;
    } else {
        return false;
    }
};

GS.photoGallery.PhotoGallery.prototype.loadThumbnails = function() {
    if (this.thumbnailLoaderPosition >= this.numberOfImages) {
        return;
    }

    var success = this.loadThumbnail(this.thumbnailLoaderPosition);

    if (success) {
        this.thumbnailLoaderPosition++;
        if (this.thumbnailLoaderPosition < this.multiSizeImageArray.length) {
            setTimeout(this.loadThumbnails.gs_bind(this), this.chosenTimeout);
        }
    } else {
        this.thumbnailLoaderPosition++;
        if (this.thumbnailLoaderPosition < this.multiSizeImageArray.length) {
            this.loadThumbnails();
        }
    }
};

GS.photoGallery.PhotoGallery.prototype.loadFullSizeImage = function(index) {
    var image = this.multiSizeImageArray[index].fullSizeImage;
    if (!image.loaded) {
        var container = jQuery('#' + this.fullSizeImageIdPrefix + '-' + index);
        container.find('img').attr('src',image.src);
        image.loaded = true;
        if (this.debug) {
            console.log("full-sized image " + index + " loaded.");
        }
        return true;
    } else {
        return false;
    }
};

GS.photoGallery.PhotoGallery.prototype.loadFullSizeImages = function() {
    if (this.fullSizeImageLoaderPosition >= this.numberOfImages) {
        return;
    }

    var success = this.loadFullSizeImage(this.fullSizeImageLoaderPosition);

    if (success) {
        this.fullSizeImageLoaderPosition++;
        if (this.fullSizeImageLoaderPosition < this.multiSizeImageArray.length) {
            setTimeout(this.loadFullSizeImages.gs_bind(this), this.chosenTimeout*2);
        }
    } else {
        this.fullSizeImageLoaderPosition++;
        if (this.fullSizeImageLoaderPosition < this.multiSizeImageArray.length) {
            this.loadFullSizeImages();
        }
    }
};

GS.photoGallery.PhotoGallery.prototype.loadImages = function() {
    //this.loadFirstFullSizeImage();
    this.loadThumbnails();
    this.loadFullSizeImages();
};

GS.photoGallery.PhotoGallery.prototype.applyThumbnailClickHandlers = function() {
    var self = this; //click handler will have reference through closure; this is probably bad and needs refactoring
    for (var i = 0; i < this.multiSizeImageArray.length; i++) {
        var id = '#' + this.thumbnailIdPrefix + '-' + i;
        var container = jQuery(id);
        container.click(function() {
            var item = jQuery(this);
            var id = item.attr('id');
            var tokens = id.split('-');
            var index = tokens[tokens.length-1];
            self.showFullSizeImage(index);
        });
    }
};

GS.photoGallery.PhotoGallery.prototype.applyButtonClickHandlers = function() {
    jQuery('#' + this.backButtonId).click(function() {
        this.showPreviousImage();
    }.gs_bind(this));
    jQuery('#' + this.nextButtonId).click(function() {
        this.showNextImage();
    }.gs_bind(this));
    jQuery('#' + this.closeButtonDomId).click(function() {
        this.hide();
        document.getElementById("fade").style.display="none";
    }.gs_bind(this));
};

GS.photoGallery.PhotoGallery.prototype.show = function() {
   jQuery('#' + this.id).show();
};
GS.photoGallery.PhotoGallery.prototype.hide = function() {
   jQuery('#' + this.id).hide();
};

/**
 * Make the gallery open when provided dom node is clicked
 * @param id
 */
GS.photoGallery.PhotoGallery.prototype.attachShowEvent = function(cssClass) {
    jQuery("#js_photo_gallery_container ." + cssClass).click(function() {
        this.loadFullSizeImages();
        document.getElementById("fade").style.display="block";
        var photoNumVar = jQuery('input.js_photoNum').val();
        var photoNumToShow = (photoNumVar !== undefined && photoNumVar !== null) ? (isNaN(photoNumVar - 1) ? 0 : (photoNumVar - 1)) : 0;
        this.showFullSizeImage(photoNumToShow); //load the first full size image into gallery
        this.show();
        return false;
    }.gs_bind(this));
};

/**
 * Constructor
 */
GS.photoGallery.MultiSizeImage = function(thumbnailImage, fullSizeImage) {
    this.thumbnailImage = thumbnailImage;
    this.fullSizeImage = fullSizeImage;
};


/**
 * Constructor
 */
GS.photoGallery.Image = function(src,alt,id,cssClass,title,height,width) {
    this.src = src;
    this.id = id;
    this.cssClass = cssClass;
    this.alt = alt;
    this.title = title;
    this.height = height;
    this.width = width;
    this.loaded = false;
};
