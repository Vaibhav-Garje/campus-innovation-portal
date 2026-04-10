package com.campusportal.campusportal.controller;

import com.campusportal.campusportal.model.Project;
import com.campusportal.campusportal.repository.ProjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/faculty")
public class FacultyController {

    @Autowired
    private ProjectRepository projectRepository;

    @GetMapping("/dashboard")
    public String facultyDashboard(Model model) {
        List<Project> pendingProjects = projectRepository.findByStatus("PENDING");
        List<Project> allProjects = projectRepository.findAll();

        model.addAttribute("pendingProjects", pendingProjects);
        model.addAttribute("allProjects", allProjects);
        model.addAttribute("pendingCount", pendingProjects.size());

        return "faculty/faculty-dashboard";   // This must match your file name exactly
    }

    @GetMapping("/projects")
    public String viewProjects(Model model) {
        List<Project> projects = projectRepository.findAll();
        model.addAttribute("projects", projects);
        return "faculty/project-approval";
    }

    @PostMapping("/approve/{id}")
    public String approveProject(@PathVariable Long id, @RequestHeader(value = "referer", required = false) String referer, RedirectAttributes ra) {
        Project project = projectRepository.findById(id).orElse(null);
        if (project != null) {
            project.setStatus("APPROVED");
            projectRepository.save(project);
            ra.addFlashAttribute("success", "Protocol '" + project.getTitle() + "' has been AUTHORIZED.");
        }
        return (referer != null) ? "redirect:" + referer : "redirect:/faculty/dashboard";
    }

    @PostMapping("/reject/{id}")
    public String rejectProject(@PathVariable Long id, @RequestHeader(value = "referer", required = false) String referer, RedirectAttributes ra) {
        Project project = projectRepository.findById(id).orElse(null);
        if (project != null) {
            project.setStatus("REJECTED");
            projectRepository.save(project);
            ra.addFlashAttribute("error", "Protocol '" + project.getTitle() + "' has been ABORTED.");
        }
        return (referer != null) ? "redirect:" + referer : "redirect:/faculty/dashboard";
    }
}