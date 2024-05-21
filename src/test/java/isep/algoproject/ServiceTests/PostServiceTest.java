package isep.algoproject.ServiceTests;

import isep.algoproject.models.Dtos.PostCommentDto;
import isep.algoproject.models.Dtos.PostIsLiked;
import isep.algoproject.models.Post;
import isep.algoproject.models.PostComment;
import isep.algoproject.models.PostLike;
import isep.algoproject.models.User;
import isep.algoproject.repositories.PostCommentRepository;
import isep.algoproject.repositories.PostRepository;
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
    void getUserPostsByUserIdTest() {
        // Case when posts list is null
        User user = new User();
        user.setId(1L);
        when(postRepository.findPostsByUserId(1L)).thenReturn(null);

        List<PostIsLiked> result = postService.getUserPostsByUserId(1L, user);
        assertNotNull(result);
        assertTrue(result.isEmpty());

        // Normal case with non-null user and posts
        User otherUser = new User();
        otherUser.setId(2L);

        Post post1 = new Post();
        post1.setUser(user);
        Post post2 = new Post();
        post2.setUser(user);

        PostLike like = new PostLike();
        like.setUser(user);
        post1.setLikes(Collections.singletonList(like));

        List<Post> posts = Arrays.asList(post1, post2);
        when(postRepository.findPostsByUserId(1L)).thenReturn(posts);

        result = postService.getUserPostsByUserId(1L, user);

        assertEquals(2, result.size());
        assertTrue(result.get(0).isLiked());
        assertFalse(result.get(1).isLiked());
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

        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt"));

        Post post = new Post();
        PostLike like = new PostLike();
        like.setUser(user);
        List<PostLike> postLikes = new ArrayList<>();
        postLikes.add(like);
        post.setLikes(postLikes);

        Page<Post> postPage = new PageImpl<>(Collections.singletonList(post));

        when(postRepository.findAll(pageable)).thenReturn(postPage);

        Page<PostIsLiked> result = postService.getAllPosts(pageable, user);

        assertEquals(1, result.getContent().size());
        assertTrue(result.getContent().get(0).isLiked());
    }
}
