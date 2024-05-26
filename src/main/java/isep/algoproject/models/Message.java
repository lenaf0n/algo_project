package isep.algoproject.models;

import com.fasterxml.jackson.annotation.JsonBackReference;
import isep.algoproject.models.enums.MessageCategory;
import jakarta.persistence.*;

import java.io.Serializable;
import java.time.Instant;
@Entity
public class Message implements Serializable {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private long id;
    @Column(nullable = false)
    private Instant createdAt;
    @Column(nullable = false)
    private boolean isRead;
    @ManyToOne
    @JoinColumn(name = "send_user_id", nullable = false)
    @JsonBackReference
    private User sendUser;
    private long recipientId;
    @Column(columnDefinition = "text")
    private String content;
    @Lob
    @Column(columnDefinition = "MEDIUMBLOB")
    private String image;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MessageCategory category;



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

    public void setId(long id) {
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

    public User getSendUser() {
        return sendUser;
    }

    public void setSendUser(User sendUser) {
        this.sendUser = sendUser;
    }
}
