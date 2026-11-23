package com.applytrack.application;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:test")
@AutoConfigureMockMvc
class JobApplicationControllerTest {

	@Autowired
	MockMvc mvc;

	@Test
	void createsAndFetchesAnApplication() throws Exception {
		mvc.perform(post("/api/applications")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"company":"Acme","role":"Backend Engineer","appliedDate":"2026-11-01"}
								"""))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.status").value("APPLIED"));

		mvc.perform(get("/api/applications"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].company").value("Acme"));
	}

	@Test
	void rejectsMissingCompany() throws Exception {
		mvc.perform(post("/api/applications")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"role\":\"Dev\",\"appliedDate\":\"2026-11-01\"}"))
				.andExpect(status().isBadRequest());
	}

	@Test
	void returns404ForUnknownId() throws Exception {
		mvc.perform(get("/api/applications/9999")).andExpect(status().isNotFound());
	}

	@Test
	void staleListsOldAppliedApplications() throws Exception {
		mvc.perform(post("/api/applications")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"company\":\"Old Co\",\"role\":\"Dev\",\"appliedDate\":\"2020-01-01\"}"));

		mvc.perform(get("/api/applications/stale?days=14"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[?(@.company=='Old Co')]").exists());
	}
}
