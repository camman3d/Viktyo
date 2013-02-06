$(function() {
    // Adjust the image
    var $img = $(".postingImageContainer img");
    var height = + $img.attr("data-height");
    var top = (height - 200) / 2;
    $img.css("margin-top", "-" + top + "px");


});