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
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ContentDisposition;
import org.springframework.http.MediaTypeFactory;

import java.nio.charset.StandardCharsets;
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

    // ==================== JOIN TEAM BUTTON (GET FORM) ====================
    @GetMapping("/join/{projectId}")
    public String joinProjectForm(@PathVariable Long projectId, Model model, Principal principal, RedirectAttributes redirectAttributes) {
        Project project = projectRepository.findById(projectId).orElse(null);
        if (project == null) {
            redirectAttributes.addFlashAttribute("error", "Protocol not found.");
            return "redirect:/projects/list";
        }
        User user = userRepository.findByName(principal.getName());
        model.addAttribute("project", project);
        model.addAttribute("currentUser", user);
        return "team/join";
    }

    // ==================== JOIN TEAM SUBMIT (WITH RESUME) ====================
    @PostMapping("/join/{projectId}")
    public String joinProjectSubmit(@PathVariable Long projectId, 
                                    @RequestParam("resume") MultipartFile resume,
                                    @RequestParam("contactEmail") String contactEmail,
                                    @RequestParam("contactPhone") String contactPhone,
                                    Principal principal, RedirectAttributes redirectAttributes) {
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
            member.setContactEmail(contactEmail);
            member.setContactPhone(contactPhone);
            
            try {
                if (!resume.isEmpty()) {
                    member.setResumeFile(resume.getBytes());
                    member.setResumeFileName(resume.getOriginalFilename());
                }
            } catch (Exception e) {
                redirectAttributes.addFlashAttribute("error", "Error uploading resume.");
                return "redirect:/projects/list";
            }
            
            teamMemberRepository.save(member);
            redirectAttributes.addFlashAttribute("success", "✅ Join request sent successfully! Awaiting owner approval.");
        } else {
            redirectAttributes.addFlashAttribute("info", "You have already requested to join this project.");
        }
        return "redirect:/projects/list";
    }

    // ==================== DOWNLOAD RESUME ====================
    @GetMapping("/resume/{id}")
    public ResponseEntity<byte[]> downloadResume(@PathVariable Long id) {
        TeamMember member = teamMemberRepository.findById(id).orElse(null);
        if (member == null || member.getResumeFile() == null) {
            return ResponseEntity.notFound().build();
        }
        
        String filename = member.getResumeFileName();
        if (filename == null || filename.isEmpty()) {
            filename = "resume_document";
        }

        ContentDisposition contentDisposition = ContentDisposition.builder("attachment")
                .filename(filename, StandardCharsets.UTF_8)
                .build();

        MediaType mediaType = MediaTypeFactory.getMediaType(filename)
                .orElse(MediaType.APPLICATION_OCTET_STREAM);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition.toString())
                .contentType(mediaType)
                .body(member.getResumeFile());
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

    // ==================== OWNER: MANAGE TEAM REQUESTS ====================
    @GetMapping("/manage/{projectId}")
    public String manageTeamRequests(@PathVariable Long projectId, Model model, Principal principal, RedirectAttributes redirectAttributes) {
        Project project = projectRepository.findById(projectId).orElse(null);
        if (project == null || principal == null || !project.getCreatedBy().getName().equals(principal.getName())) {
            redirectAttributes.addFlashAttribute("error", "Unauthorized access.");
            return "redirect:/projects/list";
        }

        List<TeamMember> pendingRequests = teamMemberRepository.findByProjectAndStatus(project, "PENDING");
        List<TeamMember> approvedMembers = teamMemberRepository.findByProjectAndStatus(project, "APPROVED");

        model.addAttribute("project", project);
        model.addAttribute("pendingRequests", pendingRequests);
        model.addAttribute("approvedMembers", approvedMembers);
        
        return "team/manage";
    }

    @PostMapping("/approve/{memberId}")
    public String approveJoinRequestOwner(@PathVariable Long memberId, Principal principal, RedirectAttributes redirectAttributes) {
        TeamMember member = teamMemberRepository.findById(memberId).orElse(null);
        if (member != null && member.getProject().getCreatedBy().getName().equals(principal.getName())) {
            member.setStatus("APPROVED");
            teamMemberRepository.save(member);
            redirectAttributes.addFlashAttribute("success", "Sync confirmed.");
        }
        return "redirect:/team/manage/" + (member != null ? member.getProject().getId() : "");
    }

    @PostMapping("/reject/{memberId}")
    public String rejectJoinRequestOwner(@PathVariable Long memberId, Principal principal, RedirectAttributes redirectAttributes) {
        TeamMember member = teamMemberRepository.findById(memberId).orElse(null);
        if (member != null && member.getProject().getCreatedBy().getName().equals(principal.getName())) {
            member.setStatus("REJECTED");
            // Alternatively, we could delete it, but status keeps a log
            teamMemberRepository.save(member);
            redirectAttributes.addFlashAttribute("error", "Request disconnected.");
        }
        return "redirect:/team/manage/" + (member != null ? member.getProject().getId() : "");
    }
}