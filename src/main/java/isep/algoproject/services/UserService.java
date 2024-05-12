package isep.algoproject.services;

import isep.algoproject.models.Connection;
import isep.algoproject.models.SearchResultUser;
import isep.algoproject.models.User;
import isep.algoproject.models.enums.Status;
import isep.algoproject.repositories.ConnectionRepository;
import isep.algoproject.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

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

    public void saveNewConnection(User sessionUser, long userId) {
        Connection connection = new Connection();
        connection.setUser1(sessionUser);
        connection.setUser2(userRepository.findById(userId));
        connection.setStatus(Status.FRIEND);

        connectionRepository.save(connection);
    }

    public void deleteConnection(User sessionUser, long userId) {
        Connection connection1 = connectionRepository.findByUser1IdAndUser2Id(sessionUser.getId(), userId);
        Connection connection2 = connectionRepository.findByUser1IdAndUser2Id(sessionUser.getId(), userId);

        if (connection1 != null) {
            connectionRepository.delete(connection1);
        } else if (connection2 != null) {
            connectionRepository.delete(connection2);
        }
    }

    public User authenticate(String username, String password) {
        User user = userRepository.findByUsername(username);
        if (passwordEncoder.matches(password, user.getPassword())) {
            return user;
        }
        return null;
    }

    public List<SearchResultUser> getUsersByUsername(String username, User user) {
        List<SearchResultUser> searchResultUsers = new ArrayList<>();
        List<User> users = userRepository.findByUsernameContaining(username);

        for (User foundUser : users) {
            if (!foundUser.equals(user)) {
                SearchResultUser searchResultUser = new SearchResultUser();
                searchResultUser.setUser(foundUser);

                Status status = getConnectionStatus(user, foundUser);
                searchResultUser.setFriend(status == Status.FRIEND);

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

}
