package com.PdfExtractor.Controller;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.PdfExtractor.Entity.FormSubmission;
import com.PdfExtractor.Service.EmailService;
import com.PdfExtractor.Service.FormSubmissionService;
import com.PdfExtractor.dto.FormSubmissionRequest;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = { "http://localhost:4200", "http://localhost:53719", "http://192.168.0.5:4200",
		"http://192.168.0.6:4200", "http://192.168.0.7:4200", "https://test.vectordb.app", "https://vectordb.app",
		"https://web.vectordb.app" }, allowedHeaders = "*", methods = { RequestMethod.GET, RequestMethod.POST,
				RequestMethod.PUT, RequestMethod.DELETE }, allowCredentials = "true", maxAge = 3600)
public class FormSubmissionController {

	@Autowired
	private FormSubmissionService formService;

	@Autowired
	private EmailService emailService;

	/**
	 * 1) Fetch consent for a country (no DB write). GET
	 * /api/whitepapers/{id}/consent?country=…
	 */
	@GetMapping("/{id}/consent")
	public ResponseEntity<Map<String, String>> getConsent(@PathVariable("id") Long whitepaperId,
			@RequestParam("country") String country) {
		try {
			String consent = formService.getConsentMessage(whitepaperId, country);
			return ResponseEntity.ok(Map.of("consentMessage", consent));
		} catch (IllegalArgumentException | IOException e) {
			return ResponseEntity.badRequest().build();
		}
	}

	/**
	 * 2) Submit the form (expects consentMessage in body). POST
	 * /api/whitepapers/{id}/submit
	 * 
	 * @throws IOException
	 */

	@PostMapping("/{id}/submit")
	public ResponseEntity<FormSubmission> submitForm(@PathVariable("id") Long whitepaperId,
			@RequestBody FormSubmissionRequest req) {
		if (req.getConsentMessage() == null || req.getConsentMessage().isBlank()) {
			return ResponseEntity.badRequest().build();
		}

		try {
			FormSubmission saved = formService.processSubmission(whitepaperId, req);

			// Check country and send email if it's in the special set
			Set<String> specialCountries = Set.of("AT", "DE", "GR", "CH", "LU", "NO");
			String countryCode = formService.resolveCountryCode(req.getCountry()); // helper needed

			if (countryCode != null && specialCountries.contains(countryCode)) {
				String formLink = String.format(
						"https://technology-trends.net/resources/lastform1.php?email=%s&token=23f42471489774220a3d72183486770b",
						URLEncoder.encode(req.getEmail(), StandardCharsets.UTF_8));
				emailService.sendHtmlEmail(req.getEmail(), req.getFullName(), // user's name for personalization
						"Download Your Content", formLink);

			}

			return ResponseEntity.ok(saved);
		} catch (Exception e) {
			return ResponseEntity.badRequest().build();
		}
	}

}
