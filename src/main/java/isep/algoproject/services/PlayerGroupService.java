package isep.algoproject.services;

import isep.algoproject.models.Group;
import isep.algoproject.models.PlayerGroup;
import isep.algoproject.repositories.GroupRepository;
import isep.algoproject.repositories.PlayerGroupRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class PlayerGroupService {

    @Autowired
    private PlayerGroupRepository playerGroupRepository;

    public PlayerGroup save(PlayerGroup playerGroup) {
        System.out.println(playerGroup.getId());
        playerGroup.setAddAt(Instant.now());
        return playerGroupRepository.save(playerGroup);
    }

    public void delete(PlayerGroup playerGroup) {
        playerGroupRepository.delete(playerGroup);
    }

    public List<PlayerGroup> findById(long id){return playerGroupRepository.findById(id);}

    public List<PlayerGroup> findByUserId(long id){return playerGroupRepository.findByUserId(id);}

    public PlayerGroup findByUserIdAndGroupId(long userId, long groupId){return playerGroupRepository.findByUserIdAndGroupId(userId, groupId);}
}
