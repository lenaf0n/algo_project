package isep.algoproject.models.Dtos;

import java.time.Instant;

public class PostCommentDto {
    private long id;
    private String content;
    private String username;
    private Instant createdAt;

    public PostCommentDto(long id, String content, String username, Instant createdAt) {
        this.id = id;
        this.content = content;
        this.username = username;
        this.createdAt = createdAt;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
