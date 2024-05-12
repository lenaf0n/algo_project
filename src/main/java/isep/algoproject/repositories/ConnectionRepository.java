package isep.algoproject.repositories;

import isep.algoproject.models.Connection;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ConnectionRepository extends JpaRepository<Connection, Long> {
    Connection findByUser1IdAndUser2Id(Long user1Id, Long user2Id);

    List<Connection> findByUser1IdOrUser2Id(Long user1Id, Long user2Id);
}
