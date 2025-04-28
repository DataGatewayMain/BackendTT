package com.PdfExtractor.Service;

import java.nio.charset.StandardCharsets;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.util.UriUtils;
import org.springframework.web.client.RestTemplate;

@Service
public class CountryResolver {
	private final RestTemplate rest = new RestTemplate();

	public String resolveCountryCode(String input) {
		if (input == null)
			return null;
		String candidate = input.trim();
		if (candidate.length() == 2) {
			return candidate.toUpperCase();
		}
		String url = "https://restcountries.com/v3.1/name/"
				+ UriUtils.encodePathSegment(candidate, StandardCharsets.UTF_8);
		try {
			var response = rest.getForObject(url, Country[].class);
			if (response != null && response.length > 0) {
				return response[0].getCca2();
			}
		} catch (HttpClientErrorException e) {
			// no match
		}
		return null;
	}

	// Inner DTO for JSON binding
	public static class Country {
		private Cca name;
		private String cca2;

		public String getCca2() {
			return cca2;
		}

		public void setCca2(String cca2) {
			this.cca2 = cca2;
		}

		public Cca getName() {
			return name;
		}

		public void setName(Cca name) {
			this.name = name;
		}

		public static class Cca {
			private String common;

			public String getCommon() {
				return common;
			}

			public void setCommon(String common) {
				this.common = common;
			}
		}
	}
}
