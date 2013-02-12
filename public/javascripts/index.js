var slideshowTimer = 0;

$(function() {

    window.mySwipe = new Swipe(document.getElementById('slider'), {
        callback: function(event, index, elem) {
            slideshowTimer = 0;
            $(".slideIcon").removeClass("active");
            $(".slideIcon:nth-child(" + (index+1) + ")").addClass("active");
        }
    });

    setInterval(function() {
        if (slideshowTimer++ >= 7)
            mySwipe.next();
    }, 1000);

    $("#sliderPrev").click(function() {
        mySwipe.prev();
    });

    $("#sliderNext").click(function() {
        mySwipe.next();
    });

});