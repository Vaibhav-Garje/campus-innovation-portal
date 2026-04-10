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

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/projects")
public class ProjectController {

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TeamMemberRepository teamMemberRepository;

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
    public String listProjects(Model model, Principal principal) {
        List<Project> projects;
        Map<Long, Boolean> memberAccessMap = new HashMap<>();
        
        if (principal != null) {
            User user = userRepository.findByName(principal.getName());
            projects = projectRepository.findByStatusOrCreatedBy("APPROVED", user);
            
            List<TeamMember> userMemberships = teamMemberRepository.findByUser(user).stream()
                .filter(tm -> "APPROVED".equals(tm.getStatus()))
                .collect(Collectors.toList());
            List<Long> approvedProjectIds = userMemberships.stream()
                .map(tm -> tm.getProject().getId())
                .collect(Collectors.toList());
                
            for (Project p : projects) {
                boolean hasAccess = (p.getCreatedBy() != null && p.getCreatedBy().getName().equals(user.getName())) 
                                    || approvedProjectIds.contains(p.getId());
                memberAccessMap.put(p.getId(), hasAccess);
            }
        } else {
            projects = projectRepository.findByStatus("APPROVED");
            for (Project p : projects) {
                memberAccessMap.put(p.getId(), false);
            }
        }
        model.addAttribute("projects", projects);
        model.addAttribute("memberAccessMap", memberAccessMap);
        return "project-list";
    }
}