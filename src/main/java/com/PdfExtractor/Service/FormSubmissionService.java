package com.PdfExtractor.Service;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

import com.PdfExtractor.Entity.FormSubmission;
import com.PdfExtractor.Entity.Whitepaper;
import com.PdfExtractor.Repo.FormSubmissionRepository;
import com.PdfExtractor.Repo.WhitepaperRepository;
import com.PdfExtractor.dto.FormSubmissionRequest;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class FormSubmissionService {
	private static final Set<String> EU_CODES = Set.of("AL", "AD", "BY", "BE", "BA", "BG", "HR", "CY", "CZ", "DK", "EE",
			"FI", "FR", "HU", "IS", "IE", "IT", "LV", "LI", "LT", "LU", "MT", "MD", "MC", "ME", "NL", "MK", "PL", "PT",
			"RO", "SM", "RS", "SK", "SI", "ES", "SE", "UA", "UK", "VA");

	@Autowired
	private WhitepaperRepository wpRepo;
	@Autowired
	private FormSubmissionRepository subRepo;
	@Autowired
	private ObjectMapper objectMapper;
	@Autowired
	private CountryResolver countryResolver;

	/**
	 * New helper: GET /consent?country=…
	 */
	public String getConsentMessage(Long wpId, String countryInput) throws IOException {
		Whitepaper wp = wpRepo.findById(wpId).orElseThrow(() -> new IllegalArgumentException("Invalid whitepaper ID"));

		Map<String, String> consentMap = objectMapper.readValue(wp.getConsentMessages(), new TypeReference<>() {
		});

		String code = countryResolver.resolveCountryCode(countryInput);
		if (code != null && consentMap.containsKey(code)) {
			return consentMap.get(code);
		} else if (code != null && EU_CODES.contains(code)) {
			return consentMap.get("EU");
		} else {
			return consentMap.get("DEFAULT");
		}
	}

	/**
	 * Unchanged: store form submission, trusting req.getConsentMessage()
	 */
	public FormSubmission processSubmission(Long wpId, FormSubmissionRequest req) throws IOException {

		Whitepaper wp = wpRepo.findById(wpId).orElseThrow(() -> new IllegalArgumentException("Invalid whitepaper ID"));

		FormSubmission sub = new FormSubmission();
		sub.setWhitepaper(wp);
		sub.setFullName(req.getFullName());
		sub.setEmail(req.getEmail());
		sub.setCountry(req.getCountry());
		sub.setPostCode(req.getPostCode());
		sub.setPhone(req.getPhone());

		sub.setAnswersJson(objectMapper.writeValueAsString(req.getAnswers()));
		// ← use the consent text the client provided
		sub.setConsentMessage(req.getConsentMessage());

		return subRepo.save(sub);
	}

	public String resolveCountryCode(String countryInput) {
		return countryResolver.resolveCountryCode(countryInput);
	}

}