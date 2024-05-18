package isep.algoproject.controlllers;

import isep.algoproject.models.Graph;
import isep.algoproject.models.SearchResultUser;
import isep.algoproject.models.User;
import isep.algoproject.services.ConnectionService;
import isep.algoproject.services.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.view.RedirectView;

import java.util.List;

@Controller
public class UserController {

    @Autowired
    private UserService userService;
    @Autowired
    private ConnectionService connectionService;

    @GetMapping("/dashboard")
    public String dashboard(HttpSession session) {
        if (!checkLogin(session)) {
            return "redirect:/login";
        }
        return "dashboard";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.removeAttribute("user");
        return "redirect:/";
    }

    @GetMapping("/search/{username}")
    public ResponseEntity<?> searchByUsername(@PathVariable String username, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new RedirectView("/login"));
        }
        List<SearchResultUser> users = userService.getUsersByUsername(username, user);
        return ResponseEntity.ok(users);
    }

    @PostMapping("/like/{userId}")
    public ResponseEntity<?> likeUser(@PathVariable long userId, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new RedirectView("/login"));
        }
        connectionService.saveNewConnection(user, userId);
        return ResponseEntity.ok("Connection request sent successful");
    }

    @DeleteMapping("/unlike/{userId}")
    public ResponseEntity<?> unlikeUser(@PathVariable long userId, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new RedirectView("/login"));
        }
        connectionService.deleteConnection(user, userId);
        return ResponseEntity.ok("Connection request deleted successful");
    }

    @GetMapping("/user/graph")
    public ResponseEntity<?> getUserGraph(HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new RedirectView("/login"));
        }
        return ResponseEntity.ok(userService.getUserGraph(user));
    }

    @GetMapping("/user/notifications")
    public ResponseEntity<?> getUserNotifications(HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new RedirectView("/login"));
        }
        return ResponseEntity.ok(connectionService.getPendingConnections(user));
    }

    @PostMapping("/accept-connection/{userId}")
    public ResponseEntity<?> acceptConnection(HttpSession session, @PathVariable long userId) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new RedirectView("/login"));
        }
        connectionService.acceptConnection(user, userId);
        return ResponseEntity.ok("Connection accepted");
    }

    private boolean checkLogin(HttpSession session) {
        User user = (User) session.getAttribute("user");
        return user != null;
    }
}
