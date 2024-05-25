package isep.algoproject.services;

import isep.algoproject.models.*;
import isep.algoproject.models.Dtos.*;
import isep.algoproject.models.enums.NodeType;
import isep.algoproject.models.enums.Status;
import isep.algoproject.repositories.ConnectionRepository;
import isep.algoproject.repositories.InterestRepository;
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

    @Autowired
    private InterestRepository interestRepository;

    public User findById(long id){return userRepository.findById(id);}

    public User findByUsername(String username){return userRepository.findByUsername(username);}

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
        List<Connection> connectionsToRemove = new ArrayList<>();
        for (Connection connection : connectionsForUsers) {
            User user1 = connection.getUser1();
            User user2 = connection.getUser2();

            if (!users.contains(user1) && !usersToAdd.contains(user1) && !user1.isGraphPrivacy()) {
                usersToAdd.add(user1);
            } else if (user1.isGraphPrivacy()) {
                connectionsToRemove.add(connection);
            }
            if (!users.contains(user2) && !usersToAdd.contains(user2) && !user2.isGraphPrivacy()) {
                usersToAdd.add(user2);
            } else if (user2.isGraphPrivacy()) {
                connectionsToRemove.add(connection);
            }
        }
        connectionsForUsers.removeAll(connectionsToRemove);

        userConnections.addAll(connectionsForUsers);
        users.addAll(usersToAdd);
        Set<Connection> userConnectionsSet = new HashSet<>(userConnections);

        List<Interest> userInterests = interestRepository.findInterestByLikedByUsersIn(users);

        return createGraph(users, new ArrayList<>(userConnectionsSet), userInterests);
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

    public Graph getUserInterestGraph(long userId, User sessionUser) {
        User user = userRepository.findById(userId);

        boolean connection1 = connectionRepository.existsByUser1IdAndUser2IdAndStatus(userId, sessionUser.getId(), Status.FRIEND);
        boolean connection2 = connectionRepository.existsByUser1IdAndUser2IdAndStatus(sessionUser.getId(), userId, Status.FRIEND);

        if (!user.isInterestPrivacy() || connection1 || connection2) {
            List<Node> nodes = new ArrayList<>();
            List<Link> links = new ArrayList<>();

            List<Interest> interests = user.getLikedInterests();

            nodes.add(new Node(user.getId().toString(), user.getUsername(), NodeType.USER));
            nodes.addAll(interests.stream()
                    .map(interest -> new Node(interest.getId().toString()+"interest", interest.getName(), NodeType.INTEREST))
                    .toList());

            for (Interest interest : interests) {
                Link link = new Link(user.getId().toString(), interest.getId().toString()+"interest");
                links.add(link);
            }

            return new Graph(nodes, links);
        }
        return null;
    }

    public void saveUserPrivacySettings(User user, PrivacyForm privacyForm) {
        user.setInterestPrivacy(privacyForm.isInterestPrivacy());
        user.setGraphPrivacy(privacyForm.isGraphPrivacy());
        user.setPostPrivacy(privacyForm.isPostPrivacy());

        userRepository.save(user);
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

    private Graph createGraph(List<User> users, List<Connection> connections, List<Interest> userInterests) {
        List<Node> nodes = users.stream()
                .map(user -> new Node(user.getId().toString(), user.getName(), NodeType.USER))
                .collect(Collectors.toList());

        List<Link> links = connections.stream()
                .map(connection -> new Link(connection.getUser1().getId().toString(), connection.getUser2().getId().toString()))
                .collect(Collectors.toList());

        nodes.addAll(userInterests.stream()
                .map(interest -> new Node(interest.getId().toString()+"interest", interest.getName(), NodeType.INTEREST))
                .toList());

        for (User user : users) {
            for (Interest interest : user.getLikedInterests()) {
                Link link = new Link(user.getId().toString(), interest.getId().toString()+"interest");
                links.add(link);
            }
        }

        return new Graph(nodes, links);
    }
}
