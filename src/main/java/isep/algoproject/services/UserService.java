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

import java.util.*;
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
        List<User> users = findFriendsOfUser(user);

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

    public List<User> findFriendsOfUser(User user){
        List<Connection> userConnections = connectionRepository.findByUser1OrUser2AndFriend(user);
        List<User> users = getDistinctUsersFromConnections(userConnections);
        return users;
    }


    public List<SearchResultUser> getUsersByUsername(String username, User user) {
        List<User> users = userRepository.findByUsernameContaining(username);
        return createSearchResultUsers(users, user);
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

    public void saveProfileImage(User user, String image) {
        user.setImage(image);
        userRepository.save(user);
    }

    public void saveProfileBio(User user, String bio) {
        user.setBio(bio);
        userRepository.save(user);
    }

    public List<SearchResultUser> recommendTop5Friends(User sessionUser) {
        Set<User> visited = new HashSet<>();
        Queue<User> queue = new LinkedList<>();

        List<Connection> connections = connectionRepository.findByUser1OrUser2(sessionUser);
        List<User> friendsOfSessionUser = getDistinctUsersFromConnections(connections);
        Map<User, Integer> recommendationScores = new HashMap<>();

        queue.add(sessionUser);
        visited.add(sessionUser);

        while (!queue.isEmpty()) {
            User currentUser = queue.poll();
            List<User> friends = getFriends(currentUser);

            for (User friend : friends) {
                if (!visited.contains(friend)) {
                    visited.add(friend);
                    queue.add(friend);

                    if (!friendsOfSessionUser.contains(friend) && !friend.equals(sessionUser)) {
                        int sharedInterestCount = countSharedInterests(sessionUser, friend);
                        int mutualFriendCount = countMutualFriends(sessionUser, friend);

                        if (sharedInterestCount > 0 || mutualFriendCount > 0) {
                            int score = calculateScore(sharedInterestCount, mutualFriendCount);
                            recommendationScores.put(friend, score);
                        }
                    }
                }
            }
        }

        // Sort and get top 5 recommendations
        List<User> users =  recommendationScores.entrySet().stream()
                .sorted(Map.Entry.<User, Integer>comparingByValue().reversed())
                .limit(5)
                .map(Map.Entry::getKey)
                .toList();

        return createSearchResultUsers(users, sessionUser);
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

        users.stream()
                .flatMap(user -> user.getLikedInterests().stream()
                        .map(interest -> new Link(user.getId().toString(), interest.getId().toString() + "interest")))
                .forEach(links::add);



        return new Graph(nodes, links);
    }

    private List<User> getFriends(User user) {
        List<Connection> connections = connectionRepository.findByUser1OrUser2AndFriend(user);
        return getDistinctUsersFromConnections(connections);
    }

    private static List<User> getDistinctUsersFromConnections(List<Connection> connections) {
        Set<User> distinctUsers = new HashSet<>();

        for (Connection connection : connections) {
            distinctUsers.add(connection.getUser1());
            distinctUsers.add(connection.getUser2());
        }

        return new ArrayList<>(distinctUsers);
    }

    private int countSharedInterests(User user1, User user2) {
        List<Interest> interests1 = interestRepository.findInterestByLikedByUsers(user1);
        List<Interest> interests2 = interestRepository.findInterestByLikedByUsers(user2);

        int sharedInterestCount = 0;
        for (Interest interest : interests1) {
            if (interests2.contains(interest)) {
                sharedInterestCount++;
            }
        }

        return sharedInterestCount;
    }

    private int countMutualFriends(User user1, User user2) {
        List<User> friendsOfUser1 = getFriends(user1);
        List<User> friendsOfUser2 = getFriends(user2);

        Set<User> mutualFriends = new HashSet<>(friendsOfUser1);
        mutualFriends.retainAll(friendsOfUser2);

        return mutualFriends.size();
    }

    private int calculateScore(int sharedInterestCount, int mutualFriendCount) {
        return (sharedInterestCount * 2) + mutualFriendCount; // Weight shared interests more
    }

    private List<SearchResultUser> createSearchResultUsers(List<User> users, User user) {
        List<SearchResultUser> searchResultUsers = new ArrayList<>();
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
}
