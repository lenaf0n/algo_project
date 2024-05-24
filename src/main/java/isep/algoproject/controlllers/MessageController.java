package isep.algoproject.controlllers;

import isep.algoproject.models.Group;
import isep.algoproject.models.Message;
import isep.algoproject.models.User;
import isep.algoproject.models.enums.MessageCategory;
import isep.algoproject.services.GroupService;
import isep.algoproject.services.MessageService;
import isep.algoproject.services.UserService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class MessageController {

    @Autowired
    private MessageService messageService;
    @Autowired
    private UserService userService;
    @Autowired
    private GroupService groupService;

    @GetMapping("/message")
    public String dashboard(Model model, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if(user == null){
            return "redirect:/login";
        }

        List<Message> messageList = messageService.findByMessageCategory(MessageCategory.GLOBAL);
        Map<Message, User> messageUserMap = new HashMap<>();

        for (Message message: messageList) {
            User authorOfMessage = userService.findById(message.getSendUserId());
            messageUserMap.put(message, authorOfMessage);
        }


        model.addAttribute("user", user);
        model.addAttribute("messageUserMap", messageUserMap);
        model.addAttribute("newMessage", new Message());
        return "/message";
    }

    @PostMapping("/message/postNewMessage")
    public String postNewMessage(@Valid Message newMessage, HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        newMessage.setCategory(MessageCategory.GLOBAL);
        newMessage.setSendUserId(user.getId());
        newMessage.setRecipientId(0);
        messageService.save(newMessage);

        dashboard(model, session);

        return "redirect:/message";
    }
}
