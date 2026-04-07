package com.campusportal.campusportal.controller;

import com.campusportal.campusportal.model.Project;
import com.campusportal.campusportal.model.TeamMember;
import com.campusportal.campusportal.repository.ProjectRepository;
import com.campusportal.campusportal.repository.TeamMemberRepository;
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

    @Autowired
    private TeamMemberRepository teamMemberRepository;

    @GetMapping("/dashboard")
    public String facultyDashboard(Model model) {
        List<Project> pendingProjects = projectRepository.findByStatus("PENDING");
        List<Project> allProjects = projectRepository.findAll();
        List<TeamMember> pendingJoinRequests = teamMemberRepository.findByStatus("PENDING");

        model.addAttribute("pendingProjects", pendingProjects);
        model.addAttribute("allProjects", allProjects);
        model.addAttribute("pendingCount", pendingProjects.size());
        model.addAttribute("pendingJoinRequests", pendingJoinRequests);

        return "faculty/faculty-dashboard";   // This must match your file name exactly
    }

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

    @PostMapping("/team/approve/{memberId}")
    public String approveJoinRequest(@PathVariable Long memberId) {
        TeamMember member = teamMemberRepository.findById(memberId).orElse(null);
        if (member != null) {
            member.setStatus("APPROVED");
            teamMemberRepository.save(member);
        }
        return "redirect:/faculty/dashboard";
    }

    @PostMapping("/team/reject/{memberId}")
    public String rejectJoinRequest(@PathVariable Long memberId) {
        TeamMember member = teamMemberRepository.findById(memberId).orElse(null);
        if (member != null) {
            teamMemberRepository.delete(member);
        }
        return "redirect:/faculty/dashboard";
    }
}