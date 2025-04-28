package com.PdfExtractor.Config;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import java.security.GeneralSecurityException;
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;

@Configuration
public class GoogleDriveConfig {

	private static final List<String> SCOPES = List.of(DriveScopes.DRIVE_FILE);

	@Bean
	public Drive driveService() throws GeneralSecurityException, IOException {
		// load the very-same filename under resources/
		try (InputStream in = getClass().getClassLoader().getResourceAsStream("drive-service-account.json")) {
			if (in == null) {
				throw new FileNotFoundException("drive-service-account.json not found on classpath");
			}
			GoogleCredentials creds = GoogleCredentials.fromStream(in).createScoped(SCOPES);

			return new Drive.Builder(GoogleNetHttpTransport.newTrustedTransport(), JacksonFactory.getDefaultInstance(),
					new HttpCredentialsAdapter(creds)).setApplicationName("WhitepaperUploader").build();
		}
	}
}
