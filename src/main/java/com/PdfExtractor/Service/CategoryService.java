package com.PdfExtractor.Service;

import java.awt.geom.Rectangle2D;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.http.HttpHeaders;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.PDFTextStripperByArea;
import org.apache.pdfbox.text.TextPosition;
import org.cloudinary.json.JSONArray;
import org.cloudinary.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import reactor.core.publisher.Mono;

@Service
public class CategoryService {
	private final WebClient client;

	public CategoryService(@Value("${openrouter.api.key}") String apiKey) {
		this.client = WebClient.builder().baseUrl("https://openrouter.ai/api/v1/chat/completions")
				.defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
				.defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE).build();
	}

	/** 1️⃣ Company → Category */
	public String classify(String companyName) {
		String systemPrompt = "You are a classifier that assigns companies to one of the following categories: Aerospace & Aviation; Agriculture & Mining; Business Services; Computers & Computers; Construction & Real Estate; Education; Energy, Raw Materials & Utilities; Finance; Government; Legal; Manufacturing; Marketing, Advertising & Public Relations; Media, Entertainment & Publishing; Non-Profit; Retail; Software, Internet & Technology; Telecommunications; Transportation; Travel Hotel Restaurant & Recreation; Wholesale & Distribution; Healthcare, Pharmaceuticals & Biotech; Food & Beverage. Given a company name, respond with the most appropriate category from the list. Do not provide any category outside this list.";

		String userPrompt = "Company Name: " + companyName + "\nCategory:";

		ChatRequest body = new ChatRequest(List.of("gpt-3.5-turbo"), // use a valid model array
																		// :contentReference[oaicite:4]{index=4}
				List.of(new Message("system", systemPrompt), new Message("user", userPrompt)), 0, 20, 1.0, 0.0, 0.0);

		ChatResponse resp = client.post().bodyValue(body).exchangeToMono(response -> {
			if (response.statusCode().is2xxSuccessful()) {
				return response.bodyToMono(ChatResponse.class);
			} else {
				// log error body for debugging :contentReference[oaicite:5]{index=5}
				return response.bodyToMono(String.class).flatMap(err -> {
					System.err.println("Category error " + response.statusCode() + ": " + err);
					return Mono.just(new ChatResponse(Collections.emptyList()));
				});
			}
		}).block();

		// defensive null/empty check :contentReference[oaicite:6]{index=6}
		if (resp == null || resp.choices() == null || resp.choices().isEmpty()) {
			return "General";
		}
		return resp.choices().get(0).message().content().trim();
	}

	/** 2️⃣ Whitepaper title → jobTitle & jobLevel */
	public String[] classifyJobTitleAndSubTitle(String subtitle) {
		String systemPrompt = "You are a classifier. From the whitepaper title, pick one jobTitle and one jobLevel. "
				+ "Return JSON with keys 'jobTitle' and 'jobLevel'.\n"
				+ "Job Titles: information-technology, operations, business, finance, medical-health, legal\n"
				+ "Job Levels: Artificial Intelligence, Cloud Engineer, Data Science, DevOps, Cybersecurity, Networking";
		// truncate large subtitle :contentReference[oaicite:7]{index=7}
		String snippet = subtitle.length() > 500 ? subtitle.substring(0, 500) : subtitle;
		String userPrompt = "Title:\n\"\"\"" + snippet
				+ "\"\"\"\nReturn JSON {\"jobTitle\":\"...\",\"jobLevel\":\"...\"}";

		ChatRequest body = new ChatRequest(List.of("gpt-3.5-turbo"),
				List.of(new Message("system", systemPrompt), new Message("user", userPrompt)), 0, 50, 1.0, 0.0, 0.0);

		ChatResponse resp = client.post().bodyValue(body).exchangeToMono(response -> {
			if (response.statusCode().is2xxSuccessful()) {
				return response.bodyToMono(ChatResponse.class);
			} else {
				return response.bodyToMono(String.class).flatMap(err -> {
					System.err.println("JobTitle error " + response.statusCode() + ": " + err);
					return Mono.just(new ChatResponse(Collections.emptyList()));
				});
			}
		}).block();

		if (resp == null || resp.choices() == null || resp.choices().isEmpty()) {
			return new String[] { "information-technology", "Artificial Intelligence" };
		}
		String json = resp.choices().get(0).message().content().trim();
		return new String[] { extractValue(json, "jobTitle"), extractValue(json, "jobLevel") };
	}

	public Map<String, String> findPrivacyAndUnsubscribeLinks(String company, String domain) {
		String apiKey = "AIzaSyDujNCRmNQk3jzSfE2xJ6LCVT2auZQHxn0"; // Replace with your API key
		String cx = "469a8354f1e68489b"; // Replace with your Search Engine ID

		Map<String, String> links = new HashMap<>();
		links.put("privacyPolicy", "");
		links.put("unsubscribeLink", "");

		try {
			// Search for Privacy Policy
			String privacyQuery = "site:" + domain + " privacy policy";
			// 100 search queries per day are free
			String privacyUrl = "https://www.googleapis.com/customsearch/v1?key=" + apiKey + "&cx=" + cx + "&q="
					+ java.net.URLEncoder.encode(privacyQuery, "UTF-8");
			String privacyResult = sendGetRequest(privacyUrl);
			String privacyLink = extractFirstLink(privacyResult);
			if (privacyLink != null) {
				links.put("privacyPolicy", privacyLink);
			} else {
				links.put("privacyPolicy", "https://" + domain + "/privacy-policy");
			}

			// Search for Unsubscribe Link
			String unsubscribeQuery = "site:" + domain + " unsubscribe";
			String unsubscribeUrl = "https://www.googleapis.com/customsearch/v1?key=" + apiKey + "&cx=" + cx + "&q="
					+ java.net.URLEncoder.encode(unsubscribeQuery, "UTF-8");
			String unsubscribeResult = sendGetRequest(unsubscribeUrl);
			String unsubscribeLink = extractFirstLink(unsubscribeResult);
			if (unsubscribeLink != null) {
				links.put("unsubscribeLink", unsubscribeLink);
			} else {
				links.put("unsubscribeLink", "https://" + domain + "/unsubscribe");
			}

		} catch (Exception e) {
			e.printStackTrace();
			// Fallback to default links
			links.put("privacyPolicy", "https://" + domain + "/privacy-policy");
			links.put("unsubscribeLink", "https://" + domain + "/unsubscribe");
		}

		return links;
	}

	private String sendGetRequest(String urlStr) throws Exception {
		URL url = new URL(urlStr);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setRequestMethod("GET");
		conn.setRequestProperty("Accept", "application/json");

		BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));
		StringBuilder sb = new StringBuilder();
		String output;
		while ((output = br.readLine()) != null) {
			sb.append(output);
		}
		conn.disconnect();
		return sb.toString();
	}

	private String extractFirstLink(String jsonResponse) {
		JSONObject json = new JSONObject(jsonResponse);
		if (json.has("items")) {
			JSONArray items = json.getJSONArray("items");
			if (items.length() > 0) {
				JSONObject firstItem = items.getJSONObject(0);
				return firstItem.getString("link");
			}
		}
		return null;
	}

	// 4️⃣ PDF text → title, subtitle, summary @throws IOException
	public Map<String, String> extractTitleSubtitleSummary(String fullText, PDDocument document) throws IOException {
		// 1) Extract page-1 body without header/footer
		PDPage page1 = document.getPage(0);
		float w = page1.getMediaBox().getWidth();
		float h = page1.getMediaBox().getHeight();
		float margin = 50f;
		Rectangle2D.Float bodyReg = new Rectangle2D.Float(0, margin, w, h - 2 * margin);
		PDFTextStripperByArea areaStripper = new PDFTextStripperByArea();
		areaStripper.addRegion("body", bodyReg);
		areaStripper.extractRegions(page1);
		String page1Text = areaStripper.getTextForRegion("body").trim();

		// 2) Extract and clean remaining pages
		StringBuilder restBuilder = new StringBuilder();
		for (int i = 1; i < document.getNumberOfPages(); i++) {
			PDPage pg = document.getPage(i);
			areaStripper.extractRegions(pg);
			restBuilder.append(areaStripper.getTextForRegion("body")).append("\n\n");
		}
		String cleanedRest = cleanHeadersFooters(restBuilder.toString());

		// 3) Title as before
		String title = extractTitleFromFirstPage(document);

		// 4) Content = first two paragraphs
		String summary = extractTwoParagraphs(cleanedRest);

		// 5) **Subtitle** = LLM-generated from that summary
		String subtitle = "";
		if (!summary.isBlank()) {
			String systemPrompt = """
					You are an AI assistant.
					Given the following two-paragraph excerpt from a whitepaper,
					generate a concise, informative subtitle (5–8 words) capturing its essence.
					""";
			ChatRequest req = new ChatRequest(List.of("gpt-3.5-turbo"),
					List.of(new Message("system", systemPrompt), new Message("user", summary)), 0, // temperature
					50, // max_tokens
					1.0, // top_p
					0.0, // freq_penalty
					0.0 // presence_penalty
			);
			ChatResponse resp = client.post().bodyValue(req).retrieve().bodyToMono(ChatResponse.class).block();
			if (resp != null && resp.choices() != null && !resp.choices().isEmpty()) {
				subtitle = resp.choices().get(0).message().content().trim();
			}
			// fallback to first sentence if model returns empty
			if (subtitle.isEmpty()) {
				String[] sents = summary.split("\\.\\s+");
				if (sents.length > 0) {
					subtitle = sents[0].trim() + ".";
				}
			}
		}

		// 6) Fallbacks
		if (title.isBlank())
			title = "Untitled";
		if (summary.isBlank())
			summary = "";
		// subtitle may remain empty if generation failed

		return Map.of("title", title, "subtitle", subtitle, "summary", summary);
	}

	// Helper methods: cleanHeadersFooters(...) and extractTwoParagraphs(...) as
	// shown above.
	private String cleanHeadersFooters(String text) {
		return Arrays.stream(text.split("\\r?\\n"))
				// drop lines containing URLs, emails, or phone-like patterns
				.filter(line -> !line.matches(".*(https?://|www\\.|@|\\d{3}[- ]?\\d{3}[- ]?\\d{4}).*"))
				// drop very short lines unlikely to be body copy
				.filter(line -> line.trim().length() > 30).collect(Collectors.joining("\n"));
	}

	private String extractTitleFromFirstPage(PDDocument document) throws IOException {
		TitleTextStripper tStripper = new TitleTextStripper();
		tStripper.setStartPage(1);
		tStripper.setEndPage(1);
		tStripper.getText(document);
		return tStripper.getTitleText();
	}

	private String extractSubtitleFromFirstPage(PDDocument document, String title) throws IOException {
		PDFTextStripper stripper = new PDFTextStripper();
		stripper.setStartPage(1);
		stripper.setEndPage(1);
		String pageText = stripper.getText(document).trim();
		String[] lines = pageText.split("\\r?\\n");
		for (int i = 0; i < lines.length - 1; i++) {
			if (lines[i].trim().equals(title)) {
				return lines[i + 1].trim();
			}
		}
		return "";
	}

	private String extractTwoParagraphs(String text) {
		String[] paragraphs = text.split("\\r?\\n\\r?\\n");
		StringBuilder summaryBuilder = new StringBuilder();
		int count = 0;
		for (String para : paragraphs) {
			if (!para.isBlank()) {
				summaryBuilder.append(para.trim()).append("\n\n");
				count++;
				if (count == 2)
					break;
			}
		}
		return summaryBuilder.toString().trim();
	}

	class TitleTextStripper extends PDFTextStripper {
		private List<TextPosition> textPositions = new ArrayList<>();

		public TitleTextStripper() throws IOException {
			super();
		}

		@Override
		protected void processTextPosition(TextPosition text) {
			textPositions.add(text);
			super.processTextPosition(text);
		}

		public String getTitleText() {
			Map<Float, StringBuilder> fontSizeToText = new HashMap<>();
			for (TextPosition text : textPositions) {
				float fontSize = text.getFontSizeInPt();
				fontSizeToText.computeIfAbsent(fontSize, k -> new StringBuilder()).append(text.getUnicode());
			}
			float maxFontSize = fontSizeToText.keySet().stream().max(Float::compare).orElse(0f);
			return fontSizeToText.getOrDefault(maxFontSize, new StringBuilder("")).toString().trim();
		}
	}

	/** 5️⃣ Company → About/Description */
	public String getCompanyDescription(String companyName) {
		String apiKey = "AIzaSyDujNCRmNQk3jzSfE2xJ6LCVT2auZQHxn0"; // Replace with your API key
		String cx = "469a8354f1e68489b"; // Replace with your Search Engine ID

		String query = companyName + " company overview";

		try {
			String urlStr = "https://www.googleapis.com/customsearch/v1?key=" + apiKey + "&cx=" + cx + "&q="
					+ URLEncoder.encode(query, "UTF-8");
			String jsonResponse = sendGetRequest(urlStr);
			JSONObject json = new JSONObject(jsonResponse);
			if (json.has("items")) {
				JSONArray items = json.getJSONArray("items");
				for (int i = 0; i < items.length(); i++) {
					JSONObject item = items.getJSONObject(i);
					String snippet = item.getString("snippet");
					if (snippet != null && !snippet.isEmpty()) {
						return snippet;
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "No description available.";
	}

	private static final List<String> predefinedTopics = Arrays.asList("Accounts Payable", "Active Directory",
			"Application Performance Management", "Application Security", "Automation Management", "Backup", "BI",
			"Big Data", "Business Intelligence", "Business IT", "Call Center", "Cloud", "Cloud communications",
			"Cloud Security", "Cloud Video Conferencing", "Collaboration", "Contact Center", "Container Platform",
			"Containers and Cloud Native", "Content Delivery Network", "CRM", "Customer Engagement", "Data Center",
			"Database Management", "Desktops, Notebooks & Operating Systems", "DevOps", "Digital Workspace",
			"Disaster Recovery", "Document Management", "E-Signature", "eCommerce", "Educational Technology",
			"Email Marketing", "Email Security", "Employee Engagement", "Encryption", "Endpoint Security",
			"Enterprise Application", "Enterprise Content Management", "Enterprise Mobility Management (EMM)",
			"Entertainment Services", "ERP", "Finance & Data Management", "FinTech", "Flash Storage", "Green IT",
			"Health IT", "Healthcare", "Healthcare Technology", "HR Technology", "Hyperconvergence",
			"Identity Management", "Internet Of Things (IoT)", "inventory management software", "IT Helpdesk",
			"IT Infrastructure", "IT Infrastructure and Management", "IT Management", "IT Operations Management",
			"IT Security & Compliance", "IT service management", "Learning Management System (LMS)",
			"Logistics Management", "Marketing Analysis", "Marketing Dispruption", "Marketing Technology",
			"Mobile & Wireless", "Mobile Computing", "Network Monitoring", "Networking & Communications", "Open Source",
			"Outsourcing", "Payroll", "Payroll Management", "Physical Security", "POS", "Project Management",
			"Ransomware", "Recruiting", "Remote Management", "Sales Enablement", "Sandbox",
			"SIEM (Security Information and Event Management)", "SMB Technology", "Social Media Management",
			"Software and Web Development", "Software Defined-Networking", "Software Development",
			"Software Management", "SSL", "Storage", "Talent Management", "Threat Detection", "Unified Communications",
			"Virtualization", "VoIP", "Web Conferencing", "Wireless Networking", "Workload Automation");

	public String classifyTopics(String fullText) {
		// System-level instruction for classification
		String systemPrompt = "You are an AI assistant trained to classify whitepapers into specific topics. Choose the most relevant topic from the provided list based on the content.";

		// User-level instruction with the whitepaper content and topic list
		String userPrompt = "Given the following whitepaper content:\n\n" + fullText
				+ "\n\nSelect the most appropriate topic from the list:\n" + String.join(", ", predefinedTopics);

		// Constructing the ChatRequest with the new prompts
		ChatRequest body = new ChatRequest(List.of("gpt-3.5-turbo"), // Replace with your chosen model
				List.of(new Message("system", systemPrompt), new Message("user", userPrompt)), 0, 20, 1.0, 0.0, 0.0);

		// Sending the request and processing the response
		ChatResponse resp = client.post().bodyValue(body).exchangeToMono(response -> {
			if (response.statusCode().is2xxSuccessful()) {
				return response.bodyToMono(ChatResponse.class);
			} else {
				return response.bodyToMono(String.class).flatMap(err -> {
					System.err.println("Topic classification error " + response.statusCode() + ": " + err);
					return Mono.just(new ChatResponse(Collections.emptyList()));
				});
			}
		}).block();

		// Returning the classified topic or defaulting to "General" if no
		// classification is found
		if (resp == null || resp.choices() == null || resp.choices().isEmpty()) {
			return "General";
		}
		return resp.choices().get(0).message().content().trim();
	}

	/** Helper: safely pull a field out of JSON text */
	private String extractValue(String json, String key) {
		try {
			json = json.replaceAll("(?s)```json|```", "").trim();
			ObjectMapper m = new ObjectMapper();
			JsonNode n = m.readTree(json);
			return n.has(key) ? n.get(key).asText() : "";
		} catch (Exception e) {
			return "";
		}
	}

	// ─── JSON DTOs ─────────────────────────────────────────────────────
	public static record ChatRequest(List<String> models, List<Message> messages, double temperature, int max_tokens,
			double top_p, double frequency_penalty, double presence_penalty) {
	}

	public static record ChatResponse(List<Choice> choices) {
	}

	public static record Choice(Message message) {
	}

	public static record Message(String role, String content) {
	}
}
