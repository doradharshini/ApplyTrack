package com.applytrack.application;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record JobApplicationRequest(
		@NotBlank String company,
		@NotBlank String role,
		ApplicationStatus status,
		@NotNull LocalDate appliedDate,
		LocalDate followUpDate,
		String jobUrl,
		String notes) {
}
