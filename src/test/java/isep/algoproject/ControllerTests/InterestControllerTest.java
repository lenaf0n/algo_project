package isep.algoproject.ControllerTests;

import isep.algoproject.controlllers.InterestController;
import isep.algoproject.models.Dtos.Graph;
import isep.algoproject.models.Dtos.Link;
import isep.algoproject.models.Dtos.Node;
import isep.algoproject.models.Dtos.SearchResultInterest;
import isep.algoproject.models.Interest;
import isep.algoproject.models.User;
import isep.algoproject.services.InterestService;
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

public class InterestControllerTest {
    @Mock
    private InterestService interestService;

    @Mock
    private HttpSession session;

    @Mock
    private Model model;

    @InjectMocks
    private InterestController interestController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void showCreateInterestForm() {
        String viewName = interestController.showCreateInterestForm(model);
        assertEquals("createInterest", viewName);
        verify(model, times(1)).addAttribute(eq("interest"), any(Interest.class));
    }

    @Test
    void createInterest_UserNotLoggedIn() {
        when(session.getAttribute("user")).thenReturn(null);

        String viewName = interestController.createInterest(new Interest(), model, session);
        assertEquals("/login", viewName);
    }

    @Test
    void createInterest_Success() {
        User user = new User();
        when(session.getAttribute("user")).thenReturn(user);
        when(interestService.createInterest(any(Interest.class), eq(user))).thenReturn(true);

        String viewName = interestController.createInterest(new Interest(), model, session);
        assertEquals("redirect:/dashboard", viewName);
    }

    @Test
    void createInterest_Failure() {
        User user = new User();
        when(session.getAttribute("user")).thenReturn(user);
        when(interestService.createInterest(any(Interest.class), eq(user))).thenReturn(false);

        String viewName = interestController.createInterest(new Interest(), model, session);
        assertEquals("createInterest", viewName);
        verify(model, times(1)).addAttribute("error", "Interest by that name already exists");
    }

    @Test
    void searchInterestByName_UserNotLoggedIn() {
        when(session.getAttribute("user")).thenReturn(null);

        ResponseEntity<?> response = interestController.searchInterestByName("test", session);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertInstanceOf(RedirectView.class, response.getBody());
    }

    @Test
    void searchInterestByName_Success() {
        User user = new User();
        when(session.getAttribute("user")).thenReturn(user);
        List<SearchResultInterest> searchResults = Collections.singletonList(new SearchResultInterest());
        when(interestService.getInterestsByName("test", user)).thenReturn(searchResults);

        ResponseEntity<?> response = interestController.searchInterestByName("test", session);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(searchResults, response.getBody());
    }

    @Test
    void likeInterest_UserNotLoggedIn() {
        when(session.getAttribute("user")).thenReturn(null);

        ResponseEntity<?> response = interestController.likeInterest(1L, session);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertInstanceOf(RedirectView.class, response.getBody());
    }

    @Test
    void likeInterest_Success() {
        User user = new User();
        when(session.getAttribute("user")).thenReturn(user);

        ResponseEntity<?> response = interestController.likeInterest(1L, session);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Successfully added interest", response.getBody());
        verify(interestService, times(1)).userAddInterest(1L, user);
    }

    @Test
    void removeInterest_UserNotLoggedIn() {
        when(session.getAttribute("user")).thenReturn(null);

        ResponseEntity<?> response = interestController.removeInterest(1L, session);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertInstanceOf(RedirectView.class, response.getBody());
    }

    @Test
    void removeInterest_Success() {
        User user = new User();
        when(session.getAttribute("user")).thenReturn(user);

        ResponseEntity<?> response = interestController.removeInterest(1L, session);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Successfully removed interest", response.getBody());
        verify(interestService, times(1)).userRemoveInterest(1L, user);
    }

    @Test
    void showInterest_UserNotLoggedIn() {
        when(session.getAttribute("user")).thenReturn(null);

        String viewName = interestController.showInterest(1L, model, session);
        assertEquals("redirect:/login", viewName);
    }

    @Test
    void showInterest_InterestNotFound() {
        User user = new User();
        when(session.getAttribute("user")).thenReturn(user);
        when(interestService.getInterestById(1L)).thenReturn(null);

        String viewName = interestController.showInterest(1L, model, session);
        assertEquals("redirect:/dashboard", viewName);
    }

    @Test
    void showInterest_Success() {
        User user = new User();
        user.setUsername("testuser");
        when(session.getAttribute("user")).thenReturn(user);

        Interest interest = new Interest();
        interest.setLikedByUsers(Collections.singletonList(user));
        when(interestService.getInterestById(1L)).thenReturn(interest);

        String viewName = interestController.showInterest(1L, model, session);
        assertEquals("interest", viewName);
        verify(model, times(1)).addAttribute("interest", interest);
        verify(model, times(1)).addAttribute("isInterest", true);
    }

    @Test
    void getInterestGraph_UserNotLoggedIn() {
        when(session.getAttribute("interest")).thenReturn(null);

        ResponseEntity<?> response = interestController.getInterestGraph(session);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertInstanceOf(RedirectView.class, response.getBody());
    }

    @Test
    void getInterestGraph_Success() {
        Interest interest = new Interest();
        User user = new User();
        user.setId(2L);
        when(session.getAttribute("interest")).thenReturn(interest);
        when(session.getAttribute("user")).thenReturn(user);

        List<Node> nodes = new ArrayList<>();
        List<Link> links = new ArrayList<>();
        Graph graph = new Graph(nodes, links);
        when(interestService.getGraphInterest(interest, user)).thenReturn(graph);

        ResponseEntity<?> response = interestController.getInterestGraph(session);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(graph, response.getBody());
    }
}
