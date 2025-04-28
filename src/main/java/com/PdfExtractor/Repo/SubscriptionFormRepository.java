package com.PdfExtractor.Repo;

import org.springframework.data.jpa.repository.JpaRepository;

import com.PdfExtractor.Entity.SubscriptionForm;

public interface SubscriptionFormRepository extends JpaRepository<SubscriptionForm, Long> {
}
