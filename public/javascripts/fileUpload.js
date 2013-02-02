$(function() {
    $(".fileSelect").click(function() {
        console.log($(this).next().val());
        $(this).next().click();
        return false;
    });
    $("input[type='file']").change(function() {
        var path = $(this).val();
        var filename = path.split(/(\\|\/)/g).pop();
        $(this).next().text(filename);
    });
});