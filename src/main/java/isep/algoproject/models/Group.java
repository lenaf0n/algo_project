package isep.algoproject.models;

import isep.algoproject.models.enums.MessageCategory;
import jakarta.persistence.*;

import java.io.Serializable;
import java.time.Instant;

@Entity
public class Group implements Serializable {
    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    private Long id;
    @Column(nullable = false)
    private long sendUserId;
    private long recipientId;
    private String content;
    @Lob
    @Column(columnDefinition = "MEDIUMBLOB")
    private String image;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MessageCategory category;
    @Column(nullable = false)
    private Instant createdAt;
    @Column(nullable = false)
    private boolean isRead;


    // Getter and Setter
    public Instant getCreatedAt() {
        return createdAt;
    }

    public Long getId() {
        return id;
    }

    public MessageCategory getCategory() {
        return category;
    }

    public String getContent() {
        return content;
    }

    public String getImage() {
        return image;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setCategory(MessageCategory category) {
        this.category = category;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public long getSendUserId() {
        return sendUserId;
    }

    public void setSendUserId(long sendUserId) {
        this.sendUserId = sendUserId;
    }

    public long getRecipientId() {
        return recipientId;
    }

    public void setRecipientId(long recipientId) {
        this.recipientId = recipientId;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }
}
