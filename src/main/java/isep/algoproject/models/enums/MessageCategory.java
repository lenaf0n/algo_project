package isep.algoproject.models.enums;

public enum MessageCategory {
    GLOBAL("Global"),
    FRIENDS("Friends"),
    GROUP("Group"),
    PRIVATE("Private");

    private final String displayName;

    MessageCategory(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
