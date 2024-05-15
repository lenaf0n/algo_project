package isep.algoproject.repositories;

import isep.algoproject.models.Message;
import isep.algoproject.models.User;
import isep.algoproject.models.enums.MessageCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    Message findByCategory(MessageCategory category);

    Message findById(long id);
}
