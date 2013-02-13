var organizationsTemplates = {
    default:
        '<div class="organizationHolder">' +
        '<div class="organization">' +
        '    <div class="organizationBackgroundImg">' +
        '        <div class="organizationBackgroundHolder">' +
        '            <img src="{{backgroundPicture}}">' +
        '        </div>' +
        '        <div class="organizationLogo profile-pic-small-border">' +
        '            <img class="profile-pic-small" src="{{profilePicture}}">' +
        '        </div>' +
        '    </div>' +
        '    <div class="organizationInfo">' +
        '        <h4>{{name}}</h4>' +
        '        <h4><small><i class="icon-map-marker"></i> {{location}}</small></h4>' +
        '    </div>' +
        '    <hr>' +
        '    <div class="organizationDescription">{{description}}</div>' +
        '    <div class="organizationLearnMore">' +
        '        <a href="{{url}}">Learn More</a>' +
        '    </div>' +
        '    <div class="organizationOverlay">' +
        '        <div class="profile-pic-small-border">' +
        '            <img class="profile-pic-small" src="{{profilePicture}}">' +
        '        </div>' +
        '        <p>{{description}}</p>' +
        '        <div class="organizationStats">' +
        '            <div class="organizationStat">' +
        '                <span class="organizationStatNumber">{{postingCount}}</span> Listings' +
        '            </div>' +
        '            <div class="organizationStat">' +
        '                <span class="organizationStatNumber">{{networkCount}}</span> Networks' +
        '            </div>' +
        '        </div>' +
        '        <div class="organizationLearnMoreOverlay">' +
        '            <a href="{{url}}">Learn More</a>' +
        '        </div>' +
        '    </div>' +
        '</div>'
};

$(function() {
    var $organizations = $("#organizations");
    for (var i=0; i< organizations.length; i++ ) {
        var org = organizations[i];
        var data = {
            url: "/users/" + org.id,
            backgroundPicture: org.backgroundPicture,
            profilePicture: org.profilePicture,
            name: org.fullname,
            location: org.location,
            description: org.description,
            postingCount: org.postings,
            networkCount: org.networks
        };
        var html = Mustache.to_html(organizationsTemplates.default, data);
        $organizations.append(html);
    }
});