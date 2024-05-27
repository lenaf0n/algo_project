document.addEventListener('DOMContentLoaded', (event) => {
    const buttons = document.querySelectorAll('#messageTypeSelector .btn');
    buttons.forEach(button => {
        button.addEventListener('click', function(e) {
            console.log("button : " + button)
            e.preventDefault();
            buttons.forEach(button => button.classList.remove('selected'));
            this.classList.add('selected');
        });
    });
});

document.addEventListener('DOMContentLoaded', (event) => {
    loadGlobalMessage();
});

function loadGlobalMessage() {
    $.ajax({
        url: '/message/global',
        method: 'GET',
        success: function(data) {
            $('#messageList').html(data);
            $('#allConversation').html("");
            $('#recipientId').attr("value", 0);
            $('#messageCategory').val("GLOBAL").change();
        },
        error: function() {
            alert('Error loading the form');
        }
    });
}

function loadGroupMessage(groupId) {
    $.ajax({
        url: '/message/group/' + groupId,
        method: 'GET',
        success: function(data) {
            $('#messageList').html(data);
            $('#recipientId').attr("value", groupId);
        },
        error: function() {
            alert('Error loading the form');
        }
    });
}

function leaveGroup(groupId) {
    $.ajax({
        url: '/message/leave-group/' + groupId,
        method: 'GET',
        success: function(data) {
            if($("#recipientId").val() == groupId){
                $('#messageList').html("");
            }
            $('#allConversation').html(data);
            $('#messageCategory').val("GROUP").change();
        },
        error: function() {
            alert('Error loading the form');
        }
    });
}

function addPlayer(groupId) {
    $.ajax({
        url: '/message/group-add/' + groupId,
        method: 'GET',
        success: function(data) {
            $('#messageList').html(data);
            $('#messageCategory').val("GROUP").change();
        },
        error: function() {
            alert('Error loading the form');
        }
    });
}

function loadPrivateMessage(friendId) {
    $.ajax({
        url: '/message/friend/' + friendId,
        method: 'GET',
        success: function(data) {
            $('#messageList').html(data);
            $('#recipientId').attr("value", friendId);
        },
        error: function() {
            alert('Error loading the form');
        }
    });
}

function loadGroupsList() {
    $.ajax({
        url: '/message/group',
        method: 'GET',
        success: function(data) {
            $('#messageList').html("");
            $('#allConversation').html(data);
            $('#messageCategory').val("GROUP").change();
        },
        error: function() {
            alert('Error loading the form');
        }
    });
}

function loadFriendsList() {
    $.ajax({
        url: '/message/friend',
        method: 'GET',
        success: function(data) {
            $('#messageList').html("");
            $('#allConversation').html(data);
            $('#messageCategory').val("FRIENDS").change();
        },
        error: function() {
            alert('Error loading the form');
        }
    });
}

function sendMessage() {
    var content = $('#content').val();
    var image = $('#imageData').prop("files")[0];
    var category = $('#messageCategory').val();
    var recipientId = $('#recipientId').val();
    var formData = new FormData();

    formData.append("content", content);
    formData.append("imageData", image);
    formData.append("category", category);
    formData.append("recipientId", recipientId);

    console.log("content = " + content);
    console.log("image = " + image);
    console.log("category = " + category);
    console.log("recipientId = " + recipientId);

    console.log("content in formData = " + formData.get("content"));
    console.log("image in formData = " + formData.get("imageData"));
    console.log("category in formData = " + formData.get("category"));
    console.log("recipientId in formData = " + formData.get("recipientId"));

    console.log(formData.values());
    $.ajax({
        url: '/message/sendNewMessage',
        method: 'POST',
        data: formData,
        processData: false,
        contentType: false,
        success: function(response) {
            $('#messageList').html(response);
            $('#content').val("").change();
            $('#imageData').val("").change();
        },
        error: function() {
            alert('Error loading the form');
        }
    });
}

$(document).ready(function () {
    $('#globalButton').click(function () {
        loadGlobalMessage();
    });
});

$(document).ready(function () {
    $('#groupsButton').click(function () {
        loadGroupsList();
    });
});

$(document).ready(function () {
    $('#friendsButton').click(function () {
        loadFriendsList();
    });
});

$(document).ready(function () {
    $('#sendMessageButton').click(function (event) {
        event.preventDefault();
        sendMessage();
    });
});


