$(function() {
    var $location = $("#location");

    var autocomplete = new google.maps.places.Autocomplete($location.get(0));
    var g = google.maps.Geocoder();

    google.maps.event.addListener(autocomplete, 'place_changed', function() {
        //infowindow.close();
        var place = autocomplete.getPlace();
        var lat = place.geometry.location.lat();
        var lng = place.geometry.location.lng();
        $("#latitude").val(lat);
        $("#longitude").val(lng);
    });

    // Prevent enter while in location
    $(window).keydown(function(event){
        if($("#location").is(":focus") && event.keyCode == 13) {
            event.preventDefault();
            return false;
        }
        return true;
    });
});