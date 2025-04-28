package com.PdfExtractor.Controller;

import org.springframework.web.bind.annotation.*;

import com.PdfExtractor.Entity.SubscriptionForm;
import com.PdfExtractor.Service.SubscriptionFormService;
import com.PdfExtractor.dto.SubscriptionFormRequest;

import org.springframework.http.ResponseEntity;
import org.springframework.beans.factory.annotation.Autowired;

@RestController
@RequestMapping("/api/form")
@CrossOrigin(origins = { "http://localhost:4200", "http://localhost:53719", "http://192.168.0.5:4200",
		"http://192.168.0.6:4200", "http://192.168.0.7:4200", "https://test.vectordb.app", "https://vectordb.app",
		"https://web.vectordb.app" }, allowedHeaders = "*", methods = { RequestMethod.GET, RequestMethod.POST,
				RequestMethod.PUT, RequestMethod.DELETE }, allowCredentials = "true", maxAge = 3600)
public class SubscriptionFormController {

	@Autowired
	private SubscriptionFormService formService;

	@PostMapping("/submit")
	public ResponseEntity<SubscriptionForm> submit(@RequestBody SubscriptionFormRequest request) {
		if (request.getEmail() == null || request.getEmail().isBlank()) {
			return ResponseEntity.badRequest().build();
		}

		SubscriptionForm saved = formService.saveForm(request);
		return ResponseEntity.ok(saved);
	}
}
