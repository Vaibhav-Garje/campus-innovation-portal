package com.campusportal.campusportal.controller;

import com.campusportal.campusportal.model.Project;
import com.campusportal.campusportal.model.TeamMember;
import com.campusportal.campusportal.model.User;
import com.campusportal.campusportal.repository.ProjectRepository;
import com.campusportal.campusportal.repository.TeamMemberRepository;
import com.campusportal.campusportal.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@Controller
@RequestMapping("/team")
public class TeamController {

    @Autowired
    private TeamMemberRepository teamMemberRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/join/{projectId}")
    public String joinProject(@PathVariable Long projectId, Principal principal) {
        User user = userRepository.findByName(principal.getName());
        Project project = projectRepository.findById(projectId).orElse(null);

        if (project != null && user != null) {
            TeamMember member = new TeamMember();
            member.setProject(project);
            member.setUser(user);
            member.setStatus("PENDING");
            teamMemberRepository.save(member);
        }
        return "redirect:/projects/list";
    }
}
