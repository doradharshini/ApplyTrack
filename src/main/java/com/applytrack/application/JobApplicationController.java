package com.applytrack.application;

import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/applications")
public class JobApplicationController {

	private final JobApplicationRepository repository;

	public JobApplicationController(JobApplicationRepository repository) {
		this.repository = repository;
	}

	@GetMapping
	public List<JobApplication> list(@RequestParam(required = false) ApplicationStatus status) {
		return status == null ? repository.findAll() : repository.findByStatus(status);
	}

	/** Applications still in APPLIED with no progress for more than {@code days} days. */
	@GetMapping("/stale")
	public List<JobApplication> stale(@RequestParam(defaultValue = "14") int days) {
		return repository.findByStatusAndAppliedDateBefore(
				ApplicationStatus.APPLIED, LocalDate.now().minusDays(days));
	}

	@GetMapping("/{id}")
	public JobApplication get(@PathVariable Long id) {
		return find(id);
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public JobApplication create(@Valid @RequestBody JobApplicationRequest request) {
		return repository.save(apply(new JobApplication(), request));
	}

	@PutMapping("/{id}")
	public JobApplication update(@PathVariable Long id, @Valid @RequestBody JobApplicationRequest request) {
		return repository.save(apply(find(id), request));
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> delete(@PathVariable Long id) {
		repository.delete(find(id));
		return ResponseEntity.noContent().build();
	}

	private JobApplication find(Long id) {
		return repository.findById(id).orElseThrow(() -> new ApplicationNotFoundException(id));
	}

	private JobApplication apply(JobApplication target, JobApplicationRequest r) {
		target.setCompany(r.company());
		target.setRole(r.role());
		target.setStatus(r.status() == null ? ApplicationStatus.APPLIED : r.status());
		target.setAppliedDate(r.appliedDate());
		target.setFollowUpDate(r.followUpDate());
		target.setJobUrl(r.jobUrl());
		target.setNotes(r.notes());
		return target;
	}
}
