package isep.algoproject.services;

import isep.algoproject.models.*;
import isep.algoproject.models.Dtos.Graph;
import isep.algoproject.models.Dtos.Link;
import isep.algoproject.models.Dtos.Node;
import isep.algoproject.models.Dtos.SearchResultInterest;
import isep.algoproject.models.enums.NodeType;
import isep.algoproject.models.enums.Status;
import isep.algoproject.repositories.ConnectionRepository;
import isep.algoproject.repositories.InterestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class InterestService {
    @Autowired
    private InterestRepository interestRepository;

    @Autowired
    private ConnectionRepository connectionRepository;

    public boolean createInterest(Interest interest, User user) {
        if (interestRepository.existsByName(interest.getName())) {
            return false;
        }
        likeInterest(interest, user);
        return true;
    }

    public List<SearchResultInterest> getInterestsByName(String name, User user) {
        List<Interest> interests = interestRepository.findByNameContaining(name);
        List<SearchResultInterest> results = new ArrayList<>();

        for (Interest interest : interests) {
            SearchResultInterest result = new SearchResultInterest();
            result.setInterest(interest);
            boolean userExists = interest.getLikedByUsers().stream()
                    .anyMatch(u -> u.getUsername().equals(user.getUsername()));
            result.setLiked(userExists);
            results.add(result);
        }

        return results;
    }

    public void userAddInterest(long interestId, User user) {
        Interest interest = interestRepository.findById(interestId);
        likeInterest(interest, user);
    }

    public void userRemoveInterest(long interestId, User user) {
        Interest interest = interestRepository.findById(interestId);
        removeInterest(interest, user);
    }

    public Interest getInterestById(long interestId) {
        return interestRepository.findById(interestId);
    }

    public Graph getGraphInterest(Interest interest, User sessionUser) {
        List<Node> nodes = new ArrayList<>();
        List<Link> links = new ArrayList<>();

        nodes.add(new Node(interest.getId().toString(), interest.getName(), NodeType.INTEREST));

        List<User> interestUsers = interest.getLikedByUsers();
        List<User> userstoRemove = new ArrayList<>();
        for (User user : interestUsers) {
            if (user.isGraphPrivacy()) {
                boolean connection1 = connectionRepository.existsByUser1IdAndUser2IdAndStatus(user.getId(), sessionUser.getId(), Status.FRIEND);
                boolean connection2 = connectionRepository.existsByUser1IdAndUser2IdAndStatus(sessionUser.getId(), user.getId(), Status.FRIEND);

                if (!connection1 || !connection2) {
                    userstoRemove.add(user);
                }
            }
        }
        interestUsers.removeAll(userstoRemove);

        for (User user : interestUsers) {
            nodes.add(new Node(user.getId().toString()+"user", user.getUsername(), NodeType.USER));
            links.add(new Link(user.getId().toString()+"user", interest.getId().toString()));
        }

        return new Graph(nodes, links);
    }

    private void likeInterest(Interest interest, User user) {
        if (!interest.getLikedByUsers().contains(user)) {
            interest.getLikedByUsers().add(user);
            interestRepository.save(interest);
        }
    }

    private void removeInterest(Interest interest, User user) {
        interest.getLikedByUsers().removeIf(u -> u.getId().equals(user.getId()));
        interestRepository.save(interest);
    }
}