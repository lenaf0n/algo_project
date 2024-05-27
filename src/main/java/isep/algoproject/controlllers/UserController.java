package isep.algoproject.controlllers;

import isep.algoproject.models.Dtos.Graph;
import isep.algoproject.models.Dtos.PrivacyForm;
import isep.algoproject.models.Dtos.SearchResultUser;
import isep.algoproject.models.User;
import isep.algoproject.services.ConnectionService;
import isep.algoproject.services.UserService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class UserController {

    @Autowired
    private UserService userService;
    @Autowired
    private ConnectionService connectionService;

    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }

        List<SearchResultUser> recommendedUsers = userService.recommendTop5Friends(user);
        model.addAttribute("recommendedUsers", recommendedUsers);

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

    @GetMapping("/profile")
    public String profile(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        model.addAttribute("user", user);
        return "profile";
    }

    @GetMapping("/user-page/{userId}")
    public String userPage(@PathVariable long userId, Model model, HttpSession session) {
        User sessionUser = (User) session.getAttribute("user");
        if (sessionUser == null) {
            return "redirect:/login";
        }

        User user = userService.findById(userId);
        String likedUser = connectionService.isUserLikedBySessionUser(user, sessionUser);
        model.addAttribute("user", user);
        model.addAttribute("likedUser", likedUser);
        return "user";
    }

    @GetMapping("/user/interest-graph/{userId}")
    public ResponseEntity<?> getUserInterestGraph(@PathVariable long userId, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new RedirectView("/login"));
        }

        Graph graph = userService.getUserInterestGraph(userId, user);
        return ResponseEntity.ok(graph);
    }

    @GetMapping("/privacyForm")
    public String showPrivacyForm(Model model, HttpSession session) {
        User sessionUser = (User) session.getAttribute("user");
        if (sessionUser == null) {
            return "redirect:/login";
        }

        model.addAttribute("user", sessionUser);
        model.addAttribute("privacyForm", new PrivacyForm());

        File iconsFolder = new File("src/main/resources/static/images/icons");
        List<String> images = Arrays.stream(iconsFolder.listFiles())
                .map(File::getName)
                .collect(Collectors.toList());
        model.addAttribute("images", images);

        return "privacyForm";
    }

    @PostMapping("/submitPrivacyForm")
    public String submitPrivacyForm(@Valid PrivacyForm privacyForm, HttpSession session) {
        User sessionUser = (User) session.getAttribute("user");
        if (sessionUser == null) {
            return "redirect:/login";
        }

        userService.saveUserPrivacySettings(sessionUser, privacyForm);
        return "redirect:/profile";
    }

    @PostMapping("/saveProfileImage/{image}")
    public String saveProfileImage(@PathVariable String image, HttpSession session) {
        User sessionUser = (User) session.getAttribute("user");
        if (sessionUser == null) {
            return "redirect:/login";
        }

        userService.saveProfileImage(sessionUser, image);
        return "redirect:/profile";
    }

    @PostMapping("/updateBio")
    public String updateBio(@RequestParam("bio") String bio, HttpSession session) {
        User sessionUser = (User) session.getAttribute("user");
        if (sessionUser == null) {
            return "redirect:/login";
        }

        userService.saveProfileBio(sessionUser, bio);
        return "redirect:/profile";
    }
}
