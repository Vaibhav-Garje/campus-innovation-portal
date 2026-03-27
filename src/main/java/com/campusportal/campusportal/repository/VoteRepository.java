package com.campusportal.campusportal.repository;

import com.campusportal.campusportal.model.Vote;
import com.campusportal.campusportal.model.Project;
import com.campusportal.campusportal.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface VoteRepository extends JpaRepository<Vote, Long> {
    Optional<Vote> findByProjectAndUser(Project project, User user);
    int countByProject(Project project);
    boolean existsByProjectAndUser(Project project, User user);
}