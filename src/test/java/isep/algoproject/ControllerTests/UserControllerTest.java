package isep.algoproject.ControllerTests;

import isep.algoproject.controlllers.UserController;
import isep.algoproject.models.Dtos.*;
import isep.algoproject.models.User;
import isep.algoproject.services.ConnectionService;
import isep.algoproject.services.UserService;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.servlet.view.RedirectView;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class UserControllerTest {
    @Mock
    private UserService userService;

    @Mock
    private ConnectionService connectionService;

    @Mock
    private HttpSession session;

    @Mock
    private Model model;

    @InjectMocks
    private UserController userController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void dashboard_UserNotLoggedIn() {
        when(session.getAttribute("user")).thenReturn(null);

        String viewName = userController.dashboard(session);
        assertEquals("redirect:/login", viewName);
    }

    @Test
    void dashboard_UserLoggedIn() {
        User user = new User();
        when(session.getAttribute("user")).thenReturn(user);

        String viewName = userController.dashboard(session);
        assertEquals("dashboard", viewName);
    }

    @Test
    void logout() {
        String viewName = userController.logout(session);
        assertEquals("redirect:/", viewName);
        verify(session, times(1)).removeAttribute("user");
    }

    @Test
    void searchByUsername_UserNotLoggedIn() {
        when(session.getAttribute("user")).thenReturn(null);

        ResponseEntity<?> response = userController.searchByUsername("username", session);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertInstanceOf(RedirectView.class, response.getBody());
    }

    @Test
    void searchByUsername_UserLoggedIn() {
        User user = new User();
        List<SearchResultUser> users = Collections.singletonList(new SearchResultUser());
        when(session.getAttribute("user")).thenReturn(user);
        when(userService.getUsersByUsername("username", user)).thenReturn(users);

        ResponseEntity<?> response = userController.searchByUsername("username", session);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(users, response.getBody());
    }

    @Test
    void likeUser_UserNotLoggedIn() {
        when(session.getAttribute("user")).thenReturn(null);

        ResponseEntity<?> response = userController.likeUser(1L, session);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertInstanceOf(RedirectView.class, response.getBody());
    }

    @Test
    void likeUser_UserLoggedIn() {
        User user = new User();
        when(session.getAttribute("user")).thenReturn(user);

        ResponseEntity<?> response = userController.likeUser(1L, session);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Connection request sent successful", response.getBody());
        verify(connectionService, times(1)).saveNewConnection(user, 1L);
    }

    @Test
    void unlikeUser_UserNotLoggedIn() {
        when(session.getAttribute("user")).thenReturn(null);

        ResponseEntity<?> response = userController.unlikeUser(1L, session);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertInstanceOf(RedirectView.class, response.getBody());
    }

    @Test
    void unlikeUser_UserLoggedIn() {
        User user = new User();
        when(session.getAttribute("user")).thenReturn(user);

        ResponseEntity<?> response = userController.unlikeUser(1L, session);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Connection request deleted successful", response.getBody());
        verify(connectionService, times(1)).deleteConnection(user, 1L);
    }

    @Test
    void getUserGraph_UserNotLoggedIn() {
        when(session.getAttribute("user")).thenReturn(null);

        ResponseEntity<?> response = userController.getUserGraph(session);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertInstanceOf(RedirectView.class, response.getBody());
    }

    @Test
    void getUserGraph_UserLoggedIn() {
        User user = new User();
        List<Node> nodes = new ArrayList<>();
        List<Link> links = new ArrayList<>();
        Graph graph = new Graph(nodes, links);
        when(session.getAttribute("user")).thenReturn(user);
        when(userService.getUserGraph(user)).thenReturn(graph);

        ResponseEntity<?> response = userController.getUserGraph(session);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(graph, response.getBody());
    }

    @Test
    void getUserNotifications_UserNotLoggedIn() {
        when(session.getAttribute("user")).thenReturn(null);

        ResponseEntity<?> response = userController.getUserNotifications(session);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertInstanceOf(RedirectView.class, response.getBody());
    }

    @Test
    void getUserNotifications_UserLoggedIn() {
        User user = new User();
        List<User> notifications = Collections.singletonList(new User());
        when(session.getAttribute("user")).thenReturn(user);
        when(connectionService.getPendingConnections(user)).thenReturn(notifications);

        ResponseEntity<?> response = userController.getUserNotifications(session);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(notifications, response.getBody());
    }

    @Test
    void acceptConnection_UserNotLoggedIn() {
        when(session.getAttribute("user")).thenReturn(null);

        ResponseEntity<?> response = userController.acceptConnection(session, 1L);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertInstanceOf(RedirectView.class, response.getBody());
    }

    @Test
    void acceptConnection_UserLoggedIn() {
        User user = new User();
        when(session.getAttribute("user")).thenReturn(user);

        ResponseEntity<?> response = userController.acceptConnection(session, 1L);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Connection accepted", response.getBody());
        verify(connectionService, times(1)).acceptConnection(user, 1L);
    }

    @Test
    void profile_UserNotLoggedIn() {
        when(session.getAttribute("user")).thenReturn(null);

        String viewName = userController.profile(session, model);
        assertEquals("redirect:/login", viewName);
    }

    @Test
    void profile_UserLoggedIn() {
        User user = new User();
        when(session.getAttribute("user")).thenReturn(user);

        String viewName = userController.profile(session, model);
        assertEquals("profile", viewName);
        verify(model, times(1)).addAttribute("user", user);
    }

    @Test
    void userPage_UserNotLoggedIn() {
        when(session.getAttribute("user")).thenReturn(null);

        String viewName = userController.userPage(1L, model, session);
        assertEquals("redirect:/login", viewName);
    }

    @Test
    void userPage_UserLoggedIn() {
        User sessionUser = new User();
        User user = new User();
        String likedUser = "liked";
        when(session.getAttribute("user")).thenReturn(sessionUser);
        when(userService.findById(1L)).thenReturn(user);
        when(connectionService.isUserLikedBySessionUser(user, sessionUser)).thenReturn(likedUser);

        String viewName = userController.userPage(1L, model, session);
        assertEquals("user", viewName);
        verify(model, times(1)).addAttribute("user", user);
        verify(model, times(1)).addAttribute("likedUser", likedUser);
    }

    @Test
    void getUserInterestGraph() {
        User sessionUser = new User();
        sessionUser.setId(2L);
        sessionUser.setUsername("sessionUser");

        when(session.getAttribute("user")).thenReturn(sessionUser);

        List<Node> nodes = new ArrayList<>();
        List<Link> links = new ArrayList<>();
        Graph graph = new Graph(nodes, links);
        when(userService.getUserInterestGraph(1L, sessionUser)).thenReturn(graph);

        ResponseEntity<?> response = userController.getUserInterestGraph(1L, session);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(graph, response.getBody());
    }

    @Test
    void getUserInterestGraph_UserNotLoggedIn() {
        when(session.getAttribute("user")).thenReturn(null);

        ResponseEntity<?> response = userController.getUserInterestGraph(1L, session);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertInstanceOf(RedirectView.class, response.getBody());
    }

    @Test
    void testShowForm() {
        User sessionUser = new User();
        when(session.getAttribute("user")).thenReturn(sessionUser);

        String result = userController.showPrivacyForm(model, session);

        assertEquals("privacyForm", result);
        verify(model).addAttribute("user", sessionUser);
        verify(model).addAttribute(eq("privacyForm"), any(PrivacyForm.class));
    }

    @Test
    void testShowForm_LoginError() {
        when(session.getAttribute("user")).thenReturn(null);

        String viewName = userController.showPrivacyForm(model, session);

        assertEquals("redirect:/login", viewName);
    }

    @Test
    void testSubmitForm() {
        PrivacyForm privacyForm = new PrivacyForm();
        User sessionUser = new User();
        when(session.getAttribute("user")).thenReturn(sessionUser);

        String viewName = userController.submitPrivacyForm(privacyForm, session);

        assertEquals("redirect:/profile", viewName);
        verify(userService, times(1)).saveUserPrivacySettings(sessionUser, privacyForm);
    }

    @Test
    void testSubmitForm_LoginError() {
        PrivacyForm privacyForm = new PrivacyForm();
        when(session.getAttribute("user")).thenReturn(null);

        String viewName = userController.submitPrivacyForm(privacyForm, session);

        assertEquals("redirect:/login", viewName);
    }
}
