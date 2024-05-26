package isep.algoproject.repositories;

import isep.algoproject.models.Group;
import isep.algoproject.models.PlayerGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlayerGroupRepository extends JpaRepository<PlayerGroup, Long> {
    List<PlayerGroup> findById(long id);
    List<PlayerGroup> findByGroupId(long id);
    PlayerGroup findByUserIdAndGroupId(long userId,long groupId);

    List<PlayerGroup> findByUserId(long id);
}
