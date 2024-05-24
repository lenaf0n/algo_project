package isep.algoproject.ServiceTests;

import isep.algoproject.models.*;
import isep.algoproject.models.Dtos.Graph;
import isep.algoproject.models.Dtos.Node;
import isep.algoproject.models.Dtos.SearchResultUser;
import isep.algoproject.models.enums.Status;
import isep.algoproject.repositories.ConnectionRepository;
import isep.algoproject.repositories.InterestRepository;
import isep.algoproject.repositories.UserRepository;
import isep.algoproject.services.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class UserServiceTests {
    @InjectMocks
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ConnectionRepository connectionRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private InterestRepository interestRepository;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testFindById() {
        User user = new User();
        user.setId(1L);

        when(userRepository.findById(1L)).thenReturn(user);

        User result = userService.findById(1L);

        assertEquals(user, result);
    }

    @Test
    public void testFindByUsername() {
        User user = new User();
        user.setUsername("john_doe");

        when(userRepository.findByUsername("john_doe")).thenReturn(user);

        User result = userService.findByUsername("john_doe");

        assertEquals(user, result);
    }

    @Test
    public void testIsUsernameUnique() {
        when(userRepository.existsByUsername("john_doe")).thenReturn(false);

        boolean result = userService.isUsernameUnique("john_doe");

        assertTrue(result);
    }

    @Test
    public void testIsEmailUnique() {
        when(userRepository.existsByEmail("john@example.com")).thenReturn(false);

        boolean result = userService.isEmailUnique("john@example.com");

        assertTrue(result);
    }

    @Test
    public void testSaveNewUser() {
        User user = new User();
        user.setPassword("password");

        when(passwordEncoder.encode("password")).thenReturn("encoded_password");

        userService.saveNewUser(user);

        assertEquals("encoded_password", user.getPassword());
        verify(userRepository, times(1)).save(user);
    }

    @Test
    public void testAuthenticateSuccess() {
        User user = new User();
        user.setUsername("john_doe");
        user.setPassword("encoded_password");

        when(userRepository.findByUsername("john_doe")).thenReturn(user);
        when(passwordEncoder.matches("password", "encoded_password")).thenReturn(true);

        User result = userService.authenticate("john_doe", "password");

        assertEquals(user, result);
    }

    @Test
    public void testAuthenticateFail() {
        User user = new User();
        user.setUsername("john_doe");
        user.setPassword("encoded_password");

        when(userRepository.findByUsername("john_doe")).thenReturn(user);
        when(passwordEncoder.matches("password", "encoded_password")).thenReturn(false);

        User result = userService.authenticate("john_doe", "password");

        assertNull(result);
    }

    @Test
    public void testGetUserGraph() {
        User user = new User();
        user.setId(1L);
        user.setUsername("john_doe");

        User user2 = new User();
        user2.setId(2L);
        user2.setUsername("mike_johnson");

        List<Connection> connections = new ArrayList<>();
        Connection connection = new Connection();
        connection.setUser1(user);
        connection.setUser2(user2);
        connection.setStatus(Status.FRIEND);
        connections.add(connection);

        List<User> users = new ArrayList<>();
        users.add(user);

        List<Interest> interests = new ArrayList<>();
        Interest interest = new Interest();
        interest.setId(1L);
        interest.setName("Music");
        interests.add(interest);

        when(connectionRepository.findByUser1OrUser2AndFriend(user)).thenReturn(connections);
        when(connectionRepository.findByUser1InOrUser2InAndStatus(users, users, Status.FRIEND)).thenReturn(connections);
        when(interestRepository.findInterestByLikedByUsersIn(users)).thenReturn(interests);

        Graph result = userService.getUserGraph(user);

        assertNotNull(result);
        assertEquals(2, result.getNodes().size());
        assertEquals(1, result.getLinks().size());
    }

    @Test
    public void testGetUsersByUsername() {
        User user = new User();
        user.setUsername("john_doe");
        User foundUser = new User();
        foundUser.setUsername("jane_doe");

        List<User> users = new ArrayList<>();
        users.add(foundUser);

        when(userRepository.findByUsernameContaining("jane")).thenReturn(users);
        when(connectionRepository.findByUser1IdAndUser2Id(user.getId(), foundUser.getId())).thenReturn(null);
        when(connectionRepository.findByUser1IdAndUser2Id(foundUser.getId(), user.getId())).thenReturn(null);

        List<SearchResultUser> results = userService.getUsersByUsername("jane", user);

        assertEquals(1, results.size());
        assertEquals("NONE", results.get(0).getStatus());
    }

    @Test
    void getUserInterestGraph_UserHasInterests() {
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");

        Interest interest1 = new Interest();
        interest1.setId(2L);
        interest1.setName("Interest1");

        Interest interest2 = new Interest();
        interest2.setId(3L);
        interest2.setName("Interest2");

        List<Interest> interests = Arrays.asList(interest1, interest2);
        user.setLikedInterests(interests);

        when(userRepository.findById(1L)).thenReturn(user);

        Graph graph = userService.getUserInterestGraph(1L);

        assertNotNull(graph);
        assertEquals(3, graph.getNodes().size());
        assertEquals(2, graph.getLinks().size());

        List<String> nodeIds = graph.getNodes().stream().map(Node::getId).toList();
        assertTrue(nodeIds.contains("1"));
        assertTrue(nodeIds.contains("2interest"));
        assertTrue(nodeIds.contains("3interest"));

        List<String> linkIds = graph.getLinks().stream().map(link -> link.getSource() + "-" + link.getTarget()).toList();
        assertTrue(linkIds.contains("1-2interest"));
        assertTrue(linkIds.contains("1-3interest"));
    }

    @Test
    void getUserInterestGraph_UserHasNoInterests() {
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setLikedInterests(List.of());

        when(userRepository.findById(1L)).thenReturn(user);

        Graph graph = userService.getUserInterestGraph(1L);

        assertNotNull(graph);
        assertEquals(1, graph.getNodes().size());
        assertEquals(0, graph.getLinks().size());

        List<String> nodeIds = graph.getNodes().stream().map(Node::getId).toList();
        assertTrue(nodeIds.contains("1"));
    }

    @Test
    void getUserInterestGraph_UserNotFound() {
        when(userRepository.findById(1L)).thenReturn(null);

        assertThrows(NullPointerException.class, () -> userService.getUserInterestGraph(1L));
    }
}
