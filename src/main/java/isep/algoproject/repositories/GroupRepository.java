package isep.algoproject.repositories;

import isep.algoproject.models.Group;
import isep.algoproject.models.Message;
import isep.algoproject.models.enums.MessageCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GroupRepository extends JpaRepository<Group, Long> {
    Group findById(long id);
}
