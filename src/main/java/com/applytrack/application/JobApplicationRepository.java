package com.applytrack.application;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JobApplicationRepository extends JpaRepository<JobApplication, Long> {

	List<JobApplication> findByStatus(ApplicationStatus status);

	List<JobApplication> findByStatusAndAppliedDateBefore(ApplicationStatus status, LocalDate date);
}
