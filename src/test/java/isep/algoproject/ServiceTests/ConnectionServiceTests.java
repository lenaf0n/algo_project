package isep.algoproject.ServiceTests;

import isep.algoproject.models.Connection;
import isep.algoproject.models.User;
import isep.algoproject.models.enums.Status;
import isep.algoproject.repositories.ConnectionRepository;
import isep.algoproject.repositories.UserRepository;
import isep.algoproject.services.ConnectionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ConnectionServiceTests {
    @InjectMocks
    private ConnectionService connectionService;

    @Mock
    private ConnectionRepository connectionRepository;

    @Mock
    private UserRepository userRepository;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testSaveNewConnection() {
        User sessionUser = new User();
        sessionUser.setId(1L);
        User user = new User();
        user.setId(2L);

        when(userRepository.findById(2L)).thenReturn(user);

        connectionService.saveNewConnection(sessionUser, 2L);

        verify(connectionRepository, times(1)).save(any(Connection.class));
    }

    @Test
    public void testDeleteConnection() {
        User sessionUser = new User();
        sessionUser.setId(1L);
        User user = new User();
        user.setId(2L);

        Connection connection = new Connection();
        connection.setUser1(sessionUser);
        connection.setUser2(user);
        connection.setId(1L);

        when(connectionRepository.findByUser1IdAndUser2Id(1L, 2L)).thenReturn(connection);
        when(connectionRepository.findByUser1IdAndUser2Id(2L, 1L)).thenReturn(null);

        connectionService.deleteConnection(sessionUser, 2L);

        verify(connectionRepository, times(1)).delete(connection);
    }

    @Test
    public void testGetPendingConnections() {
        User sessionUser = new User();
        sessionUser.setId(1L);

        List<Connection> pendingConnections = new ArrayList<>();
        Connection connection = new Connection();
        connection.setUser1(new User());
        connection.setStatus(Status.PENDING);
        pendingConnections.add(connection);

        when(connectionRepository.findByUser2AndPendingStatus(sessionUser)).thenReturn(pendingConnections);

        List<User> result = connectionService.getPendingConnections(sessionUser);

        assertEquals(1, result.size());
    }

    @Test
    public void testAcceptConnection() {
        User sessionUser = new User();
        sessionUser.setId(1L);
        User user = new User();
        user.setId(2L);

        Connection connection = new Connection();
        connection.setUser1(user);
        connection.setUser2(sessionUser);
        connection.setStatus(Status.PENDING);

        when(connectionRepository.findByUser1IdAndUser2Id(2L, 1L)).thenReturn(connection);

        connectionService.acceptConnection(sessionUser, 2L);

        assertEquals(Status.FRIEND, connection.getStatus());
        verify(connectionRepository, times(1)).save(connection);
    }

    @Test
    public void testAcceptConnectionThrowsExceptionWhenNotFound() {
        User sessionUser = new User();
        sessionUser.setId(1L);

        when(connectionRepository.findByUser1IdAndUser2Id(2L, 1L)).thenReturn(null);

        assertThrows(RuntimeException.class, () -> connectionService.acceptConnection(sessionUser, 2L));
    }

    @Test
    void isUserLikedBySessionUser_LikedPending() {
        User user = new User();
        user.setId(1L);

        User sessionUser = new User();
        sessionUser.setId(2L);

        Connection connection = new Connection();
        connection.setStatus(Status.PENDING);

        when(connectionRepository.findByUser1IdAndUser2Id(1L, 2L)).thenReturn(connection);

        String result = connectionService.isUserLikedBySessionUser(user, sessionUser);

        assertEquals("PENDING", result);
    }

    @Test
    void isUserLikedBySessionUser_LikedFriend() {
        User user = new User();
        user.setId(1L);

        User sessionUser = new User();
        sessionUser.setId(2L);

        Connection connection = new Connection();
        connection.setStatus(Status.FRIEND);

        when(connectionRepository.findByUser1IdAndUser2Id(1L, 2L)).thenReturn(connection);

        String result = connectionService.isUserLikedBySessionUser(user, sessionUser);

        assertEquals("FRIEND", result);
    }

    @Test
    void isUserLikedBySessionUser_NotLiked() {
        User user = new User();
        user.setId(1L);

        User sessionUser = new User();
        sessionUser.setId(2L);

        when(connectionRepository.findByUser1IdAndUser2Id(1L, 2L)).thenReturn(null);
        when(connectionRepository.findByUser1IdAndUser2Id(2L, 1L)).thenReturn(null);

        String result = connectionService.isUserLikedBySessionUser(user, sessionUser);

        assertEquals("NONE", result);
    }
}
