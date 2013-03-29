GS = GS || {};

GS.RealEstateAgentCreatePhotoUploader = function() {
    this.name= 'photo';

    this.url = "/mediaUpload/realEstateAgentUpload.page";
    this.uploader = null;
    this.filterType = GS.MediaUploader.MediaType.Image;

    this.uploadButton = null;
//    this.uploadButton = jQuery('.js-registrationHover:visible').find('#jq-photoUploaderButton');
    this.addButton = jQuery('.js-registrationHover:visible').find('.jq-photoUploadButton');
    this.fakeAddButton = jQuery('.js-registrationHover:visible').find('.jq-photoUploadFakeButton');
    this.buttonClass = '.js-registrationHover:visible .js-photoButtons';

    this.container = jQuery('.js-registrationHover:visible').find('.jq-photoContainer');
    this.spinner = jQuery('.js-registrationHover:visible').find('.js-photoUploaderSpinner');
    this.uploadCompleteOverlay = jQuery('.js-registrationHover:visible').find('.jsUploadComplete');

    this.uploadErrorOverlay = jQuery('.js-registrationHover:visible').find('.jsPhotoUploadError');
    this.errorMessage = null; // an error message for entire uploader to be displayed after uploader done
    this.FLASH_ENABLED_STYLE = 'position: absolute; top: 191px; background: none repeat scroll 0% 0% transparent; z-index: 9999999; width: 101px; height: 23px; left: 11px;';

    this.maxQueuedItems = 1;
    this.multi_selection = false;

    this.browseButton = $('.js-registrationHover:visible .jq-photoUploadButton')[0];
    this.uploadContainer = $('.js-registrationHover:visible .jq-photoContainer')[0];
    this.params = {'mediaType' : 'photo'};

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
    this.addButton = jQuery('.js-registrationHover:visible').find('.jq-logoUploadButton');
    this.fakeAddButton = jQuery('.js-registrationHover:visible').find('.jq-logoUploadFakeButton');
    this.buttonClass = '.js-js-registrationHover:visible .js-logoButtons';

    this.container = jQuery('.js-registrationHover:visible').find('.jq-logoContainer');
    this.spinner = jQuery('.js-registrationHover:visible').find('.js-logoUploaderSpinner');
    this.uploadCompleteOverlay = jQuery('.js-registrationHover:visible').find('.jsUploadComplete');

    this.uploadErrorOverlay = jQuery('.js-registrationHover:visible').find('.jsLogoUploadError');
    this.errorMessage = null; // an error message for entire uploader to be displayed after uploader done
    this.FLASH_ENABLED_STYLE = 'position: absolute; top: 191px; background: none repeat scroll 0% 0% transparent; z-index: 9999999; width: 101px; height: 23px; left: 11px;';

    this.maxQueuedItems = 1;
    this.multi_selection = false;

    this.browseButton = $('.js-registrationHover:visible .jq-logoUploadButton')[0];
    this.uploadContainer = $('.js-registrationHover:visible .jq-logoContainer')[0];

    this.params = {'mediaType' : 'logo'};

    this.createLogoUploader();
};

GS.RealEstateAgentCreateLogoUploader.prototype = new GS.MediaUploader();

GS.RealEstateAgentCreateLogoUploader.prototype.createLogoUploader = function() {
    this.createUploader();
};

GS.RealEstateAgentPollingViewer = function () {
    this.url = '/mediaUpload/agentUploaderTest.json';

    this.containerClasses.photoClass = '.js-registrationHover:visible .jq-photoContainer:eq(0)';
    this.containerClasses.logoClass = '.js-registrationHover:visible .jq-logoContainer:eq(0)';

    this.IMG_ID_PREFIX = 'js-photo-';

    this.pollFrequency = 5000; //ms
    this.pollingOn = true;
    this.data = null;
    this.pollingViewer();
};

GS.RealEstateAgentPollingViewer.prototype = new GS.PollingMediaViewer();
GS.RealEstateAgentPollingViewer.prototype.pollingViewer = function() {
    this.mediaViewer();
};