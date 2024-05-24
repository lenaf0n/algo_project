package isep.algoproject.ControllerTests;

import isep.algoproject.controlllers.PostController;
import isep.algoproject.models.Dtos.PostCommentDto;
import isep.algoproject.models.Dtos.PostIsLiked;
import isep.algoproject.models.Post;
import isep.algoproject.models.User;
import isep.algoproject.services.PostService;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.view.RedirectView;

import java.io.FileNotFoundException;
import java.time.Instant;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class PostControllerTest {
    @Mock
    private PostService postService;

    @Mock
    private HttpSession session;

    @Mock
    private Model model;

    @Mock
    private MultipartFile imageData;

    @InjectMocks
    private PostController postController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void showCreatePost() {
        String viewName = postController.showCreatePost(model);
        assertEquals("createPost", viewName);
        verify(model, times(1)).addAttribute(eq("post"), any(Post.class));
    }

    @Test
    void createPost_UserNotLoggedIn() throws FileNotFoundException {
        when(session.getAttribute("user")).thenReturn(null);

        String viewName = postController.createPost(new Post(), session, imageData);
        assertEquals("redirect:/login", viewName);
    }

    @Test
    void createPost_Success() throws FileNotFoundException {
        User user = new User();
        when(session.getAttribute("user")).thenReturn(user);

        String viewName = postController.createPost(new Post(), session, imageData);
        assertEquals("redirect:/profile", viewName);
        verify(postService, times(1)).createPost(any(Post.class), eq(user), eq(imageData));
    }

    @Test
    void showViewPost_UserNotLoggedIn() {
        when(session.getAttribute("user")).thenReturn(null);

        String viewName = postController.showViewPost(1L, model, session);
        assertEquals("redirect:/login", viewName);
    }

    @Test
    void showViewPost_Success() {
        User user = new User();
        when(session.getAttribute("user")).thenReturn(user);
        List<PostIsLiked> posts = Collections.singletonList(new PostIsLiked());
        when(postService.getUserPostsByUserId(1L, user)).thenReturn(posts);

        String viewName = postController.showViewPost(1L, model, session);
        assertEquals("userPosts", viewName);
        verify(model, times(1)).addAttribute("posts", posts);
    }

    @Test
    void likePost_UserNotLoggedIn() {
        when(session.getAttribute("user")).thenReturn(null);

        ResponseEntity<?> response = postController.likePost(1L, session);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertInstanceOf(RedirectView.class, response.getBody());
    }

    @Test
    void likePost_Success() {
        User user = new User();
        when(session.getAttribute("user")).thenReturn(user);

        ResponseEntity<?> response = postController.likePost(1L, session);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Post liked", response.getBody());
        verify(postService, times(1)).likePost(1L, user);
    }

    @Test
    void unlikePost_UserNotLoggedIn() {
        when(session.getAttribute("user")).thenReturn(null);

        ResponseEntity<?> response = postController.unlikePost(1L, session);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertInstanceOf(RedirectView.class, response.getBody());
    }

    @Test
    void unlikePost_Success() {
        User user = new User();
        when(session.getAttribute("user")).thenReturn(user);

        ResponseEntity<?> response = postController.unlikePost(1L, session);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Post unliked", response.getBody());
        verify(postService, times(1)).unlikePost(1L, user);
    }

    @Test
    void getCommentsByPostId() {
        List<PostCommentDto> comments = Collections.singletonList(new PostCommentDto(0, "content", "lefo", Instant.now()));
        when(postService.getPostsComments(1L)).thenReturn(comments);

        ResponseEntity<List<PostCommentDto>> response = postController.getCommentsByPostId(1L, session);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(comments, response.getBody());
        verify(session, times(1)).setAttribute("postId", 1L);
    }

    @Test
    void addComment_UserNotLoggedIn() {
        when(session.getAttribute("user")).thenReturn(null);

        ResponseEntity<?> response = postController.addComment("testComment", session);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertInstanceOf(RedirectView.class, response.getBody());
    }

    @Test
    void addComment_Success() {
        User user = new User();
        when(session.getAttribute("user")).thenReturn(user);
        when(session.getAttribute("postId")).thenReturn(1L);

        ResponseEntity<?> response = postController.addComment("testComment", session);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Comment added", response.getBody());
        verify(postService, times(1)).addComment(1L, "testComment", user);
    }

    @Test
    void showAllPosts_UserNotLoggedIn() {
        when(session.getAttribute("user")).thenReturn(null);

        String viewName = postController.showAllPosts(0, model, session);
        assertEquals("redirect:/login", viewName);
    }

    @Test
    void showAllPosts_Success() {
        User user = new User();
        when(session.getAttribute("user")).thenReturn(user);
        Pageable pageable = PageRequest.of(0, 10);
        List<PostIsLiked> posts = Collections.singletonList(new PostIsLiked());
        Page<PostIsLiked> postPage = new PageImpl<>(posts, pageable, 1);
        when(postService.getAllPosts(pageable, user)).thenReturn(postPage);

        String viewName = postController.showAllPosts(0, model, session);
        assertEquals("allPosts", viewName);
        verify(model, times(1)).addAttribute("posts", postPage.getContent());
        verify(model, times(1)).addAttribute("currentPage", 0);
        verify(model, times(1)).addAttribute("totalPages", postPage.getTotalPages());
    }
}
