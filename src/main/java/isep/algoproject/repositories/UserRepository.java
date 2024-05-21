package isep.algoproject.repositories;

import isep.algoproject.models.Post;
import isep.algoproject.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Boolean existsByUsername(String username);

    Boolean existsByEmail(String email);

    User findByUsername(String username);

    User findById(long id);

    List<User> findByUsernameContaining(String username);
}
