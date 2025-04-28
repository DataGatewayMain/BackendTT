package com.PdfExtractor.Repo;

import org.springframework.data.jpa.repository.JpaRepository;

import com.PdfExtractor.Entity.FormSubmission;
import com.PdfExtractor.Entity.Whitepaper;

public interface FormSubmissionRepository extends JpaRepository<FormSubmission, Long> {

}
