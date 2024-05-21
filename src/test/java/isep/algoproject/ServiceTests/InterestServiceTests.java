package isep.algoproject.ServiceTests;

import isep.algoproject.models.Dtos.Graph;
import isep.algoproject.models.Interest;
import isep.algoproject.models.Dtos.SearchResultInterest;
import isep.algoproject.models.User;
import isep.algoproject.repositories.InterestRepository;
import isep.algoproject.services.InterestService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class InterestServiceTests {
    @InjectMocks
    private InterestService interestService;

    @Mock
    private InterestRepository interestRepository;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testCreateInterestSuccess() {
        Interest interest = new Interest();
        interest.setName("Music");
        User user = new User();

        when(interestRepository.existsByName("Music")).thenReturn(false);

        boolean result = interestService.createInterest(interest, user);

        assertTrue(result);
        verify(interestRepository, times(1)).save(interest);
    }

    @Test
    public void testCreateInterestAlreadyExists() {
        Interest interest = new Interest();
        interest.setName("Music");
        User user = new User();

        when(interestRepository.existsByName("Music")).thenReturn(true);

        boolean result = interestService.createInterest(interest, user);

        assertFalse(result);
        verify(interestRepository, times(0)).save(interest);
    }

    @Test
    public void testGetInterestsByName() {
        User user = new User();
        user.setUsername("john_doe");
        Interest interest = new Interest();
        interest.setName("Music");
        interest.setLikedByUsers(new ArrayList<>());
        interest.getLikedByUsers().add(user);

        List<Interest> interests = new ArrayList<>();
        interests.add(interest);

        when(interestRepository.findByNameContaining("Music")).thenReturn(interests);

        List<SearchResultInterest> results = interestService.getInterestsByName("Music", user);

        assertEquals(1, results.size());
        assertTrue(results.get(0).isLiked());
    }

    @Test
    public void testUserAddInterest() {
        User user = new User();
        Interest interest = new Interest();
        interest.setLikedByUsers(new ArrayList<>());

        when(interestRepository.findById(1L)).thenReturn(interest);

        interestService.userAddInterest(1L, user);

        assertTrue(interest.getLikedByUsers().contains(user));
        verify(interestRepository, times(1)).save(interest);
    }

    @Test
    public void testUserRemoveInterest() {
        User user = new User();
        user.setId(1L);
        Interest interest = new Interest();
        List<User> likedByUsers = new ArrayList<>();
        likedByUsers.add(user);
        interest.setLikedByUsers(likedByUsers);

        when(interestRepository.findById(1L)).thenReturn(interest);

        interestService.userRemoveInterest(1L, user);

        assertFalse(interest.getLikedByUsers().contains(user));
        verify(interestRepository, times(1)).save(interest);
    }

    @Test
    public void testGetInterestById() {
        Interest interest = new Interest();
        interest.setId(1L);

        when(interestRepository.findById(1L)).thenReturn(interest);

        Interest result = interestService.getInterestById(1L);

        assertEquals(interest, result);
    }

    @Test
    public void testGetGraphInterest() {
        Interest interest = new Interest();
        interest.setId(1L);
        interest.setName("Music");
        User user = new User();
        user.setId(1L);
        user.setUsername("john_doe");
        List<User> likedByUsers = new ArrayList<>();
        likedByUsers.add(user);
        interest.setLikedByUsers(likedByUsers);

        Graph graph = interestService.getGraphInterest(interest);

        assertEquals(2, graph.getNodes().size());
        assertEquals(1, graph.getLinks().size());
        assertEquals("Music", graph.getNodes().get(0).getName());
    }
}
