GS = GS || {};

GS.RealEstateAgentCreatePhotoUploader = function() {
    this.name= 'photo';

    this.url = "/mediaUpload/realEstateAgentUpload.page";
    this.uploader = null;
    this.filterType = GS.MediaUploader.MediaType.Image;

    this.uploadButton = null;
//    this.uploadButton = jQuery('.registrationHover:visible').find('#jq-photoUploaderButton');
    this.addButton = jQuery('.registrationHover:visible').find('.jq-photoUploadButton');
    this.fakeAddButton = jQuery('.registrationHover:visible').find('.jq-photoUploadFakeButton');
    this.buttonClass = '.registrationHover:visible .js-photoButtons';

    this.container = jQuery('.registrationHover:visible').find('.photoContainer');
    this.spinner = jQuery('.registrationHover:visible').find('.js-photoUploaderSpinner');
    this.uploadCompleteOverlay = jQuery('.registrationHover:visible').find('.jsUploadComplete');

    this.uploadErrorOverlay = jQuery('.registrationHover:visible').find('.jsPhotoUploadError');
    this.errorMessage = null; // an error message for entire uploader to be displayed after uploader done
    this.FLASH_ENABLED_STYLE = 'position: absolute; top: 191px; background: none repeat scroll 0% 0% transparent; z-index: 9999999; width: 101px; height: 23px; left: 11px;';

    this.maxQueuedItems = 1;
    this.multi_selection = false;

    this.browseButton = $('.registrationHover:visible .jq-photoUploadButton')[0];
    this.uploadContainer = $('.registrationHover:visible .photoContainer')[0];
    this.params = {'isPhoto' : true};

    this.createPhotoUploader();
};

GS.RealEstateAgentCreatePhotoUploader.prototype = new GS.MediaUploader();

GS.RealEstateAgentCreatePhotoUploader.prototype.createPhotoUploader = function() {
    this.createUploader();
};

GS.RealEstateAgentCreateLogoUploader = function() {
    this.name ='logo';

    this.url = "/mediaUpload/realEstateAgentUpload.page";
    this.uploader = null;
    this.filterType = GS.MediaUploader.MediaType.Image;

    this.uploadButton = null;
    this.addButton = jQuery('.registrationHover:visible').find('.jq-logoUploadButton');
    this.fakeAddButton = jQuery('.registrationHover:visible').find('.jq-logoUploadFakeButton');
    this.buttonClass = '.registrationHover:visible .js-logoButtons';

    this.container = jQuery('.registrationHover:visible').find('.logoContainer');
    this.spinner = jQuery('.registrationHover:visible').find('.js-logoUploaderSpinner');
    this.uploadCompleteOverlay = jQuery('.registrationHover:visible').find('.jsUploadComplete');

    this.uploadErrorOverlay = jQuery('.registrationHover:visible').find('.jsLogoUploadError');
    this.errorMessage = null; // an error message for entire uploader to be displayed after uploader done
    this.FLASH_ENABLED_STYLE = 'position: absolute; top: 191px; background: none repeat scroll 0% 0% transparent; z-index: 9999999; width: 101px; height: 23px; left: 11px;';

    this.maxQueuedItems = 1;
    this.multi_selection = false;

    this.browseButton = $('.registrationHover:visible .jq-logoUploadButton')[0];
    this.uploadContainer = $('.registrationHover:visible .logoContainer')[0];

    this.params = {'isLogo' : true};

    this.createLogoUploader();
};

GS.RealEstateAgentCreateLogoUploader.prototype = new GS.MediaUploader();

GS.RealEstateAgentCreateLogoUploader.prototype.createLogoUploader = function() {
    this.createUploader();
};