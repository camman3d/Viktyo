var postingRenderer = {
    renderPanoramio: function renderPanoramio(posting) {
        var data = {
            title: posting.name,
            content: posting.description,
            link: "/posting/" + posting.id,
            imageSrc: posting.panoramio.photo_file_url,
            poster: posting.poster.fullname,
            posterLink: "/users/" + posting.poster.id,
            followers: posting.followers,
            favorites: posting.favorites,
            views: posting.views,
            panoramioImageUrl: posting.panoramio.photo_url,
            panoramioImageName: posting.panoramio.photo_title,
            panoramioAuthorUrl: posting.panoramio.owner_url,
            panoramioAuthor: posting.panoramio.owner_name
        };
        var html = Mustache.to_html(postingTemplates.panoramio, data);
        $("#postings").append(html);
    },
    renderStandard: function renderStandard(posting) {
        var data = {
            title: posting.name,
            content: posting.description,
            link: "/posting/" + posting.id,
            imageSrc: posting.coverPicture,
            poster: posting.poster.fullname,
            posterLink: "/users/" + posting.poster.id,
            followers: posting.followers,
            favorites: posting.favorites,
            views: posting.views
        };
        var html = Mustache.to_html(postingTemplates.standard, data);
        $("#postings").append(html);
    }
};

$(function() {
    for(var i in postings) {
        var posting = postings[i];
        if (posting.panoramio !== undefined && posting.panoramio !== null)
            postingRenderer.renderPanoramio(posting);
        else
            postingRenderer.renderStandard(posting);
    }
});