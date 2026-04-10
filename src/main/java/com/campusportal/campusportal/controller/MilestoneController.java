package com.campusportal.campusportal.controller;

import com.campusportal.campusportal.model.Milestone;
import com.campusportal.campusportal.model.Project;
import com.campusportal.campusportal.model.User;
import com.campusportal.campusportal.repository.MilestoneRepository;
import com.campusportal.campusportal.repository.ProjectRepository;
import com.campusportal.campusportal.repository.TeamMemberRepository;
import com.campusportal.campusportal.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/milestone")
public class MilestoneController {

    @Autowired
    private MilestoneRepository milestoneRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TeamMemberRepository teamMemberRepository;

    @GetMapping("/{projectId}")
    public String milestonePage(@PathVariable Long projectId, Model model, java.security.Principal principal, org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        Project project = projectRepository.findById(projectId).orElse(null);
        
        if (project == null) {
            redirectAttributes.addFlashAttribute("error", "Protocol not found.");
            return "redirect:/projects/list";
        }

        boolean isOwner = false;
        boolean isApprovedMember = false;

        if (principal != null) {
            User user = userRepository.findByName(principal.getName());
            if (project.getCreatedBy() != null && project.getCreatedBy().getName().equals(principal.getName())) {
                isOwner = true;
            }
            if (user != null) {
                isApprovedMember = teamMemberRepository.findByProjectAndStatus(project, "APPROVED")
                    .stream().anyMatch(tm -> tm.getUser().getId().equals(user.getId()));
            }
        }

        if (!isOwner && !isApprovedMember) {
            redirectAttributes.addFlashAttribute("error", "Access Denied: You must be an approved member to view internal milestones.");
            return "redirect:/projects/list";
        }
        
        model.addAttribute("project", project);
        model.addAttribute("milestones", milestoneRepository.findByProject(project));
        model.addAttribute("newMilestone", new Milestone());
        model.addAttribute("isOwner", isOwner);
        return "milestone";
    }

    @PostMapping("/add/{projectId}")
    public String addMilestone(@PathVariable Long projectId,
                               @ModelAttribute Milestone milestone, java.security.Principal principal) {
        Project project = projectRepository.findById(projectId).orElse(null);
        if (project != null && project.getCreatedBy() != null && principal != null && project.getCreatedBy().getName().equals(principal.getName())) {
            milestone.setProject(project);
            milestoneRepository.save(milestone);
        }
        return "redirect:/milestone/" + projectId;
    }

    @PostMapping("/progress/{projectId}")
    public String updateProgress(@PathVariable Long projectId,
                                 @RequestParam int progress, java.security.Principal principal) {
        Project project = projectRepository.findById(projectId).orElse(null);
        if (project != null && project.getCreatedBy() != null && principal != null && project.getCreatedBy().getName().equals(principal.getName())) {
            project.setProgress(progress);
            projectRepository.save(project);
        }
        return "redirect:/milestone/" + projectId;
    }
}
