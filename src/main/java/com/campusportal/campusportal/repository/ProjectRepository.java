package com.campusportal.campusportal.repository;

import com.campusportal.campusportal.model.Project;
import com.campusportal.campusportal.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ProjectRepository extends JpaRepository<Project, Long> {
    List<Project> findByCreatedBy(User user);
    List<Project> findByStatus(String status);
    List<Project> findByDomain(String domain);
    int countByStatus(String status);
}
