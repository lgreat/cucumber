$(function() {
    $('.accountType').click(function () {
        if ("f" == $('input[name=type]:checked').val()) {
            $('#premium_options').hide();
        } else {
            $('#premium_options').show();
        }

    });

    $('.deleteAccount').click(function () {
        return confirm('Are you sure you want to delete this account?');
    });

    $('#cancel').click(function () {
        window.location = '/api/admin/accounts.page';
    });
});
