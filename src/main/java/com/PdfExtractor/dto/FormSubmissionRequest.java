package com.PdfExtractor.dto;

import java.util.Map;

public class FormSubmissionRequest {
	private String fullName;
	private String email;
	private String country;
	private String postCode;
	private String phone;

	// key: "ans1", "ans2", ... value: the answer text ("Yes", "No", etc.)
	private Map<String, String> answers;

	// ← NEW: the client‐fetched consent text
	private String consentMessage;

	// getters & setters
	public String getConsentMessage() {
		return consentMessage;
	}

	public void setConsentMessage(String consentMessage) {
		this.consentMessage = consentMessage;
	}

	public String getFullName() {
		return fullName;
	}

	public void setFullName(String fullName) {
		this.fullName = fullName;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public String getPostCode() {
		return postCode;
	}

	public void setPostCode(String postCode) {
		this.postCode = postCode;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public Map<String, String> getAnswers() {
		return answers;
	}

	public void setAnswers(Map<String, String> answers) {
		this.answers = answers;
	}

}
