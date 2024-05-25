package isep.algoproject.models;

import jakarta.persistence.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
public class User implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(nullable = false, unique = true)
    public String username;

    @Column(nullable = false)
    public String name;

    @Column(nullable = false, unique = true)
    public String email;

    @Column(nullable = false)
    public String password;

    private String bio;

    @ManyToMany(mappedBy = "likedByUsers")
    private List<Interest> likedInterests = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Post> posts;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PostLike> likes;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PostComment> comments;

    @Column(nullable = false)
    private boolean postPrivacy = false;

    @Column(nullable = false)
    private boolean interestPrivacy = false;

    @Column(nullable = false)
    private boolean graphPrivacy = false;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public List<Interest> getLikedInterests() {
        return likedInterests;
    }

    public void setLikedInterests(List<Interest> likedInterests) {
        this.likedInterests = likedInterests;
    }

    public List<Post> getPosts() {
        return posts;
    }

    public void setPosts(List<Post> posts) {
        this.posts = posts;
    }

    public List<PostLike> getLikes() {
        return likes;
    }

    public void setLikes(List<PostLike> likes) {
        this.likes = likes;
    }

    public List<PostComment> getComments() {
        return comments;
    }

    public void setComments(List<PostComment> comments) {
        this.comments = comments;
    }

    public boolean isPostPrivacy() {
        return postPrivacy;
    }

    public void setPostPrivacy(boolean postPrivacy) {
        this.postPrivacy = postPrivacy;
    }

    public boolean isInterestPrivacy() {
        return interestPrivacy;
    }

    public void setInterestPrivacy(boolean interestPrivacy) {
        this.interestPrivacy = interestPrivacy;
    }

    public boolean isGraphPrivacy() {
        return graphPrivacy;
    }

    public void setGraphPrivacy(boolean graphPrivacy) {
        this.graphPrivacy = graphPrivacy;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return postPrivacy == user.postPrivacy && interestPrivacy == user.interestPrivacy && graphPrivacy == user.graphPrivacy && Objects.equals(id, user.id) && Objects.equals(username, user.username) && Objects.equals(name, user.name) && Objects.equals(email, user.email) && Objects.equals(password, user.password) && Objects.equals(bio, user.bio)  && Objects.equals(posts, user.posts) && Objects.equals(likes, user.likes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, username, name, email, password, bio, likedInterests, posts, likes, comments, postPrivacy, interestPrivacy, graphPrivacy);
    }
}
