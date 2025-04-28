package com.PdfExtractor.Service;

import org.springframework.stereotype.Service;

import com.PdfExtractor.Entity.SubscriptionForm;
import com.PdfExtractor.Repo.SubscriptionFormRepository;
import com.PdfExtractor.dto.SubscriptionFormRequest;

import org.springframework.beans.factory.annotation.Autowired;

@Service
public class SubscriptionFormService {

	@Autowired
	private SubscriptionFormRepository repo;

	public SubscriptionForm saveForm(SubscriptionFormRequest request) {
		SubscriptionForm form = new SubscriptionForm();
		form.setFirstName(request.getFirstName());
		form.setLastName(request.getLastName());
		form.setEmail(request.getEmail());
		form.setConsent(request.isConsent());

		return repo.save(form);
	}
}
