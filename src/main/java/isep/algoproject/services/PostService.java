package isep.algoproject.services;

import isep.algoproject.models.*;
import isep.algoproject.repositories.PostCommentRepository;
import isep.algoproject.repositories.PostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PostService {
    @Autowired
    private PostRepository postRepository;

    @Autowired
    private PostCommentRepository postCommentRepository;

    @Autowired
    private ImageService imageService;

    public void createPost(Post post, User user, MultipartFile file) throws FileNotFoundException {
        post.setUser(user);
        post.setCreatedAt(Instant.now());

        String fileName = StringUtils.cleanPath(file.getOriginalFilename());
        if(fileName.contains("..")) {
            throw new FileNotFoundException();
        }
        try {
            byte[] compressedFile = imageService.compressImage(file);
            post.setImage(Base64.getEncoder().encodeToString(compressedFile));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        postRepository.save(post);
    }

    public List<PostIsLiked> getUserPostsByUserId(long userId, User user) {
        List<PostIsLiked> postsIsLiked = new ArrayList<>();
        List<Post> posts = postRepository.findPostsByUserId(userId);
        for (Post post : posts) {
            PostIsLiked postIsLiked = new PostIsLiked();
            postIsLiked.setPost(post);
            if (post.getLikes().stream().anyMatch(like -> like.getUser().getId() == user.getId())) {
                postIsLiked.setLiked(true);
            }
            postsIsLiked.add(postIsLiked);
        }

        return postsIsLiked;
    }

    public void likePost(long postId, User user) {
        Post post = postRepository.findPostById(postId);

        PostLike postLike = new PostLike();
        postLike.setPost(post);
        postLike.setUser(user);

        post.getLikes().add(postLike);
        postRepository.save(post);
    }

    public void unlikePost(long postId, User user) {
        Post post = postRepository.findPostById(postId);

        PostLike postLike = post.getLikes().stream().filter(like -> like.getUser().getId() == user.getId()).findFirst().orElse(null);
        if(postLike != null) {
            post.getLikes().remove(postLike);
            postRepository.save(post);
        }
    }

    public List<PostCommentDto> getPostsComments(long postId) {
        List<PostComment> postComments = postCommentRepository.findByPostIdOrderByCreatedAtAsc(postId);

        return postComments.stream()
                .map(comment -> new PostCommentDto(
                        comment.getId(),
                        comment.getContent(),
                        comment.getUser().getUsername(),  // Extracting the username
                        comment.getCreatedAt()))
                .collect(Collectors.toList());
    }

    public void addComment(long postId, PostComment postComment, User user) {
        Post post = postRepository.findPostById(postId);

        postComment.setPost(post);
        postComment.setUser(user);
        postComment.setCreatedAt(Instant.now());

        postCommentRepository.save(postComment);
    }

    public Page<PostIsLiked> getAllPosts(Pageable pageable, User user) {
        Page<Post> posts = postRepository.findAll(pageable);
        List<PostIsLiked> postsIsLikedContent = new ArrayList<>();
        for (Post post : posts) {
            PostIsLiked postIsLiked = new PostIsLiked();
            postIsLiked.setPost(post);
            if (post.getLikes().stream().anyMatch(like -> like.getUser().getId() == user.getId())) {
                postIsLiked.setLiked(true);
            }
            postsIsLikedContent.add(postIsLiked);
        }
        return new PageImpl<>(postsIsLikedContent, pageable, posts.getTotalElements());
    }
}
