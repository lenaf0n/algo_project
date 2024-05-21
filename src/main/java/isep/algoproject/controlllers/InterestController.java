package isep.algoproject.controlllers;

import isep.algoproject.models.Dtos.Graph;
import isep.algoproject.models.Interest;
import isep.algoproject.models.Dtos.SearchResultInterest;
import isep.algoproject.models.User;
import isep.algoproject.services.InterestService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.view.RedirectView;

import java.util.List;

@Controller
public class InterestController {
    @Autowired
    private InterestService interestService;

    @GetMapping("/interest/create")
    public String showCreateInterestForm(Model model) {
        model.addAttribute("interest", new Interest());
        return "createInterest";
    }

    @PostMapping("/interest/create")
    public String createInterest(@Valid Interest interest, Model model, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "/login";
        }

        boolean interestCreated = interestService.createInterest(interest, user);
        if (interestCreated) {
            return "redirect:/dashboard";
        }
        model.addAttribute("error", "Interest by that name already exists");
        return "createInterest";
    }

    @GetMapping("/interest/{name}")
    public ResponseEntity<?> searchInterestByName(@PathVariable String name, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new RedirectView("/login"));
        }
        List<SearchResultInterest> interests = interestService.getInterestsByName(name, user);
        return ResponseEntity.ok(interests);
    }

    @PostMapping("/interest/like/{id}")
    public ResponseEntity<?> likeInterest(@PathVariable long id, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new RedirectView("/login"));
        }
        interestService.userAddInterest(id, user);
        return ResponseEntity.ok("Successfully added interest");
    }

    @DeleteMapping("/interest/remove/{id}")
    public ResponseEntity<?> removeInterest(@PathVariable long id, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new RedirectView("/login"));
        }
        interestService.userRemoveInterest(id, user);
        return ResponseEntity.ok("Successfully removed interest");
    }

    @GetMapping("/interest-page/{id}")
    public String showInterest(@PathVariable long id, Model model, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        Interest interest = interestService.getInterestById(id);
        if (interest == null) {
            return "redirect:/dashboard";
        }
        session.setAttribute("interest", interest);

        model.addAttribute("interest", interest);
        boolean userExists = interest.getLikedByUsers().stream()
                .anyMatch(u -> u.getUsername().equals(user.getUsername()));
        model.addAttribute("isInterest", userExists);
        return "interest";
    }

    @GetMapping("/interest/graph")
    public ResponseEntity<?> getInterestGraph(HttpSession session) {
        Interest interest = (Interest) session.getAttribute("interest");
        if (interest == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new RedirectView("/dashboard"));
        }
        Graph graph = interestService.getGraphInterest(interest);
        return ResponseEntity.ok(graph);
    }
}
