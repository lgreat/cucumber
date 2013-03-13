var GS = GS || {};
GS.realEstateAgent = GS.realEstateAgent || {};
GS.realEstateAgent.signUp = function(){
//    alert('submit');
};

GSType.hover.RealEstateAgentRegistrationHover = function() {
    this.loadDialog = function() {};

    this.show = function() {
        GSType.hover.realEstateAgentRegistrationHover.showModal();

        jQuery('.jq-personalInfoSubmit:visible').on('click', function(){
            var form = $('.jq-personalInfoForm:visible');
            var data = {};
            form.find('input').each(function() {
                if(!this.name.startsWith('confirm')) {
                    data[this.name] = this.value;
                }
            });

            $.ajax({
                type : 'POST',
                url : '/realEstateAgent/savePersonalInfo.page',
                data : data,
                success : function (response) {
                    form.addClass('dn');
                    $('.registrationHover:visible .jq-businessInfoForm').removeClass('dn');
                },
                error : function (e) {
//                    alert('error: ' + e);
                }
            });
        });

        jQuery('.registrationHover:visible').on('click', '.jq-businessInfoSubmit:visible', function(){
            var form = $('.jq-businessInfoForm:visible');
            var data = {};
            form.find('input').each(function() {
                data[this.name] = this.value;
            });
            data['state'] = form.find('select[name=state]').val();

            $.ajax({
                type : 'POST',
                url : '/realEstateAgent/saveBusinessInfo.page',
                data : data,
                success : function (response) {
                    form.addClass('dn');
                    $('.registrationHover:visible .jq-imageUploaderForm').removeClass('dn');

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
    };
};

GSType.hover.RealEstateAgentRegistrationHover.prototype = new GSType.hover.HoverDialog('registrationHover', 640)

GSType.hover.realEstateAgentRegistrationHover = new GSType.hover.RealEstateAgentRegistrationHover();

jQuery(function(){
    GSType.hover.realEstateAgentRegistrationHover.loadDialog();

    jQuery('#jq-realEstAgentRegisterBtn').on('click', function() {
        GSType.hover.realEstateAgentRegistrationHover.show();
        return false;
    });
});

