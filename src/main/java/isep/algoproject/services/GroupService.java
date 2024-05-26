package isep.algoproject.services;

import isep.algoproject.models.Group;
import isep.algoproject.models.Message;
import isep.algoproject.models.enums.MessageCategory;
import isep.algoproject.repositories.GroupRepository;
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

@Service
public class GroupService {

    @Autowired
    private GroupRepository groupRepository;
    @Autowired
    private ImageService imageService;

    public Group save(Group group, MultipartFile file) throws FileNotFoundException {
        if(group.getId() == 0){
            group.setCreatedAt(Instant.now());
        }
        String fileName = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));
        if(fileName.contains("..")) {
            throw new FileNotFoundException();
        }
        try {
            byte[] compressedFile = imageService.compressImage(file);
            group.setImage(Base64.getEncoder().encodeToString(compressedFile));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return groupRepository.save(group);
    }

    public void delete(Group group) {
        groupRepository.delete(group);
    }

    public List<Group> findById(long id){return groupRepository.findById(id);}
}
