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
    private String name;
    private String description;
    @Lob
    @Column(columnDefinition = "MEDIUMBLOB")
    private String image;

    @Column(nullable = false)
    private Instant createdAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    // Getter and Setter

}
