package com.campusportal.campusportal.controller;

import com.campusportal.campusportal.model.Project;
import com.campusportal.campusportal.model.User;
import com.campusportal.campusportal.model.Vote;
import com.campusportal.campusportal.repository.ProjectRepository;
import com.campusportal.campusportal.repository.UserRepository;
import com.campusportal.campusportal.repository.VoteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

@Controller
@RequestMapping("/vote")
public class VoteController {

    @Autowired
    private VoteRepository voteRepository;
    @Autowired
    private ProjectRepository projectRepository;
    @Autowired
    private UserRepository userRepository;

    @PostMapping("/{projectId}")
    public String vote(@PathVariable Long projectId, Principal principal, RedirectAttributes redirectAttributes) {

        System.out.println("=== VOTE BUTTON CLICKED ===");
        System.out.println("Principal name: " + (principal != null ? principal.getName() : "NULL"));

        User user = userRepository.findByName(principal.getName());
        Project project = projectRepository.findById(projectId).orElse(null);

        if (user == null) {
            System.out.println("ERROR: User not found in database!");
            redirectAttributes.addFlashAttribute("error", "User not found! Please login again.");
            return "redirect:/projects/list";
        }

        if (project != null) {
            boolean alreadyVoted = voteRepository.existsByProjectAndUser(project, user);
            if (!alreadyVoted) {
                Vote vote = new Vote();
                vote.setProject(project);
                vote.setUser(user);
                voteRepository.save(vote);

                project.setVoteCount(project.getVoteCount() + 1);
                projectRepository.save(project);

                redirectAttributes.addFlashAttribute("success", "✅ Vote added successfully!");
                System.out.println("SUCCESS: Vote added for project " + projectId);
            } else {
                redirectAttributes.addFlashAttribute("info", "You have already voted.");
            }
        }
        return "redirect:/projects/list";
    }
}