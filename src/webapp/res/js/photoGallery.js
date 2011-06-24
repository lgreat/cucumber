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
GS.photoGallery.PhotoGallery = function(id,multiSizeImageArray,debug) {
    this.id = id;
    this.multiSizeImageArray = multiSizeImageArray;
    this.thumbnailIdPrefix = "galleryThumbnail";
    this.fullSizeImageIdPrefix = "galleryFullSize";
    
    this.thumbnailLoaderPosition = 0;
    this.fullSizeImageLoaderPosition = 0;
    this.chosenTimeout = 500;
    this.debug = debug;
};

GS.photoGallery.PhotoGallery.prototype.showFullSizeImage = function(index) {
    var id;
    for(var i=0; i<this.multiSizeImageArray.length; i++) {
        if (i === index) {
            continue;
        }
        id = this.fullSizeImageIdPrefix + '-' + i;
        document.getElementById(id).style.display='none';
    }
    id = this.fullSizeImageIdPrefix + '-' + index;
    document.getElementById(id).style.display='block';
};

GS.photoGallery.PhotoGallery.prototype.loadFirstFullSizeImage = function() {
    this.loadFullSizeImage(0);
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
    var i = this.thumbnailLoaderPosition;
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
            var index = id.split('-')[1];
            self.showFullSizeImage(index);
        });
    }
};

GS.photoGallery.PhotoGallery.prototype.show = function() {
    this.loadFullSizeImages();
   jQuery('#' + this.id).show();
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
GS.photoGallery.Image = function(src,id,cssClass,alt,title,height,width) {
    this.src = src;
    this.id = id;
    this.cssClass = cssClass;
    this.alt = alt;
    this.title = title;
    this.height = height;
    this.width = width;

    this.loaded = false;
};
