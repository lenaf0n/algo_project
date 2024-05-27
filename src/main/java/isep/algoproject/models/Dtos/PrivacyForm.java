package isep.algoproject.models.Dtos;

public class PrivacyForm {
    private boolean InterestPrivacy;
    private boolean GraphPrivacy;
    private boolean PostPrivacy;

    public boolean isInterestPrivacy() {
        return InterestPrivacy;
    }

    public void setInterestPrivacy(boolean interestPrivacy) {
        InterestPrivacy = interestPrivacy;
    }

    public boolean isGraphPrivacy() {
        return GraphPrivacy;
    }

    public void setGraphPrivacy(boolean graphPrivacy) {
        GraphPrivacy = graphPrivacy;
    }

    public boolean isPostPrivacy() {
        return PostPrivacy;
    }

    public void setPostPrivacy(boolean postPrivacy) {
        PostPrivacy = postPrivacy;
    }
}
