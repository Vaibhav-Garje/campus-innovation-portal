package com.campusportal.campusportal.controller;

import com.campusportal.campusportal.model.Milestone;
import com.campusportal.campusportal.model.Project;
import com.campusportal.campusportal.repository.MilestoneRepository;
import com.campusportal.campusportal.repository.ProjectRepository;
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

    @GetMapping("/{projectId}")
    public String milestonePage(@PathVariable Long projectId, Model model) {
        Project project = projectRepository.findById(projectId).orElse(null);
        model.addAttribute("project", project);
        model.addAttribute("milestones", milestoneRepository.findByProject(project));
        model.addAttribute("newMilestone", new Milestone());
        return "milestone";
    }

    @PostMapping("/add/{projectId}")
    public String addMilestone(@PathVariable Long projectId,
                               @ModelAttribute Milestone milestone) {
        Project project = projectRepository.findById(projectId).orElse(null);
        if (project != null) {
            milestone.setProject(project);
            milestoneRepository.save(milestone);
        }
        return "redirect:/milestone/" + projectId;
    }

    @PostMapping("/progress/{projectId}")
    public String updateProgress(@PathVariable Long projectId,
                                 @RequestParam int progress) {
        Project project = projectRepository.findById(projectId).orElse(null);
        if (project != null) {
            project.setProgress(progress);
            projectRepository.save(project);
        }
        return "redirect:/milestone/" + projectId;
    }
}
