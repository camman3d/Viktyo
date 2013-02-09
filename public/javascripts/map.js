
/*
 * Define the different icons
 */
var mapIcons = {
    markers: {
        orange: "/assets/images/map/orange-marker.png",
        green: "/assets/images/map/green-marker.png",
        red: "/assets/images/map/red-marker.png",
        blue: "/assets/images/map/blue-marker.png",
        purple: "/assets/images/map/purple-marker.png"
    },
    balloons: {
        orange: "/assets/images/map/orange-balloon.png",
        green: "/assets/images/map/green-balloon.png",
        red: "/assets/images/map/red-balloon.png",
        blue: "/assets/images/map/blue-balloon.png",
        purple: "/assets/images/map/purple-balloon.png"
    }
};

/*
 * Code for creating div popups for the map
 */

var mapPopups = {
    templates: {
        single:
            '<div class="mapPopupSingle">' +
            '    <div class="mapPopupSingleImage">' +
            '        <img src="{{image}}">' +
            '    </div>' +
            '    <div class="mapPopupSingleContent">' +
            '        <h4>{{title}}</h4>' +
            '        <div class="mapPopupLocation"><i class="icon-map-marker"></i> {{location}}</div>' +
            '        <div class="mapPopupPoster"><em>By {{poster}}</em></div>' +
            '        <div><a href="{{url}}">Learn more</a></div>' +
            '    </div>' +
            '    <div class="mapPopupPoint"></div>' +
            '</div>',

        singlePanoramio:
            '<div class="mapPopupSingle">' +
            '    <div class="mapPopupSingleImage">' +
            '        <img src="{{image}}">' +
            '        <img src="/assets/images/postings/panoramio.gif" class="mapPopupSingleImagePanoramioLogo" />' +
            '        <div class="mapPopupSingleImagePanoramio">' +
            '            <a href="{{panoramioImageUrl}}">{{panoramioImageName}}</a><br>' +
            '            By <a href="{{panoramioAuthorUrl}}">{{panoramioAuthor}}</a>' +
            '        </div>' +
            '    </div>' +
            '    <div class="mapPopupSingleContent">' +
            '        <h4>{{title}}</h4>' +
            '        <div class="mapPopupLocation"><i class="icon-map-marker"></i> {{location}}</div>' +
            '        <div class="mapPopupPoster"><em>By {{poster}}</em></div>' +
            '        <div><a href="{{url}}">Learn more</a></div>' +
            '    </div>' +
            '    <div class="mapPopupPoint"></div>' +
            '</div>'
    },
    create: {
        single: function createSinglePopup(posting) {
            // Create the element
            var div = document.createElement('div');
            var $div = $(div);
            $div.addClass("mapPopup").addClass("single");

            // Create the html
            var html = "";
            var data = {
                image: posting.coverPicture,
                title: posting.name,
                location: posting.location.name,
                poster: posting.poster.fullname,
                url: "/posting/" + posting.id
            };
            if (posting.panoramio !== undefined && posting.panoramio !== null) {
                data.panoramioImageUrl = posting.panoramio.photo_url;
                data.panoramioImageName = posting.panoramio.photo_title;
                data.panoramioAuthorUrl = posting.panoramio.owner_url;
                data.panoramioAuthor = posting.panoramio.owner_name;
                data.image = posting.panoramio.photo_file_url;
                html = Mustache.to_html(mapPopups.templates.singlePanoramio, data);
            } else
                html = Mustache.to_html(mapPopups.templates.single, data);

            // Fill the element with the html
            $div.html(html).hide();
            return div;
        }
    },
    move: function(element, x, y) {
        $(element).css("left", x + "px").css("top", y + "px");
    }
};

/*
 * Create the Google map overlays for postings/organizations
 */

PostingOverlay.prototype = new google.maps.OverlayView();

function PostingOverlay(posting, map) {
    this.pos_ = new google.maps.LatLng(posting.location.latitude, posting.location.longitude);
    this.map_ = map;
    this.posting_ = posting;

    this.div_ = null;
    this.setMap(map);
}

PostingOverlay.prototype.onAdd = function() {
    var div = mapPopups.create.single(this.posting_);
    this.div_ = div;
    var panes = this.getPanes();
    panes.overlayMouseTarget.appendChild(div);
};
PostingOverlay.prototype.draw = function() {
    var overlayProjection = this.getProjection();
    var pos = overlayProjection.fromLatLngToDivPixel(this.pos_);
    var div = this.div_;
    mapPopups.move(div, pos.x, pos.y);
};
PostingOverlay.prototype.onRemove = function() {
    this.div_.parentNode.removeChild(this.div_);
    this.div_ = null;
};
PostingOverlay.prototype.hide = function() {
    if (this.div_)
        $(this.div_).hide();
};
PostingOverlay.prototype.show = function() {
    if (this.div_) {
        // Close the others
        for(var i in overlays)
            overlays[i].hide();
        $(this.div_).show();
    }
};
PostingOverlay.prototype.toggle = function() {
    if (this.div_) {
        if ($(this.div_).is(":hidden"))
            this.show();
        else
            this.hide();
    }
};
PostingOverlay.prototype.toggleDOM = function() {
    if (this.getMap())
        this.setMap(null);
    else
        this.setMap(this.map_);
};



$(function() {

    // Google map display options
    var mapOptions = {
        center: new google.maps.LatLng(31.50363, 19.24805),
        zoom: 4,
        mapTypeId: google.maps.MapTypeId.ROADMAP
    };
    map = new google.maps.Map(document.getElementById("map_canvas"), mapOptions);

    // Create icons and overlays for each of the postings
    window.overlays = [];
    for(var i in postings) {
        var posting = postings[i];
        var marker = new google.maps.Marker({
            position: new google.maps.LatLng(posting.location.latitude, posting.location.longitude),
            map: map,
            icon: mapIcons.markers[posting.color]
        });
        var overlay = new PostingOverlay(posting, map);

        // Set up the click listener
        marker.overlayIndex = overlays.length;
        overlays.push(overlay);
        google.maps.event.addListener(marker, 'click', function() {
            overlays[this.overlayIndex].toggle();
        });
    }
});