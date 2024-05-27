package isep.algoproject.repositories;

import isep.algoproject.models.Interest;
import isep.algoproject.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InterestRepository extends JpaRepository<Interest, Long> {
    boolean existsByName(String name);

    List<Interest> findInterestByLikedByUsersIn(List<User> users);

    List<Interest> findInterestByLikedByUsers(User user);

    List<Interest> findByNameContaining(String name);

    Interest findById(long id);
}
