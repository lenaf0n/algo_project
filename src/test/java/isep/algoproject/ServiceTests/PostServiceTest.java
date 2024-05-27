package isep.algoproject.ServiceTests;

import isep.algoproject.models.Dtos.PostCommentDto;
import isep.algoproject.models.Dtos.PostIsLiked;
import isep.algoproject.models.Post;
import isep.algoproject.models.PostComment;
import isep.algoproject.models.PostLike;
import isep.algoproject.models.User;
import isep.algoproject.models.enums.Status;
import isep.algoproject.repositories.ConnectionRepository;
import isep.algoproject.repositories.PostCommentRepository;
import isep.algoproject.repositories.PostRepository;
import isep.algoproject.repositories.UserRepository;
import isep.algoproject.services.ImageService;
import isep.algoproject.services.PostService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Instant;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class PostServiceTest {
    @Mock
    private PostRepository postRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ConnectionRepository connectionRepository;

    @Mock
    private PostCommentRepository postCommentRepository;

    @Mock
    private ImageService imageService;

    @InjectMocks
    private PostService postService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createPostTest() throws IOException {
        Post post = new Post();
        User user = new User();
        MultipartFile file = mock(MultipartFile.class);

        when(file.getOriginalFilename()).thenReturn("image.png");
        when(imageService.compressImage(file)).thenReturn(new byte[10]);

        postService.createPost(post, user, file);

        assertEquals(user, post.getUser());
        assertNotNull(post.getCreatedAt());
        verify(postRepository, times(1)).save(post);
    }

    @Test
    public void testGetUserPostsByUserId_UserHasNoPrivacyAndNoFriendConnection_ReturnsEmptyList() {
        // Arrange
        long userId = 1L;
        long sessionUserId = 2L;

        User user = new User();
        user.setId(userId);
        user.setPostPrivacy(true); // User's posts are private

        User sessionUser = new User();
        sessionUser.setId(sessionUserId);

        List<Post> posts = new ArrayList<>();

        when(userRepository.findById(userId)).thenReturn(user);
        when(postRepository.findPostsByUserIdOrderByCreatedAtDesc(userId)).thenReturn(posts);
        when(connectionRepository.existsByUser1IdAndUser2IdAndStatus(userId, sessionUserId, Status.FRIEND)).thenReturn(false);
        when(connectionRepository.existsByUser1IdAndUser2IdAndStatus(sessionUserId, userId, Status.FRIEND)).thenReturn(false);

        // Act
        List<PostIsLiked> result = postService.getUserPostsByUserId(userId, sessionUser);

        // Assert
        assertTrue(result.isEmpty());
    }

    @Test
    public void testGetUserPostsByUserId_UserIsFoundButNoPosts_ReturnsEmptyList() {
        // Arrange
        long userId = 1L;
        long sessionUserId = 2L;

        User user = new User();
        user.setId(userId);
        user.setPostPrivacy(false); // User's posts are public

        User sessionUser = new User();
        sessionUser.setId(sessionUserId);

        when(userRepository.findById(userId)).thenReturn(user);
        when(postRepository.findPostsByUserIdOrderByCreatedAtDesc(userId)).thenReturn(null);

        // Act
        List<PostIsLiked> result = postService.getUserPostsByUserId(userId, sessionUser);

        // Assert
        assertTrue(result.isEmpty());
    }

    @Test
    public void testGetUserPostsByUserId_SessionUserIsFriend_ReturnsPostList() {
        // Arrange
        long userId = 1L;
        long sessionUserId = 2L;

        User user = new User();
        user.setId(userId);
        user.setPostPrivacy(true);
        List<Post> posts = new ArrayList<>();
        Post post1 = new Post();
        post1.setId(1L);
        post1.setLikes(new ArrayList<>());
        posts.add(post1);

        User sessionUser = new User();
        sessionUser.setId(sessionUserId);

        when(userRepository.findById(userId)).thenReturn(user);
        when(postRepository.findPostsByUserIdOrderByCreatedAtDesc(userId)).thenReturn(posts);
        when(connectionRepository.existsByUser1IdAndUser2IdAndStatus(userId, sessionUserId, Status.FRIEND)).thenReturn(true);
        when(connectionRepository.existsByUser1IdAndUser2IdAndStatus(sessionUserId, userId, Status.FRIEND)).thenReturn(false);

        // Act
        List<PostIsLiked> result = postService.getUserPostsByUserId(userId, sessionUser);

        // Assert
        assertEquals(1, result.size());
        assertEquals(post1, result.get(0).getPost());
        assertFalse(result.get(0).isLiked());
    }

    @Test
    void likePost_HandlesNullLikesList() {
        User user = new User();
        user.setId(1L);

        Post post = new Post();
        post.setId(1L);
        post.setLikes(null);

        when(postRepository.findPostById(1L)).thenReturn(post);

        postService.likePost(1L, user);

        assertNotNull(post.getLikes());
        assertEquals(1, post.getLikes().size());
        verify(postRepository, times(1)).save(post);
    }

    @Test
    void likePost_HandlesNonNullLikesList() {
        User user = new User();
        user.setId(1L);

        Post post = new Post();
        post.setId(2L);
        post.setLikes(new ArrayList<>());

        when(postRepository.findPostById(2L)).thenReturn(post);

        postService.likePost(2L, user);

        assertEquals(1, post.getLikes().size());
        verify(postRepository, times(1)).save(post);
    }

    @Test
    void unlikePostTest() {
        User user = new User();
        user.setId(1L);

        Post post = new Post();
        post.setId(1L);

        PostLike like = new PostLike();
        like.setUser(user);
        List<PostLike> postLikes = new ArrayList<>();
        postLikes.add(like);
        post.setLikes(postLikes);

        when(postRepository.findPostById(1L)).thenReturn(post);

        postService.unlikePost(1L, user);

        assertEquals(0, post.getLikes().size());
        verify(postRepository, times(1)).save(post);
    }

    @Test
    void getPostsCommentsTest() {
        PostComment comment = new PostComment();
        comment.setId(1L);
        comment.setContent("Test comment");
        comment.setCreatedAt(Instant.now());
        User user = new User();
        user.setUsername("user");
        comment.setUser(user);

        when(postCommentRepository.findByPostIdOrderByCreatedAtAsc(1L)).thenReturn(Collections.singletonList(comment));

        List<PostCommentDto> result = postService.getPostsComments(1L);

        assertEquals(1, result.size());
        assertEquals("Test comment", result.get(0).getContent());
        assertEquals("user", result.get(0).getUsername());
    }

    @Test
    void addCommentTest() {
        User user = new User();
        user.setId(1L);
        user.setUsername("user");

        Post post = new Post();
        post.setId(1L);

        when(postRepository.findPostById(1L)).thenReturn(post);

        postService.addComment(1L, "Test comment", user);

        ArgumentCaptor<PostComment> commentCaptor = ArgumentCaptor.forClass(PostComment.class);
        verify(postCommentRepository, times(1)).save(commentCaptor.capture());

        assertEquals("Test comment", commentCaptor.getValue().getContent());
        assertEquals(user, commentCaptor.getValue().getUser());
        assertEquals(post, commentCaptor.getValue().getPost());
    }

    @Test
    void getAllPostsTest() {
        User user = new User();
        user.setId(1L);

        User postOwner = new User();
        postOwner.setId(2L);

        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt"));

        Post post = new Post();
        post.setUser(postOwner);
        PostLike like = new PostLike();
        like.setUser(user);
        List<PostLike> postLikes = new ArrayList<>();
        postLikes.add(like);
        post.setLikes(postLikes);

        Page<Post> postPage = new PageImpl<>(Collections.singletonList(post));

        when(postRepository.findAll(pageable)).thenReturn(postPage);
        when(connectionRepository.existsByUser1IdAndUser2IdAndStatus(postOwner.getId(), user.getId(), Status.FRIEND)).thenReturn(false);
        when(connectionRepository.existsByUser1IdAndUser2IdAndStatus(user.getId(), postOwner.getId(), Status.FRIEND)).thenReturn(false);


        Page<PostIsLiked> result = postService.getAllPosts(pageable, user);

        assertEquals(1, result.getContent().size());
        assertTrue(result.getContent().get(0).isLiked());
    }
}
