package isep.algoproject.models;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;

import java.io.Serializable;
import java.time.Instant;

@Entity
public class PlayerGroup implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    @JsonBackReference
    private User user;
    @ManyToOne
    @JoinColumn(name = "group_id", nullable = false)
    @JsonBackReference
    private Group group;
    private Instant addAt;


    // Getter and Setter
    public Long getId() {
        return id;
    }
    public void setId(long id) {
        this.id = id;
    }
    public User getUser() {
        return user;
    }
    public void setUser(User user) {
        this.user = user;
    }
    public Group getGroup() {
        return group;
    }
    public void setGroup(Group group) {
        this.group = group;
    }

    public Instant getAddAt() {
        return addAt;
    }

    public void setAddAt(Instant addAt) {
        this.addAt = addAt;
    }
}
