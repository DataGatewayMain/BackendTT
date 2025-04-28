package com.PdfExtractor.Repo;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.PdfExtractor.Entity.Whitepaper;

@Repository
public interface WhitepaperRepository extends JpaRepository<Whitepaper, Long> {
	Optional<Whitepaper> findFirstByCompanyNameIgnoreCase(String companyName);

	Whitepaper findByCompanyName(String companyName);

//	List<Whitepaper> findByCompanyNameIgnoreCase(String companyName);
	 Optional<Whitepaper> findByCompanyNameIgnoreCase(String companyName);
}
