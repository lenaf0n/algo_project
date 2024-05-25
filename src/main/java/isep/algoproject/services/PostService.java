package isep.algoproject.services;

import isep.algoproject.models.*;
import isep.algoproject.models.Dtos.PostCommentDto;
import isep.algoproject.models.Dtos.PostIsLiked;
import isep.algoproject.models.enums.Status;
import isep.algoproject.repositories.ConnectionRepository;
import isep.algoproject.repositories.PostCommentRepository;
import isep.algoproject.repositories.PostRepository;
import isep.algoproject.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class PostService {
    @Autowired
    private PostRepository postRepository;

    @Autowired
    private PostCommentRepository postCommentRepository;

    @Autowired
    private ImageService imageService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ConnectionRepository connectionRepository;

    public void createPost(Post post, User user, MultipartFile file) throws FileNotFoundException {
        post.setUser(user);
        post.setCreatedAt(Instant.now());

        String fileName = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));
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

    public List<PostIsLiked> getUserPostsByUserId(long userId, User sessionUser) {
        List<PostIsLiked> postsIsLiked = new ArrayList<>();
        User user = userRepository.findById(userId);
        List<Post> posts = postRepository.findPostsByUserIdOrderByCreatedAtDesc(userId);

        boolean connection1 = connectionRepository.existsByUser1IdAndUser2IdAndStatus(userId, sessionUser.getId(), Status.FRIEND);
        boolean connection2 = connectionRepository.existsByUser1IdAndUser2IdAndStatus(sessionUser.getId(), userId, Status.FRIEND);

        if (posts != null && (!user.isPostPrivacy() || connection1 || connection2 || Objects.equals(user.getId(), sessionUser.getId()))) {
            for (Post post : posts) {
                PostIsLiked postIsLiked = new PostIsLiked();
                postIsLiked.setPost(post);
                if (post.getLikes() != null && post.getLikes().stream().anyMatch(like -> Objects.equals(like.getUser().getId(), sessionUser.getId()))) {
                    postIsLiked.setLiked(true);
                }
                postsIsLiked.add(postIsLiked);
            }
        }
        return postsIsLiked;
    }

    public void likePost(long postId, User user) {
        Post post = postRepository.findPostById(postId);

        if (post == null) {
            throw new IllegalArgumentException("Post not found with id: " + postId);
        }

        PostLike postLike = new PostLike();
        postLike.setPost(post);
        postLike.setUser(user);

        if (post.getLikes() == null) {
            post.setLikes(new ArrayList<>());
        }

        post.getLikes().add(postLike);
        postRepository.save(post);
    }

    public void unlikePost(long postId, User user) {
        Post post = postRepository.findPostById(postId);

        PostLike postLike = post.getLikes().stream().filter(like -> Objects.equals(like.getUser().getId(), user.getId())).findFirst().orElse(null);
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

    public void addComment(long postId, String comment, User user) {
        Post post = postRepository.findPostById(postId);

        PostComment postComment = new PostComment();
        postComment.setPost(post);
        postComment.setUser(user);
        postComment.setContent(comment);
        postComment.setCreatedAt(Instant.now());

        postCommentRepository.save(postComment);
    }

    public Page<PostIsLiked> getAllPosts(Pageable pageable, User user) {
        Pageable sortedByCreatedAt = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        Page<Post> posts = postRepository.findAll(sortedByCreatedAt);

        List<PostIsLiked> postsIsLikedContent = new ArrayList<>();
        for (Post post : posts) {
            User postOwner = post.getUser();
            boolean connection1 = connectionRepository.existsByUser1IdAndUser2IdAndStatus(postOwner.getId(), user.getId(), Status.FRIEND);
            boolean connection2 = connectionRepository.existsByUser1IdAndUser2IdAndStatus(user.getId(), postOwner.getId(), Status.FRIEND);

            if (!postOwner.isPostPrivacy() || connection1 || connection2 || Objects.equals(user.getId(), postOwner.getId())) {
                PostIsLiked postIsLiked = new PostIsLiked();
                postIsLiked.setPost(post);
                postIsLiked.setLiked(post.getLikes().stream().anyMatch(like -> Objects.equals(like.getUser().getId(), user.getId())));
                postsIsLikedContent.add(postIsLiked);
            }
        }

        return new PageImpl<>(postsIsLikedContent, pageable, posts.getTotalElements());
    }

}
