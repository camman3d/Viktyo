$(function() {
    $(".panoramioImageHolder > img").click(function() {
        var index = + $(this).attr("data-panoramio-index");
        var panoramio = JSON.stringify(panoramios[index]);

        $("#panoramioValue").val(panoramio);
        $("#panoramioForm").submit();
    });
});