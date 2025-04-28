package com.PdfExtractor.Entity;

import jakarta.persistence.*;

import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class FormSubmission {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id; // ← your primary key

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Whitepaper getWhitepaper() {
		return whitepaper;
	}

	public void setWhitepaper(Whitepaper whitepaper) {
		this.whitepaper = whitepaper;
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

	public String getAnswersJson() {
		return answersJson;
	}

	public void setAnswersJson(String answersJson) {
		this.answersJson = answersJson;
	}

	public String getConsentMessage() {
		return consentMessage;
	}

	public void setConsentMessage(String consentMessage) {
		this.consentMessage = consentMessage;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "whitepaper_id", nullable = false)
	private Whitepaper whitepaper;

	private String fullName;
	private String email;
	private String country;
	private String postCode;
	private String phone;

	@Column(columnDefinition = "TEXT")
	private String answersJson;

	@Column(columnDefinition = "TEXT")
	private String consentMessage;

}
