var GS = GS || {};
GS.realEstateAgent = GS.realEstateAgent || {};

GSType.hover.RealEstateAgentRegistrationHover = function() {
    this.loadDialog = function() {};

    this.show = function() {
        GSType.hover.realEstateAgentRegistrationHover.showModal();

        var hover = jQuery('.js-registrationHover:visible');

        hover.on('click', '.jq-personalInfoSubmit:visible', function() {
            var form = $('.jq-personalInfoForm:visible');
            var data = {};
            form.find('input').each(function() {
                if(!this.name.startsWith('confirm')) {
                    data[this.name] = this.value;
                }
            });

            $.ajax({
                type : 'POST',
                url : '/real-estate/savePersonalInfo.page',
                data : data,
                success : function (response) {
                    form.addClass('dn');
                    hover.find('.jq-businessInfoForm').removeClass('dn');
                },
                error : function (e) {
//                    alert('error: ' + e);
                }
            });
        });

        hover.on('click', '.jq-businessInfoSubmit:visible', function() {
            var form = $('.jq-businessInfoForm:visible');
            var data = {};
            form.find('input').each(function() {
                data[this.name] = this.value;
            });
            data['state'] = form.find('select[name=state]').val();

            $.ajax({
                type : 'POST',
                url : '/real-estate/saveBusinessInfo.page',
                data : data,
                success : function (response) {
                    form.addClass('dn');
                    hover.find('.jq-imageUploaderForm').removeClass('dn');

                    GS.realEstateAgentPhotoUploader = new GS.RealEstateAgentCreatePhotoUploader();
                    GS.realEstateAgentLogoUploader = new GS.RealEstateAgentCreateLogoUploader();
                    GS.realEstateAgentPhotoUploader.init();
                    GS.realEstateAgentLogoUploader.init();
                },
                error : function (e) {
//                    alert('error: ' + e);
                }
            });
        });

        hover.on('click', '.jq-completeRegistration', function() {
            window.location.href = window.location.protocol + '//' + window.location.host +
                '/real-estate/create-guide.page';
        })
    };
};

GSType.hover.RealEstateAgentRegistrationHover.prototype = new GSType.hover.HoverDialog('js-registrationHover', 480)

GSType.hover.realEstateAgentRegistrationHover = new GSType.hover.RealEstateAgentRegistrationHover();

jQuery(function(){
    GSType.hover.realEstateAgentRegistrationHover.loadDialog();

    jQuery('#jq-realEstAgentRegisterBtn').on('click', function() {
        GSType.hover.realEstateAgentRegistrationHover.show();
        return false;
    });
});

