package isep.algoproject.ControllerTests;

import isep.algoproject.controlllers.HomeController;
import isep.algoproject.models.User;
import isep.algoproject.services.UserService;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.ui.Model;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class HomeControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private HttpSession session;

    @Mock
    private Model model;

    @InjectMocks
    private HomeController homeController;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testHome() {
        String viewName = homeController.home();
        assertEquals("index", viewName);
    }

    @Test
    public void testLoginGet() {
        String viewName = homeController.login(model);
        assertEquals("login", viewName);
        verify(model, times(1)).addAttribute(eq("user"), any(User.class));
    }

    @Test
    public void testLoginSubmitSuccess() {
        User user = new User();
        user.setUsername("testUser");
        user.setPassword("testPass");

        User authenticatedUser = new User();
        when(userService.authenticate("testUser", "testPass")).thenReturn(authenticatedUser);

        String viewName = homeController.loginSubmit(user, session, model);
        assertEquals("redirect:/dashboard", viewName);
        verify(session, times(1)).setAttribute("user", authenticatedUser);
    }

    @Test
    public void testLoginSubmitFailure() {
        User user = new User();
        user.setUsername("testUser");
        user.setPassword("testPass");

        when(userService.authenticate("testUser", "testPass")).thenReturn(null);

        String viewName = homeController.loginSubmit(user, session, model);
        assertEquals("login", viewName);
        verify(model, times(1)).addAttribute("error", "Invalid username or password");
    }

    @Test
    public void testSignupGet() {
        String viewName = homeController.signup(model);
        assertEquals("signup", viewName);
        verify(model, times(1)).addAttribute(eq("user"), any(User.class));
    }

    @Test
    public void testSignupSubmitSuccess() {
        User user = new User();
        user.setUsername("newUser");
        user.setEmail("newUser@example.com");

        when(userService.isUsernameUnique("newUser")).thenReturn(true);
        when(userService.isEmailUnique("newUser@example.com")).thenReturn(true);

        String viewName = homeController.signUp(user, model);
        assertEquals("redirect:/login", viewName);
        verify(userService, times(1)).saveNewUser(user);
    }

    @Test
    public void testSignupSubmitUsernameTaken() {
        User user = new User();
        user.setUsername("takenUser");
        user.setEmail("newUser@example.com");

        when(userService.isUsernameUnique("takenUser")).thenReturn(false);
        when(userService.isEmailUnique("newUser@example.com")).thenReturn(true);

        String viewName = homeController.signUp(user, model);
        assertEquals("signup", viewName);
        verify(model, times(1)).addAttribute("usernameError", "Username is already taken!");
    }

    @Test
    public void testSignupSubmitEmailTaken() {
        User user = new User();
        user.setUsername("newUser");
        user.setEmail("taken@example.com");

        when(userService.isUsernameUnique("newUser")).thenReturn(true);
        when(userService.isEmailUnique("taken@example.com")).thenReturn(false);

        String viewName = homeController.signUp(user, model);
        assertEquals("signup", viewName);
        verify(model, times(1)).addAttribute("emailError", "Email is already taken!");
    }

    @Test
    public void testSignupSubmitBothTaken() {
        User user = new User();
        user.setUsername("takenUser");
        user.setEmail("taken@example.com");

        when(userService.isUsernameUnique("takenUser")).thenReturn(false);
        when(userService.isEmailUnique("taken@example.com")).thenReturn(false);

        String viewName = homeController.signUp(user, model);
        assertEquals("signup", viewName);
        verify(model, times(1)).addAttribute("usernameError", "Username is already taken!");
        verify(model, times(1)).addAttribute("emailError", "Email is already taken!");
    }
}
