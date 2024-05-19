package isep.algoproject.repositories;

import isep.algoproject.models.Message;
import isep.algoproject.models.enums.MessageCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    List<Message> findByCategory(MessageCategory category);

    List<Message> findByRecipientId(long id);

    List<Message> findById(long id);
}
