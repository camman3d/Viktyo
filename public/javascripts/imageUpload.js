/**
 * Created with IntelliJ IDEA.
 * User: Josh
 * Date: 2/14/13
 * Time: 8:19 AM
 * To change this template use File | Settings | File Templates.
 */

$(function() {
    $(".imageFormUploading").hide();

    $("form.imageUpload").submit(function() {
        var $form = $(this),
            image = $form.find("input[type='file']").get(0).files[0],
            name = $form.find("input[name='name']"),
            purpose = $form.attr("data-purpose"),
            url = $form.attr("data-url");

        // Show the loading icon and hide the form
        $(".imageFormUploading").show();
        $form.hide();

        // Upload the image asynchronously
        var formData = new FormData();
        formData.append("image", image);
        formData.append("name", name);
        formData.append("purpose", purpose);
        $.ajax({
            url: '/images',
            data: formData,
            cache: false,
            contentType: false,
            processData: false,
            type: 'POST',
            success: function(data){

                // Done. Grab the image id and redirect
                var imageId = data.imageId;
                window.location = (url + imageId);
            },
            error: function(data) {
                alert(JSON.stringify(data));
            }
        });

        // Return false to stop the form from submitting.
        return false;
    })
});