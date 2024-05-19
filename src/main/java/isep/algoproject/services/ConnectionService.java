package isep.algoproject.services;

import isep.algoproject.models.Connection;
import isep.algoproject.models.User;
import isep.algoproject.models.enums.Status;
import isep.algoproject.repositories.ConnectionRepository;
import isep.algoproject.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ConnectionService {
    @Autowired
    private ConnectionRepository connectionRepository;

    @Autowired
    private UserRepository userRepository;

    public void saveNewConnection(User sessionUser, long userId) {
        Connection connection = new Connection();
        connection.setUser1(sessionUser);
        connection.setUser2(userRepository.findById(userId));
        connection.setStatus(Status.PENDING);

        connectionRepository.save(connection);
    }

    public void deleteConnection(User sessionUser, long userId) {
        Connection connection1 = connectionRepository.findByUser1IdAndUser2Id(sessionUser.getId(), userId);
        Connection connection2 = connectionRepository.findByUser1IdAndUser2Id(userId, sessionUser.getId());

        if (connection1 != null) {
            connectionRepository.delete(connection1);
        } else if (connection2 != null) {
            connectionRepository.delete(connection2);
        }
    }

    public List<User> getPendingConnections(User sessionUser) {
        List<Connection> pendingConnections = connectionRepository.findByUser2AndPendingStatus(sessionUser);

        List<User> users = new ArrayList<>();
        for (Connection connection : pendingConnections) {
            users.add(connection.getUser1());
        }
        return users;
    }

    public void acceptConnection(User sessionUser, long userId) {
        Connection connection = connectionRepository.findByUser1IdAndUser2Id(userId, sessionUser.getId());
        if (connection != null) {
            connection.setStatus(Status.FRIEND);
            connectionRepository.save(connection);
        } else {
            throw new RuntimeException("Connection not found");
        }
    }
}
