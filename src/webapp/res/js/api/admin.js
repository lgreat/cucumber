$(function() {
    $('.accountType').click(function () {
        if ("f" == $('input[name=type]:checked').val()) {
            $('#premium_options').hide();
        } else {
            $('#premium_options').show();
        }

    });

    $('.accountConfig').click(function () {
        $('#update').show();
    });

    $('.deleteAccount').click(function () {
        return confirm('Are you sure you want to delete this account?');
    });
});
