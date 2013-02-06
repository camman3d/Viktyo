
var postingTemplates = {
    standard:
        '<div class="posting">' +
            '    <div class="postingImageContainer">' +
            '        <div class="postingImage">' +
            '            <a href="{{link}}"><img src="{{imageSrc}}"></a>' +
            '        </div>' +
            '    </div>' +
            '    <a href="{{link}}">' +
            '        <div class="postingDescription">' +
            '            <h4>{{title}}</h4>' +
            '            <p>{{content}}</p>' +
            '        </div>' +
            '    </a>' +
            '    <div class="postingPoster">' +
            '        by <a href="{{posterLink}}">{{poster}}</a>' +
            '    </div>' +
            '    <a href="{{link}}">' +
            '        <div class="postingFooter">' +
            '            <div class="row-fluid">' +
            '                <div class="span4 centered">' +
            '                    <i class="icon-user"></i> {{followers}}' +
            '                </div>' +
            '                <div class="span4 centered">' +
            '                    <i class="icon-star"></i> {{favorites}}' +
            '                </div>' +
            '                <div class="span4 centered">' +
            '                    <i class="icon-eye-open"></i> {{views}}' +
            '                </div>' +
            '            </div>' +
            '        </div>' +
            '    </a>' +
            '</div>',

    panoramio:
        '<div class="posting panoramio">' +
            '    <div class="postingImageContainer">' +
            '        <div class="postingImage">' +
            '            <a href="{{link}}"><img src="{{imageSrc}}"></a>' +
            '        </div>' +
            '        <div class="postingPanoramioLogo">' +
            '            <img src="/assets/images/postings/panoramio.gif">' +
            '        </div>' +
            '        <div class="postingPanoramioName">' +
            '            <a href="{{panoramioImageUrl}}">{{panoramioImageName}}</a>' +
            '        </div>' +
            '        <div class="postingPanoramioAuthor">' +
            '            By <a href="{{panoramioAuthorUrl}}">{{panoramioAuthor}}</a>' +
            '        </div>' +
            '    </div>' +
            '    <a href="{{link}}">' +
            '        <div class="postingDescription">' +
            '            <h4>{{title}}</h4>' +
            '            <p>{{content}}</p>' +
            '        </div>' +
            '    </a>' +
            '    <div class="postingPoster">' +
            '        by <a href="{{posterLink}}">{{poster}}</a>' +
            '    </div>' +
            '    <a href="{{link}}">' +
            '        <div class="postingFooter">' +
            '            <div class="row-fluid">' +
            '                <div class="span4 centered">' +
            '                    <i class="icon-user"></i> {{followers}}' +
            '                </div>' +
            '                <div class="span4 centered">' +
            '                    <i class="icon-star"></i> {{favorites}}' +
            '                </div>' +
            '                <div class="span4 centered">' +
            '                    <i class="icon-eye-open"></i> {{views}}' +
            '                </div>' +
            '            </div>' +
            '        </div>' +
            '    </a>' +
            '</div>'
};
