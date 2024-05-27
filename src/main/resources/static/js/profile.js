document.addEventListener('DOMContentLoaded', (event) => {
    const links = document.querySelectorAll('#linkContainer .custom-link');
    links.forEach(link => {
        link.addEventListener('click', function(e) {
            e.preventDefault();
            links.forEach(link => link.classList.remove('selected'));
            this.classList.add('selected');
        });
    });
});

document.addEventListener('DOMContentLoaded', (event) => {
    loadMyPostsContent();
});

function loadMyPostsContent() {
    const userId = document.getElementById('userName').textContent.trim();
    $.ajax({
        url: '/post/' + userId,
        method: 'GET',
        success: function(data) {
            $('#content').html(data);
            const links = document.querySelectorAll('#linkContainer .custom-link');
            links.forEach(link => link.classList.remove('selected'));
            document.getElementById('myPostsLink').classList.add('selected');
        },
        error: function() {
            alert('Error loading the form');
        }
    });
}

$(document).ready(function(){
    $('#createPostLink').click(function(event){
        event.preventDefault();
        $.ajax({
            url: '/post/create',
            method: 'GET',
            success: function(data) {
                $('#content').html(data);
            },
            error: function() {
                alert('Error loading the page');
            }
        });
    });

    $('#myPostsLink').click(function(event){
        event.preventDefault();
        loadMyPostsContent();
    });

    $('#privacySettingsLink').click(function(event){
        event.preventDefault();
        $.ajax({
            url: '/privacyForm',
            method: 'GET',
            success: function(data) {
                $('#content').html(data);
            },
            error: function(jqXHR, textStatus, errorThrown) {
                console.log("Error details:", jqXHR, textStatus, errorThrown);
                alert('Error loading the page');
            }
        });
    });

});
