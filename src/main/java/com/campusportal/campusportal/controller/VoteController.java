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
    public String vote(@PathVariable Long projectId, Principal principal) {
        User user = userRepository.findByName(principal.getName());
        Project project = projectRepository.findById(projectId).orElse(null);

        if (project != null && user != null) {
            boolean alreadyVoted = voteRepository.existsByProjectAndUser(project, user);
            if (!alreadyVoted) {
                Vote vote = new Vote();
                vote.setProject(project);
                vote.setUser(user);
                voteRepository.save(vote);
                project.setVoteCount(project.getVoteCount() + 1);
                projectRepository.save(project);
            }
        }
        return "redirect:/projects/list";
    }
}