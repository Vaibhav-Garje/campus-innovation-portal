package com.campusportal.campusportal.repository;

import com.campusportal.campusportal.model.Milestone;
import com.campusportal.campusportal.model.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MilestoneRepository extends JpaRepository<Milestone, Long> {
    List<Milestone> findByProject(Project project);
}
