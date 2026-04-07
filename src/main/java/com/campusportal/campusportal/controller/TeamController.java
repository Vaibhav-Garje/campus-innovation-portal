package com.campusportal.campusportal.controller;

import com.campusportal.campusportal.model.Project;
import com.campusportal.campusportal.model.TeamMember;
import com.campusportal.campusportal.model.User;
import com.campusportal.campusportal.repository.ProjectRepository;
import com.campusportal.campusportal.repository.TeamMemberRepository;
import com.campusportal.campusportal.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/team")
public class TeamController {

    @Autowired
    private TeamMemberRepository teamMemberRepository;
    @Autowired
    private ProjectRepository projectRepository;
    @Autowired
    private UserRepository userRepository;

    // ==================== COLLABORATE CARD - CREATE TEAM ====================
    @GetMapping("/create")
    public String createTeamPage(Model model) {
        model.addAttribute("projects", projectRepository.findAll());
        return "team/create";
    }

    @PostMapping("/create")
    public String createTeam(@RequestParam Long projectId, Principal principal, RedirectAttributes redirectAttributes) {
        User user = userRepository.findByName(principal.getName());
        Project project = projectRepository.findById(projectId).orElse(null);

        if (user != null && project != null) {
            TeamMember member = new TeamMember();
            member.setProject(project);
            member.setUser(user);
            member.setStatus("APPROVED");
            teamMemberRepository.save(member);
            redirectAttributes.addFlashAttribute("success", "✅ Team created successfully! You are the team leader.");
        }
        return "redirect:/projects/list";
    }

    // ==================== JOIN TEAM BUTTON ====================
    @PostMapping("/join/{projectId}")
    public String joinProject(@PathVariable Long projectId, Principal principal, RedirectAttributes redirectAttributes) {
        User user = userRepository.findByName(principal.getName());
        Project project = projectRepository.findById(projectId).orElse(null);

        if (user == null || project == null) {
            redirectAttributes.addFlashAttribute("error", "Error joining team.");
            return "redirect:/projects/list";
        }

        boolean alreadyRequested = teamMemberRepository.findByProjectAndStatus(project, "PENDING")
                .stream().anyMatch(tm -> tm.getUser().getId().equals(user.getId()));

        if (!alreadyRequested) {
            TeamMember member = new TeamMember();
            member.setProject(project);
            member.setUser(user);
            member.setStatus("PENDING");
            teamMemberRepository.save(member);
            redirectAttributes.addFlashAttribute("success", "✅ Join request sent successfully!");
        } else {
            redirectAttributes.addFlashAttribute("info", "You have already requested to join this project.");
        }
        return "redirect:/projects/list";
    }

    // ==================== NEW: MY TEAMS / MY REQUESTS PAGE ====================
    @GetMapping("/my-teams")
    public String myTeams(Model model, Principal principal) {
        User user = userRepository.findByName(principal.getName());
        if (user != null) {
            List<TeamMember> myTeams = teamMemberRepository.findByUser(user);
            model.addAttribute("myTeams", myTeams);
        }
        return "team/my-teams";
    }
}