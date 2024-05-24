$(document).ready(function () {
    $('#searchInput').on('input', function () {
        let username = $(this).val();
        if (username.trim() !== '' && !username.startsWith('#')) {
            $.ajax({
                type: 'GET',
                url: '/search/' + username,
                success: function (data) {
                    $('#userList').empty();
                    if (data.length <= 10) {
                        data.forEach(function (searchResultUser) {
                            let listItem = $('<li>').addClass('list-group-item d-flex justify-content-between align-items-center');
                            let nameUsernameContainer = $('<div>').addClass('col');
                            let name = $('<strong>').text(searchResultUser.user.name).addClass('pl-1');
                            let username = $('<span>').addClass('text-muted').text('@' + searchResultUser.user.username);
                            nameUsernameContainer.append(name, username);

                            nameUsernameContainer.click(function () {
                                window.location.href = `/user-page/${searchResultUser.user.id}`;
                            });

                            let heartButton = $('<button>').addClass('btn btn-sm');

                            if (searchResultUser.status === 'FRIEND') {
                                heartButton.addClass('btn-danger').html('<i class="bi bi-heart-fill"></i>');
                            } else if (searchResultUser.status === 'PENDING') {
                                heartButton.addClass('btn-warning').attr('disabled', true).html('<i class="bi bi-heart"></i>');
                            } else {
                                heartButton.addClass('btn-outline-danger').html('<i class="bi bi-heart"></i>');
                            }

                            heartButton.click(function () {
                                if ($(this).hasClass('btn-danger')) {
                                    filledHeartFunction(searchResultUser.user.id);
                                } else {
                                    outlineHeartFunction(searchResultUser.user.id);
                                }
                            });
                            listItem.append(nameUsernameContainer, heartButton);
                            $('#userList').append(listItem);
                        });
                    }
                },
                error: function (xhr, status, error) {
                    console.error('Error:', error);
                    console.log('Response Text:', xhr.responseText); // Log response text
                }
            });
        } else if (username.replace(/^#/, "").trim() !== '' && username.startsWith('#')) {
            $.ajax({
                type: 'GET',
                url: '/interest/' + username.replace(/^#/, ""),
                success: function (data) {
                    $('#userList').empty();
                    console.log(data)
                    if (data.length <= 10) {
                        data.forEach(function (searchResultInterest) {
                            let listItem = $('<li>').addClass('list-group-item d-flex justify-content-between align-items-center');
                            let nameContainer = $('<div>').addClass('col');
                            let name = $('<strong>').text('Interest : ' + searchResultInterest.interest.name).addClass('pl-1');
                            nameContainer.append(name);

                            nameContainer.click(function () {
                                window.location.href = `/interest-page/${searchResultInterest.interest.id}`;
                            });

                            let heartButton = $('<button>').addClass('btn btn-sm');

                            if (searchResultInterest.liked) {
                                heartButton.addClass('btn-danger').html('<i class="bi bi-heart-fill"></i>');
                            } else {
                                heartButton.addClass('btn-outline-danger').html('<i class="bi bi-heart"></i>');
                            }

                            nameContainer.click(function () {
                                window.location.href = `/interest-page/${searchResultInterest.interest.id}`
                            })

                            heartButton.click(function () {
                                if ($(this).hasClass('btn-danger')) {
                                    removeInterest(searchResultInterest.interest.id);
                                } else {
                                    addInterest(searchResultInterest.interest.id);
                                }
                            });
                            listItem.append(nameContainer, heartButton);
                            $('#userList').append(listItem);
                        });
                    }
                },
                error: function (xhr, status, error) {
                    console.error('Error:', error);
                    console.log('Response Text:', xhr.responseText); // Log response text
                }
            });
        }
        else {
            $('#userList').empty();
        }
    });

    function filledHeartFunction(userId) {
        $.ajax({
            type: 'DELETE',
            url: '/unlike/' + userId,
            success: function (response) {
                closeSearchPopup();
                alert(response);
                location.reload();
            },
            error: function (xhr, status, error) {
                console.error(error);
            }
        });
    }

    function outlineHeartFunction(userId) {
        $.ajax({
            type: 'POST',
            url: '/like/' + userId,
            success: function (response) {
                closeSearchPopup();
                alert(response);
                location.reload();
            },
            error: function (xhr, status, error) {
                console.error(error);
            }
        });
    }

    function removeInterest(interestId) {
        $.ajax({
            type: 'DELETE',
            url: '/interest/remove/' + interestId,
            success: function (response) {
                closeSearchPopup();
                alert(response);
                location.reload();
            },
            error: function (xhr, status, error) {
                console.error(error);
            }
        });
    }

    function addInterest(interestId) {
        $.ajax({
            type: 'POST',
            url: '/interest/like/' + interestId,
            success: function (response) {
                closeSearchPopup();
                alert(response);
                location.reload();
            },
            error: function (xhr, status, error) {
                console.error(error);
            }
        });
    }

    $('#searchModal').on('hidden.bs.modal', function () {
        $('#searchInput').val('');
        $('#userList').empty();
    });
});

function closeSearchPopup() {
    $('#searchModal').modal('hide');
}

function fetchNotifications() {
    $.ajax({
        url: '/user/notifications',
        method: 'GET',
        success: function(response) {
            if (response.length > 0) {
                updateNotificationsLink(true);
                $('#connectionList').empty();
                response.forEach(function (connection) {
                    let listItem = $('<li>').addClass('list-group-item d-flex justify-content-between align-items-center');
                    let nameUsernameContainer = $('<div>').addClass('col');
                    let name = $('<strong>').text(connection.name).addClass('pl-1');
                    let username = $('<span>').addClass('text-muted').text('@' + connection.username);
                    nameUsernameContainer.append(name, username);
                    let acceptButton = $('<button>').addClass('btn btn-sm').text('Accept connection');

                    acceptButton.click(function () {
                        accpetConnection(connection.id);
                    });
                    listItem.append(nameUsernameContainer, acceptButton);
                    $('#connectionList').append(listItem);
                });
            } else {
                updateNotificationsLink(false);
            }
        },
        error: function(error) {
            console.error('Error fetching notifications:', error);
        }
    });
}

function updateNotificationsLink(hasNotifications) {
    const notificationsLink = document.getElementById('notificationsLink');
    if (hasNotifications) {
        notificationsLink.classList.add('text-danger');
    } else {
        notificationsLink.classList.remove('text-danger');
    }
}

function accpetConnection(userId) {
    $.ajax({
        type: 'POST',
        url: '/accept-connection/' + userId,
        success: function (response) {
            alert(response);
            location.reload();
        },
        error: function (xhr, status, error) {
            console.error(error);
        }
    });
}

fetchNotifications();
setInterval(fetchNotifications, 60000);