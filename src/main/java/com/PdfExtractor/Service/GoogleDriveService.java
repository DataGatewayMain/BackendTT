package com.PdfExtractor.Service;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.google.api.client.http.AbstractInputStreamContent;
import com.google.api.client.http.InputStreamContent;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.Permission;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;

@Service
public class GoogleDriveService {

	private static final List<String> SCOPES = Collections.singletonList(DriveScopes.DRIVE_FILE);
	private final Drive drive;

	public GoogleDriveService() throws GeneralSecurityException, IOException {
		// 1) Load service account credentials from classpath
		InputStream jsonKeyStream = getClass().getClassLoader().getResourceAsStream("drive-service-account.json");
		if (jsonKeyStream == null) {
			throw new IOException("drive-service-account.json not found on classpath");
		}

		GoogleCredentials creds = GoogleCredentials.fromStream(jsonKeyStream).createScoped(SCOPES);

		// 2) Build the Drive client
		this.drive = new Drive.Builder(GoogleNetHttpTransport.newTrustedTransport(),
				JacksonFactory.getDefaultInstance(), new HttpCredentialsAdapter(creds))
				.setApplicationName("WhitepaperUploader").build();
	}

	/**
	 * Uploads a PDF file to Google Drive and makes it publicly readable.
	 * 
	 * @param file the incoming MultipartFile (PDF)
	 * @return the Drive file ID
	 */
	public String uploadPdf(MultipartFile file) throws IOException {
		// Metadata (name & optional parent folder)
		File metadata = new File().setName(file.getOriginalFilename());
		// .setParents(Collections.singletonList("your-folder-id"));

		// Content
		AbstractInputStreamContent content = new InputStreamContent("application/pdf", file.getInputStream());

		// Upload request
		File uploaded = drive.files().create(metadata, content).setFields("id, webViewLink, webContentLink").execute();

		// Make it publicly readable
		Permission perm = new Permission().setType("anyone").setRole("reader");
		drive.permissions().create(uploaded.getId(), perm).execute();

		return uploaded.getId();
	}

	/**
	 * Streams a PDF from Drive to the provided OutputStream.
	 * 
	 * @param fileId the Drive file ID
	 * @param out    the servlet or file OutputStream
	 */
	public void downloadPdf(String fileId, OutputStream out) throws IOException {
		drive.files().get(fileId).executeMediaAndDownloadTo(out);
	}

	/**
	 * Optionally retrieve the public “view” link to embed or share.
	 */
	public String getViewLink(String fileId) throws IOException {
		File f = drive.files().get(fileId).setFields("webViewLink").execute();
		return f.getWebViewLink();
	}
}
