package isep.algoproject.models.Dtos;

import isep.algoproject.models.User;

public class SearchResultUser {
    private User user;
    private String status;

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
