package isep.algoproject.models.Dtos;

import isep.algoproject.models.Interest;

public class SearchResultInterest {
    private Interest interest;
    private boolean isLiked;

    public Interest getInterest() {
        return interest;
    }

    public void setInterest(Interest interest) {
        this.interest = interest;
    }

    public boolean isLiked() {
        return isLiked;
    }

    public void setLiked(boolean liked) {
        isLiked = liked;
    }
}
