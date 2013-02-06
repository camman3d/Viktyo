function adjustImage() {
    // Adjust the image
    var $img = $(".postingImageContainer img");
    var height = + $img.attr("data-height");
    var width = + $img.attr("data-width");
    var scale = $img.width() / width;
    var top = ((height * scale) - 200) / 2;
    $img.css("margin-top", "-" + top + "px");
}

$(function() {
    $(document).ready(adjustImage());
    $(window).load(function() {
        adjustImage();
        setTimeout(adjustImage, 1000);
    });
});