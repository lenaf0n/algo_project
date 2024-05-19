package isep.algoproject.services;

import isep.algoproject.models.Message;
import isep.algoproject.models.enums.MessageCategory;
import isep.algoproject.repositories.MessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class MessageService {

    @Autowired
    private MessageRepository messageRepository;

    public Message save(Message message) {
        message.setCreatedAt(Instant.now());
        message.setRead(false);
        return messageRepository.save(message);
    }

    public void delete(Message message) {
        messageRepository.delete(message);
    }

    public List<Message> findByRecipientId(long id){
        List<Message> messageList =  messageRepository.findByRecipientId(id);
        return messageRepository.findByRecipientId(id);
    }

    public List<Message> findByMessageCategory(MessageCategory messageCategory){return messageRepository.findByCategory(messageCategory);}
}
