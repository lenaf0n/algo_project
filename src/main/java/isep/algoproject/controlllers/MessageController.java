package isep.algoproject.controlllers;

import isep.algoproject.models.Group;
import isep.algoproject.models.Message;
import isep.algoproject.models.PlayerGroup;
import isep.algoproject.models.User;
import isep.algoproject.models.enums.MessageCategory;
import isep.algoproject.services.GroupService;
import isep.algoproject.services.MessageService;
import isep.algoproject.services.PlayerGroupService;
import isep.algoproject.services.UserService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
public class MessageController {

    @Autowired
    private MessageService messageService;
    @Autowired
    private UserService userService;
    @Autowired
    private GroupService groupService;
    @Autowired
    private PlayerGroupService playerGroupService;

    @GetMapping("/message")
    public String dashboard(Model model, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if(user == null){
            return "redirect:/login";
        }
        model.addAttribute("user", user);
        model.addAttribute("newMessage", new Message());

        return "/message";
    }

    @PostMapping("/message/sendNewMessage")
    public String postNewMessage(@RequestParam(value = "content") String content,
                                 @RequestParam(value =  "imageData", required = false) MultipartFile imageData,
                                 @RequestParam(value = "recipientId") long recipientId,
                                 @RequestParam(value = "category") MessageCategory category,
                                 HttpSession session, Model model) throws FileNotFoundException {
        User user = (User) session.getAttribute("user");
        Message newMessage = new Message();

        newMessage.setSendUser(user);
        newMessage.setRecipientId(recipientId);
        newMessage.setCategory(category);
        newMessage.setContent(content);

        System.out.println("imageData = " + imageData);
        messageService.save(newMessage, imageData);
        System.out.println("Send New Message : " + newMessage.getId());

        List<Message> messageList = messageService.findByRecipientIdAndCategory(newMessage.getRecipientId(),newMessage.getCategory());
        model.addAttribute("messageList", messageList);
        dashboard(model,session);

        return "allMessages";
    }

    @GetMapping("/message/global")
    public String showGlobalMessage(Model model,HttpSession session){
        User user = (User) session.getAttribute("user");

        List<Message> messageList = messageService.findByMessageCategory(MessageCategory.GLOBAL);
        model.addAttribute("messageList", messageList);
        return "allMessages";
    }

    @GetMapping("/message/group")
    public String showGroup(Model model, HttpSession session){
        User user = (User) session.getAttribute("user");
        List<PlayerGroup> playerGroupsList = playerGroupService.findByUserId(user.getId());
        model.addAttribute("playerGroupsList", playerGroupsList);
        model.addAttribute("newGroup", new Group());
        return "allGroup";
    }

    @GetMapping("/message/group/{groupId}")
    public String showGroupMessage(@PathVariable long groupId, Model model, HttpSession session){
        User user = (User) session.getAttribute("user");
        List<Message> messageList = messageService.findByRecipientIdAndCategory(groupId,MessageCategory.GROUP);
        model.addAttribute("messageList", messageList);
        return "allMessages";
    }

    @GetMapping("/message/group-add/{groupId}")
    public String showAddUserGroup(@PathVariable long groupId, Model model, HttpSession session){
        User user = (User) session.getAttribute("user");
        Group group = groupService.findById(groupId);
        List<User> friendsList = userService.findFriendsOfUser(user);
        model.addAttribute("friendsList", friendsList);
        model.addAttribute("group", group);
        return "editGroup";
    }

    @PostMapping("/message/group-add")
    public String addUserGroup(@RequestParam(name="groupId") long groupId, @RequestParam(name="userId") long userId, Model model, HttpSession session){
        PlayerGroup playerGroup = new PlayerGroup();
        User user = userService.findById(userId);
        Group group = groupService.findById(groupId);

        playerGroup.setUser(user);
        playerGroup.setGroup(group);

        playerGroupService.save(playerGroup);
        dashboard(model, session);
        return "redirect:/message";
    }

    @GetMapping("/message/leave-group/{groupId}")
    public String leaveGroup(@PathVariable long groupId, Model model, HttpSession session){
        User user = (User) session.getAttribute("user");

        PlayerGroup playerLeaveGroup =  playerGroupService.findByUserIdAndGroupId(user.getId(), groupId);
        playerGroupService.delete(playerLeaveGroup);

        List<PlayerGroup> playerGroupsList = playerGroupService.findByUserId(user.getId());
        model.addAttribute("playerGroupsList", playerGroupsList);
        model.addAttribute("newGroup", new Group());

        return "AllGroup";
    }

    @GetMapping("/message/friend")
    public String showFriend(Model model, HttpSession session){
        User user = (User) session.getAttribute("user");

        List<User> friendsList = userService.findFriendsOfUser(user);
        model.addAttribute("friendsList", friendsList);
        return "allFriend";
    }

    @GetMapping("/message/friend/{friendId}")
    public String showFriendMessage(@PathVariable long friendId, Model model, HttpSession session){
        User user = (User) session.getAttribute("user");

        List<Message> messageList = messageService.findByRecipientIdAndCategory(friendId,MessageCategory.FRIENDS);
        model.addAttribute("messageList", messageList);
        return "allMessages";
    }

    @PostMapping("/message/createGroup")
    public String createGroup(@Valid Group newGroup, @RequestParam("imageData") MultipartFile imageData, HttpSession session, Model model) throws FileNotFoundException {
        User user = (User) session.getAttribute("user");

        newGroup.setUser(user);
        groupService.save(newGroup, imageData);
        PlayerGroup newPlayerGroup = new PlayerGroup();
        newPlayerGroup.setUser(user);
        newPlayerGroup.setGroup(newGroup);
        newPlayerGroup.setAddAt(newGroup.getCreatedAt());
        playerGroupService.save(newPlayerGroup);

        return "redirect:/message";
    }
}
