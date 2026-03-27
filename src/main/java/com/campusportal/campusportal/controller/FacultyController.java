package com.campusportal.campusportal.controller;

import com.campusportal.campusportal.model.Project;
import com.campusportal.campusportal.repository.ProjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/faculty")
public class FacultyController {

    @Autowired
    private ProjectRepository projectRepository;

    @GetMapping("/projects")
    public String viewProjects(Model model) {
        List<Project> projects = projectRepository.findAll();
        model.addAttribute("projects", projects);
        return "faculty/project-approval";
    }

    @PostMapping("/approve/{id}")
    public String approveProject(@PathVariable Long id) {
        Project project = projectRepository.findById(id).orElse(null);
        if (project != null) {
            project.setStatus("APPROVED");
            projectRepository.save(project);
        }
        return "redirect:/faculty/projects";
    }

    @PostMapping("/reject/{id}")
    public String rejectProject(@PathVariable Long id) {
        Project project = projectRepository.findById(id).orElse(null);
        if (project != null) {
            project.setStatus("REJECTED");
            projectRepository.save(project);
        }
        return "redirect:/faculty/projects";
    }
}