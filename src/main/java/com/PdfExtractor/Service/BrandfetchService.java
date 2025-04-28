package com.PdfExtractor.Service;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.PdfExtractor.Entity.Whitepaper;
import com.PdfExtractor.Repo.WhitepaperRepository;

import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Optional;

@Service
public class BrandfetchService {

	@Value("${brandfetch.api.key}")
	private String apiKey;

	public String getDomainForCompany(String companyName) {
		try {
			String query = companyName.replace(" ", "%20");
			URL url = new URL("https://brandfetch.com/api/v2/search/" + query);

			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			conn.setRequestProperty("Authorization", "Bearer " + apiKey);

			BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			StringBuilder responseBuilder = new StringBuilder();
			String inputLine;

			while ((inputLine = in.readLine()) != null) {
				responseBuilder.append(inputLine);
			}
			in.close();

			JSONArray arr = new JSONArray(responseBuilder.toString());
			if (!arr.isEmpty()) {
				JSONObject first = arr.getJSONObject(0);
				return first.getString("domain");
			}
		} catch (Exception e) {
			System.out.println("Brandfetch domain fetch failed: " + e.getMessage());
		}

		return null;
	}

	public String getLogoUrlForDomain(String domain) {
		try {
			URL url = new URL("https://api.brandfetch.io/v2/brands/" + domain);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			conn.setRequestProperty("Authorization", "Bearer " + apiKey);

			BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			StringBuilder responseBuilder = new StringBuilder();
			String line;
			while ((line = in.readLine()) != null) {
				responseBuilder.append(line);
			}
			in.close();

			JSONObject obj = new JSONObject(responseBuilder.toString());
			JSONArray logos = obj.optJSONArray("logos");

			if (logos != null) {
				// Step 1: Prefer logo type and SVG format
				for (int i = 0; i < logos.length(); i++) {
					JSONObject logo = logos.getJSONObject(i);
					if ("logo".equalsIgnoreCase(logo.optString("type"))) {
						JSONArray formats = logo.optJSONArray("formats");
						if (formats != null) {
							for (int j = 0; j < formats.length(); j++) {
								JSONObject format = formats.getJSONObject(j);
								if ("svg".equalsIgnoreCase(format.optString("format"))) {
									return format.optString("src");
								}
							}
						}
					}
				}

				// Step 2: Fallback - get *any* logo, even PNG, etc.
				for (int i = 0; i < logos.length(); i++) {
					JSONObject logo = logos.getJSONObject(i);
					if ("logo".equalsIgnoreCase(logo.optString("type"))) {
						JSONArray formats = logo.optJSONArray("formats");
						if (formats != null && formats.length() > 0) {
							return formats.getJSONObject(0).optString("src");
						}
					}
				}
			}

		} catch (Exception e) {
			System.out.println("Error fetching logo: " + e.getMessage());
		}

		return null;
	}

}

//**********Working code below**********
//@Service
//public class BrandfetchService {
//
//    @Value("${brandfetch.api.key}")
//    private String apiKey;
//
//    @Autowired
//    private WhitepaperRepository whitepaperRepository;
//
//    public String getLogoUrlForCompany(String companyName) {
//        // Step 1: Check the database
//        Optional<Whitepaper> optionalWhitepaper = whitepaperRepository.findByCompanyNameIgnoreCase(companyName);
//        if (optionalWhitepaper.isPresent()) {
//            String logoUrl = optionalWhitepaper.get().getLogoUrl();
//            if (logoUrl != null && !logoUrl.isEmpty()) {
//                return logoUrl;
//            }
//        }
//
//        // Step 2: Fetch from Brandfetch
//        String domain = getDomainForCompany(companyName);
//        if (domain != null) {
//            return getLogoUrlForDomain(domain);
//        }
//
//        return null;
//    }
//
//    public String getDomainForCompany(String companyName) {
//        try {
//            String query = companyName.replace(" ", "%20");
//            URL url = new URL("https://brandfetch.com/api/v2/search/" + query);
//            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
//            conn.setRequestMethod("GET");
//            conn.setRequestProperty("Authorization", "Bearer " + apiKey);
//
//            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
//            StringBuilder responseBuilder = new StringBuilder();
//            String inputLine;
//            while ((inputLine = in.readLine()) != null) {
//                responseBuilder.append(inputLine);
//            }
//            in.close();
//
//            JSONArray arr = new JSONArray(responseBuilder.toString());
//            if (!arr.isEmpty()) {
//                JSONObject first = arr.getJSONObject(0);
//                return first.getString("domain");
//            }
//        } catch (Exception e) {
//            System.out.println("Brandfetch domain fetch failed: " + e.getMessage());
//        }
//        return null;
//    }
//
//    public String getLogoUrlForDomain(String domain) {
//        try {
//            URL url = new URL("https://api.brandfetch.io/v2/brands/" + domain);
//            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
//            conn.setRequestMethod("GET");
//            conn.setRequestProperty("Authorization", "Bearer " + apiKey);
//
//            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
//            StringBuilder responseBuilder = new StringBuilder();
//            String line;
//            while ((line = in.readLine()) != null) {
//                responseBuilder.append(line);
//            }
//            in.close();
//
//            JSONObject obj = new JSONObject(responseBuilder.toString());
//            JSONArray logos = obj.optJSONArray("logos");
//
//            if (logos != null) {
//                // Step 1: Prefer logo type and SVG format
//                for (int i = 0; i < logos.length(); i++) {
//                    JSONObject logo = logos.getJSONObject(i);
//                    if ("logo".equalsIgnoreCase(logo.optString("type"))) {
//                        JSONArray formats = logo.optJSONArray("formats");
//                        if (formats != null) {
//                            for (int j = 0; j < formats.length(); j++) {
//                                JSONObject format = formats.getJSONObject(j);
//                                if ("svg".equalsIgnoreCase(format.optString("format"))) {
//                                    return format.optString("src");
//                                }
//                            }
//                        }
//                    }
//                }
//                // Step 2: Fallback - get any logo
//                for (int i = 0; i < logos.length(); i++) {
//                    JSONObject logo = logos.getJSONObject(i);
//                    if ("logo".equalsIgnoreCase(logo.optString("type"))) {
//                        JSONArray formats = logo.optJSONArray("formats");
//                        if (formats != null && formats.length() > 0) {
//                            return formats.getJSONObject(0).optString("src");
//                        }
//                    }
//                }
//            }
//        } catch (Exception e) {
//            System.out.println("Error fetching logo: " + e.getMessage());
//        }
//        return null;
//    }
//}