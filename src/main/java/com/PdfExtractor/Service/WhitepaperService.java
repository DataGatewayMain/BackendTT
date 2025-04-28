package com.PdfExtractor.Service;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.apache.pdfbox.Loader;
import com.PdfExtractor.Entity.Whitepaper;
import com.PdfExtractor.Repo.WhitepaperRepository;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class WhitepaperService {

	@Autowired
	private WhitepaperRepository whitepaperRepository;

	@Autowired
	private Cloudinary cloudinary;

	@Autowired
	private BrandfetchService brandfetchService;

	@Autowired
	private CategoryService categoryService;

	@Autowired
	private GoogleDriveService driveService; // ← NEW

	public Whitepaper processAndStoreWhitepaper(MultipartFile file, String companyName, String customQuestionsJson)
			throws IOException {
		// 1) Load PDF → document + fullText
		PDDocument document = Loader.loadPDF(file.getInputStream());
		String fullText = new PDFTextStripper().getText(document).trim();

		// 2) AI: title / subtitle / summary using BOTH fullText and document
		Map<String, String> extracted = categoryService.extractTitleSubtitleSummary(fullText, document);
		String title = extracted.getOrDefault("title", "Untitled");
		String subtitle = extracted.getOrDefault("subtitle", "");
		String content = extracted.getOrDefault("summary", "");

		// 3) Render screenshot & upload to Cloudinary
		PDFRenderer renderer = new PDFRenderer(document);
		BufferedImage img = renderer.renderImageWithDPI(0, 150, ImageType.RGB);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ImageIO.write(img, "png", baos);

		// Now that we’ve taken our screenshot, we can close the document
		document.close();

		@SuppressWarnings("unchecked")
		Map<String, Object> uploadResult = cloudinary.uploader().upload(baos.toByteArray(), ObjectUtils.emptyMap());
		String screenshotUrl = (String) uploadResult.get("secure_url");

		// 4) Company metadata & categorization
		String domain = Optional.ofNullable(brandfetchService.getDomainForCompany(companyName))
				.orElse(companyName.toLowerCase().replaceAll("\\s+", "") + ".com");
		String logoUrl = brandfetchService.getLogoUrlForDomain(domain);
		String faviconUrl = (logoUrl != null) ? logoUrl : "https://www.google.com/s2/favicons?sz=64&domain=" + domain;
		Map<String, String> links = categoryService.findPrivacyAndUnsubscribeLinks(companyName, domain);

		String category = categoryService.classify(companyName);
		String[] job = categoryService.classifyJobTitleAndSubTitle(subtitle);

		String companyDescription = categoryService.getCompanyDescription(companyName);

		// 5) Classify topics using fullText
		String topics = categoryService.classifyTopics(fullText);

		// 6) Build consent JSON
		Map<String, String> consentMap = generateAllConsentMessages(companyName, links.get("privacyPolicy"),
				links.get("unsubscribeLink"));
		String consentJson = new ObjectMapper().writeValueAsString(consentMap);

		// 7) Upload raw PDF to Drive
		String pdfFileId = driveService.uploadPdf(file);
		String pdfViewLink = driveService.getViewLink(pdfFileId);

		// 8) Assemble & save Whitepaper
		Whitepaper wp = new Whitepaper();
		wp.setTitle(title);
		wp.setSubtitle(subtitle);
		wp.setContent(content);
		wp.setScreenshotUrl(screenshotUrl);
		wp.setCompanyName(companyName);
		wp.setImageDomain(domain);
		wp.setLogoUrl(logoUrl);
		wp.setFaviconUrl(faviconUrl);
		wp.setPrivacyLink(links.get("privacyPolicy"));
		wp.setUnsubscribeLink(links.get("unsubscribeLink"));
		wp.setCategories(category);
		wp.setJobTitle(job[0]);
		wp.setJobLevel(job[1]);
		wp.setCompanyDescription(companyDescription);
		wp.setConsentMessages(consentJson);
		wp.setPdfFileId(pdfFileId);
		wp.setPdfViewLink(pdfViewLink);
		wp.setTopics(topics); // Set the classified topics

		if (customQuestionsJson != null && !customQuestionsJson.isEmpty()) {
			wp.setCustomQuestions(customQuestionsJson);
		}

		return whitepaperRepository.save(wp);
	}

	/**
	 * Return an HTML <a> tag styled in the signature blue.
	 */

	private String blueLink(String url, String label) {
		return String.format("<a href=\"%s\" " + "style=\"color: #0078D4; text-decoration: none;\" "
				+ "onmouseover=\"this.style.textDecoration='underline'\" "
				+ "onmouseout=\"this.style.textDecoration='none'\">" + "%s</a>", url, label);
	}

	/**
	 * Build the full map of per-country consent messages, using blueLink(...)
	 * everywhere.
	 */
	private Map<String, String> generateAllConsentMessages(String companyName, String privacyLink,
			String unsubscribeLink) {
		Map<String, String> consentMap = new HashMap<>();

		consentMap.put("AT", String.format(
				"%1$s would like to contact you in the future regarding their products and services via phone, email, and/or post. "
						+ "By submitting your details, you agree to receive communications from %1$s in compliance with GDPR and the Austrian Telecommunications Act (TKG 2021). "
						+ "See our %2$s. You may %3$s anytime.",
				companyName, blueLink(privacyLink, "Privacy Policy"), blueLink(unsubscribeLink, "revoke")));

		consentMap.put("AF", String.format(
				"%s would like to contact you regarding their products and services via phone, email, and/or post. "
						+ "By opting in, you consent to data processing under Afghanistan’s privacy laws. See our %s. "
						+ "You may %s at any time.",
				companyName, blueLink(privacyLink, "Privacy Policy"), blueLink(unsubscribeLink, "unsubscribe")));

		consentMap.put("DZ", String.format(
				"%s would like to inform you about their latest products and services via phone, email, and/or post. "
						+ "By opting in, you agree to data processing under Algerian data protection laws. See our %s. "
						+ "You may %s at any time.",
				companyName, blueLink(privacyLink, "Privacy Policy"), blueLink(unsubscribeLink, "unsubscribe")));

		consentMap.put("CA", String.format(
				"%1$s would like to contact you regarding their products and services via phone, email, and/or post. "
						+ "By providing your details, you consent to receiving communications from %1$s in accordance with "
						+ "Canada’s Anti-Spam Legislation (CASL) and the Personal Information Protection and Electronic Documents Act (PIPEDA). "
						+ "You can %2$s your consent at any time. For more details on our privacy practices, how we manage your personal data, "
						+ "and how to unsubscribe, please see our %3$s.",
				companyName, blueLink(unsubscribeLink, "withdraw"), blueLink(privacyLink, "Privacy Statement")));

		consentMap.put("RU", String.format(
				"%s would like to contact you about their products and services via phone, email, and/or post. "
						+ "By providing your consent, you agree to our processing of your data in accordance with Russian data protection laws. "
						+ "See our %s. You may %s at any time.",
				companyName, blueLink(privacyLink, "Privacy Policy"), blueLink(unsubscribeLink, "unsubscribe")));

		consentMap.put("TR", String.format(
				"%s would like to contact you about their products and services via phone, email, and/or post. "
						+ "By opting in, you consent to our use of your personal data in compliance with Turkish data protection regulations. "
						+ "See our %s. You may %s at any time.",
				companyName, blueLink(privacyLink, "Privacy Policy"), blueLink(unsubscribeLink, "unsubscribe")));

		consentMap.put("CH", String.format(
				"%1$s would like to contact you regarding their products and services via phone, email, and/or post. "
						+ "By providing your details, you consent to receiving communications from %1$s in accordance with the Swiss Federal Act on Data Protection (FADP, rev. 2023). "
						+ "See our %2$s. You may %3$s anytime.",
				companyName, blueLink(privacyLink, "Privacy Policy"), blueLink(unsubscribeLink, "opt out")));

		consentMap.put("DE", String.format(
				"%1$s would like to contact you regarding their products and services via phone, email, and/or post. "
						+ "By providing your details, you consent to communications from %1$s in accordance with GDPR and the German Telecommunications and Telemedia Data Protection Act (TTDSG). "
						+ "See our %2$s. You may %3$s anytime.",
				companyName, blueLink(privacyLink, "Privacy Policy"), blueLink(unsubscribeLink, "opt out")));

		consentMap.put("GR", String.format(
				"%1$s would like to contact you in the future regarding its products and services via phone, email, and/or post. "
						+ "By providing your details, you consent to receiving communications from %1$s in accordance with the General Data Protection Regulation (GDPR) and the Greek Data Protection Law (Law 4624/2019). "
						+ "You may %2$s your consent at any time. For more details on how we process your personal data and how to unsubscribe, please refer to our %3$s.",
				companyName, blueLink(unsubscribeLink, "withdraw"), blueLink(privacyLink, "Privacy Statement")));

		consentMap.put("LU", String.format(
				"%1$s would like to contact you in the future regarding its products and services via phone, email, and/or post. "
						+ "By submitting your details, you agree to receive communications from %1$s in compliance with the General Data Protection Regulation (GDPR) and the Luxembourg Data Protection Law (Loi du 1er août 2018). "
						+ "You may %2$s your consent at any time. For more details on how we process your personal data and how to unsubscribe, please refer to our %3$s.",
				companyName, blueLink(unsubscribeLink, "revoke"), blueLink(privacyLink, "Privacy Statement")));

		consentMap.put("NO", String.format(
				"%1$s would like to contact you in the future regarding its products and services via phone, email, and/or post. "
						+ "By providing your details, you consent to receiving communications from %1$s in accordance with the General Data Protection Regulation (GDPR) and Norway’s Personal Data Protection Act (Personopplysningsloven). "
						+ "You may %2$s at any time.",
				companyName, blueLink(unsubscribeLink, "withdraw")));

		// Fallbacks
		consentMap.put("DEFAULT", String.format(
				"If you engage with the content, %1$s will share your data with them. For details on their information practices and how to unsubscribe, see our %2$s. You can %3$s at any time.",
				companyName, blueLink(privacyLink, "Privacy Policy"), blueLink(unsubscribeLink, "unsubscribe")));

		consentMap.put("EU", String.format(
				"%1$s would like to contact you in the future about their products and services via phone, email, and/or post. "
						+ "For details, see their %2$s. You can %3$s anytime.",
				companyName, blueLink(privacyLink, "Privacy Statement"), blueLink(unsubscribeLink, "unsubscribe")));

		return consentMap;
	}

	public Optional<Whitepaper> getWhitepaperById(Long id) {
		return whitepaperRepository.findById(id);
	}
}
