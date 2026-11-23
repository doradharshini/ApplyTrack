package com.applytrack.application;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalDate;

@Entity
public class JobApplication {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private String company;

	@Column(nullable = false)
	private String role;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private ApplicationStatus status = ApplicationStatus.APPLIED;

	@Column(nullable = false)
	private LocalDate appliedDate;

	private LocalDate followUpDate;

	private String jobUrl;

	@Column(length = 2000)
	private String notes;

	public Long getId() { return id; }
	public String getCompany() { return company; }
	public void setCompany(String company) { this.company = company; }
	public String getRole() { return role; }
	public void setRole(String role) { this.role = role; }
	public ApplicationStatus getStatus() { return status; }
	public void setStatus(ApplicationStatus status) { this.status = status; }
	public LocalDate getAppliedDate() { return appliedDate; }
	public void setAppliedDate(LocalDate appliedDate) { this.appliedDate = appliedDate; }
	public LocalDate getFollowUpDate() { return followUpDate; }
	public void setFollowUpDate(LocalDate followUpDate) { this.followUpDate = followUpDate; }
	public String getJobUrl() { return jobUrl; }
	public void setJobUrl(String jobUrl) { this.jobUrl = jobUrl; }
	public String getNotes() { return notes; }
	public void setNotes(String notes) { this.notes = notes; }
}
