package com.PdfExtractor.Controller;

import java.io.IOException;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import com.PdfExtractor.Entity.Whitepaper;
import com.PdfExtractor.Service.FormSubmissionService;
import com.PdfExtractor.Service.GoogleDriveService;
import com.PdfExtractor.Service.WhitepaperService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = { "http://localhost:4200", "http://localhost:53719", "http://192.168.0.5:4200",
		"http://192.168.0.6:4200", "http://192.168.0.7:4200", "https://test.vectordb.app", "https://vectordb.app",
		"https://web.vectordb.app" }, allowedHeaders = "*", methods = { RequestMethod.GET, RequestMethod.POST,
				RequestMethod.PUT, RequestMethod.DELETE }, allowCredentials = "true", maxAge = 3600)
public class WhitepaperController {

	@Autowired
	private WhitepaperService whitepaperService;

	// ← inject your wrapper, not the raw Drive client
	@Autowired
	private GoogleDriveService driveService;

	@PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<Whitepaper> uploadWhitepaper(@RequestParam("file") MultipartFile file,
			@RequestParam("companyName") String companyName,
			@RequestParam(value = "customQuestions", required = false) String customQuestions) {
		try {
			Whitepaper savedWp = whitepaperService.processAndStoreWhitepaper(file, companyName, customQuestions);
			return ResponseEntity.ok(savedWp);
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.badRequest().build();
		}
	}

	@GetMapping("/{id}/download")
	public void streamPdf(@PathVariable Long id, HttpServletResponse resp) throws IOException {
		Whitepaper wp = whitepaperService.getWhitepaperById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

		resp.setContentType("application/pdf");
		// use your service method, which handles the Drive call + permissions
		driveService.downloadPdf(wp.getPdfFileId(), resp.getOutputStream());
	}

	// New endpoint to retrieve a whitepaper by ID
	@GetMapping("/{id}")
	public ResponseEntity<Whitepaper> getWhitepaperById(@PathVariable Long id) {
		Optional<Whitepaper> whitepaper = whitepaperService.getWhitepaperById(id);
		return whitepaper.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
	}

}
