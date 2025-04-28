package com.PdfExtractor.Entity;

import jakarta.persistence.*;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class Whitepaper {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getSubtitle() {
		return subtitle;
	}

	public void setSubtitle(String subtitle) {
		this.subtitle = subtitle;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getPdfFileId() {
		return pdfFileId;
	}

	public void setPdfFileId(String pdfFileId) {
		this.pdfFileId = pdfFileId;
	}

	public String getPdfViewLink() {
		return pdfViewLink;
	}

	public void setPdfViewLink(String pdfViewLink) {
		this.pdfViewLink = pdfViewLink;
	}

	public String getJobTitle() {
		return jobTitle;
	}

	public void setJobTitle(String jobTitle) {
		this.jobTitle = jobTitle;
	}

	public String getJobLevel() {
		return jobLevel;
	}

	public void setJobLevel(String jobLevel) {
		this.jobLevel = jobLevel;
	}

	public String getCompanyName() {
		return companyName;
	}

	public void setCompanyName(String companyName) {
		this.companyName = companyName;
	}

	public String getImageDomain() {
		return imageDomain;
	}

	public void setImageDomain(String imageDomain) {
		this.imageDomain = imageDomain;
	}

	public String getFaviconUrl() {
		return faviconUrl;
	}

	public void setFaviconUrl(String faviconUrl) {
		this.faviconUrl = faviconUrl;
	}

	public String getCategories() {
		return categories;
	}

	public void setCategories(String categories) {
		this.categories = categories;
	}

	public String getPrivacyLink() {
		return privacyLink;
	}

	public void setPrivacyLink(String privacyLink) {
		this.privacyLink = privacyLink;
	}

	public String getUnsubscribeLink() {
		return unsubscribeLink;
	}

	public void setUnsubscribeLink(String unsubscribeLink) {
		this.unsubscribeLink = unsubscribeLink;
	}

	public String getCompanyDescription() {
		return companyDescription;
	}

	public void setCompanyDescription(String companyDescription) {
		this.companyDescription = companyDescription;
	}

	public String getLogoUrl() {
		return logoUrl;
	}

	public void setLogoUrl(String logoUrl) {
		this.logoUrl = logoUrl;
	}

	public String getConsentMessages() {
		return consentMessages;
	}

	public void setConsentMessages(String consentMessages) {
		this.consentMessages = consentMessages;
	}

	public String getScreenshotUrl() {
		return screenshotUrl;
	}

	public void setScreenshotUrl(String screenshotUrl) {
		this.screenshotUrl = screenshotUrl;
	}

	public String getCustomQuestions() {
		return customQuestions;
	}

	public void setCustomQuestions(String customQuestions) {
		this.customQuestions = customQuestions;
	}

	public String getTopics() {
		return topics;
	}

	public void setTopics(String topics) {
		this.topics = topics;
	}

	private String title;
	private String subtitle;

	@Lob
	private String content;

	private String pdfFileId;
	private String pdfViewLink;
	private String jobTitle;
	private String jobLevel;
	private String companyName;
	private String imageDomain;
	private String faviconUrl;
	private String categories;
	private String privacyLink;
	private String unsubscribeLink;
	private String companyDescription;
	private String logoUrl;

	@Column(length = 10000)
	private String consentMessages; // stores all country consents as JSON

	@Lob
	private String screenshotUrl;

	@Column(columnDefinition = "TEXT")
	private String customQuestions;

	@Column(columnDefinition = "TEXT")
	private String topics; // Stores the identified topics for the whitepaper

	
	// Default constructor
	public Whitepaper() {
	}

	// All-args constructor
	public Whitepaper(String title, String subtitle, String content, String jobTitle, String jobLevel,
			String companyName, String imageDomain, String faviconUrl, String categories, String screenshotUrl,
			String privacyLink, String unsubscribeLink, String consentMessages, String companyDescription,
			String pdfFileId, String pdfViewLink, String customQuestions,String topics) {
		this.title = title;
		this.subtitle = subtitle;
		this.content = content;
		this.jobTitle = jobTitle;
		this.jobLevel = jobLevel;
		this.companyName = companyName;
		this.imageDomain = imageDomain;
		this.faviconUrl = faviconUrl;
		this.categories = categories;
		this.screenshotUrl = screenshotUrl;
		this.privacyLink = privacyLink;
		this.unsubscribeLink = unsubscribeLink;
		this.consentMessages = consentMessages;
		this.companyDescription = companyDescription;
		this.pdfViewLink = pdfViewLink;
		this.pdfFileId = pdfFileId;
		this.customQuestions = customQuestions;
		this.topics=topics;
	}

}
