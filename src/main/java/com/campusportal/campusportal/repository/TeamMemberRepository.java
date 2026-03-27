package com.campusportal.campusportal.repository;

import com.campusportal.campusportal.model.TeamMember;
import com.campusportal.campusportal.model.Project;
import com.campusportal.campusportal.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TeamMemberRepository extends JpaRepository<TeamMember, Long> {
    List<TeamMember> findByProject(Project project);
    List<TeamMember> findByUser(User user);
    List<TeamMember> findByProjectAndStatus(Project project, String status);
}
