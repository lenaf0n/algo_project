package isep.algoproject.services;

import isep.algoproject.models.Message;
import isep.algoproject.models.enums.MessageCategory;
import isep.algoproject.repositories.MessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class MessageService {

    @Autowired
    private MessageRepository messageRepository;
    @Autowired
    private ImageService imageService;

    public Message save(Message message, MultipartFile file) throws FileNotFoundException {
        message.setCreatedAt(Instant.now());
        message.setRead(false);
        System.out.println("Save Message, File = " + file);
        if(file != null){
            String fileName = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));
            if(fileName.contains("..")) {
                throw new FileNotFoundException();
            }
            try {
                byte[] compressedFile = imageService.compressImage(file);
                message.setImage(Base64.getEncoder().encodeToString(compressedFile));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return messageRepository.save(message);
    }

    public void delete(Message message) {
        messageRepository.delete(message);
    }

    public List<Message> findByRecipientIdAndCategory(long id, MessageCategory messageCategory){
        return messageRepository.findByRecipientIdAndCategoryOrderByCreatedAt(id, messageCategory);
    }

    public List<Message> findByMessageCategory(MessageCategory messageCategory){return messageRepository.findByCategoryOrderByCreatedAt(messageCategory);}
}
