package isep.algoproject.controlllers;

import isep.algoproject.models.*;
import isep.algoproject.services.PostService;
import isep.algoproject.services.UserService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.view.RedirectView;

import java.io.FileNotFoundException;
import java.util.List;

@Controller
public class PostController {
    @Autowired
    private PostService postService;

    @Autowired
    private UserService userService;

    @GetMapping("/post/create")
    public String showCreatePost(Model model) {
        model.addAttribute("post", new Post());
        return "createPost";
    }

    @PostMapping("/post/create")
    public String createPost(@Valid Post post, HttpSession session, @RequestParam("imageData") MultipartFile imageData) throws FileNotFoundException {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }

        postService.createPost(post, user, imageData);
        return "redirect:/profile";
    }

    @GetMapping("/post/{userId}")
    public String showViewPost(@PathVariable long userId, Model model, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }

        List<PostIsLiked> posts = postService.getUserPostsByUserId(userId, user);
        model.addAttribute("posts", posts);
        model.addAttribute("postComment", new PostComment());
        return "userPosts";
    }

    @PostMapping("/post/like/{postId}")
    public ResponseEntity<?> likePost(@PathVariable long postId, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new RedirectView("/login"));
        }

        postService.likePost(postId, user);
        return ResponseEntity.ok("Post liked");
    }

    @DeleteMapping("/post/unlike/{postId}")
    public ResponseEntity<?> unlikePost(@PathVariable long postId, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new RedirectView("/login"));
        }
        postService.unlikePost(postId, user);
        return ResponseEntity.ok("Post unliked");
    }

    @GetMapping("/comments/{postId}")
    public ResponseEntity<List<PostCommentDto>> getCommentsByPostId(@PathVariable long postId, HttpSession session) {
        session.setAttribute("postId", postId);
        List<PostCommentDto> postComments = postService.getPostsComments(postId);
        return ResponseEntity.ok(postComments);
    }

    @PostMapping("/post/comment")
    public String addComment(@Valid PostComment comment, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }

        long postId = (long) session.getAttribute("postId");
        postService.addComment(postId, comment, user);
        return "redirect:/profile";
    }

    @GetMapping("/post/all")
    public String showAllPosts(@RequestParam(defaultValue = "0") int page, Model model, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }

        Pageable pageable = PageRequest.of(page, 10);

        Page<PostIsLiked> postPage = postService.getAllPosts(pageable, user);

        model.addAttribute("posts", postPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", postPage.getTotalPages());

        return "allPosts";
    }

    @GetMapping("/post/liked")
    public String showLikedPosts(Model model, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        List<PostIsLiked> posts = userService.getLikedPostsByUser(user);
        model.addAttribute("posts", posts);

        return "likedPosts";
    }
}
