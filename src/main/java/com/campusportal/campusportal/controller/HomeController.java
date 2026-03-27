package com.campusportal.campusportal.controller;

import com.campusportal.campusportal.repository.ProjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @Autowired
    private ProjectRepository projectRepository;

    @GetMapping("/")
    public String home() {
        return "redirect:/auth/login";
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("pendingCount", projectRepository.countByStatus("PENDING"));
        model.addAttribute("approvedCount", projectRepository.countByStatus("APPROVED"));
        model.addAttribute("rejectedCount", projectRepository.countByStatus("REJECTED"));
        return "dashboard";
    }
}