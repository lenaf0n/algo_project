package isep.algoproject.repositories;

import isep.algoproject.models.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
    List<Post> findPostsByUserIdOrderByCreatedAtDesc(Long userId);

    Post findPostById(Long id);
}
