package isep.algoproject.services;

import isep.algoproject.models.*;
import isep.algoproject.models.enums.NodeType;
import isep.algoproject.models.enums.Status;
import isep.algoproject.repositories.ConnectionRepository;
import isep.algoproject.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ConnectionRepository connectionRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public boolean isUsernameUnique(String username) {
        return !userRepository.existsByUsername(username);
    }

    public boolean isEmailUnique(String email) {
        return !userRepository.existsByEmail(email);
    }

    public void saveNewUser(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);
    }

    public User authenticate(String username, String password) {
        User user = userRepository.findByUsername(username);
        if (passwordEncoder.matches(password, user.getPassword())) {
            return user;
        }
        return null;
    }

    public Graph getUserGraph(User user) {
        List<Connection> userConnections = connectionRepository.findByUser1OrUser2AndFriend(user);
        List<User> users = getDistinctUsersFromConnections(userConnections);

        List<User> usersToAdd = new ArrayList<>();
        List<Connection> connectionsForUsers = connectionRepository.findByUser1InOrUser2InAndStatus(users, users, Status.FRIEND);
        for (Connection connection : connectionsForUsers) {
            User user1 = connection.getUser1();
            User user2 = connection.getUser2();

            if (!users.contains(user1) && !usersToAdd.contains(user1)) {
                usersToAdd.add(user1);
            }
            if (!users.contains(user2) && !usersToAdd.contains(user2)) {
                usersToAdd.add(user2);
            }
        }

        userConnections.addAll(connectionsForUsers);
        users.addAll(usersToAdd);
        Set<Connection> userConnectionsSet = new HashSet<>(userConnections);

        return createGraph(users, new ArrayList<>(userConnectionsSet));
    }


    public List<SearchResultUser> getUsersByUsername(String username, User user) {
        List<SearchResultUser> searchResultUsers = new ArrayList<>();
        List<User> users = userRepository.findByUsernameContaining(username);

        for (User foundUser : users) {
            if (!foundUser.equals(user)) {
                SearchResultUser searchResultUser = new SearchResultUser();
                searchResultUser.setUser(foundUser);

                Status status = getConnectionStatus(user, foundUser);
                searchResultUser.setStatus(status.toString());

                searchResultUsers.add(searchResultUser);
            }
        }

        return searchResultUsers;
    }

    private Status getConnectionStatus(User user1, User user2) {
        Connection connection1 = connectionRepository.findByUser1IdAndUser2Id(user1.getId(), user2.getId());
        Connection connection2 = connectionRepository.findByUser1IdAndUser2Id(user2.getId(), user1.getId());

        // If a connection exists in either direction, return its status, otherwise return NONE
        if (connection1 != null) {
            return connection1.getStatus();
        } else if (connection2 != null) {
            return connection2.getStatus();
        } else {
            return Status.NONE;
        }
    }

    private static List<User> getDistinctUsersFromConnections(List<Connection> connections) {
        Set<User> distinctUsers = new HashSet<>();

        for (Connection connection : connections) {
            distinctUsers.add(connection.getUser1());
            distinctUsers.add(connection.getUser2());
        }

        return new ArrayList<>(distinctUsers);
    }

    private Graph createGraph(List<User> users, List<Connection> connections) {
        List<Node> userNodes = users.stream()
                .map(user -> new Node(user.getId().toString(), user.getName(), NodeType.USER))
                .collect(Collectors.toList());

        List<Link> userLinks = connections.stream()
                .map(connection -> new Link(connection.getUser1().getId().toString(), connection.getUser2().getId().toString()))
                .collect(Collectors.toList());

        return new Graph(userNodes, userLinks);
    }
}
