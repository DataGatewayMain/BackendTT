package com.PdfExtractor.Service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.MimeMessageHelper;

@Service
public class EmailService {

	private final JavaMailSender mailSender;
	private final String fromAddress;

	public EmailService(JavaMailSender mailSender, @Value("${spring.mail.username}") String fromAddress) {
		this.mailSender = mailSender;
		this.fromAddress = fromAddress;
	}

	public void sendHtmlEmail(String to, String userName, String subject, String linkUrl) throws MessagingException {
		MimeMessage message = mailSender.createMimeMessage();
		MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

		helper.setFrom(String.format("\"%s\" <%s>", "Technology Trends", fromAddress));
		helper.setTo(to);
		helper.setSubject(subject);

		String htmlContent = """
				<div style="font-family: Arial, sans-serif; padding: 20px; background-color: #f9f9f9;">
				  <div style="max-width: 600px; margin: auto; background: white; border-radius: 10px; overflow: hidden; box-shadow: 0 0 10px rgba(0,0,0,0.1);">
				    <div style="background-color: #008CBA; padding: 20px; color: white; text-align: center;">
				      <h2>Download Your Content</h2>
				    </div>
				    <div style="padding: 30px; text-align: center;">
				      <p>Dear %s,</p>
				      <p>Thank you for your interest in our content. Please click the button below to download your exclusive copy.</p>
				      <a href="%s" style="display: inline-block; padding: 12px 20px; margin: 20px 0; font-size: 16px; color: white; background-color: #008CBA; text-decoration: none; border-radius: 5px;">Download Content</a>
				      <p>If the button does not work, copy and paste this link into your browser:</p>
				      <p><a href="%s">%s</a></p>
				    </div>
				    <div style="background-color: #f1f1f1; padding: 10px; text-align: center; font-size: 12px; color: #777;">
				      © 2025 Your Company. All rights reserved.
				    </div>
				  </div>
				</div>
				"""
				.formatted(userName, linkUrl, linkUrl, linkUrl);

		helper.setText(htmlContent, true);
		mailSender.send(message);
	}

}
