package isep.algoproject.services;

import isep.algoproject.models.Group;
import isep.algoproject.models.Message;
import isep.algoproject.models.enums.MessageCategory;
import isep.algoproject.repositories.GroupRepository;
import isep.algoproject.repositories.MessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class GroupService {

    @Autowired
    private GroupRepository groupRepository;

    public Group save(Group group) {
        if(group.getId() == null){
            group.setCreatedAt(Instant.now());
        }
        return groupRepository.save(group);
    }

    public void delete(Group group) {
        groupRepository.delete(group);
    }

    public List<Group> findById(long id){return groupRepository.findById(id);}
}
