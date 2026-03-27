package com.campusportal.campusportal.controller;

import com.campusportal.campusportal.model.Project;
import com.campusportal.campusportal.model.User;
import com.campusportal.campusportal.repository.ProjectRepository;
import com.campusportal.campusportal.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/projects")
public class ProjectController {

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/submit")
    public String submitForm(Model model) {
        model.addAttribute("project", new Project());
        return "submit-project";
    }

    @PostMapping("/create")
    public String createProject(@ModelAttribute Project project, Principal principal) {
        User user = userRepository.findByName(principal.getName());
        project.setCreatedBy(user);
        project.setStatus("PENDING");
        projectRepository.save(project);
        return "redirect:/projects/list";
    }

    @GetMapping("/list")
    public String listProjects(Model model) {
        List<Project> projects = projectRepository.findAll();
        model.addAttribute("projects", projects);
        return "project-list";
    }
}